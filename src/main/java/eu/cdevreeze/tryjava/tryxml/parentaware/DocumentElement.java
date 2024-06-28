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

package eu.cdevreeze.tryjava.tryxml.parentaware;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.cdevreeze.tryjava.tryxml.internal.DefaultElementStreamApi;
import eu.cdevreeze.tryjava.tryxml.internal.ElementStreamApi;
import eu.cdevreeze.tryjava.tryxml.queryapi.ParentAwareElementQueryApi;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * XML document element with elements in it. These elements know their parent, sibling nodes etc.
 * This class is only useful for reading XML, preferably multiple times.
 *
 * @author Chris de Vreeze
 */
public final class DocumentElement {

    // The idea is to not create any memory leak. If the DocumentElement is no longer used,
    // it is ready for GC, along with all the "internal elements".

    private final ImmutableMap<ImmutableList<Integer>, eu.cdevreeze.tryjava.tryxml.simple.Element>
            elementMap;

    private DocumentElement(ImmutableMap<ImmutableList<Integer>, eu.cdevreeze.tryjava.tryxml.simple.Element> elementMap) {
        this.elementMap = elementMap;
    }

    public Element documentElement() {
        return new Element(ImmutableList.of());
    }

    // Non-static!

    public final class Element implements XmlNode, ParentAwareElementQueryApi<Element> {

        private final ImmutableList<Integer> navigationPath;

        private Element(ImmutableList<Integer> navigationPath) {
            this.navigationPath = navigationPath;
        }

        public ImmutableList<Integer> getNavigationPath() {
            return navigationPath;
        }

        public eu.cdevreeze.tryjava.tryxml.simple.Element getUnderlyingElement() {
            return Objects.requireNonNull(elementMap.get(navigationPath));
        }

        public ImmutableList<Element> findAllChildElements() {
            return IntStream.range(0, (int) getUnderlyingElement().childElementStream().count())
                    .mapToObj(idx -> new Element(addToPath(idx, navigationPath)))
                    .collect(ImmutableList.toImmutableList());
        }

        public Optional<Element> findParent() {
            return parentPathOption(navigationPath).map(Element::new);
        }

        // Specific stream-returning methods

        public Stream<Element> childElementStream() {
            return findAllChildElements().stream();
        }

        public Stream<Element> descendantElementOrSelfStream() {
            return elementStreamApi().descendantElementOrSelfStream(Element.this);
        }

        public Stream<Element> descendantElementOrSelfStream(Predicate<Element> predicate) {
            return elementStreamApi().descendantElementOrSelfStream(Element.this, predicate);
        }

        public Stream<Element> descendantElementStream() {
            return elementStreamApi().descendantElementStream(Element.this);
        }

        public Stream<Element> descendantElementStream(Predicate<Element> predicate) {
            return elementStreamApi().descendantElementStream(Element.this, predicate);
        }

        public Stream<Element> topmostDescendantElementOrSelfStream(Predicate<Element> predicate) {
            return elementStreamApi().topmostDescendantElementOrSelfStream(Element.this, predicate);
        }

        public Stream<Element> topmostDescendantElementStream(Predicate<Element> predicate) {
            return elementStreamApi().topmostDescendantElementStream(Element.this, predicate);
        }

        // Specific stream-returning methods for element ancestry

        public Stream<Element> ancestorOrSelfStream() {
            return Stream.iterate(this, e -> e.findParent().isPresent(), e -> e.findParent().orElseThrow());
        }

        public Stream<Element> ancestorStream() {
            return ancestorOrSelfStream().skip(1);
        }

        private static ElementStreamApi<Element> elementStreamApi() {
            return (DefaultElementStreamApi<Element>) (Element::childElementStream);
        }
    }

    public static DocumentElement create(eu.cdevreeze.tryjava.tryxml.simple.Element underlyingRootElement) {
        ImmutableList<Integer> navigationPath = ImmutableList.of();
        ConcurrentMap<ImmutableList<Integer>, eu.cdevreeze.tryjava.tryxml.simple.Element> elementMap =
                new ConcurrentHashMap<>();
        buildElementCache(navigationPath, underlyingRootElement, elementMap);
        return new DocumentElement(ImmutableMap.copyOf(elementMap));
    }

    private static void buildElementCache(
            ImmutableList<Integer> elementNavigationPath,
            eu.cdevreeze.tryjava.tryxml.simple.Element element,
            ConcurrentMap<ImmutableList<Integer>, eu.cdevreeze.tryjava.tryxml.simple.Element> elementMap
    ) {
        elementMap.put(elementNavigationPath, element);

        int idx = 0;
        for (var childElement : element.childElementStream().toList()) {
            // Recursion
            buildElementCache(
                    addToPath(idx, elementNavigationPath),
                    childElement,
                    elementMap
            );
            idx += 1;
        }
    }

    private static ImmutableList<Integer> addToPath(int nextIndex, ImmutableList<Integer> path) {
        return ImmutableList.<Integer>builder().addAll(path).add(nextIndex).build();
    }

    private static Optional<ImmutableList<Integer>> parentPathOption(ImmutableList<Integer> path) {
        return Optional.of(path).flatMap(p ->
                (p.isEmpty()) ? Optional.empty() : Optional.of(p.subList(0, p.size() - 1)));
    }
}
