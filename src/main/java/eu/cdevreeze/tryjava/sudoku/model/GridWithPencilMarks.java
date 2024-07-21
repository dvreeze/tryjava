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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Grid with pencil marks. See <a href="https://www.learn-sudoku.com/pencil-marks.html">pencil-marks</a>.
 *
 * @author Chris de Vreeze
 */
public record GridWithPencilMarks(Grid grid, PencilMarks pencilMarks) implements GridApi {

    public GridWithPencilMarks {
        Preconditions.checkArgument(Sets.intersection(grid.positionsOfFilledCells(), pencilMarks.positions()).isEmpty());
    }

    @Override
    public Grid grid() {
        return grid;
    }

    @Override
    public Optional<PencilMarks> optionalPencilMarks() {
        return Optional.of(pencilMarks);
    }

    @Override
    public OptionalInt cellValue(Position position) {
        return grid.cellValue(position);
    }

    @Override
    public GridWithPencilMarks withCellValue(Position position, OptionalInt optionalValue) {
        if (optionalValue.isEmpty()) {
            return this;
        }

        Grid nextGrid = grid.withCellValue(position, optionalValue);

        return new GridWithPencilMarks(nextGrid, pencilMarks.withoutPosition(position))
                .removeInconsistencies(position);
    }

    // See https://sandiway.arizona.edu/sudoku/inconsistency.html

    public GridWithPencilMarks removeInconsistencies(Position position) {
        return removeInconsistenciesForRow(position.rowIndex())
                .removeInconsistenciesForColumn(position.columnIndex())
                .removeInconsistenciesForRegion(RegionPosition.fromPosition(position));
    }

    public GridWithPencilMarks removeInconsistenciesForRow(int rowIndex) {
        var row = grid().row(rowIndex);
        var remainingUnusedNumbers = row.remainingUnusedNumbers();
        PencilMarks updatedPencilMarks = pencilMarks.update(
                row.positionsOfRemainingUnfilledCells(),
                values -> values.stream()
                        .filter(remainingUnusedNumbers::contains)
                        .collect(ImmutableSet.toImmutableSet())
        );
        return new GridWithPencilMarks(grid(), updatedPencilMarks);
    }

    public GridWithPencilMarks removeInconsistenciesForColumn(int columnIndex) {
        var column = grid().column(columnIndex);
        var remainingUnusedNumbers = column.remainingUnusedNumbers();
        PencilMarks updatedPencilMarks = pencilMarks.update(
                column.positionsOfRemainingUnfilledCells(),
                values -> values.stream()
                        .filter(remainingUnusedNumbers::contains)
                        .collect(ImmutableSet.toImmutableSet())
        );
        return new GridWithPencilMarks(grid(), updatedPencilMarks);
    }

    public GridWithPencilMarks removeInconsistenciesForRegion(RegionPosition regionPosition) {
        var region = grid().region(regionPosition);
        var remainingUnusedNumbers = region.remainingUnusedNumbers();
        PencilMarks updatedPencilMarks = pencilMarks.update(
                region.positionsOfRemainingUnfilledCells(),
                values -> values.stream()
                        .filter(remainingUnusedNumbers::contains)
                        .collect(ImmutableSet.toImmutableSet())
        );
        return new GridWithPencilMarks(grid(), updatedPencilMarks);
    }

    public static GridWithPencilMarks of(Grid grid) {
        return new GridWithPencilMarks(grid, PencilMarks.forGrid(grid));
    }
}
