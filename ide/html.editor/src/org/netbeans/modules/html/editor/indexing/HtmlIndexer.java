/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.editor.indexing;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.index.HtmlIndex;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
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
 * HTML content indexer.
 * 
 * TODO the file changes event should be aggregated and fire one event once the indexing finishes!!!
 *
 * @author mfukala@netbeans.org
 */
public class HtmlIndexer extends EmbeddingIndexer {

    private static final Logger LOGGER = Logger.getLogger(HtmlIndexer.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    private static RequestProcessor RP = new RequestProcessor();

    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        try {
            if(LOG) {
                FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
                LOGGER.log(Level.FINE, "indexing {0}", fo.getPath()); //NOI18N
            }

            HtmlFileModel model = new HtmlFileModel(parserResult, (HtmlParsingResult)parserResult);

            IndexingSupport support = IndexingSupport.getInstance(context);
            IndexDocument document = support.createDocument(indexable);

            storeEntries(model.getReferences(), document, HtmlIndex.REFERS_KEY);

            support.addDocument(document);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void fireChange(final FileObject fo) {
        // handle events firing in separate thread:
        RP.post(new Runnable() {
            @Override
            public void run() {
                fireChangeImpl(fo);
            }
        });
    }
    
    private static void fireChangeImpl(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p == null) {
            // no project to notify
            return;
        }
        try {
            HtmlIndex index = HtmlIndex.get(p, false);
            if (index != null) {
                index.notifyChange();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void storeEntries(Collection<? extends Entry> entries, IndexDocument doc, String key) {
        Set<String> names = new HashSet<>();
        entries.forEach(e -> names.add(e.getName()));
        storeEntries(names, doc, key);
    }

    private void storeEntries(Set<String> entries, IndexDocument doc, String key) {
        for(String entry: entries) {
            doc.addPair(key, entry, true, true);
        }
    }

    public static class Factory extends EmbeddingIndexerFactory {

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(snapshot)) {
                return new HtmlIndexer();
            } else {
                return null;
            }
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : deleted) {
                    is.removeDocuments(i);
                }
                if (context.getRoot() != null) {
                    fireChange(context.getRoot());
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
                if (context.getRoot() != null) {
                    fireChange(context.getRoot());
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public String getIndexerName() {
            return HtmlIndex.NAME;
        }

        @Override
        public int getIndexVersion() {
            return HtmlIndex.VERSION;
        }

        private boolean isIndexable(Snapshot snapshot) {
            //index all files possibly containing css
            return HtmlKit.HTML_MIME_TYPE.equals(snapshot.getMimeType());
        }
    }
}
