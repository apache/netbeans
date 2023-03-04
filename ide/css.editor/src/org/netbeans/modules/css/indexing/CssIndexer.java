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
package org.netbeans.modules.css.indexing;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.indexing.api.CssIndexModel;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Css content indexer
 *
 * @author mfukala@netbeans.org
 */
public class CssIndexer extends EmbeddingIndexer {

    /**
     * For firing index changes out of the parsing thread.
     */
    private static final RequestProcessor RP = new RequestProcessor();

    private static final Logger LOGGER = Logger.getLogger(CssIndexer.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static final String CSS_CONTENT_KEY = "cssContent"; //NOI18N
    public static final String IMPORTS_KEY = "imports"; //NOI18N
    public static final String IDS_KEY = "ids"; //NOI18N
    public static final String CLASSES_KEY = "classes"; //NOI18N
    public static final String HTML_ELEMENTS_KEY = "htmlElements"; //NOI18N
    public static final String COLORS_KEY = "colors"; //NOI18N

    public static final char VIRTUAL_ELEMENT_MARKER = '!'; //NOI18N

    //used during the indexing (content is mutable)
    private static final Map<FileObject, AtomicLong> importsHashCodes = new HashMap<>();

    //final version used after the indexing finishes (immutable)
    private static Map<FileObject, AtomicLong> computedImportsHashCodes = new HashMap<>();

    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        try {
            if(LOG) {
                FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
                LOGGER.log(Level.FINE, "indexing {0}", fo.getPath()); //NOI18N
            }

            CssParserResult result = (CssParserResult) parserResult;
            CssFileModel model = CssFileModel.create(result);
            IndexingSupport support = IndexingSupport.getInstance(context);
            IndexDocument document = support.createDocument(indexable);

            storeEntries(model.getIds(), document, IDS_KEY);
            storeEntries(model.getClasses(), document, CLASSES_KEY);
            storeEntries(model.getHtmlElements(), document, HTML_ELEMENTS_KEY);
            storeEntries(model.getColors(), document, COLORS_KEY);

            //support for caching the file dependencies
            int entriesHashCode = storeEntries(model.getImports(), document, IMPORTS_KEY);
            FileObject root = context.getRoot();
            synchronized(importsHashCodes) {
                AtomicLong aggregatedHash = importsHashCodes.get(root);
                if (aggregatedHash == null) {
                    aggregatedHash = new AtomicLong(0);
                    importsHashCodes.put(root, aggregatedHash);
                }
                aggregatedHash.set(aggregatedHash.get() * 79 + entriesHashCode);
            }

            //this is a marker key so it's possible to find
            //all stylesheets easily
            document.addPair(CSS_CONTENT_KEY, Boolean.TRUE.toString(), true, true);
            //CssIndexModel support
            Collection<CssIndexModel> indexModels = CssIndexModelSupport.getModels(result);
            for(CssIndexModel indexModel : indexModels) {
                indexModel.storeToIndex(document);
            }

            support.addDocument(document);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    //1. no synchronization on the computedImportsHashCodes!
    //2. the callers of this method will get old results if an indexing is in progress and 
    //   if the cached hashcode changes - possibly add some kind of synchronization 
    //   to that call (but it seems too much error prone to me)
    public static long getImportsHashCodeForRoots(Collection<FileObject> roots) {
        long hash = 5;
        for(FileObject root : roots) {
            AtomicLong rootHash = computedImportsHashCodes.get(root);
            if(rootHash != null) {
                hash = hash * 51 + rootHash.longValue();
            }
        }
        return hash;
    }

    private int storeEntries(Collection<Entry> entries, IndexDocument doc, String key) {
        if (!entries.isEmpty()) {

            //eliminate duplicated entries
            Set<String> entryStrings = new TreeSet<>();
            for(Entry entry : entries) {
                if(entry.isVirtual()) {
                    entryStrings.add(entry.getName() + VIRTUAL_ELEMENT_MARKER);
                } else {
                    entryStrings.add(entry.getName());
                }
            }

            for(String e: entryStrings) {
                doc.addPair(key, e, true, true);
            }

            return entryStrings.hashCode();
        }
        return 0;
    }

    private static void fireChange(final FileObject fo) {
        // handle events firing in separate thread:
        RP.post(() -> fireChangeImpl(fo));
    }

    private static void fireChangeImpl(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p == null) {
            // no project to notify
            return;
        }
        try {
            CssIndex index = CssIndex.get(p);
            if (index != null) {
                index.notifyChange();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "css"; //NOI18N
        public static final int VERSION = 5;

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(snapshot)) {
                return new CssIndexer();
            } else {
                return null;
            }
        }

        @Override
        public boolean scanStarted(Context context) {
            synchronized(importsHashCodes) {
                importsHashCodes.remove(context.getRoot()); //remove the computed hashcode for the given indexing root
            }
            return super.scanStarted(context);
        }

        @Override
        public void scanFinished(Context context) {
            synchronized(importsHashCodes) {
                computedImportsHashCodes = new HashMap<>(importsHashCodes); //shallow copy
            }
            FileObject root = context.getRoot();
            if(root != null) {
                fireChange(root);
            }
            super.scanFinished(context);
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : deleted) {
                    is.removeDocuments(i);
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : dirty) {
                    is.markDirtyDocuments(i);
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

        private boolean isIndexable(Snapshot snapshot) {
            //index all files possibly containing css
            return CssLanguage.CSS_MIME_TYPE.equals(snapshot.getMimeType());
        }
    }
}
