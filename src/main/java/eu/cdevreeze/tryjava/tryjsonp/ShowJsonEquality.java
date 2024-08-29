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

import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Given a sample Json string, this program shows that equality (for JsonValue, overriding "Object.equals")
 * works as expected. JsonArray equality is Java List equality, and JsonObject equality is Java Map equality.
 * <p>
 * Note that most static "Json" value creation methods are avoided for performance reasons. See
 * <a href="https://github.com/quarkusio/quarkus/issues/42748">Json-P performance issue</a>.
 *
 * @author Chris de Vreeze
 */
public class ShowJsonEquality {

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
        JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(Map.of());
        JsonWriterFactory jsonWriterFactory =
                Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE));

        JsonObject jsonObject = jsonReaderFactory.createReader(new StringReader(JSON_STRING)).readObject();

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(jsonObject);
        String jsonString = sw.toString();

        System.out.printf("Processing JSON (pretty-printed):%n%s%n", jsonString);

        showEqualitiesAndInequalities(jsonObject, jsonWriterFactory);
    }

    public static void showEqualitiesAndInequalities(JsonObject jsonObject, JsonWriterFactory jsonWriterFactory) {
        showObjectEntryOrderIsIrreleventForEquality(jsonObject, jsonWriterFactory);
        showArrayElementOrderMattersForEquality(jsonObject, jsonWriterFactory);
        showUpdatingJsonBreaksEquality(jsonObject, jsonWriterFactory);
        showAddingDataToJsonBreaksEquality(jsonObject, jsonWriterFactory);
        showAddingJsonNullToJsonObjectBreaksEquality(jsonObject, jsonWriterFactory);
        showAddingJsonNullToJsonArrayBreaksEquality(jsonObject, jsonWriterFactory);
        showRemovingDataFromJsonBreaksEquality(jsonObject, jsonWriterFactory);
        showRoundtrippingDoesNotBreakEquality(jsonObject, jsonWriterFactory);
        showChangingObjectEntryOrderEverywhereDoesNotBreakEquality(jsonObject, jsonWriterFactory);
    }

    public static void showObjectEntryOrderIsIrreleventForEquality(JsonObject jsonObject, JsonWriterFactory jsonWriterFactory) {
        System.out.println();
        System.out.println("Changing key order in (address) Json object:");
        System.out.println();

        JsonBuilderFactory bf = Json.createBuilderFactory(Map.of());

        JsonObject addressObj = bf.createObjectBuilder()
                .add("state", "NY")
                .add("city", "New York")
                .add("postalCode", "10021")
                .add("streetAddress", "21 2nd Street")
                .build();

        JsonPatch patch = Json.createPatchBuilder()
                .replace("/address", addressObj)
                .build();
        JsonObject editedObj = patch.apply(jsonObject);

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(editedObj);
        String jsonString = sw.toString();

        System.out.printf("Edited Json:%n%s%n", jsonString);
        System.out.println();
        System.out.printf("These Json objects are equal according to Object.equals (expecting true): %b%n", jsonObject.equals(editedObj));
    }

    public static void showArrayElementOrderMattersForEquality(JsonObject jsonObject, JsonWriterFactory jsonWriterFactory) {
        System.out.println();
        System.out.println("Changing order in (phoneNumber) Json array:");
        System.out.println();

        JsonPatch patch = Json.createPatchBuilder()
                .move("/phoneNumber/0", "/phoneNumber/1")
                .build();
        JsonObject editedObj = patch.apply(jsonObject);

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(editedObj);
        String jsonString = sw.toString();

        System.out.printf("Edited Json:%n%s%n", jsonString);
        System.out.println();
        System.out.printf("These Json objects are equal according to Object.equals (expecting false): %b%n", jsonObject.equals(editedObj));
    }

    public static void showUpdatingJsonBreaksEquality(JsonObject jsonObject, JsonWriterFactory jsonWriterFactory) {
        System.out.println();
        System.out.println("Updating Json object:");
        System.out.println();

        JsonPatch patch = Json.createPatchBuilder()
                .replace("/phoneNumber/0/type", "home phone")
                .build();
        JsonObject editedObj = patch.apply(jsonObject);

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(editedObj);
        String jsonString = sw.toString();

        System.out.printf("Edited Json:%n%s%n", jsonString);
        System.out.println();
        System.out.printf("These Json objects are equal according to Object.equals (expecting false): %b%n", jsonObject.equals(editedObj));
    }

    public static void showAddingDataToJsonBreaksEquality(JsonObject jsonObject, JsonWriterFactory jsonWriterFactory) {
        System.out.println();
        System.out.println("Adding data to Json object:");
        System.out.println();

        JsonPatch patch = Json.createPatchBuilder()
                .add("/phoneNumber/0/description", "Home phone")
                .build();
        JsonObject editedObj = patch.apply(jsonObject);

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(editedObj);
        String jsonString = sw.toString();

        System.out.printf("Edited Json:%n%s%n", jsonString);
        System.out.println();
        System.out.printf("These Json objects are equal according to Object.equals (expecting false): %b%n", jsonObject.equals(editedObj));
    }

    public static void showAddingJsonNullToJsonObjectBreaksEquality(JsonObject jsonObject, JsonWriterFactory jsonWriterFactory) {
        System.out.println();
        System.out.println("Adding Json null to (Json object in) Json object:");
        System.out.println();

        JsonPatch patch = Json.createPatchBuilder()
                .add("/phoneNumber/0/description", JsonValue.NULL)
                .build();
        JsonObject editedObj = patch.apply(jsonObject);

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(editedObj);
        String jsonString = sw.toString();

        System.out.printf("Edited Json:%n%s%n", jsonString);
        System.out.println();
        System.out.printf("These Json objects are equal according to Object.equals (expecting false): %b%n", jsonObject.equals(editedObj));
    }

    public static void showAddingJsonNullToJsonArrayBreaksEquality(JsonObject jsonObject, JsonWriterFactory jsonWriterFactory) {
        System.out.println();
        System.out.println("Adding Json null to (Json array in) Json object:");
        System.out.println();

        JsonPatch patch = Json.createPatchBuilder()
                .add("/phoneNumber/2", JsonValue.NULL)
                .build();
        JsonObject editedObj = patch.apply(jsonObject);

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(editedObj);
        String jsonString = sw.toString();

        System.out.printf("Edited Json:%n%s%n", jsonString);
        System.out.println();
        System.out.printf("These Json objects are equal according to Object.equals (expecting false): %b%n", jsonObject.equals(editedObj));
    }

    public static void showRemovingDataFromJsonBreaksEquality(JsonObject jsonObject, JsonWriterFactory jsonWriterFactory) {
        System.out.println();
        System.out.println("Removing data from Json object:");
        System.out.println();

        JsonPatch patch = Json.createPatchBuilder()
                .remove("/phoneNumber/1/type")
                .build();
        JsonObject editedObj = patch.apply(jsonObject);

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(editedObj);
        String jsonString = sw.toString();

        System.out.printf("Edited Json:%n%s%n", jsonString);
        System.out.println();
        System.out.printf("These Json objects are equal according to Object.equals (expecting false): %b%n", jsonObject.equals(editedObj));
    }

    public static void showRoundtrippingDoesNotBreakEquality(JsonObject jsonObject, JsonWriterFactory jsonWriterFactory) {
        System.out.println();
        System.out.println("Roundtripping (writing and parsing back again):");
        System.out.println();

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(jsonObject);
        String jsonString = sw.toString();

        JsonObject jsonObj2 = Json.createReader(new StringReader(jsonString)).readObject();

        System.out.printf("These Json objects are equal according to Object.equals (expecting true): %b%n", jsonObject.equals(jsonObj2));
    }

    public static void showChangingObjectEntryOrderEverywhereDoesNotBreakEquality(JsonObject jsonObject, JsonWriterFactory jsonWriterFactory) {
        System.out.println();
        System.out.println("Changing key order everywhere in Json object (but not in Json arrays):");
        System.out.println();

        JsonBuilderFactory bf = Json.createBuilderFactory(Map.of());

        JsonObject jsonObj2 = bf.createObjectBuilder()
                .add("address", bf.createObjectBuilder()
                        .add("state", "NY")
                        .add("city", "New York")
                        .add("postalCode", "10021")
                        .add("streetAddress", "21 2nd Street")
                )
                .add("lastName", "Smith")
                .add("phoneNumber", bf.createArrayBuilder()
                        .add(bf.createObjectBuilder()
                                .add("number", "212 555-1234")
                                .add("type", "home")
                        )
                        .add(bf.createObjectBuilder()
                                .add("number", "646 555-4567")
                                .add("type", "fax")
                        )
                )
                .add("age", 25)
                .add("firstName", "John")
                .build();

        var sw = new StringWriter();
        jsonWriterFactory.createWriter(sw).writeObject(jsonObj2);
        String jsonString = sw.toString();

        System.out.printf("Edited Json:%n%s%n", jsonString);
        System.out.println();
        System.out.printf("These Json objects are equal according to Object.equals (expecting true): %b%n", jsonObject.equals(jsonObj2));
    }
}
