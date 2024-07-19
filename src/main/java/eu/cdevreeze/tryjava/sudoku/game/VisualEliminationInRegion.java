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

import com.google.common.base.Preconditions;
import eu.cdevreeze.tryjava.sudoku.model.Cell;
import eu.cdevreeze.tryjava.sudoku.model.GridApi;
import eu.cdevreeze.tryjava.sudoku.model.Region;
import eu.cdevreeze.tryjava.sudoku.model.RegionPosition;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * "Step finder" in a Sudoku game using visual elimination in a region.
 * See <a href="https://www.learn-sudoku.com/visual-elimination.html">visual-elimination</a>.
 *
 * @author Chris de Vreeze
 */
public record VisualEliminationInRegion(GridApi startGrid, RegionPosition regionPosition,
                                        int number) implements StepFinderInGivenHouse {

    public VisualEliminationInRegion {
        Preconditions.checkArgument(number >= 1 && number <= 9);
    }

    @Override
    public Region house() {
        return region();
    }

    public Region region() {
        return startGrid.grid().region(regionPosition);
    }

    @Override
    public Optional<StepResult> findNextStepResult() {
        var region = region();

        if (!region.remainingUnusedNumbers().contains(number)) {
            return Optional.empty();
        }

        var remainingUnfilledCells = region.remainingUnfilledCells();
        var potentiallyMatchingUnfilledCells = remainingUnfilledCells.stream()
                .filter(this::isCandidateCell)
                .filter(cell -> startGrid().withCellValue(cell.position(), OptionalInt.of(number)).grid().isValid())
                .collect(Collectors.toSet());

        if (potentiallyMatchingUnfilledCells.size() == 1) {
            return Optional.of(new Step(
                    potentiallyMatchingUnfilledCells.iterator().next().position(),
                    OptionalInt.of(number),
                    "Filling given number in last matching cell in region"
            )).map(step -> new StepResult(step, step.applyStep(startGrid)));
        } else {
            return Optional.empty();
        }
    }

    private boolean isCandidateCell(Cell cell) {
        return startGrid.optionalPencilMarks().stream().allMatch(pm -> {
            if (pm.cellCandidateNumbers().containsKey(cell.position())) {
                return Objects.requireNonNull(pm.cellCandidateNumbers().get(cell.position())).contains(number);
            } else {
                return true;
            }
        });
    }
}
