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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.JoinTokenList;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.TokenListList;

/**
 * Change of a particular TokenListList.
 *
 * @author Miloslav Metelka
 */

final class TokenListListUpdate<T extends TokenId> {

    // -J-Dorg.netbeans.lib.lexer.inc.TokenListListUpdate.level=FINE
    private static final Logger LOG = Logger.getLogger(TokenListListUpdate.class.getName());
    
    /**
     * Token list list for the case when the particular language path
     * corresponds to joined.
     */
    final TokenListList<T> tokenListList;

    /**
     * Index of first added/removed/changed ETL.
     * -1 means that the value was not initialized yet.
     */
    int modTokenListIndex;

    int removedTokenListCount;

    private EmbeddedTokenList<?,T>[] removedTokenLists;

    List<EmbeddedTokenList<?,T>> addedTokenLists;

    /**
     * Whether any of the added token lists holds a joined embedding.
     */
    boolean addedJoined;

    TokenListListUpdate(TokenListList<T> tokenListList) {
        this.tokenListList = tokenListList;
        this.modTokenListIndex = -1;
    }

    public boolean isTokenListsMod() { // If any ETL was removed/added
        return (removedTokenListCount != 0) || addedTokenLists.size() > 0;
    }

    public int modTokenListCountDiff() {
        return addedTokenLists.size() - removedTokenListCount;
    }

    public int addedTokenListCount() {
        return addedTokenLists.size();
    }

    public EmbeddedTokenList<?,T>[] removedTokenLists() {
        return removedTokenLists;
    }

    /**
     * Get an ETL in index space that would be present after physical updating.
     * @param jtl non-null join token list.
     * @param tokenListIndex token list index that corresponds to the updated state
     *   i.e. it could grab the new token lists if any and cannot grab the removed token lists.
     * @return ETL at the given index.
     */
    public EmbeddedTokenList<?,T> afterUpdateTokenList(JoinTokenList<T> jtl, int tokenListIndex) {
        EmbeddedTokenList<?,T> etl;
        if (tokenListIndex < modTokenListIndex) {
            etl = jtl.tokenList(tokenListIndex);
            // Update ETL's start offset. JTL.tokenStartLocalIndex() may skip down several ETLs
            //   for join tokens so this needs to be done to properly relex.
            etl.updateModCount();
        } else if (tokenListIndex < modTokenListIndex + addedTokenLists.size()) {
            etl = addedTokenLists.get(tokenListIndex - modTokenListIndex);
        } else { // Last part after removed and added
            etl = jtl.tokenList(tokenListIndex + removedTokenListCount - addedTokenLists.size());
            // Update ETL's start offset.
            // Only used by TLU so update without syncing.
            etl.updateModCount();
        }
        return etl;
    }

    protected int afterUpdateTokenListCount(JoinTokenList<T> jtl) {
        return jtl.tokenListCount() - removedTokenListCount + addedTokenLists.size();
    }

    void markChangedMember(EmbeddedTokenList<?,T> changedTokenList) {
        assert (modTokenListIndex == -1);
        modTokenListIndex = tokenListList.findIndex(changedTokenList.startOffset());
        assert (tokenListList.get(modTokenListIndex) == changedTokenList) :
            "changedTokenList at index " + modTokenListIndex + "; TokenListList:\n" + tokenListList;
    }

    void markChageBetween(int offset) { // Nothing added/removed and mod outside of bounds of an ETL
        assert (modTokenListIndex == -1);
        modTokenListIndex = tokenListList.findIndex(offset);
    }

    /**
     * Mark the given token list as removed in the token list list.
     * All removed token lists should be marked subsequently their increasing offset
     * so it should be necessary to search for the index just once.
     * <br/>
     * It's expected that updateStatusImpl() was already called
     * on the corresponding embedding container.
     */
    void markRemovedMember(EmbeddedTokenList<?,T> removedTokenList, TokenHierarchyEventInfo eventInfo) {
        removedTokenList.updateModCount();
        boolean indexWasMinusOne; // Used for possible exception cause debugging
//            removedTokenList.embeddingContainer().checkStatusUpdated();
        if (modTokenListIndex == -1) {
            assert (removedTokenListCount == 0);
            indexWasMinusOne = true;
            modTokenListIndex = tokenListList.findIndexDuringUpdate(removedTokenList, eventInfo);
            assert (modTokenListIndex >= 0) : "tokenListIndex=" + modTokenListIndex + " < 0"; // NOI18N
        } else { // tokenListIndex already initialized
            indexWasMinusOne = false;
        }
        EmbeddedTokenList<?,T> markedForRemoveTokenList = tokenListList.getOrNull(modTokenListIndex + removedTokenListCount);
        if (markedForRemoveTokenList != removedTokenList) {
            int realIndex = tokenListList.indexOf(removedTokenList);
            String msg = "\n\nLEXER-INTERNAL-ERROR: Removing at tokenListIndex=" + modTokenListIndex + // NOI18N
                    " but real tokenListIndex is " + realIndex + // NOI18N
                    " (indexWasMinusOne=" + indexWasMinusOne + ").\n" + // NOI18N
                    "Wishing to remove tokenList\n" + // NOI18N
                    ((removedTokenList != null) ? removedTokenList.dumpInfo(new StringBuilder(256)) : "!!<NULL>!!") + // NOI18N
                    "\nbut marked-for-remove tokenList is \n" + // NOI18N
                    ((markedForRemoveTokenList != null) ? markedForRemoveTokenList.dumpInfo(new StringBuilder(256)) : "!!<NULL>!!") + // NOI18N
                    "\nfrom tokenListList\n" + tokenListList + // NOI18N
                    "\nModification description:\n" + eventInfo.modificationDescription(true); // NOI18N
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.warning(msg);
            }
            if (indexWasMinusOne) {
                modTokenListIndex = realIndex; // Fix the index
                if (TokenList.LOG.isLoggable(Level.FINE)) {
                    // Notify an exception when running tests
                    throw new IllegalStateException("Invalid modTokenListIndex");
                }
            } else { // Cannot fix the index
                throw new IllegalStateException("Cannot fix modTokenListIndex");
            }
        }
        removedTokenListCount++;
    }

    /**
     * Mark the given token list to be added to this list of token lists.
     * At the end first the token lists marked for removal will be removed
     * and then the token lists marked for addition will be added.
     * <br/>
     * It's expected that updateStatusImpl() was already called
     * on the corresponding embedding container.
     */
    void markAddedMember(EmbeddedTokenList<?,T> addedTokenList) {
//            addedTokenList.embeddingContainer().checkStatusUpdated();
        if (addedTokenLists == null) {
            if (modTokenListIndex == -1) {
                modTokenListIndex = tokenListList.findIndex(addedTokenList.startOffset());
                assert (modTokenListIndex >= 0) : "tokenListIndex=" + modTokenListIndex + " < 0"; // NOI18N
            }
            addedTokenLists = new ArrayList<EmbeddedTokenList<?,T>>(4);
        }
        addedTokenLists.add(addedTokenList);
    }

    void replaceTokenLists() {
        assert (removedTokenListCount > 0 || addedTokenLists != null);
        removedTokenLists = tokenListList.replace(
                modTokenListIndex, removedTokenListCount, addedTokenLists);
    }
    
    /**
     * Collect removed embeddings from all the removed ETLs.
     */
    void collectRemovedEmbeddings(TokenHierarchyUpdate.UpdateItem<T> updateItem) {
        if (tokenListList.hasChildren()) {
            if (removedTokenLists != null) {
                for (int i = 0; i < removedTokenLists.length; i++) {
                    EmbeddedTokenList<?,T> etl = removedTokenLists[i];
                    updateItem.collectRemovedEmbeddings(etl);
                }
            }
        }
    }

    void collectAddedEmbeddings(TokenHierarchyUpdate.UpdateItem<T> updateItem) {
        // The TLL is non-joining yet but that may also be because there were no ETLs yet
        // and once new ETLs are added they may be joining and whole TLL becomes joining
        boolean becomeJoining = false;
        for (int i = 0; i < addedTokenLists.size(); i++) {
            EmbeddedTokenList<?,T> addedEtl = addedTokenLists.get(i);
            becomeJoining |= addedEtl.languageEmbedding().joinSections();
        }
        
        if (becomeJoining) {
            // Create JTL to init tokens
            tokenListList.setJoinSections(true);
            tokenListList.checkCreateJoinTokenList();
        }
        for (int i = 0; i < addedTokenLists.size(); i++) {
            EmbeddedTokenList<?,T> addedEtl = addedTokenLists.get(i);
            if (!becomeJoining) {
                addedEtl.initAllTokens();
            }
            if (tokenListList.hasChildren()) {
                updateItem.collectAddedEmbeddings(addedEtl, 0, addedEtl.tokenCountCurrent(), updateItem.childrenLanguages);
            }
        }
    }


    TokenListChange<T> createTokenListChange(EmbeddedTokenList<?,T> etl) {
        assert (etl != null);
        TokenListChange<T> etlTokenListChange;
        if (tokenListList.joinSections()) {
            JoinTokenList<T> jtl = tokenListList.joinTokenList();
            etlTokenListChange = new JoinTokenListChange<T>(jtl);
        } else { // Non-joining
            etlTokenListChange = new TokenListChange<T>(etl);
        }
        return etlTokenListChange;
    }

    TokenListChange<T> createJoinTokenListChange() {
        assert (tokenListList.joinSections());
        // In case when adding at jtl.tokenListCount() a last ETL must be used
        int etlIndex = Math.min(modTokenListIndex, tokenListList.size() - 1);
        JoinTokenList<T> jtl = tokenListList.joinTokenList();
        jtl.setActiveTokenListIndex(etlIndex);
        return new JoinTokenListChange<T>(jtl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(80);
        sb.append("modTLInd=").append(modTokenListIndex).append("; "); // NOI18N
        if (isTokenListsMod()) {
            sb.append("Rem:").append(removedTokenListCount); // NOI18N
            sb.append(" Add:").append(addedTokenLists.size()); // NOI18N
        } else { // no TL mod
            sb.append("NoTLMod");
        }
        sb.append(" Size:").append(tokenListList.size()); // NOI18N
        return sb.toString();
    }

}
