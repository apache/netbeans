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
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.twig.editor.lexer;

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
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

public enum TwigTopTokenId implements TokenId {
    T_TWIG_OTHER("twig_error"), //NOI18N
    T_TWIG_COMMENT("twig_comment"), //NOI18N
    T_TWIG_BLOCK_START("twig_block_delimiter"), //NOI18N
    T_TWIG_BLOCK("twig_block"), //NOI18N
    T_TWIG_BLOCK_END("twig_block_delimiter"), //NOI18N
    T_TWIG_VAR_START("twig_var_delimiter"), //NOI18N
    T_TWIG_VAR("twig_var"), //NOI18N
    T_TWIG_VAR_END("twig_var_delimiter"), //NOI18N
    T_HTML("twig_html"); //NOI18N

    private final String primaryCategory;

    TwigTopTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
    private static final Language<TwigTopTokenId> LANGUAGE =
            new LanguageHierarchy<TwigTopTokenId>() {
                @Override
                protected Collection<TwigTopTokenId> createTokenIds() {
                    return EnumSet.allOf(TwigTopTokenId.class);
                }

                @Override
                protected Map<String, Collection<TwigTopTokenId>> createTokenCategories() {
                    Map<String, Collection<TwigTopTokenId>> cats = new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<TwigTopTokenId> createLexer(LexerRestartInfo<TwigTopTokenId> info) {
                    return new TwigTopLexer(info);
                }

                @Override
                protected String mimeType() {
                    return TwigLanguage.TWIG_MIME_TYPE;
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<TwigTopTokenId> token,
                        LanguagePath languagePath, InputAttributes inputAttributes) {

                    TwigTopTokenId id = token.id();
                    if (id == T_HTML) {
                        return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                    } else if (id == T_TWIG_BLOCK) {
                        return LanguageEmbedding.create(TwigBlockTokenId.language(), 0, 0);
                    } else if (id == T_TWIG_VAR) {
                        return LanguageEmbedding.create(TwigVariableTokenId.language(), 0, 0);
                    }

                    return null;

                }
            }.language();

    public static Language<TwigTopTokenId> language() {
        return LANGUAGE;
    }
}
