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

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.impl.indexing.ClusteredIndexables;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex2;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class DocumentBasedIndexManager {

    private static DocumentBasedIndexManager instance;

    //@GuardedBy("this")
    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS",
    justification="URLs have never host part")
    private final Map<URL, Pair<DocumentIndex2.Transactional, DocumentIndexCache>> indexes =
            new HashMap<URL, Pair<DocumentIndex2.Transactional, DocumentIndexCache>> ();
    //@GuardedBy("this")
    private boolean closed;

    private DocumentBasedIndexManager() {}


    public static enum Mode {
        OPENED,
        CREATE,
        IF_EXIST;
    }


    public static synchronized DocumentBasedIndexManager getDefault () {
        if (instance == null) {
            instance = new DocumentBasedIndexManager();
        }
        return instance;
    }

   @CheckForNull
   @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS",
    justification="URLs have never host part")
    public synchronized DocumentIndex2.Transactional getIndex (final URL root, final Mode mode) throws IOException {
        assert root != null;
        assert PathRegistry.noHostPart(root) : root;
        if (closed) {
            return null;
        }
        Pair<DocumentIndex2.Transactional, DocumentIndexCache> li = indexes.get(root);
        if (li == null) {
            try {
                switch (mode) {
                    case CREATE:
                    {
                        final File file = BaseUtilities.toFile(root.toURI());
                        file.mkdir();
                        final DocumentIndexCache cache = ClusteredIndexables.createDocumentIndexCache();
                        final DocumentIndex2.Transactional index = (DocumentIndex2.Transactional) IndexManager.createTransactionalDocumentIndex(file, cache);
                        li = Pair.<DocumentIndex2.Transactional, DocumentIndexCache>of(index, cache);

                        indexes.put(root,li);
                        break;
                    }
                    case IF_EXIST:
                    {
                        final File file = BaseUtilities.toFile(root.toURI());
                        String[] children;
                        if (file.isDirectory() && (children=file.list())!= null && children.length > 0) {
                            final DocumentIndexCache cache = ClusteredIndexables.createDocumentIndexCache();
                            final DocumentIndex2.Transactional index = (DocumentIndex2.Transactional) IndexManager.createTransactionalDocumentIndex(file, cache);
                            li = Pair.<DocumentIndex2.Transactional, DocumentIndexCache>of(index, cache);
                            indexes.put(root,li);
                        }
                        break;
                    }
                }
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
        return li == null ? null : li.first();
    }

   @CheckForNull
   public synchronized DocumentIndexCache getCache(@NonNull final URL root) {
       final Pair<DocumentIndex2.Transactional, DocumentIndexCache> entry = indexes.get(root);
       return entry == null ? null : entry.second();
   }

   @CheckForNull
   public synchronized DocumentIndex2.Transactional getIndex(@NonNull final DocumentIndexCache cache) {
       Parameters.notNull("cache", cache);  //NOI18N
       for (Pair<DocumentIndex2.Transactional,DocumentIndexCache> e : indexes.values()) {
           if (cache.equals(e.second())) {
               return e.first();
           }
       }
       return null;
   }
   
   public synchronized void close() {
       if (closed) {
           return;
       }
       closed = true;
       for (Pair<DocumentIndex2.Transactional, DocumentIndexCache> index : indexes.values()) {
           try {
            index.first().close();
           } catch (IOException ioe) {
               Exceptions.printStackTrace(ioe);
           }
       }
   }

}
