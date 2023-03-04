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

package org.netbeans.api.lexer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.inc.DocumentInput;

/**
 * Token hierarchy represents a given input source as a browsable hierarchy of tokens.
 * <br>
 * It's is an entry point into the Lexer API.
 * <br/>
 * It allows to create token sequences for hierarchy exploration
 * and watching for token changes by attaching the token hierarchy listeners.
 * <br>
 * The hierarchy may either be flat or it can be a tree if the
 * corresponding language hierarchy contains language embeddings.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchy<I> { // "I" stands for mutable input source
    
    /**
     * Get or create mutable token hierarchy for the given swing document.
     * <br/>
     * The document may define a top language by doing
     * <code>doc.putProperty("mimeType", mimeType)</code>
     * (a language defined for the given mime type will be searched and used)
     * or by doing <code>putProperty(Language.class, language)</code>.
     * Otherwise the returned hierarchy will be inactive and {@link #tokenSequence()}
     * will return null.
     * <br/>
     * All the operations with the obtained token hierarchy
     * must be done under document's read lock (or write lock).
     *
     * @param doc non-null swing text document for which the token hiearchy should be obtained.
     * @return non-null token hierarchy.
     */
    public static <D extends Document> TokenHierarchy<D> get(D doc) {
        return DocumentInput.get(doc).tokenHierarchyControl().tokenHierarchy();
    }
    
    /**
     * Create token hierarchy for the given non-mutating input text (for example
     * java.lang.String).
     *
     * @see #create(CharSequence,boolean,Language,Set,InputAttributes)
     */
    public static <I extends CharSequence> TokenHierarchy<I> create(I inputText, Language<?> language) {
        return create(inputText, false, language, null, null);
    }

    /**
     * Create token hierarchy for the given input text.
     *
     * @param inputText input text containing the characters to tokenize.
     * @param copyInputText <code>true</code> in case the content of the input
     *  will not be modified in the future so the created tokens can reference it.
     *  <br>
     *  <code>false</code> means that the text can change in the future
     *  and the tokens should not directly reference it. Instead copy of the necessary text
     *  from the input should be made and the original text should not be referenced.
     * @param language language defining how the input
     *  will be tokenized.
     * @param skipTokenIds set containing the token ids for which the tokens
     *  should not be created in the created token hierarchy.
     *  <br/>
     *  <code>null</code> may be passed which means that no tokens will be skipped.
     *  <br/>
     *  This applies to top level of the token hierarchy only (not to embedded tokens).
     *  <br/>
     *  The provided set should be efficient enough - ideally created by e.g.
     *  {@link Language#tokenCategoryMembers(String)}
     *  or {@link Language#merge(Collection,Collection)}.
     *
     * @param inputAttributes additional properties related to the input
     *  that may influence token creation or lexer operation
     *  for the particular language (such as version of the language to be used).
     * @return non-null token hierarchy.
     */
    public static <I extends CharSequence, T extends TokenId> TokenHierarchy<I> create(
    I inputText, boolean copyInputText,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {

        return new TokenHierarchyOperation<I,T>(inputText, copyInputText,
                language, skipTokenIds, inputAttributes).tokenHierarchy();
    }

    /**
     * Create token hierarchy for the given reader.
     *
     * @param inputReader input reader containing the characters to tokenize.
     * @param language language defining how the input
     *  will be tokenized.
     * @param skipTokenIds set containing the token ids for which the tokens
     *  should not be created in the created token hierarchy.
     *  <br/>
     *  <code>null</code> may be passed which means that no tokens will be skipped.
     *  <br/>
     *  This applies to top level of the token hierarchy only (not to embedded tokens).
     *  <br/>
     *  The provided set should be efficient enough - ideally created by e.g.
     *  {@link Language#tokenCategoryMembers(String)}
     *  or {@link Language#merge(Collection,Collection)}.
     *
     * @param inputAttributes additional properties related to the input
     *  that may influence token creation or lexer operation
     *  for the particular language (such as version of the language to be used).
     * @return non-null token hierarchy.
     */
    public static <I extends Reader, T extends TokenId> TokenHierarchy<I> create(
    I inputReader, Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {

        return new TokenHierarchyOperation<I,T>(inputReader,
                language, skipTokenIds, inputAttributes).tokenHierarchy();
    }
    

    private final TokenHierarchyOperation<I,?> operation;

    TokenHierarchy(TokenHierarchyOperation<I,?> operation) {
        this.operation = operation;
    }

    /**
     * Get token sequence of the top level language of the token hierarchy.
     * <br/>
     * For token hierarchies over mutable input sources the input source must be read-locked.
     * <br/>
     * The token sequences for inner levels of the token hierarchy can be
     * obtained by calling {@link TokenSequence#embedded()}.
     *
     * @return token sequence of the top level of the token hierarchy
     *  or null if the token hierarchy is currently inactive ({@link #isActive()} returns false).
     */
    public TokenSequence<?> tokenSequence() {
        return operation.tokenSequence();
    }

    /**
     * Get token sequence of the top level of the language hierarchy
     * only if it's of the given language.
     *
     * @return non-null token sequence or null if the hierarchy is active
     *  and its top level token sequence satisfies the condition
     *  <code>(tokenSequence().language() == language)</code>.
     *  <br/>
     *  Null is returned otherwise.
     */
    public <T extends TokenId> TokenSequence<T> tokenSequence(Language<T> language) {
        @SuppressWarnings("unchecked")
        TokenSequence<T> ts = (TokenSequence<T>)operation.tokenSequence(language);
        return ts;
    }
    
    /**
     * Get immutable list of token sequences with the given language path
     * from this hierarchy.
     * <br/>
     * For mutable token hierarchies the method should only be invoked
     * within read-locked input source. A new list should be
     * obtained after each modification.
     * {@link java.util.ConcurrentModificationException} may be thrown
     * when iterating over (or retrieving items) from the obsolete list.
     * <br/>
     * For forward exploration of the list the iterator is preferred over
     * index-based iteration because the list contents can be constructed lazily.
     * 
     * @param languagePath non-null language path that the obtained token sequences
     *  will all have.
     * @param startOffset starting offset of the TSs to get. Use 0 for no limit.
     *  If the particular TS ends after this offset then it will be returned.
     * @param endOffset ending offset of the TS to get. Use Integer.MAX_VALUE for no limit.
     *  If the particular TS starts before this offset then it will be returned.
     * @return non-null list of <code>TokenSequence</code>s or null if the token hierarchy
     *  is inactive ({@link #isActive()} returns false).
     */
    public List<TokenSequence<?>> tokenSequenceList(
    LanguagePath languagePath, int startOffset, int endOffset) {
        return operation.tokenSequenceList(languagePath, startOffset, endOffset);
    }

    /**
     * Gets the list of all embedded <code>TokenSequence</code>s at the given offset.
     * This method will use the top level <code>TokenSequence</code> in this
     * hierarchy to drill down through the token at the specified <code>offset</code>
     * and all its possible embedded sub-sequences.
     * 
     * <p>If the <code>offset</code>
     * lies at the border between two tokens the <code>backwardBias</code>
     * parameter will be used to choose either the token on the left hand side
     * (<code>backwardBias == true</code>) of the <code>offset</code> or
     * on the right hand side (<code>backwardBias == false</code>).
     * 
     * <p>
     * For token hierarchies over mutable input sources this method must only be invoked
     * within a read-lock over the mutable input source.
     * </p>
     * 
     * @param offset The offset to look at.
     * @param backwardBias If <code>true</code> the backward lying token will
     *   be used in case that the <code>offset</code> specifies position between
     *   two tokens. If <code>false</code> the forward lying token will be used.
     * 
     * @return The list of all sequences embedded at the given offset. The list
     *   may be empty if there are no tokens in the top level <code>TokenSequence</code>
     *   at the given offset and in the specified direction or if the token hierarchy
     *   is inactive ({@link #isActive()} returns false).
     *   The sequences in the list are ordered from the top level sequence to the bottom one.
     * 
     * @since 1.20
     */
    public List<TokenSequence<?>> embeddedTokenSequences(
        int offset, boolean backwardBias
    ) {
        return operation.embeddedTokenSequences(offset, backwardBias);
    }
    
    /**
     * Get a set of language paths used by this token hierarchy.
     * <br/>
     * The set includes "static" paths that are those reachable by traversing
     * token ids of the top language and searching for the default embeddings
     * that could be created by
     * {@link org.netbeans.spi.lexer.LanguageHierarchy#embedding(Token,LanguagePath,InputAttributes)}.
     *
     * <p>
     * For token hierarchies over mutable input sources this method must only be invoked
     * within a read-lock over the mutable input source.
     * </p>
     * 
     * 
     * @return non-null set of language paths. The set will be empty
     *  if the token hierarchy is inactive ({@link #isActive()} returns false).
     */
    public Set<LanguagePath> languagePaths() {
        return operation.languagePaths();
    }

    /**
     * Whether input text of this token hierarchy is mutable or not.
     *
     * @return true if the input text is mutable or false otherwise.
     */
    public boolean isMutable() {
        return operation.isMutable();
    }
    
    /**
     * Get input source providing text over which
     * this token hierarchy was constructed.
     * <br/>
     * It may be {@link java.lang.CharSequence} or {@link java.io.Reader}
     * or a mutable input source such as swing text document
     * {@link javax.swing.text.Document}.
     *
     * @return non-null input source.
     */
    public I inputSource() {
        return operation.inputSource();
    }
    
    /**
     * Token hierarchy may be set inactive to release resources consumed
     * by tokens.
     * <br/>
     * Only token hierarchies over a mutable input can become inactive.
     * <br/>
     * When inactive the hierarchy does not hold any tokens and
     * {@link #tokenSequence()} return null.
     *
     * <p>
     * For token hierarchies over mutable input sources this method must only be invoked
     * within a read-lock over the mutable input source.
     * </p>
     * 
     * @return true if valid tokens exist for this hierarchy
     *  or false if the token hierarchy is inactive and there are currently
     *  no active tokens to represent it.
     */
    public boolean isActive() {
        return operation.isActive();
    }
    
    /**
     * Add listener for token changes inside this hierarchy.
     *
     * @param listener token change listener to be added.
     */
    public void addTokenHierarchyListener(TokenHierarchyListener listener) {
        operation.addTokenHierarchyListener(listener);
    }
    
    /**
     * Remove listener for token changes inside this hierarchy.
     *
     * @param listener token change listener to be removed.
     */
    public void removeTokenHierarchyListener(TokenHierarchyListener listener) {
        operation.removeTokenHierarchyListener(listener);
    }
    
    /**
     * Obtaining of token hierarchy operation is only intended to be done
     * by package accessor.
     */
    TokenHierarchyOperation<I,?> operation() {
        return operation;
    }
    
    @Override
    public String toString() {
        return operation.toString();
    }

}
