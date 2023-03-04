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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.inc.MutableTokenList;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.lib.lexer.inc.JoinTokenListChange;
import org.netbeans.lib.lexer.inc.TokenListChange;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.lib.lexer.token.PartToken;


/**
 * Join token list over certain range of ETLs of a TokenListList.
 * <br/>
 * It does not have any physical storage for its tokens. Regular tokens
 * are stored in individual ETLs. Tokens split across multiple ETLs
 * are represented as PartToken in each ETL referencing a JoinToken.
 * <br/>
 * The only "countable" part is a last part of a JoinToken. Each ETL holds
 * EmbeddedJoinInfo instance with 
 * <br/>
 * Lookaheads and states are assigned to a last part of the JoinToken
 * and it's stored normally in ETL like for regular tokens.
 * 
 * @author Miloslav Metelka
 */

public final class JoinTokenList<T extends TokenId> implements MutableTokenList<T> {
    
    // -J-Dorg.netbeans.lib.lexer.JoinTokenList.level=FINE
    private static final Logger LOG = Logger.getLogger(JoinTokenList.class.getName());
    
    private static final int INDEX_GAP_LENGTH_INITIAL_SIZE = (Integer.MAX_VALUE >> 1);
    
    /**
     * Create join token list over an uninitialized set of embedded token lists
     * and this method will perform initial lexing of the contained embedded token lists.
     * 
     * @param tokenListList non-null tokenListList
     * @param tokenListStartIndex index of first ETL contained in the desired JTL.
     * @param tokenListCount total number of ETLs contained in the created JTL.
     * @return non-null JTL.
     */
    static <T extends TokenId> JoinTokenList<T> create(TokenListList<T> tokenListList) {
        JoinTokenList<T> jtl = new JoinTokenList<T>(tokenListList);
        jtl.init();
        return jtl;
    }

    /** Backing token list list that holds ETLs. */
    protected final TokenListList<T> tokenListList; // 8=super + 4 = 12 bytes
    
    /**
     * Number of embedded token lists contained in join token list.
     * This may temporarily differ from number of token lists in TokenListList.
     */
    private int tokenListCount; // 16 bytes
    
    /**
     * Total count of tokens contained in JoinTokenList.
     */
    private int joinTokenCount; // 20 bytes
    
    /**
     * Index among contained embedded token lists where both index-related gaps are located.
     */
    private int indexGapsIndex; // 24 bytes
    
    /**
     * Length of an index gap for computation of indexes in a JoinTokenList
     * based on ETLs.
     * <br/>
     * The above gap checking is done by checking whether the index is above gap length
     * since the initial gap length is so high that the indexes should never reach
     * its size (even decreased by added items).
     */
    private int joinTokenIndexGapLength = INDEX_GAP_LENGTH_INITIAL_SIZE; // 28 bytes
    
    /**
     * Length of an index gap for computation of index of ETL in a JoinTokenList
     * which is useful for finding of a start-token-list-index of the join token list.
     * <br/>
     * The above gap checking is done by checking whether the index is above gap length
     * since the initial gap length is so high that the indexes should never reach
     * its size (even decreased by added items).
     */
    private int tokenListIndexGapLength = INDEX_GAP_LENGTH_INITIAL_SIZE; // 32 bytes

    /**
     * Index of active token list - the one used for operations like tokenOrEmbedding() etc.
     * The index may have value from zero to (tokenListCount - 1).
     * Special value -1 means that activeTokenListIndex was not set yet (or was reset
     * after update).
     */
    protected int activeTokenListIndex; // 36 bytes

    /** Token list currently servicing requests. */
    protected EmbeddedTokenList<?,T> activeTokenList; // 28 bytes
    
    /** Index of a first token of activeTokenList in join index metrics
     * (JoinTokenList's token index).
     */
    protected int activeStartJoinIndex; // 40 bytes
    
    /** End index of activeTokenList in join index metrics
     * (JoinTokenList's token index).
     */
    protected int activeEndJoinIndex; // 44 bytes

    private int extraModCount; // 48 bytes
    
    private JoinTokenList(TokenListList<T> tokenListList) {
        this.tokenListList = tokenListList;
        this.tokenListCount = tokenListList.size();
        resetActiveTokenList(); // Use -1 for activeTokenListIndex
    }

    @Override
    public Language<T> language() {
        return LexerUtilsConstants.innerLanguage(languagePath());
    }

    @Override
    public LanguagePath languagePath() {
        return tokenListList.languagePath();
    }

    public TokenListList<T> tokenListList() {
        return tokenListList;
    }

    /**
     * Get token list contained in this join token list.
     * 
     * @param index >=0 index of the token list in this joined token list.
     * @return non-null embedded token list at the given index.
     */
    public EmbeddedTokenList<?,T> tokenList(int index) {
        if (index < 0)
            throw new IndexOutOfBoundsException("index=" + index + " < 0"); // NOI18N
        if (index >= tokenListCount)
            throw new IndexOutOfBoundsException("index=" + index + " >= size()=" + tokenListCount); // NOI18N
        return tokenListList.get(index);
    }
    
    public int tokenListCount() {
        return tokenListCount;
    }
    
    @Override
    public int tokenCountCurrent() {
        return joinTokenCount;
    }

    @Override
    public int tokenCount() {
        return tokenCountCurrent();
    }
    
    public int activeStartJoinIndex() { // Use by TS.embeddedImpl()
        return activeStartJoinIndex;
    }

    public int activeEndJoinIndex() { // Use by TokenListUpdater
        return activeEndJoinIndex;
    }

    public int activeTokenListIndex() {
        return activeTokenListIndex;
    }

    public void setActiveTokenListIndex(int activeTokenListIndex) { // Used by ETL.joinTokenList()
        if (this.activeTokenListIndex != activeTokenListIndex) {
            forceActiveTokenListIndex(activeTokenListIndex);
        }
    }
    
    private void forceActiveTokenListIndex(int activeTokenListIndex) {
        this.activeTokenListIndex = activeTokenListIndex;
        fetchActiveTokenListData();
    }

    static boolean tested = false;
    private void fetchActiveTokenListData() {
        if (activeTokenListIndex >= tokenListCount) {
            if (!tested) {
                tested = true;
                StringBuilder sb = new StringBuilder(200);
                dumpInfo(sb);
                throw new IllegalStateException("Bad index activeTokenListIndex=" + activeTokenListIndex + "; " + sb);
            }
            return;
        }
        activeTokenList = tokenList(activeTokenListIndex);
        activeTokenList.updateModCount(rootTokenList().modCount());
        activeStartJoinIndex = activeTokenList.joinInfo().joinTokenIndex();
        activeEndJoinIndex = activeStartJoinIndex + activeTokenList.joinTokenCount();
    }

    void resetActiveTokenList() {
        activeTokenListIndex = -1;
        activeTokenList = null;
        activeStartJoinIndex = activeEndJoinIndex = 0;
    }
    
    public EmbeddedTokenList<?,T> activeTokenList() {
        return activeTokenList;
    }

    @Override
    public TokenOrEmbedding<T> tokenOrEmbedding(int index) {
        if (index >= joinTokenCount) {
            return null;
        }
        findTokenListByJoinIndex(index); // Checks for index < 0
        TokenOrEmbedding<T> tokenOrEmbedding = activeTokenList.tokenOrEmbedding(index - activeStartJoinIndex);
        AbstractToken<T> token;
        if (index == activeStartJoinIndex && // Would already be positioned on last part which would be first in ETL
            (token = tokenOrEmbedding.token()).getClass() == PartToken.class)
        {
            tokenOrEmbedding = ((PartToken<T>) token).joinTokenOrEmbedding();
        }
        return tokenOrEmbedding;
    }

    @Override
    public int tokenOffset(AbstractToken<T> token) {
        // Should never be called for any token instances
        throw new IllegalStateException("Internal error - should never be called");
    }

    @Override
    public int tokenOffset(int index) {
        findTokenListByJoinIndex(index);
        // Need to treat specially token parts - return offset of complete token
        AbstractToken<T> token;
        if (index == activeStartJoinIndex && // token part can only be the first in ETL
            (token = activeTokenList.tokenOrEmbedding(index - activeStartJoinIndex).token()).getClass() == PartToken.class
        ) {
            return ((PartToken<T>)token).joinToken().offset(null);
        }
        return activeTokenList.tokenOffset(index - activeStartJoinIndex);
    }

    public int tokenListIndex(int offset, int startIndex, int endIndex) {
        // First find the right ETL for the given offset and store it in activeTokenListIndex
        // Use binary search
        int low = startIndex;
        int high = endIndex - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midStartOffset = tokenList(mid).startOffset();
            
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // Token starting exactly at ETL.startOffset()
                high = mid;
                break;
            }
        }
        // Use lower index => high
        return high; // May return -1
    }

    @Override
    public int[] tokenIndex(int offset) {
        // Check if the current active token list covers the given offset.
        // If not covered then only search below/above the current active ETL.
        // It not only improves performance but it is NECESSARY for proper functionality
        // of TokenListUpdater.updateJoined() since it may skip removed ETLs
        // by manually using setActiveTokenListIndex() in the area below/above the removed ETLs.
        boolean activeStartsBelowOffset = ((offset >= activeTokenList.startOffset()) || activeTokenListIndex == 0);
        if (activeStartsBelowOffset) {
            if (offset < activeTokenList.endOffset() ||
                (activeTokenListIndex + 1 == tokenListCount() ||
                    offset < tokenList(activeTokenListIndex + 1).startOffset())
            ) {
                // Current active ETL covers the area
            } else if (activeTokenListIndex + 1 < tokenListCount()) { // Search above
                activeTokenListIndex = tokenListIndex(offset, activeTokenListIndex + 1, tokenListCount);
                if (activeTokenListIndex == -1 && tokenListCount > 0) {
                    activeTokenListIndex = 0;
                }
                fetchActiveTokenListData();
            }
        } else if (activeTokenListIndex > 0) { // Search below
            activeTokenListIndex = tokenListIndex(offset, 0, activeTokenListIndex);
            if (activeTokenListIndex == -1 && tokenListCount > 0) {
                activeTokenListIndex = 0;
            }
            fetchActiveTokenListData();
        }

        // Now search within a single ETL by binary search
        EmbeddedJoinInfo joinInfo = activeTokenList.joinInfo();
        int joinTokenLastPartShift = joinInfo.joinTokenLastPartShift();
        int searchETLTokenCount = activeTokenList.joinTokenCount();
        int[] indexAndTokenOffset = LexerUtilsConstants.tokenIndexBinSearch(activeTokenList, offset, searchETLTokenCount);
        int etlIndex = indexAndTokenOffset[0]; // Index inside etl
        indexAndTokenOffset[0] += joinInfo.joinTokenIndex(); // Make the index joinIndex
        if (etlIndex == searchETLTokenCount && joinTokenLastPartShift > 0) { // Must move activeTokenList to last part
            // Get last part and find out how much forward is the last part
            activeTokenListIndex += joinTokenLastPartShift;
            fetchActiveTokenListData();
            PartToken<T> lastPartToken = (PartToken<T>) activeTokenList.tokenOrEmbeddingDirect(0).token();
            indexAndTokenOffset[1] = lastPartToken.joinToken().offset(null);
            
        } else if (etlIndex == 0) { // Possibly last part of a join token
            AbstractToken<T> token = activeTokenList.tokenOrEmbedding(0).token();
            if (token.getClass() == PartToken.class) {
                // indexAndTokenOffset[0] is already ok - just fix token's offset
                indexAndTokenOffset[1] = ((PartToken<T>)token).joinToken().offset(null);
            }
        }
        return indexAndTokenOffset;
    }

    @Override
    public AbstractToken<T> replaceFlyToken(int index, AbstractToken<T> flyToken, int offset) {
        findTokenListByJoinIndex(index);
        return activeTokenList.replaceFlyToken(index - activeStartJoinIndex, flyToken, offset);
    }

    @Override
    public void setTokenOrEmbedding(int index, TokenOrEmbedding<T> t) {
        findTokenListByJoinIndex(index);
        // !!! TBD - must not wrap complete tokens of join token list.
        // Instead wrap all part tokens with another join token list
        activeTokenList.setTokenOrEmbedding(index - activeStartJoinIndex, t);
    }

    @Override
    public final int modCount() {
        return rootTokenList().modCount() + extraModCount;
    }

    @Override
    public InputAttributes inputAttributes() {
        return rootTokenList().inputAttributes();
    }
    
    @Override
    public int lookahead(int index) {
        // Locate embedded token list for the last token part (only that one stores the LA)
        findTokenListByJoinIndex(index);
        return activeTokenList.lookahead(index - activeStartJoinIndex);
    }

    @Override
    public Object state(int index) {
        // Locate embedded token list for the last token part (only that one stores the state)
        findTokenListByJoinIndex(index);
        return activeTokenList.state(index - activeStartJoinIndex);
    }

    @Override
    public final TokenList<?> rootTokenList() {
        return tokenListList.rootTokenList();
    }

    @Override
    public CharSequence inputSourceText() {
        return rootTokenList().inputSourceText();
    }

    @Override
    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return rootTokenList().tokenHierarchyOperation();
    }
    
    @Override
    public boolean isContinuous() {
        return false; // TBD can be partially continuous - could be improved
    }

    @Override
    public Set<T> skipTokenIds() {
        return null; // Not a top-level list -> no skip token ids
    }

    @Override
    public int startOffset() {
        if (tokenListCount == 0) { // No tokens
            return 0;
        }
        if (activeTokenListIndex == 0) {
            // Status already up-to-date
            return activeTokenList.startOffset();
        } else {
            EmbeddedTokenList<?,T> firstEtl = tokenList(0);
            firstEtl.updateModCount();
            return firstEtl.startOffset();
        }
    }

    @Override
    public int endOffset() {
        int tokenListCountM1 = tokenListCount() - 1;
        if (tokenListCountM1 < 0) { // No token lists contained
            return 0;
        }
        if (activeTokenListIndex == tokenListCountM1) {
            // Status already up-to-date
            return activeTokenList.endOffset();
        } else {
            EmbeddedTokenList<?,T> lastEtl = tokenList(tokenListCountM1);
            lastEtl.updateModCount();
            return lastEtl.endOffset();
        }
    }
    
    @Override
    public boolean isRemoved() {
        return false; // Should never be removed
    }

    /**
     * Get index of ETL where a token for a particular join index starts
     * and index where it's located.
     *
     * @param index join index in JTL lower than tokenCountCurrent().
     * @return local index in activeTokenList of first part of a join token.
     */
    public int tokenStartLocalIndex(int index) {
        findTokenListByJoinIndex(index);
        AbstractToken<T> token = activeTokenList.tokenOrEmbeddingDirect(index - activeStartJoinIndex).token();
        if (token.getClass() == PartToken.class) { // Last part of join token
            PartToken<T> partToken = (PartToken<T>) token;
            activeTokenListIndex -= partToken.joinToken().extraTokenListSpanCount();
            fetchActiveTokenListData();
            // The first part of join token is last in the active ETL
            return activeTokenList.tokenCountCurrent() - 1;
        }
        return index - activeStartJoinIndex;
    }

    /**
     * Locate the right activeTokenList to service the requested join index.
     *
     * @param joinIndex index in a join token list.
     * @throws IndexOutOfBoundsException for joinIndex below zero.
     */
    protected final void findTokenListByJoinIndex(int joinIndex) {
        if (joinIndex < activeStartJoinIndex) { // For activeTokenList == -1 the activeStartJoinIndex == 0
            if (joinIndex < 0) {
                throw new IndexOutOfBoundsException("index=" + joinIndex + " < 0");
            }
            // Must be lower segment - first try the one below
            activeTokenListIndex--;
            fetchActiveTokenListData();
            if (joinIndex < activeStartJoinIndex) { // Still not covered
                // Do binary search on <0, activeTokenListIndex - 1>
                findTokenListBinSearch(joinIndex, 0, activeTokenListIndex - 1);
            }

        } else if (joinIndex == activeEndJoinIndex) { // Right at end of active ETL
            int lps = (activeTokenList != null)
                    ? activeTokenList.joinInfo().joinTokenLastPartShift()
                    : 0;
            if (lps > 0) { // join token
                activeTokenListIndex += lps;
                fetchActiveTokenListData(); // Should be non-empty (contains last jointoken part)
            } else { // Move to next ETL
                activeTokenListIndex++;
                fetchActiveTokenListData();
                // Use first token but check for empty ETL and join token
                adjustJoinedOrSkipEmptyUp(joinIndex);
            }
            
        } else if (joinIndex > activeEndJoinIndex) { // joinIndex > activeEndJoinIndex
            activeTokenListIndex++;
            fetchActiveTokenListData();
            if (joinIndex >= activeEndJoinIndex) { // Still too high
                // Do binary search on <activeTokenListIndex + 1, tokenListCount-1>
                findTokenListBinSearch(joinIndex, activeTokenListIndex + 1, tokenListCount - 1);
            }
        } // else: the index is within bounds of activeTokenList
    }

    private void findTokenListBinSearch(int joinIndex, int low, int high) {
        while (low <= high) {
            activeTokenListIndex = (low + high) >>> 1;
            fetchActiveTokenListData();
            if (activeStartJoinIndex < joinIndex) {
                low = activeTokenListIndex + 1;
            } else if (activeStartJoinIndex > joinIndex) {
                high = activeTokenListIndex - 1;
            } else { // joinIndex == activeStartJoinIndex
                // The current list could be empty or possibly a part of a join token
                //   so adjust to join token's end or skip to a next non-empty ETL.
                adjustJoinedOrSkipEmptyUp(joinIndex);
                return;
            }
        }
        // low == high + 1
        // Use ETL at lower index i.e. "high". That ETL will hold the joinIndex "inside"
        // i.e. it will not be empty (because otherwise the bin-search would naturally
        // relocate to the ETL that starts exactly with joinIndex so it would be catched
        // with an exact match clause above and returned earlier).
        // For the same reason it's not necessary to check whether the token at joinIndex
        // is not a last in the ETL and check for join token possibility.
        if (activeTokenListIndex != high) {
            activeTokenListIndex = high;
            fetchActiveTokenListData();
        }
    }

    private void adjustJoinedOrSkipEmptyUp(int joinIndex) {
        while (joinIndex == activeEndJoinIndex) {
            if (activeTokenList.tokenCountCurrent() == 0) {
                activeTokenListIndex++;
                fetchActiveTokenListData();
            } else { // Non-empty ETL and joinIndex points at end => check for join token
                int lps = activeTokenList.joinInfo().joinTokenLastPartShift();
                if (lps > 0) { // join token
                    activeTokenListIndex += lps;
                    fetchActiveTokenListData();
                }
            }
        }
    }

    @Override
    public TokenOrEmbedding<T> tokenOrEmbeddingDirect(int index) {
        return tokenOrEmbedding(index);
    }

    @Override
    public boolean isFullyLexed() {
        return true;
    }

    @Override
    public void replaceTokens(TokenListChange<T> change, TokenHierarchyEventInfo eventInfo, boolean modInside) {
        markModified();
        ((JoinTokenListChange<T>) change).replaceTokens(eventInfo);
    }
    
    private void markModified() {
        extraModCount++;
    }

    @Override
    public LexerInputOperation<T> createLexerInputOperation(int tokenIndex, int relexOffset, Object relexState) {
        // Should never be called
        throw new IllegalStateException("Should never be called"); // NOI18N
    }

    public void setPrevActiveTokenListIndex() {
        activeTokenListIndex--;
        fetchActiveTokenListData();
    }
    
    public void resetActiveAfterUpdate() { // Update the active token list after updating
        if (activeTokenListIndex != -1) {
            if (tokenListCount == 0) {
                resetActiveTokenList();
            } else {
                activeTokenListIndex = Math.min(activeTokenListIndex, tokenListCount - 1);
                fetchActiveTokenListData(); // Force re-fetching since updating might change the vars
            }
        }
    }

    int joinTokenIndex(int rawJoinTokenIndex) {
        return (rawJoinTokenIndex < joinTokenIndexGapLength) // Gap big enough for this type of testing
                ? rawJoinTokenIndex
                : rawJoinTokenIndex - joinTokenIndexGapLength;
    }
    
    int tokenListIndex(int rawTokenListIndex) {
        return (rawTokenListIndex < tokenListIndexGapLength) // Gap big enough for this type of testing
                ? rawTokenListIndex
                : rawTokenListIndex - tokenListIndexGapLength;
    }
    
    /**
     * Move both gaps in sync so that ETL.JoinInfo in an ETL at "index" is above both gaps.
     * 
     * @param tokenListList non-null TLL.
     * @param index index to which the gaps should be moved.
     */
    
    public void moveIndexGap(int index) {
        if (index < indexGapsIndex) {
            // Items above index should be moved to be above gap
            int i = index;
            do {
                EmbeddedJoinInfo joinInfo = tokenListList.get(i++).joinInfo();
                joinInfo.setRawTokenListIndex(joinInfo.getRawTokenListIndex() + tokenListIndexGapLength);
                joinInfo.setRawJoinTokenIndex(joinInfo.getRawJoinTokenIndex() + joinTokenIndexGapLength);
            } while (i < indexGapsIndex);
            indexGapsIndex = index;

        } else if (index > indexGapsIndex) {
            // Items below index should be moved to be below gap
            int i = index;
            do {
                EmbeddedJoinInfo joinInfo = tokenListList.get(--i).joinInfo();
                joinInfo.setRawTokenListIndex(joinInfo.getRawTokenListIndex() - tokenListIndexGapLength);
                joinInfo.setRawJoinTokenIndex(joinInfo.getRawJoinTokenIndex() - joinTokenIndexGapLength);
            } while (i > indexGapsIndex);
            indexGapsIndex = index;
        }
    }

    public void tokenListsModified(int tokenListCountDiff) {
        // Gap assumed to be above last added token list or above
        indexGapsIndex += tokenListCountDiff; // Move gap above added 
        tokenListCount += tokenListCountDiff;
        tokenListIndexGapLength -= tokenListCountDiff;
        // Must init rawJoinTokenIndex values in the ETLs too.
    }

    public void updateJoinTokenCount(int joinTokenCountDiff) {
        joinTokenCount += joinTokenCountDiff;
        joinTokenIndexGapLength -= joinTokenCountDiff;
    }
    
    private JoinLexerInputOperation<T> createLexerInputOperation() {
        JoinLexerInputOperation<T> lexerInputOperation = new JoinLexerInputOperation<T>(
                this, 0, null, 0, startOffset());
        lexerInputOperation.init();
        return lexerInputOperation;
    }

    private void init() {
        JoinLexerInputOperation<T> lexerInputOperation = createLexerInputOperation();
        AbstractToken<T> token;
        int tokenListIndex = 0;
        int newJoinTokenCount = 0;
        if (tokenListCount > 0) {
            boolean loggable = LOG.isLoggable(Level.FINE);
            EmbeddedTokenList<?,T> tokenList = initTokenList(tokenListIndex, newJoinTokenCount);
            while ((token = lexerInputOperation.nextToken()) != null) {
                int skipTokenListCount;
                if ((skipTokenListCount = lexerInputOperation.skipTokenListCount()) > 0) {
                    while (--skipTokenListCount >= 0) {
                        tokenList = initTokenList(++tokenListIndex, newJoinTokenCount);
                    }
                    lexerInputOperation.clearSkipTokenListCount();
                }
                if (token.getClass() == JoinToken.class) {
                    // ETL for last part
                    JoinToken<T> joinToken = (JoinToken<T>) token;
                    List<PartToken<T>> joinedParts = joinToken.joinedParts();
                    // Index for active list of addition
                    // There may be ETLs so token list count may differ from part count
                    int extraTokenListSpanCount = joinToken.extraTokenListSpanCount();
                    int joinedPartIndex = 0;
                    // Only add without the last part (will be added normally outside the loop)
                    // The last ETL can not be empty (must contain the last non-empty token part)
                    for (int i = 0; i < extraTokenListSpanCount; i++) {
                        tokenList.joinInfo().setJoinTokenLastPartShift(extraTokenListSpanCount - i);
                        if (tokenList.textLength() > 0) {
                            tokenList.addToken(joinedParts.get(joinedPartIndex++), 0, null);
                        }
                        tokenList = initTokenList(++tokenListIndex, newJoinTokenCount);
                    }
                    // Last part will be added normally by subsequent code
                    token = joinedParts.get(joinedPartIndex); // Should be (joinedParts.size()-1)
                }
                tokenList.addToken(token, lexerInputOperation);
                if (loggable) {
                    StringBuilder sb = new StringBuilder(50);
                    ArrayUtilities.appendBracketedIndex(sb, newJoinTokenCount, 2);
                    token.dumpInfo(sb, null, true, true, 0);
                    sb.append('\n');
                    LOG.fine(sb.toString());
                }
                newJoinTokenCount++; // Increase after possible logging to start from index zero
            }
            if (loggable) {
                LOG.fine("JoinTokenList created for " + tokenListList.languagePath() + // NOI18N
                        " with " + newJoinTokenCount + " tokens\n"); // NOI18N
            }
            // Init possible empty ETL at the end
            while (++tokenListIndex < tokenListCount) {
                // There may be empty ETLs that contain no tokens
                tokenList = initTokenList(tokenListIndex, newJoinTokenCount);
            }
            // Trim storage of all ETLs to their current size
            for (int i = tokenListCount - 1; i >= 0; i--) {
                EmbeddedTokenList<?,?> etl = tokenListList.get(i);
                etl.trimStorageToSize();
                assert (etl.joinInfo() != null);
            }
        }
        this.indexGapsIndex = tokenListIndex; // Gap at the end of all token lists
        this.joinTokenCount = newJoinTokenCount;
    }

    private EmbeddedTokenList<?,T> initTokenList(int tokenListIndex, int joinTokenCount) {
        EmbeddedTokenList<?,T> tokenList = tokenList(tokenListIndex);
        // Removed following code since there may be mixture of embeddings with joinSections either true or false.
        // Some token lists might be created with joinSections==false and then a custom joining embedding
        // might be created.
//        if (!tokenList.embedding().joinSections()) {
//            throw new IllegalStateException(
//                    "Embedding " + tokenList.embedding() + " not declared to join sections. " +
//                    tokenList.dumpInfo(null)
//            );
//        }
                
        if (tokenList.tokenCountCurrent() > 0) {
            // Clear all tokens so that it can be initialized by joined lexing.
            // This situation may arise when there would be mixed joining and non-joining ETLs
            // (see also TokenListList's constructor and scanTokenList()).
            tokenList.clearAllTokens();
        }
        assert (tokenList.joinInfo() == null) : "Non-null joinInfo in tokenList " +
                tokenList.dumpInfo(new StringBuilder(256)) + "\n" + tokenListList;
        tokenList.setJoinInfo(new EmbeddedJoinInfo<T>(this, joinTokenCount, tokenListIndex));
        return tokenList;
    }
    
    public String checkConsistency() {
        // Check regular consistency without checking embeddings
        String error = LexerUtilsConstants.checkConsistencyTokenList(this, false);
        if (error == null) {
            // Check individual ETLs and their join infos
            int realJoinTokenCount = 0;
            JoinToken<T> activeJoinToken = null;
            int joinedPartCount = 0;
            int nextCheckPartIndex = 0;
            for (int tokenListIndex = 0; tokenListIndex < tokenListCount(); tokenListIndex++) {
                EmbeddedTokenList<?,T> etl = tokenList(tokenListIndex);
                error = LexerUtilsConstants.checkConsistencyTokenList(etl, false);
                if (error != null)
                    return error;

                EmbeddedJoinInfo joinInfo = etl.joinInfo();
                if (joinInfo == null) {
                    return "Null joinInfo for ETL at token-list-index " + tokenListIndex; // NOI18N
                }
                if (realJoinTokenCount != joinInfo.joinTokenIndex()) {
                    return "joinTokenIndex=" + realJoinTokenCount + " != etl.joinInfo.joinTokenIndex()=" + // NOI18N
                            joinInfo.joinTokenIndex() + " at token-list-index " + tokenListIndex; // NOI18N
                }
                if (tokenListIndex != joinInfo.tokenListIndex()) {
                    return "token-list-index=" + tokenListIndex + " != etl.joinInfo.tokenListIndex()=" + // NOI18N
                            joinInfo.tokenListIndex();
                }

                int etlTokenCount = etl.tokenCount();
                int etlJoinTokenCount = etlTokenCount;
                if (etlTokenCount > 0) {
                    AbstractToken<T> token = etl.tokenOrEmbeddingDirect(0).token();
                    int startCheckIndex = 0;
                    // Check first token (may also be the last token)
                    if (activeJoinToken != null) {
                        if (token.getClass() != PartToken.class) {
                            return "Unfinished joinToken at token-list-index=" + tokenListIndex; // NOI18N
                        }
                        error = checkConsistencyJoinToken(activeJoinToken, token, nextCheckPartIndex++, tokenListIndex);
                        if (error != null) {
                            return error;
                        }
                        if (nextCheckPartIndex == joinedPartCount) {
                            activeJoinToken = null; // activeJoinToken ended
                        } else { // For non-last there must be no other tokens in the list
                            if (etlTokenCount > 1) {
                                return "More than one token and non-last part of unfinished join token" +  // NOI18N
                                        " at token-list-index " + tokenListIndex; // NOI18N
                            }
                            // etlTokenCount so the first token is last too
                            // and this is an ETL with single token part that continues activeJoinToken
                            etlJoinTokenCount--;
                        }
                        startCheckIndex = 1;
                    }
                    // Check last token
                    if (etlTokenCount > startCheckIndex) {
                        assert (activeJoinToken == null);
                        token = etl.tokenOrEmbeddingDirect(etlTokenCount - 1).token();
                        if (token.getClass() == PartToken.class) {
                            etlJoinTokenCount--;
                            activeJoinToken = ((PartToken<T>) token).joinToken();
                            joinedPartCount = activeJoinToken.joinedParts().size();
                            nextCheckPartIndex = 0;
                            if (joinedPartCount < 2) {
                                return "joinedPartCount=" + joinedPartCount + " < 2";
                            }
                            error = checkConsistencyJoinToken(activeJoinToken, token, nextCheckPartIndex++, tokenListIndex);
                            if (error != null)
                                return error;
                        }
                    }
                    // Check that no other token are part tokens than the relevant ones
                    for (int j = startCheckIndex; j < etlJoinTokenCount; j++) {
                        if (etl.tokenOrEmbeddingDirect(j).token().getClass() == PartToken.class) {
                            return "Inside PartToken at index " + j + "; joinTokenCount=" + etlJoinTokenCount; // NOI18N
                        }
                    }
                }
                if (etlJoinTokenCount != etl.joinTokenCount()) {
                    return "joinTokenCount=" + etlJoinTokenCount + " != etl.joinTokenCount()=" + // NOI18N
                            etl.joinTokenCount() + " at token-list-index " + tokenListIndex; // NOI18N
                }
                realJoinTokenCount += etlJoinTokenCount;
            } // end-of-for over ETLs
            if (activeJoinToken != null) {
                return "Unfinished join token at end";
            }
            if (realJoinTokenCount != joinTokenCount) {
                return "realJoinTokenCount=" + realJoinTokenCount + " != joinTokenCount=" + joinTokenCount; // NOI18N
            }
        }
        // Check placement of index gap
        return error;
    }

    private String checkConsistencyJoinToken(JoinToken<T> joinToken, AbstractToken<T> token, int partIndex, int tokenListIndex) {
        PartToken<T> partToken = (PartToken<T>) token;
        if (joinToken.joinedParts().get(partIndex) != token) {
            return "activeJoinToken.joinedParts().get(" + partIndex + // NOI18N
                    ") != token at token-list-index " + tokenListIndex; // NOI18N
        }
        if (partToken.joinToken() != joinToken) {
            return "Invalid join token of part at partIndex " + partIndex + // NOI18N
                    " at token-list-index " + tokenListIndex; // NOI18N
        }
        EmbeddedTokenList<?,T> etl = tokenList(tokenListIndex);
        int lps = etl.joinInfo().joinTokenLastPartShift();
        if (lps < 0) {
            return "lps=" + lps + " < 0";
        }

        if (tokenListIndex + lps >= tokenListCount()) {
            return "Invalid lps=" + lps + // NOI18N
                    " at token-list-index " + tokenListIndex + // NOI18N
                    "; tokenListCount=" + tokenListCount(); // NOI18N
        }
        AbstractToken<T> lastPart = tokenList(tokenListIndex + lps).tokenOrEmbeddingDirect(0).token();
        if (lastPart.getClass() != PartToken.class) {
            return "Invalid lps: lastPart not PartToken " + lastPart.dumpInfo(null, null, true, true, 0) + // NOI18N
                    " at token-list-index " + tokenListIndex; // NOI18N
        }
        if (((PartToken<T>)lastPart).joinToken().lastPart() != lastPart) {
            return "Invalid lps: Not last part " + lastPart.dumpInfo(null, null, true, true, 0) + // NOI18N
                    " at token-list-index " + tokenListIndex; // NOI18N
        }
        return null;
    }

    @Override
    public StringBuilder dumpInfo(StringBuilder sb) {
        sb.append("joinTokenCount=").append(joinTokenCount).
                append(", activeTokenListIndex=").append(activeTokenListIndex).
                append(", JI<").append(activeStartJoinIndex).append(",").
                append(activeEndJoinIndex).append(">\n");
        sb.append("gapIndex=").append(indexGapsIndex).
                append(", joinGapLen=").append(joinTokenIndexGapLength).
                append(", tokenListGapLen=").append(tokenListIndexGapLength).append("\n");

        int digitCount = String.valueOf(tokenListCount - 1).length();
        for (int i = 0; i < tokenListCount; i++) {
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            tokenList(i).dumpInfo(sb);
            sb.append('\n');
        }
        return sb;
    }

    @Override
    public String dumpInfoType() {
        return "JTL";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        dumpInfo(sb);
        return LexerUtilsConstants.appendTokenList(sb, this).toString();
    }

}

