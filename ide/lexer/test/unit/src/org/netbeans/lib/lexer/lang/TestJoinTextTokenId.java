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
public enum TestJoinTextTokenId implements TokenId {
    
    
    /**
     * Text enclosed in (..) including '(' and ')'. <br/>
     * Expicit embedding may be created (preferrably TestJoinTextTokenId.inBracesLanguage).
     */
    PARENS(),
    /**
     * Text enclosed in [..] including '[' and ']'. <br/>
     * Automatic joining embedding of TestPlainTokenId.inBracketsLanguage.
     */
    BRACKETS(),
    /**
     * Text in apostrophes including them. </br>
     * Automatic non-joining embedding of TestPlainTokenId.inApostrophesLanguage.
     */
    APOSTROPHES(),
    /**
     * All other text. <br/>
     * No embedding.
     */
    TEXT();

    private TestJoinTextTokenId() {
    }
    
    public String primaryCategory() {
        return null;
    }

    public static final Language<TestJoinTextTokenId> language
            = new LH("text/x-join-text").language();
            
    public static final Language<TestJoinTextTokenId> inTagLanguage
            = new LH("text/x-join-in-tag").language();
            
    public static final Language<TestJoinTextTokenId> inBracesLanguage
            = new LH("text/x-join-in-braces").language();
            
    public static final Language<TestJoinTextTokenId> inBackquotesLanguage
            = new LH("text/x-join-in-quotes").language();
            
    public static final Language<TestJoinTextTokenId> inPercentsLanguage
            = new LH("text/x-join-in-percents").language();
            
    private static final class LH extends LanguageHierarchy<TestJoinTextTokenId> {

        private String mimeType;
        
        LH(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        protected String mimeType() {
            return mimeType;
        }

        @Override
        protected Collection<TestJoinTextTokenId> createTokenIds() {
            return EnumSet.allOf(TestJoinTextTokenId.class);
        }

        @Override
        protected Lexer<TestJoinTextTokenId> createLexer(LexerRestartInfo<TestJoinTextTokenId> info) {
            return new TestJoinTextLexer(info);
        }
        
        @Override
        public LanguageEmbedding<?> embedding(
        Token<TestJoinTextTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment
            switch (token.id()) {
//                case PARENS: - explicit custom embedding
//                    return LanguageEmbedding.create(TestPlainTokenId.inParensLanguage, 1, 1, true);
                case BRACKETS:
                    return LanguageEmbedding.create(TestPlainTokenId.inBracketsLanguage, 1, 1, true);
                case APOSTROPHES:
                    return LanguageEmbedding.create(TestPlainTokenId.inApostrophesLanguage, 1, 1, false);
//                case TEXT:
//                    return LanguageEmbedding.create(TestStringTokenId.language(), 1, 1);
            }
            return null; // No embedding
        }

    }
}
