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

package org.netbeans.api.lexer;

import java.util.ConcurrentModificationException;
import org.netbeans.lib.lexer.EmbeddedJoinInfo;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.EmbeddingOperation;
import org.netbeans.lib.lexer.JoinTokenList;
import org.netbeans.lib.lexer.SubSequenceTokenList;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.TokenOrEmbedding;
import org.netbeans.spi.lexer.LanguageEmbedding;

/**
 * Token sequence allows to iterate between tokens
 * of a token hierarchy.
 * <br>
 * Token sequence for top-level language of a token hierarchy
 * may be obtained by {@link TokenHierarchy#tokenSequence()}.
 * 
 * <p>
 * Use of token sequence is a two-step operation:
 * <ol>
 *   <li>
 *     Position token sequence before token that should first be retrieved
 *     (or behind desired token when iterating backwards).
 *     <br>
 *     One of the following ways may be used:
 *     <ul>
 *       <li> {@link #move(int)} positions TS before token that either starts
 *           at the given offset or "contains" it.
 *       </li>
 *       <li> {@link #moveIndex(int)} positions TS before n-th token in the underlying
 *           token list.
 *       </li>
 *       <li> {@link #moveStart()} positions TS before the first token. </li>
 *       <li> {@link #moveEnd()} positions TS behind the last token. </li>
 *       <li> Do nothing - TS is positioned before the first token automatically by default. </li>
 *     </ul>
 *     Token sequence will always be positioned between tokens
 *     when using one of the operations above
 *     ({@link #token()} will return <code>null</code> to signal between-tokens location).
 *     <br>
 *   </li>
 * 
 *   <li>
 *     Start iterating through the tokens in forward/backward direction
 *     by using {@link #moveNext()} or {@link #movePrevious()}.
 *     <br>
 *     If <code>moveNext()</code> or <code>movePrevious()</code> returned
 *     <code>true</code> then TS is positioned
 *     over a concrete token retrievable by {@link #token()}.
 *     <br>
 *     Its offset can be retrieved by {@link #offset()}.
 *   </li>
 * </ol>
 * 
 * <p>
 * An example of forward iteration through the tokens:
 * <pre>
 *   TokenSequence ts = tokenHierarchy.tokenSequence();
 *   // Possible positioning by ts.move(offset) or ts.moveIndex(index)
 *   while (ts.moveNext()) {
 *       Token t = ts.token();
 *       if (t.id() == ...) { ... }
 *       if (TokenUtilities.equals(t.text(), "mytext")) { ... }
 *       if (ts.offset() == ...) { ... }
 *   }
 * </pre>
 *
 * <p>
 * This object should be used by a single thread only. For token hierarchies
 * over mutable input sources the obtaining and using of the token sequence
 * needs to be done under a read-lock of the input source.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenSequence<T extends TokenId> {
    
    private final TokenList<T> tokenList; // 8 + 4 = 12 bytes
    
    private AbstractToken<T> token; // 16 bytes
    
    private int tokenIndex; // 20 bytes
    
    /**
     * Offset in the input at which the current token is located
     * or <code>-1</code> if the offset needs to be computed.
     */
    private int tokenOffset = -1; // 24 bytes

    private final TokenList<?> rootTokenList; // 28 bytes

    /**
     * modCount of token list at time when token sequence was constructed.
     */
    private final int modCount; // 32 bytes

    private final EmbeddedTokenList<?,T> embeddedTokenList;

    /**
     * Package-private constructor used by API accessor.
     */
    TokenSequence(TokenList<T> tokenList) {
        this.tokenList = tokenList;
        this.rootTokenList = tokenList.rootTokenList();
        assert (rootTokenList != null) : "Invalid null rootTokenList"; // NOI18N
        if (tokenList instanceof EmbeddedTokenList) {
            embeddedTokenList = (EmbeddedTokenList<?,T>) tokenList;
            embeddedTokenList.updateModCount(rootTokenList.modCount());
        } else {
            embeddedTokenList = null;
        }
        this.modCount = tokenList.modCount();
    }

    /**
     * Get the language describing token ids
     * used by tokens in this token sequence.
     */
    public Language<T> language() {
        return tokenList.language();
    }

    /**
     * Get the complete language path of the tokens contained
     * in this token sequence.
     */
    public LanguagePath languagePath() {
        return tokenList.languagePath();
    }

    /**
     * Get token to which this token sequence points to or null
     * if TS is positioned between tokens
     * ({@link #moveNext()} or {@link #movePrevious()} were not called yet).
     * <br>
     * A typical iteration usage:
     * <pre>
     *   TokenSequence ts = tokenHierarchy.tokenSequence();
     *   // Possible positioning by ts.move(offset) or ts.moveIndex(index)
     *   while (ts.moveNext()) {
     *       Token t = ts.token();
     *       if (t.id() == ...) { ... }
     *       if (TokenUtilities.equals(t.text(), "mytext")) { ... }
     *       if (ts.offset() == ...) { ... }
     *   }
     * </pre>
     *
     * The returned token instance may be flyweight
     * ({@link Token#isFlyweight()} returns true)
     * which means that its {@link Token#offset(TokenHierarchy)} will return -1.
     * <br>
     * To find a correct offset use {@link #offset()}.
     * <br>
     * Or if its necessary to revert to a regular non-flyweigt token
     * the {@link #offsetToken()} may be used.
     *
     * <p>
     * The lifetime of the returned token instance may be limited for mutable inputs.
     * The token instance should not be held across the input source modifications.
     * </p>
     *
     * @return token instance to which this token sequence is currently positioned
     *  or null if this token sequence is not positioned to any token which may
     *  happen after TS creation or after use of {@link #move(int)} or {@link #moveIndex(int)}.
     * 
     * @see #offsetToken()
     */
    public Token<T> token() {
        return token;
    }
    
    /**
     * Similar to {@link #token()} but always returns a non-flyweight token
     * with the appropriate offset.
     * <br>
     * If the current token is flyweight then this method replaces it
     * with the corresponding non-flyweight token which it then returns.
     * <br>
     * Subsequent calls to {@link #token()} will also return this non-flyweight token.
     *
     * <p>
     * This method may be handy if the token instance is referenced in a standalone way
     * (e.g. in an expression node of a parse tree) and it's necessary
     * to get the appropriate offset from the token itself
     * later when a token sequence will not be available.
     * </p>
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public Token<T> offsetToken() {
        synchronized (rootTokenList) {
            checkTokenNotNull();
            if (token.isFlyweight()) {
                token = tokenList.replaceFlyToken(tokenIndex, token, offset());
            }
        }
        return token;
    }
    
    /**
     * Get the offset of the current token in the underlying input.
     * <br>
     * The token's offset should never be computed by a client of the token sequence
     * by adding/subtracting tokens' length to a client's variable because
     * in case of the immutable token sequences there can be gaps
     * between tokens if some tokens get filtered out.
     * <br>
     * Instead this method should always be used because it offers
     * best performance with a constant time complexity.
     *
     * @return &gt;=0 absolute offset of the current token in the underlying input.
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public int offset() {
        synchronized (rootTokenList) {
            checkTokenNotNull();
            if (tokenOffset == -1) {
                tokenOffset = tokenList.tokenOffset(tokenIndex);
            }
            return tokenOffset;
        }
    }
    
    /**
     * Get an index of token to which (or before which) this TS is currently positioned.
     * <br>
     * <p>
     * Initially or after {@link #move(int)} or {@link #moveIndex(int)}
     * token sequence is positioned between tokens:
     * <pre>
     *          Token[0]   Token[1]   ...   Token[n]
     *        ^          ^                ^
     * Index: 0          1                n
     * </pre>
     * 
     * <p>
     * After use of {@link #moveNext()} or {@link #movePrevious()}
     * the token sequence is positioned over one of the actual tokens:
     * <pre>
     *          Token[0]   Token[1]   ...   Token[n]
     *             ^          ^                ^
     * Index:      0          1                n
     * </pre>
     * 
     * @return &gt;=0 index of token to which (or before which) this TS is currently positioned.
     */
    public int index() {
        return tokenIndex;
    }

    /**
     * Get embedded token sequence if the token
     * to which this token sequence is currently positioned
     * has a language embedding.
     * <br>
     * If there is a custom embedding created by
     * {@link #createEmbedding(Language,int,int)} it will be returned
     * instead of the default embedding
     * (the one created by <code>LanguageHierarchy.embedding()</code>
     * or <code>LanguageProvider</code>).
     *
     * @return embedded sequence or null if no embedding exists for this token.
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public TokenSequence<?> embedded() {
        return embeddedImpl(null, false);
    }

    /**
     * Get embedded token sequence if the token
     * to which this token sequence is currently positioned
     * has a language embedding.
     * 
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public <ET extends TokenId> TokenSequence<ET> embedded(Language<ET> embeddedLanguage) {
        return embeddedImpl(embeddedLanguage, false);
    }

    /**
     * Get embedded token sequence that possibly joins multiple embeddings
     * with the same language paths (if the embeddings allow it - see
     * {@link LanguageEmbedding#joinSections()}) into a single input text
     * which is then lexed as a single continuous text.
     * <br>
     * If any of the resulting tokens crosses embedding's boundaries then the token
     * is split into multiple part tokens.
     * <br>
     * If the embedding does not join sections then this method behaves
     * like {@link #embedded()}.
     * 
     * @return embedded sequence or null if no embedding exists for this token.
     *  The token sequence will be positioned before first token of this embedding
     *  or to a join token in case the first token of this embedding is part of the join token.
     */
    public TokenSequence<?> embeddedJoined() {
        return embeddedImpl(null, true);
    }

    /**
     * Get embedded token sequence if the token
     * to which this token sequence is currently positioned
     * has a language embedding.
     * 
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public <ET extends TokenId> TokenSequence<ET> embeddedJoined(Language<ET> embeddedLanguage) {
        return embeddedImpl(embeddedLanguage, true);
    }

    private <ET extends TokenId> TokenSequence<ET> embeddedImpl(Language<ET> embeddedLanguage, boolean joined) {
        synchronized (rootTokenList) {
            checkTokenNotNull();
            if (token.isFlyweight()) {
                return null;
            }
            checkValid();
            EmbeddedTokenList<T,ET> etl
                    = EmbeddingOperation.embeddedTokenList(tokenList, tokenIndex, embeddedLanguage, true);
            if (etl != null) {
                etl.updateModCount(rootTokenList.modCount());
                TokenSequence<ET> tse;
                JoinTokenList<ET> jtl;
                EmbeddedJoinInfo<ET> joinInfo = etl.joinInfo();
                if (joined && (joinInfo != null) && (jtl = joinInfo.joinTokenList) != null) {
                    @SuppressWarnings("unchecked")
                    JoinTokenList<ET> joinTokenList = (JoinTokenList<ET>) jtl;
                    tse = new TokenSequence<ET>(joinTokenList);
                    // Position to this etl's index
                    jtl.setActiveTokenListIndex(joinInfo.tokenListIndex());
                    tse.moveIndex(joinTokenList.activeStartJoinIndex());
                } else { // Request regular TS or no joining available
                    tse = new TokenSequence<ET>(etl);
                }
                return tse;
            }
            return null;
        }
    }

    /**
     * Create language embedding without joining of the embedded sections.
     *
     * @param startSkipLength number of characters to be skipped at token's begining.
     * @param endSkipLength number of characters to be skipped at token's end.
     * @throws IllegalStateException if {@link #token()} returns null.
     * @see #createEmbedding(Language, int, int, boolean)
     */
    public boolean createEmbedding(Language<?> embeddedLanguage,
    int startSkipLength, int endSkipLength) {
        return createEmbedding(embeddedLanguage, startSkipLength, endSkipLength, false);
    }

    /**
     * Create language embedding described by the given parameters.
     * <br>
     * If the underying text input is mutable then this method should only be called
     * within a write lock over the text input.
     *
     * @param embeddedLanguage non-null embedded language
     * @param startSkipLength &gt;=0 number of characters in an initial part of the token
     *  for which the language embedding is defined that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     * @param endSkipLength &gt;=0 number of characters at the end of the token
     *  for which the language embedding is defined that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     * @param joinSections whether sections with this embedding should be joined
     *  across the input source or whether they should stay separate.
     *  <br>
     *  For example for HTML sections embedded in JSP this flag should be true:
     *  <pre>
     *   &lt;!-- HTML comment start
     *       &lt;% System.out.println("Hello"); %&gt;
            still in HTML comment --&lt;
     *  </pre>
     *  <br>
     *  Only the embedded sections with the same language path can be joined.
     * <br>
     * If preceding embeddings requested sections joining for the particular language path
     * then this parameter will be updated from false to true automatically by the method.
     * @return true if the embedding was created successfully or false if an embedding
     *  with the given language already exists for this token.
     * @throws IllegalStateException if {@link #token()} returns null.
     */
    public boolean createEmbedding(Language<?> embeddedLanguage,
    int startSkipLength, int endSkipLength, boolean joinSections) {
        synchronized (rootTokenList) {
            checkTokenNotNull();
            checkValid();
            // Write-lock presence checked in the impl
            return EmbeddingOperation.createEmbedding(tokenList, tokenIndex,
                    embeddedLanguage, startSkipLength, endSkipLength, joinSections);
        }
    }
    
    /**
     * Remove previously created language embedding.
     * <br>
     * If the underying text input is mutable then this method should only be called
     * within a write lock over the text input.
     */
    public boolean removeEmbedding(Language<?> embeddedLanguage) {
        synchronized (rootTokenList) {
            checkTokenNotNull();
            checkValid();
            // Write-lock presence checked in the impl
            return EmbeddingOperation.removeEmbedding(tokenList, tokenIndex, embeddedLanguage);
        }
    }

    /**
     * Move to the next token in this token sequence.
     * 
     * <p>
     * The next token may not necessarily start at the offset where
     * the previous token ends (there may be gaps between tokens
     * caused by token filtering). {@link #offset()} should be used
     * for offset retrieval.
     * </p>
     *
     * @return true if the sequence was successfully moved to the next token
     *  or false if it was not moved before there are no more tokens
     *  in the forward direction.
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public boolean moveNext() {
        synchronized (rootTokenList) {
            checkValid();
            if (token != null) // Token already fetched
                tokenIndex++;
            TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(tokenIndex);
            if (tokenOrEmbedding != null) { // Might be null if no more tokens available
                AbstractToken<T> origToken = token;
                token = tokenOrEmbedding.token();
                // If origToken == null then the right offset might already be pre-computed from move()
                if (tokenOffset != -1) {
                    if (origToken != null) {
                        // If the token list is continuous or the fetched token
                        // is flyweight (there cannot be a gap before flyweight token)
                        // the original offset can be just increased
                        // by the original token's length.
                        if (tokenList.isContinuous() || token.isFlyweight()) {
                            tokenOffset += origToken.length(); // advance by previous token's length
                        } else // Offset must be recomputed
                            tokenOffset = -1; // mark the offset to be recomputed
                    } else // Not valid token previously
                        tokenOffset = -1;
                }
                return true;
            }
            if (token != null) // Unsuccessful move from existing token
                tokenIndex--;
            return false;
        }
    }

    /**
     * Move to a previous token in this token sequence.
     *
     * <p>
     * The previous token may not necessarily end at the offset where
     * the previous token started (there may be gaps between tokens
     * caused by token filtering). {@link #offset()} should be used
     * for offset retrieval.
     * </p>
     *
     * @return true if the sequence was successfully moved to the previous token
     *  or false if it was not moved because there are no more tokens
     *  in the backward direction.
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public boolean movePrevious() {
        synchronized (rootTokenList) {
            checkValid();
            if (tokenIndex > 0) {
                AbstractToken<T> origToken = token;
                tokenIndex--;
                token = tokenList.tokenOrEmbedding(tokenIndex).token();
                if (tokenOffset != -1) {
                    if (origToken != null) {
                        // If the token list is continuous or the original token
                        // is flyweight (there cannot be a gap before flyweight token)
                        // the original offset can be just decreased
                        // by the fetched token's length.
                        if (tokenList.isContinuous() || origToken.isFlyweight()) {
                            tokenOffset -= token.length(); // decrease by the fetched's token length
                        } else { // mark the offset to be computed upon call to offset()
                            tokenOffset = -1;
                        }
                    } else {
                        tokenOffset = -1;
                    }
                }
                return true;

            } // no tokens below index zero
            return false;
        }
    }

    /**
     * Position token sequence between <code>index-1</code>
     * and <code>index</code> tokens.
     * <br>
     * TS will be positioned in the following way:
     * <pre>
     *          Token[0]   ...   Token[index-1]   Token[index] ...
     *        ^                ^                ^
     * Index: 0             index-1           index
     * </pre>
     * 
     * <p>
     * Subsequent {@link #moveNext()} or {@link #movePrevious()} is needed to fetch
     * a concrete token in the desired direction.
     * <br>
     * Subsequent {@link #moveNext()} will position TS over <code>Token[index]</code>
     * (or {@link #movePrevious()} will position TS over <code>Token[index-1]</code>)
     * so that <code>{@link #token()} != null</code>.
     *
     * @param index index of the token to which this sequence
     *   should be positioned.
     *   <br>
     *   If <code>index >= {@link #tokenCount()}</code>
     *   then the TS will be positioned to {@link #tokenCount()}.
     *   <br>
     *   If <code>index &lt; 0</code> then the TS will be positioned to index 0.
     * 
     * @return difference between requested index and the index to which TS
     *   is really set.
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public int moveIndex(int index) {
        synchronized (rootTokenList) {
            checkValid();
            return moveIndexImpl(index);
        }
    }
    
    private int moveIndexImpl(int index) {
        if (index >= 0) {
            TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(index);
            if (tokenOrEmbedding != null) { // enough tokens
                resetTokenIndex(index, -1);
            } else {// Token at the requested index does not exist - leave orig. index
                resetTokenIndex(tokenCount(), -1);
            }
        } else {// index < 0
            resetTokenIndex(0, -1);
        }
        return index - tokenIndex;
    }
    
    /**
     * Move the token sequence to be positioned before the first token.
     * <br>
     * This is equivalent to <code>moveIndex(0)</code>.
     */
    public void moveStart() {
        synchronized (rootTokenList) {
            checkValid();
            moveIndex(0);
        }
    }
    
    /**
     * Move the token sequence to be positioned behind the last token.
     * <br>
     * This is equivalent to <code>moveIndex(tokenCount())</code>.
     */
    public void moveEnd() {
        synchronized (rootTokenList) {
            checkValid();
            moveIndex(tokenList.tokenCount());
        }
    }
    
    /**
     * Move token sequence to be positioned between <code>index-1</code>
     * and <code>index</code> tokens where Token[index] either starts at offset
     * or "contains" the offset.
     * <br>
     * <pre>
     *        +----------+-----+----------------+--------------+------
     *        | Token[0] | ... | Token[index-1] | Token[index] | ...
     *        | "public" | ... | "static"       | "int"        | ...
     *        +----------+-----+----------------+--------------+------
     *        ^                ^                ^
     * Index: 0             index-1           index
     * Offset:                                  ---^ (if offset points to 'i','n' or 't')
     * </pre>
     * 
     * <p>
     * Subsequent {@link #moveNext()} or {@link #movePrevious()} is needed to fetch
     * a concrete token.
     * <br>
     * If the offset is too big then the token sequence will be positioned
     * behind the last token.
     * </p>
     * 
     * <p>
     * If token filtering is used there may be gaps that are not covered
     * by any tokens and if the offset is contained in such gap then
     * the token sequence will be positioned before the token that precedes the gap.
     * </p>
     *
     *
     * @param offset absolute offset to which the token sequence should be moved.
     * @return difference between the reqeuested offset
     *  and the start offset of the token
     *  before which the the token sequence gets positioned.
     *  <br>
     *  If positioned right after the last token then (offset - last-token-end-offset)
     *  is returned.
     * 
     * @throws ConcurrentModificationException if this token sequence
     *  is no longer valid because of an underlying mutable input source modification.
     */
    public int move(int offset) {
        synchronized (rootTokenList) {
            checkValid();
            int[] indexAndTokenOffset = tokenList.tokenIndex(offset);
            if (indexAndTokenOffset[0] != -1) { // Valid index and token-offset
                resetTokenIndex(indexAndTokenOffset[0], indexAndTokenOffset[1]);
            } else { // No tokens in token list (indexAndOffset[1] == 0)
                resetTokenIndex(0, -1); // Set Index to zero and offset to invalid
            }
            return offset - indexAndTokenOffset[1];
        }
    }
    
    /**
     * Check whether this TS contains zero tokens.
     * <br>
     * This check is strongly preferred over <code>tokenCount() == 0</code>.
     * 
     * @see #tokenCount()
     */
    public boolean isEmpty() {
        synchronized (rootTokenList) {
            checkValid();
            return (tokenIndex == 0 && tokenList.tokenOrEmbedding(0) == null);
        }
    }

    /**
     * Return total count of tokens in this sequence.
     * <br>
     * <b>Note:</b> Calling this method will lead
     * to creation of all the remaining tokens in the sequence
     * if they were not yet created.
     *
     * @return total number of tokens in this token sequence.
     */
    public int tokenCount() {
        synchronized (rootTokenList) {
            checkValid();
            return tokenList.tokenCount();
        }
    }
    
    /**
     * Create sub sequence of this token sequence that only returns
     * tokens above the given offset.
     *
     * @param startOffset only tokens satisfying
     *  <code>tokenStartOffset + tokenLength > startOffset</code>
     *  will be present in the returned sequence.
     * @return non-null sub sequence of this token sequence.
     */
    public TokenSequence<T> subSequence(int startOffset) {
        return subSequence(startOffset, Integer.MAX_VALUE);
    }
    
    /**
     * Create sub sequence of this token sequence that only returns
     * tokens between the given offsets.
     *
     * @param startOffset only tokens satisfying
     *  <code>tokenStartOffset + tokenLength > startOffset</code>
     *  will be present in the returned sequence.
     * @param endOffset >=startOffset only tokens satisfying
     *  <code>tokenStartOffset &lt; endOffset</code>
     *  will be present in the returned sequence.
     * @return non-null sub sequence of this token sequence.
     */
    public TokenSequence<T> subSequence(int startOffset, int endOffset) {
        synchronized (rootTokenList) {
            checkValid();
            TokenList<T> tl;
            if (tokenList.getClass() == SubSequenceTokenList.class) {
                SubSequenceTokenList<T> stl = (SubSequenceTokenList<T>)tokenList;
                tl = stl.delegate();
                startOffset = Math.max(startOffset, stl.limitStartOffset());
                endOffset = Math.min(endOffset, stl.limitEndOffset());
            } else {// Regular token list
                tl = tokenList;
            }
            return new TokenSequence<T>(new SubSequenceTokenList<T>(tl, startOffset, endOffset));
        }
    }
    
    /**
     * Check whether this token sequence is valid and can be iterated.
     * <br>
     * If this method returns false then the underlying token hierarchy was modified
     * and this token sequence should be abandoned.
     * 
     * @return true if this token sequence is ready for use or false if it should be abandoned.
     */
    public boolean isValid() {
        synchronized (rootTokenList) {
            return !isInvalid();
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("TokenSequence for ").append(tokenList.languagePath().mimePath()); // NOI18N
        sb.append(" at tokenIndex=").append(tokenIndex); // NOI18N
        sb.append(". TokenList contains ").append(tokenList.tokenCount()).append(" tokens:\n"); // NOI18N
        LexerUtilsConstants.appendTokenList(sb, tokenList,
                tokenIndex, 0, Integer.MAX_VALUE, true, 0, true);
        sb.append('\n');
        return sb.toString();
    }
    
    private void resetTokenIndex(int index, int offset) {
        // Position to the given index e.g. by move() and moveIndex()
        tokenIndex = index;
        token = null;
        tokenOffset = offset;
    }

    private void checkTokenNotNull() {
        if (token == null) {
            throw new IllegalStateException(
                "Caller of TokenSequence forgot to call moveNext/Previous() " + // NOI18N
                "or it returned false (no more tokens)\n" + this // NOI18N
            );
        }
    }
    
    private void checkValid() {
        if (isInvalid()) {
            throw new ConcurrentModificationException(
                "Caller uses obsolete token sequence which is no longer valid. Underlying token hierarchy" + // NOI18N
                " has been modified by insertion or removal or a custom language embedding was created." + // NOI18N
                "TS.modCount=" + modCount + ", tokenList.modCount()=" + tokenList.modCount() + // NOI18N
                ", rootModCount=" + rootTokenList.modCount() + // NOI18N
                "\nPlease report a bug against a module that calls lexer's code e.g. java, php etc. " + // NOI18N
                "but not the lexer module itself." // NOI18N
            );
        }
    }

    private boolean isInvalid() {
        if (embeddedTokenList != null) {
            embeddedTokenList.updateModCount(rootTokenList.modCount());
        }
        return (modCount != tokenList.modCount());
        
    }

}
