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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
