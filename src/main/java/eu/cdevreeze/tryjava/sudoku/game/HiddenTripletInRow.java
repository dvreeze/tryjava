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
import eu.cdevreeze.tryjava.sudoku.model.CandidateMap;
import eu.cdevreeze.tryjava.sudoku.model.Grid;
import eu.cdevreeze.tryjava.sudoku.model.Position;
import eu.cdevreeze.tryjava.sudoku.model.Row;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * "Step finder" for a hidden triplet in a row.
 * See <a href="https://www.learn-sudoku.com/hidden-triplets.html">hidden-triplet</a>.
 *
 * @author Chris de Vreeze
 */
public record HiddenTripletInRow(Grid startGrid, int rowIndex) implements StepFinderInGivenHouse {

    private record NumberPosition(int number, Position position) {
    }

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

        Optional<HiddenTriplet> hiddenTripletOption = findHiddenTriplet(candidates);

        if (hiddenTripletOption.isEmpty()) {
            return Optional.empty();
        }

        HiddenTriplet hiddenTriplet = hiddenTripletOption.get();

        return findNextStepResult(hiddenTriplet, candidates);
    }

    private Optional<HiddenTriplet> findHiddenTriplet(ImmutableMap<Position, ImmutableSet<Integer>> candidates) {
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
                        .filter(kv -> !kv.getValue().isEmpty() && kv.getValue().size() <= 3)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Optional<HiddenTriplet> hiddenTripletOption = Optional.empty();

        for (var numberPosition1 : relevantNumberPositions.entrySet()) {
            Map<Integer, Set<Position>> relevantNumberPositions1 =
                    relevantNumberPositions.entrySet().stream()
                            .filter(kv -> !kv.getKey().equals(numberPosition1.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            for (var numberPosition2 : relevantNumberPositions1.entrySet()) {
                Map<Integer, Set<Position>> relevantNumberPositions2 =
                        relevantNumberPositions1.entrySet().stream()
                                .filter(kv -> !kv.getKey().equals(numberPosition2.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                for (var numberPosition3 : relevantNumberPositions2.entrySet()) {
                    Set<Position> positions = Stream.of(numberPosition1, numberPosition2, numberPosition3)
                            .flatMap(kv -> kv.getValue().stream())
                            .collect(Collectors.toSet());

                    if (positions.size() == 3) {
                        var sortedPositions = positions.stream().sorted(Position.comparator).toList();
                        Preconditions.checkArgument(sortedPositions.size() == 3);

                        hiddenTripletOption = Optional.of(
                                new HiddenTriplet(
                                        sortedPositions.get(0),
                                        sortedPositions.get(1),
                                        sortedPositions.get(2),
                                        ImmutableSet.of(numberPosition1.getKey(), numberPosition2.getKey(), numberPosition3.getKey())
                                )
                        );
                        break;
                    }
                }
            }
        }

        return hiddenTripletOption;
    }

    private Optional<StepResult> findNextStepResult(HiddenTriplet hiddenTriplet, ImmutableMap<Position, ImmutableSet<Integer>> candidates) {
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

        return optCandidateToFillIn.map(candidateToFillIn -> new Step(
                candidateToFillIn.getKey(),
                candidateToFillIn.getValue().iterator().next(),
                "Filling cell in row after processing hidden triplet"
        )).map(step -> new StepResult(step, step.applyStep(startGrid)));
    }
}
