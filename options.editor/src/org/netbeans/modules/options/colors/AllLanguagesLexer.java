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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.options.colors;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 * @author Jan Jancura
 */
class AllLanguagesLexer implements Lexer<AllLanguagesTokenId> {


    private LexerRestartInfo<AllLanguagesTokenId> info;

    AllLanguagesLexer (LexerRestartInfo<AllLanguagesTokenId> info) {
        this.info = info;
    }

    public Token<AllLanguagesTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case '/':
                i = input.read ();
                if (i == '/')
                    return info.tokenFactory ().createToken (AllLanguagesTokenId.COMMENT);
                if (i == '*') {
                    i = input.read ();
                    while (i != LexerInput.EOF) {
                        while (i == '*') {
                            i = input.read ();
                            if (i == '/')
                                return info.tokenFactory ().createToken (AllLanguagesTokenId.COMMENT);
                        }
                        i = input.read ();
                    }
                    return info.tokenFactory ().createToken (AllLanguagesTokenId.COMMENT);
                }
                if (i != LexerInput.EOF)
                    input.backup (1);
                return info.tokenFactory ().createToken (AllLanguagesTokenId.OPERATOR);
            case '+':
            case '=':
                return info.tokenFactory ().createToken (AllLanguagesTokenId.OPERATOR);
            case '{':
            case '}':
            case '(':
            case ')':
            case ';':
                return info.tokenFactory ().createToken (AllLanguagesTokenId.SEPARATOR);
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                do {
                    i = input.read ();
                } while (
                    i == ' ' ||
                    i == '\n' ||
                    i == '\r' ||
                    i == '\t'
                );
                if (i != LexerInput.EOF)
                    input.backup (1);
                return info.tokenFactory ().createToken (AllLanguagesTokenId.WHITESPACE);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                do {
                    i = input.read ();
                } while (
                    i >= '0' &&
                    i <= '9'
                );
                if (i == '.') {
                    do {
                        i = input.read ();
                    } while (
                        i >= '0' &&
                        i <= '9'
                    );
                }
                input.backup (1);
                return info.tokenFactory ().createToken (AllLanguagesTokenId.NUMBER);
            case '"':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '"' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                return info.tokenFactory ().createToken (AllLanguagesTokenId.STRING);
            case '\'':
                i = input.read ();
                if (i == '\\')
                    i = input.read ();
                i = input.read ();
                if (i != '\'')
                    return info.tokenFactory ().createToken (AllLanguagesTokenId.ERROR);
                return info.tokenFactory ().createToken (AllLanguagesTokenId.CHARACTER);
            default:
                if (
                    (i >= 'a' && i <= 'z') ||
                    (i >= 'A' && i <= 'Z')
                ) {
                    do {
                        i = input.read ();
                    } while (
                        (i >= 'a' && i <= 'z') ||
                        (i >= 'A' && i <= 'Z') ||
                        (i >= '0' && i <= '9') ||
                        i == '_' ||
                        i == '-' ||
                        i == '~'
                    );
                    input.backup (1);
                    String id = input.readText ().toString ();
                    if (id.equals ("public") ||
                        id.equals ("class")
                    )
                        return info.tokenFactory ().createToken (AllLanguagesTokenId.KEYWORD);
                    return info.tokenFactory ().createToken (AllLanguagesTokenId.IDENTIFIER);
                }
                return info.tokenFactory ().createToken (AllLanguagesTokenId.ERROR);
        }
    }

    public Object state () {
        return null;
    }

    public void release () {
    }
}


