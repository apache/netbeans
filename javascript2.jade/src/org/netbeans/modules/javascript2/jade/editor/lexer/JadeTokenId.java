/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.jade.editor.lexer;

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
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Petr Pisl
 */
public enum JadeTokenId implements TokenId {

    // doctype definition
    DOCTYPE ("doctype", "sgml-declaration"), // NOI18N
    DOCTYPE_TEMPLATE (null, "sgml-declaration"), // NOI18N
    DOCTYPE_ATTRIBUTE (null, "sgml-declaration"), // NOI18N
    DOCTYPE_STRING (null, "sgml-declaration"), // NOI18N
    DOCTYPE_STRING_START (null, "sgml-declaration"), // NOI18N
    DOCTYPE_STRING_END (null, "sgml-declaration"), // NOI18N
    
    EOL(null, "whitespace"), // NOI18N
    WHITESPACE(null, "whitespace"), // NOI18N
    
    // keywords
    KEYWORD_IF("if", "keyword"), // NOI18N
    KEYWORD_ELSE("else", "keyword"), // NOI18N
    KEYWORD_UNLESS("unless", "keyword"), // NOI18N
    KEYWORD_CASE("case", "keyword"), // NOI18N
    KEYWORD_WHEN("when", "keyword"), // NOI18N
    KEYWORD_DEFAULT("default", "keyword"), // NOI18N
    KEYWORD_EACH("each", "keyword"), // NOI18N
    KEYWORD_IN("in", "keyword"), // NOI18N
    KEYWORD_FOR("for", "keyword"), // NOI18N
    KEYWORD_WHILE("while", "keyword"), // NOI18N
    KEYWORD_MIXIN("mixin", "keyword"), // NOI18N
    
    KEYWORD_TRUE("true", "keyword"), // NOI18N
    KEYWORD_FALSE("false", "keyword"), // NOI18N
    
    KEYWORD_BLOCK("block", "keyword"), // NOI18N
    KEYWORD_EXTENDS("extends", "keyword"), // NOI18N
    KEYWORD_INCLUDE("include", "keyword"), // NOI18N
    
    MIXIN_NAME(null, "mixin-name"), //NOI18N
    IDENTIFIER(null, "identifier"),     //NOI18N
    
    OPERATOR_COLON(":", "separator"), // NOI18N
    OPERATOR_DIVISION("/", "operator"), // NOI18N
    OPERATOR_ASSIGNMENT("=", "operator"), // NOI18N
    OPERATOR_NOT_EQUALS("!=", "operator"), // NOI18N
    OPERATOR_COMMA(",", "separator"), // NOI18N
    OPERATOR_REST_ARGUMENTS("...", "operator"), //NOI18N
    OPERATOR_PLUS("+", "operator"), // NOI18N
    
    COMMENT_DELIMITER(null, "comment"), // NOI18N
    UNBUFFERED_COMMENT_DELIMITER(null, "unbuffered-comment"), //NOI18N
    COMMENT(null, "comment"), // NOI18N
    UNBUFFERED_COMMENT(null, "unbuffered-comment"), //NOI18N
    
    CODE_DELIMITER(null, "code-delimiter"), //NOI18N
    PLAIN_TEXT_DELIMITER(null, "plain-text-delimiter"), //NOI18N
    
    PLAIN_TEXT(null, "plain-text"), //NOI18N
    
    EXPRESSION_DELIMITER_OPEN(null, "expression-delimiter"), //NOI18N
    EXPRESSION_DELIMITER_CLOSE(null, "expression-delimiter"), //NOI18N
    
    CSS_ID(null, "css-id"), //NOI18N
    CSS_CLASS(null, "css-class"), //NOI18N
    
    FILE_PATH(null, "file-path"), // NOI18N
    FILTER(null, "filter"), //NOI18N
    FILTER_TEXT(null, "filter-text"), //NOI18N
    
    BLOCK_NAME(null, "block-name"), //NOI18N
    // html 
    TAG (null, "html-tag"), //NOI18N
    ATTRIBUTE(null, "html-attribute"), //NOI18N
    TEXT(null, "text"), //NOI18N
    JAVASCRIPT(null, "javascript-embedded"),
    CSS(null, "css-embedded"),
    
    BRACKET_LEFT_PAREN("(", "separator"), // NOI18N
    BRACKET_RIGHT_PAREN(")", "separator"), // NOI18N
    
    UNKNOWN(null, "error");// NOI18N
    
    public static final String JADE_MIME_TYPE = "text/jade"; // NOI18N
    
    private final String text;
    private final String primaryCategory;
    
    JadeTokenId(String text, String primaryCategory) {
        this.text = text;
        this.primaryCategory = primaryCategory;
    }
    
    public String getText() {
        return text;
    }
    
    public boolean isKeyword() {
        return "keyword".equals(primaryCategory); //NOI18N
    }
    
    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static final Language<JadeTokenId> JADE_LANGUAGE =
            new LanguageHierarchy<JadeTokenId>() {
                @Override
                protected String mimeType() {
                    return JadeTokenId.JADE_MIME_TYPE;
                }

                @Override
                protected Collection<JadeTokenId> createTokenIds() {
                    return EnumSet.allOf(JadeTokenId.class);
                }

                @Override
                protected Map<String, Collection<JadeTokenId>> createTokenCategories() {
                    Map<String, Collection<JadeTokenId>> cats =
                            new HashMap<String, Collection<JadeTokenId>>();
                    return cats;
                }

                @Override
                protected Lexer<JadeTokenId> createLexer(LexerRestartInfo<JadeTokenId> info) {
                    return JadeLexer.create(info);
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<JadeTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
                    JadeTokenId id = token.id();
                    
                    switch (id) {
                        case JAVASCRIPT: 
                            return LanguageEmbedding.create(JsTokenId.javascriptLanguage(), 0, 0, true);
                        case CSS: 
                            return LanguageEmbedding.create(CssTokenId.language(), 0, 0, true);
                        case PLAIN_TEXT:
                        case TEXT:
                            return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                    }
                    return null; // No embedding
                }

            }.language();
    
    public static Language<JadeTokenId> jadeLanguage() {
        return JADE_LANGUAGE;
    }
}
