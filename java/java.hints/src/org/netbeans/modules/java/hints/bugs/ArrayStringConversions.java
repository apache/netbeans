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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.Bundle.*;
import org.netbeans.modules.java.hints.errors.Utilities;

/**
 * Detects places where an array instance is Stringified. This is done explicitly by a call to toString(), or implicitly in
 * <ul>
 * <li>String concatenation
 * <li>parameter passed into in PrintStream.print, println, printf
 * <li>parameter passed into in PrintWriter.print, println, printf
 * <li>parameter passed into String.format
 * <li>parameter passed into MessageFormat.format()
 * </ul>
 * 
 * Theoretically, many other library function / methods may accept Object parameter and then create a String representation
 * out of it. List of FQNs of such methods could be provided by the user in options.
 *
 * @author sdedic
 */
@Hint(
        displayName = "#DN_ToStringOnArray",
        description = "#DESC_ToStringOnArray",
        category = "bugs",
        enabled = true,
        suppressWarnings = { "ImplicitArrayToString" }
)
@NbBundle.Messages({
    "TEXT_ToStringCalledOnArray=toString() called on array instance",
    "TEXT_ArrayPrintedOnStream=Array instance printed on PrintStream",
    "TEXT_ArrayPrintedOnWriter=Array instance printed on PrintWriter",
    "TEXT_ArrayFormatParameter=Array instance passed as parameter to a formatter function",
    "TEXT_ArrayConcatenatedToString=Array concatenated with String",
    "FIX_WrapUsingArraysAsList=Wrap array using Arrays.toString",
    "FIX_WrapUsingArraysAsDeepList=Wrap array using Arrays.deepToString"
})
public class ArrayStringConversions {
    
    /**
     * Determines whether the array can contain other arrays. Checks if the component type is an array type.
     * Otherwise, checks whether the component type is a declared one, and that it is an Object. Other declared
     * types cannot hold array references.
     * 
     * @param ci context
     * @param tp path to the array expression
     * @return true, if the expression (assuming array type) can hold other arrays as members
     */
    static boolean canContainArrays(CompilationInfo ci, TreePath tp) {
        TypeMirror tm = ci.getTrees().getTypeMirror(tp);
        if (!Utilities.isValidType(tm)) {
            return false;
        }
        tm = ci.getTypes().erasure(tm);
        if (tm == null || tm.getKind() != TypeKind.ARRAY) {
            return false;
        }
        ArrayType arrayType = (ArrayType)tm;
        TypeMirror ct = arrayType.getComponentType();
        boolean enableDeep = false;
        
        if (ct.getKind() == TypeKind.ARRAY) {
            // if the component kind is known to be an array, enable deepHash
            enableDeep = true;
        } else if (ct.getKind() == TypeKind.DECLARED) {
            Element obj = ci.getElements().getTypeElement("java.lang.Object"); // NOI18N
            if (obj == null) {
                return false;
            }
            // other ref types than Object cannot hold array instances, deepHash is a nonsense
            enableDeep = ci.getTypes().isSameType(obj.asType(), ct);
        }
        return enableDeep;
    }
    
    
    @TriggerPatterns({
        @TriggerPattern(
                value = "$v.toString()", 
                constraints = {
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.toString()", 
                constraints = {
                    @ConstraintVariableType(type = "int[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.toString()", 
                constraints = {
                    @ConstraintVariableType(type = "short[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.toString()", 
                constraints = {
                    @ConstraintVariableType(type = "byte[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.toString()", 
                constraints = {
                    @ConstraintVariableType(type = "long[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.toString()", 
                constraints = {
                    @ConstraintVariableType(type = "char[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.toString()", 
                constraints = {
                    @ConstraintVariableType(type = "float[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.toString()", 
                constraints = {
                    @ConstraintVariableType(type = "double[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value = "$v.toString()", 
                constraints = {
                    @ConstraintVariableType(type = "boolean[]", variable = "$v")
                }
        )
    })
    public static ErrorDescription arrayToString(HintContext ctx) {
        TreePath arrayRef = ctx.getVariables().get("$v");
        boolean deep = canContainArrays(ctx.getInfo(), arrayRef);
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), TEXT_ToStringCalledOnArray(), 
                new ArraysToStringFix(false, true, ctx.getInfo(), ctx.getPath()).toEditorFix(), 
                deep ? new ArraysToStringFix(true, true, ctx.getInfo(), ctx.getPath()).toEditorFix() : null);
    }

    /**
     * Fix that wraps the array into Arrays.toString or deepToString.
     */
    private static final class ArraysToStringFix extends JavaFix {
        private final boolean deep;
        private final boolean arraySelect;
        
        /**
         * Constructs a new fix instance
         * @param deep if true, use .deepToString
         * @param arraySelect if true, the path points to .toString invocation; false means path points to the array expr
         * @param info context
         * @param tp path to the expression/invocation
         */
        public ArraysToStringFix(boolean deep, boolean arraySelect, CompilationInfo info, TreePath tp) {
            super(info, tp);
            this.deep = deep;
            this.arraySelect = arraySelect;
        }

        @Override
        protected String getText() {
            return deep ? FIX_WrapUsingArraysAsDeepList() : FIX_WrapUsingArraysAsList();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreeMaker maker = ctx.getWorkingCopy().getTreeMaker();
            Tree t = ctx.getPath().getLeaf();
            final ExpressionTree arrayExpr;
            if (arraySelect) {
                if (t.getKind() != Tree.Kind.METHOD_INVOCATION) {
                    // PENDING: log ?
                    return;
                }
                MethodInvocationTree mtt = (MethodInvocationTree)ctx.getPath().getLeaf();
                if (mtt.getMethodSelect().getKind() != Tree.Kind.MEMBER_SELECT) {
                    // PENDING: log ?
                    return;
                }
                MemberSelectTree selector = (MemberSelectTree)mtt.getMethodSelect();
                arrayExpr = selector.getExpression();
            } else {
                arrayExpr = (ExpressionTree)t;
            }
            
            ExpressionTree ms = maker.MemberSelect(maker.QualIdent("java.util.Arrays"), deep ? "deepToString" : "toString"); // NOI18N
            Tree nue = maker.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(), 
                            ms, 
                            Collections.singletonList(arrayExpr)
            );
            ctx.getWorkingCopy().rewrite(t, nue);
        }
    }
    
    @TriggerPatterns({
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v")
                }
        )
    })
    public static ErrorDescription printPrintStream(HintContext ctx) {
        return printStreamWriter(ctx, TEXT_ArrayPrintedOnStream());
    }
    
    @TriggerPatterns({
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v")
                }
        )
    })
    public static ErrorDescription printlnPrintStream(HintContext ctx) {
        TreePath arrayRef = ctx.getVariables().get("$v");
        TypeMirror m = ctx.getInfo().getTrees().getTypeMirror(arrayRef);
        if (!Utilities.isValidType(m) || m.getKind() == TypeKind.NULL) {
            return null;
        }
        return printPrintStream(ctx);
    }

    @TriggerPatterns({
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "char[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),

        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "char[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        )
    })
    public static List<ErrorDescription> formatPrintStream(HintContext ctx) {
        return arrayFormatted(ctx, TEXT_ArrayFormatParameter());
    }
    

    @TriggerPatterns({
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "char[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),

        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "char[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintStream", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        )
    })
    public static List<ErrorDescription> printfPrintStream(HintContext ctx) {
        return arrayFormatted(ctx, TEXT_ArrayFormatParameter());
    }
    
    @TriggerPatterns({
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.print($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v")
                }
        )
    })
    public static ErrorDescription printPrintWriter(HintContext ctx) {
        return printStreamWriter(ctx, TEXT_ArrayPrintedOnWriter());
    }
    
    @TriggerPatterns({
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.println($v)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v")
                }
        )
    })
    public static ErrorDescription printlnPrintWriter(HintContext ctx) {
        return printPrintWriter(ctx);
    }

    @TriggerPatterns({
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "char[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),

        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "char[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.printf($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        )
    })
    public static List<ErrorDescription> printfPrintWriter(HintContext ctx) {
        return arrayFormatted(ctx, TEXT_ArrayFormatParameter());
    }


    @TriggerPatterns({
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "char[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),

        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "char[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        ),
        @TriggerPattern(
                value="$s.format($l, $f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.io.PrintWriter", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v"),
                    @ConstraintVariableType(type = "java.util.Locale", variable = "$l"),
                    @ConstraintVariableType(type = "java.lang.String", variable = "$f")
                }
        )
    })
    public static List<ErrorDescription> formatPrintWriter(HintContext ctx) {
        return arrayFormatted(ctx, TEXT_ArrayFormatParameter());
    }

    private static ErrorDescription printStreamWriter(HintContext ctx, String text) {
        TreePath arrayRef = ctx.getVariables().get("$v");
        boolean deep = canContainArrays(ctx.getInfo(), arrayRef);
        return ErrorDescriptionFactory.forTree(ctx, arrayRef, text, 
                new ArraysToStringFix(false, false, ctx.getInfo(), arrayRef).toEditorFix(), 
                deep ? new ArraysToStringFix(true, false, ctx.getInfo(), arrayRef).toEditorFix() : null);
    }
    
    /**
     * Assumes the context matched a parameter in a format(...) function, like printf, or String.format(). It will
     * scan rest of the arguments after the match and for each argument of [] type will issue a hint to convert to Arrays.toString(). 
     * Note - the impl allows to call for parameter list *without* formatting String as the first argument as in msgFormat.format(params).
     */
    private static List<ErrorDescription> arrayFormatted(HintContext ctx, String text) {
        // assume the path identifies a method invocation
        if (ctx.getPath().getLeaf().getKind() != Tree.Kind.METHOD_INVOCATION) {
            return null;
        }
        List<ErrorDescription> ret = new ArrayList<ErrorDescription>(2);
        CompilationInfo ci = ctx.getInfo();
        MethodInvocationTree mit = (MethodInvocationTree)ctx.getPath().getLeaf();
        TreePath arrayRef = ctx.getVariables().get("$v");
        // the pattern matcher only identifies one match, it is possible that another array is passed in the subsequent
        // formatting parameters
        TypeMirror m = ctx.getInfo().getTrees().getTypeMirror(arrayRef);
        if (!Utilities.isValidType(m) || m.getKind() == TypeKind.NULL) {
            return null;
        }
        Element e = ci.getTrees().getElement(ctx.getPath());
        if (e == null || !(e instanceof ExecutableElement)) {
            return null;
        }
        ExecutableElement el = (ExecutableElement)e;
        boolean isVarArgs = el.isVarArgs();
        
        for (int index = mit.getArguments().indexOf(arrayRef.getLeaf()); index < mit.getArguments().size(); index++) {
            Tree arg = mit.getArguments().get(index);
            TreePath argPath = new TreePath(ctx.getPath(), arg);
            if (arg != arrayRef.getLeaf()) {
                // check for array type, can bypass for the 1st provided by matcher
                TypeMirror argType = ci.getTrees().getTypeMirror(argPath);
                if (argType == null || argType.getKind() != TypeKind.ARRAY) {
                    continue;
                }
            }
            if (arg.getKind() == Tree.Kind.NULL_LITERAL) {
                continue;
            }
            if (isVarArgs && (index == el.getParameters().size() - 1) && index == (mit.getArguments().size() - 1)) {
                TypeMirror argType = ci.getTrees().getTypeMirror(argPath);
                // check whether the argtype exactly matches the vararg; so if Object[] is passed to a Object... vararg method,
                // the array will be unrolled and not printed as is
                if (ci.getTypes().isSameType(argType, el.getParameters().get(el.getParameters().size() - 1).asType())) {
                    continue;
                }
            }
            boolean deep = canContainArrays(ctx.getInfo(), argPath);
            ret.add( ErrorDescriptionFactory.forTree(ctx, argPath, text, 
                    new ArraysToStringFix(false, false, ctx.getInfo(), argPath).toEditorFix(), 
                    deep ? new ArraysToStringFix(true, false, ctx.getInfo(), argPath).toEditorFix() : null));
        }
        return ret;
    }
    
    @TriggerPatterns({
        @TriggerPattern(
                value="java.text.MessageFormat.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.text.MessageFormat.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "int[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.text.MessageFormat.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "short[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.text.MessageFormat.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "byte[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.text.MessageFormat.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "long[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.text.MessageFormat.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "char[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.text.MessageFormat.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "float[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.text.MessageFormat.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "double[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.text.MessageFormat.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "boolean[]", variable = "$v")
                }
        )
    })
    public static List<ErrorDescription> messageFormatStatic(HintContext ctx) {
        return arrayFormatted(ctx, TEXT_ArrayFormatParameter());
    }
    
    @TriggerPatterns({
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "char[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($v, $vars$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "int[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($v, $vars$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "byte[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($v, $vars$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "char[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($v, $vars$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "short[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($v, $vars$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "long[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($v, $vars$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "float[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($v, $vars$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "double[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="$s.format($v, $vars$)",
                constraints = {
                    @ConstraintVariableType(type = "java.text.MessageFormat", variable = "$s"),
                    @ConstraintVariableType(type = "boolean[]", variable = "$v")
                }
        ),
    })
    public static List<ErrorDescription> messageFormatInstance(HintContext ctx) {
        return arrayFormatted(ctx, TEXT_ArrayFormatParameter());
    }

    @TriggerPatterns({
        @TriggerPattern(
                value="java.lang.String.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.lang.String.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "int[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.lang.String.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "short[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.lang.String.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "byte[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.lang.String.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "long[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.lang.String.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "char[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.lang.String.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "float[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.lang.String.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "double[]", variable = "$v")
                }
        ),
        @TriggerPattern(
                value="java.lang.String.format($f, $vars1$, $v, $vars2$)",
                constraints = {
                    @ConstraintVariableType(type = "boolean[]", variable = "$v")
                }
        )
    })
    public static List<ErrorDescription> stringFormat(HintContext ctx) {
        return arrayFormatted(ctx, TEXT_ArrayFormatParameter());
    }
    
    
    @TriggerPatterns({
        @TriggerPattern(value = "$x + $v",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$x + $v",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "int[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$x + $v",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "short[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$x + $v",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "byte[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$x + $v",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "long[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$x + $v",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "char[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$x + $v",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "float[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$x + $v",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "double[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$x + $v",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "boolean[]", variable = "$v")
            }
        ),

        @TriggerPattern(value = "$v + $x",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "java.lang.Object[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$v + $x",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "int[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$v + $x",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "short[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$v + $x",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "byte[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$v + $x",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "long[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$v + $x",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "char[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$v + $x",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "float[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$v + $x",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "double[]", variable = "$v")
            }
        ),
        @TriggerPattern(value = "$v + $x",
            constraints = {
                @ConstraintVariableType(type = "java.lang.String", variable = "$x"),
                @ConstraintVariableType(type = "boolean[]", variable = "$v")
            }
        ),

    })
    public static ErrorDescription stringConcatenation(HintContext ctx) {
        TreePath vPath = ctx.getVariables().get("$v");
        // #248586: check that the type of the `v' variable is not null (as null is assignable to every
        // array resulting in incorrect hints
        TypeMirror m = ctx.getInfo().getTrees().getTypeMirror(vPath);
        if (!Utilities.isValidType(m) || m.getKind() == TypeKind.NULL) {
            return null;
        }
        boolean deep = canContainArrays(ctx.getInfo(), vPath);
        return ErrorDescriptionFactory.forTree(ctx, vPath, TEXT_ArrayConcatenatedToString(), 
            new ArraysToStringFix(false, false, ctx.getInfo(), vPath).toEditorFix(),
            deep ? new ArraysToStringFix(true, false, ctx.getInfo(), vPath).toEditorFix() : null
        );
    }
}
