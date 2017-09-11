/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
