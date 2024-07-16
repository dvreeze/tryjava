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

package eu.cdevreeze.tryjava.sudoku.console;

import com.google.common.base.Preconditions;
import eu.cdevreeze.tryjava.sudoku.game.Game;
import eu.cdevreeze.tryjava.sudoku.model.Grid;
import eu.cdevreeze.tryjava.sudoku.model.Row;
import eu.cdevreeze.tryjava.sudoku.parse.GridParser;
import eu.cdevreeze.tryjava.sudoku.print.GridPrinter;

import java.util.stream.IntStream;

/**
 * This program parses and tries to solve a Sudoku grid. See the grid parser and printer classes.
 * There must be 9 program arguments each containing 9 whitespace-separated numbers between 0 and 9, inclusive.
 *
 * @author Chris de Vreeze
 */
public class SolveGame {

    public static void main(String[] args) {
        Preconditions.checkArgument(
                args.length == Row.ROW_COUNT,
                "Enter 9 program arguments, each having 9 space-separated numbers >= 0 and <= 9");
        String input = String.join("\n", args);

        Grid startGrid = GridParser.parse(input);

        System.out.println();
        System.out.println("Start grid:");
        System.out.println();
        System.out.println(GridPrinter.print(startGrid));
        System.out.println();
        System.out.printf("Grid is valid so far: %b%n", startGrid.isValid());
        System.out.printf("Number of filled cells: %d%n", startGrid.filledCellCount());
        System.out.printf("Number of unfilled cells: %d%n", startGrid.remainingUnfilledCells().size());

        Game fullGame = Game.runStepFinderRepeatedly(startGrid);

        System.out.println();
        System.out.println("Last grid (after automatically trying to solve the sudoku):");
        System.out.println();
        System.out.println(GridPrinter.print(fullGame.lastGrid()));

        System.out.println();
        System.out.printf("Resulting grid is valid: %b%n", fullGame.lastGrid().isValid());
        System.out.printf("Resulting grid is solved: %b%n", fullGame.isSolved());

        System.out.println();
        System.out.println("Individual steps:");

        IntStream.range(0, fullGame.stepResults().size()).forEach(i -> {
            System.out.println();
            System.out.printf("Step %d (zero-based)%n", i);
            System.out.println();
            System.out.printf("Step: %s%n", fullGame.stepResults().get(i).step());
            System.out.println();
            System.out.printf("Resulting grid:%n");
            System.out.println();
            System.out.println(GridPrinter.print(fullGame.stepResults().get(i).resultGrid()));
        });

        System.out.println();
        System.out.printf("Resulting grid is valid: %b%n", fullGame.lastGrid().isValid());
        System.out.printf("Resulting grid is solved: %b%n", fullGame.isSolved());
    }
}
