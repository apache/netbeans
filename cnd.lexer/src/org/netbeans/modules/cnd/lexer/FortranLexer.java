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
package org.netbeans.modules.cnd.lexer;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndLexerUtilities.FortranFormat;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for Fortran languages.
 *
 */
public class FortranLexer implements Lexer<FortranTokenId> {

    protected static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final TokenFactory<FortranTokenId> tokenFactory;
    private boolean fortranFreeFormat = true;
    private int maximumTextWidth = 132; // standard length limit for fortran free format
    private final Filter<FortranTokenId> lexerFilter;

    // internal analyzer states
    // Initial state of the analyzer
    private static final int INIT = -1;
    //numbers assigned to states are not important as long as they are unique
    private static final int AFTER_SLASH = 1;       // after slash char
    private static final int AFTER_EQ = 2;          // after '='
    private static final int AFTER_STAR = 3;        // after '*'
    private static final int AFTER_LESSTHAN = 4;    // after '<'
    private static final int AFTER_GREATERTHAN = 5; // after '>'
    private static final int AFTER_B = 6;           // after 'b' or 'B'
    private static final int AFTER_O = 7;           // after 'o' or 'O'
    private static final int AFTER_Z = 8;           // after 'z' or 'Z'
    private static final int AFTER_DOT = 9;         // after '.'    
    private static final int IN_STRING = 10;        // inside string constant
    private static final int IN_STRING_AFTER_BSLASH = 11; //inside string const after backslash
    private static final int IN_LINE_COMMENT = 12;     // inside line comment
    private static final int IN_IDENTIFIER = 13;       // inside identifier
    private static final int IN_DOT_IDENTIFIER = 14;   // inside .identifier
    private static final int IN_WHITESPACE = 15;       // inside white space
    private static final int IN_INT = 16;    // integer number
    private static final int IN_BINARY = 17; // binary number
    private static final int IN_OCTAL = 18;  // octal number
    private static final int IN_HEX = 19;    // hex number
    private static final int IN_REAL = 20;   // real number
    private static final int IN_APOSTROPHE_CHAR = 21; // after id

    // specifies if the string is defined in double quotes or single quote
    private boolean stringInDoubleQuote = true;

    // specifies if the free style comment
    private boolean lineCommentFree = true;

    // this variable is put for detecting the "_" in integers and reals
    private boolean hasNumericUnderscore = false;

    // Internal state of the lexical analyzer
    private int state = INIT;

    // Number of eaten chars from the begining of line
    private int lineColomn = 0;

    @SuppressWarnings("unchecked")
    public FortranLexer(Filter<FortranTokenId> defaultFilter, LexerRestartInfo<FortranTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        Filter<FortranTokenId> filter = (Filter<FortranTokenId>) info.getAttributeValue(CndLexerUtilities.LEXER_FILTER); // NOI18N
        this.lexerFilter = filter != null ? filter : defaultFilter;
        Object o = info.getAttributeValue(CndLexerUtilities.FORTRAN_MAXIMUM_TEXT_WIDTH);
        if(o != null) {
            this.maximumTextWidth = (Integer) o;
        }
        o = info.getAttributeValue(CndLexerUtilities.FORTRAN_FREE_FORMAT);
        if(o != null) {
            if (o == FortranFormat.UNDEFINED) {
                
            }
            this.fortranFreeFormat = o == FortranFormat.FREE;
        }
        setState((State) info.state());
    }

    @Override
    public Object state() {
        return getState();
    }

    @Override
    public void release() {
    }

    @SuppressWarnings("fallthrough")
    @Override
    public Token<FortranTokenId> nextToken() {

        while (true) {
            int c = read();

            if (c == EOF) {
                break;
            }

            //STATE SWITCH
            switch (state) {
                //INIT STATE
                case INIT:
                    if (isLineBeyondLimit()) {
                        backup(1, c); //reevaluate the char
                        break;
                    }
                    if ((lineColomn == 6) && !fortranFreeFormat) {
                        if (!Character.isWhitespace(c)) {
                            return token(FortranTokenId.LINE_CONTINUATION_FIXED);
                        }
                    }

                    switch (c) {
                        case '#':
                            return finishSharp();
                        case '\n':
                            lineColomn = 0;
                            return token(FortranTokenId.NEW_LINE);
                        case 'b':
                        case 'B':
                            state = AFTER_B;
                            break;
                        case 'o':
                        case 'O':
                            state = AFTER_O;
                            break;
                        case 'z':
                        case 'Z':
                            state = AFTER_Z;
                            break;
                        case '"':
                            //make sure that this case is always after cases b, o and z
                            state = IN_STRING;
                            stringInDoubleQuote = true;
                            break;
                        case '\'': {
                            //make sure that this case is always after cases b, o and z
                            state = IN_STRING;
                            stringInDoubleQuote = false;
                            break;
                        }
                        case '/':
                            state = AFTER_SLASH;
                            break;
                        case '=':
                            state = AFTER_EQ;
                            break;
                        case '+':
                            return token(FortranTokenId.OP_PLUS);
                        case '-':
                            return token(FortranTokenId.OP_MINUS);
                        case '*':
                            if ((lineColomn == 1) && !fortranFreeFormat) {
                                lineCommentFree = false;
                                state = IN_LINE_COMMENT;
                            } else {
                                state = AFTER_STAR;
                            }
                            break;
                        case '!':
                            // Fortran comments begin with a ! and last to end of line
                            lineCommentFree = true;
                            state = IN_LINE_COMMENT;
                            break;
                        case 'C':
                        case 'c':
                            if ((lineColomn == 1) && !fortranFreeFormat) {
                                lineCommentFree = false;
                                state = IN_LINE_COMMENT;
                            } else {
                                backup(1, c);
                                state = IN_IDENTIFIER;
                            }
                            break;
                        case '<':
                            state = AFTER_LESSTHAN;
                            break;
                        case '>':
                            state = AFTER_GREATERTHAN;
                            break;
                        case '.':
                            state = AFTER_DOT;
                            break;
                        case ',':
                            return token(FortranTokenId.COMMA);
                        case ':':
                            int cc = read();
                            if (cc == ':') {
                                return token(FortranTokenId.DOUBLECOLON);
                            }
                            backup(1, c);
                            return token(FortranTokenId.COLON);
                        case '%':
                            return token(FortranTokenId.PERCENT);
                        case '&':
                            return token(FortranTokenId.AMPERSAND);
                        case '(':
                            return token(FortranTokenId.LPAREN);
                        case ')':
                            return token(FortranTokenId.RPAREN);
                        case ';':
                            return token(FortranTokenId.SEMICOLON);
                        case '?':
                            return token(FortranTokenId.QUESTION_MARK);
                        case '$':
                            return token(FortranTokenId.CURRENCY);
                        default:
                            // Check for whitespace
                            if (Character.isWhitespace(c)) {
                                state = IN_WHITESPACE;
                                break;
                            }

                            // Check for digit
                            if (Character.isDigit(c)) {
                                state = IN_INT;
                                break;
                            }

                            // Check for identifier
                            if (CndLexerUtilities.isFortranIdentifierPart(c)) {
                                state = IN_IDENTIFIER;
                                backup(1, c);
                                break;
                            }

                            return token(FortranTokenId.ERR_INVALID_CHAR);
                    }//switch(c)
                    break;
                //END INIT STATE

                case IN_WHITESPACE: // white space
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.WHITESPACE);
                    }
                    if ((!Character.isWhitespace(c)) || (c == '\n')) {
                        state = INIT;
                        backup(1, c);
                        return token(FortranTokenId.WHITESPACE);
                    }
                    break;

                case AFTER_B:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.IDENTIFIER);
                    }
                    switch (c) {
                        case '"':
                        case '\'':
                            int cc = read();
                            if (Character.isDigit(cc)) {
                                state = IN_BINARY;
                                backup(1, c);
                                break;
                            } //else continue to default
                        default:
                            state = IN_IDENTIFIER;
                            backup(2, c);  //go back and evaluate the character
                            break;
                    }//switch AFTER_B
                    break;

                case AFTER_O:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.IDENTIFIER);
                    }
                    switch (c) {
                        case '"':
                        case '\'':
                            int cc = read();
                            if (Character.isDigit(cc)) {
                                state = IN_OCTAL;
                                backup(1, c);
                                break;
                            } //else continue to default
                        default:
                            state = IN_IDENTIFIER;
                            backup(2, c);  //go back and evaluate the character
                            break;
                    }//switch AFTER_O
                    break;

                case AFTER_Z:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.IDENTIFIER);
                    }
                    switch (c) {
                        case '"':
                        case '\'':
                            int cc = read();
                            if (Character.isLetterOrDigit(cc)) {
                                state = IN_HEX;
                                backup(1, c);
                                break;
                            } //else continue to default
                        default:
                            state = IN_IDENTIFIER;
                            backup(2, c);  //go back and evaluate the character
                            break;
                    }//switch AFTER_Z
                    break;

                case IN_LINE_COMMENT:
                    switch (c) {
                        case '\n':
                            state = INIT;
                            if (input.readLength() > 1) {
                                backup(1, c);
                                if(lineCommentFree) {
                                    return token(FortranTokenId.LINE_COMMENT_FREE);
                                } else {
                                    return token(FortranTokenId.LINE_COMMENT_FIXED);
                                }
                            } else {
                                lineColomn = 0;
                                return token(FortranTokenId.NEW_LINE);
                            }
                    }//switch IN_LINE_COMMENT
                    break;

                case IN_STRING:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.ERR_INCOMPLETE_STRING_LITERAL);
                    }
                    switch (c) {
                        case '\\':
                            state = IN_STRING_AFTER_BSLASH;
                            break;
                        case '\n':
                            state = INIT;
                            backup(1, c);
                            return token(FortranTokenId.STRING_LITERAL);
                        case '"':
                            if (stringInDoubleQuote) {
                                state = INIT;
                                return token(FortranTokenId.STRING_LITERAL);
                            }
                            break;
                        case '\'':
                            if (!stringInDoubleQuote) {
                                state = INIT;
                                return token(FortranTokenId.STRING_LITERAL);
                            }
                            break;
                    } //switch IN_STRING
                    break;

                case IN_STRING_AFTER_BSLASH:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.ERR_INCOMPLETE_STRING_LITERAL);
                    }
                    switch (c) {
                        case '"':
                        case '\'':
                        case '\\':
                            break;   //ignore the meaning of these characters
                        default:
                            backup(1, c);  //go back and evaluate the character
                            break;
                    }//switch IN_STRING_AFTER_BSLASH:
                    state = IN_STRING;
                    break;

                case AFTER_SLASH:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.OP_DIV);
                    }
                    switch (c) {
                        case '/':
                            state = INIT;
                            return token(FortranTokenId.OP_CONCAT);
                        case '=':
                            state = INIT;
                            return token(FortranTokenId.OP_NOT_EQ);
                        default:
                            state = INIT;
                            backup(1, c);
                            return token(FortranTokenId.OP_DIV);
                    }//switch AFTER_SLASH
                //break;

                case AFTER_EQ:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.EQ);
                    }
                    switch (c) {
                        case '=':
                            state = INIT;
                            return token(FortranTokenId.OP_LOG_EQ);
                        default:
                            state = INIT;
                            backup(1, c);
                            return token(FortranTokenId.EQ);
                    }//switch AFTER_EQ
                //break;

                case AFTER_STAR:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.OP_MUL);
                    }
                    switch (c) {
                        case '*':
                            state = INIT;
                            return token(FortranTokenId.OP_POWER);
                        default:
                            state = INIT;
                            backup(1, c);
                            return token(FortranTokenId.OP_MUL);
                    }//switch AFTER_STAR
                //break;

                case AFTER_LESSTHAN:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.OP_LT);
                    }
                    switch (c) {
                        case '=':
                            state = INIT;
                            return token(FortranTokenId.OP_LT_EQ);
                        case '>':
                            state = INIT;
                            return token(FortranTokenId.OP_LT_GT);
                        default:
                            state = INIT;
                            backup(1, c);
                            return token(FortranTokenId.OP_LT);
                    }//switch AFTER_LESSTHAN
                //break;

                case AFTER_GREATERTHAN:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.OP_GT);
                    }
                    switch (c) {
                        case '=':
                            state = INIT;
                            return token(FortranTokenId.OP_GT_EQ);
                        default:
                            state = INIT;
                            backup(1, c);
                            return token(FortranTokenId.OP_GT);
                    }//switch AFTER_GREATERTHAN
                //break;

                case IN_IDENTIFIER:
                    Token<FortranTokenId> t = keywordOrIdentifier(c);
                    state = INIT;
                    c = read();
                    backup(1, c);
                    if (c == '\'') {
                        state = IN_APOSTROPHE_CHAR;
                    }
                    return t;

                case IN_BINARY:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.ERR_INVALID_BINARY_LITERAL);
                    }
                    if ((c == '\'' || c == '"')) {
                        state = INIT;
                        return token(FortranTokenId.NUM_LITERAL_BINARY);
                    } else if (((Character.isDigit(c)) && (c > '1')) ||
                            !(Character.isDigit(c))) {
                        state = INIT;
                        backup(1, c);
                        return token(FortranTokenId.ERR_INVALID_BINARY_LITERAL);
                    }
                    break;

                case IN_OCTAL:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.ERR_INVALID_OCTAL_LITERAL);
                    }
                    if ((c == '\'' || c == '"')) {
                        state = INIT;
                        return token(FortranTokenId.NUM_LITERAL_OCTAL);
                    } else if (((Character.isDigit(c)) && (c > '7')) ||
                            !(Character.isDigit(c))) {
                        state = INIT;
                        backup(1, c);
                        return token(FortranTokenId.ERR_INVALID_OCTAL_LITERAL);
                    }
                    break;

                case IN_HEX:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.ERR_INVALID_HEX_LITERAL);
                    }
                    if ((c == '\'' || c == '"')) {
                        state = INIT;
                        return token(FortranTokenId.NUM_LITERAL_HEX);
                    } else if (!(Character.isDigit(c)) &&
                            ((Character.toLowerCase(c) < 'a') ||
                            (Character.toLowerCase(c) > 'f'))) {
                        state = INIT;
                        backup(1, c);
                        return token(FortranTokenId.ERR_INVALID_HEX_LITERAL);
                    }
                    break;

                case IN_INT:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.NUM_LITERAL_INT);
                    }
                    switch (c) {
                        case '_':
                            hasNumericUnderscore = true;
                            break;
                        case '.':
                            if (hasNumericUnderscore) {
                                state = INIT;
                                hasNumericUnderscore = false;
                                return token(FortranTokenId.ERR_INVALID_INTEGER);
                            } else {
                                state = IN_REAL;
                                break;
                            }
                        case 'd':
                        case 'D':
                        case 'e':
                        case 'E':
                        case 'q':
                        case 'Q':
                            if (!hasNumericUnderscore) {
                                state = IN_REAL;
                            }
                            break;
                        default:
                            if (((hasNumericUnderscore) && (!(Character.isLetterOrDigit(c)))) ||
                                    ((!hasNumericUnderscore) && (!(Character.isDigit(c))))) {
                                state = INIT;
                                hasNumericUnderscore = false;
                                backup(1, c);
                                return token(FortranTokenId.NUM_LITERAL_INT);
                            }
                    }//switch
                    break;

                case IN_REAL:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.NUM_LITERAL_REAL);
                    }
                    switch (c) {
                        case '+':
                        case '-':
                            break;
                        case '_':
                            hasNumericUnderscore = true;
                            break;
                        case 'd':
                        case 'D':
                        case 'e':
                        case 'E':
                        case 'q':
                        case 'Q':
                            if (!hasNumericUnderscore) {
                                break;
                            }
                        default:
                            if (((hasNumericUnderscore) && (!(Character.isLetterOrDigit(c)))) ||
                                    ((!hasNumericUnderscore) && (!(Character.isDigit(c))))) {
                                state = INIT;
                                hasNumericUnderscore = false;
                                backup(1, c);
                                return token(FortranTokenId.NUM_LITERAL_REAL);
                            }
                    }//switch
                    break;

                case AFTER_DOT:
                    if (isLineBeyondLimit()) {
                        backup(1, c);
                        return token(FortranTokenId.DOT);
                    }
                    if (Character.isDigit(c)) {
                        state = IN_REAL;
                    } else if (CndLexerUtilities.isFortranIdentifierPart(c)) {
                        // Keyword, like .gt., .le., etc.
                        backup(2, c);
                        state = IN_DOT_IDENTIFIER;
                    } else {
                        state = INIT;
                        backup(1, c);
                        return token(FortranTokenId.DOT);
                    }
                    break;

                case IN_DOT_IDENTIFIER:
                    state = INIT;
                    Token<FortranTokenId> t2 = keywordOperator(c);
                    if(t2 != null) {
                        return t2;
                    } else {
                        return token(FortranTokenId.DOT);
                    }
                case IN_APOSTROPHE_CHAR:
                    state = INIT;
                    return token(FortranTokenId.APOSTROPHE_CHAR);
            } // end of switch(state)
        //END STATE SWITCH

        } //while(offset...)
        //END WHILE OFFSET

        /** At this stage there's no more text in the scanned buffer.
         * Scanner first checks whether this is completely the last
         * available buffer.
         */
        if (input.readLength() > 0) {
            switch (state) {
                case IN_WHITESPACE:
                    state = INIT;
                    return token(FortranTokenId.WHITESPACE);
                case AFTER_B:
                case AFTER_O:
                case AFTER_Z:
                    state = INIT;
                    return token(FortranTokenId.IDENTIFIER);
                case IN_BINARY:
                    state = INIT;
                    return token(FortranTokenId.ERR_INVALID_BINARY_LITERAL);
                case IN_OCTAL:
                    state = INIT;
                    return token(FortranTokenId.ERR_INVALID_OCTAL_LITERAL);
                case IN_HEX:
                    state = INIT;
                    return token(FortranTokenId.ERR_INVALID_HEX_LITERAL);
                case IN_STRING:
                case IN_STRING_AFTER_BSLASH:
                    return token(FortranTokenId.STRING_LITERAL); // hold the state
                case AFTER_SLASH:
                    state = INIT;
                    return token(FortranTokenId.OP_DIV);
                case AFTER_EQ:
                    state = INIT;
                    return token(FortranTokenId.EQ);
                case AFTER_STAR:
                    state = INIT;
                    return token(FortranTokenId.OP_MUL);
                case IN_LINE_COMMENT:
                    if (lineCommentFree) {
                        return token(FortranTokenId.LINE_COMMENT_FREE);
                    } else {
                        return token(FortranTokenId.LINE_COMMENT_FIXED);
                    }
                case AFTER_LESSTHAN:
                    state = INIT;
                    return token(FortranTokenId.OP_LT);
                case AFTER_GREATERTHAN:
                    state = INIT;
                    return token(FortranTokenId.OP_GT);
                case IN_INT:
                    state = INIT;
                    return token(FortranTokenId.NUM_LITERAL_INT);
                case IN_REAL:
                    state = INIT;
                    return token(FortranTokenId.NUM_LITERAL_REAL);
                case AFTER_DOT:
                    state = INIT;
                    return token(FortranTokenId.DOT);
            } //switch
        }

        /* At this stage there's no more text in the scanned buffer, but
         * this buffer is not the last so the scan will continue on another
         * buffer. The scanner tries to minimize the amount of characters
         * that will be prescanned in the next buffer by returning the token
         * where possible.
         */
        switch (state) {
            case IN_WHITESPACE:
                return token(FortranTokenId.WHITESPACE);
        }

        return null; // nothing found
    }

    /**
     * This function reads new symbol
     */
    protected final int read() {
        int c = input.read();
        if (c == '\t') {
            if (lineColomn < 5) {
                lineColomn = 5;
            }
        }
        lineColomn++;
        return c;
    }

    /**
     * This function puts last n symbols back
     */
    protected final void backup(int n, int lastChar) {
        if (lastChar == '\t') {
            if (lineColomn == 6) {
                lineColomn = n;
            }
        }
        lineColomn -= n;
        input.backup(n);
    }

    /**
     * This function returns true if the column number
     * exceeds the limit defined by FSettingsDefaults.maximumTextWidth
     * otherwise it returns null
     */
    private boolean isLineBeyondLimit() {
        if ((lineColomn > maximumTextWidth) &&
                (state != IN_LINE_COMMENT)) {
            lineCommentFree = true;
            state = IN_LINE_COMMENT;
            return true;
        }

        return false;
    }

    /**
     * This function recognizes keywords and identifiers
     */
    private Token<FortranTokenId> keywordOrIdentifier(int c) {
        StringBuilder idText = new StringBuilder();
        idText.append(Character.toLowerCase((char) c));
        while (true) {
            c = read();
            if (c == EOF || !CndLexerUtilities.isFortranIdentifierPart(c) || isLineBeyondLimit()) {
                // For surrogate 2 chars must be backed up
                backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1, c);
                FortranTokenId id = getKeywordOrIdentifierID(idText.toString());
                assert id != null : "must be valid id for " + idText;
                return token(id);
            } else {
                idText.append(Character.toLowerCase((char) c));
            }
        }
    }

    /**
     * This function recognizes keyword-operators
     */
    private Token<FortranTokenId> keywordOperator(int c) {
        int readSymbolsNumber = 0;
        StringBuilder idText = new StringBuilder();
        idText.append(Character.toLowerCase((char) c));
        while (true) {
            c = read();
            readSymbolsNumber++;
            if (c == '.') {
                idText.append(Character.toLowerCase((char) c));
                FortranTokenId id = getKeywordOperatorID(idText.toString());
                if(id != null) {
                    return token(id);
                } else {
                    backup(readSymbolsNumber, c);
                    return null;
                }
            } else if (c == EOF || !CndLexerUtilities.isFortranIdentifierPart(c) || isLineBeyondLimit()) {
                backup(readSymbolsNumber, c);
                return null;
            } else {
                idText.append(Character.toLowerCase((char) c));
            }
        }
    }
    /**
     * This function says is char sequence keyword or identifier
     */
    private FortranTokenId getKeywordOrIdentifierID(CharSequence text) {
        FortranTokenId id = lexerFilter.check(text);
        return id != null ? id : FortranTokenId.IDENTIFIER;
    }

    /**
     * This function says is char sequence keyword operator
     */
    private FortranTokenId getKeywordOperatorID(CharSequence text) {
        return lexerFilter.check(text);
    }

    /**
     * This function recognizes preprocessor directives
     */
    @SuppressWarnings("fallthrough")
    protected Token<FortranTokenId> finishSharp() {
        // one prerpocessor directive block
        while (true) {
            switch (read()) {
                case '\r':
                    input.consumeNewline();
                    // nobreak
                case '\n':
                case EOF:
                    return token(FortranTokenId.PREPROCESSOR_DIRECTIVE);
            }
        }
    }

    /**
     * Creates token
     */
    protected final Token<FortranTokenId> token(FortranTokenId id) {
        return token(id, id.fixedText(), PartType.COMPLETE);
    }

    /**
     * Creates token
     */
    protected final Token<FortranTokenId> tokenPart(FortranTokenId id, PartType part) {
        return token(id, null, part);
    }

    /**
     * Creates token
     */
    private Token<FortranTokenId> token(FortranTokenId id, String fixedText, PartType part) {
        assert id != null : "id must be not null";
        Token<FortranTokenId> token;
        if (fixedText != null) {
            // create flyweight token
            token = tokenFactory.getFlyweightToken(id, fixedText);
        } else {
            if (part != PartType.COMPLETE) {
                token = tokenFactory.createToken(id, input.readLength(), part);
            } else {
                token = tokenFactory.createToken(id);
            }
        }
        assert token != null : "token must be created as result for " + id;
        return token;
    }

    /**
     * State of lexing process
     */
    private static class State {
        // State of lexer

        private int lexerState;
        // Position on the line
        private int lineColomn;
    }

    private State getState() {
        if (state != INIT || lineColomn != 0) {
            State s = new State();
            s.lexerState = state;
            s.lineColomn = lineColomn;
            return s;
        } else {
            return null;
        }
    }

    private void setState(State s) {
        if (s != null) {
            state = s.lexerState;
            lineColomn = s.lineColomn;
        } else {
            state = INIT;
            lineColomn = 0;
        }
    }
}
