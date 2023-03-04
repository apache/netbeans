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

package org.netbeans.modules.profiler.oql.language;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;


/**
 *
 * @author Jaroslav Bachorik
 */
class OQLLexer implements Lexer<OQLTokenId> {
    private static final String TOKEN_FROM = "FROM"; // NOI18N
    private static final String TOKEN_INSTANCEOF = "INSTANCEOF"; // NOI18N
    private static final String TOKEN_SELECT = "SELECT"; // NOI18N
    private static final String TOKEN_WHERE = "WHERE"; // NOI18N

    enum State {
        INIT,
        IN_SELECT,
        IN_FROM,
        IN_WHERE,
        IN_CLASSNAME,
        IN_CLASSID,
        PLAIN_JS,
        FROM,
        FROM_INSTANCEOF,
        CLASS_ALIAS,
        JSBLOCK,
        JSBLOCK1,
        ERROR
    };

    private LexerInput          input;
    private TokenFactory<OQLTokenId>
                                tokenFactory;
    private State               state = State.INIT;
    private final Pattern       classPattern = Pattern.compile("(\\[*)[a-z]+(?:[a-z 0-9]*)(?:[\\. \\$][a-z 0-9]+)*(\\[\\])*", Pattern.CASE_INSENSITIVE); // NOI18N
    private final Pattern       classIdPattern = Pattern.compile("(0X)?([0-9 a-f A-F]+)");


    OQLLexer (LexerRestartInfo<OQLTokenId> info) {
        input = info.input ();
        tokenFactory = info.tokenFactory ();
        if (info.state () != null)
            state = (State) info.state ();
    }

    public Token<OQLTokenId> nextToken () {
        for (;;) {
            int actChar = input.read();
            if (actChar == LexerInput.EOF) {
                break;
            }
            switch (state) {
                case INIT: {
                    String lastToken = input.readText().toString().toUpperCase();
                    if (Character.isWhitespace(actChar)) {
                        return tokenFactory.createToken(OQLTokenId.WHITESPACE);
                    } else {
                        input.backup(input.readLength());
                        if (TOKEN_SELECT.startsWith(lastToken.trim())) {
                            state = State.IN_SELECT;
                        } else {
                            state = State.PLAIN_JS;
                        }
                    }
                    break;
                }
                case IN_SELECT: {
                    String lastToken = input.readText().toString().toUpperCase();
                    String trimmed = lastToken.trim();

                    if (Character.isWhitespace(actChar)) {
                        if (trimmed.length() == 0) return tokenFactory.createToken(OQLTokenId.SELECT);
                        if (TOKEN_SELECT.equals(trimmed)) {
                            state = State.JSBLOCK;
                            input.backup(1);
                            return tokenFactory.createToken(OQLTokenId.SELECT);
                        } else {
                            state = State.ERROR;
                            input.backup(input.readLength());
                        }
                    } else {
                        if (!TOKEN_SELECT.startsWith(trimmed)) {
                            input.backup(input.readLength());
                            state = State.PLAIN_JS;
                        }
                    }
                    break;
                }

                case IN_FROM: {
                    if (Character.isWhitespace(actChar)) {
                        String lastToken = input.readText().toString().toUpperCase();
                        if (lastToken.trim().length() == 0) return tokenFactory.createToken(OQLTokenId.FROM);
                        if (TOKEN_FROM.equals(lastToken.trim())) {
                            input.backup(1);
                            state = State.FROM;
                            return tokenFactory.createToken(OQLTokenId.FROM, lastToken.trim().length(), PartType.COMPLETE);
                        } else {
                            state = State.ERROR;
                            input.backup(input.readLength());
                        }
                    }
                    break;
                }

                case FROM: {
                    String lastToken = input.readText().toString().toUpperCase();
                    String trimmed = lastToken.trim();
                    if (!TOKEN_FROM.startsWith(lastToken.trim())) {
                        input.backup(input.readLength());
                        state = State.JSBLOCK;
                    }
                    if (Character.isWhitespace(actChar)) {
                        if (trimmed.length() == 0) return tokenFactory.createToken(OQLTokenId.FROM);
                        input.backup(lastToken.length() - trimmed.length());
                        if (TOKEN_FROM.equals(trimmed)) {
                            state = State.FROM_INSTANCEOF;
                            return tokenFactory.createToken(OQLTokenId.FROM);
                        } else {
                            input.backup(input.readLength());
                            state = State.JSBLOCK;
                        }
                    }

                    break;
                }

                case FROM_INSTANCEOF: {
                    String lastToken = input.readText().toString().toUpperCase();
                    String trimmed = lastToken.trim();
                    if (!TOKEN_INSTANCEOF.startsWith(trimmed)) {
                        state = State.IN_CLASSNAME;
                        input.backup(input.readLength());
                    }
                    if (Character.isWhitespace(actChar)) {
                        if (trimmed.length() == 0) return tokenFactory.createToken(OQLTokenId.INSTANCEOF);
                        input.backup(lastToken.length() - trimmed.length());
                        if (TOKEN_INSTANCEOF.equals(trimmed)) {
                            state = State.IN_CLASSNAME;
                        }
                        return tokenFactory.createToken(OQLTokenId.INSTANCEOF);
                    }
                    break;
                }

                case JSBLOCK: {
                    if (Character.isWhitespace(actChar)) {
                        String lastToken = input.readText().toString().toUpperCase();
                        String trimmed = lastToken.trim();
                        if (trimmed.endsWith(TOKEN_FROM)) {
                            state = State.FROM;
                            if (input.readLength() > 5) {
                                input.backup(5);
                                return tokenFactory.createToken(OQLTokenId.JSBLOCK);
                            } else {
                                state = State.ERROR;
                                input.backup(input.readLength());
                                break;
                            }
                        } else if (TOKEN_SELECT.equals(trimmed) || TOKEN_INSTANCEOF.equals(trimmed) || TOKEN_WHERE.equals(trimmed)) {
                            state = State.ERROR;
                            input.backup(input.readLength());
                        }
                    } else if (actChar == '(' || actChar == ')' || actChar == '[' ||
                               actChar == ']' || actChar == '{' || actChar == '}' ||
                               actChar == '.' || actChar == ',') {
                        state = State.JSBLOCK1;
                        input.backup(1);
                        if (input.readLength() > 0) {
                            return tokenFactory.createToken(OQLTokenId.JSBLOCK);
                        }
                    }
                    break;
                }

                case JSBLOCK1: {
                    if (actChar == '(' || actChar == ')' || actChar == '[' ||
                       actChar == ']' || actChar == '{' || actChar == '}') {
                        state = State.JSBLOCK;
                        return tokenFactory.createToken(OQLTokenId.BRACE);
                    } else if (actChar == '.') {
                        state = State.JSBLOCK;
                        return tokenFactory.createToken(OQLTokenId.DOT);
                    } else if (actChar == ',') {
                        state = State.JSBLOCK;
                        return tokenFactory.createToken(OQLTokenId.COMMA);
                    }
                    break;
                }

                case IN_CLASSNAME: {
                    if (Character.isWhitespace(actChar)) {
                        String lastToken = input.readText().toString().toUpperCase();
                        Matcher idMatcher = classIdPattern.matcher(lastToken.trim());
                        if (idMatcher.matches()) {
                            input.backup(1);
                            state = State.CLASS_ALIAS;
                            return tokenFactory.createToken(OQLTokenId.CLAZZ);
                        }
                        Matcher nameMatcher = classPattern.matcher(lastToken.trim());
                        if (nameMatcher.matches()) {
                            input.backup(1);
                            if ((isEmpty(nameMatcher.group(1)) ? 0 : 1) + (isEmpty(nameMatcher.group(2)) ? 0 : 1) > 1) {
                                return tokenFactory.createToken(OQLTokenId.CLAZZ_E);
//                                input.backup(input.readLength());
                            }
                            state = State.CLASS_ALIAS;
                            return tokenFactory.createToken(OQLTokenId.CLAZZ);
                        }
                    }
                    break;
                }

                case CLASS_ALIAS: {
                    String lastToken = input.readText().toString().toUpperCase();

                    if (TOKEN_SELECT.equals(lastToken) ||
                        TOKEN_FROM.equals(lastToken) ||
                        TOKEN_INSTANCEOF.equals(lastToken) ||
                        TOKEN_WHERE.equals(lastToken)) {
                        state = State.ERROR;
                        input.backup(input.readLength());
                        break;
                    }
                    if (Character.isWhitespace(actChar)) {
                        if (lastToken.trim().length() == 0) {
                            return tokenFactory.createToken(OQLTokenId.IDENTIFIER);
                        }
                        input.backup(lastToken.length() - lastToken.trim().length());
                        state = State.IN_WHERE;
                        return tokenFactory.createToken(OQLTokenId.IDENTIFIER);
                    }
                    if (!Character.isLetter(actChar)) {
                        state = State.ERROR;
                        input.backup(1);
                        break;
                    }
                    break;
                }

                case IN_WHERE: {
                    String lastToken = input.readText().toString().toUpperCase();
                    String trimmed = lastToken.trim();

                    if (!TOKEN_WHERE.startsWith(trimmed)) {
                        state = State.ERROR;
                        input.backup(input.readLength());
                    }
                    if (Character.isWhitespace(actChar)) {
                        if (trimmed.length() == 0) {
                            return tokenFactory.createToken(OQLTokenId.WHERE);
                        }
                        input.backup(lastToken.length() - trimmed.length());
                        if (TOKEN_WHERE.equals(trimmed)) {
                            state = State.JSBLOCK;
                        }
                        return tokenFactory.createToken(OQLTokenId.WHERE);
                    }
                    break;
                }

                case PLAIN_JS: {
                    break;
                }
                case ERROR: {
                    while (input.read() != LexerInput.EOF);
                    return tokenFactory.createToken(OQLTokenId.ERROR);
                }
            } // switch (state)
        }

        if (input.readLength() == 0) return null;
        switch (state) {
            case INIT: {
                return tokenFactory.createToken(OQLTokenId.UNKNOWN);
            }
            case IN_SELECT: {
                return tokenFactory.createToken(OQLTokenId.SELECT, input.readLength(), PartType.START);
            }
            case JSBLOCK: {
                String lastToken = input.readText().toString().trim().toUpperCase();
                if (lastToken.endsWith(TOKEN_FROM)) {
                    state = State.IN_FROM;
                    if (input.readLength() > 5) {
                        input.backup(5);
                        return tokenFactory.createToken(OQLTokenId.JSBLOCK);
                    } else {
                        state = State.ERROR;
                        return tokenFactory.createToken(OQLTokenId.ERROR);
                    }
                } else {
                    return tokenFactory.createToken(OQLTokenId.JSBLOCK, input.readLength(), PartType.START);
                }
            }
            case PLAIN_JS: {
                return tokenFactory.createToken(OQLTokenId.JSBLOCK);
            }
            case IN_FROM: {
                return tokenFactory.createToken(OQLTokenId.FROM, input.readLength(), PartType.START);
            }
            case FROM: {
                return tokenFactory.createToken(OQLTokenId.UNKNOWN);
            }
            case FROM_INSTANCEOF: {
                return tokenFactory.createToken(OQLTokenId.INSTANCEOF, input.readLength(), PartType.START);
            }
            case IN_CLASSNAME: {
                String lastToken = input.readText().toString().trim().toUpperCase();
                Matcher matcher = classPattern.matcher(lastToken);
                if (matcher.matches()) {
                    if ((isEmpty(matcher.group(1)) ? 0 : 1) + (isEmpty(matcher.group(2)) ? 0 : 1) > 1) {
                        return tokenFactory.createToken(OQLTokenId.ERROR);
                    }
                    state = State.CLASS_ALIAS;
                    input.backup(1);
                    return tokenFactory.createToken(OQLTokenId.CLAZZ);
                } else {
                    return tokenFactory.createToken(OQLTokenId.CLAZZ_E);
                }
            }
            case CLASS_ALIAS: {
                String lastToken = input.readText().toString().toUpperCase();

                if (TOKEN_SELECT.equals(lastToken) ||
                    TOKEN_FROM.equals(lastToken) ||
                    TOKEN_INSTANCEOF.equals(lastToken) ||
                    TOKEN_WHERE.equals(lastToken)) {
                    state = State.ERROR;
//                    input.backup(input.readLength());
                    return tokenFactory.createToken(OQLTokenId.ERROR);
                } else {
                    return tokenFactory.createToken(OQLTokenId.IDENTIFIER);
                }
            }
            case IN_WHERE: {
                return tokenFactory.createToken(OQLTokenId.WHERE, input.readLength(), PartType.START);
            }
            case ERROR: {
                return tokenFactory.createToken(OQLTokenId.ERROR);
            }
            default: {
                return tokenFactory.createToken(OQLTokenId.UNKNOWN);
            }
        }
    }

    private static final boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public Object state () {
        return state;
    }

    public void release () {
    }
}


