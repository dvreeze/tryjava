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

/**
 * This program tries to solve a specific Sudoku game.
 *
 * @author Chris de Vreeze
 */
public class SolveGame1 {

    public static void main(String[] args) {
        String[] args2 = new String[]{
                "5 3 0 0 7 0 0 0 0",
                "6 0 0 1 9 5 0 0 0",
                "0 9 8 0 0 0 0 6 0",
                "8 0 0 0 6 0 0 0 3",
                "4 0 0 8 0 3 0 0 1",
                "7 0 0 0 2 0 0 0 6",
                "0 6 0 0 0 0 2 8 0",
                "0 0 0 4 1 9 0 0 5",
                "0 0 0 0 8 0 0 7 9"
        };

        SolveGame.main(args2);
    }
}
