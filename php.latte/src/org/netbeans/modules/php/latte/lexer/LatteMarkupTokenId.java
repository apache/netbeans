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
public enum LatteMarkupTokenId implements TokenId {
    T_WHITESPACE("latte-markup-whitespace"), //NOI18N
    T_MACRO_START("latte-markup-macro"), //NOI18N
    T_MACRO_END("latte-markup-macro"), //NOI18N
    T_SYMBOL("latte-markup-symbol"), //NOI18N
    T_NUMBER("latte-markup-number"), //NOI18N
    T_VARIABLE("latte-markup-variable"), //NOI18N
    T_STRING("latte-markup-string"), //NOI18N
    T_CAST("latte-markup-cast"), //NOI18N
    T_KEYWORD("latte-markup-keyword"), //NOI18N
    T_CHAR("latte-markup-char"), //NOI18N
    T_ERROR("latte-error"); //NOI18N
    private String primaryCategory;

    private LatteMarkupTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<LatteMarkupTokenId> LANGUAGE =
            new LanguageHierarchy<LatteMarkupTokenId>() {
                @Override
                protected Collection<LatteMarkupTokenId> createTokenIds() {
                    return EnumSet.allOf(LatteMarkupTokenId.class);
                }

                @Override
                protected Map<String, Collection<LatteMarkupTokenId>> createTokenCategories() {
                    Map<String, Collection<LatteMarkupTokenId>> cats = new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<LatteMarkupTokenId> createLexer(LexerRestartInfo<LatteMarkupTokenId> info) {
                    return new LatteMarkupLexer(info);
                }

                @Override
                protected String mimeType() {
                    return LatteLanguage.LATTE_MIME_TYPE + "-markup"; //NOI18N
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<LatteMarkupTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
                    return null;
                }
            }.language();

    public static Language<LatteMarkupTokenId> language() {
        return LANGUAGE;
    }

}
