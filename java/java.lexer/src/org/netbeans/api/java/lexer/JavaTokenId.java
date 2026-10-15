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

package org.netbeans.api.java.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.java.lexer.JavaCharacterTokenId;
import org.netbeans.lib.java.lexer.JavaLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids of java language defined as enum.
 *
 * @author Miloslav Metelka
 */
public enum JavaTokenId implements TokenId {

    ERROR(null, "error"),
    IDENTIFIER(null, "identifier"),

    ABSTRACT("abstract", "keyword"),
    ASSERT("assert", "keyword-directive"),
    BOOLEAN("boolean", "keyword"),
    BREAK("break", "keyword-directive"),
    BYTE("byte", "keyword"),
    CASE("case", "keyword-directive"),
    CATCH("catch", "keyword-directive"),
    CHAR("char", "keyword"),
    CLASS("class", "keyword"),
    CONST("const", "keyword"),
    CONTINUE("continue", "keyword-directive"),
    DEFAULT("default", "keyword-directive"),
    DO("do", "keyword-directive"),
    DOUBLE("double", "keyword"),
    ELSE("else", "keyword-directive"),
    ENUM("enum", "keyword"),
    /**@since 1.34*/
    EXPORTS("exports", "keyword"),
    EXTENDS("extends", "keyword"),
    FINAL("final", "keyword"),
    FINALLY("finally", "keyword-directive"),
    FLOAT("float", "keyword"),
    FOR("for", "keyword-directive"),
    GOTO("goto", "keyword-directive"),
    IF("if", "keyword-directive"),
    IMPLEMENTS("implements", "keyword"),
    IMPORT("import", "keyword"),
    INSTANCEOF("instanceof", "keyword"),
    INT("int", "keyword"),
    INTERFACE("interface", "keyword"),
    LONG("long", "keyword"),
    /**@since 1.34*/
    MODULE("module", "keyword"),
    NATIVE("native", "keyword"),
    NEW("new", "keyword"),
    /**@since 1.35*/
    OPEN("open", "keyword"),
    /**@since 1.35*/
    OPENS("opens", "keyword"),
    PACKAGE("package", "keyword"),
    PRIVATE("private", "keyword"),
    PROTECTED("protected", "keyword"),
    /**@since 1.34*/
    PROVIDES("provides", "keyword"),
    PUBLIC("public", "keyword"),
    /**@since 1.34*/
    REQUIRES("requires", "keyword"),
    RETURN("return", "keyword-directive"),
    SHORT("short", "keyword"),
    STATIC("static", "keyword"),
    STRICTFP("strictfp", "keyword"),
    SUPER("super", "keyword"),
    SWITCH("switch", "keyword-directive"),
    SYNCHRONIZED("synchronized", "keyword"),
    THIS("this", "keyword"),
    THROW("throw", "keyword-directive"),
    THROWS("throws", "keyword"),
    /**@since 1.34*/
    TO("to", "keyword"),
    TRANSIENT("transient", "keyword"),
    /**@since 1.35*/
    TRANSITIVE("transitive", "keyword"),
    TRY("try", "keyword-directive"),
    /**@since 1.30*/
    UNDERSCORE("_", "keyword"),
    /**@since 1.34*/
    USES("uses", "keyword"),
    /**@since 1.36*/
    VAR("var", "keyword"),
    VOID("void", "keyword"),
    VOLATILE("volatile", "keyword"),
    WHILE("while", "keyword-directive"),
    /**@since 1.34*/
    WITH("while", "keyword"),

    INT_LITERAL(null, "number"),
    LONG_LITERAL(null, "number"),
    FLOAT_LITERAL(null, "number"),
    DOUBLE_LITERAL(null, "number"),
    CHAR_LITERAL(null, "character"),
    STRING_LITERAL(null, "string"),
    /**@since 1.41*/
    MULTILINE_STRING_LITERAL(null, "string"),
    
    TRUE("true", "literal"),
    FALSE("false", "literal"),
    NULL("null", "literal"),
    
    LPAREN("(", "separator"),
    RPAREN(")", "separator"),
    LBRACE("{", "separator"),
    RBRACE("}", "separator"),
    LBRACKET("[", "separator"),
    RBRACKET("]", "separator"),
    SEMICOLON(";", "separator"),
    COMMA(",", "separator"),
    DOT(".", "separator"),
    /**@since 1.23*/
    COLONCOLON("::", "separator"),

    EQ("=", "operator"),
    GT(">", "operator"),
    LT("<", "operator"),
    BANG("!", "operator"),
    TILDE("~", "operator"),
    QUESTION("?", "operator"),
    COLON(":", "operator"),
    EQEQ("==", "operator"),
    LTEQ("<=", "operator"),
    GTEQ(">=", "operator"),
    BANGEQ("!=","operator"),
    AMPAMP("&&", "operator"),
    BARBAR("||", "operator"),
    PLUSPLUS("++", "operator"),
    MINUSMINUS("--","operator"),
    PLUS("+", "operator"),
    MINUS("-", "operator"),
    STAR("*", "operator"),
    SLASH("/", "operator"),
    AMP("&", "operator"),
    BAR("|", "operator"),
    CARET("^", "operator"),
    PERCENT("%", "operator"),
    LTLT("<<", "operator"),
    GTGT(">>", "operator"),
    GTGTGT(">>>", "operator"),
    PLUSEQ("+=", "operator"),
    MINUSEQ("-=", "operator"),
    STAREQ("*=", "operator"),
    SLASHEQ("/=", "operator"),
    AMPEQ("&=", "operator"),
    BAREQ("|=", "operator"),
    CARETEQ("^=", "operator"),
    PERCENTEQ("%=", "operator"),
    LTLTEQ("<<=", "operator"),
    GTGTEQ(">>=", "operator"),
    GTGTGTEQ(">>>=", "operator"),
    /**@since 1.23*/
    ARROW("->", "operator"),
    
    ELLIPSIS("...", "special"),
    AT("@", "special"),
    
    WHITESPACE(null, "whitespace"),
    LINE_COMMENT(null, "comment"), // Token includes ending new-line
    BLOCK_COMMENT(null, "comment"),
    JAVADOC_COMMENT(null, "comment"),
    JAVADOC_COMMENT_LINE_RUN(null, "comment"), // A run of "markdown" javadoc comments, includes ending new-line
    
    // Errors
    INVALID_COMMENT_END("*/", "error"),
    FLOAT_LITERAL_INVALID(null, "number");


    private final String fixedText;

    private final String primaryCategory;

    JavaTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<JavaTokenId> language = new LanguageHierarchy<JavaTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-java";
        }

        @Override
        protected Collection<JavaTokenId> createTokenIds() {
            return EnumSet.allOf(JavaTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<JavaTokenId>> createTokenCategories() {
            Map<String,Collection<JavaTokenId>> cats = new HashMap<String,Collection<JavaTokenId>>();
            // Additional literals being a lexical error
            cats.put("error", EnumSet.of(
                JavaTokenId.FLOAT_LITERAL_INVALID
            ));
            // Literals category
            EnumSet<JavaTokenId> l = EnumSet.of(
                JavaTokenId.INT_LITERAL,
                JavaTokenId.LONG_LITERAL,
                JavaTokenId.FLOAT_LITERAL,
                JavaTokenId.DOUBLE_LITERAL,
                JavaTokenId.CHAR_LITERAL
            );
            l.add(JavaTokenId.STRING_LITERAL);
            cats.put("literal", l);

            return cats;
        }

        @Override
        protected Lexer<JavaTokenId> createLexer(LexerRestartInfo<JavaTokenId> info) {
            return new JavaLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<JavaTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment
            switch (token.id()) {
                case JAVADOC_COMMENT:
                    return LanguageEmbedding.create(JavadocTokenId.language(), 3,
                            (token.partType() == PartType.COMPLETE) ? 2 : 0);
                case JAVADOC_COMMENT_LINE_RUN:
                    return LanguageEmbedding.create(JavadocTokenId.language(), 3,
                            (token.partType() == PartType.COMPLETE) ? 1 : 0);
                case STRING_LITERAL:
                    return LanguageEmbedding.create(JavaStringTokenId.language(), 1,
                            (token.partType() == PartType.COMPLETE) ? 1 : 0);
                case CHAR_LITERAL:
                    return LanguageEmbedding.create(JavaCharacterTokenId.language(), 1,
                            (token.partType() == PartType.COMPLETE) ? 1 : 0);
            }
            return null; // No embedding
        }

//        protected CharPreprocessor createCharPreprocessor() {
//            return CharPreprocessor.createUnicodeEscapesPreprocessor();
//        }

    }.language();

    public static Language<JavaTokenId> language() {
        return language;
    }

}
