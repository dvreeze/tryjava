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
 * "Step finder" for a naked quad in a column.
 * See <a href="https://www.learn-sudoku.com/naked-triplets.html">naked-triplet</a>.
 *
 * @author Chris de Vreeze
 */
public record NakedQuadInColumn(Grid startGrid, int columnIndex) implements StepFinderInGivenHouse {

    public record Quad(ImmutableSet<Position> positions, ImmutableSet<Integer> numbers) {

        public Quad {
            Preconditions.checkArgument(positions.size() == 4);
        }

        public boolean isNakedQuad() {
            return numbers.size() == 4;
        }
    }

    @Override
    public Column house() {
        return column();
    }

    public Column column() {
        return startGrid.column(columnIndex);
    }

    @Override
    public Optional<StepResult> findNextStepResult() {
        var column = column();

        var remainingUnfilledPositions =
                column.remainingUnfilledCells().stream()
                        .map(Cell::position)
                        .sorted(Position.comparator)
                        .collect(ImmutableList.toImmutableList());

        ImmutableMap<Position, ImmutableSet<Integer>> candidates =
                PencilMarks.candidates(startGrid, remainingUnfilledPositions);

        Optional<Quad> nakedQuadOption = Optional.empty();

        for (var pos1 : remainingUnfilledPositions) {
            var remainingPositions1 = remainingUnfilledPositions.stream().filter(p -> !p.equals(pos1)).collect(Collectors.toSet());
            for (var pos2 : remainingPositions1) {
                var remainingPositions2 = remainingPositions1.stream().filter(p -> !p.equals(pos2)).collect(Collectors.toSet());
                for (var pos3 : remainingPositions2) {
                    var remainingPositions3 = remainingPositions2.stream().filter(p -> !p.equals(pos3)).collect(Collectors.toSet());
                    for (var pos4 : remainingPositions3) {
                        var positions = ImmutableSet.of(pos1, pos2, pos3, pos4);
                        Preconditions.checkArgument(positions.size() == 4);

                        ImmutableSet<Integer> numbers = candidates.entrySet()
                                .stream()
                                .filter(kv -> positions.contains(kv.getKey()))
                                .flatMap(kv -> kv.getValue().stream())
                                .collect(ImmutableSet.toImmutableSet());
                        Quad quad = new Quad(positions, numbers);

                        if (quad.isNakedQuad()) {
                            nakedQuadOption = Optional.of(quad);
                            break;
                        }
                    }
                }
            }
        }

        if (nakedQuadOption.isPresent()) {
            Quad nakedQuad = nakedQuadOption.get();

            // The naked triplet is "stripped away" from the other unfilled cells
            ImmutableMap<Position, ImmutableSet<Integer>> adaptedCandidates =
                    candidates.entrySet().stream()
                            .filter(kv -> !nakedQuad.positions.contains(kv.getKey()))
                            .map(kv -> Map.entry(
                                    kv.getKey(),
                                    kv.getValue().stream()
                                            .filter(n -> !nakedQuad.numbers.contains(n))
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
                    "Filling cell in column after processing naked quad"
            )).map(step -> new StepResult(step, step.applyStep(startGrid)));
        } else {
            return Optional.empty();
        }
    }
}
