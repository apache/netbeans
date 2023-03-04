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
package org.netbeans.modules.php.latte.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.php.latte.csl.LatteLanguage;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public enum LatteTopTokenId implements TokenId {
    T_HTML("html"), //NOI18N
    T_LATTE("latte-markup"), //NOI18N
    T_LATTE_ERROR("latte-error"), //NOI18N
    T_LATTE_OPEN_DELIMITER("latte-delimiter"), //NOI18N
    T_LATTE_CLOSE_DELIMITER("latte-delimiter"), //NOI18N
    T_LATTE_COMMENT("latte-comment"), //NOI18N
    T_LATTE_COMMENT_DELIMITER("latte-comment"); //NOI18N
    private String primaryCategory;

    private LatteTopTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<LatteTopTokenId> LANGUAGE =
            new LanguageHierarchy<LatteTopTokenId>() {
                @Override
                protected Collection<LatteTopTokenId> createTokenIds() {
                    return EnumSet.allOf(LatteTopTokenId.class);
                }

                @Override
                protected Map<String, Collection<LatteTopTokenId>> createTokenCategories() {
                    Map<String, Collection<LatteTopTokenId>> cats = new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<LatteTopTokenId> createLexer(LexerRestartInfo<LatteTopTokenId> info) {
                    return new LatteTopLexer(info);
                }

                @Override
                protected String mimeType() {
                    return LatteLanguage.LATTE_MIME_TYPE;
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<LatteTopTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
                    LanguageEmbedding<?> result = null;
                    LatteTopTokenId tokenId = token.id();
                    if (tokenId == LatteTopTokenId.T_HTML)  {
                        result = LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                    } else if (tokenId == LatteTopTokenId.T_LATTE) {
                        result = LanguageEmbedding.create(LatteMarkupTokenId.language(), 0, 0);
                    }
                    return result;
                }
            }.language();

    public static Language<LatteTopTokenId> language() {
        return LANGUAGE;
    }

}
