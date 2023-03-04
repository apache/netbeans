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

import java.util.Collection;
import java.util.Iterator;
import java.util.AbstractSet;
import java.util.NoSuchElementException;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 * A set of token ids.
 * <br/>
 * It is immutable in terms of a collection mutability although physically
 * the set can be mutated.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenIdSet<T extends TokenId> extends AbstractSet<T> {

    /**
     * Find the maximum ordinal among the given token ids.
     */
    public static int findMaxOrdinal(Collection<? extends TokenId> ids) {
        int maxOrdinal = -1;
        for (TokenId id : ids) {
            maxOrdinal = Math.max(maxOrdinal, id.ordinal());
        }
        return maxOrdinal;
    }

    public static <T extends TokenId> void checkIdsFromLanguage(Collection<T> ids, Set<T> languageIds) {
        for (T id : ids) {
            if (id != null && !languageIds.contains(id)) {
                throw new IllegalArgumentException(id + "not contained in " + languageIds); // NOI18N
            }
        }
    }
    

    final T[] indexedIds;

    private int size = -1;

    /**
     * Create new token id set.
     * 
     * @param ids collection of token ids to be contained in this set. There may be nulls in the array
     *  and they will be skipped. All the ids must belong to the languageIds.
     * @param languageIds language ids used to verify that the passed ids
     *  really belong to the given language. It's also used to get the maximum
     *  ordinal of the language.
     */
    public TokenIdSet(Collection<T> ids, int maxOrdinal, boolean checkDupOrdinals) {
        indexedIds = allocateIds(maxOrdinal + 1);
        if (ids != null) {
            for (T id : ids) {
                if (id != null) {
                    if (checkDupOrdinals && indexedIds[id.ordinal()] != null) {
                        throw new IllegalStateException(id // NOI18N
                                + " has duplicate ordinal with " + indexedIds[id.ordinal()]); // NOI18N
                    }
                    indexedIds[id.ordinal()] = id;
                }
            }
        }
    }

    @SuppressWarnings("unchecked") private T[] allocateIds(int size) {
        return (T[])new TokenId[size];
    }

    public boolean add(T id) {
        T origId = indexedIds[id.ordinal()];
        indexedIds[id.ordinal()] = id;
        size = -1;
        return (origId != null);
    }

    public boolean remove(T id) {
        T origId = indexedIds[id.ordinal()];
        indexedIds[id.ordinal()] = null;
        size = -1;
        return (origId != null);
    }

    public T[] indexedIds() {
        return indexedIds;
    }

    public int size() {
        int cnt = size;
        if (cnt < 0) {
            // Compute size by iteration as both the constructor's and indexedIds arrays
            // may contain nulls.
            cnt = 0;
            for (Iterator it = iterator(); it.hasNext();) {
                it.next();
                cnt++;
            }
            size = cnt;
        }

        return cnt;
    }

    public Iterator<T> iterator() {
        return new SkipNullsIterator();
    }

    public boolean containsTokenId(TokenId id) {
        int ordinal = id.ordinal();
        return (ordinal >= 0 && ordinal < indexedIds.length && indexedIds[ordinal] == id);
    }

    public boolean contains(Object o) {
        return (o instanceof TokenId)
            ? containsTokenId((TokenId)o)
            : false;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        for (Iterator it = iterator(); it.hasNext();) {
            TokenId id = (TokenId) it.next();
            sb.append("    ");
            sb.append(LexerUtilsConstants.idToString(id));
            sb.append('\n');
        }
        sb.append("}\n");
        return sb.toString();
    }

    /** Iterator over an array that skips the null values. */
    private final class SkipNullsIterator implements Iterator<T> {
        
        private int index;

        private int lastRetIndex = -1;
        
        SkipNullsIterator() {
        }
        
        public boolean hasNext() {
            while (index < indexedIds.length) {
                if (indexedIds[index] != null) {
                    return true;
                }
                index++;
            }
            return false;
        }
        
        public T next() {
            while (index < indexedIds.length) {
                T tokenId = indexedIds[index++];
                if (tokenId != null) {
                    lastRetIndex = index - 1;
                    return tokenId;
                }
            }
            
            throw new NoSuchElementException();
        }
        
        public void remove() {
            if (lastRetIndex >= 0) {
                indexedIds[lastRetIndex] = null;
                size = -1;
            } else {
                throw new IllegalStateException(); // nothing returned yet
            }
        }
        
    }

}

