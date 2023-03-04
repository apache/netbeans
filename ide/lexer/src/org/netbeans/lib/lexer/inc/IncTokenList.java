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

import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.lib.lexer.LAState;
import org.netbeans.lib.lexer.LexerSpiPackageAccessor;
import org.netbeans.lib.lexer.TextLexerInputOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.editor.util.FlyOffsetGapList;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;
import org.netbeans.lib.lexer.TokenOrEmbedding;
import org.netbeans.spi.lexer.MutableTextInput;


/**
 * Incremental token list maintains a list of tokens
 * at the root language level.
 * <br/>
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

public final class IncTokenList<T extends TokenId>
extends FlyOffsetGapList<TokenOrEmbedding<T>> implements MutableTokenList<T> {
    
    private final TokenHierarchyOperation<?,T> tokenHierarchyOperation;
    
    private Language<T> language;

    private LanguagePath languagePath;
    
    private CharSequence inputSourceText;
    
    /**
     * Lexer input operation used for lexing of the input.
     */
    private LexerInputOperation<T> lexerInputOperation;
    
    private int rootModCount;

    private LAState laState;

    public IncTokenList(TokenHierarchyOperation<?,T> tokenHierarchyOperation) {
        this.tokenHierarchyOperation = tokenHierarchyOperation;
        this.laState = LAState.empty();
    }
    
    /**
     * Activate this list internally if it's currently active (its languagePath() != null)
     * or deactivate if LP == null.
     */
    public void reinit() {
        if (languagePath != null) {
            MutableTextInput input = tokenHierarchyOperation.mutableTextInput();
            this.inputSourceText = LexerSpiPackageAccessor.get().text(input);
            this.lexerInputOperation = new TextLexerInputOperation<T>(this);
        } else {
            this.inputSourceText = null;
            releaseLexerInputOperation();
        }
    }
    
    public void releaseLexerInputOperation() {
        if (lexerInputOperation != null) {
            lexerInputOperation.release();
            lexerInputOperation = null;
        }
    }

    @Override
    public Language<T> language() {
        return language;
    }
    
    @Override
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public void setLanguagePath(LanguagePath languagePath) {
        this.languagePath = languagePath;
        this.language = (languagePath != null)
                ? LexerUtilsConstants.<T>innerLanguage(languagePath)
                : null;
    }

    @Override
    public int tokenCount() {
        if (lexerInputOperation != null) { // still lexing
            tokenOrEmbeddingImpl(Integer.MAX_VALUE);
        }
        return size();
    }

    @Override
    public int tokenOffset(AbstractToken<T> token) {
        int rawOffset = token.rawOffset();
        return (rawOffset < offsetGapStart()
                ? rawOffset
                : rawOffset - offsetGapLength());
    }

    @Override
    public int tokenOffset(int index) {
        return elementOffset(index);
    }
    
    @Override
    public int[] tokenIndex(int offset) {
        return LexerUtilsConstants.tokenIndexLazyTokenCreation(this, offset);
    }

    /**
     * Get modification count for which this token list was last updated
     * (mainly its cached start offset).
     */
    @Override
    public int modCount() {
        return rootModCount;
    }
    
    public void incrementModCount() {
        rootModCount++;
    }
    
    @Override
    public TokenOrEmbedding<T> tokenOrEmbedding(int index) {
        return tokenOrEmbeddingImpl(index);
    }
    
    private TokenOrEmbedding<T> tokenOrEmbeddingImpl(int index) {
        while (lexerInputOperation != null && index >= size()) {
            AbstractToken<T> token = lexerInputOperation.nextToken();
            if (token != null) { // lexer returned valid token
                if (!token.isFlyweight())
                    token.setTokenList(this);
                updateElementOffsetAdd(token);
                add(token);
                laState = laState.add(lexerInputOperation.lookahead(),
                        lexerInputOperation.lexerState());
            } else { // no more tokens from lexer
                releaseLexerInputOperation();
                trimToSize();
                laState.trimToSize();
            }
        }
        return (index < size()) ? get(index) : null;
    }
    
    @Override
    public AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        TextToken<T> nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset2Raw(offset));
        set(index, nonFlyToken);
        return nonFlyToken;
    }

    @Override
    public void setTokenOrEmbedding(int index, TokenOrEmbedding<T> t) {
        set(index, t);
    }

    @Override
    public InputAttributes inputAttributes() {
        return LexerSpiPackageAccessor.get().inputAttributes(tokenHierarchyOperation.mutableTextInput());
    }
    
    @Override
    protected int elementRawOffset(TokenOrEmbedding<T> elem) {
        return elem.token().rawOffset();
    }
 
    @Override
    protected void setElementRawOffset(TokenOrEmbedding<T> elem, int rawOffset) {
        elem.token().setRawOffset(rawOffset);
    }
    
    @Override
    protected boolean isElementFlyweight(TokenOrEmbedding<T> elem) {
        // token wrapper always contains non-flyweight token
        return (elem.embedding() == null)
            && elem.token().isFlyweight();
    }
    
    @Override
    protected int elementLength(TokenOrEmbedding<T> elem) {
        return elem.token().length();
    }
    
    @Override
    public TokenOrEmbedding<T> tokenOrEmbeddingDirect(int index) {
        // Solely for token list updater or token hierarchy snapshots
        // having single-threaded exclusive write access
        return get(index);
    }

    @Override
    public int lookahead(int index) {
        return laState.lookahead(index);
    }

    @Override
    public Object state(int index) {
        return laState.state(index);
    }

    @Override
    public int tokenCountCurrent() {
        return size();
    }

    @Override
    public TokenList<?> rootTokenList() {
        return this;
    }

    @Override
    public CharSequence inputSourceText() {
        return inputSourceText;
    }

    @Override
    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return tokenHierarchyOperation;
    }
    
    @Override
    public LexerInputOperation<T> createLexerInputOperation(
    int tokenIndex, int relexOffset, Object relexState) {
        // Possibly release unfinished lexing - will be restarted in replaceTokens()
        // Releasing the lexer now allows to share a single backing lexer's impl instance better.
        // Do not assign null to lexerInputOperation since the replaceTokens() would not know
        // that the lexing was unfinished.
        if (lexerInputOperation != null)
            lexerInputOperation.release();

        // Used for mutable lists only so maintain LA and state
        return new TextLexerInputOperation<T>(this, tokenIndex, relexState,
                relexOffset, inputSourceText.length());
    }

    @Override
    public boolean isFullyLexed() {
        return (lexerInputOperation == null);
    }

    @Override
    public void replaceTokens(TokenListChange<T> change, TokenHierarchyEventInfo eventInfo, boolean modInside) {
        int index = change.index();
        // Remove obsolete tokens (original offsets are retained)
        int removeTokenCount = change.removedTokenCount();
        AbstractToken<T> firstRemovedToken = null;
        if (removeTokenCount > 0) {
            @SuppressWarnings("unchecked")
            TokenOrEmbedding<T>[] removedTokensOrEmbeddings = new TokenOrEmbedding[removeTokenCount];
            copyElements(index, index + removeTokenCount, removedTokensOrEmbeddings, 0);
            firstRemovedToken = removedTokensOrEmbeddings[0].token();
            for (int i = 0; i < removeTokenCount; i++) {
                TokenOrEmbedding<T> tokenOrEmbedding = removedTokensOrEmbeddings[i];
                // It's necessary to update-status of all removed tokens' contained embeddings
                // since otherwise (if they would not be up-to-date) they could not be updated later
                // as they lose their parent token list which the update-status relies on.
                AbstractToken<T> token = tokenOrEmbedding.token();
                if (!token.isFlyweight()) {
                    updateElementOffsetRemove(token);
                    token.setTokenList(null);
                    EmbeddedTokenList<T,?> etl = tokenOrEmbedding.embedding();
                    if (etl != null) {
                        etl.markRemovedChain(token.rawOffset());
                    }
                }
            }
            remove(index, removeTokenCount); // Retain original offsets
            laState.remove(index, removeTokenCount); // Remove lookaheads and states
            change.setRemovedTokens(removedTokensOrEmbeddings);
        } else {
            change.setRemovedTokensEmpty();
        }

        // Move and fix the gap according to the performed modification.
        // Instead of modOffset the gap is located at first relexed token's start
        // because then the already precomputed index corresponding to the given offset
        // can be reused. Otherwise there would have to be another binary search for index.
        if (offsetGapStart() != change.offset()) {
            // Minimum of the index of the first removed index and original computed index
            moveOffsetGap(change.offset(), change.index());
        }
        updateOffsetGapLength(-eventInfo.diffLength());

        // Add created tokens.
        List<TokenOrEmbedding<T>> addedTokensOrEmbeddings = change.addedTokenOrEmbeddings();
        if (addedTokensOrEmbeddings != null && addedTokensOrEmbeddings.size() > 0) {
            for (TokenOrEmbedding<T> tokenOrEmbedding : addedTokensOrEmbeddings) {
                AbstractToken<T> token = tokenOrEmbedding.token();
                if (!token.isFlyweight())
                    token.setTokenList(this);
                updateElementOffsetAdd(token);
            }
            addAll(index, addedTokensOrEmbeddings);
            laState = laState.addAll(index, change.laState());
            change.syncAddedTokenCount();
            // Check for bounds change only
            if (removeTokenCount == 1 && addedTokensOrEmbeddings.size() == 1) {
                // Compare removed and added token ids and part types
                AbstractToken<T> addedToken = change.addedTokenOrEmbeddings().get(0).token();
                if (firstRemovedToken.id() == addedToken.id()
                    && firstRemovedToken.partType() == addedToken.partType()
                ) {
                    change.markBoundsChange();
                }
            }
        }

        // Possibly restart unfinished lexing
        if (this.lexerInputOperation != null) { // Lexing was not finished before update
            int tokenCount = tokenCountCurrent();
            lexerInputOperation = createLexerInputOperation(tokenCount, elementOrEndOffset(tokenCount),
                (tokenCount > 0) ? state(tokenCount - 1) : null);
        }
    }
    
    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public Set<T> skipTokenIds() {
        return null;
    }

    @Override
    public int startOffset() {
        return 0;
    }

    @Override
    public int endOffset() {
        return (inputSourceText != null) ? inputSourceText.length() : 0;
    }

    @Override
    public boolean isRemoved() {
        return false; // Should never become removed
    }

    public void setInputSourceText(CharSequence text) {
        this.inputSourceText = text;
    }

    public String checkConsistency() {
        if (offsetGapLength() < 0) {
            return "offsetGapLength=" + offsetGapLength() + " < 0; offsetGapStart=" + offsetGapStart(); // NOI18N
        }
        return null;
    }

    @Override
    public StringBuilder dumpInfo(StringBuilder sb) {
        sb.append("offGap(o=").append(offsetGapStart()). // NOI18N
                append(",l=").append(offsetGapLength()).append(")"); // NOI18N
        return sb;
    }

    @Override
    public String dumpInfoType() {
        return "ITL";
    }

    @Override
    public String toString() {
        return LexerUtilsConstants.appendTokenList(null, this).toString();
    }
    
}
