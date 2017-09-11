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
