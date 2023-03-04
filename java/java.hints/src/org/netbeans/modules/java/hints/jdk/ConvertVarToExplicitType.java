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
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFix.TransformationContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle.Messages;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.StatementTree;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.modules.java.hints.suggestions.ExpandEnhancedForLoop;
import org.netbeans.spi.java.hints.TriggerPatterns;

/**
 * Hint to convert type in local variable declaration from 'var' to explicit
 * type
 *
 * @author rtaneja
 */
@Hint(displayName = "#DN_ConvertVarToExplicitType", description = "#DESC_ConvertVarToExplicitType", category = "rules15", severity = Severity.HINT, minSourceVersion = "10")
@Messages("MSG_ConvertibleToExplicitType=Convert var to explicit type")
public class ConvertVarToExplicitType {

    @TriggerPatterns({
        @TriggerPattern("$mods$ $type $var = $init"), //NOI18N
        @TriggerPattern("for ($type $var : $expression) { $stmts$; }") //NOI18N
    })
    public static ErrorDescription convertVarToExplicitType(HintContext ctx) {

        if (!isLocalVarType(ctx)) {
            return null;
        }
        TreePath treePath = ctx.getPath();
        if (ctx.getInfo().getTreeUtilities().hasError(treePath.getLeaf())) {
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
            } else if (statementPath.getLeaf().getKind() == Tree.Kind.ENHANCED_FOR_LOOP) {
                EnhancedForLoopTree elfTree = (EnhancedForLoopTree) statementPath.getLeaf();
                ExpressionTree expTree = elfTree.getExpression();
                VariableTree vtt = elfTree.getVariable();
                String elfTreeVariable = elfTree.getVariable().getType().toString();
                if (expTree == null) {
                    return;
                }
                //VariableTree with null ExpressionTree as no initialization required
                VariableTree newVariableTree = make.Variable(
                        vtt.getModifiers(),
                        vtt.getName(),
                        make.Type(elfTreeVariable),
                        null
                );
                StatementTree statement = ((EnhancedForLoopTree) statementPath.getLeaf()).getStatement();
                EnhancedForLoopTree newElfTree = make.EnhancedForLoop(newVariableTree, expTree, statement);
                wc.rewrite(elfTree, newElfTree);
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
        
        TreePath treePath = ctx.getPath();

        if (!isVariableValidForVarHint(ctx)) { 
            return false;
        }

        // variable declaration of type 'var'
        return info.getTreeUtilities().isVarType(treePath);
    }
    
    protected static boolean isVariableValidForVarHint(HintContext ctx) {
        CompilationInfo info = ctx.getInfo();
        TreePath treePath = ctx.getPath();
        // hint will be enable only for JDK-10 or above.
        if (info.getSourceVersion().compareTo(SourceVersion.RELEASE_9) < 1) {
            return false;
        }
         if (treePath.getLeaf().getKind() == Tree.Kind.ENHANCED_FOR_LOOP) {
            EnhancedForLoopTree efl = (EnhancedForLoopTree) treePath.getLeaf();
            TypeMirror expressionType = ctx.getInfo().getTrees().getTypeMirror(new TreePath(treePath, efl.getExpression()));
            if (!Utilities.isValidType(expressionType)) {
                return false;
            }
        } else {
            Element treePathELement = info.getTrees().getElement(treePath);
            // should be local variable
            if (treePathELement != null && (treePathELement.getKind() != ElementKind.LOCAL_VARIABLE && treePathELement.getKind() != ElementKind.RESOURCE_VARIABLE)) {
                return false;
            }
        }
        return true;
    }

    //filter anonymous class and intersection types
    private static boolean isValidType(HintContext ctx) {
        TreePath treePath = ctx.getPath();
        TreePath initTreePath = ctx.getVariables().get("$init");  //NOI18N
        TreePath expressionTreePath = ctx.getVariables().get("$expression"); //NOI18N
        if (initTreePath != null) {
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

            if (!Utilities.isValidType(variableTypeMirror) || (variableTypeMirror.getKind() == TypeKind.INTERSECTION)) {
                return false;
            }
            return true;
        } else if (expressionTreePath != null) {
            ExecutableElement iterator = ExpandEnhancedForLoop.findIterable(ctx.getInfo());
            return (iterator != null);
        } else {
            return false;
        }
    }
}
