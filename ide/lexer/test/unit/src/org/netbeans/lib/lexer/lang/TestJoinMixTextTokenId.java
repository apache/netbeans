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
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Embedded language for join sections testing.
 *
 * @author mmetelka
 */
public enum TestJoinMixTextTokenId implements TokenId {
    
    /**
     * Word separated by whitespace.
     */
    WORD(),
    /**
     * Whitespace between words.
     */
    WHITESPACE();

    private TestJoinMixTextTokenId() {
    }
    
    public String primaryCategory() {
        return null;
    }

    public static final Language<TestJoinMixTextTokenId> language
            = new LH("text/x-join-text").language();
            
    private static final class LH extends LanguageHierarchy<TestJoinMixTextTokenId> {

        private String mimeType;
        
        LH(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        protected String mimeType() {
            return mimeType;
        }

        @Override
        protected Collection<TestJoinMixTextTokenId> createTokenIds() {
            return EnumSet.allOf(TestJoinMixTextTokenId.class);
        }

        @Override
        protected Lexer<TestJoinMixTextTokenId> createLexer(LexerRestartInfo<TestJoinMixTextTokenId> info) {
            return new TestJoinMixTextLexer(info);
        }
        
        @Override
        public LanguageEmbedding<?> embedding(
        Token<TestJoinMixTextTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment
            switch (token.id()) {
                case WORD:
                    return null;
                case WHITESPACE:
                    return null;
            }
            return null; // No embedding
        }

    }
}
