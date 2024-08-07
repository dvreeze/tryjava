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

package eu.cdevreeze.tryjava.tryxml.functionalqueryapi.internal;

import eu.cdevreeze.tryjava.tryxml.functionalqueryapi.FunctionalElementQueryApi;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Default partial implementation of FunctionalElementQueryApi
 *
 * @param <E>
 * @author Chris de Vreeze
 */
public interface DefaultFunctionalElementQueryApi<E> extends FunctionalElementQueryApi<E> {

    @Override
    default Stream<E> childElementStream(E element, Predicate<E> predicate) {
        Objects.requireNonNull(element);
        Objects.requireNonNull(predicate);

        return childElementStream(element).filter(predicate);
    }

    @Override
    default Stream<E> descendantElementOrSelfStream(E element) {
        Objects.requireNonNull(element);

        Stream<E> selfStream = Stream.of(element);
        // Recursion
        Stream<E> descendantElemStream =
                childElementStream(element).flatMap(this::descendantElementOrSelfStream);
        return Stream.concat(selfStream, descendantElemStream);
    }

    @Override
    default Stream<E> descendantElementOrSelfStream(E element, Predicate<E> predicate) {
        Objects.requireNonNull(element);
        Objects.requireNonNull(predicate);

        return descendantElementOrSelfStream(element).filter(predicate);
    }

    @Override
    default Stream<E> descendantElementStream(E element) {
        Objects.requireNonNull(element);

        return childElementStream(element).flatMap(this::descendantElementOrSelfStream);
    }

    @Override
    default Stream<E> descendantElementStream(E element, Predicate<E> predicate) {
        Objects.requireNonNull(element);
        Objects.requireNonNull(predicate);

        return descendantElementStream(element).filter(predicate);
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
