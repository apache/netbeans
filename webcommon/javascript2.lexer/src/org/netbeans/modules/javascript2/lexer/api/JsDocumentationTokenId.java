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
package org.netbeans.modules.javascript2.lexer.api;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.javascript2.lexer.JsDocumentationLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * List of JsDocumentation TokenIds.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public enum JsDocumentationTokenId implements TokenId {

    COMMENT_DOC_START(null, "COMMENT"),
    COMMENT_BLOCK_START(null, "COMMENT"),
    COMMENT_END(null, "COMMENT"),

    // represents one char tokens
    AT("@", "COMMENT"), //NOI18N
    ASTERISK("*", "COMMENT"), //NOI18N
    BRACKET_LEFT_BRACKET("[", "COMMENT"), //NOI18N
    BRACKET_RIGHT_BRACKET("]", "COMMENT"), //NOI18N
    BRACKET_LEFT_CURLY("{", "COMMENT"), //NOI18N
    BRACKET_RIGHT_CURLY("}", "COMMENT"), //NOI18N
    COMMA(",", "COMMENT"), //NOI18N
    EOL(null, "COMMENT"), //NOI18N

    // represents 1+ tokens
    HTML(null, "COMMENT_HTML"),
    WHITESPACE(null, "COMMENT"),
    KEYWORD(null, "COMMENT_KEYWORD"),
    UNKNOWN(null, "COMMENT"),
    OTHER(null, "COMMENT"),

    // string tokens
    STRING(null, "COMMENT"),
    STRING_BEGIN(null, "COMMENT"),
    STRING_END(null, "COMMENT");

    public static final String MIME_TYPE = "text/javascript-doc"; //NOI18N

    private final String fixedText;
    private final String primaryCategory;

    JsDocumentationTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    protected static final Language<JsDocumentationTokenId> LANGUAGE =
        new LanguageHierarchy<JsDocumentationTokenId>() {
                @Override
                protected String mimeType() {
                    return JsDocumentationTokenId.MIME_TYPE;
                }

                @Override
                protected Collection<JsDocumentationTokenId> createTokenIds() {
                    return EnumSet.allOf(JsDocumentationTokenId.class);
                }

                @Override
                protected Map<String, Collection<JsDocumentationTokenId>> createTokenCategories() {
            Map<String, Collection<JsDocumentationTokenId>> cats = new HashMap<String, Collection<JsDocumentationTokenId>>();
                    return cats;
                }

                @Override
                protected Lexer<JsDocumentationTokenId> createLexer(LexerRestartInfo<JsDocumentationTokenId> info) {
                    return JsDocumentationLexer.create(info);
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<JsDocumentationTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
                    // No embedding
                    return null;
                }
            }.language();

     public static Language<JsDocumentationTokenId> language() {
        return LANGUAGE;
    }

}
