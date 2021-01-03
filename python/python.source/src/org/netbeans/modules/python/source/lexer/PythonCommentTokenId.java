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
package org.netbeans.modules.python.source.lexer;

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

public enum PythonCommentTokenId implements TokenId {
    TEXT("comment"),
    KEYWORD("comment"),
    SEPARATOR("comment"),
    TYPEKEY("comment"),
    VARNAME("comment"),
    TYPE("comment"),
    TODO("comment");
    private final String primaryCategory;

    PythonCommentTokenId() {
        this(null);
    }

    PythonCommentTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
    public static final Language<PythonCommentTokenId> language =
            new LanguageHierarchy<PythonCommentTokenId>() {
                @Override
                protected Collection<PythonCommentTokenId> createTokenIds() {
                    return EnumSet.allOf(PythonCommentTokenId.class);
                }

                @Override
                protected Map<String, Collection<PythonCommentTokenId>> createTokenCategories() {
                    return null; // no extra categories
                }

                @Override
                protected Lexer<PythonCommentTokenId> createLexer(
                        LexerRestartInfo<PythonCommentTokenId> info) {
                    return new PythonCommentLexer(info, true);
                }

                @Override
                protected LanguageEmbedding<?> embedding(
                        Token<PythonCommentTokenId> token, LanguagePath languagePath,
                        InputAttributes inputAttributes) {
                    return null; // No embedding
                }

                @Override
                public String mimeType() {
                    return "text/x-python-comment"; // NOI18N
                }
            }.language();

    public static Language<PythonCommentTokenId> language() {
        return language;
    }
}
