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
package org.netbeans.modules.php.editor.typinghooks;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.options.OptionsUtils;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PhpDeletedTextInterceptor implements DeletedTextInterceptor {

    @Override
    public boolean beforeRemove(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void remove(Context context) throws BadLocationException {
        char ch = context.getText().charAt(0);
        CharRemover charRemover = CharRemoverFactory.create(ch);
        charRemover.remove(context);
    }

    @Override
    public void afterRemove(Context context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }

    @MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = DeletedTextInterceptor.Factory.class)
    public static class Factory implements DeletedTextInterceptor.Factory {

        @Override
        public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
            return new PhpDeletedTextInterceptor();
        }
    }

    private interface CharRemover {
        CharRemover NONE = new CharRemover() {

            @Override
            public void remove(Context context) throws BadLocationException {
            }
        };

        void remove(Context context) throws BadLocationException;
    }

    private static final class CharRemoverFactory {

        static CharRemover create(char ch) {
            CharRemover charRemover = CharRemover.NONE;
            switch (ch) {
                case ' ':
                    charRemover = new SpaceRemover();
                    break;
                case '{':
                    charRemover = new CurlyBracketRemover();
                    break;
                case '(':
                    charRemover = new RoundBracketRemover();
                    break;
                case '[':
                    charRemover = new SquareBracketRemover();
                    break;
                case '\"':
                    charRemover = new DoubleQuoteRemover();
                    break;
                case '\'':
                    charRemover = new SingleQuoteRemover();
                    break;
                default:
                    //no-op
            }
            return charRemover;
        }

    }

    private static final class SpaceRemover implements CharRemover {

        @Override
        public void remove(Context context) throws BadLocationException {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int dotPos = context.getOffset() - 1;
            // Backspacing over "# " ? Delete the "#" too!
            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, dotPos);
            if (ts != null) {
                ts.move(dotPos);
                if ((ts.moveNext() || ts.movePrevious()) && (ts.offset() == dotPos - 1 && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT)) {
                    doc.remove(dotPos - 1, 1);
                    context.getComponent().getCaret().setDot(dotPos - 1);
                }
            }
        }

    }

    private abstract static class BracketRemover implements CharRemover {

        @Override
        public void remove(Context context) throws BadLocationException  {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int dotPos = context.getOffset() - 1;
            if (TypingHooksUtils.isInsertMatchingEnabled()) {
                char tokenAtDot = LexUtilities.getTokenChar(doc, dotPos);
                if (tokenAtDot == getClosingBracket() && LexUtilities.getTokenBalance(doc, getOpeningBracket(), getClosingBracket(), dotPos) != 0) {
                    doc.remove(dotPos, 1);
                }
            }
        }

        protected abstract char getOpeningBracket();

        protected abstract char getClosingBracket();

    }

    private static final class CurlyBracketRemover extends BracketRemover {

        @Override
        protected char getOpeningBracket() {
            return '{';
        }

        @Override
        protected char getClosingBracket() {
            return '}';
        }

    }

    private static final class SquareBracketRemover extends BracketRemover {

        @Override
        protected char getOpeningBracket() {
            return '[';
        }

        @Override
        protected char getClosingBracket() {
            return ']';
        }

    }

    private static final class RoundBracketRemover extends BracketRemover {

        @Override
        protected char getOpeningBracket() {
            return '(';
        }

        @Override
        protected char getClosingBracket() {
            return ')';
        }

    }

    private abstract static class QuoteRemover implements CharRemover {

        @Override
        public void remove(Context context) throws BadLocationException  {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int dotPos = context.getOffset() - 1;
            if (OptionsUtils.autoCompletionSmartQuotes()) {
                TokenSequence<? extends PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(doc, dotPos);
                if (tokenSequence != null) {
                    tokenSequence.move(dotPos);
                    if ((tokenSequence.moveNext() || tokenSequence.movePrevious())
                            && (tokenSequence.token().id() == PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE
                                || tokenSequence.token().id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING)) {
                        char[] precedingChars = doc.getChars(dotPos - 1, 1);
                        if (precedingChars.length > 0 && precedingChars[0] == '\\') {
                            doc.remove(dotPos - 1, 1);
                            return;
                        }
                    }
                }
                char[] match = doc.getChars(dotPos, 1);
                if ((match != null) && (match[0] == getQuote())) {
                    doc.remove(dotPos, 1);
                }
            }
        }

        protected abstract char getQuote();

    }

    private static final class SingleQuoteRemover extends QuoteRemover {

        @Override
        protected char getQuote() {
            return '\'';
        }

    }

    private static final class DoubleQuoteRemover extends QuoteRemover {

        @Override
        protected char getQuote() {
            return '"';
        }

    }

}
