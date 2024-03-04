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

package eu.cdevreeze.tryjava.trycompilerapi.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sun.source.tree.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;
import java.util.Optional;

/**
 * Immutable thread-safe tree model, corresponding to the model in package com.sun.source.tree.
 * <p>
 * Note that the latter model is not fixed, so changes may be needed to keep them in sync.
 *
 * @author Chris de Vreeze
 */
public final class Trees {

    private Trees() {
    }

    sealed public interface Node {
        Tree.Kind getKind();
    }

    // According to the implementation of the tree API, this is an ExpressionNode
    sealed public interface TypeNode extends ExpressionNode {
    }

    public record AnnotatedTypeNode(
            ExpressionNode underlyingType,
            ImmutableList<? extends AnnotationNode> annotations) implements TypeNode {
        public Tree.Kind getKind() {
            return Tree.Kind.ANNOTATED_TYPE;
        }
    }

    public record AnnotationNode(
            Node annotationType,
            ImmutableList<? extends ExpressionNode> arguments) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.ANNOTATION;
        }
    }

    public record AnyPatternNode() implements PatternNode {
        public Tree.Kind getKind() {
            return Tree.Kind.ANY_PATTERN;
        }
    }

    public record ArrayAccessNode(
            ExpressionNode expression,
            ExpressionNode index) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.ARRAY_ACCESS;
        }
    }

    public record ArrayTypeNode(Node type) implements TypeNode {
        public Tree.Kind getKind() {
            return Tree.Kind.ARRAY_TYPE;
        }
    }

    public record AssertNode(
            ExpressionNode condition,
            Optional<ExpressionNode> detail) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.ASSERT;
        }
    }

    public record AssignmentNode(
            ExpressionNode variable,
            ExpressionNode expression) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.ASSIGNMENT;
        }
    }

    public record BinaryNode(ExpressionNode leftOperand, Tree.Kind kind,
                             ExpressionNode rightOperand) implements ExpressionNode {
        public Tree.Kind getKind() {
            return kind;
        }
    }

    public record BindingPatternNode(VariableNode variable) implements PatternNode {
        public Tree.Kind getKind() {
            return Tree.Kind.BINDING_PATTERN;
        }
    }

    public record BlockNode(ImmutableList<? extends StatementNode> statements,
                            boolean isStatic) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.BLOCK;
        }
    }

    public record BreakNode(Optional<Name> label) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.BREAK;
        }
    }

    sealed public interface CaseLabelNode extends Node {
    }

    public record CaseNode(
            CaseTree.CaseKind caseKind,
            ExpressionNode guard,
            ImmutableList<? extends ExpressionNode> expressions,
            Optional<ImmutableList<? extends StatementNode>> statements,
            Optional<Node> body,
            ImmutableList<? extends CaseLabelNode> labels
    ) implements Node {
        public Tree.Kind getKind() {
            return Tree.Kind.CASE;
        }
    }

    public record CatchNode(VariableNode parameter, BlockNode block) implements Node {
        public Tree.Kind getKind() {
            return Tree.Kind.CATCH;
        }
    }

    public record ClassNode(
            Name simpleName,
            ImmutableList<? extends TypeParameterNode> typeParameters,
            Optional<Node> extendsClause,
            ImmutableList<? extends Node> implementsClause,
            ImmutableList<? extends Node> members,
            ModifiersNode modifiers,
            ImmutableList<? extends Node> permitsClause
    ) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.CLASS;
        }
    }

    public record CompilationUnitNode(
            Optional<ExpressionNode> packageName,
            Optional<ModuleNode> module,
            Optional<PackageNode> packageNode,
            ImmutableList<? extends ImportNode> imports,
            Optional<LineMap> lineMap,
            Optional<ImmutableList<? extends AnnotationNode>> packageAnnotations,
            Optional<JavaFileObject> sourceFile,
            ImmutableList<? extends Node> typeDecls
    ) implements Node {
        public Tree.Kind getKind() {
            return Tree.Kind.COMPILATION_UNIT;
        }
    }

    public record CompoundAssignmentNode(ExpressionNode variable, Tree.Kind kind,
                                         ExpressionNode expression) implements ExpressionNode {
        public Tree.Kind getKind() {
            return kind;
        }
    }

    public record ConditionalExpressionNode(ExpressionNode condition, ExpressionNode trueExpression,
                                            ExpressionNode falseExpression) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.CONDITIONAL_EXPRESSION;
        }
    }

    public record ConstantCaseLabelNode() implements CaseLabelNode {
        public Tree.Kind getKind() {
            return Tree.Kind.CONSTANT_CASE_LABEL;
        }
    }

    public record ContinueNode(Optional<Name> label) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.CONTINUE;
        }
    }

    public record DeconstructionPatternNode(ExpressionNode deconstructor, ImmutableList<PatternNode> nestedPatterns,
                                            Tree.Kind kind) implements PatternNode {
        public Tree.Kind getKind() {
            return kind;
        }
    }

    public record DefaultCaseLabelNode() implements CaseLabelNode {
        public Tree.Kind getKind() {
            return Tree.Kind.DEFAULT_CASE_LABEL;
        }
    }

    sealed public interface DirectiveNode extends Node {
    }

    public record DoWhileLoopNode(ExpressionNode condition, StatementNode statement) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.DO_WHILE_LOOP;
        }
    }

    public record EmptyStatementNode() implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.EMPTY_STATEMENT;
        }
    }

    public record EnhancedForLoopNode(VariableNode variable, ExpressionNode expression,
                                      StatementNode statement) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.ENHANCED_FOR_LOOP;
        }
    }

    public record ErroneousNode(ImmutableList<? extends Node> errorTrees) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.ERRONEOUS;
        }
    }

    public record ExportsNode(ExpressionNode packageName,
                              Optional<ImmutableList<? extends ExpressionNode>> moduleNames) implements DirectiveNode {
        public Tree.Kind getKind() {
            return Tree.Kind.EXPORTS;
        }
    }

    public record ExpressionStatementNode(ExpressionNode expression) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.EXPRESSION_STATEMENT;
        }
    }

    sealed public interface ExpressionNode extends Node {
    }

    public record ForLoopNode(
            ImmutableList<? extends StatementNode> initializer,
            ExpressionNode condition,
            ImmutableList<? extends ExpressionStatementNode> update,
            StatementNode statement
    ) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.FOR_LOOP;
        }
    }

    public record IdentifierNode(Name name) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.IDENTIFIER;
        }
    }

    public record IfNode(ExpressionNode condition, StatementNode thenStatement,
                         Optional<StatementNode> elseStatement) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.IF;
        }
    }

    public record ImportNode(Node qualifiedIdentifier, boolean isStatic) implements Node {
        public Tree.Kind getKind() {
            return Tree.Kind.IMPORT;
        }
    }

    public record InstanceOfNode(ExpressionNode expression, Optional<PatternNode> pattern,
                                 Node type) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.INSTANCE_OF;
        }
    }

    public record IntersectionTypeNode(ImmutableList<? extends Node> bounds) implements TypeNode {
        public Tree.Kind getKind() {
            return Tree.Kind.INTERSECTION_TYPE;
        }
    }

    public record LabeledStatementNode(Name label, StatementNode statement) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.LABELED_STATEMENT;
        }
    }

    public record LambdaExpressionNode(ImmutableList<? extends VariableNode> parameters,
                                       Node body,
                                       LambdaExpressionTree.BodyKind bodyKind) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.LAMBDA_EXPRESSION;
        }
    }

    public record LiteralNode(Object value, Tree.Kind kind) implements ExpressionNode {
        public Tree.Kind getKind() {
            return kind;
        }
    }

    public record MemberReferenceNode(
            ExpressionNode qualifierExpression,
            Name name,
            MemberReferenceTree.ReferenceMode mode,
            Optional<ImmutableList<? extends ExpressionNode>> typeArguments
    ) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.MEMBER_REFERENCE;
        }
    }

    public record MemberSelectNode(
            Optional<ExpressionNode> expression,
            Name identifier
    ) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.MEMBER_SELECT;
        }
    }

    public record MethodInvocationNode(
            Optional<ExpressionNode> methodSelect,
            ImmutableList<? extends Node> typeArguments,
            ImmutableList<? extends ExpressionNode> arguments
    ) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.METHOD_INVOCATION;
        }
    }

    public record MethodNode(
            Name name,
            ModifiersNode modifiers,
            ImmutableList<? extends TypeParameterNode> typeParameters,
            ImmutableList<? extends VariableNode> parameters,
            Optional<Node> returnType,
            Optional<VariableNode> receiverParameter,
            ImmutableList<? extends ExpressionNode> throwsExpressions,
            Optional<BlockNode> body,
            Optional<Node> defaultValue
    ) implements Node {
        public Tree.Kind getKind() {
            return Tree.Kind.METHOD;
        }
    }

    public record ModifiersNode(ImmutableSet<Modifier> flags,
                                ImmutableList<AnnotationNode> annotations) implements Node {
        public Tree.Kind getKind() {
            return Tree.Kind.MODIFIERS;
        }
    }

    public record ModuleNode(
            ExpressionNode name,
            ModuleTree.ModuleKind moduleType,
            ImmutableList<? extends DirectiveNode> directives,
            ImmutableList<? extends AnnotationNode> annotations
    ) implements Node {
        public Tree.Kind getKind() {
            return Tree.Kind.MODULE;
        }
    }

    public record NewArrayNode(
            Optional<Node> type,
            ImmutableList<? extends ExpressionNode> dimensions,
            ImmutableList<? extends ExpressionNode> initializers,
            ImmutableList<? extends AnnotationNode> annotations,
            ImmutableList<? extends ImmutableList<? extends AnnotationNode>> dimAnnotations
    ) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.NEW_ARRAY;
        }
    }

    public record NewClassNode(
            Optional<ExpressionNode> enclosingExpression,
            ImmutableList<? extends Node> typeArguments,
            ExpressionNode identifier,
            ImmutableList<? extends ExpressionNode> arguments,
            Optional<ClassNode> classBody
    ) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.NEW_CLASS;
        }
    }

    public record OpensNode(ExpressionNode packageName,
                            Optional<ImmutableList<ExpressionNode>> moduleNames) implements DirectiveNode {
        public Tree.Kind getKind() {
            return Tree.Kind.OPENS;
        }
    }

    public record PackageNode(ExpressionNode packageName,
                              ImmutableList<? extends AnnotationNode> annotations) implements Node {
        public Tree.Kind getKind() {
            return Tree.Kind.PACKAGE;
        }
    }

    public record ParameterizedTypeNode(Node type, ImmutableList<? extends Node> typeArguments) implements TypeNode {
        public Tree.Kind getKind() {
            return Tree.Kind.PARAMETERIZED_TYPE;
        }
    }

    public record ParenthesizedNode(ExpressionNode expression) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.PARENTHESIZED;
        }
    }

    public record PatternCaseLabelNode(PatternNode pattern) implements CaseLabelNode {
        public Tree.Kind getKind() {
            return Tree.Kind.PATTERN_CASE_LABEL;
        }
    }

    sealed public interface PatternNode extends Node {
    }

    public record PrimitiveTypeNode(TypeKind primitiveTypeKind) implements TypeNode {
        public Tree.Kind getKind() {
            return Tree.Kind.PRIMITIVE_TYPE;
        }
    }

    public record ProvidesNode(ExpressionNode serviceName,
                               ImmutableList<ExpressionNode> implementationNames) implements DirectiveNode {
        public Tree.Kind getKind() {
            return Tree.Kind.PROVIDES;
        }
    }

    public record RequiresNode(ExpressionNode moduleName, boolean isStatic,
                               boolean isTransitive) implements DirectiveNode {
        public Tree.Kind getKind() {
            return Tree.Kind.REQUIRES;
        }
    }

    public record ReturnNode(Optional<ExpressionNode> expression) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.RETURN;
        }
    }

    sealed public interface StatementNode extends Node {
    }

    public record StringTemplateNode(
            ImmutableList<String> fragments,
            ImmutableList<? extends ExpressionNode> expressions,
            Optional<ExpressionNode> processor
    ) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.TEMPLATE; // Subject to change!
        }
    }

    public record SwitchExpressionNode(ExpressionNode expression,
                                       ImmutableList<? extends CaseNode> cases) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.SWITCH_EXPRESSION;
        }
    }

    public record SwitchNode(ExpressionNode expression,
                             ImmutableList<? extends CaseNode> cases) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.SWITCH;
        }
    }

    public record SynchronizedNode(ExpressionNode expression, BlockNode block) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.SYNCHRONIZED;
        }
    }

    public record ThrowNode(ExpressionNode expression) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.THROW;
        }
    }

    public record TryNode(
            ImmutableList<? extends Node> resources,
            BlockNode block,
            ImmutableList<? extends CatchNode> catches,
            Optional<BlockNode> finallyBlock
    ) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.TRY;
        }
    }

    public record TypeCastNode(Node type, ExpressionNode expression) implements ExpressionNode {
        public Tree.Kind getKind() {
            return Tree.Kind.TYPE_CAST;
        }
    }

    public record TypeParameterNode(
            Name name,
            ImmutableList<? extends Node> bounds,
            ImmutableList<? extends AnnotationNode> annotations
    ) implements Node {
        public Tree.Kind getKind() {
            return Tree.Kind.TYPE_PARAMETER;
        }
    }

    public record UnaryNode(ExpressionNode expression, Tree.Kind kind) implements ExpressionNode {
        public Tree.Kind getKind() {
            return kind;
        }
    }

    public record UnionTypeNode(ImmutableList<? extends Node> typeAlternatives) implements TypeNode {
        public Tree.Kind getKind() {
            return Tree.Kind.UNION_TYPE;
        }
    }

    public record UsesNode(ExpressionNode serviceName) implements DirectiveNode {
        public Tree.Kind getKind() {
            return Tree.Kind.USES;
        }
    }

    public record VariableNode(
            Name name,
            Optional<Node> type,
            ModifiersNode modifiers,
            Optional<ExpressionNode> nameExpression,
            Optional<ExpressionNode> initializer
    ) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.VARIABLE;
        }

    }

    public record WhileLoopNode(ExpressionNode condition, StatementNode statement) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.WHILE_LOOP;
        }
    }

    public record WildcardNode(Optional<Node> bound, Tree.Kind kind) implements Node {
        public Tree.Kind getKind() {
            return kind;
        }
    }

    public record YieldNode(ExpressionNode value) implements StatementNode {
        public Tree.Kind getKind() {
            return Tree.Kind.YIELD;
        }
    }
}
