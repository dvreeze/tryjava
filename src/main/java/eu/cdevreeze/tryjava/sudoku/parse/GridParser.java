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

package eu.cdevreeze.tryjava.sudoku.parse;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.tryjava.sudoku.model.Grid;

import java.util.OptionalInt;

/**
 * Sudoku grid parser, taking 9 lines of whitespace-separated numbers, where 0 or smaller means "not filled".
 *
 * @author Chris de Vreeze
 */
public class GridParser {

    private GridParser() {
    }

    private static final Splitter lineSplitter = Splitter.on(CharMatcher.whitespace())
            .trimResults()
            .omitEmptyStrings();

    public static Grid parse(String sudokuGrid) {
        ImmutableList<ImmutableList<OptionalInt>> rows =
                sudokuGrid.lines()
                        .map(line -> lineSplitter.splitToStream(line)
                                .map(Integer::parseInt)
                                .map(n -> (n <= 0) ? OptionalInt.empty() : OptionalInt.of(n))
                                .collect(ImmutableList.toImmutableList()))
                        .collect(ImmutableList.toImmutableList());

        return Grid.fromRows(rows);
    }
}
