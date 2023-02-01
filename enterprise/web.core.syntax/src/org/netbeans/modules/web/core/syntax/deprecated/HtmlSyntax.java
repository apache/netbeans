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

package org.netbeans.modules.web.core.syntax.deprecated;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;

/**
* Lexical anlyzer for HTML source files.
*
* @author Petr Nejedly
* @author Miloslav Metelka
* @version 1.00
*
* @deprecated Use Lexer API instead. See {@link HTMLLexer} and {@link HTMLTokenId}.
*/
@Deprecated
public class HtmlSyntax extends Syntax {

    /** Internal state of the lexical analyzer before entering subanalyzer of
     * character references. It is initially set to INIT, but before first usage,
     * this will be overwritten with state, which originated transition to
     * charref subanalyzer.
    */
    protected int subState = INIT;


    // Internal states
    private static final int ISI_TEXT = 1;    // Plain text between tags
    private static final int ISI_ERROR = 2;   // Syntax error in HTML syntax
    private static final int ISA_LT = 3;      // After start of tag delimiter - "<"
    private static final int ISA_SLASH = 4;   // After ETAGO - "</"
    private static final int ISI_ENDTAG = 5;  // Inside endtag - "</[a..Z]+"
    private static final int ISP_ENDTAG_X = 6;  // X-switch after ENDTAG's name
    private static final int ISP_ENDTAG_WS = 7; // In WS in ENDTAG - "</A_ _>"
    private static final int ISI_TAG = 8;     // Inside tag - "<[a..Z]+"
    private static final int ISP_TAG_X = 9;   // X-switch after TAG's name
    private static final int ISP_TAG_WS = 10; // In WS in TAG - "<A_ _...>"
    private static final int ISI_ARG = 11;    // Inside tag's argument - "<A h_r_...>"
    private static final int ISP_ARG_X = 12;  // X-switch after ARGUMENT's name
    private static final int ISP_ARG_WS = 13; // Inside WS after argument awaiting '='
    private static final int ISP_EQ = 14;     // X-switch after '=' in TAG's ARGUMENT
    private static final int ISP_EQ_WS = 15;  // In WS after '='
    private static final int ISI_VAL = 16;    // Non-quoted value
    private static final int ISI_VAL_QUOT = 17;   // Single-quoted value - may contain " chars
    private static final int ISI_VAL_DQUOT = 18;  // Double-quoted value - may contain ' chars
    private static final int ISA_SGML_ESCAPE = 19;  // After "<!"
    private static final int ISA_SGML_DASH = 20;    // After "<!-"
    private static final int ISI_HTML_COMMENT = 21; // Somewhere after "<!--"
    private static final int ISA_HTML_COMMENT_DASH = 22;  // Dash in comment - maybe end of comment
    private static final int ISI_HTML_COMMENT_WS = 23;  // After end of comment, awaiting end of comment declaration
    private static final int ISI_SGML_DECL = 24;
    private static final int ISA_SGML_DECL_DASH = 25;
    private static final int ISI_SGML_COMMENT = 26;
    private static final int ISA_SGML_COMMENT_DASH = 27;
    private static final int ISA_REF = 28;    // when comes to character reference, e.g. &amp;, after &
    private static final int ISI_REF_NAME = 29; // if the reference is symbolic - by predefined name
    private static final int ISA_REF_HASH = 30; // for numeric references - after &#
    private static final int ISI_REF_DEC = 31;  // decimal character reference, e.g. &#345;
    private static final int ISA_REF_X = 32;    //
    private static final int ISI_REF_HEX = 33;  // hexadecimal reference, in &#xa.. of &#X9..
    private static final int ISI_TAG_SLASH = 34; //after slash in html tag

    public HtmlSyntax() {
        tokenContextPath = HtmlTokenContext.contextPath;
    }

    private final boolean isAZ( char ch ) {
        return( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') );
    }

    private final boolean isName( char ch ) {
        return Character.isLetterOrDigit(ch) || 
                ch == '-' || ch == '_' || ch == '.' || ch == ':';
//        return( (ch >= 'a' && ch <= 'z') ||
//                (ch >= 'A' && ch <= 'Z') ||
//                (ch >= '0' && ch <= '9') ||
//                ch == '-' || ch == '_' || ch == '.' || ch == ':' );

    }

    /**
     * Resolves if given char is whitespace in terms of HTML4.0 specs
     * According to specs, following characters are treated as whitespace:
     * Space - <CODE>'\u0020'</CODE>, Tab - <CODE>'\u0009'</CODE>,
     * Formfeed - <CODE>'\u000C'</CODE>,Zero-width space - <CODE>'\u200B'</CODE>,
     * Carriage return - <CODE>'\u000D'</CODE> and Line feed - <CODE>'\u000A'</CODE>
     * CR's are included for completenes only, they should never appear in document
     */

    private final boolean isWS( char ch ) {
        return Character.isWhitespace(ch);
//        return ( ch == '\u0020' || ch == '\u0009' || ch == '\u000c'
//              || ch == '\u200b' || ch == '\n' || ch == '\r' );
    }

    protected TokenID parseToken() {
        char actChar;

        while(offset < stopOffset) {
            actChar = buffer[offset];
             //System.out.println("HtmlSyntax: parseToken tokenOffset=" + tokenOffset + ", actChar='" + actChar + "', offset=" + offset + ", state=" + getStateName(state) +
             //      ", stopOffset=" + stopOffset + ", lastBuffer=" + lastBuffer);
            switch( state ) {
            case INIT:              // DONE
                switch( actChar ) {
                case '<':
                    state = ISA_LT;
                    break;
                case '&':
                    state = ISA_REF;
                    subState = ISI_TEXT;
                    break;
                default:
                    state = ISI_TEXT;
                    break;
                }
                break;

            case ISI_TEXT:        // DONE
                switch( actChar ) {
                case '<':
                case '&':
                    state = INIT;
                    return HtmlTokenContext.TEXT;
                }
                break;

            case ISI_ERROR:      // DONE
                offset++;
                state = INIT;
                return HtmlTokenContext.ERROR;

            case ISA_LT:         // PENDING other transitions - e.g '<?'
                if( isAZ( actChar ) ) {   // <'a..Z'
                    state = ISI_TAG;
                    return HtmlTokenContext.TAG_OPEN_SYMBOL;
                }
                switch( actChar ) {
                case '/':               // ETAGO - </
                    state = ISA_SLASH;
                    offset++;
                    return HtmlTokenContext.TAG_OPEN_SYMBOL;
                case '>':               // Empty start tag <>, RELAXED
                    offset++;
                    state = INIT;
                    return HtmlTokenContext.TAG_CLOSE_SYMBOL;
                case '!':
                    state = ISA_SGML_ESCAPE;
                    break;
                default:                // Part of text, RELAXED
                    state = ISI_TEXT;
                    continue;             // don't eat the char, maybe its '&'
                }
                break;

            case ISA_SLASH:        // DONE
                if( isAZ( actChar ) ) {   // </'a..Z'
                    state = ISI_ENDTAG;
                    break;
                }
                switch( actChar ) {
                case '>':               // Empty end tag </>, RELAXED
                    offset++;
                    state = INIT;
                    return HtmlTokenContext.TAG_CLOSE_SYMBOL;
                default:                // Part of text, e.g. </3, </'\n', RELAXED
                    state = ISI_TEXT;
                    continue;             // don'e eat the char
                }
                //break;

            case ISI_ENDTAG:        // DONE
                if( isName( actChar ) ) break;    // Still in endtag identifier, eat next char
                state = ISP_ENDTAG_X;
                return HtmlTokenContext.TAG_CLOSE;


            case ISP_ENDTAG_X:      // DONE
                if( isWS( actChar ) ) {
                    state = ISP_ENDTAG_WS;
                    break;
                }
                switch( actChar ) {
                case '>':               // Closing of endtag, e.g. </H6 _>_
                    offset++;
                    state = INIT;
                    return HtmlTokenContext.TAG_CLOSE_SYMBOL;
                case '<':               // next tag, e.g. </H6 _<_, RELAXED
                    state = INIT;
                    continue;
                default:
                    state = ISI_ERROR;
                    continue; //don't eat
                }
                //break;

            case ISP_ENDTAG_WS:      // DONE
                if( isWS( actChar ) ) break;  // eat all WS
                state = ISP_ENDTAG_X;
                return HtmlTokenContext.WS;


            case ISI_TAG:        // DONE
                if( isName( actChar ) ) break;    // Still in tag identifier, eat next char
                state = ISP_TAG_X;
                return HtmlTokenContext.TAG_OPEN;

            case ISP_TAG_X:     // DONE
                if( isWS( actChar ) ) {
                    state = ISP_TAG_WS;
                    break;
                }
                if( isAZ( actChar ) ) {
                    state = ISI_ARG;
                    break;
                }
                switch( actChar ) {
                case '/':
                    offset++;
                    state = ISI_TAG_SLASH;
                    continue;
                case '>':
                    offset++;
                    state = INIT;
                    return HtmlTokenContext.TAG_CLOSE_SYMBOL;
                case '<':
                    state = INIT;
                    continue;       // don't eat it!!!
                default:
                    state = ISI_ERROR;
                    continue;
                }
                //break;

            case ISP_TAG_WS:        // DONE
                if( isWS( actChar ) ) break;    // eat all WS
                state = ISP_TAG_X;
                return HtmlTokenContext.WS;

            case ISI_TAG_SLASH:
                switch( actChar ) {
                    case '>':
                        offset++;
                        state = INIT;
                        return HtmlTokenContext.TAG_CLOSE_SYMBOL;
                    default:
                        state = ISI_ERROR;
                        continue;
                }
                
            case ISI_ARG:           // DONE
                if( isName( actChar ) ) break; // eat next char
                state = ISP_ARG_X;
                return HtmlTokenContext.ARGUMENT;

            case ISP_ARG_X:
                if( isWS( actChar ) ) {
                    state = ISP_ARG_WS;
                    break;
                }
                if( isAZ( actChar ) ) {
                    state = ISI_ARG;
                    break;
                }
                switch( actChar ) {
                case '/':
                case '>':
                    offset++;
                    state = INIT;
                    return HtmlTokenContext.TAG_OPEN;
                case '<':
                    state = INIT;
                    continue;           // don't eat !!!
                case '=':
                    offset++;
                    state = ISP_EQ;
                    return HtmlTokenContext.OPERATOR;
                default:
                    state = ISI_ERROR;
                    continue;
                }
                //break;
                
            case ISP_ARG_WS:
                if( isWS( actChar ) ) break;    // Eat all WhiteSpace
                state = ISP_ARG_X;
                return HtmlTokenContext.WS;

            case ISP_EQ:
                if( isWS( actChar ) ) {
                    state = ISP_EQ_WS;
                    break;
                }
                switch( actChar ) {
                case '\'':
                    state = ISI_VAL_QUOT;
                    break;
                case '"':
                    state = ISI_VAL_DQUOT;
                    break;
                case '>':               
                    offset++;
                    state = INIT;
                    return HtmlTokenContext.TAG_OPEN;
                default:
                    state = ISI_VAL; //everything else if attribute value
                    break;
                }
                break;

            case ISP_EQ_WS:
                if( isWS( actChar ) ) break;    // Consume all WS
                state = ISP_EQ;
                return HtmlTokenContext.WS;


            case ISI_VAL:
                if( !isWS( actChar ) 
                    && !(actChar == '/' || actChar == '>' || actChar == '<')) break;  // Consume whole value
                state = ISP_TAG_X;
                return HtmlTokenContext.VALUE;

            case ISI_VAL_QUOT:
                switch( actChar ) {
                case '\'':
                    offset++;
                    state = ISP_TAG_X;
                    return HtmlTokenContext.VALUE;
                case '&':
                    if( offset == tokenOffset ) {
                        subState = state;
                        state = ISA_REF;
                        break;
                    } else {
                        return HtmlTokenContext.VALUE;
                    }
                }
                break;  // else simply consume next char of VALUE

            case ISI_VAL_DQUOT:
                switch( actChar ) {
                case '"':
                    offset++;
                    state = ISP_TAG_X;
                    return HtmlTokenContext.VALUE;
                case '&':
                    if( offset == tokenOffset ) {
                        subState = state;
                        state = ISA_REF;
                        break;
                    } else {
                        return HtmlTokenContext.VALUE;
                    }
                }
                break;  // else simply consume next char of VALUE



            case ISA_SGML_ESCAPE:       // DONE
                if( isAZ(actChar) ) {
                    state = ISI_SGML_DECL;
                    break;
                }
                switch( actChar ) {
                case '-':
                    state = ISA_SGML_DASH;
                    break;
                default:
                    state = ISI_TEXT;
                    continue;
                }
                break;

            case ISA_SGML_DASH:       // DONE
                switch( actChar ) {
                case '-':
                    state = ISI_HTML_COMMENT;
                    break;
                default:
                    state = ISI_TEXT;
                    continue;
                }
                break;

            case ISI_HTML_COMMENT:        // DONE
                switch( actChar ) {
                case '-':
                    state = ISA_HTML_COMMENT_DASH;
                    break;
                //create an HTML comment token for each line of the comment - a performance fix for #43532
                case '\n':
                    offset++;
                    //leave the some state - we are still in an HTML comment,
                    //we just need to create a token for each line.
                    return HtmlTokenContext.BLOCK_COMMENT;
                }
                break;

            case ISA_HTML_COMMENT_DASH:
                switch( actChar ) {
                case '-':
                    state = ISI_HTML_COMMENT_WS;
                    break;
                default:
                    state = ISI_HTML_COMMENT;
                    continue;
                }
                break;

            case ISI_HTML_COMMENT_WS:       // DONE
                if( isWS( actChar ) ) break;  // Consume all WS
                switch( actChar ) {
                case '>':
                    offset++;
                    state = INIT;
                    return HtmlTokenContext.BLOCK_COMMENT;
                default:
                    state = ISI_HTML_COMMENT;
                    continue;
                }
                //break;

            case ISI_SGML_DECL:
                switch( actChar ) {
                case '>':
                    offset++;
                    state = INIT;
                    return HtmlTokenContext.DECLARATION;
                case '-':
                    if( offset == tokenOffset ) {
                        state = ISA_SGML_DECL_DASH;
                        break;
                    } else {
                        return HtmlTokenContext.DECLARATION;
                    }
                }
                break;

            case ISA_SGML_DECL_DASH:
                if( actChar == '-' ) {
                    state = ISI_SGML_COMMENT;
                    break;
                } else {
                    state = ISI_SGML_DECL;
                    continue;
                }

            case ISI_SGML_COMMENT:
                switch( actChar ) {
                case '-':
                    state = ISA_SGML_COMMENT_DASH;
                    break;
                }
                break;

            case ISA_SGML_COMMENT_DASH:
                if( actChar == '-' ) {
                    offset++;
                    state = ISI_SGML_DECL;
                    return HtmlTokenContext.SGML_COMMENT;
                } else {
                    state = ISI_SGML_COMMENT;
                    continue;
                }


            case ISA_REF:
                if( isAZ( actChar ) ) {
                    state = ISI_REF_NAME;
                    break;
                }
                if( actChar == '#' ) {
                    state = ISA_REF_HASH;
                    break;
                }
                state = subState;
                continue;

            case ISI_REF_NAME:
                if( isName( actChar ) ) break;
                if( actChar == ';' ) offset++;
                state = subState;
                return HtmlTokenContext.CHARACTER;

            case ISA_REF_HASH:
                if( actChar >= '0' && actChar <= '9' ) {
                    state = ISI_REF_DEC;
                    break;
                }
                if( actChar == 'x' || actChar == 'X' ) {
                    state = ISA_REF_X;
                    break;
                }
                if( isAZ( actChar ) ) {
                    offset++;
                    state = subState;
                    return HtmlTokenContext.ERROR;
                }
                state = subState;
                continue;

            case ISI_REF_DEC:
                if( actChar >= '0' && actChar <= '9' ) break;
                if( actChar == ';' ) offset++;
                state = subState;
                return HtmlTokenContext.CHARACTER;

            case ISA_REF_X:
                if( (actChar >= '0' && actChar <= '9') ||
                        (actChar >= 'a' && actChar <= 'f') ||
                        (actChar >= 'A' && actChar <= 'F')
                  ) {
                    state = ISI_REF_HEX;
                    break;
                }
                state = subState;
                return HtmlTokenContext.ERROR;       // error on previous "&#x" sequence

            case ISI_REF_HEX:
                if( (actChar >= '0' && actChar <= '9') ||
                        (actChar >= 'a' && actChar <= 'f') ||
                        (actChar >= 'A' && actChar <= 'F')
                  ) break;
                if( actChar == ';' ) offset++;
                state = subState;
                return HtmlTokenContext.CHARACTER;
            }


            offset = ++offset;
        } // end of while(offset...)

        /** At this stage there's no more text in the scanned buffer.
        * Scanner first checks whether this is completely the last
        * available buffer.
        */    
        if( lastBuffer ) {
            switch( state ) {
            case INIT:
            case ISI_TEXT:
            case ISA_LT:
            case ISA_SLASH:
            case ISA_SGML_ESCAPE:
            case ISA_SGML_DASH:
            case ISI_TAG_SLASH:
                return HtmlTokenContext.TEXT;

            case ISA_REF:
            case ISA_REF_HASH:
                if( subState == ISI_TEXT ) return HtmlTokenContext.TEXT;
                else return HtmlTokenContext.VALUE;

            case ISI_HTML_COMMENT:
            case ISA_HTML_COMMENT_DASH:
            case ISI_HTML_COMMENT_WS:
                return HtmlTokenContext.BLOCK_COMMENT;

            case ISI_TAG:
                return HtmlTokenContext.TAG_OPEN;
            case ISI_ENDTAG:
                return HtmlTokenContext.TAG_CLOSE;

            case ISI_ARG:
                return HtmlTokenContext.ARGUMENT;

            case ISI_ERROR:
                return HtmlTokenContext.ERROR;

            case ISP_ARG_WS:
            case ISP_TAG_WS:
            case ISP_ENDTAG_WS:
            case ISP_EQ_WS:
                return HtmlTokenContext.WS;

            case ISP_ARG_X:
            case ISP_TAG_X:
            case ISP_ENDTAG_X:
            case ISP_EQ:
                return HtmlTokenContext.WS;

            case ISI_VAL:
            case ISI_VAL_QUOT:
            case ISI_VAL_DQUOT:
                return HtmlTokenContext.VALUE;

            case ISI_SGML_DECL:
            case ISA_SGML_DECL_DASH:
                return HtmlTokenContext.DECLARATION;

            case ISI_SGML_COMMENT:
            case ISA_SGML_COMMENT_DASH:
                return HtmlTokenContext.SGML_COMMENT;

            case ISI_REF_NAME:
            case ISI_REF_DEC:
            case ISA_REF_X:
            case ISI_REF_HEX:
                return HtmlTokenContext.CHARACTER;
            }
        }

        return null;
    }

    public String getStateName(int stateNumber) {
        switch(stateNumber) {
        case INIT:
            return "INIT";          // NOI18N
        case ISI_TEXT:
            return "ISI_TEXT";      // NOI18N
        case ISA_LT:
            return "ISA_LT";        // NOI18N
        case ISA_SLASH:
            return "ISA_SLASH";     // NOI18N
        case ISA_SGML_ESCAPE:
            return "ISA_SGML_ESCAPE"; // NOI18N
        case ISA_SGML_DASH:
            return "ISA_SGML_DASH"; // NOI18N
        case ISI_HTML_COMMENT:
            return "ISI_HTML_COMMENT";// NOI18N
        case ISA_HTML_COMMENT_DASH:
            return "ISA_HTML_COMMENT_DASH";// NOI18N
        case ISI_HTML_COMMENT_WS:
            return "ISI_HTML_COMMENT_WS";// NOI18N
        case ISI_TAG:
            return "ISI_TAG";// NOI18N
        case ISI_ENDTAG:
            return "ISI_ENDTAG";// NOI18N
        case ISI_ARG:
            return "ISI_ARG";// NOI18N
        case ISI_ERROR:
            return "ISI_ERROR";// NOI18N
        case ISP_ARG_WS:
            return "ISP_ARG_WS";// NOI18N
        case ISP_TAG_WS:
            return "ISP_TAG_WS";// NOI18N
        case ISP_ENDTAG_WS:
            return "ISP_ENDTAG_WS";// NOI18N
        case ISP_ARG_X:
            return "ISP_ARG_X";// NOI18N
        case ISP_TAG_X:
            return "ISP_TAG_X";// NOI18N
        case ISP_ENDTAG_X:
            return "ISP_ENDTAG_X";// NOI18N
        case ISP_EQ:
            return "ISP_EQ";// NOI18N
        case ISI_VAL:
            return "ISI_VAL";// NOI18N
        case ISI_VAL_QUOT:
            return "ISI_VAL_QUOT";// NOI18N
        case ISI_VAL_DQUOT:
            return "ISI_VAL_DQUOT";// NOI18N
        case ISI_SGML_DECL:
            return "ISI_SGML_DECL";// NOI18N
        case ISA_SGML_DECL_DASH:
            return "ISA_SGML_DECL_DASH";// NOI18N
        case ISI_SGML_COMMENT:
            return "ISI_SGML_COMMENT";// NOI18N
        case ISA_SGML_COMMENT_DASH:
            return "ISA_SGML_COMMENT_DASH";// NOI18N
        case ISA_REF:
            return "ISA_REF";// NOI18N
        case ISI_REF_NAME:
            return "ISI_REF_NAME";// NOI18N
        case ISA_REF_HASH:
            return "ISA_REF_HASH";// NOI18N
        case ISI_REF_DEC:
            return "ISI_REF_DEC";// NOI18N
        case ISA_REF_X:
            return "ISA_REF_X";// NOI18N
        case ISI_REF_HEX:
            return "ISI_REF_HEX";// NOI18N
        default:
            return super.getStateName(stateNumber);
        }
    }

    /** Load valid mark state into the analyzer. Offsets
    * are already initialized when this method is called. This method
    * must get the state from the mark and set it to the analyzer. Then
    * it must decrease tokenOffset by the preScan stored in the mark state.
    * @param markState mark state to be loaded into syntax. It must be non-null value.
    */
    public void loadState(StateInfo stateInfo) {
        super.loadState( stateInfo );
        subState = ((HtmlStateInfo)stateInfo).getSubState();
    }

    /** Store state of this analyzer into given mark state. */
    public void storeState(StateInfo stateInfo) {
        super.storeState( stateInfo );
        ((HtmlStateInfo)stateInfo).setSubState( subState );
    }

    /** Compare state of this analyzer to given state info */
    public int compareState(StateInfo stateInfo) {
        if( super.compareState( stateInfo ) == DIFFERENT_STATE ) return DIFFERENT_STATE;
        return ( ((HtmlStateInfo)stateInfo).getSubState() == subState) ? EQUAL_STATE : DIFFERENT_STATE;
    }

    /** Create state info appropriate for particular analyzer */
    public StateInfo createStateInfo() {
        return new HtmlStateInfo();
    }


    /** Base implementation of the StateInfo interface */
    public static class HtmlStateInfo extends Syntax.BaseStateInfo {

        /** analyzer subState during parsing character references */
        private int subState;

        public int getSubState() {
            return subState;
        }

        public void setSubState(int subState) {
            this.subState = subState;
        }

        public String toString(Syntax syntax) {
            return super.toString(syntax) + ", subState=" + (syntax == null ? "" : syntax.getStateName(getSubState()));  // NOI18N
        }

    }


}
