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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Map;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.PointlessBitwiseExpression", description = "#DESC_org.netbeans.modules.java.hints.PointlessBitwiseExpression", category="bitwise_operations", suppressWarnings="PointlessBitwiseExpression")
public class PointlessBitwiseExpression {

    @TriggerPatterns ({
        @TriggerPattern (value="$v >> $c"),
        @TriggerPattern (value="$v >>> $c"),
        @TriggerPattern (value="$v << $c")
    })
    public static ErrorDescription checkPointlessShiftExpression (HintContext ctx) {
        TreePath treePath = ctx.getPath ();
        Map<String,TreePath> variables = ctx.getVariables ();
        CompilationInfo compilationInfo = ctx.getInfo ();
        TreePath tree = variables.get ("$c");
        Long value = IncompatibleMask.getConstant (tree, ctx);
        if (value == null) return null;
        if (value == 0)
            return ErrorDescriptionFactory.forName (
                ctx,
                treePath,
                NbBundle.getMessage (PointlessBitwiseExpression.class, "MSG_PointlessBitwiseExpression"),
        new FixImpl (
compilationInfo,
treePath,
NbBundle.getMessage (
LoggerNotStaticFinal.class,
"MSG_PointlessBitwiseExpression_fix"
),
true
).toEditorFix()
            );
        return null;
    }

    @TriggerPatterns ({
        @TriggerPattern (value="$v & $c"),
        @TriggerPattern (value="$v | $c"),
        @TriggerPattern (value="$v ^ $c")
    })
    public static ErrorDescription checkPointlessBitwiseExpression (HintContext ctx) {
        TreePath treePath = ctx.getPath ();
        CompilationInfo compilationInfo = ctx.getInfo ();
        Map<String,TreePath> variables = ctx.getVariables ();
        TreePath tree = variables.get ("$c");
        Long value = IncompatibleMask.getConstant (tree, ctx);
        boolean left = treePath.getLeaf().getKind() != Tree.Kind.AND;
        if (value != null &&
            value == 0
        ) {
            return ErrorDescriptionFactory.forName (
                ctx,
                treePath,
                NbBundle.getMessage (PointlessBitwiseExpression.class, "MSG_PointlessBitwiseExpression"),
                    new FixImpl(
                            compilationInfo,
                            treePath,
                            NbBundle.getMessage(
                                    LoggerNotStaticFinal.class,
                                    left ? "MSG_PointlessBitwiseExpression_fix" : "MSG_PointlessBitwiseExpression_fix2"
                            ),
                            left
                    ).toEditorFix()
            );
        }
        tree = variables.get("$v");
        value = IncompatibleMask.getConstant(tree, ctx);
        if (value != null
                && value == 0) {
            return ErrorDescriptionFactory.forName(
                    ctx,
                    treePath,
                    NbBundle.getMessage(PointlessBitwiseExpression.class, "MSG_PointlessBitwiseExpression"),
                    new FixImpl(
                            compilationInfo,
                            treePath,
                            NbBundle.getMessage(
                                    LoggerNotStaticFinal.class,
                                    left ? "MSG_PointlessBitwiseExpression_fix" : "MSG_PointlessBitwiseExpression_fix2"
                            ),
                            !left
                    ).toEditorFix()
            );
        }
        return null;
    }

    private static final class FixImpl extends JavaFix {

        private final String    text;
        private boolean         left;

        public FixImpl (
            CompilationInfo     info,
            TreePath            tp,
            String              text,
            boolean             left
        ) {
            super(info, tp);
            this.text = text;
            this.left = left;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            Tree vt = tp.getLeaf();
            BinaryTree e = (BinaryTree) vt;
            wc.rewrite (vt, left ? e.getLeftOperand () : e.getRightOperand ());
        }
    } // End of FixImpl class
}
