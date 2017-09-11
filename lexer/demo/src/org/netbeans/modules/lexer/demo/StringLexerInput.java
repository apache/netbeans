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

package org.netbeans.modules.lexer.demo;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.util.AbstractCharSequence;
import org.netbeans.spi.lexer.util.CharSubSequence;
import org.netbeans.spi.lexer.util.Compatibility;

/**
 * Token iterator that works over the given char sequence.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class StringLexerInput implements LexerInput {

    private String text;

    /** Index from which the read() methods read the next character */
    private int inputIndex;
    
    /** Index of the begining of the current token */
    private int tokenIndex;
    
    /** Helper variable for getReadLookahead() computation. */
    private int lookaheadIndex;
    
    /** 1 if lookahead reached EOF or 0 if not */
    private int eof;

    /**
     * Lazily created character sequence representing subsequence of text read 
     * from the lexer input by read() operations.
     */
    private CharSubSequence subReadText;
    
    public StringLexerInput(String text) {
        this.text = text;
    }
    
    public int read() {
        if (inputIndex >= text.length()) {
            eof = 1;
            return LexerInput.EOF;
            
        } else {
            return text.charAt(inputIndex++);
        }
    }
    
    public int getReadLookahead() {
        return Math.max(lookaheadIndex, inputIndex + eof) - tokenIndex;
    }
    
    public int getReadLength() {
        return inputIndex - tokenIndex;
    }
    
    public boolean isEOFLookahead() {
        return (eof != 0);
    }
    
    public void backup(int count) {
        lookaheadIndex = Math.max(lookaheadIndex, inputIndex + eof);
        inputIndex -= count;
        if (inputIndex < tokenIndex) {
            inputIndex += count;
            throw new IllegalArgumentException("count=" + count
                + " > " + (inputIndex - tokenIndex));
            
        } else if (inputIndex > lookaheadIndex - eof) {
            inputIndex += count;
            throw new IllegalArgumentException("count=" + count
                + " < " + (inputIndex + eof - lookaheadIndex));
        }
    }
    
    public Token createToken(TokenId id, int tokenLength) {
        if (tokenLength <= 0) {
            throw new IllegalArgumentException("tokenLength="
                + tokenLength + " <= 0");
        }

        if (tokenIndex + tokenLength > inputIndex) {
            throw new IllegalArgumentException("tokenLength="
                + tokenLength + " > number-of-read-characters="
                + (inputIndex - tokenIndex)
            );
        }

        Token ret = new StringToken(id, text.substring(tokenIndex,
            tokenIndex + tokenLength));
        tokenIndex += tokenLength;
        return ret;
    }
    
    public Token createToken(TokenId id) {
        return createToken(id, inputIndex - tokenIndex);
    }

    public CharSequence getReadText(int start, int end) {
        if (subReadText == null) {
            subReadText = new CharSubSequence(new ReadText());
        }

        subReadText.setBounds(start, end);

        return subReadText;
    }    
    
    char readTextCharAt(int index) {
        if (index < 0) {
            throw  new IndexOutOfBoundsException("index=" + index + " < 0");
        }

        if (index >= getReadLength()) {
            throw new IndexOutOfBoundsException("index=" + index
                + " >= getReadLength()=" + getReadLength());
        }

        return text.charAt(tokenIndex + index);
    }
    
    private class ReadText extends AbstractCharSequence {
        
        public int length() {
            return getReadLength();
        }

        public char charAt(int index) {
            return readTextCharAt(index);
        }
        
    }

}
