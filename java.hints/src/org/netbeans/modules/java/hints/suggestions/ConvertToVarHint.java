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
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.List;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.queries.SourceLevelQuery;
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
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import com.sun.tools.javac.tree.JCTree;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Hint will convert explicit type to 'var'. Supported: JDK 10 or above
 *
 * @author arusinha
 */
public class ConvertToVarHint {

    private static final SpecificationVersion JDK_10 = new SpecificationVersion("10"); //NOI18N

    private final static Set<Tree.Kind> LITERALS_TYPE_SET = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    Tree.Kind.INT_LITERAL,
                    Tree.Kind.LONG_LITERAL,
                    Tree.Kind.FLOAT_LITERAL,
                    Tree.Kind.DOUBLE_LITERAL,
                    Tree.Kind.BOOLEAN_LITERAL,
                    Tree.Kind.CHAR_LITERAL,
                    Tree.Kind.STRING_LITERAL
            )));

    /**
     *
     * @param ctx : HintContext
     * @return ErrorDescription in case Object reference type and Object
     * instance type are same.
     */
    @Hint(displayName = "#DN_CanUseVarForObjectRef", description = "#DESC_CanUseVarForObjectRef", category = "suggestions") //NOI18N

    @TriggerPattern("$mods$ $type $var =new $type($params$);") //NOI18N
    @Messages("MSG_ConvertibleToVarType=Explict type can be replaced with 'var'")  //NOI18N
    public static ErrorDescription checkNewObjInit(HintContext ctx) {

        TreePath treePath = ctx.getVariables().get("$var");

        if (treePath == null) {
            return null;
        }

        if (!isHintEnabled(ctx)) {
            return null;
        }

        Fix fix = new FixImpl(ctx.getInfo(), treePath).toEditorFix();
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.MSG_ConvertibleToVarType(), fix);

    }

    /**
     *
     * @param ctx : HintContext
     * @return ErrorDescription if variable initializer is a Literal
     */
    @Hint(displayName = "#DN_CanUseVarForLiteralRef", description = "#DESC_CanUseVarForLiteralRef", category = "suggestions")
    @TriggerPattern("$mods$ $type $var = $value;") //NOI18N

    public static ErrorDescription checkLiteralInit(HintContext ctx) {

        TreePath initPath = ctx.getVariables().get("$value");

        if (initPath == null) {
            return null;
        }

        Tree l = initPath.getLeaf();

        // checks rhs is a literal or not
        if (!(isLiteral(l))) {
            return null;
        }

        if (!isHintEnabled(ctx)) {
            return null;
        }

        Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix();
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.MSG_ConvertibleToVarType(), fix);

    }

    /**
     *
     * @param ctx : HintContext
     * @return ErrorDescription if variable initializer is Lambda expression.
     */
    @Hint(displayName = "#DN_CanUseVarForLambdaExpRef", description = "#DESC_CanUseVarForLambdaExpRef", category = "suggestions")
    @TriggerTreeKind(Kind.LAMBDA_EXPRESSION)
    public static ErrorDescription checkLambdaExpr(HintContext ctx) {
        TypeMirror samType = ctx.getInfo().getTrees().getTypeMirror(ctx.getPath());

        if (!isHintEnabled(ctx)) {
            return null;
        }

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.MSG_ConvertibleToVarType(), new LambdaExprFixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }

    /**
     *
     * @param ctx : HintContext
     * @return ErrorDescription if variable initializer is Anonymous Object.
     */
    @Hint(displayName = "#DN_CanUseVarForAnonymousObjRef", description = "#DESC_CanUseVarForAnonymousObjRef", category = "suggestions")

    @TriggerPattern("new $clazz($params$) { $method; }")  //NOI18N

    public static ErrorDescription checkAnonymousObj(HintContext ctx) {

        if (!isHintEnabled(ctx)) {
            return null;
        }

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.MSG_ConvertibleToVarType(), new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }

    /**
     *
     * @param ctx : HintContext
     * @return ErrorDescription if variable initializer Object uses diamond
     * operator(<>).
     */
    @Hint(displayName = "#DN_CanUseVarForDiamondInterfaceRef", description = "#DESC_CanUseVarForDiamondInterfaceRef", category = "suggestions")
    @TriggerPattern("new $type<$T$>($params$)") //NOI18N
    public static ErrorDescription checkdiamondType(HintContext ctx) {

        TreePath treePath = ctx.getPath();

        if (!isHintEnabled(ctx)) {
            return null;
        }

        VariableTree oldVariableTree = (VariableTree) treePath.getParentPath().getLeaf();
        Tree type = oldVariableTree.getType();

        if (!(type instanceof ParameterizedTypeTree)) {
            return null;
        } else {
            ParameterizedTypeTree paramTypeTree = (ParameterizedTypeTree) type;
            NewClassTree node = (NewClassTree) treePath.getLeaf();

            if (node.getIdentifier() instanceof JCTree.JCTypeApply) {
                JCTree.JCTypeApply identifier = ((JCTree.JCTypeApply) node.getIdentifier());
                int diamondParamsSize = identifier.getTypeArguments().size();

                //Hint is disabled if  variable initializer Object does not  uses diamond operator(<>) and Oject reference type doesn't matches with Object instance type 
                if (diamondParamsSize != 0 || !identifier.getType().toString().equals(paramTypeTree.getType().toString()) || paramTypeTree.getTypeArguments().size() == 0) {
                    return null;
                }
                return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.MSG_ConvertibleToVarType(), new DiamondInterface2VarFixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());

            }
        }
        return null;
    }

    /**
     *
     * @param ctx : HintContext
     * @return ErrorDescription if variable initializer type is Array.
     */
    @Hint(displayName = "#DN_CanUseVarForArrayRef", description = "#DESC_CanUseVarForArrayRef", category = "suggestions")
    @TriggerTreeKind(Kind.NEW_ARRAY)
    public static ErrorDescription checkNewArrayinit(HintContext ctx) {
        TypeMirror samType = ctx.getInfo().getTrees().getTypeMirror(ctx.getPath());

        if (!isHintEnabled(ctx)) {
            return null;
        }

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.MSG_ConvertibleToVarType(), new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }

    /**
     * Fix for converting explicit type to 'var' is case initializer Object uses
     * diamond operator.
     */
    private static final class DiamondInterface2VarFixImpl extends JavaFix {

        public DiamondInterface2VarFixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        @Messages("FIX_ShowMessage=Replace explicit type with var")
        protected String getText() {
            return Bundle.FIX_ShowMessage();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy copy = ctx.getWorkingCopy();

            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath statementPath = ctx.getPath();

            // TreePath blockPath = ctx.getPath().getParentPath().getParentPath();
            if (statementPath.getLeaf().getKind() == Tree.Kind.NEW_CLASS) {

                TreeMaker make = copy.getTreeMaker();
                VariableTree oldVariableTree = (VariableTree) statementPath.getParentPath().getLeaf();

                ParameterizedTypeTree varType = (ParameterizedTypeTree) oldVariableTree.getType();
                NewClassTree node = (NewClassTree) oldVariableTree.getInitializer();

                List<? extends ExpressionTree> args = node.getArguments();
                List<? extends ExpressionTree> typeArgs = (List<? extends ExpressionTree>) node.getTypeArguments();

                ExpressionTree newInitTree = make.NewClass(node.getEnclosingExpression(), typeArgs, (ExpressionTree) varType, args, node.getClassBody());
                VariableTree newVariableTree = make.Variable(
                        oldVariableTree.getModifiers(),
                        oldVariableTree.getName(),
                        make.Type("var"),
                        newInitTree
                );

                ctx.getWorkingCopy().rewrite(oldVariableTree, ctx.getWorkingCopy().resolveRewriteTarget(newVariableTree));

            }

        }

    }

    /**
     * Fix for converting explicit type to 'var' is case initializer is Literal
     * or of Object type.
     *
     */
    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_ShowMessage();
        }

        @Override
        protected void performRewrite(TransformationContext tc) throws Exception {

            WorkingCopy wc = tc.getWorkingCopy();
            TreePath statementPath = tc.getPath();
            TreeMaker make = wc.getTreeMaker();
            VariableTree oldVariableTree = null;

            if (statementPath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                oldVariableTree = (VariableTree) statementPath.getLeaf();
            } else if (statementPath.getParentPath().getLeaf().getKind() == Tree.Kind.VARIABLE) {
                oldVariableTree = (VariableTree) statementPath.getParentPath().getLeaf();
            }

            VariableTree newVariableTree = make.Variable(
                    oldVariableTree.getModifiers(),
                    oldVariableTree.getName(),
                    make.Type("var"),
                    oldVariableTree.getInitializer()
            );

            tc.getWorkingCopy().rewrite(oldVariableTree, tc.getWorkingCopy().resolveRewriteTarget(newVariableTree));

        }

    }

    /**
     * Fix for converting explicit type to 'var' is case initializer is Lambda
     * expression.
     *
     */
    private static final class LambdaExprFixImpl extends JavaFix {

        public LambdaExprFixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_ShowMessage();
        }

        @Override
        protected void performRewrite(TransformationContext tc) throws Exception {
            WorkingCopy wc = tc.getWorkingCopy();
            TreePath statementPath = tc.getPath();
            TreeMaker make = wc.getTreeMaker();
            VariableTree oldVariableTree = null;

            // check whether statement is of type variable declara
            if (statementPath.getParentPath().getLeaf().getKind() == Tree.Kind.VARIABLE) {
                oldVariableTree = (VariableTree) statementPath.getParentPath().getLeaf();
            }
            if (oldVariableTree == null) {
                return;
            }

            ExpressionTree init = oldVariableTree.getInitializer();
            ExpressionTree cast = make.TypeCast(oldVariableTree.getType(), init);

            VariableTree newVariableTree = make.Variable(
                    oldVariableTree.getModifiers(),
                    oldVariableTree.getName(),
                    make.Type("var"),
                    cast);

            tc.getWorkingCopy().rewrite(oldVariableTree, newVariableTree);

        }

    }

    /**
     *
     * @param file : Fileobject
     * @return true if Fileobject source version is JDK -10 or above.
     */
    private static boolean isSupportedSourceLevel(final FileObject file) {
        if (file == null) {
            return false;
        }
        final String sl = SourceLevelQuery.getSourceLevel(file);
        if (sl == null) {
            return false;
        }
        return (JDK_10.compareTo(new SpecificationVersion(sl)) <= 0);
    }

    /**
     *
     * @param t : Tree
     * @return true is tree is of Literal type.
     */
    private static boolean isLiteral(Tree t) {

        return t != null && (LITERALS_TYPE_SET.contains(t.getKind()));
    }

    /**
     * *
     *
     * @param TreePath : treePath
     * @return true if variable tree is in block scope (variable is in local
     * scope)
     */
    private static boolean isInsideBlock(TreePath treePath) {
        if (treePath == null) {
            return false;
        }

//      hint will work only inside a block
        TreePath statementPath = null;
        TreePath blockPath = treePath.getParentPath();
        while (!(blockPath.getLeaf() instanceof BlockTree)) {
            statementPath = blockPath;
            blockPath = blockPath.getParentPath();
            if (blockPath == null) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param ctx : HintContext
     * @return true if conditions for hint to be enable is meet
     */
    private static boolean isHintEnabled(HintContext ctx) {

        // var should reside inside a block( statement should have local scope)
        if (ctx.getPath() == null || !isInsideBlock(ctx.getPath())) {
            return false;
        }

        // hint is enable for JDK-10 or above.
        FileObject fo = ctx.getInfo().getFileObject();
        if (!isSupportedSourceLevel(fo)) {
            return false;
        }

        TreePath treePath = ctx.getPath();

        // hint will be enabled only for variable tree.
        Tree variableTree = treePath.getLeaf().getKind() == Tree.Kind.VARIABLE ? treePath.getLeaf() : treePath.getParentPath().getLeaf().getKind() == Tree.Kind.VARIABLE ? treePath.getParentPath().getLeaf() : null;
        if (variableTree == null) {
            return false;
        }

        //  hint is not applicable for  variable declaration where type is already 'var'
        if (ctx.getInfo().getTreeUtilities().isVarKeywordAvailable(variableTree)) {
            return false;
        }

        return true;

    }
}
