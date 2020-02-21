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

package org.netbeans.modules.cnd.lexer;

import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.DoxygenTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for doxygen language.
 * based on JavadocLexer
 * 
 * @version 1.00
 */

public class DoxygenLexer implements Lexer<DoxygenTokenId> {

    private static final int EOF = LexerInput.EOF;

    // states
    private static final int INIT = 0;
    private static final int OTHER = 1;

    private LexerInput input;
    private int state = INIT;

    private TokenFactory<DoxygenTokenId> tokenFactory;
    
    public DoxygenLexer(LexerRestartInfo<DoxygenTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        fromState((Integer)info.state());
    }
    
    @Override
    public Object state() {
        return state == INIT ? null : Integer.valueOf(state);
    }

    private void fromState(Integer state) {
        if (state == null) {
            this.state = INIT;
        } else {
            this.state = state.intValue();
        }
    }
    
    private final static String DOXYGEN_CONTROL_SYMBOLS = "@\\<.#"; // NOI18N
    
    public Token<DoxygenTokenId> nextToken() {
        int ch = input.read();
        
        if (ch == EOF) {
            return null;
        }
        
        int oldState = state;
        state = OTHER;
        if (oldState == INIT && ch == '<') {
            return token(DoxygenTokenId.POINTER_MARK);
        }

        if (CndLexerUtilities.isCppIdentifierStart(ch)) {
            //TODO: EOF
            while (CndLexerUtilities.isCppIdentifierPart(input.read())) {}
            
            input.backup(1);
            return token(DoxygenTokenId.IDENT);
        }
        
        if (DOXYGEN_CONTROL_SYMBOLS.indexOf(ch) == (-1)) {
            //TODO: EOF
            ch = input.read();
            
            while (!CndLexerUtilities.isCppIdentifierStart(ch) && DOXYGEN_CONTROL_SYMBOLS.indexOf(ch) == (-1) && ch != EOF) {
                ch = input.read();
            }
            
            if (ch != EOF) {
                input.backup(1);
            }
            return token(DoxygenTokenId.OTHER_TEXT);
        }
        
        switch (ch) {
            case '@':
            case '\\':
            {
                boolean first = true;
                while (true) {
                    ch = input.read();
                    boolean wasFirst = first;
                    first = false;
                    if ((wasFirst && !CndLexerUtilities.isCppIdentifierStart(ch)) || (!CndLexerUtilities.isCppIdentifierPart(ch))) {
                        input.backup(1);
                        if (input.readLength() > 1) {
                            return tokenFactory.createToken(DoxygenTokenId.TAG, input.readLength());
                        } else {
                            // no identifier after control symbol => control symbol is not tag-start
                            return tokenFactory.createToken(DoxygenTokenId.OTHER_TEXT, input.readLength());
                        }
                    }
                }
            }
            case '<':
                while (true) {
                    ch = input.read();
                    if (ch == '>' || ch == EOF) {
                        return token(DoxygenTokenId.HTML_TAG);
                    }
                }
            case '.':
                return token(DoxygenTokenId.DOT);
            case '#':
                return token(DoxygenTokenId.HASH);
        } // end of switch (ch)
        
        assert false;
        
        return null;
    }

    private Token<DoxygenTokenId> token(DoxygenTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }

}
