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
 * Token identifications of the simple plain language.
 *
 * @author mmetelka
 */
public enum TestPlainTokenId implements TokenId {
    
    WORD,
    WHITESPACE("whitespace");

    public static final String MIME_TYPE = "text/x-simple-plain";
    
    private final String primaryCategory;

    TestPlainTokenId() {
        this(null);
    }

    TestPlainTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    public static final Language<TestPlainTokenId> language
            = new LH(MIME_TYPE).language();

    public static final Language<TestPlainTokenId> inParensLanguage
            = new LH("text/x-join-in-parens").language();

    public static final Language<TestPlainTokenId> inBracketsLanguage
            = new LH("text/x-join-in-brackets").language();

    public static final Language<TestPlainTokenId> inApostrophesLanguage
            = new LH("text/x-join-in-apostrophes").language();

    private static final class LH extends LanguageHierarchy<TestPlainTokenId> {

        private String mimeType;

        public LH(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        protected Collection<TestPlainTokenId> createTokenIds() {
            return EnumSet.allOf(TestPlainTokenId.class);
        }
        
        @Override
        public Lexer<TestPlainTokenId> createLexer(LexerRestartInfo<TestPlainTokenId> info) {
            return new TestPlainLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<TestPlainTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }

        @Override
        protected String mimeType() {
            return mimeType;
        }
        
    }

    public static Language<TestPlainTokenId> language() {
        return language;
    }

}
