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

package org.netbeans.lib.lexer.lang;

import org.netbeans.lib.lexer.lang.TestTokenId;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Simple implementation a lexer.
 *
 * @author mmetelka
 */
final class TestLexer implements Lexer<TestTokenId> {

    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;

    private static final Map<String,TestTokenId> keywords = new HashMap<String,TestTokenId>();
    static {
        keywords.put(TestTokenId.PUBLIC.fixedText(),TestTokenId.PUBLIC);
        keywords.put(TestTokenId.PRIVATE.fixedText(),TestTokenId.PRIVATE);
        keywords.put(TestTokenId.STATIC.fixedText(),TestTokenId.STATIC);
    }
    
    
    private final LexerInput input;
    
    private final TokenFactory<TestTokenId> tokenFactory;
    
    private final int version;

    TestLexer(LexerRestartInfo<TestTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // never set to non-null value in state()

        Integer ver = (Integer)info.getAttributeValue("version");
        this.version = (ver != null) ? ver.intValue() : 2;
    }

    public Object state() {
        return null; // always in default state after token recognition
    }

    public Token<TestTokenId> nextToken() {
        while (true) {
            int c = input.read();
            switch (c) {
                                case '"': // string literal
                    while (true) {
                        switch (input.read()) {
                                                        case '"': // NOI18N
                                return token(TestTokenId.STRING_LITERAL);
                            case '\\':
                                input.read(); // skip the next character
                                break;
                            case '\r': input.consumeNewline();
                            case '\n':
                            case EOF:
                                return token(TestTokenId.STRING_LITERAL_INCOMPLETE);
                        }
                    }

                case '+':
                    switch (input.read()) {
                                                case '-':
                            if (input.read() == '+')
                                return token(TestTokenId.PLUS_MINUS_PLUS);
                            input.backup(2);
                            return token(TestTokenId.PLUS);

                    }
                    input.backup(1);
                    return token(TestTokenId.PLUS);

                case '-':
                    return token(TestTokenId.MINUS);

                case '*':
                    return token(TestTokenId.STAR);
                            
                case '/':
                    switch (input.read()) {
                                                case '/': // in single-line comment
                            while (true)
                                switch (input.read()) {
                                                                        case '\r': input.consumeNewline();
                                    case '\n':
                                    case EOF:
                                        return token(TestTokenId.LINE_COMMENT);
                                }
                        case '*': // in multi-line or javadoc comment
                            c = input.read();
                            if (c == '*') { // either javadoc comment or empty multi-line comment /**/
                                    c = input.read();
                                    if (c == '/')
                                        return token(TestTokenId.BLOCK_COMMENT);
                                    while (true) { // in javadoc comment
                                        while (c == '*') {
                                            c = input.read();
                                            if (c == '/')
                                                return token(TestTokenId.JAVADOC_COMMENT);
                                            else if (c == EOF)
                                                return token(TestTokenId.JAVADOC_COMMENT_INCOMPLETE);
                                        }
                                        if (c == EOF)
                                            return token(TestTokenId.JAVADOC_COMMENT_INCOMPLETE);
                                        c = input.read();
                                    }

                            } else { // in multi-line comment (and not after '*')
                                while (true) {
                                    c = input.read();
                                    while (c == '*') {
                                        c = input.read();
                                        if (c == '/')
                                            return token(TestTokenId.BLOCK_COMMENT);
                                        else if (c == EOF)
                                            return token(TestTokenId.BLOCK_COMMENT_INCOMPLETE);
                                    }
                                    if (c == EOF)
                                        return token(TestTokenId.BLOCK_COMMENT_INCOMPLETE);
                                }
                            }
                    }
                    input.backup(1);
                    return token(TestTokenId.DIV);

                // All Character.isWhitespace(c) below 0x80 follow
                // ['\t' - '\r'] and [0x1c - ' ']
                case '\t':
                case '\n':
                case 0x0b:
                case '\f':
                case '\r':
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:
                    return finishWhitespace();
                case ' ':
                    c = input.read();
                    if (c == EOF || !Character.isWhitespace(c)) { // Return single space as flyweight token
                        input.backup(1);
                        return tokenFactory.getFlyweightToken(TestTokenId.WHITESPACE, " ");
                    }
                    return finishWhitespace();

                case EOF: // no more chars on the input
                    return null; // the only legal situation when null can be returned

                default:
                    if (Character.isJavaIdentifierStart((char)c))
                        return finishIdentifier();
                    if (c >= 0x80 && Character.isWhitespace((char)c))
                        return finishWhitespace();

                    // Invalid char
                    return token(TestTokenId.ERROR);
            }
        }
    }
        
    private Token<TestTokenId> finishWhitespace() {
        while (true) {
            int c = input.read();
            if (c == EOF || !Character.isWhitespace(c)) {
                input.backup(1);
                return tokenFactory.createToken(TestTokenId.WHITESPACE);
            }
        }
    }
    
    private Token<TestTokenId> finishIdentifier() {
        while (true) {
            int c = input.read();
            if (c == EOF || !Character.isJavaIdentifierPart(c)) {
                input.backup(1);
                // Check whether the identifier is not a keyword
                TestTokenId id = keywords.get(input.readText());
                // Test: Only recognize STATIC keyword in version >= 2
                if (id == TestTokenId.STATIC && version < 2) {
                    id = null; // force IDENTIFIER
                }
                return (id == null)
                        ? tokenFactory.createToken(TestTokenId.IDENTIFIER)
                        : token(id);
            }
        }
    }

    private Token<TestTokenId> token(TestTokenId id) {
        String fixedText = id.fixedText();
        return (fixedText != null)
                ? tokenFactory.getFlyweightToken(id, fixedText)
                : tokenFactory.createToken(id);
    }
    
    public void release() {
    }

}
