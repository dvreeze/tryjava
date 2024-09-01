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
        return descendantOrSelfObjectStream(valueResult);
    }

    @Override
    public Stream<JsonObjectResult> jsonObjectStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfObjectStream(valueResult, fieldName);
    }

    @Override
    public Stream<JsonArrayResult> jsonArrayStream(JsonValueResult valueResult) {
        return descendantOrSelfArrayStream(valueResult);
    }

    @Override
    public Stream<JsonArrayResult> jsonArrayStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfArrayStream(valueResult, fieldName);
    }

    @Override
    public Stream<JsonStringResult> jsonStringStream(JsonValueResult valueResult) {
        return descendantOrSelfStringStream(valueResult);
    }

    @Override
    public Stream<JsonStringResult> jsonStringStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfStringStream(valueResult, fieldName);
    }

    @Override
    public Stream<JsonNumberResult> jsonNumberStream(JsonValueResult valueResult) {
        return descendantOrSelfNumberStream(valueResult);
    }

    @Override
    public Stream<JsonNumberResult> jsonNumberStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfNumberStream(valueResult, fieldName);
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
        return descendantOrSelfObjectStream(jsonValue);
    }

    @Override
    public Stream<JsonObjectResult> jsonObjectStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfObjectStream(jsonValue, fieldName);
    }

    @Override
    public Stream<JsonArrayResult> jsonArrayStream(JsonValue jsonValue) {
        return descendantOrSelfArrayStream(jsonValue);
    }

    @Override
    public Stream<JsonArrayResult> jsonArrayStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfArrayStream(jsonValue, fieldName);
    }

    @Override
    public Stream<JsonStringResult> jsonStringStream(JsonValue jsonValue) {
        return descendantOrSelfStringStream(jsonValue);
    }

    @Override
    public Stream<JsonStringResult> jsonStringStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfStringStream(jsonValue, fieldName);
    }

    @Override
    public Stream<JsonNumberResult> jsonNumberStream(JsonValue jsonValue) {
        return descendantOrSelfNumberStream(jsonValue);
    }

    @Override
    public Stream<JsonNumberResult> jsonNumberStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfNumberStream(jsonValue, fieldName);
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
    public Stream<JsonObjectResult> childObjectStream(JsonValueResult valueResult) {
        return childJsonStream(valueResult)
                .filter(r -> r instanceof JsonObjectResult)
                .map(r -> (JsonObjectResult) r);
    }

    @Override
    public Stream<JsonObjectResult> childObjectStream(JsonValueResult valueResult, String fieldName) {
        return childObjectStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> childArrayStream(JsonValueResult valueResult) {
        return childJsonStream(valueResult)
                .filter(r -> r instanceof JsonArrayResult)
                .map(r -> (JsonArrayResult) r);
    }

    @Override
    public Stream<JsonArrayResult> childArrayStream(JsonValueResult valueResult, String fieldName) {
        return childArrayStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> childStringStream(JsonValueResult valueResult) {
        return childJsonStream(valueResult)
                .filter(r -> r instanceof JsonStringResult)
                .map(r -> (JsonStringResult) r);
    }

    @Override
    public Stream<JsonStringResult> childStringStream(JsonValueResult valueResult, String fieldName) {
        return childStringStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> childNumberStream(JsonValueResult valueResult) {
        return childJsonStream(valueResult)
                .filter(r -> r instanceof JsonNumberResult)
                .map(r -> (JsonNumberResult) r);
    }

    @Override
    public Stream<JsonNumberResult> childNumberStream(JsonValueResult valueResult, String fieldName) {
        return childNumberStream(valueResult).filter(isObjectField(fieldName));
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
    public Stream<JsonObjectResult> childObjectStream(JsonValue jsonValue) {
        return childObjectStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonObjectResult> childObjectStream(JsonValue jsonValue, String fieldName) {
        return childObjectStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> childArrayStream(JsonValue jsonValue) {
        return childArrayStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonArrayResult> childArrayStream(JsonValue jsonValue, String fieldName) {
        return childArrayStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> childStringStream(JsonValue jsonValue) {
        return childStringStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonStringResult> childStringStream(JsonValue jsonValue, String fieldName) {
        return childStringStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> childNumberStream(JsonValue jsonValue) {
        return childNumberStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonNumberResult> childNumberStream(JsonValue jsonValue, String fieldName) {
        return childNumberStream(jsonValue).filter(isObjectField(fieldName));
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
    public Stream<JsonObjectResult> descendantOrSelfObjectStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonStream(valueResult)
                .filter(r -> r instanceof JsonObjectResult)
                .map(r -> (JsonObjectResult) r);
    }

    @Override
    public Stream<JsonObjectResult> descendantOrSelfObjectStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfObjectStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> descendantOrSelfArrayStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonStream(valueResult)
                .filter(r -> r instanceof JsonArrayResult)
                .map(r -> (JsonArrayResult) r);
    }

    @Override
    public Stream<JsonArrayResult> descendantOrSelfArrayStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfArrayStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> descendantOrSelfStringStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonStream(valueResult)
                .filter(r -> r instanceof JsonStringResult)
                .map(r -> (JsonStringResult) r);
    }

    @Override
    public Stream<JsonStringResult> descendantOrSelfStringStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfStringStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> descendantOrSelfNumberStream(JsonValueResult valueResult) {
        return descendantOrSelfJsonStream(valueResult)
                .filter(r -> r instanceof JsonNumberResult)
                .map(r -> (JsonNumberResult) r);
    }

    @Override
    public Stream<JsonNumberResult> descendantOrSelfNumberStream(JsonValueResult valueResult, String fieldName) {
        return descendantOrSelfNumberStream(valueResult).filter(isObjectField(fieldName));
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
    public Stream<JsonObjectResult> descendantOrSelfObjectStream(JsonValue jsonValue) {
        return descendantOrSelfObjectStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonObjectResult> descendantOrSelfObjectStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfObjectStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> descendantOrSelfArrayStream(JsonValue jsonValue) {
        return descendantOrSelfArrayStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonArrayResult> descendantOrSelfArrayStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfArrayStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> descendantOrSelfStringStream(JsonValue jsonValue) {
        return descendantOrSelfStringStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonStringResult> descendantOrSelfStringStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfStringStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> descendantOrSelfNumberStream(JsonValue jsonValue) {
        return descendantOrSelfNumberStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonNumberResult> descendantOrSelfNumberStream(JsonValue jsonValue, String fieldName) {
        return descendantOrSelfNumberStream(jsonValue).filter(isObjectField(fieldName));
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
    public Stream<JsonObjectResult> descendantObjectStream(JsonValueResult valueResult) {
        return descendantJsonStream(valueResult)
                .filter(r -> r instanceof JsonObjectResult)
                .map(r -> (JsonObjectResult) r);
    }

    @Override
    public Stream<JsonObjectResult> descendantObjectStream(JsonValueResult valueResult, String fieldName) {
        return descendantObjectStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> descendantArrayStream(JsonValueResult valueResult) {
        return descendantJsonStream(valueResult)
                .filter(r -> r instanceof JsonArrayResult)
                .map(r -> (JsonArrayResult) r);
    }

    @Override
    public Stream<JsonArrayResult> descendantArrayStream(JsonValueResult valueResult, String fieldName) {
        return descendantArrayStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> descendantStringStream(JsonValueResult valueResult) {
        return descendantJsonStream(valueResult)
                .filter(r -> r instanceof JsonStringResult)
                .map(r -> (JsonStringResult) r);
    }

    @Override
    public Stream<JsonStringResult> descendantStringStream(JsonValueResult valueResult, String fieldName) {
        return descendantStringStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> descendantNumberStream(JsonValueResult valueResult) {
        return descendantJsonStream(valueResult)
                .filter(r -> r instanceof JsonNumberResult)
                .map(r -> (JsonNumberResult) r);
    }

    @Override
    public Stream<JsonNumberResult> descendantNumberStream(JsonValueResult valueResult, String fieldName) {
        return descendantNumberStream(valueResult).filter(isObjectField(fieldName));
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
    public Stream<JsonObjectResult> descendantObjectStream(JsonValue jsonValue) {
        return descendantObjectStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonObjectResult> descendantObjectStream(JsonValue jsonValue, String fieldName) {
        return descendantObjectStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> descendantArrayStream(JsonValue jsonValue) {
        return descendantArrayStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonArrayResult> descendantArrayStream(JsonValue jsonValue, String fieldName) {
        return descendantArrayStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> descendantStringStream(JsonValue jsonValue) {
        return descendantStringStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonStringResult> descendantStringStream(JsonValue jsonValue, String fieldName) {
        return descendantStringStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> descendantNumberStream(JsonValue jsonValue) {
        return descendantNumberStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonNumberResult> descendantNumberStream(JsonValue jsonValue, String fieldName) {
        return descendantNumberStream(jsonValue).filter(isObjectField(fieldName));
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
    public Stream<JsonObjectResult> selfObjectStream(JsonValueResult valueResult) {
        return selfJsonStream(valueResult)
                .filter(r -> r instanceof JsonObjectResult)
                .map(r -> (JsonObjectResult) r);
    }

    @Override
    public Stream<JsonObjectResult> selfObjectStream(JsonValueResult valueResult, String fieldName) {
        return selfObjectStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> selfArrayStream(JsonValueResult valueResult) {
        return selfJsonStream(valueResult)
                .filter(r -> r instanceof JsonArrayResult)
                .map(r -> (JsonArrayResult) r);
    }

    @Override
    public Stream<JsonArrayResult> selfArrayStream(JsonValueResult valueResult, String fieldName) {
        return selfArrayStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> selfStringStream(JsonValueResult valueResult) {
        return selfJsonStream(valueResult)
                .filter(r -> r instanceof JsonStringResult)
                .map(r -> (JsonStringResult) r);
    }

    @Override
    public Stream<JsonStringResult> selfStringStream(JsonValueResult valueResult, String fieldName) {
        return selfStringStream(valueResult).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> selfNumberStream(JsonValueResult valueResult) {
        return selfJsonStream(valueResult)
                .filter(r -> r instanceof JsonNumberResult)
                .map(r -> (JsonNumberResult) r);
    }

    @Override
    public Stream<JsonNumberResult> selfNumberStream(JsonValueResult valueResult, String fieldName) {
        return selfNumberStream(valueResult).filter(isObjectField(fieldName));
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
    public Stream<JsonObjectResult> selfObjectStream(JsonValue jsonValue) {
        return selfObjectStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonObjectResult> selfObjectStream(JsonValue jsonValue, String fieldName) {
        return selfObjectStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonArrayResult> selfArrayStream(JsonValue jsonValue) {
        return selfArrayStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonArrayResult> selfArrayStream(JsonValue jsonValue, String fieldName) {
        return selfArrayStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonStringResult> selfStringStream(JsonValue jsonValue) {
        return selfStringStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonStringResult> selfStringStream(JsonValue jsonValue, String fieldName) {
        return selfStringStream(jsonValue).filter(isObjectField(fieldName));
    }

    @Override
    public Stream<JsonNumberResult> selfNumberStream(JsonValue jsonValue) {
        return selfNumberStream(JsonQueryResults.from(jsonValue));
    }

    @Override
    public Stream<JsonNumberResult> selfNumberStream(JsonValue jsonValue, String fieldName) {
        return selfNumberStream(jsonValue).filter(isObjectField(fieldName));
    }

    // Private methods

    private Predicate<JsonValueResult> isObjectField(String fieldName) {
        return valueResult -> valueResult.optionalFieldName().stream().anyMatch(fieldName::equals);
    }
}
