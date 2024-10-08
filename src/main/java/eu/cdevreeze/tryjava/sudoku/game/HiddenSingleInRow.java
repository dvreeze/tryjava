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
import java.util.OptionalInt;

/**
 * "Step finder" for a hidden single in a row.
 * See <a href="https://www.learn-sudoku.com/hidden-singles.html">hidden-singles</a>.
 *
 * @author Chris de Vreeze
 */
public record HiddenSingleInRow(GridApi startGrid, int rowIndex) implements StepFinderInGivenHouse {

    @Override
    public Row house() {
        return row();
    }

    public Row row() {
        return startGrid.grid().row(rowIndex);
    }

    @Override
    public Optional<StepResult> findNextStepResult() {
        var row = row();

        var remainingUnfilledPositions =
                row.remainingUnfilledCells().stream()
                        .map(Cell::position)
                        .sorted(Position.comparator)
                        .collect(ImmutableList.toImmutableList());

        PencilMarks pencilMarks = PencilMarks.forGrid(startGrid.grid())
                .updateIfPresent(startGrid.optionalPencilMarks());

        ImmutableMap<Position, ImmutableSet<Integer>> candidates =
                pencilMarks.filterOnPositions(remainingUnfilledPositions.stream().collect(ImmutableSet.toImmutableSet()));

        OptionalInt hiddenSingleNumberOption =
                row.remainingUnusedNumbers().stream()
                        .mapToInt((n -> n))
                        .filter(n -> candidates.values().stream().filter(cds -> cds.contains(n)).count() == 1)
                        .findFirst();

        if (hiddenSingleNumberOption.isPresent()) {
            int hiddenSingleNumber = hiddenSingleNumberOption.getAsInt();

            Position position = candidates.entrySet().stream()
                    .filter(kv -> kv.getValue().contains(hiddenSingleNumber))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElseThrow();

            return Optional.of(new SetCellValueStep(
                    position,
                    OptionalInt.of(hiddenSingleNumber),
                    "Filling hidden single in row"
            )).map(step -> new StepResult(step, step.applyStep(startGrid.withPencilMarks(pencilMarks))));
        } else {
            return Optional.empty();
        }
    }
}
