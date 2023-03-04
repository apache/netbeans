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

import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Filtering token list used by a token sub sequence.
 * <br/>
 * As the tokens are created lazily this list won't call tokenList.tokenCount()
 * until tokenCount() is called on itself.
 *
 * <p>
 * This list assumes single-threaded use only.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class SubSequenceTokenList<T extends TokenId> implements TokenList<T> {
    
    public static <T extends TokenId> SubSequenceTokenList<T> create(
    TokenList<T> tokenList, int limitStartOffset, int limitEndOffset) {
        return new SubSequenceTokenList<T>(tokenList, limitStartOffset, limitEndOffset);
    }
    
    /**
     * Token list to which this filtering token list delegates.
     */
    private TokenList<T> tokenList;
    
    /**
     * Limit of start offset under which the token sequence cannot move.
     * Integer.MIN_VALUE for no limit.
     */
    private final int limitStartOffset;
    
    /**
     * Limit of the end offset under which the token sequence cannot move.
     * Integer.MAX_VALUE for no limit.
     */
    private final int limitEndOffset;

    /**
     * Index of a first token in the underlying token list that this list provides.
     */
    private int limitStartIndex;
    
    /**
     * Initially Integer.MAX_VALUE to be computed lazily.
     */
    private int limitEndIndex;
    
    /**
     * Create new subsequence token list
     * @param tokenList non-null token list over which the subsequence gets created.
     * @param limitStartOffset lower offset bound offset or 0 for none.
     * @param limitEndOffset upper offset bound or Integer.MAX_VALUE for none.
     */
    public SubSequenceTokenList(TokenList<T> tokenList, int limitStartOffset, int limitEndOffset) {
        this.tokenList = tokenList;
        this.limitStartOffset = limitStartOffset;
        this.limitEndOffset = limitEndOffset;

        if (limitEndOffset == Integer.MAX_VALUE) { // No limit
            // No upper bound for end index so use tokenCount() (can be improved if desired)
            limitEndIndex = tokenList.tokenCount();
        } else { // Valid limit end offset
            int[] indexAndTokenOffset = tokenList.tokenIndex(limitEndOffset);
            limitEndIndex = indexAndTokenOffset[0];
            if (limitEndIndex != -1) {
                // If the limitStartOffset is "inside" a token and it's not at or beyond end of TL
                if (limitEndOffset > indexAndTokenOffset[1] && limitEndIndex < tokenList.tokenCountCurrent()) {
                    limitEndIndex++; // Include the token that contains the offset
                }
            } else { // No tokens at all
                limitEndIndex = 0;
            }
        }
            
        // Compute limitStartIndex (currently == 0)
        if (limitEndIndex > 0 && limitStartOffset > 0) {
            // Although the binary search could only be in <0,limitEndIndex> bounds
            // use regular TL.tokenIndex() because it has substantially better performance
            // e.g. in JoinTokenList.
            int[] indexAndTokenOffset = tokenList.tokenIndex(limitStartOffset);
            limitStartIndex = indexAndTokenOffset[0];
            // Check if the limitStartOffset is not in gap after end of token at limitStartIndex
            if (limitStartIndex < tokenList.tokenCountCurrent() &&
                indexAndTokenOffset[1] + tokenList.tokenOrEmbedding(limitStartIndex).token().length() <= limitStartOffset
            ) {
                limitStartIndex++;
            }
        }
    }

    public TokenList<T> delegate() {
        return tokenList;
    }
    
    public int limitStartOffset() {
        return limitStartOffset;
    }
    
    public int limitEndOffset() {
        return limitEndOffset;
    }
    
    @Override
    public TokenOrEmbedding<T> tokenOrEmbedding(int index) {
        index += limitStartIndex;
        return (index < limitEndIndex)
            ? tokenList.tokenOrEmbedding(index)
            : null;
    }

    @Override
    public int tokenOffset(int index) {
        index += limitStartIndex;
        if (index >= limitEndIndex)
            throw new IndexOutOfBoundsException("index=" + index + " >= limitEndIndex=" + limitEndIndex);
        return tokenList.tokenOffset(index);
    }

    @Override
    public int[] tokenIndex(int offset) {
        return LexerUtilsConstants.tokenIndexBinSearch(this, offset, tokenCountCurrent());
    }

    @Override
    public int tokenCount() {
        return tokenCountCurrent();
    }

    @Override
    public int tokenCountCurrent() {
        return limitEndIndex - limitStartIndex;
    }

    @Override
    public AbstractToken<T> replaceFlyToken(int index, AbstractToken<T> flyToken, int offset) {
        return tokenList.replaceFlyToken(index + limitStartIndex, flyToken, offset);
    }

    @Override
    public int modCount() {
        return tokenList.modCount();
    }

    @Override
    public Language<T> language() {
        return tokenList.language();
    }
    
    @Override
    public LanguagePath languagePath() {
        return tokenList.languagePath();
    }

    @Override
    public int tokenOffset(AbstractToken<T> token) {
        return tokenList.tokenOffset(token);
    }

    @Override
    public void setTokenOrEmbedding(int index, TokenOrEmbedding<T> t) {
        tokenList.setTokenOrEmbedding(limitStartIndex + index, t);
    }

    @Override
    public TokenList<?> rootTokenList() {
        return tokenList.rootTokenList();
    }

    @Override
    public CharSequence inputSourceText() {
        return rootTokenList().inputSourceText();
    }

    @Override
    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return tokenList.tokenHierarchyOperation();
    }
    
    @Override
    public InputAttributes inputAttributes() {
        return tokenList.inputAttributes();
    }

    @Override
    public int lookahead(int index) {
        // Can be used by LexerTestUtilities.lookahead()
        return tokenList.lookahead(index);
    }

    @Override
    public Object state(int index) {
        return tokenList.state(index);
    }

    @Override
    public boolean isContinuous() {
        return tokenList.isContinuous();
    }

    @Override
    public Set<T> skipTokenIds() {
        return tokenList.skipTokenIds();
    }
    
    @Override
    public int startOffset() {
        if (tokenCountCurrent() > 0 || tokenCount() > 0)
            return tokenOffset(0);
        return limitStartOffset;
    }

    @Override
    public int endOffset() {
        int cntM1 = tokenCount() - 1;
        if (cntM1 >= 0)
            return tokenOffset(cntM1) + tokenList.tokenOrEmbedding(cntM1).token().length();
        return limitStartOffset;
    }
    
    @Override
    public boolean isRemoved() {
        return tokenList.isRemoved();
    }

    @Override
    public StringBuilder dumpInfo(StringBuilder sb) {
        return sb;
    }

    @Override
    public String dumpInfoType() {
        return "SubSeqTL";
    }

}
