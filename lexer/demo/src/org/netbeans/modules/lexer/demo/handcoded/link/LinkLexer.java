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

package org.netbeans.modules.lexer.demo.handcoded.link;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.util.Compatibility;

/**
 * Lexer that recognizes LinkLanguage.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class LinkLexer implements Lexer {

    private static final LinkLanguage language = LinkLanguage.get();
    
    private static final int INIT = 0;
    private static final int IN_SCHEME = 1;
    private static final int AFTER_COLON = 2;
    private static final int AFTER_SLASH = 3;
    
    /** Map for mapping scheme to uri type */
    private static final Map scheme2uri = new HashMap();
    
    static {
        scheme2uri.put("http", LinkLanguage.HTTP_URI);
        scheme2uri.put("ftp", LinkLanguage.FTP_URI);
    }
    
    private LexerInput lexerInput;
    
    /** Index of first char after scheme name e.g. "http" or "ftp" */
    private int schemeEnd;
    
    /** Reused text buffer of the uri scheme */
    private Object uriSchemeReusedText;
    
    public LinkLexer() {
    }
    
    public Object getState() {
        return null;
    }

    public void restart(LexerInput input, Object state) {
        this.lexerInput = input;
        if (input == null) { // this input is no longer being used by this lexer
            uriSchemeReusedText = null; // free the reused text
        }
    }

    public Token nextToken() {
        Token token = null;
        int uriStart = findURIStart();
        switch (uriStart) {
            case -1: // no link found
                if (lexerInput.getReadLength() > 0) { // at least one char read
                    token = lexerInput.createToken(LinkLanguage.TEXT);
                }
                break;
                
            case 0: // link at the begining of token
                // Reading is positioned after "scheme://"
                findURIEnd();
                // Now read is positioned at the first non-matching char
 
                // Get the scheme in compatible way - replacement of LexerInput.getReadText()
                uriSchemeReusedText = Compatibility.getCompatibleReadText(
                    lexerInput, 0, schemeEnd, uriSchemeReusedText);

                TokenId uriType = (TokenId)scheme2uri.get(uriSchemeReusedText);
                if (uriType == null) {
                    uriType = LinkLanguage.URI;
                }
                
                token = lexerInput.createToken(uriType);
                break;
                
            default: // link occurs on the line but not at the begining
                token = lexerInput.createToken(LinkLanguage.TEXT, uriStart);
                lexerInput.backup(lexerInput.getReadLength()); // backup the extra read chars
                break;
        }
        
        return token;
    }
    
    private int findURIStart() {
        int state = INIT;
        int uriStart = -1;

        schemeEnd = 0;

        int ch = lexerInput.read();
        while (ch != LexerInput.EOF && ch != '\n') {
            switch (ch) {
                case ':':
                    switch (state) {
                        case IN_SCHEME:
                            state = AFTER_COLON;
                            schemeEnd = lexerInput.getReadLength() - 1; // exclude ':'
                            break;

                        default:
                            uriStart = -1;
                            state = INIT;
                            break;
                    }
                    break;

                case '/':
                    switch (state) {
                        case AFTER_COLON:
                            state = AFTER_SLASH;
                            break;

                        case AFTER_SLASH: // found "scheme://" => return success
                            return uriStart;

                        default:
                            uriStart = -1;
                            state = INIT;
                            break;
                    }
                    break;

                case '.': // can be part of URI scheme
                case '+': // can be part of URI scheme
                case '-': // can be part of URI scheme
                    switch (state) {
                        // case IN_SCHEME: // stay in scheme
                        default:
                            uriStart = -1;
                            state = INIT;
                            break;
                    }
                    break;

                default:
                    if (isAlpha(ch)) { // alpha char
                        switch (state) {
                            case INIT:
                                // mark begining of possible uri
                                uriStart = lexerInput.getReadLength() - 1;
                                state = IN_SCHEME;
                                break;

                            case IN_SCHEME: // stay in scheme
                                break;

                            default:
                                uriStart = -1;
                                state = INIT;
                                break;
                        }
                        
                    } else if (isDigit(ch)) {
                        switch (state) {
                            case IN_SCHEME: // stay in scheme
                                break;
                                
                            default:
                                uriStart = -1;
                                state = INIT;
                                break;
                        }
                        
                    } else {
                        uriStart = -1;
                        state = INIT;
                    }
            }
         
            ch = lexerInput.read();
        }
        
        // EOF or '\n' reached
        return -1;
    }
    
    private int findURIEnd() {
        int ch = lexerInput.read();
        while (ch != LexerInput.EOF && ch != '\n') {
            boolean stop = false;

            switch (ch) {
                // Allowed chars after "scheme://" follow - there is no particular
                // syntax observed although normally it should be
                case '#':
                case ':':
                case '?':
                case ';':
                case '&':
                case '@':
                case '=':
                case '+':
                case '-':
                case '$':
                case ',':
                case '/':
                case '.':
                case '_':
                case '!':
                case '~':
                case '\'':
                case ')':
                case '(':
                case '%':
                    break;
                    
                default:
                    if (!isAlpha(ch) && !isDigit(ch)) {
                        stop = true;
                    }
                    break;
                    
            }
            
            if (stop) {
                break;
            }
            
            ch = lexerInput.read();
        }
        
        if (ch != LexerInput.EOF) { // rollback the last char
            lexerInput.backup(1);
        }
        
        // EOF or '\n' reached
        return -1;
    }
    
    private static boolean isAlpha(int ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
    }
    
    private static boolean isDigit(int ch) {
        return ('0' <= ch && ch <= '9');
    }
    

}
