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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenSequence;

/**
 * List of token lists that collects all token lists for a given language path.
 *
 * @author Miloslav Metelka
 */

public final class TokenSequenceList extends AbstractList<TokenSequence<?>> {
    
    private TokenList<?> rootTokenList;

    private final TokenListList<?> tokenListList;
    
    private final List<TokenSequence<?>> tokenSequences;

    private final int endOffset;
    
    private final int rootModCount;
    
    /**
     * Index of the last item retrieved from tokenListList.
     * It may be equal to Integer.MAX_VALUE when searching thgroughout the token lists
     * was finished.
     */
    private int tokenListIndex;
    
    public TokenSequenceList(TokenList<?> rootTokenList, LanguagePath languagePath,
    int startOffset, int endOffset) {
        this.rootTokenList = rootTokenList;
        this.endOffset = endOffset;
        this.rootModCount = rootTokenList.modCount();

        if (languagePath.size() == 1) { // Is supported too
            tokenListList = null;
            tokenListIndex = Integer.MAX_VALUE; // Mark no mods to tokenSequences
            if (rootTokenList.languagePath() == languagePath) {
                TokenList<?> tl = checkWrapTokenList(rootTokenList, startOffset, endOffset);
                TokenSequence<?> rootTS = LexerApiPackageAccessor.get().createTokenSequence(tl);
                tokenSequences = Collections.<TokenSequence<?>>singletonList(rootTS);
            } else {
                tokenSequences = Collections.emptyList();
            }

        } else { // languagePath.size() >= 2
            // It's possible that someone requests languagePath with the top language
            //   different than the one of token hierarchy (actually happens).
            // Then there was a multiple-ETL-initialization problem since TLL does not check
            //   the top language.
            if (rootTokenList.language() != languagePath.topLanguage()) {
                tokenListList = null;
                tokenListIndex = Integer.MAX_VALUE; // Mark no mods to tokenSequences
                tokenSequences = Collections.emptyList();
            } else {
                this.tokenListList = rootTokenList.tokenHierarchyOperation().tokenListList(languagePath);
                // Possibly skip initial token lists accroding to startOffset
                int size = tokenListList.size();
                int high = size - 1;
                // Find the token list which has the end offset above or equal to the requested startOffset
                EmbeddedTokenList<?,?> firstTokenList;
                if (startOffset > 0) {
                    while (tokenListIndex <= high) {
                        int mid = (tokenListIndex + high) / 2;
                        EmbeddedTokenList<?,?> etl = tokenListList.get(mid);
                        // Update end offset before querying
                        etl.updateModCount(rootModCount);
                        int tlEndOffset = etl.endOffset(); // updateStatusImpl() just called
                        if (tlEndOffset < startOffset) {
                            tokenListIndex = mid + 1;
                        } else if (tlEndOffset > startOffset) {
                            high = mid - 1;
                        } else { // tl ends exactly at start offset
                            tokenListIndex = mid + 1; // take the first above this
                            break;
                        }
                    }
                    // If not found exactly -> take the higher one (index variable)
                    firstTokenList = tokenListList.getOrNull(tokenListIndex);
                    if (tokenListIndex == size) { // Right above the ones that existed at begining of bin search
                        while (firstTokenList != null) {
                            firstTokenList.updateModCount(rootModCount);
                            if (firstTokenList.endOffset() >= startOffset) { // updateStatusImpl() just called
                                break;
                            }
                            firstTokenList = tokenListList.getOrNull(++tokenListIndex);
                        }
                    }

                } else { // startOffset == 0
                    firstTokenList = tokenListList.getOrNull(0);
                }

                if (firstTokenList != null) {
                    firstTokenList.updateModCount(rootModCount);
                    tokenSequences = new ArrayList<TokenSequence<?>>(4);
                    TokenSequence<?> ts = LexerApiPackageAccessor.get().createTokenSequence(
                            checkWrapTokenList(firstTokenList, startOffset, endOffset));
                    tokenSequences.add(ts);

                } else {// firstTokenList == null
                    tokenSequences = Collections.emptyList();
                    tokenListIndex = Integer.MAX_VALUE; // No token sequences at all
                }
            }
        }
    }
    
    private TokenList<?> checkWrapTokenList(TokenList<?> tokenList, int startOffset, int endOffset) {
        // Expected that updateStatusImpl() was just called
        boolean wrapStart = ((startOffset > 0)
                && (tokenList.startOffset() < startOffset)
                && (startOffset < tokenList.endOffset()));
        boolean wrapEnd = ((endOffset != Integer.MAX_VALUE)
                && (tokenList.startOffset() < endOffset)
                && (endOffset < tokenList.endOffset()));
        if (wrapStart || wrapEnd) // Must create sub sequence
            tokenList = SubSequenceTokenList.create(
                    tokenList, startOffset, endOffset);
        if (wrapEnd) { // Also this will be the last one list
            tokenListIndex = Integer.MAX_VALUE;
        }
        return tokenList;
    }
    
    @Override
    public Iterator<TokenSequence<?>> iterator() {
        return new Itr();
    }
    
    public TokenSequence<?> get(int index) {
        findTokenSequenceWithIndex(index);
        return tokenSequences.get(index); // Will fail naturally if index too high
    }
    
    public TokenSequence<?> getOrNull(int index) {
        findTokenSequenceWithIndex(index);
        return (index < tokenSequences.size()) ? tokenSequences.get(index) : null;
    }

    public int size() {
        findTokenSequenceWithIndex(Integer.MAX_VALUE);
        return tokenSequences.size();
    }

    private void findTokenSequenceWithIndex(int index) {
        while (index >= tokenSequences.size() && tokenListIndex != Integer.MAX_VALUE) {
            EmbeddedTokenList<?,?> etl = tokenListList.getOrNull(++tokenListIndex);
            if (etl != null) {
                etl.updateModCount();
                if (endOffset == Integer.MAX_VALUE || etl.startOffset() < endOffset) {
                    boolean wrapEnd = ((endOffset != Integer.MAX_VALUE)
                            && (etl.startOffset() < endOffset)
                            && (endOffset < etl.endOffset()));
                    if (wrapEnd) {
                        tokenSequences.add(LexerApiPackageAccessor.get().createTokenSequence(
                                SubSequenceTokenList.create(etl, 0, endOffset)));
                        tokenListIndex = Integer.MAX_VALUE;
                    } else {
                        tokenSequences.add(LexerApiPackageAccessor.get().createTokenSequence(etl));
                    }
                } else {
                    tokenListIndex = Integer.MAX_VALUE;
                }
            } else { // Singnal no more token sequences
                tokenListIndex = Integer.MAX_VALUE;
            }
        }
    }
    
    void checkForComodification() {
        if (rootModCount != rootTokenList.modCount())
            throw new ConcurrentModificationException(
                    "Caller uses obsolete TokenSequenceList: expectedModCount=" + rootModCount + // NOI18N
                    " != modCount=" + rootTokenList.modCount()
            );
    }

    @Override
    public String toString() {
        return tokenListList.toString();
    }

    private class Itr implements Iterator<TokenSequence<?>> {
        
        private int cursor = 0;
        
        private TokenSequence<?> next;
        
        public boolean hasNext() {
            checkFetchNext();
            return (next != null);
        }
        
        public TokenSequence<?> next() {
            checkFetchNext();
            if (next == null)
                throw new NoSuchElementException();
            TokenSequence<?> ret = next;
            next = null;
            return ret;
        }
        
        private void checkFetchNext() {
            if (next == null) {
                checkForComodification();
                next = getOrNull(cursor++); // can increase cursor even if (next == null)
            }
        }
        
        public void remove() {
            throw new UnsupportedOperationException(); // underlying list is immutable
        }
        
    }
    
}
