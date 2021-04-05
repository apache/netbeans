/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_ConvertToTextBlock", description = "#DESC_ConvertToTextBlock", category="rules15",
      minSourceVersion = "13")
@Messages({
    "DN_ConvertToTextBlock=Convert to Text Block",
    "DESC_ConvertToTextBlock=Convert to Text Block"
})
public class ConvertToTextBlock {

    @TriggerTreeKind(Kind.PLUS)
    @Messages("ERR_ConvertToTextBlock=Can be converted to text block")
    public static ErrorDescription computeWarning(HintContext ctx) {
        if (!CompilerOptionsQuery.getOptions(ctx.getInfo().getFileObject()).getArguments().contains("--enable-preview"))
            return null;
        if (ctx.getPath().getParentPath() != null && getTextOrNull(ctx.getPath().getParentPath()) != null) {
            return null;
        }
        String text = getTextOrNull(ctx.getPath());
        if (text == null) {
            return null;
        }
        Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), text).toEditorFix();
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertToTextBlock(), fix);
    }

    private static String getTextOrNull(TreePath tp) {
        StringBuilder text = new StringBuilder();
        Tree current = tp.getLeaf();
        while (current.getKind() == Kind.PLUS) {
            BinaryTree bt = (BinaryTree) current;
            if (bt.getRightOperand().getKind() == Kind.STRING_LITERAL) {
                text.insert(0, ((LiteralTree) bt.getRightOperand()).getValue());
            } else {
                return null;
            }
            current = bt.getLeftOperand();
        }
        if (current.getKind() == Kind.STRING_LITERAL) {
            text.insert(0, ((LiteralTree) current).getValue());
        } else {
            return null;
        }
        String textString = text.toString();
        if (!textString.contains("\n")) {
            return null;
        }
        return textString;
    }

    private static final class FixImpl extends JavaFix {

        private final String text;

        public FixImpl(CompilationInfo info, TreePath tp, String text) {
            super(info, tp);
            this.text = text;
        }

        @Override
        @Messages("FIX_ConvertToTextBlock=Convert to text block")
        protected String getText() {
            return Bundle.FIX_ConvertToTextBlock();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            ctx.getWorkingCopy().rewrite(ctx.getPath().getLeaf(), ctx.getWorkingCopy().getTreeMaker().Literal(text.split("\n", -1)));
            //perform the required transformation
        }
    }
}
