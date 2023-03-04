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
package org.netbeans.modules.html.knockout;

import org.netbeans.modules.html.knockout.api.KODataBindTokenId;
import java.util.Collection;
import java.util.EnumSet;
import java.util.logging.Level;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author marekfukala
 */
public class KODataBindLanguageHierarchy extends LanguageHierarchy<KODataBindTokenId> {

    @Override
    protected Collection<KODataBindTokenId> createTokenIds() {
        return EnumSet.allOf(KODataBindTokenId.class);
    }

    @Override
    protected Lexer<KODataBindTokenId> createLexer(LexerRestartInfo<KODataBindTokenId> info) {
        return new KODataBindLexer(info);
    }

    @Override
    protected String mimeType() {
        return KOUtils.KO_DATA_BIND_MIMETYPE;
    }

    @Override
    @SuppressWarnings("fallthrough")
    protected LanguageEmbedding embedding(
            Token<KODataBindTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        switch (token.id()) {
            case VALUE:
                Language lang = Language.find(KOUtils.JAVASCRIPT_MIMETYPE);
                if (lang != null) {
                    return LanguageEmbedding.create(lang, 0, 0, false);
                }
            default:
                return null;
        }
    }
}
