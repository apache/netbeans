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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
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
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.netbeans.spi.java.hints.MatcherUtilities;

/**
 * Hint will convert explicit type of local variable to 'var'. Supported: JDK 10
 * or above.
 *
 * @author arusinha
 */
@Hint(displayName = "#DN_CanUseVarForExplicitType", description = "#DESC_CanUseVarForExplicitType", category = "rules15", minSourceVersion = "10") //NOI18N
@Messages("MSG_ConvertibleToVarType=Explict type can be replaced with 'var'")  //NOI18N  
public class ConvertToVarHint {

    // hint will be disabled for error codes present in SKIPPED_ERROR_CODES.
    private final static Set<String> SKIPPED_ERROR_CODES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "compiler.err.generic.array.creation" //NOI18N
            )));

    @TriggerPattern("$mods$ $type $var = $init") //NOI18N

    public static ErrorDescription computeExplicitToVarType(HintContext ctx) {
        if (!preConditionChecker(ctx)) {
            return null;
        }

        TreePath treePath = ctx.getPath();

        TreePath initTreePath = ctx.getVariables().get("$init");     //NOI18N
        ExpressionTree t = ctx.getInfo().getTreeUtilities().parseExpression(initTreePath.getLeaf().toString(), null);
        Scope s = ctx.getInfo().getTrees().getScope(ctx.getPath());
        TypeMirror initTypeMirror = ctx.getInfo().getTreeUtilities().attributeTree(t, s);

        TypeMirror VariableTypeMiror = ctx.getInfo().getTrees().getElement(treePath).asType();
        
        boolean diamondOpType  = false;
        // variable initializer type should be same as variable type.
        if (!ctx.getInfo().getTypes().isSameType(VariableTypeMiror, initTypeMirror)) {
            if(MatcherUtilities.matches(ctx, initTreePath, "new $clazz<$tparams$>($params$)")) {
                if(initTypeMirror.getKind() == TypeKind.DECLARED && (VariableTypeMiror.getKind() == TypeKind.DECLARED)) {
                    DeclaredType dtInit = (DeclaredType)initTypeMirror;
                    DeclaredType dtVarType = (DeclaredType)VariableTypeMiror;
                    if(dtInit.asElement().equals(dtVarType.asElement())) {
                        diamondOpType = true;
                    }
                }
            }
            if(!diamondOpType) {
                return null;
            }            
        }

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.MSG_ConvertibleToVarType(), new JavaFixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }

    /**
     * Fix for converting explicit type to 'var'
     *
     */
    private static final class JavaFixImpl extends JavaFix {

        public JavaFixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        @Messages("FIX_ShowMessage=Replace explicit type with var")
        protected String getText() {
            return Bundle.FIX_ShowMessage();
        }

        @Override
        protected void performRewrite(TransformationContext tc) throws Exception {

            WorkingCopy wc = tc.getWorkingCopy();
            TreePath statementPath = tc.getPath();
            TreeMaker make = wc.getTreeMaker();
              
            if (statementPath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                VariableTree oldVariableTree = (VariableTree) statementPath.getLeaf();
                ExpressionTree initializerTree = oldVariableTree.getInitializer();
                //replace diamond operator with type params
                if (initializerTree.getKind() == Tree.Kind.NEW_CLASS) {
                    NewClassTree nct = (NewClassTree)initializerTree;
                    if (nct.getIdentifier().getKind() == Tree.Kind.PARAMETERIZED_TYPE) {                        
                        if(oldVariableTree.getType().getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                            ParameterizedTypeTree ptt = (ParameterizedTypeTree) oldVariableTree.getType();
                            ParameterizedTypeTree nue = (ParameterizedTypeTree)nct.getIdentifier();
                            if(nue.getTypeArguments().isEmpty() && ptt.getTypeArguments().size() > 0) {
                                wc.rewrite(nue, ptt);
                            }                            
                        }    
                    }
                }
                VariableTree newVariableTree = make.Variable(
                        oldVariableTree.getModifiers(),
                        oldVariableTree.getName(),
                        make.Type("var"),
                        initializerTree
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
    private static boolean preConditionChecker(HintContext ctx) {

        CompilationInfo info = ctx.getInfo();

        // hint will be enable only for JDK-10 or above.
        if (info.getSourceVersion().compareTo(SourceVersion.RELEASE_9) < 1) {
            return false;
        }

        TreePath treePath = ctx.getPath();

        // variable should have local scope
        if (info.getTrees().getElement(treePath).getKind() != ElementKind.LOCAL_VARIABLE) {
            return false;
        }

        if (isDiagnosticCodeTobeSkipped(ctx.getInfo())) {
            return false;
        }

        //  hint is not applicable for  variable declaration where type is already 'var'
        return !info.getTreeUtilities().isVarType(treePath);
    }

    /**
     *
     * @param info : compilationInfo
     * @return true if Diagnostic Code is present in SKIPPED_ERROR_CODES
     */
    private static boolean isDiagnosticCodeTobeSkipped(CompilationInfo info) {
        List<Diagnostic> diagnosticsList = info.getDiagnostics();
        return diagnosticsList.stream().anyMatch((d) -> (SKIPPED_ERROR_CODES.contains(d.getCode())));
    }
}
