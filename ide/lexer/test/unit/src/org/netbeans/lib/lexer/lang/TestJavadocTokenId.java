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

import org.netbeans.lib.lexer.lang.TestHTMLTagTokenId;
import org.netbeans.lib.lexer.lang.TestJavadocLexer;
import org.netbeans.lib.lexer.test.simple.*;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
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
 * Token ids for simple javadoc language 
 * - copied from JavadocTokenId.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum TestJavadocTokenId implements TokenId {

    IDENT("comment"),
    TAG("javadoc-tag"),
    HTML_TAG("html-tag"),
    DOT("comment"),
    HASH("comment"),
    OTHER_TEXT("comment");

    private final String primaryCategory;

    TestJavadocTokenId() {
        this(null);
    }

    TestJavadocTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TestJavadocTokenId> language
    = new LanguageHierarchy<TestJavadocTokenId>() {
        @Override
        protected Collection<TestJavadocTokenId> createTokenIds() {
            return EnumSet.allOf(TestJavadocTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<TestJavadocTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        protected Lexer<TestJavadocTokenId> createLexer(LexerRestartInfo<TestJavadocTokenId> info) {
            return new TestJavadocLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<TestJavadocTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch (token.id()) {
                case HTML_TAG:
                    return LanguageEmbedding.create(TestHTMLTagTokenId.language(), 0, 0);
            }
            return null; // No embedding
        }

        @Override
        protected String mimeType() {
            return "text/x-javadoc";
        }
    }.language();

    public static Language<TestJavadocTokenId> language() {
        return language;
    }

}
