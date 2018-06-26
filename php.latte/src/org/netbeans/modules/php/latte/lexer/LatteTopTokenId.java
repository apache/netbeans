/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.latte.lexer;

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
import org.netbeans.modules.php.latte.csl.LatteLanguage;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public enum LatteTopTokenId implements TokenId {
    T_HTML("html"), //NOI18N
    T_LATTE("latte-markup"), //NOI18N
    T_LATTE_ERROR("latte-error"), //NOI18N
    T_LATTE_OPEN_DELIMITER("latte-delimiter"), //NOI18N
    T_LATTE_CLOSE_DELIMITER("latte-delimiter"), //NOI18N
    T_LATTE_COMMENT("latte-comment"), //NOI18N
    T_LATTE_COMMENT_DELIMITER("latte-comment"); //NOI18N
    private String primaryCategory;

    private LatteTopTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<LatteTopTokenId> LANGUAGE =
            new LanguageHierarchy<LatteTopTokenId>() {
                @Override
                protected Collection<LatteTopTokenId> createTokenIds() {
                    return EnumSet.allOf(LatteTopTokenId.class);
                }

                @Override
                protected Map<String, Collection<LatteTopTokenId>> createTokenCategories() {
                    Map<String, Collection<LatteTopTokenId>> cats = new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<LatteTopTokenId> createLexer(LexerRestartInfo<LatteTopTokenId> info) {
                    return new LatteTopLexer(info);
                }

                @Override
                protected String mimeType() {
                    return LatteLanguage.LATTE_MIME_TYPE;
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<LatteTopTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
                    LanguageEmbedding<?> result = null;
                    LatteTopTokenId tokenId = token.id();
                    if (tokenId == LatteTopTokenId.T_HTML)  {
                        result = LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                    } else if (tokenId == LatteTopTokenId.T_LATTE) {
                        result = LanguageEmbedding.create(LatteMarkupTokenId.language(), 0, 0);
                    }
                    return result;
                }
            }.language();

    public static Language<LatteTopTokenId> language() {
        return LANGUAGE;
    }

}
