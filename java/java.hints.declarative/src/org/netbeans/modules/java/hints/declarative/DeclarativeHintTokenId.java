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

package org.netbeans.modules.java.hints.declarative;

import java.util.Collection;
import java.util.EnumSet;
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
 *
 * @author Jan Lahoda
 */
public enum DeclarativeHintTokenId implements TokenId {

    CHAR_LITERAL("character"),
    STRING_LITERAL("string"),
    COLON("operator"),
    DOUBLE_COLON("operator"),
    LEADS_TO("operator"),
    AND("operator"),
    INSTANCEOF("keyword"),
    OTHERWISE("keyword"),
    NOT("operator"),
    DOUBLE_SEMICOLON("operator"),
    DOUBLE_PERCENT("operator"),
    VARIABLE("identifier"),
    JAVA_SNIPPET("pattern"),
    WHITESPACE("whitespace"),
    LINE_COMMENT("comment"),
    BLOCK_COMMENT("comment"),
    JAVA_BLOCK("java"),
    OPTIONS("options"),
    ERROR("error");

    public static final String MIME_TYPE = "text/x-javahints";

    private final String cat;

    DeclarativeHintTokenId(String cat) {
        this.cat = cat;
    }

    @Override
    public String primaryCategory() {
        return cat;
    }

    public static Language<DeclarativeHintTokenId> language() {
        return LANGUAGE;
    }
    
    private static final Language<DeclarativeHintTokenId> LANGUAGE = new LanguageHierarchy<DeclarativeHintTokenId>() {

        @Override
        protected Collection<DeclarativeHintTokenId> createTokenIds() {
            return EnumSet.allOf(DeclarativeHintTokenId.class);
        }

        @Override
        protected Lexer<DeclarativeHintTokenId> createLexer(LexerRestartInfo<DeclarativeHintTokenId> info) {
            return new DeclarativeHintLexer(info);
        }

        @Override
        protected String mimeType() {
            return MIME_TYPE;
        }

        @Override
        protected LanguageEmbedding<?> embedding(Token<DeclarativeHintTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch (token.id()) {
                case JAVA_SNIPPET:
                    return LanguageEmbedding.create(Language.find("text/x-java"), 0, 0);
                case JAVA_BLOCK:
                    return LanguageEmbedding.create(Language.find("text/x-java"), 2, 2);
                default:
                    return null;
            }
        }

    }.language();
}
