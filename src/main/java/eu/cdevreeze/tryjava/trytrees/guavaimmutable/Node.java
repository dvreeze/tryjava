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

package eu.cdevreeze.tryjava.trytrees.guavaimmutable;

import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Generic tree node.
 *
 * @param <N>
 * @author Chris de Vreeze
 */
public interface Node<N extends Node<N>> {

    N self();

    ImmutableList<N> findAllChildren();

    ImmutableList<N> filterChildren(Predicate<N> predicate);

    Optional<N> findChild(Predicate<N> predicate);

    ImmutableList<N> findAllDescendantsOrSelf();

    ImmutableList<N> filterDescendantsOrSelf(Predicate<N> predicate);

    Optional<N> findDescendantOrSelf(Predicate<N> predicate);

    ImmutableList<N> findAllDescendants();

    ImmutableList<N> filterDescendants(Predicate<N> predicate);

    Optional<N> findDescendant(Predicate<N> predicate);

    ImmutableList<N> findTopmostDescendantsOrSelf(Predicate<N> predicate);

    Optional<N> findTopmostDescendantOrSelf(Predicate<N> predicate);

    ImmutableList<N> findTopmostDescendants(Predicate<N> predicate);

    Optional<N> findTopmostDescendant(Predicate<N> predicate);
}
