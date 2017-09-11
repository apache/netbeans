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

import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Abstract lexer input operation over a character sequence.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public class TextLexerInputOperation<T extends TokenId> extends LexerInputOperation<T> {
    
    /**
     * Input text from which the reading of characters is done.
     */
    private final CharSequence inputSourceText;

    /**
     * Point beyond which the reading cannot go.
     */
    private int readEndOffset;


    public TextLexerInputOperation(TokenList<T> tokenList) {
        this(tokenList, 0, null, 0, -1);
    }

    public TextLexerInputOperation(TokenList<T> tokenList, int tokenIndex,
    Object lexerRestartState, int startOffset, int endOffset) {
        super(tokenList, tokenIndex, lexerRestartState);
        this.inputSourceText = tokenList.inputSourceText();
        if (endOffset == -1) {
            endOffset = inputSourceText.length();
        }
        if ((startOffset < 0) || (startOffset > endOffset) || (endOffset > inputSourceText.length())) {
            throw new IndexOutOfBoundsException("startOffset=" + startOffset + ", endOffset=" + endOffset +
                ", inputSourceText.length()=" + inputSourceText.length());
        }
        tokenStartOffset = startOffset;
        readOffset = tokenStartOffset;
        readEndOffset = endOffset;
    }
    
    public int read(int offset) {
        if (offset < readEndOffset) {
            return inputSourceText.charAt(offset);
        } else { // must read next or return EOF
            return LexerInput.EOF;
        }
    }

    public char readExisting(int offset) {
        return inputSourceText.charAt(offset);
    }

    protected void fillTokenData(AbstractToken<T> token) {
        token.setRawOffset(tokenStartOffset);
    }
    
    protected final int readEndIndex() {
        return readEndOffset;
    }

}
