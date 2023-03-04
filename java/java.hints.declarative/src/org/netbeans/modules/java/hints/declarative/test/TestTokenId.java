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

package org.netbeans.modules.java.hints.declarative.test;

import java.util.Arrays;
import java.util.Collection;
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
 *
 * @author lahvac
 */
public enum TestTokenId implements TokenId {

    METADATA("metadata"),
    JAVA_CODE("snippet");

    public static final String MIME_TYPE = "text/x-javahintstest";
    
    private final String category;

    private TestTokenId(String category) {
        this.category = category;
    }

    @Override
    public String primaryCategory() {
        return category;
    }

    private static final Language<TestTokenId> language = new LanguageHierarchy<TestTokenId>() {
        @Override
        protected Collection<TestTokenId> createTokenIds() {
            return Arrays.asList(TestTokenId.values());
        }

        @Override
        protected Lexer<TestTokenId> createLexer(LexerRestartInfo<TestTokenId> info) {
            return new TestLexer(info);
        }

        @Override
        protected String mimeType() {
            return MIME_TYPE;
        }

        @Override
        protected LanguageEmbedding<?> embedding(Token<TestTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch (token.id()) {
                case JAVA_CODE:
                    return LanguageEmbedding.create(Language.find("text/x-java"), 0, 0);
                default:
                    return null;
            }
        }

    }.language();

    public static Language<TestTokenId> language() {
        return language;
    }
}
