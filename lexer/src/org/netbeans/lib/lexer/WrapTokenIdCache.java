/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
