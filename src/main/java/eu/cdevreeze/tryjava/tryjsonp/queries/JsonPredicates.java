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

import jakarta.json.*;

import java.util.function.Predicate;

/**
 * This class creates predicates on JSON values.
 *
 * @author Chris de Vreeze
 */
public class JsonPredicates {

    private JsonPredicates() {
    }

    public static Predicate<JsonQueryResults.JsonValueResult> isObjectField(String fieldName) {
        return jsonQueryResult -> jsonQueryResult.optionalFieldName().stream().anyMatch(fieldName::equals);
    }

    public static Predicate<JsonQueryResults.JsonValueResult> isJsonValue(Predicate<JsonValue> predicate) {
        return jsonQueryResult -> predicate.test(jsonQueryResult.jsonValue());
    }

    public static Predicate<JsonQueryResults.JsonValueResult> isJsonObject(Predicate<JsonObject> predicate) {
        return jsonQueryResult -> (jsonQueryResult.jsonValue() instanceof JsonObject jsonObj) && predicate.test(jsonObj);
    }

    public static Predicate<JsonQueryResults.JsonValueResult> isJsonArray(Predicate<JsonArray> predicate) {
        return jsonQueryResult -> (jsonQueryResult.jsonValue() instanceof JsonArray jsonArr) && predicate.test(jsonArr);
    }

    public static Predicate<JsonQueryResults.JsonValueResult> isJsonString(Predicate<JsonString> predicate) {
        return jsonQueryResult -> (jsonQueryResult.jsonValue() instanceof JsonString jsonStr) && predicate.test(jsonStr);
    }

    public static Predicate<JsonQueryResults.JsonValueResult> isJsonNumber(Predicate<JsonNumber> predicate) {
        return jsonQueryResult -> (jsonQueryResult.jsonValue() instanceof JsonNumber jsonNum) && predicate.test(jsonNum);
    }
}
