/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.SideEffectVisitor;
import org.netbeans.modules.java.hints.StopProcessing;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.introduce.TreeUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

/**
 * Detects that 
 * @author sdedic
 */
@Hint(
    displayName = "#DN_SuspiciousToArray",
    description = "#DESC_SuspiciousToArrayCall",
    category = "bugs",
    enabled = true,
    suppressWarnings = "SuspiciousToArrayCall"
)
@NbBundle.Messages({
    "# {0} - the array type",
    "# {1} - the collection parameter type",
    "TEXT_SuspiciousToArrayCol=Suspicious Collection.toArray() call. Collection item type {1} is not assignable to array component type {0}",
    "# {0} - the array type",
    "# {1} - the collection parameter type",
    "TEXT_SuspiciousToArrayCast=Suspicious Collection.toArray() call. The array type {0}[] is not the same as casted-to type {1}[]",
    "# {0} - new component type",
    "FIX_ChangeToArrayType=Change array type to {0}[]",
    "# {0} - new component type",
    "FIX_ReplaceWithNewArray=Replace with new {0}[]"
})
public class SuspiciousToArray {
    @TriggerPattern(value = "$c.toArray($arr)", constraints = @ConstraintVariableType(variable = "$c", type = "java.util.Collection"))
    public static ErrorDescription run(HintContext ctx) {
        TreePath expr = ctx.getPath();
        TreePath colPath = ctx.getVariables().get("$c");
        CompilationInfo ci = ctx.getInfo();
        
        TypeMirror colType;
        
        if (colPath != null) {
            colType = ci.getTrees().getTypeMirror(colPath);
        } else {
            TreePath clazz = TreeUtils.findClass(expr);
            if (clazz == null) {
                return null;
            }
            colType = ci.getTrees().getTypeMirror(clazz);
        }
        if (!Utilities.isValidType(colType) || colType.getKind() != TypeKind.DECLARED) {
            return null;
        }
        TreePath arrPath = ctx.getVariables().get("$arr"); // NOI18N
        TypeMirror arrType = ci.getTrees().getTypeMirror(arrPath);
        if (!Utilities.isValidType(arrType) || arrType.getKind() != TypeKind.ARRAY) {
            return null;
        }
        // possible method call result ?
        arrType = SourceUtils.resolveCapturedType(ci, arrType);
        TypeMirror compType = ((ArrayType)arrType).getComponentType();
                
        StringBuilder sb = new StringBuilder();
        if (colPath != null) {
            int posStart = (int)ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), colPath.getLeaf());
            int posEnd = (int)ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), colPath.getLeaf());
            sb.append(ci.getSnapshot().getText().subSequence(posStart, posEnd)).append(".");
        }
        // iterator.next is about the only method, which _returns_ something typed by collection type parameter - 
        // we need to get "capture" type of E, so it could be assigned to E[].
        sb.append("iterator().next()"); // NOI18N
        ExpressionTree colItemExpr = ci.getTreeUtilities().parseExpression(sb.toString(), null);
        if (colItemExpr.getKind() != Tree.Kind.METHOD_INVOCATION) {
            // something weird in the selector expression, probably
            return null;
        }
        Scope s = ci.getTrees().getScope(ctx.getPath());
        List<Diagnostic<? extends JavaFileObject>> diags = new ArrayList<>();
        TypeMirror colItemType = ci.getTreeUtilities().attributeTree(colItemExpr, s);
        
         TypeMirror argType = colItemType;
        TypeMirror resType = null;
        
        if (argType == null || argType.getKind() == TypeKind.DECLARED &&
            ((TypeElement)((DeclaredType)argType).asElement()).getQualifiedName().contentEquals("java.lang.Object")) { // NOI18N
            argType = null;
            // check against the parent's typecast
            TreePath parent = expr.getParentPath();
            while (parent != null && parent.getLeaf().getKind() == Tree.Kind.TYPE_CAST) {
                TypeMirror tm = ci.getTrees().getTypeMirror(parent);
                if (tm.getKind() == TypeKind.ARRAY) {
                    resType = tm;
                }
                parent = parent.getParentPath();
            }
            if (resType != null) {
                resType = ((ArrayType)resType).getComponentType();
                if (ci.getTypes().isSameType(compType, resType)) {
                    // this is OK
                    return null;
                }
            }
        } else {
            // check against collection type parameter
            resType = argType;
            if (ci.getTypes().isAssignable(resType, compType)) {
                // this is OK
                return null;
            }
        }
        if (!Utilities.isValidType(resType)) {
            return null;
        }
        resType = SourceUtils.resolveCapturedType(ci, resType);
        String msg = argType == null ? Bundle.TEXT_SuspiciousToArrayCast(compType, resType) : 
                Bundle.TEXT_SuspiciousToArrayCol(compType, argType);
        Fix fix;
        // if the array expression at arrPath is not a 'new xx[]', we should replace the entire expression.
        if (arrPath.getLeaf().getKind() == Tree.Kind.NEW_ARRAY) {
            fix = new ChangeArrayTypeFix(
                    TreePathHandle.create(arrPath, ci), colPath == null ? null :TreePathHandle.create(colPath, ci),
                    TypeMirrorHandle.create(resType), 
                    ci.getTypeUtilities().getTypeName(resType).toString()
                ).toEditorFix();
        } else {
            // and if the $c is not a simple identifier / MemberSelect composed of only identifiers, 
            SideEffectVisitor sev = new SideEffectVisitor(ctx);
            try {
                if (colPath != null) {
                    sev.scan(colPath, ci);
                }
                fix = new ChangeArrayTypeFix(
                        TreePathHandle.create(arrPath, ci), colPath == null ? null : TreePathHandle.create(colPath, ci),
                        TypeMirrorHandle.create(resType), 
                        ci.getTypeUtilities().getTypeName(resType).toString()
                    ).toEditorFix();
            } catch (StopProcessing ex) {
                // side effects -> no fix
                fix = null;
            }
        }
        return ErrorDescriptionFactory.forTree(ctx, arrPath, msg, fix);
    }
    
    private static class ChangeArrayTypeFix extends JavaFix {
        private final TypeMirrorHandle ctype;
        private final TreePathHandle colReference;
        private final String typeName;

        public ChangeArrayTypeFix(TreePathHandle handle, TreePathHandle colReference, TypeMirrorHandle ctype, String typeName) {
            super(handle);
            this.ctype = ctype;
            this.typeName = typeName;
            this.colReference = colReference;
        }

        @Override
        protected String getText() {
            return Bundle.FIX_ChangeToArrayType(typeName);
        }
        
        private int numberOfDimensions(TypeMirror arr) {
            int dim = 0;
            while (arr.getKind() == TypeKind.ARRAY) {
                arr = ((ArrayType)arr).getComponentType();
                dim++;
            }
            return dim;
        }
        
        private void rewriteNewArrayTree(WorkingCopy copy, TreeMaker mk, TreePath natPath, TypeMirror compType) {
            NewArrayTree nat = (NewArrayTree)natPath.getLeaf();
            TypeMirror existing = copy.getTrees().getTypeMirror(natPath);
            int existingDim = numberOfDimensions(existing);
            int newDim = numberOfDimensions(compType);
            
            if (existingDim == newDim + 1 /* newDim is counted from component type, lacks the enclosing array */) {
                // simple, number of dimensions does not change
                copy.rewrite(nat.getType(), mk.Type(compType));
                return;
            }
            List<ExpressionTree> l = new ArrayList<ExpressionTree>(nat.getDimensions().subList(
                    0, Math.min(newDim + 1, nat.getDimensions().size())));
            Tree replacement = mk.NewArray(mk.Type(compType), l, null);
            GeneratorUtilities.get(copy).copyComments(nat, replacement, true);
            GeneratorUtilities.get(copy).copyComments(nat, replacement, false);
            
            copy.rewrite(nat, replacement);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath path = ctx.getPath();
            TypeMirror compType = ctype.resolve(wc);
            if (compType == null) {
                return;
            }
            TreeMaker mk = wc.getTreeMaker();
            Tree l = path.getLeaf();
            if (l.getKind() == Tree.Kind.NEW_ARRAY) {
                NewArrayTree nat = (NewArrayTree)l;
                // if there are some initializers, we should probably rewrite the whole expression.
                if (nat.getInitializers() == null) {
                    rewriteNewArrayTree(wc, mk, path, compType);
                    return;
                }
            }
            // replace the entire tree
            TreePath colRef = null;
            if (colReference != null) {
                colRef = colReference.resolve(wc);
                if (colRef == null) {
                    return;
                }
            }
            GeneratorUtilities gu = GeneratorUtilities.get(wc);
            Tree lc = gu.importComments(l, wc.getCompilationUnit());
            Tree newArrayTree = mk.NewArray(mk.Type(compType), Collections.<ExpressionTree>singletonList(
                    mk.MethodInvocation(Collections.<ExpressionTree>emptyList(), 
                        colRef == null ? mk.Identifier("size") :
                        mk.MemberSelect((ExpressionTree)colRef.getLeaf(), "size"), // NOI18N
                        Collections.<ExpressionTree>emptyList())),
                    null);
            gu.copyComments(lc, newArrayTree, true);
            gu.copyComments(lc, newArrayTree, false);
            wc.rewrite(lc, newArrayTree);
        }
        
    }
}
