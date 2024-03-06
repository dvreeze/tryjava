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

import java.util.Optional;
import java.util.function.Function;

/**
 * Tree model factory, creating the immutable (thread-safe) model from trees in the jdk.compiler API.
 *
 * @author Chris de Vreeze
 */
public final class TreeModelFactory {

    private TreeModelFactory() {
    }

    public interface NodeCreator<T extends Tree, N extends Trees.Node> extends Function<T, N> {
    }

    public static NodeCreator<Tree, Trees.Node> nodes() {
        return tree -> switch (tree) {
            case ExpressionTree t -> expressionNodes().apply(t);
            case StatementTree t -> statementNodes().apply(t);
            case CaseLabelTree t -> caseLabelNodes().apply(t);
            case DirectiveTree t -> directiveNodes().apply(t);
            case PatternTree t -> patternNodes().apply(t);
            case CaseTree t -> caseNodes().apply(t);
            case CatchTree t -> catchNodes().apply(t);
            case CompilationUnitTree t -> compilationUnitNodes().apply(t);
            case ImportTree t -> importNodes().apply(t);
            case MethodTree t -> methodNodes().apply(t);
            case ModifiersTree t -> modifiersNodes().apply(t);
            case ModuleTree t -> moduleNodes().apply(t);
            case PackageTree t -> packageNodes().apply(t);
            case TypeParameterTree t -> typeParameterNodes().apply(t);
            case WildcardTree t -> wildcardNodes().apply(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public static NodeCreator<AnnotatedTypeTree, Trees.AnnotatedTypeNode> annotatedTypeNodes() {
        return tree -> new Trees.AnnotatedTypeNode(
                expressionNodes().apply(tree.getUnderlyingType()),
                tree.getAnnotations().stream().map(t -> annotationNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<AnnotationTree, Trees.AnnotationNode> annotationNodes() {
        return tree -> new Trees.AnnotationNode(
                nodes().apply(tree.getAnnotationType()),
                tree.getArguments().stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<AnyPatternTree, Trees.AnyPatternNode> anyPatternNodes() {
        return _ -> new Trees.AnyPatternNode();
    }

    public static NodeCreator<ArrayAccessTree, Trees.ArrayAccessNode> arrayAccessNodes() {
        return tree -> new Trees.ArrayAccessNode(
                expressionNodes().apply(tree.getExpression()),
                expressionNodes().apply(tree.getIndex())
        );
    }

    public static NodeCreator<ArrayTypeTree, Trees.ArrayTypeNode> arrayTypeNodes() {
        return tree -> new Trees.ArrayTypeNode(nodes().apply(tree.getType()));
    }

    public static NodeCreator<AssertTree, Trees.AssertNode> assertNodes() {
        return tree -> new Trees.AssertNode(
                expressionNodes().apply(tree.getCondition()),
                Optional.ofNullable(tree.getDetail()).map(t -> expressionNodes().apply(t))
        );
    }

    public static NodeCreator<AssignmentTree, Trees.AssignmentNode> assignmentNodes() {
        return tree -> new Trees.AssignmentNode(
                expressionNodes().apply(tree.getVariable()),
                expressionNodes().apply(tree.getExpression())
        );
    }

    public static NodeCreator<BinaryTree, Trees.BinaryNode> binaryNodes() {
        return tree -> new Trees.BinaryNode(
                expressionNodes().apply(tree.getLeftOperand()),
                tree.getKind(),
                expressionNodes().apply(tree.getRightOperand())
        );
    }

    public static NodeCreator<BindingPatternTree, Trees.BindingPatternNode> bindingPatternNodes() {
        return tree -> new Trees.BindingPatternNode(variableNodes().apply(tree.getVariable()));
    }

    public static NodeCreator<BlockTree, Trees.BlockNode> blockNodes() {
        return tree -> new Trees.BlockNode(
                tree.getStatements().stream().map(t -> statementNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                tree.isStatic()
        );
    }

    public static NodeCreator<BreakTree, Trees.BreakNode> breakNodes() {
        return tree -> new Trees.BreakNode(Optional.ofNullable(tree.getLabel()));
    }

    public static NodeCreator<CaseLabelTree, Trees.CaseLabelNode> caseLabelNodes() {
        return tree -> switch (tree) {
            case ConstantCaseLabelTree t -> constantCaseLabelNodes().apply(t);
            case DefaultCaseLabelTree t -> defaultCaseLabelNodes().apply(t);
            case PatternCaseLabelTree t -> patternCaseLabelNodes().apply(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public static NodeCreator<CaseTree, Trees.CaseNode> caseNodes() {
        return tree -> new Trees.CaseNode(
                tree.getCaseKind(),
                expressionNodes().apply(tree.getGuard()),
                tree.getExpressions().stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getStatements()).map(stmts -> stmts.stream().map(t -> statementNodes().apply(t)).collect(ImmutableList.toImmutableList())),
                Optional.ofNullable(tree.getBody()).map(t -> nodes().apply(t)),
                tree.getLabels().stream().map(t -> caseLabelNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<CatchTree, Trees.CatchNode> catchNodes() {
        return tree -> new Trees.CatchNode(
                variableNodes().apply(tree.getParameter()),
                blockNodes().apply(tree.getBlock())
        );
    }

    public static NodeCreator<ClassTree, Trees.ClassNode> classNodes() {
        return tree -> new Trees.ClassNode(
                tree.getSimpleName(),
                tree.getTypeParameters().stream().map(t -> typeParameterNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getExtendsClause()).map(t -> nodes().apply(t)),
                tree.getImplementsClause().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList()),
                tree.getMembers().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList()),
                modifiersNodes().apply(tree.getModifiers()),
                tree.getPermitsClause().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<CompilationUnitTree, Trees.CompilationUnitNode> compilationUnitNodes() {
        return tree -> new Trees.CompilationUnitNode(
                Optional.ofNullable(tree.getPackageName()).map(t -> expressionNodes().apply(t)),
                Optional.ofNullable(tree.getModule()).map(t -> moduleNodes().apply(t)),
                Optional.ofNullable(tree.getPackage()).map(t -> packageNodes().apply(t)),
                tree.getImports().stream().map(t -> importNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getLineMap()),
                Optional.ofNullable(tree.getPackageAnnotations()).map(anns -> anns.stream().map(t -> annotationNodes().apply(t)).collect(ImmutableList.toImmutableList())),
                Optional.ofNullable(tree.getSourceFile()),
                tree.getTypeDecls().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<CompoundAssignmentTree, Trees.CompoundAssignmentNode> compoundAssignmentNodes() {
        return tree -> new Trees.CompoundAssignmentNode(
                expressionNodes().apply(tree.getVariable()),
                tree.getKind(),
                expressionNodes().apply(tree.getExpression())
        );
    }

    public static NodeCreator<ConditionalExpressionTree, Trees.ConditionalExpressionNode> conditionalExpressionNodes() {
        return tree -> new Trees.ConditionalExpressionNode(
                expressionNodes().apply(tree.getCondition()),
                expressionNodes().apply(tree.getTrueExpression()),
                expressionNodes().apply(tree.getFalseExpression())
        );
    }

    public static NodeCreator<ConstantCaseLabelTree, Trees.ConstantCaseLabelNode> constantCaseLabelNodes() {
        return tree -> new Trees.ConstantCaseLabelNode(expressionNodes().apply(tree.getConstantExpression()));
    }

    public static NodeCreator<ContinueTree, Trees.ContinueNode> continueNodes() {
        return tree -> new Trees.ContinueNode(Optional.ofNullable(tree.getLabel()));
    }

    public static NodeCreator<DeconstructionPatternTree, Trees.DeconstructionPatternNode> deconstructionPatternNodes() {
        return tree -> new Trees.DeconstructionPatternNode(
                expressionNodes().apply(tree.getDeconstructor()),
                tree.getNestedPatterns().stream().map(t -> patternNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                tree.getKind()
        );
    }

    public static NodeCreator<DefaultCaseLabelTree, Trees.DefaultCaseLabelNode> defaultCaseLabelNodes() {
        return _ -> new Trees.DefaultCaseLabelNode();
    }

    public static NodeCreator<DirectiveTree, Trees.DirectiveNode> directiveNodes() {
        return tree -> switch (tree) {
            case ExportsTree t -> exportsNodes().apply(t);
            case OpensTree t -> opensNodes().apply(t);
            case ProvidesTree t -> providesNodes().apply(t);
            case RequiresTree t -> requiresNodes().apply(t);
            case UsesTree t -> usesNodes().apply(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public static NodeCreator<DoWhileLoopTree, Trees.DoWhileLoopNode> doWhileLoopNodes() {
        return tree -> new Trees.DoWhileLoopNode(
                expressionNodes().apply(tree.getCondition()),
                statementNodes().apply(tree.getStatement())
        );
    }

    public static NodeCreator<EmptyStatementTree, Trees.EmptyStatementNode> emptyStatementNodes() {
        return _ -> new Trees.EmptyStatementNode();
    }

    public static NodeCreator<EnhancedForLoopTree, Trees.EnhancedForLoopNode> enhancedForLoopNodes() {
        return tree -> new Trees.EnhancedForLoopNode(
                variableNodes().apply(tree.getVariable()),
                expressionNodes().apply(tree.getExpression()),
                statementNodes().apply(tree.getStatement())
        );
    }

    public static NodeCreator<ErroneousTree, Trees.ErroneousNode> erroneousNodes() {
        return tree -> new Trees.ErroneousNode(
                tree.getErrorTrees().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<ExportsTree, Trees.ExportsNode> exportsNodes() {
        return tree -> new Trees.ExportsNode(
                expressionNodes().apply(tree.getPackageName()),
                Optional.ofNullable(tree.getModuleNames()).map(names -> names.stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList()))
        );
    }

    public static NodeCreator<ExpressionTree, Trees.ExpressionNode> expressionNodes() {
        return tree -> switch (tree) {
            case AnnotatedTypeTree t -> annotatedTypeNodes().apply(t);
            case AnnotationTree t -> annotationNodes().apply(t);
            case ArrayAccessTree t -> arrayAccessNodes().apply(t);
            case ArrayTypeTree t -> arrayTypeNodes().apply(t);
            case AssignmentTree t -> assignmentNodes().apply(t);
            case BinaryTree t -> binaryNodes().apply(t);
            case CompoundAssignmentTree t -> compoundAssignmentNodes().apply(t);
            case ConditionalExpressionTree t -> conditionalExpressionNodes().apply(t);
            case ErroneousTree t -> erroneousNodes().apply(t);
            case IdentifierTree t -> identifierNodes().apply(t);
            case InstanceOfTree t -> instanceOfNodes().apply(t);
            case IntersectionTypeTree t -> intersectionTypeNodes().apply(t);
            case LambdaExpressionTree t -> lambdaExpressionNodes().apply(t);
            case LiteralTree t -> literalNodes().apply(t);
            case MemberReferenceTree t -> memberReferenceNodes().apply(t);
            case MemberSelectTree t -> memberSelectNodes().apply(t);
            case MethodInvocationTree t -> methodInvocationNodes().apply(t);
            case NewArrayTree t -> newArrayNodes().apply(t);
            case NewClassTree t -> newClassNodes().apply(t);
            case ParameterizedTypeTree t -> parameterizedTypeNodes().apply(t);
            case ParenthesizedTree t -> parenthesizedNodes().apply(t);
            case PrimitiveTypeTree t -> primitiveTypeNodes().apply(t);
            case StringTemplateTree t -> stringTemplateNodes().apply(t);
            case SwitchExpressionTree t -> switchExpressionNodes().apply(t);
            case TypeCastTree t -> typeCastNodes().apply(t);
            case UnaryTree t -> unaryNodes().apply(t);
            case UnionTypeTree t -> unionTypeNodes().apply(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public static NodeCreator<ExpressionStatementTree, Trees.ExpressionStatementNode> expressionStatementNodes() {
        return tree -> new Trees.ExpressionStatementNode(expressionNodes().apply(tree.getExpression()));
    }

    public static NodeCreator<ForLoopTree, Trees.ForLoopNode> forLoopNodes() {
        return tree -> new Trees.ForLoopNode(
                tree.getInitializer().stream().map(t -> statementNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                expressionNodes().apply(tree.getCondition()),
                tree.getUpdate().stream().map(t -> expressionStatementNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                statementNodes().apply(tree.getStatement())
        );
    }

    public static NodeCreator<IdentifierTree, Trees.IdentifierNode> identifierNodes() {
        return tree -> new Trees.IdentifierNode(tree.getName());
    }

    public static NodeCreator<IfTree, Trees.IfNode> ifNodes() {
        return tree ->
                new Trees.IfNode(
                        expressionNodes().apply(tree.getCondition()),
                        statementNodes().apply(tree.getThenStatement()),
                        Optional.ofNullable(tree.getElseStatement()).map(t -> statementNodes().apply(t))
                );
    }

    public static NodeCreator<ImportTree, Trees.ImportNode> importNodes() {
        return tree -> new Trees.ImportNode(nodes().apply(tree.getQualifiedIdentifier()), tree.isStatic());
    }

    public static NodeCreator<InstanceOfTree, Trees.InstanceOfNode> instanceOfNodes() {
        return tree -> new Trees.InstanceOfNode(
                expressionNodes().apply(tree.getExpression()),
                Optional.ofNullable(tree.getPattern()).map(t -> patternNodes().apply(t)),
                nodes().apply(tree.getType())
        );
    }

    public static NodeCreator<IntersectionTypeTree, Trees.IntersectionTypeNode> intersectionTypeNodes() {
        return tree -> new Trees.IntersectionTypeNode(
                tree.getBounds().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<LabeledStatementTree, Trees.LabeledStatementNode> labeledStatementNodes() {
        return tree -> new Trees.LabeledStatementNode(
                tree.getLabel(),
                statementNodes().apply(tree.getStatement())
        );
    }

    public static NodeCreator<LambdaExpressionTree, Trees.LambdaExpressionNode> lambdaExpressionNodes() {
        return tree -> new Trees.LambdaExpressionNode(
                tree.getParameters().stream().map(t -> variableNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                nodes().apply(tree.getBody()),
                tree.getBodyKind()
        );
    }

    public static NodeCreator<LiteralTree, Trees.LiteralNode> literalNodes() {
        return tree -> new Trees.LiteralNode(tree.getValue(), tree.getKind());
    }

    public static NodeCreator<MemberReferenceTree, Trees.MemberReferenceNode> memberReferenceNodes() {
        return tree -> new Trees.MemberReferenceNode(
                expressionNodes().apply(tree.getQualifierExpression()),
                tree.getName(),
                tree.getMode(),
                Optional.ofNullable(tree.getTypeArguments())
                        .map(tps -> tps.stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList()))
        );
    }

    public static NodeCreator<MemberSelectTree, Trees.MemberSelectNode> memberSelectNodes() {
        return tree -> new Trees.MemberSelectNode(
                Optional.ofNullable(tree.getExpression()).map(t -> expressionNodes().apply(t)),
                tree.getIdentifier()
        );
    }

    public static NodeCreator<MethodInvocationTree, Trees.MethodInvocationNode> methodInvocationNodes() {
        return tree -> new Trees.MethodInvocationNode(
                Optional.ofNullable(tree.getMethodSelect()).map(t -> expressionNodes().apply(t)),
                tree.getTypeArguments().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList()),
                tree.getArguments().stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<MethodTree, Trees.MethodNode> methodNodes() {
        return tree -> new Trees.MethodNode(
                tree.getName(),
                modifiersNodes().apply(tree.getModifiers()),
                tree.getTypeParameters().stream().map(t -> typeParameterNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                tree.getParameters().stream().map(t -> variableNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getReturnType()).map(t -> nodes().apply(t)),
                Optional.ofNullable(tree.getReceiverParameter()).map(t -> variableNodes().apply(t)),
                tree.getThrows().stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getBody()).map(t -> blockNodes().apply(t)),
                Optional.ofNullable(tree.getDefaultValue()).map(t -> nodes().apply(t))
        );
    }

    public static NodeCreator<ModifiersTree, Trees.ModifiersNode> modifiersNodes() {
        return tree -> new Trees.ModifiersNode(
                tree.getFlags().stream().collect(ImmutableSet.toImmutableSet()),
                tree.getAnnotations().stream().map(t -> annotationNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<ModuleTree, Trees.ModuleNode> moduleNodes() {
        return tree -> new Trees.ModuleNode(
                expressionNodes().apply(tree.getName()),
                tree.getModuleType(),
                tree.getDirectives().stream().map(t -> directiveNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                tree.getAnnotations().stream().map(t -> annotationNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<NewArrayTree, Trees.NewArrayNode> newArrayNodes() {
        return tree -> new Trees.NewArrayNode(
                Optional.ofNullable(tree.getType()).map(t -> nodes().apply(t)),
                tree.getDimensions().stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                tree.getInitializers().stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                tree.getAnnotations().stream().map(t -> annotationNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                tree.getDimAnnotations().stream()
                        .map(da -> da.stream().map(t -> annotationNodes().apply(t)).collect(ImmutableList.toImmutableList()))
                        .collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<NewClassTree, Trees.NewClassNode> newClassNodes() {
        return tree -> new Trees.NewClassNode(
                Optional.ofNullable(tree.getEnclosingExpression()).map(t -> expressionNodes().apply(t)),
                tree.getTypeArguments().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList()),
                expressionNodes().apply(tree.getIdentifier()),
                tree.getArguments().stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getClassBody()).map(t -> classNodes().apply(t))
        );
    }

    public static NodeCreator<OpensTree, Trees.OpensNode> opensNodes() {
        return tree -> new Trees.OpensNode(
                expressionNodes().apply(tree.getPackageName()),
                Optional.ofNullable(tree.getModuleNames()).map(names -> names.stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList()))
        );
    }

    public static NodeCreator<PackageTree, Trees.PackageNode> packageNodes() {
        return tree -> new Trees.PackageNode(
                expressionNodes().apply(tree.getPackageName()),
                tree.getAnnotations().stream().map(t -> annotationNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<ParameterizedTypeTree, Trees.ParameterizedTypeNode> parameterizedTypeNodes() {
        return tree -> new Trees.ParameterizedTypeNode(
                nodes().apply(tree.getType()),
                tree.getTypeArguments().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<ParenthesizedTree, Trees.ParenthesizedNode> parenthesizedNodes() {
        return tree -> new Trees.ParenthesizedNode(expressionNodes().apply(tree.getExpression()));
    }

    public static NodeCreator<PatternCaseLabelTree, Trees.PatternCaseLabelNode> patternCaseLabelNodes() {
        return tree -> new Trees.PatternCaseLabelNode(patternNodes().apply(tree.getPattern()));
    }

    public static NodeCreator<PatternTree, Trees.PatternNode> patternNodes() {
        return tree -> switch (tree) {
            case AnyPatternTree t -> anyPatternNodes().apply(t);
            case BindingPatternTree t -> bindingPatternNodes().apply(t);
            case DeconstructionPatternTree t -> deconstructionPatternNodes().apply(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public static NodeCreator<PrimitiveTypeTree, Trees.PrimitiveTypeNode> primitiveTypeNodes() {
        return tree -> new Trees.PrimitiveTypeNode(tree.getPrimitiveTypeKind());
    }

    public static NodeCreator<ProvidesTree, Trees.ProvidesNode> providesNodes() {
        return tree -> new Trees.ProvidesNode(
                expressionNodes().apply(tree.getServiceName()),
                tree.getImplementationNames().stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<RequiresTree, Trees.RequiresNode> requiresNodes() {
        return tree -> new Trees.RequiresNode(expressionNodes().apply(tree.getModuleName()), tree.isStatic(), tree.isTransitive());
    }

    public static NodeCreator<ReturnTree, Trees.ReturnNode> returnNodes() {
        return tree -> new Trees.ReturnNode(Optional.ofNullable(tree.getExpression()).map(t -> expressionNodes().apply(t)));
    }

    public static NodeCreator<StatementTree, Trees.StatementNode> statementNodes() {
        return tree -> switch (tree) {
            case AssertTree t -> assertNodes().apply(t);
            case BlockTree t -> blockNodes().apply(t);
            case BreakTree t -> breakNodes().apply(t);
            case ClassTree t -> classNodes().apply(t);
            case ContinueTree t -> continueNodes().apply(t);
            case DoWhileLoopTree t -> doWhileLoopNodes().apply(t);
            case EmptyStatementTree t -> emptyStatementNodes().apply(t);
            case EnhancedForLoopTree t -> enhancedForLoopNodes().apply(t);
            case ExpressionStatementTree t -> expressionStatementNodes().apply(t);
            case ForLoopTree t -> forLoopNodes().apply(t);
            case IfTree t -> ifNodes().apply(t);
            case LabeledStatementTree t -> labeledStatementNodes().apply(t);
            case ReturnTree t -> returnNodes().apply(t);
            case SwitchTree t -> switchNodes().apply(t);
            case SynchronizedTree t -> synchronizedNodes().apply(t);
            case ThrowTree t -> throwNodes().apply(t);
            case TryTree t -> tryNodes().apply(t);
            case VariableTree t -> variableNodes().apply(t);
            case WhileLoopTree t -> whileLoopNodes().apply(t);
            case YieldTree t -> yieldNodes().apply(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public static NodeCreator<StringTemplateTree, Trees.StringTemplateNode> stringTemplateNodes() {
        return tree -> new Trees.StringTemplateNode(
                tree.getFragments().stream().collect(ImmutableList.toImmutableList()),
                tree.getExpressions().stream().map(t -> expressionNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getProcessor()).map(t -> expressionNodes().apply(t))
        );
    }

    public static NodeCreator<SwitchExpressionTree, Trees.SwitchExpressionNode> switchExpressionNodes() {
        return tree -> new Trees.SwitchExpressionNode(
                expressionNodes().apply(tree.getExpression()),
                tree.getCases().stream().map(t -> caseNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<SwitchTree, Trees.SwitchNode> switchNodes() {
        return tree -> new Trees.SwitchNode(
                expressionNodes().apply(tree.getExpression()),
                tree.getCases().stream().map(t -> caseNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<SynchronizedTree, Trees.SynchronizedNode> synchronizedNodes() {
        return tree -> new Trees.SynchronizedNode(
                expressionNodes().apply(tree.getExpression()),
                blockNodes().apply(tree.getBlock())
        );
    }

    public static NodeCreator<ThrowTree, Trees.ThrowNode> throwNodes() {
        return tree -> new Trees.ThrowNode(expressionNodes().apply(tree.getExpression()));
    }

    public static NodeCreator<TryTree, Trees.TryNode> tryNodes() {
        return tree -> new Trees.TryNode(
                tree.getResources().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList()),
                blockNodes().apply(tree.getBlock()),
                tree.getCatches().stream().map(t -> catchNodes().apply(t)).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getFinallyBlock()).map(t -> blockNodes().apply(t))
        );
    }

    public static NodeCreator<TypeCastTree, Trees.TypeCastNode> typeCastNodes() {
        return tree -> new Trees.TypeCastNode(
                nodes().apply(tree.getType()),
                expressionNodes().apply(tree.getExpression())
        );
    }

    public static NodeCreator<TypeParameterTree, Trees.TypeParameterNode> typeParameterNodes() {
        return tree -> new Trees.TypeParameterNode(
                tree.getName(),
                tree.getBounds().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList()),
                tree.getAnnotations().stream().map(t -> annotationNodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<UnaryTree, Trees.UnaryNode> unaryNodes() {
        return tree -> new Trees.UnaryNode(expressionNodes().apply(tree.getExpression()), tree.getKind());
    }

    public static NodeCreator<UnionTypeTree, Trees.UnionTypeNode> unionTypeNodes() {
        return tree -> new Trees.UnionTypeNode(
                tree.getTypeAlternatives().stream().map(t -> nodes().apply(t)).collect(ImmutableList.toImmutableList())
        );
    }

    public static NodeCreator<UsesTree, Trees.UsesNode> usesNodes() {
        return tree -> new Trees.UsesNode(expressionNodes().apply(tree.getServiceName()));
    }

    public static NodeCreator<VariableTree, Trees.VariableNode> variableNodes() {
        return tree -> new Trees.VariableNode(
                tree.getName(),
                Optional.ofNullable(tree.getType()).map(t -> nodes().apply(t)),
                modifiersNodes().apply(tree.getModifiers()),
                Optional.ofNullable(tree.getNameExpression()).map(t -> expressionNodes().apply(t)),
                Optional.ofNullable(tree.getInitializer()).map(t -> expressionNodes().apply(t))
        );
    }

    public static NodeCreator<WhileLoopTree, Trees.WhileLoopNode> whileLoopNodes() {
        return tree -> new Trees.WhileLoopNode(
                expressionNodes().apply(tree.getCondition()),
                statementNodes().apply(tree.getStatement())
        );
    }

    public static NodeCreator<WildcardTree, Trees.WildcardNode> wildcardNodes() {
        return tree -> new Trees.WildcardNode(Optional.ofNullable(tree.getBound()).map(t -> nodes().apply(t)), tree.getKind());
    }

    public static NodeCreator<YieldTree, Trees.YieldNode> yieldNodes() {
        return tree -> new Trees.YieldNode(expressionNodes().apply(tree.getValue()));
    }
}
