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

package org.netbeans.modules.lexer.nbbridge.test.simple;

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
public enum SimpleCharTokenId implements TokenId {

    CHARACTER,
    DIGIT;

    SimpleCharTokenId() {
    }

    public String primaryCategory() {
        return "chars";
    }

    public static final String MIME_TYPE = "text/x-simple-char";
    
    private static final Language<SimpleCharTokenId> language
    = new LanguageHierarchy<SimpleCharTokenId>() {

        @Override
        protected Collection<SimpleCharTokenId> createTokenIds() {
            return EnumSet.allOf(SimpleCharTokenId.class);
        }
        
        @Override
        public Lexer<SimpleCharTokenId> createLexer(LexerRestartInfo<SimpleCharTokenId> info) {
            return new SimpleCharLexer(info);
        }

        @Override
        public LanguageEmbedding embedding(
        Token<SimpleCharTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }

        public String mimeType() {
            return MIME_TYPE;
        }
        
    }.language();

    public static Language<SimpleCharTokenId> language() {
        return language;
    }

}
