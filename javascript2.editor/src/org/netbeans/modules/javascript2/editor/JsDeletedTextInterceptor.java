/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
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
        BaseDocument doc = (BaseDocument) context.getDocument();

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
                char[] match = doc.getChars(dotPos, 1);

                if ((match != null) && (match[0] == ch)) {
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
