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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Region column in a Sudoku grid
 *
 * @author Chris de Vreeze
 */
public record RegionColumn(Position upperMostPositionInGrid,
                           ImmutableList<Optional<Integer>> cellValues) implements GridPart {

    public RegionColumn {
        Preconditions.checkArgument(cellValues.size() == SIZE);
        Preconditions.checkArgument(
                cellValues.stream().allMatch(c -> c.stream().allMatch(v -> v >= 1 && v <= 9))
        );
    }

    @Override
    public ImmutableList<Cell> cells() {
        return IntStream.range(0, SIZE)
                .mapToObj(i -> new Cell(
                                upperMostPositionInGrid.increaseRowIndex(i),
                                cellValues.get(i)
                        )
                )
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public ImmutableList<Position> positionsInGrid() {
        return Stream.iterate(
                upperMostPositionInGrid(),
                p -> p.increaseRowIndex(1)).limit(SIZE).collect(ImmutableList.toImmutableList()
        );
    }

    @Override
    public boolean isValid() {
        List<Integer> filledCellValues = cellValues().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return filledCellValues.stream().distinct().toList().equals(filledCellValues);
    }

    public static final int SIZE = 3;
}
