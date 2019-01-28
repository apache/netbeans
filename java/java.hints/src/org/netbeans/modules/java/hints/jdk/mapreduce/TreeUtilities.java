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

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.tree.JCTree;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.TreeShims;
import org.netbeans.spi.java.hints.JavaFix;

/**
 *
 * @author alexandrugyori
 */
public class TreeUtilities {

    public static boolean isCompoundAssignementAssignement(Tree.Kind kind) {
        return Tree.Kind.AND_ASSIGNMENT == kind
                || Tree.Kind.OR_ASSIGNMENT == kind
                || Tree.Kind.PLUS_ASSIGNMENT == kind
                || Tree.Kind.MINUS_ASSIGNMENT == kind
                || Tree.Kind.MULTIPLY_ASSIGNMENT == kind
                || Tree.Kind.DIVIDE_ASSIGNMENT == kind
                || Tree.Kind.REMAINDER_ASSIGNMENT == kind
                || Tree.Kind.LEFT_SHIFT_ASSIGNMENT == kind
                || Tree.Kind.RIGHT_SHIFT_ASSIGNMENT == kind
                || Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT == kind;
    }

    public static boolean isPreOrPostfixOp(Tree.Kind opKind) {
        return opKind == Tree.Kind.POSTFIX_INCREMENT
                || opKind == Tree.Kind.PREFIX_INCREMENT
                || opKind == Tree.Kind.POSTFIX_DECREMENT
                || opKind == Tree.Kind.PREFIX_DECREMENT;
    }
    
    public static boolean completesNormally(CompilationInfo info, TreePath tp) {
        class Scanner extends TreePathScanner<Void, Void> {

            private boolean completesNormally = true;
            private Set<Tree> seenTrees = new HashSet<>();

            @Override
            public Void visitReturn(ReturnTree node, Void p) {
                completesNormally = false;
                return null;
            }

            @Override
            public Void visitBreak(BreakTree node, Void p) {
                completesNormally &= seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()));
                return null;
            }

            @Override
            public Void visitContinue(ContinueTree node, Void p) {
                completesNormally &= seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()));
                return null;
            }

            @Override
            public Void visitThrow(ThrowTree node, Void p) {
                completesNormally = false;
                return null;
            }

            @Override
            public Void visitIf(IfTree node, Void p) {
                boolean origCompletesNormally = completesNormally;
                scan(node.getThenStatement(), p);
                boolean afterThen = completesNormally;
                completesNormally = origCompletesNormally;
                scan(node.getElseStatement(), p);
                completesNormally |= afterThen;
                return null;
            }

            @Override
            public Void visitSwitch(SwitchTree node, Void p) {
                //exhaustiveness: (TODO)
                boolean hasDefault = node.getCases().stream().anyMatch(c -> c.getExpression() == null);
                if (node.getCases().size() > 0) {
                    scan(node.getCases().get(node.getCases().size() - 1), p);
                }
                completesNormally |= !hasDefault;
                return null;
            }

            //TODO: loops
            @Override
            public Void scan(Tree tree, Void p) {
                seenTrees.add(tree);
                return super.scan(tree, p);
            }

            @Override
            public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                return null;
            }

            @Override
            public Void visitClass(ClassTree node, Void p) {
                return null;
            }
        }

        Scanner scanner = new Scanner();

        scanner.scan(tp, null);
        return scanner.completesNormally;
    }

    public static void performRewriteRuleSwitch(JavaFix.TransformationContext ctx, TreePath tp, SwitchTree st) {
        WorkingCopy wc = ctx.getWorkingCopy();
        TreeMaker make = wc.getTreeMaker();
        List<CaseTree> newCases = new ArrayList<>();
        Set<VariableElement> variablesDeclaredInOtherCases = new HashSet<>();
        List<ExpressionTree> patterns = new ArrayList<>();

        for (Iterator<? extends CaseTree> it = st.getCases().iterator(); it.hasNext();) {
            CaseTree ct = it.next();
            TreePath casePath = new TreePath(tp, ct);
            patterns.addAll(TreeShims.getExpressions(ct));
            List<StatementTree> statements;
            if (ct.getStatements() == null) {
                statements = new ArrayList<>(((JCTree.JCCase) ct).stats);
            } else {
                statements = new ArrayList<>(ct.getStatements());
            }
            if (statements.isEmpty()) {
                if (it.hasNext()) {
                    continue;
                }
                //last case, no break
            } else if (statements.get(statements.size() - 1).getKind() == Tree.Kind.BREAK
                    && ctx.getWorkingCopy().getTreeUtilities().getBreakContinueTarget(new TreePath(new TreePath(tp, ct), statements.get(statements.size() - 1))) == st) {
                statements.remove(statements.size() - 1);
            } else {
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitBlock(BlockTree node, Void p) {
                        if (!node.getStatements().isEmpty()
                                && node.getStatements().get(node.getStatements().size() - 1).getKind() == Tree.Kind.BREAK
                                && ctx.getWorkingCopy().getTreeUtilities().getBreakContinueTarget(new TreePath(getCurrentPath(), node.getStatements().get(node.getStatements().size() - 1))) == st) {
                            wc.rewrite(node, make.removeBlockStatement(node, node.getStatements().get(node.getStatements().size() - 1)));
                            //TODO: optimize ifs?
                        }
                        return super.visitBlock(node, p);
                    }
                }.scan(new TreePath(new TreePath(tp, ct), statements.get(statements.size() - 1)), null);
            }
            Set<Element> seenVariables = new HashSet<>();
            int idx = 0;
            for (StatementTree statement : new ArrayList<>(statements)) {
                TreePath statementPath = new TreePath(casePath, statement);
                if (statement.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                    ExpressionTree expr = ((ExpressionStatementTree) statement).getExpression();
                    if (expr.getKind() == Tree.Kind.ASSIGNMENT) {
                        AssignmentTree at = (AssignmentTree) expr;
                        Element var = wc.getTrees().getElement(new TreePath(new TreePath(statementPath, at), at.getVariable()));
                        if (variablesDeclaredInOtherCases.contains(var)) {
                            seenVariables.add(var);
                            //XXX: take type from the original variable
                            wc.rewrite(statement,
                                    make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), var.getSimpleName(), make.Type(var.asType()), at.getExpression()));
                        }
                    }
                }
                Set<Element> thisStatementSeenVariables = new HashSet<>();
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitIdentifier(IdentifierTree node, Void p) {
                        Element el = wc.getTrees().getElement(getCurrentPath());
                        if (variablesDeclaredInOtherCases.contains(el) && seenVariables.add(el)) {
                            thisStatementSeenVariables.add(el);
                        }
                        return super.visitIdentifier(node, p);
                    }
                }.scan(statementPath, null);

                if (!thisStatementSeenVariables.isEmpty()) {
                    for (Element el : thisStatementSeenVariables) {
                        VariableElement var = (VariableElement) el;
                        statements.add(idx++, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), var.getSimpleName(), make.Type(var.asType()), null));
                    }
                }
                idx++;
            }
            Tree body = make.Block(statements, false);
            if (statements.size() == 1) {
                if (statements.get(0).getKind() == Tree.Kind.EXPRESSION_STATEMENT
                        || statements.get(0).getKind() == Tree.Kind.THROW
                        || statements.get(0).getKind() == Tree.Kind.BLOCK) {
                    body = statements.get(0);
                }
            }
            newCases.add(make.Case(patterns, body));
            patterns = new ArrayList<>();
            for (StatementTree statement : getSwitchStatement(ct)) {
                if (statement.getKind() == Tree.Kind.VARIABLE) {
                    variablesDeclaredInOtherCases.add((VariableElement) wc.getTrees().getElement(new TreePath(casePath, statement)));
                }
            }
        }

        wc.rewrite(st, make.Switch(st.getExpression(), newCases));
    }

    private static List<? extends StatementTree> getSwitchStatement(CaseTree ct) {
        if (ct.getStatements() != null) {
            return ct.getStatements();
        } else if (ct instanceof JCTree.JCCase) {
            return ((JCTree.JCCase) ct).stats;
        } else {
            return null;
        }
    }

}
