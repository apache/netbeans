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
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids for PHP Documentor Comments.
 *
 * @author Petr Pisl
 */
public enum PHPDocCommentTokenId implements TokenId {
    PHPDOC_COMMENT(null, "comment"), //NOI18N
    PHPDOC_HTML_TAG(null, "htmltag"), //NOI18N
    PHPDOC_ANNOTATION(null, "phpdockeyword"); //NOI18N

    public static final String MIME_TYPE = "text/x-php-doccomment";
    private final String fixedText;
    private final String primaryCategory;

    PHPDocCommentTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    private static final Language<PHPDocCommentTokenId> LANGUAGE =
        new LanguageHierarchy<PHPDocCommentTokenId>() {
                @Override
                protected Collection<PHPDocCommentTokenId> createTokenIds() {
                    return EnumSet.allOf(PHPDocCommentTokenId.class);
                }

                @Override
                protected Map<String, Collection<PHPDocCommentTokenId>> createTokenCategories() {
                    return null; // no extra categories
                }

                @Override
                protected Lexer<PHPDocCommentTokenId> createLexer(
                    LexerRestartInfo<PHPDocCommentTokenId> info) {
                    return new PHPDocCommentLexer(info);
                }

                @Override
                public String mimeType() {
                    return MIME_TYPE;
                }
            }.language();

    public static Language<PHPDocCommentTokenId> language() {
        return LANGUAGE;
    }
}
