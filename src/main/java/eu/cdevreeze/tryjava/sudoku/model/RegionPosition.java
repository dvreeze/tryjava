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

import java.util.stream.IntStream;

/**
 * Zero-based "region position" in a Sudoku grid in a "region coordinate system" with 3 * 3 regions.
 *
 * @author Chris de Vreeze
 */
public record RegionPosition(int regionRowIndex, int regionColumnIndex) {

    public RegionPosition {
        Preconditions.checkArgument(regionRowIndex >= 0 && regionRowIndex < 3);
        Preconditions.checkArgument(regionColumnIndex >= 0 && regionColumnIndex < 3);
    }

    public RegionPosition increaseRegionRowIndex(int delta) {
        return RegionPosition.of(regionRowIndex + delta, regionColumnIndex);
    }

    public RegionPosition increaseRegionColumnIndex(int delta) {
        return RegionPosition.of(regionRowIndex, regionColumnIndex + delta);
    }

    public Position upperLeftPositionInGrid() {
        return Position.of(regionRowIndex * 3, regionColumnIndex * 3);
    }

    public ImmutableList<Position> positionsInGrid() {
        Position upperLeftPos = upperLeftPositionInGrid();
        return IntStream.range(0, 3).boxed().flatMap(i ->
                        IntStream.range(0, 3).boxed()
                                .map(j -> upperLeftPos.increaseRowIndex(i).increaseColumnIndex(j)))
                .collect(ImmutableList.toImmutableList());
    }

    public static RegionPosition fromPosition(Position position) {
        return of(position.rowIndex() / 3, position.columnIndex() / 3);
    }

    public static RegionPosition of(int regionRowIndex, int regionColumnIndex) {
        return new RegionPosition(regionRowIndex, regionColumnIndex);
    }
}
