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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CaseTree.CaseKind;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;

/**
 * Resolves javac error by adding a missing default case to a switch.
 * 
 * @author SANDEEMI
 */
@NbBundle.Messages("FIX_Add_Default_Case=Add Default Case")
public class AddDefaultCase implements ErrorRule<Void> {

    private static final Set<String> CODES = Set.of(
            "compiler.err.not.exhaustive",
            "compiler.err.not.exhaustive.statement"
    );

    private static final String THROW_ISE = "throw new IllegalStateException(\"Unexpected value: \" + %1$s);";

    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        return List.of(new AddDefaultCaseFix(info, treePath).toEditorFix());
    }

    @Override
    public String getId() {
        return AddDefaultCase.class.getName();
    }

    @Override
    public String getDisplayName() {
        return Bundle.FIX_Add_Default_Case();
    }

    @Override
    public void cancel() {

    }

    private static final class AddDefaultCaseFix extends JavaFix {

        public AddDefaultCaseFix(CompilationInfo info, TreePath path) {
            super(info, path);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_Add_Default_Case();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreeMaker make = wc.getTreeMaker();
            TreePath path = ctx.getPath();

            switch (path.getLeaf().getKind()) {
                case SWITCH_EXPRESSION -> {
                    SwitchExpressionTree expTree = (SwitchExpressionTree) path.getLeaf();
                    List<? extends CaseTree> cases = expTree.getCases();
                    if (cases.isEmpty()) {
                        return;
                    }
                    ParenthesizedTree expression = (ParenthesizedTree) expTree.getExpression();

                    String text = THROW_ISE.formatted(expression.toString());
                    StatementTree parseStatement = wc.getTreeUtilities().parseStatement(text, new SourcePositions[1]);
                    CaseTree caseSwitchPatterns;
                    if (cases.get(0).getCaseKind() == CaseKind.RULE) {
                        caseSwitchPatterns = make.CasePatterns(List.of(), parseStatement);
                    } else {
                        caseSwitchPatterns = make.CasePatterns(List.of(), List.of(parseStatement));
                    }

                    List<CaseTree> newCases = new ArrayList<>(cases.size() + 1);
                    newCases.addAll(cases);
                    newCases.add(caseSwitchPatterns);
                    Tree switchExpression = make.SwitchExpression(expression, newCases);
                    wc.rewrite(expTree, switchExpression);
                }
                case SWITCH -> {
                    SwitchTree switchTree = (SwitchTree) path.getLeaf();
                    List<? extends CaseTree> cases = switchTree.getCases();
                    if (cases.isEmpty()) {
                        return;
                    }
                    ExpressionTree expression = ((ParenthesizedTree) switchTree.getExpression()).getExpression();

                    String text = THROW_ISE.formatted(expression.toString());
                    StatementTree parseStatement = wc.getTreeUtilities().parseStatement(text, new SourcePositions[1]);
                    CaseTree caseSwitchPatterns;
                    if (cases.get(0).getCaseKind() == CaseKind.RULE) {
                        caseSwitchPatterns = make.CasePatterns(List.of(), parseStatement);
                    } else {
                        handleLastCase(cases.get(cases.size() - 1), wc, make);
                        caseSwitchPatterns = make.CasePatterns(List.of(), List.of(parseStatement));
                    }

                    SwitchTree insertSwitchCase = make.addSwitchCase(switchTree, caseSwitchPatterns);
                    wc.rewrite(switchTree, insertSwitchCase);
                }
                default -> throw new UnsupportedOperationException(path.getLeaf().getKind() + " not implemented");
            }
        }

        private static void handleLastCase(CaseTree lastCase, WorkingCopy wc, TreeMaker make) {
            List<? extends StatementTree> statements = lastCase.getStatements();
            if (statements.isEmpty() || !(statements.get(statements.size() - 1) instanceof BreakTree)) {
                List<StatementTree> expanded = new ArrayList<>(statements.size() + 1);
                expanded.addAll(statements);
                expanded.add(make.Break(null));
                wc.rewrite(lastCase, make.CasePatterns(lastCase.getLabels(), expanded));
            }
        }
    }
}
