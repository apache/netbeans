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

import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Browsable list of tokens.
 * <br>
 * {@link org.netbeans.api.lexer.TokenSequence} delegates
 * all its operation to this class so any service provider
 * delivering this class will be able to produce token sequences.
 *
 * There are various implementations of the token list:
 * <ul>
 *   <li>BatchTokenList</li> - predecessor of batch token lists
 *   <li>TextTokenList</li> - token list over immutable char sequence
 *   <li>CopyTextTokenList</li> - token list over text input
 *     that needs to be copied. Characters that belong to tokens
 *     skipped due to skipTokenIds do not need to be copied.
 *   <li>SkimTokenList</li> - filter over CopyTextTokenList
 *     to store the token characters in multiple arrays
 *     and to correctly compute the tokens' starting offsets.
 *   <li>IncTokenList</li> - token list for mutable-input environment.
 *   <li>EmbeddedTokenList</li> - token list for a single language embedding
 *     suitable for both batch and incremental environments.
 * </ul>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenList<T extends TokenId> {

    /**
     * This is a special logger that indicates (by logging a FINE level)
     * whether the whole lexer framework runs in a "testing" mode
     * where certain additional checks are performed (for example whether flyweight tokens' text
     * matches the text consumed from lexer-input) and certain problematic situations
     * are reported as errors (for example when an invalid index of ETL is found in TLL).
     */
    // -J-Dorg.netbeans.lib.lexer.TokenList.level=FINE
    public static final Logger LOG = Logger.getLogger(TokenList.class.getName());

    Language<T> language();

    /**
     * Language path of this token list.
     */
    LanguagePath languagePath();
    
    /**
     * Get token or {@link EmbeddingContainer} at given index in this list.
     * <br/>
     * The requested index value may be arbitrarily high
     * (e.g. when TokenSequence.move(index) is used for too high value).
     *
     * @param &gt;=0 index of the token in this list.
     * @return valid token or null if the index is too high.
     */
    TokenOrEmbedding<T> tokenOrEmbedding(int index);

    /**
     * Replace flyweight token at the given index with its non-flyweight copy.
     * <br/>
     * This may be requested by <code>TokenSequence.offsetToken()</code>.
     *
     * @param index &gt;=0 index of the flyweight token in this list.
     * @param flyToken non-null flyweight token. 
     * @param offset >=0 absolute offset where the flyweight token resides.
     * @return non-flyweight token instance.
     */
    AbstractToken<T> replaceFlyToken(int index, AbstractToken<T> flyToken, int offset);
    
    /**
     * Set the token or embedding at the given index.
     *
     * @param index existing index in this token list at which the token
     *  should be set.
     * @param t non-null token or embedding.
     */
    void setTokenOrEmbedding(int index, TokenOrEmbedding<T> t);
    
    /**
     * Get absolute offset of the token at the given index in the token list.
     * <br>
     * This method can only be called if the token at the given index
     * was already fetched by {@link tokenOrEmbedding(int)}.
     * <br/>
     * For EmbeddedTokenList an updateStatus() must be called
     * prior this method to obtain up-to-date results.
     * 
     * @param index valid token index in token list.
     */
    int tokenOffset(int index);
    
    /**
     * Get absolute offset of a token contained in this token list.
     * <br/>
     * For EmbeddedTokenList a EmbeddingContainer.updateStatus() must be called
     * prior this method to obtain up-to-date results.
     *
     * @param token non-null child token of this token list.
     * @return absolute offset in the input.
     */
    int tokenOffset(AbstractToken<T> token);
    
    /**
     * Get index of the token that "contains" the given offset.
     * <br/>
     * The result is in sync with TokenSequence.moveOffset().
     * 
     * @param offset offset for which the token index should be found.
     * @return array of two items where the [0] is token's index and [1] is its offset.
     *  <br/>
     *  If offset &gt;= last-token-end-offset then [0] contains token-count and
     *  [1] conains last-token-end-offset.
     *  <br/>
     *  [0] may contain -1 to indicate that there are no tokens in the token list
     *  ([1] then contains zero).
     */
    int[] tokenIndex(int offset);
    
   /**
     * Get total count of tokens in the list.
     * <br/>
     * For token lists that create the tokens lazily
     * this will lead to lexing till the end of the input.
     */
    int tokenCount();

    /**
     * Return present number of tokens in the token list but do not create
     * any new tokens (because of possible lazy token creation).
     * <br/>
     * This is necessary e.g. for <code>TokenSequence.move()</code>
     * that needs a binary search for fast positioning 
     * but using {@link #tokenCount()} would lead to unnecessary creation
     * of all tokens.
     */
    int tokenCountCurrent();

    /**
     * Get number of modifications which mutated this token list.
     * <br>
     * Token sequence remembers this number when it gets constructed
     * and checks this number when it moves between tokens
     * and if there is an extra modification performed it throws
     * <code>IllegalStateException</code>.
     *
     * <p>
     * This is also used to check whether this token list corresponds to mutable input
     * or not because unmodifiable lists return -1 from this method.
     * </p>
     *
     * <p>
     * For embedded token lists this value should be update to root's one
     * prior constructing child token sequence.
     * </p>
     *
     * @return number of modifications performed to the list.
     *  <br/>
     *  Returns -1 if this list is constructed for immutable input and cannot be mutated. 
     */
    int modCount();

    /**
     * Get the root token list of the token list hierarchy.
     */
    TokenList<?> rootTokenList();
    
    /**
     * Get text of the whole input source.
     */
    CharSequence inputSourceText();
    
    /**
     * Get token hierarchy operation for this token list or null
     * if this token list does not have any token hierarchy.
     */
    TokenHierarchyOperation<?,?> tokenHierarchyOperation();
    
    /**
     * Extra attributes related to the input being lexed.
     */
    InputAttributes inputAttributes();

    /**
     * Get lookahead information for the token at the existing token index.
     * <br/>
     * Lookahead is number of characters that the lexer has read
     * past the end of the given token in order to recognize it in the text.
     * <br>
     * This information allows the lexer to know whether modifications
     * past the end of the token can affect its validity.
     *
     * <p>
     * In general only mutable token lists benefit from this information
     * but non-mutable token lists may store the information as well for testing
     * purposes.
     * </p>
     *
     * @param index index of the existing token.
     * @return &gt;=0 number of characters that the lexer has read
     *  in order to recognize this token. Return zero if this token list
     *  does not maintain lookaheads.
     */
    int lookahead(int index);
    
    /**
     * Get state information for the token at the existing token index.
     * <br/>
     * It is an object defining lexer's state after recognition
     * of the given token.
     * <br/>
     * This information allows to restart the lexer at the end of the given token.
     *
     * <p>
     * In general only mutable token lists benefit from this information
     * but non-mutable token lists may store the information as well for testing
     * purposes.
     * </p>
     *
     * @param index index of the existing token.
     * @return lexer's state after recognition of this token
     *  or null for default state. Return null if this token list
     *  does not maintain states.
     */
    Object state(int index);
    
    /**
     * Returns true if the underlying token list does not contain offset ranges
     * that would not be covered by tokens.
     * <br/>
     * This could happen if a batch token list would use token id filter.
     * <br/>
     * If the token list is continuous the TokenSequence
     * can compute token offsets more efficiently.
     */
    boolean isContinuous();

    /**
     * Get set of token ids to be skipped during token creation.
     */
    Set<T> skipTokenIds();

    /**
     * Get offset where a first token of this token list should start.
     * <br/>
     * If token filtering is used then the first token may start at higher offset.
     * <br/>
     * It's guaranteed that there will be no token starting below this offset.
     * <br/>
     * For EmbeddedTokenList a updateModCount() must be called
     * prior this method to obtain up-to-date results.
     */
    int startOffset();
    
    /**
     * Get offset where the last token of this token list should end.
     * <br/>
     * If token filtering is used then the last token may end at lower offset.
     * <br/>
     * It's guaranteed that there will be no token ending above this offset.
     * <br/>
     * For EmbeddedTokenList a updateModCount() must be called
     * prior this method to obtain up-to-date results.
     */
    int endOffset();
    
    /**
     * Check if this token list is removed from token hierarchy.
     * <br/>
     * Should only be called under a lock of a root token list.
     * 
     * @return true if the token list was removed or false otherwise.
     */
    boolean isRemoved();

    /**
     * Dump extra information (not token infos)
     * about this token list to the given string builder.
     *
     * @param sb non-null string builder.
     * @return sb passed as an argument.
     */
    StringBuilder dumpInfo(StringBuilder sb);

    /**
     * Type of this token list for dump info purpose.
     *
     * @return textual token list type.
     */
    String dumpInfoType();

}
