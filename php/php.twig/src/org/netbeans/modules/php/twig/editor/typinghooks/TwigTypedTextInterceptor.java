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
package org.netbeans.modules.php.twig.editor.typinghooks;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.twig.editor.embedding.TwigHtmlEmbeddingProvider;
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;
import org.netbeans.modules.php.twig.editor.lexer.TwigBlockTokenId;
import org.netbeans.modules.php.twig.editor.lexer.TwigLexerUtils;
import org.netbeans.modules.php.twig.editor.lexer.TwigTopTokenId;
import org.netbeans.modules.php.twig.editor.lexer.TwigVariableTokenId;
import org.netbeans.modules.php.twig.editor.ui.options.OptionsUtils;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

public class TwigTypedTextInterceptor implements TypedTextInterceptor {

    private final MimePath mimePath;
    private final boolean isTwig;
    private boolean codeTemplateEditing;
    private static final Logger LOGGER = Logger.getLogger(TwigTypedTextInterceptor.class.getName());

    private TwigTypedTextInterceptor(MimePath mimePath) {
        this.mimePath = mimePath;
        String path = mimePath.getPath();
        isTwig = path.contains(TwigLanguage.TWIG_MIME_TYPE);
    }

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        if (!isTwig) {
            return;
        }
        Document document = context.getDocument();
        BaseDocument doc = (BaseDocument) document;
        int caretOffset = context.getOffset();
        String selection = context.getReplacedText();
        char ch = context.getText().charAt(0);
        if (doNotAutoCompleteQuotesAndBrackets(ch) || caretOffset == 0) {
            return;
        }
        TokenSequence<? extends TokenId> ts = TwigLexerUtils.getTwigMarkupTokenSequence(doc, caretOffset);
        if (ts == null) {
            // {{}} or {%%} there is no text between delimiters
            ts = TwigLexerUtils.getTwigTokenSequence(document, caretOffset);
            if (ts == null) {
                return;
            }
        }
        ts.move(caretOffset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return;
        }

        Token<? extends TokenId> token = ts.token();
        TokenId id = token.id();
        int tokenOffset = ts.offset();
        boolean skipQuote = false;
        boolean isInString = false;

        if (id instanceof TwigBlockTokenId || id instanceof TwigVariableTokenId) {
            // complete quote or bracket
            if (isOpeningBracket(ch) || isQuote(ch)) {
                if (selection != null && selection.length() > 0) {
                    surroundSelectionWithChars(selection, ch, context);
                } else {
                    if (id != TwigBlockTokenId.T_TWIG_STRING && id != TwigVariableTokenId.T_TWIG_STRING) {
                        if (isQuote(ch)) {
                            if (TwigLexerUtils.textStartWith(token.text(), ch)) {
                                skipQuote = true;
                            } else {
                                // check backward tokens
                                skipQuote = skipQuotes(ts, ch, true);

                                // check forward tokens
                                if (!skipQuote) {
                                    ts.move(caretOffset);
                                    skipQuote = skipQuotes(ts, ch, false);
                                }
                            }
                        }
                        if (!skipQuote) {
                            completeQuoteAndBracket(context, ch);
                        }
                    } else {
                        isInString = true;
                    }
                }
            }

            // skip the same closing char
            if ((isClosingBracket(ch) || isQuote(ch))
                    && TypingHooksUtils.sameAsExistingChar(doc, ch, caretOffset)) {
                if (isInString) {
                    if (!skipQuote && isQuote(ch) && !TypingHooksUtils.isEscapeSequence(doc, caretOffset)) {
                        skipNextChar(context, ch, document, caretOffset);
                    }
                } else {
                    if (!skipQuote && !isClosingBracketMissing(doc, matching(ch), ch, caretOffset)) {
                        skipNextChar(context, ch, document, caretOffset);
                    }
                }
            }
        } else if(id == TwigTopTokenId.T_TWIG_BLOCK_END || id == TwigTopTokenId.T_TWIG_VAR_END) {
            // {{}} or {%%}
            if ((isOpeningBracket(ch) || isQuote(ch))
                    && tokenOffset == caretOffset) {
                completeQuoteAndBracket(context, ch);
            }
        }
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
        if (!isTwig) {
            return;
        }
        BaseDocument doc = (BaseDocument) context.getDocument();
        doc.runAtomicAsUser(() -> {
            try {
                afterInsertUnderWriteLock(context);
            } catch (BadLocationException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        });
    }

    @Override
    public void cancelled(Context context) {
    }

    /**
     * Surround the selected text with chars("", '', (), {}, []).
     * <b>NOTE:</b> Replace the surrounding chars if the text is already surrounded with
     * chars.
     *
     * @param selection the selected text
     * @param ch the opening bracket
     * @param context the context
     */
    private void surroundSelectionWithChars(String selection, char ch, MutableContext context) {
        char firstChar = selection.charAt(0);
        if (firstChar != ch) {
            char lastChar = selection.charAt(selection.length() - 1);
            StringBuilder sb = new StringBuilder();
            sb.append(ch);
            if (selection.length() > 1
                    && (isOpeningBracket(firstChar) || isQuote(firstChar))
                    && lastChar == matching(firstChar)) {
                String innerText = selection.substring(1, selection.length() - 1);
                sb.append(innerText);
            } else {
                sb.append(selection);
            }
            sb.append(matching(ch));
            String text = sb.toString();
            context.setText(text, text.length());
        }
    }

    /**
     * Skip the quote if there is one quote backward or forward.
     *
     * @param ts the token sequence
     * @param quote " or '
     * @param backward {@code true} if check backward tokens
     * @return {@code true} if there is one quote, otherwise {@code false}
     */
    private boolean skipQuotes(TokenSequence<? extends TokenId> ts, char quote, boolean backward) {
        boolean skip = false;
        while (backward ? ts.movePrevious() : ts.moveNext()) {
            Token<? extends TokenId> token = ts.token();
            TokenId tokenId = token.id();
            if (tokenId == TwigVariableTokenId.T_TWIG_STRING
                    || tokenId == TwigBlockTokenId.T_TWIG_STRING) {
                break;
            }
            if (tokenId == TwigVariableTokenId.T_TWIG_WHITESPACE
                    || tokenId == TwigBlockTokenId.T_TWIG_WHITESPACE
                    || tokenId == TwigVariableTokenId.T_TWIG_OTHER
                    || tokenId == TwigBlockTokenId.T_TWIG_OTHER) {
                if (token.text().toString().contains(Character.toString(quote))) {
                    skip = true;
                    break;
                }
            }
        }
        return skip;
    }

    private void skipNextChar(MutableContext context, char ch, Document document, int dotPos) throws BadLocationException {
        context.setText(Character.toString(ch), 1);
        document.remove(dotPos, 1);
    }

    private void afterInsertUnderWriteLock(Context context) throws BadLocationException {
        JTextComponent target = context.getComponent();
        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument) context.getDocument();
        int dotPos = context.getOffset();
        int tokenEndPos = dotPos + 1;
        char ch = context.getText().charAt(0);
        if (doNotAutoCompleteDelimiters(ch)
                || dotPos == 0
                || tokenEndPos > doc.getLength()) {
            return;
        }

        switch (ch) {
            case '{': // no break
            case '%':
                String mimeType = getMimeType();
                // do nothing in {% %} and {{ }}
                if (mimeType.equals(TwigLanguage.TWIG_MIME_TYPE) // in case of {{^
                        || mimeType.equals(TwigLanguage.TWIG_BLOCK_MIME_TYPE)
                        || mimeType.equals(TwigLanguage.TWIG_VARIABLE_MIME_TYPE)) {
                    return;
                }
                TokenSequence<? extends TwigTopTokenId> ts = TwigLexerUtils.getTwigTokenSequence(doc, tokenEndPos);
                if (ts == null) {
                    return;
                }
                ts.move(tokenEndPos);
                if (!ts.movePrevious() && !ts.moveNext()) {
                    return;
                }

                Token<? extends TwigTopTokenId> token = ts.token();
                TwigTopTokenId id = token.id();
                if (id == TwigTopTokenId.T_TWIG_BLOCK_START) {
                    completeOpeningDelimiter(doc, tokenEndPos, tokenEndPos + 1, caret, "  %}"); // NOI18N
                } else if (id == TwigTopTokenId.T_TWIG_VAR_START) {
                    completeOpeningDelimiter(doc, tokenEndPos, tokenEndPos + 1, caret, "  }}"); // NOI18N
                }
                break;
            default:
                break;
        }
    }

    private void completeOpeningDelimiter(Document doc, int tokenEndPos, int dotPos, Caret caret, String closingDelimiter) throws BadLocationException {
        doc.insertString(tokenEndPos, closingDelimiter, null);
        caret.setDot(dotPos);
    }

    private void completeQuoteAndBracket(MutableContext context, char bracket) throws BadLocationException {
        if (codeTemplateEditing) {
            String text = context.getText() + bracket;
            context.setText(text, text.length() - 1);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(bracket);
        sb.append(matching(bracket));
        String text = sb.toString();
        context.setText(text, 1);
    }

    private String getMimeType() {
        int size = mimePath.size();
        if (size <= 0) {
            return ""; // NOI18N
        }
        return mimePath.getMimeType(size - 1);
    }

    private static boolean isBracket(char c) {
        return isOpeningBracket(c) || isClosingBracket(c);
    }

    private static boolean isOpeningBracket(char c) {
        switch (c) {
            case '(': // no break
            case '[': // no break
            case '{':
                return true;
            default:
                return false;
        }
    }

    private static boolean isClosingBracket(char c) {
        switch (c) {
            case ')': // no break
            case ']': // no break
            case '}':
                return true;
            default:
                return false;
        }
    }

    private static boolean isQuote(char ch) {
        return ch == '"' || ch == '\'';
    }

    private static char matching(char c) {
        switch (c) {
            case '"':
                return '"';
            case '\'':
                return '\'';
            case '(':
                return ')';
            case '[':
                return ']';
            case '{':
                return '}';
            case ')':
                return '(';
            case ']':
                return '[';
            case '}':
                return '{';
            default:
                return c;
        }
    }

    private static boolean doNotAutoCompleteQuotesAndBrackets(char c) {
        return (isQuote(c) && !OptionsUtils.autoCompletionSmartQuotes())
                || (isBracket(c) && !TypingHooksUtils.isInsertMatchingEnabled());
    }

    private static boolean doNotAutoCompleteDelimiters(char c) {
        return TypingHooksUtils.isOpeningDelimiterChar(c) && !OptionsUtils.autoCompletionSmartDelimiters();
    }

    private static boolean isClosingBracketMissing(BaseDocument docment, char open, char close, int dotPos) throws BadLocationException {
        if (!isClosingBracket(close)) {
            return false;
        }
        return TwigLexerUtils.getTokenBalance(docment, open, close, dotPos) > 0;
    }

    @MimeRegistrations(value = {
        @MimeRegistration(mimeType = TwigHtmlEmbeddingProvider.TARGET_MIME_TYPE, service = TypedTextInterceptor.Factory.class),
        @MimeRegistration(mimeType = TwigLanguage.TWIG_MIME_TYPE, service = TypedTextInterceptor.Factory.class) ,
        @MimeRegistration(mimeType = TwigLanguage.TWIG_BLOCK_MIME_TYPE, service = TypedTextInterceptor.Factory.class) ,
        @MimeRegistration(mimeType = TwigLanguage.TWIG_VARIABLE_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
    })
    public static class Factory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new TwigTypedTextInterceptor(mimePath);
        }

    }

}
