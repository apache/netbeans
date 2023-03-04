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
package org.netbeans.modules.languages.antlr;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.Lookup;

/**
 *
 * @author lkishalmi
 */
public enum AntlrTokenId implements TokenId {

    ACTION("action"),
    COMMENT("comment"),
    ERROR("error"),
    KEYWORD("keyword"),
    NUMBER("number"),
    PUNCTUATION("punctuation"),
    REGEXP_CHARS("regexp-chars"),
    RULE("rule"),
    STRING("string"),
    TOKEN("token"),
    WHITESPACE("whitespace");

    private final String category;

    AntlrTokenId(String category) {
        this.category = category;
    }

    @Override
    public String primaryCategory() {
        return category;
    }

    public static abstract class AntlrLanguageHierarchy extends LanguageHierarchy<AntlrTokenId> {

        @Override
        protected Collection<AntlrTokenId> createTokenIds() {
            return EnumSet.allOf(AntlrTokenId.class);
        }

        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(Token<AntlrTokenId> token,
                LanguagePath languagePath, InputAttributes inputAttributes) {
            switch (token.id()) {
                case ACTION:
                    Language<? extends TokenId> javaLanguage = null;

                    @SuppressWarnings("unchecked") Collection<LanguageProvider> providers = (Collection<LanguageProvider>) Lookup.getDefault().lookupAll(LanguageProvider.class);
                    for (LanguageProvider provider : providers) {
                        javaLanguage = (Language<? extends TokenId>) provider.findLanguage("text/x-java"); //NOI18N
                        if (javaLanguage != null) {
                            break;
                        }
                    }

                    return javaLanguage != null ? LanguageEmbedding.create(javaLanguage, 0, 0, false) : null;
                default:
                    return null;
            }
        }
    }

}
