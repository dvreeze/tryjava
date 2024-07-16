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

package eu.cdevreeze.tryjava.sudoku.print;

import eu.cdevreeze.tryjava.sudoku.model.Grid;
import eu.cdevreeze.tryjava.sudoku.model.Row;

import java.util.stream.Collectors;

/**
 * Sudoku grid printer, printing 9 lines of whitespace-separated numbers, where 0 or smaller means "not filled".
 *
 * @author Chris de Vreeze
 */
public class GridPrinter {

    private GridPrinter() {
    }

    public static String print(Grid grid) {
        return grid.rows().stream()
                .map(GridPrinter::print)
                .collect(Collectors.joining("\n"));
    }

    private static String print(Row row) {
        return row.cellValues().stream()
                .map(c -> c.orElse(0))
                .map(Object::toString)
                .collect(Collectors.joining(" "));
    }
}
