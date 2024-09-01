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

package eu.cdevreeze.tryjava.tryjsonp.queries;

import jakarta.json.JsonValue;

import java.util.stream.Stream;

import static eu.cdevreeze.tryjava.tryjsonp.queries.JsonQueryResults.*;

/**
 * JSON query API, in the sense that this API offers methods to create Java Streams of JSON results.
 * The latter are instances of type "JsonQueryResults.JsonValueResult" (and subtypes).
 *
 * @author Chris de Vreeze
 */
public interface JsonQueryApi {

    // Alias for descendant-or-self axis

    Stream<? extends JsonValueResult> jsonStream(JsonValueResult valueResult);

    Stream<? extends JsonValueResult> jsonStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonObjectResult> jsonObjectStream(JsonValueResult valueResult);

    Stream<JsonObjectResult> jsonObjectStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonArrayResult> jsonArrayStream(JsonValueResult valueResult);

    Stream<JsonArrayResult> jsonArrayStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonStringResult> jsonStringStream(JsonValueResult valueResult);

    Stream<JsonStringResult> jsonStringStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonNumberResult> jsonNumberStream(JsonValueResult valueResult);

    Stream<JsonNumberResult> jsonNumberStream(JsonValueResult valueResult, String fieldName);

    // Alias for descendant-or-self axis, passing JsonValue instead of JsonValueResult

    Stream<? extends JsonValueResult> jsonStream(JsonValue jsonValue);

    Stream<? extends JsonValueResult> jsonStream(JsonValue jsonValue, String fieldName);

    Stream<JsonObjectResult> jsonObjectStream(JsonValue jsonValue);

    Stream<JsonObjectResult> jsonObjectStream(JsonValue jsonValue, String fieldName);

    Stream<JsonArrayResult> jsonArrayStream(JsonValue jsonValue);

    Stream<JsonArrayResult> jsonArrayStream(JsonValue jsonValue, String fieldName);

    Stream<JsonStringResult> jsonStringStream(JsonValue jsonValue);

    Stream<JsonStringResult> jsonStringStream(JsonValue jsonValue, String fieldName);

    Stream<JsonNumberResult> jsonNumberStream(JsonValue jsonValue);

    Stream<JsonNumberResult> jsonNumberStream(JsonValue jsonValue, String fieldName);

    // Child axis

    Stream<? extends JsonValueResult> childJsonStream(JsonValueResult valueResult);

    Stream<? extends JsonValueResult> childJsonStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonObjectResult> childObjectStream(JsonValueResult valueResult);

    Stream<JsonObjectResult> childObjectStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonArrayResult> childArrayStream(JsonValueResult valueResult);

    Stream<JsonArrayResult> childArrayStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonStringResult> childStringStream(JsonValueResult valueResult);

    Stream<JsonStringResult> childStringStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonNumberResult> childNumberStream(JsonValueResult valueResult);

    Stream<JsonNumberResult> childNumberStream(JsonValueResult valueResult, String fieldName);

    // Child axis, passing JsonValue instead of JsonValueResult

    Stream<? extends JsonValueResult> childJsonStream(JsonValue jsonValue);

    Stream<? extends JsonValueResult> childJsonStream(JsonValue jsonValue, String fieldName);

    Stream<JsonObjectResult> childObjectStream(JsonValue jsonValue);

    Stream<JsonObjectResult> childObjectStream(JsonValue jsonValue, String fieldName);

    Stream<JsonArrayResult> childArrayStream(JsonValue jsonValue);

    Stream<JsonArrayResult> childArrayStream(JsonValue jsonValue, String fieldName);

    Stream<JsonStringResult> childStringStream(JsonValue jsonValue);

    Stream<JsonStringResult> childStringStream(JsonValue jsonValue, String fieldName);

    Stream<JsonNumberResult> childNumberStream(JsonValue jsonValue);

    Stream<JsonNumberResult> childNumberStream(JsonValue jsonValue, String fieldName);

    // Descendant-or-self axis

    Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValueResult valueResult);

    Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonObjectResult> descendantOrSelfObjectStream(JsonValueResult valueResult);

    Stream<JsonObjectResult> descendantOrSelfObjectStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonArrayResult> descendantOrSelfArrayStream(JsonValueResult valueResult);

    Stream<JsonArrayResult> descendantOrSelfArrayStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonStringResult> descendantOrSelfStringStream(JsonValueResult valueResult);

    Stream<JsonStringResult> descendantOrSelfStringStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonNumberResult> descendantOrSelfNumberStream(JsonValueResult valueResult);

    Stream<JsonNumberResult> descendantOrSelfNumberStream(JsonValueResult valueResult, String fieldName);

    // Descendant-or-self axis, passing JsonValue instead of JsonValueResult

    Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValue jsonValue);

    Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValue jsonValue, String fieldName);

    Stream<JsonObjectResult> descendantOrSelfObjectStream(JsonValue jsonValue);

    Stream<JsonObjectResult> descendantOrSelfObjectStream(JsonValue jsonValue, String fieldName);

    Stream<JsonArrayResult> descendantOrSelfArrayStream(JsonValue jsonValue);

    Stream<JsonArrayResult> descendantOrSelfArrayStream(JsonValue jsonValue, String fieldName);

    Stream<JsonStringResult> descendantOrSelfStringStream(JsonValue jsonValue);

    Stream<JsonStringResult> descendantOrSelfStringStream(JsonValue jsonValue, String fieldName);

    Stream<JsonNumberResult> descendantOrSelfNumberStream(JsonValue jsonValue);

    Stream<JsonNumberResult> descendantOrSelfNumberStream(JsonValue jsonValue, String fieldName);

    // Descendant axis

    Stream<? extends JsonValueResult> descendantJsonStream(JsonValueResult valueResult);

    Stream<? extends JsonValueResult> descendantJsonStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonObjectResult> descendantObjectStream(JsonValueResult valueResult);

    Stream<JsonObjectResult> descendantObjectStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonArrayResult> descendantArrayStream(JsonValueResult valueResult);

    Stream<JsonArrayResult> descendantArrayStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonStringResult> descendantStringStream(JsonValueResult valueResult);

    Stream<JsonStringResult> descendantStringStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonNumberResult> descendantNumberStream(JsonValueResult valueResult);

    Stream<JsonNumberResult> descendantNumberStream(JsonValueResult valueResult, String fieldName);

    // Descendant axis, passing JsonValue instead of JsonValueResult

    Stream<? extends JsonValueResult> descendantJsonStream(JsonValue jsonValue);

    Stream<? extends JsonValueResult> descendantJsonStream(JsonValue jsonValue, String fieldName);

    Stream<JsonObjectResult> descendantObjectStream(JsonValue jsonValue);

    Stream<JsonObjectResult> descendantObjectStream(JsonValue jsonValue, String fieldName);

    Stream<JsonArrayResult> descendantArrayStream(JsonValue jsonValue);

    Stream<JsonArrayResult> descendantArrayStream(JsonValue jsonValue, String fieldName);

    Stream<JsonStringResult> descendantStringStream(JsonValue jsonValue);

    Stream<JsonStringResult> descendantStringStream(JsonValue jsonValue, String fieldName);

    Stream<JsonNumberResult> descendantNumberStream(JsonValue jsonValue);

    Stream<JsonNumberResult> descendantNumberStream(JsonValue jsonValue, String fieldName);

    // Self axis

    Stream<? extends JsonValueResult> selfJsonStream(JsonValueResult valueResult);

    Stream<? extends JsonValueResult> selfJsonStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonObjectResult> selfObjectStream(JsonValueResult valueResult);

    Stream<JsonObjectResult> selfObjectStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonArrayResult> selfArrayStream(JsonValueResult valueResult);

    Stream<JsonArrayResult> selfArrayStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonStringResult> selfStringStream(JsonValueResult valueResult);

    Stream<JsonStringResult> selfStringStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonNumberResult> selfNumberStream(JsonValueResult valueResult);

    Stream<JsonNumberResult> selfNumberStream(JsonValueResult valueResult, String fieldName);

    // Self axis, passing JsonValue instead of JsonValueResult

    Stream<? extends JsonValueResult> selfJsonStream(JsonValue jsonValue);

    Stream<? extends JsonValueResult> selfJsonStream(JsonValue jsonValue, String fieldName);

    Stream<JsonObjectResult> selfObjectStream(JsonValue jsonValue);

    Stream<JsonObjectResult> selfObjectStream(JsonValue jsonValue, String fieldName);

    Stream<JsonArrayResult> selfArrayStream(JsonValue jsonValue);

    Stream<JsonArrayResult> selfArrayStream(JsonValue jsonValue, String fieldName);

    Stream<JsonStringResult> selfStringStream(JsonValue jsonValue);

    Stream<JsonStringResult> selfStringStream(JsonValue jsonValue, String fieldName);

    Stream<JsonNumberResult> selfNumberStream(JsonValue jsonValue);

    Stream<JsonNumberResult> selfNumberStream(JsonValue jsonValue, String fieldName);
}
