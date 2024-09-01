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

import java.util.function.Predicate;
import java.util.stream.Stream;

import static eu.cdevreeze.tryjava.tryjsonp.queries.JsonQueryResults.*;

/**
 * Implementation of JsonQueryApi.
 *
 * @author Chris de Vreeze
 */
final public class ConcreteJsonQueryApi implements JsonQueryApi {

    public static ConcreteJsonQueryApi instance() {
        return new ConcreteJsonQueryApi();
    }

    // Alias for descendaht-or-self axis

    @Override
    public Stream<? extends JsonValueResult> jsonStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonStream(valueResult);
    }

    @Override
    public Stream<? extends JsonValueResult> jsonStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfJsonStream(valueResult, fieldName);
    }

    @Override
    public Stream<JsonObjectResult> jsonObjectStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonObjectStream(valueResult);
    }

    @Override
    public Stream<JsonObjectResult> jsonObjectStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfJsonObjectStream(valueResult, fieldName);
    }

    @Override
    public Stream<JsonArrayResult> jsonArrayStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonArrayStream(valueResult);
    }

    @Override
    public Stream<JsonArrayResult> jsonArrayStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfJsonArrayStream(valueResult, fieldName);
    }

    @Override
    public Stream<JsonStringResult> jsonStringStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonStringStream(valueResult);
    }

    @Override
    public Stream<JsonStringResult> jsonStringStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfJsonStringStream(valueResult, fieldName);
    }

    @Override
    public Stream<JsonNumberResult> jsonNumberStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonNumberStream(valueResult);
    }

    @Override
    public Stream<JsonNumberResult> jsonNumberStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfJsonNumberStream(valueResult, fieldName);
    }

    @Override
    public Stream<? extends JsonValueResult> jsonStream(JsonValue jsonValue) {
        return descendantOrSelfJsonStream(jsonValue);
    }

    @Override
    public Stream<? extends JsonValueResult> jsonStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfJsonStream(jsonValue, fieldName);
    }

    @Override
    public Stream<JsonObjectResult> jsonObjectStream(JsonValue jsonValue) {
        return descendantOrSelfJsonObjectStream(jsonValue);
    }

    @Override
    public Stream<JsonObjectResult> jsonObjectStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfJsonObjectStream(jsonValue, fieldName);
    }

    @Override
    public Stream<JsonArrayResult> jsonArrayStream(JsonValue jsonValue) {
        return descendantOrSelfJsonArrayStream(jsonValue);
    }

    @Override
    public Stream<JsonArrayResult> jsonArrayStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfJsonArrayStream(jsonValue, fieldName);
    }

    @Override
    public Stream<JsonStringResult> jsonStringStream(JsonValue jsonValue) {
        return descendantOrSelfJsonStringStream(jsonValue);
    }

    @Override
    public Stream<JsonStringResult> jsonStringStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfJsonStringStream(jsonValue, fieldName);
    }

    @Override
    public Stream<JsonNumberResult> jsonNumberStream(JsonValue jsonValue) {
        return descendantOrSelfJsonNumberStream(jsonValue);
    }

    @Override
    public Stream<JsonNumberResult> jsonNumberStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfJsonNumberStream(jsonValue, fieldName);
    }

    // Child axis

    @Override
    public Stream<? extends JsonValueResult> childJsonStream(JsonValueResult valueResult) {
        return valueResult.children().stream();
    }

    @Override
    public Stream<? extends JsonValueResult> childJsonStream(JsonValueResult valueResult, String fieldName) {
        return childJsonStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonObjectResult> childJsonObjectStream(JsonValueResult valueResult) {
        return childJsonStream(valueResult)
                .filter(r -> r instanceof JsonObjectResult)
                .map(r -> (JsonObjectResult) r);
    }

    @Override
    public Stream<JsonObjectResult> childJsonObjectStream(JsonValueResult valueResult, String fieldName) {
        return childJsonObjectStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> childJsonArrayStream(JsonValueResult valueResult) {
        return childJsonStream(valueResult)
                .filter(r -> r instanceof JsonArrayResult)
                .map(r -> (JsonArrayResult) r);
    }

    @Override
    public Stream<JsonArrayResult> childJsonArrayStream(JsonValueResult valueResult, String fieldName) {
        return childJsonArrayStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> childJsonStringStream(JsonValueResult valueResult) {
        return childJsonStream(valueResult)
                .filter(r -> r instanceof JsonStringResult)
                .map(r -> (JsonStringResult) r);
    }

    @Override
    public Stream<JsonStringResult> childJsonStringStream(JsonValueResult valueResult, String fieldName) {
        return childJsonStringStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> childJsonNumberStream(JsonValueResult valueResult) {
        return childJsonStream(valueResult)
                .filter(r -> r instanceof JsonNumberResult)
                .map(r -> (JsonNumberResult) r);
    }

    @Override
    public Stream<JsonNumberResult> childJsonNumberStream(JsonValueResult valueResult, String fieldName) {
        return childJsonNumberStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<? extends JsonValueResult> childJsonStream(JsonValue jsonValue) {
        return childJsonStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<? extends JsonValueResult> childJsonStream(JsonValue jsonValue, String fieldName) {
        return childJsonStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonObjectResult> childJsonObjectStream(JsonValue jsonValue) {
        return childJsonObjectStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonObjectResult> childJsonObjectStream(JsonValue jsonValue, String fieldName) {
        return childJsonObjectStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> childJsonArrayStream(JsonValue jsonValue) {
        return childJsonArrayStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonArrayResult> childJsonArrayStream(JsonValue jsonValue, String fieldName) {
        return childJsonArrayStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> childJsonStringStream(JsonValue jsonValue) {
        return childJsonStringStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonStringResult> childJsonStringStream(JsonValue jsonValue, String fieldName) {
        return childJsonStringStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> childJsonNumberStream(JsonValue jsonValue) {
        return childJsonNumberStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonNumberResult> childJsonNumberStream(JsonValue jsonValue, String fieldName) {
        return childJsonNumberStream(jsonValue).filter(isObjectField(fieldName));
    }

    // Descendant-or-self axis

    @Override
    public Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValueResult valueResult) {
        // Recursive
        return Stream.concat(
                Stream.of(valueResult),
                valueResult.children().stream().flatMap(this::descendantOrSelfJsonStream)
        );
    }

    @Override
    public Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfJsonStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonObjectResult> descendantOrSelfJsonObjectStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonStream(valueResult)
                .filter(r -> r instanceof JsonObjectResult)
                .map(r -> (JsonObjectResult) r);
    }

    @Override
    public Stream<JsonObjectResult> descendantOrSelfJsonObjectStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfJsonObjectStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> descendantOrSelfJsonArrayStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonStream(valueResult)
                .filter(r -> r instanceof JsonArrayResult)
                .map(r -> (JsonArrayResult) r);
    }

    @Override
    public Stream<JsonArrayResult> descendantOrSelfJsonArrayStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfJsonArrayStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> descendantOrSelfJsonStringStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonStream(valueResult)
                .filter(r -> r instanceof JsonStringResult)
                .map(r -> (JsonStringResult) r);
    }

    @Override
    public Stream<JsonStringResult> descendantOrSelfJsonStringStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfJsonStringStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> descendantOrSelfJsonNumberStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonStream(valueResult)
                .filter(r -> r instanceof JsonNumberResult)
                .map(r -> (JsonNumberResult) r);
    }

    @Override
    public Stream<JsonNumberResult> descendantOrSelfJsonNumberStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfJsonNumberStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValue jsonValue) {
        return descendantOrSelfJsonStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<? extends JsonValueResult> descendantOrSelfJsonStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfJsonStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonObjectResult> descendantOrSelfJsonObjectStream(JsonValue jsonValue) {
        return descendantOrSelfJsonObjectStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonObjectResult> descendantOrSelfJsonObjectStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfJsonObjectStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> descendantOrSelfJsonArrayStream(JsonValue jsonValue) {
        return descendantOrSelfJsonArrayStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonArrayResult> descendantOrSelfJsonArrayStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfJsonArrayStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> descendantOrSelfJsonStringStream(JsonValue jsonValue) {
        return descendantOrSelfJsonStringStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonStringResult> descendantOrSelfJsonStringStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfJsonStringStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> descendantOrSelfJsonNumberStream(JsonValue jsonValue) {
        return descendantOrSelfJsonNumberStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonNumberResult> descendantOrSelfJsonNumberStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfJsonNumberStream(jsonValue).filter(isObjectField(fieldName));
    }

    // Descendant axis

    @Override
    public Stream<? extends JsonValueResult> descendantJsonStream(JsonValueResult valueResult) {
        return valueResult.children().stream().flatMap(this::descendantOrSelfJsonStream);
    }

    @Override
    public Stream<? extends JsonValueResult> descendantJsonStream(JsonValueResult valueResult, String fieldName) {
        return descendantJsonStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonObjectResult> descendantJsonObjectStream(JsonValueResult valueResult) {
        return descendantJsonStream(valueResult)
                .filter(r -> r instanceof JsonObjectResult)
                .map(r -> (JsonObjectResult) r);
    }

    @Override
    public Stream<JsonObjectResult> descendantJsonObjectStream(JsonValueResult valueResult, String fieldName) {
        return descendantJsonObjectStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> descendantJsonArrayStream(JsonValueResult valueResult) {
        return descendantJsonStream(valueResult)
                .filter(r -> r instanceof JsonArrayResult)
                .map(r -> (JsonArrayResult) r);
    }

    @Override
    public Stream<JsonArrayResult> descendantJsonArrayStream(JsonValueResult valueResult, String fieldName) {
        return descendantJsonArrayStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> descendantJsonStringStream(JsonValueResult valueResult) {
        return descendantJsonStream(valueResult)
                .filter(r -> r instanceof JsonStringResult)
                .map(r -> (JsonStringResult) r);
    }

    @Override
    public Stream<JsonStringResult> descendantJsonStringStream(JsonValueResult valueResult, String fieldName) {
        return descendantJsonStringStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> descendantJsonNumberStream(JsonValueResult valueResult) {
        return descendantJsonStream(valueResult)
                .filter(r -> r instanceof JsonNumberResult)
                .map(r -> (JsonNumberResult) r);
    }

    @Override
    public Stream<JsonNumberResult> descendantJsonNumberStream(JsonValueResult valueResult, String fieldName) {
        return descendantJsonNumberStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<? extends JsonValueResult> descendantJsonStream(JsonValue jsonValue) {
        return descendantJsonStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<? extends JsonValueResult> descendantJsonStream(JsonValue jsonValue, String fieldName) {
        return descendantJsonStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonObjectResult> descendantJsonObjectStream(JsonValue jsonValue) {
        return descendantJsonObjectStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonObjectResult> descendantJsonObjectStream(JsonValue jsonValue, String fieldName) {
        return descendantJsonObjectStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> descendantJsonArrayStream(JsonValue jsonValue) {
        return descendantJsonArrayStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonArrayResult> descendantJsonArrayStream(JsonValue jsonValue, String fieldName) {
        return descendantJsonArrayStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> descendantJsonStringStream(JsonValue jsonValue) {
        return descendantJsonStringStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonStringResult> descendantJsonStringStream(JsonValue jsonValue, String fieldName) {
        return descendantJsonStringStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> descendantJsonNumberStream(JsonValue jsonValue) {
        return descendantJsonNumberStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonNumberResult> descendantJsonNumberStream(JsonValue jsonValue, String fieldName) {
        return descendantJsonNumberStream(jsonValue).filter(isObjectField(fieldName));
    }

    // Self axis

    @Override
    public Stream<? extends JsonValueResult> selfJsonStream(JsonValueResult valueResult) {
        return Stream.of(valueResult);
    }

    @Override
    public Stream<? extends JsonValueResult> selfJsonStream(JsonValueResult valueResult, String fieldName) {
        return selfJsonStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonObjectResult> selfJsonObjectStream(JsonValueResult valueResult) {
        return selfJsonStream(valueResult)
                .filter(r -> r instanceof JsonObjectResult)
                .map(r -> (JsonObjectResult) r);
    }

    @Override
    public Stream<JsonObjectResult> selfJsonObjectStream(JsonValueResult valueResult, String fieldName) {
        return selfJsonObjectStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> selfJsonArrayStream(JsonValueResult valueResult) {
        return selfJsonStream(valueResult)
                .filter(r -> r instanceof JsonArrayResult)
                .map(r -> (JsonArrayResult) r);
    }

    @Override
    public Stream<JsonArrayResult> selfJsonArrayStream(JsonValueResult valueResult, String fieldName) {
        return selfJsonArrayStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> selfJsonStringStream(JsonValueResult valueResult) {
        return selfJsonStream(valueResult)
                .filter(r -> r instanceof JsonStringResult)
                .map(r -> (JsonStringResult) r);
    }

    @Override
    public Stream<JsonStringResult> selfJsonStringStream(JsonValueResult valueResult, String fieldName) {
        return selfJsonStringStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> selfJsonNumberStream(JsonValueResult valueResult) {
        return selfJsonStream(valueResult)
                .filter(r -> r instanceof JsonNumberResult)
                .map(r -> (JsonNumberResult) r);
    }

    @Override
    public Stream<JsonNumberResult> selfJsonNumberStream(JsonValueResult valueResult, String fieldName) {
        return selfJsonNumberStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<? extends JsonValueResult> selfJsonStream(JsonValue jsonValue) {
        return selfJsonStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<? extends JsonValueResult> selfJsonStream(JsonValue jsonValue, String fieldName) {
        return selfJsonStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonObjectResult> selfJsonObjectStream(JsonValue jsonValue) {
        return selfJsonObjectStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonObjectResult> selfJsonObjectStream(JsonValue jsonValue, String fieldName) {
        return selfJsonObjectStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> selfJsonArrayStream(JsonValue jsonValue) {
        return selfJsonArrayStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonArrayResult> selfJsonArrayStream(JsonValue jsonValue, String fieldName) {
        return selfJsonArrayStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> selfJsonStringStream(JsonValue jsonValue) {
        return selfJsonStringStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonStringResult> selfJsonStringStream(JsonValue jsonValue, String fieldName) {
        return selfJsonStringStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> selfJsonNumberStream(JsonValue jsonValue) {
        return selfJsonNumberStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonNumberResult> selfJsonNumberStream(JsonValue jsonValue, String fieldName) {
        return selfJsonNumberStream(jsonValue).filter(isObjectField(fieldName));
    }

    // Private methods

    private Predicate<JsonValueResult> isObjectField(String fieldName) {
        return valueResult -> valueResult.optionalFieldName().stream().anyMatch(fieldName::equals);
    }
}
