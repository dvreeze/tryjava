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

package eu.cdevreeze.tryjava.sudoku.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * House or the entire grid.
 * <p>
 * All implementations must be immutable and thread-safe.
 *
 * @author Chris de Vreeze
 */
public interface GridOrHouse {

    ImmutableList<Cell> cells();

    /**
     * Equivalent to "cells().stream().map(cell -> cell.position()).collect(ImmutableList.toImmutableList())"
     */
    ImmutableList<Position> positionsInGrid();

    boolean isValid();

    default ImmutableSet<Cell> filledCells() {
        return cells().stream().filter(Cell::isFilled).collect(ImmutableSet.toImmutableSet());
    }

    default ImmutableSet<Position> positionsOfFilledCells() {
        return cells().stream().filter(Cell::isFilled).map(Cell::position).collect(ImmutableSet.toImmutableSet());
    }

    default ImmutableSet<Cell> remainingUnfilledCells() {
        return cells().stream().filter(Cell::isUnfilled).collect(ImmutableSet.toImmutableSet());
    }

    default ImmutableSet<Position> positionsOfRemainingUnfilledCells() {
        return cells().stream().filter(Cell::isUnfilled).map(Cell::position).collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Equivalent to "filledCells().size()"
     */
    default int filledCellCount() {
        return filledCells().size();
    }

    default boolean isFilled() {
        return cells().stream().allMatch(Cell::isFilled);
    }
}
