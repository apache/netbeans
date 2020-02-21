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
package org.netbeans.cnd.api.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.cnd.lexer.FortranLexer;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids of Fortran language defined as enum.
 *
 */
public enum FortranTokenId implements TokenId {

    IDENTIFIER(null, "identifier"), // NOI18N

    // Keywords
//    KW_ACCESS("access", "keyword"), // NOI18N
//    KW_ACTION("action", "keyword"), // NOI18N
//    KW_ADVANCE("advance", "keyword"), // NOI18N
    KW_ALLOCATABLE("allocatable", "keyword"), // NOI18N
    KW_ALLOCATE("allocate", "keyword"), // NOI18N
    KW_APOSTROPHE("apostrophe", "keyword"), // NOI18N
    KW_ASSIGNMENT("assignment", "keyword"), // NOI18N
    KW_ASSOCIATE("associate", "keyword"), // NOI18N
    KW_ASYNCHRONOUS("asynchronous", "keyword"), // NOI18N
    KW_BACKSPACE("backspace", "keyword"), // NOI18N
    KW_BIND("bind", "keyword"), // NOI18N
//    KW_BLANK("blank", "keyword"), // NOI18N
    KW_BLOCK("block", "keyword"), // NOI18N

    KW_BLOCKDATA("blockdata", "keyword"), // NOI18N
    KW_CALL("call", "keyword"), // NOI18N
    KW_CASE("case", "keyword"), // NOI18N
    KW_CHARACTER("character", "keyword"), // NOI18N
    KW_CLASS("class", "keyword"), // NOI18N
    KW_CLOSE("close", "keyword"), // NOI18N
    KW_COMMON("common", "keyword"), // NOI18N
    KW_COMPLEX("complex", "keyword"), // NOI18N
    KW_CONTAINS("contains", "keyword"), // NOI18N
    KW_CONTINUE("continue", "keyword"), // NOI18N
    KW_CYCLE("cycle", "keyword"), // NOI18N
    KW_DATA("data", "keyword"), // NOI18N

    KW_DEALLOCATE("deallocate", "keyword"), // NOI18N
//    KW_DECIMAL("decimal", "keyword"), // NOI18N
    KW_DEFAULT("default", "keyword"), // NOI18N
//    KW_DELIM("delim", "keyword"), // NOI18N
    KW_DIMENSION("dimension", "keyword"), // NOI18N
//    KW_DIRECT("direct", "keyword"), // NOI18N
    KW_DO("do", "keyword"), // NOI18N
    KW_DOUBLE("double", "keyword"), // NOI18N
    KW_DOUBLEPRECISION("doubleprecision", "keyword"), // NOI18N
    KW_ELEMENTAL("elemental", "keyword"), // NOI18N
    KW_ELSE("else", "keyword"), // NOI18N
    KW_ELSEIF("elseif", "keyword"), // NOI18N

    KW_ELSEWHERE("elsewhere", "keyword"), // NOI18N
//    KW_ENCODING("encoding", "keyword"), // NOI18N
    KW_END("end", "keyword"), // NOI18N
    KW_ENDASSOCIATE("endassociate", "keyword"), // NOI18N
    KW_ENDBLOCK("endblock", "keyword"), // NOI18N
    KW_ENDBLOCKDATA("endblockdata", "keyword"), // NOI18N
    KW_ENDDO("enddo", "keyword"), // NOI18N
    KW_ENDENUM("endenum", "keyword"), // NOI18N
    KW_ENDFILE("endfile", "keyword"), // NOI18N
    KW_ENDFORALL("endforall", "keyword"), // NOI18N
    KW_ENDFUNCTION("endfunction", "keyword"), // NOI18N
    KW_ENDIF("endif", "keyword"), // NOI18N

    KW_ENDINTERFACE("endinterface", "keyword"), // NOI18N
    KW_ENDMAP("endmap", "keyword"), // NOI18N
    KW_ENDMODULE("endmodule", "keyword"), // NOI18N
    KW_ENDPROGRAM("endprogram", "keyword"), // NOI18N
    KW_ENDSELECT("endselect", "keyword"), // NOI18N
    KW_ENDSTRUCTURE("endstructure", "keyword"), // NOI18N
    KW_ENDSUBROUTINE("endsubroutine", "keyword"), // NOI18N
    KW_ENDTYPE("endtype", "keyword"), // NOI18N
    KW_ENDUNION("endunion", "keyword"), // NOI18N
    KW_ENDWHERE("endwhere", "keyword"), // NOI18N
    KW_ENDWHILE("endwhile", "keyword"), // NOI18N // extension
    KW_ENTRY("entry", "keyword"), // NOI18N
    KW_ENUM("enum", "keyword"), // NOI18N
    KW_ENUMERATOR("enumerator", "keyword"), // NOI18N
//    KW_EOR("eor", "keyword"), // NOI18N
    KW_EQUIVALENCE("equivalance", "keyword"), // NOI18N

//    KW_ERR("err", "keyword"), // NOI18N
//    KW_ERRMSG("errmsg", "keyword"), // NOI18N
//    KW_EXIST("exist", "keyword"), // NOI18N
    KW_EXIT("exit", "keyword"), // NOI18N
    KW_EXTERNAL("external", "keyword"), // NOI18N
//    KW_FILE("file", "keyword"), // NOI18N
    KW_FLUSH("flush", "keyword"), // NOI18N
//    KW_FMT("fmt", "keyword"), // NOI18N
    KW_FORALL("forall", "keyword"), // NOI18N
//    KW_FORM("form", "keyword"), // NOI18N
    KW_FORMAT("format", "keyword"), // NOI18N
//    KW_FORMATTED("formatted", "keyword"), // NOI18N

    KW_FUNCTION("function", "keyword"), // NOI18N
    KW_GO("go", "keyword"), // NOI18N
    KW_GOTO("goto", "keyword"), // NOI18N
//    KW_ID("id", "keyword"), // NOI18N
    KW_IF("if", "keyword"), // NOI18N
    KW_IMPLICIT("implicit", "keyword"), // NOI18N
    KW_IN("in", "keyword"), // NOI18N
    KW_INCLUDE("include", "keyword"), // NOI18N
    KW_INOUT("inout", "keyword"), // NOI18N
    KW_INQUIRE("inquire", "keyword"), // NOI18N
    KW_INTEGER("integer", "keyword"), // NOI18N

    KW_INTENT("intent", "keyword"), // NOI18N
    KW_INTERFACE("interface", "keyword"), // NOI18N
    KW_INTRINSIC("intrinsic", "keyword"), // NOI18N
//    KW_IOMSG("iomsg", "keyword"), // NOI18N
//    KW_IOSTAT("iostat", "keyword"), // NOI18N
    KW_KIND("kind", "keyword"), // NOI18N
    KW_LEN("len", "keyword"), // NOI18N
    KW_LOGICAL("logical", "keyword"), // NOI18N
    KW_MAP("map", "keyword"), // NOI18N
    KW_MODULE("module", "keyword"), // NOI18N
//    KW_NAME("name", "keyword"), // NOI18N
//    KW_NAMED("named", "keyword"), // NOI18N

    KW_NAMELIST("namelist", "keyword"), // NOI18N
//    KW_NEXTREC("nextrec", "keyword"), // NOI18N
//    KW_NML("nml", "keyword"), // NOI18N
    KW_NONE("none", "keyword"), // NOI18N
    KW_NULLIFY("nullify", "keyword"), // NOI18N
//    KW_NUMBER("number", "keyword"), // NOI18N
    KW_ONLY("only", "keyword"), // NOI18N
    KW_OPEN("open", "keyword"), // NOI18N
//    KW_OPENED("opened", "keyword"), // NOI18N
    KW_OPERATOR("operator", "keyword"), // NOI18N

    KW_OPTIONAL("optional", "keyword"), // NOI18N
    KW_OUT("out", "keyword"), // NOI18N
//    KW_PAD("pad", "keyword"), // NOI18N
    KW_PARAMETER("parameter", "keyword"), // NOI18N
//    KW_PENDING("pending", "keyword"), // NOI18N
    KW_POINTER("pointer", "keyword"), // NOI18N
//    KW_POS("pos", "keyword"), // NOI18N
//    KW_POSITION("position", "keyword"), // NOI18N
    KW_PRECISION("precision", "keyword"), // NOI18N
    KW_PRINT("print", "keyword"), // NOI18N
    KW_PRIVATE("private", "keyword"), // NOI18N
    KW_PROCEDURE("procedure", "keyword"), // NOI18N

    KW_PROGRAM("program", "keyword"), // NOI18N
    KW_PROTECTED("protected", "keyword"), // NOI18N
    KW_PUBLIC("public", "keyword"), // NOI18N
    KW_PURE("pure", "keyword"), // NOI18N
    KW_QUOTE("quote", "keyword"), // NOI18N
    KW_READ("read", "keyword"), // NOI18N
//    KW_READWRITE("readwrite", "keyword"), // NOI18N
    KW_REAL("real", "keyword"), // NOI18N
//    KW_REC("rec", "keyword"), // NOI18N
//    KW_RECL("recl", "keyword"), // NOI18N

    KW_RECURSIVE("recursive", "keyword"), // NOI18N
    KW_RESULT("result", "keyword"), // NOI18N
    KW_RETURN("return", "keyword"), // NOI18N
    KW_REWIND("rewind", "keyword"), // NOI18N
//    KW_ROUND("round", "keyword"), // NOI18N
    KW_SAVE("save", "keyword"), // NOI18N
    KW_SELECT("select", "keyword"), // NOI18N
    KW_SELECTCASE("selectcase", "keyword"), // NOI18N
    KW_SELECTTYPE("selecttype", "keyword"), // NOI18N
    KW_SEQUENCE("sequence", "keyword"), // NOI18N
//    KW_SEQUENTIAL("sequential", "keyword"), // NOI18N
//    KW_SIGN("sign", "keyword"), // NOI18N
//    KW_SIZE("size", "keyword"), // NOI18N

    KW_STAT("stat", "keyword"), // NOI18N
//    KW_STATUS("status", "keyword"), // NOI18N
    KW_STOP("stop", "keyword"), // NOI18N
//    KW_STREAM("stream", "keyword"), // NOI18N
    KW_STRUCTURE("structure", "keyword"), // NOI18N
    KW_SUBROUTINE("subroutine", "keyword"), // NOI18N
    KW_TARGET("target", "keyword"), // NOI18N
    KW_THEN("then", "keyword"), // NOI18N
    KW_TO("to", "keyword"), // NOI18N
    KW_TYPE("type", "keyword"), // NOI18N
//    KW_UNFORMATTED("unformatted", "keyword"), // NOI18N
    KW_UNION("union", "keyword"), // NOI18N
    KW_USE("use", "keyword"), // NOI18N

    KW_VALUE("value", "keyword"), // NOI18N
    KW_VOLATILE("volatile", "keyword"), // NOI18N
    KW_WAIT("wait", "keyword"), // NOI18N
    KW_WHERE("where", "keyword"), // NOI18N
    KW_WHILE("while", "keyword"), // NOI18N
    KW_WRITE("write", "keyword"), // NOI18N

    // Keyword C Extensions
    KW_INT("int", "keyword"), // NOI18N
    KW_SHORT("short", "keyword"), // NOI18N
    KW_LONG("long", "keyword"), // NOI18N
    KW_SIGNED("signed", "keyword"), // NOI18N
    KW_UNSIGNED("unsigned", "keyword"), // NOI18N
    KW_SIZE_T("size_t", "keyword"), // NOI18N
    KW_INT8_T("int8_t", "keyword"), // NOI18N
    KW_INT16_T("int16_t", "keyword"), // NOI18N
    KW_INT32_T("int32_t", "keyword"), // NOI18N
    KW_INT64_T("int64_t", "keyword"), // NOI18N
    KW_INT_LEAST8_T("int_least8_t", "keyword"), // NOI18N
    KW_INT_LEAST16_T("int_least16_t", "keyword"), // NOI18N
    KW_INT_LEAST32_T("int_least32_t", "keyword"), // NOI18N
    KW_INT_LEAST64_T("int_least64_t", "keyword"), // NOI18N
    KW_INT_FAST8_T("int_fast8_t", "keyword"), // NOI18N
    KW_INT_FAST16_T("int_fast16_t", "keyword"), // NOI18N
    KW_INT_FAST32_T("int_fast32_t", "keyword"), // NOI18N
    KW_INT_FAST64_T("int_fast64_t", "keyword"), // NOI18N
    KW_INTMAX_T("intmax_t", "keyword"), // NOI18N
    KW_INTPTR_T("intptr_t", "keyword"), // NOI18N
    KW_FLOAT("float", "keyword"), // NOI18N
//    KW_DOUBLE("double", "keyword"), // NOI18N // included as part of double precision
    KW__COMPLEX("_Complex", "keyword"), // NOI18N
    KW__BOOL("_Bool", "keyword"), // NOI18N
    KW_CHAR("char", "keyword"), // NOI18N
    KW_BOOL("bool", "keyword"), // NOI18N

    // Keyword Operators
    KWOP_EQ(".eq.", "keyword-operator"), //NOI18N
    KWOP_NE(".ne.", "keyword-operator"), //NOI18N
    KWOP_LT(".lt.", "keyword-operator"), //NOI18N
    KWOP_LE(".le.", "keyword-operator"), //NOI18N
    KWOP_GT(".gt.", "keyword-operator"), //NOI18N
    KWOP_GE(".ge.", "keyword-operator"), //NOI18N
    KWOP_AND(".and.", "keyword-operator"), //NOI18N
    KWOP_OR(".or.", "keyword-operator"), //NOI18N
    KWOP_NOT(".not.", "keyword-operator"), //NOI18N
    KWOP_EQV(".eqv.", "keyword-operator"), //NOI18N
    KWOP_NEQV(".neqv.", "keyword-operator"), //NOI18N
    KWOP_TRUE(".true.", "keyword-operator"), //NOI18N
    KWOP_FALSE(".false.", "keyword-operator"), //NOI18N

    // Operators
    OP_POWER("**", "operator"), // NOI18N
    OP_MUL("*", "operator"), // NOI18N
    OP_DIV("/", "operator"), // NOI18N
    OP_PLUS("+", "operator"), // NOI18N
    OP_MINUS("-", "operator"), // NOI18N
    OP_CONCAT("//", "operator"), // NOI18N
    OP_LOG_EQ("==", "operator"), // NOI18N
    OP_NOT_EQ("/=", "operator"), // NOI18N
    OP_LT("<", "operator"), // NOI18N
    OP_LT_EQ("<=", "operator"), // NOI18N
    OP_GT(">", "operator"), // NOI18N
    OP_GT_EQ(">=", "operator"), // NOI18N
    OP_LT_GT("<>", "operator"), // NOI18N

    // Special Characters
    EQ("=", "special"), // NOI18N
    DOT(".", "special"), // NOI18N
    COMMA(",", "special"), // NOI18N
    COLON(":", "special"), // NOI18N
    DOUBLECOLON("::", "special"), // NOI18N
    LPAREN("(", "special"), // NOI18N
    RPAREN(")", "special"), // NOI18N
    APOSTROPHE_CHAR("'", "special"), // NOI18N
    EXCLAMATION("!", "special"), // NOI18N
    QUOTATION("\"", "special"), // NOI18N
    PERCENT("%", "special"), // NOI18N
    AMPERSAND("&", "special"), // NOI18N
    SEMICOLON(";", "special"), // NOI18N
    QUESTION_MARK("?", "special"), // NOI18N
    CURRENCY("$", "special"), // NOI18N

    // Numeric Literals
    NUM_LITERAL_INT(null, "number"), // NOI18N
    NUM_LITERAL_REAL(null, "number"), // NOI18N
    NUM_LITERAL_COMPLEX(null, "number"), // NOI18N
    NUM_LITERAL_BINARY(null, "number"), // NOI18N
    NUM_LITERAL_HEX(null, "number"), // NOI18N
    NUM_LITERAL_OCTAL(null, "number"), // NOI18N

    STRING_LITERAL(null, "string"), // NOI18N

    WHITESPACE(null, "whitespace"), // NOI18N
    NEW_LINE(null, "whitespace"), // NOI18N
    LINE_COMMENT_FIXED(null, "comment"), // NOI18N
    LINE_COMMENT_FREE(null, "comment"), // NOI18N
    LINE_CONTINUATION_FIXED(null, "continuation"), // NOI18N

    // Errors
    ERROR(null, "error"), // NOI18N
    ERR_INVALID_HEX_LITERAL(null, "error"), // NOI18N
    ERR_INVALID_OCTAL_LITERAL(null, "error"), // NOI18N
    ERR_INVALID_BINARY_LITERAL(null, "error"), // NOI18N
    ERR_INVALID_CHAR(null, "error"), // NOI18N
    ERR_INVALID_INTEGER(null, "error"), // NOI18N
    ERR_INCOMPLETE_STRING_LITERAL(null, "error"), // NOI18N

    // Prerpocessor
    //   - on top level
    PREPROCESSOR_DIRECTIVE(null, "preprocessor"); // NOI18N

    // Make sure string names are the same used in the tokenIds above
    public static final String WHITESPACE_CATEGORY = "whitespace"; // NOI18N
    public static final String COMMENT_CATEGORY = "comment"; // NOI18N
    public static final String KEYWORD_CATEGORY = "keyword"; // NOI18N
    public static final String KEYWORD_OPERATOR_CATEGORY = "keyword-operator"; // NOI18N
    public static final String ERROR_CATEGORY = "error"; // NOI18N
    public static final String NUMBER_CATEGORY = "number"; // NOI18N
    public static final String LITERAL_CATEGORY = "literal"; // NOI18N
    public static final String STRING_CATEGORY = "string"; // NOI18N
    public static final String OPERATOR_CATEGORY = "operator"; // NOI18N
    public static final String SPECIAL_CATEGORY = "special"; // NOI18N
    public static final String PREPROCESSOR_CATEGORY = "preprocessor"; // NOI18N

    private final String fixedText;
    private final String primaryCategory;

    FortranTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    private static final Language<FortranTokenId> languageFortran;


    static {
        languageFortran = FortranHierarchy.createFortranLanguage();
    }

    public static Language<FortranTokenId> languageFortran() {
        return languageFortran;
    }

    private static final class FortranHierarchy extends LanguageHierarchy<FortranTokenId> {

        private FortranHierarchy() {
        }

        private static Language<FortranTokenId> createFortranLanguage() {
            return new FortranHierarchy().language();
        }

        @Override
        protected String mimeType() {
            return MIMENames.FORTRAN_MIME_TYPE;
        }

        @Override
        protected Collection<FortranTokenId> createTokenIds() {
            return EnumSet.allOf(FortranTokenId.class);
        }

        @Override
        protected Map<String, Collection<FortranTokenId>> createTokenCategories() {
            Map<String, Collection<FortranTokenId>> cats = new HashMap<String, Collection<FortranTokenId>>();
            // Additional literals being a lexical error
            cats.put(ERROR_CATEGORY, EnumSet.of(
                    FortranTokenId.ERR_INCOMPLETE_STRING_LITERAL,
                    FortranTokenId.ERR_INVALID_BINARY_LITERAL,
                    FortranTokenId.ERR_INVALID_CHAR,
                    FortranTokenId.ERR_INVALID_HEX_LITERAL,
                    FortranTokenId.ERR_INVALID_INTEGER,
                    FortranTokenId.ERR_INVALID_OCTAL_LITERAL));
            // Literals category
            EnumSet<FortranTokenId> l = EnumSet.of(
                    FortranTokenId.NUM_LITERAL_INT,
                    FortranTokenId.NUM_LITERAL_BINARY,
                    FortranTokenId.NUM_LITERAL_COMPLEX,
                    FortranTokenId.NUM_LITERAL_HEX,
                    FortranTokenId.NUM_LITERAL_OCTAL,
                    FortranTokenId.NUM_LITERAL_REAL,
                    FortranTokenId.STRING_LITERAL);
            cats.put(LITERAL_CATEGORY, l);
            return cats;
        }

        @Override
        protected Lexer<FortranTokenId> createLexer(LexerRestartInfo<FortranTokenId> info) {
            return new FortranLexer(CndLexerUtilities.getFortranFilter(), info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
                Token<FortranTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment and string literal
            switch (token.id()) {
                case PREPROCESSOR_DIRECTIVE:
                    return LanguageEmbedding.create(CppTokenId.languagePreproc(), 0, 0);
            }
            return null; // No embedding
        }
    }
}
