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
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Base of Lexical analyzers for C/C++ languages.
 * <br/>
 * It handles escaped lines and delegate identifier to keyword recognition
 * to language-flavor specific filter
 *
 * @version 1.00
 */
public abstract class CndLexer implements Lexer<CppTokenId> {

    protected static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final TokenFactory<CppTokenId> tokenFactory;
    private int escapedEatenChars;
    private int tokenSplittedByEscapedLine;
    private int lastTokenEndedByEscapedLine;
    protected CndLexer(LexerRestartInfo<CppTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        setState((Integer) info.state()); // last line in contstructor
    }

    @Override
    public Object state() {
        return getState();
    }

    protected final void setState(Integer state) {
        this.lastTokenEndedByEscapedLine = state == null ? 0 : state.intValue();
    }

    protected final Integer getState() {
        return lastTokenEndedByEscapedLine == 0 ? null : lastTokenEndedByEscapedLine;
    }
    
    protected final void backup(int n) {
        input.backup(n + escapedEatenChars);
        lastTokenEndedByEscapedLine = escapedEatenChars;
        tokenSplittedByEscapedLine -= escapedEatenChars;
    }

    @SuppressWarnings("fallthrough")
    protected final int read(boolean skipEscapedLF) {
        int c = input.read();
        escapedEatenChars = 0;
        if (skipEscapedLF) { // skip escaped LF
            int next;
            while (c == '\\') {
                escapedEatenChars++;
                switch (input.read()) {
                    case '\r':
                        if (consumeNewline()) {
                            escapedEatenChars++;
                        }
                        // nobreak
                    case '\n':
                        escapedEatenChars++;
                        next = input.read();
                        break;
                    default:
                        input.backup(1);
                        escapedEatenChars--;
                        assert c == '\\' : "must be backslash " + (char)c;
                        tokenSplittedByEscapedLine += escapedEatenChars;
                        return c; // normal backslash, not escaped LF
                }
                c = next;
            }
            tokenSplittedByEscapedLine += escapedEatenChars;
        }
        return c;
    }

    protected final boolean consumeNewline() {
        return input.consumeNewline();
    }

    @SuppressWarnings("fallthrough")
    @Override
    public Token<CppTokenId> nextToken() {
        while (true) {
            // special handling for escaped lines
            if (lastTokenEndedByEscapedLine > 0) {
                int c = read(false);
                lastTokenEndedByEscapedLine--;
                assert c == '\\' : "there must be \\";
                c = read(false);
                assert c == '\n' || c == '\r' : "there must be \r or \n";
                if (c == '\r') {
                    lastTokenEndedByEscapedLine--;
                    if (input.consumeNewline()) {
                        lastTokenEndedByEscapedLine--;
                    }
                    return token(CppTokenId.ESCAPED_LINE);
                } else {
                    lastTokenEndedByEscapedLine--;
                    return token(CppTokenId.ESCAPED_LINE, "\\\n", PartType.COMPLETE); // NOI18N
                }
            } else {
                int c = read(true);
                // if read of the first char caused skipping escaped line
                // do we need to backup and create escaped lines first?
                switch (c) {
                    case '"': {
                        Token<CppTokenId> out = finishDblQuote();
                        assert out != null : "not handled dobule quote";
                        return out;
                    }
                    case '\'': {// char literal
                        Token<CppTokenId> out = finishSingleQuote();
                        assert out != null : "not handled single quote";
                        return out;
                    }
                    case '#': {
                        Token<CppTokenId> out = finishSharp();
                        assert out != null : "not handled #";
                        return out;
                    }

                    case '/':
                        switch (read(true)) {
                            case '/': // in single-line or doxygen comment
                            {
                                Token<CppTokenId> out = finishLineComment(true);
                                assert out != null : "not handled //";
                                return out;
                            }
                            case '=': // found /=
                                return token(CppTokenId.SLASHEQ);
                            case '*': // in multi-line or doxygen comment
                            {
                                Token<CppTokenId> out = finishBlockComment(true);
                                assert out != null : "not handled /*";
                                return out;
                            }
                        } // end of switch()
                        backup(1);
                        return token(CppTokenId.SLASH);

                    case '=':
                        if (read(true) == '=') {
                            return token(CppTokenId.EQEQ);
                        }
                        backup(1);
                        return token(CppTokenId.EQ);

                    case '>':
                        switch (read(true)) {
                            case '>': // >>
                                if (read(true) == '=') {
                                    return token(CppTokenId.GTGTEQ);
                                }
                                backup(1);
                                return token(CppTokenId.GTGT);
                            case '=': // >=
                                return token(CppTokenId.GTEQ);
                        }
                        backup(1);
                        return token(CppTokenId.GT);

                    case '<': {
                        Token<CppTokenId> out = finishLT();
                        assert out != null : "not handled '<'";
                        return out;
                    }

                    case '+':
                        switch (read(true)) {
                            case '+':
                                return token(CppTokenId.PLUSPLUS);
                            case '=':
                                return token(CppTokenId.PLUSEQ);
                        }
                        backup(1);
                        return token(CppTokenId.PLUS);

                    case '-':
                        switch (read(true)) {
                            case '-':
                                return token(CppTokenId.MINUSMINUS);
                            case '>':
                                if (read(true) == '*') {
                                    return token(CppTokenId.ARROWMBR);
                                }
                                backup(1);
                                return token(CppTokenId.ARROW);
                            case '=':
                                return token(CppTokenId.MINUSEQ);
                        }
                        backup(1);
                        return token(CppTokenId.MINUS);

                    case '*':
                        switch (read(true)) {
                            case '/': // invalid comment end - */ or int*/* */
                                if (read(true) == '*') {
                                    backup(2);
                                    return token(CppTokenId.STAR);
                                }
                                backup(1);
                                return token(CppTokenId.INVALID_COMMENT_END);
                            case '=':
                                return token(CppTokenId.STAREQ);
                        }
                        backup(1);
                        return token(CppTokenId.STAR);

                    case '|':
                        switch (read(true)) {
                            case '|':
                                return token(CppTokenId.BARBAR);
                            case '=':
                                return token(CppTokenId.BAREQ);
                        }
                        backup(1);
                        return token(CppTokenId.BAR);

                    case '&':
                        switch (read(true)) {
                            case '&':
                                return token(CppTokenId.AMPAMP);
                            case '=':
                                return token(CppTokenId.AMPEQ);
                        }
                        backup(1);
                        return token(CppTokenId.AMP);

                    case '%': {
                        Token<CppTokenId> out = finishPercent();
                        assert out != null : "not handled %";
                        return out;
                    }

                    case '^':
                        if (read(true) == '=') {
                            return token(CppTokenId.CARETEQ);
                        }
                        backup(1);
                        return token(CppTokenId.CARET);

                    case '!':
                        if (read(true) == '=') {
                            return token(CppTokenId.NOTEQ);
                        }
                        backup(1);
                        return token(CppTokenId.NOT);

                    case '.':
                        if ((c = read(true)) == '.') {
                            if (read(true) == '.') { // ellipsis ...
                                return token(CppTokenId.ELLIPSIS);
                            } else {
                                input.backup(2);
                            }
                        } else if ('0' <= c && c <= '9') { // float literal
                            return finishNumberLiteral(read(true), true);
                        } else if (c == '*') {
                            return token(CppTokenId.DOTMBR);
                        } else {
                            backup(1);
                        }
                        return token(CppTokenId.DOT);

                    case ':':
                        if (read(true) == ':') {
                            return token(CppTokenId.SCOPE);
                        }
                        backup(1);
                        return token(CppTokenId.COLON);

                    case '~':
                        return token(CppTokenId.TILDE);
                    case ',':
                        return token(CppTokenId.COMMA);
                    case ';':
                        return token(CppTokenId.SEMICOLON);

                    case '?':
                        return token(CppTokenId.QUESTION);
                    case '(':
                        return token(CppTokenId.LPAREN);
                    case ')':
                        return token(CppTokenId.RPAREN);
                    case '[':
                        return token(CppTokenId.LBRACKET);
                    case ']':
                        return token(CppTokenId.RBRACKET);
                    case '{':
                        return token(CppTokenId.LBRACE);
                    case '}':
                        return token(CppTokenId.RBRACE);
                    case '`':
                        return token(CppTokenId.GRAVE_ACCENT);
                    case '@':
                        return token(CppTokenId.AT);

                    case '0': // in a number literal
                        c = read(true);
                        if (c == 'x' || c == 'X' || // in hexadecimal (possibly floating-point) literal
                                c == 'b' || c == 'B' ) { // in bianry literal
                            boolean inFraction = false;
                            while (true) {
                                switch (read(true)) {
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
                                        break;
                                    case '.': // hex float literal
                                        if (!inFraction) {
                                            inFraction = true;
                                        } else { // two dots in the float literal
                                            return token(CppTokenId.FLOAT_LITERAL_INVALID);
                                        }
                                        break;
                                    case 'l':
                                    case 'L': // 0x1234l or 0x1234L
                                        return finishLongLiteral(read(true));
                                    case 'p':
                                    case 'P': // binary exponent
                                        return finishFloatExponent();
                                    case 'u':
                                    case 'U':
                                        return finishUnsignedLiteral(read(true));
                                    default:
                                        backup(1);
                                        // if float then before mandatory binary exponent => invalid
                                        return token(inFraction ? CppTokenId.FLOAT_LITERAL_INVALID
                                                : CppTokenId.INT_LITERAL);
                                }
                            } // end of while(true)
                        }
                        return finishNumberLiteral(c, false);

                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        return finishNumberLiteral(read(true), false);
                    case '\\':
                        return token(CppTokenId.BACK_SLASH);
                    case '\r':
                        consumeNewline();
                        return token(CppTokenId.NEW_LINE);
                    case '\n':
                        return token(CppTokenId.NEW_LINE, "\n", PartType.COMPLETE); // NOI18N
                    // All Character.isWhitespace(c) below 0x80 follow
                    // ['\t' - '\f'] and [0x1c - ' ']
                    case '\t':
                    case 0x0b:
                    case '\f':
                    case 0x1c:
                    case 0x1d:
                    case 0x1e:
                    case 0x1f:
                        return finishWhitespace();
                    case ' ':
                        c = read(true);
                        if (c == EOF || !Character.isWhitespace(c) || c == '\n' || c == '\r') { // Return single space as flyweight token
                            backup(1);
                            return token(CppTokenId.WHITESPACE, " ", PartType.COMPLETE); // NOI18N

                        }
                        return finishWhitespace();

                    case EOF:
                        if (isTokenSplittedByEscapedLine()) {
                            backup(1);
                            assert lastTokenEndedByEscapedLine > 0 : "lastTokenEndedByEscapedLine is " + lastTokenEndedByEscapedLine;
                            break;
                        }
                        return null;

                    case '$':
                        // dollar is extension in gcc and msvc $ is a valid start of identifiers
//                        return token(CppTokenId.DOLLAR);
                    default:
                        c = translateSurrogates(c);
                        if (CndLexerUtilities.isCppIdentifierStart(c)) {
                            if (c == 'L' || c == 'U' || c == 'u' || c == 'R') {
                                int next = read(true);
                                boolean raw_string = (c == 'R');
                                if (next == 'R' && (c == 'u' || c == 'U' || c == 'L')) {
                                    // uR, UR or LR
                                    raw_string = true;
                                    next = read(true);
                                } else if (next == '8' && c == 'u') {
                                    // u8
                                    next = read(true);
                                    if (next == 'R') {
                                        // u8R
                                        raw_string = true;
                                        next = read(true);
                                    }
                                }
                                if (next == '"') {
                                    // string with L/U/u/R prefixes
                                    Token<CppTokenId> out = raw_string ? finishRawString() : finishDblQuote();
                                    assert out != null : "not handled dobule quote";
                                    return out;
                                } else if (next == '\'' && !raw_string) {
                                    // char with L or U/u prefix
                                    Token<CppTokenId> out = finishSingleQuote();
                                    assert out != null : "not handled single quote";
                                    return out;
                                } else {
                                    backup(1);
                                }
                            }
                            if (c == 'E') {
                                if(isExecSQL(c)) {
                                    Token<CppTokenId> out = finishExecSQL();
                                    assert out != null : "not handled exec sql";
                                    return out;
                                }
                            }
                            return keywordOrIdentifier(c);
                        }
                        if (Character.isWhitespace(c)) {
                            return finishWhitespace();
                        }

                        // Invalid char
                        return token(CppTokenId.ERROR);
                }
            } // end of switch (c)
        } // end of while(true)
    }

    protected abstract CppTokenId getKeywordOrIdentifierID(CharSequence text);

    protected final Token<CppTokenId> finishLineComment(boolean createToken) {
        int c = read(true);
        boolean startOfDoxygen = (c == '/');// in doxygen comment
        while (true) {
            switch (c) {
                case '\r':
                case '\n':
                case EOF:
                    backup(1);
                    if (createToken) {
                        return startOfDoxygen ? token(CppTokenId.DOXYGEN_LINE_COMMENT) : token(CppTokenId.LINE_COMMENT);
                    } else {
                        return null;
                    }
            }
            c = read(true);
        }
    }
    
    protected final Token<CppTokenId> finishBlockComment(boolean createToken) {
        int c = read(true);
        int firstChar = c;
        if (firstChar == '*' || firstChar == '!') { // either doxygen comment or empty multi-line comment /**/
            c = read(true);
            if (c == '/' && firstChar != '!') {
                return !createToken ? null : token(CppTokenId.BLOCK_COMMENT);
            }
            while (true) { // in doxygen comment
                while (c == '*') {
                    c = read(true);
                    if (c == '/') {
                        return !createToken ? null : token(CppTokenId.DOXYGEN_COMMENT);
                    } else if (c == EOF) {
                        return !createToken ? null : tokenPart(CppTokenId.DOXYGEN_COMMENT, PartType.START);
                    }
                }
                if (c == EOF) {
                    return !createToken ? null : tokenPart(CppTokenId.DOXYGEN_COMMENT, PartType.START);
                }
                c = read(true);
            }
        } else { // in multi-line comment (and not after '*' or '!')
            while (true) {
                c = read(true);
                while (c == '*') {
                    c = read(true);
                    if (c == '/') {
                        return !createToken ? null : token(CppTokenId.BLOCK_COMMENT);
                    } else if (c == EOF) {
                        return !createToken ? null : tokenPart(CppTokenId.BLOCK_COMMENT, PartType.START);
                    }
                }
                if (c == EOF) {
                    return !createToken ? null : tokenPart(CppTokenId.BLOCK_COMMENT, PartType.START);
                }
            }
        }
    }

    private int translateSurrogates(int c) {
        if (Character.isHighSurrogate((char) c)) {
            int lowSurr = read(true);
            if (lowSurr != EOF && Character.isLowSurrogate((char) lowSurr)) {
                // c and lowSurr form the integer unicode char.
                c = Character.toCodePoint((char) c, (char) lowSurr);
            } else {
                // Otherwise it's error: Low surrogate does not follow the high one.
                // Leave the original character unchanged.
                // As the surrogates do not belong to any
                // specific unicode category the lexer should finally
                // categorize them as a lexical error.
                backup(1);
            }
        }
        return c;
    }

    private Token<CppTokenId> finishWhitespace() {
        while (true) {
            int c = read(true);
            // There should be no surrogates possible for whitespace
            // so do not call translateSurrogates()
            if (c == EOF || !Character.isWhitespace(c) || c == '\n' || c == '\r') {
                backup(1);
                return isTokenSplittedByEscapedLine() ? token(CppTokenId.ESCAPED_WHITESPACE) : token(CppTokenId.WHITESPACE);
            }
        }
    }

    private final StringBuilder idText = new StringBuilder();
    private Token<CppTokenId> keywordOrIdentifier(int c) {
        idText.setLength(0);
        idText.append((char)c);
        while (true) {
            c = read(true);
            if (c == EOF || !CndLexerUtilities.isCppIdentifierPart(c = translateSurrogates(c))) {
                // For surrogate 2 chars must be backed up
                backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
                CppTokenId id = getKeywordOrIdentifierID(idText.toString());
                assert id != null : "must be valid id for " + idText;
                return token(id);
            } else {
                idText.append((char)c);
            }
        }
    }

    private Token<CppTokenId> finishNumberLiteral(int c, boolean inFraction) {
        while (true) {
            switch (c) {
                case '.':
                    if (!inFraction) {
                        inFraction = true;
                    } else { // two dots in the literal
                        return token(CppTokenId.FLOAT_LITERAL_INVALID);
                    }
                    break;
                case 'l':
                case 'L': // 0l or 0L
                    return finishLongLiteral(read(true));
                case 'f':
                case 'F':
                    return token(CppTokenId.FLOAT_LITERAL);
                case 'u':
                case 'U':
                    return finishUnsignedLiteral(read(true));
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
                case '\'':
                    break;
                case 'e':
                case 'E': // exponent part
                    return finishFloatExponent();
                default:
                    backup(1);
                    return token(inFraction ? CppTokenId.DOUBLE_LITERAL
                            : CppTokenId.INT_LITERAL);
            }
            c = read(true);
        }
    }

    private Token<CppTokenId> finishFloatExponent() {
        int c = read(true);
        if (c == '+' || c == '-') {
            c = read(true);
        }
        if (c < '0' || '9' < c) {
            return token(CppTokenId.FLOAT_LITERAL_INVALID);
        }
        do {
            c = read(true);
        } while ('0' <= c && c <= '9'); // reading exponent
        switch (c) {
//            case 'd':
//            case 'D':
//                return token(CppTokenId.DOUBLE_LITERAL);
            case 'f':
            case 'F':
                return token(CppTokenId.FLOAT_LITERAL);
            case 'l':
            case 'L':
                return token(CppTokenId.DOUBLE_LITERAL);
            default:
                backup(1);
                return token(CppTokenId.DOUBLE_LITERAL);
        }
    }

    protected final Token<CppTokenId> token(CppTokenId id) {
        return token(id, id.fixedText(), PartType.COMPLETE);
    }

    protected final Token<CppTokenId> tokenPart(CppTokenId id, PartType part) {
        return token(id, null, part);
    }

    private Token<CppTokenId> token(CppTokenId id, String fixedText, PartType part) {
        assert id != null : "id must be not null";
        Token<CppTokenId> token;
        if (fixedText != null && !isTokenSplittedByEscapedLine()) {
            // create flyweight token
            token = tokenFactory.getFlyweightToken(id, fixedText);
        } else {
            if (part != PartType.COMPLETE) {
                token = tokenFactory.createToken(id, input.readLength(), part);
            } else {
                token = tokenFactory.createToken(id);
            }
        }
        tokenSplittedByEscapedLine = 0;
        escapedEatenChars = 0;
        assert token != null : "token must be created as result for " + id;
        postTokenCreate(id);
        return token;
    }

    protected Token<CppTokenId> finishSharp() {
        if (read(true) == '#') {
            return token(CppTokenId.DBL_SHARP);
        }
        backup(1);
        return token(CppTokenId.SHARP);
    }

    protected Token<CppTokenId> finishPercent() {
        if (read(true) == '=') {
            return token(CppTokenId.PERCENTEQ);
        }
        backup(1);
        return token(CppTokenId.PERCENT);
    }

    private Token<CppTokenId> finishRawString() {
        PartType type = CppStringLexer.finishRawString(input);
        if (type == PartType.COMPLETE) {
            return token(CppTokenId.RAW_STRING_LITERAL);
        } else {
            return tokenPart(CppTokenId.RAW_STRING_LITERAL, type);
        }
    }

    @SuppressWarnings("fallthrough")
    protected Token<CppTokenId> finishDblQuote() {
        while (true) { // string literal
            switch (read(true)) {
                case '"': // NOI18N
                    return token(CppTokenId.STRING_LITERAL);
                case '\\':
                    read(false); // read escaped char
                    break;
                case '\r':
                case '\n':
                    backup(1); // leave new line for the own token
                case EOF:
                    return tokenPart(CppTokenId.STRING_LITERAL, PartType.START);
            }
        }
    }

    @SuppressWarnings("fallthrough")
    protected Token<CppTokenId> finishSingleQuote() {
        while (true) {
            switch (read(true)) {
                case '\'': // NOI18N
                    return token(CppTokenId.CHAR_LITERAL);
                case '\\':
                    read(false); // read escaped char
                    break;
                case '\r':
                case '\n':
                    backup(1); // leave new line for the own token
                case EOF:
                    return tokenPart(CppTokenId.CHAR_LITERAL, PartType.START);
            }
        }
    }
    
    protected Token<CppTokenId> finishLT() {
        switch (read(true)) {
            case '<': // after <<
                if (read(true) == '=') {
                    return token(CppTokenId.LTLTEQ);
                }
                backup(1);
                return token(CppTokenId.LTLT);
            case '=': // <=
                return token(CppTokenId.LTEQ);
        }
        backup(1);
        return token(CppTokenId.LT);
    }

    protected boolean isExecSQL(int c) {
        if (c == 'E') {
            if (read(true) == 'X') {
                if (read(true) == 'E') {
                    if (read(true) == 'C') {
                        if (read(true) == ' ') {
                            if (read(true) == 'S') {
                                if (read(true) == 'Q') {
                                    if (read(true) == 'L') {
                                        return true;
                                    }
                                    backup(1);
                                }
                                backup(1);
                            }
                            backup(1);
                        }
                        backup(1);
                    }
                    backup(1);
                }
                backup(1);
            }
            backup(1);
        }
        return false;
    }

    @SuppressWarnings("fallthrough")
    protected Token<CppTokenId> finishExecSQL() {
        while (true) {
            switch (read(true)) {
                case ';': // NOI18N
                    backup(1);
                    return token(CppTokenId.PROC_DIRECTIVE);
                case EOF:
                    backup(1);
                    return token(CppTokenId.PROC_DIRECTIVE);
            }
        }
    }

    private Token<CppTokenId> finishLongLiteral(int c) {
        if (c == 'l' || c == 'L') {// 0ll or 0LL
            c = read(true);
            if (c == 'u' || c == 'U') {// 0llu or 0LLU
                return token(CppTokenId.UNSIGNED_LONG_LONG_LITERAL);
            } else {
                backup(1);
                return token(CppTokenId.LONG_LONG_LITERAL);
            }
        } else if (c == 'u' || c == 'U') {// 0lu or 0LU
            return token(CppTokenId.UNSIGNED_LONG_LITERAL);
        } else {
            backup(1);
            return token(CppTokenId.LONG_LITERAL);
        }
    }

    private Token<CppTokenId> finishUnsignedLiteral(int c) {
        if (c == 'l' || c == 'L') {// 0ul or 0UL
            c = read(true);
            if (c == 'l' || c == 'L') {// 0ull or 0ULL
                return token(CppTokenId.UNSIGNED_LONG_LONG_LITERAL);
            } else {
                backup(1);
                return token(CppTokenId.UNSIGNED_LONG_LITERAL);
            }
        } else {
            backup(1);
            return token(CppTokenId.UNSIGNED_LITERAL);
        }
    }
    
    protected void postTokenCreate(CppTokenId id) {

    }

    @Override
    public void release() {
    }

    protected final boolean isTokenSplittedByEscapedLine() {
        return tokenSplittedByEscapedLine > 0;
    }
}
