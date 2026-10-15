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

package org.netbeans.modules.parsing.spi.indexing.support;

import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.impl.indexing.ClusteredIndexables;
import org.netbeans.modules.parsing.impl.indexing.FileObjectIndexable;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexingTask;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Support for writing indexers. Provides persistent storage
 * for indexers.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class IndexingSupport {

    private static final Logger LOG = Logger.getLogger(IndexingSupport.class.getName());
    
    private final Context context;
    private final IndexFactoryImpl spiFactory;
    private final org.netbeans.modules.parsing.lucene.support.DocumentIndex spiIndex;

    private IndexingSupport (final Context ctx) throws IOException {
        IndexFactoryImpl factory = SPIAccessor.getInstance().getIndexFactory(ctx);
        assert factory != null;
        this.context = ctx;
        this.spiFactory = factory;
        this.spiIndex = this.spiFactory.createIndex(ctx);        
    }

    /**
     * Returns an {@link IndexingSupport} for given indexing {@link Context}
     * @param context for which the support should be returned
     * @return the context
     * @throws java.io.IOException when underlying storage is corrupted or cannot
     * be created
     */
    @NonNull
    public static IndexingSupport getInstance (@NonNull final Context context) throws IOException {
        Parameters.notNull("context", context); //NOI18N
        IndexingSupport support = SPIAccessor.getInstance().context_getAttachedIndexingSupport(context);
        if (support == null) {
            support = new IndexingSupport(context);
            SPIAccessor.getInstance().context_attachIndexingSupport(context, support);            
        }
        support.attachToCacheIfNeeded();
        return support;
    }

    /**
     * Checks a validity of the index
     * <p class="nonnormative">
     * Implementations of the {@link SourceIndexerFactory} should check the validity of the
     * index in the {@link SourceIndexerFactory#scanStarted(org.netbeans.modules.parsing.spi.indexing.Context)} method
     * and force reindexing of the root for which the index is broken. In case of CSL based factories the check is done by the CSL itself.
     * </p>
     * @return false when the index exists but it's broken
     * @since 1.21
     */
    public boolean isValid() {
        try {
            return spiIndex != null && spiIndex.getStatus() != Index.Status.INVALID;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Creates a new {@link IndexDocument}. This method does exactly the same as
     * {@link #createDocument(org.netbeans.modules.parsing.spi.indexing.Indexable)}, but you can
     * use <code>FileObject</code>s directly. The <code>FileObject</code> passed in
     * as a parameter has to be in the folders hierarchy under the root, which
     * this <code>IndexingSupport</code> instance was created for.
     *
     * @param file The file to create <code>IndexDocument</code> for.
     *
     * @return The <code>IndexDocument</code> created for the <code>file</code>.
     * @since 1.22
     */
    @NonNull
    public IndexDocument createDocument(@NonNull final FileObject file) {
        Parameters.notNull("file", file);   //NOI18N
        FileObject root = context.getRoot();
        if (FileUtil.isParentOf(root, file)) {
            return createDocument(SPIAccessor.getInstance().create(new FileObjectIndexable(root, file)));
        } else {
            throw new IllegalArgumentException(file + " is not under the root " + root); //NOI18N
        }
    }

    /**
     * Creates a new {@link IndexDocument}.
     * @return the decument
     */
    public IndexDocument createDocument (final Indexable indexable) {
        Parameters.notNull("indexable", indexable); //NOI18N
        return new IndexDocument(this.spiFactory.createDocument(indexable));
    }

    /**
     * Adds a new {@link IndexDocument} into the index
     * @param document to be added
     */
    public void addDocument (final IndexDocument document) {
        Parameters.notNull("document", document.spi); //NOI18N
        if (spiIndex != null) {
            spiIndex.addDocument (document.spi);
        }
    }

    /**
     * Removes all documents for given indexables
     * @param indexable to be removed
     */
    public void removeDocuments (final Indexable indexable) {
        Parameters.notNull("indexable", indexable); //NOI18N
        if (spiIndex != null) {
            spiIndex.removeDocument (indexable.getRelativePath());
        }
    }

    /**
     * Marks all documents for an <code>Indexable</code> as dirty. Any subsequent
     * use of <code>QuerySupport</code> for those <code>Indexable</code>s will first
     * refresh the documents (ie. call indexers) to make sure that the documents
     * are up-to-date.
     *
     * @param indexable The {@link Indexable} whose documents will be marked as dirty.
     * @since 1.4
     */
    public void markDirtyDocuments (final Indexable indexable) {
        Parameters.notNull("indexable", indexable); //NOI18N
        if (spiIndex != null) {
            spiIndex.markKeyDirty(indexable.getRelativePath());
        }
    }

    private boolean attachToCacheIfNeeded() throws IOException {
        final DocumentIndexCache cache = spiFactory.getCache(context);
        if (!(cache instanceof ClusteredIndexables.AttachableDocumentIndexCache)) {
            return false;
        }
        boolean res = false;
        final ClusteredIndexables.AttachableDocumentIndexCache ciCache = (ClusteredIndexables.AttachableDocumentIndexCache) cache;
        final ClusteredIndexables delCi = (ClusteredIndexables) SPIAccessor.getInstance().getProperty(context, ClusteredIndexables.DELETE);
        if (delCi != null) {
            ciCache.attach(ClusteredIndexables.DELETE, delCi);
            res = true;
        }
        final ClusteredIndexables indexCi = (ClusteredIndexables) SPIAccessor.getInstance().getProperty(context, ClusteredIndexables.INDEX);
        if (indexCi != null) {
            ciCache.attach(ClusteredIndexables.INDEX, indexCi);
            res = true;
        }
        return res;
    }

    /**
     * Identifies parser Tasks that are executed by indexing infrastructure. Use to customize the granularity of a {@link Parser.Result}
     * provided by e.g. {@link EmbeddingIndexer} or {@link CustomIndexer}.
     * @param t task to check
     * @return true, if the task is posted by indexing infra.
     * @since 9.22
     */
    public static boolean isIndexingTask(Task t) {
        return t instanceof IndexingTask;
    }
}
