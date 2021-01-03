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
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

public enum PythonStringTokenId implements TokenId {
    STRING_TEXT("string"),
    STRING_ESCAPE("string-escape"),
    STRING_INVALID("string-escape-invalid"),
    URL("url"),
    EMBEDDED_PYTHON("string");
    private final String primaryCategory;

    PythonStringTokenId() {
        this(null);
    }

    PythonStringTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
    public static final Language<PythonStringTokenId> language =
            new LanguageHierarchy<PythonStringTokenId>() {
                @Override
                protected Collection<PythonStringTokenId> createTokenIds() {
                    return EnumSet.allOf(PythonStringTokenId.class);
                }

                @Override
                protected Map<String, Collection<PythonStringTokenId>> createTokenCategories() {
                    return null; // no extra categories
                }

                @Override
                protected Lexer<PythonStringTokenId> createLexer(
                        LexerRestartInfo<PythonStringTokenId> info) {
                    return new PythonStringLexer(info, true);
                }

                @Override
                protected LanguageEmbedding<?> embedding(
                        Token<PythonStringTokenId> token, LanguagePath languagePath,
                        InputAttributes inputAttributes) {
                    PythonStringTokenId id = token.id();

                    if (id == EMBEDDED_PYTHON && token.text() != null) {
                        return LanguageEmbedding.create(PythonTokenId.language(), 3, 0); // 3: Exlude ">>>" prefix
                    }

                    return null; // No embedding
                }

                @Override
                public String mimeType() {
                    return "text/x-python-string"; // NOI18N
                }
            }.language();

    public static Language<PythonStringTokenId> language() {
        return language;
    }
}
