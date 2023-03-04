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
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.lib.editor.util.FlyOffsetGapList;
import org.netbeans.lib.lexer.inc.MutableTokenList;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.lib.lexer.inc.TokenListChange;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.lib.lexer.token.PartToken;
import org.netbeans.lib.lexer.token.TextToken;


/**
 * Embedded token list maintains a list of tokens
 * on a particular embedded language level .
 * <br>
 * The physical storage contains a gap to speed up list modifications
 * during typing in a document when tokens are typically added/removed
 * at the same index in the list.
 *
 * <p>
 * There is an intent to not degrade performance significantly
 * with each extra language embedding level so the token list maintains direct
 * link to the root level.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class EmbeddedTokenList<T extends TokenId, ET extends TokenId>
extends FlyOffsetGapList<TokenOrEmbedding<ET>> implements MutableTokenList<ET>, TokenOrEmbedding<T>
{
    
    /**
     * Token wrapped by this ETL.
     */
    private AbstractToken<T> branchToken; // 36 bytes (32=super + 4)
    
    /**
     * Cached start offset of the token for which this embedding container
     * was created.
     * <br/>
     * Its value may be shared by multiple embedded token lists.
     */
    private int branchTokenStartOffset; // 40 bytes

    /**
     * Root token list of the hierarchy should never be null and is final.
     * 
     */
    private final TokenList<?> rootTokenList; // 44 bytes
    
    /**
     * Cached modification count from root token list allows to determine whether the start offset
     * of branch token needs to be recomputed.
     */
    private int rootModCount; // 48 bytes

    /**
     * Additional mod count added to the modCount variable when expressing modCount
     * of this token list.
     * It is incremented when
     * 1) token(s) are being replaced in ETL due to modification inside branchToken
     *    (when branchToken is retained).
     * 2) join token list relexes due to modification.
     * 3) join token list relexes due to custom embedding creation.
     */
    private int extraModCount; // 52 bytes

    /**
     * Language embedding for this embedded token list.
     */
    private final LanguageEmbedding<ET> embedding; // 56 bytes
    
    /**
     * Language path of this token list.
     */
    private final LanguagePath languagePath; // 60 bytes
    
    /**
     * Storage for lookaheads and states.
     * <br/>
     * It's non-null only initialized for mutable token lists
     * or when in testing environment.
     */
    private LAState laState; // 64 bytes
    
    /**
     * Next embedded token list forming a single-linked list.
     */
    private EmbeddedTokenList<T,?> nextEmbeddedTokenList; // 68 bytes
    
    /**
     * Additional information in case this ETL is contained in a JoinTokenList.
     * <br/>
     * Through this info a reference to the JoinTokenList is held. There is no other
     * indexed structure so the EmbeddedTokenList members of TokenListList
     * must be binary-searched.
     */
    private EmbeddedJoinInfo<ET> joinInfo; // 72 bytes

    
    public EmbeddedTokenList(TokenList<?> rootTokenList,
            LanguagePath languagePath, LanguageEmbedding<ET> embedding
    ) {
        super(1); // Suitable for adding join-token parts
        this.rootTokenList = rootTokenList;
        this.languagePath = languagePath;
        this.embedding = embedding;
        initLAState();
    }

    void reinit(AbstractToken<T> branchToken, int branchTokenStartOffset, int modCount) {
        this.branchToken = branchToken;
        this.branchTokenStartOffset = branchTokenStartOffset;
        this.rootModCount = modCount;
    }

    public void reinitChain(AbstractToken<T> branchToken, int branchTokenStartOffset, int modCount) {
        reinit(branchToken, branchTokenStartOffset, modCount);
        if (nextEmbeddedTokenList != null) {
            nextEmbeddedTokenList.reinitChain(branchToken, branchTokenStartOffset, modCount);
        }
    }

    public void initAllTokens() {
        assert (!embedding.joinSections()) : "Cannot init all tokens since ETL joins sections\n" + // NOI18N
                this + '\n' + dumpRelatedTLL();
//        initLAState();
        // Lex the whole input represented by token at once
        LexerInputOperation<ET> lexerInputOperation = createLexerInputOperation(
                0, startOffset(), null);
        AbstractToken<ET> token = lexerInputOperation.nextToken();
        while (token != null) {
            addToken(token, lexerInputOperation);
            token = lexerInputOperation.nextToken();
        }
        lexerInputOperation.release();
        trimStorageToSize();
    }

    private void initLAState() {
        this.laState = (rootModCount!= LexerUtilsConstants.MOD_COUNT_IMMUTABLE_INPUT || TokenList.LOG.isLoggable(Level.FINE))
                ? LAState.empty() // Will collect LAState
                : null;
    }
    
    public void clearAllTokens() {
        this.clear();
        if (laState != null) {
            laState = LAState.empty();
        }
    }

    /**
     * Add token without touching laState - suitable for JoinToken's handling.
     *
     * @param token non-null token
     */
    public void addToken(AbstractToken<ET> token) {
        if (!token.isFlyweight())
            token.setTokenList(this);
        updateElementOffsetAdd(token); // must subtract startOffset()
        add(token);
    }

    public void addToken(AbstractToken<ET> token, LexerInputOperation<ET> lexerInputOperation) {
        addToken(token);
        if (laState != null) { // maintaining lookaheads and states
            // Only get LA and state when necessary (especially lexerState() may be costly)
            laState = laState.add(lexerInputOperation.lookahead(), lexerInputOperation.lexerState());
        }
    }

    /**
     * Used when dealing with PartToken instances.
     */
    public void addToken(AbstractToken<ET> token, int lookahead, Object state) {
        addToken(token);
        if (laState != null) { // maintaining lookaheads and states
            laState = laState.add(lookahead, state);
        }
    }

    public void trimStorageToSize() {
        trimToSize(); // Compact storage
        if (laState != null)
            laState.trimToSize();
    }
    
    public EmbeddedTokenList<T,?> nextEmbeddedTokenList() {
        return nextEmbeddedTokenList;
    }
    
    public void setNextEmbeddedTokenList(EmbeddedTokenList<T,?> nextEmbeddedTokenList) {
        this.nextEmbeddedTokenList = nextEmbeddedTokenList;
    }

    @Override
    public Language<ET> language() {
        return embedding.language(); // Same as languagePath.innerLanguage()
    }
    
    @Override
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public LanguageEmbedding languageEmbedding() {
        return embedding;
    }

    @Override
    public int tokenCount() {
        return tokenCountCurrent();
    }
    
    @Override
    public int tokenCountCurrent() {
        return size();
    }
    
    public EmbeddedJoinInfo<ET> joinInfo() {
        return joinInfo;
    }
    
    public void setJoinInfo(EmbeddedJoinInfo<ET> joinInfo) {
        this.joinInfo = joinInfo;
    }

    public int joinTokenCount() {
        int tokenCount = tokenCountCurrent();
        if (tokenCount > 0 && joinInfo.joinTokenLastPartShift() > 0)
            tokenCount--;
        return tokenCount;
    }

    public boolean joinBackward() {
        if (tokenCountCurrent() > 0) {
            AbstractToken<ET> token = tokenOrEmbeddingDirect(0).token();
            return (token.getClass() == PartToken.class) &&
                    ((PartToken<ET>)token).partTokenIndex() > 0;
        } else { // tokenCount == 0
            return (joinInfo.joinTokenLastPartShift() > 0);
        }
    }

    @Override
    public TokenOrEmbedding<ET> tokenOrEmbedding(int index) {
        return (index < size()) ? get(index) : null;
    }

    @Override
    public AbstractToken<T> token() {
        return branchToken;
    }

    @Override
    public EmbeddedTokenList<T,ET> embedding() {
        return this;
    }
    
    @Override
    public int lookahead(int index) {
        return (laState != null) ? laState.lookahead(index) : -1;
    }

    @Override
    public Object state(int index) {
        return (laState != null) ? laState.state(index) : null;
    }

    /**
     * Returns absolute offset of the token at the given index
     * (startOffset gets added to the child token's real offset).
     * <br/>
     * For token hierarchy snapshots the returned value is corrected
     * in the TokenSequence explicitly by adding TokenSequence.tokenOffsetDiff.
     */
    @Override
    public int tokenOffset(int index) {
        return elementOffset(index);
    }

    @Override
    public int tokenOffset(AbstractToken<ET> token) {
        if (token.getClass() == JoinToken.class) {
            return token.offset(null);
        }
        int rawOffset = token.rawOffset();
//        embeddingContainer().checkStatusUpdated();
        int relOffset = (rawOffset < offsetGapStart())
                ? rawOffset
                : rawOffset - offsetGapLength();
        return startOffset() + relOffset;
    }

    @Override
    public int[] tokenIndex(int offset) {
        return LexerUtilsConstants.tokenIndexBinSearch(this, offset, tokenCountCurrent());
    }

    int branchTokenStartOffset() {
        return branchTokenStartOffset;
    }

    @Override
    public int modCount() {
        return rootModCount + extraModCount;
    }
    
    public void updateModCount() {
        updateModCount(rootTokenList.modCount());
    }

    public void updateModCount(int rootModCount) {
        if (this.rootModCount != LexerUtilsConstants.MOD_COUNT_REMOVED) {
            if (rootModCount != this.rootModCount) {
                this.rootModCount = rootModCount;
                TokenList<T> parentTokenList = branchToken.tokenList();
                if (parentTokenList.getClass() == EmbeddedTokenList.class) { // deeper level embedding
                    ((EmbeddedTokenList<T,?>) parentTokenList).updateModCount(rootModCount);
                }                    
                branchTokenStartOffset = parentTokenList.tokenOffset(branchToken);
            }
        }
    }

    public void incrementExtraModCount() {
        extraModCount++;
    }

    /**
     * Mark removed without changing branchTokenStartOffset.
     */
    public void markRemoved() {
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("ETL.markRemoved(): ETL-");
            LexerUtilsConstants.appendIdentityHashCode(sb, this);
            sb.append('\n');
            LOG.fine(sb.toString());
            if (LOG.isLoggable(Level.FINER)) { // Include stack trace of the removal marking
                LOG.log(Level.INFO, "Embedded token list marked as removed:", new Exception());
            }
        }
        rootModCount = LexerUtilsConstants.MOD_COUNT_REMOVED;
        extraModCount = 0; // Ensure that modCount() will return MOD_COUNT_REMOVED
    }
    
    /**
     * Mark removed and explicitly set branchTokenStartOffset.
     */
    public void markRemoved(int branchTokenStartOffset) {
        this.branchTokenStartOffset = branchTokenStartOffset;
        markRemoved();
    }

    public void markRemovedChain(int branchTokenStartOffset) {
        markRemoved(branchTokenStartOffset);
        if (nextEmbeddedTokenList != null) {
            nextEmbeddedTokenList.markRemoved(branchTokenStartOffset);
        }
    }

    @Override
    public int startOffset() { // used by FlyOffsetGapList
        return branchTokenStartOffset + embedding.startSkipLength();
    }
    
    @Override
    public int endOffset() {
        return branchTokenStartOffset + branchToken.length() - embedding.endSkipLength();
    }
    
    public int textLength() {
        return branchToken.length() - embedding.startSkipLength() - embedding.endSkipLength();
    }
    
    @Override
    public boolean isRemoved() { // Expects modCount up-to-date
        return (rootModCount == LexerUtilsConstants.MOD_COUNT_REMOVED);
    }

    @Override
    public TokenList<?> rootTokenList() {
        return rootTokenList;
    }

    @Override
    public CharSequence inputSourceText() {
        return rootTokenList.inputSourceText();
    }

    @Override
    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return rootTokenList.tokenHierarchyOperation();
    }
    
    @Override
    protected int elementRawOffset(TokenOrEmbedding<ET> elem) {
        return elem.token().rawOffset();
    }

    @Override
    protected void setElementRawOffset(TokenOrEmbedding<ET> elem, int rawOffset) {
        elem.token().setRawOffset(rawOffset);
    }
    
    @Override
    protected boolean isElementFlyweight(TokenOrEmbedding<ET> elem) {
        return elem.token().isFlyweight();
    }
    
    @Override
    protected int elementLength(TokenOrEmbedding<ET> elem) {
        return elem.token().length();
    }
    
    @Override
    public AbstractToken<ET> replaceFlyToken(
    int index, AbstractToken<ET> flyToken, int offset) {
        TextToken<ET> nonFlyToken = ((TextToken<ET>)flyToken).createCopy(this, offset2Raw(offset));
        set(index, nonFlyToken);
        return nonFlyToken;
    }

    @Override
    public void setTokenOrEmbedding(int index, TokenOrEmbedding<ET> t) {
        set(index, t);
    }

    @Override
    public InputAttributes inputAttributes() {
        return rootTokenList.inputAttributes();
    }

    // MutableTokenList extra methods
    @Override
    public TokenOrEmbedding<ET> tokenOrEmbeddingDirect(int index) {
        return get(index);
    }

    @Override
    public LexerInputOperation<ET> createLexerInputOperation(
    int tokenIndex, int relexOffset, Object relexState) {
        int endOffset = endOffset();
        assert isModCountUpdated() : modCountErrorInfo();
//        assert (!branchToken.isRemoved()) : "No lexing when token is removed";
//        assert (relexOffset >= startOffset()) : "Invalid relexOffset=" + relexOffset + " < startOffset()=" + startOffset();
        assert (relexOffset <= endOffset) : "Invalid relexOffset=" + relexOffset + " > endOffset()=" + endOffset;
        return new TextLexerInputOperation<ET>(this, tokenIndex, relexState, relexOffset, endOffset);
    }

    @Override
    public boolean isFullyLexed() {
        return true;
    }

    @Override
    public void replaceTokens(TokenListChange<ET> change, TokenHierarchyEventInfo eventInfo, boolean modInside) {
        assert isModCountUpdated() : modCountErrorInfo();
        // Increase the extraModCount which helps to invalidate token-sequence in case
        // when an explicit embedding was created in join-sections setup which can affect adjacent ETLs.
        incrementExtraModCount();
        int index = change.index();
        // Remove obsolete tokens (original offsets are retained)
        int removedTokenCount = change.removedTokenCount();
        AbstractToken<ET> firstRemovedToken = null;
        if (removedTokenCount > 0) {
            @SuppressWarnings("unchecked")
            TokenOrEmbedding<ET>[] removedTokensOrEmbeddings = new TokenOrEmbedding[removedTokenCount];
            copyElements(index, index + removedTokenCount, removedTokensOrEmbeddings, 0);
            firstRemovedToken = removedTokensOrEmbeddings[0].token();
            // Here a possible offset correction of the removed tokens needs to be made.
            // For example have a jsp with embedded html having embedded javascript.
            // It's possible that there will be a remove after which an html section
            // will stay but its embedded JS will be removed. When processing JS the outer html token
            // already holds the updated offset so when removing JS tokens they will have incorrect (lower) offsets.
            // This would lead to situation when the JS removed ETLs would have non-ordered offsets within TLL
            // that would make binary search impossible. So the removed tokens must have retained offsets
            // upon removal.
            // The rule is that if the parent EC is still part of token hierarchy i.e. the parent token was just possibly moved
            // then if the parent's offset is above or equal to modOffset+insertedLength (offset was already updated by diffLength)
            // then all the removed tokens from ETL will be corrected back by -diffLength.
            //
            // However the condition should not apply in case of bounds-change in parents.
            // For example if there's a big html token at offset=0 (with several JS sections)
            // and a removal at offset=0. If the removal only means a bounds-change for the initial html token
            // then the html token will still be connected to the hierarchy (the EC will physically change)
            // and the token still starts at offset=0 so the previously mentioned condition would apply
            // and the tokens removed from a JS sections would have their offsets corrected which would be wrong.
            int removedOffsetShift = 0;
            if (!isRemoved()
                    && branchTokenStartOffset >= eventInfo.modOffset() + eventInfo.insertedLength()
                    && !change.parentChangeIsBoundsChange()
            ) {
                removedOffsetShift -= eventInfo.diffLength();
            }
            for (int i = 0; i < removedTokenCount; i++) {
                TokenOrEmbedding<ET> tokenOrEmbedding = removedTokensOrEmbeddings[i];
                // It's necessary to update-status of all removed tokens' contained embeddings
                // since otherwise (if they would not be up-to-date) they could not be updated later
                // as they lose their parent token list which the update-status relies on.
                AbstractToken<ET> token = tokenOrEmbedding.token();
                if (!token.isFlyweight()) {
                    updateElementOffsetRemove(token);
                    if (removedOffsetShift != 0) {
                        token.setRawOffset(token.rawOffset() + removedOffsetShift);
                    }
                    token.setTokenList(null);
                    EmbeddedTokenList<ET,?> etl = tokenOrEmbedding.embedding();
                    if (etl != null) {
                        etl.markRemovedChain(token.rawOffset());
                    }
                }
            }
            remove(index, removedTokenCount); // Retain original offsets
            laState.remove(index, removedTokenCount); // Remove lookaheads and states
            change.setRemovedTokens(removedTokensOrEmbeddings);
        } else {
            change.setRemovedTokensEmpty();
        }

        if (modInside) { // JoinTokenList may pass false if physical mod not contained in this ETL
            // Move and fix the gap according to the performed modification.
            // Instead of modOffset the gap is located at first relexed token's start
            // because then the already precomputed index corresponding to the given offset
            // can be reused. Otherwise there would have to be another binary search for index.
            int startOffset = startOffset(); // updateStatus() should already be called
            if (offsetGapStart() != change.offset() - startOffset) {
                // Minimum of the index of the first removed index and original computed index
                moveOffsetGap(change.offset() - startOffset, change.index());
            }
            updateOffsetGapLength(-eventInfo.diffLength());
        }

        // Add created tokens.
        // This should be called early when all the members are true tokens
        List<TokenOrEmbedding<ET>> addedTokenOrEmbeddings = change.addedTokenOrEmbeddings();
        if (addedTokenOrEmbeddings != null) {
            for (TokenOrEmbedding<ET> tokenOrEmbedding : addedTokenOrEmbeddings) {
                AbstractToken<ET> token = tokenOrEmbedding.token();
                if (!token.isFlyweight())
                    token.setTokenList(this);
                updateElementOffsetAdd(token);
            }
            addAll(index, addedTokenOrEmbeddings);
            laState = laState.addAll(index, change.laState());
            change.syncAddedTokenCount();
            // Check for bounds change only
            if (removedTokenCount == 1 && addedTokenOrEmbeddings.size() == 1) {
                // Compare removed and added token ids and part types
                AbstractToken<ET> addedToken = change.addedTokenOrEmbeddings().get(0).token();
                if (firstRemovedToken.id() == addedToken.id()
                    && firstRemovedToken.partType() == addedToken.partType()
                ) {
                    change.markBoundsChange();
                }
            }
        }
    }

    void markChildrenRemovedDeep() { // Used by custom embedding removal
        int rootModCount = rootTokenList.modCount();
        for (int i = tokenCountCurrent() - 1; i >= 0; i--) {
            EmbeddedTokenList<ET,?> etl = tokenOrEmbeddingDirect(i).embedding();
            if (etl != null) {
                etl.updateModCount(rootModCount);
                etl.markRemovedChain(etl.branchTokenStartOffset());
            }
        }
    }
    
    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public Set<ET> skipTokenIds() {
        return null;
    }
    
    /**
     * Check if this ETL is up-to-date at places where code expects it.
     * Method declared to return true so it can be used in assert stmts.
     */
    public boolean isModCountUpdated() {
        return (rootModCount == LexerUtilsConstants.MOD_COUNT_REMOVED
                || rootModCount == rootTokenList.modCount());
    }
    
    public String modCountErrorInfo() {
        return "!!!INTERNAL LEXER ERROR!!! Obsolete modCount in ETL " +
                this + "\nin token hierarchy\n" + rootTokenList.tokenHierarchyOperation();
        
    }

    @Override
    public StringBuilder dumpInfo(StringBuilder sb) {
        if (isRemoved()) {
            sb.append("REMOVED-");
        }
        sb.append(dumpInfoType()).append('@').append(Integer.toHexString(System.identityHashCode(this)));
        if (embedding.joinSections())
            sb.append('j');
        sb.append('<').append(startOffset());
        sb.append(",").append(endOffset());
        sb.append("> TC=").append(tokenCountCurrent());
        if (joinInfo != null) {
            sb.append("(").append(joinTokenCount()).append(')');
            sb.append(" JI:");
            joinInfo.dumpInfo(sb, this);
        }
        return sb;
    }

    @Override
    public String dumpInfoType() {
        return "ETL";
    }

    private String dumpRelatedTLL() {
        TokenListList<ET> tll = rootTokenList.tokenHierarchyOperation().existingTokenListList(languagePath);
        return (tll != null)
                ? tll.toString()
                : "<No TokenListList for " + languagePath.mimePath() + ">";
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this); // Was overriden by AbstractList
    }

    @Override
    public boolean equals(Object o) {
        return (this == o); // Was overriden by AbstractList
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        dumpInfo(sb);
        LexerUtilsConstants.appendTokenList(sb, this);
        return sb.toString();
    }

}
