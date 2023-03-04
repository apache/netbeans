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
package org.netbeans.modules.css.prep.editor.less;

import org.netbeans.modules.css.prep.editor.CPLexer;
import org.netbeans.modules.css.prep.editor.CPTokenId;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author marekfukala
 */
public class LessLanguage extends LanguageHierarchy<CPTokenId> {

    private static Language<CPTokenId> INSTANCE;
    
    @MimeRegistration(mimeType = "text/less", service = Language.class)
    public static Language<CPTokenId> getLanguageInstance() {
        if(INSTANCE == null) {
            INSTANCE = new LessLanguage().language();
        }
        return INSTANCE;
    }
    
    @Override
    protected Collection<CPTokenId> createTokenIds() {
        return EnumSet.allOf(CPTokenId.class);
    }

    @Override
    protected Map<String, Collection<CPTokenId>> createTokenCategories() {
        return null;
    }

    @Override
    protected Lexer<CPTokenId> createLexer(LexerRestartInfo<CPTokenId> info) {
        return new CPLexer(info);
    }

    private Language getCoreCssLanguage() {
        return CssTokenId.language();
    }

    @Override
    protected LanguageEmbedding embedding(
            Token<CPTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        //there can be just one token with CssTokenId.CSS type - always create core css language embedding
        return LanguageEmbedding.create(getCoreCssLanguage(), 0, 0);
    }

    @Override
    protected String mimeType() {
        return "text/less"; //NOI18N
    }
    
}
