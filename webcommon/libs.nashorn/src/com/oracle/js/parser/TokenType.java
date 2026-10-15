/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oracle.js.parser;

import static com.oracle.js.parser.TokenKind.BINARY;
import static com.oracle.js.parser.TokenKind.BRACKET;
import static com.oracle.js.parser.TokenKind.FUTURE;
import static com.oracle.js.parser.TokenKind.FUTURESTRICT;
import static com.oracle.js.parser.TokenKind.IR;
import static com.oracle.js.parser.TokenKind.JSX;
import static com.oracle.js.parser.TokenKind.KEYWORD;
import static com.oracle.js.parser.TokenKind.LITERAL;
import static com.oracle.js.parser.TokenKind.SPECIAL;
import static com.oracle.js.parser.TokenKind.UNARY;

import java.util.Locale;

// @formatter:off
// Checkstyle: stop
/**
 * Description of all the JavaScript tokens.
 */
public enum TokenType {
    ERROR                (SPECIAL,  null),
    EOF                  (SPECIAL,  null),
    EOL                  (SPECIAL,  null),
    COMMENT              (SPECIAL,  null),
    // comments of the form //@ foo=bar or //# foo=bar
    // These comments are treated as special instructions
    // to the lexer, parser or codegenerator.
    DIRECTIVE_COMMENT    (SPECIAL,  null),

    NOT            (UNARY,   "!",    15, false),
    NE             (BINARY,  "!=",    9, true),
    NE_STRICT      (BINARY,  "!==",   9, true),
    MOD            (BINARY,  "%",    13, true),
    ASSIGN_MOD     (BINARY,  "%=",    2, false),
    BIT_AND        (BINARY,  "&",     8, true),
    AND            (BINARY,  "&&",    5, true),
    ASSIGN_BIT_AND (BINARY,  "&=",    2, false),
    LPAREN         (BRACKET, "(",    17, true),
    RPAREN         (BRACKET, ")",     0, true),
    MUL            (BINARY,  "*",    13, true),
    ASSIGN_MUL     (BINARY,  "*=",    2, false),
    EXP            (BINARY,  "**",   14, false),
    ASSIGN_EXP     (BINARY,  "**=",   2, false),
    ADD            (BINARY,  "+",    12, true),
    INCPREFIX      (UNARY,   "++",   16, true),
    ASSIGN_ADD     (BINARY,  "+=",    2, false),
    COMMARIGHT     (BINARY,  ",",     1, true),
    SUB            (BINARY,  "-",    12, true),
    DECPREFIX      (UNARY,   "--",   16, true),
    ASSIGN_SUB     (BINARY,  "-=",    2, false),
    PERIOD         (BRACKET, ".",    18, true),
    OPTIONAL_ACCESS(BRACKET, "?.",   18, true, 11),
    DIV            (BINARY,  "/",    13, true),
    ASSIGN_DIV     (BINARY,  "/=",    2, false),
    COLON          (BINARY,  ":"),
    SEMICOLON      (BINARY,  ";"),
    LT             (BINARY,  "<",    10, true),
    SHL            (BINARY,  "<<",   11, true),
    ASSIGN_SHL     (BINARY,  "<<=",   2, false),
    LE             (BINARY,  "<=",   10, true),
    ASSIGN         (BINARY,  "=",     2, false),
    EQ             (BINARY,  "==",    9, true),
    EQ_STRICT      (BINARY,  "===",   9, true),
    ARROW          (BINARY,  "=>",    2, true),
    GT             (BINARY,  ">",    10, true),
    GE             (BINARY,  ">=",   10, true),
    SAR            (BINARY,  ">>",   11, true),
    ASSIGN_SAR     (BINARY,  ">>=",   2, false),
    SHR            (BINARY,  ">>>",  11, true),
    ASSIGN_SHR     (BINARY,  ">>>=",  2, false),
    TERNARY        (BINARY,  "?",     3, false),
    LBRACKET       (BRACKET, "[",    18, true),
    RBRACKET       (BRACKET, "]",     0, true),
    BIT_XOR        (BINARY,  "^",     7, true),
    ASSIGN_BIT_XOR (BINARY,  "^=",    2, false),
    LBRACE         (BRACKET,  "{"),
    BIT_OR         (BINARY,  "|",     6, true),
    ASSIGN_BIT_OR  (BINARY,  "|=",    2, false),
    OR             (BINARY,  "||",    4, true),
    RBRACE         (BRACKET, "}"),
    BIT_NOT        (UNARY,   "~",     15, false),
    ELLIPSIS       (UNARY,   "..."),
    AT             (UNARY,   "@"),
    ASSIGN_LOG_AND (BINARY,  "&&=",    2, false, 12),
    ASSIGN_LOG_OR  (BINARY,  "||=",    2, false, 12),
    ASSIGN_NULLISH (BINARY,  "??=",    2, false, 12),
    NULLISH        (BINARY,  "??",     4, true, 11),

    // ECMA 7.6.1.1 Keywords, 7.6.1.2 Future Reserved Words.
    // All other Java keywords are commented out.

//  ABSTRACT       (FUTURE,   "abstract"),
//  BOOLEAN        (FUTURE,   "boolean"),
    BREAK          (KEYWORD,  "break"),
//  BYTE           (FUTURE,   "byte"),
    CASE           (KEYWORD,  "case"),
    CATCH          (KEYWORD,  "catch"),
//  CHAR           (FUTURE,   "char"),
    CLASS          (FUTURE,   "class"),
    CONST          (KEYWORD,  "const"),
    CONTINUE       (KEYWORD,  "continue"),
    DEBUGGER       (KEYWORD,  "debugger"),
    DEFAULT        (KEYWORD,  "default"),
    DELETE         (UNARY,    "delete",     15, false),
    DO             (KEYWORD,  "do"),
//  DOUBLE         (FUTURE,   "double"),
//  EACH           (KEYWORD,  "each"),  // Contextual.
    ELSE           (KEYWORD,  "else"),
    ENUM           (FUTURE,   "enum"),
    EXPORT         (FUTURE,   "export"),
    EXTENDS        (FUTURE,   "extends"),
    FALSE          (LITERAL,  "false"),
//  FINAL          (FUTURE,   "final"),
    FINALLY        (KEYWORD,  "finally"),
//  FLOAT          (FUTURE,   "float"),
    FOR            (KEYWORD,  "for"),
    FUNCTION       (KEYWORD,  "function"),
//  GET            (KEYWORD,  "get"), // Contextual.
//  GOTO           (FUTURE,   "goto"),
    IF             (KEYWORD,   "if"),
    IMPLEMENTS     (FUTURESTRICT,   "implements"),
    IMPORT         (FUTURE,   "import"),
    IN             (BINARY,   "in",         10, true),
    INSTANCEOF     (BINARY,   "instanceof", 10, true),
//  INT            (FUTURE,   "int"),
    INTERFACE      (FUTURESTRICT,   "interface"),
    LET            (FUTURESTRICT,   "let"),
//  LONG           (FUTURE,   "long"),
//  NATIVE         (FUTURE,   "native"),
    NEW            (UNARY,    "new",        18, false),
    NULL           (LITERAL,  "null"),
    PACKAGE        (FUTURESTRICT,   "package"),
    PRIVATE        (FUTURESTRICT,   "private"),
    PROTECTED      (FUTURESTRICT,   "protected"),
    PUBLIC         (FUTURESTRICT,   "public"),
    RETURN         (KEYWORD,  "return"),
//  SET            (KEYWORD,  "set"), // Contextual.
//  SHORT          (FUTURE,   "short"),
    STATIC         (FUTURESTRICT,   "static"),
    SUPER          (FUTURE,   "super"),
    SWITCH         (KEYWORD,  "switch"),
//  SYNCHRONIZED   (FUTURE,   "synchronized"),
    THIS           (KEYWORD,  "this"),
    THROW          (KEYWORD,  "throw"),
//  THROWS         (FUTURE,   "throws"),
//  TRANSIENT      (FUTURE,   "transient"),
    TRUE           (LITERAL,  "true"),
    TRY            (KEYWORD,  "try"),
    TYPEOF         (UNARY,    "typeof",     15, false),
    VAR            (KEYWORD,  "var"),
    VOID           (UNARY,    "void",       15, false),
//  VOLATILE       (FUTURE,   "volatile"),
    WHILE          (KEYWORD,  "while"),
    WITH           (KEYWORD,  "with"),
    YIELD          (FUTURESTRICT,  "yield"),

    DECIMAL        (LITERAL,  null),
    BIGINT         (LITERAL,  null),
    HEXADECIMAL    (LITERAL,  null),
    OCTAL_LEGACY   (LITERAL,  null),
    OCTAL          (LITERAL,  null),
    BINARY_NUMBER  (LITERAL,  null),
    FLOATING       (LITERAL,  null),
    STRING         (LITERAL,  null),
    ESCSTRING      (LITERAL,  null),
    EXECSTRING     (LITERAL,  null),
    IDENT          (LITERAL,  null),
    REGEX          (LITERAL,  null),
    XML            (LITERAL,  null),
    OBJECT         (LITERAL,  null),
    ARRAY          (LITERAL,  null),
    TEMPLATE       (LITERAL,  null),
    TEMPLATE_HEAD  (LITERAL,  null),
    TEMPLATE_MIDDLE(LITERAL,  null),
    TEMPLATE_TAIL  (LITERAL,  null),

    COMMALEFT      (IR,       null),
    DECPOSTFIX     (IR,       null),
    INCPOSTFIX     (IR,       null),
    SPREAD_ARGUMENT(IR,       null),
    SPREAD_ARRAY   (IR,       null),
    SPREAD_OBJECT  (IR,       null),
    YIELD_STAR     (IR,       null),
    AWAIT          (IR,       null),

    JSX_IDENTIFIER   (JSX,  null),
    JSX_TEXT         (JSX,  null),
    JSX_STRING       (JSX,  null),
    JSX_ELEM_START   (JSX,  "<"),
    JSX_ELEM_END     (JSX,  ">"),
    JSX_ELEM_CLOSE   (JSX,  "/")
    ;

    /** Next token kind in token lookup table. */
    private TokenType next;

    /** Classification of token. */
    private final TokenKind kind;

    /** Printable name of token. */
    private final String name;

    /** Operator precedence. */
    private final int precedence;

    /** Left associativity */
    private final boolean isLeftAssociative;

    private final int ecmascriptEdition;

    /** Cache values to avoid cloning. */
    private static final TokenType[] values;

    TokenType(final TokenKind kind, final String name) {
        this.next              = null;
        this.kind              = kind;
        this.name              = name;
        this.precedence        = 0;
        this.isLeftAssociative = false;
        this.ecmascriptEdition = 0;
    }

    TokenType(final TokenKind kind, final String name, final int precedence, final boolean isLeftAssociative) {
        this.next              = null;
        this.kind              = kind;
        this.name              = name;
        this.precedence        = precedence;
        this.isLeftAssociative = isLeftAssociative;
        this.ecmascriptEdition = 0;
    }

    TokenType(final TokenKind kind, final String name, final int precedence, final boolean isLeftAssociative, int ecmascriptEdition) {
        this.next              = null;
        this.kind              = kind;
        this.name              = name;
        this.precedence        = precedence;
        this.isLeftAssociative = isLeftAssociative;
        this.ecmascriptEdition = ecmascriptEdition;
    }

    /**
     * Determines if the token has greater precedence than other.
     *
     * @param other  Compare token.
     * @param isLeft Is to the left of the other.
     *
     * @return {@code true} if greater precedence.
     */
    public boolean needsParens(final TokenType other, final boolean isLeft) {
        return other.precedence != 0 &&
               (precedence > other.precedence ||
               precedence == other.precedence && isLeftAssociative && !isLeft);
    }

    /**
     * Determines if the type is a valid operator.
     *
     * @param noIn {@code true} if IN operator should be ignored.
     *
     * @return {@code true} if valid operator.
     */
    public boolean isOperator(final boolean noIn) {
        return kind == BINARY && (!noIn || this != IN) && precedence != 0;
    }

    public int getLength() {
        assert name != null : "Token name not set";
        return name.length();
    }

    public String getName() {
        return name;
    }

    public String getNameOrType() {
        return name == null ? super.name().toLowerCase(Locale.ENGLISH) : name;
    }

    public TokenType getNext() {
        return next;
    }

    public void setNext(final TokenType next) {
        this.next = next;
    }

    public TokenKind getKind() {
        return kind;
    }

    public int getPrecedence() {
        return precedence;
    }

    public boolean isLeftAssociative() {
        return isLeftAssociative;
    }

    boolean startsWith(final char c) {
        return name != null && name.length() > 0 && name.charAt(0) == c;
    }

    public int getEcmascriptEdition() {
        return ecmascriptEdition;
    }

    static TokenType[] getValues() {
       return values;
    }

    @Override
    public String toString() {
        return getNameOrType();
    }

    /**
     * Is type one of {@code = *= /= %= += -= <<= >>= >>>= &= ^= |= **=}?
     */
    public boolean isAssignment() {
        switch (this) {
        case ASSIGN:
        case ASSIGN_ADD:
        case ASSIGN_BIT_AND:
        case ASSIGN_BIT_OR:
        case ASSIGN_BIT_XOR:
        case ASSIGN_DIV:
        case ASSIGN_MOD:
        case ASSIGN_EXP:
        case ASSIGN_MUL:
        case ASSIGN_SAR:
        case ASSIGN_SHL:
        case ASSIGN_SHR:
        case ASSIGN_SUB:
        case ASSIGN_LOG_AND:
        case ASSIGN_LOG_OR:
        case ASSIGN_NULLISH:
           return true;
        default:
           return false;
        }
    }

    public boolean isSupported(int targetEcmascriptEdition) {
        return ecmascriptEdition <= targetEcmascriptEdition;
    }

    static {
        // Avoid cloning of enumeration.
        values = TokenType.values();
    }
}
