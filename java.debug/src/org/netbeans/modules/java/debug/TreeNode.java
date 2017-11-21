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
package org.netbeans.modules.java.debug;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ExpressionStatementTree;
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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class TreeNode extends AbstractNode implements OffsetProvider {
    
    private TreePath tree;
    private CompilationInfo info;
    private boolean synthetic;
    
    public static Node getTree(CompilationInfo info, TreePath tree, AtomicBoolean cancel) {
        List<Node> result = new ArrayList<Node>();
        
        new FindChildrenTreeVisitor(info, cancel).scan(tree, result);
        
        return result.get(0);
    }

    public static @CheckForNull Node findNode(@NonNull Node parent, @NonNull TreePath tree) {
        List<Tree> trees = new LinkedList<Tree>();

        while (tree != null) {
            trees.add(tree.getLeaf());
            tree = tree.getParentPath();
        }

        if (trees.isEmpty()) return null;
        
        Collections.reverse(trees);
        Iterator<Tree> it = trees.iterator();

        it.next();

        return findNode(parent, it);
    }

    private static @NonNull Node findNode(@NonNull Node parent, @NonNull Iterator<Tree> trees) {
        if (!trees.hasNext()) return parent;

        Tree next = trees.next();

        for (Node child : parent.getChildren().getNodes(true)) {
            if (child.getLookup().lookup(Tree.class) == next) {
                return findNode(child, trees);
            }
        }

        return parent;
    }

    public TreeNode(CompilationInfo info, TreePath tree, List<Node> nodes) {
        super(nodes.isEmpty() ? Children.LEAF: new NodeChilren(nodes), Lookups.singleton(tree.getLeaf()));
        this.tree = tree;
        this.info = info;
        this.synthetic = info.getTreeUtilities().isSynthetic(tree);
        int start = (int) info.getTrees().getSourcePositions().getStartPosition(tree.getCompilationUnit(), tree.getLeaf());
        int end   = (int) info.getTrees().getSourcePositions().getEndPosition(tree.getCompilationUnit(), tree.getLeaf());
        String text;

        if (start >= 0 && end >= 0 && end > start) {
            text = info.getText().substring(start, end);
        } else {
            text = tree.getLeaf().toString();
        }

        setDisplayName(tree.getLeaf().getKind().toString() + ":" + text); //NOI18N
        setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/tree.png"); //NOI18N
    }

    @Override
    public String getHtmlDisplayName() {
        if (synthetic) {
            return "<html><font color='#808080'>" + translate(getDisplayName()); //NOI18N
        }
        
        return null;
    }
            
    private static String[] c = new String[] {"&", "<", ">", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "&quot;"}; // NOI18N
    
    private String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }
        
        return input;
    }
    
    public int getStart() {
        return (int)info.getTrees().getSourcePositions().getStartPosition(tree.getCompilationUnit(), tree.getLeaf());
    }

    public int getEnd() {
        return (int)info.getTrees().getSourcePositions().getEndPosition(tree.getCompilationUnit(), tree.getLeaf());
    }

    public int getPreferredPosition() {
        return -1;
    }
    
    static Node nodeForElement(CompilationInfo info, Element el) {
        if (el != null) {
            return new ElementNode(info, el, Collections.<Node>emptyList());
        } else {
            return new NotFoundElementNode(NbBundle.getMessage(TreeNode.class, "Cannot_Resolve_Element"));
        }
    }
        
    static final class NodeChilren extends Children.Array {
        public NodeChilren(List<Node> nodes) {
            super(nodes);
        }
    }
    
    private static class FindChildrenTreeVisitor extends CancellableTreePathScanner<Void, List<Node>> {
        
        private final CompilationInfo info;
        
        public FindChildrenTreeVisitor(CompilationInfo info, AtomicBoolean cancel) {
            super(cancel);
            this.info = info;
        }
        
        @Override
        public Void visitAnnotation(AnnotationTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            //???
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitAnnotation(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitMethodInvocation(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitAssert(AssertTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitAssert(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitAssignment(AssignmentTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitAssignment(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitCompoundAssignment(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitBinary(BinaryTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitBinary(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitBlock(BlockTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitBlock(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitBreak(BreakTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitBreak(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitCase(CaseTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitCase(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitCatch(CatchTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitCatch(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitClass(ClassTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            addCorrespondingJavadoc(below);
            
            super.visitClass(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitConditionalExpression(ConditionalExpressionTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitConditionalExpression(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitContinue(ContinueTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitContinue(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitUnionType(UnionTypeTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();

            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitUnionType(tree, below);

            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitDoWhileLoop(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitErroneous(ErroneousTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            scan(tree.getErrorTrees(), below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitExports(ExportsTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitExports(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitExpressionStatement(ExpressionStatementTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitExpressionStatement(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitEnhancedForLoop(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitForLoop(ForLoopTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitForLoop(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitIdentifier(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitIf(IfTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitIf(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitImport(ImportTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitImport(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitArrayAccess(ArrayAccessTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitArrayAccess(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitLabeledStatement(LabeledStatementTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitLabeledStatement(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();

            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitLambdaExpression(tree, below);

            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitLiteral(LiteralTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitLiteral(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();

            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);

            super.visitMemberReference(tree, below);

            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitMethod(MethodTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            addCorrespondingJavadoc(below);
            
            super.visitMethod(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitModifiers(ModifiersTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitModifiers(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitModule(ModuleTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitModule(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitNewArray(NewArrayTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitNewArray(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitNewClass(NewClassTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitNewClass(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitParenthesized(ParenthesizedTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitParenthesized(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitProvides(ProvidesTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitProvides(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitRequires(RequiresTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitRequires(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitReturn(ReturnTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitReturn(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitMemberSelect(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitEmptyStatement(EmptyStatementTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitEmptyStatement(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitSwitch(SwitchTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitSwitch(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitSynchronized(SynchronizedTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitSynchronized(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitThrow(ThrowTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitThrow(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitCompilationUnit(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitPackage(PackageTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitPackage(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitTry(TryTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitTry(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitAnnotatedType(AnnotatedTypeTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitAnnotatedType(tree, below);

            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitParameterizedType(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitArrayType(ArrayTypeTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitArrayType(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitTypeCast(TypeCastTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitTypeCast(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitIntersectionType(IntersectionTypeTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitIntersectionType(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitPrimitiveType(PrimitiveTypeTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitPrimitiveType(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitTypeParameter(TypeParameterTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            
            super.visitTypeParameter(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitInstanceOf(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitUnary(UnaryTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitUnary(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitUses(UsesTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitUses(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitVariable(VariableTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingElement(below);
            addCorrespondingType(below);
            addCorrespondingComments(below);
            addCorrespondingJavadoc(below);
            
            super.visitVariable(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitWhileLoop(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }

        @Override
        public Void visitWildcard(WildcardTree tree, List<Node> d) {
            List<Node> below = new ArrayList<Node>();
            
            addCorrespondingType(below);
            addCorrespondingComments(below);
            super.visitWildcard(tree, below);
            
            d.add(new TreeNode(info, getCurrentPath(), below));
            return null;
        }
        
        private void addCorrespondingJavadoc(List<Node> below) {
            DocCommentTree docCommentTree = ((DocTrees) info.getTrees()).getDocCommentTree(getCurrentPath());
            
            if (docCommentTree != null) {
                below.add(new DocTreeNode(info, getCurrentPath(), docCommentTree, docCommentTree));
            } else {
                below.add(new NotFoundJavadocNode("<javadoc-not-found>"));
            }
        }

        private void addCorrespondingElement(List<Node> below) {
            Element el = info.getTrees().getElement(getCurrentPath());
            
            below.add(nodeForElement(info, el));
        }

        private void addCorrespondingType(List<Node> below) {
            TypeMirror tm = info.getTrees().getTypeMirror(getCurrentPath());
            
            if (tm != null) {
                below.add(new TypeNode(tm));
            } else {
                below.add(new NotFoundTypeNode(NbBundle.getMessage(TreeNode.class, "Cannot_Resolve_Type")));
            }
        }
        
        private void addCorrespondingComments(List<Node> below) {
            below.add(new CommentsNode(NbBundle.getMessage(TreeNode.class, "NM_Preceding_Comments"), info.getTreeUtilities().getComments(getCurrentPath().getLeaf(), true)));
            below.add(new CommentsNode(NbBundle.getMessage(TreeNode.class, "NM_Trailing_Comments"), info.getTreeUtilities().getComments(getCurrentPath().getLeaf(), false)));
        }
    }
    
    private static class NotFoundJavadocNode extends AbstractNode {
        
        public NotFoundJavadocNode(String name) {
            super(Children.LEAF);
            setName(name);
            setDisplayName(name);
//            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/element.png"); //NOI18N
        }
        
    }
    
    private static class NotFoundElementNode extends AbstractNode {
        
        public NotFoundElementNode(String name) {
            super(Children.LEAF);
            setName(name);
            setDisplayName(name);
            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/element.png"); //NOI18N
        }
        
    }
    
    private static class TypeNode extends AbstractNode {
        
        public TypeNode(TypeMirror type) {
            super(Children.LEAF);
            setDisplayName(type.getKind().toString() + ":" + type.toString()); //NOI18N
            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/type.png"); //NOI18N
        }
        
    }
    
    private static class NotFoundTypeNode extends AbstractNode {
        
        public NotFoundTypeNode(String name) {
            super(Children.LEAF);
            setName(name);
            setDisplayName(name);
            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/type.png"); //NOI18N
        }
        
    }    
}
