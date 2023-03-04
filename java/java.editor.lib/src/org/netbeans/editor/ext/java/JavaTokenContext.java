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

package org.netbeans.editor.ext.java;

import java.util.HashMap;
import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.Utilities;

/**
* Java token-context defines token-ids and token-categories
* used in Java language.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaTokenContext extends TokenContext {

    // Token category-ids
    public static final int KEYWORDS_ID           = 1;
    public static final int OPERATORS_ID          = KEYWORDS_ID + 1;
    public static final int NUMERIC_LITERALS_ID   = OPERATORS_ID + 1;
    public static final int ERRORS_ID             = NUMERIC_LITERALS_ID + 1;

    // Numeric-ids for token-ids
    public static final int WHITESPACE_ID         = ERRORS_ID + 1;
    public static final int IDENTIFIER_ID         = WHITESPACE_ID + 1;
    public static final int LINE_COMMENT_ID       = IDENTIFIER_ID + 1;
    public static final int BLOCK_COMMENT_ID      = LINE_COMMENT_ID + 1;
    public static final int CHAR_LITERAL_ID       = BLOCK_COMMENT_ID + 1;
    public static final int STRING_LITERAL_ID     = CHAR_LITERAL_ID + 1;
    public static final int INT_LITERAL_ID        = STRING_LITERAL_ID + 1;
    public static final int LONG_LITERAL_ID       = INT_LITERAL_ID + 1;
    public static final int HEX_LITERAL_ID        = LONG_LITERAL_ID + 1;
    public static final int OCTAL_LITERAL_ID      = HEX_LITERAL_ID + 1;
    public static final int FLOAT_LITERAL_ID      = OCTAL_LITERAL_ID + 1;
    public static final int DOUBLE_LITERAL_ID     = FLOAT_LITERAL_ID + 1;

    // Operator numeric-ids
    public static final int EQ_ID = DOUBLE_LITERAL_ID + 1; // =
    public static final int LT_ID = EQ_ID + 1;            // <
    public static final int GT_ID = LT_ID + 1;            // >
    public static final int LSHIFT_ID = GT_ID + 1;        // <<
    public static final int RSSHIFT_ID = LSHIFT_ID + 1;   // >>
    public static final int RUSHIFT_ID = RSSHIFT_ID + 1;  // >>>
    public static final int PLUS_ID = RUSHIFT_ID + 1;     // +
    public static final int MINUS_ID = PLUS_ID + 1;       // -
    public static final int MUL_ID = MINUS_ID + 1;        // *
    public static final int DIV_ID = MUL_ID + 1;          // /
    public static final int AND_ID = DIV_ID + 1;          // &
    public static final int OR_ID = AND_ID + 1;           // |
    public static final int XOR_ID = OR_ID + 1;           // ^
    public static final int MOD_ID = XOR_ID + 1;          // %
    public static final int NOT_ID = MOD_ID + 1;          // !
    public static final int NEG_ID = NOT_ID + 1;          // ~
    public static final int EQ_EQ_ID = NEG_ID + 1;        // ==
    public static final int LT_EQ_ID = EQ_EQ_ID + 1;      // <=
    public static final int GT_EQ_ID = LT_EQ_ID + 1;         // >=
    public static final int LSHIFT_EQ_ID = GT_EQ_ID + 1;  // <<=
    public static final int RSSHIFT_EQ_ID = LSHIFT_EQ_ID + 1; // >>=
    public static final int RUSHIFT_EQ_ID = RSSHIFT_EQ_ID + 1; // >>>=
    public static final int PLUS_EQ_ID = RUSHIFT_EQ_ID + 1; // +=
    public static final int MINUS_EQ_ID = PLUS_EQ_ID + 1; // -=
    public static final int MUL_EQ_ID = MINUS_EQ_ID + 1;  // *=
    public static final int DIV_EQ_ID = MUL_EQ_ID + 1;    // /=
    public static final int AND_EQ_ID = DIV_EQ_ID + 1;    // &=
    public static final int OR_EQ_ID = AND_EQ_ID + 1;     // |=
    public static final int XOR_EQ_ID = OR_EQ_ID + 1;     // ^=
    public static final int MOD_EQ_ID = XOR_EQ_ID + 1;    // %=
    public static final int NOT_EQ_ID = MOD_EQ_ID + 1;    // !=
    public static final int DOT_ID = NOT_EQ_ID + 1;       // .
    public static final int COMMA_ID = DOT_ID + 1;        // ,
    public static final int COLON_ID = COMMA_ID + 1;      // :
    public static final int SEMICOLON_ID = COLON_ID + 1;  // ;
    public static final int QUESTION_ID = SEMICOLON_ID + 1; // ?
    public static final int LPAREN_ID = QUESTION_ID + 1;  // (
    public static final int RPAREN_ID = LPAREN_ID + 1;    // )
    public static final int LBRACKET_ID = RPAREN_ID + 1;  // [
    public static final int RBRACKET_ID = LBRACKET_ID + 1; // ]
    public static final int LBRACE_ID = RBRACKET_ID + 1;  // {
    public static final int RBRACE_ID = LBRACE_ID + 1;    // }
    public static final int PLUS_PLUS_ID = RBRACE_ID + 1; // ++
    public static final int MINUS_MINUS_ID = PLUS_PLUS_ID + 1; // --
    public static final int AND_AND_ID = MINUS_MINUS_ID + 1; // &&
    public static final int OR_OR_ID = AND_AND_ID + 1;    // ||

    // Data type keyword numeric-ids
    public static final int BOOLEAN_ID = OR_OR_ID + 1;
    public static final int BYTE_ID = BOOLEAN_ID + 1;
    public static final int CHAR_ID = BYTE_ID + 1;
    public static final int DOUBLE_ID = CHAR_ID + 1;
    public static final int FLOAT_ID = DOUBLE_ID + 1;
    public static final int INT_ID = FLOAT_ID + 1;
    public static final int LONG_ID = INT_ID + 1;
    public static final int SHORT_ID = LONG_ID + 1;

    // Void type keyword numeric-id
    public static final int VOID_ID = SHORT_ID + 1;

    // Other keywords numeric-ids
    public static final int ABSTRACT_ID = VOID_ID + 1;
    public static final int ASSERT_ID = ABSTRACT_ID + 1;
    public static final int BREAK_ID = ASSERT_ID + 1;
    public static final int CASE_ID = BREAK_ID + 1;
    public static final int CATCH_ID = CASE_ID + 1;
    public static final int CLASS_ID = CATCH_ID + 1;
    public static final int CONST_ID = CLASS_ID + 1;
    public static final int CONTINUE_ID = CONST_ID + 1;
    public static final int DEFAULT_ID = CONTINUE_ID + 1;
    public static final int DO_ID = DEFAULT_ID + 1;
    public static final int ELSE_ID = DO_ID + 1;
    public static final int ENUM_ID = ELSE_ID + 1;
    public static final int EXTENDS_ID = ENUM_ID+ 1;
    public static final int FALSE_ID = EXTENDS_ID + 1;
    public static final int FINAL_ID = FALSE_ID + 1;
    public static final int FINALLY_ID = FINAL_ID + 1;
    public static final int FOR_ID = FINALLY_ID + 1;
    public static final int GOTO_ID = FOR_ID + 1;
    public static final int IF_ID = GOTO_ID + 1;
    public static final int IMPLEMENTS_ID = IF_ID + 1;
    public static final int IMPORT_ID = IMPLEMENTS_ID + 1;
    public static final int INSTANCEOF_ID = IMPORT_ID + 1;
    public static final int INTERFACE_ID = INSTANCEOF_ID + 1;
    public static final int NATIVE_ID = INTERFACE_ID + 1;
    public static final int NEW_ID = NATIVE_ID + 1;
    public static final int NULL_ID = NEW_ID + 1;
    public static final int PACKAGE_ID = NULL_ID + 1;
    public static final int PRIVATE_ID = PACKAGE_ID + 1;
    public static final int PROTECTED_ID = PRIVATE_ID + 1;
    public static final int PUBLIC_ID = PROTECTED_ID + 1;
    public static final int RETURN_ID = PUBLIC_ID + 1;
    public static final int STATIC_ID = RETURN_ID + 1;
    public static final int STRICTFP_ID = STATIC_ID + 1;
    public static final int SUPER_ID = STRICTFP_ID + 1;
    public static final int SWITCH_ID = SUPER_ID + 1;
    public static final int SYNCHRONIZED_ID = SWITCH_ID + 1;
    public static final int THIS_ID = SYNCHRONIZED_ID + 1;
    public static final int THROW_ID = THIS_ID + 1;
    public static final int THROWS_ID = THROW_ID + 1;
    public static final int TRANSIENT_ID = THROWS_ID + 1;
    public static final int TRUE_ID = TRANSIENT_ID + 1;
    public static final int TRY_ID = TRUE_ID + 1;
    public static final int VOLATILE_ID = TRY_ID + 1;
    public static final int WHILE_ID = VOLATILE_ID + 1;

    // Incomplete tokens
    public static final int INCOMPLETE_STRING_LITERAL_ID = WHILE_ID + 1;
    public static final int INCOMPLETE_CHAR_LITERAL_ID = INCOMPLETE_STRING_LITERAL_ID + 1;
    public static final int INCOMPLETE_HEX_LITERAL_ID = INCOMPLETE_CHAR_LITERAL_ID + 1;
    public static final int INVALID_CHAR_ID = INCOMPLETE_HEX_LITERAL_ID + 1;
    public static final int INVALID_OPERATOR_ID = INVALID_CHAR_ID + 1;
    public static final int INVALID_OCTAL_LITERAL_ID = INVALID_OPERATOR_ID + 1;
    public static final int INVALID_COMMENT_END_ID = INVALID_OCTAL_LITERAL_ID + 1;
    
    // JDK1.5 "@ident" annotation
    // "@keyword" gets returned as two tokens "@" and "keyword"
    public static final int ANNOTATION_ID = INVALID_COMMENT_END_ID + 1;

    // JDK1.5 vararg's "..."
    public static final int ELLIPSIS_ID = ANNOTATION_ID + 1;


    // Token-categories
    /** All the keywords belong to this category. */
    public static final BaseTokenCategory KEYWORDS
    = new BaseTokenCategory("keywords", KEYWORDS_ID); // NOI18N

    /** All the operators belong to this category. */
    public static final BaseTokenCategory OPERATORS
    = new BaseTokenCategory("operators", OPERATORS_ID); // NOI18N

    /** All the numeric literals belong to this category. */
    public static final BaseTokenCategory NUMERIC_LITERALS
    = new BaseTokenCategory("numeric-literals", NUMERIC_LITERALS_ID); // NOI18N

    /** All the errorneous constructions and incomplete tokens
     * belong to this category.
     */
    public static final BaseTokenCategory ERRORS
    = new BaseTokenCategory("errors", ERRORS_ID); // NOI18N


    // Token-ids
    public static final BaseTokenID WHITESPACE
    = new BaseTokenID("whitespace", WHITESPACE_ID); // NOI18N

    public static final BaseTokenID IDENTIFIER
    = new BaseTokenID("identifier", IDENTIFIER_ID); // NOI18N

    /** Comment with the '//' prefix */
    public static final BaseTokenID LINE_COMMENT
    = new BaseTokenID("line-comment", LINE_COMMENT_ID); // NOI18N

    /** Block comment */
    public static final BaseTokenID BLOCK_COMMENT
    = new BaseTokenID("block-comment", BLOCK_COMMENT_ID); // NOI18N

    /** Character literal e.g. 'c' */
    public static final BaseTokenID CHAR_LITERAL
    = new BaseTokenID("char-literal", CHAR_LITERAL_ID); // NOI18N

    /** Java string literal e.g. "hello" */
    public static final BaseTokenID STRING_LITERAL
    = new BaseTokenID("string-literal", STRING_LITERAL_ID); // NOI18N

    /** Java integer literal e.g. 1234 */
    public static final BaseTokenID INT_LITERAL
    = new BaseTokenID("int-literal", INT_LITERAL_ID, NUMERIC_LITERALS); // NOI18N

    /** Java long literal e.g. 12L */
    public static final BaseTokenID LONG_LITERAL
    = new BaseTokenID("long-literal", LONG_LITERAL_ID, NUMERIC_LITERALS); // NOI18N

    /** Java hexadecimal literal e.g. 0x5a */
    public static final BaseTokenID HEX_LITERAL
    = new BaseTokenID("hex-literal", HEX_LITERAL_ID, NUMERIC_LITERALS); // NOI18N

    /** Java octal literal e.g. 0123 */
    public static final BaseTokenID OCTAL_LITERAL
    = new BaseTokenID("octal-literal", OCTAL_LITERAL_ID, NUMERIC_LITERALS); // NOI18N

    /** Java float literal e.g. 1.5e+20f */
    public static final BaseTokenID FLOAT_LITERAL
    = new BaseTokenID("float-literal", FLOAT_LITERAL_ID, NUMERIC_LITERALS); // NOI18N

    /** Java double literal e.g. 1.5e+20 */
    public static final BaseTokenID DOUBLE_LITERAL
    = new BaseTokenID("double-literal", DOUBLE_LITERAL_ID, NUMERIC_LITERALS); // NOI18N

    // Operators
    public static final BaseImageTokenID EQ
    = new BaseImageTokenID("eq", EQ_ID, OPERATORS, "="); // NOI18N

    public static final BaseImageTokenID LT
    = new BaseImageTokenID("lt", LT_ID, OPERATORS, "<"); // NOI18N

    public static final BaseImageTokenID GT
    = new BaseImageTokenID("gt", GT_ID, OPERATORS, ">"); // NOI18N

    public static final BaseImageTokenID LSHIFT
    = new BaseImageTokenID("lshift", LSHIFT_ID, OPERATORS, "<<"); // NOI18N

    public static final BaseImageTokenID RSSHIFT
    = new BaseImageTokenID("rsshift", RSSHIFT_ID, OPERATORS, ">>"); // NOI18N

    public static final BaseImageTokenID RUSHIFT
    = new BaseImageTokenID("rushift", RUSHIFT_ID, OPERATORS, ">>>"); // NOI18N

    public static final BaseImageTokenID PLUS
    = new BaseImageTokenID("plus", PLUS_ID, OPERATORS, "+"); // NOI18N

    public static final BaseImageTokenID MINUS
    = new BaseImageTokenID("minus", MINUS_ID, OPERATORS, "-"); // NOI18N

    public static final BaseImageTokenID MUL
    = new BaseImageTokenID("mul", MUL_ID, OPERATORS, "*"); // NOI18N

    public static final BaseImageTokenID DIV
    = new BaseImageTokenID("div", DIV_ID, OPERATORS, "/"); // NOI18N

    public static final BaseImageTokenID AND
    = new BaseImageTokenID("and", AND_ID, OPERATORS, "&"); // NOI18N

    public static final BaseImageTokenID OR
    = new BaseImageTokenID("or", OR_ID, OPERATORS, "|"); // NOI18N

    public static final BaseImageTokenID XOR
    = new BaseImageTokenID("xor", XOR_ID, OPERATORS, "^"); // NOI18N

    public static final BaseImageTokenID MOD
    = new BaseImageTokenID("mod", MOD_ID, OPERATORS, "%"); // NOI18N

    public static final BaseImageTokenID NOT
    = new BaseImageTokenID("not", NOT_ID, OPERATORS, "!"); // NOI18N

    public static final BaseImageTokenID NEG
    = new BaseImageTokenID("neg", NEG_ID, OPERATORS, "~"); // NOI18N


    public static final BaseImageTokenID EQ_EQ
    = new BaseImageTokenID("eq-eq", EQ_EQ_ID, OPERATORS, "=="); // NOI18N

    public static final BaseImageTokenID LT_EQ
    = new BaseImageTokenID("le", LT_EQ_ID, OPERATORS, "<="); // NOI18N

    public static final BaseImageTokenID GT_EQ
    = new BaseImageTokenID("ge", GT_EQ_ID, OPERATORS, ">="); // NOI18N

    public static final BaseImageTokenID LSHIFT_EQ
    = new BaseImageTokenID("lshift-eq", LSHIFT_EQ_ID, OPERATORS, "<<="); // NOI18N

    public static final BaseImageTokenID RSSHIFT_EQ
    = new BaseImageTokenID("rsshift-eq", RSSHIFT_EQ_ID, OPERATORS, ">>="); // NOI18N

    public static final BaseImageTokenID RUSHIFT_EQ
    = new BaseImageTokenID("rushift-eq", RUSHIFT_EQ_ID, OPERATORS, ">>>="); // NOI18N

    public static final BaseImageTokenID PLUS_EQ
    = new BaseImageTokenID("plus-eq", PLUS_EQ_ID, OPERATORS, "+="); // NOI18N

    public static final BaseImageTokenID MINUS_EQ
    = new BaseImageTokenID("minus-eq", MINUS_EQ_ID, OPERATORS, "-="); // NOI18N

    public static final BaseImageTokenID MUL_EQ
    = new BaseImageTokenID("mul-eq", MUL_EQ_ID, OPERATORS, "*="); // NOI18N

    public static final BaseImageTokenID DIV_EQ
    = new BaseImageTokenID("div-eq", DIV_EQ_ID, OPERATORS, "/="); // NOI18N

    public static final BaseImageTokenID AND_EQ
    = new BaseImageTokenID("and-eq", AND_EQ_ID, OPERATORS, "&="); // NOI18N

    public static final BaseImageTokenID OR_EQ
    = new BaseImageTokenID("or-eq", OR_EQ_ID, OPERATORS, "|="); // NOI18N

    public static final BaseImageTokenID XOR_EQ
    = new BaseImageTokenID("xor-eq", XOR_EQ_ID, OPERATORS, "^="); // NOI18N

    public static final BaseImageTokenID MOD_EQ
    = new BaseImageTokenID("mod-eq", MOD_EQ_ID, OPERATORS, "%="); // NOI18N

    public static final BaseImageTokenID NOT_EQ
    = new BaseImageTokenID("not-eq", NOT_EQ_ID, OPERATORS, "!="); // NOI18N


    public static final BaseImageTokenID DOT
    = new BaseImageTokenID("dot", DOT_ID, OPERATORS, "."); // NOI18N

    public static final BaseImageTokenID COMMA
    = new BaseImageTokenID("comma", COMMA_ID, OPERATORS, ","); // NOI18N

    public static final BaseImageTokenID COLON
    = new BaseImageTokenID("colon", COLON_ID, OPERATORS, ":"); // NOI18N

    public static final BaseImageTokenID SEMICOLON
    = new BaseImageTokenID("semicolon", SEMICOLON_ID, OPERATORS, ";"); // NOI18N

    public static final BaseImageTokenID QUESTION
    = new BaseImageTokenID("question", QUESTION_ID, OPERATORS, "?"); // NOI18N

    public static final BaseImageTokenID LPAREN
    = new BaseImageTokenID("lparen", LPAREN_ID, OPERATORS, "("); // NOI18N

    public static final BaseImageTokenID RPAREN
    = new BaseImageTokenID("rparen", RPAREN_ID, OPERATORS, ")"); // NOI18N

    public static final BaseImageTokenID LBRACKET
    = new BaseImageTokenID("lbracket", LBRACKET_ID, OPERATORS, "["); // NOI18N

    public static final BaseImageTokenID RBRACKET
    = new BaseImageTokenID("rbracket", RBRACKET_ID, OPERATORS, "]"); // NOI18N

    public static final BaseImageTokenID LBRACE
    = new BaseImageTokenID("lbrace", LBRACE_ID, OPERATORS, "{"); // NOI18N

    public static final BaseImageTokenID RBRACE
    = new BaseImageTokenID("rbrace", RBRACE_ID, OPERATORS, "}"); // NOI18N

    public static final BaseImageTokenID PLUS_PLUS
    = new BaseImageTokenID("plus-plus", PLUS_PLUS_ID, OPERATORS, "++"); // NOI18N

    public static final BaseImageTokenID MINUS_MINUS
    = new BaseImageTokenID("minus-minus", MINUS_MINUS_ID, OPERATORS, "--"); // NOI18N

    public static final BaseImageTokenID AND_AND
    = new BaseImageTokenID("and-and", AND_AND_ID, OPERATORS, "&&"); // NOI18N

    public static final BaseImageTokenID OR_OR
    = new BaseImageTokenID("or-or", OR_OR_ID, OPERATORS, "||"); // NOI18N


    // Data types
    public static final BaseImageTokenID BOOLEAN
    = new BaseImageTokenID("boolean", BOOLEAN_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID BYTE
    = new BaseImageTokenID("byte", BYTE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID CHAR
    = new BaseImageTokenID("char", CHAR_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID DOUBLE
    = new BaseImageTokenID("double", DOUBLE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID FLOAT
    = new BaseImageTokenID("float", FLOAT_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID INT
    = new BaseImageTokenID("int", INT_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID LONG
    = new BaseImageTokenID("long", LONG_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID SHORT
    = new BaseImageTokenID("short", SHORT_ID, KEYWORDS); // NOI18N

    // Void type
    public static final BaseImageTokenID VOID
    = new BaseImageTokenID("void", VOID_ID, KEYWORDS); // NOI18N

    // Rest of the keywords
    public static final BaseImageTokenID ABSTRACT
    = new BaseImageTokenID("abstract", ABSTRACT_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID ASSERT
    = new BaseImageTokenID("assert", ASSERT_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID BREAK
    = new BaseImageTokenID("break", BREAK_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID CASE
    = new BaseImageTokenID("case", CASE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID CATCH
    = new BaseImageTokenID("catch", CATCH_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID CLASS
    = new BaseImageTokenID("class", CLASS_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID CONST
    = new BaseImageTokenID("const", CONST_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID CONTINUE
    = new BaseImageTokenID("continue", CONTINUE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID DEFAULT
    = new BaseImageTokenID("default", DEFAULT_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID DO
    = new BaseImageTokenID("do", DO_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID ELSE
    = new BaseImageTokenID("else", ELSE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID ENUM
    = new BaseImageTokenID("enum", ENUM_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID EXTENDS
    = new BaseImageTokenID("extends", EXTENDS_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID FALSE
    = new BaseImageTokenID("false", FALSE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID FINAL
    = new BaseImageTokenID("final", FINAL_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID FINALLY
    = new BaseImageTokenID("finally", FINALLY_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID FOR
    = new BaseImageTokenID("for", FOR_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID GOTO
    = new BaseImageTokenID("goto", GOTO_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID IF
    = new BaseImageTokenID("if", IF_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID IMPLEMENTS
    = new BaseImageTokenID("implements", IMPLEMENTS_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID IMPORT
    = new BaseImageTokenID("import", IMPORT_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID INSTANCEOF
    = new BaseImageTokenID("instanceof", INSTANCEOF_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID INTERFACE
    = new BaseImageTokenID("interface", INTERFACE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID NATIVE
    = new BaseImageTokenID("native", NATIVE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID NEW
    = new BaseImageTokenID("new", NEW_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID NULL
    = new BaseImageTokenID("null", NULL_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID PACKAGE
    = new BaseImageTokenID("package", PACKAGE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID PRIVATE
    = new BaseImageTokenID("private", PRIVATE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID PROTECTED
    = new BaseImageTokenID("protected", PROTECTED_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID PUBLIC
    = new BaseImageTokenID("public", PUBLIC_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID RETURN
    = new BaseImageTokenID("return", RETURN_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID STATIC
    = new BaseImageTokenID("static", STATIC_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID STRICTFP
    = new BaseImageTokenID("strictfp", STRICTFP_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID SUPER
    = new BaseImageTokenID("super", SUPER_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID SWITCH
    = new BaseImageTokenID("switch", SWITCH_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID SYNCHRONIZED
    = new BaseImageTokenID("synchronized", SYNCHRONIZED_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID THIS
    = new BaseImageTokenID("this", THIS_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID THROW
    = new BaseImageTokenID("throw", THROW_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID THROWS
    = new BaseImageTokenID("throws", THROWS_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID TRANSIENT
    = new BaseImageTokenID("transient", TRANSIENT_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID TRUE
    = new BaseImageTokenID("true", TRUE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID TRY
    = new BaseImageTokenID("try", TRY_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID VOLATILE
    = new BaseImageTokenID("volatile", VOLATILE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID WHILE
    = new BaseImageTokenID("while", WHILE_ID, KEYWORDS); // NOI18N

    // Incomplete and error token-ids
    public static final BaseTokenID INCOMPLETE_STRING_LITERAL
    = new BaseTokenID("incomplete-string-literal", INCOMPLETE_STRING_LITERAL_ID, ERRORS); // NOI18N

    public static final BaseTokenID INCOMPLETE_CHAR_LITERAL
    = new BaseTokenID("incomplete-char-literal", INCOMPLETE_CHAR_LITERAL_ID, ERRORS); // NOI18N

    public static final BaseTokenID INCOMPLETE_HEX_LITERAL
    = new BaseTokenID("incomplete-hex-literal", INCOMPLETE_HEX_LITERAL_ID, ERRORS); // NOI18N

    public static final BaseTokenID INVALID_CHAR
    = new BaseTokenID("invalid-char", INVALID_CHAR_ID, ERRORS); // NOI18N

    public static final BaseTokenID INVALID_OPERATOR
    = new BaseTokenID("invalid-operator", INVALID_OPERATOR_ID, ERRORS); // NOI18N

    public static final BaseTokenID INVALID_OCTAL_LITERAL
    = new BaseTokenID("invalid-octal-literal", INVALID_OCTAL_LITERAL_ID, ERRORS); // NOI18N

    public static final BaseTokenID INVALID_COMMENT_END
    = new BaseTokenID("invalid-comment-end", INVALID_COMMENT_END_ID, ERRORS); // NOI18N

    
    // JDK1.5 "@ident" annotation
    // "@keyword" gets returned as two tokens "@" and "keyword"
    public static final BaseTokenID ANNOTATION
    = new BaseTokenID("annotation", ANNOTATION_ID); // NOI18N

    // JDK1.5 vararg's "..."
    public static final BaseImageTokenID ELLIPSIS
    = new BaseImageTokenID("ellipsis", ELLIPSIS_ID, OPERATORS, "..."); // NOI18N

    // Context instance declaration
    public static final JavaTokenContext context = new JavaTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    private static final HashMap str2kwd = new HashMap();

    static {
        BaseImageTokenID[] kwds = new BaseImageTokenID[] {
            ABSTRACT, ASSERT, BREAK, CASE, CATCH, CLASS, CONST, CONTINUE, DEFAULT, DO, 
            ELSE, ENUM, EXTENDS, FALSE, FINAL, FINALLY, FOR, GOTO, IF, IMPLEMENTS, 
            IMPORT, INSTANCEOF, INTERFACE, NATIVE, NEW, NULL, PACKAGE, PRIVATE, 
            PROTECTED, PUBLIC, RETURN, STATIC, STRICTFP, SUPER, SWITCH, SYNCHRONIZED, THIS, 
            THROW, THROWS, TRANSIENT, TRUE, TRY, VOLATILE, WHILE,
            BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, VOID
        };

        for (int i = kwds.length - 1; i >= 0; i--) {
            str2kwd.put(kwds[i].getImage(), kwds[i]);
        }
    }

    /** Checks whether the given token-id is a type-keyword.
    * @return true when the keyword is a data type.
    */
    public static boolean isType(TokenID keywordTokenID) {
        int numID = (keywordTokenID != null) ? keywordTokenID.getNumericID() : -1;
        return (numID >= BOOLEAN_ID && numID < VOID_ID);
    }

    /** Checks whether the given string is a type-keyword. */
    public static boolean isType(String s) {
        return isType((TokenID)str2kwd.get(s));
    }

    /** Checks whether the given token-id is a data-type-keyword or void-keyword.
    * @return true when the keyword is a data-type-keyword or void-keyword.
    */
    public static boolean isTypeOrVoid(TokenID keywordTokenID) {
        int numID = (keywordTokenID != null) ? keywordTokenID.getNumericID() : -1;
        return (numID >= BOOLEAN_ID && numID <= VOID_ID);
    }

    /** Checks whether the given string is a data-type-keyword or void-keyword. */
    public static boolean isTypeOrVoid(String s) {
        return isTypeOrVoid((TokenID)str2kwd.get(s));
    }

    /** Get the keyword token-id from string */
    public static TokenID getKeyword(String s) {
        return (TokenID)str2kwd.get(s);
    }

    private JavaTokenContext() {
        super("java-"); // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Utilities.annotateLoggable(e);
        }

    }

}
