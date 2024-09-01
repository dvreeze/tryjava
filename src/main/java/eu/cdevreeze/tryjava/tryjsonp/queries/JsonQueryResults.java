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

import java.util.List;
import java.util.Optional;

/**
 * JSON query result, which is a JsonValue with optional key (i.e. the field name, if any).
 *
 * @author Chris de Vreeze
 */
public class JsonQueryResults {

    private JsonQueryResults() {
    }

    public interface JsonValueResult {

        Optional<String> optionalFieldName();

        JsonValue jsonValue();

        List<JsonValueResult> children();

        default boolean isField() {
            return optionalFieldName().isPresent();
        }
    }

    public interface JsonStructureResult extends JsonValueResult {

        @Override
        JsonStructure jsonValue();
    }

    public record JsonObjectResult(Optional<String> optionalFieldName,
                                   JsonObject jsonObject) implements JsonStructureResult {

        @Override
        public JsonObject jsonValue() {
            return jsonObject;
        }

        @Override
        public List<JsonValueResult> children() {
            return jsonObject.entrySet().stream()
                    .map(kv -> from(Optional.of(kv.getKey()), kv.getValue()))
                    .toList();
        }
    }

    public record JsonArrayResult(Optional<String> optionalFieldName,
                                  JsonArray jsonArray) implements JsonStructureResult {

        @Override
        public JsonArray jsonValue() {
            return jsonArray;
        }

        @Override
        public List<JsonValueResult> children() {
            return jsonArray.stream()
                    .map(v -> from(Optional.empty(), v))
                    .toList();
        }
    }

    public record JsonStringResult(Optional<String> optionalFieldName,
                                   JsonString jsonString) implements JsonValueResult {

        @Override
        public JsonString jsonValue() {
            return jsonString;
        }

        @Override
        public List<JsonValueResult> children() {
            return List.of();
        }
    }

    public record JsonNumberResult(Optional<String> optionalFieldName,
                                   JsonNumber jsonNumber) implements JsonValueResult {

        @Override
        public JsonNumber jsonValue() {
            return jsonNumber;
        }

        @Override
        public List<JsonValueResult> children() {
            return List.of();
        }
    }

    public record JsonTrueResult(Optional<String> optionalFieldName) implements JsonValueResult {

        @Override
        public JsonValue jsonValue() {
            return JsonValue.TRUE;
        }

        @Override
        public List<JsonValueResult> children() {
            return List.of();
        }
    }

    public record JsonFalseResult(Optional<String> optionalFieldName) implements JsonValueResult {

        @Override
        public JsonValue jsonValue() {
            return JsonValue.FALSE;
        }

        @Override
        public List<JsonValueResult> children() {
            return List.of();
        }
    }

    public record JsonNullResult(Optional<String> optionalFieldName) implements JsonValueResult {

        @Override
        public JsonValue jsonValue() {
            return JsonValue.NULL;
        }

        @Override
        public List<JsonValueResult> children() {
            return List.of();
        }
    }

    public static JsonValueResult from(Optional<String> optionalFieldName, JsonValue jsonValue) {
        if (jsonValue instanceof JsonObject jsonObject) {
            return new JsonObjectResult(optionalFieldName, jsonObject);
        } else if (jsonValue instanceof JsonArray jsonArray) {
            return new JsonArrayResult(optionalFieldName, jsonArray);
        } else if (jsonValue instanceof JsonString jsonString) {
            return new JsonStringResult(optionalFieldName, jsonString);
        } else if (jsonValue instanceof JsonNumber jsonNumber) {
            return new JsonNumberResult(optionalFieldName, jsonNumber);
        } else if (jsonValue.getValueType().equals(JsonValue.ValueType.TRUE)) {
            return new JsonTrueResult(optionalFieldName);
        } else if (jsonValue.getValueType().equals(JsonValue.ValueType.FALSE)) {
            return new JsonFalseResult(optionalFieldName);
        } else if (jsonValue.getValueType().equals(JsonValue.ValueType.NULL)) {
            return new JsonNullResult(optionalFieldName);
        } else {
            throw new RuntimeException("Unknown JsonValue: " + jsonValue);
        }
    }

    public static JsonValueResult from(JsonValue jsonValue) {
        return from(Optional.empty(), jsonValue);
    }
}
