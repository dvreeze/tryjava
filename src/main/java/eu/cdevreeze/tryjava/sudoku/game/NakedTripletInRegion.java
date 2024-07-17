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
import eu.cdevreeze.tryjava.sudoku.model.*;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * "Step finder" for a naked triplet in a region.
 * See <a href="https://www.learn-sudoku.com/naked-triplets.html">naked-triplet</a>.
 *
 * @author Chris de Vreeze
 */
public record NakedTripletInRegion(Grid startGrid,
                                   RegionPosition regionPosition) implements StepFinderInGivenHouse {

    public record Triplet(ImmutableSet<Position> positions, ImmutableSet<Integer> numbers) {

        public Triplet {
            Preconditions.checkArgument(positions.size() == 3);
        }

        public boolean isNakedTriplet() {
            return numbers.size() == 3;
        }
    }

    @Override
    public Region house() {
        return region();
    }

    public Region region() {
        return startGrid.region(regionPosition);
    }

    @Override
    public Optional<StepResult> findNextStepResult() {
        var region = region();

        var remainingUnfilledPositions =
                region.remainingUnfilledCells().stream()
                        .map(Cell::position)
                        .sorted(Position.comparator)
                        .collect(ImmutableList.toImmutableList());

        ImmutableMap<Position, ImmutableSet<Integer>> candidates =
                PencilMarks.candidates(startGrid, remainingUnfilledPositions);

        Optional<Triplet> nakedTripletOption = Optional.empty();

        for (var pos1 : remainingUnfilledPositions) {
            var remainingPositions1 = remainingUnfilledPositions.stream().filter(p -> !p.equals(pos1)).collect(Collectors.toSet());
            for (var pos2 : remainingPositions1) {
                var remainingPositions2 = remainingPositions1.stream().filter(p -> !p.equals(pos2)).collect(Collectors.toSet());
                for (var pos3 : remainingPositions2) {
                    var positions = ImmutableSet.of(pos1, pos2, pos3);
                    Preconditions.checkArgument(positions.size() == 3);

                    ImmutableSet<Integer> numbers = candidates.entrySet()
                            .stream()
                            .filter(kv -> positions.contains(kv.getKey()))
                            .flatMap(kv -> kv.getValue().stream())
                            .collect(ImmutableSet.toImmutableSet());
                    Triplet triplet = new Triplet(positions, numbers);

                    if (triplet.isNakedTriplet()) {
                        nakedTripletOption = Optional.of(triplet);
                        break;
                    }
                }
            }
        }

        if (nakedTripletOption.isPresent()) {
            Triplet nakedTriplet = nakedTripletOption.get();

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

            return optCandidateToFillIn.map(candidateToFillIn -> new Step(
                    candidateToFillIn.getKey(),
                    candidateToFillIn.getValue().iterator().next(),
                    "Filling cell in region after processing naked triplet"
            )).map(step -> new StepResult(step, step.applyStep(startGrid)));
        } else {
            return Optional.empty();
        }
    }
}
