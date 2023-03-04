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

package org.netbeans.modules.groovy.gsp.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.gsp.GspLanguage;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Martin Janicek
 */
public final class GspLexerLanguage extends LanguageHierarchy<GspTokenId> {

    private static Language<GspTokenId> language;

    private GspLexerLanguage() {
    }

    public static Language<GspTokenId> getLanguage() {
        if (language == null) {
            language = new GspLexerLanguage().language();
        }
        return language;
    }

    @Override
    protected Collection<GspTokenId> createTokenIds() {
        return EnumSet.allOf(GspTokenId.class);
    }

    @Override
    protected Lexer<GspTokenId> createLexer(LexerRestartInfo<GspTokenId> info) {
        return new GspLexer(info);
    }

    @Override
    protected String mimeType() {
        return GspLanguage.GSP_MIME_TYPE;
    }

    @Override
    protected Map<String, Collection<GspTokenId>> createTokenCategories() {
        return null;
    }

    @Override
    protected LanguageEmbedding<? extends TokenId> embedding(Token<GspTokenId> token,
                              LanguagePath languagePath, InputAttributes inputAttributes) {
        switch(token.id()) {
            case HTML:
                return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);

            case GSTRING_CONTENT:
            case SCRIPTLET_CONTENT:
            case SCRIPTLET_OUTPUT_VALUE_CONTENT:
                return LanguageEmbedding.create(GroovyTokenId.language(), 0, 0, false);

            default:
                return null;
        }
    }
}
