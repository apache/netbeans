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

import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Lexical analyzer for javadoc language.
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavadocLexer implements Lexer<JavadocTokenId> {

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;
    
    private TokenFactory<JavadocTokenId> tokenFactory;
    
    private Integer state = null;
    
    public JavadocLexer(LexerRestartInfo<JavadocTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.state = (Integer) info.state();        
    }
    
    public Object state() {
        return state;
    }
    
    public Token<JavadocTokenId> nextToken() {
        int ch = input.read();
        
        if (ch == EOF) {
            return null;
        }
        
        if (Character.isJavaIdentifierStart(ch)) {
            //TODO: EOF
            while (Character.isJavaIdentifierPart(input.read()))
                ;
            
            input.backup(1);
            if (state != null && state == 2) {
                state = 1;
                return token(JavadocTokenId.IDENT, "javadoc-identifier"); //NOI18N
            }
            if (state == null) {
                state = 1;
            }
            return token(JavadocTokenId.IDENT);
        }
        
        switch (ch) {
            case '@':
                if (state != null) {
                    return otherText(ch);
                }
                String tag = "";
                while (true) {
                    ch = input.read();
                    if (!Character.isLetter(ch)) {
                        state = "param".equals(tag) ? 2 : "code".equals(tag) || "literal".equals(tag) ? 3 : 1; //NOI18N
                        input.backup(1);
                        return tokenFactory.createToken(JavadocTokenId.TAG, input.readLength());
                    } else {
                        tag+=new String(Character.toChars(ch));
                    }
                }
            case '<':
                if (state != null && state == 3) {
                    return otherText(ch);
                }
                int backupCounter = 0;
                boolean newline = false;
                boolean asterisk = false;
                while (true) {
                    ch = input.read();
                    ++backupCounter;
                    if (ch == EOF) {
                        state = null;
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (ch == '>') {
                        if (state != null && state == 2) {
                            state = 1;
                            return token(JavadocTokenId.IDENT, "javadoc-identifier"); //NOI18N
                        }
                        state = 1;
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (ch == '<') {
                        state = 1;
                        input.backup(1);
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (ch == '\n') {
                        state = null;
                        backupCounter = 1;
                        newline = true;
                        asterisk = false;
                    } else if (newline && ch == '@') {
                        input.backup(backupCounter);
                        return token(JavadocTokenId.HTML_TAG);
                    } else if (newline && !asterisk && ch == '*') {
                        asterisk = true;
                    } else if (newline && !Character.isWhitespace(ch)) {
                        newline = false;
                    }
                }
            case '.':
                if (state == null) {
                    state = 1;
                }
                return token(JavadocTokenId.DOT);
            case '#':
                if (state == null) {
                    state = 1;
                }
                return token(JavadocTokenId.HASH);
            default:
                return otherText(ch);
        } // end of switch (ch)
    }

    private Token<JavadocTokenId> otherText(int ch) {
        boolean newline = state == null;
        boolean leftbr = false;
        while (true) {
            if (Character.isJavaIdentifierStart(ch)) {
                if ((newline || leftbr) && state != null && state != 3) {
                    state = null;
                }
                input.backup(1);
                return token(JavadocTokenId.OTHER_TEXT);
            }
            switch (ch) {
                case '<':
                    if (state != null && state == 3) {
                        leftbr = false;
                        newline = false;
                        break;
                    }
                case '.':
                case '#':
                    input.backup(1);
                case EOF:
                    return token(JavadocTokenId.OTHER_TEXT);
                case '@':
                    if ((newline || leftbr) && (state == null || state != 3)) {
                        state = null;
                        input.backup(1);                        
                        return token(JavadocTokenId.OTHER_TEXT);
                    }
                    leftbr = false;
                    newline = false;
                    break;
                case '{':
                    leftbr = true;
                    newline = false;
                    break;
                case '\n':
                    newline = true;
                    break;
                case '}':
                    if (state != null && state == 3) {
                        state = 1;
                        if (input.readLength() > 1)
                            input.backup(1);
                        return token(JavadocTokenId.OTHER_TEXT);
                    }
                    leftbr = false;
                    newline = false;
                    break;
                case '/':
                    //TODO: check comment type?
                    if (newline) {
                        if (input.read() == '/') {
                            if (input.read() == '/') {
                                break;
                            } else {
                                input.backup(1);
                            }
                        } else {
                            input.backup(1);
                        }
                        newline = false; //for fall-through:
                    }
                case '*':
                    if (newline) {
                        break;
                    }
                default:
                    if (!Character.isWhitespace(ch)) {
                        leftbr = false;
                        newline = false;
                    }
            }
            ch = input.read();
        }
    }

    private Token<JavadocTokenId> token(JavadocTokenId id) {
        return tokenFactory.createToken(id);
    }

    private Token<JavadocTokenId> token(JavadocTokenId id, final Object property) {
        return tokenFactory.createPropertyToken(id, input.readLength(), new TokenPropertyProvider<JavadocTokenId>() {

        @Override
        public Object getValue(Token<JavadocTokenId> token, Object key) {
            if (property.equals(key)) 
                return true; 
            return null;
        }
        
    });
    }

    public void release() {
    }

}
