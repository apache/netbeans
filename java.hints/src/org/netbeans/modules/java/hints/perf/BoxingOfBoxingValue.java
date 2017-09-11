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
package org.netbeans.modules.java.hints.perf;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 * Detects boxing operation on a value, which was already boxed. This means a typecast to boxed type with
 * an argument of boxed type, BoxedType.valueOf(boxedTypeValue) or new BoxedType(boxedTypeValue).
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "TEXT_BoxingOfBoxedValue=Boxing of already boxed value",
    "FIX_RemoveBoxingOfBoxed=Remove extra boxing"
})
@Hint(
    displayName = "#DN_BoxingOfBoxedValue",
    description = "#DESC_BoxingOfBoxedValue",
    category = "performance",
    enabled = true,
    suppressWarnings = "BoxingBoxedValue"
)
public class BoxingOfBoxingValue {
    @TriggerPatterns({
        @TriggerPattern(value = "new java.lang.Byte($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Byte")),
        @TriggerPattern(value = "new java.lang.Character($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Character")),
        @TriggerPattern(value = "new java.lang.Double($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Double")),
        @TriggerPattern(value = "new java.lang.Float($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Float")),
        @TriggerPattern(value = "new java.lang.Integer($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Integer")),
        @TriggerPattern(value = "new java.lang.Long($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Long")),
        @TriggerPattern(value = "new java.lang.Short($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Short")),
        @TriggerPattern(value = "new java.lang.Boolean($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Boolean"))
    })
    public static ErrorDescription newBoxedType(HintContext ctx) {
        TreePath p = ctx.getVariables().get("$v"); // NOI18N
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_BoxingOfBoxedValue(),
                new RemoveBoxingFix(TreePathHandle.create(ctx.getPath(), ctx.getInfo()), 
                TreePathHandle.create(p, ctx.getInfo())).toEditorFix());
    }
    
    @TriggerPatterns({
        @TriggerPattern(value = "java.lang.Byte.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Byte")),
        @TriggerPattern(value = "java.lang.Character.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Character")),
        @TriggerPattern(value = "java.lang.Double.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Double")),
        @TriggerPattern(value = "java.lang.Float.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Float")),
        @TriggerPattern(value = "java.lang.Integer.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Integer")),
        @TriggerPattern(value = "java.lang.Long.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Long")),
        @TriggerPattern(value = "java.lang.Short.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Short")),
        @TriggerPattern(value = "java.lang.Boolean.valueOf($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Boolean"))
    })
    public static ErrorDescription valueOfBoxed(HintContext ctx) {
        TreePath p = ctx.getVariables().get("$v"); // NOI18N
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_BoxingOfBoxedValue(),
                new RemoveBoxingFix(TreePathHandle.create(ctx.getPath(), ctx.getInfo()), 
                TreePathHandle.create(p, ctx.getInfo())).toEditorFix());
    }

    @TriggerPatterns({
        @TriggerPattern(value = "(java.lang.Byte)$v", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Byte")),
        @TriggerPattern(value = "(java.lang.Character)$v", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Character")),
        @TriggerPattern(value = "(java.lang.Double)$v", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Double")),
        @TriggerPattern(value = "(java.lang.Float)$v", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Float")),
        @TriggerPattern(value = "(java.lang.Integer)$v", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Integer")),
        @TriggerPattern(value = "(java.lang.Long)$v", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Long")),
        @TriggerPattern(value = "(java.lang.Short)$v", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Short")),
        @TriggerPattern(value = "(java.lang.Boolean)$v", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Boolean")),

        @TriggerPattern(value = "(java.lang.Byte)($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Byte")),
        @TriggerPattern(value = "(java.lang.Character)($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Character")),
        @TriggerPattern(value = "(java.lang.Double)($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Double")),
        @TriggerPattern(value = "(java.lang.Float)($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Float")),
        @TriggerPattern(value = "(java.lang.Integer)($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Integer")),
        @TriggerPattern(value = "(java.lang.Long)($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Long")),
        @TriggerPattern(value = "(java.lang.Short)($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Short")),
        @TriggerPattern(value = "(java.lang.Boolean)($v)", constraints = @ConstraintVariableType(variable = "$v", type = "java.lang.Boolean"))
    })
    public static ErrorDescription typecastBoxed(HintContext ctx) {
        TreePath p = ctx.getVariables().get("$v"); // NOI18N
        if (p.getLeaf().getKind() == Tree.Kind.NULL_LITERAL) {
            return null;
        }
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), Bundle.TEXT_BoxingOfBoxedValue(),
                new RemoveBoxingFix(TreePathHandle.create(ctx.getPath(), ctx.getInfo()), 
                TreePathHandle.create(p, ctx.getInfo())).toEditorFix());
    }
    
    private static class RemoveBoxingFix extends JavaFix {
        private final TreePathHandle    rawExpressionPath;

        public RemoveBoxingFix(TreePathHandle handle, TreePathHandle rawExpressionPath) {
            super(handle);
            this.rawExpressionPath = rawExpressionPath;
        }

        @Override
        protected String getText() {
            return Bundle.FIX_RemoveBoxingOfBoxed();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath from = ctx.getPath();
            TreePath to = rawExpressionPath.resolve(wc);
            if (to == null) {
                return;
            }
            GeneratorUtilities gu = GeneratorUtilities.get(wc);
            gu.importComments(from.getLeaf(), wc.getCompilationUnit());
            gu.copyComments(from.getLeaf(), to.getLeaf(), true);
            gu.copyComments(from.getLeaf(), to.getLeaf(), false);
            wc.rewrite(from.getLeaf(), to.getLeaf());
        }
    }
}
