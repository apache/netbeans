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

package org.netbeans.modules.parsing.spi.indexing;

import java.net.URL;

/**
 *
 * @author vita
 */
public abstract class BinaryIndexerFactory {

    /**
     * Creates  new {@link BinaryIndexer}.
     * @return an indexer
     */
    public abstract BinaryIndexer createIndexer();

    /**
     * Called by indexing infrastructure to notify indexer that roots were deregistered,
     * for example the project owning these roots was closed. The indexer may free memory caches
     * for given roots or do any other clean up.
     *
     * @param removedRoots the iterable of removed roots
     * @since 1.19
     */
    public abstract void rootsRemoved (Iterable<? extends URL> removedRoots);

    /**
     * Return the name of this indexer. This name should be unique because GSF
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

    /**
     * Notifies the indexer that a binary root is going to be scanned.
     *
     * @param context The indexed binary root.
     *
     * @return <code>false</code> means that the whole root should be rescanned
     *   (eg. no up to date check is done, etc)
     * @since 1.29
     */
    public boolean scanStarted (final Context context) {
        return true;
    }

    /**
     * Notifies the indexer that scanning of a binary root just finished.
     *
     * @param context The indexed binary root.
     *
     * @since 1.29
     */
    public void scanFinished (final Context context) {

    }
}
