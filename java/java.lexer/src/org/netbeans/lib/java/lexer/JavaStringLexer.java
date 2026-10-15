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

import org.netbeans.api.java.lexer.JavaStringTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for java string language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavaStringLexer<T extends TokenId> implements Lexer<T> {

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;
    
    private TokenFactory<T> tokenFactory;
    private boolean isJavaStringTokenId;
    
    public JavaStringLexer(LexerRestartInfo<T> info, boolean isJavaStringTokenId) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.isJavaStringTokenId = isJavaStringTokenId;
        assert (info.state() == null); // passed argument always null
    }
   
    public Object state() {
        return null;
    }
    
    public Token<T> nextToken() {
        while(true) {
            int ch = input.read();
            switch (ch) {
                case EOF:
                    if (input.readLength() > 0)
                        return token((T) (isStringTokenId() ? JavaStringTokenId.TEXT : JavaCharacterTokenId.TEXT));
                    else
                        return null;
                case '\\': //NOI18N
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return tokenFactory.createToken((T) (isStringTokenId() ? JavaStringTokenId.TEXT : JavaCharacterTokenId.TEXT), input.readLength());
                    }
                    switch (ch = input.read()) {
                        case 'b': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.BACKSPACE : JavaCharacterTokenId.BACKSPACE));
                        case 'f': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.FORM_FEED : JavaCharacterTokenId.FORM_FEED));
                        case 'n': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.NEWLINE : JavaCharacterTokenId.NEWLINE));
                        case 'r': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.CR : JavaCharacterTokenId.CR));
                        case 't': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.TAB : JavaCharacterTokenId.TAB));
                        case '\'': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.SINGLE_QUOTE : JavaCharacterTokenId.SINGLE_QUOTE));
                        case '"': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.DOUBLE_QUOTE : JavaCharacterTokenId.DOUBLE_QUOTE));
                        case '\\': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.BACKSLASH : JavaCharacterTokenId.BACKSLASH));
                        case '{': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.TEMPLATE_START : JavaCharacterTokenId.TEMPLATE_START));
                        case 'u': //NOI18N
                            while ('u' == (ch = input.read())) {}; //NOI18N
                            
                            for(int i = 0; ; i++) {
                                ch = Character.toLowerCase(ch);
                                
                                if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'f')) { //NOI18N
                                    input.backup(1);
                                    return token((T) (isStringTokenId() ? JavaStringTokenId.UNICODE_ESCAPE_INVALID : JavaCharacterTokenId.UNICODE_ESCAPE_INVALID));
                                }
                             
                                if (i == 3) { // four digits checked, valid sequence
                                    return token((T) (isStringTokenId() ? JavaStringTokenId.UNICODE_ESCAPE : JavaCharacterTokenId.UNICODE_ESCAPE));
                                }
                                
                                ch = input.read();
                            }
                            
                        case '0': case '1': case '2': case '3': //NOI18N
                            switch (input.read()) {
                                case '0': case '1': case '2': case '3': //NOI18N
                                case '4': case '5': case '6': case '7': //NOI18N
                                    switch (input.read()) {
                                        case '0': case '1': case '2': case '3': //NOI18N
                                        case '4': case '5': case '6': case '7': //NOI18N
                                            return token((T) (isStringTokenId() ? JavaStringTokenId.OCTAL_ESCAPE : JavaCharacterTokenId.OCTAL_ESCAPE));
                                    }
                                    input.backup(1);
                                    return token((T) (isStringTokenId() ? JavaStringTokenId.OCTAL_ESCAPE_INVALID : JavaCharacterTokenId.OCTAL_ESCAPE_INVALID));
                            }
                            input.backup(1);
                            return token((T) (isStringTokenId() ? JavaStringTokenId.OCTAL_ESCAPE_INVALID : JavaCharacterTokenId.OCTAL_ESCAPE_INVALID));
                    }
                    input.backup(1);
                    return token((T) (isStringTokenId() ? JavaStringTokenId.ESCAPE_SEQUENCE_INVALID : JavaCharacterTokenId.ESCAPE_SEQUENCE_INVALID));
            } // end of switch (ch)
        } // end of while(true)
    }

    private Token<T> token(T id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }

    private boolean isStringTokenId() {
        return isJavaStringTokenId;
    }

}
