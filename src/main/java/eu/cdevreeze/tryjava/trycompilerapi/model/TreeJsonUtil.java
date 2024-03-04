/*
 * Copyright 2023-2024 Chris de Vreeze
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

package eu.cdevreeze.tryjava.trycompilerapi.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.lang.model.element.Name;
import java.io.IOException;

/**
 * JSON serialization support for the tree model. Note that most of it is supported out of the box,
 * for records, and for Guava immutable collections (when registering the Guava Jackson module).
 * This class offers the few missing pieces.
 *
 * @author Chris de Vreeze
 */
public class TreeJsonUtil {

    private TreeJsonUtil() {
    }

    public static final class NameSerializer extends StdSerializer<Name> {

        public NameSerializer() {
            this(null);
        }

        public NameSerializer(Class<Name> t) {
            super(t);
        }

        @Override
        public void serialize(Name value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
            generator.writeString(value.toString());
        }
    }
}
