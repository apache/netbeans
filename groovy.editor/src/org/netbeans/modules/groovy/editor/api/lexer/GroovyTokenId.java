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

package org.netbeans.modules.groovy.editor.api.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Two interesting pages related to Groovy lexer.
 * 
 * http://groovy.codehaus.org/jsr/spec/Chapter18Syntax.html
 * http://groovy.codehaus.org/jsr/spec/GroovyLexer.html
 * 
 * In the second one there is a grammar definition which could be used if anyone
 * would like to know what is the meaning of some tokens.
 * 
 * @author Martin Adamek
 * @author Martin Janicek
 */
public enum GroovyTokenId implements TokenId {

    // update also GroovyLexer.getTokenId(int) if you make changes here

    EOL(null, "whitespace"),
    WHITESPACE(null, "whitespace"),
    ERROR(null, "error"),
    IDENTIFIER(null, "identifier"),
    
    REGEXP_LITERAL(null, "regexp"),
    REGEXP_SYMBOL(null, "regexp"),
    REGEXP_CTOR_END(null, "regexp"),
    
    STRING_LITERAL(null, "string"),
    STRING_CONSTRUCTOR(null, "string"),
    STRING_CTOR_END(null, "string"),
    STRING_CTOR_MIDDLE(null, "string"),
    STRING_CTOR_START(null, "string"),
    STRING_CH(null, "string"),
    STRING_NL(null, "string"),

    SH_COMMENT(null, "comment"), // Special groovy single line comment #!
    SL_COMMENT(null, "comment"), // Single line comment //
    LINE_COMMENT(null, "comment"), // General line comment
    BLOCK_COMMENT(null, "comment"), // Multiline comment /* ...whatever.. */
    
    LPAREN("(", "separator"),
    RPAREN(")", "separator"),
    LBRACE("{", "separator"),
    RBRACE("}", "separator"),
    LBRACKET("[", "separator"),
    RBRACKET("]", "separator"),

    NUM_INT(null, "number"),
    NUM_LONG(null, "number"),
    NUM_FLOAT(null, "number"),
    NUM_DOUBLE(null, "number"),
    NUM_BIG_INT(null, "number"),
    NUM_BIG_DECIMAL(null, "number"),
    DIGIT(null, "number"),
    DIGITS_WITH_UNDERSCORE(null, "number"),
    DIGITS_WITH_UNDERSCORE_OPT(null, "number"),

    AT("@", "operator"),
    BAND("&", "operator"),
    BNOT("~", "operator"),
    BOR("|", "operator"),
    BSR(">>>", "operator"),
    BXOR("^", "operator"),
    COLON(":", "operator"),
    COMMA(",", "operator"),
    COMPARE_TO("<=>", "operator"),
    DEC("--", "operator"),
    DOT(".", "operator"),
    DOLLAR("$", "operator"),
    DIV("/", "operator"),
    ELVIS_OPERATOR("?:", "operator"),
    EQUAL("==", "operator"),
    GE(">=", "operator"),
    GT(">", "operator"),
    INC("++", "operator"),
    LAND("&&", "operator"),
    LE("<=", "operator"),
    LNOT("!", "operator"),
    LOR("||", "operator"),
    LT("<", "operator"),
    MEMBER_POINTER(".&", "operator"),
    MINUS("-", "operator"),
    MOD("%", "operator"),
    NOT_EQUAL("!=", "operator"),
    OPTIONAL_DOT("?.", "operator"),
    PLUS("+", "operator"),
    QUESTION("?", "operator"),
    SL("<<", "operator"),
    SR(">>", "operator"),
    RANGE_INCLUSIVE("..", "operator"),
    RANGE_EXCLUSIVE("..<", "operator"),
    REGEX_FIND("=~", "operator"),
    REGEX_MATCH("==~", "operator"),
    SEMI(";", "operator"),
    SPREAD_DOT("*.", "operator"),
    STAR("*", "operator"),
    STAR_STAR("**", "operator"),
    TRIPLE_DOT("...", "operator"),

    ASSIGN("=", "operator"),
    BAND_ASSIGN("&=", "operator"),
    BOR_ASSIGN("|=", "operator"),
    BSR_ASSIGN(">>>=", "operator"),
    BXOR_ASSIGN("^=", "operator"),
    DIV_ASSIGN("/=", "operator"),
    MINUS_ASSIGN("-=", "operator"),
    MOD_ASSIGN("%=", "operator"),
    PLUS_ASSIGN("+=", "operator"),
    SL_ASSIGN("<<=", "operator"),
    SR_ASSIGN(">>=", "operator"),
    STAR_ASSIGN("*=", "operator"),
    STAR_STAR_ASSIGN("**=", "operator"),
    
    LITERAL_abstract("abstract", "keyword"),
    LITERAL_as("as", "keyword"),
    LITERAL_assert("assert", "keyword"),
    LITERAL_boolean("boolean", "keyword"),
    LITERAL_break("break", "keyword"),
    LITERAL_byte("byte", "keyword"),
    LITERAL_case("case", "keyword"),
    LITERAL_catch("catch", "keyword"),
    LITERAL_class("class", "keyword"),
    LITERAL_continue("continue", "keyword"),
    LITERAL_def("def", "keyword"),
    LITERAL_default("default", "keyword"),
    LITERAL_double("double", "keyword"),
    LITERAL_else("else", "keyword"),
    LITERAL_enum("enum", "keyword"),
    LITERAL_extends("extends", "keyword"),
    LITERAL_false("false", "keyword"),
    LITERAL_final("final", "keyword"),
    LITERAL_finally("finally", "keyword"),
    LITERAL_float("float", "keyword"),
    LITERAL_for("for", "keyword"),
    LITERAL_char("char", "keyword"),
    LITERAL_if("if", "keyword"),
    LITERAL_implements("implements", "keyword"),
    LITERAL_import("import", "keyword"),
    LITERAL_in("in", "keyword"),
    LITERAL_instanceof("instanceof", "keyword"),
    LITERAL_int("int", "keyword"),
    LITERAL_interface("interface", "keyword"),
    LITERAL_long("long", "keyword"),
    LITERAL_native("native", "keyword"),
    LITERAL_new("new", "keyword"),
    LITERAL_null("null", "keyword"),
    LITERAL_package("package", "keyword"),
    LITERAL_private("private", "keyword"),
    LITERAL_protected("protected", "keyword"),
    LITERAL_public("public", "keyword"),
    LITERAL_return("return", "keyword"),
    LITERAL_short("short", "keyword"),
    LITERAL_static("static", "keyword"),
    LITERAL_super("super", "keyword"),
    LITERAL_switch("switch", "keyword"),
    LITERAL_synchronized("synchronized", "keyword"),
    LITERAL_this("this", "keyword"),
    LITERAL_threadsafe("threadsafe", "keyword"),
    LITERAL_throw("throw", "keyword"),
    LITERAL_throws("throws", "keyword"),
    LITERAL_transient("transient", "keyword"),
    LITERAL_true("true", "keyword"),
    LITERAL_try("try", "keyword"),
    LITERAL_void("void", "keyword"),
    LITERAL_volatile("volatile", "keyword"),
    LITERAL_while("while", "keyword"),

    CLOSURE_OP(null, "operator"),
    CLOSED_BLOCK_OP(null, "default"),

    ANNOTATION_ARRAY_INIT(null, "default"),
    ANNOTATION_DEF(null, "default"),
    ANNOTATION_FIELD_DEF(null, "default"),
    ANNOTATION_MEMBER_VALUE_PAIR(null, "default"),
    ANNOTATION(null, "default"),
    ANNOTATIONS(null, "default"),
    ARRAY_DECLARATOR(null, "default"),
    BIG_SUFFIX(null, "number"),
    BLOCK(null, "default"),
    CASE_GROUP(null, "default"),
    CLASS_DEF(null, "default"),
    CLOSED_BLOCK(null, "default"),
    CTOR_CALL(null, "default"),
    CTOR_IDENT(null, "default"),
    DYNAMIC_MEMBER(null, "default"),
    DOLLAR_REGEXP_CTOR_END(null, "default"),
    DOLLAR_REGEXP_LITERAL(null, "default"),
    DOLLAR_REGEXP_SYMBOL(null, "default"),
    ELIST(null, "default"),
    EMPTY_STAT(null, "default"),
    ENUM_CONSTANT_DEF(null, "default"),
    ENUM_DEF(null, "default"),
    EOF(null, "default"),
    ESC(null, "default"),
    ESCAPED_DOLLAR(null, "default"),
    ESCAPED_SLASH(null, "default"),
    EXPONENT(null, "number"),
    EXPR(null, "default"),
    EXTENDS_CLAUSE(null, "default"),
    FLOAT_SUFFIX(null, "number"),
    FOR_CONDITION(null, "default"),
    FOR_EACH_CLAUSE(null, "default"),
    FOR_IN_ITERABLE(null, "default"),
    FOR_INIT(null, "default"),
    FOR_ITERATOR(null, "default"),
    HEX_DIGIT(null, "number"),
    IDENTICAL(null, "default"),
    IMPLEMENTS_CLAUSE(null, "default"),
    IMPLICIT_PARAMETERS(null, "default"),
    IMPORT(null, "default"),
    INDEX_OP(null, "default"),
    INSTANCE_INIT(null, "default"),
    INTERFACE_DEF(null, "default"),
    LABELED_ARG(null, "default"),
    LABELED_STAT(null, "default"),
    LETTER(null, "default"),
    LIST_CONSTRUCTOR(null, "default"),
    MAP_CONSTRUCTOR(null, "default"),
    METHOD_CALL(null, "default"),
    METHOD_DEF(null, "default"),
    MODIFIERS(null, "default"),
    MULTICATCH(null, "default"),
    MULTICATCH_TYPES(null, "default"),
    NOT_IDENTICAL(null, "default"),
    NLS(null, "default"),
    NULL_TREE_LOOKAHEAD(null, "default"),
    OBJBLOCK(null, "default"),
    ONE_NL(null, "default"),
    PACKAGE_DEF(null, "default"),
    PARAMETER_DEF(null, "default"),
    PARAMETERS(null, "default"),
    POST_DEC(null, "default"),
    POST_INC(null, "default"),
    SELECT_SLOT(null, "default"),
    SLIST(null, "default"),
    SPREAD_ARG(null, "default"),
    SPREAD_MAP_ARG(null, "default"),
    STATIC_IMPORT(null, "default"),
    STATIC_INIT(null, "default"),
    STRICTFP(null, "default"),
    SUPER_CTOR_CALL(null, "default"),
    TYPE_ARGUMENT(null, "default"),
    TYPE_ARGUMENTS(null, "default"),
    TYPE_LOWER_BOUNDS(null, "default"),
    TYPE_PARAMETER(null, "default"),
    TYPE_PARAMETERS(null, "default"),
    TYPE_UPPER_BOUNDS(null, "default"),
    TYPE(null, "default"),
    TYPECAST(null, "default"),
    UNARY_MINUS(null, "default"),
    UNARY_PLUS(null, "default"),
    UNUSED_CONST(null, "default"),
    UNUSED_DO(null, "default"),
    UNUSED_GOTO(null, "default"),
    VARIABLE_DEF(null, "default"),
    VARIABLE_PARAMETER_DEF(null, "default"),
    VOCAB(null, "default"),
    WILDCARD_TYPE(null, "default");

    /**
     * MIME type for Groovy. Don't change this without also consulting the various XML files
     * that cannot reference this value directly.
     */
    public static final String GROOVY_MIME_TYPE = "text/x-groovy"; // NOI18N

    private static final Language<GroovyTokenId> LANGUAGE = new GroovyHierarchy().language();
    private final String primaryCategory;
    private final String fixedText;

    GroovyTokenId(String fixedText, String primaryCategory) {
        this.primaryCategory = primaryCategory;
        this.fixedText = fixedText;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    public static Language<GroovyTokenId> language() {
        return LANGUAGE;
    }

    private static class GroovyHierarchy extends LanguageHierarchy<GroovyTokenId> {

        @Override
        protected String mimeType() {
            return GroovyTokenId.GROOVY_MIME_TYPE;
        }

        @Override
        protected Collection<GroovyTokenId> createTokenIds() {
            return EnumSet.allOf(GroovyTokenId.class);
        }

        @Override
        protected Map<String, Collection<GroovyTokenId>> createTokenCategories() {
            Map<String, Collection<GroovyTokenId>> categories = new HashMap<String, Collection<GroovyTokenId>>();
            for (GroovyTokenId tokenId : EnumSet.allOf(GroovyTokenId.class)) {
                String category = tokenId.primaryCategory();
                Collection<GroovyTokenId> items = categories.get(category);
                if (items == null) {
                    items = new HashSet<GroovyTokenId>();
                    categories.put(category, items);
                }
                items.add(tokenId);
            }

            return categories;
        }

        @Override
        protected Lexer<GroovyTokenId> createLexer(LexerRestartInfo<GroovyTokenId> info) {
            return new GroovyLexer(info);
        }
    }
}
