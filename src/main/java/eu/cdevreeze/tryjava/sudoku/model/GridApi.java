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

import java.util.Optional;

/**
 * Grid with or without pencil marks. See <a href="https://www.learn-sudoku.com/pencil-marks.html">pencil-marks</a>.
 *
 * @author Chris de Vreeze
 */
sealed public interface GridApi permits Grid, GridWithPencilMarks {

    Grid grid();

    Optional<Integer> cellValue(Position position);

    GridApi withCellValue(Position position, Optional<Integer> value);
}
