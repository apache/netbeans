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
