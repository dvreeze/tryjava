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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import eu.cdevreeze.tryjava.sudoku.internal.Permutations;
import eu.cdevreeze.tryjava.sudoku.model.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * "Step finder" for a naked triplet in a region.
 * See <a href="https://www.learn-sudoku.com/naked-triplets.html">naked-triplet</a>.
 *
 * @author Chris de Vreeze
 */
public record NakedTripletInRegion(GridApi startGrid,
                                   RegionPosition regionPosition) implements StepFinderInGivenHouse {

    public record NakedTriplet(ImmutableSet<Position> positions, ImmutableSet<Integer> numbers) {

        public NakedTriplet {
            Preconditions.checkArgument(positions.size() == 3);
            Preconditions.checkArgument(numbers.size() == 3);
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
        var region = region();

        var remainingUnfilledPositions =
                region.remainingUnfilledCells().stream()
                        .map(Cell::position)
                        .sorted(Position.comparator)
                        .collect(ImmutableList.toImmutableList());

        PencilMarks pencilMarks = PencilMarks.forGrid(startGrid.grid())
                .updateIfPresent(startGrid.optionalPencilMarks());

        ImmutableMap<Position, ImmutableSet<Integer>> candidates =
                pencilMarks.filterOnPositions(remainingUnfilledPositions.stream().collect(ImmutableSet.toImmutableSet()));

        List<Integer> remainingNumbers = region.remainingUnusedNumbers().stream().sorted().toList();
        List<List<Integer>> numberPermutations =
                Permutations.orderedPermutations3(remainingNumbers, Comparator.comparingInt(v -> v));

        Optional<NakedTriplet> nakedTripletOption =
                numberPermutations.stream()
                        .flatMap(numberGroup -> {
                            List<Position> positions = candidates.entrySet().stream()
                                    .filter(kv -> Sets.difference(kv.getValue(), ImmutableSet.copyOf(numberGroup)).isEmpty())
                                    .map(Map.Entry::getKey)
                                    .sorted(Position.comparator)
                                    .toList();

                            if (positions.size() == 3) {
                                return Stream.of(
                                        new NakedTriplet(
                                                positions.stream().collect(ImmutableSet.toImmutableSet()),
                                                numberGroup.stream().collect(ImmutableSet.toImmutableSet())
                                        ));
                            } else {
                                return Stream.empty();
                            }
                        })
                        .findFirst();

        if (nakedTripletOption.isPresent()) {
            NakedTriplet nakedTriplet = nakedTripletOption.get();

            // The naked triplet is "stripped away" from the other unfilled cells
            ImmutableMap<Position, ImmutableSet<Integer>> adaptedCandidates =
                    candidates.entrySet().stream()
                            .filter(kv -> !nakedTriplet.positions.contains(kv.getKey()))
                            .map(kv -> Map.entry(
                                    kv.getKey(),
                                    kv.getValue().stream()
                                            .filter(n -> !nakedTriplet.numbers.contains(n))
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
                    "Filling cell in region after processing naked triplet"
            )).map(step -> new StepResult(step, step.applyStep(startGrid.withPencilMarks(adaptedPencilMarks))));
        } else {
            return Optional.empty();
        }
    }
}
