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
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Language that changes dynamically to test Language.refresh().
 *
 * @author Miloslav Metelka
 */
public enum TestChangingTokenId implements TokenId {
    
    TEXT, // any text
    A; // "a"; added after change only

    private final String primaryCategory;

    TestChangingTokenId() {
        this(null);
    }

    TestChangingTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static boolean changed;

    private static Language<TestChangingTokenId> createLanguage() {
        return new LanguageHierarchy<TestChangingTokenId>() {
            @Override
            protected Collection<TestChangingTokenId> createTokenIds() {
                return changed
                        ? EnumSet.allOf(TestChangingTokenId.class)
                        : EnumSet.of(TEXT);
            }

            @Override
            protected Map<String,Collection<TestChangingTokenId>> createTokenCategories() {
                Map<String,Collection<TestChangingTokenId>> cats = null;
                if (changed) {
                    cats = new HashMap<String,Collection<TestChangingTokenId>>();
                    cats.put("test",EnumSet.of(A));
                }
                return cats;
            }

            @Override
            protected Lexer<TestChangingTokenId> createLexer(LexerRestartInfo<TestChangingTokenId> info) {
                return null;
            }

            @Override
            protected String mimeType() {
                return MIME_TYPE;
            }
        }.language();
    }

    private static Language<TestChangingTokenId> language;

    public static Language<TestChangingTokenId> language() {
        if (language == null)
            language = createLanguage();
        return language;
    }
    
    public static void change() {
        changed = true;
        language = null;
    }

    public static final String MIME_TYPE = "text/x-changing";
    
}
