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
package org.netbeans.spi.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.junit.Test;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import static org.junit.Assert.*;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Florian Vogler
 */
public class LexerInputTest {

    public LexerInputTest() {
    }

    @Test
    public void testLexerInput_ReadText_StartEnd() {
        String text = "abcdefg";
        TokenHierarchy hi = TokenHierarchy.create(text, TokenIdImpl.language());
        TokenSequence ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
    }

    private static class LexerImpl implements Lexer<TokenIdImpl> {

        private final LexerRestartInfo<TokenIdImpl> info;
        private final LexerInput input;
        private final TokenFactory<TokenIdImpl> tokenFactory;

        public LexerImpl(LexerRestartInfo<TokenIdImpl> info) {
            this.info = info;
            this.input = this.info.input();
            this.tokenFactory = info.tokenFactory();
        }

        @Override
        public Token<TokenIdImpl> nextToken() {
            int ch;
            while (true) {
                ch = input.read();
                if (ch == LexerInput.EOF) {
                    break;
                }
            }
            String text = String.valueOf(input.readText());

            String subText = text.substring(2, 4);
            assertTrue(subText.contentEquals(input.readText(2, 4)));

            return tokenFactory.createToken(TokenIdImpl.TEXT);
        }

        @Override
        public Object state() {
            return null;
        }

        @Override
        public void release() {
        }
    }

    public static enum TokenIdImpl implements TokenId {

        TEXT;

        TokenIdImpl() {
        }

        @Override
        public String primaryCategory() {
            return "text";
        }

        public static Language<TokenIdImpl> language() {
            return LanguageHierarchyImpl.INSTANCE;
        }
    }

    private static class LanguageHierarchyImpl extends LanguageHierarchy<TokenIdImpl> {

        private static final Language<TokenIdImpl> INSTANCE = new LanguageHierarchyImpl().language();
        public static final String MIME_TYPE = "text/x-LexerInput";

        @Override
        protected String mimeType() {
            return MIME_TYPE;
        }

        @Override
        protected Collection<TokenIdImpl> createTokenIds() {
            return EnumSet.allOf(TokenIdImpl.class);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
                Token<TokenIdImpl> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }

        @Override
        protected Lexer<TokenIdImpl> createLexer(LexerRestartInfo<TokenIdImpl> info) {
            return new LexerImpl(info);
        }
    }
}
