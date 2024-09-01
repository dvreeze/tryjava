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

    Stream<JsonObjectResult> childJsonObjectStream(JsonValueResult valueResult);

    Stream<JsonObjectResult> childJsonObjectStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonArrayResult> childJsonArrayStream(JsonValueResult valueResult);

    Stream<JsonArrayResult> childJsonArrayStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonStringResult> childJsonStringStream(JsonValueResult valueResult);

    Stream<JsonStringResult> childJsonStringStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonNumberResult> childJsonNumberStream(JsonValueResult valueResult);

    Stream<JsonNumberResult> childJsonNumberStream(JsonValueResult valueResult, String fieldName);

    // Child axis, passing JsonValue instead of JsonValueResult

    Stream<? extends JsonValueResult> childJsonStream(JsonValue jsonValue);

    Stream<? extends JsonValueResult> childJsonStream(JsonValue jsonValue, String fieldName);

    Stream<JsonObjectResult> childJsonObjectStream(JsonValue jsonValue);

    Stream<JsonObjectResult> childJsonObjectStream(JsonValue jsonValue, String fieldName);

    Stream<JsonArrayResult> childJsonArrayStream(JsonValue jsonValue);

    Stream<JsonArrayResult> childJsonArrayStream(JsonValue jsonValue, String fieldName);

    Stream<JsonStringResult> childJsonStringStream(JsonValue jsonValue);

    Stream<JsonStringResult> childJsonStringStream(JsonValue jsonValue, String fieldName);

    Stream<JsonNumberResult> childJsonNumberStream(JsonValue jsonValue);

    Stream<JsonNumberResult> childJsonNumberStream(JsonValue jsonValue, String fieldName);

    // Descendant-or-self axis

    Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValueResult valueResult);

    Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonObjectResult> descendantOrSelfJsonObjectStream(JsonValueResult valueResult);

    Stream<JsonObjectResult> descendantOrSelfJsonObjectStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonArrayResult> descendantOrSelfJsonArrayStream(JsonValueResult valueResult);

    Stream<JsonArrayResult> descendantOrSelfJsonArrayStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonStringResult> descendantOrSelfJsonStringStream(JsonValueResult valueResult);

    Stream<JsonStringResult> descendantOrSelfJsonStringStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonNumberResult> descendantOrSelfJsonNumberStream(JsonValueResult valueResult);

    Stream<JsonNumberResult> descendantOrSelfJsonNumberStream(JsonValueResult valueResult, String fieldName);

    // Descendant-or-self axis, passing JsonValue instead of JsonValueResult

    Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValue jsonValue);

    Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValue jsonValue, String fieldName);

    Stream<JsonObjectResult> descendantOrSelfJsonObjectStream(JsonValue jsonValue);

    Stream<JsonObjectResult> descendantOrSelfJsonObjectStream(JsonValue jsonValue, String fieldName);

    Stream<JsonArrayResult> descendantOrSelfJsonArrayStream(JsonValue jsonValue);

    Stream<JsonArrayResult> descendantOrSelfJsonArrayStream(JsonValue jsonValue, String fieldName);

    Stream<JsonStringResult> descendantOrSelfJsonStringStream(JsonValue jsonValue);

    Stream<JsonStringResult> descendantOrSelfJsonStringStream(JsonValue jsonValue, String fieldName);

    Stream<JsonNumberResult> descendantOrSelfJsonNumberStream(JsonValue jsonValue);

    Stream<JsonNumberResult> descendantOrSelfJsonNumberStream(JsonValue jsonValue, String fieldName);

    // Descendant axis

    Stream<? extends JsonValueResult> descendantJsonStream(JsonValueResult valueResult);

    Stream<? extends JsonValueResult> descendantJsonStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonObjectResult> descendantJsonObjectStream(JsonValueResult valueResult);

    Stream<JsonObjectResult> descendantJsonObjectStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonArrayResult> descendantJsonArrayStream(JsonValueResult valueResult);

    Stream<JsonArrayResult> descendantJsonArrayStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonStringResult> descendantJsonStringStream(JsonValueResult valueResult);

    Stream<JsonStringResult> descendantJsonStringStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonNumberResult> descendantJsonNumberStream(JsonValueResult valueResult);

    Stream<JsonNumberResult> descendantJsonNumberStream(JsonValueResult valueResult, String fieldName);

    // Descendant axis, passing JsonValue instead of JsonValueResult

    Stream<? extends JsonValueResult> descendantJsonStream(JsonValue jsonValue);

    Stream<? extends JsonValueResult> descendantJsonStream(JsonValue jsonValue, String fieldName);

    Stream<JsonObjectResult> descendantJsonObjectStream(JsonValue jsonValue);

    Stream<JsonObjectResult> descendantJsonObjectStream(JsonValue jsonValue, String fieldName);

    Stream<JsonArrayResult> descendantJsonArrayStream(JsonValue jsonValue);

    Stream<JsonArrayResult> descendantJsonArrayStream(JsonValue jsonValue, String fieldName);

    Stream<JsonStringResult> descendantJsonStringStream(JsonValue jsonValue);

    Stream<JsonStringResult> descendantJsonStringStream(JsonValue jsonValue, String fieldName);

    Stream<JsonNumberResult> descendantJsonNumberStream(JsonValue jsonValue);

    Stream<JsonNumberResult> descendantJsonNumberStream(JsonValue jsonValue, String fieldName);

    // Self axis

    Stream<? extends JsonValueResult> selfJsonStream(JsonValueResult valueResult);

    Stream<? extends JsonValueResult> selfJsonStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonObjectResult> selfJsonObjectStream(JsonValueResult valueResult);

    Stream<JsonObjectResult> selfJsonObjectStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonArrayResult> selfJsonArrayStream(JsonValueResult valueResult);

    Stream<JsonArrayResult> selfJsonArrayStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonStringResult> selfJsonStringStream(JsonValueResult valueResult);

    Stream<JsonStringResult> selfJsonStringStream(JsonValueResult valueResult, String fieldName);

    Stream<JsonNumberResult> selfJsonNumberStream(JsonValueResult valueResult);

    Stream<JsonNumberResult> selfJsonNumberStream(JsonValueResult valueResult, String fieldName);

    // Self axis, passing JsonValue instead of JsonValueResult

    Stream<? extends JsonValueResult> selfJsonStream(JsonValue jsonValue);

    Stream<? extends JsonValueResult> selfJsonStream(JsonValue jsonValue, String fieldName);

    Stream<JsonObjectResult> selfJsonObjectStream(JsonValue jsonValue);

    Stream<JsonObjectResult> selfJsonObjectStream(JsonValue jsonValue, String fieldName);

    Stream<JsonArrayResult> selfJsonArrayStream(JsonValue jsonValue);

    Stream<JsonArrayResult> selfJsonArrayStream(JsonValue jsonValue, String fieldName);

    Stream<JsonStringResult> selfJsonStringStream(JsonValue jsonValue);

    Stream<JsonStringResult> selfJsonStringStream(JsonValue jsonValue, String fieldName);

    Stream<JsonNumberResult> selfJsonNumberStream(JsonValue jsonValue);

    Stream<JsonNumberResult> selfJsonNumberStream(JsonValue jsonValue, String fieldName);
}
