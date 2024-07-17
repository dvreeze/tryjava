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
import eu.cdevreeze.tryjava.sudoku.model.Grid;
import eu.cdevreeze.tryjava.sudoku.model.Position;
import eu.cdevreeze.tryjava.sudoku.model.Region;
import eu.cdevreeze.tryjava.sudoku.model.RegionPosition;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Step finder" for "omission" (row-based).
 * See <a href="https://www.learn-sudoku.com/omission.html">omission</a>.
 *
 * @author Chris de Vreeze
 */
public record IntersectionWithRow(Grid startGrid,
                                  RegionPosition regionPosition) implements StepFinderInGivenRowOrColumnOrRegion {

    @Override
    public Region rowOrColumnOrRegion() {
        return region();
    }

    public Region region() {
        return startGrid.region(regionPosition);
    }

    @Override
    public Optional<StepResult> findNextStepResult() {
        var region = region();

        var candidateMap = CandidateMap.forGrid(startGrid);

        return region.rowIndicesInGrid().stream().sorted().flatMap(i -> findNextStepResult(i, candidateMap).stream()).findFirst();
    }

    public Optional<StepResult> findNextStepResult(int rowIndex, CandidateMap candidateMap) {
        if (!region().rowIndicesInGrid().contains(rowIndex)) {
            return Optional.empty();
        }

        Set<Integer> candidateNumbersInRowRegionIntersection = candidateMap.cellCandidatesInRegion(regionPosition)
                .entrySet()
                .stream()
                .filter(kv -> kv.getKey().rowIndex() == rowIndex)
                .flatMap(kv -> kv.getValue().stream())
                .collect(Collectors.toSet());

        Set<Integer> candidateNumbersInRowExcludingRegion = candidateMap.cellCandidatesInRow(rowIndex)
                .entrySet()
                .stream()
                .filter(kv -> kv.getKey().rowIndex() == rowIndex)
                .filter(kv -> !region().positionsInGrid().contains(kv.getKey()))
                .flatMap(kv -> kv.getValue().stream())
                .collect(Collectors.toSet());

        Set<Integer> candidateNumbersInRowThatAreOnlyInRegion = candidateNumbersInRowRegionIntersection.stream()
                .filter(n -> !candidateNumbersInRowExcludingRegion.contains(n))
                .collect(Collectors.toSet());

        if (candidateNumbersInRowThatAreOnlyInRegion.isEmpty()) {
            return Optional.empty();
        }

        int candidateNumber = candidateNumbersInRowThatAreOnlyInRegion.iterator().next();

        var candidates = candidateMap.cellCandidatesInRegion(regionPosition);

        // The candidate number is "stripped away" from the other rows in the region
        ImmutableMap<Position, ImmutableSet<Integer>> adaptedCandidates =
                candidates.entrySet().stream()
                        .filter(kv -> kv.getKey().rowIndex() != rowIndex)
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

        return optCandidateToFillIn.map(candidateToFillIn -> new Step(
                candidateToFillIn.getKey(),
                candidateToFillIn.getValue().iterator().next(),
                "Filling cell in region after processing omission (row-based)"
        )).map(step -> new StepResult(step, step.applyStep(startGrid)));
    }
}
