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

import jakarta.json.JsonObject;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriterFactory;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jakarta.json.stream.JsonCollectors.toJsonObject;

/**
 * Given a sample Json string, this program shows JSON querying using JSON-P in Java Stream pipelines.
 * Maybe it does not look like that at the surface, but JSON-P with the Java Stream API combined make up
 * a decent JSON query API.
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

        System.out.printf("Querying JSON (pretty-printed):%n%s%n", jsonString);

        List<String> homeNumbers = findHomeNumbersOfJohnSmith(jsonObject);

        System.out.println();
        System.out.printf(
                "Home numbers of John Smith: %s%n",
                String.join(", ", homeNumbers)
        );

        List<String> allNumbers = findPhoneNumbersOfJohnSmith(jsonObject);

        System.out.println();
        System.out.printf(
                "All numbers of John Smith: %s%n",
                String.join(", ", allNumbers)
        );

        Map<String, String> numberMap = findPhoneNumbersOfJohnSmithAsMap(jsonObject);

        System.out.println();
        System.out.printf(
                "All numbers of John Smith (again): %s%n",
                numberMap
        );
    }

    public static List<String> findHomeNumbersOfJohnSmith(JsonObject jsonObject) {
        return jsonObject.getJsonArray("phoneNumber")
                .stream()
                .map(JsonValue::asJsonObject)
                .filter(phoneNumber -> phoneNumber.getString("type").equals("home"))
                .map(phoneNumber -> phoneNumber.getString("number"))
                .toList();
    }

    public static List<String> findPhoneNumbersOfJohnSmith(JsonObject jsonObject) {
        return jsonObject.getJsonArray("phoneNumber")
                .stream()
                .map(JsonValue::asJsonObject)
                .map(phoneNumber -> phoneNumber.getString("number"))
                .toList();
    }

    public static Map<String, String> findPhoneNumbersOfJohnSmithAsMap(JsonObject jsonObject) {
        return jsonObject.getJsonArray("phoneNumber")
                .stream()
                .collect(
                        toJsonObject(
                                phone -> phone.asJsonObject().getString("type"),
                                phone -> phone.asJsonObject().getJsonString("number")
                        )
                )
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, kv -> kv.getValue().toString()));
    }

    private static String writeJsonObjectToString(JsonObject jsonObj, JsonProvider jsonProvider) {
        JsonWriterFactory jsonWriterFactory =
                jsonProvider.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE));

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(jsonObj);
        return sw.toString();
    }
}
