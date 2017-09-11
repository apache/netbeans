/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
