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

package org.netbeans.cnd.api.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.cnd.lexer.DoxygenLexer;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for doxygen language (embedded in doxygen comments).
 *
 * based on JavadocTokenId
 * @version 1.00
 */
public enum DoxygenTokenId implements TokenId {

    IDENT("comment"), // NOI18N
    TAG("doxygen-tag"), // NOI18N
    POINTER_MARK("doxygen-tag"), // NOI18N
    HTML_TAG("html-tag"), // NOI18N
    DOT("comment"), // NOI18N
    HASH("comment"), // NOI18N
    OTHER_TEXT("comment"); // NOI18N

    private final String primaryCategory;

    DoxygenTokenId() {
        this(null);
    }

    DoxygenTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<DoxygenTokenId> language;
    static {
        language = new DoxygenHierarchy().language();
    }
    
    public static Language<DoxygenTokenId> language() {
        return language;
    }

    private static final class DoxygenHierarchy extends LanguageHierarchy<DoxygenTokenId> {
        @Override
        protected Collection<DoxygenTokenId> createTokenIds() {
            return EnumSet.allOf(DoxygenTokenId.class);
        }

        @Override
        protected Lexer<DoxygenTokenId> createLexer(LexerRestartInfo<DoxygenTokenId> info) {
            return new DoxygenLexer(info);
        }

        @Override
        protected String mimeType() {
            return MIMENames.DOXYGEN_MIME_TYPE;
        }
    };    
}
