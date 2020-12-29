/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
