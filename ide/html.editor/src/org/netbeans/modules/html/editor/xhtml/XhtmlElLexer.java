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
package org.netbeans.modules.html.editor.xhtml;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Special top level lexer splitting the text into EL and non-EL tokens.
 * @author mfukala@netbeans.org
 */
public class XhtmlElLexer implements Lexer<XhtmlElTokenId> {

    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final InputAttributes inputAttributes;
    private final TokenFactory<XhtmlElTokenId> tokenFactory;

    //main internal lexer state
    private int lexerState = INIT;

    // how many curly braces are nested inside the EL
    private int lexerCurlyNestedLevel = 0;

    // Internal analyzer states
    private static final int INIT = 0;  // initial lexer state = content language
    private static final int ISA_EL_DELIM = 1; //after $ or # in content language
    private static final int ISI_EL = 2; //expression language in content (after ${ or #{ )
    private static final int ISI_EL_SINGLE_QUOTE = 3; //inside single quoted string
    private static final int ISA_EL_SINGLE_QUOTE_ESCAPE = 4; //inside single quoted string after backslash
    private static final int ISI_EL_DOUBLE_QUOTE = 5; //inside double quoted string
    private static final int ISA_EL_DOUBLE_QUOTE_ESCAPE = 6; //inside double quoted string after backslash
    
    public XhtmlElLexer(LexerRestartInfo<XhtmlElTokenId> info) {
        this.input = info.input();
        this.inputAttributes = info.inputAttributes();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            lexerState = INIT;
            lexerCurlyNestedLevel = 0;
        } else {
            CompoundState cs = (CompoundState) info.state();
            lexerState = cs.lexerState;
            lexerCurlyNestedLevel = cs.lexerCurlyNestedLevel;
        }
    }

    @Override
    public Object state() {
        return new CompoundState(lexerState, lexerCurlyNestedLevel);
    }

    @Override
    public Token<XhtmlElTokenId> nextToken() {
        int actChar;
        while (true) {
            actChar = input.read();

            if (actChar == EOF) {
                if (input.readLengthEOF() == 1) {
                    return null; //just EOL is read
                } else {
                    //there is something else in the buffer except EOL
                    //we will return last token now
                    input.backup(1); //backup the EOL, we will return null in next nextToken() call
                    break;
                }
            }

            switch (lexerState) {
                case INIT:
                    switch (actChar) {
                        case '$':
                        case '#': //maybe expression language
                            lexerState = ISA_EL_DELIM;
                            break;
                    }
                    break;


                case ISA_EL_DELIM:

                    switch (actChar) {
                        case '{':
                            if (input.readLength() > 2) {
                                input.backup(3); //backup the '$/#{' and possible '\'
                                // check whether we've read literal EL ('\#{' or '\${')
                                boolean escaped = '\\' == input.read();
                                if (escaped && input.readLength() > 3) {
                                    input.backup(1); // backup the escape char
                                    //we have something read except the '\${' or '\#{' => it's content language
                                    return token(XhtmlElTokenId.HTML);
                                } else if (!escaped) {
                                    //we have something read except the '${' or '#{' => it's content language
                                    return token(XhtmlElTokenId.HTML);
                                } else {
                                    // we're in EL - read back the remaining 
                                    // backed up characters ('${' or '#{') and continue
                                    input.read();
                                    input.read();
                                }
                            }
                            lexerState = ISI_EL;
                            break;
                        default:
                            input.backup(1); //put the read char back
                            lexerState = INIT;
                    }

                    break;

                case ISI_EL:
                    switch(actChar) {
                        case '\'':
                            lexerState = ISI_EL_SINGLE_QUOTE;
                            break;
                        case '"':
                            lexerState = ISI_EL_DOUBLE_QUOTE;
                            break;
                        case '{':
                            lexerCurlyNestedLevel++;
                            break;
                        case '}':
                            if (lexerCurlyNestedLevel == 0) {
                                //return EL token
                                lexerState = INIT;
                                return token(XhtmlElTokenId.EL);
                            } else {
                                lexerCurlyNestedLevel--;
                            }
                    }
                    break;
                    
                case ISI_EL_SINGLE_QUOTE:
                    switch(actChar) {
                        case '\\':
                            lexerState = ISA_EL_SINGLE_QUOTE_ESCAPE;
                            break;
                        case '\'':
                            lexerState = ISI_EL;
                            break;
                    }
                    break;
                    
                case ISI_EL_DOUBLE_QUOTE:
                    switch(actChar) {
                        case '\\':
                            lexerState = ISA_EL_DOUBLE_QUOTE_ESCAPE;
                            break;
                        case '"':
                            lexerState = ISI_EL;
                            break;
                    }
                    break;
                    
                case ISA_EL_DOUBLE_QUOTE_ESCAPE:
                    //just skip back qouted string
                    lexerState = ISI_EL_DOUBLE_QUOTE;
                    break;
                    
                case ISA_EL_SINGLE_QUOTE_ESCAPE:
                    //just skip back qouted string
                    lexerState = ISI_EL_SINGLE_QUOTE;
                    break;
                    
            }

        }

        // At this stage there's no more text in the scanned buffer.
        // Scanner first checks whether this is completely the last
        // available buffer.

        switch (lexerState) {
            case INIT:
                if (input.readLength() == 0) {
                    return null;
                } else {
                    return token(XhtmlElTokenId.HTML);
                }
            case ISA_EL_DELIM:
                return token(XhtmlElTokenId.HTML);
            case ISI_EL:
            case ISI_EL_DOUBLE_QUOTE:
            case ISI_EL_SINGLE_QUOTE:
            case ISA_EL_DOUBLE_QUOTE_ESCAPE:
            case ISA_EL_SINGLE_QUOTE_ESCAPE:
                return token(XhtmlElTokenId.EL);
            default:
                break;
        }

        return null;

    }

    private Token<XhtmlElTokenId> token(XhtmlElTokenId tokenId) {
        return tokenFactory.createToken(tokenId);
    }

    @Override
    public void release() {
        //no-op
    }

    private static class CompoundState {

        private final int lexerState;
        private final int lexerCurlyNestedLevel;

        public CompoundState(int lexerState, int lexerCurlyNestedLevel) {
            this.lexerState = lexerState;
            this.lexerCurlyNestedLevel = lexerCurlyNestedLevel;
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
            if (this.lexerState != other.lexerState) {
                return false;
            }
            if (this.lexerCurlyNestedLevel != other.lexerCurlyNestedLevel) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + lexerState;
            hash = 11 * hash + lexerCurlyNestedLevel;
            return hash;
        }
    }

}

