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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.lib.lexer.lang;

import org.netbeans.lib.lexer.lang.TestStringTokenId;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.test.simple.*;
import org.netbeans.lib.lexer.lang.TestJavadocTokenId;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
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
 * Simple implementation of enumerated token id.
 *
 * @author mmetelka
 */
public enum TestTokenId implements TokenId {
    
    IDENTIFIER(null, null),
    WHITESPACE(null, null), // normally would be "whitespace" category here but testing to do it in language hierarchy
    BLOCK_COMMENT(null, "comment"),
    LINE_COMMENT(null, "comment"),
    JAVADOC_COMMENT(null, "comment"),
    PLUS("+", "operator"),
    MINUS("-", "operator"),
    PLUS_MINUS_PLUS("+-+", null),
    DIV("/", "operator"),
    STAR("*", "operator"),
    ERROR(null, "error"),
    PUBLIC("public", "keyword"),
    PRIVATE("private", "keyword"),
    STATIC("static", "keyword"),
    STRING_LITERAL(null, "string"),

    BLOCK_COMMENT_INCOMPLETE(null, "comment"),
    JAVADOC_COMMENT_INCOMPLETE(null, "comment"),
    STRING_LITERAL_INCOMPLETE(null, "string"),
    ;

    private final String fixedText;

    private final String primaryCategory;

    TestTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }
    
    public String fixedText() {
        return fixedText;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TestTokenId> language
    = new LanguageHierarchy<TestTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-simple";
        }

        @Override
        protected Collection<TestTokenId> createTokenIds() {
            return EnumSet.allOf(TestTokenId.class);
        }

        @Override
        protected Map<String,Collection<TestTokenId>> createTokenCategories() {
            Map<String,Collection<TestTokenId>> cats = new HashMap<String,Collection<TestTokenId>>();
            cats.put("operator",EnumSet.of(TestTokenId.PLUS_MINUS_PLUS));
            // Normally whitespace category would be a primary category in token id's declaration
            cats.put("whitespace",EnumSet.of(TestTokenId.WHITESPACE));
            cats.put("incomplete",EnumSet.of(
                    TestTokenId.BLOCK_COMMENT_INCOMPLETE,TestTokenId.JAVADOC_COMMENT_INCOMPLETE,TestTokenId.STRING_LITERAL_INCOMPLETE
            ));
            cats.put("error",EnumSet.of(
                    TestTokenId.BLOCK_COMMENT_INCOMPLETE,TestTokenId.JAVADOC_COMMENT_INCOMPLETE,TestTokenId.STRING_LITERAL_INCOMPLETE
            ));
            cats.put("test-category",EnumSet.of(
                    TestTokenId.IDENTIFIER,TestTokenId.PLUS,TestTokenId.MINUS
            ));
            return cats;
        }

        @Override
        protected Lexer<TestTokenId> createLexer(LexerRestartInfo<TestTokenId> info) {
            return new TestLexer(info);
        }
        
        @Override
        public LanguageEmbedding<?> embedding(
        Token<TestTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment
            switch (token.id()) {
                case BLOCK_COMMENT:
                    return LanguageEmbedding.create(TestPlainTokenId.language(), 2, 2);

                case JAVADOC_COMMENT:
                    return LanguageEmbedding.create(TestJavadocTokenId.language(), 3, 2);

                case STRING_LITERAL:
                case STRING_LITERAL_INCOMPLETE:
                    return LanguageEmbedding.create(TestStringTokenId.language(), 1, 1);
            }
            return null; // No embedding
        }

//        protected CharPreprocessor createCharPreprocessor() {
//            return CharPreprocessor.createUnicodeEscapesPreprocessor();
//        }

    }.language();

    public static Language<TestTokenId> language() {
        return language;
    }

}
