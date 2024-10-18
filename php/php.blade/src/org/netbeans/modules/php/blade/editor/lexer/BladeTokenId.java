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
    BLADE_COMMENT_START("blade_comment"),
    BLADE_COMMENT("blade_comment"),
    BLADE_COMMENT_END("blade_comment"),
    BLADE_DIRECTIVE("blade_directive"),
    BLADE_ECHO_DELIMITOR("blade_echo_delimiters"),
    BLADE_PAREN(""),
    HTML("html"),
    WS_D("html"),
    BLADE_DIRECTIVE_UNKNOWN("at_string"),
    PHP_BLADE_EXPRESSION("blade_php"),
    PHP_BLADE_ECHO_EXPR("blade_php"),
    PHP_BLADE_INLINE_CODE("blade_php"),
    PHP_INLINE("php"),
    OTHER("error");
    private final String category;

    BladeTokenId(String category) {
        this.category = category;
    }

    @Override
    public String primaryCategory() {
        return category;
    }

    public static abstract class BladeLanguageHierarchy extends LanguageHierarchy<BladeTokenId> {

        private final WeakHashMap<BladeTokenId, LanguageEmbedding<?>> tokenLangCache
                = new WeakHashMap<>();

        @Override
        protected Collection<BladeTokenId> createTokenIds() {
            return EnumSet.allOf(BladeTokenId.class);
        }

        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(Token<BladeTokenId> token,
                LanguagePath languagePath, InputAttributes inputAttributes) {
            boolean joinHtml = true;
            switch (token.id()) {
                case PHP_BLADE_INLINE_CODE, PHP_BLADE_EXPRESSION -> {
                    Language<? extends TokenId> phpLanguage = PHPTokenId.languageInPHP();
                    if (phpLanguage == null || token.text() == null){
                        return null;
                    }

                    //php brace matcher freeze issue patch
                    String tokenText = token.text().toString();
                    int startOffset = 0;
                    int endOffset = 0;

                    if (!tokenText.startsWith("(") && !tokenText.startsWith("[")){
                        return LanguageEmbedding.create(phpLanguage, startOffset, endOffset, false);
                    }
                                      
                    //php brace matcher freeze issue patch
                    if (tokenText.startsWith("((") && tokenText.endsWith("))")){ //NOI18N
                        startOffset = 2;
                        endOffset = 2;
                    } else if (tokenText.startsWith("[[") && tokenText.endsWith("]]")){ //NOI18N
                        startOffset = 2;
                        endOffset = 2;
                    } else if (tokenText.startsWith("([") && tokenText.endsWith("])")){ //NOI18N
                        startOffset = 2;
                        endOffset = 2;
                    } else if (tokenText.startsWith("[(") && tokenText.endsWith(")]")){ //NOI18N
                        startOffset = 2;
                        endOffset = 2;
                    } else if (tokenText.startsWith("([") || tokenText.startsWith("[(")){ //NOI18N
                        startOffset = 2;
                    } else if (tokenText.startsWith("(") && tokenText.endsWith(")")){ //NOI18N
                        startOffset = 1;
                        endOffset = 1;
                    } else if (tokenText.startsWith("[") && tokenText.endsWith("]")){ //NOI18N
                        startOffset = 1;
                        endOffset = 1;
                    }  else if (tokenText.startsWith("(") || tokenText.startsWith("[")){ //NOI18N
                        startOffset = 1;
                    }
                    return LanguageEmbedding.create(phpLanguage, startOffset, endOffset, false);
                }
                case PHP_BLADE_ECHO_EXPR ->  {
                    Language<? extends TokenId> phpLanguage = PHPTokenId.languageInPHP();
                    if (phpLanguage == null || token.text() == null){
                        return null;
                    }
                    String tokenText = token.text().toString();
                    int startOffset = 0;
                    int endOffset = 0;
                    
                    if (!tokenText.startsWith("(") && !tokenText.startsWith("[")){
                        return LanguageEmbedding.create(phpLanguage, startOffset, endOffset, false);
                    }
                                      
                    //php brace matcher freeze issue patch
                    if (tokenText.startsWith("((") && tokenText.endsWith("))")){ //NOI18N
                        startOffset = 2;
                        endOffset = 2;
                    } else if (tokenText.startsWith("[[") && tokenText.endsWith("]]")){ //NOI18N
                        startOffset = 2;
                        endOffset = 2;
                    } else if (tokenText.startsWith("([") && tokenText.endsWith("])")){ //NOI18N
                        startOffset = 2;
                        endOffset = 2;
                    } else if (tokenText.startsWith("[(") && tokenText.endsWith(")]")){ //NOI18N
                        startOffset = 2;
                        endOffset = 2;
                    } else if (tokenText.startsWith("([") || tokenText.startsWith("[(")){ //NOI18N
                        startOffset = 2;
                    } else if (tokenText.startsWith("(") && tokenText.endsWith(")")){ //NOI18N
                        startOffset = 1;
                        endOffset = 1;
                    } else if (tokenText.startsWith("[") && tokenText.endsWith("]")){ //NOI18N
                        startOffset = 1;
                        endOffset = 1;
                    }  else if (tokenText.startsWith("(") || tokenText.startsWith("[")){ //NOI18N
                        startOffset = 1;
                    }
                    return LanguageEmbedding.create(phpLanguage, startOffset, endOffset, false);
                }
                case PHP_INLINE -> {
                    Language<? extends TokenId> phpLanguageCode = PHPTokenId.language();
                    return phpLanguageCode != null ? LanguageEmbedding.create(phpLanguageCode, 0, 0, false) : null;
                }
                case HTML -> {
                    LanguageEmbedding<?> lang;

                    if (tokenLangCache.containsKey(token.id())) {
                        lang = tokenLangCache.get(token.id());
                    } else {
                        Language<? extends TokenId> htmlLanguage = null;

                        @SuppressWarnings("unchecked")
                                Collection<LanguageProvider> providers = (Collection<LanguageProvider>) Lookup.getDefault().lookupAll(LanguageProvider.class);
                        for (LanguageProvider provider : providers) {
                            htmlLanguage = (Language<? extends TokenId>) provider.findLanguage("text/html"); //NOI18N
                            if (htmlLanguage != null) {
                                break;
                            }
                        }

                        lang = htmlLanguage != null ? LanguageEmbedding.create(htmlLanguage, 0, 0, joinHtml) : null;
                        tokenLangCache.put(token.id(), lang);
                    }

                    return lang;
                }
                default -> {
                    return null;
                }
            }
        }
    }

}
