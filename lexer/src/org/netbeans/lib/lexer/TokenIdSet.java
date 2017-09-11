/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

