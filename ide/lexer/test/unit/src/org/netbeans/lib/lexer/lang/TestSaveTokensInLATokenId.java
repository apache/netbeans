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

package org.netbeans.lib.lexer.lang;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Token identifications of the simple plain language.
 *
 * @author mmetelka
 */
public enum TestSaveTokensInLATokenId implements TokenId {
    
    A, // matches "a"; also checks for "b" and "c"
    B, // matches "b"; also checks for "c"
    C, // matches "c"
    TEXT; // other text

    TestSaveTokensInLATokenId() {
    }

    public String primaryCategory() {
        return null;
    }

    private  static final Language<TestSaveTokensInLATokenId> language
    = new LanguageHierarchy<TestSaveTokensInLATokenId>() {

        @Override
        protected Collection<TestSaveTokensInLATokenId> createTokenIds() {
            return EnumSet.allOf(TestSaveTokensInLATokenId.class);
        }
        
        @Override
        public Lexer<TestSaveTokensInLATokenId> createLexer(LexerRestartInfo<TestSaveTokensInLATokenId> info) {
            return new LexerImpl(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<TestSaveTokensInLATokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }

        @Override
        protected String mimeType() {
            return "text/x-simple-plain";
        }
        
    }.language();

    public static Language<TestSaveTokensInLATokenId> language() {
        return language;
    }

    private static final class LexerImpl implements Lexer<TestSaveTokensInLATokenId> {

        private static final int EOF = LexerInput.EOF;

        private LexerInput input;

        private TokenFactory<TestSaveTokensInLATokenId> tokenFactory;

        public LexerImpl(LexerRestartInfo<TestSaveTokensInLATokenId> info) {
            this.input = info.input();
            this.tokenFactory = info.tokenFactory();
            assert (info.state() == null); // passed argument always null
        }

        public Object state() {
            return null;
        }

        public Token<TestSaveTokensInLATokenId> nextToken() {
            int ch = input.read();
            switch (ch) {
                case 'a': // check for 'b' and 'c'
                    if (input.read() == 'b') {
                        if (input.read() == 'c') { // just check for "c"
                        }
                        input.backup(1);
                    }
                    input.backup(1);
                    return tokenFactory.createToken(A);

                case 'b':
                    if (input.read() == 'c') { // just check for "c"
                    }
                    input.backup(1);
                    return tokenFactory.createToken(B);
                    
                case 'c':
                    return tokenFactory.createToken(C);

                case EOF:
                    return null;

                default:
                    return tokenFactory.createToken(TEXT);
            }
        }

        private Token<TestSaveTokensInLATokenId> token(TestSaveTokensInLATokenId id) {
            return tokenFactory.createToken(id);
        }

        public void release() {
        }

    }

}
