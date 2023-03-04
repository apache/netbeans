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

package org.netbeans.modules.php.editor.lexer;

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
 *
 * @author Petr Pisl
 */
public enum PHPTopTokenId implements TokenId {

    T_HTML (null, "phptop"),
    T_PHP (null, "php"),
    T_PHP_OPEN_DELIMITER (null, "php_delimiter"),
    T_PHP_CLOSE_DELIMITER (null, "php_delimiter");

    private final String fixedText;
    private final String primaryCategory;

    PHPTopTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<PHPTopTokenId> LANGUAGE =
            new LanguageHierarchy<PHPTopTokenId>() {

                @Override
                protected Collection<PHPTopTokenId> createTokenIds() {
                    return EnumSet.allOf(PHPTopTokenId.class);
                }

                @Override
                protected Map<String, Collection<PHPTopTokenId>> createTokenCategories() {
                    Map<String, Collection<PHPTopTokenId>> cats = new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<PHPTopTokenId> createLexer(LexerRestartInfo<PHPTopTokenId> info) {
                    return PHPTopLexer.create(info);
                }

                @Override
                protected String mimeType() {
                    return "text/PHP";
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<PHPTopTokenId> token,
                    LanguagePath languagePath, InputAttributes inputAttributes) {
                    return null; // No embedding
                }

            }.language();

    public static Language<PHPTopTokenId> language() {
        return LANGUAGE;
    }

}
