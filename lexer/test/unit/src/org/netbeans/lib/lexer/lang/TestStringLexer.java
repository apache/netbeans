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

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for simple string language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class TestStringLexer implements Lexer<TestStringTokenId> {

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;

    private TokenFactory<TestStringTokenId> tokenFactory;
    
    public TestStringLexer(LexerRestartInfo<TestStringTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // passed argument always null
    }
    
    public Object state() {
        return null;
    }
    
    public Token<TestStringTokenId> nextToken() {
        while(true) {
            int ch = input.read();
            switch (ch) {
                case EOF:
                    if (input.readLength() > 0)
                        return token(TestStringTokenId.TEXT);
                    else
                        return null;
                case '\\':
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return tokenFactory.createToken(TestStringTokenId.TEXT, input.readLength());
                    }
                    switch (ch = input.read()) {
                                                case 'b':
                            return token(TestStringTokenId.BACKSPACE);
                        case 'f':
                            return token(TestStringTokenId.FORM_FEED);
                        case 'n':
                            return token(TestStringTokenId.NEWLINE);
                        case 't':
                            return token(TestStringTokenId.TAB);
                        case '\'':
                            return token(TestStringTokenId.SINGLE_QUOTE);
                        case '"':
                            return token(TestStringTokenId.DOUBLE_QUOTE);
                        case '\\':
                            return token(TestStringTokenId.BACKSLASH);
                        case '0': case '1': case '2': case '3':
                            switch (input.read()) {
                                                                case '0': case '1': case '2': case '3':
                                case '4': case '5': case '6': case '7':
                                    switch (input.read()) {
                                                                                case '0': case '1': case '2': case '3':
                                        case '4': case '5': case '6': case '7':
                                            return token(TestStringTokenId.OCTAL_ESCAPE);
                                    }
                                    return token(TestStringTokenId.OCTAL_ESCAPE_INVALID);
                            }
                            return token(TestStringTokenId.OCTAL_ESCAPE_INVALID);
                    }
                    return token(TestStringTokenId.ESCAPE_SEQUENCE_INVALID);
            } // end of switch (ch)
        } // end of while(true)
    }

    private Token<TestStringTokenId> token(TestStringTokenId id) {
        return tokenFactory.createToken(id);
    }
    
    public void release() {
    }

}
