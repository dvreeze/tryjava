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
import com.google.common.collect.ImmutableSet;
import eu.cdevreeze.tryjava.sudoku.model.CandidateMap;
import eu.cdevreeze.tryjava.sudoku.model.Grid;
import eu.cdevreeze.tryjava.sudoku.model.Position;
import eu.cdevreeze.tryjava.sudoku.model.Row;
import eu.cdevreeze.tryjava.sudoku.parse.GridParser;
import eu.cdevreeze.tryjava.sudoku.print.GridPrinter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * This program parses and prints a Sudoku grid. See the grid parser and printer classes.
 * There must be 9 program arguments each containing 9 whitespace-separated numbers between 0 and 9, inclusive.
 *
 * @author Chris de Vreeze
 */
public class ParseAndPrintGrid {

    public static void main(String[] args) {
        Preconditions.checkArgument(
                args.length == Row.ROW_COUNT,
                "Enter 9 program arguments, each having 9 space-separated numbers >= 0 and <= 9");
        String input = String.join("\n", args);

        Grid grid = GridParser.parse(input);

        System.out.println(GridPrinter.print(grid));
        System.out.println();
        System.out.printf("Grid is valid so far: %b%n", grid.isValid());
        System.out.printf("Number of filled cells: %d%n", grid.filledCellCount());
        System.out.printf("Number of unfilled cells: %d%n", grid.remainingUnfilledCells().size());

        CandidateMap candidateMap = CandidateMap.forGrid(grid);

        System.out.println();
        System.out.println("Candidate numbers:");
        System.out.println();

        candidateMap.cellCandidates().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Position.comparator))
                .forEach(kv -> {
                    Position pos = kv.getKey();
                    ImmutableSet<Integer> candidateNumbers = kv.getValue();
                    System.out.printf(
                            "Position %s, candidates %s%n",
                            pos,
                            candidateNumbers.stream().sorted().map(Object::toString).collect(Collectors.joining(", ")));
                });
    }
}
