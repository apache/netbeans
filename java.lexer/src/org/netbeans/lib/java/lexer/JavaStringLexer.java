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

package org.netbeans.lib.java.lexer;

import org.netbeans.api.java.lexer.JavaStringTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for java string language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavaStringLexer<T extends TokenId> implements Lexer<T> {

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;
    
    private TokenFactory<T> tokenFactory;
    private boolean isJavaStringTokenId;
    
    public JavaStringLexer(LexerRestartInfo<T> info, boolean isJavaStringTokenId) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.isJavaStringTokenId = isJavaStringTokenId;
        assert (info.state() == null); // passed argument always null
    }
   
    public Object state() {
        return null;
    }
    
    public Token<T> nextToken() {
        while(true) {
            int ch = input.read();
            switch (ch) {
                case EOF:
                    if (input.readLength() > 0)
                        return token((T) (isStringTokenId() ? JavaStringTokenId.TEXT : JavaCharacterTokenId.TEXT));
                    else
                        return null;
                case '\\': //NOI18N
                    if (input.readLength() > 1) {// already read some text
                        input.backup(1);
                        return tokenFactory.createToken((T) (isStringTokenId() ? JavaStringTokenId.TEXT : JavaCharacterTokenId.TEXT), input.readLength());
                    }
                    switch (ch = input.read()) {
                        case 'b': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.BACKSPACE : JavaCharacterTokenId.BACKSPACE));
                        case 'f': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.FORM_FEED : JavaCharacterTokenId.FORM_FEED));
                        case 'n': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.NEWLINE : JavaCharacterTokenId.NEWLINE));
                        case 'r': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.CR : JavaCharacterTokenId.CR));
                        case 't': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.TAB : JavaCharacterTokenId.TAB));
                        case '\'': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.SINGLE_QUOTE : JavaCharacterTokenId.SINGLE_QUOTE));
                        case '"': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.DOUBLE_QUOTE : JavaCharacterTokenId.DOUBLE_QUOTE));
                        case '\\': //NOI18N
                            return token((T) (isStringTokenId() ? JavaStringTokenId.BACKSLASH : JavaCharacterTokenId.BACKSLASH));
                        case 'u': //NOI18N
                            while ('u' == (ch = input.read())) {}; //NOI18N
                            
                            for(int i = 0; ; i++) {
                                ch = Character.toLowerCase(ch);
                                
                                if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'f')) { //NOI18N
                                    input.backup(1);
                                    return token((T) (isStringTokenId() ? JavaStringTokenId.UNICODE_ESCAPE_INVALID : JavaCharacterTokenId.UNICODE_ESCAPE_INVALID));
                                }
                             
                                if (i == 3) { // four digits checked, valid sequence
                                    return token((T) (isStringTokenId() ? JavaStringTokenId.UNICODE_ESCAPE : JavaCharacterTokenId.UNICODE_ESCAPE));
                                }
                                
                                ch = input.read();
                            }
                            
                        case '0': case '1': case '2': case '3': //NOI18N
                            switch (input.read()) {
                                case '0': case '1': case '2': case '3': //NOI18N
                                case '4': case '5': case '6': case '7': //NOI18N
                                    switch (input.read()) {
                                        case '0': case '1': case '2': case '3': //NOI18N
                                        case '4': case '5': case '6': case '7': //NOI18N
                                            return token((T) (isStringTokenId() ? JavaStringTokenId.OCTAL_ESCAPE : JavaCharacterTokenId.OCTAL_ESCAPE));
                                    }
                                    input.backup(1);
                                    return token((T) (isStringTokenId() ? JavaStringTokenId.OCTAL_ESCAPE_INVALID : JavaCharacterTokenId.OCTAL_ESCAPE_INVALID));
                            }
                            input.backup(1);
                            return token((T) (isStringTokenId() ? JavaStringTokenId.OCTAL_ESCAPE_INVALID : JavaCharacterTokenId.OCTAL_ESCAPE_INVALID));
                    }
                    input.backup(1);
                    return token((T) (isStringTokenId() ? JavaStringTokenId.ESCAPE_SEQUENCE_INVALID : JavaCharacterTokenId.ESCAPE_SEQUENCE_INVALID));
            } // end of switch (ch)
        } // end of while(true)
    }

    private Token<T> token(T id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }

    private boolean isStringTokenId() {
        return isJavaStringTokenId;
    }

}
