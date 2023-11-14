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

package eu.cdevreeze.tryjava.trytrees.immutable2.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Default implementation of NodeStreamApi
 *
 * @param <N>
 * @author Chris de Vreeze
 */
public interface DefaultNodeStreamApi<N> extends NodeStreamApi<N> {

    @Override
    default Stream<N> filterChildren(N node, Predicate<N> predicate) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(predicate);

        return findAllChildren(node).filter(predicate);
    }

    @Override
    default Optional<N> findChild(N node, Predicate<N> predicate) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(predicate);

        return filterChildren(node, predicate).findFirst();
    }

    @Override
    default Stream<N> findAllDescendantsOrSelf(N node) {
        Objects.requireNonNull(node);

        return filterDescendantsOrSelf(node, n -> true);
    }

    @Override
    default Stream<N> filterDescendantsOrSelf(N node, Predicate<N> predicate) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(predicate);

        var first = Stream.of(node).filter(predicate);

        // Recursive calls
        var remainder = findAllChildren(node).flatMap(ch -> filterDescendantsOrSelf(ch, predicate));

        return Stream.concat(first, remainder);
    }

    @Override
    default Optional<N> findDescendantOrSelf(N node, Predicate<N> predicate) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(predicate);

        return filterDescendantsOrSelf(node, n -> true).findFirst();
    }

    @Override
    default Stream<N> findAllDescendants(N node) {
        Objects.requireNonNull(node);

        return filterDescendants(node, n -> true);
    }

    @Override
    default Stream<N> filterDescendants(N node, Predicate<N> predicate) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(predicate);

        return findAllChildren(node).flatMap(ch -> filterDescendantsOrSelf(ch, predicate));
    }

    @Override
    default Optional<N> findDescendant(N node, Predicate<N> predicate) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(predicate);

        return filterDescendants(node, predicate).findFirst();
    }

    @Override
    default Stream<N> findTopmostDescendantsOrSelf(N node, Predicate<N> predicate) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(predicate);

        // Recursive calls
        return (predicate.test(node)) ?
                Stream.of(node) :
                findAllChildren(node).flatMap(ch -> findTopmostDescendantsOrSelf(ch, predicate));
    }

    @Override
    default Optional<N> findTopmostDescendantOrSelf(N node, Predicate<N> predicate) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(predicate);

        return findTopmostDescendantsOrSelf(node, predicate).findFirst();
    }

    @Override
    default Stream<N> findTopmostDescendants(N node, Predicate<N> predicate) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(predicate);

        return findAllChildren(node).flatMap(ch -> findTopmostDescendantsOrSelf(ch, predicate));
    }

    @Override
    default Optional<N> findTopmostDescendant(N node, Predicate<N> predicate) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(predicate);

        return findTopmostDescendants(node, predicate).findFirst();
    }
}
