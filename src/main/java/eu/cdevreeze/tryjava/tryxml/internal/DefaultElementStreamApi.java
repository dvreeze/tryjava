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

package eu.cdevreeze.tryjava.tryxml.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Default implementation of ElementStreamApi
 *
 * @param <E>
 * @author Chris de Vreeze
 */
@FunctionalInterface
public interface DefaultElementStreamApi<E> extends ElementStreamApi<E> {

    /**
     * On each call, returns a fresh Stream of child element nodes of the given element.
     */
    @Override
    Stream<E> childElementStream(E element);

    @Override
    default Stream<E> childElementStream(E element, Predicate<E> predicate) {
        Objects.requireNonNull(element);
        Objects.requireNonNull(predicate);

        return childElementStream(element).filter(predicate);
    }

    @Override
    default Stream<E> descendantElementOrSelfStream(E element) {
        Objects.requireNonNull(element);

        return descendantElementOrSelfStream(element, ignoredElem -> true);
    }

    @Override
    default Stream<E> descendantElementOrSelfStream(E element, Predicate<E> predicate) {
        Objects.requireNonNull(element);
        Objects.requireNonNull(predicate);

        Optional<E> selfOption = Optional.of(element).filter(predicate);
        // Recursion
        Stream<E> descendantElems =
                childElementStream(element).flatMap(che -> descendantElementOrSelfStream(che, predicate));
        return Stream.concat(selfOption.stream(), descendantElems);
    }

    @Override
    default Stream<E> descendantElementStream(E element) {
        Objects.requireNonNull(element);

        return descendantElementStream(element, ignoredElem -> true);
    }

    @Override
    default Stream<E> descendantElementStream(E element, Predicate<E> predicate) {
        Objects.requireNonNull(element);
        Objects.requireNonNull(predicate);

        return childElementStream(element).flatMap(che -> descendantElementOrSelfStream(che, predicate));
    }

    @Override
    default Stream<E> topmostDescendantElementOrSelfStream(E element, Predicate<E> predicate) {
        Objects.requireNonNull(element);
        Objects.requireNonNull(predicate);

        if (predicate.test(element)) {
            return Stream.of(element);
        } else {
            // Recursion
            return childElementStream(element).flatMap(che -> topmostDescendantElementOrSelfStream(che, predicate));
        }
    }

    @Override
    default Stream<E> topmostDescendantElementStream(E element, Predicate<E> predicate) {
        Objects.requireNonNull(element);
        Objects.requireNonNull(predicate);

        return childElementStream(element).flatMap(che -> topmostDescendantElementOrSelfStream(che, predicate));
    }
}
