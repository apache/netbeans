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
package org.netbeans.modules.html.editor.xhtml;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Special top level lexer splitting the text into EL and non-EL tokens.
 *
 * @author Marek Fukala
 */
public enum XhtmlElTokenId implements TokenId {

    HTML("html"),
    EL("expression-language");

    private final String primaryCategory;
    private static Language<? extends TokenId> EL_LANGUAGE;

    XhtmlElTokenId() {
        this(null);
    }

    XhtmlElTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    // Token ids declaration
    private static final Language<XhtmlElTokenId> language = new LanguageHierarchy<XhtmlElTokenId>() {

        @Override
        protected Collection<XhtmlElTokenId> createTokenIds() {
            return EnumSet.allOf(XhtmlElTokenId.class);
        }

        @Override
        protected Map<String, Collection<XhtmlElTokenId>> createTokenCategories() {
            return null;
        }

        @Override
        protected Lexer<XhtmlElTokenId> createLexer(LexerRestartInfo<XhtmlElTokenId> info) {
            return new XhtmlElLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
                Token<XhtmlElTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch (token.id()) {
                case HTML:
                    return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                case EL:
                    //lexer infrastructure workaround - need to adjust skiplenghts in case of short token
                    int startSkipLength = token.length() > 2 ? 2 : token.length();
                    int endSkipLength = token.length() > 2 ? 1 : 0;
                    Language<? extends TokenId> elLang = getELLanguage();
                    if(elLang != null) {
                        return LanguageEmbedding.create(elLang, startSkipLength, endSkipLength);
                    }

                default:
                    return null;
            }
        }

        @Override
        protected String mimeType() {
            return "text/xhtml";
        }
    }.language();

    public static Language<XhtmlElTokenId> language() {
        return language;
    }

    private static synchronized Language<? extends TokenId> getELLanguage() {
        //keep trying to get the instance if not available - may happen during some lazy modules loading?!?!
        if (EL_LANGUAGE == null) {
            EL_LANGUAGE = Language.find("text/x-el"); //NOI18N
        }
        return EL_LANGUAGE;
    }
}

