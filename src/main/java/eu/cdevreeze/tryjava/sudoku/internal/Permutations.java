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

package eu.cdevreeze.tryjava.sudoku.internal;

import java.util.Comparator;
import java.util.List;

/**
 * Utility for computing k-permutations (for fixed small value of "k").
 *
 * @author Chris de Vreeze
 */
public class Permutations {

    private Permutations() {
    }

    public static <T> List<List<T>> orderedPermutations2(List<T> elements, Comparator<T> comparator) {
        return elements.stream().flatMap(e1 ->
                        elements.stream().filter(e -> comparator.compare(e1, e) < 0)
                                .map(e2 -> List.of(e1, e2)))
                .toList();
    }

    public static <T> List<List<T>> orderedPermutations3(List<T> elements, Comparator<T> comparator) {
        return elements.stream().flatMap(e1 ->
                        elements.stream().filter(e -> comparator.compare(e1, e) < 0).flatMap(e2 ->
                                elements.stream().filter(e -> comparator.compare(e2, e) < 0)
                                        .map(e3 -> List.of(e1, e2, e3))))
                .toList();
    }

    public static <T> List<List<T>> orderedPermutations4(List<T> elements, Comparator<T> comparator) {
        return elements.stream().flatMap(e1 ->
                        elements.stream().filter(e -> comparator.compare(e1, e) < 0).flatMap(e2 ->
                                elements.stream().filter(e -> comparator.compare(e2, e) < 0).flatMap(e3 ->
                                        elements.stream().filter(e -> comparator.compare(e3, e) < 0)
                                                .map(e4 -> List.of(e1, e2, e3, e4)))))
                .toList();
    }
}
