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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_ConvertTextBlockToString", description = "#DESC_ConvertTextBlockToString", category = "general", minSourceVersion = "13")
@Messages({
    "DN_ConvertTextBlockToString=Convert Text block to String",
    "DESC_ConvertTextBlockToString=Text Block may not be supported in older versions of java older then 13"
})
public class ConvertTextBlockToString {

    @TriggerTreeKind(Tree.Kind.STRING_LITERAL)
    @Messages("ERR_ConvertTextBlockToString=Text block may not be supported")//NOI18N
    public static ErrorDescription computeWarning(HintContext ctx) {
        TokenSequence<?> ts = ctx.getInfo().getTokenHierarchy().tokenSequence();
        if(ts==null)return null;
        ts.move((int) ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getPath().getCompilationUnit(), ctx.getPath().getLeaf()));
        if (!ts.moveNext() || ts.token().id() != JavaTokenId.MULTILINE_STRING_LITERAL) {
            return null;
        }

        String orignalString = ctx.getPath().getLeaf().toString();
        String orignalStringArr[] = textBlockToStringArr(orignalString.substring(1, orignalString.length() - 1));
        Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), orignalStringArr).toEditorFix();
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertTextBlockToString(), fix);
    }

    private static String[] textBlockToStringArr(String textBlock) {
        String oneLine[] = textBlock.split("\\\\n", -1);    // NOI18N
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
            ExpressionTree ext = buildTree(orignalStringArr, orignalStringArr.length - 1, ctx);
            ctx.getWorkingCopy().rewrite(ctx.getPath().getLeaf(), ext);

        }

        static private ExpressionTree buildTree(String textBlockLines[], int currentLine, TransformationContext ctx) {
            for (int i = 0; i < textBlockLines.length; i++) {
                textBlockLines[i] = textBlockLines[i].replaceAll("\\Q\\\"\\E", "\""); // NOI18N
                textBlockLines[i] = textBlockLines[i].replaceAll("\\Q\\\'\\E", "\'"); // NOI18N
            }
            if (textBlockLines.length == 1) {
                return ctx.getWorkingCopy().getTreeMaker().Binary(Tree.Kind.PLUS, ctx.getWorkingCopy().getTreeMaker().Literal(textBlockLines[0]), ctx.getWorkingCopy().getTreeMaker().Literal(""));// NOI18N
            }
            if (currentLine == 1) {
                if (currentLine == textBlockLines.length - 1) {
                    return ctx.getWorkingCopy().getTreeMaker().Binary(Tree.Kind.PLUS, ctx.getWorkingCopy().getTreeMaker().Literal(textBlockLines[currentLine - 1] + "\n"), ctx.getWorkingCopy().getTreeMaker().Literal(textBlockLines[currentLine]));// NOI18N
                }
                return ctx.getWorkingCopy().getTreeMaker().Binary(Tree.Kind.PLUS, ctx.getWorkingCopy().getTreeMaker().Literal(textBlockLines[currentLine - 1] + "\n"), ctx.getWorkingCopy().getTreeMaker().Literal(textBlockLines[currentLine] + "\n"));// NOI18N
            }
            if (currentLine == textBlockLines.length - 1) {
                return ctx.getWorkingCopy().getTreeMaker().Binary(Tree.Kind.PLUS, buildTree(textBlockLines, currentLine - 1, ctx), ctx.getWorkingCopy().getTreeMaker().Literal(textBlockLines[currentLine]));
            }
            return ctx.getWorkingCopy().getTreeMaker().Binary(Tree.Kind.PLUS, buildTree(textBlockLines, currentLine - 1, ctx), ctx.getWorkingCopy().getTreeMaker().Literal(textBlockLines[currentLine] + "\n"));// NOI18N
        }
    }
}
