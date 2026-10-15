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

package org.netbeans.modules.subversion;

import java.awt.EventQueue;
import java.util.Map.Entry;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.client.SvnClient;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.netbeans.modules.subversion.notifications.NotificationsManager;
import org.netbeans.modules.subversion.ui.status.StatusAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnSearchHistorySupport;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.SearchHistorySupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 *
 * @author Maros Sandor
 */
class FilesystemHandler extends VCSInterceptor {

    private final FileStatusCache   cache;

    /**
     * Stores all moved files for a later cache refresh in afterMove
     */
    private final Set<File> movedFiles = new HashSet<File>();
    private final Set<File> copiedFiles = new HashSet<File>();

    private final Set<File> internalyDeletedFiles = new HashSet<File>();
    private final Set<File> toLockFiles = Collections.synchronizedSet(new HashSet<File>());
    private final Map<File, Boolean> readOnlyFiles = Collections.synchronizedMap(new LinkedHashMap<File, Boolean>() {
        @Override
        protected boolean removeEldestEntry (Entry<File, Boolean> eldest) {
            return size() > 100;
        }
    });
    private static final RequestProcessor RP = new RequestProcessor("Subversion FileSystemHandler", 1, false, false); //NOI18N

    /**
     * Stores .svn folders that should be deleted ASAP.
     */
    private final Set<File> invalidMetadata = new HashSet<File>(5);
    private static final int STATUS_VCS_MODIFIED_ATTRIBUTE
            = FileInformation.STATUS_VERSIONED_CONFLICT
            | FileInformation.STATUS_VERSIONED_MERGE
            | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
            | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY
            | FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;
    public FilesystemHandler(Subversion svn) {
        cache = svn.getStatusCache();
    }

    @Override
    public boolean beforeDelete(File file) {
        Subversion.LOG.log(Level.FINE, "beforeDelete {0}", file);
        if(!SvnClientFactory.isClientAvailable()) {
            Subversion.LOG.fine(" skipping delete due to missing client");
            return false;
        }
        if (SvnUtils.isPartOfSubversionMetadata(file)) return true;
        // calling cache results in SOE, we must check manually
        return isVersioned(file.getParentFile());
    }

    /**
     * This interceptor ensures that subversion metadata is NOT deleted.
     *
     * @param file file to delete
     */
    @Override
    public void doDelete(File file) throws IOException {
        Subversion.LOG.log(Level.FINE, "doDelete {0}", file);
        if (!SvnUtils.isPartOfSubversionMetadata(file)) {
            try {
                SvnClient client = Subversion.getInstance().getClient(false);
                try {
                    client.remove(new File [] { file }, true); // delete all files recursively
                    return;
                } catch (SVNClientException ex) {
                    // not interested, will continue and ask isVersioned()
                }
                /**
                 * Copy a folder, it becames svn added/copied.
                 * Revert its parent and check 'Delete new files', the folder becomes unversioned.
                 * 'Delete new files' deletes the folder and invokes this method.
                 * But client.remove cannot be called since the folder is unversioned and we do not want to propagate an exception
                 */
                if (isVersioned(file.getParentFile())) {
                    client.remove(new File [] { file }, true); // delete all files recursively
                }
                // with the cache refresh we rely on afterDelete
            } catch (SVNClientException e) {
                if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                    SvnClientExceptionHandler.notifyException(e, false, false); // log this
                }
                IOException ex = new IOException();
                Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(FilesystemHandler.class, "MSG_DeleteFailed", new Object[] {file, e.getLocalizedMessage()})); // NOI18N
                ex.getCause().initCause(e);
                throw ex;
            } finally {
                internalyDeletedFiles.add(file);
            }
        }
    }

    @Override
    public void afterDelete(final File file) {
        Subversion.LOG.log(Level.FINE, "afterDelete {0}", file);
        if (file == null || SvnUtils.isPartOfSubversionMetadata(file)) return;

        // TODO the afterXXX events should not be triggered by the FS listener events
        //      their order isn't guaranteed when e.g calling fo.delete() a fo.create()
        //      in an atomic action

        // check if delete already handled
        if(internalyDeletedFiles.remove(file)) {
            // file was already deleted we only have to refresh the cache
            cache.refreshAsync(file);
            return;
        }

        // there was no doDelete event for the file ->
        // could be deleted externaly, so try to handle it by 'svn rm' too
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    File parent = file.getParentFile();
                    if(parent != null && !parent.exists()) {
                        return;
                    }
                    try {
                        SvnClient client = Subversion.getInstance().getClient(false);
                        if (shallRemove(client, file)) {
                            client.remove(new File [] { file }, true);
                        }
                    } catch (SVNClientException e) {
                        // ignore; we do not know what to do here
                        Subversion.LOG.log(Level.FINER, null, e);
                    }
                } finally {
                    cache.refreshAsync(file);
                }
            }
        });
    }

    /**
     * Moves folder's content between different repositories.
     * Does not move folders, only files inside them.
     * The created tree in the target working copy is created without subversion metadata.
     * @param from folder being moved. MUST be a folder.
     * @param to a folder from's content shall be moved into
     * @throws java.io.IOException if error occurs
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException if error occurs
     */
    private void moveFolderToDifferentRepository(File from, File to) throws IOException, SVNClientException {
        assert from.isDirectory();
        assert to.getParentFile().exists();
        if (!to.exists()) {
            if (to.mkdir()) {
                cache.refreshAsync(to);
            } else {
                Subversion.LOG.log(Level.WARNING, "{0}: Cannot create folder {1}", new Object[]{FilesystemHandler.class.getName(), to});
            }
        }
        File[] files = from.listFiles();
        for (File file : files) {
            if (!SvnUtils.isAdministrative(file)) {
                svnMoveImplementation(file, new File(to, file.getName()));
            }
        }
    }

    /**
     * Copies folder's content between different repositories.
     * Does not copy folders, only files inside them.
     * The created tree in the target working copy is created without subversion metadata.
     * @param from folder being copied. MUST be a folder.
     * @param to a folder from's content shall be copied into
     * @throws java.io.IOException if error occurs
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException if error occurs
     */
    private void copyFolderToDifferentRepository(File from, File to) throws IOException, SVNClientException {
        assert from.isDirectory();
        assert to.getParentFile().exists();
        if (!to.exists()) {
            if (to.mkdir()) {
                cache.refreshAsync(to);
            } else {
                Subversion.LOG.log(Level.WARNING, "{0}: Cannot create folder {1}", new Object[]{FilesystemHandler.class.getName(), to});
            }
        }
        File[] files = from.listFiles();
        for (File file : files) {
            if (!SvnUtils.isAdministrative(file)) {
                svnCopyImplementation(file, new File(to, file.getName()));
            }
        }
    }

    /**
     * Tries to determine if the <code>file</code> is supposed to be really removed by svn or not.<br/>
     * i.e. unversioned files should not be removed at all.
     * @param file file sheduled for removal
     * @return <code>true</code> if the <code>file</code> shall be really removed, <code>false</code> otherwise.
     */
    private boolean shallRemove(SvnClient client, File file) throws SVNClientException {
        boolean retval = true;
        if (!"true".equals(System.getProperty("org.netbeans.modules.subversion.deleteMissingFiles", "false"))) { //NOI18N
            // prevents automatic svn remove for those who dislike such behavior
            Subversion.LOG.log(Level.FINE, "File {0} deleted externally, metadata not repaired (org.netbeans.modules.subversion.deleteMissingFiles=false by default)", new String[] {file.getAbsolutePath()}); //NOI18N
            retval = false;
        } else {
            ISVNStatus status = getStatus(client, file);
            if (!SVNStatusKind.MISSING.equals(status.getTextStatus())) {
                Subversion.LOG.fine(" shallRemove: skipping delete due to correct metadata");
                retval = false;
            } else if (Utilities.isMac() || Utilities.isWindows()) {
                String existingFilename = FileUtils.getExistingFilenameInParent(file);
                if (existingFilename != null) {
                    retval = false;
                }
            }
        }
        return retval;
    }

    @Override
    public boolean beforeMove(File from, File to) {
        Subversion.LOG.log(Level.FINE, "beforeMove {0} -> {1}", new Object[]{from, to});
        if(!SvnClientFactory.isClientAvailable()) {
            Subversion.LOG.fine(" skipping move due to missing client");
            return false;
        }
        File destDir = to.getParentFile();
        if (from != null && destDir != null) {
            // a direct cache call could, because of the synchrone beforeMove handling,
            // trigger an reentrant call on FS => we have to check manually
            if (isVersioned(from) || isVersioned(to)) {
                return SvnUtils.isManaged(to);
            }
            // else XXX handle file with saved administative
            // right now they have old status in cache but is it guaranteed?
        }
        return false;
    }

    @Override
    public void doMove(final File from, final File to) throws IOException {
        Subversion.LOG.log(Level.FINE, "doMove {0} -> {1}", new Object[]{from, to});
        svnMoveImplementation(from, to);
    }

    @Override
    public void afterMove(final File from, final File to) {
        Subversion.LOG.log(Level.FINE, "afterMove {0} -> {1}", new Object[]{from, to});
        File[] files;
        synchronized(movedFiles) {
            movedFiles.add(from);
            files = movedFiles.toArray(new File[0]);
            movedFiles.clear();
        }
        cache.refreshAsync(true, to);  // refresh the whole target tree
        cache.refreshAsync(files);
        File parent = to.getParentFile();
        if (parent != null) {
            if (from.equals(to)) {
                Subversion.LOG.log(Level.WARNING, "Wrong (identity) rename event for {0}", from.getAbsolutePath());
            }
        }
    }

    @Override
    public boolean beforeCopy(File from, File to) {
        Subversion.LOG.log(Level.FINE, "beforeCopy {0} -> {1}", new Object[]{from, to});
        if(!SvnClientFactory.isClientAvailable()) {
            Subversion.LOG.fine(" skipping copy due to missing client");
            return false;
        }

        File destDir = to.getParentFile();
        if (from != null && destDir != null) {
            // a direct cache call could, because of the synchrone beforeCopy handling,
            // trigger an reentrant call on FS => we have to check manually
            if (isVersioned(from) || isVersioned(to)) {
                
                if(from.isDirectory()) {
                    // always handle copy of versioned folders.
                    // we have to take care of metadata even if svn copy can't
                    // be called - e.g when copy into an unversioned folder filesystem would also 
                    // try to copy the .svn folder and fail
                    return true;
                } else {
                    return SvnUtils.isManaged(to);
                }
            } 
            // else XXX handle file with saved administative
            // right now they have old status in cache but is it guaranteed?
        }        

        return false;
    }

    @Override
    public void doCopy(final File from, final File to) throws IOException {
        Subversion.LOG.log(Level.FINE, "doCopy {0} -> {1}", new Object[]{from, to});
        svnCopyImplementation(from, to);
    }

    @Override
    public void afterCopy(final File from, final File to) {
        Subversion.LOG.log(Level.FINE, "afterCopy {0} -> {1}", new Object[]{from, to});
        File[] files;
        synchronized(copiedFiles) {
            copiedFiles.add(from);
            files = copiedFiles.toArray(new File[0]);
            copiedFiles.clear();
        }
        cache.refreshAsync(true, to);  // refresh the whole target tree
        cache.refreshAsync(files);
        File parent = to.getParentFile();
        if (parent != null) {
            if (from.equals(to)) {
                Subversion.LOG.log(Level.WARNING, "Wrong (identity) rename event for {0}", from.getAbsolutePath());
            }
        }
    }

    private void svnCopyImplementation(final File from, final File to) throws IOException {
        try {
            SvnClient client = Subversion.getInstance().getClient(false);

            // prepare destination, it must be under Subversion control
            removeInvalidMetadata();

            File parent;
            if (to.isDirectory()) {
                parent = to;
            } else {
                parent = to.getParentFile();
            }

            boolean parentManaged = false;
            boolean parentIgnored = false;
            if (parent != null) {
                parentManaged = SvnUtils.isManaged(parent);
                // a direct cache call could, because of the synchrone svnMove/CopyImplementation handling,
                // trigger an reentrant call on FS => we have to check manually
                if (parentManaged && !isVersioned(parent)) {
                    parentIgnored = !addDirectories(parent);
                }
            }

            // perform
            int retryCounter = 6;
            while (true) {
                try {
                    ISVNStatus toStatus = getStatus(client, to);

                    // check the status - if the file isn't in the repository yet ( ADDED | UNVERSIONED )
                    // then it also can't be moved via the svn client
                    ISVNStatus status = getStatus(client, from);

                    // store all from-s children -> they also have to be refreshed in after copy
                    List<File> srcChildren = null;
                    try {
                        srcChildren = SvnUtils.listManagedRecursively(from);
                        if (parentIgnored) {
                            // do not svn copy into ignored folders
                            if(!copyFile(from, to)) {
                                Subversion.LOG.log(Level.INFO, "Cannot copy file {0} to {1}", new Object[] {from, to});
                            }
                        } else if (status != null && (status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)
                                || status.getTextStatus().equals(SVNStatusKind.IGNORED))) { // ignored file CAN'T be moved via svn
                            // check if the file wasn't just deleted in this session
                            revertDeleted(client, toStatus, to, true);

                            if(!copyFile(from, to)) {
                                Subversion.LOG.log(Level.INFO, "Cannot copy file {0} to {1}", new Object[] {from, to});
                            }
                        } else {
                            SVNUrl repositorySource = SvnUtils.getRepositoryRootUrl(from);
                            SVNUrl repositoryTarget = parentManaged ? SvnUtils.getRepositoryRootUrl(parent) : null;
                            if (parentManaged && repositorySource.equals(repositoryTarget)) {
                                // use client.copy only for a single repository
                                client.copy(from, to);
                            } else {
                                // copy into unversioned folder or
                                // from a repository into another
                                if (from.isDirectory()) {
                                    // tree should be copied separately,
                                    // otherwise the metadata from the source WC will be copied too
                                    copyFolderToDifferentRepository(from, to);
                                } else if (copyFile(from, to)) {
                                    Subversion.LOG.log(Level.FINE, FilesystemHandler.class.getName()
                                            + ": copying between different repositories {0} to {1}", new Object[] {from, to});
                                } else {
                                    Subversion.LOG.log(Level.WARNING, FilesystemHandler.class.getName()
                                            + ": cannot copy {0} to {1}", new Object[] {from, to});
                                }
                            }
                        }
                        break;
                    } finally {
                        // we moved the files so schedule them a for a refresh
                        // in the following afterMove call
                        synchronized(copiedFiles) {
                            if(srcChildren != null) {
                                copiedFiles.addAll(srcChildren);
                            }
                        }
                    }
                } catch (SVNClientException e) {
                    // svn: Working copy '/tmp/co/svn-prename-19/AnagramGame-pack-rename/src/com/toy/anagrams/ui2' locked
                    if (e.getMessage().endsWith("' locked") && retryCounter > 0) { // NOI18N
                        // XXX HACK AWT- or FS Monitor Thread performs
                        // concurrent operation
                        try {
                            Thread.sleep(107);
                        } catch (InterruptedException ex) {
                            // ignore
                        }
                        retryCounter--;
                        continue;
                    }
                    if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                        SvnClientExceptionHandler.notifyException(e, false, false); // log this
                    }
                    IOException ex = new IOException();
                    Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(FilesystemHandler.class, "MSG_MoveFailed", new Object[] {from, to, e.getLocalizedMessage()})); // NOI18N
                    ex.getCause().initCause(e);
                    throw ex;
                }
            }
        } catch (SVNClientException e) {
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                SvnClientExceptionHandler.notifyException(e, false, false); // log this
            }
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(ex, "Subversion failed to move " + from.getAbsolutePath() + " to: " + to.getAbsolutePath() + "\n" + e.getLocalizedMessage()); // NOI18N
            ex.getCause().initCause(e);
            throw ex;
        }                 

    }

    @Override
    public boolean beforeCreate(File file, boolean isDirectory) {
        Subversion.LOG.log(Level.FINE, "beforeCreate {0}", file);
        if(!SvnClientFactory.isClientAvailable()) {
            Subversion.LOG.fine(" skipping create due to missing client");
            return false;
        }
        if (SvnUtils.isPartOfSubversionMetadata(file)) {
            synchronized(invalidMetadata) {
                File p = file;
                while(!SvnUtils.isAdministrative(p.getName())) {
                    p = p.getParentFile();
                    assert p != null : "file " + file + " doesn't have a .svn parent";
                }
                invalidMetadata.add(p);
            }
            return false;
        } else {
            if (!file.exists()) {
                try {
                    SvnClient client = Subversion.getInstance().getClient(false);
                    // check if the file wasn't just deleted in this session
                    revertDeleted(client, file, true);
                } catch (SVNClientException ex) {
                    if (!WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                        SvnClientExceptionHandler.notifyException(ex, false, false);
                    }
                }
            }
            return false;
        }
    }

    @Override
    public void doCreate(File file, boolean isDirectory) throws IOException {
        // do nothing
    }

    @Override
    public void afterCreate(final File file) {
        Subversion.LOG.log(Level.FINE, "afterCreate {0}", file);
        if (SvnUtils.isPartOfSubversionMetadata(file)) {
            // not interested in .svn events
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                if (file == null) return;
                // I. refresh cache
                int status = cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
                if ((status & FileInformation.STATUS_MANAGED) == 0) {
                    return;
                }
                if (file.isDirectory()) {
                    // II. refresh the whole dir
                    cache.directoryContentChanged(file);
                } else if ((status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) != 0 && file.exists()) {
                    // file exists but it's status is set to deleted
                    File temporary = FileUtils.generateTemporaryFile(file.getParentFile(), file.getName());
                    try {
                        SvnClient client = Subversion.getInstance().getClient(false);
                        if (file.renameTo(temporary)) {
                            client.revert(file, false);
                            file.delete();
                        } else {
                            Subversion.LOG.log(Level.WARNING, "FileSystemHandler.afterCreate: cannot rename {0} to {1}", new Object[] { file, temporary }); //NOI18N
                            client.addFile(file); // at least add the file so it is not deleted
                        }
                    } catch (SVNClientException ex) {
                        Subversion.LOG.log(Level.INFO, null, ex);
                    } finally {
                        if (temporary.exists()) {
                            try {
                                if (!temporary.renameTo(file)) {
                                    Subversion.LOG.log(Level.WARNING, "FileSystemHandler.afterCreate: cannot rename {0} back to {1}, {1} exists={2}", new Object[] { temporary, file, file.exists() }); //NOI18N
                                    FileUtils.copyFile(temporary, file);
                                }
                            } catch (IOException ex) {
                                Subversion.LOG.log(Level.INFO, "FileSystemHandler.afterCreate: cannot copy {0} back to {1}", new Object[] { temporary, file }); //NOI18N
                            } finally {
                                temporary.delete();
                            }
                        }
                        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
                    }
                }
            }
        });
    }

    @Override
    public void afterChange(final File file) {
        if(!SvnClientFactory.isClientAvailable()) {
            Subversion.LOG.fine(" skipping afterChange due to missing client");
            return;
        }
        Subversion.LOG.log(Level.FINE, "afterChange {0}", file);
        RP.post(new Runnable() {
            @Override
            public void run() {
                if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
                    cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
            }
        });
    }

    @Override
    public Object getAttribute(final File file, String attrName) {
        if("ProvidedExtensions.RemoteLocation".equals(attrName)) {
            return getRemoteRepository(file);
        } else if("ProvidedExtensions.Refresh".equals(attrName)) {
            return new Runnable() {
                @Override
                public void run() {
                    if (!SvnClientFactory.isClientAvailable()) {
                        Subversion.LOG.fine(" skipping ProvidedExtensions.Refresh due to missing client"); //NOI18N
                        return;
                    }
                    if (!SvnUtils.isManaged(file)) {
                        return;
                    }
                    try {
                        SvnClient client = Subversion.getInstance().getClient(file);
                        if (client != null) {
                            Subversion.getInstance().getStatusCache().refreshCached(new Context(file));
                            StatusAction.executeStatus(file, client, null, false); // no need to contact server
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ex, true, true);
                        return;
                    }
                }
            };
        } else if (SearchHistorySupport.PROVIDED_EXTENSIONS_SEARCH_HISTORY.equals(attrName)){
            return new SvnSearchHistorySupport(file);
        } else if ("ProvidedExtensions.VCSIsModified".equals(attrName)) {

            if (file == null) {
                return null;
            }

            if (!SvnClientFactory.isClientAvailable()) {
                Subversion.LOG.fine(" skipping ProvidedExtensions.VCSIsModified due to missing client"); //NOI18N
                return null;
            }
            if (!SvnUtils.isManaged(file)) {
                return null;
            }
            try {
                SvnClient client = Subversion.getInstance().getClient(file);
                if (client != null) {
                    Context ctx = new Context(file);
                    Subversion.getInstance().getStatusCache().refreshCached(ctx);
                    StatusAction.executeStatus(file, client, null, false); // no need to contact server
                    return cache.containsFiles(ctx, STATUS_VCS_MODIFIED_ATTRIBUTE, true);
                }
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
            }
            return null;
        } else {
            return super.getAttribute(file, attrName);
        }
    }

    @Override
    public void beforeEdit (final File file) {
        if (cache.ready()) {
            NotificationsManager.getInstance().scheduleFor(file);
        }
        ensureLocked(file);
    }

    @Override
    public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
        long retval = -1;
        if (SvnUtils.isAdministrative(dir.getName())) {
            retval = 0;
        }
        return retval;
    }

    @Override
    public boolean isMutable(File file) {
        boolean mutable = SvnUtils.isPartOfSubversionMetadata(file) || super.isMutable(file);
        if (!mutable && SvnModuleConfig.getDefault().isAutoLock() && !readOnlyFiles.containsKey(file)) {
            toLockFiles.add(file);
            return true;
        }
        return mutable;
    }

    private String getRemoteRepository(File file) {
        if(file == null) return null;
        SVNUrl url = null;
        try {
            url = SvnUtils.getRepositoryRootUrl(file);
        } catch (SVNClientException ex) {
            Subversion.LOG.log(Level.FINE, "No repository root url found for managed file : [" + file + "]", ex); //NOI18N
            try {
                url = SvnUtils.getRepositoryUrl(file); // try to falback
            } catch (SVNClientException ex1) {
                Subversion.LOG.log(Level.FINE, "No repository url found for managed file : [" + file + "]", ex1);
            }
        }
        return url != null ? SvnUtils.decodeToString(url) : null;
    }

    /**
     * Removes invalid metadata from all known folders.
     */
    void removeInvalidMetadata() {
        synchronized(invalidMetadata) {
            for (File file : invalidMetadata) {
                Utils.deleteRecursively(file);
            }
            invalidMetadata.clear();
        }
    }

    // private methods ---------------------------

    private boolean hasMetadata(File file) {
        return new File(file, SvnUtils.SVN_ENTRIES_DIR).canRead();
    }

    private boolean isVersioned(File file) {
        if (SvnUtils.isPartOfSubversionMetadata(file)) return false;
        if  (( !file.isFile() && hasMetadata(file) ) || ( file.isFile() && hasMetadata(file.getParentFile()) )) {
            return true;
        }
        try {
            SVNStatusKind statusKind = SvnUtils.getSingleStatus(Subversion.getInstance().getClient(false), file).getTextStatus();
            return statusKind != SVNStatusKind.UNVERSIONED && statusKind != SVNStatusKind.IGNORED;
        } catch (SVNClientException ex) {
            return false;
        }
    }

    /**
     * Returns all direct parent folders from the given file which are scheduled for deletion
     *
     * @param file
     * @param client
     * @return a list of folders
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    private static List<File> getDeletedParents(File file, SvnClient client) throws SVNClientException {
        List<File> ret = new ArrayList<File>();
        for(File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {
            ISVNStatus status = getStatus(client, parent);
            if (status == null || !status.getTextStatus().equals(SVNStatusKind.DELETED)) {
                return ret;
            }
            ret.add(parent);
        }
        return ret;
    }

    private void revertDeleted(SvnClient client, final File file, boolean checkParents) {
        try {
            ISVNStatus status = getStatus(client, file);
            revertDeleted(client, status, file, checkParents);
        } catch (SVNClientException ex) {
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
            }
        }
    }

    private void revertDeleted(SvnClient client, ISVNStatus status, final File file, boolean checkParents) {
        try {
            if (FilesystemHandler.this.equals(status, SVNStatusKind.DELETED)) {
                if(checkParents) {
                    // we have a file scheduled for deletion but it's going to be created again,
                    // => it's parent folder can't stay deleted either
                    final List<File> deletedParents = getDeletedParents(file, client);
                    // XXX JAVAHL client.revert(deletedParents.toArray(new File[deletedParents.size()]), false);
                    for (File parent : deletedParents) {
                        client.revert(parent, false);
                    }
                    if (!deletedParents.isEmpty()) {
                        Subversion.getInstance().getStatusCache().refreshAsync(deletedParents.toArray(new File[0]));
                    }
                }

                // reverting the file will set the metadata uptodate
                client.revert(file, false);
                // our goal was ony to fix the metadata ->
                //  -> get rid of the reverted file
                internalyDeletedFiles.add(file); // prevents later removal in afterDelete if the file is recreated
                file.delete();
            }
        } catch (SVNClientException ex) {
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
            }
        }
    }

    private void svnMoveImplementation(final File from, final File to) throws IOException {        
        try {
            boolean force = true; // file with local changes must be forced
            SvnClient client = Subversion.getInstance().getClient(false);

            // prepare destination, it must be under Subversion control
            removeInvalidMetadata();

            File parent;
            if (to.isDirectory()) {
                parent = to;
            } else {
                parent = to.getParentFile();
            }

            boolean parentIgnored = false;
            if (parent != null) {
                assert SvnUtils.isManaged(parent) : "Cannot move " + from.getAbsolutePath() + " to " + to.getAbsolutePath() + ", " + parent.getAbsolutePath() + " is not managed";  // NOI18N see implsMove above
                // a direct cache call could, because of the synchrone svnMoveImplementation handling,
                // trigger an reentrant call on FS => we have to check manually
                if (!isVersioned(parent)) {
                    parentIgnored = !addDirectories(parent);
                }
            }

            // perform
            int retryCounter = 6;
            while (true) {
                try {
                    ISVNStatus toStatus = getStatus(client, to);

                    // check the status - if the file isn't in the repository yet ( ADDED | UNVERSIONED )
                    // then it also can't be moved via the svn client
                    ISVNStatus status = getStatus(client, from);

                    // store all from-s children -> they also have to be refreshed in after move
                    List<File> srcChildren = null;
                    SVNUrl url = status != null && status.isCopied() ? getCopiedUrl(client, from) : null;
                    SVNUrl toUrl = toStatus != null ? toStatus.getUrl() : null;
                    try {
                        srcChildren = SvnUtils.listManagedRecursively(from);
                        boolean moved = true;
                        if (status != null 
                                && (status.getTextStatus().equals(SVNStatusKind.ADDED) || status.getTextStatus().equals(SVNStatusKind.REPLACED)) 
                                && (!status.isCopied() || (url != null && url.equals(toUrl)))) {
                            // 1. file is ADDED (new or added) AND is not COPIED (by invoking svn copy)
                            // 2. file is ADDED and COPIED (by invoking svn copy) and target equals the original from the first copy
                            // otherwise svn move should be invoked

                            File temp = from;
                            if (Utilities.isWindows() && from.equals(to) || Utilities.isMac() && from.getPath().equalsIgnoreCase(to.getPath())) {
                                Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround for filename case change {0} -> {1}", new Object[] { from, to }); //NOI18N
                                temp = FileUtils.generateTemporaryFile(from.getParentFile(), from.getName());
                                Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround, step 1: {0} -> {1}", new Object[] { from, temp }); //NOI18N
                                client.move(from, temp, force);
                            }
                            
                            // check if the file wasn't just deleted in this session
                            revertDeleted(client, toStatus, to, true);

                            moved = temp.renameTo(to);
                            if (moved) {
                                // indeed just ADDED, not REPLACED
                                if (status.getTextStatus().equals(SVNStatusKind.ADDED)) {
                                    client.revert(temp, true);
                                } else {
                                    client.remove(new File[] { temp }, true);
                                }
                            }
                        } else if (status != null && (status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)
                                || status.getTextStatus().equals(SVNStatusKind.IGNORED))) { // ignored file CAN'T be moved via svn
                            // check if the file wasn't just deleted in this session
                            revertDeleted(client, toStatus, to, true);

                            moved = from.renameTo(to);
                        } else if (parentIgnored) {
                            // parent is ignored so do not add the file
                            moved = from.renameTo(to);
                            client.remove(new File[] { from }, true);
                        } else {
                            SVNUrl repositorySource = SvnUtils.getRepositoryRootUrl(from);
                            SVNUrl repositoryTarget = SvnUtils.getRepositoryRootUrl(parent);
                            if (repositorySource.equals(repositoryTarget)) {
                                // use client.move only for a single repository
                                try {
                                    client.move(from, to, force);
                                } catch (SVNClientException ex) {
                                    if (Utilities.isWindows() && from.equals(to) || Utilities.isMac() && from.getPath().equalsIgnoreCase(to.getPath())) {
                                        Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround for filename case change {0} -> {1}", new Object[] { from, to }); //NOI18N
                                        File temp = FileUtils.generateTemporaryFile(to.getParentFile(), from.getName());
                                        Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround, step 1: {0} -> {1}", new Object[] { from, temp }); //NOI18N
                                        client.move(from, temp, force);
                                        Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround, step 2: {0} -> {1}", new Object[] { temp, to }); //NOI18N
                                        client.move(temp, to, force);
                                        Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround completed"); //NOI18N
                                    } else {
                                        throw ex;
                                    }
                                }
                            } else {
                                boolean remove = false;
                                if (from.isDirectory()) {
                                    // tree should be moved separately, otherwise the metadata from the source WC will be copied too
                                    moveFolderToDifferentRepository(from, to);
                                    remove = true;
                                } else if (from.renameTo(to)) {
                                    remove = true;
                                } else {
                                    Subversion.LOG.log(Level.WARNING, FilesystemHandler.class.getName()
                                            + ": cannot rename {0} to {1}", new Object[] {from, to});
                                }
                                if (remove) {
                                    client.remove(new File[] {from}, force);
                                    Subversion.LOG.log(Level.FINE, FilesystemHandler.class.getName()
                                            + ": moving between different repositories {0} to {1}", new Object[] {from, to});
                                }
                            }
                        }
                        if (!moved) {
                            Subversion.LOG.log(Level.INFO, "Cannot rename file {0} to {1}", new Object[] {from, to});
                        }
                    } finally {
                        // we moved the files so schedule them a for a refresh
                        // in the following afterMove call
                        synchronized(movedFiles) {
                            if(srcChildren != null) {
                                movedFiles.addAll(srcChildren);
                            }
                        }
                    }
                    break;
                } catch (SVNClientException e) {
                    // svn: Working copy '/tmp/co/svn-prename-19/AnagramGame-pack-rename/src/com/toy/anagrams/ui2' locked
                    if (e.getMessage().endsWith("' locked") && retryCounter > 0) { // NOI18N
                        // XXX HACK AWT- or FS Monitor Thread performs
                        // concurrent operation
                        try {
                            Thread.sleep(107);
                        } catch (InterruptedException ex) {
                            // ignore
                        }
                        retryCounter--;
                        continue;
                    }
                    if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                        SvnClientExceptionHandler.notifyException(e, false, false); // log this
                    }
                    IOException ex = new IOException();
                    Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(FilesystemHandler.class, "MSG_MoveFailed", new Object[] {from, to, e.getLocalizedMessage()})); //NOI18N
                    ex.getCause().initCause(e);
                    throw ex;
                }
            }
        } catch (SVNClientException e) {
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                SvnClientExceptionHandler.notifyException(e, false, false); // log this
            }
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(ex, "Subversion failed to move " + from.getAbsolutePath() + " to: " + to.getAbsolutePath() + "\n" + e.getLocalizedMessage()); // NOI18N
            ex.getCause().initCause(e);
            throw ex;
        }
    }

    /**
     * Seeks versioned root and then adds all folders
     * under Subversion (so it contains metadata),
     */
    private boolean addDirectories(final File dir) throws SVNClientException  {
        SvnClient client = Subversion.getInstance().getClient(false);
        ISVNStatus s = getStatus(client, dir);
        if(s.getTextStatus().equals(SVNStatusKind.IGNORED)) {
            return false;
        }
        File parent = dir.getParentFile();
        if (parent != null) {
            if (SvnUtils.isManaged(parent) && !isVersioned(parent)) {
                if(!addDirectories(parent)) {  // RECURSION
                    return false;
                }
            }
            client.addDirectory(dir, false);
            cache.refreshAsync(dir);
            return true;
        } else {
            throw new SVNClientException("Reached FS root, but it's still not Subversion versioned!"); // NOI18N
        }
    }

    private static ISVNStatus getStatus(SvnClient client, File file) throws SVNClientException {
        // a direct cache call could, because of the synchrone beforeCreate handling,
        // trigger an reentrant call on FS => we have to check manually
        return SvnUtils.getSingleStatus(client, file);
    }

    private boolean equals(ISVNStatus status, SVNStatusKind kind) {
        return status != null && status.getTextStatus().equals(kind);
    }

    private boolean copyFile(File from, File to) {
        try {
            FileUtils.copyFile(from, to);
        } catch (IOException ex) {
            SvnClientExceptionHandler.notifyException(ex, false, false); // log this
            return false;
        }
        return true;
    }

    private void ensureLocked (final File file) {
        if (toLockFiles.contains(file)) {
            Runnable outsideAWT = new Runnable () {
                @Override
                public void run () {
                    boolean readOnly = true;
                    try {
                        // unlock files that...
                        // ... have svn:needs-lock prop set
                        SvnClient client = Subversion.getInstance().getClient(false);
                        boolean hasPropSet = false;
                        for (ISVNProperty prop : client.getProperties(file)) {
                            if ("svn:needs-lock".equals(prop.getName())) { //NOI18N
                                hasPropSet = true;
                                break;
                            }
                        }
                        if (hasPropSet) {
                            ISVNStatus status = SvnUtils.getSingleStatus(client, file);
                            // ... are not just added - lock does not make sense since the file is not in repo yet
                            if (status != null && status.getTextStatus() != SVNStatusKind.ADDED) {
                                SVNUrl url = SvnUtils.getRepositoryRootUrl(file);
                                if (url != null) {
                                    client = Subversion.getInstance().getClient(url);
                                    if (status.getLockOwner() != null) {
                                        // the file is locked yet it's still read-only, it may be a result of:
                                        // 1. svn lock A
                                        // 2. svn move A B - B is new and read-only
                                        // 3. move B A - A is now also read-only
                                        client.unlock(new File[] { file }, false); //NOI18N
                                    }
                                    client.lock(new File[] { file }, "", false); //NOI18N
                                    readOnly = false;
                                }
                            }
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ex, false, false);
                        readOnly = true;
                    }
                    if (readOnly) {
                        // conditions to unlock failed, set the file to read-only
                        readOnlyFiles.put(file, Boolean.TRUE);
                    }
                    toLockFiles.remove(file);
                }
            };
            if (EventQueue.isDispatchThread()) {
                Subversion.getInstance().getRequestProcessor().post(outsideAWT);
            } else {
                outsideAWT.run();
            }
        }
    }

    private SVNUrl getCopiedUrl (SvnClient client, File f) {
        try {
            ISVNInfo info = SvnUtils.getInfoFromWorkingCopy(client, f);
            if (info != null) {
                return info.getCopyUrl();
            }
        } catch (SVNClientException e) {
            // at least log the exception
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                Subversion.LOG.log(Level.INFO, null, e);
            }
        }
        return null;
    }

}
