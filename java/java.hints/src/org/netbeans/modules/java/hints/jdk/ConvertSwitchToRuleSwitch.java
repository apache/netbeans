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

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_org.netbeans.modules.java.hints.jdk.ConvertSwitchToRuleSwitch",
      description = "#DESC_org.netbeans.modules.java.hints.jdk.ConvertSwitchToRuleSwitch", category="rules15",
      minSourceVersion = "12")
@Messages({
    "DN_org.netbeans.modules.java.hints.jdk.ConvertSwitchToRuleSwitch=Convert switch to rule switch",
    "DESC_org.netbeans.modules.java.hints.jdk.ConvertSwitchToRuleSwitch=Converts switch to rule switch",
    "DN_org.netbeans.modules.java.hints.jdk.ConvertSwitchStatementToSwitchExpression=Convert to switch expression",
    "DESC_org.netbeans.modules.java.hints.jdk.ConvertSwitchStatementToSwitchExpression=Converts to switch expression",
})
public class ConvertSwitchToRuleSwitch {
    
    @TriggerTreeKind(Tree.Kind.SWITCH)
    @Messages({"ERR_ConvertSwitchToRuleSwitch=Convert switch to rule switch", "ERR_ConvertSwitchToSwitchExpression=Convert to switch expression"})
    public static ErrorDescription switch2RuleSwitch(HintContext ctx) {
        if (!CompilerOptionsQuery.getOptions(ctx.getInfo().getFileObject()).getArguments().contains("--enable-preview"))
            return null;
        SwitchTree st = (SwitchTree) ctx.getPath().getLeaf();
        boolean completesNormally = false;
        boolean wasDefault = false;
        boolean wasEmpty = false;
        for (CaseTree ct : st.getCases()) {
            if (ct.getStatements() == null) //TODO: test
                return null;
            if (completesNormally) {
                if (!wasEmpty) //fall-through from a non-empty case
                    return null;
                if (wasDefault) //fall-through from default to a case
                    return null;
                if (!wasDefault && ct.getExpression() == null) //fall-through from a case to default
                    return null;
            }
            completesNormally = Utilities.completesNormally(ctx.getInfo(), new TreePath(ctx.getPath(), ct));
            wasDefault = ct.getExpression() == null;
            wasEmpty = ct.getStatements().isEmpty();
        }
        if (wasDefault && Utilities.isCompatibleWithSwitchExpression(st)) {
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertSwitchToSwitchExpression(), new FixImpl1(ctx.getInfo(), ctx.getPath()).toEditorFix());
        } else {
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertSwitchToRuleSwitch(), new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
        }
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath switchStatement) {
            super(info, switchStatement);
        }

        @Override
        @Messages("FIX_ConvertToRuleSwitch=Convert to rule switch")
        protected String getText() {
            return Bundle.FIX_ConvertToRuleSwitch();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            TreePath tp = ctx.getPath();
            SwitchTree st = (SwitchTree) tp.getLeaf();
            Utilities.performRewriteRuleSwitch(ctx, tp, st, false);
        }
    }

    private static final class FixImpl1 extends JavaFix {

        public FixImpl1(CompilationInfo info, TreePath switchStatement) {
            super(info, switchStatement);
        }

        @Override
        @Messages("FIX_ConvertToSwitchExpression=Convert to switch expression")
        protected String getText() {
            return Bundle.FIX_ConvertToSwitchExpression();
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) {
            TreePath tp = ctx.getPath();
            SwitchTree st = (SwitchTree) tp.getLeaf();
            Utilities.performRewriteRuleSwitch(ctx, tp, st, true);
        }
    }
}
