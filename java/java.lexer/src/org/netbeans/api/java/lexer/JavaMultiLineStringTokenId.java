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
package org.netbeans.api.java.lexer;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.java.lexer.JavaCharacterTokenId;
import org.netbeans.lib.java.lexer.JavaLexer;
import org.netbeans.lib.java.lexer.JavaMultiLineStringLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author jlahoda
 */
public enum JavaMultiLineStringTokenId implements TokenId {

    INDENT("whitespace"),
    TEXT("text"),
    NEWLINE("whitespace");

    private final String primaryCategory;

    JavaMultiLineStringTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<JavaMultiLineStringTokenId> language = new LanguageHierarchy<JavaMultiLineStringTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-java";
        }

        @Override
        protected Collection<JavaMultiLineStringTokenId> createTokenIds() {
            return EnumSet.allOf(JavaMultiLineStringTokenId.class);
        }

        @Override
        protected Map<String,Collection<JavaMultiLineStringTokenId>> createTokenCategories() {
            return Collections.emptyMap();
        }

        @Override
        protected Lexer<JavaMultiLineStringTokenId> createLexer(LexerRestartInfo<JavaMultiLineStringTokenId> info) {
            return new JavaMultiLineStringLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<JavaMultiLineStringTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            //TODO: any embedding
            return null; // No embedding
        }
    }.language();

    public static Language<JavaMultiLineStringTokenId> language() {
        return language;
    }
}
