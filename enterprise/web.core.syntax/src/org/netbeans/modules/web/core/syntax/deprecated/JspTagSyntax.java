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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.core.syntax.*;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;

/**
* Syntax class for JSP tags. It is not meant to be used by itself, but as one of syntaxes with
* MultiSyntax. Recognizes JSP tags, comments and directives. Does not recognize scriptlets,
* expressions and declarations, which should be rocognized by the master syntax, as expressions
* can appear embedded in a JSP tag. Moreover, they all share Java syntax.
*
* @author Petr Jiricka
* @version 1.00
* @deprecated Use JSP Lexer instead
*/
@Deprecated
public class JspTagSyntax extends Syntax {

    // Internal states
    // general
    private static final int ISI_ERROR           =  1; // when the fragment does not start with <
    private static final int ISA_LT              =  2; // after '<' char
    // tags and directives
    private static final int ISI_TAG             =  3; // inside JSP tag
    private static final int ISI_DIR             =  4; // inside JSP directive
    private static final int ISP_TAG             =  5; // after JSP tag
    private static final int ISP_DIR             =  6; // after JSP directive
    private static final int ISI_TAG_I_WS        =  7; // inside JSP tag after whitespace
    private static final int ISI_DIR_I_WS        =  8; // inside JSP directive after whitespace
    private static final int ISI_ENDTAG          =  9; // inside end JSP tag
    private static final int ISI_TAG_ATTR        = 10; // inside tag attribute
    private static final int ISI_DIR_ATTR        = 11; // inside directive attribute
    private static final int ISP_TAG_EQ          = 12; // just after '=' in tag
    private static final int ISP_DIR_EQ          = 13; // just after '=' in directive
    private static final int ISI_TAG_STRING      = 14; // inside string (value - "") in tag
    private static final int ISI_DIR_STRING      = 15; // inside string (value - "") in directive
    private static final int ISI_TAG_STRING_B    = 16; // inside string (value - "") after backslash in tag
    private static final int ISI_DIR_STRING_B    = 17; // inside string (value - "") after backslash in directive
    private static final int ISI_TAG_STRING2     = 18; // inside string (value - '') in tag
    private static final int ISI_DIR_STRING2     = 19; // inside string (value - '') in directive
    private static final int ISI_TAG_STRING2_B   = 20; // inside string (value - '') after backslash in tag
    private static final int ISI_DIR_STRING2_B   = 21; // inside string (value - '') after backslash in directive
    private static final int ISA_ENDSLASH        = 22; // after ending '/' in JSP tag
    private static final int ISA_ENDPC           = 23; // after ending '%' in JSP directive
    // comments (+directives)
    private static final int ISA_LT_PC           = 24; // after '<%' (comment or directive)
    private static final int ISI_JSP_COMMENT     = 25; // after <%-
    
    private static final int ISI_JSP_COMMENT_M   = 26; // inside JSP comment after -
    private static final int ISI_JSP_COMMENT_MM  = 27; // inside JSP comment after --
    private static final int ISI_JSP_COMMENT_MMP = 28; // inside JSP comment after --%
    // end state
    static final int ISA_END_JSP                 = 29; // JSP fragment has finished and control 
                                                       // should be returned to master syntax
    // more errors                                                   
    private static final int ISI_TAG_ERROR       = 30; // error in tag, can be cleared by > or \n
    private static final int ISI_DIR_ERROR       = 31; // error in directive, can be cleared by %>, \n, \t or space
    private static final int ISI_DIR_ERROR_P     = 32; // error in directive after %, can be cleared by > or \n
    
    private static final int ISA_LT_PC_AT        = 33; // after '<%@' (directive)
    private static final int ISA_LT_SLASH        =  34; // after '</' sequence
    private static final int ISA_LT_PC_DASH      = 35; // after <%- ;not comment yet
    
  
    public JspTagSyntax() {
        tokenContextPath = JspTagTokenContext.contextPath;
    }

    public boolean isIdentifierPart(char ch) {
        return Character.isJavaIdentifierPart(ch);
    }

    protected TokenID parseToken() {
        char actChar;
        int _state; //help state

        while(offset < stopOffset) {
            actChar = buffer[offset];
            //System.out.println("JspTagSyntax: offset: " + offset + " actchar: '" + actChar
            //    + "' state: " + getStateName(state));
            switch (state) {
                case INIT:
                    switch (actChar) {
                        case '\n':
                            offset++;
                            return JspTagTokenContext.EOL;
                        case '<':
                            state = ISA_LT;
                            break;
                        default:
                            state = ISI_ERROR;
                            break;
                    }
                    break;

                case ISA_LT:
                    if (Character.isLetter(actChar) ||
                        (actChar == '_')
                    ) { // possible tag begining
                        state = ISI_TAG;
                        return JspTagTokenContext.SYMBOL;
                    }

                    switch (actChar) {
                        case '/':
                            state = ISA_LT_SLASH;
                            break;
                        case '\n':
                            state = ISI_TAG_ERROR;
                            return JspTagTokenContext.SYMBOL;
                        case '%':
                            state = ISA_LT_PC;
                            break;
                        default:
                            state = ISI_TAG_ERROR;
                            break;
                    }
                    break;
          
                case ISA_LT_SLASH:
                    if (Character.isLetter(actChar) ||
                        (actChar == '_')) {
                        //possible end tag beginning
                        //offset++;
                        state = ISI_ENDTAG;
                        return JspTagTokenContext.SYMBOL;
                    }
                    
                    //everyting alse is an error
                    state = ISI_TAG_ERROR;
                    break;
                    
                case ISI_TAG:
                case ISI_DIR:
                    if (!(Character.isLetter(actChar) ||
                          Character.isDigit(actChar) ||
                          (actChar == '_') ||
                          (actChar == '-') ||
                          (actChar == ':') || 
                          (actChar == '.'))
                    ) { // not alpha
                        if (actChar == '<'){
                            _state = state;
                            state = ISA_END_JSP;
                            return ((_state == ISI_TAG) ? JspTagTokenContext.TAG : JspDirectiveTokenContext.TAG);//TODO - parser
                        }
                        _state = state;
                        state = ((state == ISI_TAG) ? ISP_TAG : ISP_DIR);
                        return ((_state == ISI_TAG) ? JspTagTokenContext.TAG : JspDirectiveTokenContext.TAG); //TODO - parser
                    }
                    break;

                case ISP_TAG:
                case ISP_DIR:
                    if (Character.isLetter(actChar) ||
                        (actChar == '_')
                    ) {
                        state = ((state == ISP_TAG) ? ISI_TAG_ATTR : ISI_DIR_ATTR);
                        break;
                    }
                    switch (actChar) {
                        case '\n':
                            if (offset == tokenOffset) { // no char
                                offset++;
                                return ((state == ISP_TAG) ? JspTagTokenContext.EOL : JspDirectiveTokenContext.EOL );//TODO - parser
                            } else { // return string first
                                return ((state == ISP_TAG) ? JspTagTokenContext.TAG : JspDirectiveTokenContext.TAG );//TODO - parser
                            }
                        case '>': // for tags
                            if (state == ISP_TAG) {
                                if (offset == tokenOffset) {  // no char
                                    offset++;
                                    state = ISA_END_JSP;
                                    return JspTagTokenContext.SYMBOL;
                                }
                                else { // return string first
                                    return JspTagTokenContext.TAG;
                                }
                            }  
                            else { // directive
                                //state = ISI_DIR_ERROR; 
                                //commented out to minimize errors during the process of writing directives
                                break;
                            }
                        case '/': // for tags
                            if (state == ISP_TAG) {
                                if (offset == tokenOffset) {  // no char
                                    state = ISA_ENDSLASH;
                                    break;
                                }
                                else { // return string first
                                    return JspTagTokenContext.TAG;
                                }
                            }  
                            else { // directive
                                //state = ISI_DIR_ERROR;
                                //commented out to minimize errors during the process of writing directives
                                break;
                            }
                        case '%': // for directives
                            if (state == ISP_DIR) {
                                if (offset == tokenOffset) {  // no char
                                    state = ISA_ENDPC;
                                    break;
                                }
                                else { // return string first
                                    return JspDirectiveTokenContext.TAG;
                                }
                            }  
                            else { // tag
                                state = ISI_TAG_ERROR;
                                break;
                            }
                        case '=':
                            offset++;
                            _state = state;
                            state = ((state == ISP_TAG) ? ISP_TAG_EQ : ISP_DIR_EQ);
                            return ((_state == ISP_TAG) ? JspTagTokenContext.SYMBOL : JspDirectiveTokenContext.SYMBOL);//TODO - parser
                        case ' ':
                        case '\t':
                            state = ((state == ISP_TAG) ? ISI_TAG_I_WS : ISI_DIR_I_WS);
                            break;
                        case '<': // assume that this is the start of the next tag
                            _state = state;
                            state=ISA_END_JSP;
                            return ((_state == ISP_TAG) ? JspTagTokenContext.TAG : JspDirectiveTokenContext.TAG );//TODO - parser
                        default: //numbers or illegal symbols
                            state = ((state == ISP_TAG) ? ISI_TAG_ERROR : ISI_DIR_ERROR);
                            break;
                    }
                    break;
          
                case ISI_TAG_I_WS:
                case ISI_DIR_I_WS:
                    switch (actChar) {
                        case ' ':
                        case '\t':
                            break;
                        case '<':
                            _state = state;
                            state = ISA_END_JSP;
                            return ((_state == ISI_TAG_I_WS) ? JspTagTokenContext.TAG : JspDirectiveTokenContext.TAG);//TODO - parser
                        default:
                            _state = state;
                            state = ((state == ISI_TAG_I_WS) ? ISP_TAG : ISP_DIR);
                            return ((_state == ISI_TAG_I_WS) ? JspTagTokenContext.WHITESPACE : JspDirectiveTokenContext.WHITESPACE );//TODO - parser
                    }
                    break;

                case ISI_ENDTAG:
                    if (!(Character.isLetter(actChar) ||
                          Character.isDigit(actChar) ||
                          (actChar == '_') ||
                          (actChar == '-') ||
                          (actChar == ':'))
                    ) { // not alpha
                        state = ISP_TAG;
                        return JspTagTokenContext.TAG;
                    }
                    break;

                case ISI_TAG_ATTR:
                case ISI_DIR_ATTR:
                    if (!(Character.isLetter(actChar) ||
                          Character.isDigit(actChar) ||
                          (actChar == '_') ||
                          (actChar == ':') ||
                          (actChar == '-'))
                    ) { // not alpha or '-' (http-equiv)
                        _state = state;
                        state = ((state == ISI_TAG_ATTR) ? ISP_TAG : ISP_DIR);
                        return ((_state == ISI_TAG_ATTR) ? JspTagTokenContext.ATTRIBUTE : JspDirectiveTokenContext.ATTRIBUTE);//TODO - parser
                    }
                    break;

                case ISP_TAG_EQ:
                case ISP_DIR_EQ:
                    switch (actChar) {
                        case '\n':
                            if (offset == tokenOffset) { // no char
                                offset++;
                                return ((state == ISP_TAG_EQ) ? JspTagTokenContext.EOL : JspDirectiveTokenContext.EOL );//TODO - parser
                            } else { // return string first
                                return ((state == ISP_TAG_EQ) ? JspTagTokenContext.ATTR_VALUE : JspDirectiveTokenContext.ATTR_VALUE );//TODO - parser
                            }
                        case '"':
                            state = ((state == ISP_TAG_EQ) ? ISI_TAG_STRING : ISI_DIR_STRING);
                            break;
                        case '\'':
                            state = ((state == ISP_TAG_EQ) ? ISI_TAG_STRING2 : ISI_DIR_STRING2);
                            break;
                        case ' ':
                        case '\t':
                            // don't change the state
                            break;
                        default:
                            _state = state;
                            state = ((state == ISP_TAG_EQ) ? ISP_TAG : ISP_DIR);
                            return ((_state == ISP_TAG_EQ) ? JspTagTokenContext.ATTR_VALUE : JspDirectiveTokenContext.ATTR_VALUE );//TODO - parser                            
                    }
                    break;

                case ISI_TAG_STRING:
                case ISI_DIR_STRING:
                case ISI_TAG_STRING2:
                case ISI_DIR_STRING2:
                    if ((actChar == '"') && ((state == ISI_TAG_STRING) || (state == ISI_DIR_STRING))) {
                        offset++;
                        _state = state;
                        state = ((state == ISI_TAG_STRING) ? ISP_TAG : ISP_DIR);
                        return ((_state == ISI_TAG_STRING) ? JspTagTokenContext.ATTR_VALUE : JspDirectiveTokenContext.ATTR_VALUE );//TODO - parser
                    }
          
                    if ((actChar == '\'') && ((state == ISI_TAG_STRING2) || (state == ISI_DIR_STRING2))) {
                        offset++;
                        _state = state;
                        state = ((state == ISI_TAG_STRING2) ? ISP_TAG : ISP_DIR);
                        return ((_state == ISI_TAG_STRING2) ? JspTagTokenContext.ATTR_VALUE : JspDirectiveTokenContext.ATTR_VALUE );//TODO - parser
                    }

                    switch (actChar) {
                        case '\\':
                            switch (state) {
                                case ISI_TAG_STRING: 
                                    state = ISI_TAG_STRING_B;
                                    break;
                                case ISI_DIR_STRING: 
                                    state = ISI_DIR_STRING_B;
                                    break;
                                case ISI_TAG_STRING2: 
                                    state = ISI_TAG_STRING2_B;
                                    break;
                                case ISI_DIR_STRING2: 
                                    state = ISI_DIR_STRING2_B;
                                    break;
                            }
                            break;
                        case '\n':
                            if (offset == tokenOffset) { // no char
                                offset++;
                                return (((state == ISI_TAG_STRING) || (state == ISI_TAG_STRING2)) ? JspTagTokenContext.EOL : JspDirectiveTokenContext.EOL );//TODO - parser
                                
                            } else { // return string first
                                return (((state == ISI_TAG_STRING) || (state == ISI_TAG_STRING2)) ? JspTagTokenContext.ATTR_VALUE : JspDirectiveTokenContext.ATTR_VALUE );//TODO - parser                                
                            }
                    }
                    break;

                case ISI_TAG_STRING_B:
                case ISI_DIR_STRING_B:
                case ISI_TAG_STRING2_B:
                case ISI_DIR_STRING2_B:
                    switch (actChar) {
                        case '"':
                        case '\'':
                        case '\\':
                            break;
                        default:
                            offset--;
                            break;
                    }
                    switch (state) {
                        case ISI_TAG_STRING_B: 
                            state = ISI_TAG_STRING;
                            break;
                        case ISI_DIR_STRING_B: 
                            state = ISI_DIR_STRING;
                            break;
                        case ISI_TAG_STRING2_B: 
                            state = ISI_TAG_STRING2;
                            break;
                        case ISI_DIR_STRING2_B:
                            state = ISI_DIR_STRING2;
                            break;
                    }    
                    break;

                case ISA_ENDSLASH:
                    switch (actChar) {
                        case '>':
                            offset++;
                            state = ISA_END_JSP;
                            return JspTagTokenContext.SYMBOL;
                        case '\n':
                            state = ISI_TAG_ERROR;
                            return JspTagTokenContext.SYMBOL;
                        default:
                            state = ISP_TAG;
                            return JspTagTokenContext.SYMBOL;
                    }
                    //break; not reached
          
                case ISA_ENDPC:
                    switch (actChar) {
                        case '>':
                            offset++;
                            state = ISA_END_JSP;
                            return JspDirectiveTokenContext.SYMBOL;
                        case '\n':
                            state = ISI_DIR_ERROR;
                            return JspDirectiveTokenContext.SYMBOL;
                        default:
                            state = ISP_DIR;
                            return JspDirectiveTokenContext.SYMBOL;
                    }
                    //break; not reached
          
                case ISA_LT_PC:
                    switch (actChar) {
                        case '@':
                            offset++;
                            state = ISA_LT_PC_AT;
                            return JspDirectiveTokenContext.SYMBOL;
                        case '-':
                            state = ISA_LT_PC_DASH;
                            break;
                        default: // just cut it, because this will be recognized 
                                          // by master syntax as a Java scriptlet/expression/declaration
                            state = ISA_END_JSP;
                            return JspTagTokenContext.SYMBOL;
                    }
                    break;
          
                case ISA_LT_PC_DASH:
                    switch(actChar) {
                        case '-':
                            state = ISI_JSP_COMMENT;
                            break;
                        default:
                            offset++;
                            state = ISA_END_JSP;
                            return JspDirectiveTokenContext.TEXT;
                    }
                    break;
                    
                // JSP states                
                case ISI_JSP_COMMENT:
                    switch (actChar) {
                        case '\n':
                            if (offset == tokenOffset) { // no char
                                offset++;
                                return JspTagTokenContext.EOL;
                            } else { // return block comment first
                                return JspTagTokenContext.COMMENT;
                            }
                        case '-':  
                            state = ISI_JSP_COMMENT_M;
                            break;
                    }
                    break;
          
                case ISI_JSP_COMMENT_M:
                    switch (actChar) {
                        case '\n':
                            state = ISI_JSP_COMMENT;
                            if (offset == tokenOffset) { // no char
                                offset++;
                                return JspTagTokenContext.EOL;
                            } else { // return block comment first
                                return JspTagTokenContext.COMMENT;
                            }
                        case '-':
                            state = ISI_JSP_COMMENT_MM;
                            break;
                        default:
                            state = ISI_JSP_COMMENT;
                            break;
                    }
                    break;
          
                case ISI_JSP_COMMENT_MM:
                    switch (actChar) {
                        case '\n':
                            state = ISI_JSP_COMMENT;
                            if (offset == tokenOffset) { // no char
                                offset++;
                                return JspTagTokenContext.EOL;
                            } else { // return block comment first
                                return JspTagTokenContext.COMMENT;
                            }
                        case '%':
                            state = ISI_JSP_COMMENT_MMP;
                            break;
                        case '-':
                            state = ISI_JSP_COMMENT_MM;
                            break;
                        default:
                            state = ISI_JSP_COMMENT;
                            break;
                    }
                    break;
          
                case ISI_JSP_COMMENT_MMP:
                    switch (actChar) {
                        case '\n':
                            state = ISI_JSP_COMMENT;
                            if (offset == tokenOffset) { // no char
                                offset++;
                                return JspTagTokenContext.EOL;
                            } else { // return block comment first
                                return JspTagTokenContext.COMMENT;
                            }
                        case '>':
                            state = ISA_END_JSP;
                            offset++;
                            return JspTagTokenContext.COMMENT;
                        default:
                            state = ISI_JSP_COMMENT;
                            break;
                    }
                    break;
          
                case ISI_ERROR:
                    switch (actChar) {
                        case '\n':
                            state = INIT;
                            return JspTagTokenContext.ERROR;
                        case '<':
                            state = ISA_LT;
                            return JspTagTokenContext.ERROR;
                    }
                    break;
          
                case ISI_TAG_ERROR:
                    switch (actChar) {
                        case '\n':
                            if (offset == tokenOffset) { // no char
                                offset++;
                                state = ISI_TAG_I_WS;
                                return JspTagTokenContext.EOL;
                            } else { // return error first
                                return JspTagTokenContext.ERROR;
                            }
                        case '>':
                            state = ISI_TAG_I_WS;
                            return JspTagTokenContext.ERROR;
                        case ' ':
                        case '\t':
                            state = ISI_TAG;
                            return JspTagTokenContext.ERROR;
                            
                    }
                    break;
          
                case ISI_DIR_ERROR:
                    switch (actChar) {
                        case '\n':
                            if (offset == tokenOffset) { // no char
                                offset++;
                                state = ISI_DIR_I_WS;
                                return JspDirectiveTokenContext.EOL;
                            } else { // return error first
                                return JspDirectiveTokenContext.ERROR;
                            }
                        case '%':
                            state = ISI_TAG;
                            return JspDirectiveTokenContext.ERROR;    
                        case '\t':    
                        case ' ':
                            state = ISI_DIR_I_WS;
                            return JspDirectiveTokenContext.ERROR;                        
                    }
                    break;
          
                case ISI_DIR_ERROR_P:
                    switch (actChar) {
                        case '\n':
                            if (offset == tokenOffset) { // no char
                                offset++;
                                state = ISI_DIR_I_WS;
                                return JspDirectiveTokenContext.EOL;
                            } else { // return error first
                                return JspDirectiveTokenContext.ERROR;
                            }
                        case '>':
                            offset--;
                            state = ISI_DIR_I_WS;
                            return JspDirectiveTokenContext.ERROR;
                    }
                    break;
          
                case ISA_END_JSP:
                    if (offset == tokenOffset) {
                        offset++;
                        return JspTagTokenContext.AFTER_UNEXPECTED_LT;
                    }
                    else {
                        return JspTagTokenContext.TEXT;
                    }
                    //break;
                    
                // added states
                case ISA_LT_PC_AT:  
                    if (Character.isLetter(actChar) ||
                        (actChar == '_')
                    ) { // the directive starts
                        state = ISI_DIR;
                        return JspDirectiveTokenContext.TAG;
                    }

                    switch (actChar) {
                        case '\n':
                            if (offset == tokenOffset) { // no char
                                offset++;
                                return JspDirectiveTokenContext.EOL;
                            }
                            else {
                                return JspDirectiveTokenContext.TAG;
                            }
                    }
                    break;
          
            }
      
            ++offset;
        } // end of while(offset...)

        // At this stage there's no more text in the scanned buffer.
        // Scanner first checks whether this is completely the last
        // available buffer.

        if (lastBuffer) {
            switch(state) {
                case ISI_ERROR:
                case ISI_TAG_ERROR:  
                    return JspTagTokenContext.ERROR;
                case ISI_DIR_ERROR:  
                case ISI_DIR_ERROR_P:  
                    return JspDirectiveTokenContext.ERROR;
                case ISA_LT:
                case ISA_LT_SLASH:
                case ISA_ENDSLASH:
                case ISP_TAG_EQ:
                    return JspTagTokenContext.SYMBOL;
                case ISA_LT_PC:
                case ISA_LT_PC_DASH:
                case ISA_ENDPC:
                case ISP_DIR_EQ:
                    return JspDirectiveTokenContext.SYMBOL;
                case ISI_TAG:
                case ISI_ENDTAG:
                    return JspTagTokenContext.TAG;
                case ISI_DIR:
                    return JspDirectiveTokenContext.TAG;
                case ISP_TAG:
                case ISI_TAG_I_WS:
                    return JspTagTokenContext.TAG;
                case ISP_DIR:
                case ISI_DIR_I_WS:
                case ISA_LT_PC_AT:
                    return JspDirectiveTokenContext.TAG;
                case ISI_TAG_ATTR:
                    return JspTagTokenContext.ATTRIBUTE;
                case ISI_DIR_ATTR:
                    return JspDirectiveTokenContext.ATTRIBUTE;
                case ISI_TAG_STRING:
                case ISI_TAG_STRING_B:
                case ISI_TAG_STRING2:    
                case ISI_TAG_STRING2_B:
                    return JspTagTokenContext.ATTR_VALUE;
                case ISI_DIR_STRING:
                case ISI_DIR_STRING_B:
                case ISI_DIR_STRING2:
                case ISI_DIR_STRING2_B:
                    return JspDirectiveTokenContext.ATTR_VALUE;
                case ISI_JSP_COMMENT:
                case ISI_JSP_COMMENT_M:
                case ISI_JSP_COMMENT_MM:
                case ISI_JSP_COMMENT_MMP:
                    return JspTagTokenContext.COMMENT;
                case ISA_END_JSP:
                    return JspTagTokenContext.TEXT;
                default:
                    Logger.getLogger("global").log(Level.INFO, null, new Exception("Unhandled state : " + getStateName(state)));
            }
        }

        // At this stage there's no more text in the scanned buffer, but
        // this buffer is not the last so the scan will continue on another buffer.
        // The scanner tries to minimize the amount of characters
        // that will be prescanned in the next buffer.

        // pending
        
        //Following code has been commented out because it may produce incomplete tokens
        //when the parse buffer end falls somewhere inside a token. In such a case 
        //an incomplete token is created - the token ends at the offset of the parse buffer.
        //Although this approach may save some machine time due to syntax prescan minimalization,
        //it can and in fact does cause problems in a code which relies on tokens 
        //to be compact (e.g. code completion - see issue #47165).
        /*switch(state) {
            case ISI_ERROR:
            case ISI_TAG_ERROR:  
            case ISI_DIR_ERROR:  
            case ISI_DIR_ERROR_P:  
                return JspTagTokenContext.ERROR;
            case ISA_LT:
            case ISA_LT_PC:
            // case ISA_ENDSLASH:   // it is important to keep '/>' token complete
            case ISA_ENDPC:
            case ISP_TAG_EQ:
            case ISP_DIR_EQ:
                return JspTagTokenContext.SYMBOL;
            case ISI_TAG:
            case ISI_DIR:
            case ISI_ENDTAG:
                return JspTagTokenContext.TAG;
            case ISP_TAG:
            case ISP_DIR:
            case ISI_TAG_I_WS:
            case ISI_DIR_I_WS:
            case ISA_LT_PC_AT:
                return JspTagTokenContext.TAG;
            case ISI_TAG_ATTR:
            case ISI_DIR_ATTR:
                return JspTagTokenContext.ATTRIBUTE;
            case ISI_TAG_STRING:
            case ISI_DIR_STRING:
            case ISI_TAG_STRING_B:
            case ISI_DIR_STRING_B:
            case ISI_TAG_STRING2:
            case ISI_DIR_STRING2:
            case ISI_TAG_STRING2_B:
            case ISI_DIR_STRING2_B:
                return JspTagTokenContext.ATTR_VALUE;
            case ISI_JSP_COMMENT:
            case ISI_JSP_COMMENT_M:
            case ISI_JSP_COMMENT_MM:
            case ISI_JSP_COMMENT_MMP:
                return JspTagTokenContext.COMMENT;
            case ISA_END_JSP:
                return JspTagTokenContext.TEXT;
        }
*/
        return null;

    }


    public String getStateName(int stateNumber) {
        switch(stateNumber) {
            case ISI_ERROR           : return "jsptag_ISI_ERROR"; // NOI18N
            case ISA_LT              : return "jsptag_ISA_LT";    // NOI18N
            case ISI_TAG             : return "jsptag_ISI_TAG";   // NOI18N
            case ISI_DIR             : return "jsptag_ISI_DIR";   // NOI18N
            case ISP_TAG             : return "jsptag_ISP_TAG";   // NOI18N
            case ISP_DIR             : return "jsptag_ISP_DIR";   // NOI18N
            case ISI_TAG_I_WS        : return "jsptag_ISI_TAG_I_WS";   // NOI18N
            case ISI_DIR_I_WS        : return "jsptag_ISI_DIR_I_WS";   // NOI18N
            case ISI_ENDTAG          : return "jsptag_ISI_ENDTAG";     // NOI18N
            case ISI_TAG_ATTR        : return "jsptag_ISI_TAG_ATTR";   // NOI18N
            case ISI_DIR_ATTR        : return "jsptag_ISI_DIR_ATTR";   // NOI18N
            case ISP_TAG_EQ          : return "jsptag_ISP_TAG_EQ";     // NOI18N
            case ISP_DIR_EQ          : return "jsptag_ISP_DIR_EQ";     // NOI18N
            case ISI_TAG_STRING      : return "jsptag_ISI_TAG_STRING";      // NOI18N
            case ISI_DIR_STRING      : return "jsptag_ISI_DIR_STRING";      // NOI18N
            case ISI_TAG_STRING_B    : return "jsptag_ISI_TAG_STRING_B";    // NOI18N
            case ISI_DIR_STRING_B    : return "jsptag_ISI_DIR_STRING_B";    // NOI18N
            case ISI_TAG_STRING2     : return "jsptag_ISI_TAG_STRING2";     // NOI18N
            case ISI_DIR_STRING2     : return "jsptag_ISI_DIR_STRING2";     // NOI18N
            case ISI_TAG_STRING2_B   : return "jsptag_ISI_TAG_STRING2_B";   // NOI18N
            case ISI_DIR_STRING2_B   : return "jsptag_ISI_DIR_STRING2_B";   // NOI18N
            case ISA_ENDSLASH        : return "jsptag_ISA_ENDSLASH";   // NOI18N
            case ISA_ENDPC           : return "jsptag_ISA_ENDPC";      // NOI18N
            case ISA_LT_PC           : return "jsptag_ISA_LT_PC";      // NOI18N
            case ISI_JSP_COMMENT     : return "jsptag_ISI_JSP_COMMENT";       // NOI18N
            case ISI_JSP_COMMENT_M   : return "jsptag_ISI_JSP_COMMENT_M";     // NOI18N
            case ISI_JSP_COMMENT_MM  : return "jsptag_ISI_JSP_COMMENT_MM";    // NOI18N
            case ISI_JSP_COMMENT_MMP : return "jsptag_ISI_JSP_COMMENT_MMP";   // NOI18N
            case ISA_END_JSP         : return "jsptag_ISA_END_JSP";       // NOI18N
            case ISI_TAG_ERROR       : return "jsptag_ISI_TAG_ERROR";     // NOI18N
            case ISI_DIR_ERROR       : return "jsptag_ISI_DIR_ERROR";     // NOI18N
            case ISI_DIR_ERROR_P     : return "jsptag_ISI_DIR_ERROR_P";   // NOI18N
            case ISA_LT_PC_AT        : return "jsptag_ISA_LT_PC_AT";      // NOI18N
            default:
                return super.getStateName(stateNumber);
        }
    }

}

