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

package org.netbeans.lib.lexer.inc;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddedJoinInfo;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.JoinLexerInputOperation;
import org.netbeans.lib.lexer.JoinTokenList;
import org.netbeans.lib.lexer.TokenOrEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.lib.lexer.token.PartToken;

/**
 * Token list change for join token lists.
 *
 * @author Miloslav Metelka
 */
public final class JoinTokenListChange<T extends TokenId> extends TokenListChange<T> {
    
    /** ETL where character modification occurred. */
    EmbeddedTokenList<?,T> charModTokenList;
    
    private TokenListListUpdate<T> tokenListListUpdate;

    private int relexTokenListIndex;

    private List<RelexTokenListChange<T>> relexChanges;
    
    private RelexTokenListChange<T> lastRelexChange;
    
    private JoinLexerInputOperation<T> joinLexerInputOperation;

    public JoinTokenListChange(JoinTokenList<T> tokenList) {
        super(tokenList);
    }

    public List<? extends TokenListChange<T>> relexChanges() {
        return relexChanges;
    }

    public TokenListListUpdate<T> tokenListListUpdate() {
        return tokenListListUpdate;
    }
    
    public void setTokenListListUpdate(TokenListListUpdate<T> tokenListListUpdate) {
        this.tokenListListUpdate = tokenListListUpdate;
    }
    
    public void setStartInfo(JoinLexerInputOperation<T> joinLexerInputOperation, int localIndex) {
        this.joinLexerInputOperation = joinLexerInputOperation;
        this.relexTokenListIndex = joinLexerInputOperation.activeTokenListIndex();
        this.relexChanges = new ArrayList<RelexTokenListChange<T>>(
                tokenListListUpdate.addedTokenListCount() + 3);
        // Add first change now to incorporate starting modified token index
        lastRelexChange = new RelexTokenListChange<T>(
                joinLexerInputOperation.tokenList(relexTokenListIndex));
        // Set index in ETL to properly do replaceTokens() in ETL
        // Setting both index and offset is BTW necessary in order to properly move offset gap in ETL
        lastRelexChange.setIndex(localIndex);
        int relexOffset = joinLexerInputOperation.lastTokenEndOffset();
        lastRelexChange.setOffset(relexOffset);
        lastRelexChange.setMatchOffset(relexOffset); // Due to removeLastAddedToken() and etc.
        lastRelexChange.setParentChangeIsBoundsChange(this.parentChangeIsBoundsChange);
        relexChanges.add(lastRelexChange);
    }

    public void setNoRelexStartInfo() {
        this.relexTokenListIndex = tokenListListUpdate.modTokenListIndex;
        this.relexChanges = new ArrayList<RelexTokenListChange<T>>(1);
    }

    @Override
    public void addToken(AbstractToken<T> token, int lookahead, Object state) {
        // Check if lexer-input-operation advanced to next list and possibly add corresponding relex change(s)
        int skipTokenListCount;
        if ((skipTokenListCount = joinLexerInputOperation.skipTokenListCount()) > 0) {
            while (--skipTokenListCount >= 0) {
                lastRelexChange.finish();
                addRelexChange();
            }
            joinLexerInputOperation.clearSkipTokenListCount();
        }
        if (token.getClass() == JoinToken.class) {
            JoinToken<T> joinToken = (JoinToken<T>) token;
            List<PartToken<T>> joinedParts = joinToken.joinedParts();
            int extraTokenListSpanCount = joinToken.extraTokenListSpanCount();
            int joinedPartIndex = 0;
            // Only add without the last part (will be added normally outside the loop)
            // The last ETL can not be empty (must contain the last non-empty token part)
            for (int i = 0; i < extraTokenListSpanCount; i++) {
                lastRelexChange.joinTokenLastPartShift = extraTokenListSpanCount - i;
                if (((EmbeddedTokenList<?,T>)lastRelexChange.tokenList()).textLength() > 0) {
                    PartToken<T> partToken = joinedParts.get(joinedPartIndex++);
                    lastRelexChange.addToken(partToken, 0, null);
                }
                addRelexChange();
            }
            // Last part will be added normally by subsequent code
            token = joinedParts.get(joinedPartIndex); // Should be (joinedParts.size()-1)
        }
        lastRelexChange.addToken(token, lookahead, state);
        addedEndOffset = lastRelexChange.addedEndOffset;
        tokenChangeInfo().updateAddedTokenCount(+1);
    }

    private void addRelexChange() {
        EmbeddedTokenList<?,T> etl = joinLexerInputOperation.tokenList(
                relexTokenListIndex + relexChanges.size());
        lastRelexChange = new RelexTokenListChange<T>(etl);
        int startOffset = etl.startOffset();
        lastRelexChange.setOffset(startOffset);
        lastRelexChange.setParentChangeIsBoundsChange(this.parentChangeIsBoundsChange);
        relexChanges.add(lastRelexChange);
    }

    @Override
    public int increaseMatchIndex() {
        JoinTokenList<T> jtl = (JoinTokenList<T>) tokenList();
        AbstractToken<T> token = jtl.tokenOrEmbeddingDirect(matchIndex).token();
        // matchOffset needs to be set to end of token at matchIndex
        if (token.getClass() == JoinToken.class) {
            matchOffset = ((JoinToken<T>) token).endOffset();
        } else { // Check whether the matchIndex points to begining of ETL
            if (matchIndex == jtl.activeStartJoinIndex()) { // First in ETL
                // Cannot use previous value of matchOffset since it pointed to end of previous ETL
                // Token is not join token so use its natural length()
                EmbeddedTokenList<?,T> etl = jtl.activeTokenList();
                // If an insertion was done right at the begining of a modified ETL
                // then the matchOffset should only be increased because
                // the computation would not include the inserted text.
                if (etl != charModTokenList) {
                    matchOffset = etl.startOffset() + token.length();
                } else {
                    matchOffset += token.length();
                }
            } else {
                matchOffset += token.length();
            }
        }
        matchIndex++;
        return matchOffset;
    }

    @Override
    public AbstractToken<T> removeLastAddedToken() {
        AbstractToken<T> lastRemovedToken = lastRelexChange.removeLastAddedToken();
        if (lastRemovedToken.getClass() == PartToken.class) { // Join token
            // Remove extra parts - the relex changes
            int extraCount = ((PartToken<T>) lastRemovedToken).joinToken().extraTokenListSpanCount();
            for (int i = extraCount - 1; i >= 0; i--) {
                relexChanges.remove(relexChanges.size() - 1);
            }
            lastRelexChange = relexChanges.get(relexChanges.size() - 1);
            lastRemovedToken = lastRelexChange.removeLastAddedToken();
        }
        if (lastRelexChange.addedTokenOrEmbeddings().size() == 0) { // Empty change
            // Use addedEndOffset of the previous change and remove this one
            relexChanges.remove(relexChanges.size() - 1);
            
        }
        addedEndOffset = lastRelexChange.addedEndOffset;
        tokenChangeInfo().updateAddedTokenCount(-1);
        return lastRemovedToken;
    }
    
    void replaceTokenLists() {
        JoinTokenList<T> jtl = (JoinTokenList<T>) tokenList();
        // Move gap after last ETL that was relexed (obsolete ETLs still not removed)
        jtl.moveIndexGap(tokenListListUpdate.modTokenListIndex + tokenListListUpdate.removedTokenListCount);
        // Do physical ETLs replace
        tokenListListUpdate.replaceTokenLists();
        jtl.tokenListsModified(tokenListListUpdate.modTokenListCountDiff());
    }
    
    public void replaceTokens(TokenHierarchyEventInfo eventInfo) {
        // BTW the following case must be properly handled:
        // Remove ']' from "<x>a<]>c"  (present in JoinRandomTest)
        // The 'x' is joined with ']' and removal of ']' means that the last ETL
        // with char data is the one containing 'x'. However ETL with "<]>" is now "<>"
        // but the contained token ']' must be removed.

        // Determine position of matchIndex in token lists and ensure that in all ETLs
        // except the matchTokenListIndex the matchIndex will be set to ETL.tokenCount()
        // and in the matchLocalIndex it will be set to localMatchIndex.

        // Determine matchTokenListIndex and localMatchIndex corresponding to matchIndex.
        // If matchIndex == jtl.tokenCount() the token list index will be jtl.tokenListCount()
        // and localMatchIndex will be 0.
        JoinTokenList<T> jtl = (JoinTokenList<T>) tokenList();
        int matchTokenListIndex;
        int localMatchIndex;
        if (matchIndex == jtl.tokenCount()) {
            localMatchIndex = 0;
            matchTokenListIndex = jtl.tokenListCount();
        } else {
            localMatchIndex = jtl.tokenStartLocalIndex(matchIndex); // Also fetch activeTokenListIndex
            matchTokenListIndex = jtl.activeTokenListIndex();
        }
        // Since relexChanges only contain the "new" ETLs (i.e. the removed ETLs are not contained)
        // the algorithm must handle that.
        // To simplify the algorithm first ensure that the matchTokenListIndex is >= possible last removed ETL.
        // That should mostly be true but could possibly not be true if e.g. last removed ETL is empty and non-joined
        // so fix that case.
        int removedEndTokenListIndex = tokenListListUpdate.modTokenListIndex + tokenListListUpdate.removedTokenListCount;
        if (matchTokenListIndex < removedEndTokenListIndex) {
            matchTokenListIndex = removedEndTokenListIndex;
            localMatchIndex = 0;
        }
        // Now matchTokenListIndex >= removedEndTokenListIndex
        int afterUpdateMatchEndIndex = matchTokenListIndex + tokenListListUpdate.modTokenListCountDiff();
        // If localMatchIndex == 0 it means that in fact only previous ETL was covered.
        if (localMatchIndex != 0) { // Include ETL at matchTokenListIndex in relexChanges too.
            afterUpdateMatchEndIndex++;
        }
        int relexChangesEndIndex = relexTokenListIndex + relexChanges.size();
        while (relexChangesEndIndex < afterUpdateMatchEndIndex) {
            RelexTokenListChange<T> change = new RelexTokenListChange<T>(
                    tokenListListUpdate.afterUpdateTokenList(jtl, relexChangesEndIndex++));
            change.setParentChangeIsBoundsChange(this.parentChangeIsBoundsChange);
            relexChanges.add(change);
        }
        // Now if localMatchIndex != 0 then ETL at (relexChangesEndIndex-1) must match at localMatchIndex
        // and rest at their ETL.tokenCount().
        // For localMatchIndex == 0 all relexChanges should match at ETL.tokenCount().
        int index = afterUpdateMatchEndIndex - 1;
        if (localMatchIndex != 0 && index >= relexTokenListIndex) {
            TokenListChange<T> change = relexChanges.get(index - relexTokenListIndex);
            change.setMatchIndex(localMatchIndex);
            index--;
        }
        // Now all the relex changes at and below index should match at their ETL.tokenCount().
        // Newly added ETLs should be fine since they should still be empty at this point.
        while (index >= relexTokenListIndex) {
            TokenListChange<T> change = relexChanges.get(index - relexTokenListIndex);
            change.setMatchIndex(change.tokenList().tokenCountCurrent());
            index--;
        }

        // Physically replace the token lists
        if (tokenListListUpdate.isTokenListsMod()) {
            replaceTokenLists();
        }
        jtl.moveIndexGap(relexTokenListIndex + relexChanges.size());

        // Remember join token count right before the first relexed ETL
        int joinTokenIndex;
        if (relexTokenListIndex > 0) {
            EmbeddedTokenList<?,T> etl = jtl.tokenList(relexTokenListIndex - 1);
            joinTokenIndex = etl.joinInfo().joinTokenIndex() + etl.joinTokenCount(); // Physical removal already performed
        } else {
            joinTokenIndex = 0;
        }
        // Find index at which it's necessary to grab tokens from removed ETLs (no relex changes for them).
        boolean collectRemovedETLs = (tokenListListUpdate.isTokenListsMod() && tokenListListUpdate.removedTokenListCount > 0);
        RemovedTokensCollector removedTokensCollector = new RemovedTokensCollector();
        // Now process each relex change and update join token count etc.
        int i;
        for (i = 0; i < relexChanges.size(); i++) {
            RelexTokenListChange<T> change = relexChanges.get(i);
            //assert (change.laState().size() == change.addedTokenOrEmbeddingsCount());
            EmbeddedTokenList<?,T> etl = (EmbeddedTokenList<?,T>) change.tokenList();
            if (etl.joinInfo() == null) {
                etl.setJoinInfo(new EmbeddedJoinInfo<T>(jtl, joinTokenIndex, relexTokenListIndex + i));
            } else {
                etl.joinInfo().setRawJoinTokenIndex(joinTokenIndex);
            }
            // Set new joinTokenLastPartShift before calling etl.joinTokenCount()
            // Only set LPS for non-last change and in case the removal was till end
            // of ETL.
            if (i < relexChanges.size() - 1 || change.index() + change.removedTokenCount() == etl.tokenCountCurrent()) {
                etl.joinInfo().setJoinTokenLastPartShift(change.joinTokenLastPartShift);
            }
            // Replace tokens in the individual ETL
            etl.replaceTokens(change, eventInfo, (etl == charModTokenList));
            // Fix join token count
            joinTokenIndex += etl.joinTokenCount();
            // Possibly grab the removed tokens from removed ETLs (so that they are present in a TokenChange)
            if (collectRemovedETLs && i == tokenListListUpdate.modTokenListIndex - relexTokenListIndex) {
                removedTokensCollector.collectRemovedTokenLists();
                collectRemovedETLs = false;
            }
            // Grab removed tokens from relex change (so that removed tokens are present in a TokenChange)
            removedTokensCollector.collectRelexChange(change);
        }
        // Cover case when there are no relex changes at all
        // or when removed token lists are right above the relex changes
        if (collectRemovedETLs) {
            removedTokensCollector.collectRemovedTokenLists();
        }
        removedTokensCollector.finish();
        
        // Now fix the total join token count
        i += relexTokenListIndex;
        int origJoinTokenIndex = (i < jtl.tokenListCount())
                ? jtl.tokenList(i).joinInfo().joinTokenIndex()
                : jtl.tokenCountCurrent();
        int joinTokenCountDiff = joinTokenIndex - origJoinTokenIndex;
        jtl.updateJoinTokenCount(joinTokenCountDiff);
        
        // Possibly mark this change as bound change
        if (relexChanges.size() == 1 && !tokenListListUpdate.isTokenListsMod()) { // Only change inside single ETL
            if (relexChanges.get(0).isBoundsChange()) {
                markBoundsChange(); // Joined change treated as bounds change too
            }
        }
        
        // The jtl cannot be used without jtl.resetActiveAfterUpdate() since it may cache
        //   an obsolete ETL as activeTokenList
        // This may show up to clients since JTL instance will be present in TokenChange.currentTokenSequence().
        jtl.resetActiveAfterUpdate();
//        assert (jtl.checkConsistency() == null) : jtl.checkConsistency();
    }

    void collectAddedRemovedEmbeddings(TokenHierarchyUpdate.UpdateItem<T> updateItem) {
        // Collecting of removed embeddings must be done in the following order:
        // 1) Removed embeddings from relexChanges located below modTokenListIndex
        // 2) All removed ETLs in TokenListListUpdate
        // 3) embeddings from relexChanges located above modTokenListIndex
        int modIndexInRelexChanges = tokenListListUpdate.modTokenListIndex - relexTokenListIndex;
        int i;
        for (i = 0; i < modIndexInRelexChanges; i++) {
            RelexTokenListChange change = relexChanges.get(i);
            updateItem.collectRemovedEmbeddings(change);
        }
        tokenListListUpdate.collectRemovedEmbeddings(updateItem);
        for (; i < relexChanges.size(); i++) {
            RelexTokenListChange change = relexChanges.get(i);
            updateItem.collectRemovedEmbeddings(change);
        }

        // All added embeddings from relexChanges will be added one by one
        // since relexChanges contain a change for added ETLs.
        for (i = 0; i < relexChanges.size(); i++) {
            RelexTokenListChange change = relexChanges.get(i);
            updateItem.collectAddedEmbeddings(change);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "\nTLLUpdate: " + tokenListListUpdate + // NOI18N
                ", relexTLInd=" + relexTokenListIndex + // NOI18N
                ", relexChgs.size()=" + relexChanges.size();
    }

    @Override
    public String toStringMods(int indent) {
        StringBuilder sb = new StringBuilder(100);
        for (RelexTokenListChange change : relexChanges) {
            sb.append(change.toStringMods(indent));
            sb.append('\n');
        }
        return sb.toString();
    }
    
    static final class RelexTokenListChange<T extends TokenId> extends TokenListChange<T> {
        
        int joinTokenLastPartShift; // New value for EmbeddedJoinInfo.joinTokenLastPartShift during relex

        RelexTokenListChange(EmbeddedTokenList<?,T> tokenList) {
            super(tokenList);
        }
        
        void finish() {
            setMatchIndex(tokenList().tokenCountCurrent());
        }

        @Override
        public String toString() {
            return super.toString() + ", lps=" + joinTokenLastPartShift;
        }
        
    }

    private final class RemovedTokensCollector {

        @SuppressWarnings("unchecked")
        TokenOrEmbedding<T>[] removedTokensOrEs = new TokenOrEmbedding[removedTokenCount()];

        int removedTokensIndex;

        AbstractToken<T> lastBranchToken;

        void collectRelexChange(RelexTokenListChange<T> relexChange) {
            RemovedTokenList<T> rtl = relexChange.tokenChangeInfo().removedTokenList();
            if (rtl.tokenCount() > 0) {
                TokenOrEmbedding<T> tokenOrE = rtl.tokenOrEmbedding(0);
                if (tokenOrE.token().getClass() == PartToken.class) {
                    if (((PartToken)tokenOrE.token()).joinToken() != lastBranchToken) {
                        removedTokensOrEs[removedTokensIndex++] = tokenOrE;
                    }
                } else {
                    removedTokensOrEs[removedTokensIndex++] = tokenOrE;
                }
                // Copy the rest
                int tokenCountM1 = rtl.tokenCount() - 1;
                System.arraycopy(rtl.tokenOrEmbeddings(), 1, removedTokensOrEs,
                        removedTokensIndex, tokenCountM1);
                removedTokensIndex += tokenCountM1;
                // Set last branch token
                lastBranchToken = rtl.tokenOrEmbedding(tokenCountM1).token();
                lastBranchToken = (lastBranchToken.getClass() == PartToken.class)
                        ? ((PartToken<T>)lastBranchToken).joinToken()
                        : null;
            }
      }

      void collectRemovedTokenLists() {
            EmbeddedTokenList<?,T>[] removedTokenLists = tokenListListUpdate.removedTokenLists();
            for (int j = 0; j < removedTokenLists.length; j++) {
                EmbeddedTokenList<?,T> removedEtl = removedTokenLists[j];
                int tokenCountM1 = removedEtl.tokenCountCurrent() - 1;
                if (tokenCountM1 >= 0) { // at least one token in removed ETL
                    TokenOrEmbedding<T> tokenOrE = removedEtl.tokenOrEmbedding(0);
                    if (tokenOrE.token().getClass() == PartToken.class) {
                        if (((PartToken) tokenOrE.token()).joinToken() != lastBranchToken) {
                            removedTokensOrEs[removedTokensIndex++] = tokenOrE;
                        }
                    } else {
                        removedTokensOrEs[removedTokensIndex++] = tokenOrE;
                    }
                    // Copy the rest
                    removedEtl.copyElements(1, tokenCountM1 + 1, removedTokensOrEs, removedTokensIndex);
                    removedTokensIndex += tokenCountM1;
                    // Set last branch token
                    lastBranchToken = removedEtl.tokenOrEmbedding(tokenCountM1).token();
                    lastBranchToken = (lastBranchToken.getClass() == PartToken.class)
                            ? ((PartToken<T>) lastBranchToken).joinToken()
                            : null;
                }
            }
        }

      void finish() {
          if (removedTokensIndex != removedTokensOrEs.length) {
              throw new IndexOutOfBoundsException("Invalid removedTokensIndex=" + removedTokensIndex + // NOI18N
                      " != removedTokens.length=" + removedTokensOrEs.length); // NOI18N
          }
          setRemovedTokens(removedTokensOrEs);
      }

    }

}
