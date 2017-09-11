/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
