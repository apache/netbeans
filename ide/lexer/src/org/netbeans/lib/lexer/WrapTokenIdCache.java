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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 * Cache for various WrapTokenId instances.
 *
 * @author Miloslav Metelka
 */
public class WrapTokenIdCache<T extends TokenId> {
    
    private static final int MAX_LAST_CACHED_IDS = 4;

    /**
     * Weak references to caches indexed by id of the language (root language).
     */
    private static Reference<WrapTokenIdCache<?>>[] cacheRefs;
    
    private static int id(Language<?> language) {
        return LexerApiPackageAccessor.get().languageId(language);
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T extends TokenId> WrapTokenIdCache<T> get(Language<T> language) {
        int lid = id(language);
        if (cacheRefs == null || lid >= cacheRefs.length) {
            Reference<WrapTokenIdCache<?>>[] n = (Reference<WrapTokenIdCache<?>>[])
                    new Reference[lid + 1];
            if (cacheRefs != null) {
                System.arraycopy(cacheRefs, 0, n, 0, cacheRefs.length);
            }
            cacheRefs = n;
        }
        Reference<WrapTokenIdCache<?>> cacheRef = cacheRefs[lid];
        WrapTokenIdCache<T> cache;
        if (cacheRef == null || (cache = (WrapTokenIdCache<T>) cacheRef.get()) == null) {
            cache = new WrapTokenIdCache<T>(language);
            cacheRefs[lid] = new WeakReference<WrapTokenIdCache<?>>(cache);
        }
        return cache;
    }
    
    /**
     * Prevent GC of the language (when holding the cache by TokenHierarchyOp)
     * to guarantee stability of language's id.
     */
    private final Language<T> language;

    /**
     * Cached wrap ids with null languageIds indexed by token-id's ordinal.
     */
    private final WrapTokenId<T>[] plainWids;
    
    /**
     * Cached wrap ids with null default embedding languageIds instance
     * indexed by token-id's ordinal.
     */
    private final WrapTokenId<T>[] noDefaultEmbeddingWids;
    
    /**
     * Array of most used wrap ids for a given language
     * (indexed by token-id's ordinal).
     */
    private final WrapTokenId<T>[][] lastWids;
    
    @SuppressWarnings("unchecked")
    private WrapTokenIdCache(Language<T> language) {
        this.language = language;
        int arrayLen = language.maxOrdinal() + 1;
        plainWids = new WrapTokenId[arrayLen];
        noDefaultEmbeddingWids = new WrapTokenId[arrayLen];
        for (int i = 0; i < arrayLen; i++) {
            T id = language.tokenId(i);
            plainWids[i] = new WrapTokenId<T>(id);
            noDefaultEmbeddingWids[i] = new WrapTokenId<T>(id, LanguageIds.NULL_LANGUAGE_ONLY);
        }
        lastWids = new WrapTokenId[arrayLen][];
    }

    /**
     * Get wrap token id either plain or with proper failed embedding language id(s).
     *
     * @param id non-null token id.
     * @param failedEmbeddingLanguageIds either null or ids of languages where embedding failed
     *  for the token with the given wid.
     * @return either cached or a fresh instance of wrap token id.
     */
    public WrapTokenId<T> findWid(T id, LanguageIds failedEmbeddingLanguageIds) {
        if (failedEmbeddingLanguageIds == LanguageIds.EMPTY) {
            return plainWid(id);
        } else if (failedEmbeddingLanguageIds == LanguageIds.NULL_LANGUAGE_ONLY) {
            return noDefaultEmbeddingWid(id);
        } else {
            return wid(id, failedEmbeddingLanguageIds);
        }
    }
    
    public WrapTokenId<T> plainWid(T id) {
        return plainWids[id.ordinal()];
    }

    public WrapTokenId<T> noDefaultEmbeddingWid(T id) {
        return noDefaultEmbeddingWids[id.ordinal()];
    }

    public WrapTokenId<T> wid(T id, LanguageIds failedEmbeddingLanguageIds) {
        WrapTokenId<T>[] ids = lastWids[id.ordinal()];
        if (ids == null) {
            @SuppressWarnings("unchecked")
            WrapTokenId<T>[] idsL = (WrapTokenId<T>[]) new WrapTokenId[MAX_LAST_CACHED_IDS];
            ids = idsL;
            lastWids[id.ordinal()] = ids;
        }
        for (int i = 0; i < MAX_LAST_CACHED_IDS; i++) {
            WrapTokenId<T> wid = ids[i];
            if (wid == null) {
                break;
            }
            if (wid.languageIds() == failedEmbeddingLanguageIds) { // LanguageIds can be compared by ==
                 if (i != 0) { // Move to index 0
                     System.arraycopy(ids, 0, ids, 1, i);
                     ids[0] = wid;
                 }
                 return wid;
            }
        }
        System.arraycopy(ids, 0, ids, 1, MAX_LAST_CACHED_IDS - 1);
        WrapTokenId<T> wid = new WrapTokenId<T>(id, failedEmbeddingLanguageIds);
        ids[0] = wid;
        return wid;
    }

}
