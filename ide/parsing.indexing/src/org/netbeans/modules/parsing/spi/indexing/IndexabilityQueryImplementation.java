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
import javax.swing.event.ChangeListener;
import org.netbeans.modules.parsing.impl.indexing.IndexabilityQueryContextAccessor;

/**
 * Determine whether files should be skipped for indexing.
 * <p>
 * Global lookup is used to find all instances of
 * IndexabilityQueryImplementation.
 * </p>
 * <p>
 * Threading note: implementors should avoid acquiring locks that might be held
 * by other threads. Generally treat this interface similarly to SPIs in
 * {@link org.openide.filesystems} with respect to threading semantics.
 * </p>
 *
 * @see org.netbeans.modules.parsing.impl.indexing.IndexabilityQuery
 * @since org.netbeans.modules.parsing.indexing 9.24.0
 */
public interface IndexabilityQueryImplementation {

    /**
     * @return name of the IndexabilityQueryImplementation. The allowed
     * characters are from the range: [A-Za-z0-9$_.+/].
     **/
    public String getName();

    /**
     * @return version of the IndexabilityQueryImplementation, must be
     * incremented if changes in the implementation would affect indexing
     * results
     */
    public int getVersion();

    /**
     * It is not the intention of this to represent the configuration,
     * that should be stored in the configuration, but should make it possible
     * to identify changes to the configuration. A valid identifier for example
     * would be a checksum over the configuration. It must be ensured, that
     * for a given configuration the same stateIdentifier is returned. It must
     * not change between NetBeans restarts - else a full reindex will be done.
     *
     * @return an identifier, that identifies the state of the indexability
     * query.  The allowed characters are from the range: [A-Za-z0-9$_.+/].
     */
    public String getStateIdentifier();

    /**
     * Determine if the supplied indexable should be indexed using the given
     * indexer (identified either by the name of the indexer or its factory
     * class).
     *
     * @param iqp information about the indexable and circumstances of the
     * indexing action
     * @return
     */
    default boolean preventIndexing(IndexabilityQueryContext iqp) {
        return false;
    }

    /**
     * Add a listener to changes.
     *
     * @param l a listener to add
     */
    void addChangeListener(ChangeListener l);

    /**
     * Stop listening to changes.
     *
     * @param l a listener to remove
     */
    void removeChangeListener(ChangeListener l);

    public static final class IndexabilityQueryContext {

        static {
            IndexabilityQueryContextAccessor.setInstance(new Accessor());
        }

        private final String indexerName;
        private final URL indexable;
        private final URL root;

        private IndexabilityQueryContext(
                URL indexable,
                String indexerName,
                URL rootUrl) {
            this.indexerName = indexerName;
            this.indexable = indexable;
            this.root = rootUrl;
        }

        /**
         * @return the file that is to be indexed, never {@code null}.
         */
        public URL getIndexable() {
            return this.indexable;
        }

        /**
         * @return name of the indexer that is about to index the file. Will be
         * {@code null} to query general indexability of the supplied indexable.
         */
        public String getIndexerName() {
            return this.indexerName;
        }

        /**
         * @return the root of the scanning process, can be {@code null}
         */
        public URL getRoot() {
            return this.root;
        }

        private static class Accessor extends IndexabilityQueryContextAccessor {

            @Override
            public IndexabilityQueryContext createContext(URL indexable, String indexerName, URL root) {
                return new IndexabilityQueryContext(indexable, indexerName, root);
            }

        }
    }

}
