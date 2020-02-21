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
import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for C/C++ string language.
 * based on JavaStringLexer
 *
 * @version 1.00
 */

public class CppStringLexer implements Lexer<CppStringTokenId> {
    private static final int INIT   = 0;
    private static final int OTHER  = 1;
    private static final int PREFIX  = 2;
    private static final int START_DELIMETER  = 3;
    private static final int AFTER_START_DELIMETER  = 4;
    private static final int END_DELIMETER  = 5;
    private static final int AFTER_END_DELIMETER  = 6;

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;

    private TokenFactory<CppStringTokenId> tokenFactory;
    private boolean escapedLF = false;
    private final boolean dblQuoted;
    private final boolean rawString;
    private int state = INIT;
    private String rawDelimeter = null;
    private static final class RawStringLexerState {
        private final int state;
        private final String delimeter;

        public RawStringLexerState(int state, String delimeter) {
            this.state = state;
            this.delimeter = delimeter;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + this.state;
            hash = 67 * hash + (this.delimeter != null ? this.delimeter.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RawStringLexerState other = (RawStringLexerState) obj;
            if (this.state != other.state) {
                return false;
            }
            if ((this.delimeter == null) ? (other.delimeter != null) : !this.delimeter.equals(other.delimeter)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "RAW_STR{" + "state=" + state + ", delimeter=" + delimeter + '}'; // NOI18N
        }
    }
    
    public CppStringLexer(LexerRestartInfo<CppStringTokenId> info, boolean doubleQuotedString, boolean raw) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.dblQuoted = doubleQuotedString;
        this.rawString = raw;
        fromState(info.state()); // last line in contstructor
    }

    @Override
    public Object state() {
        if (rawString) {
            if (this.state == INIT) {
                return null;
            } else if (this.state == PREFIX || this.state == START_DELIMETER) {
                return Integer.valueOf(state);
            } else if (this.rawDelimeter != null && this.state == OTHER) {
                return this.rawDelimeter;
            } else {
                return new RawStringLexerState(state, rawDelimeter);
            }
        } else {
            return Integer.valueOf(state);
        }
    }

    private void fromState(Object state) {
        if (state == null) {
            this.state = INIT;
            return;
        }
        if (rawString) {
            if (state instanceof Integer) {
                this.state = ((Integer)state).intValue();
                this.rawDelimeter = null;
            } else if (state instanceof String) {
                this.state = OTHER;
                this.rawDelimeter = (String) state;
            } else {
                RawStringLexerState lexerState = (RawStringLexerState) state;
                this.state = lexerState.state;
                this.rawDelimeter = lexerState.delimeter;
            }
        } else {
            this.state = ((Integer)state).intValue();
        }
    }

    @Override
    public Token<CppStringTokenId> nextToken() {
        int startState = state;
        state = OTHER;
        while(true) {
            int ch;
            if (startState == START_DELIMETER) {
                assert rawString : "this state is valid only for raw strings";
                ch = input.read();
                StringBuilder delim = new StringBuilder();
                while (isRawStringDelimeterCharacter(ch)) {
                    delim.append((char) ch);
                    ch = input.read();
                }
                if (ch == '(') {
                    rawDelimeter = delim.toString();
                    if (rawDelimeter.length() > 0) {
                        input.backup(1);
                        state = AFTER_START_DELIMETER;
                        return token(CppStringTokenId.START_DELIMETER);
                    } else {
                        return token(CppStringTokenId.START_DELIMETER_PAREN);
                    }
                } else {
                    // failed to get delimeter, need to recover
                    // we backup 1 or 2 symbols: one for the last non-delim ch 
                    // and 
                    // one for possible closing dbl-quote if was stored in delim
                    if (delim.length() > 0 && delim.charAt(delim.length()-1) == '\"') {
                        input.backup(2);
                    } else {
                        input.backup(1);
                    }
                    if (input.readLength() > 0) {
                        state = AFTER_END_DELIMETER;
                        return token(CppStringTokenId.TEXT, null, PartType.MIDDLE);
                    } else {
                        state = OTHER;
                        startState = AFTER_END_DELIMETER;
                        continue;
                    }
                }
            } else if (startState == END_DELIMETER) {
                int read = input.read();
                for (int i = 0; i < rawDelimeter.length(); i++) {
                    assert (read == rawDelimeter.charAt(i));
                    read = input.read();
                }
                assert read == '"';
                if (rawDelimeter.length() > 0) {
                    input.backup(1);
                    state = AFTER_END_DELIMETER;
                    return token(CppStringTokenId.END_DELIMETER);
                } else {
                    return token(CppStringTokenId.LAST_QUOTE);
                }
            } else {
                ch = read();
            }
            switch (ch) {
                case '(':
                    if (rawString && startState == AFTER_START_DELIMETER) {
                        return token(CppStringTokenId.START_DELIMETER_PAREN);
                    }
                    break;
                case ')':
                    if (rawString && startState == OTHER && rawDelimeter != null) {
                        if (input.readLength() > 1) {
                            input.backup(1);
                            // return what already had
                            return token(CppStringTokenId.TEXT);
                        }
                        // try to find end delimeter of raw string
                        int backup = 1;
                        int read = input.read();
                        boolean ok = true;
                        for (int i = 0; i < rawDelimeter.length(); i++) {
                            if (read == rawDelimeter.charAt(i)) {
                                read = input.read();
                                backup++;
                            } else {
                                ok = false;
                                break;
                            }
                        }
                        if (read == '"') {
                            if (ok) {
                                input.backup(backup);
                                state = END_DELIMETER;
                                return token(CppStringTokenId.END_DELIMETER_PAREN);
                            } else {
                                return token(CppStringTokenId.TEXT);
                            }
                        }
                        if (read == EOF) {
                            return token(CppStringTokenId.TEXT, null, PartType.START);
                        }
                        if (read == ')') {
                            // return what already had, but put ')' back to have next round of delimeter check
                            input.backup(1);
                            return token(CppStringTokenId.TEXT);
                        }
                    }
                    break;
                case 'L':
                    if (startState == INIT) {
                        state = PREFIX;
                        int next = read();
                        if (next == 'R') {
                            assert rawString;
                            return token(CppStringTokenId.PREFIX_LR);
                        } else {
                            input.backup(1);
                            return token(CppStringTokenId.PREFIX_L);
                        }                        
                    }
                    break;
                case 'U':
                    if (startState == INIT) {
                        state = PREFIX;
                        int next = read();
                        if (next == 'R') {
                            assert rawString;
                            return token(CppStringTokenId.PREFIX_UR);
                        } else {
                            input.backup(1);
                            return token(CppStringTokenId.PREFIX_U);
                        }
                    }
                    break;
                case 'u':
                    if (startState == INIT) {
                        state = PREFIX;
                        int next = read();
                        if (next == '8') {
                            next = read();
                            if (next == 'R') {
                                assert rawString;
                                return token(CppStringTokenId.PREFIX_u8R);
                            } else {
                                input.backup(1);
                                return token(CppStringTokenId.PREFIX_u8);
                            }
                        } else if (next == 'R') {
                            assert rawString;
                            return token(CppStringTokenId.PREFIX_uR);
                        } else {
                            input.backup(1);
                            return token(CppStringTokenId.PREFIX_u);
                        }
                    }
                    break;
                case 'R':
                    if (startState == INIT) {
                        state = PREFIX;
                        assert rawString;
                        return token(CppStringTokenId.PREFIX_R);
                    }
                    break;
                case EOF:
                    if (input.readLength() > 0) {
                        return token(CppStringTokenId.TEXT);
                    } else {
                        return null;
                    }
                case '\'':
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return token(CppStringTokenId.TEXT);
                    }
                    return token(CppStringTokenId.SINGLE_QUOTE);
                case '"':
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return token(CppStringTokenId.TEXT);
                    }
                    if (this.dblQuoted) {
                        if (startState == PREFIX || startState == INIT) {
                            if (rawString) {
                                state = START_DELIMETER;
                            }
                            return token(CppStringTokenId.FIRST_QUOTE);
                        } else {
                            if (!rawString || startState == AFTER_END_DELIMETER) {
                                return token(CppStringTokenId.LAST_QUOTE);
                            } else {
                                return token(CppStringTokenId.DOUBLE_QUOTE);
                            }
                        }
                    } else {
                        return token(CppStringTokenId.DOUBLE_QUOTE);
                    }
                case '\\': //NOI18N
                    if (rawString) {
                        continue;
                    }
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return token(CppStringTokenId.TEXT);
                    }
                    switch (ch = read()) {
                        case 'a': //NOI18N
                            return token(CppStringTokenId.BELL);
                        case 'b': //NOI18N
                            return token(CppStringTokenId.BACKSPACE);
                        case 'e': //NOI18N
                            return token(CppStringTokenId.ANSI_COLOR);
                        case 'f': //NOI18N
                            return token(CppStringTokenId.FORM_FEED);
                        case 'n': //NOI18N
                            return token(CppStringTokenId.NEWLINE);
                        case 'r': //NOI18N
                            return token(CppStringTokenId.CR);
                        case 't': //NOI18N
                            return token(CppStringTokenId.TAB);
                        case '\'': //NOI18N
                            return token(CppStringTokenId.SINGLE_QUOTE_ESCAPE);
                        case '"': //NOI18N
                            return token(CppStringTokenId.DOUBLE_QUOTE_ESCAPE);
                        case '\\': //NOI18N
                            return token(CppStringTokenId.BACKSLASH_ESCAPE);
                       case 'u': //NOI18N
                            while ('u' == (ch = read())) {}; //NOI18N

                            for(int i = 0; ; i++) {
                                ch = Character.toLowerCase(ch);

                                if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'f')) { //NOI18N
                                    input.backup(1);
                                    return token(CppStringTokenId.UNICODE_ESCAPE_INVALID);
                                }

                                if (i == 3) { // four digits checked, valid sequence
                                    return token(CppStringTokenId.UNICODE_ESCAPE);
                                }

                                ch = read();
                            }
                        case 'x': // NOI18N
                        {
                            int len = 0;
                            while (true) {
                                switch (read()) {
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                    case 'a':
                                    case 'b':
                                    case 'c':
                                    case 'd':
                                    case 'e':
                                    case 'f':
                                    case 'A':
                                    case 'B':
                                    case 'C':
                                    case 'D':
                                    case 'E':
                                    case 'F':
                                        len++;
                                        break;
                                    default:
                                        input.backup(1);
                                        // if float then before mandatory binary exponent => invalid
                                        return token(len > 0 ? CppStringTokenId.HEX_ESCAPE : CppStringTokenId.HEX_ESCAPE_INVALID);
                                }
                            } // end of while(true)      
                        }
                        case '0': case '1': case '2': case '3': //NOI18N
                            switch (read()) {
                                case '0': case '1': case '2': case '3': //NOI18N
                                case '4': case '5': case '6': case '7': //NOI18N
                                    switch (read()) {
                                        case '0': case '1': case '2': case '3': //NOI18N
                                        case '4': case '5': case '6': case '7': //NOI18N
                                            return token(CppStringTokenId.OCTAL_ESCAPE);
                                    }
                                    input.backup(1);
                                    return token(CppStringTokenId.OCTAL_ESCAPE);
                            }
                            input.backup(1);
                            return token(CppStringTokenId.OCTAL_ESCAPE);
                    }
                    input.backup(1);
                    return token(CppStringTokenId.ESCAPE_SEQUENCE_INVALID);
            } // end of switch (ch)
        } // end of while(true)
    }

    protected final Token<CppStringTokenId> token(CppStringTokenId id) {
        return token(id, id.fixedText(), PartType.COMPLETE);
    }

    private Token<CppStringTokenId> token(CppStringTokenId id, String fixedText, PartType part) {
        assert id != null : "id must be not null";
        Token<CppStringTokenId> token;
        if (fixedText != null && !escapedLF) {
            // create flyweight token
            token = tokenFactory.getFlyweightToken(id, fixedText);
        } else {
            if (part != PartType.COMPLETE) {
                token = tokenFactory.createToken(id, input.readLength(), part);
            } else {
                token = tokenFactory.createToken(id);
            }
        }
        escapedLF = false;
        assert token != null : "token must be created as result for " + id;
        return token;
    }

    @SuppressWarnings("fallthrough")
    protected final int read() {
        boolean skipEscapedLF = true;
        int c = input.read();
        if (skipEscapedLF) { // skip escaped LF
            int next;
            while (c == '\\') {
                switch (input.read()) {
                    case '\r':
                        input.consumeNewline();
                        // nobreak
                    case '\n':
                        escapedLF = true;
                        next = input.read();
                        break;
                    default:
                        input.backup(1);
                        assert c == '\\' : "must be backslash " + (char)c;
                        return c; // normal backslash, not escaped LF
                }
                c = next;
            }
        }
        return c;
    }

    @Override
    public void release() {
    }

    private enum RawStringLexingState {
        PREFIX_DELIMETER,
        BODY,
        POSTFIX_DELIMETER,
        ERROR
    }

    protected static PartType finishRawString(LexerInput input) {
        RawStringLexingState state = RawStringLexingState.PREFIX_DELIMETER;
        StringBuilder delim = new StringBuilder("");
        String delimeter = "";
        while (true) {
            int read = input.read();
            switch (state) {
                case PREFIX_DELIMETER: {
                    if (isRawStringDelimeterCharacter(read)) {
                        delim.append((char) read);
                        break;
                    }
                    if (read == '(') {
                        delimeter = delim.toString();
                        state = RawStringLexingState.BODY;
                    } else if (read == EOF) {
                        return PartType.START;
                    } else {
                        // return back non-delim character for better recover
                        input.backup(1);
                        state = RawStringLexingState.ERROR;
                    }
                    break;
                }
                case BODY: {
                    if (read == EOF) {
                        return PartType.START;
                    }
                    if (read == ')') {
                        state = RawStringLexingState.POSTFIX_DELIMETER;
                    }
                    break;
                }
                case POSTFIX_DELIMETER: {
                    boolean ok = true;
                    // characters after closing ')' met in body should be as in start delimeter
                    for (int i = 0; i < delimeter.length(); i++) {
                        if (delimeter.charAt(i) == (char) read) {
                            read = input.read();
                        } else {
                            ok = false;
                            break;
                        }
                    }
                    if (read == '"' && ok) {
                        return PartType.COMPLETE;
                    }
                    if (read == EOF) {
                        return PartType.START;
                    }
                    if (read == ')') {
                        // next round to detect postfix delimeter
                        state = RawStringLexingState.POSTFIX_DELIMETER;
                    } else {
                        // it still was a body
                        state = RawStringLexingState.BODY;
                    }
                    break;
                }
                case ERROR: {
                    // incorrect delimeter, try to recover
                    switch (read) {
                        case '"': // NOI18N
                            return PartType.START;
                        case '\r':
                        case '\n':
                        case ' ':
                        case '\t':
                            input.backup(1);
                            return PartType.START;
                        case EOF:
                            return PartType.START;
                    }
                }

            }
        }
    }

    private static boolean isRawStringDelimeterCharacter(int c) {
        switch (c) {
            case '.':
            // {}[]#<>%:;?*+-/^&|~!=,"'
            case '{':
            case '}':
            case '[':
            case ']':
            case '#':
            case '<':
            case '>':
            case '%':
            case ':':
            case ';':
            case '?':
            case '*':
            case '+':
            case '-':
            case '/':
            case '^':
            case '&':
            case '|':
            case '~':
            case '!':
            case '=':
            case ',':
            case '"':
            case '\'':
                return true;
            default:
                return CndLexerUtilities.isCppIdentifierPart(c);
        }
    }
}
