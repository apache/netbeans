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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CancelRequest;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.LogContext;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.impl.indexing.lucene.LayeredDocumentIndex;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Represents a context of indexing given root.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class Context {

    private final URL rootURL;    
    private final String indexerName;
    private final int indexerVersion;
    private final boolean followUpJob;
    private final boolean checkForEditorModifications;
    private final boolean sourceForBinaryRoot;
    private final CancelRequest cancelRequest;
    private final SuspendStatus suspendedStatus;
    private final LogContext logContext;
    private final Map<String,Object> props;
    private final Callable<FileObject> indexBaseFolderFactory;
    private FileObject indexFolder;
    private boolean allFilesJob;
    private FileObject root;
    private FileObject indexBaseFolder;
    private IndexingSupport indexingSupport;    

    private final IndexFactoryImpl factory;

    Context (@NonNull final FileObject indexBaseFolder,
             @NonNull final URL rootURL,
             @NonNull final String indexerName,
             final int indexerVersion,
             @NullAllowed final IndexFactoryImpl factory,
             final boolean followUpJob,
             final boolean checkForEditorModifications,
             final boolean sourceForBinaryRoot,
             @NonNull final SuspendStatus suspendedStatus,
             @NullAllowed final CancelRequest cancelRequest,
             @NullAllowed final LogContext logContext
    ) throws IOException {
        assert indexBaseFolder != null;
        assert rootURL != null;
        assert indexerName != null;
        this.indexBaseFolderFactory = null;
        this.indexBaseFolder = indexBaseFolder;
        this.rootURL = rootURL;
        this.indexerName = indexerName;
        this.indexerVersion = indexerVersion;
        this.factory = factory != null ? factory : LuceneIndexFactory.getDefault();
        this.followUpJob = followUpJob;
        this.checkForEditorModifications = checkForEditorModifications;
        this.sourceForBinaryRoot = sourceForBinaryRoot;
        this.cancelRequest = cancelRequest;
        this.suspendedStatus = suspendedStatus;
        this.logContext = logContext;
        this.props = new HashMap<String, Object>();
    }

    Context (@NonNull final Callable<FileObject> indexBaseFolderFactory,
             @NonNull final URL rootURL,
             @NonNull final String indexerName,
             final int indexerVersion,
             @NullAllowed final IndexFactoryImpl factory,
             final boolean followUpJob,
             final boolean checkForEditorModifications,
             final boolean sourceForBinaryRoot,
             @NonNull final SuspendStatus suspendedStatus,
             @NullAllowed final CancelRequest cancelRequest,
             @NullAllowed final LogContext logContext
    ) throws IOException {
        assert indexBaseFolderFactory != null;
        assert rootURL != null;
        assert indexerName != null;
        this.indexBaseFolderFactory = indexBaseFolderFactory;
        this.rootURL = rootURL;
        this.indexerName = indexerName;
        this.indexerVersion = indexerVersion;
        this.factory = factory != null ? factory : LuceneIndexFactory.getDefault();
        this.followUpJob = followUpJob;
        this.checkForEditorModifications = checkForEditorModifications;
        this.sourceForBinaryRoot = sourceForBinaryRoot;
        this.cancelRequest = cancelRequest;
        this.suspendedStatus = suspendedStatus;
        this.logContext = logContext;
        this.props = new HashMap<String, Object>();
    }

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    /**
     * Returns the cache folder where the indexer may store language metadata.
     * For each root and indexer there exist a separate cache folder.
     * @return The cache folder
     */
    public FileObject getIndexFolder () {
        if (this.indexFolder == null) {
            try {
                final String path = getIndexerPath(indexerName, indexerVersion);
                if (this.indexBaseFolder == null) {
                    try {
                        this.indexBaseFolder = this.indexBaseFolderFactory.call();
                    } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                    if (this.indexBaseFolder == null) {
                        throw new IllegalStateException(
                            String.format(
                                "Factory %s returned null index base folder.",  //NOI18N
                                this.indexBaseFolderFactory));
                    }
                }
                this.indexFolder = FileUtil.createFolder(this.indexBaseFolder,path);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return this.indexFolder;
    }

    /**
     * Return the {@link URL} of the processed root
     * @return the absolute URL
     */
    public URL getRootURI () {
        return this.rootURL;
    }

    /**
     * Return the processed root, may return null
     * when the processed root was deleted.
     * The {@link Context#getRootURI()} can be used in
     * this case.
     * @return the root or null when the root doesn't exist
     */
    public FileObject getRoot () {
        if (root == null) {            
            root = URLMapper.findFileObject(this.rootURL);
        }
        return root;
    }

    /**
     * Schedules additional files for reindexing. This method can be used for requesting
     * reindexing of additional files that an indexer discovers while indexing some
     * other files. The files passed to this method will be processed by a new
     * indexing job after the current indexing is finished.
     * That means that all the indexers appropriate
     * for each file will have a chance to update their index. No timestamp checks
     * are done on the additional files, which means that even files that have not been changed
     * since their last indexing will be reindexed again.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param files The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     *
     * @since 1.3
     */
    public void addSupplementaryFiles(URL root, Collection<? extends URL> files) {
        Logger repouLogger = Logger.getLogger(RepositoryUpdater.class.getName());
        if (repouLogger.isLoggable(Level.FINE)) {
            repouLogger.fine("addSupplementaryFiles: root=" + root + ", files=" + files); //NOI18N
        }
        RepositoryUpdater.getDefault().addIndexingJob(
                root,
                files,
                true,
                false,
                false,
                true,
                true,
                LogContext.create(LogContext.EventType.INDEXER, null, logContext).
                    withRoot(root).addFiles(files)
                );
    }

    /**
     * Indicates whether the current indexing job was requested by calling
     * {@link #addSupplementaryFiles(java.net.URL, java.util.Collection) } method.
     *
     * @return <code>true</code> if the indexing job was requested by <code>addSupplementaryFiles</code>,
     *   otherwise <code>false</code>.
     *
     * @since 1.3
     */
    public boolean isSupplementaryFilesIndexing() {
        return followUpJob;
    }

    /**
     * Indicates whether all files under the root are being indexed. In general indexing
     * jobs can either index selected files under a given root (eg. when scheduled
     * through {@link IndexingManager}) or they can index all files under the root. Some
     * indexers are interested in knowing this information in order to optimize their
     * indexing.
     *
     * @return <code>true</code> if indexing all files under the root.
     *
     * @since 1.6
     */
    public boolean isAllFilesIndexing() {
        return allFilesJob;
    }

    /**
     * Indicates whether sources of some binary library are being indexed. Some
     * indexers are interested in knowing this information in order to optimize their
     * indexing.
     *
     * @return <code>true</code> if indexing sources for binary root.
     *
     * @since 1.17
     */
    public boolean isSourceForBinaryRootIndexing() {
        return sourceForBinaryRoot;
    }

    /**
     * Notifies indexers whether they should use editor documents rather than just
     * files. This is mostly useful for <code>CustomIndexer</code>s that may optimize
     * their work and not try to find editor documents for their <code>Indexable</code>s.
     *
     * <p><code>EmbeddingIndexer</code>s can safely ignore this flag since they operate
     * on <code>Parser.Result</code>s and <code>Snapshot</code>s, which are guaranteed
     * to be in sync with editor documents or loaded efficiently from a file if the
     * file is not opened in the editor.
     *
     * @return <code>false</code> if indexers don't have to care about possible
     *   editor modifications or <code>true</code> otherwise.
     * 
     * @since 1.10
     */
    public boolean checkForEditorModifications() {
        return checkForEditorModifications;
    }

    /**
     * Returns true if the indexing job is canceled either by external event like
     * IDE exit or by a new indexing job which obscures the current one.
     * The indexer should check the {@link Context#isCancelled()} in its index method
     * and return as soon as possible if it returns true. The indexer factory should
     * also check the {@link Context#isCancelled()} if it overrides the
     * {@link SourceIndexerFactory#scanFinished(org.netbeans.modules.parsing.spi.indexing.Context)}
     * method, when it's true the scanFinished should roll back all changes. The changes done into
     * {@link IndexingSupport} are rolled back automatically.
     * 
     * @return true if indexing job is canceled.
     * @since 1.13
     */
    public boolean isCancelled() {
        return cancelRequest != null ?
            cancelRequest.isRaised() :
            false;
    }
    
    /**
     * Returns {@link SuspendStatus} providing information
     * about indexing suspension.
     * @return the {@link SuspendStatus}
     * @since 1.52
     */
    @NonNull
    public SuspendStatus getSuspendStatus() {
        return suspendedStatus;
    }

    // -----------------------------------------------------------------------
    // Package private implementation
    // -----------------------------------------------------------------------

    IndexFactoryImpl getIndexFactory() {
        return this.factory;
    }

    String getIndexerName () {
        return this.indexerName;
    }

    int getIndexerVersion () {
        return this.indexerVersion;
    }

    void attachIndexingSupport(IndexingSupport support) {
        assert this.indexingSupport == null;
        this.indexingSupport = support;
        try {
            final LayeredDocumentIndex index = this.factory.getIndex(getIndexFolder());
            if (index != null) {
                index.begin();
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    IndexingSupport getAttachedIndexingSupport() {
        return this.indexingSupport;
    }

    void clearAttachedIndexingSupport() {
        this.indexingSupport = null;
    }

    void setAllFilesJob (final boolean allFilesJob) {
        this.allFilesJob = allFilesJob;
    }

    void putProperty(
            @NonNull String propName,
            @NullAllowed final Object value) {
        Parameters.notNull("propName", propName);   //NOI18N
        props.put(propName, value);
    }

    Object getProperty(@NonNull String propName) {
        Parameters.notNull("propName", propName);   //NOI18N
        return props.get(propName);
    }    

    static String getIndexerPath (final String indexerName, final int indexerVersion) {
        final StringBuilder sb = new StringBuilder();
        sb.append(indexerName);
        sb.append('/'); //NOI18N
        sb.append(indexerVersion);
        return sb.toString();
    }
}
