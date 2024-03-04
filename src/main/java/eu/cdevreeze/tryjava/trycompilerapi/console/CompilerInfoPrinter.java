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

package eu.cdevreeze.tryjava.trycompilerapi.console;

import com.google.common.collect.ImmutableList;

import javax.tools.ToolProvider;

/**
 * Program that prints some info about the Java compiler, in particular the supported major versions.
 * See <a href="https://www.developer.com/design/an-introduction-to-the-java-compiler-api/">Introduction to Java Compiler API</a>.
 *
 * @author Chris de Vreeze
 */
public class CompilerInfoPrinter {

    public static void main(String[] args) {
        var javaCompiler = ToolProvider.getSystemJavaCompiler();

        var sourceVersions = javaCompiler.getSourceVersions().stream().sorted().collect(ImmutableList.toImmutableList());

        System.out.printf("Name: %s%n", javaCompiler.name());

        System.out.println("Supported Java versions:");
        sourceVersions.forEach(System.out::println);
    }
}
