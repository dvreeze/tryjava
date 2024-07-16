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

import eu.cdevreeze.tryjava.sudoku.model.Grid;
import eu.cdevreeze.tryjava.sudoku.model.Position;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * "Step finder" for a lone single.
 * See <a href="https://www.learn-sudoku.com/lone-singles.html">lone-single</a>.
 *
 * @author Chris de Vreeze
 */
public record LoneSingle(Grid startGrid) implements StepFinder {

    @Override
    public Optional<StepResult> findNextStepResult() {
        CandidateMap candidateMap = CandidateMap.forGrid(startGrid);

        Optional<Position> loneSinglePositionOption =
                candidateMap.cellCandidates().entrySet()
                        .stream()
                        .filter(kv -> kv.getValue().size() == 1)
                        .findFirst()
                        .map(Map.Entry::getKey);

        if (loneSinglePositionOption.isPresent()) {
            var loneSinglePosition = loneSinglePositionOption.get();

            return Optional.of(new Step(
                    loneSinglePosition,
                    Objects.requireNonNull(candidateMap.cellCandidates().get(loneSinglePosition)).iterator().next(),
                    "Lone single"
            )).map(step -> new StepResult(step, step.applyStep(startGrid)));
        } else {
            return Optional.empty();
        }
    }
}
