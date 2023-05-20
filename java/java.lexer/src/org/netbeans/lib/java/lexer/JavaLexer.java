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

package org.netbeans.lib.java.lexer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for java language.
 * <br/>
 * It recognizes "version" attribute and expects <code>java.lang.Integer</code>
 * value for it. The default value is Integer.valueOf(5). The lexer changes
 * its behavior in the following way:
 * <ul>
 *     <li> Integer.valueOf(4) - "assert" recognized as keyword (not identifier)
 *     <li> Integer.valueOf(5) - "enum" recognized as keyword (not identifier)
 * </ul>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
//XXX: be carefull about flyweight tokens - needs to check if the inputX.readLength() matches the image!
public class JavaLexer implements Lexer<JavaTokenId> {
    
    private static final int EOF = LexerInput.EOF;

    private final LexerInput input;
    
    private final TokenFactory<JavaTokenId> tokenFactory;
    
    private final int version;
    
    private Integer state = null;
    
    public JavaLexer(LexerRestartInfo<JavaTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() instanceof ComplexState) {
            ComplexState complex = (ComplexState) info.state();
            this.pendingStringLiteral = complex.pendingStringLiteral;
            this.pendingBraces = complex.pendingBraces;
            this.literalHistory = complex.literalHistory;
            this.state = complex.state;
        } else {
            this.state = (Integer) info.state();
        }
        if (state == null) {
            Supplier<String> fileName = (Supplier<String>)info.getAttributeValue("fileName"); //NOI18N
            if (fileName != null && "module-info.java".equals(fileName.get())) { //NOI18N
                state = 1; // parsing module info
            }
        }
        
        Integer ver = null;
        Object verAttribute = info.getAttributeValue("version"); //NOI18N 
        if (verAttribute instanceof Supplier) {
            Object val = ((Supplier) verAttribute).get();
            if (val instanceof String) {
                ver = getVersionAsInt(((Supplier<String>) (verAttribute)).get());
            }
        } else if (verAttribute instanceof Integer) {
            ver = (Integer) verAttribute;
        }
        this.version = (ver != null) ? ver.intValue() : 10; // TODO: Java 1.8 used by default        
    }

    private static final class ComplexState {
        public final JavaTokenId pendingStringLiteral;
        public final int pendingBraces;
        public final LiteralHistoryNode literalHistory;
        public final Integer state;

        public ComplexState(JavaTokenId pendingStringLiteral, int pendingBraces,
                            LiteralHistoryNode literalHistory, Integer state) {
            this.pendingStringLiteral = pendingStringLiteral;
            this.pendingBraces = pendingBraces;
            this.literalHistory = literalHistory;
            this.state = state;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.pendingStringLiteral);
            hash = 97 * hash + this.pendingBraces;
            hash = 97 * hash + Objects.hashCode(this.literalHistory);
            hash = 97 * hash + Objects.hashCode(this.state);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ComplexState other = (ComplexState) obj;
            if (this.pendingBraces != other.pendingBraces) {
                return false;
            }
            if (this.pendingStringLiteral != other.pendingStringLiteral) {
                return false;
            }
            if (!Objects.equals(this.literalHistory, other.literalHistory)) {
                return false;
            }
            return Objects.equals(this.state, other.state);
        }

    }
    public Object state() {
        if (pendingStringLiteral != null) {
            return new ComplexState(pendingStringLiteral, 0, literalHistory, state);
        }
        return state;
    }
    
    int previousLength = -1;
    int currentLength = -1;
    
    public int nextChar() {
        previousLength = currentLength;
        
        int backupReadLength = input.readLength();
        int c = input.read();
        
        if (c != '\\') {
            currentLength = 1;
            return c;
        }
        
        boolean wasU = false;
        int first;
        
        while ((first = input.read()) == 'u')
            wasU = true;
        
        if (!wasU) {
            input.backup(input.readLengthEOF()- backupReadLength);
            currentLength = 1;
            return input.read();
        }
        
        int second = input.read();
        int third = input.read();
        int fourth = input.read();
        
        if (fourth == LexerInput.EOF) {
            //TODO: broken unicode
            input.backup(input.readLengthEOF()- backupReadLength);
            currentLength = 1;
            return input.read();
        }
        
        first = Character.digit(first, 16);
        second = Character.digit(second, 16);
        third = Character.digit(third, 16);
        fourth = Character.digit(fourth, 16);
        
        if (first == (-1) || second == (-1) || third == (-1) || fourth == (-1)) {
            //TODO: broken unicode
            input.backup(input.readLengthEOF()- backupReadLength);
            currentLength = 1;
            return input.read();
        }
        
        currentLength = input.readLength() - backupReadLength;
        return ((first * 16 + second) * 16 + third) * 16 + fourth;
    }
    
    public void backup(int howMany) {
        switch (howMany) {
            case 1:
                assert currentLength != (-1);
                input.backup(currentLength);
                currentLength = previousLength;
                previousLength = (-1);
                break;
            case 2:
                assert currentLength != (-1) && previousLength != (-1);
                input.backup(currentLength + previousLength);
                currentLength = previousLength = (-1);
                break;
            default:
                assert false : howMany;
        }
    }
    
    public void consumeNewline() {
        if (nextChar() != '\n') backup(1);
    }

    private JavaTokenId pendingStringLiteral;
    private int pendingBraces;

    private class LiteralHistoryNode {
        public final JavaTokenId pendingStringLiteral;
        public final int pendingBraces;
        public final LiteralHistoryNode next;

        public LiteralHistoryNode(JavaTokenId pendingStringLiteral, int pendingBraces, LiteralHistoryNode next) {
            this.pendingStringLiteral = pendingStringLiteral;
            this.pendingBraces = pendingBraces;
            this.next = next;
        }

    }

    LiteralHistoryNode literalHistory = null;

    public Token<JavaTokenId> nextToken() {
        boolean stringLiteralContinuation = false;
        JavaTokenId lookupId = null;
        while(true) {
            int c = stringLiteralContinuation ? '"' : nextChar();
            switch (c) {
                case '#':
                    //Support for exotic identifiers has been removed 6999438
                    return token(JavaTokenId.ERROR);
                case '"': // string literal
                    if (lookupId == null) lookupId = JavaTokenId.STRING_LITERAL;
                    while (true) {
                        switch (nextChar()) {
                            case '"': // NOI18N
                                String text = input.readText().toString();
                                if (text.length() == 2 && !stringLiteralContinuation) {
                                    int mark = input.readLength();
                                    if (nextChar() != '"') {
                                        input.backup(1); //TODO: EOF???
                                        return token(lookupId);
                                    }
                                    int c2 = nextChar();
                                    while (Character.isWhitespace(c2) && c2 != '\n') {
                                        c2 = nextChar();
                                    }
                                    if (c2 != '\n') {
                                        input.backup(input.readLengthEOF()- mark);
                                        return token(lookupId);
                                    }
                                    lookupId = JavaTokenId.MULTILINE_STRING_LITERAL;
                                }
                                if (lookupId == JavaTokenId.MULTILINE_STRING_LITERAL) {
                                    if (text.endsWith("\"\"\"") && !text.endsWith("\\\"\"\"") && (text.length() > 6 || stringLiteralContinuation)) {
                                        return token(lookupId, stringLiteralContinuation ? PartType.END : PartType.COMPLETE);
                                    } else {
                                        break;
                                    }
                                }
                                
                                return token(lookupId, stringLiteralContinuation ? PartType.END : PartType.COMPLETE);
                            case '\\':
                                switch (nextChar()) {
                                    case '{':
                                        if (pendingStringLiteral != null) {
                                            literalHistory = new LiteralHistoryNode(pendingStringLiteral, pendingBraces, literalHistory);
                                        }
                                        pendingStringLiteral = lookupId;
                                        pendingBraces = 0;
                                        return token(lookupId, stringLiteralContinuation ? PartType.MIDDLE : PartType.START);
                                }
                                break;
                            case '\r': consumeNewline();
                            case '\n':
                                if (lookupId == JavaTokenId.MULTILINE_STRING_LITERAL) {
                                    break;
                                }
                            case EOF:
                                return tokenFactory.createToken(lookupId, //XXX: \n handling for exotic identifiers?
                                        input.readLength(), PartType.START);
                        }
                    }

                case '\'': // char literal
                    while (true)
                        switch (nextChar()) {
                            case '\'': // NOI18N
                                return token(JavaTokenId.CHAR_LITERAL);
                            case '\\':
                                nextChar(); // read escaped char
                                break;
                            case '\r': consumeNewline();
                            case '\n':
                            case EOF:
                                return tokenFactory.createToken(JavaTokenId.CHAR_LITERAL,
                                        input.readLength(), PartType.START);
                        }

                case '/':
                    switch (nextChar()) {
                        case '/': // in single-line comment
                            while (true)
                                switch (nextChar()) {
                                    case '\r': consumeNewline();
                                    case '\n':
                                    case EOF:
                                        return token(JavaTokenId.LINE_COMMENT);
                                }
                        case '=': // found /=
                            return token(JavaTokenId.SLASHEQ);
                        case '*': // in multi-line or javadoc comment
                            c = nextChar();
                            if (c == '*') { // either javadoc comment or empty multi-line comment /**/
                                    c = nextChar();
                                    if (c == '/')
                                        return token(JavaTokenId.BLOCK_COMMENT);
                                    while (true) { // in javadoc comment
                                        while (c == '*') {
                                            c = nextChar();
                                            if (c == '/')
                                                return token(JavaTokenId.JAVADOC_COMMENT);
                                            else if (c == EOF)
                                                return tokenFactory.createToken(JavaTokenId.JAVADOC_COMMENT,
                                                        input.readLength(), PartType.START);
                                        }
                                        if (c == EOF)
                                            return tokenFactory.createToken(JavaTokenId.JAVADOC_COMMENT,
                                                        input.readLength(), PartType.START);
                                        c = nextChar();
                                    }

                            } else { // in multi-line comment (and not after '*')
                                while (true) {
                                    c = nextChar();
                                    while (c == '*') {
                                        c = nextChar();
                                        if (c == '/')
                                            return token(JavaTokenId.BLOCK_COMMENT);
                                        else if (c == EOF)
                                            return tokenFactory.createToken(JavaTokenId.BLOCK_COMMENT,
                                                    input.readLength(), PartType.START);
                                    }
                                    if (c == EOF)
                                        return tokenFactory.createToken(JavaTokenId.BLOCK_COMMENT,
                                                input.readLength(), PartType.START);
                                }
                            }
                    } // end of switch()
                    backup(1);
                    return token(JavaTokenId.SLASH);

                case '=':
                    if (nextChar() == '=')
                        return token(JavaTokenId.EQEQ);
                    backup(1);
                    return token(JavaTokenId.EQ);

                case '>':
                    switch (nextChar()) {
                        case '>': // after >>
                            switch (c = nextChar()) {
                                case '>': // after >>>
                                    if (nextChar() == '=')
                                        return token(JavaTokenId.GTGTGTEQ);
                                    backup(1);
                                    return token(JavaTokenId.GTGTGT);
                                case '=': // >>=
                                    return token(JavaTokenId.GTGTEQ);
                            }
                            backup(1);
                            return token(JavaTokenId.GTGT);
                        case '=': // >=
                            return token(JavaTokenId.GTEQ);
                    }
                    backup(1);
                    return token(JavaTokenId.GT);

                case '<':
                    switch (nextChar()) {
                        case '<': // after <<
                            if (nextChar() == '=')
                                return token(JavaTokenId.LTLTEQ);
                            backup(1);
                            return token(JavaTokenId.LTLT);
                        case '=': // <=
                            return token(JavaTokenId.LTEQ);
                    }
                    backup(1);
                    return token(JavaTokenId.LT);

                case '+':
                    switch (nextChar()) {
                        case '+':
                            return token(JavaTokenId.PLUSPLUS);
                        case '=':
                            return token(JavaTokenId.PLUSEQ);
                    }
                    backup(1);
                    return token(JavaTokenId.PLUS);

                case '-':
                    switch (nextChar()) {
                        case '-':
                            return token(JavaTokenId.MINUSMINUS);
                        case '=':
                            return token(JavaTokenId.MINUSEQ);
                        case '>':
                            return token(JavaTokenId.ARROW);
                    }
                    backup(1);
                    return token(JavaTokenId.MINUS);

                case '*':
                    switch (nextChar()) {
                        case '/': // invalid comment end - */
                            return token(JavaTokenId.INVALID_COMMENT_END);
                        case '=':
                            return token(JavaTokenId.STAREQ);
                    }
                    backup(1);
                    return token(JavaTokenId.STAR);

                case '|':
                    switch (nextChar()) {
                        case '|':
                            return token(JavaTokenId.BARBAR);
                        case '=':
                            return token(JavaTokenId.BAREQ);
                    }
                    backup(1);
                    return token(JavaTokenId.BAR);

                case '&':
                    switch (nextChar()) {
                        case '&':
                            return token(JavaTokenId.AMPAMP);
                        case '=':
                            return token(JavaTokenId.AMPEQ);
                    }
                    backup(1);
                    return token(JavaTokenId.AMP);

                case '%':
                    if (nextChar() == '=')
                        return token(JavaTokenId.PERCENTEQ);
                    backup(1);
                    return token(JavaTokenId.PERCENT);

                case '^':
                    if (nextChar() == '=')
                        return token(JavaTokenId.CARETEQ);
                    backup(1);
                    return token(JavaTokenId.CARET);

                case '!':
                    if (nextChar() == '=')
                        return token(JavaTokenId.BANGEQ);
                    backup(1);
                    return token(JavaTokenId.BANG);

                case '.':
                    if ((c = nextChar()) == '.')
                        if (nextChar() == '.') { // ellipsis ...
                            return token(JavaTokenId.ELLIPSIS);
                        } else
                            backup(2);
                    else if ('0' <= c && c <= '9') { // float literal
                        return finishNumberLiteral(nextChar(), true);
                    } else
                        backup(1);
                    return token(JavaTokenId.DOT);

                case '~':
                    return token(JavaTokenId.TILDE);
                case ',':
                    return token(JavaTokenId.COMMA);
                case ';':
                    if (state != null) {
                        if (state >= 4 && state < 11) {
                            state = 3; // inside module decl
                        } else {
                            state = 1; // parsing module-info
                        }
                    }
                    return token(JavaTokenId.SEMICOLON);
                case ':':
                    if (nextChar() == ':')
                        return token(JavaTokenId.COLONCOLON);
                    backup(1);
                    return token(JavaTokenId.COLON);
                case '?':
                    return token(JavaTokenId.QUESTION);
                case '(':
                    if (state != null && state >= 12) {
                        state++;
                    }
                    return token(JavaTokenId.LPAREN);
                case ')':
                    if (state != null) {
                        if (state == 13) {
                            state = 1;
                        } else if (state > 13) {
                            state--;
                        }
                    }
                    return token(JavaTokenId.RPAREN);
                case '[':
                    return token(JavaTokenId.LBRACKET);
                case ']':
                    return token(JavaTokenId.RBRACKET);
                case '{':
                    if (pendingStringLiteral != null ) {
                        pendingBraces++;
                    }
                    if (state != null && state == 2) {
                        state = 3; // inside module decl
                    }
                    return token(JavaTokenId.LBRACE);
                case '}':
                    if (pendingStringLiteral != null && pendingBraces-- == 0) {
                        lookupId = pendingStringLiteral;
                        if (literalHistory == null) {
                            pendingStringLiteral = null;
                            pendingBraces = 0;
                        } else {
                            pendingStringLiteral = literalHistory.pendingStringLiteral;
                            pendingBraces = literalHistory.pendingBraces;
                            literalHistory = literalHistory.next;
                        }
                        stringLiteralContinuation = true;
                        break;
                    }
                    state = null;
                    return token(JavaTokenId.RBRACE);
                case '@':
                    if (state != null && state == 1) {
                        state = 12; // after annotation
                    }
                    return token(JavaTokenId.AT);

                case '0': // in a number literal
		    c = nextChar();
                    if (c == 'x' || c == 'X') { // in hexadecimal (possibly floating-point) literal
                        boolean inFraction = false;
                        boolean afterDigit = false;
                        while (true) {
                            switch (nextChar()) {
                                case '0': case '1': case '2': case '3': case '4':
                                case '5': case '6': case '7': case '8': case '9':
                                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                                case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                                    afterDigit = true;
                                    break;
                                case '.': // hex float literal
                                    if (!inFraction) {
                                        inFraction = true;
                                        afterDigit = false;
                                    } else { // two dots in the float literal
                                        return token(JavaTokenId.FLOAT_LITERAL_INVALID);
                                    }
                                    break;
                                case 'p': case 'P': // binary exponent
                                    return finishFloatExponent();
                                case 'l': case 'L':
                                    return token(JavaTokenId.LONG_LITERAL);
                                case '_':
                                    if (this.version >= 7 && afterDigit) {
                                        int cc = nextChar();
                                        backup(1);
                                        if (cc >= '0' && cc <= '9' || cc >= 'a' && cc <= 'f' || cc >= 'A' && cc <= 'F' || cc == '_') {
                                            break;
                                        }
                                    }
                                default:
                                    backup(1);
                                    // if float then before mandatory binary exponent => invalid
                                    return token(inFraction ? JavaTokenId.FLOAT_LITERAL_INVALID
                                            : JavaTokenId.INT_LITERAL);
                            }
                        } // end of while(true)
                    } else if (this.version >= 7 && (c == 'b' || c == 'B')) { // in binary literal
                        boolean afterDigit = false;
                        while (true) {
                            switch (nextChar()) {
                                case '0': case '1':
                                    afterDigit = true;
                                    break;
                                case 'l': case 'L':
                                    return token(JavaTokenId.LONG_LITERAL);
                                case '_':
                                    if (afterDigit) {
                                        int cc = nextChar();
                                        backup(1);
                                        if (cc == '0' || cc == '1' || cc == '_') {
                                            break;
                                        }
                                    }
                                default:
                                    backup(1);
                                    return token(JavaTokenId.INT_LITERAL);
                            }
                        }
                    }
                    return finishNumberLiteral(c, false);
                    
                case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    return finishNumberLiteral(nextChar(), false);

                    
                // Keywords lexing    
                case 'a':
                    switch (c = nextChar()) {
                        case 'b':
                            if ((c = nextChar()) == 's'
                             && (c = nextChar()) == 't'
                             && (c = nextChar()) == 'r'
                             && (c = nextChar()) == 'a'
                             && (c = nextChar()) == 'c'
                             && (c = nextChar()) == 't')
                                return keywordOrIdentifier(JavaTokenId.ABSTRACT);
                            break;
                        case 's':
                            if ((c = nextChar()) == 's'
                             && (c = nextChar()) == 'e'
                             && (c = nextChar()) == 'r'
                             && (c = nextChar()) == 't')
                                return (version >= 4)
                                        ? keywordOrIdentifier(JavaTokenId.ASSERT)
                                        : finishIdentifier();
                            break;
                    }
                    return finishIdentifier(c);

                case 'b':
                    switch (c = nextChar()) {
                        case 'o':
                            if ((c = nextChar()) == 'o'
                             && (c = nextChar()) == 'l'
                             && (c = nextChar()) == 'e'
                             && (c = nextChar()) == 'a'
                             && (c = nextChar()) == 'n')
                                return keywordOrIdentifier(JavaTokenId.BOOLEAN);
                            break;
                        case 'r':
                            if ((c = nextChar()) == 'e'
                             && (c = nextChar()) == 'a'
                             && (c = nextChar()) == 'k')
                                return keywordOrIdentifier(JavaTokenId.BREAK);
                            break;
                        case 'y':
                            if ((c = nextChar()) == 't'
                             && (c = nextChar()) == 'e')
                                return keywordOrIdentifier(JavaTokenId.BYTE);
                            break;
                    }
                    return finishIdentifier(c);

                case 'c':
                    switch (c = nextChar()) {
                        case 'a':
                            switch (c = nextChar()) {
                                case 's':
                                    if ((c = nextChar()) == 'e')
                                        return keywordOrIdentifier(JavaTokenId.CASE);
                                    break;
                                case 't':
                                    if ((c = nextChar()) == 'c'
                                     && (c = nextChar()) == 'h')
                                        return keywordOrIdentifier(JavaTokenId.CATCH);
                                    break;
                            }
                            break;
                        case 'h':
                            if ((c = nextChar()) == 'a'
                             && (c = nextChar()) == 'r')
                                return keywordOrIdentifier(JavaTokenId.CHAR);
                            break;
                        case 'l':
                            if ((c = nextChar()) == 'a'
                             && (c = nextChar()) == 's'
                             && (c = nextChar()) == 's')
                                return keywordOrIdentifier(JavaTokenId.CLASS);
                            break;
                        case 'o':
                            if ((c = nextChar()) == 'n') {
                                switch (c = nextChar()) {
                                    case 's':
                                        if ((c = nextChar()) == 't')
                                            return keywordOrIdentifier(JavaTokenId.CONST);
                                        break;
                                    case 't':
                                        if ((c = nextChar()) == 'i'
                                         && (c = nextChar()) == 'n'
                                         && (c = nextChar()) == 'u'
                                         && (c = nextChar()) == 'e')
                                            return keywordOrIdentifier(JavaTokenId.CONTINUE);
                                        break;
                                }
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'd':
                    switch (c = nextChar()) {
                        case 'e':
                            if ((c = nextChar()) == 'f'
                             && (c = nextChar()) == 'a'
                             && (c = nextChar()) == 'u'
                             && (c = nextChar()) == 'l'
                             && (c = nextChar()) == 't')
                                return keywordOrIdentifier(JavaTokenId.DEFAULT);
                            break;
                        case 'o':
                            switch (c = nextChar()) {
                                case 'u':
                                    if ((c = nextChar()) == 'b'
                                     && (c = nextChar()) == 'l'
                                     && (c = nextChar()) == 'e')
                                        return keywordOrIdentifier(JavaTokenId.DOUBLE);
                                    break;
                                default:
                                    return keywordOrIdentifier(JavaTokenId.DO, c);
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'e':
                    switch (c = nextChar()) {
                        case 'l':
                            if ((c = nextChar()) == 's'
                             && (c = nextChar()) == 'e')
                                return keywordOrIdentifier(JavaTokenId.ELSE);
                            break;
                        case 'n':
                            if ((c = nextChar()) == 'u'
                             && (c = nextChar()) == 'm')
                                return (version >= 5)
                                        ? keywordOrIdentifier(JavaTokenId.ENUM)
                                        : finishIdentifier();
                            break;
                        case 'x':
                            switch (c = nextChar()) {
                                case 'p':
                                    if ((c = nextChar()) == 'o'
                                     && (c = nextChar()) == 'r'
                                     && (c = nextChar()) == 't'
                                     && (c = nextChar()) == 's'
                                     && state != null && state == 3) {
                                        Token<JavaTokenId> kwOrId = keywordOrIdentifier(JavaTokenId.EXPORTS);
                                        if (kwOrId.id() == JavaTokenId.EXPORTS) {
                                            state = 5; // after exports
                                        }
                                        return kwOrId;
                                    }
                                    break;
                                case 't':
                                    if ((c = nextChar()) == 'e'
                                     && (c = nextChar()) == 'n'
                                     && (c = nextChar()) == 'd'
                                     && (c = nextChar()) == 's')
                                        return keywordOrIdentifier(JavaTokenId.EXTENDS);
                                    break;
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'f':
                    switch (c = nextChar()) {
                        case 'a':
                            if ((c = nextChar()) == 'l'
                             && (c = nextChar()) == 's'
                             && (c = nextChar()) == 'e')
                                return keywordOrIdentifier(JavaTokenId.FALSE);
                            break;
                        case 'i':
                            if ((c = nextChar()) == 'n'
                             && (c = nextChar()) == 'a'
                             && (c = nextChar()) == 'l')
                                switch (c = nextChar()) {
                                    case 'l':
                                        if ((c = nextChar()) == 'y')
                                            return keywordOrIdentifier(JavaTokenId.FINALLY);
                                        break;
                                    default:
                                        return keywordOrIdentifier(JavaTokenId.FINAL, c);
                                }
                            break;
                        case 'l':
                            if ((c = nextChar()) == 'o'
                             && (c = nextChar()) == 'a'
                             && (c = nextChar()) == 't')
                                return keywordOrIdentifier(JavaTokenId.FLOAT);
                            break;
                        case 'o':
                            if ((c = nextChar()) == 'r')
                                return keywordOrIdentifier(JavaTokenId.FOR);
                            break;
                    }
                    return finishIdentifier(c);

                case 'g':
                    if ((c = nextChar()) == 'o'
                     && (c = nextChar()) == 't'
                     && (c = nextChar()) == 'o')
                        return keywordOrIdentifier(JavaTokenId.GOTO);
                    return finishIdentifier(c);
                    
                case 'i':
                    switch (c = nextChar()) {
                        case 'f':
                            return keywordOrIdentifier(JavaTokenId.IF);
                        case 'm':
                            if ((c = nextChar()) == 'p') {
                                switch (c = nextChar()) {
                                    case 'l':
                                        if ((c = nextChar()) == 'e'
                                         && (c = nextChar()) == 'm'
                                         && (c = nextChar()) == 'e'
                                         && (c = nextChar()) == 'n'
                                         && (c = nextChar()) == 't'
                                         && (c = nextChar()) == 's')
                                            return keywordOrIdentifier(JavaTokenId.IMPLEMENTS);
                                        break;
                                    case 'o':
                                        if ((c = nextChar()) == 'r'
                                         && (c = nextChar()) == 't') {
                                            if (state != null && state == 1) {
                                                state = 11; // after import
                                            }
                                            return keywordOrIdentifier(JavaTokenId.IMPORT);
                                        }
                                        break;
                                }
                            }
                            break;
                        case 'n':
                            switch (c = nextChar()) {
                                case 's':
                                    if ((c = nextChar()) == 't'
                                     && (c = nextChar()) == 'a'
                                     && (c = nextChar()) == 'n'
                                     && (c = nextChar()) == 'c'
                                     && (c = nextChar()) == 'e'
                                     && (c = nextChar()) == 'o'
                                     && (c = nextChar()) == 'f')
                                        return keywordOrIdentifier(JavaTokenId.INSTANCEOF);
                                    break;
                                case 't':
                                    switch (c = nextChar()) {
                                        case 'e':
                                            if ((c = nextChar()) == 'r'
                                             && (c = nextChar()) == 'f'
                                             && (c = nextChar()) == 'a'
                                             && (c = nextChar()) == 'c'
                                             && (c = nextChar()) == 'e')
                                                return keywordOrIdentifier(JavaTokenId.INTERFACE);
                                            break;
                                        default:
                                            return keywordOrIdentifier(JavaTokenId.INT, c);
                                    }
                                    break;
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'l':
                    if ((c = nextChar()) == 'o'
                     && (c = nextChar()) == 'n'
                     && (c = nextChar()) == 'g')
                        return keywordOrIdentifier(JavaTokenId.LONG);
                    return finishIdentifier(c);

                case 'm':
                    if ((c = nextChar()) == 'o'
                     && (c = nextChar()) == 'd'
                     && (c = nextChar()) == 'u'
                     && (c = nextChar()) == 'l'
                     && (c = nextChar()) == 'e'
                     && state != null && state == 1) {
                        Token<JavaTokenId> kwOrId = keywordOrIdentifier(JavaTokenId.MODULE);
                        if (kwOrId.id() == JavaTokenId.MODULE) {
                            state = 2; // after module
                        }
                        return kwOrId;
                    }
                    return finishIdentifier(c);

                case 'n':
                    switch (c = nextChar()) {
                        case 'a':
                            if ((c = nextChar()) == 't'
                             && (c = nextChar()) == 'i'
                             && (c = nextChar()) == 'v'
                             && (c = nextChar()) == 'e')
                                return keywordOrIdentifier(JavaTokenId.NATIVE);
                            break;
                        case 'e':
                            if ((c = nextChar()) == 'w')
                                return keywordOrIdentifier(JavaTokenId.NEW);
                            break;
                        case 'u':
                            if ((c = nextChar()) == 'l'
                             && (c = nextChar()) == 'l')
                                return keywordOrIdentifier(JavaTokenId.NULL);
                            break;
                    }
                    return finishIdentifier(c);

                case 'o':
                    if ((c = nextChar()) == 'p'
                     && (c = nextChar()) == 'e'
                     && (c = nextChar()) == 'n'
                     && state != null && state >= 1)
                        switch (c = nextChar()) {
                            case 's':
                                if (state == 3) {
                                    Token<JavaTokenId> kwOrId = keywordOrIdentifier(JavaTokenId.OPENS);
                                    if (kwOrId.id() == JavaTokenId.OPENS) {
                                        state = 6; // after opens
                                    }
                                    return kwOrId;
                                }
                                break;
                            default:
                                if (state == 1) {
                                    return keywordOrIdentifier(JavaTokenId.OPEN, c);
                                }
                        }
                    return finishIdentifier(c);

                case 'p':
                    switch (c = nextChar()) {
                        case 'a':
                            if ((c = nextChar()) == 'c'
                             && (c = nextChar()) == 'k'
                             && (c = nextChar()) == 'a'
                             && (c = nextChar()) == 'g'
                             && (c = nextChar()) == 'e')
                                return keywordOrIdentifier(JavaTokenId.PACKAGE);
                            break;
                        case 'r':
                            switch (c = nextChar()) {
                                case 'i':
                                    if ((c = nextChar()) == 'v'
                                     && (c = nextChar()) == 'a'
                                     && (c = nextChar()) == 't'
                                     && (c = nextChar()) == 'e')
                                        return keywordOrIdentifier(JavaTokenId.PRIVATE);
                                    break;
                                case 'o':
                                    switch (c = nextChar()) {
                                        case 't':
                                            if ((c = nextChar()) == 'e'
                                             && (c = nextChar()) == 'c'
                                             && (c = nextChar()) == 't'
                                             && (c = nextChar()) == 'e'
                                             && (c = nextChar()) == 'd')
                                                return keywordOrIdentifier(JavaTokenId.PROTECTED);
                                            break;
                                        case 'v':
                                            if ((c = nextChar()) == 'i'
                                             && (c = nextChar()) == 'd'
                                             && (c = nextChar()) == 'e'
                                             && (c = nextChar()) == 's'
                                             && state != null && state == 3) {
                                                Token<JavaTokenId> kwOrId = keywordOrIdentifier(JavaTokenId.PROVIDES);
                                                if (kwOrId.id() == JavaTokenId.PROVIDES) {
                                                    state = 8; // after provides
                                                }
                                                return kwOrId;
                                            }
                                            break;
                                    }
                                    break;
                            }
                            break;
                        case 'u':
                            if ((c = nextChar()) == 'b'
                             && (c = nextChar()) == 'l'
                             && (c = nextChar()) == 'i'
                             && (c = nextChar()) == 'c')
                                return keywordOrIdentifier(JavaTokenId.PUBLIC);
                            break;
                    }
                    return finishIdentifier(c);

                case 'r':
                    if ((c = nextChar()) == 'e') {
                        switch (c = nextChar()) {
                            case 'q':
                                if ((c = nextChar()) == 'u'
                                 && (c = nextChar()) == 'i'
                                 && (c = nextChar()) == 'r'
                                 && (c = nextChar()) == 'e'
                                 && (c = nextChar()) == 's'
                                 && state != null && state == 3) {
                                    Token<JavaTokenId> kwOrId = keywordOrIdentifier(JavaTokenId.REQUIRES);
                                    if (kwOrId.id() == JavaTokenId.REQUIRES) {
                                        state = 4; // after requires
                                    }
                                    return kwOrId;
                                }
                                break;
                            case 't':    
                                if ((c = nextChar()) == 'u'
                                 && (c = nextChar()) == 'r'
                                 && (c = nextChar()) == 'n')
                                    return keywordOrIdentifier(JavaTokenId.RETURN);
                                break;
                        }
                    }
                    return finishIdentifier(c);

                case 's':
                    switch (c = nextChar()) {
                        case 'h':
                            if ((c = nextChar()) == 'o'
                             && (c = nextChar()) == 'r'
                             && (c = nextChar()) == 't')
                                return keywordOrIdentifier(JavaTokenId.SHORT);
                            break;
                        case 't':
                            switch (c = nextChar()) {
                                case 'a':
                                    if ((c = nextChar()) == 't'
                                     && (c = nextChar()) == 'i'
                                     && (c = nextChar()) == 'c')
                                        return keywordOrIdentifier(JavaTokenId.STATIC);
                                    break;
                                case 'r':
                                    if ((c = nextChar()) == 'i'
                                     && (c = nextChar()) == 'c'
                                     && (c = nextChar()) == 't'
                                     && (c = nextChar()) == 'f'
                                     && (c = nextChar()) == 'p')
                                        return keywordOrIdentifier(JavaTokenId.STRICTFP);
                                    break;
                            }
                            break;
                        case 'u':
                            if ((c = nextChar()) == 'p'
                             && (c = nextChar()) == 'e'
                             && (c = nextChar()) == 'r')
                                return keywordOrIdentifier(JavaTokenId.SUPER);
                            break;
                        case 'w':
                            if ((c = nextChar()) == 'i'
                             && (c = nextChar()) == 't'
                             && (c = nextChar()) == 'c'
                             && (c = nextChar()) == 'h')
                                return keywordOrIdentifier(JavaTokenId.SWITCH);
                            break;
                        case 'y':
                            if ((c = nextChar()) == 'n'
                             && (c = nextChar()) == 'c'
                             && (c = nextChar()) == 'h'
                             && (c = nextChar()) == 'r'
                             && (c = nextChar()) == 'o'
                             && (c = nextChar()) == 'n'
                             && (c = nextChar()) == 'i'
                             && (c = nextChar()) == 'z'
                             && (c = nextChar()) == 'e'
                             && (c = nextChar()) == 'd')
                                return keywordOrIdentifier(JavaTokenId.SYNCHRONIZED);
                            break;
                    }
                    return finishIdentifier(c);

                case 't':
                    switch (c = nextChar()) {
                        case 'h':
                            switch (c = nextChar()) {
                                case 'i':
                                    if ((c = nextChar()) == 's')
                                        return keywordOrIdentifier(JavaTokenId.THIS);
                                    break;
                                case 'r':
                                    if ((c = nextChar()) == 'o'
                                     && (c = nextChar()) == 'w')
                                        switch (c = nextChar()) {
                                            case 's':
                                                return keywordOrIdentifier(JavaTokenId.THROWS);
                                            default:
                                                return keywordOrIdentifier(JavaTokenId.THROW, c);
                                        }
                                    break;
                            }
                            break;
                        case 'o':
                            if (state != null && (state == 5 || state == 6)) {
                                Token<JavaTokenId> kwOrId = keywordOrIdentifier(JavaTokenId.TO);
                                if (kwOrId.id() == JavaTokenId.TO) {
                                    state = 9; // after to
                                }
                                return kwOrId;
                            }
                            break;
                        case 'r':
                            switch (c = nextChar()) {
                                case 'a':
                                    if ((c = nextChar()) == 'n'
                                     && (c = nextChar()) == 's'
                                     && (c = nextChar()) == 'i') {
                                        switch (c = nextChar()) {
                                            case 'e':
                                                if ((c = nextChar()) == 'n'
                                                 && (c = nextChar()) == 't')
                                                    return keywordOrIdentifier(JavaTokenId.TRANSIENT);
                                                break;
                                            case 't':
                                                if ((c = nextChar()) == 'i'
                                                 && (c = nextChar()) == 'v'
                                                 && (c = nextChar()) == 'e'
                                                 && state != null && state == 4)
                                                    return keywordOrIdentifier(JavaTokenId.TRANSITIVE);
                                                break;
                                        }
                                    }
                                    break;
                                case 'u':
                                    if ((c = nextChar()) == 'e')
                                        return keywordOrIdentifier(JavaTokenId.TRUE);
                                    break;
                                case 'y':
                                    return keywordOrIdentifier(JavaTokenId.TRY);
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'u':
                    if ((c = nextChar()) == 's'
                     && (c = nextChar()) == 'e'
                     && (c = nextChar()) == 's'
                     && state != null && state == 3) {
                        Token<JavaTokenId> kwOrId = keywordOrIdentifier(JavaTokenId.USES);
                        if (kwOrId.id() == JavaTokenId.USES) {
                            state = 7; // after uses
                        }
                        return kwOrId;
                    }
                    return finishIdentifier(c);

                case 'v':
                    switch ((c = nextChar())) {
                        case 'a':
                            if ((c = nextChar()) == 'r') {
                                c = nextChar();
                                // Check whether the given char is non-ident and if so then return keyword
                                if (c != EOF && !Character.isJavaIdentifierPart(c = translateSurrogates(c)) &&
                                    version >= 10) {
                                    // For surrogate 2 chars must be backed up
                                    backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);

                                    int len = input.readLength();

                                    Token next = nextToken();
                                    boolean varKeyword = false;

                                    if (AFTER_VAR_TOKENS.contains(next.id())) {
                                        do {
                                            next = nextToken();
                                        } while (next != null && AFTER_VAR_TOKENS.contains(next.id()));

                                        varKeyword = next != null && next.id() == JavaTokenId.IDENTIFIER;
                                    }

                                    input.backup(input.readLengthEOF()- len);

                                    assert input.readLength() == len;

                                    if (varKeyword) {
                                        return token(JavaTokenId.VAR);
                                    }
                                } else {
                                    // For surrogate 2 chars must be backed up
                                    backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
                                }
                            }
                            c = nextChar();
                            break;
                        case 'o':
                            switch (c = nextChar()) {
                                case 'i':
                                    if ((c = nextChar()) == 'd')
                                        return keywordOrIdentifier(JavaTokenId.VOID);
                                    break;
                                case 'l':
                                    if ((c = nextChar()) == 'a'
                                     && (c = nextChar()) == 't'
                                     && (c = nextChar()) == 'i'
                                     && (c = nextChar()) == 'l'
                                     && (c = nextChar()) == 'e')
                                        return keywordOrIdentifier(JavaTokenId.VOLATILE);
                                    break;
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'w':
                    switch (c = nextChar()) {
                        case 'h':
                            if ((c = nextChar()) == 'i'
                             && (c = nextChar()) == 'l'
                             && (c = nextChar()) == 'e')
                                return keywordOrIdentifier(JavaTokenId.WHILE);
                            break;
                        case 'i':
                            if ((c = nextChar()) == 't'
                             && (c = nextChar()) == 'h'
                             && state != null && state == 8) {
                                Token<JavaTokenId> kwOrId = keywordOrIdentifier(JavaTokenId.WITH);
                                if (kwOrId.id() == JavaTokenId.WITH) {
                                    state = 10; // after with
                                }
                                return kwOrId;
                            }
                            break;
                    }
                    return finishIdentifier(c);

                // Rest of lowercase letters starting identifiers
                case 'h': case 'j': case 'k':
                case 'q': case 'x': case 'y': case 'z':
                // Uppercase letters starting identifiers
                case 'A': case 'B': case 'C': case 'D': case 'E':
                case 'F': case 'G': case 'H': case 'I': case 'J':
                case 'K': case 'L': case 'M': case 'N': case 'O':
                case 'P': case 'Q': case 'R': case 'S': case 'T':
                case 'U': case 'V': case 'W': case 'X': case 'Y':
                case 'Z':
                case '$':
                    return finishIdentifier();
                    
                case '_':
                    if (this.version >= 9)
                        return keywordOrIdentifier(JavaTokenId.UNDERSCORE);
                    return finishIdentifier();
                    
                // All Character.isWhitespace(c) below 0x80 follow
                // ['\t' - '\r'] and [0x1c - ' ']
                case '\t':
                case '\n':
                case 0x0b:
                case '\f':
                case '\r':
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:
                    if (state != null && state >= 12) {
                        state = 1;
                    }
                    return finishWhitespace();
                case ' ':
                    c = nextChar();
                    if (c == EOF || !Character.isWhitespace(c)) { // Return single space as flyweight token
                        backup(1);
                        return   input.readLength() == 1
                               ? tokenFactory.getFlyweightToken(JavaTokenId.WHITESPACE, " ")
                               : tokenFactory.createToken(JavaTokenId.WHITESPACE);
                    }
                    return finishWhitespace();

                case EOF:
                    return null;

                default:
                    if (c >= 0x80) { // lowSurr ones already handled above
                        c = translateSurrogates(c);
                        if (Character.isJavaIdentifierStart(c))
                            return finishIdentifier();
                        if (Character.isWhitespace(c))
                            return finishWhitespace();
                    }

                    // Invalid char
                    return token(JavaTokenId.ERROR);
            } // end of switch (c)
        } // end of while(true)
    }
    
    private int translateSurrogates(int c) {
        if (Character.isHighSurrogate((char)c)) {
            int lowSurr = nextChar();
            if (lowSurr != EOF && Character.isLowSurrogate((char)lowSurr)) {
                // c and lowSurr form the integer unicode char.
                c = Character.toCodePoint((char)c, (char)lowSurr);
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

    private Token<JavaTokenId> finishWhitespace() {
        while (true) {
            int c = nextChar();
            // There should be no surrogates possible for whitespace
            // so do not call translateSurrogates()
            if (c == EOF || !Character.isWhitespace(c)) {
                backup(1);
                return tokenFactory.createToken(JavaTokenId.WHITESPACE);
            }
        }
    }
    
    private Token<JavaTokenId> finishIdentifier() {
        return finishIdentifier(nextChar());
    }
    
    private Token<JavaTokenId> finishIdentifier(int c) {
        while (true) {
            if (c == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c))) {
                // For surrogate 2 chars must be backed up
                backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
                return tokenFactory.createToken(JavaTokenId.IDENTIFIER);
            }
            c = nextChar();
        }
    }

    private Token<JavaTokenId> keywordOrIdentifier(JavaTokenId keywordId) {
        return keywordOrIdentifier(keywordId, nextChar());
    }

    private Token<JavaTokenId> keywordOrIdentifier(JavaTokenId keywordId, int c) {
        // Check whether the given char is non-ident and if so then return keyword
        if (c == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c))) {
            // For surrogate 2 chars must be backed up
            backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
            return token(keywordId);
        } else // c is identifier part
            return finishIdentifier();
    }
    
    private Token<JavaTokenId> finishNumberLiteral(int c, boolean inFraction) {
        boolean afterDigit = true;
        while (true) {
            switch (c) {
                case '.':
                    if (!inFraction) {
                        inFraction = true;
                        afterDigit = false;
                    } else { // two dots in the literal
                        return token(JavaTokenId.FLOAT_LITERAL_INVALID);
                    }
                    break;
                case 'l': case 'L': // 0l or 0L
                    return token(JavaTokenId.LONG_LITERAL);
                case 'd': case 'D':
                    return token(JavaTokenId.DOUBLE_LITERAL);
                case 'f': case 'F':
                    return token(JavaTokenId.FLOAT_LITERAL);
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    afterDigit = true;
                    break;
                case 'e': case 'E': // exponent part
                    return finishFloatExponent();
                case '_':
                    if (this.version >= 7 && afterDigit) {
                        int cc = nextChar();
                        backup(1);
                        if (cc >= '0' && cc <= '9' || cc == '_') {
                            break;
                        }
                    }
                default:
                    backup(1);
                    return token(inFraction ? JavaTokenId.DOUBLE_LITERAL
                            : JavaTokenId.INT_LITERAL);
            }
            c = nextChar();
        }
    }
    
    private Token<JavaTokenId> finishFloatExponent() {
        int c = nextChar();
        if (c == '+' || c == '-') {
            c = nextChar();
        }
        if (c < '0' || '9' < c)
            return token(JavaTokenId.FLOAT_LITERAL_INVALID);
        do {
            c = nextChar();
        } while ('0' <= c && c <= '9'); // reading exponent
        switch (c) {
            case 'd': case 'D':
                return token(JavaTokenId.DOUBLE_LITERAL);
            case 'f': case 'F':
                return token(JavaTokenId.FLOAT_LITERAL);
            default:
                backup(1);
                return token(JavaTokenId.DOUBLE_LITERAL);
        }
    }
    
    private Token<JavaTokenId> token(JavaTokenId id) {
        return token(id, PartType.COMPLETE);
    }

    private Token<JavaTokenId> token(JavaTokenId id, PartType partType) {
        String fixedText = id.fixedText();
        return (fixedText != null && fixedText.length() == input.readLength() && partType == PartType.COMPLETE)
                ? tokenFactory.getFlyweightToken(id, fixedText)
                : partType == PartType.COMPLETE ? tokenFactory.createToken(id)
                                                : tokenFactory.createToken(id, input.readLength(), partType);
    }

    private static final Set<JavaTokenId> AFTER_VAR_TOKENS = EnumSet.of(
            JavaTokenId.BLOCK_COMMENT, JavaTokenId.JAVADOC_COMMENT,
            JavaTokenId.LINE_COMMENT, JavaTokenId.WHITESPACE
    );

    // Get version as Integer x for version String 1.x
    private Integer getVersionAsInt(String version) {
        Integer ver = null;
        if (version != null) {
            try {
                // expect format 1.x or x
                if (version.startsWith("1.")) { //NOI18N
                    ver = Integer.parseInt(version.substring(2));
                } else {
                    ver = Integer.parseInt(version);
                }
            } catch (NumberFormatException e) {
                // should not happen if version is
                // set using SourceLevelQuery,
                // ignore other strings
            }
        }
        return ver;
    }

    public void release() {
    }

}
