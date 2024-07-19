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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import eu.cdevreeze.tryjava.sudoku.model.*;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Step finder" for "(inverse) omission" (column-based).
 * See <a href="https://www.learn-sudoku.com/omission.html">omission</a>.
 *
 * @author Chris de Vreeze
 */
public record InverseIntersectionWithColumn(GridApi startGrid,
                                            RegionPosition regionPosition) implements StepFinderInGivenHouse {

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

        PencilMarks pencilMarks = PencilMarks.forGrid(startGrid.grid())
                .updateIfPresent(startGrid.optionalPencilMarks());

        return region.columnIndicesInGrid().stream().sorted().flatMap(i -> findNextStepResult(i, pencilMarks).stream()).findFirst();
    }

    public Optional<StepResult> findNextStepResult(int columnIndex, PencilMarks pencilMarks) {
        if (!region().columnIndicesInGrid().contains(columnIndex)) {
            return Optional.empty();
        }

        Set<Integer> candidateNumbersInRegionInColumn = pencilMarks.cellCandidatesInRegion(regionPosition)
                .entrySet()
                .stream()
                .filter(kv -> kv.getKey().columnIndex() == columnIndex)
                .flatMap(kv -> kv.getValue().stream())
                .collect(Collectors.toSet());

        Set<Integer> candidateNumbersInRegionOutsideColumn = pencilMarks.cellCandidatesInRegion(regionPosition)
                .entrySet()
                .stream()
                .filter(kv -> kv.getKey().columnIndex() != columnIndex)
                .flatMap(kv -> kv.getValue().stream())
                .collect(Collectors.toSet());

        Set<Integer> candidateNumbersInRegionThatAreOnlyInColumn = candidateNumbersInRegionInColumn.stream()
                .filter(n -> !candidateNumbersInRegionOutsideColumn.contains(n))
                .collect(Collectors.toSet());

        if (candidateNumbersInRegionThatAreOnlyInColumn.isEmpty()) {
            return Optional.empty();
        }

        int candidateNumber = candidateNumbersInRegionThatAreOnlyInColumn.iterator().next();

        var candidates = pencilMarks.cellCandidatesInColumn(columnIndex);

        // The candidate number is "stripped away" from the other cells in the column
        ImmutableMap<Position, ImmutableSet<Integer>> adaptedCandidates =
                candidates.entrySet().stream()
                        .filter(kv -> !region().positionsInGrid().contains(kv.getKey()))
                        .map(kv -> Map.entry(
                                kv.getKey(),
                                kv.getValue().stream()
                                        .filter(n -> n != candidateNumber)
                                        .collect(ImmutableSet.toImmutableSet()))
                        )
                        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

        Optional<Map.Entry<Position, ImmutableSet<Integer>>> optCandidateToFillIn =
                adaptedCandidates.entrySet().stream()
                        .filter(kv -> kv.getValue().size() == 1)
                        .findFirst();

        PencilMarks adaptedPencilMarks = pencilMarks.update(adaptedCandidates);

        return optCandidateToFillIn.map(candidateToFillIn -> new Step(
                candidateToFillIn.getKey(),
                OptionalInt.of(candidateToFillIn.getValue().iterator().next()),
                "Filling cell in row after processing \"inverse omission\" (column-based)"
        )).map(step -> new StepResult(step, step.applyStep(startGrid.withPencilMarks(adaptedPencilMarks))));
    }
}
