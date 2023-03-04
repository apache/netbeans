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

package org.netbeans.modules.options.colors;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 * @author Jan Jancura
 */
class AllLanguagesLexer implements Lexer<AllLanguagesTokenId> {


    private LexerRestartInfo<AllLanguagesTokenId> info;

    AllLanguagesLexer (LexerRestartInfo<AllLanguagesTokenId> info) {
        this.info = info;
    }

    public Token<AllLanguagesTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case '/':
                i = input.read ();
                if (i == '/')
                    return info.tokenFactory ().createToken (AllLanguagesTokenId.COMMENT);
                if (i == '*') {
                    i = input.read ();
                    while (i != LexerInput.EOF) {
                        while (i == '*') {
                            i = input.read ();
                            if (i == '/')
                                return info.tokenFactory ().createToken (AllLanguagesTokenId.COMMENT);
                        }
                        i = input.read ();
                    }
                    return info.tokenFactory ().createToken (AllLanguagesTokenId.COMMENT);
                }
                if (i != LexerInput.EOF)
                    input.backup (1);
                return info.tokenFactory ().createToken (AllLanguagesTokenId.OPERATOR);
            case '+':
            case '=':
                return info.tokenFactory ().createToken (AllLanguagesTokenId.OPERATOR);
            case '{':
            case '}':
            case '(':
            case ')':
            case ';':
                return info.tokenFactory ().createToken (AllLanguagesTokenId.SEPARATOR);
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                do {
                    i = input.read ();
                } while (
                    i == ' ' ||
                    i == '\n' ||
                    i == '\r' ||
                    i == '\t'
                );
                if (i != LexerInput.EOF)
                    input.backup (1);
                return info.tokenFactory ().createToken (AllLanguagesTokenId.WHITESPACE);
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
                do {
                    i = input.read ();
                } while (
                    i >= '0' &&
                    i <= '9'
                );
                if (i == '.') {
                    do {
                        i = input.read ();
                    } while (
                        i >= '0' &&
                        i <= '9'
                    );
                }
                input.backup (1);
                return info.tokenFactory ().createToken (AllLanguagesTokenId.NUMBER);
            case '"':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '"' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                return info.tokenFactory ().createToken (AllLanguagesTokenId.STRING);
            case '\'':
                i = input.read ();
                if (i == '\\')
                    i = input.read ();
                i = input.read ();
                if (i != '\'')
                    return info.tokenFactory ().createToken (AllLanguagesTokenId.ERROR);
                return info.tokenFactory ().createToken (AllLanguagesTokenId.CHARACTER);
            default:
                if (
                    (i >= 'a' && i <= 'z') ||
                    (i >= 'A' && i <= 'Z')
                ) {
                    do {
                        i = input.read ();
                    } while (
                        (i >= 'a' && i <= 'z') ||
                        (i >= 'A' && i <= 'Z') ||
                        (i >= '0' && i <= '9') ||
                        i == '_' ||
                        i == '-' ||
                        i == '~'
                    );
                    input.backup (1);
                    String id = input.readText ().toString ();
                    if (id.equals ("public") ||
                        id.equals ("class")
                    )
                        return info.tokenFactory ().createToken (AllLanguagesTokenId.KEYWORD);
                    return info.tokenFactory ().createToken (AllLanguagesTokenId.IDENTIFIER);
                }
                return info.tokenFactory ().createToken (AllLanguagesTokenId.ERROR);
        }
    }

    public Object state () {
        return null;
    }

    public void release () {
    }
}


