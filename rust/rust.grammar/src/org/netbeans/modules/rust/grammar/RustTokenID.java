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
package org.netbeans.modules.rust.grammar;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.rust.grammar.antlr4.RustLexer;
import static org.netbeans.modules.rust.grammar.RustTokenIDCategory.*;

/**
 *
 * @author antonio
 */
public enum RustTokenID implements TokenId {
    AND(OPERATOR, RustLexer.AND),
    ANDAND(OPERATOR, RustLexer.ANDAND),
    ANDEQ(OPERATOR, RustLexer.ANDEQ),
    AT(OPERATOR, RustLexer.AT),
    BIN_LITERAL(NUMBER, RustLexer.BIN_LITERAL),
    BLOCK_COMMENT(COMMENT, RustLexer.BLOCK_COMMENT),
    BYTE_LITERAL(NUMBER, RustLexer.BYTE_LITERAL),
    BYTE_STRING_LITERAL(STRING, RustLexer.BYTE_STRING_LITERAL),
    CARET(OPERATOR, RustLexer.CARET),
    CARETEQ(OPERATOR, RustLexer.CARETEQ),
    CHAR_LITERAL(NUMBER, RustLexer.CHAR_LITERAL),
    COLON(OPERATOR, RustLexer.COLON),
    COMMA(SEPARATOR, RustLexer.COMMA),
    DEC_LITERAL(NUMBER, RustLexer.DEC_LITERAL),
    DOLLAR(OPERATOR, RustLexer.DOLLAR),
    DOT(OPERATOR, RustLexer.DOT),
    DOTDOT(OPERATOR, RustLexer.DOTDOT),
    DOTDOTDOT(OPERATOR, RustLexer.DOTDOTDOT),
    DOTDOTEQ(OPERATOR, RustLexer.DOTDOTEQ),
    EQ(OPERATOR, RustLexer.EQ),
    EQEQ(OPERATOR, RustLexer.EQEQ),
    FATARROW(OPERATOR, RustLexer.FATARROW),
    FLOAT_LITERAL(NUMBER, RustLexer.FLOAT_LITERAL),
    GE(OPERATOR, RustLexer.GE),
    GT(OPERATOR, RustLexer.GT),
    HEX_LITERAL(NUMBER, RustLexer.HEX_LITERAL),
    INNER_BLOCK_DOC(COMMENT_HTML, RustLexer.INNER_BLOCK_DOC),
    INNER_LINE_DOC(COMMENT_HTML, RustLexer.INNER_LINE_DOC),
    INTEGER_LITERAL(NUMBER, RustLexer.INTEGER_LITERAL),
    KW_ABSTRACT(KEYWORD, RustLexer.KW_ABSTRACT),
    KW_AS(KEYWORD, RustLexer.KW_AS),
    KW_ASYNC(KEYWORD, RustLexer.KW_ASYNC),
    KW_AWAIT(KEYWORD, RustLexer.KW_AWAIT),
    KW_BECOME(KEYWORD, RustLexer.KW_BECOME),
    KW_BOX(KEYWORD, RustLexer.KW_BOX),
    KW_BREAK(KEYWORD, RustLexer.KW_BREAK),
    KW_CONST(KEYWORD, RustLexer.KW_CONST),
    KW_CONTINUE(KEYWORD, RustLexer.KW_CONTINUE),
    KW_CRATE(KEYWORD, RustLexer.KW_CRATE),
    KW_DO(KEYWORD, RustLexer.KW_DO),
    KW_DOLLARCRATE(KEYWORD, RustLexer.KW_DOLLARCRATE),
    KW_DYN(KEYWORD, RustLexer.KW_DYN),
    KW_ELSE(KEYWORD, RustLexer.KW_ELSE),
    KW_ENUM(KEYWORD, RustLexer.KW_ENUM),
    KW_EXTERN(KEYWORD, RustLexer.KW_EXTERN),
    KW_FALSE(NUMBER, RustLexer.KW_FALSE),
    KW_FINAL(KEYWORD, RustLexer.KW_FINAL),
    KW_FN(KEYWORD, RustLexer.KW_FN),
    KW_FOR(KEYWORD, RustLexer.KW_FOR),
    KW_IF(KEYWORD, RustLexer.KW_IF),
    KW_IMPL(KEYWORD, RustLexer.KW_IMPL),
    KW_IN(KEYWORD, RustLexer.KW_IN),
    KW_LET(KEYWORD, RustLexer.KW_LET),
    KW_LOOP(KEYWORD, RustLexer.KW_LOOP),
    KW_MACRO(KEYWORD, RustLexer.KW_MACRO),
    KW_MACRORULES(KEYWORD, RustLexer.KW_MACRORULES),
    KW_MATCH(KEYWORD, RustLexer.KW_MATCH),
    KW_MOD(KEYWORD, RustLexer.KW_MOD),
    KW_MOVE(KEYWORD, RustLexer.KW_MOVE),
    KW_MUT(KEYWORD, RustLexer.KW_MUT),
    KW_OVERRIDE(KEYWORD, RustLexer.KW_OVERRIDE),
    KW_PRIV(KEYWORD, RustLexer.KW_PRIV),
    KW_PUB(KEYWORD, RustLexer.KW_PUB),
    KW_REF(KEYWORD, RustLexer.KW_REF),
    KW_RETURN(KEYWORD, RustLexer.KW_RETURN),
    KW_SELFTYPE(KEYWORD, RustLexer.KW_SELFTYPE),
    KW_SELFVALUE(KEYWORD, RustLexer.KW_SELFVALUE),
    KW_STATIC(KEYWORD, RustLexer.KW_STATIC),
    KW_STATICLIFETIME(KEYWORD, RustLexer.KW_STATICLIFETIME),
    KW_STRUCT(KEYWORD, RustLexer.KW_STRUCT),
    KW_SUPER(KEYWORD, RustLexer.KW_SUPER),
    KW_TRAIT(KEYWORD, RustLexer.KW_TRAIT),
    KW_TRUE(NUMBER, RustLexer.KW_TRUE),
    KW_TRY(KEYWORD, RustLexer.KW_TRY),
    KW_TYPE(KEYWORD, RustLexer.KW_TYPE),
    KW_TYPEOF(KEYWORD, RustLexer.KW_TYPEOF),
    KW_UNDERLINELIFETIME(KEYWORD, RustLexer.KW_UNDERLINELIFETIME),
    KW_UNION(KEYWORD, RustLexer.KW_UNION),
    KW_UNSAFE(KEYWORD, RustLexer.KW_UNSAFE),
    KW_UNSIZED(KEYWORD, RustLexer.KW_UNSIZED),
    KW_USE(KEYWORD, RustLexer.KW_USE),
    KW_VIRTUAL(KEYWORD, RustLexer.KW_VIRTUAL),
    KW_WHERE(KEYWORD, RustLexer.KW_WHERE),
    KW_WHILE(KEYWORD, RustLexer.KW_WHILE),
    KW_YIELD(KEYWORD, RustLexer.KW_YIELD),
    LCURLYBRACE(SEPARATOR, RustLexer.LCURLYBRACE),
    LE(OPERATOR, RustLexer.LE),
    LIFETIME_OR_LABEL(OPERATOR, RustLexer.LIFETIME_OR_LABEL),
    LINE_COMMENT(COMMENT, RustLexer.LINE_COMMENT),
    LPAREN(SEPARATOR, RustLexer.LPAREN),
    LSQUAREBRACKET(SEPARATOR, RustLexer.LSQUAREBRACKET),
    LT(OPERATOR, RustLexer.LT),
    MINUS(OPERATOR, RustLexer.MINUS),
    MINUSEQ(OPERATOR, RustLexer.MINUSEQ),
    NE(OPERATOR, RustLexer.NE),
    NEWLINE(RustTokenIDCategory.WHITESPACE, RustLexer.NEWLINE),
    NON_KEYWORD_IDENTIFIER(IDENTIFIER, RustLexer.NON_KEYWORD_IDENTIFIER),
    NOT(OPERATOR, RustLexer.NOT),
    OCT_LITERAL(NUMBER, RustLexer.OCT_LITERAL),
    OR(OPERATOR, RustLexer.OR),
    OREQ(OPERATOR, RustLexer.OREQ),
    OROR(OPERATOR, RustLexer.OROR),
    OUTER_LINE_DOC(COMMENT_HTML, RustLexer.OUTER_LINE_DOC),
    PATHSEP(OPERATOR, RustLexer.PATHSEP),
    PERCENT(OPERATOR, RustLexer.PERCENT),
    PERCENTEQ(OPERATOR, RustLexer.PERCENTEQ),
    PLUS(OPERATOR, RustLexer.PLUS),
    PLUSEQ(OPERATOR, RustLexer.PLUSEQ),
    POUND(OPERATOR, RustLexer.POUND),
    QUESTION(OPERATOR, RustLexer.QUESTION),
    RARROW(OPERATOR, RustLexer.RARROW),
    RAW_BYTE_STRING_LITERAL(STRING, RustLexer.RAW_BYTE_STRING_LITERAL),
    RAW_IDENTIFIER(IDENTIFIER, RustLexer.RAW_IDENTIFIER),
    RAW_STRING_LITERAL(STRING, RustLexer.RAW_STRING_LITERAL),
    RCURLYBRACE(SEPARATOR, RustLexer.RCURLYBRACE),
    RPAREN(SEPARATOR, RustLexer.RPAREN),
    RSQUAREBRACKET(SEPARATOR, RustLexer.RSQUAREBRACKET),
    SEMI(SEPARATOR, RustLexer.SEMI),
    SHEBANG(SEPARATOR, RustLexer.SHEBANG),
    SHLEQ(OPERATOR, RustLexer.SHLEQ),
    SHREQ(OPERATOR, RustLexer.SHREQ),
    SLASH(OPERATOR, RustLexer.SLASH),
    SLASHEQ(OPERATOR, RustLexer.SLASHEQ),
    STAR(OPERATOR, RustLexer.STAR),
    STAREQ(OPERATOR, RustLexer.STAREQ),
    STRING_LITERAL(STRING, RustLexer.STRING_LITERAL),
    UNDERSCORE(OPERATOR, RustLexer.UNDERSCORE),
    WHITESPACE(RustTokenIDCategory.WHITESPACE, RustLexer.WHITESPACE),
    EOF(RustTokenIDCategory.EOF, RustLexer.EOF),
    SINGLEQUOTE(RustTokenIDCategory.ERROR, RustLexer.SINGLEQUOTE),
    DOUBLEQUOTE(RustTokenIDCategory.ERROR, RustLexer.DOUBLEQUOTE),
    BACKSLASH(RustTokenIDCategory.ERROR, RustLexer.BACKSLASH),
    ERROR(RustTokenIDCategory.ERROR, RustLexer.VOCABULARY.getMaxTokenType() + 1);

    private static Map<Integer, RustTokenID> ANTLR_2_TOKENID;

    static {
        ANTLR_2_TOKENID = Collections.unmodifiableMap(Arrays.stream(values()).collect(Collectors.toMap(RustTokenID::getValue, Function.identity())));
    }

    public final int value;
    public final RustTokenIDCategory category;

    private RustTokenID(RustTokenIDCategory category, int value) {
        this.value = value;
        this.category = category;
    }

    /**
     * Returns a RustTokenID from an AntlrV4 Token.
     *
     * @param token the Antlrv4 Token.
     * @return The RustTokenID
     * @throws IllegalStateException if there's no such an Antlrv4 token mapping
     * to a RustTokenID.
     */
    public static RustTokenID from(Token token) {
        return fromType(token.getType());
    }

    /**
     * Returns a RustTokenID from an AntlrV4 token type.
     * @param type The type of the token.
     * @return An equivalent RustTokenID.
     */
    public static RustTokenID fromType(int type) {
        RustTokenID rustToken = ANTLR_2_TOKENID.get(type);
        return rustToken == null ? ERROR: rustToken;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String primaryCategory() {
        return category.category;
    }

}
