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
package org.netbeans.modules.java.hints.perf;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.ArithmeticUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 * Detects StringBuffer/StrinBuilder variables that are accessed just as ordinary Strings.
 * Theoretically, it would be better to use Flow to trace usages of the original variable, or even to get the initial
 * assignments, if definitely initialized/not reassigned. But Flow skips effectively dead branches, so some usages
 * of the declared variable may be skipped. If Flow is to be used, a visitor that scans entire subtree in an attempt
 * to find inappropriate direct usage must be run anyway.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "TEXT_ReplaceStringBufferByString=Replace StringBuffer/Builder by String",
    "FIX_ReplaceStringBufferByString=Replace by String"
})
@Hint(
    displayName = "#DN_ReplaceBufferByString",
    description = "#DESC_ReplaceBufferByString",
    category = "performance",
    enabled = true,
    suppressWarnings = { "ReplaceStringBufferByString", "", "StringBufferReplaceableByString" }
)
public class ReplaceBufferByString {
    @TriggerPatterns({
        @TriggerPattern(value = "java.lang.StringBuffer $x = $expr;"),
        @TriggerPattern(value = "java.lang.StringBuilder $x = $expr;"),
    })
    public static ErrorDescription checkReplace(HintContext ctx) {
        CompilationInfo ci = ctx.getInfo();
        TreePath vp = ctx.getVariables().get("$x"); // NOI18N
        TreePath initP = ctx.getVariables().get("$expr"); // NOI18N
        Element el = ci.getTrees().getElement(vp);
        if (el == null || el.getKind() != ElementKind.LOCAL_VARIABLE) {
            return null;
        }
        
        StringBufferUsageScanner scanner = new StringBufferUsageScanner(ci, ((VariableElement)el));
        TreePath declRoot = ctx.getPath().getParentPath();
        scanner.scan(declRoot, null);
        if (scanner.isIncompatible()) {
            return null;
        }
        NewAppendScanner newScan = new NewAppendScanner(ci);
        if (newScan.scan(initP, null) != Boolean.TRUE || !newScan.hasContents) {
            return null;
        }
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_ReplaceStringBufferByString(), 
                new RewriteToStringFix(
                    TreePathHandle.create(vp, ci),
                    TreePathHandle.create(declRoot, ci)
                ).toEditorFix());
    }
    
    /**
     * The scanner determines, if the expression is just a series of new SB().append().append()...
     */
    private static class NewAppendScanner extends ErrorAwareTreePathScanner<Boolean, Void> {
        private final CompilationInfo ci;
        private boolean hasContents;
        
        public NewAppendScanner(CompilationInfo ci) {
            this.ci = ci;
        }
        
        @Override
        public Boolean reduce(Boolean r1, Boolean r2) {
            return false;
        }
        
        @Override
        public Boolean visitMemberSelect(MemberSelectTree node, Void p) {
            Boolean r = scan(node.getExpression(), p);
            if (r != Boolean.TRUE) {
                return false;
            }
            boolean appended = node.getIdentifier().contentEquals("append"); // NOI18N
            hasContents |= appended;
            return appended;
        }

        @Override
        public Boolean visitNewClass(NewClassTree node, Void p) {
            TypeMirror tm = ci.getTrees().getTypeMirror(getCurrentPath());
            if (tm == null || tm.getKind() != TypeKind.DECLARED) {
                return false;
            }
            TypeElement el = (TypeElement)((DeclaredType)tm).asElement();
            if (el == null) {
                return false;
            }
            Name n = el.getQualifiedName();
            boolean res = n.contentEquals("java.lang.StringBuilder") || n.contentEquals("java.lang.StringBuffer"); // NOI18N
            // check if there is some initial contents
            if (node.getArguments().size() == 1 && 
                    Utilities.isJavaString(ci, ci.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getArguments().get(0))))) {
                hasContents = true;
            }
            return res;
        }

        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, Void p) {
            return scan(node.getMethodSelect(), p);
        }
    }
        
    
    private static class RewriteToStringFix extends JavaFix {
        private final TreePathHandle initPath;
        private WorkingCopy wc;
        private TreeMaker mk;
        private DeclaredType stringType;
        private GeneratorUtilities gu;
        
        public RewriteToStringFix(TreePathHandle initPath, TreePathHandle handle) {
            super(handle);
            this.initPath = initPath;
        }
        
        @Override
        protected String getText() {
            return Bundle.FIX_ReplaceStringBufferByString();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            // rewrite appends in the initializer
            TreePath p = initPath.resolve(ctx.getWorkingCopy());
            if (p == null) {
                return;
            }
            this.wc = ctx.getWorkingCopy();
            this.mk = wc.getTreeMaker();
            Element el = wc.getTrees().getElement(p);
            if (el == null) {
                return;
            }

            this.gu = GeneratorUtilities.get(wc);
            gu.importComments(p.getLeaf(), wc.getCompilationUnit());
            try {
                TypeElement stringEl = wc.getElements().getTypeElement("java.lang.String"); // NOI18N
                if (stringEl == null) {
                    return;
                }
                stringType = (DeclaredType)stringEl.asType();
                VariableTree vt = (VariableTree)p.getLeaf();
                rewriteAppends(new TreePath(p, vt.getInitializer()));
                ToStringTranslator tst = new ToStringTranslator(wc, el);
                tst.scan(ctx.getPath(), tst);
                Tree t = mk.Type(stringType);
                gu.copyComments(vt.getType(), t, true);
                gu.copyComments(vt.getType(), t, false);
                wc.rewrite(vt.getType(), t); // NOI18N
            } finally {
                this.wc = null;
                this.mk = null;
                this.stringType = null;
            }
        }

        private ExpressionTree rewriteNewClass(TreePath p) {
            ExpressionTree expr = (ExpressionTree) p.getLeaf();
            NewClassTree nct = (NewClassTree) expr;
            Element el = wc.getTrees().getElement(p);
            if (el != null && el.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement) el;
                if (ee.getParameters().isEmpty()) {
                    // ctor without parameters, remove
                    return null;
                }
                TypeMirror argType = ee.getParameters().get(0).asType();
                if (argType.getKind() == TypeKind.DECLARED) {
                    ExpressionTree a = nct.getArguments().get(0);
                    gu.copyComments(expr, a, true);
                    gu.copyComments(expr, a, false);
                    wc.rewrite(expr, a);
                    return a;
                }
                return null;
            }
            return expr;
        }

        /**
         * Rewrites appends in the initializer into string concatenation.
         *
         * @param wc
         * @param p
         * @return
         */
        private ExpressionTree rewriteAppends(TreePath p) {
            ExpressionTree expr = (ExpressionTree) p.getLeaf();
            if (expr.getKind() == Tree.Kind.NEW_CLASS) {
                return rewriteNewClass(p);
            }
            if (expr.getKind() != Tree.Kind.METHOD_INVOCATION) {
                return expr;
            }

            MethodInvocationTree mit = (MethodInvocationTree) expr;
            if (mit.getMethodSelect().getKind() != Tree.Kind.MEMBER_SELECT) {
                return expr;
            }
            MemberSelectTree select = (MemberSelectTree) mit.getMethodSelect();
            if (!select.getIdentifier().contentEquals("append")) { // NOI18N
                return expr;
            }
            // rewrite the subtree
            ExpressionTree selector = rewriteAppends(new TreePath(new TreePath(p, mit.getMethodSelect()), select.getExpression()));
            if (mit.getArguments().size() != 1) {
                // error ?
                if (selector == null) {
                    return null;
                } else {
                    gu.copyComments(select.getExpression(), mit, true);
                    gu.copyComments(select.getExpression(), mit, false);
                    wc.rewrite(mit, select.getExpression());
                    return expr;
                }
            }
            ExpressionTree arg = mit.getArguments().get(0);
            TreePath argPath = new TreePath(p, arg);
            TypeMirror tm1 = wc.getTrees().getTypeMirror(argPath);
            boolean b1 = wc.getTypes().isSameType(tm1, stringType);

            if (selector != null) {
                // ensure that at least one of the arguments is a String
                TreePath selectPath = new TreePath(p, selector);
                // the selector part is potentially rewritten; reattribute
                wc.getTreeUtilities().attributeTree(selector, wc.getTrees().getScope(selectPath));
                TypeMirror tm2 = wc.getTrees().getTypeMirror(selectPath);
                boolean b2 = wc.getTypes().isSameType(tm2, stringType);

                // if either of operands is String, the result should be String as well. Otherwise use
                // wrapper types .toString(param), or Object's toString().
                if (b1 || b2) {
                    arg = makeParenthesis(arg);
                } else {
                    arg = makeToString(argPath);
                    selector = makeToString(selectPath);
                }
                arg = mk.Binary(Tree.Kind.PLUS, selector, makeParenthesis(arg));
                TreePath resPath = new TreePath(p, arg);
                Object o = ArithmeticUtilities.compute(wc, resPath, true, true);
                if (o instanceof String) {
                    arg = mk.Literal(o);
                }
            } else {
                Object o = ArithmeticUtilities.compute(wc, argPath, true, true);
                if (ArithmeticUtilities.isRealValue(o)) {
                    arg = mk.Literal(o.toString());
                } else if (!b1) {
                    arg = makeToString(argPath);
                }
            }
            gu.copyComments(mit, arg, true);
            gu.copyComments(mit, arg, false);
            wc.rewrite(mit, arg);
            return arg;
        }

        private ExpressionTree makeParenthesis(ExpressionTree arg) {
            Class c =  arg.getKind().asInterface();
            // if the original append argument was an expression, surround it in parenthesis, to get the same toString effect
            if (c == BinaryTree.class || c == UnaryTree.class || c == CompoundAssignmentTree.class || c == AssignmentTree.class ||
                c == ConditionalExpressionTree.class) {
                return mk.Parenthesized(arg);
            } else {
                return arg;
            }
        }

        /**
         * Turns the argPath leaf expression to Wrapper.toString(arg) for primitives, or to String.valueOf(arg) for
         * object types. String.valueOf is null-safe.
         */
        private ExpressionTree makeToString(TreePath argPath) {
            ExpressionTree arg = (ExpressionTree)argPath.getLeaf();
            TypeMirror tm = wc.getTrees().getTypeMirror(argPath);
            if (isPrimitiveType(tm)) {
                // call WrapperType.toString(arg)
                arg = mk.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),
                    mk.MemberSelect(
                        mk.QualIdent(wc.getTypes().boxedClass((PrimitiveType)tm)), 
                        "toString" // NOI18N
                    ), Collections.singletonList(arg));
            } else {
                arg = mk.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),
                    mk.MemberSelect(
                        mk.QualIdent(stringType.asElement()),
                        "valueOf" // NOI18N
                    ), Collections.singletonList(arg)
                );
            }

            return arg;
        }
    }
    
    private static boolean isPrimitiveType(TypeMirror tm) {
        if (tm == null) {
            return false;
        }
        switch(tm.getKind()) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE: case FLOAT: 
            case INT: case LONG: case SHORT:
                return true;
            default:
                return false;
        }
    }
    
    
    /**
     * Translates rest of references in the method so that .toString() is stripped, as the variable is going
     * to change type to String.
     */
    private static class ToStringTranslator extends ErrorAwareTreePathScanner {
        private final WorkingCopy wc;
        private final Element varElement;
        private final GeneratorUtilities gu;
        
        public ToStringTranslator(WorkingCopy wc, Element varElement) {
            this.wc = wc;
            this.varElement = varElement;
            this.gu = GeneratorUtilities.get(wc);
        }
        
        @Override
        public Object visitMemberSelect(MemberSelectTree node, Object p) {
            if (wc.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression())) != varElement) {
                return super.visitMemberSelect(node, p);
            }
            Element target = wc.getTrees().getElement(getCurrentPath());
            if (target != null && target.getKind() == ElementKind.METHOD) {
                Tree x = getCurrentPath().getParentPath().getLeaf();
                Tree.Kind k = x.getKind();
                if (k == Tree.Kind.METHOD_INVOCATION) {
                    if (node.getIdentifier().contentEquals("toString")) { // NOI18N
                        // rewrite the node to just the variable, which is going to change the type
                        gu.copyComments(x, node.getExpression(), true);
                        gu.copyComments(x, node.getExpression(), false);
                        wc.rewrite(x, node.getExpression());
                    }
                }
            }
            return super.visitMemberSelect(node, p);
        }
    }
    
    /**
     * Methods, which are present on the StringBuffer, but are not available with String. 
     */
    private static final Set<String> INCOMPATIBLE_METHOD_NAMES = new HashSet<String>(Arrays.<String>asList(
        "append", // NOI18N
        "delete", // NOI18N
        "deleteCharAt", // NOI18N
        "insert", // NOI18N
        "replace", // NOI18N
        "reverse",  // NOI18N
        "setCharAt", // NOI18N
        "setLength", // NOI18N
        
        "capacity", // NOI18N
        "ensureCapacity", // NOI18N
        "trimToSize" // NOI18N
    ));
    
    /**
     * Checks uses of the 'var' element throughout the method. The variable must NOT be assigned to (except the initializer),
     * its value must NOT be assigned to another variable, field, passed to another method call or returned from the method.
     * Only String-compatible methods may be called on the variable.
     */
    private static class StringBufferUsageScanner extends ErrorAwareTreePathScanner<Boolean, Void> {
        private final CompilationInfo ci;
        private final VariableElement var;
        
        private boolean assignedFromVar;
        private boolean assignedToVar;
        private boolean assignedToArray;
        private boolean returned;
        private boolean incompatibleMethodCalled;
        private boolean passedToMethod;
//        private Set<Tree> approvedTrees = Collections.emptySet();
//        private Set<Tree> foundTrees = new HashSet<Tree>();

        public StringBufferUsageScanner(CompilationInfo ci, VariableElement var) {
            this.ci = ci;
            this.var = var;
        }
        
        public boolean isIncompatible() {
            return assignedFromVar || assignedToArray || assignedToVar || returned || incompatibleMethodCalled || passedToMethod;
        }

        @Override
        public Boolean scan(Tree tree, Void p) {
            boolean stop = isIncompatible();
            if (stop /*|| approvedTrees.contains(tree) */) {
                return false;
            }
            return super.scan(tree, p);
        }

        @Override
        public Boolean visitIdentifier(IdentifierTree node, Void p) {
            Element el = ci.getTrees().getElement(getCurrentPath());
            if (el == var) {
                // foundTrees.add(node);
                return true;
            }
            return super.visitIdentifier(node, p);
        }
        
        @Override
        public Boolean visitAssignment(AssignmentTree node, Void p) {
            // used at the L-value
            Boolean lval = scan(node.getVariable(), p);
            if (lval == Boolean.TRUE) {
                assignedFromVar = true;
                return true;
            }
            Boolean res = scan(node.getExpression(), p);
            if (res == Boolean.TRUE) {
                assignedToVar = true;
            }
            return false;
        }

        @Override
        public Boolean visitNewArray(NewArrayTree node, Void p) {
            if (node.getInitializers() != null) {
                for (ExpressionTree et : node.getInitializers()) {
                    if (scan(et, p) == Boolean.TRUE) {
                        assignedToArray = true;
                        break;
                    }
                }
            }
            return false;
        }

        @Override
        public Boolean visitNewClass(NewClassTree node, Void p) {
            scan(node.getEnclosingExpression(), p);
            for (ExpressionTree et : node.getArguments()) {
                Boolean used = scan(et, p);
                if (used == Boolean.TRUE) {
                    passedToMethod = true;
                }
            }
            return false;
        }

        @Override
        public Boolean visitMemberSelect(MemberSelectTree node, Void p) {
            Boolean expr = scan(node.getExpression(), p);
            if (expr != Boolean.TRUE) {
                return false;
            }
            String n = node.getIdentifier().toString();
            return INCOMPATIBLE_METHOD_NAMES.contains(n);
        }
        
        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, Void p) {
            Boolean expr = scan(node.getMethodSelect(), p);
            if (expr == Boolean.TRUE) {
                // check invoked methods
                incompatibleMethodCalled = true;
                return expr;
            } else {
                for (ExpressionTree et : node.getArguments()) {
                    Boolean used = scan(et, p);
                    if (used == Boolean.TRUE) {
                        passedToMethod = true;
                    }
                }
            }
            
            return false;
        }

        @Override
        public Boolean visitReturn(ReturnTree node, Void p) {
            if (scan(node.getExpression(), p) == Boolean.TRUE) {
                returned = true;
            }
            return false;
        }
        
    }
}
