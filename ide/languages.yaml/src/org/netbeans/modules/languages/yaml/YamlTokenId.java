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
package org.netbeans.modules.languages.yaml;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.yaml.ruby.RubyEmbeddingProvider;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.util.Lookup;

/**
 * Token type definitions for YAML
 *
 * @author Tor Norbye
 */
public enum YamlTokenId implements TokenId {

    TEXT("identifier"),
    COMMENT("comment"),
    /**
     * Contents inside <%# %>
     */
    RUBYCOMMENT("comment"),
    /**
     * Contents inside <%= %>
     */
    RUBY_EXPR("ruby"),
    /**
     * Contents inside <% %>
     */
    RUBY("ruby"),
    /**
     * <% or %>
     */
    DELIMITER("ruby-delimiter"),
    PHP("php"),
    MUSTACHE("mustache"),
    MUSTACHE_DELIMITER("mustache-delimiter");
    private final String primaryCategory;

    YamlTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public static boolean isRuby(TokenId id) {
        return id == RUBY || id == RUBY_EXPR || id == RUBYCOMMENT;
    }
    private static final Language<YamlTokenId> language =
            new LanguageHierarchy<YamlTokenId>() {

                @Override
                protected String mimeType() {
                    return YamlTokenId.YAML_MIME_TYPE;
                }

                @Override
                protected Collection<YamlTokenId> createTokenIds() {
                    return EnumSet.allOf(YamlTokenId.class);
                }

                @Override
                protected Map<String, Collection<YamlTokenId>> createTokenCategories() {
                    Map<String, Collection<YamlTokenId>> cats =
                            new HashMap<String, Collection<YamlTokenId>>();
                    return cats;
                }

                @Override
                protected Lexer<YamlTokenId> createLexer(LexerRestartInfo<YamlTokenId> info) {
                    return new YamlLexer(info);
                }

                @Override
                protected LanguageEmbedding<? extends TokenId> embedding(Token<YamlTokenId> token,
                        LanguagePath languagePath, InputAttributes inputAttributes) {
                    switch (token.id()) {
                        case RUBY_EXPR:
                        case RUBY:
                            // No dependency on the Ruby module:
                            //Language rubyLanguage = RubyTokenId.language();
                            Language<? extends TokenId> rubyLanguage = null;

                            @SuppressWarnings("unchecked") Collection<LanguageProvider> providers = (Collection<LanguageProvider>) Lookup.getDefault().lookupAll(LanguageProvider.class);
                            for (LanguageProvider provider : providers) {
                                rubyLanguage = (Language<? extends TokenId>) provider.findLanguage(RubyEmbeddingProvider.RUBY_MIME_TYPE);
                                if (rubyLanguage != null) {
                                    break;
                                }
                            }

                            return rubyLanguage != null ? LanguageEmbedding.create(rubyLanguage, 0, 0, false) : null;
                        case PHP:
                            Language<? extends TokenId> phpLanguage = null;

                            providers = (Collection<LanguageProvider>) Lookup.getDefault().lookupAll(LanguageProvider.class);
                            for (LanguageProvider provider : providers) {
                                phpLanguage = (Language<? extends TokenId>) provider.findLanguage("text/x-php5");
                                if (phpLanguage != null) {
                                    break;
                                }
                            }
                            return phpLanguage != null ? LanguageEmbedding.create(phpLanguage, 0, 0, false) : null;
                        default:
                            return null;
                    }
                }
            }.language();

    public static Language<YamlTokenId> language() {
        return language;
    }
    /**
     * MIME type for YAML. Don't change this without also consulting the various
     * XML files that cannot reference this value directly.
     */
    public static final String YAML_MIME_TYPE = "text/x-yaml"; // NOI18N
}
