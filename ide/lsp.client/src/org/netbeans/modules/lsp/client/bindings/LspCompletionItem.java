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
package org.netbeans.modules.lsp.client.bindings;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.eclipse.lsp4j.InsertReplaceEdit;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.util.Exceptions;

final class LspCompletionItem extends AbstractCompletionItem<Either<TextEdit, InsertReplaceEdit>> {
    private final org.eclipse.lsp4j.CompletionItem i;
    private final LSPBindings server;
    private final AsyncCompletionQuery outer;
    private final CompletionProviderImpl cp;

    public LspCompletionItem(
        org.eclipse.lsp4j.CompletionItem i,
        Document doc, int caretOffset, LSPBindings server,
        String leftLabel, String rightLabel, ImageIcon icon, String sortText,
        String insert,
        AsyncCompletionQuery outer, CompletionProviderImpl cp
    ) {
        super(doc, caretOffset, leftLabel, rightLabel, icon, sortText, insert);
        this.cp = cp;
        this.outer = outer;
        this.i = i;
        this.server = server;
    }

    @Override
    Either<TextEdit, InsertReplaceEdit> findEdit(boolean[] hideImmediately) {
        Either<TextEdit, InsertReplaceEdit> edit = i.getTextEdit();
        if (edit != null && edit.isRight()) {
            //TODO: the NetBeans client does not currently support InsertReplaceEdits, should not happen
            hideImmediately[0] = true;
            return null;
        }
        return edit;
    }

    @Override
    boolean isTextEdit(Either<TextEdit, InsertReplaceEdit> edit) {
        return edit != null && edit.getLeft() != null;
    }

    @Override
    int findStart(Document doc, Either<TextEdit, InsertReplaceEdit> te) {
        return Utils.getOffset(doc, te.getLeft().getRange().getStart());
    }

    @Override
    int findEnd(Document doc, Either<TextEdit, InsertReplaceEdit> te) {
        return Utils.getOffset(doc, te.getLeft().getRange().getEnd());
    }

    @Override
    String findNewText(Either<TextEdit, InsertReplaceEdit> te) {
        return te.getLeft().getNewText();
    }

    @Override
    List<String> getCommitCharacters() {
        return i.getCommitCharacters();
    }

    @Override
    boolean isTriggerCharacter(String commitText) {
        return cp.isTriggerCharacter(server, commitText);
    }

    @Override
    Supplier<String[]> resolveDocumentation(Document doc, int caretOffset) {
        org.eclipse.lsp4j.CompletionItem resolved;
        if ((i.getDetail() == null || i.getDocumentation() == null) && cp.hasCompletionResolve(server)) {
            org.eclipse.lsp4j.CompletionItem temp;
            try {
                temp = server.getTextDocumentService().resolveCompletionItem(i).get();
            } catch (InterruptedException | ExecutionException ex) {
                Exceptions.printStackTrace(ex);
                temp = i;
            }
            resolved = temp;
        } else {
            resolved = i;
        }
        if (resolved.getDocumentation() != null || resolved.getDetail() != null) {
            return () -> {
                MarkupContent content;
                if (resolved.getDocumentation().isLeft()) {
                    content = new MarkupContent();
                    content.setKind("plaintext");
                    content.setValue(resolved.getDocumentation().getLeft());
                } else {
                    content = resolved.getDocumentation().getRight();
                }
                String txt;
                switch (content.getKind()) {
                    case "markdown":
                        txt = HtmlRenderer.builder().build().render(Parser.builder().build().parse(content.getValue()));
                        break;
                    default:
                        txt = "<pre>\n" + content.getValue() + "\n</pre>";
                        break;
                }
                return new String[] { resolved.getDetail(), txt };
            };
        } else {
            return null;
        }
    }
}
