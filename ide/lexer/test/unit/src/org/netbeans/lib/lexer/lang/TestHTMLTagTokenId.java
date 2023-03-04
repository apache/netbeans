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
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for simple javadoc language 
 * - copied from HTMLTagTokenId.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum TestHTMLTagTokenId implements TokenId {

    LT("lt"),
    TEXT("text"),
    GT("gt");

    private final String primaryCategory;

    TestHTMLTagTokenId() {
        this(null);
    }

    TestHTMLTagTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TestHTMLTagTokenId> language = new LanguageHierarchy<TestHTMLTagTokenId>() {
        @Override
        protected Collection<TestHTMLTagTokenId> createTokenIds() {
            return EnumSet.allOf(TestHTMLTagTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<TestHTMLTagTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        protected Lexer<TestHTMLTagTokenId> createLexer(LexerRestartInfo<TestHTMLTagTokenId> info) {
            return new TestHTMLTagLexer(info);
        }

        @Override
        protected String mimeType() {
            return "text/x-test-html-tag";
        }
    }.language();

    public static Language<TestHTMLTagTokenId> language() {
        return language;
    }

}
