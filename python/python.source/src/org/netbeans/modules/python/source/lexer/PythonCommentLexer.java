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
 * A lexer for python comments.
 *
 * Highlights TODO items and certain keywords (like @-param and @-type (without -)).
 *
 */
public class PythonCommentLexer implements Lexer<PythonCommentTokenId> {
    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final TokenFactory<PythonCommentTokenId> tokenFactory;
    private final boolean substituting;

    private static enum State {
        INIT,
        /** We've just seen @type */
        SEEN_TYPE_KEY,
        /** We've just seen @type< > */
        SEEN_TYPE_WS,
        /** We've just seen @type <varname> */
        SEEN_NAME,
        /** We've just seen @type varname< > */
        SEEN_NAME_WS
    };
    private State state;

    /**
     * A Lexer for Python strings
     * @param substituting If true, handle substitution rules for double quoted strings, otherwise
     *    single quoted strings.
     */
    public PythonCommentLexer(LexerRestartInfo<PythonCommentTokenId> info, boolean substituting) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.substituting = substituting;
        state = (State)info.state();
        if (state == null) {
            state = State.INIT;
        }
    }

    @Override
    public Object state() {
        return state;
    }

    @Override
    public Token<PythonCommentTokenId> nextToken() {
        switch (state) {
        case SEEN_NAME:
        case SEEN_TYPE_KEY:
            while (true) {
                int ch = input.read();
                if (ch == ':' && state == State.SEEN_NAME && input.readLength() == 1) {
                    continue;
                }
                if (ch == EOF || !Character.isWhitespace(ch)) {
                    if (ch != EOF) {
                        input.backup(1);
                    }
                    if (input.readLength() > 0) {
                        state = (state == State.SEEN_TYPE_KEY) ? State.SEEN_TYPE_WS : State.SEEN_NAME_WS;
                        return tokenFactory.createToken(PythonCommentTokenId.SEPARATOR,
                                input.readLength());
                    } else {
                        return null;
                    }
                }
            }

        case SEEN_NAME_WS:
        case SEEN_TYPE_WS:
            while (true) {
                int ch = input.read();
                if (ch == EOF || Character.isWhitespace(ch) || ch == ':') {
                    if (ch != EOF) {
                        input.backup(1);
                    }
                    if (input.readLength() > 0) {
                        State nextState;
                        PythonCommentTokenId id;
                        if (state == State.SEEN_TYPE_WS) {
                            nextState = State.SEEN_NAME;
                            id = PythonCommentTokenId.VARNAME;
                        } else {
                            nextState = State.INIT;
                            id = PythonCommentTokenId.TYPE;
                        }
                        state = nextState;
                        return tokenFactory.createToken(id, input.readLength());
                    } else if (ch == EOF) {
                        return null;
                    } else {
                        // Error - : without an actual var name
                        state = State.INIT;
                        return nextToken(); // recurse
                    }
                }
            }
        default:
        case INIT: {

            int last = EOF;
            while (true) {
                int ch = input.read();

                switch (ch) {
                case EOF:
                    if (input.readLength() > 0) {
                        return tokenFactory.createToken(PythonCommentTokenId.TEXT,
                                input.readLength());
                    } else {
                        return null;
                    }

                case '@': {
                    // Is it "@type"
                    int initialReadLength = input.readLength();
                    if (input.read() == 't' && input.read() == 'y' && input.read() == 'p' && input.read() == 'e') {
                        if (input.readLength() > 5) {
                            input.backup(5);
                            // Finish this token such that we can do a dedicated token for the @type item.
                            return tokenFactory.createToken(PythonCommentTokenId.TEXT,
                                    input.readLength());
                        }
                        state = State.SEEN_TYPE_KEY;
                        return tokenFactory.createToken(PythonCommentTokenId.TYPEKEY,
                                input.readLength());
                    }
                    if (input.readLength() > initialReadLength) {
                        input.backup(input.readLength() - initialReadLength);
                    } else {
                        return tokenFactory.createToken(PythonCommentTokenId.TEXT,
                                input.readLength());
                    }
                }
                break;

                case 'T': {
                    if (last == EOF || !Character.isLetter(last)) {
                        // Is it "\wTODO\w" ?
                        int initialReadLength = input.readLength();
                        if (input.read() == 'O' && input.read() == 'D' && input.read() == 'O') {
                            int peek = input.read();
                            input.backup(1);
                            if (peek == EOF || !Character.isLetter(peek)) {
                                if (input.readLength() > 4) {
                                    input.backup(4);
                                    // Finish this token such that we can do a dedicated token for the @type item.
                                    return tokenFactory.createToken(PythonCommentTokenId.TEXT,
                                            input.readLength());
                                }
                                return tokenFactory.createToken(PythonCommentTokenId.TODO,
                                        input.readLength());
                            }
                        }
                        if (input.readLength() > initialReadLength) {
                            input.backup(input.readLength() - initialReadLength);
                        } else {
                            return tokenFactory.createToken(PythonCommentTokenId.TEXT,
                                    input.readLength());
                        }
                    }
                }
                break;
                }

                last = ch;
            }

        }
        }
    }

    @Override
    public void release() {
    }
}
