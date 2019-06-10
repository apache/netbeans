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
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.TreeShims;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_org.netbeans.modules.java.hints.jdk.ConvertSwitchToTraditionalSwitch",
        description = "#DESC_org.netbeans.modules.java.hints.jdk.ConvertSwitchToTraditionalSwitch", category = "rules15",
        minSourceVersion = "12")
@Messages({
    "DN_org.netbeans.modules.java.hints.jdk.ConvertSwitchToTraditionalSwitch=Convert switch to traditional switch",
    "DESC_org.netbeans.modules.java.hints.jdk.ConvertSwitchToTraditionalSwitch=Convert switch to traditional switch",})
public class ConvertSwitchToTraditionalSwitch {

    @TriggerTreeKind(Tree.Kind.SWITCH)
    @Messages({"ERR_ConvertSwitchToTraditionalSwitch=Convert switch to traditional switch"})
    public static ErrorDescription ruleSwitch2TraditionalSwitch(HintContext ctx) {
        SwitchTree st = (SwitchTree) ctx.getPath().getLeaf();
        for (CaseTree ct : st.getCases()) {
            if (!TreeShims.RULE_CASE_KIND.equals(TreeShims.getCaseKind(ct))) {
                return null;
            }
        }
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertSwitchToTraditionalSwitch(), new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());

    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath switchStatement) {
            super(info, switchStatement);
        }

        @Override
        @Messages("FIX_ConvertSwitchToTraditionalSwitch=Convert to traditional switch")
        protected String getText() {
            return Bundle.FIX_ConvertSwitchToTraditionalSwitch();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            TreePath tp = ctx.getPath();
            SwitchTree st = (SwitchTree) tp.getLeaf();
            WorkingCopy wc = ctx.getWorkingCopy();
            TreeMaker make = wc.getTreeMaker();
            List<CaseTree> newCases = new ArrayList<>();
            List<? extends CaseTree> cases;
            List<ExpressionTree> patterns = new ArrayList<>();
            boolean switchExpressionFlag = st.getKind().toString().equals(TreeShims.SWITCH_EXPRESSION);
            if (switchExpressionFlag) {
                cases = TreeShims.getCases(st);
            } else {
                cases = ((SwitchTree) st).getCases();
            }
            for (Iterator<? extends CaseTree> it = cases.iterator(); it.hasNext();) {
                CaseTree ct = it.next();
                patterns.addAll(TreeShims.getExpressions(ct));
                List<StatementTree> statements;
                if (ct.getStatements() == null) {
                    statements = new ArrayList<>(((JCTree.JCCase) ct).stats);
                    if (statements.size() == 1 && statements.get(0).getKind() == Tree.Kind.BLOCK) {
                        statements = new ArrayList<>(((BlockTree) statements.get(0)).getStatements());
                    }
                } else {
                    statements = new ArrayList<>(ct.getStatements());
                }
                statements.add(make.Break(null));
                if (patterns.isEmpty()) {
                    newCases.add(make.Case(null, statements));
                } else {
                    for (int i = 0; i < patterns.size() - 1; i++) {
                        newCases.add(make.Case(patterns.get(i), new ArrayList<>()));
                    }
                    newCases.add(make.Case(patterns.get(patterns.size() - 1), statements));
                }

                patterns = new ArrayList<>();
            }

            wc.rewrite((SwitchTree) st, make.Switch(((SwitchTree) st).getExpression(), newCases));
        }
    }

}
