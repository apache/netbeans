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
package org.netbeans.modules.languages.jflex.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.api.lexer.Language;

public enum JflexTokenId implements TokenId {
    KEYWORD("keyword"), // NOI18N
    OPTIONS_SEPARATOR("options_separator"), // NOI18N
    OPTION("option"), // NOI18N
    MACRO("macro"), // NOI18N
    LEXICAL_STATE("lexical_state"), // NOI18N
    DIRECTIVE_VALUE("directive_value"), // NOI18N
    REGEX("regex"), // NOI18N
    QUANTIFIER("quantifier"), // NOI18N
    NUMBER("number"), // NOI18N
    STRING("string"), // NOI18N
    COMMENT("comment"), // NOI18N
    CODE("mod-custom1"), // NOI18N
    WHITESPACE("whitespace"), // NOI18N
    OPERATOR("operator"), // NOI18N
    PUNCTUATION("punctuation"), // NOI18N
    ERROR("error");
    private final String primaryCategory;

    JflexTokenId(String category) {
        this.primaryCategory = category;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public static abstract class JflexLanguageHierarchy extends LanguageHierarchy<JflexTokenId> {

        @Override
        protected Collection<JflexTokenId> createTokenIds() {
            return EnumSet.allOf(JflexTokenId.class);
        }

        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(Token<JflexTokenId> token,
                LanguagePath languagePath, InputAttributes inputAttributes) {

            return switch (token.id()) {
                case CODE -> LanguageEmbedding.create( Language.find("text/x-java"), 0, 0, false );
                default -> null;
            };
        }
    }
}
