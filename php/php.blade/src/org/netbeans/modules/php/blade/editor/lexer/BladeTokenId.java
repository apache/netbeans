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
package org.netbeans.modules.php.blade.editor.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.WeakHashMap;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.util.Lookup;

/**
 *
 * @author bogdan
 */
public enum BladeTokenId implements TokenId {
    BLADE_COMMENT_START("blade_comment"), // NOI18N
    BLADE_COMMENT("blade_comment"), // NOI18N
    BLADE_COMMENT_END("blade_comment"), // NOI18N
    BLADE_DIRECTIVE("blade_directive"), // NOI18N
    BLADE_CUSTOM_DIRECTIVE("blade_directive"), // NOI18N
    BLADE_ECHO_DELIMITOR("blade_echo_delimiters"), // NOI18N
    BLADE_TAG_ERROR("html"), // NOI18N
    BLADE_PAREN("token"), // NOI18N
    BLADE_COMPONENT_ATTRIBUTE("blade_comp_attribute"), // NOI18N
    HTML("html"), // NOI18N
    WS_D("html"), // NOI18N
    BLADE_DIRECTIVE_UNKNOWN("at_string"), // NOI18N
    PHP_BLADE_EXPRESSION("blade_php"), // NOI18N
    PHP_BLADE_ECHO_EXPR("blade_php"), // NOI18N
    PHP_BLADE_INLINE_CODE("blade_php"), // NOI18N
    PHP_INLINE("php"), // NOI18N
    OTHER("error"); // NOI18N
    private final String category;

    BladeTokenId(String category) {
        this.category = category;
    }

    @Override
    public String primaryCategory() {
        return category;
    }

    public static abstract class BladeLanguageHierarchy extends LanguageHierarchy<BladeTokenId> {

        @Override
        protected Collection<BladeTokenId> createTokenIds() {
            return EnumSet.allOf(BladeTokenId.class);
        }

        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(Token<BladeTokenId> token,
                LanguagePath languagePath, InputAttributes inputAttributes) {

            switch (token.id()) {
                case PHP_BLADE_INLINE_CODE:
                case PHP_BLADE_EXPRESSION: {
                    Language<? extends TokenId> phpLanguage = PHPTokenId.languageInPHP();
                    if (phpLanguage == null || token.text() == null) {
                        return null;
                    }

                    //php brace matcher freeze issue patch
                    String tokenText = token.text().toString();

                    //php brace matcher freeze issue patch
                    OffsetPair offsetPair = computeEmebeddingOffsets(tokenText);
                    
                    return LanguageEmbedding.create(phpLanguage, offsetPair.start, offsetPair.end, false);
                }
                case PHP_BLADE_ECHO_EXPR: {
                    Language<? extends TokenId> phpLanguage = PHPTokenId.languageInPHP();
                    if (phpLanguage == null || token.text() == null) {
                        return null;
                    }
                    String tokenText = token.text().toString();
                      
                    //php brace matcher freeze issue patch
                    OffsetPair offsetPair = computeEmebeddingOffsets(tokenText);
                    
                    return LanguageEmbedding.create(phpLanguage, offsetPair.start, offsetPair.end, false);
                }
                case PHP_INLINE: {
                    Language<? extends TokenId> phpLanguageCode = PHPTokenId.language();
                    return phpLanguageCode != null ? LanguageEmbedding.create(phpLanguageCode, 0, 0, false) : null;
                }
                case HTML: {
                    return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                }
                default: {
                    return null;
                }
            }
        }
    }

    private static class OffsetPair {

        public final int start;
        public final int end;

        public OffsetPair(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * patch for avoiding php brace matcher freeze issue
     * https://github.com/apache/netbeans/issues/7803
     * 
     * create a offset for the php embedding content to exclude wrapping brackets and parenthesis
     * 
     * @param tokenText
     * @return 
     */
    private static OffsetPair computeEmebeddingOffsets(String tokenText) {
        int startOffset = 0;
        int endOffset = 0;
        
        if (!tokenText.startsWith("(") && !tokenText.startsWith("[")) { //NOI18N
            return new OffsetPair(startOffset, endOffset);
        }

        if (tokenText.startsWith("((") && tokenText.endsWith("))")) { //NOI18N
            startOffset = 2;
            endOffset = 2;
        } else if (tokenText.startsWith("[[") && tokenText.endsWith("]]")) { //NOI18N
            startOffset = 2;
            endOffset = 2;
        } else if (tokenText.startsWith("([") && tokenText.endsWith("])")) { //NOI18N
            startOffset = 2;
            endOffset = 2;
        } else if (tokenText.startsWith("[(") && tokenText.endsWith(")]")) { //NOI18N
            startOffset = 2;
            endOffset = 2;
        } else if (tokenText.startsWith("([") || tokenText.startsWith("[(")) { //NOI18N
            startOffset = 2;
        } else if (tokenText.startsWith("(") && tokenText.endsWith(")")) { //NOI18N
            startOffset = 1;
            endOffset = 1;
        } else if (tokenText.startsWith("[") && tokenText.endsWith("]")) { //NOI18N
            startOffset = 1;
            endOffset = 1;
        } else if (tokenText.startsWith("(") || tokenText.startsWith("[")) { //NOI18N
            startOffset = 1;
        }

        return new OffsetPair(startOffset, endOffset);
    }
}
