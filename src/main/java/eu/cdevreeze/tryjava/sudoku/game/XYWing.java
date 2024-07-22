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
import com.google.common.collect.Sets;
import eu.cdevreeze.tryjava.sudoku.internal.Permutations;
import eu.cdevreeze.tryjava.sudoku.model.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * "Step finder" for XY-wings.
 * See <a href="https://www.learn-sudoku.com/xy-wing.html">xy-wing</a>.
 *
 * @author Chris de Vreeze
 */
public record XYWing(GridApi startGrid) implements StepFinder {

    public record XYWingOccurrence(PencilMarksCell middle, PencilMarksCell wing1, PencilMarksCell wing2) {

        public XYWingOccurrence {
            Preconditions.checkArgument(Stream.of(middle, wing1, wing2).map(PencilMarksCell::position).distinct().count() == 3);
            Preconditions.checkArgument(Stream.of(middle, wing1, wing2).allMatch(c -> c.candidateValues().size() == 2));
            Preconditions.checkArgument(Stream.of(middle, wing1, wing2).flatMap(c -> c.candidateValues().stream()).distinct().count() == 3);
            Preconditions.checkArgument(intersect(middle, wing1));
            Preconditions.checkArgument(intersect(middle, wing2));
            Preconditions.checkArgument(!intersect(wing1, wing2));
            Preconditions.checkArgument(Sets.intersection(wing1.candidateValues(), wing2.candidateValues()).size() == 1);

            int candidateNumberSharedByWings = Sets.intersection(wing1.candidateValues(), wing2.candidateValues()).iterator().next();

            Preconditions.checkArgument(!middle.candidateValues().contains(candidateNumberSharedByWings));
            Preconditions.checkArgument(Sets.intersection(middle.candidateValues(), wing1.candidateValues()).size() == 1);
            Preconditions.checkArgument(Sets.intersection(middle.candidateValues(), wing2.candidateValues()).size() == 1);
            Preconditions.checkArgument(!wing1.candidateValues().equals(wing2.candidateValues()));
        }

        public int candidateNumberSharedByWings() {
            return wing1.candidateValues().stream().filter(n -> wing2.candidateValues().contains(n)).findFirst().orElseThrow();
        }

        public static Optional<XYWingOccurrence> optionallyOf(List<PencilMarksCell> pencilMarksCells) {
            if (pencilMarksCells.size() != 3) {
                return Optional.empty();
            }
            if (pencilMarksCells.stream().map(PencilMarksCell::position).distinct().count() != 3) {
                return Optional.empty();
            }
            if (pencilMarksCells.stream().anyMatch(c -> c.candidateValues().size() != 2)) {
                return Optional.empty();
            }
            if (pencilMarksCells.stream().flatMap(c -> c.candidateValues().stream()).distinct().count() != 3) {
                return Optional.empty();
            }

            if (pencilMarksCells.stream().anyMatch(middle -> {
                List<PencilMarksCell> wings = pencilMarksCells.stream().filter(c -> !c.position().equals(middle.position())).toList();
                Preconditions.checkArgument(wings.size() == 2);
                return intersect(middle, wings.getFirst()) &&
                        intersect(middle, wings.get(1)) &&
                        !intersect(wings.getFirst(), wings.get(1));
            })) {
                PencilMarksCell middle = pencilMarksCells.stream()
                        .filter(m -> pencilMarksCells.stream().allMatch(c -> c.equals(m) || intersect(m, c)))
                        .findFirst()
                        .orElseThrow();
                List<PencilMarksCell> wings = pencilMarksCells.stream().filter(c -> !c.equals(middle)).toList();
                Preconditions.checkArgument(wings.size() == 2);
                PencilMarksCell wing1 = wings.stream().min(Comparator.comparing(PencilMarksCell::position, Position.comparator)).orElseThrow();
                PencilMarksCell wing2 = wings.stream().filter(c -> !c.equals(wing1)).findFirst().orElseThrow();

                if (Sets.intersection(middle.candidateValues(), wing1.candidateValues()).size() != 1) {
                    return Optional.empty();
                }
                if (Sets.intersection(middle.candidateValues(), wing2.candidateValues()).size() != 1) {
                    return Optional.empty();
                }
                if (Sets.intersection(wing1.candidateValues(), wing2.candidateValues()).size() != 1) {
                    return Optional.empty();
                }

                int sharedNumber = Sets.intersection(wing1.candidateValues(), wing2.candidateValues()).iterator().next();

                if (wing1.candidateValues().equals(wing2.candidateValues())) {
                    return Optional.empty();
                }
                if (middle.candidateValues().contains(sharedNumber)) {
                    return Optional.empty();
                }

                return Optional.of(new XYWingOccurrence(middle, wing1, wing2));
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<StepResult> findNextStepResult() {
        PencilMarks pencilMarks = PencilMarks.forGrid(startGrid.grid())
                .updateIfPresent(startGrid.optionalPencilMarks());
        var candidates = pencilMarks.cellCandidateNumbers();

        List<PencilMarksCell> relevantPencilMarksCells = candidates.entrySet()
                .stream()
                .filter(kv -> kv.getValue().size() == 2)
                .map(kv -> new PencilMarksCell(kv.getKey(), kv.getValue()))
                .toList();

        List<List<PencilMarksCell>> pencilMarksCellTriplets =
                Permutations.orderedPermutations3(relevantPencilMarksCells, Comparator.comparing(PencilMarksCell::position, Position.comparator));

        List<XYWingOccurrence> xyWings =
                pencilMarksCellTriplets.stream().flatMap(triplet -> Objects.requireNonNull(XYWingOccurrence.optionallyOf(triplet)).stream()).toList();

        return findNextStepResult(xyWings, pencilMarks);
    }

    private Optional<StepResult> findNextStepResult(
            List<XYWingOccurrence> xyWings,
            PencilMarks pencilMarks
    ) {
        return xyWings.stream().flatMap(xyWing -> {
                    var candidates = pencilMarks.cellCandidateNumbers();

                    // The number shared by the 2 wings is "stripped away" from the intersecting (pencil marks) cells
                    ImmutableMap<Position, ImmutableSet<Integer>> adaptedCandidates =
                            candidates.entrySet().stream()
                                    .filter(kv -> {
                                        Position pos = kv.getKey();
                                        return !pos.equals(xyWing.wing1.position()) &&
                                                !pos.equals(xyWing.wing2.position()) &&
                                                !pos.equals(xyWing.middle.position()) &&
                                                (intersect(pos, xyWing.wing1.position()) || intersect(pos, xyWing.wing2.position()));
                                    })
                                    .map(kv -> Map.entry(
                                            kv.getKey(),
                                            kv.getValue().stream()
                                                    .filter(n -> n != xyWing.candidateNumberSharedByWings())
                                                    .collect(ImmutableSet.toImmutableSet()))
                                    )
                                    .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

                    Optional<Map.Entry<Position, ImmutableSet<Integer>>> optCandidateToFillIn =
                            adaptedCandidates.entrySet().stream()
                                    .filter(kv -> kv.getValue().size() == 1)
                                    .findFirst();

                    PencilMarks adaptedPencilMarks = pencilMarks.update(adaptedCandidates);

                    Optional<StepResult> optResult = optCandidateToFillIn.map(candidateToFillIn -> new SetCellValueStep(
                                    candidateToFillIn.getKey(),
                                    OptionalInt.of(candidateToFillIn.getValue().iterator().next()),
                                    "Filling cell after processing XY-Wing"
                            )).map(step -> new StepResult(step, step.applyStep(startGrid)))
                            .or(() -> (!adaptedPencilMarks.limits(pencilMarks)) ? Optional.empty() :
                                    Optional.of(new UpdatePencilMarksStep(
                                            "Updating pencil marks after processing XY-Wing",
                                            adaptedPencilMarks
                                    )).map(step -> new StepResult(step, step.applyStep(startGrid)))
                            );
                    return optResult.stream();
                })
                .findFirst();
    }

    private static boolean intersect(PencilMarksCell cell1, PencilMarksCell cell2) {
        return intersect(cell1.position(), cell2.position());
    }

    private static boolean intersect(Position position1, Position position2) {
        if (position1.equals(position2)) {
            return false;
        }
        return position1.rowIndex() == position2.rowIndex() ||
                position1.columnIndex() == position2.columnIndex() ||
                RegionPosition.fromPosition(position1).equals(RegionPosition.fromPosition(position2));
    }
}
