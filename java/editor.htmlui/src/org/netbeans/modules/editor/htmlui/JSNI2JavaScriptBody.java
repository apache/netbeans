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
package org.netbeans.modules.editor.htmlui;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.lexer.JavaTokenId;
import static org.netbeans.api.java.lexer.JavaTokenId.BLOCK_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.JAVADOC_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.LINE_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.WHITESPACE;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_JSNI2JavaScriptBody", description = "#DESC_JSNI2JavaScriptBody", category = "general")
@Messages({
    "DN_JSNI2JavaScriptBody=JSNI to @JavaScriptBody",
    "DESC_JSNI2JavaScriptBody=JSNI to @JavaScriptBody"
})
public class JSNI2JavaScriptBody {

    @TriggerTreeKind(Kind.METHOD)
    @Messages("ERR_JSNI2JavaScriptBody=Can convert JSNI to @JavaScriptBody")
    public static ErrorDescription computeWarning(final HintContext ctx) {
        Token<JavaTokenId> token = findBlockToken(ctx.getInfo(), ctx.getPath(), ctx);

        if (token == null) {
            return null;
        }

        Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix();
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_JSNI2JavaScriptBody(), fix);
    }

    private static Token<JavaTokenId> findBlockToken(CompilationInfo info, TreePath path, HintContext ctx) {
        int end = (int) info.getTrees().getSourcePositions().getEndPosition(path.getCompilationUnit(), path.getLeaf());
        TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        if (ts == null) return null;

        ts.move(end);

        if ((ctx != null && ctx.isCanceled()) || !ts.movePrevious() || ts.token().id() != JavaTokenId.SEMICOLON) return null;

        OUTER: while (ts.movePrevious()) {
            if (ctx != null && ctx.isCanceled()) return null;

            switch (ts.token().id()) {
                case WHITESPACE: break;
                case LINE_COMMENT: break;
                case JAVADOC_COMMENT: break;
                case BLOCK_COMMENT:
                    final CharSequence tok = ts.token().text();
                    final int l = tok.length();
                    if (l > 4
                        && tok.subSequence(0, 4).toString().equals("/*-{") // NOI18N
                        && tok.subSequence(l - 4, l).toString().equals("}-*/") // NOI18N
                    ) {
                        return ts.offsetToken();
                    }
                    break;
                default:
                    break OUTER;
            }
        }

        return null;
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        @Messages("FIX_JSNI2JavaScriptBody=Convert JSNI to @JavaScriptBody")
        protected String getText() {
            return Bundle.FIX_JSNI2JavaScriptBody();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            Token<JavaTokenId> jsniComment = findBlockToken(ctx.getWorkingCopy(), ctx.getPath(), null);

            if (jsniComment == null) {
                //XXX: warn?
                return ;
            }

            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            MethodTree mt = (MethodTree) ctx.getPath().getLeaf();
            List<LiteralTree> params = new ArrayList<LiteralTree>();

            for (VariableTree p : mt.getParameters()) {
                params.add(make.Literal(p.getName().toString()));
            }

            String body = jsniComment.text().toString().replace("\"", "\\\"");
            body = body.replace("/*-{", "").replace("}-*/", "");

            List<ExpressionTree> arr = new ArrayList<ExpressionTree>();
            arr.add(make.Assignment(make.Identifier("args"), make.NewArray(null, Collections.<ExpressionTree>emptyList(), params)));
            if (body.contains("@") && body.contains("::")) {
                arr.add(make.Assignment(make.Identifier("javacall"), make.Literal(true)));
            }
            final String[] lines = body.split("\n");
            StringBuilder jsB = new StringBuilder();
            String sep = "\"";
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (i < lines.length - 1) {
                    line = line + "\\n";
                }
                jsB.append(sep).append(line).append("\"");
                sep = " + \n\"";
            }
            arr.add(make.Assignment(make.Identifier("body"), make.Identifier(jsB.toString())));

            AnnotationTree jsBody = make.Annotation(make.QualIdent("net.java.html.js.JavaScriptBody"), arr);
            ctx.getWorkingCopy().rewrite(mt.getModifiers(), make.addModifiersAnnotation(mt.getModifiers(), jsBody));
        }
    }
}
