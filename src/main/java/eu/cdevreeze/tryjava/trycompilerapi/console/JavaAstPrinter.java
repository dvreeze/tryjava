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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import eu.cdevreeze.tryjava.trycompilerapi.model.TreeJsonUtil;
import eu.cdevreeze.tryjava.trycompilerapi.model.TreeModelFactory;
import eu.cdevreeze.tryjava.trycompilerapi.model.Trees;

import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Program that prints the Java AST of a given Java source file.
 * Currently, the classpath cannot be set, so the source file must exist in this project.
 *
 * @author Chris de Vreeze
 */
public class JavaAstPrinter {

    public static void main(String[] args) {
        Preconditions.checkPositionIndex(0, args.length);
        var sourcePath = Path.of(args[0]);

        var compilationUnits = parse(sourcePath);

        System.out.println();
        System.out.printf(
                "Syntax of compilation unit(s) '%s':%n",
                compilationUnits.stream().map(u -> u.getSourceFile().getName()).collect(Collectors.joining(", "))
        );
        System.out.println();

        compilationUnits.stream().map(Object::toString).forEach(System.out::println);

        System.out.println();
        System.out.printf(
                "AST(s) of compilation unit(s) '%s':%n",
                compilationUnits.stream().map(u -> u.getSourceFile().getName()).collect(Collectors.joining(", "))
        );
        System.out.println();

        compilationUnits.stream().map(JavaAstPrinter::formatCompilationUnit).forEach(System.out::println);
    }

    public static ImmutableList<? extends CompilationUnitTree> parse(Path javaSourcePath) {
        var javaCompiler = ToolProvider.getSystemJavaCompiler();

        try (var javaFileManager = javaCompiler.getStandardFileManager(null, null, null)) {
            var sources = javaFileManager.getJavaFileObjectsFromPaths(Collections.singletonList(javaSourcePath));

            var compilationTask =
                    (JavacTask) javaCompiler.getTask(null, null, null, null, null, sources);

            return ImmutableList.copyOf(compilationTask.parse());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatCompilationUnit(CompilationUnitTree compilationUnit) {
        Trees.CompilationUnitNode modelCompilationUnit =
                TreeModelFactory.compilationUnitNodes().apply(compilationUnit);

        ObjectMapper mapper = JsonMapper
                .builder()
                .addModule(TreeJsonUtil.createModule())
                .addModule(new Jdk8Module())
                .addModule(new GuavaModule())
                .build()
                .enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(modelCompilationUnit);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
