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
import com.sun.source.tree.CaseLabelTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.source.TreeShims;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;

/**
 *
 * @author SANDEEMI
 */
@NbBundle.Messages("FIX_Add_Default=Add Default")
public class AddDefault implements ErrorRule<Void> {

    private static final Set<String> CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.not.exhaustive",
            "compiler.err.not.exhaustive.statement"
    ));

    @Override
    public Set<String> getCodes() {
        return Collections.unmodifiableSet(CODES);
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        Tree leaf = treePath.getLeaf();
        return Collections.<Fix>singletonList(new AddDefault.FixImpl(info, treePath).toEditorFix());
    }

    @Override
    public String getId() {
        return AddDefault.class.getName();
    }

    @Override
    public String getDisplayName() {
        return Bundle.FIX_Add_Default();
    }

    @Override
    public void cancel() {

    }

    private static final class FixImpl extends JavaFix {

        CompilationInfo info;
        TreePath path;

        public FixImpl(CompilationInfo info, TreePath path) {
            super(info, path);
            this.info = info;
            this.path = path;
        }

        @Override
        protected String getText() {
            return Bundle.FIX_Add_Default();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreeMaker make = wc.getTreeMaker();

            if (path.getLeaf().getKind().toString().equals("SWITCH_EXPRESSION")) {
                Tree tree = path.getLeaf();
                ParenthesizedTree expression = ((ParenthesizedTree) TreeShims.getExpressions(tree).get(0));
                List<CaseTree> cases = (List<CaseTree>) TreeShims.getCases(tree);
                int size = cases.size();
                String text = "throw new IllegalStateException(\"Unexpected value: \" + " + expression.toString() + ");";
                TreeUtilities tu = wc.getTreeUtilities();
                StatementTree parseStatement = tu.parseStatement(text, new SourcePositions[1]);
                CaseTree caseSwitchPatterns;

                if (TreeShims.isRuleCase(cases.get(0))) {
                    caseSwitchPatterns = make.CasePatterns(new LinkedList<>(), parseStatement);
                } else {
                    List<StatementTree> statements = new ArrayList<>();
                    statements.add((StatementTree) parseStatement);
                    caseSwitchPatterns = make.CasePatterns(new LinkedList<>(), statements);
                }
                List<CaseTree> newCases = new ArrayList<>();
                newCases.addAll(cases);
                newCases.add(caseSwitchPatterns);
                Tree SwitchExpression = make.SwitchExpression(expression, newCases);
                wc.rewrite(tree, SwitchExpression);
            } else {
                SwitchTree switchTree = (SwitchTree) path.getLeaf();
                ExpressionTree expression = ((ParenthesizedTree) switchTree.getExpression()).getExpression();
                int size = switchTree.getCases().size();
                String text = "throw new IllegalStateException(\"Unexpected value: \" + " + expression.toString() + ");";
                TreeUtilities tu = wc.getTreeUtilities();
                StatementTree parseStatement = tu.parseStatement(text, new SourcePositions[1]);
                CaseTree caseSwitchPatterns;

                if (TreeShims.isRuleCase((CaseTree) switchTree.getCases().get(0))) {
                    caseSwitchPatterns = make.CasePatterns(new LinkedList<>(), parseStatement);
                } else {
                    handleLastCase(switchTree, wc, make, size);
                    List<StatementTree> statements = new ArrayList<>();
                    statements.add((StatementTree) parseStatement);
                    caseSwitchPatterns = make.CasePatterns(new LinkedList<>(), statements);
                }
                SwitchTree insertSwitchCase = make.insertSwitchCase(switchTree, size, caseSwitchPatterns);
                wc.rewrite(switchTree, insertSwitchCase);
            }
        }

        private void handleLastCase(SwitchTree switchTree, WorkingCopy wc, TreeMaker make, int size) {
            CaseTree lastCase = switchTree.getCases().get(size - 1);
            List<Tree> labels = (List<Tree>) TreeShims.getLabels(lastCase);
            List<? extends StatementTree> statements = lastCase.getStatements();
            StatementTree lastStatement = statements.get(statements.size() - 1);
            if (!(lastStatement instanceof JCTree.JCBreak)) {
                List<StatementTree> newStatements = new ArrayList<>(statements);
                BreakTree Break = make.Break(null);
                newStatements.add(Break);
                CaseTree addCaseStatement = make.CasePatterns(labels, newStatements);
                wc.rewrite(lastCase, addCaseStatement);
            }
        }
    }
}
