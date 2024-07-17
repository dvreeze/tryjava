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
import eu.cdevreeze.tryjava.sudoku.model.Grid;
import eu.cdevreeze.tryjava.sudoku.model.Position;
import eu.cdevreeze.tryjava.sudoku.model.Row;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Step finder" for a hidden pair in a row.
 * See <a href="https://www.learn-sudoku.com/hidden-pairs.html">hidden-pair</a>.
 *
 * @author Chris de Vreeze
 */
public record HiddenPairInRow(Grid startGrid, int rowIndex) implements StepFinderInGivenHouse {

    private record NumberPosition(int number, Position position) {
    }

    public record HiddenPair(Position pos1, Position pos2, ImmutableSet<Integer> numbers) {

        public HiddenPair {
            Preconditions.checkArgument(!pos1.equals(pos2));
            Preconditions.checkArgument(numbers.size() == 2);
        }
    }

    @Override
    public Row house() {
        return row();
    }

    public Row row() {
        return startGrid.row(rowIndex);
    }

    @Override
    public Optional<StepResult> findNextStepResult() {
        ImmutableMap<Position, ImmutableSet<Integer>> candidates =
                CandidateMap.candidatesForRow(startGrid, rowIndex);

        Optional<HiddenPair> hiddenPairOption = findHiddenPair(candidates);

        if (hiddenPairOption.isEmpty()) {
            return Optional.empty();
        }

        HiddenPair hiddenPair = hiddenPairOption.get();

        return findNextStepResult(hiddenPair, candidates);
    }

    private Optional<HiddenPair> findHiddenPair(ImmutableMap<Position, ImmutableSet<Integer>> candidates) {
        Map<Integer, Set<Position>> numberPositions =
                candidates.entrySet().stream()
                        .flatMap(kv -> kv.getValue().stream().map(n -> new NumberPosition(n, kv.getKey())))
                        .collect(Collectors.groupingBy(
                                        NumberPosition::number,
                                        Collectors.mapping(NumberPosition::position, Collectors.toSet())
                                )
                        );

        Map<Integer, Set<Position>> relevantNumberPositions =
                numberPositions.entrySet().stream()
                        .filter(kv -> kv.getValue().size() == 2)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Optional<HiddenPair> hiddenPairOption = Optional.empty();
        for (var numberPosition1 : relevantNumberPositions.entrySet()) {
            for (var numberPosition2 : relevantNumberPositions.entrySet()) {
                if (numberPosition1.getKey().intValue() != numberPosition2.getKey().intValue()) {
                    if (numberPosition1.getValue().equals(numberPosition2.getValue())) {
                        var sortedPositions = numberPosition1.getValue().stream().sorted(Position.comparator).toList();
                        Preconditions.checkArgument(sortedPositions.size() == 2);

                        hiddenPairOption = Optional.of(
                                new HiddenPair(
                                        sortedPositions.get(0),
                                        sortedPositions.get(1),
                                        ImmutableSet.of(numberPosition1.getKey(), numberPosition2.getKey())
                                )
                        );
                        break;
                    }
                }
            }
        }

        return hiddenPairOption;
    }

    private Optional<StepResult> findNextStepResult(HiddenPair hiddenPair, ImmutableMap<Position, ImmutableSet<Integer>> candidates) {
        // The hidden pair is "stripped away" from the same cells
        ImmutableMap<Position, ImmutableSet<Integer>> adaptedCandidates =
                candidates.entrySet().stream()
                        .filter(kv -> kv.getKey().equals(hiddenPair.pos1) || kv.getKey().equals(hiddenPair.pos2))
                        .map(kv -> Map.entry(
                                kv.getKey(),
                                kv.getValue().stream()
                                        .filter(n -> !hiddenPair.numbers.contains(n))
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
                "Filling cell in row after processing hidden pair"
        )).map(step -> new StepResult(step, step.applyStep(startGrid)));
    }
}
