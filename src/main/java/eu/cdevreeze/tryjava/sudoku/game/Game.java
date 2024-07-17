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
import eu.cdevreeze.tryjava.sudoku.model.Grid;
import eu.cdevreeze.tryjava.sudoku.model.GridApi;

import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Sudoku game.
 *
 * @author Chris de Vreeze
 */
public record Game(Grid startGrid, ImmutableList<StepResult> stepResults) {

    public Game {
        Preconditions.checkArgument(startGrid.isValid());

        GridApi grid = startGrid;
        for (var stepResult : stepResults) {
            Preconditions.checkArgument(stepResult.step().isValidStep(grid.grid()));
            final var nextGrid = stepResult.step().applyStep(grid);
            Preconditions.checkArgument(nextGrid.equals(stepResult.resultGrid()));
            grid = nextGrid;
        }
    }

    public boolean isSolved() {
        return lastGrid().grid().isSolved();
    }

    public GridApi lastGrid() {
        return (stepResults.isEmpty()) ? startGrid : stepResults.getLast().resultGrid();
    }

    public Game plus(StepResult stepResult) {
        return new Game(startGrid, ImmutableList.<StepResult>builder().addAll(stepResults).add(stepResult).build());
    }

    public static Game startGame(Grid grid) {
        return new Game(grid, ImmutableList.of());
    }

    public static Game runStepFinderRepeatedly(Grid startGrid) {
        return Stream.iterate(
                        new GameStatus(Game.startGame(startGrid), true),
                        GameStatus::progressing,
                        gameStatus -> {
                            if (!gameStatus.progressing()) {
                                return gameStatus;
                            }
                            Game game = gameStatus.game;
                            var stepFinder = new DefaultStepFinder(game.lastGrid());
                            var nextStepResultOption = stepFinder.findNextStepResult();
                            return nextStepResultOption
                                    .map(game::plus)
                                    .map(g -> new GameStatus(g, true)).orElse(new GameStatus(game, false));
                        }
                )
                .limit(MAX_STEPS)
                .map(GameStatus::game)
                .max(Comparator.comparingInt(game -> game.stepResults.size()))
                .orElseThrow();
    }

    public record GameStatus(Game game, boolean progressing) {
    }

    public static final int MAX_STEPS = 100;
}
