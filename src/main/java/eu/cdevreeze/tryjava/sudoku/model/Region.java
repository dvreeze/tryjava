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
import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Region (3 * 3) in a Sudoku grid. It is also known as a block.
 *
 * @author Chris de Vreeze
 */
public record Region(Position upperLeftPosition, ImmutableList<RegionRow> regionRows) implements House {

    public Region {
        Preconditions.checkArgument(regionRows.size() == ROW_COUNT);
        Preconditions.checkArgument(ALL_REGION_UPPER_LEFT_POSITIONS.contains(upperLeftPosition));
        Preconditions.checkArgument(
                regionRows.stream().map(RegionRow::leftMostPositionInGrid).toList().equals(
                        Stream.iterate(upperLeftPosition, pos -> pos.increaseRowIndex(1))
                                .limit(ROW_COUNT)
                                .toList())
        );
    }

    public RegionRow regionRow(int idx) {
        Objects.checkIndex(idx, ROW_COUNT);
        return regionRows.get(idx);
    }

    public ImmutableList<RegionColumn> regionColumns() {
        return IntStream.range(0, COLUMN_COUNT)
                .mapToObj(this::regionColumn)
                .collect(ImmutableList.toImmutableList());
    }

    public RegionColumn regionColumn(int idx) {
        Objects.checkIndex(idx, COLUMN_COUNT);
        return new RegionColumn(
                upperLeftPosition.increaseColumnIndex(idx),
                regionRows.stream().map(r -> r.cellValues().get(idx)).collect(ImmutableList.toImmutableList())
        );
    }

    @Override
    public ImmutableList<Cell> cells() {
        return regionRows.stream()
                .flatMap(r -> r.cells().stream())
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public ImmutableList<OptionalInt> cellValues() {
        return regionRows.stream()
                .flatMap(r -> r.cellValues().stream())
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public ImmutableList<Position> positionsInGrid() {
        return regionRows.stream()
                .flatMap(r -> r.positionsInGrid().stream())
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableSet<Integer> rowIndicesInGrid() {
        return positionsInGrid().stream().map(Position::rowIndex).collect(ImmutableSet.toImmutableSet());
    }

    public ImmutableSet<Integer> columnIndicesInGrid() {
        return positionsInGrid().stream().map(Position::columnIndex).collect(ImmutableSet.toImmutableSet());
    }

    public static final int ROW_COUNT = 3;
    public static final int COLUMN_COUNT = 3;

    public static final ImmutableList<Position> ALL_REGION_UPPER_LEFT_POSITIONS = ImmutableList.of(
            Position.of(0, 0),
            Position.of(0, 3),
            Position.of(0, 6),
            Position.of(3, 0),
            Position.of(3, 3),
            Position.of(3, 6),
            Position.of(6, 0),
            Position.of(6, 3),
            Position.of(6, 6)
    );

    public static final ImmutableList<RegionPosition> ALL_REGION_POSITIONS = ImmutableList.of(
            RegionPosition.of(0, 0),
            RegionPosition.of(0, 1),
            RegionPosition.of(0, 2),
            RegionPosition.of(1, 0),
            RegionPosition.of(1, 1),
            RegionPosition.of(1, 2),
            RegionPosition.of(2, 0),
            RegionPosition.of(2, 1),
            RegionPosition.of(2, 2)
    );
}
