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
import eu.cdevreeze.tryjava.sudoku.model.Position;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Step in a Sudoku game. A step typically but not necessarily sets a value at an empty position.
 * If it does not set a value, it typically has the side effect of updating the pencil marks.
 *
 * @author Chris de Vreeze
 */
public sealed interface Step permits SetCellValueStep, UpdatePencilMarksStep {

    Optional<Position> optionalPosition();

    OptionalInt optionalValue();

    String description();

    boolean isValidStep(Grid grid);

    GridApi applyStep(GridApi grid);
}
