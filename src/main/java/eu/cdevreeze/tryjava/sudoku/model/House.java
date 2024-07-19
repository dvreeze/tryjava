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

import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Row, or column, or (3 * 3) region in a grid. This is called a house.
 * <p>
 * All implementations must be immutable and thread-safe.
 *
 * @author Chris de Vreeze
 */
public interface House extends GridOrHouse {

    ImmutableSet<Integer> ALL_NUMBERS =
            IntStream.rangeClosed(1, 9).boxed().collect(ImmutableSet.toImmutableSet());

    /**
     * Equivalent to "cells().stream().map(cell -> cell.optionalValue()).collect(ImmutableList.toImmutableList())"
     */
    ImmutableList<OptionalInt> cellValues();

    default ImmutableSet<Integer> remainingUnusedNumbers() {
        Set<Integer> usedNumbers = filledCells().stream().map(cell -> cell.optionalValue().orElseThrow()).collect(Collectors.toSet());
        return ALL_NUMBERS.stream().filter(n -> !usedNumbers.contains(n)).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    default boolean isValid() {
        List<Integer> filledCellValues = cellValues().stream()
                .filter(OptionalInt::isPresent)
                .map(OptionalInt::getAsInt)
                .toList();
        return filledCellValues.stream().distinct().toList().equals(filledCellValues);
    }
}
