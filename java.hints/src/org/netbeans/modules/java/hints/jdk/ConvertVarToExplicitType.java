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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFix.TransformationContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle.Messages;

/**
 * Hint to convert type in local variable declaration from 'var' to explicit
 * type
 *
 * @author rtaneja
 */
@Hint(displayName = "#DN_ConvertVarToExplicitType", description = "#DESC_ConvertVarToExplicitType", category = "rules15", minSourceVersion = "10")
@Messages("MSG_ConvertibleToExplicitType=Convert var to explicit type")
public class ConvertVarToExplicitType {

    @TriggerPattern("$mods$ $type $var = $init") //NOI18N
    public static ErrorDescription convertVarToExplicitType(HintContext ctx) {

        if (!isLocalVarType(ctx)) {
            return null;
        }
        TreePath treePath = ctx.getPath();
        if (hasDiagnosticErrors(ctx.getInfo(), treePath.getLeaf())) {
            return null;
        }

        if (!isValidType(ctx)) {
            return null;
        }

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.MSG_ConvertibleToExplicitType(),
                new JavaFixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }

    /**
     * Fix for converting local 'var' type to explicit variable type
     *
     */
    private static final class JavaFixImpl extends JavaFix {

        public JavaFixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        @Messages("FIX_convertVarToExplicitType=Replace var with explicit type")
        protected String getText() {
            return Bundle.FIX_convertVarToExplicitType();
        }

        @Override
        protected void performRewrite(TransformationContext tc) throws Exception {
            WorkingCopy wc = tc.getWorkingCopy();
            CompilationUnitTree cut = wc.getCompilationUnit();
            TreePath statementPath = tc.getPath();

            TreeMaker make = wc.getTreeMaker();

            if (statementPath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                VariableTree oldVariableTree = (VariableTree) statementPath.getLeaf();
                TypeMirror type = wc.getTrees().getTypeMirror(statementPath);
                VariableTree newVariableTree = make.Variable(
                        oldVariableTree.getModifiers(),
                        oldVariableTree.getName(),
                        make.Type(type),
                        oldVariableTree.getInitializer()
                );
                wc.rewrite(oldVariableTree, newVariableTree);
            }
        }

    }

    /**
     *
     * @param ctx : HintContext
     * @return true if pre-conditions for hint to be enable is meet
     */
    private static boolean isLocalVarType(HintContext ctx) {

        CompilationInfo info = ctx.getInfo();
        
        if (info.getSourceVersion().compareTo(SourceVersion.RELEASE_9) < 1) {
            return false;
        }        

        TreePath treePath = ctx.getPath();

        // should be local variable
        if (info.getTrees().getElement(treePath).getKind() != ElementKind.LOCAL_VARIABLE) {
            return false;
        }

        // variable declaration of type 'var'
        return info.getTreeUtilities().isVarType(treePath);
    }

    private static boolean hasDiagnosticErrors(CompilationInfo info, Tree tree) {
        long startPos = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
        long endPos = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree);

        for (Diagnostic<?> d : info.getDiagnostics()) {
            if (d.getKind() == Kind.ERROR) {
                if ((d.getPosition() >= startPos) && (d.getPosition() <= endPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    //filter anonymous class and intersection types
    private static boolean isValidType(HintContext ctx) {
        TreePath treePath = ctx.getPath();
        TypeMirror variableTypeMirror = ctx.getInfo().getTrees().getElement(treePath).asType();

        if (Utilities.isAnonymousType(variableTypeMirror)) {
            return false;
        } else if (variableTypeMirror.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) variableTypeMirror;
            if (dt.getTypeArguments().size() > 0) {
                for (TypeMirror paramType : dt.getTypeArguments()) {
                    if (Utilities.isAnonymousType(paramType)) {
                        return false;
                    }
                }
            }
        }

        if (!Utilities.isValidType(variableTypeMirror) ||(variableTypeMirror.getKind() == TypeKind.INTERSECTION)) {
            return false;
        }
        return true;
    }
}
