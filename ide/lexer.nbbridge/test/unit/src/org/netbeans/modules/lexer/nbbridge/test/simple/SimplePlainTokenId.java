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
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token identifications of the simple plain language.
 *
 * @author mmetelka
 */
public enum SimplePlainTokenId implements TokenId {

    WORD,
    WHITESPACE("whitespace");

    private final String primaryCategory;

    SimplePlainTokenId() {
        this(null);
    }

    SimplePlainTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static final Language<SimplePlainTokenId> language = new LanguageHierarchy<SimplePlainTokenId>() {

        @Override
        protected Collection<SimplePlainTokenId> createTokenIds() {
            return EnumSet.allOf(SimplePlainTokenId.class);
        }
        
        @Override
        protected Lexer<SimplePlainTokenId> createLexer(LexerRestartInfo<SimplePlainTokenId> info) {
            return new SimplePlainLexer(info);
        }
        
        @Override
        protected String mimeType() {
            return "text/x-simple-plain";
        }
        
    }.language();
    
    public static Language<SimplePlainTokenId> language() {
        return language;
    }


}
