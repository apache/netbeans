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

import java.util.List;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.lib.lexer.token.PartToken;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Lexer input operation over multiple joined sections (embedded token lists).
 * <br/>
 * It produces regular tokens (to be added directly into ETL represented by
 * {@link #activeTokenList()} and also special {@link #JoinToken} instances
 * in case a token spans boundaries of multiple ETLs.
 * <br/>
 * It can either work over JoinTokenList directly or, during a modification,
 * it simulates that certain token lists are already removed/added to underlying token list.
 * <br/>
 * 
 * {@link #recognizedTokenLastInTokenList()} gives information whether the lastly
 * produced token ends right at boundary of the activeTokenList.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JoinLexerInputOperation<T extends TokenId> extends LexerInputOperation<T> {
    
    CharSequence inputSourceText;

    private TokenListText readText; // For servicing read()
    
    private TokenListText readExistingText;
    
    /**
     * Token list in which the last recognized token started.
     */
    private EmbeddedTokenList<?,T> activeTokenList;
    
    /**
     * Index of activeTokenList in JTL.
     */
    private int activeTokenListIndex;
    
    /**
     * End offset of the active token list.
     */
    private int activeTokenListEndOffset;
    
    /**
     * Real token's start offset used to derive the token's offset in ETL.
     * Since tokenStartOffset is affected by TokenListList.readOffsetShift
     * it cannot be used for this purpose.
     */
    private int realTokenStartOffset;
    
    private boolean recognizedTokenJoined; // Whether recognized token will consist of parts
    
    private int skipTokenListCount;
    

    public JoinLexerInputOperation(JoinTokenList<T> joinTokenList, int relexJoinIndex, Object lexerRestartState,
            int activeTokenListIndex, int relexOffset
    ) {
        super(joinTokenList, relexJoinIndex, lexerRestartState);
        this.inputSourceText = joinTokenList.inputSourceText();
        this.activeTokenListIndex = activeTokenListIndex;
        tokenStartOffset = relexOffset;
        readOffset = relexOffset;
    }

    public final void init() {
        // Following code uses tokenList() method overriden in MutableJoinLexerInputOperation
        // so the following code would fail when placed in constructor since the constructor of MJLIO would not yet run.
        fetchActiveTokenList();
        // readOffset contains relex-offset. Skip empty parts (ETLs) to obtain
        // correct start offset of first lexed token
        readText = new TokenListText(activeTokenListIndex);

        // Assign realTokenStartOffset after fetchActiveTokenList() since it would overwrite it
        realTokenStartOffset = readOffset;
    }

    /**
     * Get active ETL into which the last produced token should be added.
     * For join tokens there is an ETL into which a last part of JT should be added.
     */
    public EmbeddedTokenList<?,T> activeTokenList() {
        return activeTokenList;
    }
    
    /**
     * Get index of active ETL into which the last produced token should be added.
     * For join tokens there is an index of the last ETL into which a last part of JT should be added.
     */
    public int activeTokenListIndex() {
        return activeTokenListIndex;
    }

    public int skipTokenListCount() {
        return skipTokenListCount;
    }

    public void clearSkipTokenListCount() {
        skipTokenListCount = 0;
    }

    /**
     * True if the last returned token is last in {@link #activeTokenList()}.
     * For join tokens this applies to the last part of join token.
     */
    public boolean recognizedTokenLastInTokenList() {
        // realTokenStartOffset is set to the end of last recognized token
        return (realTokenStartOffset == activeTokenListEndOffset);
    }

    @Override
    public int lastTokenEndOffset() {
        return realTokenStartOffset;
    }

    @Override
    public int read(int offset) { // index >= 0 is guaranteed by contract
        return readText.read(offset);
    }

    @Override
    public char readExisting(int offset) {
        if (readText.isInBounds(offset)) {
            return readText.inBoundsChar(offset);
        }
        if (readExistingText == null) {
            readExistingText = new TokenListText(readText);
        }
        return readExistingText.existingChar(offset);
    }

    @Override
    public void assignTokenLength(int tokenLength) {
        super.assignTokenLength(tokenLength);
        // Check whether activeTokenList needs to be changed due to various flags
        if (recognizedTokenLastInTokenList()) { // Advance to next token list
            // Since this is done when recognizing a next token it should be ok when recognizing
            // last token in the last ETL (it should not go beyond last ETL).
            do {
                // Cannot check activeTokenList.joinInfo == null since for token list updater
                // the lexing does not directly modify the ETLs.
                skipTokenListCount++;
                activeTokenListIndex++;
                fetchActiveTokenList();
            } while (realTokenStartOffset == activeTokenListEndOffset); // Skip empty ETLs
        }
        // Advance to end of currently recognized token
        realTokenStartOffset += tokenLength;
        // Joined token past ETL's boundary
        recognizedTokenJoined = (realTokenStartOffset > activeTokenListEndOffset);
    }
    
    private void fetchActiveTokenList() {
        activeTokenList = tokenList(activeTokenListIndex);
        activeTokenList.updateModCount();
        realTokenStartOffset = activeTokenList.startOffset();
        activeTokenListEndOffset = activeTokenList.endOffset();
    }
    
    public EmbeddedTokenList<?,T> tokenList(int tokenListIndex) { // Also used by JoinTokenListChange
        return ((JoinTokenList<T>) tokenList).tokenList(tokenListIndex);
    }

    protected int tokenListCount() {
        return ((JoinTokenList<T>) tokenList).tokenListCount();
    }

    protected void fillTokenData(AbstractToken<T> token) {
        if (!recognizedTokenJoined) {
            // Subtract tokenLength since this is already advanced to end of token
            token.setRawOffset(realTokenStartOffset - tokenLength);
        }
    }
    
    @Override
    protected boolean isFlyTokenAllowed() {
        return super.isFlyTokenAllowed() && !recognizedTokenJoined;
    }
    
    @Override
    protected AbstractToken<T> createDefaultTokenInstance(T id) {
        if (recognizedTokenJoined) {
            return createJoinToken(id, null, PartType.COMPLETE);
        } else { // Regular case
            return super.createDefaultTokenInstance(id);
        }
    }

    @Override
    protected AbstractToken<T> createPropertyTokenInstance(T id,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        if (recognizedTokenJoined) {
            return createJoinToken(id, propertyProvider, partType);
        } else { // Regular case
            return super.createPropertyTokenInstance(id, propertyProvider, partType);
        }
    }
    
    private AbstractToken<T> createJoinToken(T id,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        // Create join token
        // realTokenStartOffset is already advanced by tokenLength so first decrease it
        realTokenStartOffset -= tokenLength;
        WrapTokenId<T> wid = wrapTokenIdCache.plainWid(id);
        JoinToken<T> joinToken = new JoinToken<T>(wid, tokenLength, propertyProvider, partType);
        int joinPartCountEstimate = readText.tokenListIndex - activeTokenListIndex + 1;
        @SuppressWarnings("unchecked")
        PartToken<T>[] parts = new PartToken[joinPartCountEstimate];
        int partLength = activeTokenListEndOffset - realTokenStartOffset;
        PartToken<T> partToken = new PartToken<T>(wid, partLength, propertyProvider, PartType.START, joinToken, 0, 0);
        partToken.setRawOffset(realTokenStartOffset); // realTokenStartOffset already decreased by tokenLength
        parts[0] = partToken;
        int partIndex = 1;
        int partTextOffset = partLength; // Length of created parts so far
        int firstPartTokenListIndex = activeTokenListIndex;
        do {
            activeTokenListIndex++;
            fetchActiveTokenList();
            // realTokenStartOffset set to start activeTokenList
            PartType partPartType;
            // Attempt total ETL's length as partLength
            partLength = activeTokenListEndOffset - realTokenStartOffset;
            if (partLength == 0) {
                continue;
            }
            if (partTextOffset + partLength >= tokenLength) { // Last part
                partLength = tokenLength - partTextOffset;
                // If the partType of the join token is not complete then this will be PartType.MIDDLE
                partPartType = (partType == PartType.START) ? PartType.MIDDLE : PartType.END;
            } else { // Non-last part
                partPartType = PartType.MIDDLE;
            }

            partToken = new PartToken<T>(wid, partLength, propertyProvider, partPartType, joinToken, partIndex, partTextOffset);
            // realTokenStartOffset still points to start of activeTokenList
            partToken.setRawOffset(realTokenStartOffset); // ETL.startOffset() will be subtracted upon addition to ETL
            partTextOffset += partLength;
            parts[partIndex++] = partToken;
        } while (partTextOffset < tokenLength);
        // Update realTokenStartOffset which pointed to start of activeTokenList
        realTokenStartOffset += partLength;
        // Check that the array does not have any extra items
        if (partIndex < parts.length) {
            @SuppressWarnings("unchecked")
            PartToken<T>[] tmp = new PartToken[partIndex];
            System.arraycopy(parts, 0, tmp, 0, partIndex);
            parts = tmp;
        }
        List<PartToken<T>> partList = ArrayUtilities.unmodifiableList(parts);
        joinToken.setJoinedParts(partList, activeTokenListIndex - firstPartTokenListIndex);
        // joinToken.setTokenList() makes no sense - JoinTokenList instances are temporary
        // joinToken.setRawOffset() makes no sense - offset taken from initial part
        return joinToken;
    }
    
    /**
     * Class for reading of text of subsequent ETLs - it allows to see their text
     * as a consecutive character sequence (inputSourceText is used as a backing char sequence)
     * with an increasing readIndex (it's not decremented after token's recognition).
     */
    final class TokenListText {

        int tokenListIndex;

        int tokenListStartOffset;

        int tokenListEndOffset;

        /**
         * A constant added to readOffset to allow a smoothly increasing reading offset
         * when reading through multiple ETLs with gaps among them.
         * Its value is zero for the first ETL at relexOffset. When going to next TL it should be increased by
         * (tokenList(n+1).getStartOffset() - tokenList(n).getEndOffset()) and similarly
         * for backward move among token lists.
         */
        int readOffsetShift;

        TokenListText(int tokenListIndex) {
            this.tokenListIndex = tokenListIndex;
            EmbeddedTokenList<?,T> etl = tokenList(tokenListIndex);
            etl.updateModCount();
            tokenListStartOffset = etl.startOffset();
            tokenListEndOffset = etl.endOffset();
            readOffsetShift = 0;
        }

        TokenListText(TokenListText text) {
            this.tokenListIndex = text.tokenListIndex;
            this.tokenListStartOffset = text.tokenListStartOffset;
            this.tokenListEndOffset = text.tokenListEndOffset;
            this.readOffsetShift = text.readOffsetShift;
        }

        /**
         * Read next char or return EOF.
         */
        int read(int offset) {
            offset += readOffsetShift;
            if (offset < tokenListEndOffset) {
                if (offset >= tokenListStartOffset) {
                    return inputSourceText.charAt(offset);
                } else { // Goto previous
                    while (true) { // Char should exist
                        offset -= movePreviousTokenList();
                        if (offset >= tokenListStartOffset) { // ETL might be empty
                            return inputSourceText.charAt(offset);
                        }
                    }
                }
            } else { // offset >= tokenListEndOffset
                while (tokenListIndex + 1 < tokenListCount()) {
                    offset += moveNextTokenList();
                    if (offset < tokenListEndOffset) { // ETL might be empty
                        return inputSourceText.charAt(offset);
                    }
                }
                return LexerInput.EOF;
            }
        }

        /**
         * Check whether currently set text covers the given relative index.
         * 
         * @param index index in the same metrics as readIndex.
         * @return whether the given index is within current bounds.
         */
        boolean isInBounds(int offset) {
            offset += readOffsetShift;
            return offset >= tokenListStartOffset && offset < tokenListEndOffset;
        }
        
        /**
         * Get char that was previously verified to be within bounds.
         */
        char inBoundsChar(int offset) {
            offset += readOffsetShift;
            return inputSourceText.charAt(offset);
        }
        
        char existingChar(int offset) {
            offset += readOffsetShift;
            if (offset < tokenListStartOffset) {
                while (true) { // Char should exist
                    offset -= movePreviousTokenList();
                    if (offset >= tokenListStartOffset) { // ETL might be empty
                        return inputSourceText.charAt(offset);
                    }
                }
                
            } else if (offset >= tokenListEndOffset) {
                while (true) { // Char should exist
                    offset += moveNextTokenList();
                    if (offset < tokenListEndOffset) { // ETL might be empty
                        return inputSourceText.charAt(offset);
                    }
                }
                
            }
            // Index within current bounds
            return inputSourceText.charAt(offset);
        }
        
        private int movePreviousTokenList() {
            tokenListIndex--;
            EmbeddedTokenList etl = tokenList(tokenListIndex);
            etl.updateModCount();
            tokenListEndOffset = etl.endOffset();
            // Decrease offset shift by the size of gap between ETLs
            int shift = tokenListStartOffset - tokenListEndOffset;
            readOffsetShift -= shift;
            // Also shift given offset value
            tokenListStartOffset = etl.startOffset();
            return shift;
        }
        
        private int moveNextTokenList() {
            tokenListIndex++;
            EmbeddedTokenList etl = tokenList(tokenListIndex);
            etl.updateModCount();
            tokenListStartOffset = etl.startOffset();
            // Increase offset shift by the size of gap between ETLs
            int shift = tokenListStartOffset - tokenListEndOffset;
            readOffsetShift += shift;
            // Also shift given offset value
            tokenListEndOffset = etl.endOffset();
            return shift;
        }

        @Override
        public String toString() {
            return "tlInd=" + tokenListIndex + ", <" + tokenListStartOffset + "," + // NOI18N
                    tokenListEndOffset + ">"; // NOI18N
        }

    }
    
    @Override
    public String toString() {
        return super.toString() + ", realTokenStartOffset=" + realTokenStartOffset + // NOI18N
                ", activeTokenListIndex=" + activeTokenListIndex + // NOI18N
                ", activeTokenListEndOffset=" + activeTokenListEndOffset; // NOI18N
    }

}
