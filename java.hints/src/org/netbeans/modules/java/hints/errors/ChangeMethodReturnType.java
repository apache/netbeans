/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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

    private final static Set<String> CODES = new HashSet<String>(Arrays.asList(
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

        TypeMirror targetType = purify(info, info.getTrees().getTypeMirror(treePath));

        if (targetType == null) return null;

        if (targetType.getKind() == TypeKind.EXECUTABLE) {
            String expression = info.getText().substring((int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), treePath.getLeaf()), (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), treePath.getLeaf()));
            Scope s = info.getTrees().getScope(treePath);
            ExpressionTree expr = info.getTreeUtilities().parseExpression(expression, new SourcePositions[1]);

            targetType = purify(info, info.getTreeUtilities().attributeTree(expr, s));
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
