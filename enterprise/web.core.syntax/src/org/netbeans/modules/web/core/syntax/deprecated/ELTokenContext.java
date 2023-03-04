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

package org.netbeans.modules.web.core.syntax.deprecated;

import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
* Tokens used in formatting
*
* @author Petr Jiricka, Petr Pisl
* @version 1.00
* @deprecated Use {@link ELLexer} instead.
*/
@Deprecated
public class ELTokenContext extends TokenContext {

    // Token category-ids
    public static final int KEYWORDS_ID		  = 1;
    public static final int OPERATORS_ID          = KEYWORDS_ID + 1;
    public static final int NUMERIC_LITERALS_ID   = OPERATORS_ID + 1;
    public static final int ERRORS_ID             = NUMERIC_LITERALS_ID + 1;
    
    // Numeric-ids for token-ids
    public static final int WHITESPACE_ID	    = ERRORS_ID + 1;
    public static final int EOL_ID		    = WHITESPACE_ID + 1;
    public static final int EL_DELIM_ID		    = EOL_ID + 1;
    public static final int STRING_LITERAL_ID	    = EL_DELIM_ID + 1;
    public static final int TAG_LIB_PREFIX_ID	    = STRING_LITERAL_ID + 1;
    public static final int IDENTIFIER_ID	    = TAG_LIB_PREFIX_ID + 1;
    public static final int CHAR_LITERAL_ID	    = IDENTIFIER_ID + 1;
    public static final int INT_LITERAL_ID	    = CHAR_LITERAL_ID + 1;
    public static final int LONG_LITERAL_ID	    = INT_LITERAL_ID + 1;
    public static final int HEX_LITERAL_ID	    = LONG_LITERAL_ID + 1;
    public static final int OCTAL_LITERAL_ID	    = HEX_LITERAL_ID + 1;
    public static final int FLOAT_LITERAL_ID	    = OCTAL_LITERAL_ID + 1;
    public static final int DOUBLE_LITERAL_ID	    = FLOAT_LITERAL_ID + 1;
    
    
    // Operator numeric-ids
    public static final int LT_ID = DOUBLE_LITERAL_ID + 1;// <
    public static final int GT_ID = LT_ID + 1;            // >
    public static final int PLUS_ID = GT_ID + 1;     // +
    public static final int MINUS_ID = PLUS_ID + 1;       // -
    public static final int MUL_ID = MINUS_ID + 1;        // *
    public static final int DIV_ID = MUL_ID + 1;          // /
    public static final int MOD_ID = DIV_ID + 1;          // %
    public static final int DOT_ID = MOD_ID + 1;       // .
    public static final int COMMA_ID = DOT_ID + 1;       // ,
    public static final int QUESTION_ID = COMMA_ID + 1;  // ?
    public static final int NOT_ID = QUESTION_ID + 1;          // !
    public static final int COLON_ID = NOT_ID + 1;      // :
    public static final int SEMICOLON_ID = COLON_ID + 1;  // ;
    public static final int LPAREN_ID = SEMICOLON_ID + 1;  // (
    public static final int RPAREN_ID = LPAREN_ID + 1;    // )
    public static final int LBRACKET_ID = RPAREN_ID + 1;  // [
    public static final int RBRACKET_ID = LBRACKET_ID + 1; // ]
    
    public static final int AND_AND_ID = RBRACKET_ID + 1;    // &&
    public static final int OR_OR_ID = AND_AND_ID + 1;    // ||
    public static final int LT_EQ_ID = OR_OR_ID + 1;      // <=
    public static final int GT_EQ_ID = LT_EQ_ID + 1;         // >=
    public static final int EQ_EQ_ID = GT_EQ_ID + 1;        // ==
    public static final int NOT_EQ_ID = EQ_EQ_ID + 1;    // !=
    
    // Other keywords numeric-ids
    public static final int AND_KEYWORD_ID	    = NOT_EQ_ID + 1;
    public static final int DIV_KEYWORD_ID	    = AND_KEYWORD_ID + 1;
    public static final int EMPTY_KEYWORD_ID	    = DIV_KEYWORD_ID + 1;
    public static final int EQ_KEYWORD_ID	    = EMPTY_KEYWORD_ID + 1;
    public static final int FALSE_KEYWORD_ID	    = EQ_KEYWORD_ID + 1;
    public static final int GE_KEYWORD_ID	    = FALSE_KEYWORD_ID + 1;
    public static final int GT_KEYWORD_ID	    = GE_KEYWORD_ID + 1;
    public static final int INSTANCEOF_KEYWORD_ID   = GT_KEYWORD_ID + 1;
    public static final int LE_KEYWORD_ID	    = INSTANCEOF_KEYWORD_ID + 1;
    public static final int LT_KEYWORD_ID	    = LE_KEYWORD_ID + 1;
    public static final int MOD_KEYWORD_ID	    = LT_KEYWORD_ID + 1;
    public static final int NE_KEYWORD_ID	    = MOD_KEYWORD_ID + 1;
    public static final int NOT_KEYWORD_ID	    = NE_KEYWORD_ID + 1;
    public static final int NULL_KEYWORD_ID	    = NOT_KEYWORD_ID + 1;
    public static final int OR_KEYWORD_ID	    = NULL_KEYWORD_ID + 1;
    public static final int TRUE_KEYWORD_ID	    = OR_KEYWORD_ID + 1;
    
    // Incomplete tokens
    public static final int INVALID_OCTAL_LITERAL_ID = OR_KEYWORD_ID + 1;
    public static final int INVALID_CHAR_ID	    = INVALID_OCTAL_LITERAL_ID + 1;
    
    
    // Token-categories
    /** All the keywords belong to this category. */
    public static final BaseTokenCategory KEYWORDS = new BaseTokenCategory("keywords", KEYWORDS_ID); //NOI18M
    
    /** All the operators belong to this category. */
    public static final BaseTokenCategory OPERATORS = new BaseTokenCategory("operators", OPERATORS_ID); //NOI18M
    
    /** All the numeric literals belong to this category. */
    public static final BaseTokenCategory NUMERIC_LITERALS = new BaseTokenCategory("numeric-literals", NUMERIC_LITERALS_ID); //NOI18M
    
    /** All the errorneous constructions and incomplete tokens
     * belong to this category.
     */
    public static final BaseTokenCategory ERRORS = new BaseTokenCategory("errors", ERRORS_ID); //NOI18M

    public static final BaseTokenID WHITESPACE = new BaseTokenID("whitespace", WHITESPACE_ID); //NOI18M

    public static final BaseImageTokenID EOL = new BaseImageTokenID("EOL", EOL_ID, "\n"); // NOI18N

public static final BaseTokenID EL_DELIM = new BaseTokenID("el-delimiter", EL_DELIM_ID);   // NOI18N
    
    public static final BaseTokenID STRING_LITERAL = new BaseTokenID("string", STRING_LITERAL_ID); //NOI18M
    
    public static final BaseTokenID TAG_LIB_PREFIX = new BaseTokenID("tag-lib-prefix", TAG_LIB_PREFIX_ID);//NOI18M
    
    public static final BaseTokenID IDENTIFIER = new BaseTokenID("identifier", IDENTIFIER_ID);//NOI18M
    
    public static final BaseTokenID CHAR_LITERAL = new BaseTokenID("char-literal", CHAR_LITERAL_ID); //NOI18M
    
    
    // Operators
    public static final BaseImageTokenID LT = new BaseImageTokenID("lt", LT_ID, OPERATORS, "<");//NOI18M
    public static final BaseImageTokenID GT = new BaseImageTokenID("gt", GT_ID, OPERATORS, ">");//NOI18M
    public static final BaseImageTokenID DOT = new BaseImageTokenID("dot", DOT_ID, OPERATORS, ".");//NOI18M
    public static final BaseImageTokenID COMMA = new BaseImageTokenID("comma", COMMA_ID, OPERATORS, ",");//NOI18M
    public static final BaseImageTokenID QUESTION = new BaseImageTokenID("question", QUESTION_ID, OPERATORS, "?");//NOI18M
    public static final BaseImageTokenID PLUS = new BaseImageTokenID("plus", PLUS_ID, OPERATORS, "+");//NOI18M
    public static final BaseImageTokenID MINUS = new BaseImageTokenID("minus", MINUS_ID, OPERATORS, "-");//NOI18M
    public static final BaseImageTokenID MUL = new BaseImageTokenID("mul", MUL_ID, OPERATORS, "*");//NOI18M
    public static final BaseImageTokenID DIV = new BaseImageTokenID("div", DIV_ID, OPERATORS, "/");//NOI18M
    public static final BaseImageTokenID MOD = new BaseImageTokenID("mod", MOD_ID, OPERATORS, "%");//NOI18M
    public static final BaseImageTokenID EQ_EQ = new BaseImageTokenID("eq-eq", EQ_EQ_ID, OPERATORS, "==");//NOI18M
    public static final BaseImageTokenID LT_EQ = new BaseImageTokenID("le", LT_EQ_ID, OPERATORS, "<=");//NOI18M
    public static final BaseImageTokenID GT_EQ = new BaseImageTokenID("ge", GT_EQ_ID, OPERATORS, ">=");//NOI18M
    public static final BaseImageTokenID NOT_EQ = new BaseImageTokenID("not-eq", NOT_EQ_ID, OPERATORS, "!=");//NOI18M
    public static final BaseImageTokenID AND_AND = new BaseImageTokenID("and-and", AND_AND_ID, OPERATORS, "&&");//NOI18M
    public static final BaseImageTokenID OR_OR = new BaseImageTokenID("or-or", OR_OR_ID, OPERATORS, "||");//NOI18M
    public static final BaseImageTokenID COLON = new BaseImageTokenID("colon", COLON_ID, OPERATORS, ":");//NOI18M
    public static final BaseImageTokenID NOT = new BaseImageTokenID("not", NOT_ID, OPERATORS, "!");//NOI18M
    public static final BaseImageTokenID LPAREN = new BaseImageTokenID("lparen", LPAREN_ID, OPERATORS, "(");//NOI18M
    public static final BaseImageTokenID RPAREN = new BaseImageTokenID("rparen", RPAREN_ID, OPERATORS, ")");//NOI18M
    public static final BaseImageTokenID LBRACKET = new BaseImageTokenID("lbracket", LBRACKET_ID, OPERATORS, "[");//NOI18M
    public static final BaseImageTokenID RBRACKET = new BaseImageTokenID("rbracket", RBRACKET_ID, OPERATORS, "]");//NOI18M

    // The keywords
    public static final BaseImageTokenID AND_KEYWORD = new BaseImageTokenID("and", AND_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID DIV_KEYWORD = new BaseImageTokenID("div", DIV_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID EMPTY_KEYWORD = new BaseImageTokenID("empty", EMPTY_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID EQ_KEYWORD = new BaseImageTokenID("eq", EQ_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID FALSE_KEYWORD = new BaseImageTokenID("false", FALSE_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID GE_KEYWORD = new BaseImageTokenID("ge", GE_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID GT_KEYWORD = new BaseImageTokenID("gt", GT_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID INSTANCEOF_KEYWORD = new BaseImageTokenID("instanceof", INSTANCEOF_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID LE_KEYWORD = new BaseImageTokenID("le", LE_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID LT_KEYWORD = new BaseImageTokenID("lt", LT_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID MOD_KEYWORD = new BaseImageTokenID("mod", MOD_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID NE_KEYWORD = new BaseImageTokenID("ne", NE_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID NOT_KEYWORD = new BaseImageTokenID("not", NOT_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID NULL_KEYWORD = new BaseImageTokenID("null", NULL_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID OR_KEYWORD = new BaseImageTokenID("or", OR_KEYWORD_ID, KEYWORDS);//NOI18M
    public static final BaseImageTokenID TRUE_KEYWORD = new BaseImageTokenID("true", TRUE_KEYWORD_ID, KEYWORDS);//NOI18M
    
    /** Java integer literal e.g. 1234 */
    public static final BaseTokenID INT_LITERAL
	= new BaseTokenID("int-literal", INT_LITERAL_ID, NUMERIC_LITERALS);//NOI18M

    /** Java long literal e.g. 12L */
    public static final BaseTokenID LONG_LITERAL
	= new BaseTokenID("long-literal", LONG_LITERAL_ID, NUMERIC_LITERALS);//NOI18M

    /** Java hexadecimal literal e.g. 0x5a */
    public static final BaseTokenID HEX_LITERAL
	= new BaseTokenID("hex-literal", HEX_LITERAL_ID, NUMERIC_LITERALS);//NOI18M

    /** Java octal literal e.g. 0123 */
    public static final BaseTokenID OCTAL_LITERAL
	= new BaseTokenID("octal-literal", OCTAL_LITERAL_ID, NUMERIC_LITERALS);//NOI18M

    /** Java float literal e.g. 1.5e+20f */
    public static final BaseTokenID FLOAT_LITERAL
	= new BaseTokenID("float-literal", FLOAT_LITERAL_ID, NUMERIC_LITERALS);//NOI18M

    /** Java double literal e.g. 1.5e+20 */
    public static final BaseTokenID DOUBLE_LITERAL
	= new BaseTokenID("double-literal", DOUBLE_LITERAL_ID, NUMERIC_LITERALS);//NOI18M

    // Incomplete and error token-ids
    public static final BaseTokenID INVALID_OCTAL_LITERAL
	= new BaseTokenID("invalid-octal-literal", INVALID_OCTAL_LITERAL_ID, ERRORS);//NOI18M
    public static final BaseTokenID INVALID_CHAR
	= new BaseTokenID("invalid-char", INVALID_CHAR_ID, ERRORS);//NOI18M
     
    // Context declaration
    public static final ELTokenContext context = new ELTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    private ELTokenContext() {
        super("el-");//NOI18M

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }

    }

}
