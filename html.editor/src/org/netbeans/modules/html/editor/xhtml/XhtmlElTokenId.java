/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.html.editor.xhtml;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.netbeans.api.html.lexer.HTMLTokenId;
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
 * Special top level lexer splitting the text into EL and non-EL tokens.
 *
 * @author Marek Fukala
 */
public enum XhtmlElTokenId implements TokenId {

    HTML("html"),
    EL("expression-language");

    private final String primaryCategory;
    private static Language<? extends TokenId> EL_LANGUAGE;

    XhtmlElTokenId() {
        this(null);
    }

    XhtmlElTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    // Token ids declaration
    private static final Language<XhtmlElTokenId> language = new LanguageHierarchy<XhtmlElTokenId>() {

        @Override
        protected Collection<XhtmlElTokenId> createTokenIds() {
            return EnumSet.allOf(XhtmlElTokenId.class);
        }

        @Override
        protected Map<String, Collection<XhtmlElTokenId>> createTokenCategories() {
            return null;
        }

        @Override
        protected Lexer<XhtmlElTokenId> createLexer(LexerRestartInfo<XhtmlElTokenId> info) {
            return new XhtmlElLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
                Token<XhtmlElTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch (token.id()) {
                case HTML:
                    return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                case EL:
                    //lexer infrastructure workaround - need to adjust skiplenghts in case of short token
                    int startSkipLength = token.length() > 2 ? 2 : token.length();
                    int endSkipLength = token.length() > 2 ? 1 : 0;
                    Language<? extends TokenId> elLang = getELLanguage();
                    if(elLang != null) {
                        return LanguageEmbedding.create(elLang, startSkipLength, endSkipLength);
                    }

                default:
                    return null;
            }
        }

        @Override
        protected String mimeType() {
            return "text/xhtml";
        }
    }.language();

    public static Language<XhtmlElTokenId> language() {
        return language;
    }

    private static synchronized Language<? extends TokenId> getELLanguage() {
        //keep trying to get the instance if not available - may happen during some lazy modules loading?!?!
        if (EL_LANGUAGE == null) {
            EL_LANGUAGE = Language.find("text/x-el"); //NOI18N
        }
        return EL_LANGUAGE;
    }
}

