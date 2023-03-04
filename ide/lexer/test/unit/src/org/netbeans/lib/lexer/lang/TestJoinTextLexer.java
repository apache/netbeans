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

package org.netbeans.lib.lexer.lang;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author mmetelka
 */
final class TestJoinTextLexer implements Lexer<TestJoinTextTokenId> {

    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;

    private final LexerInput input;
    
    private final TokenFactory<TestJoinTextTokenId> tokenFactory;
    
    // Accumulation of token's text for compareAndClearText()
    private StringBuilder text = new StringBuilder();
    
    TestJoinTextLexer(LexerRestartInfo<TestJoinTextTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
    }

    public Object state() {
        return null;
    }

    public Token<TestJoinTextTokenId> nextToken() {
        int c = input.read();
        switch (c) {
            case '(':
                if (input.readLength() > 1) {
                    input.backup(1);
                    return token(TestJoinTextTokenId.TEXT);
                }
                text.append((char)c);
                while (true) {
                    switch ((c = input.read())) {
                        case ')':
                            text.append((char)c);
                            return token(TestJoinTextTokenId.PARENS);
                        case EOF:
                            return token(TestJoinTextTokenId.TEXT);
                    }
                    text.append((char)c);
                }
                // break;

            case '[':
                if (input.readLength() > 1) {
                    input.backup(1);
                    return token(TestJoinTextTokenId.TEXT);
                }
                text.append((char)c);
                while (true) {
                    switch ((c = input.read())) {
                        case ']':
                            text.append((char)c);
                            return token(TestJoinTextTokenId.BRACKETS);
                        case EOF:
                            return token(TestJoinTextTokenId.TEXT);
                    }
                    text.append((char)c);
                }
            // break;

            case '\'':
                if (input.readLength() > 1) {
                    input.backup(1);
                    return token(TestJoinTextTokenId.TEXT);
                }
                text.append((char)c);
                while (true) {
                    switch ((c = input.read())) {
                        case '\'':
                            text.append((char)c);
                            return token(TestJoinTextTokenId.APOSTROPHES);
                        case EOF:
                            return token(TestJoinTextTokenId.TEXT);
                    }
                    text.append((char)c);
                }
            // break;

            case EOF: // no more chars on the input
                return null; // the only legal situation when null can be returned

            default: // In regular text
                text.append((char) c);
                while (true) {
                    switch ((c = input.read())) {
                        case '(':
                        case '[':
                        case '\'':
                        case EOF:
                            input.backup(1);
                            return token(TestJoinTextTokenId.TEXT);
                    }
                    text.append((char) c);
                }
                // break;
        }
    }
    
    private Token<TestJoinTextTokenId> token(TestJoinTextTokenId id) {
        compareAndClearText();
        return tokenFactory.createToken(id);
    }
    
    private void compareAndClearText() {
        String str = input.readText().toString();
        assert (str.length() == text.length()) : dumpText(str);
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != text.charAt(i)) {
                throw new IllegalStateException("Difference at index " + i + ": " + dumpText(str));
            }
        }
        assert (str.length() == input.readLength()); // Expected since no explicit no explicit lengths used in token creation
        input.backup(input.readLengthEOF()); // EOF possibly consumed - must be included in backup count
        for (int i = 0; i < str.length(); i++) {
            int c = input.read();
            if (c != text.charAt(i)) {
                input.backup(1); 
                c = input.read();
                c = input.readText().charAt(i);
                throw new IllegalStateException("Read difference at index " + i + ", c='" + (char)c + "':\n" + dumpText(str));
            }
        }
        text.delete(0, text.length());
    }

    private String dumpText(String str) {
        return "str(" + str.length() + ")=\"" + CharSequenceUtilities.debugText(str) +
                "\"\ntext(" + text.length() + ")=\"" + CharSequenceUtilities.debugText(text) + "\"\n";
    }

    public void release() {
    }

}
