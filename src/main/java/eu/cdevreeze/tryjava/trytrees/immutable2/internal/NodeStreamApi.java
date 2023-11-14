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

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * API for querying generic tree nodes, across common axes
 *
 * @param <N>
 * @author Chris de Vreeze
 */
public interface NodeStreamApi<N> {

    Stream<N> findAllChildren(N node);

    Stream<N> filterChildren(N node, Predicate<N> predicate);

    Optional<N> findChild(N node, Predicate<N> predicate);

    Stream<N> findAllDescendantsOrSelf(N node);

    Stream<N> filterDescendantsOrSelf(N node, Predicate<N> predicate);

    Optional<N> findDescendantOrSelf(N node, Predicate<N> predicate);

    Stream<N> findAllDescendants(N node);

    Stream<N> filterDescendants(N node, Predicate<N> predicate);

    Optional<N> findDescendant(N node, Predicate<N> predicate);

    Stream<N> findTopmostDescendantsOrSelf(N node, Predicate<N> predicate);

    Optional<N> findTopmostDescendantOrSelf(N node, Predicate<N> predicate);

    Stream<N> findTopmostDescendants(N node, Predicate<N> predicate);

    Optional<N> findTopmostDescendant(N node, Predicate<N> predicate);
}
