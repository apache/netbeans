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

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.TreePath;
import javax.lang.model.SourceVersion;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "TEXT_UnnecessaryUnboxing=Unnecessary unboxing",
    "# {0} - the type-specific unboxing method name",
    "FIX_UnnecessaryUnboxing=Remove .{0}()"
})
@Hint(
    category = "rules15",
    displayName = "#DN_UnnecessaryUnboxing",
    description = "#DESC_UnnecessaryUnboxing",
    enabled = true,
    suppressWarnings = "UnnecessaryUnboxing",
    minSourceVersion = "5"
    
)
public class UnnecessaryUnboxing {
    
    @TriggerPatterns({
        @TriggerPattern(value = "$v.intValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Integer")),
        @TriggerPattern(value = "$v.byteValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Byte")),
        @TriggerPattern(value = "$v.shortValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Short")),
        @TriggerPattern(value = "$v.longValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Long")),
        @TriggerPattern(value = "$v.charValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Character")),
        @TriggerPattern(value = "$v.floatValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Float")),
        @TriggerPattern(value = "$v.doubleValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Double")),
        @TriggerPattern(value = "$v.booleanValue()", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Boolean"))
    })
    public static ErrorDescription run(HintContext ctx) {
        CompilationInfo ci = ctx.getInfo();
        if (ctx.getPath().getLeaf().getKind() != Tree.Kind.METHOD_INVOCATION) {
            return null;
        }
        MethodInvocationTree mit = (MethodInvocationTree)ctx.getPath().getLeaf();
        if (mit.getMethodSelect().getKind() != Tree.Kind.MEMBER_SELECT) {
            return null;
        }
        String mn = ((MemberSelectTree)mit.getMethodSelect()).getIdentifier().toString();
        TreePath unboxPath = ctx.getPath();
        TreePath vPath = ctx.getVariables().get("$v"); // NOI18N
        
        Tree followed = unboxPath.getLeaf();
        TreePath parentPath = unboxPath.getParentPath();
        while (parentPath.getLeaf().getKind() == Tree.Kind.PARENTHESIZED) {
            followed = parentPath.getLeaf();
            parentPath = parentPath.getParentPath();
        }
        
        Tree t = parentPath.getLeaf();
        TreePath otherPath = null;
        
        switch (t.getKind()) {
            // with conditional expression, if both operands are object type, the result is most probably object
            // type as well? 
            case TYPE_CAST: {
                // unboxing needed if casted to a primitive which is not assignable to the casted type
                TypeCastTree castTree = (TypeCastTree)t;
                TreePath toTypePath = new TreePath(parentPath, castTree.getType());
                TypeMirror m = ci.getTrees().getTypeMirror(toTypePath);
                TypeMirror vt = ci.getTrees().getTypeMirror(vPath);
                if (!Utilities.isValidType(m) || 
                    (m.getKind().isPrimitive() && !ci.getTypes().isAssignable(ci.getTypes().unboxedType(vt), m))) {
                    return null;
                }
                break;
            }
            case CONDITIONAL_EXPRESSION: {
                ConditionalExpressionTree cte = (ConditionalExpressionTree)t;
                
                Tree other;
                if (cte.getTrueExpression() == followed) {
                    other = cte.getFalseExpression();
                } else if (cte.getFalseExpression() == followed) {
                    other = cte.getTrueExpression();
                } else {
                    break; // switch
                }
                otherPath = new TreePath(parentPath, other);
                TypeMirror m = ci.getTrees().getTypeMirror(otherPath);
                if (m == null || !m.getKind().isPrimitive()) {
                    return null;
                }
                break;
            }
                
            // in the original expression, == and != apply to object references; the other operand MUST be primitive
            // in order those operator work as value comparisons
            case EQUAL_TO:
            case NOT_EQUAL_TO: {
                BinaryTree bt = (BinaryTree)t;
                Tree other = followed == bt.getLeftOperand() ? bt.getRightOperand() : bt.getLeftOperand();
                otherPath = new TreePath(parentPath, other);
                TypeMirror m = ci.getTrees().getTypeMirror(otherPath);
                if (m == null || !m.getKind().isPrimitive()) {
                    return null;
                }
                break;
            }
            
            case METHOD_INVOCATION: {
                if (!Utilities.checkAlternativeInvocation(ci, parentPath, ctx.getPath(), vPath, null)) {
                    return null;
                }
            }
        }
        // check the shape of the other parameter in binary/conditional. It is more readable to have them BOTH
        // unboxed, although one would be sufficient.
        if (otherPath != null) {
            while (otherPath.getLeaf().getKind() == Tree.Kind.PARENTHESIZED) {
                otherPath = new TreePath(otherPath, ((ParenthesizedTree)otherPath.getLeaf()).getExpression());
            }
            Tree other = otherPath.getLeaf();
            if (other.getKind() == Tree.Kind.METHOD_INVOCATION) {
                mit = (MethodInvocationTree)other;
                Tree selTree = mit.getMethodSelect();
                if (selTree.getKind() == Tree.Kind.MEMBER_SELECT) {
                    MemberSelectTree mst = (MemberSelectTree)selTree;
                    TypeMirror x = ctx.getInfo().getTrees().getTypeMirror(new TreePath(otherPath, mst.getExpression()));
                    if (Utilities.isPrimitiveWrapperType(x) &&
                        mst.getIdentifier().toString().endsWith("Value")) { // NOI18N
                        return null;
                    }
                }
            }
        }
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_UnnecessaryUnboxing(), 
                JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_UnnecessaryUnboxing(mn), ctx.getPath(), "$v"));
    }
}
