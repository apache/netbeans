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
package org.netbeans.modules.html.knockout;

import org.netbeans.modules.html.knockout.api.KODataBindTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author marekfukala
 */
public class KODataBindLexer implements Lexer<KODataBindTokenId> {

    private enum State {

        INIT,
        IN_WS_BEFORE_KEY,
        IN_KEY,
        AFTER_KEY,
        WS_AFTER_KEY,
        IN_VALUE,
        IN_VALUE_ESCAPE,
        AFTER_VALUE,;
    }

    private static class CompoundState {

        private State state;
        private byte parenDepth;
        private boolean inSingleQuotedString;
        private boolean inDoubleQuotedString;

        public CompoundState(State state, byte parenDepth, boolean inSingleQuotedString, boolean inDoubleQuotedString) {
            this.state = state;
            this.parenDepth = parenDepth;
            this.inSingleQuotedString = inSingleQuotedString;
            this.inDoubleQuotedString = inDoubleQuotedString;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + (this.state != null ? this.state.hashCode() : 0);
            hash = 11 * hash + this.parenDepth;
            hash = 11 * hash + (this.inSingleQuotedString ? 1 : 0);
            hash = 11 * hash + (this.inDoubleQuotedString ? 1 : 0);
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
            final CompoundState other = (CompoundState) obj;
            if (this.state != other.state) {
                return false;
            }
            if (this.parenDepth != other.parenDepth) {
                return false;
            }
            if (this.inSingleQuotedString != other.inSingleQuotedString) {
                return false;
            }
            if (this.inDoubleQuotedString != other.inDoubleQuotedString) {
                return false;
            }
            return true;
        }
    }
    private final LexerInput input;
    private final TokenFactory<KODataBindTokenId> tokenFactory;

    private State state;
    private byte parenDepth = 0; //parenthesis depth in "IN_VALUE" state
    private boolean inSingleQuotedString;
    private boolean inDoubleQuotedString;

    @Override
    public Object state() {
        return new CompoundState(state, parenDepth, inSingleQuotedString, inDoubleQuotedString);
    }

    public KODataBindLexer(LexerRestartInfo<KODataBindTokenId> info) {
        this.tokenFactory = info.tokenFactory();
        this.input = info.input();
        if (info.state() == null) {
            state = State.INIT;
        } else {
            CompoundState compoundState = (CompoundState) info.state();
            state = compoundState.state;
            parenDepth = compoundState.parenDepth;
        }
    }

    @Override
    public Token<KODataBindTokenId> nextToken() {
        int c;

        while (true) {
            c = input.read();

            if (c == LexerInput.EOF) {
                if (input.readLengthEOF() == 1) {
                    return null; //just EOL is read
                } else {
                    //there is something else in the buffer except EOL we will return last token now
                    input.backup(1); //backup the EOL, we will return null in next nextToken() call
                    break;
                }
            }


            switch (state) {
                case INIT:
                    if (Character.isLetter(c) || c == '_' || c == '$') {
                        state = State.IN_KEY;
                    } else if (Character.isWhitespace(c)) {
                        input.backup(1); //backup the ws
                        state = State.IN_WS_BEFORE_KEY;
                    } else {
                        return tokenFactory.createToken(KODataBindTokenId.ERROR);
                    }
                    break;

                case IN_WS_BEFORE_KEY:
                    if (Character.isLetter(c) || c == '_' || c == '$') {
                        state = State.IN_KEY;
                        input.backup(1); //backup the first key char
                        return tokenFactory.createToken(KODataBindTokenId.WS);
                    } else if (Character.isWhitespace(c)) {
                        //stay
                    } else {
                        //error
                        state = State.INIT;
                        return tokenFactory.createToken(KODataBindTokenId.ERROR);
                    }
                    break;

                case IN_KEY:
                    if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_' && c != '$') {
                        if (c == ':') {
                            state = State.AFTER_KEY;
                            input.backup(1); //backup the colon
                            return tokenFactory.createToken(KODataBindTokenId.KEY);
                        } else if (Character.isWhitespace(c)) {
                            state = State.WS_AFTER_KEY;
                            input.backup(1); //backup the ws
                            return tokenFactory.createToken(KODataBindTokenId.KEY);
                        } else {
                            state = State.INIT;
                            return tokenFactory.createToken(KODataBindTokenId.ERROR);
                        }
                    }
                    //stay in IN_KEY
                    break;

                case AFTER_KEY:
                    assert c == ':';
                    state = State.IN_VALUE;
                    return tokenFactory.createToken(KODataBindTokenId.COLON);

                case WS_AFTER_KEY:
                    if (Character.isWhitespace(c)) {
                        //stay
                    } else if (c == ':') {
                        state = State.AFTER_KEY;
                        input.backup(1); //backup the colon
                        return tokenFactory.createToken(KODataBindTokenId.WS);
                    } else {
                        state = State.INIT;
                        return tokenFactory.createToken(KODataBindTokenId.ERROR);
                    }
                    break;

                case IN_VALUE:
                    switch (c) {
                        case ',':
                            if (parenDepth == 0 && !inSingleQuotedString && !inDoubleQuotedString) {
                                state = State.AFTER_VALUE;
                                input.backup(1); //backup the comma

                                if (input.readLength() > 0) {
                                    //return value token if it is not empty like here: "key:,"
                                    return tokenFactory.createToken(KODataBindTokenId.VALUE);
                                }
                            }
                            break;
                        case '(':
                        case '{':
                        case '[':
                            parenDepth++;
                            break;
                        case ')':
                        case '}':
                        case ']':
                            parenDepth--;
                            break;
                        case '\'':
                            inSingleQuotedString = !inSingleQuotedString;
                            break;
                        case '"':
                            inDoubleQuotedString = !inDoubleQuotedString;
                            break;
                        case '\\':
                            if(inSingleQuotedString || inDoubleQuotedString) {
                                state = State.IN_VALUE_ESCAPE;
                            }
                            break;
                        default:
                            break;
                    }
                    break;

                case IN_VALUE_ESCAPE:
                    //just go back to the IN_VALUE state => ignore the semantic of the char after backslash
                    state = State.IN_VALUE;
                    break;
                    
                case AFTER_VALUE:
                    assert c == ',';
                    state = State.INIT;
                    return tokenFactory.createToken(KODataBindTokenId.COMMA);

            }
        }

        //"last buffer" handling
        switch (state) {
            case INIT:
                if (input.readLength() == 0) {
                    return null;
                }
                break;
            case IN_WS_BEFORE_KEY:
                return tokenFactory.createToken(KODataBindTokenId.WS);
            case IN_KEY:
            case AFTER_KEY:
            case WS_AFTER_KEY:
                return tokenFactory.createToken(KODataBindTokenId.KEY);
            case IN_VALUE:
            case IN_VALUE_ESCAPE:
            case AFTER_VALUE:
                return tokenFactory.createToken(KODataBindTokenId.VALUE);
        }

        return null;
    }

    @Override
    public void release() {
        //no-op
    }
}
