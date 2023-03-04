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

package org.netbeans.lib.lexer.test.dump;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * @author mmetelka
 */
public enum TextAsSingleTokenTokenId implements TokenId {

    /** Token covering whole input till the end. */
    TEXT(null);

    private String primaryCategory;

    private TextAsSingleTokenTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }
    
    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static final Language<TextAsSingleTokenTokenId> lang = new LanguageHierarchy<TextAsSingleTokenTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-eof-mark";
        }

        @Override
        protected Collection<TextAsSingleTokenTokenId> createTokenIds() {
            return EnumSet.allOf(TextAsSingleTokenTokenId.class);
        }

        @Override
        protected Lexer<TextAsSingleTokenTokenId> createLexer(LexerRestartInfo<TextAsSingleTokenTokenId> info) {
            return new TextAsSingleTokenLexer(info);
        }
        
    }.language();
    
    public static Language<TextAsSingleTokenTokenId> language() {
        return lang;
    }
    
}
