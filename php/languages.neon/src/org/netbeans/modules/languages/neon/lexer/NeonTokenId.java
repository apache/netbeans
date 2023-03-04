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

package org.netbeans.modules.languages.neon.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.neon.csl.NeonLanguageConfig;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public enum NeonTokenId implements TokenId {

    NEON_KEYWORD("keyword"), //NOI18N
    NEON_INTERPUNCTION("interpunction"), //NOI18N
    NEON_BLOCK("block"), //NOI18N
    NEON_VALUED_BLOCK("valuedblock"), //NOI18N
    NEON_STRING("string"), //NOI18N
    NEON_COMMENT("comment"), //NOI18N
    NEON_UNKNOWN("error"), //NOI18N
    NEON_LITERAL("literal"), //NOI18N
    NEON_VARIABLE("variable"), //NOI18N
    NEON_NUMBER("number"), //NOI18N
    NEON_REFERENCE("reference"), //NOI18N
    NEON_WHITESPACE("whitespace"); //NOI18N

    private final String name;

    private static final Language<NeonTokenId> LANGUAGE = new LanguageHierarchy<NeonTokenId>() {

        @Override
        protected Collection<NeonTokenId> createTokenIds() {
            return EnumSet.allOf(NeonTokenId.class);
        }

        @Override
        protected Lexer<NeonTokenId> createLexer(LexerRestartInfo<NeonTokenId> info) {
            return NeonLexer.create(info);
        }

        @Override
        protected String mimeType() {
            return NeonLanguageConfig.MIME_TYPE;
        }
    }.language();

    NeonTokenId(String name) {
        this.name = name;
    }

    @Override
    public String primaryCategory() {
        return name;
    }

    public static Language<NeonTokenId> language() {
        return LANGUAGE;
    }

}
