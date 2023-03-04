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
package org.netbeans.modules.languages.ini.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.ini.csl.IniLanguageConfig;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

public enum IniTokenId implements TokenId {

    INI_COMMENT("comment"), // NOI18N
    INI_SECTION_DELIM("section_delim"), // NOI18N
    INI_SECTION("section"), // NOI18N
    INI_KEY("key"), // NOI18N
    INI_EQUALS("equals"), // NOI18N
    INI_VALUE("value"), // NOI18N
    INI_WHITESPACE("whitespace"), // NOI18N
    INI_ERROR("error"); // NOI18N

    private final String name;

    private static final Language<IniTokenId> LANGUAGE = new LanguageHierarchy<IniTokenId>() {

        @Override
        protected Collection<IniTokenId> createTokenIds() {
            return EnumSet.allOf(IniTokenId.class);
        }

        @Override
        protected Lexer<IniTokenId> createLexer(LexerRestartInfo<IniTokenId> info) {
            return IniLexer.create(info);
        }

        @Override
        protected String mimeType() {
            return IniLanguageConfig.MIME_TYPE;
        }
    }.language();

    IniTokenId(String name) {
        this.name = name;
    }

    @Override
    public String primaryCategory() {
        return name;
    }

    public static Language<IniTokenId> language() {
        return LANGUAGE;
    }
}
