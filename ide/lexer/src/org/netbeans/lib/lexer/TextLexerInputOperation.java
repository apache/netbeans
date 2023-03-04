/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
