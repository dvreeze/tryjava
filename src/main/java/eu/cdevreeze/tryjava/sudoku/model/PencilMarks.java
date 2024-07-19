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

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * So-called "pencil marks" in a Sudoku game. See <a href="https://www.learn-sudoku.com/pencil-marks.html">pencil-marks</a>.
 *
 * @author Chris de Vreeze
 */
public record PencilMarks(ImmutableMap<Position, ImmutableSet<Integer>> cellCandidateNumbers) {

    public ImmutableSet<Position> positions() {
        return cellCandidateNumbers.keySet();
    }

    public ImmutableMap<Position, ImmutableSet<Integer>> cellCandidatesInRow(int rowIndex) {
        return filterOnPositions(pos -> pos.rowIndex() == rowIndex);
    }

    public ImmutableMap<Position, ImmutableSet<Integer>> cellCandidatesInColumn(int columnIndex) {
        return filterOnPositions(pos -> pos.columnIndex() == columnIndex);
    }

    public ImmutableMap<Position, ImmutableSet<Integer>> cellCandidatesInRegion(RegionPosition regionPosition) {
        ImmutableSet<Position> positionsInGrid =
                regionPosition.positionsInGrid().stream().collect(ImmutableSet.toImmutableSet());
        return filterOnPositions(positionsInGrid);
    }

    public ImmutableMap<Position, ImmutableSet<Integer>> filterOnPositions(Predicate<Position> predicate) {
        return cellCandidateNumbers.entrySet().stream()
                .filter(kv -> predicate.test(kv.getKey()))
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public ImmutableMap<Position, ImmutableSet<Integer>> filterOnPositions(ImmutableSet<Position> positions) {
        return filterOnPositions(positions::contains);
    }

    public PencilMarks withoutPosition(Position pos) {
        return new PencilMarks(
                cellCandidateNumbers.entrySet()
                        .stream()
                        .filter(kv -> !kv.getKey().equals(pos))
                        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    public PencilMarks update(PencilMarks other) {
        return new PencilMarks(
                update(this.cellCandidateNumbers, other.cellCandidateNumbers)
        );
    }

    public PencilMarks update(ImmutableMap<Position, ImmutableSet<Integer>> other) {
        return update(new PencilMarks(other));
    }

    public PencilMarks updateIfPresent(Optional<PencilMarks> optionalPencilMarks) {
        return optionalPencilMarks.map(this::update).orElse(this);
    }

    public PencilMarks update(ImmutableSet<Position> positions, UnaryOperator<ImmutableSet<Integer>> valuesOperator) {
        ImmutableMap<Position, ImmutableSet<Integer>> cellCandidates =
                positions.stream()
                        .flatMap(pos -> Optional.ofNullable(cellCandidateNumbers.get(pos))
                                .map(values -> Map.entry(pos, valuesOperator.apply(values))).stream())
                        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
        return update(cellCandidates);
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

    public static ImmutableMap<Position, ImmutableSet<Integer>> update(
            ImmutableMap<Position, ImmutableSet<Integer>> candidates1,
            ImmutableMap<Position, ImmutableSet<Integer>> candidates2
    ) {
        return ImmutableMap.<Position, ImmutableSet<Integer>>builder()
                .putAll(candidates1)
                .putAll(candidates2)
                .buildKeepingLast();
    }

    public static ImmutableMap<Position, ImmutableSet<Integer>> updateIfPresent(
            ImmutableMap<Position, ImmutableSet<Integer>> candidates1,
            Optional<ImmutableMap<Position, ImmutableSet<Integer>>> optionalCandidates2
    ) {
        return optionalCandidates2.map(cds -> update(candidates1, cds)).orElse(candidates1);
    }
}
