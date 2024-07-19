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
import eu.cdevreeze.tryjava.sudoku.model.Column;
import eu.cdevreeze.tryjava.sudoku.model.GridApi;
import eu.cdevreeze.tryjava.sudoku.model.PencilMarks;
import eu.cdevreeze.tryjava.sudoku.model.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * "Step finder" for a hidden pair in a column.
 * See <a href="https://www.learn-sudoku.com/hidden-pairs.html">hidden-pair</a>.
 *
 * @author Chris de Vreeze
 */
public record HiddenPairInColumn(GridApi startGrid, int columnIndex) implements StepFinderInGivenHouse {

    public record HiddenPair(Position pos1, Position pos2, ImmutableSet<Integer> numbers) {

        public HiddenPair {
            Preconditions.checkArgument(!pos1.equals(pos2));
            Preconditions.checkArgument(numbers.size() == 2);
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
        PencilMarks pencilMarks = PencilMarks.forGrid(startGrid.grid())
                .updateIfPresent(startGrid.optionalPencilMarks());

        ImmutableMap<Position, ImmutableSet<Integer>> candidates =
                pencilMarks.cellCandidatesInColumn(columnIndex);

        Optional<HiddenPair> hiddenPairOption = findHiddenPair(candidates);

        if (hiddenPairOption.isEmpty()) {
            return Optional.empty();
        }

        HiddenPair hiddenPair = hiddenPairOption.get();

        return findNextStepResult(hiddenPair, candidates, pencilMarks);
    }

    private Optional<HiddenPair> findHiddenPair(ImmutableMap<Position, ImmutableSet<Integer>> candidates) {
        List<Integer> remainingNumbers = column().remainingUnusedNumbers().stream().sorted().toList();
        List<List<Integer>> numberPermutations =
                Permutations.orderedPermutations2(remainingNumbers, Comparator.comparingInt(v -> v));

        return numberPermutations.stream()
                .flatMap(numberGroup -> {
                    List<Position> positions = candidates.entrySet().stream()
                            .filter(kv -> kv.getValue().containsAll(numberGroup))
                            .map(Map.Entry::getKey)
                            .sorted(Position.comparator)
                            .toList();

                    if (positions.size() == 2) {
                        return Stream.of(
                                new HiddenPair(
                                        positions.get(0),
                                        positions.get(1),
                                        numberGroup.stream().collect(ImmutableSet.toImmutableSet())
                                ));
                    } else {
                        return Stream.empty();
                    }
                })
                .findFirst();
    }

    private Optional<StepResult> findNextStepResult(HiddenPair hiddenPair, ImmutableMap<Position, ImmutableSet<Integer>> candidates, PencilMarks pencilMarks) {
        // The hidden pair is retained in the same cells
        ImmutableMap<Position, ImmutableSet<Integer>> adaptedCandidates =
                candidates.entrySet().stream()
                        .filter(kv -> kv.getKey().equals(hiddenPair.pos1) || kv.getKey().equals(hiddenPair.pos2))
                        .map(kv -> Map.entry(
                                kv.getKey(),
                                kv.getValue().stream()
                                        .filter(hiddenPair.numbers::contains)
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
                "Filling cell in column after processing hidden pair"
        )).map(step -> new StepResult(step, step.applyStep(startGrid.withPencilMarks(adaptedPencilMarks))));
    }
}
