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
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for simple string embedding - copied from JavaStringTokenId
 * for java string language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum TestStringTokenId implements TokenId {

    TEXT("string"),
    BACKSPACE("string-escape"),
    FORM_FEED("string-escape"),
    NEWLINE("string-escape"),
    CR("string-escape"),
    TAB("string-escape"),
    SINGLE_QUOTE("string-escape"),
    DOUBLE_QUOTE("string-escape"),
    BACKSLASH("string-escape"),
    OCTAL_ESCAPE("string-escape"),
    OCTAL_ESCAPE_INVALID("string-escape-invalid"),
    ESCAPE_SEQUENCE_INVALID("string-escape-invalid");

    private final String primaryCategory;

    TestStringTokenId() {
        this(null);
    }

    TestStringTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TestStringTokenId> language
    = new LanguageHierarchy<TestStringTokenId>() {
        @Override
        protected Collection<TestStringTokenId> createTokenIds() {
            return EnumSet.allOf(TestStringTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<TestStringTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        public Lexer<TestStringTokenId> createLexer(LexerRestartInfo<TestStringTokenId> info) {
            return new TestStringLexer(info);
        }

        @Override
        public LanguageEmbedding<?> embedding(
        Token<TestStringTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }
        
        @Override
        protected String mimeType() {
            return "text/x-simple-string";
        }
    }.language();

    public static Language<TestStringTokenId> language() {
        return language;
    }

    public void release() {
    }

}
