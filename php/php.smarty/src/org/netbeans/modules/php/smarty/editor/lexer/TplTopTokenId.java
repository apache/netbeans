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
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.smarty.editor.TplDataLoader;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Martin Fousek
 */
public enum TplTopTokenId implements TokenId {

    T_HTML(null, "smartytop"),
    T_SMARTY(null, "smarty"),
    T_SMARTY_CLOSE_DELIMITER(null, "smarty_delimiter"),
    T_SMARTY_OPEN_DELIMITER(null, "smarty_delimiter"),
    T_COMMENT(null, "comment"),
    T_LITERAL_DEL(null, "literal"),
    T_ERROR(null, "error"),
    T_PHP(null, "php_embedding"),
    T_PHP_DEL(null, "php_del");
    private final String fixedText;
    private final String primaryCategory;

    TplTopTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    private static final Language<TplTopTokenId> language =
            new LanguageHierarchy<TplTopTokenId>() {

                @Override
                protected Collection<TplTopTokenId> createTokenIds() {
                    return EnumSet.allOf(TplTopTokenId.class);
                }

                @Override
                protected Map<String, Collection<TplTopTokenId>> createTokenCategories() {
                    Map<String, Collection<TplTopTokenId>> cats = new HashMap<String, Collection<TplTopTokenId>>();
                    return cats;
                }

                @Override
                protected Lexer<TplTopTokenId> createLexer(LexerRestartInfo<TplTopTokenId> info) {
                    return TplTopLexer.create(info);
                }

                @Override
                protected String mimeType() {
                    return TplDataLoader.MIME_TYPE;
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<TplTopTokenId> token,
                        LanguagePath languagePath, InputAttributes inputAttributes) {
                    TplTopTokenId id = token.id();
                    if (id == T_HTML) {
                        return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                    } else if (id == T_SMARTY) {
                        return LanguageEmbedding.create(TplTokenId.language(), 0, 0, false);
                    } else if (id == T_PHP) {
                        return LanguageEmbedding.create(PHPTokenId.languageInPHP(), 0, 0, true);
                    }

                    return null; // No embedding
                }
            }.language();

    /**
     * Is returning top level language.
     * @return top level Language
     */
    public static Language<TplTopTokenId> language() {
        return language;
    }
}
