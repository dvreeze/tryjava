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

/**
 * Tree model factory, creating the immutable (thread-safe) model from trees in the jdk.compiler API.
 *
 * @author Chris de Vreeze
 */
public final class TreeModelFactory {

    public Trees.Node build(Tree tree) {
        return switch (tree) {
            case ExpressionTree t -> build(t);
            case StatementTree t -> build(t);
            case CaseLabelTree t -> build(t);
            case DirectiveTree t -> build(t);
            case PatternTree t -> build(t);
            case CaseTree t -> build(t);
            case CatchTree t -> build(t);
            case CompilationUnitTree t -> build(t);
            case ImportTree t -> build(t);
            case MethodTree t -> build(t);
            case ModifiersTree t -> build(t);
            case ModuleTree t -> build(t);
            case PackageTree t -> build(t);
            case TypeParameterTree t -> build(t);
            case WildcardTree t -> build(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public Trees.AnnotatedTypeNode build(AnnotatedTypeTree tree) {
        return new Trees.AnnotatedTypeNode(
                build(tree.getUnderlyingType()),
                tree.getAnnotations().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.AnnotationNode build(AnnotationTree tree) {
        return new Trees.AnnotationNode(
                build(tree.getAnnotationType()),
                tree.getArguments().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.AnyPatternNode build(AnyPatternTree tree) {
        return new Trees.AnyPatternNode();
    }

    public Trees.ArrayAccessNode build(ArrayAccessTree tree) {
        return new Trees.ArrayAccessNode(build(tree.getExpression()), build(tree.getIndex()));
    }

    public Trees.ArrayTypeNode build(ArrayTypeTree tree) {
        return new Trees.ArrayTypeNode(build(tree.getType()));
    }

    public Trees.AssertNode build(AssertTree tree) {
        return new Trees.AssertNode(
                build(tree.getCondition()),
                Optional.ofNullable(tree.getDetail()).map(this::build)
        );
    }

    public Trees.AssignmentNode build(AssignmentTree tree) {
        return new Trees.AssignmentNode(build(tree.getVariable()), build(tree.getExpression()));
    }

    public Trees.BinaryNode build(BinaryTree tree) {
        return new Trees.BinaryNode(
                build(tree.getLeftOperand()),
                tree.getKind(),
                build(tree.getRightOperand())
        );
    }

    public Trees.BindingPatternNode build(BindingPatternTree tree) {
        return new Trees.BindingPatternNode(build(tree.getVariable()));
    }

    public Trees.BlockNode build(BlockTree tree) {
        return new Trees.BlockNode(
                tree.getStatements().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                tree.isStatic()
        );
    }

    public Trees.BreakNode build(BreakTree tree) {
        return new Trees.BreakNode(Optional.ofNullable(tree.getLabel()));
    }

    public Trees.CaseLabelNode build(CaseLabelTree tree) {
        return switch (tree) {
            case ConstantCaseLabelTree t -> build(t);
            case DefaultCaseLabelTree t -> build(t);
            case PatternCaseLabelTree t -> build(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public Trees.CaseNode build(CaseTree tree) {
        return new Trees.CaseNode(
                tree.getCaseKind(),
                build(tree.getGuard()),
                tree.getExpressions().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getStatements()).map(stmts -> stmts.stream().map(this::build).collect(ImmutableList.toImmutableList())),
                Optional.ofNullable(tree.getBody()).map(this::build),
                tree.getLabels().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.CatchNode build(CatchTree tree) {
        return new Trees.CatchNode(build(tree.getParameter()), build(tree.getBlock()));
    }

    public Trees.ClassNode build(ClassTree tree) {
        return new Trees.ClassNode(
                tree.getSimpleName(),
                tree.getTypeParameters().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getExtendsClause()).map(this::build),
                tree.getImplementsClause().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                tree.getMembers().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                build(tree.getModifiers()),
                tree.getPermitsClause().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.CompilationUnitNode build(CompilationUnitTree tree) {
        return new Trees.CompilationUnitNode(
                Optional.ofNullable(tree.getPackageName()).map(this::build),
                Optional.ofNullable(tree.getModule()).map(this::build),
                Optional.ofNullable(tree.getPackage()).map(this::build),
                tree.getImports().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getLineMap()),
                Optional.ofNullable(tree.getPackageAnnotations()).map(anns -> anns.stream().map(this::build).collect(ImmutableList.toImmutableList())),
                Optional.ofNullable(tree.getSourceFile()),
                tree.getTypeDecls().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.CompoundAssignmentNode build(CompoundAssignmentTree tree) {
        return new Trees.CompoundAssignmentNode(
                build(tree.getVariable()),
                tree.getKind(),
                build(tree.getExpression())
        );
    }

    public Trees.ConditionalExpressionNode build(ConditionalExpressionTree tree) {
        return new Trees.ConditionalExpressionNode(
                build(tree.getCondition()),
                build(tree.getTrueExpression()),
                build(tree.getFalseExpression())
        );
    }

    public Trees.ConstantCaseLabelNode build(ConstantCaseLabelTree tree) {
        return new Trees.ConstantCaseLabelNode();
    }

    public Trees.ContinueNode build(ContinueTree tree) {
        return new Trees.ContinueNode(Optional.ofNullable(tree.getLabel()));
    }

    public Trees.DeconstructionPatternNode build(DeconstructionPatternTree tree) {
        return new Trees.DeconstructionPatternNode(
                build(tree.getDeconstructor()),
                tree.getNestedPatterns().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                tree.getKind()
        );
    }

    public Trees.DefaultCaseLabelNode build(DefaultCaseLabelTree tree) {
        return new Trees.DefaultCaseLabelNode();
    }

    public Trees.DirectiveNode build(DirectiveTree tree) {
        return switch (tree) {
            case ExportsTree t -> build(t);
            case OpensTree t -> build(t);
            case ProvidesTree t -> build(t);
            case RequiresTree t -> build(t);
            case UsesTree t -> build(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public Trees.DoWhileLoopNode build(DoWhileLoopTree tree) {
        return new Trees.DoWhileLoopNode(build(tree.getCondition()), build(tree.getStatement()));
    }

    public Trees.EmptyStatementNode build(EmptyStatementTree tree) {
        return new Trees.EmptyStatementNode();
    }

    public Trees.EnhancedForLoopNode build(EnhancedForLoopTree tree) {
        return new Trees.EnhancedForLoopNode(
                build(tree.getVariable()),
                build(tree.getExpression()),
                build(tree.getStatement())
        );
    }

    public Trees.ErroneousNode build(ErroneousTree tree) {
        return new Trees.ErroneousNode(
                tree.getErrorTrees().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.ExportsNode build(ExportsTree tree) {
        return new Trees.ExportsNode(
                build(tree.getPackageName()),
                Optional.ofNullable(tree.getModuleNames()).map(names -> names.stream().map(this::build).collect(ImmutableList.toImmutableList()))
        );
    }

    public Trees.ExpressionStatementNode build(ExpressionStatementTree tree) {
        return new Trees.ExpressionStatementNode(build(tree.getExpression()));
    }

    public Trees.ExpressionNode build(ExpressionTree tree) {
        return switch (tree) {
            case AnnotatedTypeTree t -> build(t);
            case AnnotationTree t -> build(t);
            case ArrayAccessTree t -> build(t);
            case ArrayTypeTree t -> build(t);
            case AssignmentTree t -> build(t);
            case BinaryTree t -> build(t);
            case CompoundAssignmentTree t -> build(t);
            case ConditionalExpressionTree t -> build(t);
            case ErroneousTree t -> build(t);
            case IdentifierTree t -> build(t);
            case InstanceOfTree t -> build(t);
            case IntersectionTypeTree t -> build(t);
            case LambdaExpressionTree t -> build(t);
            case LiteralTree t -> build(t);
            case MemberReferenceTree t -> build(t);
            case MemberSelectTree t -> build(t);
            case MethodInvocationTree t -> build(t);
            case NewArrayTree t -> build(t);
            case NewClassTree t -> build(t);
            case ParameterizedTypeTree t -> build(t);
            case ParenthesizedTree t -> build(t);
            case PrimitiveTypeTree t -> build(t);
            case StringTemplateTree t -> build(t);
            case SwitchExpressionTree t -> build(t);
            case TypeCastTree t -> build(t);
            case UnaryTree t -> build(t);
            case UnionTypeTree t -> build(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public Trees.ForLoopNode build(ForLoopTree tree) {
        return new Trees.ForLoopNode(
                tree.getInitializer().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                build(tree.getCondition()),
                tree.getUpdate().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                build(tree.getStatement())
        );
    }

    public Trees.IdentifierNode build(IdentifierTree tree) {
        return new Trees.IdentifierNode(tree.getName());
    }

    public Trees.IfNode build(IfTree tree) {
        return new Trees.IfNode(
                build(tree.getCondition()),
                build(tree.getThenStatement()),
                Optional.ofNullable(tree.getElseStatement()).map(this::build)
        );
    }

    public Trees.ImportNode build(ImportTree tree) {
        return new Trees.ImportNode(build(tree.getQualifiedIdentifier()), tree.isStatic());
    }

    public Trees.InstanceOfNode build(InstanceOfTree tree) {
        return new Trees.InstanceOfNode(
                build(tree.getExpression()),
                Optional.ofNullable(tree.getPattern()).map(this::build),
                build(tree.getType())
        );
    }

    public Trees.IntersectionTypeNode build(IntersectionTypeTree tree) {
        return new Trees.IntersectionTypeNode(
                tree.getBounds().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.LabeledStatementNode build(LabeledStatementTree tree) {
        return new Trees.LabeledStatementNode(
                tree.getLabel(),
                build(tree.getStatement())
        );
    }

    public Trees.LambdaExpressionNode build(LambdaExpressionTree tree) {
        return new Trees.LambdaExpressionNode(
                tree.getParameters().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                build(tree.getBody()),
                tree.getBodyKind()
        );
    }

    public Trees.LiteralNode build(LiteralTree tree) {
        return new Trees.LiteralNode(tree.getValue(), tree.getKind());
    }

    public Trees.MemberReferenceNode build(MemberReferenceTree tree) {
        return new Trees.MemberReferenceNode(
                build(tree.getQualifierExpression()),
                tree.getName(),
                tree.getMode(),
                Optional.ofNullable(tree.getTypeArguments())
                        .map(tps -> tps.stream().map(this::build).collect(ImmutableList.toImmutableList()))
        );
    }

    public Trees.MemberSelectNode build(MemberSelectTree tree) {
        return new Trees.MemberSelectNode(
                Optional.ofNullable(tree.getExpression()).map(this::build),
                tree.getIdentifier()
        );
    }

    public Trees.MethodInvocationNode build(MethodInvocationTree tree) {
        return new Trees.MethodInvocationNode(
                Optional.ofNullable(tree.getMethodSelect()).map(this::build),
                tree.getTypeArguments().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                tree.getArguments().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.MethodNode build(MethodTree tree) {
        return new Trees.MethodNode(
                tree.getName(),
                build(tree.getModifiers()),
                tree.getTypeParameters().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                tree.getParameters().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getReturnType()).map(this::build),
                Optional.ofNullable(tree.getReceiverParameter()).map(this::build),
                tree.getThrows().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getBody()).map(this::build),
                Optional.ofNullable(tree.getDefaultValue()).map(this::build)
        );
    }

    public Trees.ModifiersNode build(ModifiersTree tree) {
        return new Trees.ModifiersNode(
                tree.getFlags().stream().collect(ImmutableSet.toImmutableSet()),
                tree.getAnnotations().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.ModuleNode build(ModuleTree tree) {
        return new Trees.ModuleNode(
                build(tree.getName()),
                tree.getModuleType(),
                tree.getDirectives().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                tree.getAnnotations().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.NewArrayNode build(NewArrayTree tree) {
        return new Trees.NewArrayNode(
                Optional.ofNullable(tree.getType()).map(this::build),
                tree.getDimensions().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                tree.getInitializers().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                tree.getAnnotations().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                tree.getDimAnnotations().stream()
                        .map(da -> da.stream().map(this::build).collect(ImmutableList.toImmutableList()))
                        .collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.NewClassNode build(NewClassTree tree) {
        return new Trees.NewClassNode(
                Optional.ofNullable(tree.getEnclosingExpression()).map(this::build),
                tree.getTypeArguments().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                build(tree.getIdentifier()),
                tree.getArguments().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getClassBody()).map(this::build)
        );
    }

    public Trees.OpensNode build(OpensTree tree) {
        return new Trees.OpensNode(
                build(tree.getPackageName()),
                Optional.ofNullable(tree.getModuleNames()).map(names -> names.stream().map(this::build).collect(ImmutableList.toImmutableList()))
        );
    }

    public Trees.PackageNode build(PackageTree tree) {
        return new Trees.PackageNode(
                build(tree.getPackageName()),
                tree.getAnnotations().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.ParameterizedTypeNode build(ParameterizedTypeTree tree) {
        return new Trees.ParameterizedTypeNode(
                build(tree.getType()),
                tree.getTypeArguments().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.ParenthesizedNode build(ParenthesizedTree tree) {
        return new Trees.ParenthesizedNode(build(tree.getExpression()));
    }

    public Trees.PatternCaseLabelNode build(PatternCaseLabelTree tree) {
        return new Trees.PatternCaseLabelNode(build(tree.getPattern()));
    }

    public Trees.PatternNode build(PatternTree tree) {
        return switch (tree) {
            case AnyPatternTree t -> build(t);
            case BindingPatternTree t -> build(t);
            case DeconstructionPatternTree t -> build(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public Trees.PrimitiveTypeNode build(PrimitiveTypeTree tree) {
        return new Trees.PrimitiveTypeNode(tree.getPrimitiveTypeKind());
    }

    public Trees.ProvidesNode build(ProvidesTree tree) {
        return new Trees.ProvidesNode(
                build(tree.getServiceName()),
                tree.getImplementationNames().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.RequiresNode build(RequiresTree tree) {
        return new Trees.RequiresNode(build(tree.getModuleName()), tree.isStatic(), tree.isTransitive());
    }

    public Trees.ReturnNode build(ReturnTree tree) {
        return new Trees.ReturnNode(Optional.ofNullable(tree.getExpression()).map(this::build));
    }

    public Trees.StatementNode build(StatementTree tree) {
        return switch (tree) {
            case AssertTree t -> build(t);
            case BlockTree t -> build(t);
            case BreakTree t -> build(t);
            case ClassTree t -> build(t);
            case ContinueTree t -> build(t);
            case DoWhileLoopTree t -> build(t);
            case EmptyStatementTree t -> build(t);
            case EnhancedForLoopTree t -> build(t);
            case ExpressionStatementTree t -> build(t);
            case ForLoopTree t -> build(t);
            case IfTree t -> build(t);
            case LabeledStatementTree t -> build(t);
            case ReturnTree t -> build(t);
            case SwitchTree t -> build(t);
            case SynchronizedTree t -> build(t);
            case ThrowTree t -> build(t);
            case TryTree t -> build(t);
            case VariableTree t -> build(t);
            case WhileLoopTree t -> build(t);
            case YieldTree t -> build(t);
            default -> throw new IllegalStateException("Unexpected value: " + tree);
        };
    }

    public Trees.StringTemplateNode build(StringTemplateTree tree) {
        return new Trees.StringTemplateNode(
                tree.getFragments().stream().collect(ImmutableList.toImmutableList()),
                tree.getExpressions().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getProcessor()).map(this::build)
        );
    }

    public Trees.SwitchExpressionNode build(SwitchExpressionTree tree) {
        return new Trees.SwitchExpressionNode(
                build(tree.getExpression()),
                tree.getCases().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.SwitchNode build(SwitchTree tree) {
        return new Trees.SwitchNode(
                build(tree.getExpression()),
                tree.getCases().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.SynchronizedNode build(SynchronizedTree tree) {
        return new Trees.SynchronizedNode(build(tree.getExpression()), build(tree.getBlock()));
    }

    public Trees.ThrowNode build(ThrowTree tree) {
        return new Trees.ThrowNode(build(tree.getExpression()));
    }

    public Trees.TryNode build(TryTree tree) {
        return new Trees.TryNode(
                tree.getResources().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                build(tree.getBlock()),
                tree.getCatches().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                Optional.ofNullable(tree.getFinallyBlock()).map(this::build)
        );
    }

    public Trees.TypeCastNode build(TypeCastTree tree) {
        return new Trees.TypeCastNode(build(tree.getType()), build(tree.getExpression()));
    }

    public Trees.TypeParameterNode build(TypeParameterTree tree) {
        return new Trees.TypeParameterNode(
                tree.getName(),
                tree.getBounds().stream().map(this::build).collect(ImmutableList.toImmutableList()),
                tree.getAnnotations().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.UnaryNode build(UnaryTree tree) {
        return new Trees.UnaryNode(build(tree.getExpression()), tree.getKind());
    }

    public Trees.UnionTypeNode build(UnionTypeTree tree) {
        return new Trees.UnionTypeNode(
                tree.getTypeAlternatives().stream().map(this::build).collect(ImmutableList.toImmutableList())
        );
    }

    public Trees.UsesNode build(UsesTree tree) {
        return new Trees.UsesNode(build(tree.getServiceName()));
    }

    public Trees.VariableNode build(VariableTree tree) {
        return new Trees.VariableNode(
                tree.getName(),
                Optional.ofNullable(tree.getType()).map(this::build),
                build(tree.getModifiers()),
                Optional.ofNullable(tree.getNameExpression()).map(this::build),
                Optional.ofNullable(tree.getInitializer()).map(this::build)
        );
    }

    public Trees.WhileLoopNode build(WhileLoopTree tree) {
        return new Trees.WhileLoopNode(build(tree.getCondition()), build(tree.getStatement()));
    }

    public Trees.WildcardNode build(WildcardTree tree) {
        return new Trees.WildcardNode(Optional.ofNullable(tree.getBound()).map(this::build), tree.getKind());
    }

    public Trees.YieldNode build(YieldTree tree) {
        return new Trees.YieldNode(build(tree.getValue()));
    }
}
