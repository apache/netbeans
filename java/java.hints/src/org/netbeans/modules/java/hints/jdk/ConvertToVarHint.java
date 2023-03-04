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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFix.TransformationContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle.Messages;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.java.hints.errors.Utilities;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import org.netbeans.modules.java.hints.suggestions.ExpandEnhancedForLoop;
import org.netbeans.spi.java.hints.TriggerPatterns;

/**
 * Hint will convert explicit type of local variable to 'var'. Supported: JDK 10
 * or above.
 *
 * @author arusinha
 */
@Hint(displayName = "#DN_CanUseVarForExplicitType", description = "#DESC_CanUseVarForExplicitType", category = "rules15", severity = Severity.HINT, minSourceVersion = "10") //NOI18N
@Messages("MSG_ConvertibleToVarType=Explict type can be replaced with 'var'")  //NOI18N  
public class ConvertToVarHint {

    // hint will be disabled for error codes present in SKIPPED_ERROR_CODES.
    private static final String[] SKIPPED_ERROR_CODES = {
        "compiler.err.generic.array.creation" //NOI18N
    };

    @TriggerPatterns({
        @TriggerPattern("$mods$ $type $var = $init"), //NOI18N
        @TriggerPattern("for ($type $var : $expression) { $stmts$; }") //NOI18N
    })
    public static ErrorDescription computeExplicitToVarType(HintContext ctx) {
        if (!preConditionChecker(ctx)) {
            return null;
        }

        if(!isValidVarType(ctx)) {
            return null;
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
                if(initializerTree == null) {
                    return;
                }
                //check if initializer with diamond operator
                if (initializerTree.getKind() == Tree.Kind.NEW_CLASS) {
                    NewClassTree nct = (NewClassTree)initializerTree;
                    if (nct.getIdentifier().getKind() == Tree.Kind.PARAMETERIZED_TYPE) {                        
                        if(oldVariableTree.getType().getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                            ParameterizedTypeTree ptt = (ParameterizedTypeTree) oldVariableTree.getType();
                            ParameterizedTypeTree nue = (ParameterizedTypeTree)nct.getIdentifier();
                            if(nue.getTypeArguments().isEmpty() && ptt.getTypeArguments().size() > 0) {
                                //replace diamond operator with type params from lhs
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
            } else if (statementPath.getLeaf().getKind() == Tree.Kind.ENHANCED_FOR_LOOP) {
                EnhancedForLoopTree elfTree = (EnhancedForLoopTree) statementPath.getLeaf();
                ExpressionTree expTree = elfTree.getExpression();
                VariableTree vtt = elfTree.getVariable();
                if (expTree == null || vtt == null) {
                    return;
                }
                VariableTree newVariableTree = make.Variable(
                        vtt.getModifiers(),
                        vtt.getName(),
                        make.Type("var"),
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
    private static boolean preConditionChecker(HintContext ctx) {

        CompilationInfo info = ctx.getInfo();

        TreePath treePath = ctx.getPath();

        if (!ConvertVarToExplicitType.isVariableValidForVarHint(ctx)) {
            return false;
        }

        if (ctx.getInfo().getTreeUtilities().hasError(treePath.getLeaf(), SKIPPED_ERROR_CODES)) {
            return false;
        }

        // hint is not applicable for compound variable declaration.
        if (info.getTreeUtilities().isPartOfCompoundVariableDeclaration(treePath.getLeaf()))
            return false;

        //  hint is not applicable for  variable declaration where type is already 'var'
        return !info.getTreeUtilities().isVarType(treePath);
    }
    
    private static boolean isValidVarType(HintContext ctx) {
        TreePath treePath = ctx.getPath();
        TreePath initTreePath = ctx.getVariables().get("$init");  //NOI18N
        TreePath expressionTreePath = ctx.getVariables().get("$expression"); //NOI18N
        TreePath typeTreePath = ctx.getVariables().get("$type"); //NOI18N
        if (initTreePath != null) {
            Tree.Kind kind = initTreePath.getLeaf().getKind();
            switch (kind) {
                case NEW_CLASS:
                    NewClassTree nct = (NewClassTree) (initTreePath.getLeaf());
                    //anonymous class type
                    if (nct.getClassBody() != null) {
                        return false;
                    }
                    break;
                case NEW_ARRAY:
                    NewArrayTree nat = (NewArrayTree) ((VariableTree) treePath.getLeaf()).getInitializer();
                    //array initializer expr type
                    if (nat.getType() == null) {
                        return false;
                    }
                    break;
                case LAMBDA_EXPRESSION:
                    return false;
                case MEMBER_REFERENCE:
                    return false;
                default:
                    break;
            }
            // variable initializer type should be same as variable type.
            TypeMirror initTypeMirror = ctx.getInfo().getTrees().getTypeMirror(initTreePath);
            TypeMirror variableTypeMirror = ctx.getInfo().getTrees().getElement(treePath).asType();
            if ((!Utilities.isValidType(initTypeMirror)) || (!ctx.getInfo().getTypes().isSameType(variableTypeMirror, Utilities.resolveCapturedType(ctx.getInfo(), initTypeMirror)))) {
                return false;
            }
            return true;
        } else if (expressionTreePath != null) {
            ExecutableElement iterator = ExpandEnhancedForLoop.findIterable(ctx.getInfo());
            TypeMirror expTypeMirror = ctx.getInfo().getTrees().getTypeMirror(expressionTreePath);
            TypeMirror typeTypeMirror = ctx.getInfo().getTrees().getTypeMirror(typeTreePath);
            if (expTypeMirror.getKind() == TypeKind.DECLARED) {
                DeclaredType dt = (DeclaredType) expTypeMirror;
                if (dt.getTypeArguments().size() > 0) {
                    TypeMirror paramType = dt.getTypeArguments().get(0);
                    if ((!Utilities.isValidType(typeTypeMirror)) || (!ctx.getInfo().getTypes().isSameType(typeTypeMirror, paramType))) {
                        return false;
                    }
                }
            } else {
                ArrayType arrayTypeExp = (ArrayType) Utilities.resolveCapturedType(ctx.getInfo(), expTypeMirror);
                Type arrayTypeExpType = arrayTypeExp.getComponentType();
                if ((!Utilities.isValidType(typeTypeMirror)) || (!ctx.getInfo().getTypes().isSameType(typeTypeMirror, arrayTypeExpType))) {
                    return false;
                }
            }
            return (iterator != null);
        } else {
            return false;
        }
    }
}
