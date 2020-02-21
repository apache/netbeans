/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.impl.xref;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.openide.text.NbDocument;

/**
 *
 *
 */
public class ReferencesCache {

    static CsmObject UNRESOLVED = new CsmObject() {

        @Override
        public String toString() {
            return "FAKE REFERENCE"; // NOI18N
        }
    };
    private static final int MAX_CACHE_SIZE = 10;
    private final Object cacheLock = new CacheLock();

    private final static class CacheLock {};

    private LinkedHashMap<CsmFile, Map<TokenItem<TokenId>, CacheEntry>> cache = new LinkedHashMap<CsmFile, Map<TokenItem<TokenId> , CacheEntry>>();

    CsmObject getReferencedObject(CsmFile file, TokenItem<TokenId> token, long callTimeVersion) {
        synchronized (cacheLock) {
            Map<TokenItem<TokenId> , CacheEntry> entry = cache.get(file);
            CsmObject out = null;
            if (entry != null) {
                CacheEntry cacheEntry = entry.get(token);
                if (cacheEntry != null) {
                    out = cacheEntry.csmObject;
                    if (out == UNRESOLVED) {
                        long storedVersion = cacheEntry.fileVersion;
                        long fileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
                        if (fileVersion != storedVersion) {
                            entry.put(token, null);
                            out = null;
                        }
                    } else if (!CsmBaseUtilities.isValid(out)) {
                        // clean cache if remembered object become invalid
                        entry.put(token, null);
                        out = null;
                    }
                }
            }
            return out;
        }
    }

    void putReferencedObject(CsmFile file, TokenItem<TokenId> token, CsmObject object, long fileVersionOnStartResolving) {
        synchronized (cacheLock) {
            Map<TokenItem<TokenId>, CacheEntry> entry = cache.get(file);
            if (entry == null) {
                if (cache.size() > MAX_CACHE_SIZE) {
                    Entry<CsmFile, Map<TokenItem<TokenId>, CacheEntry>> next = cache.entrySet().iterator().next();
                    cache.remove(next.getKey());
                }
                entry = new HashMap<TokenItem<TokenId>, CacheEntry>();
                cache.put(file, entry);
            }
            CacheEntry cacheEntry = entry.get(token);
            if (cacheEntry == null) {
                cacheEntry = new CacheEntry(fileVersionOnStartResolving, object);
                entry.put(token, cacheEntry);
            } else {
                if (object == UNRESOLVED) {
                    long currentFileVersion = CsmFileInfoQuery.getDefault().getFileVersion(file);
                    if (fileVersionOnStartResolving != currentFileVersion) {
                        // we don't beleive in such fake
                        // System.err.println("skip caching FAKE NULL at " + offset + " in " + file);
                        return;
                    }
                }
                // replace only by newer version of resolved object
                if (cacheEntry.fileVersion < fileVersionOnStartResolving) {
                    cacheEntry = new CacheEntry(fileVersionOnStartResolving, object);
                    entry.put(token, cacheEntry);
                }
            }
        }
    }

    void clearFileReferences(CsmFile file) {
        synchronized (cacheLock) {
            if (file == null) {
                for (Iterator<Entry<CsmFile, Map<TokenItem<TokenId>, CacheEntry>>> it = cache.entrySet().iterator(); it.hasNext();) {
                    Entry<CsmFile, Map<TokenItem<TokenId>, CacheEntry>> entry = it.next();
                    CsmFile aFile = entry.getKey();
                    if (CsmFileInfoQuery.getDefault().isDocumentBasedFile(aFile)) {
                        clearUnresolved(aFile);
                    } else {
                        it.remove();
                    }
                }
            } else {
                if (CsmFileInfoQuery.getDefault().isDocumentBasedFile(file)) {
                    clearUnresolved(file);
                } else {
                    cache.remove(file);
                }
            }
        }
    }

    private void clearUnresolved(CsmFile file) {
        Map<TokenItem<TokenId>, CacheEntry> entry = cache.get(file);
        if (entry != null) {
            for (Iterator<Entry<TokenItem<TokenId>, CacheEntry>> it = entry.entrySet().iterator(); it.hasNext();) {
                Entry<TokenItem<TokenId>, CacheEntry> next = it.next();
                CacheEntry value = next.getValue();
                if (value == null || value.csmObject == UNRESOLVED) {
                    it.remove();
                }
            }
        }
    }

    void dumpInfo(PrintWriter printOut) {
        synchronized (cacheLock) {
            printOut.printf("cache of size %d%n", cache.size()); // NOI18N
            for (Map.Entry<CsmFile, Map<TokenItem<TokenId>, CacheEntry>> entry : cache.entrySet()) {
                final CsmFile file = entry.getKey();
                printOut.printf("-----------------------%n"); // NOI18N
                printOut.printf("file %s version=%d, class=%s%n", file.getAbsolutePath(), CsmFileInfoQuery.getDefault().getFileVersion(file), file.getClass().getName()); // NOI18N
                SortedMap<TokenItem<TokenId>, CacheEntry> unresolved = new TreeMap<TokenItem<TokenId>, CacheEntry>();
                SortedMap<TokenItem<TokenId>, CacheEntry> invalid = new TreeMap<TokenItem<TokenId>, CacheEntry>();
                for (Map.Entry<TokenItem<TokenId>, CacheEntry> entry1 : entry.getValue().entrySet()) {
                    CacheEntry value = entry1.getValue();
                    if (value != null) {
                        CsmObject csmObject = value.csmObject;
                        if (csmObject == UNRESOLVED) {
                            unresolved.put(entry1.getKey(), value);
                        } else if (!CsmBaseUtilities.isValid(csmObject)) {
                            invalid.put(entry1.getKey(), value);
                        }
                    }
                }
                if (unresolved.isEmpty()) {
                    printOut.printf("no UNRESOLVED %n");// NOI18N
                } else {
                    for (Map.Entry<TokenItem<TokenId>, CacheEntry> entry1 : unresolved.entrySet()) {
                        printOut.printf("UNRESOLVED [%s] version=%d%n", getPosition(entry1.getKey(), file), entry1.getValue().fileVersion);// NOI18N
                        CsmObject checkAgain = ReferencesSupport.findDeclaration(file, CsmReferenceRepository.getDocument(file), entry1.getKey(), entry1.getKey().offset());
                        if (checkAgain != null) {
                            printOut.printf("\t ERROR: resolved as [%s]%n", checkAgain);// NOI18N
                        }
                    }
                }
                if (invalid.isEmpty()) {
                    printOut.printf("no INVALID %n");// NOI18N
                } else {
                    for (Map.Entry<TokenItem<TokenId>, CacheEntry> entry1 : invalid.entrySet()) {
                        CsmObject csmObject = entry1.getValue().csmObject;
                        printOut.printf("INVALID [%s] version=%d %s%n", getPosition(entry1.getKey(), file), entry1.getValue().fileVersion, csmObject);// NOI18N
                        CsmObject checkAgain = ReferencesSupport.findDeclaration(file, CsmReferenceRepository.getDocument(file), entry1.getKey(), entry1.getKey().offset());
                        if (checkAgain != csmObject) {
                            printOut.printf("\t ERROR: invalid resolved as [%s]%n", checkAgain);// NOI18N
                        }
                    }
                }
            }
            printOut.printf("-----------------------%n");// NOI18N
        }
    }

    private String getPosition(TokenItem<TokenId> offset, CsmFile file) {
        BaseDocument document = CsmReferenceRepository.getDocument(file);
        StringBuilder out = new StringBuilder();
        out.append("offset=").append(offset.offset()); // NOI18N
        if (document instanceof StyledDocument) {
            int line = NbDocument.findLineNumber((StyledDocument) document, offset.offset()) + 1;
            out.append(", line=").append(line); // NOI18N
            int col = NbDocument.findLineColumn((StyledDocument) document, offset.offset()) + 1;
            out.append(", column=").append(col); // NOI18N
            TokenItem<TokenId> jumpToken;
            document.readLock();
            try {
                jumpToken = CndTokenUtilities.getTokenCheckPrev(document, offset.offset());
            } finally {
                document.readUnlock();
            }
            out.append(", tok=").append(jumpToken); // NOI18N
        }
        return out.toString();
    }

    private static final class CacheEntry {

        private final long fileVersion;
        private final CsmObject csmObject;

        public CacheEntry(long fileVersion, CsmObject csmObject) {
            this.fileVersion = fileVersion;
            this.csmObject = csmObject;
        }
    }
}
