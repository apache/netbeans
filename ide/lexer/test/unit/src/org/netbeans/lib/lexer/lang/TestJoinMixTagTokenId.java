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
 * Language that recognizes tags in the text allowing to test certain anomalies
 * such as mixing join and non-join embeddings.
 * Text of tag tokens is joined and 
 *
 * @author Miloslav Metelka
 */
public enum TestJoinMixTagTokenId implements TokenId {

    /**
     * Text enclosed in &lt;..&gt; including them. <br/>
     * But not an empty "<>" (used for embedding testing).
     * Implicit joining embedding of TestJoinMixTextTokenId.inTagLanguage.
     */
    TAG(),
    /**
     * Everything else.
     * Implicit embedding of TestPlainTokenId.inQuotesLanguage
     */
    TEXT();

    
    /**
     * Allow to control whether embedding should be returned or not (null embedding).
     * Switching allowance of embedding simulates an errorneous state
     * which the lexer framework should attempt to overcome.
     */
    private static boolean allowEmbedding = true;

    /**
     * Allow to control whether sections joining is on or off.
     * Switching allowance of sections joining simulates an errorneous state
     * which the lexer framework should attempt to overcome.
     */
    private static boolean joinSections = true;

    public static boolean isJoinSections() {
        return joinSections;
    }

    public static void setJoinSections(boolean joinSections) {
        TestJoinMixTagTokenId.joinSections = joinSections;
    }

    public static boolean isAllowEmbedding() {
        return allowEmbedding;
    }

    public static void setAllowEmbedding(boolean allowEmbedding) {
        TestJoinMixTagTokenId.allowEmbedding = allowEmbedding;
    }
    
    private TestJoinMixTagTokenId() {
    }
    
    public String primaryCategory() {
        return null;
    }

    private static final Language<TestJoinMixTagTokenId> language
    = new LanguageHierarchy<TestJoinMixTagTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-join-mix-tag";
        }

        @Override
        protected Collection<TestJoinMixTagTokenId> createTokenIds() {
            return EnumSet.allOf(TestJoinMixTagTokenId.class);
        }

        @Override
        protected Lexer<TestJoinMixTagTokenId> createLexer(LexerRestartInfo<TestJoinMixTagTokenId> info) {
            return new TestJoinMixTagLexer(info);
        }
        
        @Override
        public LanguageEmbedding<?> embedding(
        Token<TestJoinMixTagTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            if (!allowEmbedding) {
                return null;
            }
            // Test language embedding in the block comment
            switch (token.id()) {
                case TAG:
                    return LanguageEmbedding.create(TestJoinMixTextTokenId.language, 1, 1, joinSections);
                case TEXT:
                    return null;
            }
            return null; // No embedding
        }

    }.language();

    public static Language<TestJoinMixTagTokenId> language() {
        return language;
    }

}
