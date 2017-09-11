/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.declarative;

import java.util.Collection;
import java.util.EnumSet;
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
 * @author Jan Lahoda
 */
public enum DeclarativeHintTokenId implements TokenId {

    CHAR_LITERAL("character"),
    STRING_LITERAL("string"),
    COLON("operator"),
    DOUBLE_COLON("operator"),
    LEADS_TO("operator"),
    AND("operator"),
    INSTANCEOF("keyword"),
    OTHERWISE("keyword"),
    NOT("operator"),
    DOUBLE_SEMICOLON("operator"),
    DOUBLE_PERCENT("operator"),
    VARIABLE("identifier"),
    JAVA_SNIPPET("pattern"),
    WHITESPACE("whitespace"),
    LINE_COMMENT("comment"),
    BLOCK_COMMENT("comment"),
    JAVA_BLOCK("java"),
    OPTIONS("options"),
    ERROR("error");

    public static final String MIME_TYPE = "text/x-javahints";

    private final String cat;

    DeclarativeHintTokenId(String cat) {
        this.cat = cat;
    }

    public String primaryCategory() {
        return cat;
    }

    public static Language<DeclarativeHintTokenId> language() {
        return LANGUAGE;
    }
    
    private static final Language<DeclarativeHintTokenId> LANGUAGE = new LanguageHierarchy<DeclarativeHintTokenId>() {

        @Override
        protected Collection<DeclarativeHintTokenId> createTokenIds() {
            return EnumSet.allOf(DeclarativeHintTokenId.class);
        }

        @Override
        protected Lexer<DeclarativeHintTokenId> createLexer(LexerRestartInfo<DeclarativeHintTokenId> info) {
            return new DeclarativeHintLexer(info);
        }

        @Override
        protected String mimeType() {
            return MIME_TYPE;
        }

        @Override
        protected LanguageEmbedding<?> embedding(Token<DeclarativeHintTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch (token.id()) {
                case JAVA_SNIPPET:
                    return LanguageEmbedding.create(Language.find("text/x-java"), 0, 0);
                case JAVA_BLOCK:
                    return LanguageEmbedding.create(Language.find("text/x-java"), 2, 2);
                default:
                    return null;
            }
        }

    }.language();
}
