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

package org.netbeans.lib.java.lexer;

import org.netbeans.api.java.lexer.*;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.java.lexer.JavaStringLexer;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for java character language
 * (embedded in java character literals).
 *

 */
public enum JavaCharacterTokenId implements TokenId {

    TEXT("character"), //NOI18N
    BACKSPACE("character-escape"), //NOI18N
    FORM_FEED("character-escape"), //NOI18N
    NEWLINE("character-escape"), //NOI18N
    CR("character-escape"), //NOI18N
    TAB("character-escape"), //NOI18N
    SINGLE_QUOTE("character-escape"), //NOI18N
    DOUBLE_QUOTE("character-escape"), //NOI18N
    BACKSLASH("character-escape"), //NOI18N
    OCTAL_ESCAPE("character-escape"), //NOI18N
    OCTAL_ESCAPE_INVALID("character-escape-invalid"), //NOI18N
    UNICODE_ESCAPE("character-escape"), //NOI18N
    UNICODE_ESCAPE_INVALID("character-escape-invalid"), //NOI18N
    ESCAPE_SEQUENCE_INVALID("character-escape-invalid"), //NOI18N
    /**
     * @since 1.58
     */
    TEMPLATE_START("character"); //NOI18N

    private final String primaryCategory;

    JavaCharacterTokenId() {
        this(null);
    }

    JavaCharacterTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<JavaCharacterTokenId> language = new LanguageHierarchy<JavaCharacterTokenId>() {
        @Override
        protected Collection<JavaCharacterTokenId> createTokenIds() {
            return EnumSet.allOf(JavaCharacterTokenId.class);
        }
        
        @Override
        protected Map<String, Collection<JavaCharacterTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        protected Lexer<JavaCharacterTokenId> createLexer(LexerRestartInfo<JavaCharacterTokenId> info) {
            return new JavaStringLexer<JavaCharacterTokenId>(info, false);
        }

        @Override
        protected String mimeType() {
            return "text/x-java-character"; //NOI18N
        }
    }.language();

    public static Language<JavaCharacterTokenId> language() {
        return language;
    }

}
