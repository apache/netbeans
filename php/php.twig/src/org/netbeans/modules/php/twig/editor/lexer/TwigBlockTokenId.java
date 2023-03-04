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

package org.netbeans.modules.php.twig.editor.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
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

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public enum TwigBlockTokenId implements TokenId {
    T_TWIG_TAG("twig_tag"), //NOI18N
    T_TWIG_NAME("twig_name"), //NOI18N
    T_TWIG_OPERATOR("twig_operator"), //NOI18N
    T_TWIG_PUNCTUATION("twig_punctuation"), //NOI18N
    T_TWIG_NUMBER("twig_number"), //NOI18N
    T_TWIG_STRING("twig_string"), //NOI18N
    T_TWIG_INTERPOLATION_START("twig_interpolation"), //NOI18N
    T_TWIG_INTERPOLATION_END("twig_interpolation"), //NOI18N
    T_TWIG_OTHER("twig_other"), //NOI18N
    T_TWIG_WHITESPACE("twig_whitespace"); //NOI18N

    private final String primaryCategory;

    TwigBlockTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
    private static final Language<TwigBlockTokenId> LANGUAGE =
            new LanguageHierarchy<TwigBlockTokenId>() {
                @Override
                protected Collection<TwigBlockTokenId> createTokenIds() {
                    return EnumSet.allOf(TwigBlockTokenId.class);
                }

                @Override
                protected Map<String, Collection<TwigBlockTokenId>> createTokenCategories() {
                    Map<String, Collection<TwigBlockTokenId>> cats = new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<TwigBlockTokenId> createLexer(LexerRestartInfo<TwigBlockTokenId> info) {
                    return new TwigBlockLexer(info);
                }

                @Override
                protected String mimeType() {
                    return TwigLanguage.TWIG_MIME_TYPE + "-block"; // NOI18N
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<TwigBlockTokenId> token,
                        LanguagePath languagePath, InputAttributes inputAttributes) {
                    return null;
                }
            }.language();

    public static Language<TwigBlockTokenId> language() {
        return LANGUAGE;
    }
}
