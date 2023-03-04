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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class ChangeMethodReturnType implements ErrorRule<Void> {

    private static final Set<String> CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.cant.ret.val.from.meth.decl.void",
            "compiler.err.prob.found.req"
    ));

    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        TreePath parentPath = treePath.getParentPath();
        if (parentPath == null || parentPath.getLeaf().getKind() != Kind.RETURN) return null;
        
        TreePath method = null;
        TreePath tp = treePath;

        while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            if (tp.getLeaf().getKind() == Kind.METHOD) {
                method = tp;
                break;
            }

            tp = tp.getParentPath();
        }

        if (method == null) return null;

        MethodTree mt = (MethodTree) tp.getLeaf();

        if (mt.getReturnType() == null) return null;

        TypeMirror targetType; 

        if (treePath.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
            String expression = info.getText().substring((int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), treePath.getLeaf()), (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), treePath.getLeaf()));
            Scope s = info.getTrees().getScope(treePath);
            ExpressionTree expr = info.getTreeUtilities().parseExpression(expression, new SourcePositions[1]);

            targetType = purify(info, info.getTreeUtilities().attributeTree(expr, s));
        } else {
            targetType = purify(info, info.getTrees().getTypeMirror(treePath));
        }

        if (targetType == null || targetType.getKind() == TypeKind.EXECUTABLE) return null;

        return Collections.singletonList(new FixImpl(info, method, TypeMirrorHandle.create(targetType), info.getTypeUtilities().getTypeName(targetType).toString()).toEditorFix());
    }

    private TypeMirror purify(CompilationInfo info, TypeMirror targetType) {
        if (targetType != null && targetType.getKind() == TypeKind.ERROR) {
            targetType = info.getTrees().getOriginalType((ErrorType) targetType);
        }

        if (targetType == null || targetType.getKind() == /*XXX:*/TypeKind.ERROR || targetType.getKind() == TypeKind.NONE || targetType.getKind() == TypeKind.NULL) return null;

        return Utilities.resolveTypeForDeclaration(info, targetType);
    }

    @Override
    public String getId() {
        return ChangeMethodReturnType.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ChangeMethodReturnType.class, "DN_ChangeMethodReturnType");
    }

    @Override
    public void cancel() {}

    static final class FixImpl extends JavaFix {

        private final TypeMirrorHandle targetTypeHandle;
        private final String targetTypeDN;

        public FixImpl(CompilationInfo info, TreePath tp, TypeMirrorHandle targetTypeHandle, String targetTypeDN) {
            super(info, tp);
            this.targetTypeHandle = targetTypeHandle;
            this.targetTypeDN = targetTypeDN;
        }

        @Override
        protected String getText() {
            return NbBundle.getMessage(ChangeMethodReturnType.class, "FIX_ChangeMethodReturnType", ChangeTypeFix.escape(targetTypeDN));
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            TypeMirror targetType = targetTypeHandle.resolve(wc);

            if (targetType == null) {
                //XXX: log
                return ;
            }

            MethodTree mt = (MethodTree) tp.getLeaf();
            TreeMaker make = wc.getTreeMaker();

            wc.rewrite(mt.getReturnType(), make.Type(targetType));
        }

    }

}
