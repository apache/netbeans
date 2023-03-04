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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.EmbeddingOperation;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.TokenListList;
import org.netbeans.lib.lexer.TokenOrEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Request for updating of token hierarchy after text modification
 * or custom embedding creation/removal.
 * <br/>
 * This class contains all the data and methods related to updating.
 *
 * @author Miloslav Metelka
 */

public final class TokenHierarchyUpdate {

    public static <T extends TokenId> UpdateItem<T> createUpdateItem(TokenListChange<T> change) {
        UpdateItem<T> updateItem = new UpdateItem<T>(null, null, null);
        updateItem.tokenListChange = change;
        return updateItem;
    }
    
    // -J-Dorg.netbeans.lib.lexer.TokenHierarchyUpdate.level=FINE
    static final Logger LOG = Logger.getLogger(TokenHierarchyUpdate.class.getName());

    /**
     * Special constant value to avoid double map search for token list lists updating.
     */
    private static final UpdateItem<?> NO_ITEM = new UpdateItem<TokenId>(null, null, null);
    
    final TokenHierarchyEventInfo eventInfo;
    
    /**
     * Infos ordered from higher top levels of the hierarchy to lower levels.
     * Useful for top-down updating at the end.
     */
    private List<List<UpdateItem<?>>> itemLevels;
    
    /**
     * Mapping of LP to UpdateItem for a joined ETLs.
     */
    private Map<LanguagePath,UpdateItem<?>> path2Item;
    
    private LanguagePath lastPath2ItemPath;
    private UpdateItem<?> lastPath2ItemItem;
    
    public TokenHierarchyUpdate(TokenHierarchyEventInfo eventInfo) {
        this.eventInfo = eventInfo;
    }

    public void update() {
        TokenHierarchyOperation<?,?> operation = eventInfo.tokenHierarchyOperation();
        IncTokenList<?> incTokenList = (IncTokenList<?>) operation.rootTokenList();

        if (LOG.isLoggable(Level.FINE)) {
            if (LOG.isLoggable(Level.FINEST)) {
                // Display current state of the hierarchy by faking its text
                // through original text
                CharSequence text = incTokenList.inputSourceText();
                assert (text != null);
                incTokenList.setInputSourceText(eventInfo.originalText());
                // Dump all contents
                LOG.finest("\n\nBEFORE UPDATE:\n" + operation.toString() + '\n');
                // Return the original text
                incTokenList.setInputSourceText(text);
            }

            StringBuilder sb = new StringBuilder(150);
            sb.append("<<<<<<<<<<<<<<<<<< LEXER CHANGE START ------------------\n"); // NOI18N
            sb.append(eventInfo.modificationDescription(false));
            LOG.fine(sb.toString());
        }

        updateImpl(incTokenList, operation.rootChildrenLanguages());

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("AFFECTED: " + eventInfo.dumpAffected() + "\n"); // NOI18N
            String extraMsg = "";
            if (LOG.isLoggable(Level.FINER)) {
                // Check consistency of the whole token hierarchy
                String error = operation.checkConsistency();
                if (error != null) {
                    String msg = "\n!!!CONSISTENCY-ERROR!!!: " + error + "\n\n" + // NOI18N
                            "INCONSISTENT TOKEN HIERARCHY:\n" + operation + "\n\n";
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine(msg);
                    }
                    throw new IllegalStateException("INCONSISTENCY in token hierarchy occurred"); // NOI18N
                } else {
                    extraMsg = "(TokenHierarchy Check OK) ";
                }
            }
            LOG.fine(">>>>>>>>>>>>>>>>>> LEXER CHANGE END " + extraMsg + "------------------\n"); // NOI18N
        }

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("\n\nAFTER UPDATE:\n" + operation.toString() + '\n');
        }
    }

    private <T extends TokenId> void updateImpl(IncTokenList<T> incTokenList, Object rootChildrenLanguages) {
        incTokenList.incrementModCount();
        
        // Update starts at the top language path an goes to possibly embedded-token-lists (ETLs)
        // based on the top-level change. If there are embeddings that join sections
        // a token-list-list (TLL) exists for the given language path that maintains
        // all ETLs for the whole input source.
        // 1. The updating must always go from upper levels to more embedded levels of the token hierarchy
        //    to ensure that the tokens of the possible joined ETLs get updated properly
        //    as the tokens created/removed at upper levels may contain embeddings that will
        //    need to be added/removed from TLL of more embedded level.
        // 2. A single insert/remove may produce token updates at several
        //    places in the document due to joining of ETLs. In turn the added/removed
        //    ETLs may affect more embedded levels so the update can affect
        //    multiple places of input source.
        // 3. The algorithm must collect both removed and added ETLs
        //    and process them prior calling the TokenListUpdater to update actual tokens.
        // 4. For a removed ETL the updating must check and collect nested ETLs
        //    because some embedded tokens of the removed ETL might contain
        //    another ETL that might be maintained as TLL.
        // 5. Added ETLs must also be inspected for nested ETLs maintained in a TLL.
        //    Initialization of added ETLs is done when the particular level is processed
        //    because TLL can join sections so they must be lexed once the definite additions
        //    and removals of ETLs are known. For non-joining ETLs this could be done
        //    immediately but it is not necessary so it's done at the same time as well.
        // 6. For all TLLs their parent TLLs (for language path with last language stripped)
        //    are also maintained mandatorily.
        // 7. Algorithm maintains "item-levels" to respect top-down processing
        //    according to language-path-depth.
        
        itemLevels = new ArrayList<List<UpdateItem<?>>>(3); // Suffice for two-level embedding without realloc
        // Create root item first for root token list
        UpdateItem<T> rootItem = new UpdateItem<T>(this, null, rootChildrenLanguages);
        rootItem.tokenListChange = new TokenListChange<T>(incTokenList);
        addItem(rootItem, 0);
        processLevelInfos();
    }
    
    public <T extends TokenId> void updateCreateOrRemoveEmbedding(EmbeddedTokenList<?,T> addedOrRemovedTokenList, boolean add) {
        LanguagePath languagePath = addedOrRemovedTokenList.languagePath();
        int level = languagePath.size() - 1;
        itemLevels = new ArrayList<List<UpdateItem<?>>>(level + 2); // One extra level for growth
        UpdateItem<T> item = tokenListListItem(languagePath);
        if (item != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("THU.updateCreateOrRemoveEmbedding() add=" + add + ": " +
                    addedOrRemovedTokenList.dumpInfo(new StringBuilder(256)));
            }
            if (add) {
                item.tokenListListUpdate.markAddedMember(addedOrRemovedTokenList);
            } else {
                item.tokenListListUpdate.markRemovedMember(addedOrRemovedTokenList, eventInfo);
            }
            processLevelInfos();
        }
    }
    
    private void processLevelInfos() {
        // Process item levels which can extend the list by new items at the same level
        // or in the next levels. Therefore iterate by INDEX.since size() may change.
        for (int i = 0; i < itemLevels.size(); i++) {
            List<UpdateItem<?>> items = itemLevels.get(i);
            // The "items" list should not be extended by additional items dynamically during iteration.
            for (UpdateItem<?> item : items) {
                item.update();
            }
        }
    }
    
    void addItem(UpdateItem<?> item, int level) {
        while (level >= itemLevels.size()) {
            itemLevels.add(new ArrayList<UpdateItem<?>>(3));
        }
        List<UpdateItem<?>> items = itemLevels.get(level);
        items.add(item);
    }

    /**
     * Return tll info or null if the token list list is not maintained
     * for the given language path.
     */
    private <T extends TokenId> UpdateItem<T> tokenListListItem(LanguagePath languagePath) {
        if (languagePath == lastPath2ItemPath) { // Use last queried one
            @SuppressWarnings("unchecked")
            UpdateItem<T> item = (UpdateItem<T>) lastPath2ItemItem;
            return item;

        } else { // Not last returned item
            if (path2Item == null) { // Init since it will contain either target item or noInfo()
                path2Item = new HashMap<LanguagePath,UpdateItem<?>>(4, 0.5f);
            }
            @SuppressWarnings("unchecked")
            UpdateItem<T> item = (UpdateItem<T>)path2Item.get(languagePath);
            if (item == NO_ITEM) { // Marker value for null (to query just single map - this one)
                item = null;
            } else if (item == null) {
                TokenListList<T> tokenListList = eventInfo.tokenHierarchyOperation().existingTokenListList(languagePath);
                if (tokenListList != null) {
                    item = new UpdateItem<T>(this, tokenListList, tokenListList.childrenLanguages());
                    int level = languagePath.size() - 1;
                    addItem(item, level); // Add item to be scheduled for processing
                    path2Item.put(languagePath, item);
                } else { // Use NO_ITEM marker value to immediately know that there's no tokenListList for the given LP
                    path2Item.put(languagePath, NO_ITEM); // NO_ITEM is of type UpdateItem<?>
                }
            } // else - regular valid item
            lastPath2ItemItem = item; // Remember unmasked value i.e. "null" directly
            return item;
        }
    }
    
    /**
     * Information about update in a particular token list or a particular token list list.
     */
    public static final class UpdateItem<T extends TokenId> {

        final TokenHierarchyUpdate update;

        final TokenListListUpdate<T> tokenListListUpdate;
        
        final Object childrenLanguages; // null or language or Language[]

        UpdateItem<?> parentItem;

        /**
         * Token list change performed during this update.
         */
        TokenListChange<T> tokenListChange;
        
        public UpdateItem(TokenHierarchyUpdate update, TokenListList<T> tokenListList, Object childrenLanguages) {
            this.update = update;
            this.tokenListListUpdate = (tokenListList != null)
                    ? new TokenListListUpdate<T>(tokenListList)
                    : null;
            this.childrenLanguages = childrenLanguages;
        }

        void setParentItem(UpdateItem<?> parentItem) {
            assert (this.parentItem == null);
            this.parentItem = parentItem;
        }

        void initTokenListChange(EmbeddedTokenList<?,T> etl) {
            assert (tokenListChange == null);
            if (tokenListListUpdate != null) {
                // ETL managed by a TokenListList. If the TLL joins sections
                // then a JoinTokenListChange needs to be created.
                tokenListChange = tokenListListUpdate.createTokenListChange(etl);

            } else { // No child managed by TLL but want to process nested possible bounds changes as deep as possible
                // Perform change in child - it surely does not join the sections
                // since otherwise the tllItem could not be null.
                // Token list change is surely non-join since there is no TLLInfo
                tokenListChange = new TokenListChange<T>(etl);
            }
        }
        
        /**
         * Update token list(s) after added and removed embedded token lists
         * are known and in place.
         */
        void update() {
            TokenHierarchyEventInfo eventInfo = update.eventInfo;
            if (tokenListChange == null) { // Joined or unjoined ETLs
                assert (tokenListListUpdate != null);
                if (tokenListListUpdate.tokenListList.joinSections()) {
                    tokenListChange = tokenListListUpdate.createJoinTokenListChange();
                }
            } // else tokenListChange != null

            // Use always non-null List for added token lists
            if (tokenListListUpdate != null && tokenListListUpdate.addedTokenLists == null) {
                tokenListListUpdate.addedTokenLists = Collections.emptyList();
            }
            
            // Process the token list change by calling token list updater
            if (tokenListChange != null) { // Updating a concrete token list as a bounds change or joined change
                // Possibly mark that the parent change was bounds change which may affect
                // restoration of removed tokens in child ETLs
                tokenListChange.setParentChangeIsBoundsChange(parentItem != null &&
                        parentItem.tokenListChange != null && parentItem.tokenListChange.isBoundsChange());
                if (tokenListChange.getClass() == JoinTokenListChange.class) {
                    JoinTokenListChange<T> jChange = (JoinTokenListChange<T>) tokenListChange;
                    assert (tokenListListUpdate != null);
                    assert (tokenListListUpdate.modTokenListIndex != -1);
                    jChange.setTokenListListUpdate(tokenListListUpdate);
                    TokenListUpdater.updateJoined(jChange, eventInfo);

                } else { // non-joined update
                    TokenListUpdater.updateRegular(tokenListChange, eventInfo);
                    if (parentItem == null) {
                        eventInfo.setTokenChangeInfo(tokenListChange.tokenChangeInfo());
                    }
                }

                // Possibly process bounds change
                if (tokenListChange.isBoundsChange()) {
                    TokenListChange<T> change;
                    if (tokenListChange.getClass() == JoinTokenListChange.class) {
                        // Process the one embedded change
                        JoinTokenListChange<T> jChange = (JoinTokenListChange<T>) tokenListChange;
                        assert (jChange.relexChanges().size() == 1);
                        change = jChange.relexChanges().get(0);
                    } else {
                        change = tokenListChange;
                    }
                    Object attemptEmbeddingLanguages = processBoundsChange(change);
                    if (attemptEmbeddingLanguages != null) {
                        collectAddedEmbeddings(change, attemptEmbeddingLanguages);
                    }
        
                } else { // Non-bounds change
                    // Mark changed area based on start of first mod.token and end of last mod.token
                    // of the root-level change
                    eventInfo.setMinAffectedStartOffset(tokenListChange.offset());
                    eventInfo.setMaxAffectedEndOffset(tokenListChange.addedEndOffset());
                    if (childrenLanguages != null) { // If there are any possible embedded changes with TokenListList
                        if (tokenListChange.getClass() == JoinTokenListChange.class) {
                            JoinTokenListChange<T> jChange = (JoinTokenListChange<T>) tokenListChange;
                            jChange.collectAddedRemovedEmbeddings(this);
                        } else { // Regular change
                            collectRemovedEmbeddings(tokenListChange);
                            collectAddedEmbeddings(tokenListChange);
                        }
                    } // else: there is no embedding with TLL; existing ETLs will be abandoned; new one created on demand
                }

            } else if (tokenListListUpdate != null) { // Only service added/removed ETLs
                tokenListListUpdate.replaceTokenLists();
                tokenListListUpdate.collectRemovedEmbeddings(this);
                tokenListListUpdate.collectAddedEmbeddings(this);
            }

            // Add an embedded change to the parent change (if exists)
            // tokenListChange may be null in case there are added/removed ETLs
            // in a non-joined TLL. In that case take its parent's change
            if (parentItem != null && tokenListChange != null) {
                UpdateItem<?> parent = parentItem;
                while (parent.tokenListChange == null) {
                    parent = parent.parentItem;
                }
                assert (parent != null) : "No valid tokenListChange";
                parent.tokenListChange.tokenChangeInfo().addEmbeddedChange(tokenListChange.tokenChangeInfo());
            }
        }

        /**
         * Process a change where just a single token was relexed and it's the same
         * just with updated bounds.
         *
         * @param change non-null change describing the change.
         * @return children languages (possibly differs from childrenLanguages)
         */
        Object processBoundsChange(TokenListChange<T> change) {
            // Set of embeddings that will be attempted to be created.
            // For example for "a%" there is a PERCENTS token but there can't be embedding
            // because there must be two skip chars (assumed "%something%").
            // Once "%" is typed there should be an empty embedding for "%%" created.
            Object attemptEmbeddingLanguages = childrenLanguages; // null or language or Language[]
            // Go through all embedded list in a chain and check whether the embeddings are OK
            TokenList<T> removedTokenList = change.tokenChangeInfo().removedTokenList();
            TokenOrEmbedding<T> t = removedTokenList.tokenOrEmbedding(0);
            // Token's offset should be retained
            int tokenStartOffset = removedTokenList.tokenOffset(t.token());
            EmbeddedTokenList<T,?> etl = t.embedding();
            if (etl != null) { // The only removed token had embeddings
                // Rewrap token in etl - use the added token
                AbstractToken<T> addedToken = change.addedTokenOrEmbeddings().get(0).token();
                int rootModCount = etl.rootTokenList().modCount();
                etl.reinitChain(addedToken, tokenStartOffset, rootModCount);
                TokenList<T> tokenList = change.tokenList();
                int index = change.index();
                tokenList.setTokenOrEmbedding(index, etl);
                // Go through all ETLs and check whether chars in start/end skip lengths weren't modified
                // Check the text length beyond modification => end skip length must not be affected
                TokenHierarchyEventInfo eventInfo = update.eventInfo;
                int modRelOffset = eventInfo.modOffset() - change.offset();
                int beyondModLength = change.addedEndOffset() - (eventInfo.modOffset() + eventInfo.diffLengthOrZero());
                EmbeddedTokenList<T,?> prevEtl = null;
                do {
                    // Check whether chars in start/end skip lengths weren't modified
                    if (processBoundsChangeEmbeddedTokenList(etl, modRelOffset, beyondModLength)) { // Embedding saved -> proceed to next ETL
                        // In fact all the ETLs that remain should logically be excluded from attempt
                        // for embedding creation. The removed ETLs should remain among attempted.
                        if (attemptEmbeddingLanguages != null) {
                            Language<?> lang = etl.language();
                            if (LexerUtilsConstants.languageOrArrayContains(attemptEmbeddingLanguages, lang)) {
                                if (LexerUtilsConstants.languageOrArraySize(attemptEmbeddingLanguages) == 1) { // single and contained
                                    attemptEmbeddingLanguages = null;
                                } else { // Multiple langs
                                    attemptEmbeddingLanguages = LexerUtilsConstants.languageOrArrayRemove(
                                            attemptEmbeddingLanguages, lang);
                                }
                            } // else - lang not contained in attemptEmbeddingLanguages
                        }
                        // Process the next ETL
                        prevEtl = etl;
                        etl = prevEtl.nextEmbeddedTokenList();
                    } else { // Drop etl
                        EmbeddedTokenList<T,?> next = etl.nextEmbeddedTokenList();
                        if (prevEtl == null) { // etl is first one
                            if (next == null) {
                                tokenList.setTokenOrEmbedding(index, addedToken);
                            } else {
                                tokenList.setTokenOrEmbedding(index, next);
                            }
                        } else {
                            prevEtl.setNextEmbeddedTokenList(next);
                        }
                        etl.setNextEmbeddedTokenList(null);
                        etl = next;
                        // Removal of children is done in processBoundsChangeEmbeddedTokenList()
                    }
                } while (etl != null);
                return attemptEmbeddingLanguages;
            }
            return childrenLanguages;
        }

        /**
         * This method is extracted from processBoundsChangeEmbeddings() to allow a separate generification
         * for each ETL contained in an EC.
         * 
         * @param etl
         * @param ecTokenChange change for token wrapped by EC in which the ETL is hosted.
         * @param hasChildren
         * @param modRelOffset
         * @param beyondModLength
         * @return true if the embedding should be saved or false if it should be removed.
         */
        private <ET extends TokenId> boolean processBoundsChangeEmbeddedTokenList(
                EmbeddedTokenList<?,ET> etl, int modRelOffset, int beyondModLength
        ) {
            UpdateItem<ET> childItem = (childrenLanguages != null)
                    ? update.<ET>tokenListListItem(etl.languagePath())
                    : null;
            // Check whether the change was not in the start or end skip lengths
            // and if so then remove the embedding
            if (modRelOffset >= etl.languageEmbedding().startSkipLength() && beyondModLength >= etl.languageEmbedding().endSkipLength()) {
                // Modification within embedding's bounds => embedding can stay
                // Embedding will be updated once the level gets processed
                if (childItem == null) {
                    childItem = new UpdateItem<ET>(update, null, null);
                    int level = etl.languagePath().size() - 1;
                    update.addItem(childItem, level);
                } else { // TokenListList exists - item already added
                    // Mark a bounds change
                    childItem.tokenListListUpdate.markChangedMember(etl);
                }
                childItem.setParentItem(this);
                childItem.initTokenListChange(etl);
                return true; // Embedding saved -> proceed with next

            } else { // Mod in start/stop skip length => Remove the etl from chain
                if (childItem != null) {
                    // update-status already done as part of rewrap-token
                    childItem.tokenListListUpdate.markRemovedMember(etl, update.eventInfo);
                }
                // Signal to remove embedding
                return false;
            }
        }

        void collectRemovedEmbeddings(TokenListChange<?> change) {
            // Only called when tll children exist
            // First collect the removed embeddings
            TokenList<?> removedTokenList = change.tokenChangeInfo().removedTokenList();
            if (removedTokenList != null) {
                collectRemovedEmbeddings(removedTokenList);
            }
        }

        /**
         * Collect removed embeddings for the given token list recursively
         * and nest deep enough for all maintained children
         * token list lists.
         */
        void collectRemovedEmbeddings(TokenList<?> removedTokenList) {
            int tokenCount = removedTokenList.tokenCountCurrent();
            for (int i = 0; i < tokenCount; i++) { // Must go from first to last
                EmbeddedTokenList<?,?> etl = removedTokenList.tokenOrEmbedding(i).embedding();
                if (etl != null) {
                    do {
                        int rootModCount = etl.rootTokenList().modCount();
                        etl.updateModCount(rootModCount);
                        internalMarkRemovedMember(etl);
                        etl = etl.nextEmbeddedTokenList();
                    } while (etl != null);
                }
            }
        }

        void collectAddedEmbeddings(TokenListChange<?> change) {
            collectAddedEmbeddings(change, childrenLanguages);
        }

        void collectAddedEmbeddings(TokenListChange<?> change, Object attemptLanguages) {
            // Now collect added embeddings
            TokenList<?> currentTokenList = change.tokenList();
            collectAddedEmbeddings(currentTokenList, change.index(), change.addedTokenOrEmbeddingsCount(), attemptLanguages);
        }

        void collectAddedEmbeddings(TokenList<?> tokenList, int index, int addedCount, Object attemptLanguages) {
            int attemptLanguagesSize = LexerUtilsConstants.languageOrArraySize(attemptLanguages);
            if (attemptLanguagesSize > 0) {
                for (int i = 0; i < addedCount; i++) {
                    for (int j = 0; j < attemptLanguagesSize; j++) {
                        Language attemptLanguage = LexerUtilsConstants.languageOrArrayGet(attemptLanguages, j);
                        EmbeddedTokenList<?,?> etl = EmbeddingOperation.embeddedTokenList(tokenList, index + i, attemptLanguage, false);
                        if (etl != null) {
                            internalMarkAddedMember(etl);
                        }
                    }
                }
            }
        }

        /**
         * This code is extracted from collectRemovedEmbeddings() for convenient generification
         * over a type ET.
         */
        private <ET extends TokenId> void internalMarkRemovedMember(EmbeddedTokenList<?,ET> etl) {
            UpdateItem<ET> item = update.tokenListListItem(etl.languagePath());
            if (item != null) {
                // update-status called in caller
                item.tokenListListUpdate.markRemovedMember(etl, update.eventInfo);
                if (item.parentItem == null) {
                    item.setParentItem(this);
                }
            }
        }

        /**
         * This code is extracted from collectAddedEmbeddings() for convenient generification
         * over a type ET.
         */
        private <ET extends TokenId> void internalMarkAddedMember(EmbeddedTokenList<?,ET> etl) {
            UpdateItem<ET> item = update.tokenListListItem(etl.languagePath());
            if (item != null) {
                // update-status called in caller
                item.tokenListListUpdate.markAddedMember(etl);
                if (item.parentItem == null) {
                    item.setParentItem(this);
                }
            }
        }

    }

}
