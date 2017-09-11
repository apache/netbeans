/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
 * Portions Copyrighted 2012-2013 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): Alexandru Gyori <Alexandru.Gyori at gmail.com>
 */
package org.netbeans.modules.java.hints.jdk.mapreduce;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author alexandrugyori
 */
public class Refactorer {

    private boolean untrasformable;

    private boolean isOneStatementBlock( StatementTree then) {
        return then.getKind() == Tree.Kind.BLOCK && ((BlockTree) then).getStatements().size() == 1;
    }

    private boolean isReturningIf( IfTree ifTree) {
        StatementTree then = ifTree.getThenStatement();
        if (then.getKind() == Tree.Kind.RETURN) {
            return true;
        } else if (then.getKind() == Tree.Kind.BLOCK) {
            BlockTree block = (BlockTree) then;
            if (block.getStatements().size() == 1 && block.getStatements().get(0).getKind() == Tree.Kind.RETURN) {
                return true;
            }
        }
        return false;
    }
    private EnhancedForLoopTree loop;
    private WorkingCopy workingCopy;
    private TreeMaker treeMaker;
    private PreconditionsChecker preconditionsChecker;
    private boolean hasIterable;

    public Refactorer( EnhancedForLoopTree loop,  WorkingCopy workingCopy,  PreconditionsChecker scanner) {
        this.loop = loop;
        this.workingCopy = workingCopy;
        this.treeMaker = workingCopy.getTreeMaker();
        this.preconditionsChecker = scanner;
    }
     List<ProspectiveOperation> prospectives;

    public Boolean isRefactorable() {
        prospectives = this.getListRepresentation(loop.getStatement(), true);
        if (prospectives != null && !prospectives.isEmpty()) {
            prospectives.get(prospectives.size() - 1).eagerize();
            if (this.untrasformable) {
                return false;
            }
            for ( int i = 0; i < prospectives.size() - 1; i++) {
                if (!prospectives.get(i).isLazy()) {
                    return false;
                }
            }
            hasIterable = false;
            VariableTree var = loop.getVariable();
            TypeElement el = workingCopy.getElements().getTypeElement("java.lang.Iterable"); // NOI18N
            if (el != null) {
                TreePath path = TreePath.getPath(workingCopy.getCompilationUnit(), loop.getExpression());
                TypeMirror m = workingCopy.getTrees().getTypeMirror(path);
                Types types  = workingCopy.getTypes();
                hasIterable = 
                        types.isSubtype(
                            types.erasure(m),
                            types.erasure(el.asType())
                        );
            }
            prospectives = ProspectiveOperation.mergeIntoComposableOperations(prospectives);
            return prospectives != null;

        } else {
            return false;
        }

    }

    public StatementTree refactor( TreeMaker treeMaker) {

        StatementTree loopBody = loop.getStatement();
        VariableTree var = loop.getVariable();
        ExpressionTree expr = loop.getExpression();

        MethodInvocationTree mi = chainAllProspectives(treeMaker, expr);

        ProspectiveOperation lastOperation = prospectives.get(prospectives.size() - 1);
        StatementTree returnValue = propagateSideEffects(lastOperation, mi, treeMaker);

        return returnValue;


    }

    private Boolean isIfWithContinue( IfTree ifTree) {
        StatementTree then = ifTree.getThenStatement();
        if (then.getKind() == Tree.Kind.CONTINUE) {
            return true;
        } else if (then.getKind() == Tree.Kind.BLOCK) {
            List<? extends StatementTree> statements = ((BlockTree) then).getStatements();
            if (statements.size() == 1 && statements.get(0).getKind() == Tree.Kind.CONTINUE) {
                return true;
            }

        }
        return false;
    }

    private List<ProspectiveOperation> getListRepresentation( StatementTree tree, boolean last) {
        List<ProspectiveOperation> ls = new ArrayList<ProspectiveOperation>();
        if (tree.getKind() == Tree.Kind.BLOCK) {
            ls.addAll(getBlockListRepresentation(tree, last));
        } else if (tree.getKind() == Tree.Kind.IF) {
            ls.addAll(getIfListRepresentation(tree, last));

        } else {
            ls.addAll(getSingleStatementListRepresentation(tree));
        }

        return ls;
    }

    private IfTree refactorContinuingIf( IfTree ifTree,  List<? extends StatementTree> newStatements) {
        ExpressionTree newPredicate = treeMaker.Unary(Tree.Kind.LOGICAL_COMPLEMENT, ifTree.getCondition());
        BlockTree newThen = treeMaker.Block(newStatements, false);
        return treeMaker.If(newPredicate, newThen, null);
    }

    private MethodInvocationTree chainAllProspectives( TreeMaker treeMaker,  ExpressionTree expr) {
        // Special case: if the only operation is forEach{Ordered}, 
        if (hasIterable && prospectives.size() == 1 && prospectives.get(0).isForeach()) {
            ProspectiveOperation prospective = prospectives.get(0);
            return treeMaker.MethodInvocation(
                    new ArrayList<ExpressionTree>(),
                    treeMaker.MemberSelect(expr, "forEach"), // NOI18N
                    prospective.getArguments()
            );
        }

        MethodInvocationTree mi = treeMaker.MethodInvocation(new ArrayList<ExpressionTree>(), treeMaker.MemberSelect(expr, "stream"), new ArrayList<ExpressionTree>());
        //mi = treeMaker.MethodInvocation(new ArrayList<ExpressionTree>(), treeMaker.MemberSelect(mi, "parallel"), new ArrayList<ExpressionTree>());
        for ( ProspectiveOperation prospective : prospectives) {
            mi = treeMaker.MethodInvocation(new ArrayList<ExpressionTree>(), treeMaker.MemberSelect(mi, prospective.getSuitableMethod()), prospective.getArguments());
        }
        return mi;
    }

    private StatementTree propagateSideEffects( ProspectiveOperation lastOperation,  MethodInvocationTree mi,  TreeMaker treeMaker) {
        StatementTree returnValue;
        if (lastOperation.shouldReturn()) {
            returnValue = propagateReturn(lastOperation, mi, treeMaker);
        } else if (lastOperation.shouldAssign()) {
            returnValue = treeMaker.ExpressionStatement(treeMaker.Assignment(this.preconditionsChecker.getVariableToAssign(), mi));
        } else {
            returnValue = treeMaker.ExpressionStatement(mi);
        }
        return returnValue;
    }

    private StatementTree propagateReturn( ProspectiveOperation lastOperation,  MethodInvocationTree mi,  TreeMaker treeMaker) {
        ReturnTree returnExpre = null;
        ExpressionTree pred = null;
        if ("anyMatch".equals(lastOperation.getSuitableMethod())) {
            pred = mi;
            returnExpre = treeMaker.Return(treeMaker.Literal(true));
        } else if ("noneMatch".equals(lastOperation.getSuitableMethod())) {
            pred = treeMaker.Unary(Tree.Kind.LOGICAL_COMPLEMENT, mi);
            returnExpre = treeMaker.Return(treeMaker.Literal(false));
        }
        return treeMaker.If(pred, returnExpre, null);
    }

    private List<ProspectiveOperation> getBlockListRepresentation( StatementTree tree, boolean last) {
        List<ProspectiveOperation> ls = new ArrayList<ProspectiveOperation>();
        BlockTree blockTree = (BlockTree) tree;
        List<? extends StatementTree> statements = blockTree.getStatements();
        for ( int i = 0; i < statements.size(); i++) {
            StatementTree statement = statements.get(i);
            boolean l = last &&  i == statements.size() - 1;
            if (statement.getKind() == Tree.Kind.IF) {
                IfTree ifTree = (IfTree) statement;
                if (isIfWithContinue(ifTree)) {
                    ifTree = refactorContinuingIf(ifTree, statements.subList(i + 1, statements.size()));
                    // the if was refactored, so that all the statements are nested in it, so it became
                    // the last (and single) statement within the parent
                    ls.addAll(this.getListRepresentation(ifTree, last));
                    break;
                } else if (l) {
                    ls.addAll(this.getListRepresentation(ifTree, true));
                } else {
                    if (this.isReturningIf(ifTree)) {
                        this.untrasformable = true;
                    }
                    ls.addAll(ProspectiveOperation.createOperator(ifTree, ProspectiveOperation.OperationType.MAP, preconditionsChecker, workingCopy));
                }
            } else {
                ls.addAll(getListRepresentation(statement, l));
            }
        }
        return ls;
    }

    private List<ProspectiveOperation> getIfListRepresentation( StatementTree tree, boolean last) {
        IfTree ifTree = (IfTree) tree;
        List<ProspectiveOperation> ls = new ArrayList<ProspectiveOperation>();
        if (ifTree.getElseStatement() == null) {

            StatementTree then = ifTree.getThenStatement();
            if (isOneStatementBlock(then)) {
                then = ((BlockTree) then).getStatements().get(0);
            }
            if (then.getKind() == Tree.Kind.RETURN) {
                ReturnTree returnTree = (ReturnTree) then;
                ExpressionTree returnExpression = returnTree.getExpression();
                if (returnExpression.getKind() == Tree.Kind.BOOLEAN_LITERAL && ((LiteralTree) returnExpression).getValue().equals(true)) {
                    ls.addAll(ProspectiveOperation.createOperator(ifTree, ProspectiveOperation.OperationType.ANYMATCH, this.preconditionsChecker, this.workingCopy));
                } else if (returnExpression.getKind() == Tree.Kind.BOOLEAN_LITERAL && ((LiteralTree) returnExpression).getValue().equals(false)) {
                    ls.addAll(ProspectiveOperation.createOperator(ifTree, ProspectiveOperation.OperationType.NONEMATCH, this.preconditionsChecker, this.workingCopy));
                }
            } else {
                ls.addAll(ProspectiveOperation.createOperator(ifTree, ProspectiveOperation.OperationType.FILTER, this.preconditionsChecker, this.workingCopy));
                ls.addAll(getListRepresentation(ifTree.getThenStatement(), last));
            }
        } else {

            ls.addAll(ProspectiveOperation.createOperator(ifTree, ProspectiveOperation.OperationType.MAP, this.preconditionsChecker, this.workingCopy));
        }
        return ls;
    }

    private List<ProspectiveOperation> getSingleStatementListRepresentation( StatementTree tree) {
        List<ProspectiveOperation> ls = new ArrayList<ProspectiveOperation>();
        if (this.preconditionsChecker.isReducer() && this.preconditionsChecker.getReducer().equals(tree)) {
            ls.addAll(ProspectiveOperation.createOperator(tree, ProspectiveOperation.OperationType.REDUCE, this.preconditionsChecker, this.workingCopy));
        } else {
            ls.addAll(ProspectiveOperation.createOperator(tree, ProspectiveOperation.OperationType.MAP, this.preconditionsChecker, this.workingCopy));
        }
        return ls;
    }
}
