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
import jakarta.json.JsonObject;
import jakarta.json.JsonReaderFactory;
import jakarta.json.spi.JsonProvider;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.cdevreeze.tryjava.tryjsonp.queries.JsonPredicates.isObjectField;
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

    private static final JsonQueryApi jq = ConcreteJsonQueryApi.instance();

    @Test
    void testQueryAllFieldNames() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        List<String> fieldNames = jq.jsonObjectStream(bookstoreJsonObj)
                .flatMap(jq::childJsonStream)
                .flatMap(v -> v.optionalFieldName().stream())
                .distinct()
                .toList();

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

    @Test
    void testQueryNumberOfTitles() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        List<String> titles =
                jq.jsonArrayStream(bookstoreJsonObj)
                        .filter(v -> isObjectField("books").test(v) || isObjectField("magazines").test(v))
                        .flatMap(v -> jq.jsonStringStream(v, "title"))
                        .map(v -> v.jsonString().getString())
                        .toList();

        assertEquals(8L, titles.size());
    }

    @Test
    void testQueryMagazineMonths() {
        JsonObject bookstoreJsonObj = parseBookstoreJson();

        List<String> magazineMonths =
                jq.jsonArrayStream(bookstoreJsonObj, "magazines")
                        .flatMap(v -> jq.jsonStringStream(v, "month"))
                        .map(v -> v.jsonString().getString())
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
                jq.jsonArrayStream(bookstoreJsonObj, "magazines")
                        .flatMap(v -> jq.jsonStringStream(v, "title"))
                        .map(v -> v.jsonString().getString())
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
                        jq.childArrayStream(bookJsonObj, "authors")
                                .flatMap(jq::childObjectStream)
                                .anyMatch(v ->
                                        jq.childStringStream(v, "firstName")
                                                .anyMatch(v2 -> v2.jsonString().getString().equals("Jennifer")) &&
                                                jq.childStringStream(v, "lastName")
                                                        .anyMatch(v2 -> v2.jsonString().getString().equals("Widom"))
                                );

        Set<String> bookTitlesCoauthoredByJenniferWidom =
                jq.jsonArrayStream(bookstoreJsonObj, "books")
                        .flatMap(jq::childObjectStream)
                        .filter(v -> bookCowrittenByJenniferWidom.test(v.jsonObject()))
                        .flatMap(v -> jq.childStringStream(v, "title"))
                        .map(v -> v.jsonString().getString())
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
                    String firstName = jq.childStringStream(authorJsonObj, "firstName")
                            .map(v -> v.jsonString().getString())
                            .findFirst()
                            .orElseThrow();
                    String lastName = jq.childStringStream(authorJsonObj, "lastName")
                            .map(v -> v.jsonString().getString())
                            .findFirst()
                            .orElseThrow();
                    return String.format("%s %s", firstName, lastName).strip();
                };

        Set<String> authorNames =
                jq.jsonArrayStream(bookstoreJsonObj, "books")
                        .flatMap(v -> jq.jsonArrayStream(v, "authors"))
                        .flatMap(jq::childObjectStream)
                        .map(v -> getAuthorName.apply(v.jsonObject()))
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
                        jq.childArrayStream(bookJsonObj, "authors")
                                .flatMap(jq::childObjectStream)
                                .anyMatch(v ->
                                        jq.childStringStream(v, "firstName")
                                                .anyMatch(v2 -> v2.jsonString().getString().equals("Jennifer")) &&
                                                jq.childStringStream(v, "lastName")
                                                        .anyMatch(v2 -> v2.jsonString().getString().equals("Widom"))
                                );

        Set<String> bookISBNsCoauthoredByJenniferWidom =
                jq.jsonArrayStream(bookstoreJsonObj, "books")
                        .flatMap(jq::childObjectStream)
                        .filter(v -> bookCowrittenByJenniferWidom.test(v.jsonObject()))
                        .flatMap(v -> jq.childStringStream(v, "ISBN"))
                        .map(v -> v.jsonString().getString())
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
                jq.jsonArrayStream(bookstoreJsonObj, "magazines")
                        .flatMap(jq::childObjectStream)
                        .filter(v ->
                                jq.jsonStringStream(v, "month")
                                        .anyMatch(v2 -> v2.jsonString().getString().equals("February"))
                        )
                        .flatMap(v -> jq.childStringStream(v, "title"))
                        .map(v -> v.jsonString().getString())
                        .toList();

        assertEquals(
                List.of("National Geographic", "Newsweek"),
                februaryMagazineTitles
        );
    }

    private static JsonObject parseBookstoreJson() {
        JsonReaderFactory jsonReaderFactory = jsonProvider.createReaderFactory(Map.of());

        return jsonReaderFactory.createReader(new StringReader(BOOKSTORE_JSON_STRING)).readObject();
    }
}
