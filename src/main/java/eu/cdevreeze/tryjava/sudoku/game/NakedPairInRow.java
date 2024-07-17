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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * "Step finder" for a naked pair in a row.
 * See <a href="https://www.learn-sudoku.com/naked-pairs.html">naked-pair</a>.
 *
 * @author Chris de Vreeze
 */
public record NakedPairInRow(GridApi startGrid, int rowIndex) implements StepFinderInGivenHouse {

    public record NakedPair(Position pos1, Position pos2, ImmutableSet<Integer> numbers) {

        public NakedPair {
            Preconditions.checkArgument(numbers.size() == 2);
        }
    }

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

        ImmutableMap<Position, ImmutableSet<Integer>> candidates =
                PencilMarks.update(
                        PencilMarks.candidates(startGrid.grid(), remainingUnfilledPositions),
                        startGrid.optionalPencilMarks().map(PencilMarks::cellCandidateNumbers).orElse(ImmutableMap.of())
                );

        Optional<NakedPair> nakedPairOption =
                candidates.entrySet().stream()
                        .filter(kv -> kv.getValue().size() == 2)
                        .collect(Collectors.groupingBy(
                                        Map.Entry::getValue,
                                        Collectors.mapping(Map.Entry::getKey, Collectors.toSet())
                                )
                        )
                        .entrySet()
                        .stream()
                        .filter(kv -> kv.getKey().size() == 2 && kv.getValue().size() == 2)
                        .findFirst()
                        .map(kv -> {
                            List<Position> positions = kv.getValue().stream().sorted(Position.comparator).toList();
                            ImmutableSet<Integer> numbers = kv.getKey();
                            return new NakedPair(positions.get(0), positions.get(1), numbers);
                        });

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

            return optCandidateToFillIn.map(candidateToFillIn -> new Step(
                    candidateToFillIn.getKey(),
                    candidateToFillIn.getValue().iterator().next(),
                    "Filling cell in row after processing naked pair"
            )).map(step -> new StepResult(step, step.applyStep(startGrid.withPencilMarks(new PencilMarks(candidates)))));
        } else {
            return Optional.empty();
        }
    }
}
