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
package org.netbeans.modules.xml.text.syntax;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;

/**
 * Gracefull lexical analyzer for XML source files. It rather returns <code>ERROR</code>
 * token than terminates scanning. In such case is stays in current state.
 *
 * @author Petr Nejedly
 * @author Miloslav Metelka
 * @author Sandeep Singh Randhawa
 * @author Petr Kuzel
 *
 * @version 1.10 XML spec aware
 */

public class XMLDefaultSyntax extends Syntax {
    
    /** 
     * Internal state of the lexical analyzer before entering subanalyzer of
     * character references. It is initially set to INIT, but before first 
     * usage, this will be overwritten with state, which originated
     * ransition to charref subanalyzer.
     */
    protected int subState = INIT;
    
    /**
     * Identifies internal DTD layer. Most of functionality is same
     * as at document layer, however there are minor exceptions.
     * @see isInternalDTD checks in code
     */
    protected boolean subInternalDTD = false;
    
    // Internal states I = in state
    //                 P = expected (char probed but not consumed)
    //                 A = after (char probed and consumed)
    private static final int ISI_TEXT = 1;    // Plain text between tags
    private static final int ISI_ERROR = 2;   // Syntax error in XML syntax
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
    private static final int ISI_VAL_APOS = 17;   // Single-quoted value - may contain " chars
    private static final int ISI_VAL_QUOT = 18;  // Double-quoted value - may contain ' chars
    private static final int ISA_SGML_ESCAPE = 19;  // After "<!"
    private static final int ISA_SGML_DASH = 20;    // After "<!-"
    private static final int ISI_XML_COMMENT = 21; // Somewhere after "<!--"
    private static final int ISA_XML_COMMENT_DASH = 22;  // Dash in comment - maybe end of comment
    private static final int ISI_XML_COMMENT_WS = 23;  // After end of comment, awaiting end of comment declaration
    private static final int ISI_SGML_DECL = 24;
    private static final int ISA_SGML_DECL_DASH = 25;
    //    private static final int ISI_SGML_COMMENT = 26;
    //    private static final int ISA_SGML_COMMENT_DASH = 27;
    private static final int ISA_REF = 28;    // when comes to character reference, e.g. &amp;, after &
    private static final int ISI_REF_NAME = 29; // if the reference is symbolic - by predefined name
    private static final int ISA_REF_HASH = 30; // for numeric references - after &#
    private static final int ISI_REF_DEC = 31;  // decimal character reference, e.g. &#345;
    private static final int ISA_REF_X = 32;    //
    private static final int ISI_REF_HEX = 33;  // hexadecimal reference, in &#xa.. of &#X9..
    
    
    private static final int ISI_PI = 35;  //after <?...
    private static final int ISI_PI_TARGET = 36;  //in <?..|..
    private static final int ISP_PI_TARGET_WS = 37; //after <?...|
    private static final int ISI_PI_CONTENT = 38;   //in PI content
    private static final int ISA_PI_CONTENT_QMARK = 39;  //after ? in content
    private static final int ISP_PI_CONTENT_QMARK = 40;  //spotet ? in content

    // CDATA section handler
    private static final int ISA_LTEXBR = 41;
    private static final int ISA_LTEXBRC = 42;
    private static final int ISA_LTEXBRCD = 43;
    private static final int ISA_LTEXBRCDA = 44;
    private static final int ISA_LTEXBRCDAT = 45;
    private static final int ISA_LTEXBRCDATA = 46;
    private static final int ISI_CDATA = 47;
    private static final int ISA_CDATA_BR = 48;
    private static final int ISA_CDATA_BRBR = 49;

    // strings in declaration
    private static final int ISI_DECL_CHARS = 50;
    private static final int ISI_DECL_STRING = 51;
    private static final int ISP_DECL_CHARS = 52;
    private static final int ISP_DECL_STRING = 53;

    // internal DTD handling
    private static final int ISA_INIT_BR = 54;
    
    public XMLDefaultSyntax() {
        tokenContextPath = XMLDefaultTokenContext.contextPath;
    }
    
    protected TokenID parseToken() {
        
        char actChar;
        while(offset < stopOffset) {
            actChar = buffer[offset];
            switch( state ) {
                case INIT:              //     DONE
                    switch( actChar ) {
                        case '<':
                            state = ISA_LT;
                            break;
                        case '&':
                            if (isInternalDTD() == false) {
                                state = ISA_REF;
                                subState = ISI_TEXT;                                
                            } else {
                                state = ISI_TEXT;
                            }
                            break;
                        case '%':
                            if (isInternalDTD()) {
                                state = ISA_REF;
                                subState = INIT;                                
                            } else {
                                state = ISI_TEXT;
                            }
                            break;
                        case ']':
                            if (isInternalDTD()) {
                                state = ISA_INIT_BR;                                
                            } else {
                                state = ISI_TEXT;
                            }
                            break;
                        default:
                            state = ISI_TEXT;
                            break;
                    }
                    break;
                    
                case ISI_TEXT:        // DONE
                    switch( actChar ) {
                        case '<':
                            state = INIT;
                            return XMLDefaultTokenContext.TEXT;                            
                        case '&':
                            if (isInternalDTD() == false) {
                                state = INIT;
                                return XMLDefaultTokenContext.TEXT;                                                            
                            }
                            break;
                        case '%':
                            if (isInternalDTD()) {
                                state = INIT;
                                return XMLDefaultTokenContext.TEXT;                                
                            }
                            break;
                        case ']':
                            if (isInternalDTD()) {
                                state = ISA_INIT_BR;
                            }
                            break;
                    }
                    break;
                    
                case ISI_ERROR:      // DONE
                    offset++;
                    state = INIT;
                    return XMLDefaultTokenContext.ERROR;
                    
                case ISA_LT:         // DONE
                    
                    if( UnicodeClasses.isXMLNameStartChar( actChar ) && isInternalDTD() == false) {
                        state = ISI_TAG;
                        break;
                    }
                    switch( actChar ) {
                        case '/':               // ETAGO - </
                            state = ISA_SLASH;
                            break;
                        case '!':
                            state = ISA_SGML_ESCAPE;
                            break;
                        case '?':
                            state = ISI_PI;
                            offset++;
                            return XMLDefaultTokenContext.PI_START;
                        default:
                            state = ISI_TEXT;  //RELAXED to allow editing in the  middle of document
                            continue;             // don't eat the char, maybe its '&'
                    }
                    break;

                case ISI_PI:
                    if ( UnicodeClasses.isXMLNameStartChar( actChar )) {
                        state = ISI_PI_TARGET;
                        break;
                    }
                    state = ISI_ERROR;
                    break;
                    
                case ISI_PI_TARGET:
                    if ( UnicodeClasses.isXMLNameChar( actChar )) break;
                    if (isWS( actChar )) {
                        state = ISP_PI_TARGET_WS;
                        return XMLDefaultTokenContext.PI_TARGET;
                    }
                    state = ISI_ERROR;
                    break;
                    
                case ISP_PI_TARGET_WS:
                    if (isWS( actChar)) break;
                    state = ISI_PI_CONTENT;
                    return XMLDefaultTokenContext.WS;

                case ISI_PI_CONTENT:
                    if (actChar != '?') break;  // eat content
                    state = ISP_PI_CONTENT_QMARK;
                    return XMLDefaultTokenContext.PI_CONTENT;  // may do extra break
                    
                case ISP_PI_CONTENT_QMARK:
                    if (actChar != '?') throw new IllegalStateException ("'?' expected in ISP_PI_CONTENT_QMARK");
                    state = ISA_PI_CONTENT_QMARK;
                    break;

                case ISA_PI_CONTENT_QMARK:
                    if (actChar != '>') {
                        state = ISI_PI_CONTENT;
                        break;
                    }
                    state = INIT;
                    offset++;
                    return XMLDefaultTokenContext.PI_END;                    
                    
                case ISA_SLASH:        // DONE
                    
                    if( UnicodeClasses.isXMLNameStartChar( actChar )){
                        state = ISI_ENDTAG;
                        break;
                    }
                    switch( actChar ) {
                        case ' ':
                            state = ISI_TEXT;
                            continue;
                        case '\n':
                            state = ISI_TEXT;
                            continue;
                        case '\r':
                            state = ISI_TEXT;
                            continue;
                        default:                // Part of text, e.g. </3, </'\n', RELAXED
                            state = ISI_TEXT;
                            continue;             // don'e eat the char
                    }
                    //break;
                    
                case ISI_ENDTAG:        // DONE
                    if( UnicodeClasses.isXMLNameChar( actChar )){
                        break;    // Still in endtag identifier, eat next char
                    }
                    
                    state = ISP_ENDTAG_X;
                    return XMLDefaultTokenContext.TAG;
                    
                    
                case ISP_ENDTAG_X:      // DONE
                    if( isWS( actChar ) ) {
                        state = ISP_ENDTAG_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '>':               // Closing of endtag, e.g. </H6 _>_
                            offset++;
                            state = INIT;
                            return XMLDefaultTokenContext.TAG;
                        default:
                            state = ISI_ERROR;
                            continue; //don't eat
                    }
                    //break;
                    
                case ISP_ENDTAG_WS:      // DONE
                    if( isWS( actChar ) ) break;  // eat all WS
                    state = ISP_ENDTAG_X;
                    return XMLDefaultTokenContext.WS;
                    
                    
                case ISI_TAG:        // DONE
                    if( UnicodeClasses.isXMLNameChar( actChar ) ) break; // Still in tag identifier, eat next char
                    state = ISP_TAG_X;
                    return XMLDefaultTokenContext.TAG;
                    
                case ISP_TAG_X:     // DONE
                    if( isWS( actChar ) ) {
                        state = ISP_TAG_WS;
                        break;
                    }
                    if( UnicodeClasses.isXMLNameStartChar( actChar ) ) {
                        state = ISI_ARG;
                        break;
                    }
                    switch( actChar ) {
                        case '/':
                            offset++;
                            continue;
                        case '?': //Prolog and PI's now similar to Tag
                            offset++;
                            continue;
                        case '>':
                            offset++;
                            state = INIT;
                            return XMLDefaultTokenContext.TAG;
                        default:
                            state = ISI_ERROR;
                            continue;
                    }
                    //break;
                    
                    
                case ISP_TAG_WS:        // DONE
                    if( isWS( actChar ) ) break;    // eat all WS
                    state = ISP_TAG_X;
                    return XMLDefaultTokenContext.WS;
                    
                case ISI_ARG:           // DONE
                    if( UnicodeClasses.isXMLNameChar( actChar ) ) break; // eat next char
                    state = ISP_ARG_X;
                    return XMLDefaultTokenContext.ARGUMENT;
                    
                case ISP_ARG_X:
                    if( isWS( actChar ) ) {
                        state = ISP_ARG_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '=':
                            offset++;
                            state = ISP_EQ;
                            return XMLDefaultTokenContext.OPERATOR;
                        default:
                            state = ISI_ERROR;
                            continue;
                    }
                    //break;
                    
                case ISP_ARG_WS:
                    if( isWS( actChar ) ) break;    // Eat all WhiteSpace
                    state = ISP_ARG_X;
                    return XMLDefaultTokenContext.WS;
                    
                case ISP_EQ:
                    if( isWS( actChar ) ) {
                        state = ISP_EQ_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '\'':
                            state = ISI_VAL_APOS;
                            break;
                        case '"':
                            state = ISI_VAL_QUOT;
                            break;
                        default:
                            state = ISI_ERROR;
                            continue;
                    }
                    break;
                    
                case ISP_EQ_WS:
                    if( isWS( actChar ) ) break;    // Consume all WS
                    state = ISP_EQ;
                    return XMLDefaultTokenContext.WS;
                                        
                case ISI_VAL_APOS:
                    switch( actChar ) {
                        case '\'':
                            offset++;
                            state = ISP_TAG_X;
                            return XMLDefaultTokenContext.VALUE;
                        case '&':
                            if( offset == tokenOffset ) {
                                subState = state;
                                state = ISA_REF;
                                break;
                            } else {
                                return XMLDefaultTokenContext.VALUE;
                            }
                    }
                    break;  // else simply consume next char of VALUE
                    
                case ISI_VAL_QUOT:
                    switch( actChar ) {
                        case '"':
                            offset++;
                            state = ISP_TAG_X;
                            return XMLDefaultTokenContext.VALUE;
                        case '&':
                            if( offset == tokenOffset ) {
                                subState = state;
                                state = ISA_REF;
                                break;
                            } else {
                                return XMLDefaultTokenContext.VALUE;
                            }
                    }
                    break;  // else simply consume next char of VALUE
                    
                    
                case ISA_SGML_ESCAPE:       // DONE
                    if (actChar == '[') {
                        state = ISA_LTEXBR;
                        break;
                    } else if( isAZ(actChar) ) {
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
                    
                case ISA_LTEXBR:
                    if (actChar == 'C') {
                        state = ISA_LTEXBRC;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }

                case ISA_LTEXBRC:
                    if (actChar == 'D') {
                        state = ISA_LTEXBRCD;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }

                case ISA_LTEXBRCD:
                    if (actChar == 'A') {
                        state = ISA_LTEXBRCDA;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }

                case ISA_LTEXBRCDA:
                    if (actChar == 'T') {
                        state = ISA_LTEXBRCDAT;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }

                case ISA_LTEXBRCDAT:
                    if (actChar == 'A') {
                        state = ISA_LTEXBRCDATA;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }

                case ISA_LTEXBRCDATA:
                    if (actChar == '[') {
                        state = ISI_CDATA;
                        break;
                    } else {
                        state = ISI_TEXT;
                        continue;
                    }

                case ISI_CDATA:
                    if (actChar == ']') {
                        state = ISA_CDATA_BR;
                        break;
                    }

                case ISA_CDATA_BR:
                    if (actChar == ']') {
                        state = ISA_CDATA_BRBR;
                        break;
                    } else {
                        state = ISI_CDATA;
                        break;                        
                    }

                case ISA_CDATA_BRBR:
                    if (actChar == '>') {
                        state = ISI_TEXT;           //It s allowed only in content
                        offset++;
                        return XMLTokenIDs.CDATA_SECTION;
                    } else if (actChar == ']') {
                        // stay in the same state
                        break;
                    } else {
                        state = ISI_CDATA;
                        break;
                    }
                    
                    
                case ISA_SGML_DASH:       // DONE
                    switch( actChar ) {
                        case '-':
                            state = ISI_XML_COMMENT;
                            break;
                        default:
                            state=ISI_ERROR;
                            continue;
                    }
                    break;
                    
                case ISI_XML_COMMENT:        // DONE
                    switch( actChar ) {
                        case '-':
                            state = ISA_XML_COMMENT_DASH;
                            break;
                        //create an XML comment token for each line of the comment - a workaround fix for performance bug #39446
                        //this also causes a SyntaxtElement to be created for each line of the comment - see XMLSyntaxSupport.createElement:277
                        //PENDING - this code can be removed after editor solve it somehow in their code
                        case '\n':
                            offset++;
                            //leave the some state - we are still in an XML comment,
                            //we just need to create a token for each line.
                            return XMLDefaultTokenContext.BLOCK_COMMENT;
                    }
                    break;
                    
                case ISA_XML_COMMENT_DASH:
                    switch( actChar ) {
                        case '-':
                            state = ISI_XML_COMMENT_WS;
                            break;
                        default:
                            state = ISI_XML_COMMENT;
                            continue;
                    }
                    break;
                    
                case ISI_XML_COMMENT_WS:       // DONE
                    if( isWS( actChar ) ) break;  // Consume all WS
                    switch( actChar ) {
                        case '>':
                            offset++;
                            state = INIT;
                            return XMLDefaultTokenContext.BLOCK_COMMENT;
                        default:
                            state = ISI_ERROR;
                            return XMLDefaultTokenContext.BLOCK_COMMENT;
                    }
                    
                case ISP_DECL_STRING:
                    if (actChar != '"') throw new IllegalStateException("Unexpected " + actChar);
                    state = ISI_DECL_STRING;
                    break;
                    
                case ISI_DECL_STRING:
                    if ( actChar == '"') {
                            state = ISI_SGML_DECL;
                            offset++;
                            return XMLDefaultTokenContext.VALUE;
                    }
                    break;

                case ISP_DECL_CHARS:
                    if (actChar != '\'') throw new IllegalStateException("Unexpected " + actChar);
                    state = ISI_DECL_CHARS;
                    break;
                    
                case ISI_DECL_CHARS:
                    if ( actChar == '\'') {
                            state = ISI_SGML_DECL;
                            offset++;
                            return XMLDefaultTokenContext.VALUE;
                    }
                    break;
                    
                case ISI_SGML_DECL:
                    switch( actChar ) {
                        case '"':
                            state = ISP_DECL_STRING;
                            return XMLDefaultTokenContext.DECLARATION;
                        case '\'':
                            state = ISP_DECL_CHARS;
                            return XMLDefaultTokenContext.DECLARATION;
                        case '[':
                            offset++;
                            state = INIT;
                            enterInternalDTD();
                            return XMLDefaultTokenContext.DECLARATION;
                        case '>':
                            offset++;
                            state = INIT;
                            return XMLDefaultTokenContext.DECLARATION;
                    }
                    break;

                case ISA_INIT_BR:
                    if (isWS(actChar)) break;
                    if (actChar == '>') {
                        offset++;
                        state = INIT;
                        leaveInternalDTD();
                        return XMLDefaultTokenContext.DECLARATION;
                    } else {
                        state = INIT;
                        return XMLDefaultTokenContext.ERROR;
                    }
                    
                case ISA_SGML_DECL_DASH:
                    if( actChar == '-' ) {
                        state = ISI_ERROR;
                        break;
                    } else {
                        if(isWS(actChar)){
                            state = ISI_ERROR;
                            continue;
                        } else {
                            state = ISI_SGML_DECL;
                            continue;
                        }
                    }
                    
                case ISA_REF:
                    if( UnicodeClasses.isXMLNameStartChar( actChar ) ) {
                        state = ISI_REF_NAME;
                        break;
                    }
                    if( actChar == '#') {
                        state = ISA_REF_HASH;
                        break;
                    }
                    state = subState;
                    continue;
                    
                case ISI_REF_NAME:
                    if( UnicodeClasses.isXMLNameChar( actChar ) ) break;
                    if( actChar == ';' ) offset++;
                    state = subState;
                    return XMLDefaultTokenContext.CHARACTER;
                    
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
                        return XMLDefaultTokenContext.ERROR;
                    }
                    state = subState;
                    continue;
                    
                case ISI_REF_DEC:
                    if( actChar >= '0' && actChar <= '9' ) break;
                    if( actChar == ';' ) offset++;
                    state = subState;
                    return XMLDefaultTokenContext.CHARACTER;
                    
                case ISA_REF_X:
                    if (isHex(actChar)) {
                        state = ISI_REF_HEX;
                        break;
                    }
                    state = subState;
                    return XMLDefaultTokenContext.ERROR;       // error on previous "&#x" sequence
                    
                case ISI_REF_HEX:
                    if (isHex(actChar)) break;
                    if (actChar == ';' ) offset++;
                    state = subState;
                    return XMLDefaultTokenContext.CHARACTER;
            }
            
            
            offset++;
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
                    return XMLDefaultTokenContext.TEXT;
                    
                case ISA_REF:
                case ISA_REF_HASH:
                    if( subState == ISI_TEXT ) return XMLDefaultTokenContext.TEXT;
                    else return XMLDefaultTokenContext.VALUE;
                    
                case ISI_XML_COMMENT:
                case ISA_XML_COMMENT_DASH:
                case ISI_XML_COMMENT_WS:
                    return XMLDefaultTokenContext.BLOCK_COMMENT;
                    
                case ISI_TAG:
                case ISI_ENDTAG:
                    return XMLDefaultTokenContext.TAG;
                    
                case ISI_ARG:
                    return XMLDefaultTokenContext.ARGUMENT;
                    
                case ISI_ERROR:
                    return XMLDefaultTokenContext.ERROR;
                    
                case ISP_ARG_WS:
                case ISP_TAG_WS:
                case ISP_ENDTAG_WS:
                case ISP_EQ_WS:
                    return XMLDefaultTokenContext.WS;
                    
                case ISP_ARG_X:
                case ISP_TAG_X:
                case ISP_ENDTAG_X:
                case ISP_EQ:
                    return XMLDefaultTokenContext.WS;
                    
                case ISI_VAL_APOS:
                case ISI_VAL_QUOT:
                case ISI_DECL_CHARS:
                case ISI_DECL_STRING:
                    return XMLDefaultTokenContext.VALUE;
                    
                case ISI_SGML_DECL:
                case ISA_SGML_DECL_DASH:
                case ISP_DECL_STRING:
                case ISP_DECL_CHARS:
                    return XMLDefaultTokenContext.DECLARATION;
                    
                case ISI_REF_NAME:
                case ISI_REF_DEC:
                case ISA_REF_X:
                case ISI_REF_HEX:
                    return XMLDefaultTokenContext.CHARACTER;
                    
                case ISI_PI:
                    return XMLDefaultTokenContext.PI_START;
                case ISI_PI_TARGET:
                    return XMLDefaultTokenContext.PI_TARGET;
                case ISP_PI_TARGET_WS:
                    return XMLDefaultTokenContext.WS;
                case ISI_PI_CONTENT:
                    return XMLDefaultTokenContext.PI_CONTENT;
                case ISA_PI_CONTENT_QMARK:                    
                case ISP_PI_CONTENT_QMARK:
                    // we are at end of the last buffer and expect that next char will be '>'
                    return XMLDefaultTokenContext.PI_END;  

                case ISA_LTEXBR:
                case ISA_LTEXBRC:
                case ISA_LTEXBRCD:
                case ISA_LTEXBRCDA:
                case ISA_LTEXBRCDAT:
                case ISA_LTEXBRCDATA:                    
                    return XMLDefaultTokenContext.TEXT;

                case ISI_CDATA:                    
                case ISA_CDATA_BR:
                case ISA_CDATA_BRBR:                    
                    return XMLTokenIDs.CDATA_SECTION;

                case ISA_INIT_BR:
                    return XMLDefaultTokenContext.TEXT;
                    
                default:
                    throw new IllegalStateException("Last buffer does not handle state " + state + "!");    //NOI18N
            }
        }
        
        return null;  // ask for next buffer
        
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
            case ISI_XML_COMMENT:
                return "ISI_XML_COMMENT";// NOI18N
            case ISA_XML_COMMENT_DASH:
                return "ISA_XML_COMMENT_DASH";// NOI18N
            case ISI_XML_COMMENT_WS:
                return "ISI_XML_COMMENT_WS";// NOI18N
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
            case ISI_VAL_APOS:
                return "ISI_VAL_APOS";// NOI18N
            case ISI_VAL_QUOT:
                return "ISI_VAL_QUOT";// NOI18N
            case ISI_SGML_DECL:
                return "ISI_SGML_DECL";// NOI18N
            case ISA_SGML_DECL_DASH:
                return "ISA_SGML_DECL_DASH";// NOI18N
                //            case ISI_SGML_COMMENT:
                //                return "ISI_SGML_COMMENT";// NOI18N
                //            case ISA_SGML_COMMENT_DASH:
                //                return "ISA_SGML_COMMENT_DASH";// NOI18N
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
            case ISI_PI:
                return "ISI_PI"; // NOI18N                
            case ISI_PI_TARGET:
                return "ISI_PI_TARGET";// NOI18N                
            case ISP_PI_TARGET_WS:
                return "ISP_PI_TARGET_WS";// NOI18N                
            case ISI_PI_CONTENT:
                return "ISI_PI_CONTENT";// NOI18N                
            case ISA_PI_CONTENT_QMARK:
                return "ISA_PI_CONTENT_QMARK";// NOI18N                
            case ISP_PI_CONTENT_QMARK:
                return "ISP_PI_CONTENT_QMARK";// NOI18N                
                
            case ISI_DECL_CHARS:
                return "ISI_DECL_CHARS";
            case ISI_DECL_STRING:
                return "ISI_DECL_STRING";
            case ISP_DECL_CHARS:
                return "ISP_DECL_CHARS";
            case ISP_DECL_STRING:
                return "ISP_DECL_STRING";
            case ISA_INIT_BR:
                return "ISA_INIT_BR";                
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
        subState = ((XMLStateInfo)stateInfo).getSubState();
        subInternalDTD = ((XMLStateInfo)stateInfo).isInternalDTD();
    }
    
    /** Store state of this analyzer into given mark state. */
    public void storeState(StateInfo stateInfo) {
        super.storeState( stateInfo );
        ((XMLStateInfo)stateInfo).setSubState( subState );
        ((XMLStateInfo)stateInfo).setInternalDTD( subInternalDTD );
    }
    
    /** Compare state of this analyzer to given state info */
    public int compareState(StateInfo stateInfo) {
        if( super.compareState( stateInfo ) == DIFFERENT_STATE ) return DIFFERENT_STATE;
        return ( ((XMLStateInfo)stateInfo).getSubState() == subState
            && ((XMLStateInfo)stateInfo).isInternalDTD() == subInternalDTD) 
            ? EQUAL_STATE : DIFFERENT_STATE;
    }
    
    /** Create state info appropriate for particular analyzer */
    public StateInfo createStateInfo() {
        return new XMLStateInfo();
    }

    // ~~~~~~~~~~~~~~~~~~~~~ utility methods ~~~~~~~~~~~~~~~~~~~~~~~~
    
    private boolean isAZ( char ch ) {
        return( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') );
    }

    private boolean isHex( char ch) {
        return (ch >= '0' && ch <= '9') || isAF(ch);
    }

    private boolean isAF( char ch ) {
        return( (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F') );
    }
    
    private boolean isName( char ch ) {
        return( (ch >= 'a' && ch <= 'z') ||
        (ch >= 'A' && ch <= 'Z') ||
        (ch >= '0' && ch <= '9') ||
        ch == '-' || ch == '_' || ch == '.' || ch == ':' );
        
    }

    private void enterInternalDTD() {
        subInternalDTD = true;
    }

    private void leaveInternalDTD() {
        subInternalDTD = false;
    }
    
    private boolean isInternalDTD() {
        return subInternalDTD;
    }
    
    /**
     * Resolves if given char is whitespace in terms of XML 1.0 specs
     * According to specs, following characters are treated as whitespace:
     * Space - <CODE>'\u0020'</CODE>, Tab - <CODE>'\u0009'</CODE>,
     * Formfeed - <CODE>'\u000C'</CODE>,Zero-width space - <CODE>'\u200B'</CODE>,
     * Carriage return - <CODE>'\u000D'</CODE> and Line feed - <CODE>'\u000A'</CODE>
     * CR's are included for completenes only, they should never appear in document
     */
    
    private boolean isWS( char ch ) {
        return ( ch == '\u0020' || ch == '\u0009' || ch == '\u000c'
        /*|| ch == '\u200b'*/ || ch == '\n' || ch == '\r' );
    }
    
    
    /** Base implementation of the StateInfo interface */
    public static class XMLStateInfo extends Syntax.BaseStateInfo {
        
        /** analyzer subState during parsing character references */
        private int subState;
        private boolean subInternalDTD;
        
        public int getSubState() {
            return subState;
        }
        
        public void setSubState(int subState) {
            this.subState = subState;
        }
        
        public boolean isInternalDTD() {
            return subInternalDTD;
        }
        
        public void setInternalDTD(boolean val) {
            subInternalDTD = val;
        }
        
        public String toString(Syntax syntax) {
            return super.toString(syntax) 
                + ", subState=" + syntax.getStateName(getSubState())            // NOI18N
                + ", inDTD=" + subState;                                        // NOI18N
        }
        
    }
}

