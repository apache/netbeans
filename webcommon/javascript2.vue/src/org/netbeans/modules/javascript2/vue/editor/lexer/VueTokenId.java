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
package org.netbeans.modules.javascript2.vue.editor.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;

/**
 *
 * @author bogdan.haidu
 */
public enum VueTokenId implements TokenId {
    HTML("html"), // NOI18N
    CSS("css"), // NOI18N
    JAVASCRIPT_ATTR("javascript"), // NOI18N
    JAVASCRIPT("javascript"), // NOI18N
    QUOTE_ATTR("attr_quote"), // NOI18N
    VUE_DIRECTIVE("vue_directive"), // NOI18N
    VAR_TAG("var_tag"), // NOI18N
    ;
    private final String primaryCategory;

    VueTokenId(String category) {
        this.primaryCategory = category;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public static abstract class VueLanguageHierarchy extends LanguageHierarchy<VueTokenId> {

        @Override
        protected Collection<VueTokenId> createTokenIds() {
            return EnumSet.allOf(VueTokenId.class);
        }

        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(Token<VueTokenId> token,
                LanguagePath languagePath, InputAttributes inputAttributes) {

            switch (token.id()) {
                case JAVASCRIPT:
                case JAVASCRIPT_ATTR: {
                    return LanguageEmbedding.create(JsTokenId.javascriptLanguage(), 0, 0, true);
                }
                case HTML:{
                    return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                }
                case CSS:{
                    return LanguageEmbedding.create(CssTokenId.language(), 0, 0, true);
                }
                default: {
                    return null;
                }
            }
        }
    }
}