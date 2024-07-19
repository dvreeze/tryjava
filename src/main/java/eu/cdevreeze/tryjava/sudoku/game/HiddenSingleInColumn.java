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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import eu.cdevreeze.tryjava.sudoku.model.*;

import java.util.Map;
import java.util.Optional;

/**
 * "Step finder" for a hidden single in a column.
 * See <a href="https://www.learn-sudoku.com/hidden-singles.html">hidden-singles</a>.
 *
 * @author Chris de Vreeze
 */
public record HiddenSingleInColumn(GridApi startGrid, int columnIndex) implements StepFinderInGivenHouse {

    @Override
    public Column house() {
        return column();
    }

    public Column column() {
        return startGrid.grid().column(columnIndex);
    }

    @Override
    public Optional<StepResult> findNextStepResult() {
        var column = column();

        var remainingUnfilledPositions =
                column.remainingUnfilledCells().stream()
                        .map(Cell::position)
                        .sorted(Position.comparator)
                        .collect(ImmutableList.toImmutableList());

        PencilMarks pencilMarks = PencilMarks.forGrid(startGrid.grid())
                .updateIfPresent(startGrid.optionalPencilMarks());

        ImmutableMap<Position, ImmutableSet<Integer>> candidates =
                pencilMarks.filterOnPositions(remainingUnfilledPositions.stream().collect(ImmutableSet.toImmutableSet()));

        Optional<Integer> hiddenSingleNumberOption =
                column.remainingUnusedNumbers().stream()
                        .filter(n -> candidates.values().stream().filter(cds -> cds.contains(n)).count() == 1)
                        .findFirst();

        if (hiddenSingleNumberOption.isPresent()) {
            int hiddenSingleNumber = hiddenSingleNumberOption.get();

            Position position = candidates.entrySet().stream()
                    .filter(kv -> kv.getValue().contains(hiddenSingleNumber))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElseThrow();

            return Optional.of(new Step(
                    position,
                    hiddenSingleNumber,
                    "Filling hidden single in column"
            )).map(step -> new StepResult(step, step.applyStep(startGrid.withPencilMarks(pencilMarks))));
        } else {
            return Optional.empty();
        }
    }
}
