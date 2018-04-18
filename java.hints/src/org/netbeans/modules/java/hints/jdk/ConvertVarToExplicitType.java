/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
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
@Messages("MSG_ConvertibleToExplicitType=Convert var type to explict type")
public class ConvertVarToExplicitType {

    @TriggerPattern("$mods$ $type $var = $init") //NOI18N
    public static ErrorDescription convertVarToExplicitType(HintContext ctx) {

        if (!isLocalVarType(ctx)) {
            return null;
        }
        TreePath treePath = ctx.getPath();

        TreePath initTreePath = ctx.getVariables().get("$init");  //NOI18N
        ExpressionTree t = ctx.getInfo().getTreeUtilities().parseExpression(initTreePath.getLeaf().toString(), null);
        Scope s = ctx.getInfo().getTrees().getScope(ctx.getPath());
        TypeMirror initTypeMirror = ctx.getInfo().getTreeUtilities().attributeTree(t, s);

        TypeMirror variableTypeMiror = ctx.getInfo().getTrees().getElement(treePath).asType();

        // variable initializer type should be same as variable type.
        if ((variableTypeMiror.getKind() == TypeKind.ERROR) || (!ctx.getInfo().getTypes().isSameType(variableTypeMiror, initTypeMirror))) {
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

}
