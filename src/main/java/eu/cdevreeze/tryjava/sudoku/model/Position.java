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

import java.util.Comparator;

/**
 * Zero-based position in a Sudoku grid
 *
 * @author Chris de Vreeze
 */
public record Position(int rowIndex, int columnIndex) {

    public Position {
        Preconditions.checkArgument(rowIndex >= 0 && rowIndex < 9);
        Preconditions.checkArgument(columnIndex >= 0 && columnIndex < 9);
    }

    public Position increaseRowIndex(int delta) {
        return Position.of(rowIndex + delta, columnIndex);
    }

    public Position increaseColumnIndex(int delta) {
        return Position.of(rowIndex, columnIndex + delta);
    }

    public static Position of(int rowIndex, int columnIndex) {
        return new Position(rowIndex, columnIndex);
    }

    public static final Comparator<Position> comparator =
            Comparator.comparingInt(Position::rowIndex).thenComparingInt(Position::columnIndex);
}
