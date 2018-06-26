/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author Petr Pisl
 */
public final class PHPTopLexer implements Lexer<PHPTopTokenId> {

    private final PHPTopColoringLexer scanner;
    private TokenFactory<PHPTopTokenId> tokenFactory;

    private PHPTopLexer(LexerRestartInfo<PHPTopTokenId> info) {
        scanner = new PHPTopColoringLexer(info, (State) info.state());
        tokenFactory = info.tokenFactory();
    }

    public static synchronized PHPTopLexer create(LexerRestartInfo<PHPTopTokenId> info) {
        return new PHPTopLexer(info);
    }

    @Override
    public Token<PHPTopTokenId> nextToken() {
        PHPTopTokenId tokenId = scanner.nextToken();
        Token<PHPTopTokenId> token = null;
        if (tokenId != null) {
            token = tokenFactory.createToken(tokenId);
        }
        return token;
    }

    @Override
    public Object state() {
        return scanner.getState();
    }

    @Override
    public void release() {
    }

    private enum State {

        OUTER,
        AFTER_GT,
        IN_PHP_DELIMITER_SHORT,
        IN_PHP_DELIMITER,
        IN_PHP_STRING,
        IN_PHP_CONSTANT_STRING,
        IN_PHP_HEREDOC_STRING,
        IN_END_DELIMITER,
        AFTER_QUESTION_MARK,
        IN_TEMPLATE_CONTROL,
        IN_PHP
    }

    private static class PHPTopColoringLexer {

        private State state;
        private final LexerInput input;

        public PHPTopColoringLexer(LexerRestartInfo<PHPTopTokenId> info, State state) {
            this.input = info.input();
            if (state == null) {
                this.state = State.OUTER;
            } else {
                this.state = state;
            }
        }

        public PHPTopTokenId nextToken() {
            int c = input.read();
            int hereDocStart = -1;
            CharSequence hereDocDelimiter = null;
            int hereDocDelimiterLenght = 0;
            CharSequence text;
            int textLength;
            if (c == LexerInput.EOF) {
                return null;
            }
            while (c != LexerInput.EOF) {
                char cc = (char) c;
                text = input.readText();
                textLength = text.length();
                switch (state) {
                    case OUTER:
                        if (cc == '<') {
                            state = State.AFTER_GT;
                        }
                        break;
                    case AFTER_GT:
                        switch (cc) {
                            case '?':
                                state = State.IN_PHP_DELIMITER_SHORT;
                                if (textLength > 2) {
                                    input.backup(2);
                                    return PHPTopTokenId.T_HTML;
                                }
                                break;
                            default:
                                state = State.OUTER;
                        }
                        break;
                    case IN_PHP_DELIMITER_SHORT:
                        if (cc == 'p') {
                            state = State.IN_PHP_DELIMITER;
                        } else {
                            if (input.readLength() == 3) {
                                state = State.IN_PHP;
                                input.backup(1);
                                return PHPTopTokenId.T_PHP_OPEN_DELIMITER;
                            }
                        }
                        break;
                    case IN_PHP_DELIMITER:
                        if (textLength == 5) {
                            if (!(text.charAt(textLength - 3) == 'p'
                                    && text.charAt(textLength - 2) == 'h'
                                    && text.charAt(textLength - 1) == 'p')) {
                                input.backup(3);
                            }
                            state = State.IN_PHP;
                            return PHPTopTokenId.T_PHP_OPEN_DELIMITER;
                        }
                        if (Character.isWhitespace(cc)) {
                            input.backup(textLength - 2);
                            state = State.IN_PHP;
                            return PHPTopTokenId.T_PHP_OPEN_DELIMITER;
                        }
                        break;
                    case IN_PHP:
                        switch (cc) {
                            case '?':
                                state = State.IN_END_DELIMITER;
                                break;
                            case '\'':
                                state = State.IN_PHP_CONSTANT_STRING;
                                break;
                            case '"':
                                state = State.IN_PHP_STRING;
                                break;
                            case '<':
                                if (input.readLength() > 3) {
                                    if (text.charAt(textLength - 3) == '<'
                                            && text.charAt(textLength - 2) == '<'
                                            && Character.isWhitespace(text.charAt(textLength - 4))) {
                                        state = State.IN_PHP_HEREDOC_STRING;
                                        hereDocStart = textLength - 3;
                                    }
                                }
                                break;
                            default:
                                // no-op
                        }
                        break;
                    case IN_PHP_CONSTANT_STRING:
                        if (cc == '\'') {
                            char before = text.charAt(input.readLength() - 2);
                            if (before != '\\') {
                                state = State.IN_PHP;
                            }
                        }
                        break;
                    case IN_PHP_STRING:
                        if (cc == '"') {
                            char before = text.charAt(input.readLength() - 2);
                            if (before != '\\') {
                                state = State.IN_PHP;
                            }
                        }
                        break;
                    case IN_END_DELIMITER:
                        if (cc == '>') {
                            if (textLength == 2) {
                                state = State.OUTER;
                                return PHPTopTokenId.T_PHP_CLOSE_DELIMITER;
                            } else {
                                input.backup(2);
                                return PHPTopTokenId.T_PHP;
                            }
                        }
                    case IN_PHP_HEREDOC_STRING:
                        switch (cc) {
                            case '\r':
                            case '\n':
                                if (hereDocStart > -1) {
                                    //find heredoc delimiter start
                                    int delimiterStart = hereDocStart + 3;
                                    char delimiterChar = text.charAt(delimiterStart);
                                    while ((delimiterChar == ' ')
                                            && ++delimiterStart < textLength) {
                                        delimiterChar = text.charAt(delimiterStart);
                                    }
                                    if (delimiterChar != '\n' && delimiterChar != '\r') {
                                        // find heredoc delimiter
                                        hereDocDelimiter = text.subSequence(delimiterStart, text.length() - 1);
                                        if (hereDocDelimiter.charAt(0) == '\''
                                            || hereDocDelimiter.charAt(0) == '"') {
                                                // possible nowdoc or 5.3 heredoc
                                                hereDocDelimiter = hereDocDelimiter.subSequence(1, hereDocDelimiter.length() - 1);
                                            }
                                        hereDocDelimiterLenght = hereDocDelimiter.length();
                                        hereDocStart = -1;
                                    } else {
                                        // the heredoc delimiter is not finished yet
                                        state = State.IN_PHP;
                                    }
                                } else {
                                    // check whether is on the line is just the heredoc delimiter
                                    if (text.charAt(textLength - 2) == ';'
                                            && (text.charAt(textLength - 3 - hereDocDelimiterLenght) == '\r'
                                            || text.charAt(textLength - 3 - hereDocDelimiterLenght) == '\n')
                                            && TokenUtilities.textEquals(hereDocDelimiter, text.subSequence(textLength - 2 - hereDocDelimiterLenght, textLength - 2))) {
                                        // heredoc finished
                                        state = State.IN_PHP;
                                    }
                                }
                                break;
                            default:
                                //no-op
                        }
                    default:
                        //no-op
                }
                c = input.read();
            }

            switch (state) {
                case IN_PHP:
                case IN_PHP_CONSTANT_STRING:
                case IN_PHP_HEREDOC_STRING:
                case IN_PHP_STRING:
                    return PHPTopTokenId.T_PHP;
                case IN_PHP_DELIMITER_SHORT:
                case IN_PHP_DELIMITER:
                    return PHPTopTokenId.T_PHP_OPEN_DELIMITER;
                default:
                    return PHPTopTokenId.T_HTML;
            }
        }

        Object getState() {
            return new Object();
        }
    }
}
