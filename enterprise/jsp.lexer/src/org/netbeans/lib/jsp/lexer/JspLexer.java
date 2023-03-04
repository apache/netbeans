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

package org.netbeans.lib.jsp.lexer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.spi.jsp.lexer.JspParseData;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Syntax class for JSP tags.
 *
 * @author Petr Jiricka
 * @author Marek Fukala
 *
 * @version 1.00
 */

public class JspLexer implements Lexer<JspTokenId> {
    
    private static final Logger LOGGER = Logger.getLogger(JspLexer.class.getName());
    private static final boolean LOG = Boolean.getBoolean("j2ee_lexer_debug"); //NOI18N
    
    
    private static final int EOF = LexerInput.EOF;
    
    private static final String JSP_STANDART_TAG_PREFIX = "jsp:";
    
    private final LexerInput input;
    
    private final InputAttributes inputAttributes;
    private final JspParseData jspParseData;
    
    private final TokenFactory<JspTokenId> tokenFactory;
    
    @Override
    public Object state() {
        return new JspState(lexerState, lexerStateBeforeEL, lexerStateBeforeScriptlet, lexerStateJspScriptlet, lexerStateCurlyNestedLevel);
    }
    
    //main internal lexer state
    private int lexerState = INIT;
    
    //secondary internal state for EL expressions in JSP
    //is it used to eliminate a number of lexer states when EL is found -
    //we have 8 states just in attribute value so I would have to copy the EL
    //recognition code eight-times.
    private int lexerStateBeforeEL = INIT;

    //the same for jsp scriptlets
    private int lexerStateBeforeScriptlet = INIT;
    
    //internal state signalling whether the lexer is in <jsp:scriptlet> tag
    private int lexerStateJspScriptlet = INIT;

    //internal state signalling deep of the curly bracket nested level
    private int lexerStateCurlyNestedLevel = 0;
    
    // Internal analyzer states
    // general
    private static final int INIT                =  0;  // initial lexer state = content language
    private static final int ISI_ERROR           =  1; // when the fragment does not start with <
    private static final int ISA_LT              =  2; // after '<' char
    // tags and directives
    private static final int ISI_TAGNAME         =  3; // inside JSP tag name
    private static final int ISI_DIRNAME         =  4; // inside JSP directive name
    private static final int ISP_TAG             =  5; // after JSP tag name
    private static final int ISP_DIR             =  6; // after JSP directive name
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
    private static final int ISA_LT_PC           = 24; // after '<%' - comment or directive or scriptlet
    private static final int ISI_JSP_COMMENT     = 25; // after <%-
    
    private static final int ISI_JSP_COMMENT_M   = 26; // inside JSP comment after -
    private static final int ISI_JSP_COMMENT_MM  = 27; // inside JSP comment after --
    private static final int ISI_JSP_COMMENT_MMP = 28; // inside JSP comment after --%
    // end state
    //    static final int ISA_END_JSP                 = 29; // JSP fragment has finished and control
    // should be returned to master syntax
    // more errors
    private static final int ISI_TAG_ERROR       = 30; // error in tag, can be cleared by > or \n
    private static final int ISI_DIR_ERROR       = 31; // error in directive, can be cleared by %>, \n, \t or space
    private static final int ISI_DIR_ERROR_P     = 32; // error in directive after %, can be cleared by > or \n
    
    private static final int ISA_LT_PC_AT        = 33; // after '<%@' (directive)
    private static final int ISA_LT_SLASH        = 34; // after '</' sequence
    private static final int ISA_LT_PC_DASH      = 35; // after <%- ;not comment yet
    
    private static final int ISI_SCRIPTLET       = 36; // inside java scriptlet/declaration/expression
    private static final int ISP_SCRIPTLET_PC    = 37; // just after % in scriptlet

    //expression language
    
    //EL in content language
    private static final int ISA_EL_DELIM_DOLLAR        = 38; //after $ or # in content language
    private static final int ISA_EL_DELIM_HASH        = 42; //after $ or # in content language
    private static final int ISI_EL              = 39; //expression language in content (after ${ or #{ )
    
    private static final int ISI_EL_SINGLE_QUOTE = 50; //inside single quoted string
    private static final int ISA_EL_SINGLE_QUOTE_ESCAPE = 51; //inside single quoted string after backslash
    private static final int ISI_EL_DOUBLE_QUOTE = 52; //inside double quoted string
    private static final int ISA_EL_DOUBLE_QUOTE_ESCAPE = 53; //inside double quoted string after backslash
    
    private static final int ISA_BS              = 40; //after backslash in text - needed to disable EL by scaping # or $
    private static final int ISP_GT_SCRIPTLET    = 41; //before closing < symbol in jsp:expression/s/d tag

    //BE AWARE WHEN ADDING MORE STATES - each lexer state are encoded in 6 bits, so max value is 64!!!

    //scriptlet substate states
    //in standart syntax jsp
    private static final int JAVA_SCRITPLET = 1; //java scriptlet
    private static final int JAVA_DECLARATION = 2; //java declaration
    private static final int JAVA_EXPRESSION = 3; //java expression
    //in xml syntax jsp (jsp document)
    private static final int JAVA_SCRITPLET_DOCUMENT = 4; //java scriptlet in JSP document
    private static final int JAVA_DECLARATION_DOCUMENT = 5; //java declaration in JSP document
    private static final int JAVA_EXPRESSION_DOCUMENT = 6; //java expression in JSP document
    
   
    public JspLexer(LexerRestartInfo<JspTokenId> info) {
        this.input = info.input();
        this.inputAttributes = info.inputAttributes();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            lexerState = INIT;
            lexerStateBeforeEL = INIT;
            lexerStateBeforeScriptlet = INIT;
            lexerStateJspScriptlet = INIT;
            lexerStateCurlyNestedLevel = 0;
        } else {
            JspState state = (JspState) info.state();
            lexerStateJspScriptlet = state.getLexerStateJspScriptlet();
            lexerStateBeforeScriptlet = state.getLexerStateBeforeScriptlet();
            lexerStateBeforeEL = state.getLexerStateBeforeEL();
            lexerState = state.getLexerState();
            lexerStateCurlyNestedLevel = state.getLexerStateCurlyNestedLevel();
        }
        if(inputAttributes != null) {
            jspParseData = (JspParseData)inputAttributes.getValue(LanguagePath.get(JspTokenId.language()), JspParseData.class);
        } else {
            jspParseData = null;
        }
    }
    
    public boolean isIdentifierPart(char character) {
        return Character.isJavaIdentifierPart(character);
    }
    
    /** Determines whether a given string is a JSP tag. */
    private boolean isJspTag(CharSequence tagName) {
        if (startsWith(tagName, JSP_STANDART_TAG_PREFIX)) { // NOI18N
            return true;
        }

        //TODO handle custom tags from JSP parser here
        if (jspParseData != null) {
            //Issue #149994 workaround
            //All prefix tags (<xxx:yyy ... />) will be lexed as jsp tags until the jsp parser finishes. So this will fix the scanning
            //problem and the only sideeffect for xhtml users using namespaces is that if thay open their xhtml file they non-jsp tags
            //with namespaces will look like jsp tags for a while until the jsp parser finishes and tells the lexer which are real jsp
            //tags and which not.
            if (!jspParseData.isInitialized()) {
                return contains(tagName, ':'); //NOI18N
            } else {
                int colonIndex = indexOf(tagName, ':');//NOI18N
                if (colonIndex != -1) {
                    CharSequence prefix = tagName.subSequence(0, colonIndex);
                    return jspParseData.isTagLibRegistered(prefix.toString());
                }
            }
        } else {
            return contains(tagName, ':'); //NOI18N
        }

        return false;
    }

    private boolean contains(CharSequence text, char ch) {
        for(int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ch) {
                return true;
            }
        }
        return false;
    }
    
    private boolean startsWith(CharSequence text, CharSequence prefix) {
        if(text.length() < prefix.length()) {
            return false;
        }
       
        for(int i = 0; i < prefix.length(); i++) {
            if(text.charAt(i) != prefix.charAt(i)) {
                return false;
            }
        }
        
        return true;
    }
    
    private int indexOf(CharSequence text, char ch) {
        for(int i = 0; i < text.length(); i++) {
            if(text.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }
    
    private boolean isELIgnored() {
        return jspParseData == null ? false : jspParseData.isELIgnored();
    }
    
    private boolean isXMLSyntax() {
        return jspParseData == null ? false: jspParseData.isXMLSyntax();
    }
    
    private CharSequence getPossibleTagName() {
        int actChar;
        int prev_read = input.readLength(); //remember the size of the read sequence
        int read = 0;
        while(true) {
            actChar = input.read();
            read++;
            if(!(Character.isLetter(actChar) ||
                    Character.isDigit(actChar) ||
                    (actChar == '_') ||
                    (actChar == '-') ||
                    (actChar == ':') ||
                    (actChar == '.') ||
                    (actChar == '/')) ||
                    (actChar == EOF)) { // EOL or not alpha
                //end of tagname
                CharSequence tagName = input.readText().subSequence(prev_read, prev_read + read - 1);
                input.backup(read); //put the lookahead text back to the buffer
                return tagName;
            }
        }
    }
    
    /** Looks ahead into the character buffer and checks if a jsp tag name follows. */
    private boolean followsJspTag() {
        return isJspTag(getPossibleTagName());
    }

    private boolean followsScriptletExpressionDelimiter(int actChar) {
        if(actChar == '<') {
            int next = input.read();
            if(next == '%') {
                next = input.read();
                if(next == '=') {
                    //yes, it follows
                    return true;
                } else {
                    input.backup(2); //backup %+next
                }
            } else {
                input.backup(1); //backup next
            }
        }
        return false;
    }
    
    public Token<JspTokenId> nextToken() {
        int actChar;
        while (true) {
            actChar = input.read();
            
            if (actChar == EOF) {
                if(input.readLengthEOF() == 1) {
                    return null; //just EOL is read
                } else {
                    //there is something else in the buffer except EOL
                    //we will return last token now
                    input.backup(1); //backup the EOL, we will return null in next nextToken() call
                    break;
                }
            }
            
            switch (lexerState) {
                case INIT:
                    switch (actChar) {
                        //                        case '\n':
                        //                            return token(JspTokenId.EOL);
                        case '<':
                            lexerState = ISA_LT;
                            break;
                            //                        default:
                            //                            state = ISI_ERROR;
                            //                            break;
                        case '\\':
                            lexerState = ISA_BS;
                            break;
                        case '$':
                            lexerStateBeforeEL = lexerState; //remember main state
                            lexerState = ISA_EL_DELIM_DOLLAR;
                            break;
                        case '#': //maybe expression language
                            lexerStateBeforeEL = lexerState; //remember main state
                            lexerState = ISA_EL_DELIM_HASH;
                            break;
                    }
                    break;
                    
                case ISA_BS:
                    if(actChar != '\\') {
                        lexerState = INIT; //prevent scaped EL in text being recognized
                    }
                    break;
                    
                case ISA_EL_DELIM_DOLLAR:
                    if(isELIgnored()) {
                        //reset to previous state - do not recognize EL
                        lexerState = lexerStateBeforeEL;
                        lexerStateBeforeEL = INIT;
                        break;
                    }
                case ISA_EL_DELIM_HASH:
                    switch (actChar) {
                        case '{':
                            if (input.readLength() > 2) {
                                //we have something read except the '${' or '#{' => it's content language
                                input.backup(2); //backup the '$/#{'
                                lexerState = lexerStateBeforeEL; //we will read the '$/#{' again
                                lexerStateBeforeEL = INIT;
                                return token(JspTokenId.TEXT); //return the content language token
                            }
                            lexerState = ISI_EL;
                            break;
                        default:
                            input.backup(1); //put the read char back
                            lexerState = lexerStateBeforeEL;
                            lexerStateBeforeEL = INIT;
                    }
                    break;
                    
                case ISI_EL:
                    switch (actChar) {
                        case '\'':
                            lexerState = ISI_EL_SINGLE_QUOTE;
                            break;
                        case '"':
                            lexerState = ISI_EL_DOUBLE_QUOTE;
                            break;
                        case '{':
                            lexerStateCurlyNestedLevel++;
                            break;
                        case '}':
                            if (lexerStateCurlyNestedLevel > 0) {
                                lexerStateCurlyNestedLevel--;
                            } else {
                                //return EL token
                                lexerState = lexerStateBeforeEL;
                                lexerStateBeforeEL = INIT;
                                return token(JspTokenId.EL);
                            }
                    }
                    break;

                case ISI_EL_SINGLE_QUOTE:
                    switch (actChar) {
                        case '\\':
                            lexerState = ISA_EL_SINGLE_QUOTE_ESCAPE;
                            break;
                        case '\'':
                            lexerState = ISI_EL;
                            break;
                    }
                    break;

                case ISI_EL_DOUBLE_QUOTE:
                    switch (actChar) {
                        case '\\':
                            if (lexerStateBeforeEL != 14) {
                                lexerState = ISA_EL_DOUBLE_QUOTE_ESCAPE;
                            }
                            break;
                        case '"':
                            lexerState = ISI_EL;
                            break;
                    }
                    break;

                case ISA_EL_DOUBLE_QUOTE_ESCAPE:
                    //just skip back qouted string
                    lexerState = ISI_EL_DOUBLE_QUOTE;
                    break;

                case ISA_EL_SINGLE_QUOTE_ESCAPE:
                    //just skip back qouted string
                    lexerState = ISI_EL_SINGLE_QUOTE;
                    break;
                    
                case ISA_LT:
                    if (Character.isLetter(actChar) ||
                            (actChar == '_')
                            ) { // possible tag begining
                        input.backup(1); //backup the read letter
                        CharSequence tagName = getPossibleTagName();
                        if(isJspTag(tagName)) { //test if a jsp tag follows
                            if(input.readLength() > 1) {
                                //we have something read except the '<' => it's content language
                                input.backup(1); //backup the '<'
                                lexerState = INIT; //we will read the '<' again
                                return token(JspTokenId.TEXT); //return the content language token
                            }
                            //possibly switch to scriptlet when <jsp:scriptlet> found
                            
                            if(CharSequenceUtilities.equals("jsp:scriptlet",tagName)) { //NOI18N
                                lexerStateJspScriptlet = JAVA_SCRITPLET_DOCUMENT;
                            } else if(CharSequenceUtilities.equals("jsp:declaration", tagName)) { //NOI18N
                                lexerStateJspScriptlet = JAVA_DECLARATION_DOCUMENT;
                            } else if(CharSequenceUtilities.equals("jsp:expression", tagName)) { //NOI18N
                                lexerStateJspScriptlet = JAVA_EXPRESSION_DOCUMENT;
                            }
                            
                            lexerState = ISI_TAGNAME;
                            return token(JspTokenId.SYMBOL); //return the read '<' symbol first
                        } else {
                            //just a content language
                            lexerState = INIT;
                            break;
                        }
                    }
                    
                    switch (actChar) {
                        case '/':
                            lexerState = ISA_LT_SLASH;
                            break;
                            //                        case '\n':
                            //                            state = ISI_TAG_ERROR;
                            //                            input.backup(1);
                            //                            return token(JspTokenId.SYMBOL);
                        case '%':
                            lexerState = ISA_LT_PC;
                            break;
                        default:
                            input.backup(1);
                            lexerState = INIT; //just content
                            //                            state = ISI_TAG_ERROR;
                            //                            break;
                    }
                    break;
                    
                case ISA_LT_SLASH:
                    if (Character.isLetter(actChar) ||
                            (actChar == '_')) {
                        //possible end tag beginning
                        input.backup(1); //backup the first letter
                        if(followsJspTag()) {
                            if(input.readLength() > 2) {
                                //we have something read except the '</' symbol
                                input.backup(2);
                                lexerState = INIT;
                                return token(JspTokenId.TEXT);
                            } else {
                                lexerState = ISI_ENDTAG;
                                return token(JspTokenId.SYMBOL);  //return the read '</' symbol first
                            }
                            //break;
                        } else {
                            //just a content language
                            lexerState = INIT;
                            break;
                        }
                    }
                    
                    //not jsp end tag -> just content -> switch to init state
                    lexerState = INIT;
                    break;
                    
                case ISI_TAGNAME:
                case ISI_DIRNAME:
                    
                    if (!(Character.isLetter(actChar) ||
                            Character.isDigit(actChar) ||
                            (actChar == '_') ||
                            (actChar == '-') ||
                            (actChar == ':') ||
                            (actChar == '.'))) { // not alpha
                        switch(actChar) {
                            case '<':
                                lexerState = INIT;
                                input.backup(1);
                                break;
                            case '/':
                                input.backup(1);
                                lexerState = ((lexerState == ISI_TAGNAME) ? ISP_TAG : ISP_DIR);
                                break;
                            case '>':
                                input.backup(1); //backup the '<' char
                                if(lexerStateJspScriptlet != INIT) {
                                    //switch to java scriptlet
                                    lexerState = ISP_GT_SCRIPTLET;
                                } else {
                                    lexerState = ((lexerState == ISI_TAGNAME) ? ISP_TAG : ISP_DIR);
                                }
                                break;
                            case ' ':
                                input.backup(1);
                                lexerState = ((lexerState == ISI_TAGNAME) ? ISP_TAG : ISP_DIR);
                                break;
                            case '\n':
                                lexerState = ISP_TAG;
                                input.backup(1); //backup the eof
                                return token(JspTokenId.TAG);
                            default:
                                lexerState = ((lexerState == ISI_TAGNAME) ? ISP_TAG : ISP_DIR);
                        }
                        return token(JspTokenId.TAG);
                    }
                    break;

                //internal state for > symbol after jsp:expression/scriptlet/declaration tag
                case ISP_GT_SCRIPTLET:
                    assert actChar == '>';
                    lexerState = ISI_SCRIPTLET;
                    return token(JspTokenId.SYMBOL);

                case ISP_TAG:
                case ISP_DIR:
                    if (Character.isLetter(actChar) ||
                            (actChar == '_')
                            ) {
                        lexerState = ((lexerState == ISP_TAG) ? ISI_TAG_ATTR : ISI_DIR_ATTR);
                        break;
                    }
                    switch (actChar) {
                        case '\n':
                            //                            if (input.readLength() == 1) { // no char
                            return token(JspTokenId.EOL);
                            //                            } else { // return string first
                            //                                input.backup(1);
                            //                                return decide_jsp_tag_token();
                            //                            }
                        case '>': // for tags
                            if (lexerState == ISP_TAG) {
                                //                                if (input.readLength() == 1) {  // no char
                                //                                    state = ISA_END_JSP;
                                lexerState = INIT;
                                return token(JspTokenId.SYMBOL);
                                //                                } else { // return string first
                                //                                    input.backup(1);
                                //                                    return decide_jsp_tag_token();
                                //                                }
                            } else { // directive
                                lexerState = ISI_DIR_ERROR;
                                break;
                            }
                        case '/': // for tags
                            if (lexerState == ISP_TAG) {
                                //                                if (input.readLength() == 1) {  // no char
                                lexerState = ISA_ENDSLASH;
                                break;
                                //                                } else { // return string first
                                //                                    input.backup(1);
                                //                                    return decide_jsp_tag_token();
                                //                                }
                            } else { // directive
                                lexerState = ISI_DIR_ERROR;
                                break;
                            }
                        case '%': // for directives
                            if (lexerState == ISP_DIR) {
                                //                                if (input.readLength() == 1) {  // no char
                                lexerState = ISA_ENDPC;
                                break;
                                //                                } else { // return string first
                                //                                    input.backup(1);
                                //                                    return decide_jsp_tag_token();
                                //                                }
                            } else { // tag
                                lexerState = ISI_TAG_ERROR;
                                break;
                            }
                        case '=':
                            lexerState = ((lexerState == ISP_TAG) ? ISP_TAG_EQ : ISP_DIR_EQ);
                            return token(JspTokenId.SYMBOL);
                        case ' ':
                        case '\t':
                            lexerState = ((lexerState == ISP_TAG) ? ISI_TAG_I_WS : ISI_DIR_I_WS);
                            break;
                        case '<':
                            // assume that this is the start of the next tag
                            //we shouldn't have anything else than then the < char in buffer
                            assert input.readLength() == 1 : "There is something more than '<' char in the read text: '" + input.readText() + "'"; //NOI18N
                            input.backup(1);
                            lexerState = INIT;
                        default: //numbers or illegal symbols
                            lexerState = ((lexerState == ISP_TAG) ? ISI_TAG_ERROR : ISI_DIR_ERROR);
                            break;
                    }
                    break;
                    
                case ISI_TAG_I_WS:
                case ISI_DIR_I_WS:
                    switch (actChar) {
                        case ' ':
                        case '\t':
                            break;
                        case '<': //start of the next tag
                            //                            state = ISA_END_JSP;
                            lexerState = INIT;
                            input.backup(1);
                            return token(JspTokenId.TAG);
                        default:
                            lexerState = ((lexerState == ISI_TAG_I_WS) ? ISP_TAG : ISP_DIR);
                            input.backup(1);
                            return token(JspTokenId.WHITESPACE);
                    }
                    break;
                    
                case ISI_ENDTAG:
                    if (!(Character.isLetter(actChar) ||
                            Character.isDigit(actChar) ||
                            (actChar == '_') ||
                            (actChar == '.') ||
                            (actChar == '-') ||
                            (actChar == ':'))
                            ) { // not alpha
                        lexerState = ISP_TAG;
                        input.backup(1);
                        return token(JspTokenId.ENDTAG);
                    }
                    break;
                    
                case ISI_TAG_ATTR:
                case ISI_DIR_ATTR:
                    if (!(Character.isLetter(actChar) ||
                            Character.isDigit(actChar) ||
                            (actChar == '_') ||
                            (actChar == ':') ||
                            (actChar == '.') ||
                            (actChar == '-'))
                            ) { // not alpha or '-' (http-equiv)
                        lexerState = ((lexerState == ISI_TAG_ATTR) ? ISP_TAG : ISP_DIR);
                        input.backup(1);
                        return token(JspTokenId.ATTRIBUTE);
                    }
                    break;
                    
                case ISP_TAG_EQ:
                case ISP_DIR_EQ:
                    switch (actChar) {
                        case '\n':
                            //                            if (input.readLength() == 1) { // no char
                            return token(JspTokenId.EOL);
                            //                            } else { // return string first
                            //                                input.backup(1);
                            //                                return token(JspTokenId.ATTR_VALUE);
                            //                            }
                        case '"':
                            lexerState = ((lexerState == ISP_TAG_EQ) ? ISI_TAG_STRING : ISI_DIR_STRING);
                            break;
                        case '\'':
                            lexerState = ((lexerState == ISP_TAG_EQ) ? ISI_TAG_STRING2 : ISI_DIR_STRING2);
                            break;
                        case ' ':
                        case '\t':
                            // don't change the state
                            break;
                        default:
                            //invalid value - lets backup it and swith to tag content
                            lexerState = ((lexerState == ISP_TAG_EQ) ? ISP_TAG : ISP_DIR);
                            input.backup(input.readLength());
                            break;
                    }
                    break;
                    
                case ISI_TAG_STRING:
                case ISI_DIR_STRING:
                case ISI_TAG_STRING2:
                case ISI_DIR_STRING2:
                    if ((actChar == '"') && ((lexerState == ISI_TAG_STRING) || (lexerState == ISI_DIR_STRING))) {
                        lexerState = ((lexerState == ISI_TAG_STRING) ? ISP_TAG : ISP_DIR);
                        return token(JspTokenId.ATTR_VALUE);
                    }
                    
                    if ((actChar == '\'') && ((lexerState == ISI_TAG_STRING2) || (lexerState == ISI_DIR_STRING2))) {
                        lexerState = ((lexerState == ISI_TAG_STRING2) ? ISP_TAG : ISP_DIR);
                        return token(JspTokenId.ATTR_VALUE);
                    }

                    if(followsScriptletExpressionDelimiter(actChar)) {
                        if(input.readLength() == 3) {
                            //just the delimiter in buffer, we may make the token

                            //remember where we jumped into the scriptlet,
                            //we'll return to the state once we reach end of the
                            //scriptlet
                            lexerStateBeforeScriptlet = lexerState;
                            lexerStateJspScriptlet = JAVA_EXPRESSION;
                            lexerState = ISI_SCRIPTLET;
                            
                            return token(JspTokenId.SYMBOL2);
                            
                        } else {
                            //first tokenize the text before
                            input.backup(3); //backup <%=
                            //keep the state
                            return token(JspTokenId.ATTR_VALUE);
                        }
                    }

                    switch (actChar) {
                        case '\\':
                            switch (lexerState) {
                                case ISI_TAG_STRING:
                                    lexerState = ISI_TAG_STRING_B;
                                    break;
                                case ISI_DIR_STRING:
                                    lexerState = ISI_DIR_STRING_B;
                                    break;
                                case ISI_TAG_STRING2:
                                    lexerState = ISI_TAG_STRING2_B;
                                    break;
                                case ISI_DIR_STRING2:
                                    lexerState = ISI_DIR_STRING2_B;
                                    break;
                            }
                            break;
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else { // return string first
                                input.backup(1);
                                return token(JspTokenId.ATTR_VALUE);
                            }
                        case '$':
                            if(input.readLength() > 1) {
                                //return part of the attribute value before EL
                                input.backup(1); //backup $ or #
                                return token(JspTokenId.ATTR_VALUE);
                            } else {
                                lexerStateBeforeEL = lexerState; //remember main state
                                lexerState = ISA_EL_DELIM_DOLLAR;
                            }
                            break;
                        case '#':
                            if(input.readLength() > 1) {
                                //return part of the attribute value before EL
                                input.backup(1); //backup $ or #
                                return token(JspTokenId.ATTR_VALUE);
                            } else {
                                lexerStateBeforeEL = lexerState; //remember main state
                                lexerState = ISA_EL_DELIM_HASH;
                            }
                            break;
                            
                        default:
                            break;//stay in ISI_TAG_STRING/2;
                            
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
                        case '$':
                        case '#':
                            break;
                        default:
                            input.backup(1);
                            break;
                    }
                    switch (lexerState) {
                        case ISI_TAG_STRING_B:
                            lexerState = ISI_TAG_STRING;
                            break;
                        case ISI_DIR_STRING_B:
                            lexerState = ISI_DIR_STRING;
                            break;
                        case ISI_TAG_STRING2_B:
                            lexerState = ISI_TAG_STRING2;
                            break;
                        case ISI_DIR_STRING2_B:
                            lexerState = ISI_DIR_STRING2;
                            break;
                    }
                    break;
                    
                case ISA_ENDSLASH:
                    switch (actChar) {
                        case '>':
                            //                            state = ISA_END_JSP;
                            lexerState = INIT;
                            return token(JspTokenId.SYMBOL);
                        case '\n':
                            lexerState = ISI_TAG_ERROR;
                            input.backup(1);
                            return token(JspTokenId.SYMBOL);
                        default:
                            lexerState = ISP_TAG;
                            input.backup(1);
                            return token(JspTokenId.SYMBOL);
                    }
                    //break; not reached
                    
                case ISA_ENDPC:
                    switch (actChar) {
                        case '>':
                            //                            state = ISA_END_JSP;
                            lexerState = INIT;
                            return token(JspTokenId.SYMBOL);
                        case '\n':
                            lexerState = ISI_DIR_ERROR;
                            input.backup(1);
                            return token(JspTokenId.SYMBOL);
                        default:
                            lexerState = ISP_DIR;
                            input.backup(1);
                            return token(JspTokenId.SYMBOL);
                    }
                    //break; not reached
                    
                case ISA_LT_PC:
                    switch (actChar) {
                        case '@':
                            if(input.readLength() == 3) {
                                // just <%@ read
                                lexerState = ISA_LT_PC_AT;
                                return token(JspTokenId.SYMBOL);
                            } else {
                                //jsp symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%@
                                lexerState = INIT;
                                return token(JspTokenId.TEXT); //return CL token
                            }
                        case '-': //may be JSP comment
                            lexerState = ISA_LT_PC_DASH;
                            break;
                        case '!': // java declaration
                        case '=': // java expression
                            if(input.readLength() == 3) {
                                // just <%! or <%= read
                                lexerStateJspScriptlet = actChar == '!' ? JAVA_DECLARATION : JAVA_EXPRESSION;
                                lexerState = ISI_SCRIPTLET;
                                return token(JspTokenId.SYMBOL2);
                            } else {
                                //jsp symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%! or <%=
                                lexerState = INIT;
                                return token(JspTokenId.TEXT); //return CL token
                            }
                        default:  //java scriptlet delimiter '<%'
                            if(input.readLength() == 3) {
                                // just <% + something != [-,!,=,@] read
                                lexerStateJspScriptlet = JAVA_SCRITPLET;
                                lexerState = ISI_SCRIPTLET;
                                input.backup(1); //backup the third character, it is a part of the java scriptlet
                                return token(JspTokenId.SYMBOL2);
                            } else {
                                //jsp symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%@
                                lexerState = INIT;
                                return token(JspTokenId.TEXT); //return CL token
                            }
                    }
                    break;
                    
                case ISI_SCRIPTLET:
                    switch(actChar) {
                        case '%':
                                lexerState = ISP_SCRIPTLET_PC;
                                break;
                        case '<':
                            //may be end of scriptlet section in JSP document
                            CharSequence tagName = getPossibleTagName();
                            if(CharSequenceUtilities.equals("/jsp:scriptlet", tagName) || //NOI18N
                                    CharSequenceUtilities.equals("/jsp:declaration", tagName) || //NOI18N
                                    CharSequenceUtilities.equals("/jsp:expression", tagName)) { //NOI18N
                                if(input.readLength() == 1) {
                                    //just the '<' symbol read
                                    input.backup(1);
                                    lexerState = INIT;
                                } else {
                                    //return the scriptlet content
                                    input.backup(1); // backup '<' we will read it again
                                    int lxs = lexerStateJspScriptlet;
                                    lexerStateJspScriptlet = INIT;
                                    return scriptletToken(JspTokenId.SCRIPTLET, lxs);
                                }
                            }
                    }
                    break;
                    
                case ISP_SCRIPTLET_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                lexerState = lexerStateBeforeScriptlet;
                                lexerStateBeforeScriptlet = INIT;
                                lexerStateJspScriptlet = INIT;
                                return token(JspTokenId.SYMBOL2);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                lexerState = ISI_SCRIPTLET;
                                int lxs = lexerStateJspScriptlet;
                                lexerStateJspScriptlet = INIT;
                                return scriptletToken(JspTokenId.SCRIPTLET, lxs);
                            }
                        default:
                            lexerState = ISI_SCRIPTLET;
                            break;
                    }
                    break;
                    
                case ISA_LT_PC_DASH:
                    switch(actChar) {
                        case '-':
                            if(input.readLength() == 4) {
                                //just the '<%--' symbol read
                                lexerState = ISI_JSP_COMMENT;
                            } else {
                                //return the scriptlet content
                                input.backup(4); // backup '<%--', we will read it again
                                lexerState = INIT;
                                return token(JspTokenId.TEXT);
                            }
                            break;
                        default:
                            //                            state = ISA_END_JSP;
                            lexerState = INIT; //XXX how to handle content language?
                            return token(JspTokenId.TEXT); //marek: should I token here????
                    }
                    
                    // JSP states
                case ISI_JSP_COMMENT:
                    switch (actChar) {
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else { // return block comment first
                                input.backup(1);
                                return token(JspTokenId.COMMENT);
                            }
                        case '-':
                            lexerState = ISI_JSP_COMMENT_M;
                            break;
                    }
                    break;
                    
                case ISI_JSP_COMMENT_M:
                    switch (actChar) {
                        case '\n':
                            lexerState = ISI_JSP_COMMENT;
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else { // return block comment first
                                input.backup(1);
                                return token(JspTokenId.COMMENT);
                            }
                        case '-':
                            lexerState = ISI_JSP_COMMENT_MM;
                            break;
                        default:
                            lexerState = ISI_JSP_COMMENT;
                            break;
                    }
                    break;
                    
                case ISI_JSP_COMMENT_MM:
                    switch (actChar) {
                        case '\n':
                            lexerState = ISI_JSP_COMMENT;
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else { // return block comment first
                                input.backup(1);
                                return token(JspTokenId.COMMENT);
                            }
                        case '%':
                            lexerState = ISI_JSP_COMMENT_MMP;
                            break;
                        case '-':
                            lexerState = ISI_JSP_COMMENT_MM;
                            break;
                        default:
                            lexerState = ISI_JSP_COMMENT;
                            break;
                    }
                    break;
                    
                case ISI_JSP_COMMENT_MMP:
                    switch (actChar) {
                        case '\n':
                            lexerState = ISI_JSP_COMMENT;
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else { // return block comment first
                                input.backup(1);
                                return token(JspTokenId.COMMENT);
                            }
                        case '>':
                            //                            state = ISA_END_JSP;
                            lexerState = INIT;
                            return token(JspTokenId.COMMENT);
                        default:
                            lexerState = ISI_JSP_COMMENT;
                            break;
                    }
                    break;
                    
                case ISI_ERROR:
                    switch (actChar) {
                        case '\n':
                            lexerState = INIT;
                            input.backup(1);
                            return token(JspTokenId.ERROR);
                        case '<':
                            lexerState = ISA_LT;
                            input.backup(1);
                            return token(JspTokenId.ERROR);
                    }
                    break;
                    
                case ISI_TAG_ERROR:
                    switch (actChar) {
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                lexerState = ISP_TAG;
                                return token(JspTokenId.EOL);
                            } else { // return error first
                                input.backup(1);
                                return token(JspTokenId.ERROR);
                            }
                        case '>':
                        case ' ':
                        case '\t':
                            lexerState = ISP_TAG;
                            input.backup(1);
                            return token(JspTokenId.ERROR);
                        default:
                            break;
                    }
                    break;
                    
                case ISI_DIR_ERROR:
                    switch (actChar) {
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                lexerState = ISP_DIR;
                                return token(JspTokenId.EOL);
                            } else { // return error first
                                input.backup(1);
                                return token(JspTokenId.ERROR);
                            }
                            //                        case '%':
                        case '\t':
                        case ' ':
                            lexerState = ISP_DIR;
                            if(input.readLength() > 1) {
                                input.backup(1);
                                return token(JspTokenId.ERROR);
                            }
                        default:
                            break;
                    }
                    break;
                    
                case ISI_DIR_ERROR_P:
                    switch (actChar) {
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                lexerState = ISI_DIR_I_WS;
                                return token(JspTokenId.EOL);
                            } else { // return error first
                                input.backup(1);
                                return token(JspTokenId.ERROR);
                            }
                        case '>':
                            input.backup(2);
                            lexerState = ISI_DIR_I_WS;
                            return token(JspTokenId.ERROR);
                        default:
                            break;
                    }
                    break;
                    
                    //                case ISA_END_JSP:
                    //                    if (input.readLength() == 1) {
                    //                        offset++;
                    //                        return JspTokenId.AFTER_UNEXPECTED_LT;
                    //                    }
                    //                    else {
                    //                        return JspTokenId.TEXT;
                    //                    }
                    //                    //break;
                    
                    // added states
                case ISA_LT_PC_AT:
                    if (Character.isLetter(actChar) ||
                            (actChar == '_')
                            ) {
                        // the directive starts
                        lexerState = ISI_DIRNAME;
                        break;
                    }
                    
                    switch (actChar) {
                        case '\n':
                            if (input.readLength() == 1) { // no char
                                return token(JspTokenId.EOL);
                            } else {
                                input.backup(1);
                                return token(JspTokenId.SYMBOL);
                            }
                        case ' ':
                        case '\t':
                            break;
                        case '%':
                            lexerState = ISA_ENDPC;
                            break;
                        default:
                            //error
                            lexerState = ISI_DIR_ERROR;
                            if(input.readLength() > 1) {
                                input.backup(1); //backup the error char if there is something more in the buffer
                                return token(JspTokenId.SYMBOL);
                            }
                            break;
                    }
                    break;
                    
            }
            
        }
        
        // At this stage there's no more text in the scanned buffer.
        // Scanner first checks whether this is completely the last
        // available buffer.
        
        switch(lexerState) {
            case INIT:
            case ISA_BS:
            case ISA_LT:
            case ISA_LT_SLASH:
                if (input.readLength() == 0) {
                    return null;
                } else {
                    return token(JspTokenId.TEXT);
                }
            case ISI_ERROR:
            case ISI_TAG_ERROR:
                lexerState = INIT;
                return token(JspTokenId.ERROR);
            case ISI_DIR_ERROR:
            case ISI_DIR_ERROR_P:
                lexerState = INIT;
                return token(JspTokenId.ERROR);
            case ISA_ENDSLASH:
            case ISP_TAG_EQ:
                lexerState = INIT;
                return token(JspTokenId.SYMBOL);
            case ISA_LT_PC:
            case ISA_LT_PC_DASH:
            case ISA_ENDPC:
            case ISP_DIR_EQ:
                lexerState = INIT;
                return token(JspTokenId.SYMBOL);
            case ISI_TAGNAME:
            case ISI_ENDTAG:
                lexerState = INIT;
                return token(JspTokenId.TAG);
            case ISI_DIRNAME:
                lexerState = INIT;
                return token(JspTokenId.TAG);
            case ISP_TAG:
            case ISI_TAG_I_WS:
                lexerState = INIT;
                return token(JspTokenId.TAG);
            case ISP_DIR:
            case ISI_DIR_I_WS:
            case ISA_LT_PC_AT:
                lexerState = INIT;
                return token(JspTokenId.TAG);
            case ISI_TAG_ATTR:
                lexerState = INIT;
                return token(JspTokenId.ATTRIBUTE);
            case ISI_DIR_ATTR:
                lexerState = INIT;
                return token(JspTokenId.ATTRIBUTE);
            case ISI_TAG_STRING:
            case ISI_TAG_STRING_B:
            case ISI_TAG_STRING2:
            case ISI_TAG_STRING2_B:
                lexerState = INIT;
                return token(JspTokenId.ATTR_VALUE);
            case ISI_DIR_STRING:
            case ISI_DIR_STRING_B:
            case ISI_DIR_STRING2:
            case ISI_DIR_STRING2_B:
                lexerState = INIT;
                return token(JspTokenId.ATTR_VALUE);
            case ISI_JSP_COMMENT:
            case ISI_JSP_COMMENT_M:
            case ISI_JSP_COMMENT_MM:
            case ISI_JSP_COMMENT_MMP:
                lexerState = INIT;
                return token(JspTokenId.COMMENT);
            case ISA_EL_DELIM_DOLLAR:
            case ISA_EL_DELIM_HASH:
                lexerState = INIT;
                return token(JspTokenId.TEXT);
            case ISI_EL:
            case ISI_EL_DOUBLE_QUOTE:
            case ISI_EL_SINGLE_QUOTE:
            case ISA_EL_DOUBLE_QUOTE_ESCAPE:
            case ISA_EL_SINGLE_QUOTE_ESCAPE:                
                lexerState = INIT;
                return token(JspTokenId.EL);
            case ISI_SCRIPTLET:
            case ISP_SCRIPTLET_PC:
                lexerState = INIT;
                return scriptletToken(JspTokenId.SCRIPTLET, lexerStateJspScriptlet);
            default:
                break;
        }
        
        return null;
        
    }
    
    private Token<JspTokenId> token(JspTokenId tokenId) {
        if(LOG) {
            checkToken(tokenId);
        }
        return tokenFactory.createToken(tokenId);
    }
    
    private Token<JspTokenId> scriptletToken(JspTokenId tokenId, int javaCodeType) {
        if(LOG) {
            checkToken(tokenId);
        }
        JspTokenId.JavaCodeType scriptletType;
        switch(javaCodeType) {
            case JAVA_SCRITPLET:
            case JAVA_SCRITPLET_DOCUMENT:
                scriptletType = JspTokenId.JavaCodeType.SCRIPTLET;
                break;
            case JAVA_DECLARATION:
            case JAVA_DECLARATION_DOCUMENT:
                scriptletType = JspTokenId.JavaCodeType.DECLARATION;
                break;
            case JAVA_EXPRESSION:
            case JAVA_EXPRESSION_DOCUMENT:
                scriptletType = JspTokenId.JavaCodeType.EXPRESSION;
                break;
            default:
                throw new IllegalStateException("Unsupported scriptlet type " + lexerStateJspScriptlet);
        }
        
        return tokenFactory.createPropertyToken(tokenId, input.readLength(),
                new JspTokenPropertyProvider(scriptletType), PartType.COMPLETE);
    }
    
    private void checkToken(JspTokenId tokenId) {
            if(input.readLength() == 0) {
                LOGGER.log(Level.INFO, "Found zero length token: ");
            }
            LOGGER.log(Level.INFO, "[" + this.getClass().getSimpleName() + "] token ('" + input.readText().toString() + "'; id=" + tokenId + "; state=" + state() + ")\n");
    }
    
    public void release() {
    }
    
    private static class JspTokenPropertyProvider implements TokenPropertyProvider {
        
        private final JspTokenId.JavaCodeType scriptletType;
        
        JspTokenPropertyProvider(JspTokenId.JavaCodeType scriptletType) {
            this.scriptletType = scriptletType;
        }

        public Object getValue(Token token, Object key) {
            if (JspTokenId.SCRIPTLET_TOKEN_TYPE_PROPERTY.equals(key))
                return scriptletType;
            return null;
        }

    }
    
}

