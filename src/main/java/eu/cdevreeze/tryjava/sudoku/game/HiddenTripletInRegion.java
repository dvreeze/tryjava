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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import eu.cdevreeze.tryjava.sudoku.internal.Permutations;
import eu.cdevreeze.tryjava.sudoku.model.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * "Step finder" for a hidden triplet in a region.
 * See <a href="https://www.learn-sudoku.com/hidden-triplets.html">hidden-triplet</a>.
 *
 * @author Chris de Vreeze
 */
public record HiddenTripletInRegion(GridApi startGrid,
                                    RegionPosition regionPosition) implements StepFinderInGivenHouse {

    public record HiddenTriplet(Position pos1, Position pos2, Position pos3, ImmutableSet<Integer> numbers) {

        public HiddenTriplet {
            Preconditions.checkArgument(Stream.of(pos1, pos2, pos3).distinct().count() == 3);
            Preconditions.checkArgument(numbers.size() == 3);
        }

        public ImmutableSet<Position> positions() {
            return ImmutableSet.of(pos1, pos2, pos3);
        }
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
        PencilMarks pencilMarks = PencilMarks.forGrid(startGrid.grid())
                .updateIfPresent(startGrid.optionalPencilMarks());

        ImmutableMap<Position, ImmutableSet<Integer>> candidates =
                pencilMarks.cellCandidatesInRegion(regionPosition);

        Optional<HiddenTriplet> hiddenTripletOption = findHiddenTriplet(candidates);

        if (hiddenTripletOption.isEmpty()) {
            return Optional.empty();
        }

        HiddenTriplet hiddenTriplet = hiddenTripletOption.get();

        return findNextStepResult(hiddenTriplet, candidates, pencilMarks);
    }

    private Optional<HiddenTriplet> findHiddenTriplet(ImmutableMap<Position, ImmutableSet<Integer>> candidates) {
        List<Integer> remainingNumbers = region().remainingUnusedNumbers().stream().sorted().toList();
        List<List<Integer>> numberPermutations =
                Permutations.orderedPermutations3(remainingNumbers, Comparator.comparingInt(v -> v));

        return numberPermutations.stream()
                .flatMap(numberGroup -> {
                    List<Position> positions = candidates.entrySet().stream()
                            .filter(kv -> kv.getValue().containsAll(numberGroup))
                            .map(Map.Entry::getKey)
                            .sorted(Position.comparator)
                            .toList();

                    if (positions.size() == 3) {
                        return Stream.of(
                                new HiddenTriplet(
                                        positions.get(0),
                                        positions.get(1),
                                        positions.get(2),
                                        numberGroup.stream().collect(ImmutableSet.toImmutableSet())
                                ));
                    } else {
                        return Stream.empty();
                    }
                })
                .findFirst();
    }

    private Optional<StepResult> findNextStepResult(HiddenTriplet hiddenTriplet, ImmutableMap<Position, ImmutableSet<Integer>> candidates, PencilMarks pencilMarks) {
        // The hidden triplet is retained in the same cells
        ImmutableMap<Position, ImmutableSet<Integer>> adaptedCandidates =
                candidates.entrySet().stream()
                        .filter(kv -> hiddenTriplet.positions().contains(kv.getKey()))
                        .map(kv -> Map.entry(
                                kv.getKey(),
                                kv.getValue().stream()
                                        .filter(hiddenTriplet.numbers::contains)
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
                "Filling cell in region after processing hidden triplet"
        )).map(step -> new StepResult(step, step.applyStep(startGrid.withPencilMarks(adaptedPencilMarks))));
    }
}
