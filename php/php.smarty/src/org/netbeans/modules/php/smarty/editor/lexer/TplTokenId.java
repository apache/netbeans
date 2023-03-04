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

package org.netbeans.modules.php.smarty.editor.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
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
 * Token ids of SMARTY template language
 *
 * @author Martin Fousek
 */
public enum TplTokenId implements TokenId {

    OTHER(null, "other"),
    ERROR(null, "error"),
    PHP_VARIABLE(null, "php_variable"),
    CONFIG_VARIABLE(null, "config_variable"),
    PIPE("|", "pipe"),
    VARIABLE_MODIFIER(null, "variable_modifier"),
    WHITESPACE(null, "whitespace"),
    STRING(null, "string"),
    OPERATOR(null, "smarty_operator"),
    FUNCTION(null, "smarty_function"),
    ARGUMENT(null, "argument"),
    ARGUMENT_VALUE(null, "argument_value"),
    CHAR(null, "char");

    private final String fixedText;

    private final String primaryCategory;

    TplTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    /**
     * Return fixed text.
     * @return fixed text of command
     */
    public String fixedText() {
        return fixedText;
    }

    /**
     * Return category of command.
     * @return category of command
     */
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TplTokenId> language = new LanguageHierarchy<TplTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-tpl-inner";
        }

        @Override
        protected Collection<TplTokenId> createTokenIds() {
            return EnumSet.allOf(TplTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<TplTokenId>> createTokenCategories() {
            Map<String,Collection<TplTokenId>> cats = new HashMap<String,Collection<TplTokenId>>();
            return cats;
        }

        @Override
        protected Lexer<TplTokenId> createLexer(LexerRestartInfo<TplTokenId> info) {
            return new TplLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<TplTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {

            return null; // No embedding
        }
    }.language();

    /**
     * Return new language for TplTokenId.
     * @return language
     */
    public static Language<TplTokenId> language() {
        return language;
    }

}
