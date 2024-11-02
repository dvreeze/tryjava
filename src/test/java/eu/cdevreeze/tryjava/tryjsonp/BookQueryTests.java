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

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Book query tests.
 *
 * @author Chris de Vreeze
 */
public class BookQueryTests {

    private static final String BOOKSTORE_JSON_STRING =
            """
                    {
                        "bookstore": {
                            "books": [
                                {
                                    "title": "A First Course in Database Systems",
                                    "ISBN": "ISBN-0-13-713526-2",
                                    "price": 85,
                                    "edition": "3rd",
                                    "authors": [
                                        {
                                            "firstName": "Jeffrey",
                                            "lastName": "Ullman"
                                        },
                                        {
                                            "firstName": "Jennifer",
                                            "lastName": "Widom"
                                        }
                                    ]
                                },
                                {
                                    "title": "Database Systems: The Complete Book",
                                    "ISBN": "ISBN-0-13-815504-6",
                                    "price": 100,
                                    "authors": [
                                        {
                                            "firstName": "Hector",
                                            "lastName": "Garcia-Molina"
                                        },
                                        {
                                            "firstName": "Jeffrey",
                                            "lastName": "Ullman"
                                        },
                                        {
                                            "firstName": "Jennifer",
                                            "lastName": "Widom"
                                        }
                                    ],
                                    "remark": "Buy this book bundled with \\"A First Course\\" - a great deal!"
                                },
                                {
                                    "title": "Hector and Jeff's Database Hints",
                                    "ISBN": "ISBN-0-11-222222-3",
                                    "price": 50,
                                    "authors": [
                                        {
                                            "firstName": "Jeffrey",
                                            "lastName": "Ullman"
                                        },
                                        {
                                            "firstName": "Hector",
                                            "lastName": "Garcia-Molina"
                                        }
                                    ],
                                    "remark": "An indispensable companion to your textbook"
                                },
                                {
                                    "title": "Jennifer's Economical Database Hints",
                                    "ISBN": "ISBN-9-88-777777-6",
                                    "price": 25,
                                    "authors": [
                                        {
                                            "firstName": "Jennifer",
                                            "lastName": "Widom"
                                        }
                                    ]
                                }
                            ],
                            "magazines": [
                                {
                                    "title": "National Geographic",
                                    "month": "January",
                                    "year": 2009
                                },
                                {
                                    "title": "National Geographic",
                                    "month": "February",
                                    "year": 2009
                                },
                                {
                                    "title": "Newsweek",
                                    "month": "February",
                                    "year": 2009
                                },
                                {
                                    "title": "Hector and Jeff's Database Hints",
                                    "month": "March",
                                    "year": 2009
                                }
                            ]
                        }
                    }""";

    // Avoiding expensive Json static methods, that repeatedly look up the JsonProvider
    private static final JsonProvider jsonProvider = JsonProvider.provider();

    @Test
    void testQueryAllFieldNames() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        List<String> fieldNames = findAllFieldNames(bookstoreJsonObj).toList();

        assertEquals(List.of(
                "bookstore",
                "books",
                "magazines",
                "title",
                "ISBN",
                "price",
                "edition",
                "authors",
                "firstName",
                "lastName",
                "remark",
                "month",
                "year"
        ), fieldNames);
    }

    // Improve this querying support based on JSON-P and Java Streams a lot. This is not ergonomic!
    // One way to fix this is to first introduce "mirrors" of JsonValue and subtypes. These mirrors
    // would get an API corresponding to the JsonValue (and subtype) API, except that the methods
    // would return Java Streams. So JsonObject.getJsonObject would correspond to method jsonObjectStream,
    // and JsonObject.getJsonArray would correspond to method jsonArrayStream. Likewise, JsonValue.asJsonObject
    // would correspond to method selfAsJsonObjectStream, etc. Some methods taking a field name parameter
    // could be overloaded to take a predicate. Of course, converting between JsonValue and "mirrors"
    // should be trivial. In short, this Stream-based query API would be safe when expected data is missing,
    // and it does not expect single results when traversing JSON. The "mirror" API could either wrap
    // JsonValue objects, or it could be entirely decoupled in memory, which would make conversions costly.

    @Test
    void testQueryNumberOfTitles() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        List<String> titles =
                bookstoreJsonObj.getJsonObject("bookstore").entrySet().stream()
                        .filter(kv -> Set.of("books", "magazines").contains(kv.getKey()))
                        .map(Map.Entry::getValue)
                        .flatMap(v -> asOptionalJsonArray(v).stream())
                        .flatMap(arr -> arr.stream().flatMap(v -> asOptionalJsonObject(v).stream()))
                        .filter(obj -> obj.containsKey("title"))
                        .map(obj -> obj.getString("title"))
                        .toList();

        assertEquals(8L, titles.size());
    }

    @Test
    void testQueryMagazineMonths() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        List<String> magazineMonths =
                bookstoreJsonObj.getJsonObject("bookstore").get("magazines").asJsonArray().stream()
                        .flatMap(v -> asOptionalJsonObject(v).stream())
                        .map(v -> v.getString("month"))
                        .toList();

        assertEquals(
                List.of("January", "February", "February", "March"),
                magazineMonths
        );
    }

    @Test
    void testQueryMagazineTitles() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        List<String> magazineTitles =
                bookstoreJsonObj.getJsonObject("bookstore").getJsonArray("magazines").stream()
                        .flatMap(arr -> asOptionalJsonObject(arr).stream())
                        .filter(obj -> obj.containsKey("title"))
                        .map(obj -> obj.getString("title"))
                        .distinct()
                        .toList();

        assertEquals(
                List.of(
                        "National Geographic",
                        "Newsweek",
                        "Hector and Jeff's Database Hints"
                ),
                magazineTitles
        );
    }

    @Test
    void testQueryBooksCoauthoredByJenniferWidom() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        Predicate<JsonObject> bookCowrittenByJenniferWidom =
                bookJsonObj ->
                        bookJsonObj.getJsonArray("authors").stream()
                                .flatMap(v -> asOptionalJsonObject(v).stream())
                                .anyMatch(v ->
                                        v.getString("firstName").equals("Jennifer") &&
                                                v.getString("lastName").equals("Widom")
                                );

        Set<String> bookTitlesCoauthoredByJenniferWidom =
                bookstoreJsonObj.getJsonObject("bookstore").getJsonArray("books").stream()
                        .flatMap(v -> asOptionalJsonObject(v).stream())
                        .filter(bookCowrittenByJenniferWidom)
                        .map(v -> v.getString("title"))
                        .collect(Collectors.toSet());

        assertEquals(
                Set.of(
                        "A First Course in Database Systems",
                        "Database Systems: The Complete Book",
                        "Jennifer's Economical Database Hints"
                ),
                bookTitlesCoauthoredByJenniferWidom
        );
    }

    @Test
    void testQueryAuthorNames() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        Function<JsonObject, String> getAuthorName =
                authorJsonObj -> {
                    String firstName = authorJsonObj.getString("firstName");
                    String lastName = authorJsonObj.getString("lastName");
                    return String.format("%s %s", firstName, lastName).strip();
                };

        Set<String> authorNames =
                bookstoreJsonObj.getJsonObject("bookstore").getJsonArray("books").stream()
                        .flatMap(v -> asOptionalJsonObject(v).stream())
                        .flatMap(kv -> kv.getJsonArray("authors").stream())
                        .map(JsonValue::asJsonObject)
                        .map(getAuthorName)
                        .collect(Collectors.toSet());

        assertEquals(
                Set.of("Jeffrey Ullman", "Jennifer Widom", "Hector Garcia-Molina"),
                authorNames
        );
    }

    @Test
    void testQueryISBNsOfBooksCoauthoredByJenniferWidom() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        Predicate<JsonObject> bookCowrittenByJenniferWidom =
                bookJsonObj ->
                        bookJsonObj.getJsonArray("authors").stream()
                                .flatMap(v -> asOptionalJsonObject(v).stream())
                                .anyMatch(v ->
                                        v.getString("firstName").equals("Jennifer") &&
                                                v.getString("lastName").equals("Widom")
                                );

        Set<String> bookISBNsCoauthoredByJenniferWidom =
                bookstoreJsonObj.getJsonObject("bookstore").getJsonArray("books").stream()
                        .flatMap(v -> asOptionalJsonObject(v).stream())
                        .filter(bookCowrittenByJenniferWidom)
                        .map(v -> v.getString("ISBN"))
                        .collect(Collectors.toSet());

        assertEquals(
                Set.of("ISBN-0-13-713526-2", "ISBN-0-13-815504-6", "ISBN-9-88-777777-6"),
                bookISBNsCoauthoredByJenniferWidom
        );
    }

    @Test
    void testQueryFebruaryMagazines() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        List<String> februaryMagazineTitles =
                bookstoreJsonObj.getJsonObject("bookstore").get("magazines").asJsonArray().stream()
                        .flatMap(v -> asOptionalJsonObject(v).stream())
                        .filter(v -> v.getString("month").equals("February"))
                        .map(v -> v.getString("title"))
                        .toList();

        assertEquals(
                List.of("National Geographic", "Newsweek"),
                februaryMagazineTitles
        );
    }

    private static Stream<String> findAllFieldNames(JsonObject jsonObject) {
        // Recursive, and mutually recursive with overloaded method
        return Stream.concat(
                jsonObject.keySet().stream(),
                jsonObject.values().stream().flatMap(jsonValue -> {
                    if (jsonValue instanceof JsonObject jsonObj) {
                        return findAllFieldNames(jsonObj);
                    } else if (jsonValue instanceof JsonArray jsonArr) {
                        return findAllFieldNames(jsonArr);
                    } else {
                        return Stream.empty();
                    }
                })
        ).distinct();
    }

    private static Stream<String> findAllFieldNames(JsonArray jsonArray) {
        // Recursive, and mutually recursive with overloaded method
        return jsonArray.stream().flatMap(v -> {
            if (v instanceof JsonObject jsonObj) {
                return findAllFieldNames(jsonObj);
            } else if (v instanceof JsonArray jsonArr) {
                return findAllFieldNames(jsonArr);
            } else {
                return Stream.empty();
            }
        }).distinct();
    }

    private static JsonObject parseBookstoreJson() {
        JsonReaderFactory jsonReaderFactory = jsonProvider.createReaderFactory(Map.of());

        return jsonReaderFactory.createReader(new StringReader(BOOKSTORE_JSON_STRING)).readObject();
    }

    // JSON-P Java Stream helpers

    private static Optional<JsonObject> asOptionalJsonObject(JsonValue jsonValue) {
        return Optional.of(jsonValue).filter(v -> v instanceof JsonObject).map(JsonValue::asJsonObject);
    }

    private static Optional<JsonArray> asOptionalJsonArray(JsonValue jsonValue) {
        return Optional.of(jsonValue).filter(v -> v instanceof JsonArray).map(JsonValue::asJsonArray);
    }
}
