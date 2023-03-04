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
package org.netbeans.modules.languages.toml;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author lkishalmi
 */
public enum TomlTokenId implements TokenId {

    COMMENT("comment"),
    ERROR("error"),
    KEY("key"),
    BOOLEAN("boolean"),
    DATE("date"),
    NUMBER("number"),
    EQUALS("operator"),
    DOT("separator"),
    TABLE_MARK("table-mark"),
    STRING("string"),
    STRING_QUOTE("string"),
    ESCAPE_SEQUENCE("string-escape"),
    WHITESPACE("whitespace");

    private final String category;

    TomlTokenId(String category) {
        this.category = category;
    }

    @Override
    public String primaryCategory() {
        return category;
    }

    private static final Language<TomlTokenId> LANGUAGE = new LanguageHierarchy<TomlTokenId>() {
        @Override
        protected Collection<TomlTokenId> createTokenIds() {
            return EnumSet.allOf(TomlTokenId.class);
        }

        @Override
        protected Lexer<TomlTokenId> createLexer(LexerRestartInfo<TomlTokenId> info) {
            return new TomlLexer(info);
        }

        @Override
        protected String mimeType() {
            return TomlTokenId.TOML_MIME_TYPE;
        }
    }.language();

    @MimeRegistration(mimeType = TOML_MIME_TYPE, service = Language.class)
    public static final Language<?> language() {
        return LANGUAGE;
    }

    public static final String TOML_MIME_TYPE = "text/x-toml"; // NOI18N
}

