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

import org.netbeans.api.lexer.Language;

/**
 * Array of one or more language ids.
 *
 * @author Miloslav Metelka
 */
public final class LanguageIds {
    
    private static final LanguageIds[] EMPTY_ARR = new LanguageIds[0];
    
    /**
     * Language ids instance containing no languages.
     */
    public static final LanguageIds EMPTY = new LanguageIds(EMPTY_ARR, 0);
    
    /**
     * Language ids instance containing a single null language.
     */
    public static final LanguageIds NULL_LANGUAGE_ONLY = get(0);
    
    /**
     * Get ids containing just the given language.
     *
     * @param language language or null.
     * @return a (cached) instance that contains only the given language.
     */
    public static LanguageIds get(Language<?> language) {
        return getImpl(id(language));
    }

    /**
     * Get ids containing just the given language.
     *
     * @param languageId language id or 0 which marks null language.
     * @return a (cached) instance that contains only the given language id.
     */
    public static LanguageIds get(int languageId) {
        checkLanguageId(languageId);
        return getImpl(languageId);
    }

    private static LanguageIds getImpl(int id) {
        return EMPTY.getExtended(id);
    }

    /**
     * Get ids extended by the given language id.
     *
     * @param ids non-null ids. The instance will remain unchanged.
     * @param language language or null. If language is already part of ids the original ids
     *  parameter will be returned.
     * @return a (cached) instance that contains all language in ids plus the given language.
     */
    public static LanguageIds get(LanguageIds ids, Language<?> language) {
        return getImpl(ids, id(language));
    }

    /**
     * Get ids extended by the given language.
     *
     * @param ids non-null ids. The instance will remain unchanged.
     * @param language language id or 0 for null language. If language id
     *  is already part of ids the original ids parameter will be returned.
     * @return a (cached) instance that contains all language ids in ids plus the given language id.
     */
    public static LanguageIds get(LanguageIds ids, int languageId) {
        checkLanguageId(languageId);
        return getImpl(ids, languageId);
    }

    private static LanguageIds getImpl(LanguageIds ids, int id) {
        return ids.getExtended(id);
    }
    
    /**
     * Get ids with the given language removed.
     *
     * @param ids non-null ids. The instance will remain unchanged.
     * @param removedLanguage language or null to remove from ids. If language is not part of ids
     *  the original ids parameter will be returned.
     * @return a (cached) instance that contains all language in ids minus the given language.
     *  If ids contained just a single language then NO_LANGUAGES will be returned.
     */
    public static LanguageIds getRemoved(LanguageIds ids, Language<?> removedLanguage) {
        return getRemovedImpl(ids, id(removedLanguage));
    }

    /**
     * Get ids with the given language id removed.
     *
     * @param ids non-null ids. The instance will remain unchanged.
     * @param removedLanguageId language id or 0 (for null language) to remove from ids.
     *  If removed language id is not part of ids the original ids parameter will be returned.
     * @return a (cached) instance that contains all language in ids minus the given language id.
     *  If ids contained just a single language then NO_LANGUAGES will be returned.
     */
    public static LanguageIds getRemoved(LanguageIds ids, int removedLanguageId) {
        checkLanguageId(removedLanguageId);
        return getRemovedImpl(ids, removedLanguageId);
    }

    private static LanguageIds getRemovedImpl(LanguageIds ids, int removedId) {
        return ids.getRemoved(removedId);
    }
    
    private static void checkLanguageId(int languageId) {
        if (languageId < 0) {
            throw new IllegalArgumentException("Invalid id=" + languageId + " < 0"); // NOI18N
        }
    }

    private static LanguageIds create(int id) {
        LanguageIds[] ids = new LanguageIds[id + 1];
        ids[id] = EMPTY; // Parent ids
        return new LanguageIds(ids, id);
    }
    
    private static LanguageIds create(LanguageIds ids, int id) {
//        assert (id > ids.ids.length) : "Attempt to add language with lower id=" + id; // NOI18N
        LanguageIds[] arr = new LanguageIds[id + 1];
        LanguageIds[] parentArr = ids.arr;
        System.arraycopy(parentArr, 0, arr, 0, parentArr.length);
        arr[id] = ids; // Parent ids
        int hash = (ids.hashCode() << 5) ^ id;
        return new LanguageIds(arr, hash);
    }
    
    static int id(Language language) {
        return (language != null) ? LexerApiPackageAccessor.get().languageId(language) : 0;
    }

    /**
     * Language containment is determined by checking whether arr[id] != null.
     * Last member of the array is always != null and contains index of next contained index.
     * First contained language's index contains NO_LANGUAGES.
     */
    private final LanguageIds[] arr;
    
    private final int hash;
    
    /**
     * Extensions of this 
     */
    private LanguageIds[] ext = EMPTY_ARR;
    
    private LanguageIds(LanguageIds[] arr, int hash) {
        this.arr = arr;
        this.hash = hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }
    
    public boolean equals(Object o) {
        return (o == this);
    }
    
    public boolean containsLanguage(Language<?> language) {
        return containsId(id(language));
    }
    
    public boolean containsId(int id) {
        return id < arr.length && arr[id] != null;
    }
    
    public int maxId() {
        return arr.length - 1;
    }
    
    private LanguageIds getExtended(int id) {
        LanguageIds ids;
        if (id < arr.length) {
            if (arr[id] != null) { // Already contained
                ids = this;
            } else {
                // Find nearest with lower highest ID (go up from id + 1)
                int i = id + 1;
                while ((ids = arr[i]) == null) { // Surely stops at arr.length - 1
                    i++;
                }
                ids = ids.getExtended(id);
                // i points at the first extra language
                while (i < arr.length) {
                    if (arr[i] != null) {
                        ids = ids.getExtended(i);
                    }
                    i++;
                }
            }
        } else {
            int extIndex = id - arr.length;
            synchronized (this) {
                if (extIndex >= ext.length) {
                    LanguageIds[] n = new LanguageIds[extIndex + 1];
                    System.arraycopy(ext, 0, n, 0, ext.length);
                    ext = n;
                }
                ids = ext[extIndex];
                if (ids == null) {
                    ids = create(this, id);
                    ext[extIndex] = ids;
                }
            }
        }
        return ids;
    }

    private LanguageIds getRemoved(int removedId) {
        LanguageIds ids;
        if (removedId < arr.length) {
            if ((ids = arr[removedId]) == null) { // Already removed
                ids = this;
            } else {
                for (int i = removedId + 1; i < arr.length; i++) {
                    if (arr[i] != null) {
                        ids = ids.getExtended(i);
                    }
                }
            }
        } else {
            ids = this;
        }
        return ids;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        if (this == EMPTY) {
            sb.append("EMPTY");
        } else if (this == NULL_LANGUAGE_ONLY) {
            sb.append("NULL_LANGUAGE_ONLY");
        } else {
            sb.append("LanguageIds");
        }
        sb.append(":\n");
        for (int i = 0; i < arr.length; i++) {
            sb.append("  arr[").append(i).append("]=").append(containsId(i)).append('\n');
        }
        sb.append("  hash=").append(hash);
        sb.append(", ext.length=").append(ext.length);
        return sb.toString();
    }

}
