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

package eu.cdevreeze.tryjava.tryscripting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Program that runs "git pull" repeatedly, iterating over direct subdirectories of a given directory.
 * <p>
 * The program only visits those direct subdirectories that themselves have a ".git" subdirectory.
 * <p>
 * The only program argument is a parent directory. The command itself can be overridden with system
 * property "gitPullCommand".
 * <p>
 * Subdirectories can be excluded by system property "excludedRepos", holding a comma-separated list of
 * (simple) directory names.
 * <p>
 * In order for this program to work, it needs a git client to be installed. The program is implemented
 * using the Java Process API, for invoking git commands (in particular "git pull"). It is also
 * possible that some "git login" is needed before being able to run this program successfully.
 * <p>
 * Note that using JEP 330 (Launch Single-File Source-Code Programs), this could be turned into
 * a self-executable shell script. Note that this requires the absence of any needed libraries other than
 * the JDK standard library.
 *
 * @author Chris de Vreeze
 */
public class GitPullRunner {

    private static final List<String> gitPullCommand =
            List.of(System.getProperty("gitPullCommand", "git pull").split(Pattern.quote(" ")));

    private static final Set<String> excludedRepoNames =
            Set.of(System.getProperty("excludedRepos", "").split(Pattern.quote(",")));

    public static void main(String[] args) {
        Objects.checkFromToIndex(0, 1, args.length);
        var parentDir = Path.of(args[0]);
        Objects.requireNonNull(parentDir);

        if (!parentDir.toFile().isDirectory())
            throw new IllegalArgumentException(String.format("Not a (parent) directory: %s", parentDir));

        var matchingSubdirectories = findMatchingSubdirectories(parentDir)
                .stream().filter(d -> !excludedRepoNames.contains(d.toFile().getName())).toList();

        for (Path projectDir : matchingSubdirectories) {
            System.out.println();
            System.out.printf("Running 'git pull' command for project directory %s%n", projectDir);
            runGitPull(projectDir);
        }
    }

    private static List<Path> findMatchingSubdirectories(Path parentDir) {
        var parentDirAsFile = Objects.requireNonNull(Objects.requireNonNull(parentDir).toFile());
        return Stream.of(
                        Objects.requireNonNull(parentDirAsFile.listFiles(GitPullRunner::isMatchingSubdirectory)))
                .map(File::toPath).toList();
    }

    private static boolean isMatchingSubdirectory(File dir) {
        return dir.isDirectory() &&
                Objects.requireNonNull(dir.listFiles(f -> f.getName().equals(".git"))).length > 0;
    }

    private static void runGitPull(Path projectDir) {
        try {
            var cmd = gitPullCommand;
            System.out.printf("Starting command: %s%n", String.join(" ", cmd));

            var runningCmd = startCommand(cmd, projectDir.toFile());

            System.out.printf("PID: %s%n", runningCmd.pid());
            var exitCode = runningCmd.waitFor();

            if (exitCode != 0) System.out.println("The 'git pull' command was unsuccessful");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Process startCommand(List<String> command, File cwd) {
        var processBuilder = new ProcessBuilder().command(command);
        processBuilder.directory(cwd);

        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
