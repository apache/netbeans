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
package org.netbeans.modules.javascript2.knockout.index;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Roman Svitanic
 */
public class KnockoutIndexer extends EmbeddingIndexer {

    public static final String CUSTOM_ELEMENT = "ce"; //NOI18N

    private static final Logger LOG = Logger.getLogger(KnockoutIndexer.class.getName());

    private static final ThreadLocal<Map<URI, Collection<KnockoutCustomElement>>> CUSTOM_ELEMENTS = new ThreadLocal<>();

    @Override
    protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
        Map<URI, Collection<KnockoutCustomElement>> cElements = CUSTOM_ELEMENTS.get();
        if (cElements != null && !cElements.isEmpty()) {
            IndexingSupport support;
            try {
                support = IndexingSupport.getInstance(context);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
                return;
            }

            try {
                Collection<KnockoutCustomElement> elements = cElements.remove(indexable.getURL().toURI());
                if (elements != null) {
                    IndexDocument elementDocument = support.createDocument(indexable);
                    for (KnockoutCustomElement customElement : elements) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(customElement.getName()).append(":");    //NOI18N
                        sb.append(customElement.getFqn()).append(":");     //NOI18N
                        sb.append(customElement.getOffset());
                        if (!customElement.getParameters().isEmpty()) {
                            sb.append(":");                                //NOI18N
                            for (int i = 0; i < customElement.getParameters().size(); i++) {
                                sb.append(customElement.getParameters().get(i));
                                if (i != (customElement.getParameters().size() - 1)) {
                                    sb.append(";");                        //NOI18N
                                }
                            }
                        }
                        elementDocument.addPair(CUSTOM_ELEMENT, sb.toString(), true, true);
                    }
                    support.addDocument(elementDocument);
                }
            } catch (URISyntaxException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }
    }

    public static void addCustomElement(final URI uri, final KnockoutCustomElement customElement) {
        final Map<URI, Collection<KnockoutCustomElement>> map = CUSTOM_ELEMENTS.get();

        if (map == null) {
            throw new IllegalStateException("KnockoutIndexer.addCustomElement can be called only from scanner thread.");  //NOI18N
        }
        Collection<KnockoutCustomElement> elements = map.get(uri);
        if (elements == null) {
            elements = new ArrayList<>();
            elements.add(customElement);
            map.put(uri, elements);
        } else {
            elements.add(customElement);
        }
    }

    public static boolean isScannerThread() {
        return CUSTOM_ELEMENTS.get() != null;
    }

    public static final class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "knockoutjs"; // NOI18N
        public static final int VERSION = 1;
        private static final int PRIORITY = 220;

        private static final ThreadLocal<Collection<Runnable>> postScanTasks = new ThreadLocal<>();

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(indexable, snapshot)) {
                return new KnockoutIndexer();
            } else {
                return null;
            }
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable i : deleted) {
                    is.removeDocuments(i);
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

        private boolean isIndexable(Indexable indexable, Snapshot snapshot) {
            return JsTokenId.JAVASCRIPT_MIME_TYPE.equals(snapshot.getMimeType());
        }

        @Override
        public boolean scanStarted(Context context) {
            postScanTasks.set(new LinkedList<Runnable>());
            CUSTOM_ELEMENTS.set(new HashMap<URI, Collection<KnockoutCustomElement>>());
            return super.scanStarted(context);
        }

        @Override
        public void scanFinished(Context context) {
            try {
                for (Runnable task : postScanTasks.get()) {
                    task.run();
                }
            } finally {
                postScanTasks.remove();
                super.scanFinished(context);
            }
        }

        public static boolean isScannerThread() {
            return postScanTasks.get() != null;
        }

        public static void addPostScanTask(final Runnable task) {
            Parameters.notNull("task", task);   //NOI18N
            final Collection<Runnable> tasks = postScanTasks.get();
            if (tasks == null) {
                throw new IllegalStateException("JsIndexer.postScanTask can be called only from scanner thread.");  //NOI18N
            }
            tasks.add(task);
        }

        @Override
        public int getPriority() {
            return PRIORITY;
        }
    }

}
