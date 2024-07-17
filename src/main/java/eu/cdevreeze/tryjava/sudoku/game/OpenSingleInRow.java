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
import eu.cdevreeze.tryjava.sudoku.model.Row;

import java.util.Optional;

/**
 * "Step finder" for an open single in a row.
 * See <a href="https://www.learn-sudoku.com/open-singles.html">open-singles</a>.
 *
 * @author Chris de Vreeze
 */
public record OpenSingleInRow(Grid startGrid, int rowIndex) implements StepFinderInGivenHouse {

    @Override
    public Row rowOrColumnOrRegion() {
        return row();
    }

    public Row row() {
        return startGrid.row(rowIndex);
    }

    @Override
    public Optional<StepResult> findNextStepResult() {
        var row = row();

        var remainingUnfilledCells = row.remainingUnfilledCells();
        var remainingUnusedNumbers = row.remainingUnusedNumbers();

        if (remainingUnfilledCells.size() == 1 && remainingUnusedNumbers.size() == 1) {
            return Optional.of(new Step(
                    remainingUnfilledCells.iterator().next().position(),
                    remainingUnusedNumbers.iterator().next(),
                    "Filling open single in row"
            )).map(step -> new StepResult(step, step.applyStep(startGrid)));
        } else {
            return Optional.empty();
        }
    }
}
