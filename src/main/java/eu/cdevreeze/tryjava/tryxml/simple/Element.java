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
import eu.cdevreeze.tryjava.tryxml.internal.DefaultElementStreamApi;
import eu.cdevreeze.tryjava.tryxml.internal.ElementStreamApi;
import eu.cdevreeze.tryjava.tryxml.queryapi.ElementQueryApi;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

        ImmutableList<QName> names = ImmutableList.<QName>builder().add(name).addAll(attributes.keySet()).build();
        var optionalInScopeNamespaces = getOptionalInScopeNamespaces(names);
        Preconditions.checkArgument(
                optionalInScopeNamespaces.isPresent(),
                "There is no consistent mapping from prefix to namespace");
    }

    // Specific stream-returning methods

    public Stream<Element> childElementStream() {
        return filterElements(children().stream());
    }

    public Stream<Element> descendantElementOrSelfStream() {
        return elementStreamApi().get().descendantElementOrSelfStream(Element.this);
    }

    public Stream<Element> descendantElementOrSelfStream(Predicate<Element> predicate) {
        return elementStreamApi().get().descendantElementOrSelfStream(Element.this, predicate);
    }

    public Stream<Element> descendantElementStream() {
        return elementStreamApi().get().descendantElementStream(Element.this);
    }

    public Stream<Element> descendantElementStream(Predicate<Element> predicate) {
        return elementStreamApi().get().descendantElementStream(Element.this, predicate);
    }

    public Stream<Element> topmostDescendantElementOrSelfStream(Predicate<Element> predicate) {
        return elementStreamApi().get().topmostDescendantElementOrSelfStream(Element.this, predicate);
    }

    public Stream<Element> topmostDescendantElementStream(Predicate<Element> predicate) {
        return elementStreamApi().get().topmostDescendantElementStream(Element.this, predicate);
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

    private static Optional<ImmutableMap<String, String>> getOptionalInScopeNamespaces(List<QName> names) {
        Map<String, List<String>> prefixNamespaces =
                names.stream().collect(Collectors.groupingBy(
                                name -> Optional.ofNullable(name.getPrefix()).orElse(XMLConstants.DEFAULT_NS_PREFIX),
                                Collectors.mapping(
                                        name -> Optional.ofNullable(name.getNamespaceURI()).orElse(XMLConstants.DEFAULT_NS_PREFIX),
                                        Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                nss -> nss.stream()
                                                        .filter(ns -> !ns.isEmpty())
                                                        .distinct()
                                                        .collect(ImmutableList.toImmutableList()))
                                )
                        )).entrySet().stream()
                        .filter(kv -> !kv.getValue().isEmpty())
                        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

        if (prefixNamespaces.values().stream().distinct().allMatch(namespaces -> namespaces.size() <= 1)) {
            return Optional.of(
                    prefixNamespaces.entrySet().stream().collect(ImmutableMap.toImmutableMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().getFirst()
                    )));
        } else {
            return Optional.empty();
        }
    }

    private static Supplier<ElementStreamApi<Element>> elementStreamApi() {
        return () -> (DefaultElementStreamApi<Element>) (Element::childElementStream);
    }
}
