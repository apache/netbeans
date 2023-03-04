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
 * Top level language for join sections testing.
 *
 * @author mmetelka
 */
public enum TestJoinTopTokenId implements TokenId {
    
    /**
     * Text enclosed in &lt;..&gt; including them. <br/>
     * Implicit joining embedding of TestJoinTextTokenId.inTagLanguage.
     */
    TAG(),
    /**
     * Text enclosed in {..} including '{' and '}'. <br/>
     * Implicit non-joining embedding of TestJoinTextTokenId.inBracesLanguage
     */
    BRACES(),
    /**
     * Text enclosed within back quotes `xyz` - it's used instead of regular quotes not run into
     * necessity to prefix the regular quotes by backslash e.g. when making an extract of a failing test.
     * <br/>
     * Implicit non-joining embedding of TestJoinTextTokenId.inQuotesLanguage
     */
    BACKQUOTES(),
    /**
     * Text enclosed in percents e.g. %a% - specific is that there does not need to be
     * closing percent and the end of input and the token will still be percents.
     * Implicit joining embedding of TestPlainTokenId.inPercentsLanguage
     */
    PERCENTS(),
    /**
     * Everything else.
     * Implicit embedding of TestPlainTokenId.inQuotesLanguage
     */
    TEXT();

    private TestJoinTopTokenId() {
    }
    
    public String primaryCategory() {
        return null;
    }

    private static final Language<TestJoinTopTokenId> language
    = new LanguageHierarchy<TestJoinTopTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-join-top";
        }

        @Override
        protected Collection<TestJoinTopTokenId> createTokenIds() {
            return EnumSet.allOf(TestJoinTopTokenId.class);
        }

        @Override
        protected Lexer<TestJoinTopTokenId> createLexer(LexerRestartInfo<TestJoinTopTokenId> info) {
            return new TestJoinTopLexer(info);
        }
        
        @Override
        public LanguageEmbedding<?> embedding(
        Token<TestJoinTopTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment
            switch (token.id()) {
                case TAG:
                    // Create embedding that joins the sections
                    return LanguageEmbedding.create(TestJoinTextTokenId.inTagLanguage, 1, 1, true);
                case BRACES:
                    // Embedding that does not join tokens
                    return LanguageEmbedding.create(TestJoinTextTokenId.inBracesLanguage, 1, 1, false);
                case BACKQUOTES:
                    return LanguageEmbedding.create(TestJoinTextTokenId.inBackquotesLanguage, 1, 1, false);
                case PERCENTS:
                    return LanguageEmbedding.create(TestJoinTextTokenId.inPercentsLanguage, 1, 1, true);
                case TEXT:
                    // Create embedding that joins the sections - has 0-length start/end skip lengths
                    return LanguageEmbedding.create(TestJoinTextTokenId.language, 0, 0, true);
            }
            return null; // No embedding
        }

    }.language();

    public static Language<TestJoinTopTokenId> language() {
        return language;
    }

}
