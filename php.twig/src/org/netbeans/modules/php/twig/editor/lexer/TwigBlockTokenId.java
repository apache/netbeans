/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.twig.editor.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public enum TwigBlockTokenId implements TokenId {
    T_TWIG_TAG("twig_tag"), //NOI18N
    T_TWIG_NAME("twig_name"), //NOI18N
    T_TWIG_OPERATOR("twig_operator"), //NOI18N
    T_TWIG_PUNCTUATION("twig_punctuation"), //NOI18N
    T_TWIG_NUMBER("twig_number"), //NOI18N
    T_TWIG_STRING("twig_string"), //NOI18N
    T_TWIG_INTERPOLATION_START("twig_interpolation"), //NOI18N
    T_TWIG_INTERPOLATION_END("twig_interpolation"), //NOI18N
    T_TWIG_OTHER("twig_other"), //NOI18N
    T_TWIG_WHITESPACE("twig_whitespace"); //NOI18N

    private final String primaryCategory;

    TwigBlockTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
    private static final Language<TwigBlockTokenId> LANGUAGE =
            new LanguageHierarchy<TwigBlockTokenId>() {
                @Override
                protected Collection<TwigBlockTokenId> createTokenIds() {
                    return EnumSet.allOf(TwigBlockTokenId.class);
                }

                @Override
                protected Map<String, Collection<TwigBlockTokenId>> createTokenCategories() {
                    Map<String, Collection<TwigBlockTokenId>> cats = new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<TwigBlockTokenId> createLexer(LexerRestartInfo<TwigBlockTokenId> info) {
                    return new TwigBlockLexer(info);
                }

                @Override
                protected String mimeType() {
                    return TwigLanguage.TWIG_MIME_TYPE + "-block"; // NOI18N
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<TwigBlockTokenId> token,
                        LanguagePath languagePath, InputAttributes inputAttributes) {
                    return null;
                }
            }.language();

    public static Language<TwigBlockTokenId> language() {
        return LANGUAGE;
    }
}
