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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.DTDTokenId;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * New simple implementation of DTD lexer. Unlike XML lexer, this one does not attempt
 * to report erroneous token based on context; so keyword is a keyword (almost) on all places. Syntax
 * analyzer is needed to report premature ends or invalid combinations / sequences of tokens.
 * 
 * @author sdedic
 */
public class DTDLexer implements Lexer<DTDTokenId> {
    /**
     * Default state outside declarations or instructions
     */
    private static final int ISI_INIT = 0;
    
    /**
     * Plaintext outside declarations
     */
    private static final int ISI_TEXT = 1;
    
    /**
     * &amp;? processing instruction
     */
    private static final int ISI_PROCESSING_INSTR = 2;
    
    /**
     * DTD declaration
     */
    private static final int ISI_DECLARATION = 3;
    
    private static final int ISI_ENTITY = 4;
    private static final int ISI_ELEMENT = 5;
    private static final int ISI_ATTLIST = 6;
    private static final int ISI_NOTATION = 7;
    
    /**
     * Comment
     */
    private static final int ISI_COMMENT = 8;
    
    /**
     * Declaration processing; after initial symbol & before the target
     */
    private static final int SUB_PROCESSING_TARGET = 0;
    
    /**
     * Declaration processing; after target - content is being processed
     */
    private static final int SUB_PROCESSING_CONTENT = 1;
    
    /**
     * XML declaration is being processed; in between attributes, after
     * attribute name and before value
     */
    private static final int SUB_PROCESSING_XML = 2;
    
    private static final int SUB_DECLARATION_NAME = 0;
    private static final int SUB_DECLARATION_DEF = 1;

    private static final int SUB_VALUE_QUOTE = 5;
    private static final int SUB_VALUE_DOUBLE = 6;
    
    private LexerInput input;
    private int state;
    private int substate;
    private int intrSubstate;
    
    private TokenFactory<DTDTokenId> tokenFactory;
    
    private Token<DTDTokenId> unterminatedFoundOpen() {
        int ch = input.read();
        switch (ch) {
            case '?':
                setState(ISI_PROCESSING_INSTR, SUB_PROCESSING_TARGET);
                return error();
            case '!':
                ch = input.read();
                if (Character.isAlphabetic(ch)) {
                    // directive; symbol
                    setState(ISI_DECLARATION);
                    return error();
                } else if (ch == '-') { // NOI18N
                    // potential comment
                    ch = input.read();
                    if (ch == '-') { // NOI18N
                        setState(ISI_COMMENT);
                        return error();
                    }
                }
                input.backup(1);
                return error();
                
        }
        input.backup(1);
        return error();
    }
    
    public Token<DTDTokenId> nextTokenInit() {
        int ch = input.read();
        Token<DTDTokenId> tukac = null;
        switch (ch) {
            case '<': { // NOI18N
                ch = input.read();
                switch (ch) {
                    case '?': // NOI18N
                        // processing instruction; symbol
                        setState(ISI_PROCESSING_INSTR, SUB_PROCESSING_TARGET);
                        return tokenFactory.createToken(DTDTokenId.SYMBOL);
                    case '!': { // NOI18N
                        ch = input.read();
                        if (Character.isAlphabetic(ch)) {
                            // directive; symbol
                            setState(ISI_DECLARATION);
                            input.backup(1);
                            return tokenFactory.createToken(DTDTokenId.SYMBOL);
                        } else if (ch == '-') { // NOI18N
                            // potential comment
                            ch = input.read();
                            if (ch == '-') { // NOI18N
                                return skipComment();
                            }
                        }
                        input.backup(1);
                        return tokenFactory.createToken(DTDTokenId.ERROR);
                    }
                    default:
                        // fall through
                        break;
                }
            }
            case '&':
                tukac = processEntityOrCharacterRef();
                return tukac == null ? error() : tukac;
            case '%':
                tukac = processParsedEntity();
                return tukac == null ? error() : tukac;
            default:
                // fall through
                break;
        }
        if (tukac != null) {
            return tukac;
        }
        return nextTokenContent();
    }
    
    private Token<DTDTokenId> processCharacterReference() {
        int ch = input.read();
        boolean hex = ch == 'x';
        if (hex) {
            ch = input.read();
        }
        boolean first = true;
        do {
            if (ch == ';') {
                break;
            }
            if (!((ch >= '0' && ch <= '9') ||
                 hex && ((ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f')))) {
                return null;
            }
            first = false;
        } while ((ch = input.read()) != LexerInput.EOF);
        return createReferenceToken(first ? DTDTokenId.ERROR : DTDTokenId.CHARACTER);
    }
    
    private Token<DTDTokenId> createReferenceToken(DTDTokenId id) {
        Token<DTDTokenId> t = intrSubstate != 0 ?
                tokenFactory.createToken(id, input.readLength(), PartType.MIDDLE) :
                tokenFactory.createToken(id);
        return t;
    }
    
    private Token<DTDTokenId> processEntityOrCharacterRef() {
        int ch;
        boolean first = true;
        Token<DTDTokenId> t = null;
        while ((ch = input.read()) != LexerInput.EOF) {
            if (ch == ';') {
                return first ? error() : createReferenceToken(DTDTokenId.REFERENCE);
            }
            if (first && ch == '#') {
                int r = input.readLength();
                t = processCharacterReference();
                if (t != null) {
                    return t;
                } else {
                    input.backup(input.readLength() - r);
                    return error();
                }
            }
            if (!((first && Character.isLetter(ch)) || 
                    (!first && (
                        ch == '-' || ch == '.' || Character.isLetterOrDigit(ch))))) {
                // not an entity reference, fall back to normal text processing
                input.backup(1);
                break;
            } 
            first = false;
        }
        // end of document inside potential reference
        return error();
    }
    
    private Token<DTDTokenId> skipComment() {
        int ch;
        boolean minus = false;
        setState(ISI_COMMENT);
        while ((ch = input.read()) != LexerInput.EOF) {
            if (ch == '-') {
                if (minus) {
                    ch = input.read();
                    if (ch == '>') { // NOI18N
                        setState(ISI_INIT);
                        return tokenFactory.createToken(DTDTokenId.COMMENT);
                    } else {
                        intrSubstate = 0;
                        if (input.readLength() > 2) {
                            setState(ISI_INIT);
                            input.backup(2);
                            return tokenFactory.createToken(DTDTokenId.COMMENT);
                        } else {
                            return tokenFactory.createToken(DTDTokenId.ERROR);
                        }
                    }
                } else {
                    minus = true;
                }
                continue;
            }
            minus = false;
            if (ch == '&') {
                if (input.readLength() > 1) {
                    input.backup(1);
                    return tokenFactory.createToken(DTDTokenId.COMMENT);
                }
                intrSubstate = saveState();
                return processEntityOrCharacterRef();
            }
        }
        intrSubstate = 0;
        setState(ISI_INIT);
        if (input.readLength() > 0) {
            // report entire thing as a comment
            input.backup(1);
            return tokenFactory.createToken(DTDTokenId.COMMENT);
        } else {
            return tokenFactory.createToken(DTDTokenId.ERROR);
        }
    }
    
    private void setState(int state) {
        setState(state, 0);
    }
    
    private void setState(int state, int subState) {
        this.state = state;
        this.substate = subState;
        intrSubstate = 0;
   }
    
    /**
     * Regular content.
     * Stop at first 'markup' character.
     */
    private Token<DTDTokenId> nextTokenContent() {
        int ch;
        while ((ch = input.read()) != LexerInput.EOF) {
            switch (ch) {
                case '<':
                    // retract a bit:
                    input.backup(1);
                    setState(ISI_INIT);
                    return tokenFactory.createToken(DTDTokenId.PLAIN);
                case '&':
                    if (input.readLength() > 1) {
                        input.backup(1);
                        setState(ISI_INIT);
                        return tokenFactory.createToken(DTDTokenId.PLAIN);
                    }
                    return processEntityOrCharacterRef();
                case '%':
                    // parsed entity
                    if (input.readLength() > 1) {
                        input.backup(1);
                        setState(ISI_INIT);
                        return tokenFactory.createToken(DTDTokenId.PLAIN);
                    }
                    Token<DTDTokenId> id =  processParsedEntity();
                    if (id != null) {
                        setState(ISI_INIT);
                        return id;
                    }
                    // fall through:
                default:
                    break;
            }
        }
        setState(ISI_INIT);
        if (input.readLength() == 0) {
            return null;
        }
        return tokenFactory.createToken(DTDTokenId.PLAIN);
    }
    
    private Token<DTDTokenId> processParsedEntity() {
        int ch;
        boolean first = true;
        while ((ch = input.read()) != LexerInput.EOF) {
            if (ch == ';') {
                return first ? null : tokenFactory.createToken(DTDTokenId.REFERENCE);
            }
            if (!((first && Character.isLetter(ch)) || 
                    (!first && (
                        ch == '-' || ch == '.' || Character.isLetterOrDigit(ch))))) {
                // not an entity reference, fall back to normal text processing:
                break;
            } 
            first = false;
        }
        return null;
    }
    
    @Override
    public Token<DTDTokenId> nextToken() {
        switch (state) {
            case ISI_INIT:
                return nextTokenInit();
            case ISI_COMMENT:
                return skipComment();
            case ISI_PROCESSING_INSTR:
                return nextProcessingInstr();
            case ISI_DECLARATION:
                return nextDeclaration();
            case ISI_ELEMENT:
            case ISI_ATTLIST:
                return processElementOrAttlist();
            case ISI_ENTITY:
            case ISI_NOTATION:
                break;
        }
        int ch = input.read();
        if (ch != LexerInput.EOF) {
            return error();
        } else {
            return null;
        }
    }
    
    private Token<DTDTokenId> processElementOrAttlist() {
        switch (substate) {
            case SUB_VALUE_DOUBLE:
                restoreState();
                return stringvalue('"');
            case SUB_VALUE_QUOTE:
                restoreState();
                return stringvalue('\'');
        }
        Token<DTDTokenId> wh = skipWhitespace();
        if (wh != null) {
            return wh;
        }
        int ch = input.read();
        if (ch == LexerInput.EOF) {
            return null;
        }
        if (ch == '>') {
            // terminate definition
            setState(ISI_INIT);
            return tokenFactory.createToken(DTDTokenId.SYMBOL);
        } else if (ch == '&' || ch == '%') {
            return processEntityOrCharacterRef();
        }
        switch (substate) {
            case SUB_DECLARATION_NAME:
                input.backup(1);
                return processDeclarationName();
            case SUB_DECLARATION_DEF:
                input.backup(1);
                return processDeclarationDef();
        }
        return error();
    }
    
    private static final Map<String, Integer>   DECLARATION_KEYWORDS = new HashMap<>();

    /**
     * Recognized declaration keywords. A declaration (&lt;!) followed by other name
     * will be reported as ERROR token.
     */
    static {
        DECLARATION_KEYWORDS.put("ELEMENT", ISI_ELEMENT);
        DECLARATION_KEYWORDS.put("ATTLIST", ISI_ATTLIST);
        DECLARATION_KEYWORDS.put("ENTITY", ISI_ENTITY);
        DECLARATION_KEYWORDS.put("NOTATION", ISI_NOTATION);
    }
    
    private Token<DTDTokenId> nextDeclaration() {
        int ch = input.read();
        // process escapes:
        switch (ch) {
            case '>':
                // end of declaration
                setState(ISI_INIT);
                return tokenFactory.createToken(DTDTokenId.SYMBOL);
            case '<':
                return unterminatedFoundOpen();
        }
        if (!Character.isLetter(ch)) {
            return error();
        }
        while ((ch = input.read()) != LexerInput.EOF) {
            if (!Character.isLetterOrDigit(ch)) {
                if (input.readLength() == 1) {
                    setState(ISI_INIT);
                    return error();
                }
                input.backup(1);
                break;
            }
        }
        String name = input.readText().toString();
        Integer nState = DECLARATION_KEYWORDS.get(name);
        if (nState == null) {
            setState(ISI_INIT);
            return error();
        }
        setState(nState);
        return tokenFactory.createToken(DTDTokenId.DECLARATION);
    }
    
    private Token<DTDTokenId> processDeclarationName() {
        int ch;
        boolean first = true;
        while ((ch = input.read()) != LexerInput.EOF) {
            if (first && (ch == '%') && (state == ISI_ENTITY)) {
                int n = input.read();
                if (Character.isWhitespace(n)) {
                    // ENTITY % whatever. Emit OPERATOR
                    return tokenFactory.createToken(DTDTokenId.OPERATOR);
                }
                input.backup(1);
            }
            if (ch == '%' || ch == '&') {
                Token<DTDTokenId> t = processEntityOrCharacterRef();
                substate = SUB_DECLARATION_DEF;
                if (t != null) {
                    return t;
                } else {
                    return error();
                }
            }
            if (!isNametokenChar(ch, first)) {
                if (first) {
                    substate = SUB_DECLARATION_DEF;
                    return error();
                }
                input.backup(1);
                break;
            }
            first = false;
        }
        substate = SUB_DECLARATION_DEF;
        return tokenFactory.createToken(DTDTokenId.NAME);
    }

    /**
     * Keywords possible after &lt;!ELEMENT.
     */
    private static final Set<String> ELEMENT_KEYWORDS = new HashSet<>();
    static {
        ELEMENT_KEYWORDS.add("#PCDATA");
        ELEMENT_KEYWORDS.add("EMPTY");
        ELEMENT_KEYWORDS.add("ANY");
    }

    /**
     * Keywords possible after &lt;!ATTLIST.
     */
    private static final Set<String> ATTLIST_KEYWORDS = new HashSet<>();
    
    /**
     * Keywords possible after &lt;!NOTATION.
     */
    private static final Set<String> NOTATION_KEYWORDS = new HashSet<>();
    
    static {
        ATTLIST_KEYWORDS.add("#PCDATA");
        ATTLIST_KEYWORDS.add("#REQUIRED");
        ATTLIST_KEYWORDS.add("CDATA");
        ATTLIST_KEYWORDS.add("ID");
        ATTLIST_KEYWORDS.add("IDREF");
        ATTLIST_KEYWORDS.add("IDREFS");
        ATTLIST_KEYWORDS.add("ENTITY");
        ATTLIST_KEYWORDS.add("NMTOKEN");
        ATTLIST_KEYWORDS.add("NMTOKENS");
        ATTLIST_KEYWORDS.add("NOTATION");
        ATTLIST_KEYWORDS.add("#REQUIRED");
        ATTLIST_KEYWORDS.add("#IMPLIED");
        ATTLIST_KEYWORDS.add("#FIXED");
        
        NOTATION_KEYWORDS.add("SYSTEM");
        NOTATION_KEYWORDS.add("PUBLIC");
        NOTATION_KEYWORDS.add("NDATA");
    }
    
    private Token<DTDTokenId> processDeclarationDef() {
        Token<DTDTokenId> wh = skipWhitespace();
        if (wh != null) {
            return wh;
        }
        int ch = input.read();
        
        switch (ch) {
            case '(': case ')': case '|': case ',':                     // NOI18N
            case '+':                                                   // NOI18N
            case '*':                                                   // NOI18N
            case '?':                                                   // NOI18N
                return tokenFactory.createToken(DTDTokenId.OPERATOR);
            
            case '%': case '&':                                         // NOI18N
                return processEntityOrCharacterRef();
                
            case '"': case '\'':                                        // NOI18N
                return stringvalue(ch);
                
            default:
                if (ch == '#' || Character.isLetter(ch)) {              // NOI18N
                    while ((ch = input.read()) != LexerInput.EOF &&
                           Character.isLetterOrDigit(ch)) {
                        // advance
                    }
                    input.backup(1);
                    String t = input.readText().toString();
                    Set<String> check;
                    switch (state) {
                        case ISI_ATTLIST:
                            check = ATTLIST_KEYWORDS;
                            break;
                        case ISI_ELEMENT:
                            check = ELEMENT_KEYWORDS;
                            break;
                        case ISI_ENTITY:
                        case ISI_NOTATION:
                            check = NOTATION_KEYWORDS;
                            break;
                        default:
                            check = Collections.emptySet();
                    }
                    if (check.contains(t)) {
                        return tokenFactory.createToken(DTDTokenId.KEYWORD);
                    } else {
                        return tokenFactory.createToken(DTDTokenId.NAME);
                    }
                }
                break;
        }
        return error();
    }
    
    private Token<DTDTokenId> nextProcessingInstr() {
        switch (substate) {
            case SUB_VALUE_DOUBLE:
                restoreState();
                return stringvalue('"');
            case SUB_VALUE_QUOTE:
                restoreState();
                return stringvalue('\'');
        }
        Token<DTDTokenId> wh = skipWhitespace();
        if (wh != null) {
            return wh;
        }
        int ch = input.read();
        if (ch == LexerInput.EOF) {
            return null;
        }
        // process escapes to the upper level
        switch (ch) {
            case '?':   // NOI18N
                return endProcessingInstruction();
            case '>':   // NOI18N
                setState(ISI_INIT);
                return error();
            case '<':   // NOI18N
                return unterminatedFoundOpen();
            default:
                break;
        }
        switch (substate) {
            case SUB_PROCESSING_TARGET:
                input.backup(1);
                return nextProcessingTarget();
            case SUB_PROCESSING_CONTENT:
                input.backup(1);
                return nextProcessingContent();
            case SUB_PROCESSING_XML:
                if (ch == '=') { // NOI18N
                    // operator
                    return tokenFactory.createToken(DTDTokenId.OPERATOR);
                } else if (ch == '"' || ch == '\'') { // NOI18N
                    return stringvalue(ch);
                } else if (Character.isLetter(ch)) {
                    // probably a name
                    input.backup(1);
                    return processName();
                } else {
                    error();
                }
                break;
        }
        return error();
    }
    
    private Token<DTDTokenId> stringvalue(int delimiter) {
        int ch;

        while ((ch = input.read()) != LexerInput.EOF) {
            if (ch == delimiter) {
                restoreState();
                return tokenFactory.createToken(DTDTokenId.STRING);
            }
            // entity reference in attribute !
            if (ch == '&') {    // NOI18N
                intrSubstate = saveState();
                substate = delimiter == '"' ? SUB_VALUE_DOUBLE : SUB_VALUE_QUOTE;
                return valueEntityReference();
            } else if (ch == '<') { // NOI18N
                // ouch ! unterminated value - go to the basic state.
                input.backup(1);
                break;
            }
        }
        if (input.readLength() > 0) {
            return tokenFactory.createToken(DTDTokenId.STRING);
        } 
        setState(ISI_INIT);
        return error();
    }
    
    private Token<DTDTokenId> valueEntityReference() {
        if (input.readLength() > 1) {
            // output partial value token
            input.backup(1);
            return tokenFactory.createToken(DTDTokenId.STRING);
        }
        return processEntityOrCharacterRef();
    }
    
    /**
     * Skips whitespaces within declaration, produces DTDToken if whitespace
     * is found. Does not change state / substate.
     * @return 
     */
    private Token<DTDTokenId> skipWhitespace() {
        int ch;
        int start = input.readLength();
        while ((ch = input.read()) != LexerInput.EOF) {
            if (!Character.isWhitespace(ch)) {
                input.backup(1);
                if ((input.readLength() - start) > 0) {
                    return tokenFactory.createToken(DTDTokenId.WS, input.readLength() - start);
                }
                break;
            }
        }
        if ((input.readLength() - start) > 0) {
            return tokenFactory.createToken(DTDTokenId.WS, input.readLength() - start);
        } else {
            return null;
        }
    }
    
    private boolean isNametokenChar(int c, boolean first) {
        if (first) {
            return c == ':' || c == '_' || Character.isLetter(c);
        } else {
            switch (c) {
                case ':': case '_': // NOI18N
                case '-': case '.': // NOI18N
                    return true;
                default:
                    return Character.isLetterOrDigit(c);
            }
        }
    }
    
    private Token<DTDTokenId> error() {
        Token<DTDTokenId> t;
        if (intrSubstate != 0) {
            t = tokenFactory.createToken(DTDTokenId.ERROR, input.readLength(), PartType.MIDDLE);
        } else {
            t = tokenFactory.createToken(DTDTokenId.ERROR);
        }
        return t;
    }
    
    private Token<DTDTokenId> processName() {
        int ch;
        boolean first = true;
        while ((ch = input.read()) != LexerInput.EOF) {
            if (isNametokenChar(ch, first)) {
                first = false;
            } else {
                input.backup(1);
                break;
            }
        }
        if (input.readLength() > 0) {
            // output a NAME token
            return tokenFactory.createToken(DTDTokenId.NAME);
        } else if (ch == '?') {
            return endProcessingInstruction();
        } else {
            return error();
        }
    }
    
    private Token<DTDTokenId> endProcessingInstruction() {
        // ? was already consumed
        int ch = input.read();
        setState(ISI_INIT);
        if (ch == '>') {
            return tokenFactory.createToken(DTDTokenId.SYMBOL);
        } else {
            // ? is erroneous
            return error();
        }
    }
    
    private Token<DTDTokenId> nextProcessingTarget() {
        int ch;
        boolean first = true;
        
        while ((ch = input.read()) != LexerInput.EOF) {
            if (ch == '?') { // NOI18N
                if (first) {
                    return nextProcessingContent();
                }
                substate = SUB_PROCESSING_CONTENT;
                input.backup(1);
                break;
            } else if (Character.isWhitespace(ch)) {
                if (first) {
                    return tokenFactory.createToken(DTDTokenId.ERROR);
                }
                substate = SUB_PROCESSING_CONTENT;
                input.backup(1);
                break;
            }
            if (!((first & Character.isLetter(ch)) ||
                (!first && Character.isLetterOrDigit(ch)))) {
                // apparently an error:
                return tokenFactory.createToken(DTDTokenId.ERROR);
            }
            first = false;
        }
        if ("xml".equalsIgnoreCase(input.readText().toString())) { // NOI18N
            substate = SUB_PROCESSING_XML;
        }
        return tokenFactory.createToken(DTDTokenId.TARGET);
    }
    
    private Token<DTDTokenId> nextProcessingContent() {
        int ch;
        boolean white = false;
        int whiteStart = -1;
        
        while ((ch = input.read()) != LexerInput.EOF) {
            if (ch == '?') {
                if (whiteStart > 0) {
                    return tokenFactory.createToken(DTDTokenId.PI_CONTENT, whiteStart);
                }
                if (input.readLength() > 1) {
                    input.backup(1);
                    return tokenFactory.createToken(DTDTokenId.PI_CONTENT);
                }
                ch = input.read();
                if (ch == '>') {
                    return tokenFactory.createToken(DTDTokenId.SYMBOL);
                }
                // exit the processing content
                setState(ISI_INIT);
                return tokenFactory.createToken(DTDTokenId.ERROR);
            }
            if (Character.isWhitespace(ch)) {
                if (whiteStart == -1) {
                    whiteStart = input.readLength();
                }
                if (input.readLength() == 1) {
                    white = true;
                }
            } else if (white) {
                // report whitespace & continue lexing in the same state:
                return tokenFactory.createToken(DTDTokenId.WS);
            }
        }
        return tokenFactory.createToken(DTDTokenId.PI_CONTENT);
    }

    @Override
    public Object state() {
        return (state & 0x0f) | ((substate & 0x0f) << 4) | ((intrSubstate & 0xff) << 16);
    }
    
    private int saveState() {
        return (state & 0x0f) | ((substate & 0x0f) << 4);
    }
    
    private void restoreState() {
        if (intrSubstate == 0) {
            return;
        }
        int s = intrSubstate;
        this.state = s & 0x0f;
        this.substate = (s >> 4) & 0x0f;
        this.intrSubstate = 0;
    }

    @Override
    public void release() {
    }
    
    public DTDLexer(LexerRestartInfo<DTDTokenId> info) {
        this.input = info.input();
        if (info.state() == null) {
            state = ISI_INIT;
            substate = 0;
        } else {
            int s = (Integer)info.state();
            this.state = s & 0x0f;
            this.substate = (s >> 4) & 0x0f;
            this.intrSubstate = (s >> 16) & 0xff;
        }
        this.tokenFactory = info.tokenFactory();
    }

}
