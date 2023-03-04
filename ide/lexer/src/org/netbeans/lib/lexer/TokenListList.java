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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;

/**
 * List of token lists that collects all token lists for a given language path.
 * <br/>
 * There can be both lists with/without joining of the embedded sections.
 * Non-joining TLL gets created when someone asks for TokenHierarchy.tokenSequenceList().
 * Joining TLL gets created if any of the embeddings for the particular language path
 * has LanguageEmbedding.joinSections() set to true.
 * <br/>
 * Initial implementation attempted to initialize the list of token lists lazily
 * upon asking for it by client. However there was a problem with fixing
 * of token list explorers' state when the list is partially initialized
 * and there is an update of the token hierarchy. Sometimes there were inconsistencies
 * that a particular token list appeared twice in the token list list.
 * <br/>
 * Current impl is non-lazy so once the list becomes created it gets fully initialized
 * by traversing the parent token lists's tokens for embeddings of the particular language.
 * <br/>
 * Advantages:
 * <ul>
 *   <li> Easier updating - no issues with incomplete exploration.
 *   <li> More errorsafe approach with joinSections - if any of the scanned lists is joinSections
 *        then the whole token list list becomes joinSections from the begining.
 *   <li> It's disputable how much time the lazy impl has been saving.
 *   <li> More deterministic behavior - helps to diagnose errors.
 * </ul>
 * 
 * <p>
 * GapList is used for faster updates and there can be either single top-level
 * non-EmbeddedTokenList token list or zero or more nested EmbeddedTokenList(s).
 * </p>
 * 
 * <p>
 * joinSections approach:
 * <br/>
 * Non-joining embedded token lists' contents will be lexed without token list list assistance.
 * <br/>
 * JoinTokenList deals with sections joining.
 * </p>
 *
 * @author Miloslav Metelka
 */

public final class TokenListList<T extends TokenId> extends GapList<EmbeddedTokenList<?,T>> {

    private final TokenList<?> rootTokenList;

    private final LanguagePath languagePath;

    private boolean joinSections;

    private Object childrenLanguages; // null or language or Language[]
    
    private JoinTokenList<T> joinTokenList;
    

    public TokenListList(TokenList<?> rootTokenList, LanguagePath languagePath) {
        super(4);
        this.rootTokenList = rootTokenList;
        this.languagePath = languagePath;
        childrenLanguages = null;

        // languagePath has size >= 2
        assert (languagePath.size() >= 2) : "Cannot create TLL for languagePath=" + languagePath; // NOI18N
        Language<T> language = LexerUtilsConstants.innerLanguage(languagePath);
        if (languagePath.size() > 2) {
            TokenListList<?> parentTokenList = rootTokenList.tokenHierarchyOperation().tokenListList(languagePath.parent());
            for (int parentIndex = 0; parentIndex < parentTokenList.size(); parentIndex++) {
                TokenList<?> tokenList = parentTokenList.get(parentIndex);
                scanTokenList(tokenList, language);
            }
        } else { // Parent is root token list
            scanTokenList(rootTokenList, language);
        }
        
        checkCreateJoinTokenList();
        if (joinTokenList == null) {
            // Init individual lists if they are not yet.
            // TLL may be created later once someone calls e.g. TH.tokenSequenceList() but
            // individual non-joined ETLs might already been asked and populated with tokens.
            for (EmbeddedTokenList<?,T> etl : this) {
                assert (!etl.languageEmbedding().joinSections());
                // Here some ETLs might already be initialized and some not.
                // There is no extra flag whether tokens were inited or not.
                // Therefore check for token-count and possibly offset span.
                if (etl.tokenCountCurrent() == 0 && etl.textLength() > 0) {
                    etl.initAllTokens();
                }
            }
        }
    }
    
    private void scanTokenList(TokenList<?> tokenList, Language<T> language) {
        int tokenCount = tokenList.tokenCount();
        for (int i = 0; i < tokenCount; i++) {
            // Check for embedded token list of the given language
            // Do not initialize tokens of ETLs - do so because
            // there might be just few joinSections ETLs and rest non-joining 
            // and then the whole TLL should become joining so avoid extra work
            // that could be thrown away.
            EmbeddedTokenList<?,T> etl = EmbeddingOperation.embeddedTokenList(tokenList, i, language, false);
            if (etl != null) {
                add(etl);
                if (etl.languageEmbedding().joinSections()) {
                    this.joinSections = true;
                }
            }
        }
    }
    
    public JoinTokenList<T> joinTokenList() {
        return joinTokenList;
    }
    
    public void checkCreateJoinTokenList() {
        if (joinSections && joinTokenList == null) {
            joinTokenList = JoinTokenList.create(this);
        }
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    /**
     * Return true if this list is mandatorily updated because there is
     * one or more embeddings that join sections.
     */
    public boolean joinSections() {
        return joinSections;
    }
    
    public void setJoinSections(boolean joinSections) {
        this.joinSections = joinSections;
    }
    
    public void notifyChildAdded(Language<?> language) {
        childrenLanguages = LexerUtilsConstants.languageOrArrayAdd(childrenLanguages, language);
    }
    
    public void notifyChildRemoved(Language<?> language) {
        childrenLanguages = LexerUtilsConstants.languageOrArrayRemove(childrenLanguages, language);
    }
    
    public boolean hasChildren() {
        return (childrenLanguages != null);
    }

    public Object childrenLanguages() {
        return childrenLanguages;
    }

    /**
     * Return a valid token list or null if the index is too high.
     */
    public EmbeddedTokenList<?,T> getOrNull(int index) {
        return (index < size()) ? get(index) : null;
    }
    
    private static final EmbeddedTokenList<?,?>[] EMPTY_TOKEN_LIST_ARRAY = new EmbeddedTokenList<?,?>[0];

    public EmbeddedTokenList<?,T>[] replace(int index, int removeTokenListCount, List<EmbeddedTokenList<?,T>> addTokenLists) {
        @SuppressWarnings("unchecked")
        EmbeddedTokenList<?,T>[] removed = (removeTokenListCount > 0)
                ? (EmbeddedTokenList<?,T>[]) new EmbeddedTokenList[removeTokenListCount]
                : (EmbeddedTokenList<?,T>[]) EMPTY_TOKEN_LIST_ARRAY;
        if (removeTokenListCount > 0) {
            copyElements(index, index + removeTokenListCount, removed, 0);
            remove(index, removeTokenListCount);
        }
        addAll(index, addTokenLists);
        return removed;
    }

    public TokenList<?> rootTokenList() {
        return rootTokenList;
    }

    void childAdded() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public int findIndex(int offset) {
        int high = size() - 1;
        int low = 0;
        int rootModCount = rootTokenList.modCount();
        while (low <= high) {
            int mid = (low + high) >>> 1;
            EmbeddedTokenList<?,T> etl = get(mid);
            // Ensure that the startOffset() will be updated
            etl.updateModCount(rootModCount);
            int cmp = etl.startOffset() - offset;
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else { // cmp == 0 -> take the previous one
                low = mid;
                break;
            }
        }
        return low;
    }
    
    /**
     * Find an index during update of the token list.
     * <br>
     * If there was a removal performed and some of the contained token lists
     * were removed then these TLs then the token lists beyond the modification point
     * will be forced to update itself which may 
     */
    public int findIndexDuringUpdate(EmbeddedTokenList<?,T> targetEtl, TokenHierarchyEventInfo eventInfo) {
        int high = size() - 1;
        int low = 0;
        int rootModCount = rootTokenList.modCount();
        int targetStartOffset = LexerUtilsConstants.updatedStartOffset(targetEtl, rootModCount, eventInfo);
        while (low <= high) {
            int mid = (low + high) >>> 1;
            EmbeddedTokenList<?,T> etl = get(mid);
            // Ensure that the startOffset() will be updated
            int startOffset = LexerUtilsConstants.updatedStartOffset(etl, rootModCount, eventInfo);
            int cmp = startOffset - targetStartOffset;
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else {
                low = mid;
                // Now it's also possible that there was a larger remove when multiple token lists
                // inside the removed area were removed and they all have startOffset being modOffset.
                // In such case these need to be searched by linear search in both directions
                // from the found one.
                if (etl != targetEtl) {
                    while (--low >= 0) {
                        etl = get(low);
                        if (etl == targetEtl) { // Quick check for match
                            return low;
                        }
                        // Check whether this was appropriate attempt for match
                        if (LexerUtilsConstants.updatedStartOffset(etl, rootModCount, eventInfo) != targetStartOffset)
                            break;
                    }
                    
                    // Go up from mid
                    low = mid;
                    while (++low < size()) {
                        etl = get(low);
                        if (etl == targetEtl) { // Quick check for match
                            return low;
                        }
                        // Check whether this was appropriate attempt for match
                        if (LexerUtilsConstants.updatedStartOffset(etl, rootModCount, eventInfo) != targetStartOffset)
                            break;
                    }
                }
                break;
            }
        }
        return low;
    }
    
    public String checkConsistency() {
        // Check whether the token lists are in a right order
        int lastEndOffset = 0;
        for (int i = 0; i < size(); i++) {
            EmbeddedTokenList<?,T> etl = get(i);
            etl.updateModCount();
            if (etl.isRemoved()) {
                return "TOKEN-LIST-LIST Removed token list at index=" + i + '\n' + this;
            }
            if (etl.startOffset() < lastEndOffset) {
                return "TOKEN-LIST-LIST Invalid start offset at index=" + i +
                        ": etl[" + i + "].startOffset()=" + etl.startOffset() +
                        " < lastEndOffset=" + lastEndOffset +
                        "\n" + this;
            }
            if (etl.startOffset() > etl.endOffset()) {
                return "TOKEN-LIST-LIST Invalid end offset at index=" + i +
                        ": etl[" + i + "].startOffset()=" + etl.startOffset() +
                        " > etl[" + i + "].endOffset()="+ etl.endOffset() +
                        "\n" + this;
            }
            if (etl.isRemoved()) {
                return "TOKEN-LIST-LIST Removed ec at index=" + i + "\n" + this;
            }
            lastEndOffset = etl.endOffset();
        }
        if (joinSections() && size() > 0) {
            return joinTokenList().checkConsistency();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(2048);
        if (joinSections()) {
            sb.append("J");
        }
        sb.append("TLL for \"");
        sb.append(languagePath().mimePath()).append('"');
        if (hasChildren()) {
            sb.append(", hasChildren");
        }
        sb.append('\n');
        int digitCount = ArrayUtilities.digitCount(size());
        for (int i = 0; i < size(); i++) {
            EmbeddedTokenList<?,T> etl = get(i);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            etl.updateModCount();
            etl.dumpInfo(sb);
            sb.append('\n');
//            LexerUtilsConstants.appendTokenListIndented(sb, etl, 4);
        }
        return sb.toString();
    }

}
