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

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.List;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.bugs.NPECheck;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.suggestions.ExpectedTypeResolver;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.UseOptions;
import org.openide.util.NbBundle;

/**
 * Inspects boxing operations. Most of them are unnecessary under JDK5+. Whether a node can be left unboxed 
 * depends mainly on the containing expressions: 
 * - Method call must be re-attributed to check whether it does not resolve to a different overload. 
 * - in conditional, the other branch must be checked to produce a (boxed) primitive as well.
 * - in binary expressions, the other side 
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - name of boxing class",
    "TEXT_UnnecessaryBoxing=Unnecessary boxing to {0}",
    "# {0} - name of boxing class",
    "FIX_UnnecessaryBoxing1=Remove new {0}",
    "# {0} - name of boxing class",
    "FIX_UnnecessaryBoxing2=Remove {0}.valueOf",
    "# {0} - name of primitive class",
    "FIX_ChangeBoxingToTypecast=Cast to {0} instead of boxing"
})
public class UnnecessaryBoxing {
    
    private static final boolean DEFAULT_PREFER_CAST_TO_BOXING = false;
    
    @BooleanOption(
        displayName = "#OPTNAME_PreferCastsToBoxing",
        tooltip = "#OPTDESC_PreferCastsToBoxing",
        defaultValue = DEFAULT_PREFER_CAST_TO_BOXING
    )
    static final String PREFER_CAST_TO_BOXING = "boxing.prefer.cast"; // NOI18N
    
    @TriggerPatterns({
        @TriggerPattern(value = "new java.lang.Byte($v)"),
        @TriggerPattern(value = "new java.lang.Character($v)"),
        @TriggerPattern(value = "new java.lang.Double($v)"),
        @TriggerPattern(value = "new java.lang.Float($v)"),
        @TriggerPattern(value = "new java.lang.Integer($v)"),
        @TriggerPattern(value = "new java.lang.Long($v)"),
        @TriggerPattern(value = "new java.lang.Short($v)"),
        @TriggerPattern(value = "new java.lang.Boolean($v)"),

        @TriggerPattern(value = "java.lang.Byte.valueOf($v)"),
        @TriggerPattern(value = "java.lang.Character.valueOf($v)"),
        @TriggerPattern(value = "java.lang.Double.valueOf($v)"),
        @TriggerPattern(value = "java.lang.Float.valueOf($v)"),
        @TriggerPattern(value = "java.lang.Integer.valueOf($v)"),
        @TriggerPattern(value = "java.lang.Long.valueOf($v)"),
        @TriggerPattern(value = "java.lang.Short.valueOf($v)"),
        @TriggerPattern(value = "java.lang.Boolean.valueOf($v)")
    })
    @Hint(
            displayName = "#DN_UnnecessaryBoxing",
            description = "#DESC_UnnecessaryBoxing",
            category = "rules15",
            enabled = true,
            suppressWarnings = "UnnecessaryBoxing",
            minSourceVersion = "5"
    )
    @UseOptions(PREFER_CAST_TO_BOXING)
    public static ErrorDescription run(HintContext ctx) {
        CompilationInfo ci = ctx.getInfo();
        TreePath p = ctx.getPath();
        
        TreePath vp = ctx.getVariables().get("$v"); // NOI18N
        TypeMirror exprType = ci.getTrees().getTypeMirror(vp);
        if (exprType == null || !exprType.getKind().isPrimitive()) {
            return null;
        }
        
        TreePath expr = ctx.getPath().getParentPath();
        Tree prev = ctx.getPath().getLeaf();
        while (expr != null && expr.getLeaf().getKind() == Tree.Kind.PARENTHESIZED) {
            prev = expr.getLeaf();
            expr = expr.getParentPath();
        }
        if (expr == null) {
            return null;
        }
        boolean ok = true;
        Tree.Kind k = expr.getLeaf().getKind();
        if (k == Tree.Kind.METHOD_INVOCATION) {
            ok = checkMethodInvocation(ctx, expr, vp);
        } else if (k == Tree.Kind.NEW_CLASS) {
            ok = checkMethodInvocation(ctx, expr, vp);
        } else if (k == Tree.Kind.INSTANCE_OF) {
            return null;
        } else if (k == Tree.Kind.CONDITIONAL_EXPRESSION) {
            ok = checkConditional(ci, expr, prev);
        } else if (k == Tree.Kind.MEMBER_SELECT) {
            return null;
        } else if (BinaryTree.class.isAssignableFrom(k.asInterface())) {
            ok = checkBinaryOp(ci, expr, prev);
        } else if (AssignmentTree.class.isAssignableFrom(k.asInterface())) {
            AssignmentTree as = (AssignmentTree)expr.getLeaf();
            TypeMirror m = ci.getTrees().getTypeMirror(new TreePath(expr, as.getVariable()));
            ok = m != null && ci.getTypes().isAssignable(exprType, m);
        }
        if (!ok) {
            return null;
        }
        
        TypeMirror boxedType = ci.getTrees().getTypeMirror(ctx.getPath());
        if (boxedType == null || boxedType.getKind() != TypeKind.DECLARED) {
            return null;
        }
        String tname = ci.getTypeUtilities().getTypeName(boxedType).toString();
        TypeMirror valType = ci.getTrees().getTypeMirror(vp);
        TypeMirror unboxedResult = Utilities.unboxIfNecessary(ci, boxedType);
        if (unboxedResult == null) {
            return null;
        }
        TypeKind rk = unboxedResult.getKind();
        if (ci.getTypes().isSameType(valType, unboxedResult)) {
            rk = null;
        }
        
        String text;
        if (rk != null) {
            boolean preferCast = ctx.getPreferences().getBoolean(PREFER_CAST_TO_BOXING, DEFAULT_PREFER_CAST_TO_BOXING);
            if (!preferCast) {
                return null;
            }
            text = Bundle.FIX_ChangeBoxingToTypecast(ci.getTypeUtilities().getTypeName(valType));
        } else if (p.getLeaf().getKind() == Tree.Kind.NEW_CLASS) {
            text = Bundle.FIX_UnnecessaryBoxing1(tname);
        } else {
            text = Bundle.FIX_UnnecessaryBoxing2(tname);
        }
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(),
                Bundle.TEXT_UnnecessaryBoxing(tname), 
                new RemoveBoxingFix(
                    TreePathHandle.create(vp, ci),
                    text,
                    TreePathHandle.create(ctx.getPath(), ci),
                    rk).toEditorFix());
    }
    
    private static boolean checkBinaryOp(CompilationInfo ci, TreePath expr, Tree prev) {
        BinaryTree bt = (BinaryTree)expr.getLeaf();
        Tree other = prev == bt.getLeftOperand() ? bt.getRightOperand() : bt.getLeftOperand();
        Boolean b = checkTwoArguments(ci, expr, other, prev);
        if (Boolean.TRUE == b) {
            return true;
        }
        if (b == null) {
            return false;
        }
        TypeMirror tm  = ci.getTrees().getTypeMirror(new TreePath(expr, other));
        if (tm != null && tm.getKind() == TypeKind.DECLARED) {
            Element el = ((DeclaredType)tm).asElement();
            if (el != null && el.getKind() == ElementKind.CLASS) {
                return ((TypeElement)el).getQualifiedName().contentEquals("java.lang.String"); // NOI18N
            }
        }
        return false;
    }
    
    private static Boolean checkTwoArguments(CompilationInfo ci, TreePath expr, Tree other, Tree prev) {
        if (other == null || prev == null) {
            return null;
        }
        TreePath otherPath = new TreePath(expr, other);
        TreePath prevPath = new TreePath(expr, prev);
        
        TypeMirror pt = Utilities.unboxIfNecessary(ci, ci.getTrees().getTypeMirror(prevPath)); // assume boxed
        TypeMirror ot = Utilities.unboxIfNecessary(ci, ci.getTrees().getTypeMirror(otherPath));
        if (!(Utilities.isValidType(pt) && Utilities.isValidType(ot))) {
            return null;
        }
        ExpectedTypeResolver res = new ExpectedTypeResolver(expr, prevPath, ci);
        List<? extends TypeMirror> types = res.scan(expr, null);
        if (types == null) {
            return null;
        }
        for (TypeMirror m : types) {
            if (ci.getTypes().isAssignable(pt, m)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks whether the other branch of the conditional has a matching type. If the prev expression
     * is the conditional's expression, it's OK.
     * 
     * @param ci context
     * @param expr the conditional expression
     * @param prev the parameter containing the boxing
     * @return true, if it is OK to leave out the boxing
     */
    private static boolean checkConditional(CompilationInfo ci, TreePath expr, Tree prev) {
        ConditionalExpressionTree ct = (ConditionalExpressionTree)expr.getLeaf();
        if (ct.getCondition() == prev) {
            return true;
        }
        TreePath prevPath = new TreePath(expr, prev);
        TypeMirror boxedPrev = ci.getTrees().getTypeMirror(prevPath);
        TypeMirror pt = Utilities.unboxIfNecessary(ci, boxedPrev); // assume boxed
        if (!Utilities.isValidType(pt)) {
            return false;
        }
        ExpectedTypeResolver res = new ExpectedTypeResolver(expr, prevPath, ci);
        List<? extends TypeMirror> types = res.scan(expr, null);
        if (types == null) {
            // cannot determine the type -> no hint, probably an error
            return false;
        }
        for (TypeMirror m : types) {
            if (!m.getKind().isPrimitive() && !Utilities.isPrimitiveWrapperType(m)) {
                return false;
            }
            m = Utilities.unboxIfNecessary(ci, m);
            if (ci.getTypes().isAssignable(pt, m)) {
                // special case, see issue #269269; if the OTHER argument of the conditional
                // is a primitive wrapper AND it is _not_ known to contain non-null, do not produce unboxing warning
                // as both boxed types prevent cond.op. to unbox.
                TreePath other = new TreePath(expr, 
                        prev == ct.getTrueExpression() ? ct.getFalseExpression() : ct.getTrueExpression());
                TypeMirror m2 = ci.getTrees().getTypeMirror(other);
                if (!Utilities.isValidType(m2)) {
                    continue;
                }
                if (NPECheck.isSafeToDereference(ci, other)) {
                    return true;
                }
                if (!Utilities.isPrimitiveWrapperType(m2) ||
                        ci.getTypes().isSameType(boxedPrev, m2)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean checkMethodInvocation(HintContext ctx, TreePath invPath, TreePath valPath) {
        Trees trees = ctx.getInfo().getTrees();
        Tree invLeaf = invPath.getLeaf();
        List<? extends ExpressionTree> arguments;
        TypeMirror m;
        
        switch (invLeaf.getKind()) {
            case METHOD_INVOCATION: {
                MethodInvocationTree mit = (MethodInvocationTree)invLeaf;
                arguments = mit.getArguments();
                m = trees.getTypeMirror(new TreePath(invPath, mit.getMethodSelect()));
                break;
            }
            case NEW_CLASS: {
                NewClassTree nct = (NewClassTree)invLeaf;
                arguments = nct.getArguments();
                Element e = trees.getElement(invPath);
                TypeMirror cl = trees.getTypeMirror(invPath);
                if (!Utilities.isValidType(cl) || cl.getKind().isPrimitive()) {
                    return false;
                }
                m = ctx.getInfo().getTypes().asMemberOf((DeclaredType)cl, e);
                break;
            }
            default:
                return false;
        }
        
        if (!Utilities.isValidType(m) || m.getKind() != TypeKind.EXECUTABLE) {
            return false;
        }
        ExecutableType execType = (ExecutableType)m;
        int idx = arguments.indexOf(ctx.getPath().getLeaf());
        if (idx < 0 || idx >= execType.getParameterTypes().size()) {
            return false;
        }
        TypeMirror paramType = execType.getParameterTypes().get(idx);
        TypeMirror curType = trees.getTypeMirror(ctx.getPath());
        TypeMirror valType = trees.getTypeMirror(valPath);
        
        if (!paramType.getKind().isPrimitive() && valType.getKind().isPrimitive()) {
            valType = ctx.getInfo().getTypes().boxedClass((PrimitiveType)valType).asType();
            // ensure that the passed INSTANCE type will not change when the boxing is removed
            if (!ctx.getInfo().getTypes().isSameType(curType, valType)) {
                return false;
            }
        }
                
        return Utilities.checkAlternativeInvocation(ctx.getInfo(), invPath, ctx.getPath(), valPath, null);
    }
    
    /**
     * Since removing the boxing operator will change the type from reference to primitive,
     * another method overload could possibly resolve for that expression. This check will stringify the 
     * current expression, parse it again and check whether the method invocation still resolves AND
     * is the same as the original one.
     * 
     * 
     * @param ctx
     * @param invPath
     * @return true, if the method resolves and resolves to the same method as before the transformation.
     */
    private static boolean checkCommonInvocation(HintContext ctx, TreePath invPath, Tree sel, TreePath valPath) {
        CompilationInfo ci = ctx.getInfo();
        CharSequence source = ci.getSnapshot().getText();
        Element e = ci.getTrees().getElement(invPath);
        if (!(e instanceof ExecutableElement)) {
            return false;
        }
        SourcePositions sp = ci.getTrees().getSourcePositions();
        
        int invOffset = (int)sp.getEndPosition(ci.getCompilationUnit(), sel) - 1;
        int origExpStart = (int)sp.getStartPosition(ci.getCompilationUnit(), ctx.getPath().getLeaf());
        int origExpEnd = (int)sp.getEndPosition(ci.getCompilationUnit(), ctx.getPath().getLeaf());
        
        TreePath exp = invPath;
        boolean statement = false;
        
        // try to minimize the parsed content: find the nearest expression that breaks the type inference,
        // typically break if the method is contained within a condition of a switch/if/loop.
        out: do {
            boolean breakPrev = false;
            TreePath previousPath = exp;
            Tree previous = exp.getLeaf();
            exp = exp.getParentPath();
            Tree t = exp.getLeaf();
            Class c = t.getKind().asInterface();
            if (c == CompoundAssignmentTree.class ||
                c == AssignmentTree.class) {
                break;
            }
            switch (t.getKind()) {
                case CONDITIONAL_EXPRESSION: {
                    // if the tree is the condition part, then we're done and the result is a boolean.
                    ConditionalExpressionTree ctree = (ConditionalExpressionTree)t;
                    if (ctree.getCondition() == previous) {
                        breakPrev = true;
                    }
                    break;
                }
                case DO_WHILE_LOOP: {
                    DoWhileLoopTree dlp = (DoWhileLoopTree)t;
                    if (dlp.getCondition() == previous) {
                        breakPrev = true;
                    }
                    break;
                }
                case FOR_LOOP: {
                    ForLoopTree flp =(ForLoopTree)t;
                    if (previous == flp.getCondition()) {
                        breakPrev = true;
                    }
                    break;
                }
                    
                case ENHANCED_FOR_LOOP: {
                    EnhancedForLoopTree eflp = (EnhancedForLoopTree)t;
                    if (previous == eflp.getExpression()) {
                        breakPrev = true;
                    }
                    break;
                }
                case SWITCH: {
                    SwitchTree st = (SwitchTree)t;
                    if (previous == st.getExpression()) {
                        breakPrev = true;
                    }
                    break;
                }
                case SYNCHRONIZED: {
                    SynchronizedTree st = (SynchronizedTree)t;
                    if (previous == st.getExpression()) {
                        breakPrev = true;
                    }
                    break;
                }
                case WHILE_LOOP: {
                    WhileLoopTree wlt = (WhileLoopTree)t;
                    if (previous == wlt.getCondition()) {
                        breakPrev = true;
                    }
                    break;
                }
                case IF: {
                    IfTree it = (IfTree)t;
                    if (previous == it.getCondition()) {
                        breakPrev = true;
                    }
                    break;
                }
            }
            if (breakPrev) {
                exp = previousPath;
                break;
            }
            if (StatementTree.class.isAssignableFrom(c)) {
                statement = true;
                break;
            }
        } while (exp.getParentPath()!= null);
        TreePath stPath = exp;
        if (!statement) {
            while (stPath != null && !(stPath.getLeaf() instanceof StatementTree)) {
                stPath = stPath.getParentPath();
            }
        }
        if (stPath == null) {
            return false;
        }
        
        int baseIndex = (int)sp.getStartPosition(ci.getCompilationUnit(), exp.getLeaf());
        StringBuilder sb = new StringBuilder();
        sb.append(source.subSequence(
                baseIndex,
                origExpStart));
        // instead of the boxing expression, append only the value expression, in parenthesis
        sb.append("("). // NOI18N
            append(source.subSequence(
                (int)sp.getStartPosition(ci.getCompilationUnit(), valPath.getLeaf()),
                (int)sp.getEndPosition(ci.getCompilationUnit(), valPath.getLeaf()))).
            append(")"); // NOI18N
        
        sb.append(source.subSequence(
                origExpEnd,
                (int)sp.getEndPosition(ci.getCompilationUnit(), exp.getLeaf())));
        
        SourcePositions[] nsp = new SourcePositions[1];
        Tree t;
        if (statement) {
            sb.append(";"); // NOI18N
            t = ci.getTreeUtilities().parseStatement(sb.toString(), nsp);
        } else {
            t = ci.getTreeUtilities().parseExpression(sb.toString(), nsp);
        }
        
        Scope s = ci.getTreeUtilities().scopeFor((int)sp.getStartPosition(ci.getCompilationUnit(), exp.getLeaf()) - 1);
        ci.getTreeUtilities().attributeTree(t, s);
        
        TreePath newPath = new TreePath(exp.getParentPath(), t);
        // path for the method invocation within the newly formed expression or statement.
        // the +1 ensures that we are inside the method invocation subtree (method has >= 1 char as ident)
        TreePath newInvPath = ci.getTreeUtilities().pathFor(newPath, invOffset - baseIndex + 1, nsp[0]);
        while (newInvPath != null && newInvPath.getLeaf().getKind() != Tree.Kind.METHOD_INVOCATION) {
            newInvPath = newInvPath.getParentPath();
        }
        if (newInvPath == null) {
            return false;
        }
        Element me = ci.getTrees().getElement(newInvPath);
        Element origEl = ci.getTrees().getElement(invPath);
        return me == origEl;
    }
    
    private static class RemoveBoxingFix extends JavaFix {
        final TreePathHandle valHandle;
        final String msg;
        final TypeKind primitiveKind;

        public RemoveBoxingFix(TreePathHandle valHandle, String msg, TreePathHandle handle, TypeKind primitiveKind) {
            super(handle);
            this.valHandle = valHandle;
            this.msg = msg;
            this.primitiveKind = primitiveKind;
        }

        @Override
        protected String getText() {
            return msg;
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath val = valHandle.resolve(wc);
            
            if (primitiveKind == null || !primitiveKind.isPrimitive()) {
                wc.rewrite(ctx.getPath().getLeaf(), val.getLeaf());
            } else {
                TreeMaker mk = wc.getTreeMaker();
                wc.rewrite(ctx.getPath().getLeaf(), 
                        mk.TypeCast(
                            mk.Type(wc.getTypes().getPrimitiveType(primitiveKind)), (ExpressionTree)val.getLeaf()));
            }
        }
        
    }
}
