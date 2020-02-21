/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.cnd.api.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.cnd.lexer.CppStringLexer;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for C/C++ string language
 * (embedded in C/C++ string or character literals).
 *
 * based on JavaStringTokenId
 *
 * @version 1.00
 */
public enum CppStringTokenId implements TokenId {

    TEXT(null, "string"), //NOI18N
    PREFIX_L("L", "string-escape"), // NOI18N
    // raw string
    PREFIX_R("R", "string-escape"), // NOI18N
    // unicode strings
    PREFIX_U("U", "string-escape"), // NOI18N
    PREFIX_u("u", "string-escape"), // NOI18N
    PREFIX_u8("u8", "string-escape"), // NOI18N
    // raw unicode strings
    PREFIX_LR("LR", "string-escape"), // NOI18N
    PREFIX_UR("UR", "string-escape"), // NOI18N
    PREFIX_uR("uR", "string-escape"), // NOI18N
    PREFIX_u8R("u8R", "string-escape"), // NOI18N
    
    SINGLE_QUOTE("'", "string"), // NOI18N
    FIRST_QUOTE("\"", "string-escape"), // NOI18N
    START_DELIMETER(null, "string-escape"), // NOI18N
    START_DELIMETER_PAREN("(", "string-escape"), // NOI18N
    DOUBLE_QUOTE("\"", "string"), // NOI18N
    END_DELIMETER_PAREN(")", "string-escape"), // NOI18N
    END_DELIMETER(null, "string-escape"), // NOI18N
    LAST_QUOTE("\"", "string-escape"), // NOI18N
    BELL("\\a", "string-escape"), //NOI18N
    BACKSPACE("\\b", "string-escape"), //NOI18N
    ANSI_COLOR(null, "string-escape"), //NOI18N
    FORM_FEED("\\f","string-escape"), //NOI18N
    NEWLINE("\\n","string-escape"), //NOI18N
    CR("\\r","string-escape"), //NOI18N
    TAB("\\t","string-escape"), //NOI18N
    SINGLE_QUOTE_ESCAPE("\\\"", "string-escape"), //NOI18N
    DOUBLE_QUOTE_ESCAPE("\\'", "string-escape"), //NOI18N
    BACKSLASH_ESCAPE("\\\\","string-escape"), //NOI18N
    OCTAL_ESCAPE(null, "string-escape"), //NOI18N
    OCTAL_ESCAPE_INVALID(null, "error"), //NOI18N
    HEX_ESCAPE(null, "string-escape"), //NOI18N
    HEX_ESCAPE_INVALID(null, "error"), //NOI18N
    UNICODE_ESCAPE(null, "string-escape"), //NOI18N
    UNICODE_ESCAPE_INVALID(null, "error"), //NOI18N
    ESCAPE_SEQUENCE_INVALID(null, "error"); //NOI18N

    private final String primaryCategory;
    private final String fixedText;

    CppStringTokenId(String fixedText, String primaryCategory) {
        this.primaryCategory = primaryCategory;
        this.fixedText = fixedText;
    }

    public String fixedText() {
        return fixedText;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<CppStringTokenId> languageDouble;
    private static final Language<CppStringTokenId> languageRawString;
    private static final Language<CppStringTokenId> languageSingle;

    static {
        languageDouble = new StringHierarchy(true, false).language();
        languageRawString = new StringHierarchy(true, true).language();
        languageSingle = new StringHierarchy(false, false).language();
    }

    public static Language<CppStringTokenId> languageDouble() {
        return languageDouble;
    }

    public static Language<CppStringTokenId> languageRawString() {
        return languageRawString;
    }

    public static Language<CppStringTokenId> languageSingle() {
        return languageSingle;
    }

    private static final class StringHierarchy extends LanguageHierarchy<CppStringTokenId> {
        private final boolean dblQuoted;
        private final boolean raw;
        public StringHierarchy(boolean doubleQuotedString, boolean raw) {
            this.dblQuoted = doubleQuotedString;
            this.raw = raw;
        }

        @Override
        protected Collection<CppStringTokenId> createTokenIds() {
            return EnumSet.allOf(CppStringTokenId.class);
        }

        @Override
        protected Map<String, Collection<CppStringTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        protected Lexer<CppStringTokenId> createLexer(LexerRestartInfo<CppStringTokenId> info) {
            return new CppStringLexer(info, this.dblQuoted, this.raw);
        }

        @Override
        protected String mimeType() {
            return this.dblQuoted ? MIMENames.STRING_DOUBLE_MIME_TYPE : MIMENames.STRING_SINGLE_MIME_TYPE;
        }
    }
}
