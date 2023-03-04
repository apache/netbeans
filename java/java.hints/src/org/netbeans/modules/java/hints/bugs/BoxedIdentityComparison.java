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

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import javax.lang.model.SourceVersion;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.Bundle.*;

/**
 * Implements generalized 'number comparison using =='. In general no wrapper types should be compared by ==.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - typename of the boxed type",
    "TEXT_BoxedValueIdentityComparison={0} values compared using == or !="
})
@Hint(
    displayName = "#DN_WrapperIdentityComparison",
    description = "#DESC_WrapperIdentityComparison",
    category = "bugs",
    options = Hint.Options.QUERY,
    enabled = true,
    suppressWarnings = { "BoxedValueEquality", "NumberEquality" }
)
public class BoxedIdentityComparison {
    
    @TriggerPatterns({
        @TriggerPattern(value = "$x == $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Integer")),
        @TriggerPattern(value = "$x == $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Byte")),
        @TriggerPattern(value = "$x == $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Short")),
        @TriggerPattern(value = "$x == $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Long")),
        @TriggerPattern(value = "$x == $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Float")),
        @TriggerPattern(value = "$x == $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Double")),
        @TriggerPattern(value = "$x == $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Character")),
        @TriggerPattern(value = "$x == $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Boolean")),

        @TriggerPattern(value = "$x != $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Integer")),
        @TriggerPattern(value = "$x != $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Byte")),
        @TriggerPattern(value = "$x != $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Short")),
        @TriggerPattern(value = "$x != $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Long")),
        @TriggerPattern(value = "$x != $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Float")),
        @TriggerPattern(value = "$x != $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Double")),
        @TriggerPattern(value = "$x != $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Character")),
        @TriggerPattern(value = "$x != $y", constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Boolean")),

    })
    public static ErrorDescription wrapperComparisonUsingIdentity(HintContext ctx) {
        TreePath logExprPath = ctx.getPath();
        BinaryTree bt = (BinaryTree)logExprPath.getLeaf();
        TreePath wrapperPath = ctx.getVariables().get("$x"); // NOI18N
        Tree otherOperand = bt.getRightOperand();

        // JLS 15.21; if the other type is primitive, the comparison is correct
        TypeMirror t = ctx.getInfo().getTrees().getTypeMirror(new TreePath(logExprPath, otherOperand));
        if (t == null || t.getKind() != TypeKind.DECLARED) {
            return null;
        }
        t = ctx.getInfo().getTrees().getTypeMirror(wrapperPath);
        // primitive type is assignable to the wrapper, so it will trigger the hint
        if (t == null || t.getKind() != TypeKind.DECLARED) {
            return null;
        }
        final Fix fix;
        
        if (ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_7) < 0) {
           fix = null;
        } else {
            fix = new NullSafeEqualsFix(TreePathHandle.create(logExprPath, ctx.getInfo())).toEditorFix();
        }
        
        return ErrorDescriptionFactory.forTree(ctx, logExprPath, 
                TEXT_BoxedValueIdentityComparison(ctx.getInfo().getTypeUtilities().getTypeName(t)), fix);
    }

    private static final String JU_OBJECTS = "java.util.Objects"; // NOI18N
    
    @NbBundle.Messages({
        "FIX_UseNullSafeEquals=Replace with null-safe equals()"
    })
    private static class NullSafeEqualsFix extends JavaFix {

        public NullSafeEqualsFix(TreePathHandle handle) {
            super(handle);
        }

        @Override
        protected String getText() {
            return FIX_UseNullSafeEquals();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath p = ctx.getPath();
            if (p.getLeaf().getKind() != Tree.Kind.EQUAL_TO  && p.getLeaf().getKind() != Tree.Kind.NOT_EQUAL_TO) {
                // TODO - report ?
                return;
            }
            BinaryTree bt = (BinaryTree)p.getLeaf();
            TreeMaker mk = ctx.getWorkingCopy().getTreeMaker();
            ExpressionTree replace = mk.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                mk.MemberSelect(
                    mk.QualIdent(JU_OBJECTS), "equals" // NOI18N
                ), 
                Arrays.asList(bt.getLeftOperand(), bt.getRightOperand())
            );
            if (bt.getKind() == Tree.Kind.NOT_EQUAL_TO) {
                replace = mk.Unary(Tree.Kind.LOGICAL_COMPLEMENT, replace);
            }
            ctx.getWorkingCopy().rewrite(bt, replace);
        }
    }
}
