/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
 * Handle error rule "compiler.err.var.not.allowed.compound" 
 * and provide the fix.
 * @author vkprabha
 */
public class VarCompDeclaration implements ErrorRule<Void> {

    private static final Set<String> ERROR_CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.var.not.allowed.compound", "compiler.err.restricted.type.not.allowed.compound")); // NOI18N

    @Override
    public Set<String> getCodes() {
        return Collections.unmodifiableSet(ERROR_CODES);
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {

        Tree.Kind parentKind = treePath.getParentPath().getLeaf().getKind();
        if (parentKind != Tree.Kind.BLOCK && parentKind != Tree.Kind.CASE) {
            return null;
        }

        return Collections.<Fix>singletonList(new VarCompDeclaration.FixImpl(info, treePath).toEditorFix());
    }

    @Override
    public String getId() {
        return VarCompDeclaration.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(VarCompDeclaration.class, "FIX_VarCompDeclaration"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(VarCompDeclaration.class, "FIX_VarCompDeclaration"); // NOI18N
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
            return NbBundle.getMessage(VarCompDeclaration.class, "FIX_VarCompDeclaration"); // NOI18N
        }

        public String toDebugString() {
            return NbBundle.getMessage(VarCompDeclaration.class, "FIX_VarCompDeclaration"); // NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath statementPath = ctx.getPath();
            Tree parent = statementPath.getParentPath().getLeaf();
            List<? extends StatementTree> statements = null;
            switch (parent.getKind()) {
                case BLOCK:
                    statements = ((BlockTree) parent).getStatements();
                    break;
                case CASE:
                    statements = ((CaseTree) parent).getStatements();
                    break;
                default:
                    // Ignore other scenario
                    break;
            }
            WorkingCopy wc = ctx.getWorkingCopy();
            TreeMaker make = wc.getTreeMaker();
            int pos = statements.indexOf(statementPath.getLeaf());
            List<StatementTree> newStatements = new ArrayList<>();
            if (pos > 0) {
                if(info.getTreeUtilities().isPartOfCompoundVariableDeclaration(statements.get(pos - 1))
                        && !info.getTreeUtilities().isEndOfCompoundVariableDeclaration(statements.get(pos - 1))){
                    pos--;
                }
                newStatements.addAll(statements.subList(0, pos));
            }

            int current = 0;
            for (current = pos; current < statements.size(); current++) {
                StatementTree t = (StatementTree) statements.get(current);
                if (t instanceof VariableTree) {
                    VariableTree oldVariableTree = (VariableTree) t;
                    VariableTree newVariableTree = make.Variable(
                            oldVariableTree.getModifiers(),
                            oldVariableTree.getName(),
                            make.Type("var"), // NOI18N
                            oldVariableTree.getInitializer()
                    );
                    newStatements.add(make.asReplacementOf(newVariableTree, oldVariableTree));
                    
                    // Check variable tree seperated with ","
                    if(info.getTreeUtilities().isEndOfCompoundVariableDeclaration(t)) break;
                }
            }
            if (current + 1 < statements.size()) {
                newStatements.addAll(statements.subList(current + 1, statements.size()));
            }

            Tree target = null;

            switch (parent.getKind()) {
                case BLOCK:
                    target = make.Block(newStatements, ((BlockTree) parent).isStatic());
                    break;
                case CASE:
                    target = make.Case(((CaseTree) parent).getExpression(), newStatements);
                    break;
                default:
                    // Ignore other scenario
                    break;
            }

            wc.rewrite(parent, target);
        }

    }

}
