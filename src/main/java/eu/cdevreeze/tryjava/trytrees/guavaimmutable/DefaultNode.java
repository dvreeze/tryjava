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
import eu.cdevreeze.tryjava.trytrees.guavaimmutable.internal.NodeStreamApi;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Default implementation of the generic tree node.
 *
 * @param <N>
 * @author Chris de Vreeze
 */
public interface DefaultNode<N extends DefaultNode<N>> extends Node<N> {

    NodeStreamApi<N> nodeStreamApi();

    @Override
    default ImmutableList<N> filterChildren(Predicate<N> predicate) {
        Objects.requireNonNull(predicate);
        return nodeStreamApi().filterChildren(self(), predicate).collect(ImmutableList.toImmutableList());
    }

    @Override
    default Optional<N> findChild(Predicate<N> predicate) {
        Objects.requireNonNull(predicate);
        return nodeStreamApi().findChild(self(), predicate);
    }

    @Override
    default ImmutableList<N> findAllDescendantsOrSelf() {
        return nodeStreamApi().findAllDescendantsOrSelf(self()).collect(ImmutableList.toImmutableList());
    }

    @Override
    default ImmutableList<N> filterDescendantsOrSelf(Predicate<N> predicate) {
        Objects.requireNonNull(predicate);
        return nodeStreamApi().filterDescendantsOrSelf(self(), predicate).collect(ImmutableList.toImmutableList());
    }

    @Override
    default Optional<N> findDescendantOrSelf(Predicate<N> predicate) {
        Objects.requireNonNull(predicate);
        return nodeStreamApi().findDescendantOrSelf(self(), predicate);
    }

    @Override
    default ImmutableList<N> findAllDescendants() {
        return nodeStreamApi().findAllDescendants(self()).collect(ImmutableList.toImmutableList());
    }

    @Override
    default ImmutableList<N> filterDescendants(Predicate<N> predicate) {
        Objects.requireNonNull(predicate);
        return nodeStreamApi().filterDescendants(self(), predicate).collect(ImmutableList.toImmutableList());
    }

    @Override
    default Optional<N> findDescendant(Predicate<N> predicate) {
        Objects.requireNonNull(predicate);
        return nodeStreamApi().findDescendant(self(), predicate);
    }

    @Override
    default ImmutableList<N> findTopmostDescendantsOrSelf(Predicate<N> predicate) {
        Objects.requireNonNull(predicate);
        return nodeStreamApi().findTopmostDescendantsOrSelf(self(), predicate).collect(ImmutableList.toImmutableList());
    }

    @Override
    default Optional<N> findTopmostDescendantOrSelf(Predicate<N> predicate) {
        Objects.requireNonNull(predicate);
        return nodeStreamApi().findTopmostDescendantOrSelf(self(), predicate);
    }

    @Override
    default ImmutableList<N> findTopmostDescendants(Predicate<N> predicate) {
        Objects.requireNonNull(predicate);
        return nodeStreamApi().findTopmostDescendants(self(), predicate).collect(ImmutableList.toImmutableList());
    }

    @Override
    default Optional<N> findTopmostDescendant(Predicate<N> predicate) {
        Objects.requireNonNull(predicate);
        return nodeStreamApi().findTopmostDescendant(self(), predicate);
    }
}
