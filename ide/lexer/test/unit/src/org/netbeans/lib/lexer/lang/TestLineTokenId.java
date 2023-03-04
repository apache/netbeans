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

package org.netbeans.lib.lexer.lang;

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
 * Token identifications of the simple plain language.
 *
 * @author mmetelka
 */
public enum TestLineTokenId implements TokenId {

    LINE;

    TestLineTokenId() {
    }

    public String primaryCategory() {
        return "text";
    }

    public static final String MIME_TYPE = "text/x-line";
    
    private static final Language<TestLineTokenId> language
    = new LanguageHierarchy<TestLineTokenId>() {

        @Override
        protected Collection<TestLineTokenId> createTokenIds() {
            return EnumSet.allOf(TestLineTokenId.class);
        }
        
        @Override
        protected Lexer<TestLineTokenId> createLexer(LexerRestartInfo<TestLineTokenId> info) {
            return new TestLineLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<TestLineTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }

        @Override
        protected String mimeType() {
            return MIME_TYPE;
        }
        
    }.language();

    public static Language<TestLineTokenId> language() {
        return language;
    }

}
