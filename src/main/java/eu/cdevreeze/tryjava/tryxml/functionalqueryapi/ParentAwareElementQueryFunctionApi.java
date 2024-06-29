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

package eu.cdevreeze.tryjava.tryxml.functionalqueryapi;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Streaming API for querying XML element nodes, across common axes, including ancestor axes.
 * Each method below that returns a stream should on each call return a fresh new stream.
 * <p>
 * In Scala this would have been a type class.
 *
 * @param <E>
 * @author Chris de Vreeze
 */
public interface ParentAwareElementQueryFunctionApi<E> extends ElementQueryFunctionApi<E> {

    Stream<E> ancestorElementOrSelfStream(E element);

    Stream<E> ancestorElementStream(E element);

    Optional<E> parentElementOption(E element);
}
