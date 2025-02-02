/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
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

    private final CharSequence inputSourceText;

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

    private boolean recognizedTokenJoined; // Whether recognized token will consist of parts

    private int skipTokenListCount;

    private int relexOffset;

    public JoinLexerInputOperation(JoinTokenList<T> joinTokenList, int relexJoinIndex, Object lexerRestartState,
            int activeTokenListIndex, int relexOffset
    ) {
        super(joinTokenList, relexJoinIndex, lexerRestartState);
        LOG.log(Level.FINE, "JoinLexerInputOperation(jtl, relexJoiIndex: {1}, lexerRestartState: {2}, activeTokenListIndex: {3}, releaxOffset: {4})", new Object[] {joinTokenList, relexJoinIndex, lexerRestartState, this.activeTokenListIndex, relexOffset});
        this.inputSourceText = joinTokenList.inputSourceText();
        this.activeTokenListIndex = activeTokenListIndex;
        this.relexOffset = relexOffset;
    }

    public final void init() {
        int tlReadOffset = 0;
        int lastEndOffset = 0;
        for (int i = 0; i < tokenListCount(); i++) {
            TokenList tl = tokenList(i);
            tlReadOffset += (tl.startOffset() - lastEndOffset);
            lastEndOffset = tl.endOffset();
            if(tl.endOffset() >= relexOffset) {
                break;
            }
        }
        this.readOffset = relexOffset - tlReadOffset;
        this.tokenStartOffset = this.readOffset;
        // Following code uses tokenList() method overriden in MutableJoinLexerInputOperation
        // so the following code would fail when placed in constructor since the constructor of MJLIO would not yet run.
        fetchActiveTokenList();
        // readOffset contains relex-offset. Skip empty parts (ETLs) to obtain
        // correct start offset of first lexed token
        readText = new TokenListText(activeTokenListIndex);
    }

    /**
     * Get active ETL into which the last produced token should be added.
     * For join tokens there is an ETL into which a last part of JT should be added.
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    private EmbeddedTokenList<?,T> activeTokenList() {
//        LOG.log(Level.FINE, "activeTokenList() => {0}", activeTokenList);
        return activeTokenList;
    }

    /**
     * Get index of active ETL into which the last produced token should be added.
     * For join tokens there is an index of the last ETL into which a last part of JT should be added.
     */
    public int activeTokenListIndex() {
        LOG.log(Level.FINE, "activeTokenListIndex() => {0}", activeTokenListIndex);
        return activeTokenListIndex;
    }

    public int skipTokenListCount() {
        LOG.log(Level.FINE, "skipTokenListCount() => {0}", skipTokenListCount);
        return skipTokenListCount;
    }

    public void clearSkipTokenListCount() {
        skipTokenListCount = 0;
        LOG.log(Level.FINE, "clearSkipTokenListCount()");
    }

    /**
     * True if the last returned token is last in {@link #activeTokenList()}.
     * For join tokens this applies to the last part of join token.
     */
    public boolean recognizedTokenLastInTokenList() {
        boolean result = toGlobalPosition(readOffset - tokenLength, true) == activeTokenList().endOffset();
        LOG.log(Level.FINE, "recognizedTokenLastInTokenList() => {0}", result);
        return result;
    }

    @Override
    public int lastTokenEndOffset() {
        int result = toGlobalPosition(super.lastTokenEndOffset(), true);
//        int result = toGlobalPosition(super.lastTokenEndOffset(), false);
        LOG.log(Level.FINE, "lastTokenEndOffset() => {0}", result);
        return result;
    }

    @Override
    public int read(int offset) { // index >= 0 is guaranteed by contract
        int result = readText.read(offset);
        LOG.log(Level.FINE, "read() => {1} ({2})", new Object[]{offset, result, (char) result});
        return result;
    }

    @Override
    public char readExisting(int offset) {
        if (readText.isInBounds(offset)) {
            char result = readText.inBoundsChar(offset);
            LOG.log(Level.FINE, "readExisting() => {1}", new Object[]{offset, result});
            return result;
        }
        if (readExistingText == null) {
            readExistingText = new TokenListText(readText);
        }
        char result = readExistingText.existingChar(offset);
        LOG.log(Level.FINE, "readExisting() => {1}", new Object[]{offset, result});
        return result;
    }

    @Override
    public void assignTokenLength(int tokenLength) {
        super.assignTokenLength(tokenLength);
        // Check whether activeTokenList needs to be changed due to various flags
        int pos = toGlobalPosition(readOffset - tokenLength, true);
        if (recognizedTokenLastInTokenList()) { // Advance to next token list
            // Since this is done when recognizing a next token it should be ok when recognizing
            // last token in the last ETL (it should not go beyond last ETL).
            do {
                // Cannot check activeTokenList.joinInfo == null since for token list updater
                // the lexing does not directly modify the ETLs.
                skipTokenListCount++;
                activeTokenListIndex++;
                fetchActiveTokenList();
            } while ((pos == activeTokenList().endOffset() || activeTokenList().startOffset() == activeTokenList().endOffset()) && (activeTokenListIndex + 1) < tokenListCount()); // Skip empty ETLs
        }
        // Joined token past ETL's boundary
        recognizedTokenJoined = toGlobalPosition(readOffset, true) > activeTokenList().endOffset();
        LOG.log(Level.FINE, "assignTokenLength(tokenLength: {0} / recognizedTokenJoined: {1} / skipTokenListCount: {2} / activeTokenListIndex: {3})", new Object[]{tokenLength, recognizedTokenJoined, skipTokenListCount, activeTokenListIndex});
    }

    private void fetchActiveTokenList() {
        activeTokenList = tokenList(activeTokenListIndex);
        activeTokenList.updateModCount();
    }

    public EmbeddedTokenList<?,T> tokenList(int tokenListIndex) { // Also used by JoinTokenListChange
        EmbeddedTokenList<?,T> result = ((JoinTokenList<T>) tokenList).tokenList(tokenListIndex);
//        LOG.log(Level.FINE, "tokenList({0}) => {1}", new Object[]{tokenListIndex, result});
        return result;
    }

    protected int tokenListCount() {
        int result = ((JoinTokenList<T>) tokenList).tokenListCount();
//        LOG.log(Level.FINE, "tokenListCount() => {0}", new Object[]{result});
        return result;
    }

    @Override
    protected void fillTokenData(AbstractToken<T> token) {
        if (!recognizedTokenJoined) {
            // Subtract tokenLength since this is already advanced to end of token
            token.setRawOffset(toGlobalPosition(readOffset, true) - tokenLength);
            LOG.log(Level.FINE, "fillTokenData#setRawOffset({0})", new Object[]{toGlobalPosition(readOffset, true) - tokenLength});
        }
        LOG.log(Level.FINE, "fillTokenData()", new Object[]{});
    }

    @Override
    protected boolean isFlyTokenAllowed() {
        boolean result = super.isFlyTokenAllowed() && !recognizedTokenJoined;
        LOG.log(Level.FINE, "isFlyTokenAllowed() => {0}", new Object[]{result});
        return result;
    }

    @Override
    protected AbstractToken<T> createDefaultTokenInstance(T id) {
        AbstractToken<T> result;
        if (recognizedTokenJoined) {
            result = createJoinToken(id, null, PartType.COMPLETE);
        } else { // Regular case
            result = super.createDefaultTokenInstance(id);
        }
        LOG.log(Level.FINE, "createDefaultTokenInstance({0})", new Object[]{id, result});
        return result;
    }

    @Override
    protected AbstractToken<T> createPropertyTokenInstance(T id,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        AbstractToken<T> result;
        if (recognizedTokenJoined) {
            result = createJoinToken(id, propertyProvider, partType);
        } else { // Regular case
            result = super.createPropertyTokenInstance(id, propertyProvider, partType);
        }
        LOG.log(Level.FINE, "createDefaultTokenInstance({0}, {1}, {2})", new Object[]{id, propertyProvider, partType, result});
        return result;
    }

    private AbstractToken<T> createJoinToken(T id,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        // Create join token
        // realTokenStartOffset is already advanced by tokenLength so first decrease it
        int start = readOffset - tokenLength;
        int globalStart = toGlobalPosition(start, false);
        int end = readOffset;
        int globalEnd = toGlobalPosition(end, true);
        WrapTokenId<T> wid = wrapTokenIdCache.plainWid(id);
        JoinToken<T> joinToken = new JoinToken<>(wid, tokenLength, propertyProvider, partType);
        @SuppressWarnings("unchecked")
        ArrayList<PartToken<T>> parts = new ArrayList<>(readText.getTokenListIndexIndex() - activeTokenListIndex + 1);
        int partIndex = 0;
        int partTextOffset = 0;
        int firstPartTokenListIndex = -1;
        int lastPartTokenListIndex = -1;
        for(int i = 0; ;i++) {
            activeTokenListIndex = i;
            fetchActiveTokenList();
            int etlStart = activeTokenList().startOffset();
            int etlEnd = activeTokenList().endOffset();
            if(etlEnd < globalStart || etlEnd == etlStart) {
                continue;
            }
            int partStart = Math.max(etlStart, globalStart);
            int partEnd = Math.min(etlEnd, globalEnd);
            PartType partPartType;
            if (parts.isEmpty()) {
                partPartType = PartType.START;
            } else if (etlEnd >= globalEnd) { // Last part
                partPartType = PartType.END;
            } else { // Non-last part
                partPartType = PartType.MIDDLE;
            }

            int partLength = partEnd - partStart;
            PartToken<T> partToken = new PartToken<>(wid, partEnd - partStart, propertyProvider, partPartType, joinToken, partIndex, partTextOffset);
            partIndex++;
            partToken.setRawOffset(partStart);
            parts.add(partToken);
            partTextOffset += partLength;
            lastPartTokenListIndex = i;
            if(firstPartTokenListIndex == -1) {
                firstPartTokenListIndex = i;
            }
            if(partPartType == PartType.END) {
                break;
            }
        }
        parts.trimToSize();
        joinToken.setJoinedParts(Collections.unmodifiableList(parts), lastPartTokenListIndex - firstPartTokenListIndex);
        activeTokenListIndex = lastPartTokenListIndex;
        fetchActiveTokenList();
        return joinToken;
    }

    private int toGlobalPosition(int positionInTokenList, boolean end) {
        int shift = 0;
        int lastEnd = 0;
        for (int i = 0; i < tokenListCount(); i++) {
            TokenList tl = tokenList(i);
            shift += tl.startOffset() - lastEnd;
            lastEnd = tl.endOffset();
            int shiftedPos = shift + positionInTokenList;
            if ((end ? (shiftedPos <= lastEnd) : shiftedPos < lastEnd) && relexOffset <= shiftedPos) {
                break;
            }
        }
        return shift + positionInTokenList;
    }

    /**
     * Class for reading of text of subsequent ETLs - it allows to see their text
     * as a consecutive character sequence (inputSourceText is used as a backing char sequence)
     * with an increasing readIndex (it's not decremented after token's recognition).
     */
    private final class TokenListText {

        private int tokenListIndex;

        private int tokenListStartOffset;

        private int tokenListEndOffset;

        /**
         * A constant added to readOffset to allow a smoothly increasing reading offset
         * when reading through multiple ETLs with gaps among them.
         * Its value is zero for the first ETL at relexOffset. When going to next TL it should be increased by
         * (tokenList(n+1).getStartOffset() - tokenList(n).getEndOffset()) and similarly
         * for backward move among token lists.
         */
        private int readOffsetShift;

        TokenListText(int tokenListIndex) {
            this.tokenListIndex = tokenListIndex;
            EmbeddedTokenList<?,T> etl = tokenList(tokenListIndex);
            etl.updateModCount();
            tokenListStartOffset = etl.startOffset();
            tokenListEndOffset = etl.endOffset();
            readOffsetShift = 0;
            int lastEndOffset = 0;
            for (int i = 0; i <= tokenListIndex; i++) {
                TokenList tl = tokenList(i);
                readOffsetShift += (tl.startOffset() - lastEndOffset);
                lastEndOffset = tl.endOffset();
            }
        }

        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        TokenListText(TokenListText text) {
            this.tokenListIndex = text.tokenListIndex;
            this.tokenListStartOffset = text.tokenListStartOffset;
            this.tokenListEndOffset = text.tokenListEndOffset;
            this.readOffsetShift = text.readOffsetShift;
        }

        /**
         * Read next char or return EOF.
         */
        @SuppressWarnings("AssignmentToMethodParameter")
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
        @SuppressWarnings("AssignmentToMethodParameter")
        boolean isInBounds(int offset) {
            offset += readOffsetShift;
            return offset >= tokenListStartOffset && offset < tokenListEndOffset;
        }

        /**
         * Get char that was previously verified to be within bounds.
         */
        @SuppressWarnings("AssignmentToMethodParameter")
        char inBoundsChar(int offset) {
            offset += readOffsetShift;
            return inputSourceText.charAt(offset);
        }

        @SuppressWarnings("AssignmentToMethodParameter")
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

        public int getTokenListIndexIndex() {
            return tokenListIndex;
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
        return super.toString() + ", activeTokenListIndex=" + activeTokenListIndex; // NOI18N
    }

}
