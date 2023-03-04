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

package org.netbeans.modules.csl.core;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author vita
 */
public final class EmbeddingIndexerFactoryImpl extends EmbeddingIndexerFactory {

    // EmbeddingIndexerFactory implementation

    @Override
    public boolean scanStarted(final Context context) {
        if (!getFactory().scanStarted(context)) {
            return false;
        }
        return verifyIndex(context);
    }

    @Override
    public void scanFinished(final Context context) {
        getFactory().scanFinished(context);
    }
    
    @Override
    public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
        return getFactory().createIndexer(indexable, snapshot);
    }

    @Override
    public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        getFactory().filesDeleted(deleted, context);
    }

    @Override
    public void rootsRemoved(final Iterable<? extends URL> removedRoots) {
        assert removedRoots != null;
        getFactory().rootsRemoved(removedRoots);
    }
    
    @Override
    public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        getFactory().filesDirty(dirty, context);
    }

    @Override
    public String getIndexerName() {
        return getFactory().getIndexerName();
    }

    @Override
    public int getIndexVersion() {
        return getFactory().getIndexVersion();
    }

    @Override
    public String toString() {
        return String.format("%s[%s]",getClass().getName(),getFactory());
    }

    // public implementation

    public static EmbeddingIndexerFactory create(FileObject fileObject) {
        String mimeType = fileObject.getParent().getPath().substring("Editors/".length()); //NOI18N
        return new EmbeddingIndexerFactoryImpl(mimeType);
    }

    // private implementation

    private static boolean verifyIndex(final Context context) {
        try {
            return IndexingSupport.getInstance(context).isValid();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
    }

    private static final Logger LOG = Logger.getLogger(EmbeddingIndexerFactoryImpl.class.getName());
    
    private static final EmbeddingIndexerFactory VOID_INDEXER_FACTORY = new EmbeddingIndexerFactory() {
        private final EmbeddingIndexer voidIndexer = new EmbeddingIndexer() {
            @Override
            protected void index(Indexable indexable, Result parserResult, Context context) {
                // no-op
            }
        };

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            return voidIndexer;
        }

        @Override
        public String getIndexerName() {
            return "void-indexer"; //NOI18N
        }

        @Override
        public int getIndexVersion() {
            return 0;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            // no-op
        }

        @Override
        public void rootsRemoved(Iterable<? extends URL> removedRoots) {
            // no-op
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            // no-op
        }
    };

    private final String mimeType;
    private EmbeddingIndexerFactory realFactory;

    private EmbeddingIndexerFactoryImpl(String mimeType) {
        this.mimeType = mimeType;
    }

    private EmbeddingIndexerFactory getFactory() {
        if (realFactory == null) {
            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
            if (language != null) {
                EmbeddingIndexerFactory factory = language.getIndexerFactory();
                if (factory != null) {
                    realFactory = factory;
                }
            }

            if (realFactory == null) {
                realFactory = VOID_INDEXER_FACTORY;
            }

            LOG.fine("EmbeddingIndexerFactory for '" + mimeType + "': " + realFactory); //NOI18N
        }

        return realFactory;
    }
}
