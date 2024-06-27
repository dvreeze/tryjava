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

package eu.cdevreeze.tryjava.tryxml.queryapi;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Element query API, returning streams of elements. This is a common API offered by multiple XML
 * element implementations in this library.
 *
 * @author Chris de Vreeze
 */
public interface ElementQueryApi<E> {

    // Aliases of other stream-returning methods

    /**
     * Alias of descendantElementOrSelfStream
     */
    default Stream<E> elementStream() {
        return descendantElementOrSelfStream();
    }

    /**
     * Alias of descendantElementOrSelfStream
     */
    default Stream<E> elementStream(Predicate<E> predicate) {
        return descendantElementOrSelfStream(predicate);
    }

    /**
     * Alias of topmostDescendantElementOrSelfStream
     */
    default Stream<E> topmostElementStream(Predicate<E> predicate) {
        return topmostDescendantElementOrSelfStream(predicate);
    }

    // Specific stream-returning methods

    Stream<E> childElementStream();

    Stream<E> descendantElementOrSelfStream();

    Stream<E> descendantElementOrSelfStream(Predicate<E> predicate);

    Stream<E> descendantElementStream();

    Stream<E> descendantElementStream(Predicate<E> predicate);

    Stream<E> topmostDescendantElementOrSelfStream(Predicate<E> predicate);

    Stream<E> topmostDescendantElementStream(Predicate<E> predicate);
}
