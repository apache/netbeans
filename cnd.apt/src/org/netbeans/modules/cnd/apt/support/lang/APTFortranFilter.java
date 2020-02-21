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

package org.netbeans.modules.cnd.apt.support.lang;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;

/**
 * Filter for Fortran language.
 *
 */
final class APTFortranFilter extends APTBaseLanguageFilter {

    private final String flavor;
    private Map<Integer,Integer> filter = new HashMap<Integer,Integer>();

    public APTFortranFilter(String flavor) {
        super(true);
        initialize();
        this.flavor = flavor;
    }

    @Override
    public TokenStream getFilteredStream(TokenStream origStream) {
        return new APTCommentsFilter(new APTFortranFilterEx(flavor).getFilteredStream(new APTFortranEOSFilter().getFilteredStream(super.getFilteredStream(origStream))));
    }

    private void initialize() {

        // keywords

        filter("INTEGER", APTTokenTypes.T_INTEGER); // NOI18N
        filter("REAL", APTTokenTypes.T_REAL); // NOI18N
        filter("COMPLEX", APTTokenTypes.T_COMPLEX); // NOI18N
        filter("CHARACTER", APTTokenTypes.T_CHARACTER); // NOI18N
        filter("LOGICAL", APTTokenTypes.T_LOGICAL); // NOI18N

        filter("ABSTRACT", APTTokenTypes.T_ABSTRACT); // NOI18N
        filter("ALLOCATABLE", APTTokenTypes.T_ALLOCATABLE); // NOI18N
        filter("ALLOCATE", APTTokenTypes.T_ALLOCATE); // NOI18N
        filter("ASSIGNMENT", APTTokenTypes.T_ASSIGNMENT); // NOI18N

// Assign keyword was excluded
// Bug 182942 - *Fortran* Navigator doesn't show modules in source files
//        filter("ASSIGN", APTTokenTypes.T_ASSIGN); // NOI18N

        filter("ASSOCIATE", APTTokenTypes.T_ASSOCIATE); // NOI18N
        filter("ASYNCHRONOUS", APTTokenTypes.T_ASYNCHRONOUS); // NOI18N
        filter("BACKSPACE", APTTokenTypes.T_BACKSPACE); // NOI18N
        filter("BLOCK", APTTokenTypes.T_BLOCK); // NOI18N
        filter("BLOCKDATA", APTTokenTypes.T_BLOCKDATA); // NOI18N
        filter("CALL", APTTokenTypes.T_CALL); // NOI18N
        filter("CASE", APTTokenTypes.T_CASE); // NOI18N
        filter("CLASS", APTTokenTypes.T_CLASS); // NOI18N
        filter("CLOSE", APTTokenTypes.T_CLOSE); // NOI18N

// Helps parser to skip unknown constructions
// Bug 183073 - keyword common breaks *Fortran* Navigator
//        filter("COMMON", APTTokenTypes.T_COMMON); // NOI18N

        filter("CONTAINS", APTTokenTypes.T_CONTAINS); // NOI18N
        filter("CONTINUE", APTTokenTypes.T_CONTINUE); // NOI18N
        filter("CYCLE", APTTokenTypes.T_CYCLE); // NOI18N
        filter("DATA", APTTokenTypes.T_DATA); // NOI18N
        filter("DEFAULT", APTTokenTypes.T_DEFAULT); // NOI18N
        filter("DEALLOCATE", APTTokenTypes.T_DEALLOCATE); // NOI18N
        filter("DEFERRED", APTTokenTypes.T_DEFERRED); // NOI18N
        filter("DO", APTTokenTypes.T_DO); // NOI18N
        filter("DOUBLE", APTTokenTypes.T_DOUBLE); // NOI18N
        filter("DOUBLEPRECISION", APTTokenTypes.T_DOUBLEPRECISION); // NOI18N
        filter("DOUBLECOMPLEX", APTTokenTypes.T_DOUBLECOMPLEX); // NOI18N
        filter("ELEMENTAL", APTTokenTypes.T_ELEMENTAL); // NOI18N
        filter("ELSE", APTTokenTypes.T_ELSE); // NOI18N
        filter("ELSEIF", APTTokenTypes.T_ELSEIF); // NOI18N
        filter("ELSEWHERE", APTTokenTypes.T_ELSEWHERE); // NOI18N
        filter("ENTRY", APTTokenTypes.T_ENTRY); // NOI18N
        filter("ENUM", APTTokenTypes.T_ENUM); // NOI18N
        filter("ENUMERATOR", APTTokenTypes.T_ENUMERATOR); // NOI18N
        filter("EQUIVALENCE", APTTokenTypes.T_EQUIVALENCE); // NOI18N
        filter("EXIT", APTTokenTypes.T_EXIT); // NOI18N
        filter("EXTENDS", APTTokenTypes.T_EXTENDS); // NOI18N
        filter("EXTERNAL", APTTokenTypes.T_EXTERNAL); // NOI18N
        filter("FILE", APTTokenTypes.T_FILE); // NOI18N
        filter("FINAL", APTTokenTypes.T_FINAL); // NOI18N
        filter("FLUSH", APTTokenTypes.T_FLUSH); // NOI18N
        filter("FORALL", APTTokenTypes.T_FORALL); // NOI18N
        filter("FORMAT", APTTokenTypes.T_FORMAT); // NOI18N
        filter("FORMATTED", APTTokenTypes.T_FORMATTED); // NOI18N
        filter("FUNCTION", APTTokenTypes.T_FUNCTION); // NOI18N
        filter("GENERIC", APTTokenTypes.T_GENERIC); // NOI18N
        filter("GO", APTTokenTypes.T_GO); // NOI18N
        filter("GOTO", APTTokenTypes.T_GOTO); // NOI18N
        filter("IF", APTTokenTypes.T_IF); // NOI18N
        filter("IMPLICIT", APTTokenTypes.T_IMPLICIT); // NOI18N
        filter("IMPORT", APTTokenTypes.T_IMPORT); // NOI18N
        filter("IN", APTTokenTypes.T_IN); // NOI18N
        filter("INOUT", APTTokenTypes.T_INOUT); // NOI18N
        filter("INTENT", APTTokenTypes.T_INTENT); // NOI18N
        filter("INTERFACE", APTTokenTypes.T_INTERFACE); // NOI18N
        filter("INTRINSIC", APTTokenTypes.T_INTRINSIC); // NOI18N
        filter("INQUIRE", APTTokenTypes.T_INQUIRE); // NOI18N
        filter("MODULE", APTTokenTypes.T_MODULE); // NOI18N
        filter("NAMELIST", APTTokenTypes.T_NAMELIST); // NOI18N
        filter("NONE", APTTokenTypes.T_NONE); // NOI18N
        filter("NON_INTRINSIC", APTTokenTypes.T_NON_INTRINSIC); // NOI18N
        filter("NON_OVERRIDABLE", APTTokenTypes.T_NON_OVERRIDABLE); // NOI18N
        filter("NOPASS", APTTokenTypes.T_NOPASS); // NOI18N
        filter("NULLIFY", APTTokenTypes.T_NULLIFY); // NOI18N
        filter("ONLY", APTTokenTypes.T_ONLY); // NOI18N
        filter("OPEN", APTTokenTypes.T_OPEN); // NOI18N
        filter("OPERATOR", APTTokenTypes.T_OPERATOR); // NOI18N
        filter("OPTIONAL", APTTokenTypes.T_OPTIONAL); // NOI18N
        filter("OUT", APTTokenTypes.T_OUT); // NOI18N
        filter("PARAMETER", APTTokenTypes.T_PARAMETER); // NOI18N
        filter("PASS", APTTokenTypes.T_PASS); // NOI18N

// Helps parser to skip unknown constructions
// Bug 183152 - keyword pause breaks *Fortran* Navigator
//        filter("PAUSE", APTTokenTypes.T_PAUSE); // NOI18N

        filter("POINTER", APTTokenTypes.T_POINTER); // NOI18N
        filter("PRINT", APTTokenTypes.T_PRINT); // NOI18N
        filter("PRECISION", APTTokenTypes.T_PRECISION); // NOI18N
        filter("PRIVATE", APTTokenTypes.T_PRIVATE); // NOI18N
        filter("PROCEDURE", APTTokenTypes.T_PROCEDURE); // NOI18N
        filter("PROGRAM", APTTokenTypes.T_PROGRAM); // NOI18N
        filter("PROTECTED", APTTokenTypes.T_PROTECTED); // NOI18N
        filter("PUBLIC", APTTokenTypes.T_PUBLIC); // NOI18N
        filter("PURE", APTTokenTypes.T_PURE); // NOI18N
        filter("READ", APTTokenTypes.T_READ); // NOI18N
        filter("RECURSIVE", APTTokenTypes.T_RECURSIVE); // NOI18N
        filter("RESULT", APTTokenTypes.T_RESULT); // NOI18N
        filter("RETURN", APTTokenTypes.T_RETURN); // NOI18N
        filter("REWIND", APTTokenTypes.T_REWIND); // NOI18N
        filter("SAVE", APTTokenTypes.T_SAVE); // NOI18N
        filter("SELECT", APTTokenTypes.T_SELECT); // NOI18N
        filter("SELECTCASE", APTTokenTypes.T_SELECTCASE); // NOI18N
        filter("SELECTTYPE", APTTokenTypes.T_SELECTTYPE); // NOI18N
        filter("SEQUENCE", APTTokenTypes.T_SEQUENCE); // NOI18N
        filter("STOP", APTTokenTypes.T_STOP); // NOI18N
        filter("SUBROUTINE", APTTokenTypes.T_SUBROUTINE); // NOI18N
        filter("TARGET", APTTokenTypes.T_TARGET); // NOI18N
        filter("THEN", APTTokenTypes.T_THEN); // NOI18N
        filter("TO", APTTokenTypes.T_TO); // NOI18N
        filter("TYPE", APTTokenTypes.T_TYPE); // NOI18N
        filter("UNFORMATTED", APTTokenTypes.T_UNFORMATTED); // NOI18N
        filter("USE", APTTokenTypes.T_USE); // NOI18N
        filter("VALUE", APTTokenTypes.T_VALUE); // NOI18N
        filter("VOLATILE", APTTokenTypes.T_VOLATILE); // NOI18N
        filter("WAIT", APTTokenTypes.T_WAIT); // NOI18N
        filter("WHERE", APTTokenTypes.T_WHERE); // NOI18N
        filter("WHILE", APTTokenTypes.T_WHILE); // NOI18N
        filter("WRITE", APTTokenTypes.T_WRITE); // NOI18N

        filter("ENDASSOCIATE", APTTokenTypes.T_ENDASSOCIATE); // NOI18N
        filter("ENDBLOCK", APTTokenTypes.T_ENDBLOCK); // NOI18N
        filter("ENDBLOCKDATA", APTTokenTypes.T_ENDBLOCKDATA); // NOI18N
        filter("ENDDO", APTTokenTypes.T_ENDDO); // NOI18N
        filter("ENDENUM", APTTokenTypes.T_ENDENUM); // NOI18N
        filter("ENDFORALL", APTTokenTypes.T_ENDFORALL); // NOI18N
        filter("ENDFILE", APTTokenTypes.T_ENDFILE); // NOI18N
        filter("ENDFUNCTION", APTTokenTypes.T_ENDFUNCTION); // NOI18N
        filter("ENDIF", APTTokenTypes.T_ENDIF); // NOI18N
        filter("ENDINTERFACE", APTTokenTypes.T_ENDINTERFACE); // NOI18N
        filter("ENDMODULE", APTTokenTypes.T_ENDMODULE); // NOI18N
        filter("ENDPROGRAM", APTTokenTypes.T_ENDPROGRAM); // NOI18N
        filter("ENDSELECT", APTTokenTypes.T_ENDSELECT); // NOI18N
        filter("ENDSUBROUTINE", APTTokenTypes.T_ENDSUBROUTINE); // NOI18N
        filter("ENDTYPE", APTTokenTypes.T_ENDTYPE); // NOI18N
        filter("ENDWHERE", APTTokenTypes.T_ENDWHERE); // NOI18N

        filter("END", APTTokenTypes.T_END); // NOI18N

        filter("DIMENSION", APTTokenTypes.T_DIMENSION); // NOI18N

        filter("KIND", APTTokenTypes.T_KIND); // NOI18N

        filter("LEN", APTTokenTypes.T_LEN); // NOI18N

        filter("BIND", APTTokenTypes.T_BIND); // NOI18N

        filter(APTTokenTypes.STAR, APTTokenTypes.T_ASTERISK);
        filter(APTTokenTypes.COMMA, APTTokenTypes.T_COMMA);
        filter(APTTokenTypes.CHAR_LITERAL, APTTokenTypes.T_CHAR_CONSTANT);
        filter(APTTokenTypes.EQUAL, APTTokenTypes.T_EQV);
        filter(APTTokenTypes.NOTEQUAL, APTTokenTypes.T_NEQV);
        filter(APTTokenTypes.ASSIGNEQUAL, APTTokenTypes.T_EQUALS);
        filter(APTTokenTypes.SCOPE, APTTokenTypes.T_COLON_COLON);
        filter(APTTokenTypes.LESSTHAN, APTTokenTypes.T_LESSTHAN);
        filter(APTTokenTypes.GREATERTHAN, APTTokenTypes.T_GREATERTHAN);
        filter(APTTokenTypes.LESSTHANOREQUALTO, APTTokenTypes.T_LESSTHAN_EQ);
        filter(APTTokenTypes.GREATERTHANOREQUALTO, APTTokenTypes.T_GREATERTHAN_EQ);        
        filter(APTTokenTypes.PLUS, APTTokenTypes.T_PLUS);
        filter(APTTokenTypes.MINUS, APTTokenTypes.T_MINUS);
        filter(APTTokenTypes.DIVIDE, APTTokenTypes.T_SLASH);
        filter(APTTokenTypes.COLON, APTTokenTypes.T_COLON);

        filter(APTTokenTypes.OCTALINT, APTTokenTypes.T_DIGIT_STRING);
        filter(APTTokenTypes.DECIMALINT, APTTokenTypes.T_DIGIT_STRING);
        filter(APTTokenTypes.NUMBER, APTTokenTypes.T_DIGIT_STRING);
        filter(APTTokenTypes.FLOATONE, APTTokenTypes.T_REAL_CONSTANT);
        filter(APTTokenTypes.FLOATTWO, APTTokenTypes.T_REAL_CONSTANT);
        
        filter(APTTokenTypes.LPAREN, APTTokenTypes.T_LPAREN);
        filter(APTTokenTypes.RPAREN, APTTokenTypes.T_RPAREN);
    }

    protected void filter(int ttype1, int ttype2) {
        filter.put(ttype1, ttype2);
    }

    @Override
    protected Token onID(Token token) {
        Token ret = defaultWrap(token);
        if(ret.getType() == APTTokenTypes.IDENT) {
            ret = new FilterToken((APTToken)ret, APTTokenTypes.T_IDENT);
        }
        return ret;
    }

    @Override
    protected Token onToken(Token token) {
        token = super.onToken(token);
        if(filter.containsKey(token.getType())) {
            return new FilterToken((APTToken)token, filter.get(token.getType()));
        }

        // TODO:
//      if(this.sourceForm == FrontEnd.FIXED_FORM) {
//         if((token.getText().equals("C") || token.getText().equals("*")) &&
//            token.getColumn() == 0) {
//            return new FilterToken((APTToken)token, APTTokenTypes.LINE_COMMENT);
//         } else if(token.getColumn() == 5 &&
//                   Character.isWhitespace((char)letter_value) == false) {
//            // if a character appears in the 6th column in fixed format it
//            // is a continuation character.
//            letter_value = (int)'&';
//         }
//      }

        return token;
    }


}
