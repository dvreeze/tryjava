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
import com.google.common.collect.Sets;

import java.util.Optional;

/**
 * Grid with pencil marks. See <a href="https://www.learn-sudoku.com/pencil-marks.html">pencil-marks</a>.
 *
 * @author Chris de Vreeze
 */
public record GridWithPencilMarks(Grid grid, PencilMarks pencilMarks) implements GridApi {

    public GridWithPencilMarks {
        Preconditions.checkArgument(Sets.intersection(grid.positionsOfFilledCells(), pencilMarks.positions()).isEmpty());
    }

    public Optional<Integer> cellValue(Position position) {
        return grid.cellValue(position);
    }

    public GridWithPencilMarks withCellValue(Position position, Optional<Integer> value) {
        return new GridWithPencilMarks(
                grid.withCellValue(position, value),
                (value.isEmpty()) ? pencilMarks : pencilMarks.withoutPosition(position)
        );
    }
}
