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

import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Row in a Sudoku grid
 *
 * @author Chris de Vreeze
 */
public record Row(int rowIndex, ImmutableList<OptionalInt> cellValues) implements House {

    public Row {
        Objects.checkIndex(rowIndex, ROW_COUNT);
        Preconditions.checkArgument(cellValues.size() == SIZE);
        Preconditions.checkArgument(
                cellValues.stream().allMatch(c -> c.stream().allMatch(v -> v >= 1 && v <= 9))
        );
    }

    public ImmutableList<Cell> cells() {
        return IntStream.range(0, SIZE)
                .mapToObj(i -> new Cell(Position.of(rowIndex, i), cellValues.get(i)))
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Position> positionsInGrid() {
        return IntStream.range(0, SIZE)
                .mapToObj(i -> Position.of(rowIndex, i))
                .collect(ImmutableList.toImmutableList());
    }

    public Row withCellValue(int columnIndex, OptionalInt value) {
        ImmutableList.Builder<OptionalInt> cellValuesBuilder = ImmutableList.builder();
        cellValuesBuilder.addAll(cellValues.stream().limit(columnIndex).toList());
        cellValuesBuilder.add(value);
        cellValuesBuilder.addAll(cellValues.stream().skip(columnIndex + 1).toList());

        ImmutableList<OptionalInt> updatedCellValues = cellValuesBuilder.build();
        return new Row(rowIndex, updatedCellValues);
    }

    public static final int SIZE = 9;
    public static final int ROW_COUNT = 9;
}
