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

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.Feature;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFix.TransformationContext;
import org.openide.util.NbBundle;

/**
 * Handle error rule "compiler.err.switch.mixing.case.types" and provide the
 * fix.
 *
 * @author vkprabha
 */
public class DifferentCaseKindsFix implements ErrorRule<Void> {

    private static final Set<String> ERROR_CODES = Set.of("compiler.err.switch.mixing.case.types"); // NOI18N

    @Override
    public Set<String> getCodes() {
        return ERROR_CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        if (!Feature.SWITCH_EXPRESSIONS.isEnabled(info)) {
            return null;
        }
        TreePath parentPath = treePath.getParentPath();
        if(parentPath.getLeaf() instanceof CaseTree){
            parentPath = parentPath.getParentPath();
        }
        List<? extends CaseTree> caseTrees = null;
        boolean flag = false;
        if(parentPath.getLeaf().getKind() == Kind.SWITCH_EXPRESSION) {
            caseTrees = ((SwitchExpressionTree) parentPath.getLeaf()).getCases();
        } else {
            flag = true;
            caseTrees = ((SwitchTree) parentPath.getLeaf()).getCases();
        }
            boolean completesNormally = false;
            boolean wasDefault = false;
            boolean wasEmpty = false;
            for (CaseTree ct : caseTrees) {
                if (ct.getStatements() == null && ct.getBody() == null) {
                    return null;
                } else if (flag && ct.getStatements() != null) {
                    if (completesNormally) {
                        if (!wasEmpty) {//fall-through from a non-empty case
                            return null;
                        }
                        if (wasDefault) {//fall-through from default to a case
                            return null;
                        }
                        if (!wasDefault && ct.getExpression() == null) {//fall-through from a case to default
                            return null;
                        }
                    }
                    completesNormally = Utilities.completesNormally(info, new TreePath(treePath.getParentPath(), ct));
                    wasDefault = ct.getExpression() == null;
                    wasEmpty = ct.getStatements().isEmpty();
                }
            }      

        return Collections.<Fix>singletonList(new DifferentCaseKindsFix.FixImpl(info, treePath).toEditorFix());
    }    

    @Override
    public String getId() {
        return DifferentCaseKindsFix.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DifferentCaseKindsFix.class, "FIX_DifferentCaseKinds"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(DifferentCaseKindsFix.class, "FIX_DifferentCaseKinds"); // NOI18N
    }

    @Override
    public void cancel() {
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath path) {
            super(info, path);
        }

        @Override
        protected String getText() {
            return NbBundle.getMessage(DifferentCaseKindsFix.class, "FIX_DifferentCaseKinds"); // NOI18N
        }

        public String toDebugString() {
            return NbBundle.getMessage(DifferentCaseKindsFix.class, "FIX_DifferentCaseKinds"); // NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            TreePath tp = ctx.getPath();
            TreePath switchPath = tp.getParentPath();
            if(switchPath.getLeaf() instanceof CaseTree){
                switchPath = switchPath.getParentPath();
            }
            Tree switchBlock = switchPath.getLeaf();
            Utilities.performRewriteRuleSwitch(ctx, tp, switchBlock, false);
        }

    } 
    
}
