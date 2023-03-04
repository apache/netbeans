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

package org.netbeans.lib.xml.lexer;

import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for XML. Based on original XML lexer from xml/editor module.
 *
 * @author Petr Nejedly
 * @author Miloslav Metelka
 * @author Jan Lahoda
 * @author Marek Fukala
 * @author Tomasz Slota
 * @version 1.00
 */

public class XMLLexer implements Lexer<XMLTokenId> {
    private LexerInput input;
    
    private TokenFactory<XMLTokenId> tokenFactory;
    
    public Object state() {
        Integer encoded = (this.state << 1) + (subInternalDTD ? 1 : 0);
        return encoded;
    }
    
    private void loadState(final Object state) {
        if (state == null) {
            subState = INIT;
            this.state = INIT;
            subInternalDTD = false;
        } else {
            int encoded = ((Integer) state).intValue();
            
            this.state    = (encoded & (0xff << 1)) >> 1;
            subInternalDTD = encoded % 2 == 1;
        }
    }
    
    /**
     * Internal state of the lexical analyzer before entering subanalyzer of
     * character references. It is initially set to INIT, but before first
     * usage, this will be overwritten with state, which originated
     * ransition to charref subanalyzer.
     */
    protected int state = INIT;
    
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
    
    /** Initial internal state of the analyzer */
    public static final int INIT = 0;
    
    // Internal states I = in state
    //                 P = expected (char probed but not consumed)
    //                 A = after (char probed and consumed)
    // states, whcih are observable between token lexing are numbered from .
    // states, which are used just within the nextToken() loop are numbered >= 100.
    
    
    // the following states are observable from outside. We need to keep the number of those states < 64
    private static final int ISI_TEXT = 1;    // Plain text between tags
    private static final int ISI_ERROR = 2;   // Syntax error in XML syntax
    private static final int ISP_ENDTAG_X = 3;  // X-switch after ENDTAG's name
    private static final int ISP_TAG_X = 4;   // X-switch after TAG's name
    private static final int ISP_ARG_X = 5;  // X-switch after ARGUMENT's name
    private static final int ISP_EQ = 6;     // X-switch after '=' in TAG's ARGUMENT
    private static final int ISP_EQ_WS = 7;  // In WS after '='
    private static final int ISI_VAL_APOS = 8;   // Single-quoted value - may contain " chars
    private static final int ISI_VAL_QUOT = 9;  // Double-quoted value - may contain ' chars
    private static final int ISI_SGML_DECL = 10;
    private static final int ISA_REF = 11;    // when comes to character reference, e.g. &amp;, after &
    private static final int ISI_PI = 12;  //after <?...
    private static final int ISP_PI_TARGET_WS = 13; //after <?...|
    private static final int ISI_PI_CONTENT = 14;   //in PI content
    private static final int ISP_DECL_CHARS = 15;
    private static final int ISP_DECL_STRING = 16;
    private static final int ISP_PI_CONTENT_QMARK = 17;  //spotet ? in content
    private static final int ISI_CDATA = 18;
    
    // observable just because the EOF condition
    private static final int ISI_XML_COMMENT = 31; // Somewhere after "<!--"

    // the following states should never escape the lexer.
    private static final int ISA_LT = 103;      // After start of tag delimiter - "<"
    private static final int ISA_SLASH = 104;   // After ETAGO - "</"
    private static final int ISI_ENDTAG = 105;  // Inside endtag - "</[a..Z]+"
    private static final int ISP_ENDTAG_WS = 107; // In WS in ENDTAG - "</A_ _>"
    private static final int ISI_TAG = 108;     // Inside tag - "<[a..Z]+"
    private static final int ISP_TAG_WS = 110; // In WS in TAG - "<A_ _...>"
    private static final int ISI_ARG = 111;    // Inside tag's argument - "<A h_r_...>"
    private static final int ISP_ARG_WS = 113; // Inside WS after argument awaiting '='
    private static final int ISA_SGML_ESCAPE = 119;  // After "<!"
    private static final int ISA_SGML_DASH = 120;    // After "<!-"
    private static final int ISA_XML_COMMENT_DASH = 122;  // Dash in comment - maybe end of comment
    private static final int ISI_XML_COMMENT_WS = 123;  // After end of comment, awaiting end of comment declaration
    
    // seems not used at all - an error ?
    private static final int ISA_SGML_DECL_DASH = 125;
    
    //    private static final int ISI_SGML_COMMENT = 26;
    //    private static final int ISA_SGML_COMMENT_DASH = 27;
    private static final int ISI_REF_NAME = 129; // if the reference is symbolic - by predefined name
    private static final int ISA_REF_HASH = 130; // for numeric references - after &#
    private static final int ISI_REF_DEC = 131;  // decimal character reference, e.g. &#345;
    private static final int ISA_REF_X = 132;    //
    private static final int ISI_REF_HEX = 133;  // hexadecimal reference, in &#xa.. of &#X9..
    private static final int ISI_PI_TARGET = 136;  //in <?..|..
    private static final int ISA_PI_CONTENT_QMARK = 139;  //after ? in content

    // CDATA section handler
    private static final int ISA_LTEXBR = 141;
    private static final int ISA_LTEXBRC = 142;
    private static final int ISA_LTEXBRCD = 143;
    private static final int ISA_LTEXBRCDA = 144;
    private static final int ISA_LTEXBRCDAT = 145;
    private static final int ISA_LTEXBRCDATA = 146;
    private static final int ISA_CDATA_BR = 148;
    private static final int ISA_CDATA_BRBR = 149;
    // strings in declaration
    private static final int ISI_DECL_CHARS = 150;
    private static final int ISI_DECL_STRING = 151;

    // internal DTD handling
    private static final int ISA_INIT_BR = 154;
    
    private static final int ISI_ERROR_TAG = 155;
    
    public XMLLexer(LexerRestartInfo<XMLTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        loadState(info.state());
    }
    
    private final boolean isAZ( int ch ) {
        return( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') );
    }
    
    /**
     * Resolves if given char is whitespace in terms of XML4.0 specs
     * According to specs, following characters are treated as whitespace:
     * Space - <CODE>'\u0020'</CODE>, Tab - <CODE>'\u0009'</CODE>,
     * Formfeed - <CODE>'\u000C'</CODE>,Zero-width space - <CODE>'\u200B'</CODE>,
     * Carriage return - <CODE>'\u000D'</CODE> and Line feed - <CODE>'\u000A'</CODE>
     * CR's are included for completenes only, they should never appear in document
     */
    
    private final boolean isWS( int ch ) {
        return Character.isWhitespace(ch);
        //        return ( ch == '\u0020' || ch == '\u0009' || ch == '\u000c'
        //              || ch == '\u200b' || ch == '\n' || ch == '\r' );
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
    
    public Token<XMLTokenId> nextToken() {
        boolean ok = false;
        try {
            subState = state;
            Token<XMLTokenId> tok = nextTokenInternal();
            if (this.state >= 100) {
                throw new IllegalArgumentException("Unexpected state: " + this.state + " at " + this.input);
            }
            ok = true;
            return tok;
        } finally {
            if (!ok) {
                this.state = INIT;
                this.subState = INIT;
            }
        }
    }
    
    public Token<XMLTokenId> nextTokenInternal() {
        int actChar;
        int prevState = 0;
        while(true) {
            actChar = input.read();
            
            if (actChar == LexerInput.EOF){
                
                if (input.readLength() == 0){
                    return null;
                }
                
                input.backup(1);
                break;
            }
            
            switch( state ) {
                case INIT:              //     DONE
                    prevState = 0;
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
                            input.backup(1);
                            if(input.readLength() > 0) {
                                return token(XMLTokenId.TEXT);
                            }
                            break;
                        case '&':
                            if (isInternalDTD() == false) {
                                state = INIT;
                                input.backup(1);
                                if(input.readLength() > 0) {
                                    return token(XMLTokenId.TEXT);
                                }
                            }
                            break;
                        case '%':
                            if (isInternalDTD()) {
                                state = INIT;
                                input.backup(1);
                                return token(XMLTokenId.TEXT);
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
                    state = INIT;
                    prevState = 0;
                    subState = 0;
                    if (input.readLength() > 1) {
                        input.backup(1);
                    }
                    return token(XMLTokenId.ERROR);
                    
                case ISI_ERROR_TAG:
                    state = ISP_TAG_X;
                    if (input.readLength() > 1) {
                        input.backup(1);
                    }
                    return token(XMLTokenId.ERROR);
                
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
                            return token(XMLTokenId.PI_START);
                        default:
                            // note: it would be more correct to raise an error here,
                            // and return TAG PartType=Start, BUT some code already expects
                            // unfinished tags to be reported as TEXT.
                            state = ISI_TEXT;
                            input.backup(1);
                            break;
                            /*
                            return tokenFactory.createToken(
                                    XMLTokenId.TEXT, input.readLength());
                            */
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
                        input.backup(1);
                        return token(XMLTokenId.PI_TARGET);
                    }
                    state = ISI_ERROR;
                    break;
                    
                case ISP_PI_TARGET_WS:
                    if (isWS( actChar)) break;
                    state = ISI_PI_CONTENT;
                    input.backup(1);
                    return token(XMLTokenId.WS);
                    
                case ISI_PI_CONTENT:
                    // < is in theory allowed in PI content, as the delimiter is ?>, nut noone uses it.
                    if (actChar == '<') {
                        state = INIT;
                        input.backup(1);
                    } else {
                        if (actChar != '?') break;  // eat content
                        state = ISP_PI_CONTENT_QMARK;
                        input.backup(1);
                    }
                    if(input.readLength() > 0) {
                        return token(XMLTokenId.PI_CONTENT);  // may do extra break
                    }
                    break;
                    
                case ISP_PI_CONTENT_QMARK:
                    if (actChar != '?') throw new IllegalStateException("'?' expected in ISP_PI_CONTENT_QMARK");
                    state = ISA_PI_CONTENT_QMARK;
                    break;
                    
                case ISA_PI_CONTENT_QMARK:
                    if (actChar != '>') {
                        state = ISI_PI_CONTENT;
                        break;
                    }
                    state = INIT;
                    return token(XMLTokenId.PI_END);
                    
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
                    input.backup(1);
                    return token(XMLTokenId.TAG);
                    
                    
                case ISP_ENDTAG_X:      // DONE
                    if( isWS( actChar ) ) {
                        state = ISP_ENDTAG_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '>':               // Closing of endtag, e.g. </H6 _>_
                            state = INIT;
                            return token(XMLTokenId.TAG);
                        default:
                            state = ISI_ERROR;
                            continue; //don't eat
                    }
                    //break;
                    
                case ISP_ENDTAG_WS:      // DONE
                    if( isWS( actChar ) ) break;  // eat all WS
                    state = ISP_ENDTAG_X;
                    input.backup(1);
                    if (actChar == '>') {  
                        return token(XMLTokenId.WS);
                    }
                    state = ISI_ERROR;
                    break;
                    
                case ISI_TAG:        // DONE
                    if( UnicodeClasses.isXMLNameChar( actChar ) ) break; // Still in tag identifier, eat next char
                    state = ISP_TAG_X;
                    input.backup(1);
                    return token(XMLTokenId.TAG);
                    
                case ISP_TAG_X:     // DONE
                    if( isWS( actChar ) ) {
                        state = ISP_TAG_WS;
                        break;
                    }
                    if( UnicodeClasses.isXMLNameStartChar( actChar ) ) {
                        if (prevState == ISP_TAG_WS) {
                            prevState = 0;
                            input.backup(1);
                            return token(XMLTokenId.WS);
                        }
                        state = ISI_ARG;
                        break;
                    }
                    int c;
                    
                    switch( actChar ) {
                        case '/':
                            c = input.read();
                            if (c == '>') {
                                if (prevState == ISP_TAG_WS) {
                                    prevState = 0;
                                    input.backup(2);
                                    return token(XMLTokenId.WS);
                                }
                                state = INIT;
                                return token(XMLTokenId.TAG);
                            } else {
                                state = ISI_ERROR;
                                input.backup(1);
                                continue;
                            }
                        case '?': //Prolog and PI's now similar to Tag
                            c = input.read();
                            if (c == '>') {
                                if (prevState == ISP_TAG_WS) {
                                    prevState = 0;
                                    input.backup(1);
                                    return token(XMLTokenId.WS);
                                }
                                state = INIT;
                                return token(XMLTokenId.TAG);
                            } else {
                                state = ISI_ERROR;
                                input.backup(1);
                                continue;
                            }
                        case '>':
                            if (prevState == ISP_TAG_WS) {
                                prevState = 0;
                                input.backup(1);
                                return token(XMLTokenId.WS);
                            }
                            state = INIT;
                            return token(XMLTokenId.TAG);
                        case '<':
                            if (prevState == ISP_TAG_WS) {
                                prevState = 0;
                                input.backup(1);
                                state = ISI_ERROR;
                                continue;
                            }
                            // unexpected tag start:
                            state = INIT;
                            input.backup(1);
                            continue;
                        default:
                            if (prevState == ISP_TAG_WS) {
                                prevState = 0;
                                input.backup(1);
                                return token(XMLTokenId.WS);
                            }
                            input.backup(1);
                            state = ISI_ERROR_TAG;
                            continue;
                    }
                    
                    
                case ISP_TAG_WS:        // DONE
                    //input.backup(1);
                    if( isWS( actChar ) ) break;    // eat all WS
                    prevState = state;
                    state = ISP_TAG_X;
                    input.backup(1);
                    break;
                    //return token(XMLTokenId.WS);
                    
                case ISI_ARG:           // DONE
                    if( UnicodeClasses.isXMLNameChar( actChar ) ) break; // eat next char
                    prevState = ISI_ARG;
                    state = ISP_ARG_X;
                    input.backup(1);
                    return token(XMLTokenId.ARGUMENT);
                    
                case ISP_ARG_X:
                    if( isWS( actChar ) ) {
                        state = ISP_ARG_WS;
                        break;
                    }
                    switch( actChar ) {
                        case '=':
                            if (prevState == ISP_ARG_WS) {
                                prevState = 0;
                                input.backup(1);
                                return token(XMLTokenId.WS);
                            }
                            prevState = state;
                            state = ISP_EQ;
                            break;
                        case '<':
                            if (prevState == ISP_ARG_WS) {
                                prevState = 0;
                                state = ISI_ERROR;
                                input.backup(1);
                                continue;
                            } else {
                                input.backup(1);
                                state = INIT;
                                continue;
                            }
                            
                        default:
                            if (input.readLength() > 1) {
                                input.backup(1);
                            }
                            state = ISI_ERROR_TAG;
                            continue;
                    }
                    break;
                    
                case ISP_ARG_WS:
                    if( isWS( actChar ) ) break;    // Eat all WhiteSpace
                    prevState = state;
                    state = ISP_ARG_X;
                    input.backup(1);
                    break;
                    
                case ISP_EQ:
                    if( isWS( actChar ) ) {
                        state = ISP_EQ_WS;
                        input.backup(1);
                        return token(XMLTokenId.OPERATOR);
                    }
                    int newState = 0;
                    switch( actChar ) {
                        case '\'':
                            newState = ISI_VAL_APOS;
                            break;
                        case '"':
                            newState = ISI_VAL_QUOT;
                            break;
                        case '<':
                            if (input.readLength() > 0) {
                                input.backup(1);
                            }
                            state = ISI_ERROR;
                            continue;
                        default:
                            if (prevState == ISP_EQ_WS && input.readLength() > 1) {
                                input.backup(1);
                                // erroneous whitespace
                            }
                            state = ISI_ERROR_TAG;
                            continue;
                            
                    }
                    
                    if (prevState == ISP_EQ_WS) {
                        input.backup(1);
                        return token(XMLTokenId.WS);
                    } else if (prevState == ISP_ARG_X) {
                        input.backup(1);
                        return token(XMLTokenId.OPERATOR);
                    } else {
                        state = newState;
                        break;
                    }
                    
                case ISP_EQ_WS:
                    if( isWS( actChar ) ) break;    // Consume all WS
                    prevState = state;
                    state = ISP_EQ;
                    input.backup(1);
                    break;
                    
                case ISI_VAL_APOS:
                    switch( actChar ) {
                        case '\'':
                            state = ISP_TAG_X;
                            return token(XMLTokenId.VALUE);
                        case '&':
                            if(input.readLength() == 1) {
                                subState = state;
                                state = ISA_REF;
                                break;
                            } else {
                                input.backup(1);
                                return token(XMLTokenId.VALUE);
                            }
                        case '<':
                            // error / unterminated tag, but the next token should be
                            state = INIT;
                            input.backup(1);
                            if(input.readLength() > 0) {
                                return token(XMLTokenId.VALUE);
                            }
                            break;
                    }
                    break;  // else simply consume next char of VALUE
                    
                case ISI_VAL_QUOT:
                    switch( actChar ) {
                        case '"':
                            state = ISP_TAG_X;
                            return token(XMLTokenId.VALUE);
                        case '&':
                            if(input.readLength() == 1) {
                                subState = state;
                                state = ISA_REF;
                                break;
                            } else {
                                input.backup(1);
                                return token(XMLTokenId.VALUE);
                            }
                        case '<':
                            // error / unterminated tag, but the next token should be
                            state = INIT;
                            input.backup(1);
                            if(input.readLength() > 0) {
                                return token(XMLTokenId.VALUE);
                            }
                            break;
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
                    } else {
                        state = ISI_CDATA;
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
                        return token(XMLTokenId.CDATA_SECTION);
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
//                            //create an XML comment token for each line of the comment - a workaround fix for performance bug #39446
//                            //this also causes a SyntaxtElement to be created for each line of the comment - see XMLSyntaxSupport.createElement:277
//                            //PENDING - this code can be removed after editor solve it somehow in their code
//                        case '\n':
//                            //leave the some state - we are still in an XML comment,
//                            //we just need to create a token for each line.
//                            return token(XMLTokenId.BLOCK_COMMENT);
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
                            state = INIT;
                            return token(XMLTokenId.BLOCK_COMMENT);
                        default:
                            state = ISI_ERROR;
                            input.backup(1);
                            return token(XMLTokenId.BLOCK_COMMENT);
                    }
                    
                case ISP_DECL_STRING:
                    if (actChar != '"') throw new IllegalStateException("Unexpected " + actChar);
                    state = ISI_DECL_STRING;
                    break;
                    
                case ISI_DECL_STRING:
                    if ( actChar == '"') {
                        state = ISI_SGML_DECL;
                        return token(XMLTokenId.VALUE);
                    }
                    break;
                    
                case ISP_DECL_CHARS:
                    if (actChar != '\'') throw new IllegalStateException("Unexpected " + actChar);
                    state = ISI_DECL_CHARS;
                    break;
                    
                case ISI_DECL_CHARS:
                    if ( actChar == '\'') {
                        state = ISI_SGML_DECL;
                        return token(XMLTokenId.VALUE);
                    }
                    break;
                    
                case ISI_SGML_DECL:
                    switch( actChar ) {
                        case '"':
                            state = ISP_DECL_STRING;
                            input.backup(1);
                            if (input.readLength() > 0)
                                return token(XMLTokenId.DECLARATION);
                            break;
                        case '\'':
                            state = ISP_DECL_CHARS;
                            input.backup(1);
                            if (input.readLength() > 0)
                                return token(XMLTokenId.DECLARATION);
                            break;
                        case '[':
                            state = INIT;
                            enterInternalDTD();
                            return token(XMLTokenId.DECLARATION);
                        case '>':
                            state = INIT;
                            return token(XMLTokenId.DECLARATION);
                    }
                    break;
                    
                case ISA_INIT_BR:
                    if (isWS(actChar)) break;
                    if (actChar == '>') {
                        state = INIT;
                        leaveInternalDTD();
                        return token(XMLTokenId.DECLARATION);
                    } else {
                        state = INIT;
                        input.backup(1);
                        if (input.readLength() > 0)
                            return token(XMLTokenId.ERROR);
                    }
                    break;
                    
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
                    // get back to &, proclaim as character, although not according to spec.
                    input.backup(1);
                    state = subState;
                    continue;
                    
                case ISI_REF_NAME:
                    if( UnicodeClasses.isXMLNameChar( actChar ) ) break;
                    if( actChar != ';' ) input.backup(1);
                    state = subState;
                    return token(XMLTokenId.CHARACTER);
                    
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
                        state = subState;
                        return token(XMLTokenId.ERROR);
                    }
                    state = subState;
                    continue;
                    
                case ISI_REF_DEC:
                    if( actChar >= '0' && actChar <= '9' ) break;
                    if( actChar != ';' ) input.backup(1);
                    state = subState;
                    return token(XMLTokenId.CHARACTER);
                    
                case ISA_REF_X:
                    if (isHex(actChar)) {
                        state = ISI_REF_HEX;
                        break;
                    }
                    state = subState;
                    input.backup(1);
                    return token(XMLTokenId.ERROR);       // error on previous "&#x" sequence
                    
                case ISI_REF_HEX:
                    if (isHex(actChar)) break;
                    if (actChar != ';' ) input.backup(1);
                    state = subState;
                    return token(XMLTokenId.CHARACTER);
            }
        } // end of while(offset...)
        
        switch( state ) {
            case INIT:
            case ISI_TEXT:
            case ISA_LT:
            case ISA_SLASH:
            case ISA_SGML_ESCAPE:
            case ISA_SGML_DASH:
                state = ISI_TEXT;
                return token(XMLTokenId.TEXT);
                
            case ISA_REF:
            case ISA_REF_HASH:
                state = subState;
                if( subState == ISI_TEXT ) {
                    return token(XMLTokenId.TEXT);
                } else {
                    return token(XMLTokenId.VALUE);
                }
                
            case ISI_XML_COMMENT:
            case ISA_XML_COMMENT_DASH:
            case ISI_XML_COMMENT_WS:
                state = ISI_XML_COMMENT;
                return token(XMLTokenId.BLOCK_COMMENT);
                
            case ISI_TAG:
            case ISI_ENDTAG:
                state = ISP_TAG_X;
                return token(XMLTokenId.TAG);
                
            case ISI_ARG:
                state = ISP_ARG_X;
                return token(XMLTokenId.ARGUMENT);
                
            case ISI_ERROR:
                return token(XMLTokenId.ERROR);
                
            case ISP_ARG_WS:
            case ISP_TAG_WS:
            case ISP_ENDTAG_WS:
            case ISP_EQ_WS:
                state = ISP_TAG_X;
                return token(XMLTokenId.WS);
                
            case ISP_ARG_X:
            case ISP_TAG_X:
            case ISP_ENDTAG_X:
            case ISP_EQ:
                state = ISP_TAG_X;
                return token(XMLTokenId.WS);
                
            case ISI_VAL_APOS:
            case ISI_VAL_QUOT:
            case ISI_DECL_CHARS:
            case ISI_DECL_STRING:
                state = ISI_VAL_APOS;
                return token(XMLTokenId.VALUE);
                
            case ISI_SGML_DECL:
            case ISA_SGML_DECL_DASH:
            case ISP_DECL_STRING:
            case ISP_DECL_CHARS:
                state = ISI_SGML_DECL;
                return token(XMLTokenId.DECLARATION);
                
            case ISI_REF_NAME:
            case ISI_REF_DEC:
            case ISA_REF_X:
            case ISI_REF_HEX:
                state = subState;
                return token(XMLTokenId.CHARACTER);
                
            case ISI_PI:
                return token(XMLTokenId.PI_START);
            case ISI_PI_TARGET:
                state = ISI_PI;
                return token(XMLTokenId.PI_TARGET);
            case ISP_PI_TARGET_WS:
                state = ISI_PI;
                return token(XMLTokenId.WS);
            case ISI_PI_CONTENT:
                state = ISI_PI;
                return token(XMLTokenId.PI_CONTENT);
            case ISA_PI_CONTENT_QMARK:
            case ISP_PI_CONTENT_QMARK:
                // we are at end of the last buffer and expect that next char will be '>'
                state = ISI_PI;
                return token(XMLTokenId.PI_END);
                
            case ISA_LTEXBR:
            case ISA_LTEXBRC:
            case ISA_LTEXBRCD:
            case ISA_LTEXBRCDA:
            case ISA_LTEXBRCDAT:
            case ISA_LTEXBRCDATA:
                state = ISI_TEXT;
                return token(XMLTokenId.TEXT);
                
            case ISI_CDATA:
            case ISA_CDATA_BR:
            case ISA_CDATA_BRBR:
                state = ISI_CDATA;
                return token(XMLTokenId.CDATA_SECTION);
                
            case ISA_INIT_BR:
                state = ISI_TEXT;
                return token(XMLTokenId.TEXT);
                
            default:
                throw new IllegalStateException("Last buffer does not handle state " + state + "!");    //NOI18N
        }
        
    }
    
    private Token<XMLTokenId> token(XMLTokenId id) {
//        System.out.print("--- token(" + id + "; '" + input.readText().toString() + "')");
//        if(input.readLength() == 0) {
//            System.out.println("XMLLexer error - zero length token!");
//        }
        Token<XMLTokenId> t = tokenFactory.createToken(id);
//        System.out.println(t.id() + "; " + t.length());
        return t;
    }
    
    private boolean isHex(int ch) {
        return (ch >= '0' && ch <= '9') || isAF(ch);
    }
    
    private boolean isAF(int ch) {
        return( (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F') );
    }

    public void release() {
    }

}
