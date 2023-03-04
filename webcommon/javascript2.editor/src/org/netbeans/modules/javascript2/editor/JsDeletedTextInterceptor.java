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
package org.netbeans.modules.javascript2.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.EditorOptions;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.options.OptionsUtils;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;

/**
 *
 * @author Petr Hejl
 */
public class JsDeletedTextInterceptor implements DeletedTextInterceptor {

    private final Language<JsTokenId> language;

    private final boolean singleQuote;

    private final boolean comments;

    public JsDeletedTextInterceptor(Language<JsTokenId> language, boolean singleQuote, boolean comments) {
        this.language = language;
        this.singleQuote = singleQuote;
        this.comments = comments;
    }

    private boolean isInsertMatchingEnabled() {
        EditorOptions options = EditorOptions.get(language.mimeType());
        if (options != null) {
            return options.getMatchBrackets();
        }

        return true;
    }

    private boolean isSmartQuotingEnabled() {
        return OptionsUtils.forLanguage(language).autoCompletionSmartQuotes();
    }

    @Override
    public void afterRemove(Context context) throws BadLocationException {
    }

    @Override
    public boolean beforeRemove(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void cancelled(Context context) {
    }

    @Override
    public void remove(Context context) throws BadLocationException {
        // Can be replaced by LineDocument, except perhaps the getChars(), which is optimized.
        Document doc = context.getDocument();

        int dotPos = context.getOffset() - 1;
        // FIXME
        char ch = context.getText().charAt(0);
        JTextComponent target = context.getComponent();
        switch (ch) {
        case ' ': {
            if (comments) {
                // Backspacing over "// " ? Delete the "//" too!
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(
                        doc, dotPos, language);
                if (ts != null && ts.token().id() == JsTokenId.LINE_COMMENT) {
                    if (ts.offset() == dotPos - 2 && !doc.getText(dotPos, 1).equals(" ")) { //NOI18N
                        doc.remove(dotPos-2, 2);
                        target.getCaret().setDot(dotPos-2);

                        return;
                    }
                }
            }
            break;
        }

        case '{':
        case '(':
        case '[': { // and '{' via fallthrough
            if (isInsertMatchingEnabled()) {
                char tokenAtDot = LexUtilities.getTokenChar(doc, dotPos, language);

                if (((tokenAtDot == ']') &&
                        (LexUtilities.getTokenBalance(doc, JsTokenId.BRACKET_LEFT_BRACKET, JsTokenId.BRACKET_RIGHT_BRACKET, dotPos, language) != 0)) ||
                        ((tokenAtDot == ')') &&
                        (LexUtilities.getTokenBalance(doc, JsTokenId.BRACKET_LEFT_PAREN, JsTokenId.BRACKET_RIGHT_PAREN, dotPos, language) != 0)) ||
                        ((tokenAtDot == '}') &&
                        (LexUtilities.getTokenBalance(doc, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY, dotPos, language) != 0))) {
                    doc.remove(dotPos, 1);
                }
            }
            break;
        }

        case '/': {
            if (comments) {
                // Backspacing over "//" ? Delete the whole "//"
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(
                        doc, dotPos, language);
                if (ts != null && ts.token().id() == JsTokenId.REGEXP_BEGIN) {
                    if (ts.offset() == dotPos-1) {
                        doc.remove(dotPos-1, 1);
                        target.getCaret().setDot(dotPos-1);

                        return;
                    }
                } else if (ts != null && ts.token().id() == JsTokenId.STRING) {
                    // ignore deleting of '/' inside a string
                    break;
                }
            }
            // Fallthrough for match-deletion
        }
        case '\'':
            if (!singleQuote) {
                break;
            }
        case '`':
            if (!singleQuote) {
                break;
            }
        case '\"': {
            if (isSmartQuotingEnabled()) {
                CharSequence match = DocumentUtilities.getText(doc, dotPos, 1);

                if ((match != null) && (match.length() > 0) && (match.charAt(0) == ch)) {
                    doc.remove(dotPos, 1);
                }
            }
            break;
        } // TODO: Test other auto-completion chars, like %q-foo-
        default:
            break;
        }
    }


    @MimeRegistration(mimeType = JsTokenId.JAVASCRIPT_MIME_TYPE, service = DeletedTextInterceptor.Factory.class)
    public static class JsFactory implements DeletedTextInterceptor.Factory {

        @Override
        public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
            return new JsDeletedTextInterceptor(JsTokenId.javascriptLanguage(), true, true);
        }

    }

    @MimeRegistration(mimeType = JsTokenId.JSON_MIME_TYPE, service = DeletedTextInterceptor.Factory.class)
    public static class JsonFactory implements DeletedTextInterceptor.Factory {

        @Override
        public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
            return new JsDeletedTextInterceptor(JsTokenId.jsonLanguage(), false, false);
        }

    }
}
