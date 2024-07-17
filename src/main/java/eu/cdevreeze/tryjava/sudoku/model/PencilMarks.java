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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * So-called "pencil marks" in a Sudoku game. See <a href="https://www.learn-sudoku.com/pencil-marks.html">pencil-marks</a>.
 *
 * @author Chris de Vreeze
 */
public record PencilMarks(ImmutableMap<Position, ImmutableSet<Integer>> cellCandidateNumbers) {

    public ImmutableMap<Position, ImmutableSet<Integer>> cellCandidatesInRow(int rowIndex) {
        return cellCandidateNumbers.entrySet().stream()
                .filter(kv -> kv.getKey().rowIndex() == rowIndex)
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public ImmutableMap<Position, ImmutableSet<Integer>> cellCandidatesInColumn(int columnIndex) {
        return cellCandidateNumbers.entrySet().stream()
                .filter(kv -> kv.getKey().columnIndex() == columnIndex)
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public ImmutableMap<Position, ImmutableSet<Integer>> cellCandidatesInRegion(RegionPosition regionPosition) {
        Set<Position> positionsInGrid = new HashSet<>(regionPosition.positionsInGrid());
        return cellCandidateNumbers.entrySet().stream()
                .filter(kv -> positionsInGrid.contains(kv.getKey()))
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static PencilMarks forGrid(Grid grid) {
        var remainingUnfilledPositions = grid.remainingUnfilledCells().stream().map(Cell::position).collect(ImmutableList.toImmutableList());
        var cellCandidates = candidates(grid, remainingUnfilledPositions);
        return new PencilMarks(cellCandidates);
    }

    public static ImmutableMap<Position, ImmutableSet<Integer>> candidatesForRow(Grid grid, int rowIndex) {
        ImmutableList<Position> positions =
                IntStream.range(0, Row.SIZE).mapToObj(j -> Position.of(rowIndex, j)).collect(ImmutableList.toImmutableList());
        return candidates(grid, positions);
    }

    public static ImmutableMap<Position, ImmutableSet<Integer>> candidatesForColumn(Grid grid, int columnIndex) {
        ImmutableList<Position> positions =
                IntStream.range(0, Column.SIZE).mapToObj(i -> Position.of(i, columnIndex)).collect(ImmutableList.toImmutableList());
        return candidates(grid, positions);
    }

    public static ImmutableMap<Position, ImmutableSet<Integer>> candidatesForRegion(Grid grid, RegionPosition regionPosition) {
        ImmutableList<Position> positions = regionPosition.positionsInGrid();
        return candidates(grid, positions);
    }

    public static ImmutableMap<Position, ImmutableSet<Integer>> candidates(Grid grid, ImmutableList<Position> positions) {
        return positions.stream()
                .filter(pos -> grid.cellValue(pos).isEmpty())
                .collect(ImmutableMap.toImmutableMap(Function.identity(), pos -> candidates(grid, pos)));
    }

    public static ImmutableSet<Integer> candidates(Grid grid, Position position) {
        Preconditions.checkArgument(grid.cellValue(position).isEmpty());

        Row row = grid.row(position.rowIndex());
        Column column = grid.column(position.columnIndex());
        Region region = grid.region(RegionPosition.fromPosition(position));

        return row.remainingUnusedNumbers().stream()
                .filter(n -> column.remainingUnusedNumbers().contains(n))
                .filter(n -> region.remainingUnusedNumbers().contains(n))
                .filter(n -> grid.withCellValue(position, Optional.of(n)).isValid())
                .collect(ImmutableSet.toImmutableSet());
    }
}
