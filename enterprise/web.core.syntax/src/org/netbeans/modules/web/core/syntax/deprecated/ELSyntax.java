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

import org.netbeans.modules.web.core.syntax.*;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;

/**
* Lexical analyzer for the plain text.
*
* @deprecated Use {@link ELLexer} instead.
* 
* @author Petr Pisl
*/
@Deprecated
public class ELSyntax extends Syntax {

    /* Internal states used internally by analyzer. There
    * can be any number of them declared by the analyzer.
    * They are usually numbered starting from zero but they don't
    * have to. The only reserved value is -1 which is reserved
    * for the INIT state - the initial internal state of the analyzer.
    */
    
    private static final int ISI_IDENTIFIER = 2;
    private static final int ISI_CHAR = 3; // inside char constant
    private static final int ISI_CHAR_A_BSLASH = 4; // inside char constant after backslash
    private static final int ISI_STRING = 5; // inside a string " ... "
    private static final int ISI_STRING_A_BSLASH = 6; // inside string "..." constant after backslash
    private static final int ISI_CHAR_STRING = 7;  // inside a string '...'
    private static final int ISI_CHAR_STRING_A_BSLASH = 8; // inside string '...'contant after backslash
    private static final int ISA_ZERO = 9; // after '0'
    private static final int ISI_INT = 10; // integer number
    private static final int ISI_OCTAL = 11; // octal number
    private static final int ISI_DOUBLE = 12; // double number
    private static final int ISI_DOUBLE_EXP = 13; // double number
    private static final int ISI_HEX = 14; // hex number
    private static final int ISA_DOT = 15; // after '.'
    private static final int ISI_WHITESPACE = 16; // inside white space
    private static final int ISA_EQ = 17; // after '='
    private static final int ISA_GT = 18; // after '>'
    private static final int ISA_LT = 19; // after '<'
    //private static final int ISA_PLUS = 20; // after '+'
    //private static final int ISA_MINUS = 21; // after '-'
    //private static final int ISA_STAR = 22; // after '*'
    private static final int ISA_PIPE = 23; // after '|'
    private static final int ISA_AND = 24; // after '&'
    private static final int ISA_EXCLAMATION = 25; // after '!'
    private static final int ISI_BRACKET = 26; // after '['
    private static final int ISI_BRACKET_A_WHITESPACE = 27;
    private static final int ISI_BRACKET_A_IDENTIFIER = 28;
    private static final int ISI_BRACKET_ISA_EQ = 29;
    private static final int ISI_BRACKET_ISA_GT = 30;
    private static final int ISI_BRACKET_ISA_LT =31;
    private static final int ISI_BRACKET_ISA_PIPE = 32; // after '|'
    private static final int ISI_BRACKET_ISA_AND = 33; // after '&'
    private static final int ISI_BRACKET_ISA_ZERO = 34; // after '0'
    private static final int ISI_BRACKET_ISA_DOT = 35; // after '.'
    private static final int ISI_BRACKET_ISI_INT = 36; // after '.'
    private static final int ISI_BRACKET_ISI_OCTAL = 37; // octal number
    private static final int ISI_BRACKET_ISI_DOUBLE = 38; // double number
    private static final int ISI_BRACKET_ISI_DOUBLE_EXP = 39; // double number
    private static final int ISI_BRACKET_ISI_HEX = 40; // hex number
    private static final int ISI_DOULE_EXP_ISA_SIGN = 41;
    private static final int ISI_BRACKET_ISI_DOULE_EXP_ISA_SIGN = 42;
    //private static final int ISA_PERCENT = 24; // after '%'
    

    public ELSyntax() {
        tokenContextPath = ELTokenContext.contextPath;
    }
    
    /** This is core function of analyzer and it returns one of following numbers:
    * a) token number of next token from scanned text
    * b) EOL when end of line was found in scanned buffer
    * c) EOT when there is no more chars available in scanned buffer.
    *
    * The function scans the active character and does one or more
    * of the following actions:
    * 1. change internal analyzer state (state = new-state)
    * 2. return token ID (return token-ID)
    * 3. adjust current position to signal different end of token;
    *    the character that offset points to is not included in the token
    */
    protected TokenID parseToken() {
        // The main loop that reads characters one by one follows
        while (offset < stopOffset) {
            char ch = buffer[offset]; // get the current character
	    // - System.out.print(ch);
            switch (state) { // switch by the current internal state
            case INIT:
		// - System.out.print(" INIT (");
                switch (ch) {		    
		    case '"':
			// - System.out.print(" state->ISI_STRING ");
			state = ISI_STRING;
			break;
		    case '\'':
			// - System.out.print(" state->ISI_CHAR ");
			state = ISI_CHAR;
			break;
		    case '/':
			offset++;
			return ELTokenContext.DIV;
		    case '=':
			state = ISA_EQ;
			break;
		    case '>':
			state = ISA_GT;
			break;
		    case '<':
			state = ISA_LT;
			break;
		   case '+':
			offset++;
			return ELTokenContext.PLUS;
		    case '-':
			offset++;
			return ELTokenContext.MINUS;
		    case '*':
			offset++;
			return ELTokenContext.MUL;
		    case '|':
			state = ISA_PIPE;
			break;
		    case '&':
			state = ISA_AND;
			break;
		    case '[':
			//state = ISI_BRACKET;
			offset++;
			return ELTokenContext.LBRACKET;
			//break;
		    case ']':
			offset++;
			return ELTokenContext.RBRACKET;
		    case '%':
			offset++;
			return ELTokenContext.MOD;
		    case ':':
			//state = ISA_COLON;
			//break;
			offset++;
			return ELTokenContext.COLON;
		    case '!':
			state = ISA_EXCLAMATION;
			break;
		    case '(':
			offset++;
			return ELTokenContext.LPAREN;
		    case ')':
			offset++;
			return ELTokenContext.RPAREN;
	            case ',':
			offset++;
			return ELTokenContext.COMMA;
		    case '?':
			offset++;
			return ELTokenContext.QUESTION;
		    case '\n':
			offset++;
			// - System.out.print("r=EOL )");
			return ELTokenContext.EOL;
		    case '0':
			state = ISA_ZERO;
			break;
		    case '.':
			state = ISA_DOT;
			break;
		    default:
			// Check for whitespace
			if (Character.isWhitespace(ch)) {
			    state = ISI_WHITESPACE;
			    break;
			}
			
			// check whether it can be identifier
			if (Character.isJavaIdentifierStart(ch)){
			    // - System.out.print(" state->ISI_IDENTIFIER ");
			    state = ISI_IDENTIFIER;
			    break;
			}
			// Check for digit
			if (Character.isDigit(ch)) {
			    state = ISI_INT;
			    break;
			}
			// - System.out.print(" r=INVALID_CHAR )");
			offset++;
			return ELTokenContext.INVALID_CHAR;
			//break;
		    }
		// - System.out.print(")");
                break;
		
	    	
	    case ISI_WHITESPACE: // white space
                if (!Character.isWhitespace(ch)) {
                    state = INIT;
                    return ELTokenContext.WHITESPACE;
                }
                break;
		
	   case ISI_BRACKET:
		switch (ch){
		    case ']':
			state = INIT;
			//offset++;
			return ELTokenContext.IDENTIFIER;
		    case '"':
			offset++;
			return ELTokenContext.LBRACKET;
		    case '\'':
			offset++;
			return ELTokenContext.LBRACKET;
		    case '/':
			offset++;
			return ELTokenContext.DIV;
		    case '+':
			offset++;
			return ELTokenContext.PLUS;
		    case '-':
			offset++;
			return ELTokenContext.MINUS;
		    case '*':
			offset++;
			return ELTokenContext.MUL;
		    case '[':
			offset++;
			return ELTokenContext.LBRACKET;
		    case '%':
			offset++;
			return ELTokenContext.MOD;
		    case ':':
			offset++;
			return ELTokenContext.COLON;
		    case '(':
			offset++;
			return ELTokenContext.LPAREN;
		    case ')':
			offset++;
			return ELTokenContext.RPAREN;
		    case ',':
			offset++;
			return ELTokenContext.COMMA;	
		    case '?':
			offset++;
			return ELTokenContext.QUESTION;
		    case '=':
			state = ISI_BRACKET_ISA_EQ;
			break;
		    case '>':
			state = ISI_BRACKET_ISA_GT;
			break;
		    case '<':
			state = ISI_BRACKET_ISA_LT;
			break;
		    case '|':
			state = ISI_BRACKET_ISA_PIPE;
			break;
		    case '&':
			state = ISI_BRACKET_ISA_AND;
			break;
		    case '0':
			state = ISI_BRACKET_ISA_ZERO;
			break;
		    case '.':
			state = ISI_BRACKET_ISA_DOT;
			break;
		    default :
			// Check for whitespace
			if (Character.isWhitespace(ch)) {
			    state = ISI_BRACKET_A_WHITESPACE;
			    break;
			}
			if (Character.isJavaIdentifierStart(ch)){
			    // - System.out.print(" state->ISI_IDENTIFIER ");
			    state = ISI_BRACKET_A_IDENTIFIER;
			    break;
			}
			// Check for digit
			if (Character.isDigit(ch)) {
			    state = ISI_BRACKET_ISI_INT;
			    break;
			}
			// - System.out.print(" r=INVALID_CHAR )");
			offset++;
			return ELTokenContext.INVALID_CHAR;
			//break;
		}
		break;
		   
	    case ISI_BRACKET_A_WHITESPACE:
		if (!Character.isWhitespace(ch)) {
                    state = ISI_BRACKET;
                    return ELTokenContext.WHITESPACE;
                }
		break;
		
	    case ISI_BRACKET_ISA_EQ:
	    case ISA_EQ:
                switch (ch) {
                case '=':
                    offset++;
                    return  ELTokenContext.EQ_EQ;
                default:
		    state = (state == ISI_BRACKET_ISA_EQ) ? ISI_BRACKET : INIT;
		    offset--;
                    //return ELTokenContext.INVALID_CHAR;
                }
                break;
		
	    case ISI_BRACKET_ISA_GT:
	    case ISA_GT:
                switch (ch) {
                case '=':
                    offset++;
                    return ELTokenContext.GT_EQ;
                default:
                    state = (state == ISI_BRACKET_ISA_GT) ? ISI_BRACKET : INIT;
                    return ELTokenContext.GT;
                }
                //break;
	    case ISI_BRACKET_ISA_LT:	
            case ISA_LT:
                switch (ch) {
                case '=':
                    offset++;
                    return ELTokenContext.LT_EQ;
                default:
                    state = (state == ISI_BRACKET_ISA_LT) ? ISI_BRACKET : INIT;
                    return ELTokenContext.LT;
                }
                //break;
	    case ISI_BRACKET_ISA_PIPE:
	    case ISA_PIPE:
		switch (ch) {                
		case '|':
		    offset++;
		    state = INIT;
		    return ELTokenContext.OR_OR;
		default:
		    state = (state == ISI_BRACKET_ISA_PIPE) ? ISI_BRACKET : INIT;
		    offset--;
		}
		break;
	    case ISI_BRACKET_ISA_AND:	
	    case ISA_AND:
                switch (ch) {                
                case '&':
                    offset++;
                    state = INIT;
                    return ELTokenContext.AND_AND;
                default:
                    state = (state == ISI_BRACKET_ISA_AND) ? ISI_BRACKET : INIT;
                    offset--;
                }
                break;
	    case ISA_EXCLAMATION:
                switch (ch) {
                case '=':
                    offset++;
                    state = INIT;
                    return ELTokenContext.NOT_EQ;
                default:
                    state = INIT;
                    return ELTokenContext.NOT;
                }
	    case ISI_STRING:
		// - System.out.print(" ISI_STRING (");
		switch (ch) {
		    case '\\':
			// - System.out.print(" state->ISI_STRING_A_BSLASH");
			state = ISI_STRING_A_BSLASH;
			break;
		    case '\n':
			state = INIT;
			// - System.out.print(" state->INIT r=STRING_LITERAL )");
			return ELTokenContext.STRING_LITERAL;
		    case '"': // NOI18N
			offset++;
			state = INIT;
			// - System.out.print(" state->INIT r=STRING_LITERAL )");
			return ELTokenContext.STRING_LITERAL;
		    }
		// - System.out.print(")");
                break;
	    case ISI_STRING_A_BSLASH: 
		// - System.out.print(" ISI_STRING_A_BSLASH (");
		//switch (ch){
		//    case '"':
			state = ISI_STRING;
			// - System.out.print(" state->INIT ");
		// - System.out.print(")");
		break;	
	     case ISI_BRACKET_A_IDENTIFIER:
	     case ISI_IDENTIFIER:		 
		 // - System.out.print(" ISI_IDENTIFIER (");
		 if (!(Character.isJavaIdentifierPart(ch))){
		     switch (state){
			 case ISI_IDENTIFIER:
			    state = INIT; break;
			 case ISI_BRACKET_A_IDENTIFIER:
			    state = ISI_BRACKET;
			    break;
		     }
		     // - System.out.print(" state->INIT ");
		     
		     TokenID tid = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
		     if (tid == null){
			 if (ch == ':'){
			     // - System.out.print(" r=TAG_LIB_PREFIX) ");
			    tid = ELTokenContext.TAG_LIB_PREFIX;
			 }
			 else{
			     tid = ELTokenContext.IDENTIFIER;
			     // - System.out.print(" r=IDENTIFIER)");
			 }
		     }
		     else {
			 // - System.out.println(" r=KEYWORDS )");
		     }		
		    return tid;
		 }
		// - System.out.print(")");
		break;
		
	    case ISI_CHAR:
		// - System.out.print(" ISI_CHAR (");
                switch (ch) {
                case '\\':
		    // - System.out.print(" state->ISI_CHAR_A_BSLASH )");
                    state = ISI_CHAR_A_BSLASH;
                    break;
		case '\n':
                    state = INIT;
		    // - System.out.print(" state->INIT r=CHAR_LITERAL )");
                    return ELTokenContext.CHAR_LITERAL;
                case '\'':
                    offset++;
                    state = INIT;
		    // - System.out.print(" state->INIT r=CHAR_LITERAL )");
                    return ELTokenContext.CHAR_LITERAL;
		default :
		    
		    if (buffer[offset-1] != '\'' && buffer[offset-1] != '\\'){
			// - System.out.print(" state->ISI_CHAR_STRING ");
			state = ISI_CHAR_STRING;
		    }
			
                }
		// - System.out.print(")");    
                break;

            case ISI_CHAR_A_BSLASH:
                switch (ch) {
                case '\'':
                case '\\':
                    break;
                default:
                    offset--;
                    break;
                }
                state = ISI_CHAR;
                break;
		
	     case ISI_CHAR_STRING:
		// - System.out.print(" ISI_CHAR_STRING (");
                switch (ch) {
                case '\\':
		    // - System.out.print(" state->ISI_CHAR_A_BSLASH )");
                    state = ISI_CHAR_STRING_A_BSLASH;
                    break;
		case '\n':
                    state = INIT;
		    // - System.out.print(" state->INIT r=STRING_LITERAL )");
                    return ELTokenContext.STRING_LITERAL;
                case '\'':
                    offset++;
                    state = INIT;
		    // - System.out.print(" state->INIT r=STRING_LITERAL )");
                    return ELTokenContext.STRING_LITERAL;
                }
		// - System.out.print(")");    
                break;
		
            case ISI_CHAR_STRING_A_BSLASH:
                switch (ch) {
                case '\'':
                case '\\':
                    break;
                default:
                    offset--;
                    break;
                }
                state = ISI_CHAR_STRING;
                break;
		
	    case ISI_BRACKET_ISA_ZERO:
	    case ISA_ZERO:
                switch (ch) {
                case '.':
		    state = (state == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET_ISI_DOUBLE : ISI_DOUBLE;
                    break;
                case 'x':
                case 'X':
		    state = (state == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET_ISI_HEX : ISI_HEX;
                    break;
                case 'l':
                case 'L':
                    offset++;
                    state = (state == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET : INIT;
                    return ELTokenContext.LONG_LITERAL;
                case 'f':
                case 'F':
                    offset++;
                    state = (state == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET : INIT;
                    return ELTokenContext.FLOAT_LITERAL;
                case 'd':
                case 'D':
                    offset++;
                    state = (state == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET : INIT;
                    return ELTokenContext.DOUBLE_LITERAL;
                case '8': // it's error to have '8' and '9' in octal number
                case '9': 
                    state = (state == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET : INIT;
                    offset++;
                    return ELTokenContext.INVALID_OCTAL_LITERAL;
                case 'e':
                case 'E':
		    state = (state == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET_ISI_DOUBLE_EXP : ISI_DOUBLE_EXP;
                    break;
                default:
                    if (Character.isDigit(ch)) { // '8' and '9' already handled
			state = (state == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET_ISI_OCTAL : ISI_OCTAL;
                        break;
                    }
                    state = (state == ISI_BRACKET_ISA_ZERO) ? ISI_BRACKET : INIT;
                    return ELTokenContext.INT_LITERAL;
                }
                break;
		
	    case ISI_BRACKET_ISI_INT:
	    case ISI_INT:
                switch (ch) {
                case 'l':
                case 'L':
                    offset++;
                    state = (state == ISI_BRACKET_ISI_INT) ? ISI_BRACKET : INIT;
                    return ELTokenContext.LONG_LITERAL;
                case '.':
		    state = (state == ISI_BRACKET_ISI_INT) ? ISI_BRACKET_ISI_DOUBLE : ISI_DOUBLE;
                    break;
                case 'f':
                case 'F':
                    offset++;
                    state = (state == ISI_BRACKET_ISI_INT) ? ISI_BRACKET : INIT;
                    return ELTokenContext.FLOAT_LITERAL;
                case 'd':
                case 'D':
                    offset++;
                    state = (state == ISI_BRACKET_ISI_INT) ? ISI_BRACKET : INIT;
                    return ELTokenContext.DOUBLE_LITERAL;
                case 'e':
                case 'E':
                    state = ISI_DOUBLE_EXP;
                    break;
                default:
                    if (!(ch >= '0' && ch <= '9')) {
                        state = (state == ISI_BRACKET_ISI_INT) ? ISI_BRACKET : INIT;
                        return ELTokenContext.INT_LITERAL;
                    }
                }
                break;
		
	    case ISI_BRACKET_ISI_OCTAL:
            case ISI_OCTAL:
                if (!(ch >= '0' && ch <= '7')) {
                    state = (state == ISI_BRACKET_ISI_OCTAL) ? ISI_BRACKET : INIT;
                    return ELTokenContext.OCTAL_LITERAL;
                }
                break;
		
	    case ISI_BRACKET_ISI_DOUBLE:
            case ISI_DOUBLE:
                switch (ch) {
                case 'f':
                case 'F':
                    offset++;
                    state = (state == ISI_BRACKET_ISI_DOUBLE) ? ISI_BRACKET : INIT;
                    return ELTokenContext.FLOAT_LITERAL;
                case 'd':
                case 'D':
                    offset++;
                    state = (state == ISI_BRACKET_ISI_DOUBLE) ? ISI_BRACKET : INIT;
                    return ELTokenContext.DOUBLE_LITERAL;
                case 'e':
                case 'E':
		    state = (state == ISI_BRACKET_ISI_DOUBLE) ? ISI_BRACKET_ISI_DOUBLE_EXP : ISI_DOUBLE_EXP;
                    break;
                default:
                    if (!((ch >= '0' && ch <= '9')
                            || ch == '.')) {
                        state = (state == ISI_BRACKET_ISI_DOUBLE) ? ISI_BRACKET : INIT;
                        return ELTokenContext.DOUBLE_LITERAL;
                    }
                }
                break;
		
	    case ISI_DOUBLE_EXP:
            case ISI_BRACKET_ISI_DOUBLE_EXP:
                switch (ch) {
                case 'f':
                case 'F':
                    offset++;
                    state = (state == ISI_BRACKET_ISI_DOUBLE_EXP) ? ISI_BRACKET : INIT;
                    return ELTokenContext.FLOAT_LITERAL;
                case 'd':
                case 'D':
                    offset++;
                    state = (state == ISI_BRACKET_ISI_DOUBLE_EXP) ? ISI_BRACKET : INIT;
                    return ELTokenContext.DOUBLE_LITERAL;
		case '-':
		case '+':
		    state = ISI_DOULE_EXP_ISA_SIGN;
		    break;
                default:
                    if (!Character.isDigit(ch)){
                            //|| ch == '-' || ch == '+')) {
                        state = (state == ISI_BRACKET_ISI_DOUBLE_EXP) ? ISI_BRACKET : INIT;
                        return ELTokenContext.DOUBLE_LITERAL;
                    }
                }
                break;
		
	    case ISI_DOULE_EXP_ISA_SIGN:
	    case ISI_BRACKET_ISI_DOULE_EXP_ISA_SIGN:	
		if (!Character.isDigit(ch)){
		    state = (state == ISI_BRACKET_ISI_DOULE_EXP_ISA_SIGN) ? ISI_BRACKET : INIT;
		    return ELTokenContext.DOUBLE_LITERAL;
		}
		break;
		
	    case ISI_BRACKET_ISI_HEX:
            case ISI_HEX:
                if (!((ch >= 'a' && ch <= 'f')
                        || (ch >= 'A' && ch <= 'F')
                        || Character.isDigit(ch))
                   ) {
                    state = (state == ISI_BRACKET_ISI_HEX) ? ISI_BRACKET : INIT;
                    return ELTokenContext.HEX_LITERAL;
                }
                break;
	    
	    case ISI_BRACKET_ISA_DOT:
            case ISA_DOT:
                if (Character.isDigit(ch)) {
                    state = (state == ISI_BRACKET_ISA_DOT) ? ISI_BRACKET_ISI_DOUBLE : ISI_DOUBLE;

                } else { // only single dot
                    state = (state == ISI_BRACKET_ISA_DOT) ? ISI_BRACKET : INIT;
                    return ELTokenContext.DOT;
                }
                break;
		
            } // end of switch(state)

            offset++; // move to the next char
        }

        /* At this state there's no more text in the scanned buffer.
        * The caller will decide either to stop scanning at all
        * or to relocate scanning and provide next buffer with characters.
        * The lastBuffer variable indicates whether the scanning will
        * stop (true) or the caller will provide another buffer
        * to continue on (false) and call relocate() to continue on the given buffer.
        * If this is the last buffer, the analyzer must ensure
        * that for all internal states there will be some token ID returned.
        * The easiest way how to ensure that all the internal states will
        * be covered is to copy all the internal state constants and
        * put them after the switch() and provide the code that will return
        * appropriate token ID.
        *
        * When there are no more characters available in the buffer
        * and the buffer is not the last one the analyzer can still
        * decide to return the token ID even if it doesn't know whether
        * the token is complete or not. This is possible in this simple
        * implementation for example because it doesn't matter whether
        * it returns the text all together or broken into several pieces.
        * The advantage of such aproach is that the preScan value
        * is minimized which avoids the additional increasing of the buffer
        * by preScan characters, but on the other hand it can become
        * problematic if the token should be forwarded for some further
        * processing. For example it could seem handy to return incomplete
        * token for java block comments but it could become difficult
        * if we would want to analyzer these comment tokens additionally
        * by the HTML analyzer for example.
        */

        // Normally the following block would be done only for lastBuffer == true
        // but in this case it can always be done
	if (lastBuffer){	    
	    switch (state) {
		case ISI_WHITESPACE:
		    state = INIT;
		    return ELTokenContext.WHITESPACE;
		case ISI_IDENTIFIER:
		    state = INIT;
		    TokenID kwd = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
		    return (kwd != null) ? kwd : ELTokenContext.IDENTIFIER;
		case ISI_STRING:
		case ISI_STRING_A_BSLASH:
		    return ELTokenContext.STRING_LITERAL; // hold the state
		case ISI_CHAR:
		case ISI_CHAR_A_BSLASH:
		    return ELTokenContext.CHAR_LITERAL;
		case ISI_CHAR_STRING :
		case ISI_CHAR_STRING_A_BSLASH :
		    return ELTokenContext.STRING_LITERAL;
		case ISA_ZERO:
		case ISI_INT:
		    state = INIT;
		    return ELTokenContext.INT_LITERAL;
		case ISI_OCTAL:
		    state = INIT;
		    return ELTokenContext.OCTAL_LITERAL;
		case ISI_DOUBLE:
		case ISI_DOUBLE_EXP:
		case ISI_DOULE_EXP_ISA_SIGN:
		case ISI_BRACKET_ISI_DOULE_EXP_ISA_SIGN:    
                    state = INIT;
	            return ELTokenContext.DOUBLE_LITERAL;
		case ISI_HEX:
		    state = INIT;
		    return ELTokenContext.HEX_LITERAL;
		case ISA_DOT:
		    state = INIT;
		    return ELTokenContext.DOT;
		case ISA_EQ:
		    state = INIT;
		    return ELTokenContext.EQ_EQ;
		case ISA_GT:
		    state = INIT;
		    return ELTokenContext.GT;
		case ISA_LT:
		    state = INIT;
		    return ELTokenContext.LT;
		case ISA_PIPE:
		    state = INIT;
		    return ELTokenContext.OR_OR;
		case ISA_AND:
		    state = INIT;
		    return ELTokenContext.AND_AND;
		case ISA_EXCLAMATION:
		    state = INIT;
		    return ELTokenContext.NOT;
		case ISI_BRACKET:		
		case ISI_BRACKET_A_IDENTIFIER:   
		    state = INIT;
		    return ELTokenContext.IDENTIFIER;
		case ISI_BRACKET_A_WHITESPACE:
		    state = ISI_BRACKET;
		    return ELTokenContext.WHITESPACE;
		case ISI_BRACKET_ISA_EQ:
		    state = ISI_BRACKET;
		    return ELTokenContext.EQ_EQ;
		case ISI_BRACKET_ISA_GT:
		    state = ISI_BRACKET;
		    return ELTokenContext.GT_EQ;
		case ISI_BRACKET_ISA_LT:
		    state = ISI_BRACKET;
		    return ELTokenContext.LT_EQ;
		case ISI_BRACKET_ISA_AND:
		    state = ISI_BRACKET;
		    return ELTokenContext.AND_AND;
		case ISI_BRACKET_ISA_PIPE:
		    state = ISI_BRACKET;
		    return ELTokenContext.OR_OR;
		case ISI_BRACKET_ISA_DOT:
		    state = ISI_BRACKET;
		    return ELTokenContext.DOT;
		case ISI_BRACKET_ISA_ZERO:    
		case ISI_BRACKET_ISI_INT:
		    state = ISI_BRACKET;
		    return ELTokenContext.INT_LITERAL;
		
		    
	    }
	}
	    
        return null;
    }
    
    
    public static TokenID matchKeyword(char[] buffer, int offset, int len) {	
        if (len > 10)
            return null;
        if (len <= 1)
            return null;
        switch (buffer[offset++]) {
            case 'a':
		if (len <= 2) return null;
		return (len == 3
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'd')
                        ? ELTokenContext.AND_KEYWORD : null;
	    case 'd':
		if (len <= 2) return null;
		return (len == 3
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'v')
                        ? ELTokenContext.DIV_KEYWORD : null;
	    case 'e':
		switch (buffer[offset++]) {
		    case 'q':
			return (len == 2) ? ELTokenContext.EQ_KEYWORD : null;
		    case 'm':
			return (len == 5
                        && buffer[offset++] == 'p'
			&& buffer[offset++] == 't'
                        && buffer[offset++] == 'y')
                        ? ELTokenContext.EMPTY_KEYWORD : null;
		    default:
			return null;
		}
	    case 'f':
		return (len == 5
		    && buffer[offset++] == 'a'
		    && buffer[offset++] == 'l'
		    && buffer[offset++] == 's'
		    && buffer[offset++] == 'e')
		    ? ELTokenContext.FALSE_KEYWORD : null;
	    case 'g':
		switch (buffer[offset++]){
		    case 'e':
			return (len == 2) ? ELTokenContext.GE_KEYWORD : null;
		    case 't':
			return (len == 2) ? ELTokenContext.GT_KEYWORD : null;
		    default:
			return null;
		}
	    case 'l':
		switch (buffer[offset++]){
		    case 'e':
			return (len == 2) ? ELTokenContext.LE_KEYWORD : null;
		    case 't':
			return (len == 2) ? ELTokenContext.LT_KEYWORD : null;
		    default:
			return null;
		}
	    case 'i':
		if (len <= 9) return null;
		return (len == 10
                        && buffer[offset++] == 'n'
			&& buffer[offset++] == 's'
			&& buffer[offset++] == 't'
			&& buffer[offset++] == 'a'
			&& buffer[offset++] == 'n'
			&& buffer[offset++] == 'c'
			&& buffer[offset++] == 'e'
			&& buffer[offset++] == 'o'
                        && buffer[offset++] == 'f')
                        ? ELTokenContext.INSTANCEOF_KEYWORD : null;
	    case 'm':
		if (len <= 2) return null;
		return (len == 3
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'd')
                        ? ELTokenContext.MOD_KEYWORD : null;
	    case 'n':
		switch (buffer[offset++]){
		    case 'e':
			return (len == 2) ? ELTokenContext.NE_KEYWORD : null;
		    case 'o':
			return (len == 3
			    && buffer[offset++] == 't')
			    ? ELTokenContext.NOT_KEYWORD : null;
		    case 'u':
			return (len == 4
			    && buffer[offset++] == 'l'
			    && buffer[offset++] == 'l')
			    ? ELTokenContext.NULL_KEYWORD : null;
		    default:
			return null;
		}
	    case 'o':
		return (len == 2 
		    && buffer[offset++] == 'r')
		    ? ELTokenContext.OR_KEYWORD : null;
	    case 't':
		return (len == 4
		    && buffer[offset++] == 'r'
		    && buffer[offset++] == 'u'
		    && buffer[offset++] == 'e')
		    ? ELTokenContext.TRUE_KEYWORD : null;

	    default : 
		return null;
	}
    }
    
    public String getStateName(int stateNumber) {
	switch (stateNumber){
	    case ISI_IDENTIFIER		: return "jsp_el_ISI_IDENTIFIER";   //NOI18N
	    case ISI_CHAR		: return "el_ISI_CHAR";		    //NOI18N  
	    case ISI_CHAR_A_BSLASH	: return "el_ISI_CHAR_A_BSLASH";    //NOI18N
	    case ISI_STRING		: return "el_ISI_STRING";	    //NOI18N
	    case ISI_STRING_A_BSLASH	: return "el_ISI_STRING_A_BSLASH";  //NOI18N
	    case ISI_CHAR_STRING	: return "el_ISI_CHAR_STRING";	    //NOI18N
	    case ISI_CHAR_STRING_A_BSLASH : return "el_ISI_CHAR_STRING_A_BSLASH";//NOI18N
	    case ISA_ZERO		: return "el_ISA_ZERO";		    //NOI18N
	    case ISI_INT		: return "el_ISI_INT";		    //NOI18N
	    case ISI_OCTAL		: return "el_ISI_OCTAL";	    //NOI18N
	    case ISI_DOUBLE		: return "el_ISI_DOUBLE";	    //NOI18N
	    case ISI_DOUBLE_EXP		: return "el_ISI_DOUBLE_EXP";	    //NOI18N
	    case ISI_HEX		: return "el_ISI_HEX";		    //NOI18N
	    case ISA_DOT		: return "el_ISA_DOT";		    //NOI18N
	    case ISI_WHITESPACE		: return "el_ISI_WHITESPACE";	    //NOI18N
	    case ISA_EQ			: return "el_ISA_EQ";		    //NOI18N
	    case ISA_GT			: return "el_ISA_GT";		    //NOI18N
	    case ISA_LT			: return "el_ISA_LT";		    //NOI18N
	    case ISA_PIPE		: return "el_ISA_PIPE";		    //NOI18N
	    case ISA_AND		: return "el_ISA_AND";		    //NOI18N
	    case ISA_EXCLAMATION	: return "el_ISA_EXCLAMATION";	    //NOI18N
	    case ISI_BRACKET		: return "el_ISI_BRACKET";	    //NOI18N
	    case ISI_BRACKET_A_WHITESPACE: return "el_ISI_BRACKET_A_WHITSPACE";//NOI18N
	    case ISI_BRACKET_ISA_EQ	: return "el_ISI_BRACKET_ISA_EQ";   //NOI18N
	    case ISI_BRACKET_ISA_GT	: return "el_ISI_BRACKET_ISA_GT";   //NOI18N
	    case ISI_BRACKET_ISA_LT	: return "el_ISI_BRACKET_ISA_LT";   //NOI18N
	    case ISI_BRACKET_ISA_AND	: return "el_ISI_BRACKET_ISA_AND";  //NOI18N
	    case ISI_BRACKET_ISA_PIPE	: return "el_ISI_BRACKET_ISA_PIPE"; //NOI18N
	    case ISI_BRACKET_ISI_INT	: return "el_ISI_BRACKET_ISI_INT";  //NOI18N
	    case ISI_BRACKET_ISI_OCTAL	: return "el_ISI_BRACKET_ISI_OCTAL";	//NOI18N
	    case ISI_BRACKET_ISI_DOUBLE	: return "el_ISI_BRACKET_ISI_DOUBLE";	//NOI18N
	    case ISI_BRACKET_ISI_DOUBLE_EXP: return "el_ISI_BRACKET_ISI_DOUBLE_EXP";//NOI18N
	    case ISI_BRACKET_ISI_HEX	: return "el_ISI_BRACKET_ISI_HEX";  //NOI18N
	    case ISI_BRACKET_ISI_DOULE_EXP_ISA_SIGN : return "el_ISI_BRACKET_ISI_DOULE_EXP_ISA_SIGN";
	    case ISI_DOULE_EXP_ISA_SIGN : return "el_ISI_DOULE_EXP_ISA_SIGN";
	    default:
                return super.getStateName(stateNumber);
	}
    }
    
}
