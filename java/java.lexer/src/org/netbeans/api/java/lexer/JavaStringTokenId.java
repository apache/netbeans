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

package org.netbeans.api.java.lexer;

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
 * Token ids for java string language
 * (embedded in java string).
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum JavaStringTokenId implements TokenId {

    TEXT("string"), //NOI18N
    BACKSPACE("string-escape"), //NOI18N
    FORM_FEED("string-escape"), //NOI18N
    NEWLINE("string-escape"), //NOI18N
    CR("string-escape"), //NOI18N
    TAB("string-escape"), //NOI18N
    SINGLE_QUOTE("string-escape"), //NOI18N
    DOUBLE_QUOTE("string-escape"), //NOI18N
    BACKSLASH("string-escape"), //NOI18N
    OCTAL_ESCAPE("string-escape"), //NOI18N
    OCTAL_ESCAPE_INVALID("string-escape-invalid"), //NOI18N
    UNICODE_ESCAPE("string-escape"), //NOI18N
    UNICODE_ESCAPE_INVALID("string-escape-invalid"), //NOI18N
    ESCAPE_SEQUENCE_INVALID("string-escape-invalid"), //NOI18N
    /**
     * @since 1.58
     */
    TEMPLATE_START("string"); //NOI18N

    private final String primaryCategory;

    JavaStringTokenId() {
        this(null);
    }

    JavaStringTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<JavaStringTokenId> language = new LanguageHierarchy<JavaStringTokenId>() {
        @Override
        protected Collection<JavaStringTokenId> createTokenIds() {
            return EnumSet.allOf(JavaStringTokenId.class);
        }
        
        @Override
        protected Map<String, Collection<JavaStringTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        protected Lexer<JavaStringTokenId> createLexer(LexerRestartInfo<JavaStringTokenId> info) {
            return new JavaStringLexer<JavaStringTokenId>(info, true);
        }

        @Override
        protected String mimeType() {
            return "text/x-java-string"; //NOI18N
        }
    }.language();

    public static Language<JavaStringTokenId> language() {
        return language;
    }

}
