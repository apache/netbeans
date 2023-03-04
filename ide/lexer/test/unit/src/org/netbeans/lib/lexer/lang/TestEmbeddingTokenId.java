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
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Language that changes dynamically to test Language.refresh().
 *
 * @author Miloslav Metelka
 */
public enum TestEmbeddingTokenId implements TokenId {
    
    TEXT, // any text
    A, // "a" - like always query
    C, // "c" - like cached first query
    N, // "n" - like none
    LINE_COMMENT; // "// ..."; added after change only

    private final String primaryCategory;

    TestEmbeddingTokenId() {
        this(null);
    }

    TestEmbeddingTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static Language<TestEmbeddingTokenId> language = 
        new LanguageHierarchy<TestEmbeddingTokenId>() {
            @Override
            protected Collection<TestEmbeddingTokenId> createTokenIds() {
                return EnumSet.allOf(TestEmbeddingTokenId.class);
            }

            @Override
            protected Map<String,Collection<TestEmbeddingTokenId>> createTokenCategories() {
                return null;
            }

            @Override
            protected Lexer<TestEmbeddingTokenId> createLexer(LexerRestartInfo<TestEmbeddingTokenId> info) {
                return new LexerImpl(info);
            }
            
            @Override
            protected LanguageEmbedding<?> embedding(Token<TestEmbeddingTokenId> token,
            LanguagePath languagePath, InputAttributes inputAttributes) {
                switch (token.id()) {
                    case A:
                        aEmbeddingQueryCount++;
                        return null; // Should be re-called even after returning null
                    case C:
                        cEmbeddingQueryCount++;
                        return null;
                    case N:
                        // Should never be reached due to embeddingPresence()
                        throw new IllegalStateException("Should never be reached.");
                    default:
                        return null;
                }
            }

            @Override
            protected EmbeddingPresence embeddingPresence(TestEmbeddingTokenId id) {
                switch (id) {
                    case A:
                        return EmbeddingPresence.ALWAYS_QUERY;
                    case C:
                        return EmbeddingPresence.CACHED_FIRST_QUERY;
                    case N:
                        return EmbeddingPresence.NONE;
                }
                return EmbeddingPresence.CACHED_FIRST_QUERY;
            }

            @Override
            protected String mimeType() {
                return MIME_TYPE;
            }
        }.language();


    public static Language<TestEmbeddingTokenId> language() {
        return language;
    }
    
    public static final String MIME_TYPE = "text/x-embedding";
    
    public static int aEmbeddingQueryCount;
    
    public static int cEmbeddingQueryCount;
    
    private static final class LexerImpl implements Lexer<TestEmbeddingTokenId> {
    
        // Copy of LexerInput.EOF
        private static final int EOF = LexerInput.EOF;

        private final LexerInput input;

        private final TokenFactory<TestEmbeddingTokenId> tokenFactory;

        LexerImpl(LexerRestartInfo<TestEmbeddingTokenId> info) {
            this.input = info.input();
            this.tokenFactory = info.tokenFactory();
            assert (info.state() == null); // never set to non-null value in state()
        }

        public Object state() {
            return null; // always in default state after token recognition
        }

        public Token<TestEmbeddingTokenId> nextToken() {
            while (true) {
                switch (input.read()) {
                    case '/':
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(TEXT);
                        }
                        switch (input.read()) {
                            case '/': // in single-line comment
                                while (true) {
                                    switch (input.read()) {
                                        case '\r': input.consumeNewline();
                                        case '\n':
                                        case EOF:
                                            return token(LINE_COMMENT);
                                    }
                                }
                                //break;
                        }
                        break;
                        
                    case 'a':
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(TEXT);
                        }
                        return token(A);

                    case 'c':
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(TEXT);
                        }
                        return token(C);

                    case 'n':
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(TEXT);
                        }
                        return token(N);

                    case EOF:
                        if (input.readLength() > 0) {
                            input.backup(1);
                            return token(TEXT);
                        }
                        return null;
                }
            }
        }
        
        private Token<TestEmbeddingTokenId> token(TestEmbeddingTokenId id) {
            return tokenFactory.createToken(id);
        }
        
        public void release() {
        }
    
    }
    
}
