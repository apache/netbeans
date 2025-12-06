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
package org.netbeans.modules.docker.editor.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.docker.editor.DockerfileResolver;
import org.netbeans.modules.docker.editor.lexer.DockerfileTokenId;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public final class DockerfileTypedBreakInterceptor implements TypedBreakInterceptor {

    DockerfileTypedBreakInterceptor() {}

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        final BaseDocument doc = (BaseDocument) context.getDocument();
        final int offset = context.getCaretOffset();
        final int lineStart = LineDocumentUtils.getLineStartOffset(doc, offset);
        final int lineEnd = LineDocumentUtils.getLineEndOffset(doc, offset);
        if (lineStart == lineEnd) {
            //Empty line
            return;
        }
        final TokenSequence<DockerfileTokenId> seq = TokenHierarchy.get(doc).tokenSequence(DockerfileTokenId.language());
        if (seq == null) {
            return;
        }
        seq.move(offset);
        if (!seq.moveNext() && !seq.movePrevious()) {
            return;
        }
        final int tokenEnd = seq.index();
        Token<DockerfileTokenId> token;
        while ((token = seq.token()) != null && token.id() == DockerfileTokenId.WHITESPACE) {
            if (!seq.movePrevious()) {
                break;
            }
        }
        final boolean lineContinuation = isLineContinuation(token, offset-seq.offset());
        final Pair<Integer,TokenSequence<DockerfileTokenId>> p = findImportantLine(seq, doc, lineStart, tokenEnd);
        int indent;
        if (lineContinuation) {
            indent = IndentUtils.lineIndent(doc, p.first());
            if (p.second().token().id().isKeyword()) {
                indent+=IndentUtils.indentLevelSize(doc);
            }
        } else {
            token = p.second().token();
            if (token.id() == DockerfileTokenId.WHITESPACE) {
                indent = IndentUtils.lineIndent(doc, lineStart);
            } else {
                if (!token.id().isKeyword() && token.id() != DockerfileTokenId.LINE_COMMENT) {
                    int kwOffset = findPrevKeyword(p.second());
                    if (kwOffset != -1) {
                        indent = IndentUtils.lineIndent(doc, kwOffset);
                    } else {
                        indent = IndentUtils.lineIndent(doc, p.first()) - IndentUtils.indentLevelSize(doc);
                    }
                } else {
                    indent = IndentUtils.lineIndent(doc, p.first());
                }
            }
        }
        final StringBuilder sb = new StringBuilder("\n");   //NOI18N
        sb.append(IndentUtils.createIndentString(doc, indent));
        context.setText(sb.toString(), 0, sb.length());
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }

    private boolean isLineContinuation(
            @NullAllowed final Token<DockerfileTokenId> token,
            final int offsetInToken) {
        if (token == null) {
            return false;
        }
        if (token.id() == DockerfileTokenId.ESCAPE) {
            return true;
        }
        if (token.id() == DockerfileTokenId.STRING_LITERAL &&
            TokenUtilities.endsWith(TokenUtilities.trim(token.text().subSequence(0, offsetInToken)),"\\")) {  //NOI18N
            return true;
        }
        return false;
    }

    @NonNull
    private static Pair<Integer,TokenSequence<DockerfileTokenId>> findImportantLine(
            @NonNull final TokenSequence<DockerfileTokenId> seq,
            @NonNull final BaseDocument doc,
            int lineStart,
            final int tokenEnd) {
        seq.move(lineStart);
        seq.moveNext();
        final int newTokenEnd = seq.offset();
        Token<DockerfileTokenId> token;
        while (((token = seq.token()) != null && token.id() == DockerfileTokenId.WHITESPACE)) {
            if (seq.index() == tokenEnd) {
                int offset = lineStart - 1;
                if (offset < 0) {
                    return Pair.of(lineStart,seq);
                }
                lineStart = LineDocumentUtils.getLineStart(doc, offset);
                return findImportantLine(seq, doc, lineStart, newTokenEnd);
            } else {
                if (!seq.moveNext()) {
                    break;
                }
            }
        }
        return Pair.of(lineStart,seq);
    }

    private static int findPrevKeyword(
            @NonNull final TokenSequence<DockerfileTokenId> seq) {
        Token<DockerfileTokenId> token;
        while ((token = seq.token()) != null && !token.id().isKeyword()) {
            if(!seq.movePrevious()) {
                break;
            }
        }
        return token != null && token.id().isKeyword() ?
            seq.offset() :
            -1;
    }

    @MimeRegistration(mimeType = DockerfileResolver.MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
    public static final class Factory implements TypedBreakInterceptor.Factory {
        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(org.netbeans.api.editor.mimelookup.MimePath mimePath) {
            return new DockerfileTypedBreakInterceptor();
        }
    }
}
