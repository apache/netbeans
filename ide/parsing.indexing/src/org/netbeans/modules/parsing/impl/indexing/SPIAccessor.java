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

package org.netbeans.modules.parsing.impl.indexing;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.*;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SPIAccessor {
    
    private static volatile SPIAccessor instance;

    public static void setInstance (final SPIAccessor _instance) {
        assert _instance != null;
        instance = _instance;
    }

    public static synchronized SPIAccessor getInstance () {
        if (instance == null) {
            try {
                Class.forName(Indexable.class.getName(), true, Indexable.class.getClassLoader());
                assert instance != null;
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return instance;
    }

    public abstract Indexable create (final IndexableImpl delegate);

    @NonNull
    public abstract Context createContext(
            @NonNull FileObject indexFolder,
            @NonNull URL rootURL,
            @NonNull String indexerName,
            int indexerVersion,
            @NullAllowed IndexFactoryImpl factory,
            boolean followUpJob,
            boolean checkForEditorModifications,
            boolean sourceForBinaryRoot,
            @NonNull final SuspendStatus suspendedStatus,
            @NullAllowed final CancelRequest cancelRequest,
            @NullAllowed final LogContext logContext) throws IOException;

    @NonNull
    public abstract Context createContext(
            @NonNull final Callable<FileObject> indexFolderFactory,
            @NonNull final URL rootURL,
            @NonNull final String indexerName,
            int indexerVersion,
            @NullAllowed final IndexFactoryImpl factory,
            boolean followUpJob,
            boolean checkForEditorModifications,
            boolean sourceForBinaryRoot,
            @NonNull final SuspendStatus suspendedStatus,
            @NullAllowed final CancelRequest cancelRequest,
            @NullAllowed final LogContext logContext) throws IOException;
    
    @NonNull
    public abstract SuspendStatus createSuspendStatus(@NonNull SuspendSupport.SuspendStatusImpl impl);

    public abstract void context_attachIndexingSupport(Context context, IndexingSupport support);

    public abstract IndexingSupport context_getAttachedIndexingSupport(Context context);

    public abstract void context_clearAttachedIndexingSupport(Context context);
    
    public abstract String getIndexerName (Context ctx);

    public abstract int getIndexerVersion (Context ctx);

    public abstract String getIndexerPath (String indexerName, int indexerVersion);

    public abstract IndexFactoryImpl getIndexFactory (Context ctx);

    public abstract void index (BinaryIndexer indexer, Context context);

    public abstract void index (@NonNull ConstrainedBinaryIndexer indexer, @NonNull Map<String,? extends Iterable<? extends FileObject>> files, @NonNull Context context);

    public abstract void index (CustomIndexer indexer, Iterable<? extends Indexable> files, Context context);

    public abstract void index (EmbeddingIndexer indexer, Indexable indexable, Parser.Result parserResult, Context ctx);

    public abstract void setAllFilesJob (Context context, boolean allFilesJob);

    public abstract boolean scanStarted(@NonNull ConstrainedBinaryIndexer indexer, @NonNull Context context);

    public abstract void scanFinished(@NonNull ConstrainedBinaryIndexer indexer, @NonNull Context context);

    public abstract void rootsRemoved(@NonNull ConstrainedBinaryIndexer indexer, @NonNull Iterable<? extends URL> removed);

    public abstract void putProperty(@NonNull Context context, @NonNull String propName, @NullAllowed Object value);

    public abstract Object getProperty(@NonNull Context context, @NonNull String propName);

    public abstract boolean isTypeOf (@NonNull Indexable indexable, @NonNull String mimeType);

    public abstract FileObject getFileObject(@NonNull Indexable indexable);
}
