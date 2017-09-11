/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
    PHP("php");
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
