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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class RemoveUselessCast implements ErrorRule<Void> {
    
    public RemoveUselessCast() {
    }
    
    public Set<String> getCodes() {
        return Collections.singleton("compiler.warn.redundant.cast"); // NOI18N
    }
    
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        
        if (path != null && path.getLeaf().getKind() == Kind.TYPE_CAST) {
            return Collections.singletonList(new FixImpl(info, path).toEditorFix());
        }
        
        return Collections.<Fix>emptyList();
    }

    public void cancel() {
        //XXX: not yet implemented
    }
    
    public String getId() {
        return RemoveUselessCast.class.getName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(RemoveUselessCast.class, "LBL_Remove_Useless_Cast_Fix");
    }
    
    public String getDescription() {
        return NbBundle.getMessage(RemoveUselessCast.class, "DSC_Remove_Useless_Cast_Fix");
    }

    private static final class FixImpl extends JavaFix {
        
        public FixImpl(CompilationInfo info, TreePath path) {
            super(info, path);
        }

        public String getText() {
            return NbBundle.getMessage(RemoveUselessCast.class, "LBL_FIX_Remove_redundant_cast");
        }
        
        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath path = ctx.getPath();
            TypeCastTree tct = (TypeCastTree) path.getLeaf();
            ExpressionTree expression = tct.getExpression();

            while (expression.getKind() == Kind.PARENTHESIZED
                   && !JavaFixUtilities.requiresParenthesis(((ParenthesizedTree) expression).getExpression(), tct, path.getParentPath().getLeaf())) {
                expression = ((ParenthesizedTree) expression).getExpression();
            }

            while (path.getParentPath().getLeaf().getKind() == Kind.PARENTHESIZED
                   && !JavaFixUtilities.requiresParenthesis(expression, path.getLeaf(), path.getParentPath().getParentPath().getLeaf())) {
                path = path.getParentPath();
            }

            wc.rewrite(path.getLeaf(), expression);
        }
    }
}
