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

package eu.cdevreeze.tryjava.sudoku.game;

import eu.cdevreeze.tryjava.sudoku.model.Grid;
import eu.cdevreeze.tryjava.sudoku.model.GridApi;
import eu.cdevreeze.tryjava.sudoku.model.PencilMarks;
import eu.cdevreeze.tryjava.sudoku.model.Position;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Step in a Sudoku game that only updates the pencil marks.
 *
 * @author Chris de Vreeze
 */
public record UpdatePencilMarksStep(String description, PencilMarks pencilMarks) implements Step {

    @Override
    public Optional<Position> optionalPosition() {
        return Optional.empty();
    }

    @Override
    public OptionalInt optionalValue() {
        return OptionalInt.empty();
    }

    @Override
    public boolean isValidStep(Grid grid) {
        return true;
    }

    @Override
    public GridApi applyStep(GridApi grid) {
        return grid.withPencilMarks(pencilMarks);
    }
}
