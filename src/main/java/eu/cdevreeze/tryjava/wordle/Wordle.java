/*
 * Copyright 2023-2024 Chris de Vreeze
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

package eu.cdevreeze.tryjava.wordle;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.primitives.ImmutableIntArray;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Wordle game (from a programming course). The matching of words is case-sensitive, which is not very lenient.
 * The target word is randomly chosen from a word list at <a href="https://www.mit.edu/~ecprice/wordlist.10000">MIT word list</a>.
 * All potential target words have 5 letters, so there is no point in guessing any smaller or longer words.
 * All potential target words are lower-case, so it's important to guess only lower-case words.
 * <p>
 * This implementation is driven by a data structure, separates IO from computation, and uses (almost) pure functions.
 * The "data records" (as Java records) have convenience methods derived from their state.
 *
 * @author Chris de Vreeze
 */
public class Wordle {

    /**
     * Data structure for specific letter occurrences (with their positions) in a word.
     * Useful for guessed word letters in correct or wrong positions.
     */
    public record LettersInWord(String word, ImmutableIntArray letterIndices) {

        public LettersInWord {
            Preconditions.checkArgument(letterIndices.stream().allMatch(i -> i >= 0));
            Preconditions.checkArgument(letterIndices.stream().allMatch(i -> i < word.length()));
            Preconditions.checkArgument(letterIndices.stream().distinct().count() == letterIndices.length());
        }

        public ImmutableList<Character> letters() {
            return letterIndices.stream().mapToObj(word::charAt).collect(ImmutableList.toImmutableList());
        }
    }

    public record GuessingResult(
            String guessedWord,
            ImmutableIntArray letterIndicesInCorrectPosition,
            ImmutableIntArray letterIndicesInWrongPosition) {

        public GuessingResult {
            var letterIndicesInCorrectPositionSet =
                    ImmutableSet.<Integer>builder().addAll(letterIndicesInCorrectPosition.asList()).build();
            var letterIndicesInWrongPositionSet =
                    ImmutableSet.<Integer>builder().addAll(letterIndicesInWrongPosition.asList()).build();
            Preconditions.checkArgument(Sets.intersection(letterIndicesInCorrectPositionSet, letterIndicesInWrongPositionSet).isEmpty());

            var letterIndices = ImmutableIntArray.builder()
                    .addAll(letterIndicesInCorrectPosition)
                    .addAll(letterIndicesInWrongPosition)
                    .build();
            Preconditions.checkArgument(letterIndices.stream().allMatch(i -> i >= 0));
            Preconditions.checkArgument(letterIndices.stream().allMatch(i -> i < guessedWord.length()));
            Preconditions.checkArgument(letterIndices.stream().distinct().count() == letterIndices.length());
        }

        public LettersInWord lettersInCorrectPosition() {
            return new LettersInWord(guessedWord, letterIndicesInCorrectPosition);
        }

        public LettersInWord lettersInWrongPosition() {
            return new LettersInWord(guessedWord, letterIndicesInWrongPosition);
        }

        public boolean targetWordGuessed(String targetWord) {
            return letterIndicesInCorrectPosition.length() == targetWord.length();
        }

        public int numberOfFoundLetters() {
            return letterIndicesInCorrectPosition.length() + letterIndicesInWrongPosition.length();
        }

        public String correctlyPositionedCharString() {
            var boxedChars = IntStream.range(0, guessedWord.length())
                    .mapToObj(i -> (letterIndicesInCorrectPosition.contains(i)) ? guessedWord.charAt(i) : '_')
                    .toList();
            return Joiner.on("").join(boxedChars);
        }
    }

    private final String targetWord;

    public Wordle(String targetWord) {
        this.targetWord = targetWord;
    }

    public GuessingResult makeGuess(String guessedWord) {
        Preconditions.checkArgument(guessedWord.length() == targetWord.length());
        return new GuessingResult(
                guessedWord,
                lettersInCorrectPosition(guessedWord).letterIndices(),
                lettersInWrongPosition(guessedWord).letterIndices()
        );
    }

    private LettersInWord lettersInCorrectPosition(String guessedWord) {
        assert guessedWord.length() == targetWord.length();

        var indices = ImmutableIntArray.copyOf(
                IntStream.range(0, targetWord.length())
                        .filter(i -> guessedWord.charAt(i) == targetWord.charAt(i)));
        return new LettersInWord(guessedWord, indices);
    }

    private LettersInWord lettersInWrongPosition(String guessedWord) {
        assert guessedWord.length() == targetWord.length();

        var lettersInCorrectPosition = lettersInCorrectPosition(guessedWord);

        var guessedWordIndices = new ArrayList<Integer>();
        var usedTargetWordIndices =
                new ArrayList<>(lettersInCorrectPosition.letterIndices.asList());

        // Local side effects (recursion would have been a purely functional alternative)
        int bound = guessedWord.length();
        for (int i = 0; i < bound; i++) {
            for (int j = 0; j < bound; j++) {
                if (j != i && guessedWord.charAt(i) == targetWord.charAt(j) &&
                        !lettersInCorrectPosition.letterIndices.contains(i) &&
                        !guessedWordIndices.contains(i) &&
                        !usedTargetWordIndices.contains(j)) {
                    guessedWordIndices.add(i);
                    usedTargetWordIndices.add(j);
                }
            }
        }

        return new LettersInWord(guessedWord, ImmutableIntArray.copyOf(guessedWordIndices));
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        int WORD_SIZE = 5;
        var wordListUri = Objects.requireNonNull(Wordle.class.getResource("/wordlist.txt")).toURI();
        var potentialTargetWords = Files.readAllLines(new File(wordListUri).toPath())
                .stream()
                .filter(s -> s.length() == WORD_SIZE)
                .collect(ImmutableList.toImmutableList());

        var random = new Random();
        var targetWordIndex = random.nextInt(potentialTargetWords.size());
        var targetWord = potentialTargetWords.get(targetWordIndex);

        var wordle = new Wordle(targetWord);

        // IO and repetition of guesses only here

        int MAX_ATTEMPTS = 6;
        var input = new Scanner(System.in);

        var wordFound = false;
        var i = 0;
        while (!wordFound && i < MAX_ATTEMPTS) {
            wordFound = makeGuess(wordle, input, i == 0);
            i += 1;
        }

        if (!wordFound) {
            System.out.printf("You have not guessed the word '%s', and have no more attempts left.%n", targetWord);
        }
    }

    private static boolean makeGuess(Wordle wordle, Scanner input, boolean isFirstGuess) {
        System.out.println();
        System.out.printf("Please make %s guess:%n", (isFirstGuess) ? "your first" : "another");
        var guess = input.nextLine();

        if (guess.length() != wordle.targetWord.length()) {
            System.out.printf("Wrong number of letters. The word must have %d letters.%n", wordle.targetWord.length());
            return false;
        }

        var guessingResult = wordle.makeGuess(guess);

        var targetWordFound = guessingResult.targetWordGuessed(wordle.targetWord);
        var numberOfFoundLetters = guessingResult.numberOfFoundLetters();

        if (targetWordFound) {
            System.out.println("Congrats! You have found today's Wordle!");
        } else {
            System.out.printf("You have found %d letters.%n", numberOfFoundLetters);
            guessingResult.lettersInWrongPosition().letters()
                    .forEach(c -> System.out.printf("Letter %s exists but is in the wrong place.%n", c.toString()));

            if (!guessingResult.lettersInCorrectPosition().letterIndices.isEmpty()) {
                System.out.println(guessingResult.correctlyPositionedCharString());
            }
        }

        return targetWordFound;
    }
}
