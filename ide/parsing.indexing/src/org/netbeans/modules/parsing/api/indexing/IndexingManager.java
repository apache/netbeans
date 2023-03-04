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
package org.netbeans.modules.parsing.api.indexing;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.impl.indexing.LogContext;
import org.netbeans.modules.parsing.impl.indexing.IndexingUtils;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vita Stejskal
 * @since 1.6
 */
public final class IndexingManager {

    private static final Logger LOG = Logger.getLogger(IndexingManager.class.getName());

    // -----------------------------------------------------------------------
    // public implementation
    // -----------------------------------------------------------------------

    public static synchronized IndexingManager getDefault() {
        if (instance == null) {
            instance = new IndexingManager();
        }
        return instance;
    }

    /**
     * Checks whether there are any indexing tasks running.
     *
     * @return <code>true</code> if there are indexing tasks running, otherwise <code>false</code>.
     */
    public boolean isIndexing() {
        return IndexingUtils.isScanInProgress();
    }

    /**
     * Schedules new files for indexing. The set of files passed to this method
     * will be scheduled for reindexing. That means that all the indexers appropriate
     * for each file will have a chance to update their index. No timestamp checks
     * are doen for the files, which means that even files that have not been changed
     * since their last indexing will be reindexed again.
     *
     * <p>This method simply calls {@link #refreshIndex(java.net.URL, java.util.Collection, boolean)}
     * with <code>forceRefresh</code> set to <code>true</code>.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param filesOrFolders The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     */
    public void refreshIndex(URL root, Collection<? extends URL> files) {
        refreshIndex(root, files, true, false);
    }

    /**
     * Schedules new files for indexing. The set of files passed to this method
     * will be scheduled for reindexing. That means that all the indexers appropriate
     * for each file will have a chance to update their index.
     * 
     * <p>If <code>forceRefresh</code> parameter is set to <code>true</code>
     * no timestamp checks will be done for the files passed to this method.
     * This means that even files that have not been changed since their last indexing
     * will be reindexed again. On the other hand if <code>forceRefresh</code> is
     * <code>false</code> the infrastructure will check timestamps and will reindex
     * only files that have been changed since the last time they were indexed.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param filesOrFolders The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     * @param fullRescan If <code>true</code> no timestamps check will be done
     *   on the <code>files</code> passed in and they all will be reindexed. If
     *   <code>false</code> only changed files will be reindexed.
     *
     * @since 1.16
     */
    public void refreshIndex(URL root, Collection<? extends URL> files, boolean fullRescan) {
        refreshIndex(root, files, fullRescan, false);
    }
    
    /**
     * Schedules new files for indexing. The set of files passed to this method
     * will be scheduled for reindexing. That means that all the indexers appropriate
     * for each file will have a chance to update their index.
     * 
     * <p>If <code>forceRefresh</code> parameter is set to <code>true</code>
     * no timestamp checks will be done for the files passed to this method.
     * This means that even files that have not been changed since their last indexing
     * will be reindexed again. On the other hand if <code>forceRefresh</code> is
     * <code>false</code> the infrastructure will check timestamps and will reindex
     * only files that have been changed since the last time they were indexed.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     * 
     * <p> If <code>checkEditor</code> is true the indexers will use unsaved content
     * of editor documents. Scan is using the file content rather than editor content,
     * the editor content is needed only in special cases like error badge recovery.
     * Simpler version {@link IndexingManager#refreshIndex(java.net.URL, java.util.Collection, boolean)}
     * should be preferred.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param filesOrFolders The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     * @param fullRescan If <code>true</code> no timestamps check will be done
     *   on the <code>files</code> passed in and they all will be reindexed. If
     *   <code>false</code> only changed files will be reindexed.
     * @param checkEditor when true the indexers will use content of modified editor
     * documents rather than saved files. For scans the indexers should prefer
     * content of files. the document content may be useful for example for error badge
     * recovery.
     *
     * @since 1.37
     */
    public void refreshIndex(@NonNull URL root, @NullAllowed Collection<? extends URL> files, boolean fullRescan, boolean checkEditor) {
        addIndexingJob(root, files, false, checkEditor, false, fullRescan, true);
    }

    /**
     * Schedules new files for indexing and blocks until they are reindexed. This
     * method does the same thing as {@link #refreshIndex(java.net.URL, java.util.Collection) },
     * but it will block the caller until the index refreshing is done.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param filesOrFolders The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     */
    public void refreshIndexAndWait(URL root, Collection<? extends URL> files) {
        refreshIndexAndWait(root, files, true, false);
    }

    /**
     * Schedules new files for indexing and blocks until they are reindexed. This
     * method does the same thing as {@link #refreshIndex(java.net.URL, java.util.Collection, boolean)  },
     * but it will block the caller until the index refreshing is done.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param filesOrFolders The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     * @param fullRescan If <code>true</code> no timestamps check will be done
     *   on the <code>files</code> passed in and they all will be reindexed. If
     *   <code>false</code> only changed files will be reindexed.
     *
     * @since 1.16
     */
    public void refreshIndexAndWait(URL root, Collection<? extends URL> files, boolean fullRescan) {
        refreshIndexAndWait(root, files, fullRescan, false);
    }


    /**
     * Schedules new files for indexing and blocks until they are reindexed. This
     * method does the same thing as {@link #refreshIndex(java.net.URL, java.util.Collection, boolean, boolean)  },
     * but it will block the caller until the index refreshing is done.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param files The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     * @param fullRescan If <code>true</code> no timestamps check will be done
     *   on the <code>files</code> passed in and they all will be reindexed. If
     *   <code>false</code> only changed files will be reindexed.
     * @param checkEditor when true the indexers will use content of modified editor
     *  documents rather than saved files. For scans the indexers should prefer
     *  content of files. the document content may be useful for example for error badge
     *  recovery.
     * @throws  IllegalStateException when caller holds the parser lock or when called from indexer
     * @since 1.40
     */
    public void refreshIndexAndWait(URL root, Collection<? extends URL> files, boolean fullRescan, boolean checkEditor) throws IllegalStateException {
        if (ParserManager.isParsing()) {
            throw new IllegalStateException("The caller holds TaskProcessor.parserLock");   //NOI18N
        }
        if (RepositoryUpdater.getDefault().isIndexer()) {
            throw new IllegalStateException("The IndexingManager.refreshIndexAndWait called from an indexer");   //NOI18N
        }
        addIndexingJob(root, files, false, checkEditor, true, fullRescan, true);
    }

    /**
     * Schedules a new job for refreshing all indices created by the given indexer.
     * This method only works for <code>CustomIndexer</code>s, or <code>EmbeddedIndexer</code>s. 
     * It is not possible to refresh indices created by <code>BinaryIndexer</code>s.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param indexerName The name of the indexer, which indices should be refreshed.
     *   Can be <code>null</code> in which case all indices created by <b>all</b>
     *   indexers will be refreshed (ie. all types of indexers will used,
     *   not just <code>CustomIndexers</code>).
     *
     * @since 1.8
     * @since 
     */
    public void refreshAllIndices(String indexerName) {
        if (indexerName != null) {
            LOG.log(
                Level.FINEST,
                "Request to refresh indexer: {0} in all roots.",    //NOI18N
                indexerName);
            RepositoryUpdater.getDefault().addIndexingJob(
                    indexerName,
                    LogContext.create(LogContext.EventType.MANAGER, null));
        } else {
            refreshAll(true, false, false);
        }
    }

    /**
     * Schedules a new job that will reindex known source roots determined by
     * <code>filesOrFolder</code> parameter.
     * This method does the same as {@link #refreshAllIndices(boolean, boolean, org.openide.filesystems.FileObject[])}
     * with <code>fullRescan == true</code> and <code>wait == false</code>.
     *
     * 
     * @param filesOrFolders The list of files or folders that should be refreshed.
     *   This can be a mixture of files or folders that either lie under some source
     *   root or contain (folders) source roots. The files lying under a source root
     *   will simply be rescanned. The folders lying under a source root will be rescanned
     *   recursively. Files lying outside of all source roots will be ignored. Folders
     *   lying outside of all source folders will be checked for source roots that they
     *   contain and these source roots will be rescanned.
     *   <p>Can be <code>null</code> in which case all indices for
     *   all roots will be refreshed.
     *
     * @since 1.11
     */
    public void refreshAllIndices(FileObject... filesOrFolders) {
        refreshAllIndices(true, false, filesOrFolders);
    }

    /**
     * Schedules a new job that will reindex known source roots determined by
     * <code>filesOrFolder</code> parameter.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param fullRescan If <code>false</code> only modified, new or deleted
     *   files under the roots will be rescanned. Otherwise files will be rescanned
     *   no matter if they were modified or not.
     * @param wait If <code>true</code> the method will block the caller until
     *   refreshing is done.
     * @param filesOrFolders The list of files or folders that should be refreshed.
     *   This can be a mixture of files or folders that either lie under some source
     *   root or contain (folders) source roots. The files lying under a source root
     *   will simply be rescanned. The folders lying under a source root will be rescanned
     *   recursively. Files lying outside of all source roots will be ignored. Folders
     *   lying outside of all source folders will be checked for source roots that they
     *   contain and these source roots will be rescanned.
     *   <p>Can be <code>null</code> in which case all indices for
     *   all roots will be refreshed.
     *
     * @since 1.23
     */
    public void refreshAllIndices(boolean fullRescan, boolean wait, FileObject... filesOrFolders) {
        refreshAll(fullRescan, wait, false, (Object []) filesOrFolders);
    }

    /**
     * Schedules a new job that will reindex known source roots determined by
     * <code>filesOrFolder</code> parameter. This is the same method as {@link #refreshAllIndices(boolean, boolean, org.openide.filesystems.FileObject[])},
     * but it accepts <code>java.io.File</code>s rather than <code>FileObject</code>s.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param fullRescan If <code>false</code> only modified, new or deleted
     *   files under the roots will be rescanned. Otherwise files will be rescanned
     *   no matter if they were modified or not.
     * @param wait If <code>true</code> the method will block the caller until
     *   refreshing is done.
     * @param filesOrFolders The list of files or folders that should be refreshed.
     *   This can be a mixture of files or folders that either lie under some source
     *   root or contain (folders) source roots. The files lying under a source root
     *   will simply be rescanned. The folders lying under a source root will be rescanned
     *   recursively. The files lying outside of all source roots will be ignored. Finally,
     *   the folders lying outside of all source folders will be checked for source roots
     *   that they contain and these source roots will be rescanned.
     *   <p>Can be <code>null</code> in which case all indices for
     *   all roots will be refreshed. The files have to be normalized, @see FileUtil#normalizeFile
     *
     * @since 1.24
     */
    public void refreshAllIndices(boolean fullRescan, boolean wait, File... filesOrFolders) {
        refreshAll(fullRescan, wait, false, (Object []) filesOrFolders);
    }

    /**
     * Runs the <code>operation</code> in protected mode. All events that would normally
     * trigger rescanning are remembered and processed after the operation finishes.
     * <p>Note that events coming from other threads during the time that this
     * thread is performing the operation will also be queued.
     * @param operation The operation to run without rescanning while the operation
     *   is running.
     * @return Whatever value the <code>operation</code> returns.
     * 
     * @throws Exception Any exception thrown from the operation is rethrown.
     * @since 1.24
     */
    public <T> T runProtected(Callable<T> operation) throws Exception {
        IndexingController.getDefault().enterProtectedMode();
        try {
            return operation.call();
        } finally {
            IndexingController.getDefault().exitProtectedMode(null);
        }
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private static IndexingManager instance;


    private IndexingManager() {
        // Start ReporistoryUpdater if it has not been already started
        RepositoryUpdater.getDefault().start(false);
    }

    private void refreshAll(
            final boolean fullRescan,
            final boolean wait,
            final boolean logStatistics,
            @NullAllowed Object... filesOrFileObjects) {
        LOG.log(
            Level.FINEST,
            "Request to refresh all indexes"); //NOI18N
        RepositoryUpdater.getDefault().refreshAll(
            fullRescan,
            wait,
            logStatistics,
            LogContext.create(LogContext.EventType.MANAGER, null),
            filesOrFileObjects);
    }

    private void addIndexingJob(
            @NonNull final URL rootUrl,
            @NullAllowed final Collection<? extends URL> fileUrls,
            final boolean followUpJob,
            final boolean checkEditor,
            final boolean wait,
            final boolean forceRefresh,
            final boolean steady){
        LOG.log(
            Level.FINEST,
            "Request to add indexing job for root: {0}", //NOI18N
            rootUrl);
        RepositoryUpdater.getDefault().addIndexingJob(
            rootUrl,
            fileUrls,
            followUpJob,
            checkEditor,
            wait,
            forceRefresh,
            steady,
            LogContext.create(LogContext.EventType.MANAGER, null).withRoot(rootUrl));
    }
}
