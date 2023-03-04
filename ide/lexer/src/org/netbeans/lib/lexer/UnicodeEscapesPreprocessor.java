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

package org.netbeans.lib.lexer;

import org.netbeans.spi.lexer.LexerInput;

/**
 * The character translator allows to translate a sequence
 * of characters to a single character so it may be used
 * for example for generic Unicode sequences translation.
 *
 * <p>
 * The preprocessor must be stateless.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class UnicodeEscapesPreprocessor extends CharPreprocessor {

    /**
     * Preprocess a input text preprocessing unicode escape sequences.
     */
    protected void preprocessChar() {
        int ch;
        switch (ch = inputRead()) {
            case '\\': // 1 input-char-read
                // According to JLS only odd number of backslashes
                // opens a unicode escape sequence. Therefore after seeing two
                // backslashes it's possible to pass them unchanged.
                switch (ch = inputRead()) {
                    case 'u': // 2 input-chars-read
                        // Four hex digits should follow
                        int i;
                        int c;
                        for (i = 4; i > 0; i--) {
                            switch (c = inputRead()) {
                                case '0': case '1': case '2': case '3': case '4':
                                case '5': case '6': case '7': case '8': case '9':
                                    ch = (ch << 4) + (c - '0');
                                    break;
                                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                                    ch = (ch << 4) + (c - 'a' + 10);
                                    break;
                                case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                                    ch = (ch << 4) + (c - 'A' + 10);
                                    break;
                                case LexerInput.EOF: // Do not backup EOF
                                    // EOF does not count for a character
                                    i = -i; // cause for() loop to break
                                    break;
                                default: // Invalid char in the sequence
                                    inputBackup(1); // backup the extra non-EOF char
                                    i = -i; // cause for() loop to break
                                    break;
                            }
                        }
                        if (i < 0) { // Invalid char or EOF
                            // Return Unicode invalid char
                            // i < 0 -> (4 - number-of-read-chars)
                            outputPreprocessed((char)0xFFFF, 5 + i);
                            notifyError("Invalid unicode sequence");
                        } else {
                            outputPreprocessed((char)ch, 5);
                        }
                        break;
                        
                    // case '\\':
                    default:
                        outputOriginal('\\');
                        outputOriginal(ch);
                        break;
                }
                break;
                
            default:
                outputOriginal(ch);
                break;
        }
    }

    protected boolean isSensitiveChar(char ch) {
        switch (ch) {
            case '\\':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
            case 'u':
                return true;

            default:
                return false;
        }
    }

    protected int maxLookahead() {
        // Only one extra character is necessary to decide
        // whether the sequence continues or not.
        // After finding '\' if the next char is '\' then
        // there is no sequence.
        // After '\' 'u' there may be arbitrary number of 'u' chars
        // and then four hexadecimal digits.
        return 1;
    }

}
