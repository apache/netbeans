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
public enum LatteMarkupTokenId implements TokenId {
    T_WHITESPACE("latte-markup-whitespace"), //NOI18N
    T_MACRO_START("latte-markup-macro"), //NOI18N
    T_MACRO_END("latte-markup-macro"), //NOI18N
    T_SYMBOL("latte-markup-symbol"), //NOI18N
    T_NUMBER("latte-markup-number"), //NOI18N
    T_VARIABLE("latte-markup-variable"), //NOI18N
    T_STRING("latte-markup-string"), //NOI18N
    T_CAST("latte-markup-cast"), //NOI18N
    T_KEYWORD("latte-markup-keyword"), //NOI18N
    T_CHAR("latte-markup-char"), //NOI18N
    T_ERROR("latte-error"); //NOI18N
    private String primaryCategory;

    private LatteMarkupTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<LatteMarkupTokenId> LANGUAGE =
            new LanguageHierarchy<LatteMarkupTokenId>() {
                @Override
                protected Collection<LatteMarkupTokenId> createTokenIds() {
                    return EnumSet.allOf(LatteMarkupTokenId.class);
                }

                @Override
                protected Map<String, Collection<LatteMarkupTokenId>> createTokenCategories() {
                    Map<String, Collection<LatteMarkupTokenId>> cats = new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<LatteMarkupTokenId> createLexer(LexerRestartInfo<LatteMarkupTokenId> info) {
                    return new LatteMarkupLexer(info);
                }

                @Override
                protected String mimeType() {
                    return LatteLanguage.LATTE_MIME_TYPE + "-markup"; //NOI18N
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<LatteMarkupTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
                    return null;
                }
            }.language();

    public static Language<LatteMarkupTokenId> language() {
        return LANGUAGE;
    }

}
