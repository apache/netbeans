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
/*
 * Contributor(s): Alexandru Gyori <Alexandru.Gyori at gmail.com>
 */
package org.netbeans.modules.java.hints.jdk.mapreduce;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.source.util.Trees;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;

/**
 *
 * @author alexandrugyori
 */
public class PreconditionsChecker {

    private ForLoopTreeVisitor visitor;
    private boolean hasUncaughtException = false;
    private boolean isForLoop;
    private Set<Name> innerVariables;
    private CompilationInfo workingCopy;
    private boolean isIterable;

    public PreconditionsChecker(Tree forLoop, CompilationInfo workingCopy) {
        if (forLoop.getKind() == Tree.Kind.ENHANCED_FOR_LOOP) {
            this.isForLoop = true;
            this.workingCopy = workingCopy;
            this.hasUncaughtException = workingCopy.getTreeUtilities()
                    .getUncaughtExceptions(TreePath.getPath(workingCopy.getCompilationUnit(), forLoop)).stream().anyMatch(this::filterCheckedExceptions);
            this.innerVariables = this.getInnerVariables(forLoop, workingCopy.getTrees());
            this.visitor = new ForLoopTreeVisitor(this.innerVariables, workingCopy, new TreePath(workingCopy.getCompilationUnit()), (EnhancedForLoopTree) forLoop);
            this.isIterable = this.isIterbale(((EnhancedForLoopTree) forLoop).getExpression());
            visitor.scan(TreePath.getPath(workingCopy.getCompilationUnit(), forLoop), workingCopy.getTrees());
        } else {
            this.isForLoop = false;
        }
    }

    public Set<Name> getInnerVariables() {
        return this.innerVariables;
    }

    private Set<Name> getInnerVariables(Tree tree, Trees trees) {
        VariablesVisitor vis = new VariablesVisitor(new TreePath(workingCopy.getCompilationUnit()));
        vis.scan(tree, trees);
        return vis.getInnervariables();
    }

    public Boolean isSafeToRefactor() {
        return this.isForLoop
                && this.iteratesOverIterable()
                && !(this.throwsException()
                || this.containsNEFs()
                || this.containsReturn()
                || this.containsBreak()
                || this.containsContinue());
    }

    /*
     * Precondition 1
     * The signature of the lambda expressions used in list operations
     * does not have a throws clause in its signature.
     */
    protected Boolean throwsException() {
        return this.hasUncaughtException;
    }

    /*
     * Precondition 2
     * The body of the lambda does not refer Non-Effectively-Final(NEF) references
     * that are defined outsed the loop. 
     * This is because from within a lambda you cannot have references to a NEF.
     */
    protected Boolean containsNEFs() {
        return visitor.hasNonEffectivelyFinalVars;
    }
    /*
     * Precondition 3
     * The method is not allowed to have any break statements
     */

    protected Boolean containsBreak() {
        return visitor.containsBreak();
    }

    /*
     * Precondition 4: overly conservative - to be weakened when handled properly;
     * The method is not allowed to have continues in it.
     */
    protected Boolean containsContinue() {
        return visitor.containsContinue();
    }
    /*
     * preocndition 5: overly conservative - to be weakened when handled properly.
     * The method is not allowed to have Returns in it.
     */

    protected Boolean containsReturn() {
        return visitor.containsReturn();
    }

    public Boolean isReducer() {
        return this.visitor.reducerStatement != null;
    }

    public Tree getReducer() {
        return this.visitor.reducerStatement;
    }

    public IdentifierTree getVariableToAssign() {
        return this.visitor.mutatedVariable;
    }

    Map<Name, String> getVarToName() {
        return this.visitor.varToType;
    }

    private boolean iteratesOverIterable() {
        return this.isIterable;
    }

    /*
     * 
     */
    private boolean isIterbale(ExpressionTree expression) {
        TypeMirror tm = workingCopy.getTrees().getTypeMirror(TreePath.getPath(workingCopy.getCompilationUnit(), expression));
        if (!Utilities.isValidType(tm)) {
            return false;
        }
        if (tm.getKind() == TypeKind.ARRAY) {
            return false;
        } else {
            tm = workingCopy.getTypes().erasure(tm);
            TypeElement typeEl = workingCopy.getElements().getTypeElement("java.util.Collection");
            if (typeEl != null) {
                TypeMirror collection = typeEl.asType();
                collection = workingCopy.getTypes().erasure(collection);
                if (this.workingCopy.getTypes().isSubtype(tm, collection)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static class VariablesVisitor extends ErrorAwareTreeScanner<Tree, Trees> {

        private Set<Name> innerVariables = new HashSet<Name>();
        private Set<Name> allLocalVariables = new HashSet<Name>();
        private TreePath treePath;

        public VariablesVisitor(TreePath tp) {
            this.treePath = tp;
        }

        public Set<Name> getInnervariables() {
            return innerVariables;
        }

        public Set<Name> getAllLocalVariablesUsed() {
            return this.allLocalVariables;
        }
        @Override
        public Tree visitVariable(VariableTree that, Trees trees) {
            this.innerVariables.add(that.getName());
            this.allLocalVariables.add(that.getName());
            return super.visitVariable(that, trees);
        }

        @Override
        public Tree visitIdentifier(IdentifierTree that, Trees trees) {
            if (isLocalVariable(that, trees)) {

                this.allLocalVariables.add(that.getName());
            }
            return super.visitIdentifier(that, trees);
        }

        private boolean isLocalVariable(IdentifierTree id, Trees trees) {
            TreePath path = TreePath.getPath(treePath, id);
            if (path == null) {
                return false;
            }
            Element el = trees.getElement(path);
            if (el == null) {
                return false;
            }
            return el.getKind() == ElementKind.LOCAL_VARIABLE || el.getKind() == ElementKind.PARAMETER;
        }
    }

    /*
     * This class visits the loop to check if there are any failed preconditions.
     */
    private static class ForLoopTreeVisitor extends ErrorAwareTreePathScanner<Tree, Trees> {

        private Set<Name> inners;
        private CompilationInfo workingCopy;
        EnhancedForLoopTree loop;
        private Tree reducerStatement = null;
        private IdentifierTree mutatedVariable;
        Map<Name, String> varToType = new HashMap<Name, String>();

        public ForLoopTreeVisitor(Set<Name> inners, CompilationInfo workingCopy, TreePath treePath, EnhancedForLoopTree loop) {
            this.inners = inners;
            this.workingCopy = workingCopy;
            this.loop = loop;
        }
        private Boolean hasReturns = false;
        private Boolean hasBreaks = false;
        private Boolean hasContinue = false;
        private Boolean hasNonEffectivelyFinalVars = false;

        public Boolean containsReturn() {
            return this.hasReturns;
        }

        public Boolean containsBreak() {
            return this.hasBreaks;
        }

        public Boolean containsContinue() {
            return this.hasContinue;
        }
        //TODO: make sure you don't consider fields and etc.
        private Boolean hasOneNEFReducer = false;

        @Override
        public Tree visitIdentifier(IdentifierTree that, Trees trees) {

            TypeMirror type = trees.getTypeMirror(this.getCurrentPath());
            if (type == null /* will work even with error types || type.getKind() == TypeKind.ERROR */) {
                return super.visitIdentifier(that, trees);
            }
            if (type.getKind().isPrimitive()) {
                this.varToType.put(that.getName(), workingCopy.getTypes().boxedClass((PrimitiveType) type).toString());
            } else {
                this.varToType.put(that.getName(), type.toString());
            }
            TreePath currentTreePath = this.getCurrentPath();
            Element el = trees.getElement(currentTreePath);
            if (el != null && isExternalNEF(el, that)) {
                checkIfRefactorableMutation(currentTreePath, that);
            }
            return super.visitIdentifier(that, trees);
        }

        //Checks it the non-null element is a local variable or a parameter of the method
        private boolean isLocalVariable(Element el) {
            return el.getKind() == ElementKind.LOCAL_VARIABLE || el.getKind() == ElementKind.PARAMETER;
        }

        @Override
        public Tree visitContinue(ContinueTree that, Trees trees) {
            if (that.getLabel() != null || !isIfWithContinueOnly(that)) {
                this.hasContinue = true;
            }
            return super.visitContinue(that, trees);
        }
        private Boolean hasMatcherReturn = false;

        @Override
        public Tree visitReturn(ReturnTree that, Trees trees) {
            ExpressionTree thatExpression = that.getExpression();
            if (!this.hasMatcherReturn && thatExpression != null && thatExpression.getKind() == Tree.Kind.BOOLEAN_LITERAL
                    && thisIsMatcherReturn(that, this.getCurrentPath())) {
                this.hasMatcherReturn = true;
            } else {
                this.hasReturns = true;
            }
            return super.visitReturn(that, trees);

        }

        @Override
        public Tree visitBreak(BreakTree that, Trees trees) {
            this.hasBreaks = true;
            return super.visitBreak(that, trees);

        }

        private boolean isIfWithContinueOnly(ContinueTree that) {
            TreePath currentTreePath = this.getCurrentPath();
            TreePath parentPath = currentTreePath.getParentPath();
            Tree parentTree = parentPath.getLeaf();
            if (parentTree.getKind() == Tree.Kind.IF) {
                return true;
            } else if (parentTree.getKind() == Tree.Kind.BLOCK) {
                BlockTree parentBlock = (BlockTree) parentTree;
                if (parentBlock.getStatements().size() == 1) {
                    return true;
                }
            }
            return false;
        }

        private boolean thisIsMatcherReturn(Tree that, TreePath currentTreePath) {
            TreePath parentPath = currentTreePath.getParentPath();
            Tree parent = parentPath.getLeaf();
            if (parent.getKind() == Tree.Kind.BLOCK && ((BlockTree) parent).getStatements().size() == 1) {
                return thisIsMatcherReturn(parent, parentPath);
            } else if (parent.getKind() == Tree.Kind.IF && ((IfTree) parent).getElseStatement() == null) {
                return true;
            }
            return false;


        }

        private boolean isLastInControlFlow(TreePath pathToInstruction) {
            Tree currentTree = pathToInstruction.getLeaf();
            Tree parentTree = pathToInstruction.getParentPath().getLeaf();
            if (parentTree.equals(this.loop)) {
                return true;
            } else if (parentTree.getKind() == Tree.Kind.BLOCK) {
                List<? extends StatementTree> ls = ((BlockTree) parentTree).getStatements();
                if (ls.get(ls.size() - 1).equals(currentTree)) {
                    return isLastInControlFlow(pathToInstruction.getParentPath());
                } else {
                    return false;
                }

            } else if (parentTree.getKind() == Tree.Kind.AND.IF && ((IfTree) parentTree).getElseStatement() != null) {
                return false;
            } else {
                return this.isLastInControlFlow(pathToInstruction.getParentPath());
            }


        }

        private boolean isStatementPreOrPostfix(Tree parent, Tree parentOfParent) {
            return TreeUtilities.isPreOrPostfixOp(parent.getKind()) && parentOfParent.getKind() == Tree.Kind.EXPRESSION_STATEMENT;
        }

        private boolean isLeftHandSideOfCompoundAssignement(Tree parent, IdentifierTree that) {
            return TreeUtilities.isCompoundAssignementAssignement(parent.getKind()) && ((CompoundAssignmentTree) parent).getVariable().equals(that);
        }

        private boolean isExternalNEF(Element el, IdentifierTree that) {
            return this.isLocalVariable(el)
                    && !workingCopy.getElementUtilities().isEffectivelyFinal((VariableElement) el)
                    && !this.inners.contains(that.getName());
        }

        private boolean isPureMutator(TreePath parentOfParentPath) {
            return parentOfParentPath.getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT
                    && isLastInControlFlow(parentOfParentPath);
        }

        private void checkIfRefactorableMutation(TreePath currentTreePath, IdentifierTree that) {
            Tree parent = currentTreePath.getParentPath().getLeaf();
            TreePath parentOfParentPath = currentTreePath.getParentPath().getParentPath();
            Tree parentOfParent = parentOfParentPath.getLeaf();

            if ((isStatementPreOrPostfix(parent, parentOfParent) || isLeftHandSideOfCompoundAssignement(parent, that))
                    && isPureMutator(parentOfParentPath)) {
                if (this.hasOneNEFReducer) {
                    this.hasNonEffectivelyFinalVars = true;
                } else {
                    this.hasOneNEFReducer = true;
                    this.reducerStatement = currentTreePath.getParentPath().getParentPath().getLeaf();
                    this.mutatedVariable = that;
                }
            } else {
                this.hasNonEffectivelyFinalVars = true;
            }
        }
    };
    
    private boolean filterCheckedExceptions(TypeMirror ex) {
        TypeElement el = workingCopy.getElements().getTypeElement("java.lang.RuntimeException"); // NOI18N
        if (el == null) {
            return true;
        }
        if (workingCopy.getTypes().isSubtype(ex, el.asType())) {
            return false;
        }
        el = workingCopy.getElements().getTypeElement("java.lang.Error"); // NOI18N
        return el == null || !workingCopy.getTypes().isSubtype(ex, el.asType()); 
    }
}
