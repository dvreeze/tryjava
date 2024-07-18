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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * "Step finder" for a naked pair in a column.
 * See <a href="https://www.learn-sudoku.com/naked-pairs.html">naked-pair</a>.
 *
 * @author Chris de Vreeze
 */
public record NakedPairInColumn(GridApi startGrid, int columnIndex) implements StepFinderInGivenHouse {

    public record NakedPair(Position pos1, Position pos2, ImmutableSet<Integer> numbers) {

        public NakedPair {
            Preconditions.checkArgument(numbers.size() == 2);
            Preconditions.checkArgument(Stream.of(pos1, pos2).distinct().count() == 2);
        }

        public ImmutableSet<Position> positions() {
            return ImmutableSet.of(pos1, pos2);
        }
    }

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

        PencilMarks pencilMarks =
                new PencilMarks(PencilMarks.candidates(startGrid.grid(), remainingUnfilledPositions))
                        .updateIfPresent(startGrid.optionalPencilMarks());

        ImmutableMap<Position, ImmutableSet<Integer>> candidates =
                pencilMarks.filterOnPositions(remainingUnfilledPositions.stream().collect(ImmutableSet.toImmutableSet()));

        List<Integer> remainingNumbers = column.remainingUnusedNumbers().stream().sorted().toList();
        List<List<Integer>> numberPermutations =
                Permutations.orderedPermutations2(remainingNumbers, Comparator.comparingInt(v -> v));

        Optional<NakedPair> nakedPairOption =
                numberPermutations.stream()
                        .flatMap(numberGroup -> {
                            List<Position> positions = candidates.entrySet().stream()
                                    .filter(kv -> Sets.difference(kv.getValue(), ImmutableSet.copyOf(numberGroup)).isEmpty())
                                    .map(Map.Entry::getKey)
                                    .sorted(Position.comparator)
                                    .toList();

                            if (positions.size() == 2) {
                                return Stream.of(
                                        new NakedPair(
                                                positions.get(0),
                                                positions.get(1),
                                                numberGroup.stream().collect(ImmutableSet.toImmutableSet())
                                        ));
                            } else {
                                return Stream.empty();
                            }
                        })
                        .findFirst();

        if (nakedPairOption.isPresent()) {
            NakedPair nakedPair = nakedPairOption.get();

            // The naked pair is "stripped away" from the other unfilled cells
            ImmutableMap<Position, ImmutableSet<Integer>> adaptedCandidates =
                    candidates.entrySet().stream()
                            .filter(kv -> !kv.getKey().equals(nakedPair.pos1) && !kv.getKey().equals(nakedPair.pos2))
                            .map(kv -> Map.entry(
                                    kv.getKey(),
                                    kv.getValue().stream()
                                            .filter(n -> !nakedPair.numbers.contains(n))
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
                    candidateToFillIn.getValue().iterator().next(),
                    "Filling cell in column after processing naked pair"
            )).map(step -> new StepResult(step, step.applyStep(startGrid.withPencilMarks(adaptedPencilMarks))));
        } else {
            return Optional.empty();
        }
    }
}
