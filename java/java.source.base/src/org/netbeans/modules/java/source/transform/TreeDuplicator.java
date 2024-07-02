/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.source.transform;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AnyPatternTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ConstantCaseLabelTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DeconstructionPatternTree;
import com.sun.source.tree.DefaultCaseLabelTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PatternCaseLabelTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.tree.YieldTree;
import com.sun.tools.javac.util.Context;
import java.util.List;
import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.TreeFactory;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
public class TreeDuplicator implements TreeVisitor<Tree, Void> {

    private final CommentHandlerService comments;
    private final ASTService model;
    private final TreeFactory make;

    public TreeDuplicator(Context context) {
        comments = CommentHandlerService.instance(context);
        model = ASTService.instance(context);
        make = TreeFactory.instance(context);
    }

    @Override
    public Tree visitAnnotatedType(AnnotatedTypeTree tree, Void p) {
        AnnotatedTypeTree n = make.AnnotatedType(tree.getAnnotations(), tree.getUnderlyingType());
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitAnnotation(AnnotationTree tree, Void p) {
        AnnotationTree n = tree.getKind() == Tree.Kind.ANNOTATION
                ? make.Annotation(tree.getAnnotationType(), tree.getArguments())
                : make.TypeAnnotation(tree.getAnnotationType(), tree.getArguments());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitMethodInvocation(MethodInvocationTree tree, Void p) {
        MethodInvocationTree n = make.MethodInvocation((List<? extends ExpressionTree>)tree.getTypeArguments(), tree.getMethodSelect(), tree.getArguments());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitAssert(AssertTree tree, Void p) {
        AssertTree n = make.Assert(tree.getCondition(), tree.getDetail());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitAssignment(AssignmentTree tree, Void p) {
        AssignmentTree n = make.Assignment(tree.getVariable(), tree.getExpression());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitCompoundAssignment(CompoundAssignmentTree tree, Void p) {
        CompoundAssignmentTree n = make.CompoundAssignment(tree.getKind(), tree.getVariable(), tree.getExpression());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitBinary(BinaryTree tree, Void p) {
        BinaryTree n = make.Binary(tree.getKind(), tree.getLeftOperand(), tree.getRightOperand());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitBlock(BlockTree tree, Void p) {
        BlockTree n = make.Block(tree.getStatements(), tree.isStatic());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitBreak(BreakTree tree, Void p) {
        BreakTree n = make.Break(tree.getLabel());
//        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitCase(CaseTree tree, Void p) {
        CaseTree n = tree.getCaseKind() == CaseTree.CaseKind.STATEMENT
                ? make.CaseMultiplePatterns(tree.getLabels(), tree.getGuard(), tree.getStatements())
                : make.CaseMultiplePatterns(tree.getLabels(), tree.getGuard(), tree.getBody());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitCatch(CatchTree tree, Void p) {
        CatchTree n = make.Catch(tree.getParameter(), tree.getBlock());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitClass(ClassTree tree, Void p) {
        ClassTree n = make.Class(tree.getModifiers(), tree.getSimpleName(), tree.getTypeParameters(),
                                     tree.getExtendsClause(), tree.getImplementsClause(), tree.getMembers());
        model.setElement(n, model.getElement(tree));
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitConditionalExpression(ConditionalExpressionTree tree, Void p) {
        ConditionalExpressionTree n = make.ConditionalExpression(tree.getCondition(), tree.getTrueExpression(), tree.getFalseExpression());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitContinue(ContinueTree tree, Void p) {
        ContinueTree n = make.Continue(tree.getLabel());
//        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitDoWhileLoop(DoWhileLoopTree tree, Void p) {
        DoWhileLoopTree n = make.DoWhileLoop(tree.getCondition(), tree.getStatement());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitErroneous(ErroneousTree tree, Void p) {
        ErroneousTree n = make.Erroneous(tree.getErrorTrees());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitExpressionStatement(ExpressionStatementTree tree, Void p) {
        ExpressionStatementTree n = make.ExpressionStatement(tree.getExpression());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitEnhancedForLoop(EnhancedForLoopTree tree, Void p) {
        EnhancedForLoopTree n = make.EnhancedForLoop(tree.getVariable(), tree.getExpression(), tree.getStatement());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitForLoop(ForLoopTree tree, Void p) {
         ForLoopTree n = make.ForLoop(tree.getInitializer(), tree.getCondition(), tree.getUpdate(), tree.getStatement());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitIdentifier(IdentifierTree tree, Void p) {
        IdentifierTree n = make.Identifier(tree.getName());
        model.setElement(n, model.getElement(tree));
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitIf(IfTree tree, Void p) {
        IfTree n = make.If(tree.getCondition(), tree.getThenStatement(), tree.getElseStatement());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitPackage(PackageTree tree, Void p) {
        PackageTree n = make.Package(tree.getAnnotations(), tree.getPackageName());
        model.setElement(n, model.getElement(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitImport(ImportTree tree, Void p) {
        ImportTree n = make.Import(tree.getQualifiedIdentifier(), tree.isStatic());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitArrayAccess(ArrayAccessTree tree, Void p) {
        ArrayAccessTree n = make.ArrayAccess(tree.getExpression(), tree.getIndex());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitLabeledStatement(LabeledStatementTree tree, Void p) {
        LabeledStatementTree n = make.LabeledStatement(tree.getLabel(), tree.getStatement());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitLiteral(LiteralTree tree, Void p) {
        LiteralTree n = make.Literal(tree.getValue());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitMethod(MethodTree tree, Void p) {
        MethodTree n = make.Method((ModifiersTree) tree.getModifiers(),
                tree.getName().toString(), (ExpressionTree) tree.getReturnType(),
                tree.getTypeParameters(), tree.getParameters(),
                tree.getThrows(), tree.getBody(),
                (ExpressionTree) tree.getDefaultValue());
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitModifiers(ModifiersTree tree, Void p) {
        ModifiersTree n = make.Modifiers(tree.getFlags(), tree.getAnnotations());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitNewArray(NewArrayTree tree, Void p) {
        NewArrayTree n = make.NewArray(tree.getType(), tree.getDimensions(), tree.getInitializers());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitNewClass(NewClassTree tree, Void p) {
        NewClassTree n = make.NewClass((ExpressionTree)tree.getEnclosingExpression(),
                (List<? extends ExpressionTree>)tree.getTypeArguments(),
                tree.getIdentifier(), tree.getArguments(), tree.getClassBody());
        model.setElement(n, model.getElement(tree));
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitLambdaExpression(LambdaExpressionTree tree, Void p) {
        LambdaExpressionTree n = make.LambdaExpression(tree.getParameters(), tree.getBody());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitParenthesized(ParenthesizedTree tree, Void p) {
        ParenthesizedTree n = make.Parenthesized(tree.getExpression());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitReturn(ReturnTree tree, Void p) {
        ReturnTree n = make.Return(tree.getExpression());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree tree, Void p) {
        MemberSelectTree n = make.MemberSelect(tree.getExpression(), tree.getIdentifier());
        model.setElement(n, model.getElement(tree));
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitMemberReference(MemberReferenceTree tree, Void p) {
        MemberReferenceTree n = make.MemberReference(tree.getMode(), tree.getName(), tree.getQualifierExpression(), (List<ExpressionTree>)tree.getTypeArguments());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitEmptyStatement(EmptyStatementTree tree, Void p) {
        EmptyStatementTree n = make.EmptyStatement();
//        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitSwitch(SwitchTree tree, Void p) {
        SwitchTree n = make.Switch(tree.getExpression(), tree.getCases());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitSynchronized(SynchronizedTree tree, Void p) {
        SynchronizedTree n = make.Synchronized(tree.getExpression(), tree.getBlock());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitThrow(ThrowTree tree, Void p) {
        ThrowTree n = make.Throw(tree.getExpression());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree tree, Void p) {
        CompilationUnitTree n = make.CompilationUnit(tree.getPackage(), tree.getImports(), TreeHelpers.getCombinedTopLevelDecls(tree), tree.getSourceFile());
        model.setElement(n, model.getElement(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitTry(TryTree tree, Void p) {
        TryTree n = make.Try(tree.getResources(), tree.getBlock(), tree.getCatches(), tree.getFinallyBlock());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitParameterizedType(ParameterizedTypeTree tree, Void p) {
        ParameterizedTypeTree n = make.ParameterizedType(tree.getType(), tree.getTypeArguments());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitUnionType(UnionTypeTree tree, Void p) {
        UnionTypeTree n = make.UnionType(tree.getTypeAlternatives());
//	model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitIntersectionType(IntersectionTypeTree tree, Void p) {
        IntersectionTypeTree n = make.IntersectionType(tree.getBounds());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitArrayType(ArrayTypeTree tree, Void p) {
        ArrayTypeTree n = make.ArrayType(tree.getType());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitTypeCast(TypeCastTree tree, Void p) {
        TypeCastTree n = make.TypeCast(tree.getType(), tree.getExpression());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitPrimitiveType(PrimitiveTypeTree tree, Void p) {
        PrimitiveTypeTree n = make.PrimitiveType(tree.getPrimitiveTypeKind());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitTypeParameter(TypeParameterTree tree, Void p) {
        TypeParameterTree n = make.TypeParameter(tree.getName(), 
                (List<? extends ExpressionTree>)tree.getBounds());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitInstanceOf(InstanceOfTree tree, Void p) {
        InstanceOfTree n = make.InstanceOf(tree.getExpression(), tree.getType());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitUnary(UnaryTree tree, Void p) {
        UnaryTree n = make.Unary(tree.getKind(), tree.getExpression());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitVariable(VariableTree tree, Void p) {
        VariableTree n = make.Variable(tree.getModifiers(), tree.getName().toString(), tree.getType(), tree.getInitializer());
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitWhileLoop(WhileLoopTree tree, Void p) {
        WhileLoopTree n = make.WhileLoop(tree.getCondition(), tree.getStatement());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitWildcard(WildcardTree tree, Void p) {
        WildcardTree n = make.Wildcard(tree.getKind(), tree.getBound());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitModule(ModuleTree tree, Void p) {
        ModuleTree n = make.Module(make.Modifiers(0, tree.getAnnotations()), tree.getModuleType(), tree.getName(), tree.getDirectives());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitExports(ExportsTree tree, Void p) {
        ExportsTree n = make.Exports(tree.getPackageName(), tree.getModuleNames());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitOpens(OpensTree tree, Void p) {
        OpensTree n = make.Opens(tree.getPackageName(), tree.getModuleNames());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitProvides(ProvidesTree tree, Void p) {
        ProvidesTree n = make.Provides(tree.getServiceName(), tree.getImplementationNames());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitRequires(RequiresTree tree, Void p) {
        RequiresTree n = make.Requires(tree.isTransitive(), tree.isStatic(), tree.getModuleName());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitUses(UsesTree tree, Void p) {
        UsesTree n = make.Uses(tree.getServiceName());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitYield(YieldTree tree, Void p) {
        YieldTree n = make.Yield(tree.getValue());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitBindingPattern(BindingPatternTree tree, Void p) {
        BindingPatternTree n = make.BindingPattern(tree.getVariable());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitDefaultCaseLabel(DefaultCaseLabelTree tree, Void p) {
        DefaultCaseLabelTree n = make.DefaultCaseLabel();
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitConstantCaseLabel(ConstantCaseLabelTree tree, Void p) {
        ConstantCaseLabelTree n = make.ConstantCaseLabel(tree.getConstantExpression());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitPatternCaseLabel(PatternCaseLabelTree tree, Void p) {
        PatternCaseLabelTree n = make.PatternCaseLabel(tree.getPattern());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitDeconstructionPattern(DeconstructionPatternTree tree, Void p) {
        DeconstructionPatternTree n = make.DeconstructionPattern(tree.getDeconstructor(), tree.getNestedPatterns());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitAnyPattern(AnyPatternTree tree, Void p) {
        return make.AnyPattern();
    }

    @Override
    public Tree visitSwitchExpression(SwitchExpressionTree tree, Void p) {
        SwitchExpressionTree n = make.SwitchExpression(tree.getExpression(), tree.getCases());
        model.setType(n, model.getType(tree));
        comments.copyComments(tree, n);
        model.setPos(n, model.getPos(tree));
        return n;
    }

    @Override
    public Tree visitOther(Tree tree, Void p) {
        return tree;
    }
}
