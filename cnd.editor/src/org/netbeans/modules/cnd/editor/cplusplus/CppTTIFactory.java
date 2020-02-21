/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.indent.HotCharIndent;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;
import org.openide.util.Exceptions;

/**
 *
 */
@MimeRegistrations({
    // cnd source files
    @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TypedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TypedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TypedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.DOXYGEN_MIME_TYPE, service = TypedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.STRING_DOUBLE_MIME_TYPE, service = TypedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.STRING_SINGLE_MIME_TYPE, service = TypedTextInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.PREPROC_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
})
public class CppTTIFactory implements TypedTextInterceptor.Factory {

    @Override
    public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
        return new TypedTextInterceptorImpl();
    }

    private static class TypedTextInterceptorImpl implements TypedTextInterceptor {
        private CppTypingCompletion.ExtraText rawStringText = null;
        private int caretPosition;
        public TypedTextInterceptorImpl() {
        }

        @Override
        public boolean beforeInsert(Context context) throws BadLocationException {
            // reset flag
            rawStringText = null;
            caretPosition = -1;
            return false;
        }

        @Override
        public void insert(MutableContext context) throws BadLocationException {
            BaseDocument doc = (BaseDocument) context.getDocument();
            if (!BracketCompletion.completionSettingEnabled(doc)) {
                return;
            }
            rawStringText = CppTypingCompletion.checkRawStringInsertion(context);
            if (rawStringText == null) {
            char insertedChar = context.getText().charAt(0);
                switch(insertedChar) {
                case '(':
                case '[':
                case '<':
                        BracketCompletion.completeOpeningBracket(context);
                    break;
                case ')':
                case ']':
                case '>':
                        caretPosition = BracketCompletion.skipClosingBracket(context);
                    break;
                }
                // TODO: completeQuote and moveSemicolon should be moved here as well
            }
        }

        @Override
        public void afterInsert(final Context context) throws BadLocationException {
            final BaseDocument doc = (BaseDocument) context.getDocument();
            doc.runAtomicAsUser(new Runnable() {

                @Override
                public void run() {
                    if (rawStringText != null) {
                        int caretPosition = rawStringText.getCaretPosition();
                        if (caretPosition != -1) {
                            String txt = rawStringText.getExtraText();
                            if (txt != null) {
                                try {
                                    // to have correct undo we have to insert in caret position
                                    // and then in extra text position
                                    int shift = caretPosition < rawStringText.getExtraTextPostion() ? 1 : 0;
                                    doc.insertString(rawStringText.getCaretPosition(), txt, null);
                                    doc.insertString(rawStringText.getExtraTextPostion() + shift, txt, null);
                                    // put cursor after inserted text
                                    caretPosition++;
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            if (context.getOffset() != caretPosition) {
                                context.getComponent().setCaretPosition(caretPosition);
                            }
                        }
                    } else if (caretPosition != -1) {
                        context.getComponent().setCaretPosition(caretPosition);
                        caretPosition = -1;
                    } else {
                        int offset = context.getOffset();
                        String typedText = context.getText();
                        if (HotCharIndent.INSTANCE.getKeywordBasedReformatBlock(doc, offset, typedText)) {
                            Indent indent = Indent.get(doc);
                            indent.lock();
                            try {
                                doc.putProperty("abbrev-ignore-modification", Boolean.TRUE); // NOI18N
                                indent.reindent(offset);
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            } finally {
                                doc.putProperty("abbrev-ignore-modification", Boolean.FALSE); // NOI18N
                                indent.unlock();
                            }
                        } else {
                            Caret caret = context.getComponent().getCaret();
                            boolean blockCommentStart = false;
                            if (offset > 0 && typedText.charAt(0) == '*') { //NOI18N
                                TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, offset - 1, true, false);
                                if (ts != null) {
                                    // this is begin of block comment
                                    if (ts.token().id() == CppTokenId.BLOCK_COMMENT) {
                                        int offsetToken = ts.offset();
                                        CharSequence text = ts.token().text();
                                        blockCommentStart = true;
                                        // check if it's begin of line
                                        while (ts.movePrevious()) {
                                            TokenId id = ts.token().id();
                                            if (id != CppTokenId.WHITESPACE) {
                                                blockCommentStart = (id == CppTokenId.NEW_LINE) ||
                                                                    (id == CppTokenId.PREPROCESSOR_DIRECTIVE) ||
                                                                    (id == CppTokenId.ESCAPED_LINE);
                                                break;
                                            }
                                        }
                                        if (blockCommentStart) {
                                            int delta = offset - offsetToken;
                                            String[] split = text.toString().split("\n"); // NOI18N
                                            if (split.length > 0) {
                                                String s = split[0];
                                                if (!s.trim().equals("/*")) { // NOI18N
                                                    blockCommentStart = false;
                                                }
                                                if (delta >= s.length()) {
                                                    blockCommentStart = false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            try {
                                BracketCompletion.charInserted(doc, offset, caret, typedText.charAt(0), blockCommentStart);
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void cancelled(Context context) {
        }
    }
}
