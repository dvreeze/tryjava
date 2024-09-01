/*
 * Copyright 2024-2024 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.tryjava.tryjsonp;

import eu.cdevreeze.tryjava.tryjsonp.queries.ConcreteJsonQueryApi;
import eu.cdevreeze.tryjava.tryjsonp.queries.JsonQueryApi;
import eu.cdevreeze.tryjava.tryjsonp.queries.JsonQueryResults;
import jakarta.json.JsonObject;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonString;
import jakarta.json.JsonWriterFactory;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Given a sample Json string, this program shows JSON querying in a way that is inspired by XML
 * querying based on XPath axis descendant-or-self.
 *
 * @author Chris de Vreeze
 */
public class ShowJsonQueries {

    // See https://jakarta.ee/specifications/jsonp/2.1/apidocs/jakarta.json/jakarta/json/jsonobject
    private static final String JSON_STRING =
            """
                    {
                        "firstName": "John", "lastName": "Smith", "age": 25,
                        "address" : {
                            "streetAddress": "21 2nd Street",
                            "city": "New York",
                            "state": "NY",
                            "postalCode": "10021"
                        },
                        "phoneNumber": [
                            { "type": "home", "number": "212 555-1234" },
                            { "type": "fax", "number": "646 555-4567" }
                        ]
                    }""";

    public static void main(String[] args) {
        // Avoiding expensive Json static methods, that repeatedly look up the JsonProvider
        JsonProvider jsonProvider = JsonProvider.provider();

        JsonReaderFactory jsonReaderFactory = jsonProvider.createReaderFactory(Map.of());

        JsonObject jsonObject = jsonReaderFactory.createReader(new StringReader(JSON_STRING)).readObject();

        String jsonString = writeJsonObjectToString(jsonObject, jsonProvider);

        JsonQueryApi jq = ConcreteJsonQueryApi.instance();

        System.out.printf("Querying JSON (pretty-printed):%n%s%n", jsonString);

        List<String> homeNumbers = findHomeNumbersOfJohnSmith(jsonObject, jq);

        System.out.println();
        System.out.printf(
                "Home numbers of John Smith: %s%n",
                String.join(", ", homeNumbers)
        );

        List<String> allNumbers = findPhoneNumbersOfJohnSmith(jsonObject, jq);

        System.out.println();
        System.out.printf(
                "All numbers of John Smith: %s%n",
                String.join(", ", allNumbers)
        );
    }

    public static List<String> findHomeNumbersOfJohnSmith(JsonObject jsonObject, JsonQueryApi jq) {
        return jq.jsonArrayStream(jsonObject, "phoneNumber")
                .flatMap(jq::childObjectStream)
                .filter(v ->
                        jq.childStringStream(v, "type")
                                .anyMatch(jsonStr -> jsonStr.jsonString().getString().equals("home"))
                )
                .flatMap(v ->
                        jq.childStringStream(v, "number")
                                .map(JsonQueryResults.JsonStringResult::jsonString)
                                .map(JsonString::getString)
                )
                .toList();
    }

    public static List<String> findPhoneNumbersOfJohnSmith(JsonObject jsonObject, JsonQueryApi jq) {
        return jq.jsonArrayStream(jsonObject, "phoneNumber")
                .flatMap(jq::childObjectStream)
                .flatMap(v ->
                        jq.childStringStream(v, "number")
                                .map(JsonQueryResults.JsonStringResult::jsonString)
                                .map(JsonString::getString)
                )
                .toList();
    }

    private static String writeJsonObjectToString(JsonObject jsonObj, JsonProvider jsonProvider) {
        JsonWriterFactory jsonWriterFactory =
                jsonProvider.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE));

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(jsonObj);
        return sw.toString();
    }
}
