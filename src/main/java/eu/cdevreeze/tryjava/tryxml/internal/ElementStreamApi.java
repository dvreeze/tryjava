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

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Streaming API for querying XML element nodes, across common axes.
 * Each method below that returns a stream should on each call return a fresh new stream.
 *
 * @param <E>
 * @author Chris de Vreeze
 */
public interface ElementStreamApi<E> {

    Stream<E> findAllChildElements(E element);

    Stream<E> filterChildElements(E element, Predicate<E> predicate);

    Stream<E> findAllDescendantElementsOrSelf(E element);

    Stream<E> filterDescendantElementsOrSelf(E element, Predicate<E> predicate);

    Stream<E> findAllDescendantElements(E element);

    Stream<E> filterDescendantElements(E element, Predicate<E> predicate);

    Stream<E> findTopmostDescendantElementsOrSelf(E element, Predicate<E> predicate);

    Stream<E> findTopmostDescendantElements(E element, Predicate<E> predicate);
}
