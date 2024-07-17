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
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Entire Sudoku grid
 *
 * @author Chris de Vreeze
 */
public record Grid(ImmutableList<Row> rows) implements GridApi, GridOrHouse {

    public Grid {
        Preconditions.checkArgument(rows.size() == Row.ROW_COUNT);
        Preconditions.checkArgument(
                rows.stream().map(Row::rowIndex).toList().equals(IntStream.range(0, Row.ROW_COUNT).boxed().toList())
        );
    }

    @Override
    public ImmutableList<Cell> cells() {
        return rows.stream().flatMap(r -> r.cells().stream()).collect(ImmutableList.toImmutableList());
    }

    @Override
    public ImmutableList<Position> positionsInGrid() {
        return rows.stream().flatMap(r -> r.positionsInGrid().stream()).collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Column> columns() {
        return IntStream.range(0, Column.COLUMN_COUNT)
                .mapToObj(this::column)
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Region> regions() {
        return Region.ALL_REGION_UPPER_LEFT_POSITIONS.stream()
                .map(this::region)
                .collect(ImmutableList.toImmutableList());
    }

    public Optional<Integer> cellValue(Position position) {
        Objects.checkIndex(position.rowIndex(), Row.ROW_COUNT);
        Objects.checkIndex(position.columnIndex(), Column.COLUMN_COUNT);
        return row(position.rowIndex()).cellValues().get(position.columnIndex());
    }

    public Grid withCellValue(Position position, Optional<Integer> value) {
        ImmutableList.Builder<Row> rowBuilder = ImmutableList.builder();
        rowBuilder.addAll(rows.stream().limit(position.rowIndex()).toList());
        rowBuilder.add(row(position.rowIndex()).withCellValue(position.columnIndex(), value));
        rowBuilder.addAll(rows.stream().skip(position.rowIndex() + 1).toList());

        ImmutableList<Row> updatedRows = rowBuilder.build();
        return new Grid(updatedRows);
    }

    public Row row(int idx) {
        Objects.checkIndex(idx, Row.ROW_COUNT);
        return rows.get(idx);
    }

    public Column column(int idx) {
        Objects.checkIndex(idx, Column.COLUMN_COUNT);
        return new Column(
                idx,
                rows.stream().map(r -> r.cellValues().get(idx)).collect(ImmutableList.toImmutableList())
        );
    }

    public Region region(Position position) {
        RegionPosition regionPosition = RegionPosition.fromPosition(position);
        return region(regionPosition);
    }

    public Region region(RegionPosition regionPosition) {
        Position upperLeftPosition = regionPosition.upperLeftPositionInGrid();
        return new Region(
                upperLeftPosition,
                IntStream.range(0, Region.ROW_COUNT)
                        .mapToObj(i -> {
                            var leftPosition = upperLeftPosition.increaseRowIndex(i);
                            return new RegionRow(
                                    leftPosition,
                                    IntStream.range(0, Region.COLUMN_COUNT)
                                            .mapToObj(j -> cellValue(leftPosition.increaseColumnIndex(j)))
                                            .collect(ImmutableList.toImmutableList())
                            );
                        })
                        .collect(ImmutableList.toImmutableList())

        );
    }

    @Override
    public boolean isValid() {
        return rows.stream().allMatch(Row::isValid) &&
                columns().stream().allMatch(Column::isValid) &&
                regions().stream().allMatch(Region::isValid);
    }

    @Override
    public boolean isFilled() {
        return filledCellCount() == Row.ROW_COUNT * Row.SIZE;
    }

    public boolean isSolved() {
        return isValid() && isFilled();
    }

    public static Grid fromRows(ImmutableList<ImmutableList<Optional<Integer>>> rows) {
        Preconditions.checkArgument(rows.size() == Row.ROW_COUNT);
        rows.forEach(r -> Preconditions.checkArgument(r.size() == Row.SIZE));

        return new Grid(
                IntStream.range(0, Row.ROW_COUNT)
                        .mapToObj(i -> new Row(i, rows.get(i)))
                        .collect(ImmutableList.toImmutableList())
        );
    }
}
