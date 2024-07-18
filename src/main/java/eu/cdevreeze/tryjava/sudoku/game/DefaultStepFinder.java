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

import eu.cdevreeze.tryjava.sudoku.model.GridApi;
import eu.cdevreeze.tryjava.sudoku.model.Region;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Default "step finder" in a Sudoku game, trying out several specific step finders.
 *
 * @author Chris de Vreeze
 */
public record DefaultStepFinder(GridApi startGrid) implements StepFinder {

    @Override
    public Optional<StepResult> findNextStepResult() {
        return Stream.<Stream<StepFinder>>of(
                        IntStream.range(0, 9).mapToObj(i -> new OpenSingleInRow(startGrid, i)),
                        IntStream.range(0, 9).mapToObj(i -> new OpenSingleInColumn(startGrid, i)),
                        Region.ALL_REGION_POSITIONS.stream().map(rp -> new OpenSingleInRegion(startGrid, rp)),
                        IntStream.range(0, 9).boxed()
                                .flatMap(i -> IntStream.rangeClosed(1, 9)
                                        .mapToObj(n -> new VisualEliminationInRow(startGrid, i, n))),
                        IntStream.range(0, 9).boxed()
                                .flatMap(i -> IntStream.rangeClosed(1, 9)
                                        .mapToObj(n -> new VisualEliminationInColumn(startGrid, i, n))),
                        Region.ALL_REGION_POSITIONS.stream()
                                .flatMap(rp -> IntStream.rangeClosed(1, 9)
                                        .mapToObj(n -> new VisualEliminationInRegion(startGrid, rp, n))),
                        Stream.of(new LoneSingle(startGrid)),
                        IntStream.range(0, 9).mapToObj(i -> new HiddenSingleInRow(startGrid, i)),
                        IntStream.range(0, 9).mapToObj(i -> new HiddenSingleInColumn(startGrid, i)),
                        Region.ALL_REGION_POSITIONS.stream().map(rp -> new HiddenSingleInRegion(startGrid, rp)),
                        IntStream.range(0, 9).mapToObj(i -> new NakedPairInRow(startGrid, i)),
                        IntStream.range(0, 9).mapToObj(i -> new NakedPairInColumn(startGrid, i)),
                        Region.ALL_REGION_POSITIONS.stream().map(rp -> new NakedPairInRegion(startGrid, rp)),
                        IntStream.range(0, 9).mapToObj(i -> new NakedTripletInRow(startGrid, i)),
                        IntStream.range(0, 9).mapToObj(i -> new NakedTripletInColumn(startGrid, i)),
                        Region.ALL_REGION_POSITIONS.stream().map(rp -> new NakedTripletInRegion(startGrid, rp)),
                        IntStream.range(0, 9).mapToObj(i -> new NakedQuadInRow(startGrid, i)),
                        IntStream.range(0, 9).mapToObj(i -> new NakedQuadInColumn(startGrid, i)),
                        Region.ALL_REGION_POSITIONS.stream().map(rp -> new NakedQuadInRegion(startGrid, rp)),
                        Stream.of(new XWingInRows(startGrid)),
                        Stream.of(new XWingInColumns(startGrid)),
                        Region.ALL_REGION_POSITIONS.stream().map(rp -> new IntersectionWithRow(startGrid.grid(), rp)),
                        Region.ALL_REGION_POSITIONS.stream().map(rp -> new IntersectionWithColumn(startGrid.grid(), rp)),
                        Region.ALL_REGION_POSITIONS.stream().map(rp -> new InverseIntersectionWithRow(startGrid.grid(), rp)),
                        Region.ALL_REGION_POSITIONS.stream().map(rp -> new InverseIntersectionWithColumn(startGrid.grid(), rp)),
                        IntStream.range(0, 9).mapToObj(i -> new HiddenPairInRow(startGrid, i)),
                        IntStream.range(0, 9).mapToObj(i -> new HiddenPairInColumn(startGrid, i)),
                        Region.ALL_REGION_POSITIONS.stream().map(rp -> new HiddenPairInRegion(startGrid, rp)),
                        IntStream.range(0, 9).mapToObj(i -> new HiddenTripletInRow(startGrid.grid(), i))
                )
                .flatMap(Function.identity())
                .flatMap(stepFinder -> stepFinder.findNextStepResult().stream())
                .findFirst();
    }
}
