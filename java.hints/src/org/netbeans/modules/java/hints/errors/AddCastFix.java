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

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.TreePath;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
final class AddCastFix extends JavaFix {
    
    private JavaSource js;
    private String treeName;
    private String type;
    private final TypeMirrorHandle<TypeMirror> targetType;
    private final TreePathHandle idealTypeTree;
    
    public AddCastFix(CompilationInfo info, TreePath expression, TreePath idealTypeTree, TypeMirror targetType) {
        super(info, expression);
        this.idealTypeTree = idealTypeTree != null ? TreePathHandle.create(idealTypeTree, info) : null;
        this.targetType = TypeMirrorHandle.create(targetType);
        this.treeName = Utilities.shortDisplayName(info, (ExpressionTree) expression.getLeaf());
        this.type = org.netbeans.modules.editor.java.Utilities.getTypeName(info, targetType, false).toString();
    }
    
    @Override
    protected void performRewrite(TransformationContext ctx) throws Exception {
        TypeMirror resolvedTargetType = targetType.resolve(ctx.getWorkingCopy());
        
        if (resolvedTargetType == null) {
            //cannot resolve anymore:
            return;
        }
        
        TreePath resolvedIdealTypeTree = idealTypeTree != null ? idealTypeTree.resolve(ctx.getWorkingCopy()) : null;
        
        TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
        ExpressionTree toCast = (ExpressionTree) ctx.getPath().getLeaf();

        Class interf = toCast.getKind().asInterface();
        boolean wrapWithBrackets = interf == BinaryTree.class || interf == ConditionalExpressionTree.class;

        if (/*TODO: replace with JavaFixUtilities.requiresparenthesis*/wrapWithBrackets) {
            toCast = make.Parenthesized(toCast);
        }

        ExpressionTree cast = make.TypeCast(resolvedIdealTypeTree != null ? resolvedIdealTypeTree.getLeaf() : make.Type(resolvedTargetType), toCast);

        ctx.getWorkingCopy().rewrite(ctx.getPath().getLeaf(), cast);
    }
    
    public String getText() {
        return NbBundle.getMessage(AddCastFix.class, "LBL_FIX_Add_Cast", treeName, type); // NOI18N
    }
    
    public String toDebugString() {
        return "[AddCastFix:" + treeName + ":" + type + "]";
    }

}
