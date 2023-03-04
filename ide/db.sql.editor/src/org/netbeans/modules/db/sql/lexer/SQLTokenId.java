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

package org.netbeans.modules.db.sql.lexer;

import java.util.Collection;
import java.util.Collections;
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
 *
 * @author Andrei Badea
 */
public enum SQLTokenId implements TokenId {
    WHITESPACE("sql-whitespace"), // NOI18N
    LINE_COMMENT("sql-line-comment"), // NOI18N
    BLOCK_COMMENT("sql-block-comment"), // NOI18N
    STRING("sql-string-literal"), // NOI18N
    INCOMPLETE_STRING("sql-errors"), // NOI18N
    INCOMPLETE_IDENTIFIER("sql-errors"), // NOI18N
    IDENTIFIER("sql-identifier"), // NOI18N
    OPERATOR("sql-operator"), // NOI18N
    LPAREN("sql-operator"), // NOI18N
    RPAREN("sql-operator"), // NOI18N
    DOT("sql-dot"), // NOI18N
    COMMA("sql-operator"), //  // NOI18N XXX or have own category?
    INT_LITERAL("sql-int-literal"),  // NOI18N
    DOUBLE_LITERAL("sql-double-literal"), // NOI18N
    KEYWORD("sql-keyword"); // NOI18N

    private final String primaryCategory;

    private SQLTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;

    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<SQLTokenId> language = new LanguageHierarchy<SQLTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-sql"; // NOI18N
        }

        @Override
        protected Collection<SQLTokenId> createTokenIds() {
            return EnumSet.allOf(SQLTokenId.class);
        }

        @Override
        protected Map<String,Collection<SQLTokenId>> createTokenCategories() {
            // XXX what comes here?
            return Collections.emptyMap();
        }

        @Override
        protected Lexer<SQLTokenId> createLexer(LexerRestartInfo<SQLTokenId> info) {
            return new SQLLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<SQLTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null;
        }
    }.language();

    public static Language<SQLTokenId> language() {
        return language;
    }
}
