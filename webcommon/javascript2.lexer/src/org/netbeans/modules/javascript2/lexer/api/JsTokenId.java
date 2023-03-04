/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.lexer.api;

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
import org.netbeans.modules.javascript2.lexer.JsLexer;
import org.netbeans.modules.javascript2.lexer.JsonLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Petr Pisl
 * @author Petr Hejl
 */
public enum JsTokenId implements TokenId {

    ERROR(null, "error") { // NOI18N
        @Override
        public boolean isError() {
            return true;
        }
    },

    UNKNOWN(null, "unknown") { // NOI18N
        @Override
        public boolean isError() {
            return true;
        }
    },

    NUMBER(null, "constant"), // NOI18N

    IDENTIFIER(null, "identifier"), // NOI18N
    PRIVATE_IDENTIFIER(null, "identifier"), // NOI18N

    WHITESPACE(null, "whitespace"), // NOI18N
    EOL(null, "whitespace"), // NOI18N

    LINE_COMMENT(null, "comment-line"), // NOI18N
    BLOCK_COMMENT(null, "comment"), // NOI18N
    DOC_COMMENT(null, "comment"), // NOI18N

    STRING_BEGIN(null, "string"), // NOI18N
    STRING(null, "string"), // NOI18N
    STRING_END(null, "string"), // NOI18N
    
    TEMPLATE_BEGIN(null, "string"), // NOI18N
    TEMPLATE(null, "string"), // NOI18N
    TEMPLATE_END(null, "string"), // NOI18N
    
    TEMPLATE_EXP_BEGIN(null, "separator"), // NOI18N
    TEMPLATE_EXP_END(null, "separator"), // NOI18N

    REGEXP_BEGIN(null, "mod-regexp"), // NOI18N
    REGEXP(null, "mod-regexp"), // NOI18N
    REGEXP_END(null, "mod-regexp"), // NOI18N

    BRACKET_LEFT_PAREN("(", "separator"), // NOI18N
    BRACKET_RIGHT_PAREN(")", "separator"), // NOI18N
    BRACKET_LEFT_CURLY("{", "separator"), // NOI18N
    BRACKET_RIGHT_CURLY("}", "separator"), // NOI18N
    BRACKET_LEFT_BRACKET("[", "separator"), // NOI18N
    BRACKET_RIGHT_BRACKET("]", "separator"), // NOI18N

    OPERATOR_SEMICOLON(";", "separator"), // NOI18N
    OPERATOR_COMMA(",", "separator"), // NOI18N
    OPERATOR_DOT(".", "separator"), // NOI18N
    OPERATOR_OPTIONAL_ACCESS("?.", "separator"), // NOI18N
    OPERATOR_REST("...", "separator"), // NOI18N
    OPERATOR_ASSIGNMENT("=", "operator"), // NOI18N
    OPERATOR_GREATER(">", "operator"), // NOI18N
    OPERATOR_LOWER("<", "operator"), // NOI18N
    OPERATOR_NOT("!", "operator"), // NOI18N
    OPERATOR_BITWISE_NOT("~", "operator"), // NOI18N
    OPERATOR_TERNARY("?", "operator"), // NOI18N
    OPERATOR_COLON(":", "separator"), // NOI18N
    OPERATOR_EQUALS("==", "operator"), // NOI18N
    OPERATOR_EQUALS_EXACTLY("===", "operator"), // NOI18N
    OPERATOR_LOWER_EQUALS("<=", "operator"), // NOI18N
    OPERATOR_GREATER_EQUALS(">=", "operator"), // NOI18N
    OPERATOR_NOT_EQUALS("!=", "operator"), // NOI18N
    OPERATOR_NOT_EQUALS_EXACTLY("!==", "operator"), // NOI18N
    OPERATOR_AND("&&", "operator"), // NOI18N
    OPERATOR_OR("||", "operator"), // NOI18N
    OPERATOR_INCREMENT("++", "operator"), // NOI18N
    OPERATOR_DECREMENT("--", "operator"), // NOI18N
    OPERATOR_PLUS("+", "operator"), // NOI18N
    OPERATOR_MINUS("-", "operator"), // NOI18N
    OPERATOR_EXPONENTIATION("**", "operator"), // NOI18N
    OPERATOR_MULTIPLICATION("*", "operator"), // NOI18N
    OPERATOR_DIVISION("/", "operator"), // NOI18N
    OPERATOR_BITWISE_AND("&", "operator"), // NOI18N
    OPERATOR_BITWISE_OR("|", "operator"), // NOI18N
    OPERATOR_BITWISE_XOR("^", "operator"), // NOI18N
    OPERATOR_MODULUS("%", "operator"), // NOI18N
    OPERATOR_LEFT_SHIFT_ARITHMETIC("<<", "operator"), // NOI18N
    OPERATOR_RIGHT_SHIFT_ARITHMETIC(">>", "operator"), // NOI18N
    OPERATOR_RIGHT_SHIFT(">>>", "operator"), // NOI18N
    OPERATOR_PLUS_ASSIGNMENT("+=", "operator"), // NOI18N
    OPERATOR_MINUS_ASSIGNMENT("-=", "operator"), // NOI18N
    OPERATOR_EXPONENTIATION_ASSIGNMENT("**=", "operator"), // NOI18N
    OPERATOR_MULTIPLICATION_ASSIGNMENT("*=", "operator"), // NOI18N
    OPERATOR_DIVISION_ASSIGNMENT("/=", "operator"), // NOI18N
    OPERATOR_BITWISE_AND_ASSIGNMENT("&=", "operator"), // NOI18N
    OPERATOR_BITWISE_OR_ASSIGNMENT("|=", "operator"), // NOI18N
    OPERATOR_BITWISE_XOR_ASSIGNMENT("^=", "operator"), // NOI18N
    OPERATOR_MODULUS_ASSIGNMENT("%=", "operator"), // NOI18N
    OPERATOR_LEFT_SHIFT_ARITHMETIC_ASSIGNMENT("<<=", "operator"), // NOI18N
    OPERATOR_RIGHT_SHIFT_ARITHMETIC_ASSIGNMENT(">>=", "operator"), // NOI18N
    OPERATOR_RIGHT_SHIFT_ASSIGNMENT(">>>=", "operator"), // NOI18N
    OPERATOR_ARROW("=>", "operator"), // NOI18N
    OPERATOR_AT("@", "operator"), // NOI18N
    OPERATOR_NULLISH("??", "operator"), // NOI18N
    OPERATOR_ASSIGN_LOG_AND("&&=", "operator"), // NOI18N
    OPERATOR_ASSIGN_LOG_OR("||=", "operator"), // NOI18N
    OPERATOR_ASSIGN_NULLISH("??=", "operator"), // NOI18N

    KEYWORD_BREAK("break", "keyword"), // NOI18N
    KEYWORD_CASE("case", "keyword"), // NOI18N
    KEYWORD_CATCH("catch", "keyword"), // NOI18N
    KEYWORD_CONTINUE("continue", "keyword"), // NOI18N
    KEYWORD_DEBUGGER("debugger", "keyword"), // NOI18N
    KEYWORD_DEFAULT("default", "keyword"), // NOI18N
    KEYWORD_DELETE("delete", "keyword"), // NOI18N
    KEYWORD_DO("do", "keyword"), // NOI18N
    KEYWORD_ELSE("else", "keyword"), // NOI18N
    KEYWORD_FINALLY("finally", "keyword"), // NOI18N
    KEYWORD_FOR("for", "keyword"), // NOI18N
    KEYWORD_FUNCTION("function", "keyword"), // NOI18N
    KEYWORD_IF("if", "keyword"), // NOI18N
    KEYWORD_IN("in", "keyword"), // NOI18N
    KEYWORD_INSTANCEOF("instanceof", "keyword"), // NOI18N
    KEYWORD_NEW("new", "keyword"), // NOI18N
    KEYWORD_RETURN("return", "keyword"), // NOI18N
    KEYWORD_SWITCH("switch", "keyword"), // NOI18N
    KEYWORD_THIS("this", "keyword"), // NOI18N
    KEYWORD_THROW("throw", "keyword"), // NOI18N
    KEYWORD_TRY("try", "keyword"), // NOI18N
    KEYWORD_TYPEOF("typeof", "keyword"), // NOI18N
    KEYWORD_VAR("var", "keyword"), // NOI18N
    KEYWORD_VOID("void", "keyword"), // NOI18N
    KEYWORD_WHILE("while", "keyword"), // NOI18N
    KEYWORD_WITH("with", "keyword"), // NOI18N

    KEYWORD_CLASS("class", "keyword"), // NOI18N
    KEYWORD_CONST("const", "keyword"), // NOI18N
    KEYWORD_EXTENDS("extends", "keyword"), // NOI18N
    KEYWORD_EXPORT("export", "keyword"), // NOI18N
    KEYWORD_IMPORT("import", "keyword"), // NOI18N
    KEYWORD_SUPER("super", "keyword"), // NOI18N
    KEYWORD_YIELD("yield", "keyword"), // NOI18N
    
    RESERVED_ENUM("enum", "reserved"), // NOI18N
    RESERVED_IMPLEMENTS("implements", "reserved"), // NOI18N
    RESERVED_INTERFACE("interface", "reserved"), // NOI18N
    RESERVED_LET("let", "reserved"), // NOI18N
    RESERVED_PACKAGE("package", "reserved"), // NOI18N
    RESERVED_PRIVATE("private", "reserved"), // NOI18N
    RESERVED_PROTECTED("protected", "reserved"), // NOI18N
    RESERVED_PUBLIC("public", "reserved"), // NOI18N
    RESERVED_STATIC("static", "reserved"), // NOI18N
    
    RESERVED_AWAIT("await", "reserved"), //NOI18N
    
    KEYWORD_TRUE("true", "keyword"), // NOI18N
    KEYWORD_FALSE("false", "keyword"), // NOI18N
    KEYWORD_NULL("null", "keyword"), // NOI18N
    
    JSX_EXP_BEGIN(null, "separator"), // NOI18N
    JSX_EXP_END(null, "separator"), // NOI18N
    
    JSX_TEXT(null, "jsx_text"); //NOI18N
    
    // JavaScript mimetypes
    public static final String JAVASCRIPT_MIME_TYPE = "text/javascript"; // NOI18N
    public static final String GULP_MIME_TYPE = "text/gulp+javascript"; // NOI18N
    public static final String GRUNT_MIME_TYPE = "text/grunt+javascript"; // NOI18N
    public static final String KARMACONF_MIME_TYPE = "text/karmaconf+javascript"; // NOI18N

    // JSON mimetypes
    public static final String JSON_MIME_TYPE = "text/x-json"; // NOI18N
    public static final String PACKAGE_JSON_MIME_TYPE = "text/package+x-json"; // NOI18N
    public static final String BOWER_JSON_MIME_TYPE = "text/bower+x-json"; // NOI18N
    public static final String BOWERRC_JSON_MIME_TYPE = "text/bowerrc+x-json"; // NOI18N
    public static final String JSHINTRC_JSON_MIME_TYPE = "text/jshintrc+x-json"; // NOI18N
    private static final String JSON_MIME_TYPE_END = "x-json";  //NOI18N
    
    private final String fixedText;

    private final String primaryCategory;
    
    public static boolean isJSONBasedMimeType(String mimeType) {
        return (mimeType != null && mimeType.endsWith(JSON_MIME_TYPE_END));
    }

    JsTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public boolean isKeyword() {
        return "keyword".equals(primaryCategory); //NOI18N
    }

    public boolean isError() {
        return false;
    }

    private static final Language<JsTokenId> JAVASCRIPT_LANGUAGE =
            new LanguageHierarchy<JsTokenId>() {
                @Override
                protected String mimeType() {
                    return JsTokenId.JAVASCRIPT_MIME_TYPE;
                }

                @Override
                protected Collection<JsTokenId> createTokenIds() {
                    return EnumSet.allOf(JsTokenId.class);
                }

                @Override
                protected Map<String, Collection<JsTokenId>> createTokenCategories() {
                    Map<String, Collection<JsTokenId>> cats = new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<JsTokenId> createLexer(LexerRestartInfo<JsTokenId> info) {
                    return JsLexer.create(info);
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<JsTokenId> token,
                        LanguagePath languagePath, InputAttributes inputAttributes) {
                    JsTokenId id = token.id();

                    if (id == DOC_COMMENT || id == BLOCK_COMMENT) {
                        return LanguageEmbedding.create(JsDocumentationTokenId.language(), 0, 0);
                    } else if (id == JSX_TEXT || id == JSX_EXP_BEGIN || id == JSX_EXP_END) {
                        return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                    }

                    return null; // No embedding
                }
            }.language();

    private static final Language<JsTokenId> JSON_LANGUAGE =
            new LanguageHierarchy<JsTokenId>() {
                @Override
                protected String mimeType() {
                    return JsTokenId.JSON_MIME_TYPE;
                }

                @Override
                protected Collection<JsTokenId> createTokenIds() {
                    return EnumSet.allOf(JsTokenId.class);
                }

                @Override
                protected Map<String, Collection<JsTokenId>> createTokenCategories() {
                    Map<String, Collection<JsTokenId>> cats = new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<JsTokenId> createLexer(LexerRestartInfo<JsTokenId> info) {
                    return JsonLexer.create(info);
                }
            }.language();

    public static Language<JsTokenId> javascriptLanguage() {
        return JAVASCRIPT_LANGUAGE;
    }

    public static Language<JsTokenId> jsonLanguage() {
        return JSON_LANGUAGE;
    }
}
