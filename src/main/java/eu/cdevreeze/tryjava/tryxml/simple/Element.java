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

package eu.cdevreeze.tryjava.tryxml.simple;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.cdevreeze.tryjava.tryxml.queryapi.ElementQueryApi;
import eu.cdevreeze.tryjava.tryxml.functionalqueryapi.internal.DefaultElementQueryFunctionApi;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * XML element node
 *
 * @author Chris de Vreeze
 */
public record Element(
        QName name,
        ImmutableMap<QName, String> attributes,
        ImmutableList<XmlNode> children) implements XmlNode, ElementQueryApi<Element> {
    public Element {
        Objects.requireNonNull(name);
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(children);

        // Too expensive?

        // Note that attributes must not use the default namespace, if any. That's not checked here.
        ImmutableList<QName> names = ImmutableList.<QName>builder().add(name).addAll(attributes.keySet()).build();
        var prefixNamespaces = getPrefixNamespaceMap(names);
        Preconditions.checkArgument(
                prefixNamespaces.entrySet().stream().allMatch(kv -> kv.getValue().size() == 1),
                "There is no consistent mapping from prefix to namespace");
    }

    @Override
    public QName elementName() {
        return name;
    }

    @Override
    public ImmutableMap<QName, String> attributes() {
        return attributes;
    }

    // Specific stream-returning methods

    @Override
    public Stream<Element> childElementStream() {
        return elementQueryFunctionApi.childElementStream(Element.this);
    }

    @Override
    public Stream<Element> childElementStream(Predicate<Element> predicate) {
        return elementQueryFunctionApi.childElementStream(Element.this, predicate);
    }

    @Override
    public Stream<Element> descendantElementOrSelfStream() {
        return elementQueryFunctionApi.descendantElementOrSelfStream(Element.this);
    }

    @Override
    public Stream<Element> descendantElementOrSelfStream(Predicate<Element> predicate) {
        return elementQueryFunctionApi.descendantElementOrSelfStream(Element.this, predicate);
    }

    @Override
    public Stream<Element> descendantElementStream() {
        return elementQueryFunctionApi.descendantElementStream(Element.this);
    }

    @Override
    public Stream<Element> descendantElementStream(Predicate<Element> predicate) {
        return elementQueryFunctionApi.descendantElementStream(Element.this, predicate);
    }

    @Override
    public Stream<Element> topmostDescendantElementOrSelfStream(Predicate<Element> predicate) {
        return elementQueryFunctionApi.topmostDescendantElementOrSelfStream(Element.this, predicate);
    }

    @Override
    public Stream<Element> topmostDescendantElementStream(Predicate<Element> predicate) {
        return elementQueryFunctionApi.topmostDescendantElementStream(Element.this, predicate);
    }

    private static Map<String, Set<String>> getPrefixNamespaceMap(List<QName> names) {
        return names.stream()
                // keeping only real namespaces (and preventing default namespace conflicts with un-prefixed attributes)
                .filter(nm -> !nm.getNamespaceURI().isEmpty())
                .collect(Collectors.groupingBy(
                        QName::getPrefix, // never null, but possibly empty string
                        Collectors.mapping(
                                QName::getNamespaceURI, // never null, but possibly empty string
                                Collectors.toSet()
                        )
                ));
    }

    public static final ElementQueryFunctionApi elementQueryFunctionApi = new ElementQueryFunctionApi();

    // Note that Element and ElementQueryFunctionApi mutually depend on each other

    public static final class ElementQueryFunctionApi implements DefaultElementQueryFunctionApi<Element> {

        @Override
        public QName elementName(Element element) {
            return element.name();
        }

        @Override
        public ImmutableMap<QName, String> attributes(Element element) {
            return element.attributes();
        }

        @Override
        public Stream<Element> childElementStream(Element element) {
            return filterElements(element.children().stream());
        }

        private static Stream<Element> filterElements(Stream<XmlNode> nodeStream) {
            return nodeStream.flatMap(node -> {
                if (node instanceof Element elem) {
                    return Stream.of(elem);
                } else {
                    return Stream.empty();
                }
            });
        }
    }
}
