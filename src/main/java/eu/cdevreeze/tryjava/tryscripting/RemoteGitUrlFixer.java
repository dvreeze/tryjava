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
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Program that fixes remote git URLs, of remote repository "origin" (unless overridden). It iterators over direct subdirectories
 * of a given directory, and adapts the ("origin") git URLs (returned by the "git remote -v" command, if these URLs exist),
 * if they contain a given substring, replacing the substring in the URL by a given replacement string. No regexes are used,
 * so care must be taken to not use dangerously short substrings to match on.
 * <p>
 * The program only visits those direct subdirectories that themselves have a ".git" subdirectory.
 * <p>
 * The program arguments are a parent directory, a substring to find in git URLs, and a replacement string.
 * Repository name "origin" can be overridden by system property "repoName".
 * <p>
 * Subdirectories can be excluded by system property "excludedRepos", holding a comma-separated list of
 * (simple) directory names.
 * <p>
 * In order for this program to work, it needs a git client to be installed. The program is implemented
 * using the Java Process API, for invoking git commands (like "git remote set-url ..."). It is also
 * possible that some "git login" is needed before being able to run this program successfully.
 * <p>
 * Note that using JEP 330 (Launch Single-File Source-Code Programs), this could be turned into
 * a self-executable shell script. Note that this requires the absence of any needed libraries other than
 * the JDK standard library.
 *
 * @author Chris de Vreeze
 */
public final class RemoteGitUrlFixer {

    private static final String repoName = System.getProperty("repoName", "origin");

    private static final Set<String> excludedRepoNames =
            Set.of(System.getProperty("excludedRepos", "").split(Pattern.quote(",")));

    public static void main(String[] args) {
        Objects.checkFromToIndex(0, 2, args.length);
        var parentDir = Path.of(args[0]);
        Objects.requireNonNull(parentDir);

        if (!parentDir.toFile().isDirectory())
            throw new IllegalArgumentException(String.format("Not a (parent) directory: %s", parentDir));

        var stringToReplace = args[1];
        Objects.requireNonNull(stringToReplace);
        var replacementString = args[2];
        Objects.requireNonNull(replacementString);

        var matchingSubdirectories = findMatchingSubdirectories(parentDir)
                .stream().filter(d -> !excludedRepoNames.contains(d.toFile().getName())).toList();

        for (Path projectDir : matchingSubdirectories) {
            System.out.println();
            System.out.printf("Fixing remote git repo URLs (repo name %s) in directory '%s'%n", repoName, projectDir);
            fixUrls(projectDir, stringToReplace, replacementString);
        }
    }

    private static List<Path> findMatchingSubdirectories(Path parentDir) {
        var parentDirAsFile = Objects.requireNonNull(Objects.requireNonNull(parentDir).toFile());
        return Stream.of(
                        Objects.requireNonNull(parentDirAsFile.listFiles(RemoteGitUrlFixer::isMatchingSubdirectory)))
                .map(File::toPath).toList();
    }

    private static boolean isMatchingSubdirectory(File dir) {
        return dir.isDirectory() &&
                Objects.requireNonNull(dir.listFiles(f -> f.getName().equals(".git"))).length > 0;
    }

    private static void fixUrls(Path projectDir, String stringToReplace, String replacementString) {
        fixUrl(projectDir, stringToReplace, replacementString, false);
        fixUrl(projectDir, stringToReplace, replacementString, true);
    }

    private static void fixUrl(Path projectDir, String stringToReplace, String replacementString, boolean push) {
        System.out.println();
        var maybeUrl = getRemoteRepoUrl(projectDir, push);

        maybeUrl.ifPresent(url -> {
            var newUrl = url.replace(stringToReplace, replacementString);

            System.out.printf("New URL: %s%n", newUrl);
            setRemoteRepoUrl(projectDir, newUrl, push);
        });
    }

    private static Optional<String> getRemoteRepoUrl(Path projectDir, boolean push) {
        try {
            var cmd = buildGitGetUrlCommandLine(push);
            System.out.printf("Starting command: %s%n", String.join(" ", cmd));

            var runningCmd = startCommand(cmd, projectDir.toFile());

            System.out.printf("PID: %s%n", runningCmd.pid());
            var exitCode = runningCmd.waitFor();

            if (exitCode != 0) return Optional.empty();

            var scanner = new Scanner(runningCmd.getInputStream());
            var output = scanner.nextLine().trim();

            System.out.printf("Output: %s%n", output);

            return Optional.of(output);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setRemoteRepoUrl(Path projectDir, String newUrl, boolean push) {
        try {
            var cmd = buildGitSetUrlCommandLine(push, newUrl);
            System.out.printf("Starting command: %s%n", String.join(" ", cmd));

            var runningCmd = startCommand(cmd, projectDir.toFile());

            System.out.printf("PID: %s%n", runningCmd.pid());
            runningCmd.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> buildGitGetUrlCommandLine(boolean push) {
        return (push) ?
                List.of("git", "remote", "get-url", "--push", repoName) :
                List.of("git", "remote", "get-url", repoName);
    }

    private static List<String> buildGitSetUrlCommandLine(boolean push, String newUrl) {
        return (push) ?
                List.of("git", "remote", "set-url", "--push", repoName, newUrl) :
                List.of("git", "remote", "set-url", repoName, newUrl);
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
