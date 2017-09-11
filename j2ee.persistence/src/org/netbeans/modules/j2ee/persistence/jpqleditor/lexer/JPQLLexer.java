/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.jpqleditor.lexer;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author sp153251
 */
public class JPQLLexer implements Lexer<JPQLTokenId>{
    private LexerRestartInfo<JPQLTokenId> info;
    
    private static final int EOF = LexerInput.EOF;

    private final LexerInput input;
    
    private final TokenFactory<JPQLTokenId> tokenFactory;
    

    JPQLLexer(LexerRestartInfo<JPQLTokenId> info) {
        this.info = info;
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        //JavaCharStream stream = new JavaCharStream(info.input());
        //javaParserTokenManager = new JavaParserTokenManager(stream);
    }

    @Override
    public org.netbeans.api.lexer.Token<JPQLTokenId> nextToken() {
        while(true) {
            int c = input.read();
            JPQLTokenId lookupId = null;
            switch (c) {
                case '"': // string literal
                    if (lookupId == null) {
                        lookupId = JPQLTokenId.STRING_LITERAL;
                    }
                    while (true) {
                        switch (input.read()) {
                            case '"': // NOI18N
                                return token(lookupId);
                            case '\\':
                                input.read();
                                break;
                            case '\r': input.consumeNewline();
                            case '\n':
                            case EOF:
                                return tokenFactory.createToken(lookupId, //XXX: \n handling for exotic identifiers?
                                        input.readLength(), PartType.START);
                        }
                    }
                case '\'': // char literal
                    if (lookupId == null) {
                        lookupId = JPQLTokenId.STRING_LITERAL;
                    }
                    while (true)
                        switch (input.read()) {
                            case '\'': // NOI18N
                                return token(lookupId);
                            case '\\':
                                input.read(); // read escaped char
                                break;
                            case '\r': input.consumeNewline();
                            case '\n':
                            case EOF:
                                return tokenFactory.createToken(lookupId, //XXX: \n handling for exotic identifiers?
                                        input.readLength(), PartType.START);
                        }
//                case '=':
//                    input.backup(1);
//                    return token(JPQLTokenId.EQ);

                case '>':
                    switch (input.read()) {
                        case '=': // >=
                            return token(JPQLTokenId.GTEQ);
                    }
                    input.backup(1);
                    return token(JPQLTokenId.GT);

                case '<':
                    switch (input.read()) {
                        case '=': // <=
                            return token(JPQLTokenId.LTEQ);
                    }
                    input.backup(1);
                    return token(JPQLTokenId.LT);

                case '+':
                    switch (input.read()) {
                        case '=':
                            return token(JPQLTokenId.PLUSEQ);
                    }
                    input.backup(1);
                    return token(JPQLTokenId.PLUS);

                case '-':
                    switch (input.read()) {
                        case '=':
                            return token(JPQLTokenId.MINUSEQ);
                    }
                    input.backup(1);
                    return token(JPQLTokenId.MINUS);

//                case '*':
//                    input.backup(1);
//                    return token(JPQLTokenId.STAR);
//
//                case '|':
//                    input.backup(1);
//                    return token(JPQLTokenId.BAR);

                case '&':
                    switch (input.read()) {
                        case '&':
                            return token(JPQLTokenId.AMPAMP);
                    }
                    input.backup(1);
                    return token(JPQLTokenId.AMP);

//                case '%':
//                    input.backup(1);
//                    return token(JPQLTokenId.PERCENT);
//
//                case '^':
//                    input.backup(1);
//                    return token(JPQLTokenId.CARET);
//
//                case '!':
//                    input.backup(1);
//                    return token(JPQLTokenId.BANG);

                case '.':
                    if ((c = input.read()) == '.') {
                        if (input.read() == '.') { // ellipsis ...
                            return token(JPQLTokenId.ELLIPSIS);
                        } else {
                            input.backup(2);
                        }
                    }
                    else if ('0' <= c && c <= '9') { // float literal
                        return finishNumberLiteral(input.read(), true);
                    } else {
                        input.backup(1);
                    }
                    return token(JPQLTokenId.DOT);

                case '~':
                    return token(JPQLTokenId.TILDE);
                case ',':
                    return token(JPQLTokenId.COMMA);
                case ';':
                    return token(JPQLTokenId.SEMICOLON);
                case ':':
                    return token(JPQLTokenId.COLON);
                case '?':
                    return token(JPQLTokenId.QUESTION);
                case '(':
                    return token(JPQLTokenId.LPAREN);
                case ')':
                    return token(JPQLTokenId.RPAREN);
                case '[':
                    return token(JPQLTokenId.LBRACKET);
                case ']':
                    return token(JPQLTokenId.RBRACKET);
                case '{':
                    return token(JPQLTokenId.LBRACE);
                case '}':
                    return token(JPQLTokenId.RBRACE);
                case '@':
                    return token(JPQLTokenId.AT);

                case '0': // in a number literal
		    c = input.read();
                    if (c == 'x' || c == 'X') { // in hexadecimal (possibly floating-point) literal
                        boolean inFraction = false;
                        while (true) {
                            switch (input.read()) {
                                case '0': case '1': case '2': case '3': case '4':
                                case '5': case '6': case '7': case '8': case '9':
                                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                                case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                                    break;
                                case '.': // hex float literal
                                    if (!inFraction) {
                                        inFraction = true;
                                    } else { // two dots in the float literal
                                        return token(JPQLTokenId.ERROR);
                                    }
                                    break;
                                case 'p': case 'P': // binary exponent
                                    return finishFloatExponent();
                                default:
                                    input.backup(1);
                                    // if float then before mandatory binary exponent => invalid
                                    return token(inFraction ? JPQLTokenId.ERROR
                                            : JPQLTokenId.INT_LITERAL);
                            }
                        } // end of while(true)
                    }
                    return finishNumberLiteral(c, false);
                    
                case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    return finishNumberLiteral(input.read(), false);

                    
                // Keywords lexing    
                case 'a':
                case 'A':
                    switch (c = input.read()) {
                        case 'b':
                        case 'B':
                            if ((c = input.read()) == 's' || c=='S') {
                                return keywordOrIdentifier(JPQLTokenId.ABS);
                            }
                            break;
                        case 's':
                        case 'S':
                            if ((c = input.read()) == 'c' || c=='C') {
                                return keywordOrIdentifier(JPQLTokenId.ASC);
                            }
                            break;
                    }
                    return finishIdentifier(c);

//                case 'b':
//                case 'B':
//                    switch (c = input.read()) {
//                        case 'e':
//                        case 'E':
//                            if ((c = input.read()) == 'o'
//                             && (c = input.read()) == 'l'
//                             && (c = input.read()) == 'e'
//                             && (c = input.read()) == 'a'
//                             && (c = input.read()) == 'n')
//                                return keywordOrIdentifier(JPQLTokenId.BOOLEAN);
//                            break;
//                        case 'r':
//                            if ((c = input.read()) == 'e'
//                             && (c = input.read()) == 'a'
//                             && (c = input.read()) == 'k')
//                                return keywordOrIdentifier(JPQLTokenId.BREAK);
//                            break;
//                        case 'y':
//                            if ((c = input.read()) == 't'
//                             && (c = input.read()) == 'e')
//                                return keywordOrIdentifier(JPQLTokenId.BYTE);
//                            break;
//                    }
//                    return finishIdentifier(c);
//
//                case 'c':
//                    switch (c = input.read()) {
//                        case 'a':
//                            switch (c = input.read()) {
//                                case 's':
//                                    if ((c = input.read()) == 'e')
//                                        return keywordOrIdentifier(JPQLTokenId.CASE);
//                                    break;
//                                case 't':
//                                    if ((c = input.read()) == 'c'
//                                     && (c = input.read()) == 'h')
//                                        return keywordOrIdentifier(JPQLTokenId.CATCH);
//                                    break;
//                            }
//                            break;
//                        case 'h':
//                            if ((c = input.read()) == 'a'
//                             && (c = input.read()) == 'r')
//                                return keywordOrIdentifier(JPQLTokenId.CHAR);
//                            break;
//                        case 'l':
//                            if ((c = input.read()) == 'a'
//                             && (c = input.read()) == 's'
//                             && (c = input.read()) == 's')
//                                return keywordOrIdentifier(JPQLTokenId.CLASS);
//                            break;
//                        case 'o':
//                            if ((c = input.read()) == 'n') {
//                                switch (c = input.read()) {
//                                    case 's':
//                                        if ((c = input.read()) == 't')
//                                            return keywordOrIdentifier(JPQLTokenId.CONST);
//                                        break;
//                                    case 't':
//                                        if ((c = input.read()) == 'i'
//                                         && (c = input.read()) == 'n'
//                                         && (c = input.read()) == 'u'
//                                         && (c = input.read()) == 'e')
//                                            return keywordOrIdentifier(JPQLTokenId.CONTINUE);
//                                        break;
//                                }
//                            }
//                            break;
//                    }
//                    return finishIdentifier(c);
//
//                case 'd':
//                    switch (c = input.read()) {
//                        case 'e':
//                            if ((c = input.read()) == 'f'
//                             && (c = input.read()) == 'a'
//                             && (c = input.read()) == 'u'
//                             && (c = input.read()) == 'l'
//                             && (c = input.read()) == 't')
//                                return keywordOrIdentifier(JPQLTokenId.DEFAULT);
//                            break;
//                        case 'o':
//                            switch (c = input.read()) {
//                                case 'u':
//                                    if ((c = input.read()) == 'b'
//                                     && (c = input.read()) == 'l'
//                                     && (c = input.read()) == 'e')
//                                        return keywordOrIdentifier(JPQLTokenId.DOUBLE);
//                                    break;
//                                default:
//                                    return keywordOrIdentifier(JPQLTokenId.DO, c);
//                            }
//                            break;
//                    }
//                    return finishIdentifier(c);
//
//                case 'e':
//                    switch (c = input.read()) {
//                        case 'l':
//                            if ((c = input.read()) == 's'
//                             && (c = input.read()) == 'e')
//                                return keywordOrIdentifier(JPQLTokenId.ELSE);
//                            break;
//                        case 'n':
//                            if ((c = input.read()) == 'u'
//                             && (c = input.read()) == 'm')
//                                return (version >= 5)
//                                        ? keywordOrIdentifier(JPQLTokenId.ENUM)
//                                        : finishIdentifier();
//                            break;
//                        case 'x':
//                            if ((c = input.read()) == 't'
//                             && (c = input.read()) == 'e'
//                             && (c = input.read()) == 'n'
//                             && (c = input.read()) == 'd'
//                             && (c = input.read()) == 's')
//                                return keywordOrIdentifier(JPQLTokenId.EXTENDS);
//                            break;
//                    }
//                    return finishIdentifier(c);
//
//                case 'f':
//                    switch (c = input.read()) {
//                        case 'a':
//                            if ((c = input.read()) == 'l'
//                             && (c = input.read()) == 's'
//                             && (c = input.read()) == 'e')
//                                return keywordOrIdentifier(JPQLTokenId.FALSE);
//                            break;
//                        case 'i':
//                            if ((c = input.read()) == 'n'
//                             && (c = input.read()) == 'a'
//                             && (c = input.read()) == 'l')
//                                switch (c = input.read()) {
//                                    case 'l':
//                                        if ((c = input.read()) == 'y')
//                                            return keywordOrIdentifier(JPQLTokenId.FINALLY);
//                                        break;
//                                    default:
//                                        return keywordOrIdentifier(JPQLTokenId.FINAL, c);
//                                }
//                            break;
//                        case 'l':
//                            if ((c = input.read()) == 'o'
//                             && (c = input.read()) == 'a'
//                             && (c = input.read()) == 't')
//                                return keywordOrIdentifier(JPQLTokenId.FLOAT);
//                            break;
//                        case 'o':
//                            if ((c = input.read()) == 'r')
//                                return keywordOrIdentifier(JPQLTokenId.FOR);
//                            break;
//                    }
//                    return finishIdentifier(c);
//
//                case 'g':
//                    if ((c = input.read()) == 'o'
//                     && (c = input.read()) == 't'
//                     && (c = input.read()) == 'o')
//                        return keywordOrIdentifier(JPQLTokenId.GOTO);
//                    return finishIdentifier(c);
//                    
//                case 'i':
//                    switch (c = input.read()) {
//                        case 'f':
//                            return keywordOrIdentifier(JPQLTokenId.IF);
//                        case 'm':
//                            if ((c = input.read()) == 'p') {
//                                switch (c = input.read()) {
//                                    case 'l':
//                                        if ((c = input.read()) == 'e'
//                                         && (c = input.read()) == 'm'
//                                         && (c = input.read()) == 'e'
//                                         && (c = input.read()) == 'n'
//                                         && (c = input.read()) == 't'
//                                         && (c = input.read()) == 's')
//                                            return keywordOrIdentifier(JPQLTokenId.IMPLEMENTS);
//                                        break;
//                                    case 'o':
//                                        if ((c = input.read()) == 'r'
//                                         && (c = input.read()) == 't')
//                                            return keywordOrIdentifier(JPQLTokenId.IMPORT);
//                                        break;
//                                }
//                            }
//                            break;
//                        case 'n':
//                            switch (c = input.read()) {
//                                case 's':
//                                    if ((c = input.read()) == 't'
//                                     && (c = input.read()) == 'a'
//                                     && (c = input.read()) == 'n'
//                                     && (c = input.read()) == 'c'
//                                     && (c = input.read()) == 'e'
//                                     && (c = input.read()) == 'o'
//                                     && (c = input.read()) == 'f')
//                                        return keywordOrIdentifier(JPQLTokenId.INSTANCEOF);
//                                    break;
//                                case 't':
//                                    switch (c = input.read()) {
//                                        case 'e':
//                                            if ((c = input.read()) == 'r'
//                                             && (c = input.read()) == 'f'
//                                             && (c = input.read()) == 'a'
//                                             && (c = input.read()) == 'c'
//                                             && (c = input.read()) == 'e')
//                                                return keywordOrIdentifier(JPQLTokenId.INTERFACE);
//                                            break;
//                                        default:
//                                            return keywordOrIdentifier(JPQLTokenId.INT, c);
//                                    }
//                                    break;
//                            }
//                            break;
//                    }
//                    return finishIdentifier(c);
//
//                case 'l':
//                    if ((c = input.read()) == 'o'
//                     && (c = input.read()) == 'n'
//                     && (c = input.read()) == 'g')
//                        return keywordOrIdentifier(JPQLTokenId.LONG);
//                    return finishIdentifier(c);
//
//                case 'n':
//                    switch (c = input.read()) {
//                        case 'a':
//                            if ((c = input.read()) == 't'
//                             && (c = input.read()) == 'i'
//                             && (c = input.read()) == 'v'
//                             && (c = input.read()) == 'e')
//                                return keywordOrIdentifier(JPQLTokenId.NATIVE);
//                            break;
//                        case 'e':
//                            if ((c = input.read()) == 'w')
//                                return keywordOrIdentifier(JPQLTokenId.NEW);
//                            break;
//                        case 'u':
//                            if ((c = input.read()) == 'l'
//                             && (c = input.read()) == 'l')
//                                return keywordOrIdentifier(JPQLTokenId.NULL);
//                            break;
//                    }
//                    return finishIdentifier(c);
//
//                case 'p':
//                    switch (c = input.read()) {
//                        case 'a':
//                            if ((c = input.read()) == 'c'
//                             && (c = input.read()) == 'k'
//                             && (c = input.read()) == 'a'
//                             && (c = input.read()) == 'g'
//                             && (c = input.read()) == 'e')
//                                return keywordOrIdentifier(JPQLTokenId.PACKAGE);
//                            break;
//                        case 'r':
//                            switch (c = input.read()) {
//                                case 'i':
//                                    if ((c = input.read()) == 'v'
//                                     && (c = input.read()) == 'a'
//                                     && (c = input.read()) == 't'
//                                     && (c = input.read()) == 'e')
//                                        return keywordOrIdentifier(JPQLTokenId.PRIVATE);
//                                    break;
//                                case 'o':
//                                    if ((c = input.read()) == 't'
//                                     && (c = input.read()) == 'e'
//                                     && (c = input.read()) == 'c'
//                                     && (c = input.read()) == 't'
//                                     && (c = input.read()) == 'e'
//                                     && (c = input.read()) == 'd')
//                                        return keywordOrIdentifier(JPQLTokenId.PROTECTED);
//                                    break;
//                            }
//                            break;
//                        case 'u':
//                            if ((c = input.read()) == 'b'
//                             && (c = input.read()) == 'l'
//                             && (c = input.read()) == 'i'
//                             && (c = input.read()) == 'c')
//                                return keywordOrIdentifier(JPQLTokenId.PUBLIC);
//                            break;
//                    }
//                    return finishIdentifier(c);
//
//                case 'r':
//                    if ((c = input.read()) == 'e'
//                     && (c = input.read()) == 't'
//                     && (c = input.read()) == 'u'
//                     && (c = input.read()) == 'r'
//                     && (c = input.read()) == 'n')
//                        return keywordOrIdentifier(JPQLTokenId.RETURN);
//                    return finishIdentifier(c);
//
//                case 's':
//                    switch (c = input.read()) {
//                        case 'h':
//                            if ((c = input.read()) == 'o'
//                             && (c = input.read()) == 'r'
//                             && (c = input.read()) == 't')
//                                return keywordOrIdentifier(JPQLTokenId.SHORT);
//                            break;
//                        case 't':
//                            switch (c = input.read()) {
//                                case 'a':
//                                    if ((c = input.read()) == 't'
//                                     && (c = input.read()) == 'i'
//                                     && (c = input.read()) == 'c')
//                                        return keywordOrIdentifier(JPQLTokenId.STATIC);
//                                    break;
//                                case 'r':
//                                    if ((c = input.read()) == 'i'
//                                     && (c = input.read()) == 'c'
//                                     && (c = input.read()) == 't'
//                                     && (c = input.read()) == 'f'
//                                     && (c = input.read()) == 'p')
//                                        return keywordOrIdentifier(JPQLTokenId.STRICTFP);
//                                    break;
//                            }
//                            break;
//                        case 'u':
//                            if ((c = input.read()) == 'p'
//                             && (c = input.read()) == 'e'
//                             && (c = input.read()) == 'r')
//                                return keywordOrIdentifier(JPQLTokenId.SUPER);
//                            break;
//                        case 'w':
//                            if ((c = input.read()) == 'i'
//                             && (c = input.read()) == 't'
//                             && (c = input.read()) == 'c'
//                             && (c = input.read()) == 'h')
//                                return keywordOrIdentifier(JPQLTokenId.SWITCH);
//                            break;
//                        case 'y':
//                            if ((c = input.read()) == 'n'
//                             && (c = input.read()) == 'c'
//                             && (c = input.read()) == 'h'
//                             && (c = input.read()) == 'r'
//                             && (c = input.read()) == 'o'
//                             && (c = input.read()) == 'n'
//                             && (c = input.read()) == 'i'
//                             && (c = input.read()) == 'z'
//                             && (c = input.read()) == 'e'
//                             && (c = input.read()) == 'd')
//                                return keywordOrIdentifier(JPQLTokenId.SYNCHRONIZED);
//                            break;
//                    }
//                    return finishIdentifier(c);
//
//                case 't':
//                    switch (c = input.read()) {
//                        case 'h':
//                            switch (c = input.read()) {
//                                case 'i':
//                                    if ((c = input.read()) == 's')
//                                        return keywordOrIdentifier(JPQLTokenId.THIS);
//                                    break;
//                                case 'r':
//                                    if ((c = input.read()) == 'o'
//                                     && (c = input.read()) == 'w')
//                                        switch (c = input.read()) {
//                                            case 's':
//                                                return keywordOrIdentifier(JPQLTokenId.THROWS);
//                                            default:
//                                                return keywordOrIdentifier(JPQLTokenId.THROW, c);
//                                        }
//                                    break;
//                            }
//                            break;
//                        case 'r':
//                            switch (c = input.read()) {
//                                case 'a':
//                                    if ((c = input.read()) == 'n'
//                                     && (c = input.read()) == 's'
//                                     && (c = input.read()) == 'i'
//                                     && (c = input.read()) == 'e'
//                                     && (c = input.read()) == 'n'
//                                     && (c = input.read()) == 't')
//                                        return keywordOrIdentifier(JPQLTokenId.TRANSIENT);
//                                    break;
//                                case 'u':
//                                    if ((c = input.read()) == 'e')
//                                        return keywordOrIdentifier(JPQLTokenId.TRUE);
//                                    break;
//                                case 'y':
//                                    return keywordOrIdentifier(JPQLTokenId.TRY);
//                            }
//                            break;
//                    }
//                    return finishIdentifier(c);
//
//                case 'v':
//                    if ((c = input.read()) == 'o') {
//                        switch (c = input.read()) {
//                            case 'i':
//                                if ((c = input.read()) == 'd')
//                                    return keywordOrIdentifier(JPQLTokenId.VOID);
//                                break;
//                            case 'l':
//                                if ((c = input.read()) == 'a'
//                                 && (c = input.read()) == 't'
//                                 && (c = input.read()) == 'i'
//                                 && (c = input.read()) == 'l'
//                                 && (c = input.read()) == 'e')
//                                    return keywordOrIdentifier(JPQLTokenId.VOLATILE);
//                                break;
//                        }
//                    }
//                    return finishIdentifier(c);
//
//                case 'w':
//                    if ((c = input.read()) == 'h'
//                     && (c = input.read()) == 'i'
//                     && (c = input.read()) == 'l'
//                     && (c = input.read()) == 'e')
//                        return keywordOrIdentifier(JPQLTokenId.WHILE);
//                    return finishIdentifier(c);
//
                // Rest of lowercase letters starting identifiers
                case 'h': case 'j': case 'k': case 'm': case 'o':
                case 'q': case 'u': case 'x': case 'y': case 'z':
                // Uppercase letters starting identifiers
                case 'B': case 'C': case 'D': case 'E':
                case 'F': case 'G': case 'H': case 'I': case 'J':
                case 'K': case 'L': case 'M': case 'N': case 'O':
                case 'P': case 'Q': case 'R': case 'S': case 'T':
                case 'U': case 'V': case 'W': case 'X': case 'Y':
                case 'Z':
                case '$': case '_':
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
                    return finishWhitespace();
                case ' ':
                    c = input.read();
                    if (c == EOF || !Character.isWhitespace(c)) { // Return single space as flyweight token
                        input.backup(1);
                        return tokenFactory.getFlyweightToken(JPQLTokenId.WHITESPACE, " ");
                    }
                    return finishWhitespace();

                case EOF:
                    return null;

                default:
                    if (c >= 0x80) { // lowSurr ones already handled above
                        c = translateSurrogates(c);
                        if (Character.isJavaIdentifierStart(c)) {
                            return finishIdentifier();
                        }
                        if (Character.isWhitespace(c)) {
                            return finishWhitespace();
                        }
                    }

                    // Invalid char
                    return token(JPQLTokenId.WHITESPACE);
                    //return token(JPQLTokenId.ERROR); //disabled until complete implementation for the parser
            } // end of switch (c)
        } // end of while(true)

    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
    }
    private Token<JPQLTokenId> token(JPQLTokenId id) {
        String fixedText = id.getText();
        return (fixedText != null)
                ? tokenFactory.getFlyweightToken(id, fixedText)
                : tokenFactory.createToken(id);
    }
    private Token<JPQLTokenId> finishWhitespace() {
        while (true) {
            int c = input.read();
            // There should be no surrogates possible for whitespace
            // so do not call translateSurrogates()
            if (c == EOF || !Character.isWhitespace(c)) {
                input.backup(1);
                return tokenFactory.createToken(JPQLTokenId.WHITESPACE);
            }
        }
    }
    
    private Token<JPQLTokenId> finishIdentifier() {
        return finishIdentifier(input.read());
    }
    private Token<JPQLTokenId> finishIdentifier(int c) {
        while (true) {
            if (c == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c))) {
                // For surrogate 2 chars must be backed up
                input.backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
                return tokenFactory.createToken(JPQLTokenId.IDENTIFIER);
            }
            c = input.read();
        }
    }

    private Token<JPQLTokenId> keywordOrIdentifier(JPQLTokenId keywordId) {
        return keywordOrIdentifier(keywordId, input.read());
    }

    private Token<JPQLTokenId> keywordOrIdentifier(JPQLTokenId keywordId, int c) {
        // Check whether the given char is non-ident and if so then return keyword
        if (c == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c))) {
            // For surrogate 2 chars must be backed up
            input.backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
            return token(keywordId);
        } else {// c is identifier part
            return finishIdentifier();
        }
    }
    
    private Token<JPQLTokenId> finishNumberLiteral(int c, boolean inFraction) {
        while (true) {
            switch (c) {
                case '.':
                    if (!inFraction) {
                        inFraction = true;
                    } else { // two dots in the literal
                        return token(JPQLTokenId.ERROR);
                    }
                    break;
                case 'l': case 'L': // 0l or 0L
                    return token(JPQLTokenId.LONG_LITERAL);
                case 'd': case 'D':
                    return token(JPQLTokenId.DOUBLE_LITERAL);
                case 'f': case 'F':
                    return token(JPQLTokenId.FLOAT_LITERAL);
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    break;
                case 'e': case 'E': // exponent part
                    return finishFloatExponent();
                default:
                    input.backup(1);
                    return token(inFraction ? JPQLTokenId.DOUBLE_LITERAL
                            : JPQLTokenId.INT_LITERAL);
            }
            c = input.read();
        }
    }
    
    private Token<JPQLTokenId> finishFloatExponent() {
        int c = input.read();
        if (c == '+' || c == '-') {
            c = input.read();
        }
        if (c < '0' || '9' < c) {
            return token(JPQLTokenId.ERROR);
        }
        do {
            c = input.read();
        } while ('0' <= c && c <= '9'); // reading exponent
        switch (c) {
            case 'd': case 'D':
                return token(JPQLTokenId.DOUBLE_LITERAL);
            case 'f': case 'F':
                return token(JPQLTokenId.FLOAT_LITERAL);
            default:
                input.backup(1);
                return token(JPQLTokenId.DOUBLE_LITERAL);
        }
    }
    private int translateSurrogates(int c) {
        if (Character.isHighSurrogate((char)c)) {
            int lowSurr = input.read();
            if (lowSurr != EOF && Character.isLowSurrogate((char)lowSurr)) {
                // c and lowSurr form the integer unicode char.
                c = Character.toCodePoint((char)c, (char)lowSurr);
            } else {
                // Otherwise it's error: Low surrogate does not follow the high one.
                // Leave the original character unchanged.
                // As the surrogates do not belong to any
                // specific unicode category the lexer should finally
                // categorize them as a lexical error.
                input.backup(1);
            }
        }
        return c;
    }


}
