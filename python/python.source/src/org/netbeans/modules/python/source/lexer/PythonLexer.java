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
package org.netbeans.modules.python.source.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexer for Python.
 *
 * This is a hand written lexer for Python which recognizes the logical token types
 * we care about within the IDE support.
 *
 * Initially, we were using Jython's lexer directly here. However, that had some
 * problems:
 *
 * <ul>
 *   <li>
 *     In the IDE, we have to support incremental parsing. Not only is this a must
 *     from a user performance perspective (typing a keystroke near the bottom of a
 *     2,000 line file shouldn't cause complete re-lexing of the entire document), but
 *     the NetBeans APIs require it; they restart the lexer on the nearest token
 *     boundary.
 *     ANTLR doesn't support incremental lexing. I looked into what it would take
 *     to patch it to do so (as I have done for JRuby in the Ruby support), but
 *     it (a) wasn't trivial, and (b) would require a LOT of data to be stored for
 *     every token boundary.
 *   </li>
 *   <li>
 *     Similarly, we need to put our lexer on top of a LexerInput, and it is not
 *     particularly compatible with the way ANTLR handles input. We had an
 *     adapter class for this which was jumping through various hoops to expose
 *     the LexerInput as ANTLR input. However, it had some severe problems with
 *     large inputs.
 *   </li>
 *   <li>
 *     We need slightly different token divisions. For example, for matching bracket
 *     purposes, we'd like to have strings split into string delimiters and the
 *     string contents. Similarly, Jython did some things like coalesce all whitespace
 *     around a newline into that newline token which causes some complications
 *     for our token analysis.
 *   </li>
 *   <li>
 *     For Ruby, I decided to use the JRuby lexer because JRuby lexing is very
 *     difficult. They have a huge complicated class to do the lexing - and there's
 *     no Ruby language spec.  Python on the other hand seems to have a very simple
 *     lexing model, and a clear spec and grammar, so I don't feel worried that
 *     our custom Python lexer is going to have a lot of corner case bugs.
 *   </li>
 * </ul>
 *
 */
public final class PythonLexer implements Lexer<PythonTokenId> {
    public static final String COMMENT_CAT = "comment";
    public static final String KEYWORD_CAT = "keyword"; // NOI18N
    public static final String STRING_CAT = "string"; // NOI18N
    public static final String WHITESPACE_CAT = "whitespace"; // NOI18N
    public static final String OPERATOR_CAT = "operator"; // NOI18N
    public static final String SEPARATOR_CAT = "separator"; // NOI18N
    public static final String ERROR_CAT = "error"; // NOI18N
    public static final String NUMBER_CAT = "number"; // NOI18N
    public static final String IDENTIFIER_CAT = "identifier"; // NOI18N
    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final TokenFactory<PythonTokenId> tokenFactory;

    // Lexer state - preserved per token boundary
    private enum State {
        /** Normal state, same state as on entry into a Python file */
        INIT,
        /** We've processed the beginning string delimiter of a double-quoted short string */
        BEGIN_SHORTSTRING_DOUBLE,
        /** We've processed the beginning string delimiter of a single-quoted short string */
        BEGIN_SHORTSTRING_SINGLE,
        /** We've processed the beginning string delimiter of a double-quoted long string */
        BEGIN_LONGSTRING_DOUBLE,
        /** We've processed the beginning string delimiter of a singl-quoted long string */
        BEGIN_LONGSTRING_SINGLE,
        /** We've processed the string content in a double-quoted short string */
        END_SHORTSTRING_DOUBLE,
        /** We've processed the string content in a single-quoted short string */
        END_SHORTSTRING_SINGLE,
        /** We've processed the string content in a double-quoted long string */
        END_LONGSTRING_DOUBLE,
        /** We've processed the string content in a single-quoted long string */
        END_LONGSTRING_SINGLE,
    };
    private State state;

    public PythonLexer(LexerRestartInfo<PythonTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();

        state = (State)info.state();
        if (state == null) {
            state = State.INIT;
        }
    }

    @Override
    public Object state() {
        return state;
    }

    private Token<PythonTokenId> createToken(PythonTokenId id, int tokenLength) {
        String fixedText = id.fixedText();
        return (fixedText != null) ? tokenFactory.getFlyweightToken(id, fixedText)
                : tokenFactory.createToken(id, tokenLength);
    }

    @SuppressWarnings("fallthrough")
    @Override
    public Token<PythonTokenId> nextToken() {
        switch (state) {
        case INIT: {
            int ch = input.read();
            switch (ch) {
            case EOF:
                return null;

            // Newline
            case '\n':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.NEWLINE, 1);

            // Whitespace
            case ' ':
            case '\t': {
                for (; ch != EOF; ch = input.read()) {
                    if (ch != ' ' && ch != '\t') {
                        break;
                    }
                }
                input.backup(1);
                return createToken(PythonTokenId.WHITESPACE, input.readLength());
            }

            // Comment
            case '#': {
                ch = input.read();
                while (ch != EOF && ch != '\n') {
                    ch = input.read();
                }
                input.backup(1);
                return createToken(PythonTokenId.COMMENT, input.readLength());
            }

            case '.': {
                assert input.readLength() == 1;
                int peek = input.read();
                input.backup(1);
                if (!Character.isDigit(peek)) {
                    return createToken(PythonTokenId.DOT, 1);
                } // else: Fallthrough to process the number!!
            } // FALLTHROUGH
            // FALLTHROUGH!!!!

            // Number (integer, float, complex)
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9': {
                if (ch == '0') {
                    int peek = input.read();
                    if (peek == 'x' || peek == 'X') {
                        // Hex
                        ch = input.read();
                        while (ch != EOF) {
                            if (!(Character.isDigit(ch) ||
                                    (ch >= 'a' && ch <= 'f') ||
                                    (ch >= 'A' && ch <= 'F'))) {
                                break;
                            }
                            ch = input.read();
                        }
                        if (ch != 'l' && (ch != 'L')) {
                            input.backup(1);
                        }
                        return createToken(PythonTokenId.INT_LITERAL, input.readLength());
                    }
                    input.backup(1);
                }
                boolean isFloat = false;
                digitLoop:
                for (; ch != EOF; ch = input.read()) {
                    switch (ch) {
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
                        continue;
                    case '.':
                        isFloat = true;
                        continue;
                    case 'e': // Exponent
                    case 'E': {
                        int peek = input.read();
                        if (peek != '+' && peek != '-') {
                            input.backup(1);
                        }
                        ch = input.read();
                        while (ch != EOF) {
                            if (!Character.isDigit(ch)) {
                                break;
                            }
                            ch = input.read();
                        }
                        if (ch != 'j' && ch != 'J') {
                            input.backup(1);
                        }
                        return createToken(PythonTokenId.FLOAT_LITERAL, input.readLength());
                    }
                    case 'j': // Imaginary
                    case 'J':
                        isFloat = true;
                        break digitLoop;
                    case 'l': // Long
                    case 'L':
                        break digitLoop;
                    case EOF:
                    default:
                        input.backup(1);
                        break digitLoop;

                    }
                }

                return createToken(isFloat ? PythonTokenId.FLOAT_LITERAL : PythonTokenId.INT_LITERAL, input.readLength());
            }

            // Operators and delimiters
            case '+': // +,+=
                if (input.read() != '=') {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            case '-': // -,-=
                if (input.read() != '=') {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            case '*': { // *,**,*=, **=
                int peek = input.read();
                if (peek == '=') {
                    // No need to back up, include it
                } else if (peek == '*') {
                    peek = input.read();
                    if (peek != '=') {
                        input.backup(1);
                    }
                } else {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            }
            case '/': {
                // Look for /,//, /=, //=
                int peek = input.read();
                if (peek == '=') {
                    // No need to back up, include it
                } else if (peek == '/') {
                    peek = input.read();
                    if (peek != '=') {
                        input.backup(1);
                    }
                } else {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            }
            case '%': { // Look for %,   %=
                if (input.read() != '=') {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            }
            case '<': {
                // Look for <, <<, <=, <>, <<=
                int peek = input.read();
                if (peek == '=') {
                    // No need to back up, include it
                } else if (peek == '<') {
                    peek = input.read();
                    if (peek != '=') {
                        input.backup(1);
                    }
                } else if (peek != '>') {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            }
            case '>': {
                // Look for >, >>, >=, >>=
                int peek = input.read();
                if (peek == '=') {
                    // No need to back up, include it
                } else if (peek == '>') {
                    peek = input.read();
                    if (peek != '=') {
                        input.backup(1);
                    }
                } else {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            }
            case '&': { // Look for &,&=
                if (input.read() != '=') {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            }
            case '|': { // Look for |, |=
                if (input.read() != '=') {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            }
            case '^': { // ^,^=
                if (input.read() != '=') {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            }
            case '=': {
                // Look for =,==
                if (input.read() != '=') {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            }
            case '!': {
                // Look for !=
                if (input.read() != '=') {
                    input.backup(1);
                }
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());
            }
            case '~':
            case '`':
            case ';':
                return createToken(PythonTokenId.ANY_OPERATOR, input.readLength());

            case ':':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.COLON, 1);
            case '(':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.LPAREN, 1);
            case ')':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.RPAREN, 1);
            case '[':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.LBRACKET, 1);
            case ']':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.RBRACKET, 1);
            case '{':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.LBRACE, 1);
            case '}':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.RBRACE, 1);
            case ',':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.COMMA, 1);
            case '\\':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.ESC, 1);

            case '$':
            case '?':
                assert input.readLength() == 1;
                return createToken(PythonTokenId.ERROR, 1);

            // String?
            case '\'':
            case '"': {
                int peek = input.read();
                if (peek != ch) {
                    input.backup(1);
                    assert input.readLength() == 1;
                    state = (ch == '"') ? State.BEGIN_SHORTSTRING_DOUBLE : State.BEGIN_SHORTSTRING_SINGLE;
                    return createToken(PythonTokenId.STRING_BEGIN, 1);
                }
                // We've seen two quotes... it's either an empty string,
                // or the beginning of a longstring
                int peek2 = input.read();
                if (peek2 == peek) {
                    // It's a longstring!
                    assert input.readLength() == 3;
                    state = (ch == '"') ? State.BEGIN_LONGSTRING_DOUBLE : State.BEGIN_LONGSTRING_SINGLE;
                    return createToken(PythonTokenId.STRING_BEGIN, 3);
                } else {
                    input.backup(2);
                    assert input.readLength() == 1;
                    state = (ch == '"') ? State.BEGIN_SHORTSTRING_DOUBLE : State.BEGIN_SHORTSTRING_SINGLE;
                    return createToken(PythonTokenId.STRING_BEGIN, 1);
                }
            }
            case '@': { // Decorator
                // Identifier or keyword?
                ch = input.read();
                if (Character.isJavaIdentifierStart(ch)) {
                    while (ch != EOF && Character.isJavaIdentifierPart(ch) && ch != '$') {
                        ch = input.read();
                    }
                    input.backup(1);

                    return createToken(PythonTokenId.DECORATOR, input.readLength());
                }
                input.backup(1); // Remove the peeked char

                assert input.readLength() == 1;
                return createToken(PythonTokenId.DECORATOR, 1);
            }

            case 'r':
            case 'R':
            case 'u':
            case 'U': {
                // Digest the "u" and the "r" and position the input
                // before the following ' or "
                boolean isStringPrefix = false;
                int peek = input.read();
                if (ch == 'r' || ch == 'R') {
                    if (peek == '\'' || peek == '"') {
                        isStringPrefix = true;
                    }
                    input.backup(1);
                } else {
                    assert ch == 'u' || ch == 'U';
                    if (peek == 'r' || peek == 'R') {
                        int peek2 = input.read();
                        if (peek2 == '\'' || peek2 == '"') {
                            isStringPrefix = true;
                        }
                        input.backup(1);
                    } else if (peek == '\'' || peek == '"') {
                        isStringPrefix = true;
                        input.backup(1);
                    }
                    if (!isStringPrefix) {
                        input.backup(1);
                    }
                }
                if (isStringPrefix) {
                    ch = input.read();
                    assert ch == '\'' || ch == '"';

                    peek = input.read();
                    if (peek != ch) {
                        input.backup(1);
                        state = (ch == '"') ? State.BEGIN_SHORTSTRING_DOUBLE : State.BEGIN_SHORTSTRING_SINGLE;
                        return createToken(PythonTokenId.STRING_BEGIN, input.readLength());
                    }
                    // We've seen two quotes... it's either an empty string,
                    // or the beginning of a longstring
                    int peek2 = input.read();
                    if (peek2 == peek) {
                        // It's a longstring!
                        state = (ch == '"') ? State.BEGIN_LONGSTRING_DOUBLE : State.BEGIN_LONGSTRING_SINGLE;
                        return createToken(PythonTokenId.STRING_BEGIN, input.readLength());
                    } else {
                        input.backup(2);
                        state = (ch == '"') ? State.BEGIN_SHORTSTRING_DOUBLE : State.BEGIN_SHORTSTRING_SINGLE;
                        return createToken(PythonTokenId.STRING_BEGIN, input.readLength());
                    }
                }// else: FALLTHROUGH!!! The "u" or "r" is probably an identifier prefix!!
            }
            // Fallthrough...

            default: {
                // Identifier or keyword?
                if (Character.isJavaIdentifierStart(ch)) {
                    while (ch != EOF && Character.isJavaIdentifierPart(ch) && ch != '$') {
                        ch = input.read();
                    }
                    input.backup(1);

                    // See if it's a keyword
                    PythonTokenId pid = getKeywordToken(input.readText());
                    if (pid != null) {
                        return createToken(pid, input.readLength());
                    } else {
                        return createToken(PythonTokenId.IDENTIFIER, input.readLength());
                    }
                }

                assert input.readLength() == 1;
                return createToken(PythonTokenId.ANY_OPERATOR, 1);
            }
            }
        }

        case BEGIN_LONGSTRING_SINGLE:
        case BEGIN_LONGSTRING_DOUBLE: {
            // In a long string. Look for the end.
            int ch = input.read();
            if (ch == EOF) {
                return null;
            }
            int term = (state == State.BEGIN_LONGSTRING_DOUBLE) ? '"' : '\'';
            while (ch != EOF) {
                if (ch == '\\') {
                    // It's an escape - read escaped char
                    input.read();
                } else if (ch == term) {
                    int peek = input.read();
                    if (peek == term) {
                        int peek2 = input.read();
                        if (peek2 == term) {
                            // Found the end
                            if (input.readLength() == 3) {
                                // Empty string - go straight to closed state
                                state = State.INIT;
                                return createToken(PythonTokenId.STRING_END, input.readLength());
                            }
                            input.backup(3);
                            if (state == State.BEGIN_LONGSTRING_DOUBLE) {
                                state = State.END_LONGSTRING_DOUBLE;
                            } else {
                                assert state == State.BEGIN_LONGSTRING_SINGLE;
                                state = State.END_LONGSTRING_SINGLE;
                            }
                            return createToken(PythonTokenId.STRING_LITERAL, input.readLength());
                        }
                        input.backup(1);
                    }
                    input.backup(1);
                }
                ch = input.read();
            }
            // Literal not terminated
            state = State.INIT;
            return createToken(PythonTokenId.ERROR, input.readLength());
        }
        case BEGIN_SHORTSTRING_SINGLE:
        case BEGIN_SHORTSTRING_DOUBLE: {
            // In a short string. Look for the end.
            int ch = input.read();
            if (ch == EOF) {
                return null;
            }
            int term = (state == State.BEGIN_SHORTSTRING_DOUBLE) ? '"' : '\'';
            while (ch != EOF) {
                if (ch == '\\') {
                    // It's an escape - read escaped char
                    input.read();
                } else if (ch == '\n') {
                    // Literal not terminated
                    state = State.INIT;
                    return createToken(PythonTokenId.ERROR, input.readLength());
                } else if (ch == term) {
                    if (input.readLength() == 1) {
                        // It's an empty string! Skip straight to the end state
                        state = State.INIT;
                        return createToken(PythonTokenId.STRING_END, input.readLength());
                    }
                    input.backup(1);
                    if (state == State.BEGIN_SHORTSTRING_DOUBLE) {
                        state = State.END_SHORTSTRING_DOUBLE;
                    } else {
                        assert state == State.BEGIN_SHORTSTRING_SINGLE;
                        state = State.END_SHORTSTRING_SINGLE;
                    }
                    return createToken(PythonTokenId.STRING_LITERAL, input.readLength());
                }
                ch = input.read();
            }
            // Literal not terminated
            state = State.INIT;
            return createToken(PythonTokenId.ERROR, input.readLength());
        }

        case END_LONGSTRING_SINGLE:
        case END_LONGSTRING_DOUBLE: {
            // In a long string. Look for the end.
            int ch = input.read();
            if (ch == EOF) {
                return null;
            }
            int term = (state == State.END_LONGSTRING_DOUBLE) ? '"' : '\'';
            while (ch != EOF) {
                if (ch == term) {
                    int peek = input.read();
                    if (peek == term) {
                        int peek2 = input.read();
                        if (peek2 == term) {
                            // Found the end
                            state = State.INIT;
                            return createToken(PythonTokenId.STRING_END, input.readLength());
                        }
                        input.backup(1);
                    }
                    input.backup(1);
                }
                ch = input.read();
            }
            // Literal not terminated
            state = State.INIT;
            return createToken(PythonTokenId.ERROR, input.readLength());
        }
        case END_SHORTSTRING_SINGLE:
        case END_SHORTSTRING_DOUBLE: {
            // In a short string. Look for the end.
            int ch = input.read();
            if (ch == EOF) {
                return null;
            }
            int term = (state == State.END_SHORTSTRING_DOUBLE) ? '"' : '\'';
            if (ch == term) {
                state = State.INIT;
                return createToken(PythonTokenId.STRING_END, input.readLength());
            }
            state = State.INIT;
            return createToken(PythonTokenId.ERROR, input.readLength());
        }

        default:
            assert false : state;
        }

        return null;
    }

    @Override
    public void release() {
    }

    private static PythonTokenId getKeywordToken(CharSequence s) {
        int length = s.length();
        if (length < 2) {
            return null;
        }

        if (BuiltinException.isBuiltInException(s)) {
            return PythonTokenId.ERROR;
        }

        char c1 = s.charAt(0);
        char c2 = s.charAt(1);

        switch (c1) {
        case 'a': // and, as, assert, async, await
            switch (c2) {
            case 'b': // abs
                if (length == 3 && TokenUtilities.textEquals(s, "abs")) { // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
            case 'l': // all
                if (length == 3 && TokenUtilities.textEquals(s, "all")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
            case 'n': // and, any
                if (length == 3 && TokenUtilities.textEquals(s, "and")) { // NOI18N
                    return PythonTokenId.ANY_KEYWORD;
                } else if (TokenUtilities.textEquals(s, "any")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                break;
            case 's':  // as, ascii, assert, async
                if (length == 2) { // as
                    return PythonTokenId.ANY_KEYWORD;
                }
                if (length == 5 && TokenUtilities.textEquals(s, "ascii")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                if (length == 6 && TokenUtilities.textEquals(s, "assert")) { // NOI18N
                    return PythonTokenId.ANY_KEYWORD;
                }
                if (length == 5 && TokenUtilities.textEquals(s, "async")) { // NOI18N
                    return PythonTokenId.ANY_KEYWORD;
                }
                break;
            case 'w': // await
                if (length == 5 && TokenUtilities.textEquals(s, "await")) { // NOI18N
                    return PythonTokenId.ANY_KEYWORD;
                }
            }
            break;
        case 'b': // basestring, bin, bool, break, bytearray, bytes
            if (length == 10 && TokenUtilities.textEquals(s, "basestring")) {  // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 3 && TokenUtilities.textEquals(s, "bin")) {  // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 4 && TokenUtilities.textEquals(s, "bool")) {  // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 5 && TokenUtilities.textEquals(s, "break")) { // NOI18N
                return PythonTokenId.ANY_KEYWORD;
            }
            if (length == 9 && TokenUtilities.textEquals(s, "bytearray")) {  // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 5 && TokenUtilities.textEquals(s, "bytes")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'c': // callable, chr, class, classmethod, continue
            switch (c2) {
            case 'a': // callable
                if (length == 8 && TokenUtilities.textEquals(s, "callable")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                break;
            case 'h': // chr
                if (length == 3 && TokenUtilities.textEquals(s, "chr")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                break;
            case 'l': // class, classmethod
                if (length == 5 && TokenUtilities.textEquals(s, "class")) { // NOI18N
                    return PythonTokenId.CLASS;
                }
                if (length == 11 && TokenUtilities.textEquals(s, "classmethod")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                break;
            case 'm': // cmp
                if (length == 3 && TokenUtilities.textEquals(s, "cmp")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                break;
            case 'o':  // compile, complex, continue
                if (length == 7 && TokenUtilities.textEquals(s, "compile")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                if (length == 7 && TokenUtilities.textEquals(s, "complex")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                if (length == 8 && TokenUtilities.textEquals(s, "continue")) { // NOI18N
                    return PythonTokenId.ANY_KEYWORD;
                }
                break;
            }
            break;
        case 'd': // def, del
            switch (c2) {
                case 'e': // def, del, delattr
                    if (length == 3 && TokenUtilities.textEquals(s, "def")) {  // NOI18N
                        return PythonTokenId.DEF;
                    }
                    if (length == 3 && TokenUtilities.textEquals(s, "del")) {  // NOI18N
                        return PythonTokenId.ANY_KEYWORD;
                    }
                    if (length == 7 && TokenUtilities.textEquals(s, "delattr")) {  // NOI18N
                        return PythonTokenId.BUILTIN_FUNCTION;
                    }
                    break;
                case 'i': // dict, dir, divmod
                    if (length == 4 && TokenUtilities.textEquals(s, "dict")) {  // NOI18N
                        return PythonTokenId.BUILTIN_FUNCTION;
                    }
                    if (length == 3 && TokenUtilities.textEquals(s, "dir")) {  // NOI18N
                        return PythonTokenId.BUILTIN_FUNCTION;
                    }
                    if (length == 6 && TokenUtilities.textEquals(s, "divmod")) {  // NOI18N
                        return PythonTokenId.BUILTIN_FUNCTION;
                    }
                    break;
            }
            break;
        case 'e': // elif, else, enumerate, eval, except, exec, execfile
            switch (c2) {
            case 'l': // elif, else
                if (length == 4) {
                    if (TokenUtilities.textEquals(s, "elif")) { // NOI18N
                        return PythonTokenId.ELIF;
                    }
                    if (TokenUtilities.textEquals(s, "else")) { // NOI18N
                        return PythonTokenId.ELSE;
                    }
                }
                break;
            case 'n': // enumerate
                if (length == 9 && TokenUtilities.textEquals(s, "enumerate")) { // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                break;
            case 'v': // eval
                if (length == 4 && TokenUtilities.textEquals(s, "eval")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                break;
            case 'x': // except, exec, execfile
                if (length == 4 && TokenUtilities.textEquals(s, "exec")) { // NOI18N
                    return PythonTokenId.ANY_KEYWORD;
                }
                if (length == 8 && TokenUtilities.textEquals(s, "execfile")) { // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                if (length == 6 && TokenUtilities.textEquals(s, "except")) { // NOI18N
                    return PythonTokenId.EXCEPT;
                }
                break;
            }
            break;
        case 'f': // file, filter, finally, for, from
            switch (c2) {
            case 'i': // file, filter, finally
                if (length == 4 && TokenUtilities.textEquals(s, "file")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                if (length == 6 && TokenUtilities.textEquals(s, "filter")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                if (length == 7 && TokenUtilities.textEquals(s, "finally")) { // NOI18N
                    return PythonTokenId.FINALLY;
                }
                break;
            case 'o': // for, format
                if (length == 3 && TokenUtilities.textEquals(s, "for")) { // NOI18N
                    return PythonTokenId.ANY_KEYWORD;
                }
                if (length == 6 && TokenUtilities.textEquals(s, "format")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                break;
            case 'r': // from, frozenset
                if (length == 4 && TokenUtilities.textEquals(s, "from")) { // NOI18N
                    return PythonTokenId.FROM;
                }
                if (length == 9 && TokenUtilities.textEquals(s, "frozenset")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                break;
            case 'l':
                if (length == 5 && TokenUtilities.textEquals(s, "float")) {
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                break;
            }
            break;
        case 'g': // getattr, global, globals
            if (length == 7 && TokenUtilities.textEquals(s, "getattr")) {  // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 6 && TokenUtilities.textEquals(s, "global")) { // NOI18N
                return PythonTokenId.ANY_KEYWORD;
            }
            if (length == 7 && TokenUtilities.textEquals(s, "globals")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'h': // hasattr, hash, help, hex
            if (length == 7 && TokenUtilities.textEquals(s, "hasattr")) {  // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 4 && TokenUtilities.textEquals(s, "hash")) {  // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 4 && TokenUtilities.textEquals(s, "help")) {  // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 3 && TokenUtilities.textEquals(s, "hex")) {  // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'i': // id, if, import, in, input, int, is, issubclass, iter
            if (length == 2) {
                switch (c2) {
                case 'd': // id
                    return PythonTokenId.BUILTIN_FUNCTION;
                case 'f': // if
                    return PythonTokenId.IF;
                case 'n': // in
                    if (length == 2 && TokenUtilities.textEquals(s, "in")) { //NOI18N
                        return PythonTokenId.ANY_KEYWORD;
                    }
                case 's': // is
                    return PythonTokenId.ANY_KEYWORD;
                }
            } else if (c2 == 'm' && length == 6 && TokenUtilities.textEquals(s, "import")) { // NOI18N
                return PythonTokenId.IMPORT;
            } else if (length == 5 && TokenUtilities.textEquals(s, "input")) {  // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            } else if (length == 3 && TokenUtilities.textEquals(s, "int")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            } else if (length == 10 && TokenUtilities.textEquals(s, "isinstance")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            } else if (length == 10 && TokenUtilities.textEquals(s, "issubclass")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            } else if (length == 4 && TokenUtilities.textEquals(s, "iter")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'l': // lambda, len, list, locals, long
            if (length == 6 && TokenUtilities.textEquals(s, "lambda")) { // NOI18N
                return PythonTokenId.ANY_KEYWORD;
            } else if (length == 3 && TokenUtilities.textEquals(s, "len")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            } else if (length == 4 && TokenUtilities.textEquals(s, "list")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            } else if (length == 6 && TokenUtilities.textEquals(s, "locals")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            } else if (length == 4 && TokenUtilities.textEquals(s, "long")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'm': // map, max, memoryview, min
            if (length == 3 && TokenUtilities.textEquals(s, "map")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 3 && TokenUtilities.textEquals(s, "max")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 10 && TokenUtilities.textEquals(s, "memoryview")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 3 && TokenUtilities.textEquals(s, "min")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'n': // next, nonlocal, not
            if (length == 4 && TokenUtilities.textEquals(s, "next")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 8 && TokenUtilities.textEquals(s, "nonlocal")) { // NOI18N
                return PythonTokenId.ANY_KEYWORD;
            }
            if (length == 3 && TokenUtilities.textEquals(s, "not")) { // NOI18N
                return PythonTokenId.ANY_KEYWORD;
            }
            break;
        case 'o': // or, object, oct, open, ord
            if (length == 2 && TokenUtilities.textEquals(s, "or")) { // NOI18N
                return PythonTokenId.ANY_KEYWORD;
            }
            if (length == 6 && TokenUtilities.textEquals(s, "object")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 3 && TokenUtilities.textEquals(s, "oct")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 4 && TokenUtilities.textEquals(s, "open")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 3 && TokenUtilities.textEquals(s, "ord")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'p': // pass, pow, print, property
            if (c2 == 'a') { // pass
                if (length == 4 && TokenUtilities.textEquals(s, "pass")) { // NOI18N
                    return PythonTokenId.PASS;
                }
            }
            if (length == 3 && TokenUtilities.textEquals(s, "pow")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (c2 == 'r') { // print, property
                if (length == 5 && TokenUtilities.textEquals(s, "print")) { // NOI18N
                    return PythonTokenId.ANY_KEYWORD;
                }
                if (length == 8 && TokenUtilities.textEquals(s, "property")) { // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
            }
            break;
        case 'r': // raise, range, raise, raw_input, reduce, reload, repr, return, reversed, round
            if (c2 == 'a') { // raise, range
                if (length == 5 && TokenUtilities.textEquals(s, "range")) { // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                if (length == 5 && TokenUtilities.textEquals(s, "raise")) { // NOI18N
                    return PythonTokenId.RAISE;
                }
                if (length == 9 && TokenUtilities.textEquals(s, "raw_input")) { // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
            } else if (c2 == 'e') { // reduce, reload, repr, return, reversed
                if (length == 6 && TokenUtilities.textEquals(s, "reduce")) { // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                if (length == 6 && TokenUtilities.textEquals(s, "reload")) {  // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                if (length == 4 && TokenUtilities.textEquals(s, "repr")) { // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
                if (length == 6 && TokenUtilities.textEquals(s, "return")) { // NOI18N
                    return PythonTokenId.RETURN;
                }
                if (length == 8 && TokenUtilities.textEquals(s, "reversed")) { // NOI18N
                    return PythonTokenId.BUILTIN_FUNCTION;
                }
            } else if (length == 5 && TokenUtilities.textEquals(s, "round")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 's': // self, set, setattr, slice, sorted, staticmethod, str, sum, super
            if (length == 4 && TokenUtilities.textEquals(s, "self")) { // NOI18N
                return PythonTokenId.ANY_KEYWORD;
            }
            if (length == 3 && TokenUtilities.textEquals(s, "set")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 7 && TokenUtilities.textEquals(s, "setattr")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 5 && TokenUtilities.textEquals(s, "slice")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 6 && TokenUtilities.textEquals(s, "sorted")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 12 && TokenUtilities.textEquals(s, "staticmethod")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 3 && TokenUtilities.textEquals(s, "str")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 3 && TokenUtilities.textEquals(s, "sum")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 5 && TokenUtilities.textEquals(s, "super")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 't': // try, tuple, type
            if (length == 3 && TokenUtilities.textEquals(s, "try")) { // NOI18N
                return PythonTokenId.TRY;
            } else if (length == 5 && TokenUtilities.textEquals(s, "tuple")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            } else if (length == 4 && TokenUtilities.textEquals(s, "type")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'u': // unichr, unicode
            if (length == 6 && TokenUtilities.textEquals(s, "unichr")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            if (length == 7 && TokenUtilities.textEquals(s, "unicode")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'v': // vars
            if (length == 4 && TokenUtilities.textEquals(s, "vars")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'w': // while,with
            if (c2 == 'h') { // while
                if (length == 5 && TokenUtilities.textEquals(s, "while")) { // NOI18N
                    return PythonTokenId.ANY_KEYWORD;
                }
            } else if (c2 == 'i') { // with
                if (length == 4 && TokenUtilities.textEquals(s, "with")) { // NOI18N
                    return PythonTokenId.ANY_KEYWORD;
                }
            }
            break;
        case 'x': // xrange
            if (length == 6 && TokenUtilities.textEquals(s, "xrange")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'y': // yield
            if (length == 5 && TokenUtilities.textEquals(s, "yield")) { // NOI18N
                return PythonTokenId.ANY_KEYWORD;
            }
            break;
        case 'z': // zip
            if (length == 3 && TokenUtilities.textEquals(s, "zip")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        case 'F': // False
            if (length == 5 && TokenUtilities.textEquals(s, "False")) { // NOI18N
                return PythonTokenId.BOOL;
            }
            break;
        case 'N': // None
            if (length == 4 && TokenUtilities.textEquals(s, "None")) { // NOI18N
                return PythonTokenId.BOOL;
            }
            break;
        case 'T': // True
            if (length == 4 && TokenUtilities.textEquals(s, "True")) { // NOI18N
                return PythonTokenId.BOOL;
            }
            break;
        case '_': // Special symbols of python
            if (length > 4 && TokenUtilities.startsWith(s, "__") && TokenUtilities.endsWith(s, "__")) { // NOI18N
                return PythonTokenId.BUILTIN_FUNCTION;
            }
            break;
        }

        return null;
    }

    public static boolean isKeywordOrBuiltin(CharSequence name) {
        return getKeywordToken(name) != null;
    }
}
