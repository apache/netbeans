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
package org.netbeans.modules.languages.yaml;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author Tor Norbye
 */
public final class YamlLexer implements Lexer<YamlTokenId> {

    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final TokenFactory<YamlTokenId> tokenFactory;
    //main internal lexer state
    private int state = ISI_WHITESPACE;
    // Internal analyzer states
    private static final int ISI_WHITESPACE = 0;  // initial lexer state = content language, no whitespace seen
    private static final int ISA_LT = 1; // after '<' char
    private static final int ISA_LT_PC = 2; // after '<%' - comment or directive or scriptlet
    private static final int ISI_SCRIPTLET = 3; // inside Ruby scriptlet
    private static final int ISI_SCRIPTLET_PC = 4; // just after % in scriptlet
    private static final int ISI_COMMENT_SCRIPTLET = 5; // Inside a Ruby comment scriptlet
    private static final int ISI_COMMENT_SCRIPTLET_PC = 6; // just after % in a Ruby comment scriptlet
    private static final int ISI_EXPR_SCRIPTLET = 7; // inside Ruby expression scriptlet
    private static final int ISI_EXPR_SCRIPTLET_PC = 8; // just after % in an expression scriptlet
    private static final int ISI_RUBY_LINE = 9; // just after % in an %-line
    private static final int ISI_NONWHITESPACE = 10; // after seeing non space characters on a line
    private static final int ISI_PHP = 11; // after <?

    /**
     * A Lexer for ruby strings
     *
     * @param substituting If true, handle substitution rules for double quoted
     * strings, otherwise single quoted strings.
     */
    public YamlLexer(LexerRestartInfo<YamlTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            this.state = ISI_WHITESPACE;
        } else {
            state = ((Integer) info.state()).intValue();
        }
    }

    @Override
    public Object state() {
        return state;
    }

    @Override
    public Token<YamlTokenId> nextToken() {
        // TODO - support embedded Ruby in <% %> tags.
        // This is used in fixtures files from Rails for example; see
        //   http://api.rubyonrails.com/classes/Fixtures.html
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

            switch (state) {
                case ISI_NONWHITESPACE:
                    if (actChar == '\n') {
                        state = ISI_WHITESPACE;
                        return token(YamlTokenId.TEXT);
                    }
                // Fallthrough

                case ISI_WHITESPACE:
                    switch (actChar) {
                        case '#': {
                            if (state == ISI_WHITESPACE) {
                                // Comment
                                if (input.readLength() > 1) {
                                    input.backup(1);
                                    return token(YamlTokenId.TEXT);
                                }

                                int ch = input.read();
                                while (!(ch == EOF || ch == '\r' || ch == '\n')) {
                                    ch = input.read();
                                }
                                //if (ch != EOF) {
                                //    input.backup(1);
                                //}
                                state = ISI_WHITESPACE;
                                return token(YamlTokenId.COMMENT);
                            }
                        }
                        break;

                        case '<':
                            state = ISA_LT;
                            break;

                        case '%': {
                            int peek = input.read();
                            if (peek == '%') {
                                // %% means just %
                                break;
                            }
                            if (peek != LexerInput.EOF) {
                                input.backup(1);
                            }

                            // See if we're in a line prefix
                            if (input.readLength() == 1) {
                                state = ISI_RUBY_LINE;
                                return token(YamlTokenId.DELIMITER);
                            }
                            CharSequence cs = input.readText();
                            // -2: skip the final %
                            for (int i = cs.length() - 2; i >= 0; i--) {
                                char c = cs.charAt(i);
                                if (c == '\n') {
                                    // We're in a new line: Finish this token as HTML.
                                    input.backup(1);
                                    // When we come back we'll just process the line as a delimiter
                                    return token(YamlTokenId.TEXT);
                                } else if (!Character.isWhitespace(c)) {
                                    // The % is not the beginning of a line
                                    break;
                                }
                            }
                            break;
                        }
                        case ' ':
                            break;

                        default:
                            if (!Character.isWhitespace(actChar)) {
                                state = ISI_NONWHITESPACE;
                            }
                            break;
                    }
                    break;

                case ISA_LT:
                    switch (actChar) {
                        case '%':
                            state = ISA_LT_PC;
                            break;
                        case '?':
                            state = ISI_PHP;
                            if (input.readLength() > 2) {
                                input.backup(2);
                                return token(YamlTokenId.TEXT);
                            }
                            break;
                        default:
                            state = ISI_WHITESPACE; //just content
//                            state = ISI_TAG_ERROR;
//                            break;
                    }
                    break;

                case ISA_LT_PC:
                    switch (actChar) {
                        case '=':
                            if (input.readLength() == 3) {
                                // just <%! or <%= read
                                state = ISI_EXPR_SCRIPTLET;
                                return token(YamlTokenId.DELIMITER);
                            } else {
                                // RHTML symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%=
                                state = ISI_WHITESPACE;
                                return token(YamlTokenId.TEXT); //return CL token
                            }
                        case '%': {
                            int peek = input.read();
                            if (peek != LexerInput.EOF) {
                                input.backup(1);
                            }
                            if (peek != '>') {
                                // Handle <%% == <%
                                if (input.readLength() == 3) {
                                    // <%% is just an escape for <% in HTML...
                                    state = ISI_WHITESPACE;
                                    break;
                                } else {
                                    // RHTML symbol, but we also have content language in the buffer
                                    input.backup(3); //backup <%@
                                    state = ISI_WHITESPACE;
                                    return token(YamlTokenId.TEXT); //return CL token
                                }
                            } else if (input.readLength() == 3) {
                                // We have <%%> - it's just a <% opener followed by a %> closer;
                                // digest the open delimiter now
                                input.backup(1);
                                state = ISI_SCRIPTLET;
                                return token(YamlTokenId.DELIMITER);
                            } else {
                                state = ISI_WHITESPACE;
                                input.backup(3);
                                return token(YamlTokenId.TEXT);
                            }
                        }

                        case '#':
                            if (input.readLength() == 3) {
                                // just <%! or <%= read
                                state = ISI_COMMENT_SCRIPTLET;
                                return token(YamlTokenId.DELIMITER);
                            } else {
                                //ERB symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%! or <%=
                                state = ISI_WHITESPACE;
                                return token(YamlTokenId.TEXT); //return CL token
                            }
                        case '-':
                            if (input.readLength() == 3) {
                                // just read <%-
                                state = ISI_SCRIPTLET;
                                return token(YamlTokenId.DELIMITER);
                            } else {
                                // RHTML symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%-
                                state = ISI_WHITESPACE;
                                return token(YamlTokenId.TEXT); //return CL token
                            }
                        default:  // RHTML scriptlet delimiter '<%'
                            if (input.readLength() == 3) {
                                // just <% + something != [=,#] read
                                state = ISI_SCRIPTLET;
                                input.backup(1); //backup the third character, it is a part of the Ruby scriptlet
                                return token(YamlTokenId.DELIMITER);
                            } else {
                                // RHTML symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%@
                                state = ISI_WHITESPACE;
                                return token(YamlTokenId.TEXT); //return CL token
                            }
                    }
                    break;

                case ISI_COMMENT_SCRIPTLET:
                    switch (actChar) {
                        case '%':
                            state = ISI_COMMENT_SCRIPTLET_PC;
                            break;
                    }
                    break;


                case ISI_SCRIPTLET:
                    switch (actChar) {
                        case '%':
                            state = ISI_SCRIPTLET_PC;
                            break;
                    }
                    break;


                case ISI_SCRIPTLET_PC:
                    switch (actChar) {
                        case '>':
                            if (input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = ISI_WHITESPACE;
                                return token(YamlTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = ISI_SCRIPTLET;
                                return token(YamlTokenId.RUBY);
                            }
                        default:
                            state = ISI_SCRIPTLET;
                            break;
                    }
                    break;

                case ISI_RUBY_LINE:
                    while (actChar != '\n') {
                        actChar = input.read();
                        if (actChar == LexerInput.EOF) {
                            break;
                        }
                    }
                    if (actChar == '\n') {
                        input.backup(1);
                    }
                    state = ISI_WHITESPACE;
                    if (input.readLength() > 0) {
                        return token(YamlTokenId.RUBY);
                    }
                    break;

                case ISI_EXPR_SCRIPTLET:
                    switch (actChar) {
                        case '%':
                            state = ISI_EXPR_SCRIPTLET_PC;
                            break;
                    }
                    break;


                case ISI_EXPR_SCRIPTLET_PC:
                    switch (actChar) {
                        case '>':
                            if (input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = ISI_WHITESPACE;
                                return token(YamlTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = ISI_EXPR_SCRIPTLET;
                                return token(YamlTokenId.RUBY_EXPR);
                            }
                        default:
                            state = ISI_EXPR_SCRIPTLET;
                            break;
                    }
                    break;

                case ISI_COMMENT_SCRIPTLET_PC:
                    switch (actChar) {
                        case '>':
                            if (input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = ISI_WHITESPACE;
                                return token(YamlTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = ISI_COMMENT_SCRIPTLET;
                                return token(YamlTokenId.RUBYCOMMENT);
                            }
                        default:
                            state = ISI_COMMENT_SCRIPTLET;
                            break;
                    }
                    break;
                case ISI_PHP:
                    if (actChar == '>' && input.readText().charAt(input.readLength() - 2) == '?') {
                        state = ISI_WHITESPACE;
                        return token(YamlTokenId.PHP);
                    }
                    break;
            }
        }

        // At this stage there's no more text in the scanned buffer.
        // Scanner first checks whether this is completely the last
        // available buffer.

        switch (state) {
            case ISI_NONWHITESPACE:
            case ISI_WHITESPACE:
                if (input.readLength() == 0) {
                    return null;
                } else {
                    return token(YamlTokenId.TEXT);
                }
            case ISA_LT:
                state = ISI_WHITESPACE;
                return token(YamlTokenId.DELIMITER);
            case ISA_LT_PC:
                state = ISI_WHITESPACE;
                return token(YamlTokenId.DELIMITER);
            case ISI_SCRIPTLET_PC:
                state = ISI_WHITESPACE;
                return token(YamlTokenId.DELIMITER);
            case ISI_SCRIPTLET:
                state = ISI_WHITESPACE;
                return token(YamlTokenId.RUBY);
            case ISI_EXPR_SCRIPTLET_PC:
                state = ISI_WHITESPACE;
                return token(YamlTokenId.DELIMITER);
            case ISI_EXPR_SCRIPTLET:
                state = ISI_WHITESPACE;
                return token(YamlTokenId.RUBY_EXPR);
            case ISI_COMMENT_SCRIPTLET_PC:
                state = ISI_WHITESPACE;
                return token(YamlTokenId.DELIMITER);
            case ISI_COMMENT_SCRIPTLET:
                state = ISI_WHITESPACE;
                return token(YamlTokenId.RUBYCOMMENT);
            case ISI_PHP:
                state = ISI_WHITESPACE;
                return token(YamlTokenId.PHP);
            default:
                System.out.println("RhtmlLexer - unhandled state : " + state);   // NOI18N
        }

        return null;
    }

    private Token<YamlTokenId> token(YamlTokenId id) {
        return tokenFactory.createToken(id);
    }

    @Override
    public void release() {
    }
}
