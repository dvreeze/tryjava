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
import eu.cdevreeze.tryjava.sudoku.model.Column;
import eu.cdevreeze.tryjava.sudoku.model.GridApi;
import eu.cdevreeze.tryjava.sudoku.model.PencilMarks;
import eu.cdevreeze.tryjava.sudoku.model.Position;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * "Step finder" for X-Wings in columns.
 * See <a href="https://www.learn-sudoku.com/x-wing.html">x-wing</a>.
 *
 * @author Chris de Vreeze
 */
public record XWingInColumns(GridApi startGrid) implements StepFinder {

    public record PotentialColumnInXWing(int columnIndex, int rowIndex1, int rowIndex2, int number) {

        public PotentialColumnInXWing {
            Preconditions.checkArgument(rowIndex1 < rowIndex2);
        }

        public boolean matches(PotentialColumnInXWing other) {
            return columnIndex != other.columnIndex &&
                    rowIndex1 == other.rowIndex1 &&
                    rowIndex2 == other.rowIndex2 &&
                    number == other.number;
        }
    }

    public record XWing(int colIndex1, int colIndex2, int rowIndex1, int rowIndex2, int number) {

        public XWing {
            Preconditions.checkArgument(colIndex1 != colIndex2);
        }
    }

    @Override
    public Optional<StepResult> findNextStepResult() {
        PencilMarks pencilMarks = PencilMarks.forGrid(startGrid.grid())
                .updateIfPresent(startGrid.optionalPencilMarks());
        var candidates = pencilMarks.cellCandidateNumbers();

        List<PotentialColumnInXWing> potentialColumnsInXWing =
                IntStream.range(0, Column.COLUMN_COUNT).boxed()
                        .flatMap(i -> {
                            var column = startGrid.grid().column(i);
                            var candidatesForColumn = pencilMarks.cellCandidatesInColumn(i);

                            return column.remainingUnusedNumbers().stream().flatMap(n -> {
                                var positions = candidatesForColumn.entrySet().stream()
                                        .filter(kv -> kv.getValue().contains(n))
                                        .map(Map.Entry::getKey)
                                        .sorted(Position.comparator)
                                        .toList();

                                if (positions.size() == 2) {
                                    return Stream.of(new PotentialColumnInXWing(
                                            i,
                                            positions.get(0).rowIndex(),
                                            positions.get(1).rowIndex(),
                                            n
                                    ));
                                } else {
                                    return Stream.empty();
                                }
                            });
                        })
                        .toList();

        Optional<XWing> optionalXWing = Optional.empty();

        for (var potentialColumnInXWing1 : potentialColumnsInXWing) {
            for (var potentialColumnInXWing2 : potentialColumnsInXWing) {
                if (potentialColumnInXWing1.matches(potentialColumnInXWing2)) {
                    optionalXWing = Optional.of(new XWing(
                            potentialColumnInXWing1.columnIndex,
                            potentialColumnInXWing2.columnIndex,
                            potentialColumnInXWing1.rowIndex1,
                            potentialColumnInXWing1.rowIndex2,
                            potentialColumnInXWing1.number
                    ));
                    break;
                }
            }
        }

        if (optionalXWing.isEmpty()) {
            return Optional.empty();
        }

        XWing xwing = optionalXWing.get();

        int number = xwing.number;

        // The X-Wing is "stripped away" from the 2 rows in the other columns
        ImmutableMap<Position, ImmutableSet<Integer>> adaptedCandidates =
                candidates.entrySet().stream()
                        .filter(kv -> kv.getKey().columnIndex() != xwing.colIndex1)
                        .filter(kv -> kv.getKey().columnIndex() != xwing.colIndex2)
                        .map(kv -> Map.entry(
                                kv.getKey(),
                                kv.getValue().stream()
                                        .filter(n -> n != number)
                                        .collect(ImmutableSet.toImmutableSet()))
                        )
                        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

        Optional<Map.Entry<Position, ImmutableSet<Integer>>> optCandidateToFillIn =
                adaptedCandidates.entrySet().stream()
                        .filter(kv -> kv.getValue().size() == 1)
                        .findFirst();

        PencilMarks adaptedPencilMarks = pencilMarks.update(adaptedCandidates);

        return optCandidateToFillIn.map(candidateToFillIn -> new SetCellValueStep(
                        candidateToFillIn.getKey(),
                        OptionalInt.of(candidateToFillIn.getValue().iterator().next()),
                        "Filling cell after processing X-Wing (column-based)"
                )).map(step -> new StepResult(step, step.applyStep(startGrid)))
                .or(() -> (!adaptedPencilMarks.limits(pencilMarks)) ? Optional.empty() :
                        Optional.of(new UpdatePencilMarksStep(
                                "Updating pencil marks after processing X-Wing (column-based)",
                                adaptedPencilMarks
                        )).map(step -> new StepResult(step, step.applyStep(startGrid)))
                );
    }
}
