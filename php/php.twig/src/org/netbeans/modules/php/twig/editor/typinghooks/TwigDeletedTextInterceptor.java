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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
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
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;

/**
 * Based on PHP editor.
 */
public class TwigDeletedTextInterceptor implements DeletedTextInterceptor {

    private final boolean isTwig;
    private final MimePath mimePath;

    private TwigDeletedTextInterceptor(MimePath mimePath) {
        this.mimePath = mimePath;
        String path = mimePath.getPath();
        isTwig = path.contains(TwigLanguage.TWIG_MIME_TYPE);
    }

    @Override
    public boolean beforeRemove(Context context) throws BadLocationException {
        if (isTwig) {
            char ch = context.getText().charAt(0);
            if (TypingHooksUtils.isOpeningDelimiterChar(ch)) {
                BaseDocument doc = (BaseDocument) context.getDocument();
                final AtomicBoolean removed = new AtomicBoolean();
                final AtomicReference<BadLocationException> ble = new AtomicReference<>();
                doc.runAtomic(() -> {
                    BeforeRemover remover = BeforeRemoverFactory.create(ch);
                    try {
                        removed.set(remover.beforeRemove(context));
                    } catch (BadLocationException ex) {
                        ble.set(ex);
                    }
                });
                if (ble.get() != null) {
                    throw ble.get();
                }
                // if true, the further processing will be stopped
                return removed.get();
            }
        }
        return false;
    }

    @Override
    public void remove(Context context) throws BadLocationException {
        if (!isTwig) {
            return;
        }
        char c = context.getText().charAt(0);
        Remover remover = RemoverFactory.create(c);
        remover.remove(context);

    }

    @Override
    public void afterRemove(Context context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }

    private interface BeforeRemover {

        BeforeRemover NONE = (Context context) -> {
            return false;
        };

        boolean beforeRemove(Context context) throws BadLocationException;
    }

    private static final class BeforeRemoverFactory {

        public static BeforeRemover create(char c) {
            switch (c) {
                case '%':
                    return new BlockDelimiterRemover();
                case '{':
                    return new VariableDelimiterRemover();
                default:
                    assert false;
                    break;
            }
            return BeforeRemover.NONE;
        }
    }

    private abstract static class DelimiterRemover implements BeforeRemover {

        @Override
        public boolean beforeRemove(Context context) throws BadLocationException {
            if (OptionsUtils.autoCompletionSmartDelimiters()) {
                int dotPos = context.getOffset();
                Document document = context.getDocument();
                TokenSequence<? extends TwigTopTokenId> ts = TwigLexerUtils.getTwigTokenSequence(document, dotPos);
                // now support {{}} and {%%}, should also check whitespaces?
                if (ts != null) {
                    ts.move(dotPos);
                    if (ts.movePrevious()) {
                        if (ts.token().id() == getOpeningId()
                                && ts.offset() == dotPos - 2) {
                            ts.move(dotPos);
                            if (ts.moveNext()) {
                                if (ts.token().id() == getClosingId()
                                        && ts.offset() == dotPos) {
                                    document.remove(dotPos - 2, 4); // {{}} or {%%}
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        protected abstract TwigTopTokenId getOpeningId();

        protected abstract TwigTopTokenId getClosingId();
    }

    private static final class BlockDelimiterRemover extends DelimiterRemover {

        @Override
        protected TwigTopTokenId getOpeningId() {
            return TwigTopTokenId.T_TWIG_BLOCK_START;
        }

        @Override
        protected TwigTopTokenId getClosingId() {
            return TwigTopTokenId.T_TWIG_BLOCK_END;
        }

    }

    private static final class VariableDelimiterRemover extends DelimiterRemover {

        @Override
        protected TwigTopTokenId getOpeningId() {
            return TwigTopTokenId.T_TWIG_VAR_START;
        }

        @Override
        protected TwigTopTokenId getClosingId() {
            return TwigTopTokenId.T_TWIG_VAR_END;
        }

    }

    private interface Remover {

        Remover NONE = (Context context) -> {
        };

        void remove(Context context) throws BadLocationException;
    }

    private static final class RemoverFactory {

        static Remover create(char ch) {
            Remover remover;
            switch (ch) {
                case '{':
                    remover = new CurlyBracketRemover();
                    break;
                case '(':
                    remover = new RoundBracketRemover();
                    break;
                case '[':
                    remover = new SquareBracketRemover();
                    break;
                case '\"':
                    remover = new DoubleQuoteRemover();
                    break;
                case '\'':
                    remover = new SingleQuoteRemover();
                    break;
                default:
                    remover = Remover.NONE;
                    break;
            }
            return remover;
        }

    }

    private abstract static class BracketRemover implements Remover {

        @Override
        public void remove(Context context) throws BadLocationException {
            if (TypingHooksUtils.isInsertMatchingEnabled()) {
                BaseDocument doc = (BaseDocument) context.getDocument();
                int dotPos = context.getOffset() - 1;
                if (TypingHooksUtils.sameAsExistingChar(doc, getClosingBracket(), dotPos)
                        && TwigLexerUtils.getTokenBalance(doc, getOpeningBracket(), getClosingBracket(), dotPos) < 0) {
                    // opening bracket is already removed
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

    private abstract static class QuoteRemover implements Remover {

        @Override
        public void remove(Context context) throws BadLocationException {
            if (OptionsUtils.autoCompletionSmartQuotes()) {
                BaseDocument doc = (BaseDocument) context.getDocument();
                int dotPos = context.getOffset() - 1;
                TokenSequence<? extends TokenId> ts = TwigLexerUtils.getTwigMarkupTokenSequence(doc, dotPos);
                // check escape sequence
                // e.g. "\"" '\''
                if (ts != null) {
                    ts.move(dotPos);
                    if ((ts.moveNext() || ts.movePrevious())) {
                        TokenId id = ts.token().id();
                        if (id == TwigBlockTokenId.T_TWIG_STRING || id == TwigVariableTokenId.T_TWIG_STRING) {
                            if (TypingHooksUtils.isEscapeSequence(doc, dotPos)) {
                                doc.remove(dotPos - 1, 1);
                                return;
                            }
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

    @MimeRegistrations(value = {
        @MimeRegistration(mimeType = TwigHtmlEmbeddingProvider.TARGET_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
        @MimeRegistration(mimeType = TwigLanguage.TWIG_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
        @MimeRegistration(mimeType = TwigLanguage.TWIG_BLOCK_MIME_TYPE, service = DeletedTextInterceptor.Factory.class),
        @MimeRegistration(mimeType = TwigLanguage.TWIG_VARIABLE_MIME_TYPE, service = DeletedTextInterceptor.Factory.class)
    })
    public static class Factory implements DeletedTextInterceptor.Factory {

        @Override
        public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
            return new TwigDeletedTextInterceptor(mimePath);
        }

    }
}
