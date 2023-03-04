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
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_ConvertTextBlockToString", description = "#DESC_ConvertTextBlockToString", category = "rules15", severity = Severity.HINT)
@Messages({
    "DN_ConvertTextBlockToString=Convert Text block to String",
    "DESC_ConvertTextBlockToString=Converts java 15 Text Blocks back to regular Strings."
})
public class ConvertTextBlockToString {

    @TriggerTreeKind(Tree.Kind.STRING_LITERAL)
    @Messages("ERR_ConvertTextBlockToString=Text block can be converted to String")//NOI18N
    public static ErrorDescription computeWarning(HintContext ctx) {
        TokenSequence<?> ts = ctx.getInfo().getTokenHierarchy().tokenSequence();
        if (ts == null) {
            return null;
        }
        int textBlockIndex = (int) ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getPath().getCompilationUnit(), ctx.getPath().getLeaf());
        if (textBlockIndex == -1) {
            return null;
        }
        ts.move(textBlockIndex);

        if (!ts.moveNext() || ts.token().id() != JavaTokenId.MULTILINE_STRING_LITERAL) {
            return null;
        }

        String orignalString = (String) ((LiteralTree) ctx.getPath().getLeaf()).getValue();
        String orignalStringArr[] = textBlockToStringArr(orignalString);
        Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), orignalStringArr).toEditorFix();
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertTextBlockToString(), fix);
    }

    private static String[] textBlockToStringArr(String textBlock) {
        String oneLine[] = textBlock.split("\n", -1);    // NOI18N
        return oneLine;
    }

    private static final class FixImpl extends JavaFix {

        String orignalStringArr[];

        public FixImpl(CompilationInfo info, TreePath tp, String orignalStringArr[]) {
            super(info, tp);
            this.orignalStringArr = orignalStringArr;
        }

        @Override
        @Messages("FIX_ConvertTextBlockToString=Convert to String")
        protected String getText() {
            return Bundle.FIX_ConvertTextBlockToString();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            ExpressionTree ext = ctx.getWorkingCopy().getTreeMaker().Literal(orignalStringArr[orignalStringArr.length - 1]);
            if (orignalStringArr.length > 1) {
                ext = ctx.getWorkingCopy().getTreeMaker().Binary(Tree.Kind.PLUS, buildTree(orignalStringArr, orignalStringArr.length - 2, ctx), ext);
                if (orignalStringArr[orignalStringArr.length - 1].isEmpty()) {
                    ext = (((BinaryTree) ext).getLeftOperand());
                }
            }
            ctx.getWorkingCopy().rewrite(ctx.getPath().getLeaf(), ext);
        }

        private static ExpressionTree buildTree(String textBlockLines[], int currentLine, TransformationContext ctx) {
            if (currentLine == 0) {
                return ctx.getWorkingCopy().getTreeMaker().Literal(textBlockLines[0] + "\n");// NOI18N
            }
            return ctx.getWorkingCopy().getTreeMaker().Binary(Tree.Kind.PLUS, buildTree(textBlockLines, currentLine - 1, ctx), ctx.getWorkingCopy().getTreeMaker().Literal(textBlockLines[currentLine] + "\n"));// NOI18N
        }
    }
}
