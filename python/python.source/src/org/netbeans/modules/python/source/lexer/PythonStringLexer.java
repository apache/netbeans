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
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * A lexer for python strings. Highlights escape sequences, and recognizes
 * doctest sections and highlights these as well.
 *   http://docs.python.org/lib/module-doctest.html
 *
 * @todo Track whether strings are raw or not, and don't do escape sequence
 *  highlighting in raw strings
 *
 */
public class PythonStringLexer implements Lexer<PythonStringTokenId> {
    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final TokenFactory<PythonStringTokenId> tokenFactory;
    private final boolean substituting;

    /**
     * A Lexer for Python strings
     * @param substituting If true, handle substitution rules for double quoted strings, otherwise
     *    single quoted strings.
     */
    public PythonStringLexer(LexerRestartInfo<PythonStringTokenId> info, boolean substituting) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.substituting = substituting;
        assert (info.state() == null); // passed argument always null
    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public Token<PythonStringTokenId> nextToken() {
        boolean inWord = false;
        while (true) {
            int ch = input.read();

            switch (ch) {
            case EOF:

                if (input.readLength() > 0) {
                    return tokenFactory.createToken(PythonStringTokenId.STRING_TEXT,
                            input.readLength());
                } else {
                    return null;
                }

            case '>':
                // Look for doctest:  \n, whitespace, >>>{embedded python}\n
                int initialReadLength = input.readLength();
                input.read();
                if (ch == '>') {
                    ch = input.read();
                    if (ch == '>') {
                        if (input.readLength() > 3) {
                            input.backup(3);
                            // Finish this token such that we can do a dedicated token for the ">>>" line.
                            return tokenFactory.createToken(PythonStringTokenId.STRING_TEXT,
                                    input.readLength());
                        }
                        // Find end...
                        boolean nonempty = false;
                        while (true) {
                            ch = input.read();
                            if (ch == EOF) {
                                break;
                            } else if (ch == '\n') {
                                if (nonempty) {
                                    input.backup(1); // Don't include the \n
                                    return tokenFactory.createToken(PythonStringTokenId.EMBEDDED_PYTHON,
                                            input.readLength());

                                }
                                break;
                            } else if (!Character.isWhitespace(ch)) {
                                nonempty = true;
                            }
                        }
                    }
                }
                if (input.readLength() > initialReadLength) {
                    input.backup(input.readLength() - initialReadLength);
                } else {
                    return tokenFactory.createToken(PythonStringTokenId.STRING_TEXT,
                            input.readLength());
                }
                break;

            case '\\':

                if (input.readLength() > 1) { // already read some text
                    input.backup(1);

                    return tokenFactory.createToken(PythonStringTokenId.STRING_TEXT,
                            input.readLength());
                }

                ch = input.read();
                if (ch == EOF) {
                    return tokenFactory.createToken(PythonStringTokenId.STRING_INVALID,
                            input.readLength());
                } else {
                    return tokenFactory.createToken(PythonStringTokenId.STRING_ESCAPE,
                            input.readLength());
                }

            case 'f': // ftp:
            case 'm': // mailto:
            case 'w': // www.
            case 'h': { // http links. TODO: link:, ftp:, mailto:, and www.

                if (inWord) {
                    break;
                }

                int originalLength = input.readLength();
                boolean foundLinkBegin = false;

                if (ch == 'h') { // http:

                    if (input.read() == 't') {
                        if (input.read() == 't') {
                            if (input.read() == 'p') {
                                int r = input.read();
                                if (r == ':') {
                                    foundLinkBegin = true;
                                } else if (r == 's') {
                                    if (input.read() == ':') {
                                        foundLinkBegin = true;
                                    } else {
                                        input.backup(5);
                                    }
                                } else {
                                    input.backup(4);
                                }
                            } else {
                                input.backup(3);
                            }
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }
                } else if (ch == 'f') { // ftp:

                    if (input.read() == 't') {
                        if (input.read() == 'p') {
                            if (input.read() == ':') {
                                foundLinkBegin = true;
                            } else {
                                input.backup(3);
                            }
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }
                } else if (ch == 'm') { // mailto:

                    if (input.read() == 'a') {
                        if (input.read() == 'i') {
                            if (input.read() == 'l') {
                                if (input.read() == 't') {
                                    if (input.read() == 'o') {
                                        if (input.read() == ':') {
                                            foundLinkBegin = true;
                                        } else {
                                            input.backup(6);
                                        }
                                    } else {
                                        input.backup(5);
                                    }
                                } else {
                                    input.backup(4);
                                }
                            } else {
                                input.backup(3);
                            }
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }
                } else if (ch == 'w') { // www.

                    if (input.read() == 'w') {
                        if (input.read() == 'w') {
                            if (input.read() == '.') {
                                foundLinkBegin = true;
                            } else {
                                input.backup(3);
                            }
                        } else {
                            input.backup(2);
                        }
                    } else {
                        input.backup(1);
                    }
                }

                if (foundLinkBegin) {
                    while (ch != EOF) {
                        ch = input.read();

                        if ((ch == ']') || (ch == ')') || Character.isWhitespace(ch) ||
                                (ch == '\'') || (ch == '"')) {
                            input.backup(1);

                            break;
                        }
                    }

                    if (originalLength > 1) {
                        input.backup(input.readLengthEOF() - originalLength + 1);

                        return tokenFactory.createToken(PythonStringTokenId.STRING_TEXT,
                                input.readLength());
                    }

                    if (input.readLength() > 2) {
                        return tokenFactory.createToken(PythonStringTokenId.URL,
                                input.readLength());
                    }
                }
                break;
            }
            }

            inWord = Character.isJavaIdentifierPart(ch);
        }
    }

    @Override
    public void release() {
    }
}
