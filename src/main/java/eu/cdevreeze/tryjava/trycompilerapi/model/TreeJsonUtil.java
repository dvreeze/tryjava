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
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sun.source.tree.LineMap;

import javax.lang.model.element.Name;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;

/**
 * JSON serialization support for the tree model. Note that much of it is supported out of the box,
 * e.g. for Guava immutable collections (when registering the Guava Jackson module).
 * This class offers the few missing pieces.
 *
 * @author Chris de Vreeze
 */
public class TreeJsonUtil {

    private TreeJsonUtil() {
    }

    public static final class NodeSerializer extends StdSerializer<Trees.Node> {

        public NodeSerializer() {
            this(null);
        }

        public NodeSerializer(Class<Trees.Node> t) {
            super(t);
        }

        @Override
        public void serialize(Trees.Node value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();

            // First output the kind of value
            generator.writeStringField("kind", value.getKind().toString());

            // And then output the rest
            var recordComponents = value.getClass().getRecordComponents();

            for (RecordComponent comp : recordComponents) {
                try {
                    var mappedValue = comp.getAccessor().invoke(value);
                    provider.defaultSerializeField(comp.getName(), mappedValue, provider.getGenerator());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }

            generator.writeEndObject();
        }
    }

    public static final class NameSerializer extends StdSerializer<Name> {

        public NameSerializer() {
            this(null);
        }

        public NameSerializer(Class<Name> t) {
            super(t);
        }

        @Override
        public void serialize(Name value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(value.toString());
        }
    }

    public static final class LineMapSerializer extends StdSerializer<LineMap> {

        public LineMapSerializer() {
            this(null);
        }

        public LineMapSerializer(Class<LineMap> t) {
            super(t);
        }

        @Override
        public void serialize(LineMap value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeNull(); // Not much to write, given the "limited" API of LineMap
        }
    }

    public static final class JavaFileObjectSerializer extends StdSerializer<JavaFileObject> {

        public JavaFileObjectSerializer() {
            this(null);
        }

        public JavaFileObjectSerializer(Class<JavaFileObject> t) {
            super(t);
        }

        @Override
        public void serialize(JavaFileObject value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(value.getName());
        }
    }

    public static Module createModule() {
        var simpleModule = new SimpleModule();
        simpleModule.addSerializer(Trees.Node.class, new NodeSerializer());
        simpleModule.addSerializer(Name.class, new NameSerializer());
        simpleModule.addSerializer(LineMap.class, new LineMapSerializer());
        simpleModule.addSerializer(JavaFileObject.class, new JavaFileObjectSerializer());
        return simpleModule;
    }
}
