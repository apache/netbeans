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

package org.netbeans.modules.parsing.spi.indexing;

import java.net.URL;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;

/**
 * Abstract predecessor of the {@link CustomIndexerFactory} and {@link EmbeddingIndexerFactory}.
 * The indexer factory should never subclass this class. It should always subclass either the {@link CustomIndexerFactory}
 * or {@link EmbeddingIndexerFactory}
 * <div class="nonnormative">
 * <p>The {@link IndexingSupport} can be used to implement the {@link SourceIndexerFactory}</p>
 * </div>
 * @since 1.20
 * @author Tomas Zezula
 */
public abstract class SourceIndexerFactory {

    /**
     * Notifies the indexer that a source root is going to be scanned.
     *
     * @param context The indexed source root.
     *
     * @return <code>false</code> means that the whole root should be rescanned
     *   (eg. no up to date check is done, etc)
     * <div class="nonnormative">
     *  <p>If the {@link IndexingSupport} is used to implement the {@link SourceIndexerFactory}
     * the implementation of this method should delegate to {@link IndexingSupport#isValid()}</p>
     * </div>
     * @since 1.20
     */
    public boolean scanStarted (final Context context) {
        return true;
    }

    /**
     * Notifies the indexer that scanning of a source root just finished.
     *
     * @param context The indexed source root.
     *
     * @since 1.20
     */
    public void scanFinished (final Context context) {

    }

    /**
     * Returns a priority of the indexer.
     * The priority is used for ordering indexers of same type working on the same mime type.
     * @return priority, the lower number for higher priority.
     * @since 1.73.0
     */
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    /**
     * Called by indexing infrastructure to allow indexer to clean indexes for deleted files.
     * @param deleted the collection of deleted {@link Indexable}s
     * @param context an indexing context
     * <div class="nonnormative">
     *  <p>If the {@link IndexingSupport} is used to implement the {@link SourceIndexerFactory}
     * the implementation of this method should delegate to {@link IndexingSupport#isValid()}</p>
     * </div>
     * @since 1.18
     */
    public abstract void filesDeleted (Iterable<? extends Indexable> deleted, Context context);

    /**
     * Called by indexing infrastructure to notify indexer that roots were deregistered,
     * for example the project owning these roots was closed. The indexer may free memory caches
     * for given roots or do any other clean up.
     * @param removedRoots the iterable of removed roots
     * <div class="nonnormative">
     *  <p>If the {@link IndexingSupport} is used to implement the {@link SourceIndexerFactory}
     *   the implementation of this method should delegate to {@link IndexingSupport#removeDocuments}</p>
     * </div>
     * @since 1.19
     */
    public void rootsRemoved (Iterable<? extends URL> removedRoots) {

    }

    /**
     * Called by indexing infrastructure to notify indexer that a file was modified and so its
     * index may contain stale data.
     * @param dirty the collection of dirty {@link Indexable}s
     * @param context an indexing context
     * <div class="nonnormative">
     *  <p>If {@link IndexingSupport} is used to implement the {@link SourceIndexerFactory}
     *   the implementation of this method should delegate to {@link IndexingSupport#markDirtyDocuments}</p>
     * </div>
     * @since 1.18
     */
    public abstract void filesDirty (Iterable<? extends Indexable> dirty, Context context);

    /**
     * Return the name of this indexer. This name should be unique because the infrastructure
     * will use this name to produce a separate data directory for each indexer
     * where it has its own storage.
     *
     * @return The indexer name. This does not need to be localized since it is
     * never shown to the user, but should contain filesystem safe characters.
     */
    public abstract String getIndexerName ();


    /**
     * Return the version stamp of the schema that is currently being stored
     * by this indexer. Along with the index name this string will be used to
     * create a unique data directory for the database.
     *
     * Whenever you incompatibly change what is stored by the indexer,
     * update the version stamp.
     *
     * @return The version stamp of the current index.
     */
    public abstract int getIndexVersion ();

}
