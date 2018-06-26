/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
