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
package org.netbeans.modules.subversion.remote;

import org.netbeans.modules.subversion.remote.client.SvnClientFactory;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.remotefs.versioning.api.SearchHistorySupport;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNProperty;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.notifications.NotificationsManager;
import org.netbeans.modules.subversion.remote.ui.status.StatusAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnSearchHistorySupport;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 *
 * 
 */
class FilesystemHandler extends VCSInterceptor {

    private final FileStatusCache   cache;

    /**
     * Stores all moved files for a later cache refresh in afterMove
     */
    private final Set<VCSFileProxy> movedFiles = new HashSet<>();
    private final Set<VCSFileProxy> copiedFiles = new HashSet<>();

    private final Set<VCSFileProxy> internalyDeletedFiles = new HashSet<>();
    private final Set<VCSFileProxy> toLockFiles = Collections.synchronizedSet(new HashSet<VCSFileProxy>());
    private final Map<VCSFileProxy, Boolean> readOnlyFiles = Collections.synchronizedMap(new LinkedHashMap<VCSFileProxy, Boolean>() {
        @Override
        protected boolean removeEldestEntry (Map.Entry<VCSFileProxy, Boolean> eldest) {
            return size() > 100;
        }
    });
    private static final RequestProcessor RP = new RequestProcessor("Subversion FileSystemHandler", 1, false, false); //NOI18N

    /**
     * Stores .svn folders that should be deleted ASAP.
     */
    private final Set<VCSFileProxy> invalidMetadata = new HashSet<>(5);
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
    public boolean beforeDelete(VCSFileProxy file) {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
           Subversion.LOG.log(Level.FINE, "beforeDelete {0}", file);
        }
        if(!SvnClientFactory.isClientAvailable(new Context(file))) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.fine(" skipping delete due to missing client");
            }
            return false;
        }
        if (SvnUtils.isPartOfSubversionMetadata(file)) {
            return true;
        }
        // calling cache results in SOE, we must check manually
        return isVersioned(file.getParentFile());
    }

    /**
     * This interceptor ensures that subversion metadata is NOT deleted.
     *
     * @param file file to delete
     */
    @Override
    public void doDelete(VCSFileProxy file) throws IOException {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "doDelete {0}", file);
        }
        if (!SvnUtils.isPartOfSubversionMetadata(file)) {
            try {
                SvnClient client = Subversion.getInstance().getClient(false, new Context(file));
                try {
                    client.remove(new VCSFileProxy [] { file }, true); // delete all files recursively
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
                    client.remove(new VCSFileProxy [] { file }, true); // delete all files recursively
                }
                // with the cache refresh we rely on afterDelete
            } catch (SVNClientException e) {
                // skip "does not exist" exception.
                // It can be after move
                if(e.getMessage().indexOf("does not exist") < 0) { //NOI18N
                    if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                        SvnClientExceptionHandler.notifyException(new Context(file), e, false, false); // log this
                    }
                    IOException ex = new IOException();
                    Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(FilesystemHandler.class, "MSG_DeleteFailed", new Object[] {file, e.getLocalizedMessage()})); // NOI18N
                    ex.getCause().initCause(e);
                    throw ex;
                }
            } finally {
                internalyDeletedFiles.add(file);
            }
        }
    }

    @Override
    public void afterDelete(final VCSFileProxy file) {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "afterDelete {0}", file);
        }
        if (file == null || SvnUtils.isPartOfSubversionMetadata(file)) {
            return;
        }

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
                    VCSFileProxy parent = file.getParentFile();
                    if(parent != null && !parent.exists()) {
                        return;
                    }
                    try {
                        SvnClient client = Subversion.getInstance().getClient(false, new Context(file));
                        if (shallRemove(client, file)) {
                            client.remove(new VCSFileProxy [] { file }, true);
                        }
                    } catch (SVNClientException e) {
                        // ignore; we do not know what to do here
                        if (Subversion.LOG.isLoggable(Level.FINER)) {
                            Subversion.LOG.log(Level.FINER, null, e);
                        }
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
    private void moveFolderToDifferentRepository(VCSFileProxy from, VCSFileProxy to) throws IOException, SVNClientException {
        assert from.isDirectory();
        assert to.getParentFile().exists();
        if (!to.exists()) {
            if (VCSFileProxySupport.mkdir(to)) {
                cache.refreshAsync(to);
            } else {
                Subversion.LOG.log(Level.WARNING, "{0}: Cannot create folder {1}", new Object[]{FilesystemHandler.class.getName(), to});
            }
        }
        VCSFileProxy[] files = from.listFiles();
        if (files != null) {
            for (VCSFileProxy file : files) {
                if (!SvnUtils.isAdministrative(file)) {
                    svnMoveImplementation(file, VCSFileProxy.createFileProxy(to, file.getName()));
                }
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
    private void copyFolderToDifferentRepository(VCSFileProxy from, VCSFileProxy to) throws IOException, SVNClientException {
        assert from.isDirectory();
        assert to.getParentFile().exists();
        if (!to.exists()) {
            if (VCSFileProxySupport.mkdir(to)) {
                cache.refreshAsync(to);
            } else {
                Subversion.LOG.log(Level.WARNING, "{0}: Cannot create folder {1}", new Object[]{FilesystemHandler.class.getName(), to});
            }
        }
        VCSFileProxy[] files = from.listFiles();
        if (files != null) {
            for (VCSFileProxy file : files) {
                if (!SvnUtils.isAdministrative(file)) {
                    svnCopyImplementation(file, VCSFileProxy.createFileProxy(to, file.getName()));
                }
            }
        }
    }

    /**
     * Tries to determine if the <code>file</code> is supposed to be really removed by svn or not.<br/>
     * i.e. unversioned files should not be removed at all.
     * @param file file sheduled for removal
     * @return <code>true</code> if the <code>file</code> shall be really removed, <code>false</code> otherwise.
     */
    private boolean shallRemove(SvnClient client, VCSFileProxy file) throws SVNClientException {
        boolean retval = true;
        if (!"true".equals(System.getProperty("org.netbeans.modules.subversion.deleteMissingFiles", "false"))) { //NOI18N
            // prevents automatic svn remove for those who dislike such behavior
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, "File {0} deleted externally, metadata not repaired (org.netbeans.modules.subversion.deleteMissingFiles=false by default)", new String[] {file.getPath()}); //NOI18N
            }
            retval = false;
        } else {
            ISVNStatus status = getStatus(client, file);
            if (!SVNStatusKind.MISSING.equals(status.getTextStatus())) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.fine(" shallRemove: skipping delete due to correct metadata");
                }
                retval = false;
            } else if (VCSFileProxySupport.isMac(file)) {
                String existingFilename = VCSFileProxySupport.getExistingFilenameInParent(file);
                if (existingFilename != null) {
                    retval = false;
                }
            }
        }
        return retval;
    }

    @Override
    public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "beforeMove {0} -> {1}", new Object[]{from, to});
        }
        if(!SvnClientFactory.isClientAvailable(new Context(from))) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.fine(" skipping move due to missing client");
            }
            return false;
        }
        VCSFileProxy destDir = to.getParentFile();
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
    public void doMove(final VCSFileProxy from, final VCSFileProxy to) throws IOException {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "doMove {0} -> {1}", new Object[]{from, to});
        }
        svnMoveImplementation(from, to);
    }

    @Override
    public void afterMove(final VCSFileProxy from, final VCSFileProxy to) {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "afterMove {0} -> {1}", new Object[]{from, to});
        }
        VCSFileProxy[] files;
        synchronized(movedFiles) {
            movedFiles.add(from);
            files = movedFiles.toArray(new VCSFileProxy[movedFiles.size()]);
            movedFiles.clear();
        }
        cache.refreshAsync(true, to);  // refresh the whole target tree
        cache.refreshAsync(files);
        VCSFileProxy parent = to.getParentFile();
        if (parent != null) {
            if (from.equals(to)) {
                Subversion.LOG.log(Level.WARNING, "Wrong (identity) rename event for {0}", from.getPath());
            }
        }
    }

    @Override
    public boolean beforeCopy(VCSFileProxy from, VCSFileProxy to) {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "beforeCopy {0} -> {1}", new Object[]{from, to});
        }
        if(!SvnClientFactory.isClientAvailable(new Context(from))) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.fine(" skipping copy due to missing client");
            }
            return false;
        }

        VCSFileProxy destDir = to.getParentFile();
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
    public void doCopy(final VCSFileProxy from, final VCSFileProxy to) throws IOException {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "doCopy {0} -> {1}", new Object[]{from, to});
        }
        svnCopyImplementation(from, to);
    }

    @Override
    public void afterCopy(final VCSFileProxy from, final VCSFileProxy to) {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "afterCopy {0} -> {1}", new Object[]{from, to});
        }
        VCSFileProxy[] files;
        synchronized(copiedFiles) {
            copiedFiles.add(from);
            files = copiedFiles.toArray(new VCSFileProxy[copiedFiles.size()]);
            copiedFiles.clear();
        }
        cache.refreshAsync(true, to);  // refresh the whole target tree
        cache.refreshAsync(files);
        VCSFileProxy parent = to.getParentFile();
        if (parent != null) {
            if (from.equals(to)) {
                Subversion.LOG.log(Level.WARNING, "Wrong (identity) rename event for {0}", from.getPath());
            }
        }
    }

    private void svnCopyImplementation(final VCSFileProxy from, final VCSFileProxy to) throws IOException {
        try {
            SvnClient client = Subversion.getInstance().getClient(false, new Context(from));

            // prepare destination, it must be under Subversion control
            removeInvalidMetadata();

            VCSFileProxy parent;
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
                    List<VCSFileProxy> srcChildren = null;
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
                                    if (Subversion.LOG.isLoggable(Level.FINE)) {
                                        Subversion.LOG.log(Level.FINE, FilesystemHandler.class.getName()
                                                + ": copying between different repositories {0} to {1}", new Object[] {from, to}); //NOI18N
                                    }
                                } else {
                                    Subversion.LOG.log(Level.WARNING, FilesystemHandler.class.getName()
                                            + ": cannot copy {0} to {1}", new Object[] {from, to}); //NOI18N
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
                        SvnClientExceptionHandler.notifyException(null, e, false, false); // log this
                    }
                    IOException ex = new IOException();
                    Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(FilesystemHandler.class, "MSG_MoveFailed", new Object[] {from, to, e.getLocalizedMessage()})); // NOI18N
                    ex.getCause().initCause(e);
                    throw ex;
                }
            }
        } catch (SVNClientException e) {
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                SvnClientExceptionHandler.notifyException(new Context(from), e, false, false); // log this
            }
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(ex, "Subversion failed to move " + from.getPath() + " to: " + to.getPath() + "\n" + e.getLocalizedMessage()); // NOI18N
            ex.getCause().initCause(e);
            throw ex;
        }                 

    }

    @Override
    public boolean beforeCreate(VCSFileProxy file, boolean isDirectory) {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "beforeCreate {0}", file);
        }
        if(!SvnClientFactory.isClientAvailable(new Context(file))) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.fine(" skipping create due to missing client");
            }
            return false;
        }
        if (SvnUtils.isPartOfSubversionMetadata(file)) {
            synchronized(invalidMetadata) {
                VCSFileProxy p = file;
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
                    SvnClient client = Subversion.getInstance().getClient(false, new Context(file));
                    // check if the file wasn't just deleted in this session
                    revertDeleted(client, file, true);
                } catch (SVNClientException ex) {
                    if (!WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                        SvnClientExceptionHandler.notifyException(new Context(file), ex, false, false);
                    }
                }
            }
            return false;
        }
    }

    @Override
    public void doCreate(VCSFileProxy file, boolean isDirectory) throws IOException {
        // do nothing
    }

    @Override
    public void afterCreate(final VCSFileProxy file) {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "afterCreate {0}", file);
        }
        if (file == null) {
            return;
        }
        if (SvnUtils.isPartOfSubversionMetadata(file)) {
            // not interested in .svn events
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
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
                        VCSFileProxy temporary = VCSFileProxySupport.generateTemporaryFile(file.getParentFile(), file.getName());
                        try {
                            SvnClient client = Subversion.getInstance().getClient(false, new Context(file));
                            if (VCSFileProxySupport.renameTo(file, temporary)) {
                                client.revert(file, false);
                                VCSFileProxySupport.delete(file);
                            } else {
                                Subversion.LOG.log(Level.WARNING, "FileSystemHandler.afterCreate: cannot rename {0} to {1}", new Object[] { file, temporary }); //NOI18N
                                client.addFile(file); // at least add the file so it is not deleted
                            }
                        } catch (SVNClientException ex) {
                            Subversion.LOG.log(Level.INFO, null, ex);
                        } finally {
                            if (temporary.exists()) {
                                try {
                                    if (!VCSFileProxySupport.renameTo(temporary, file)) {
                                        Subversion.LOG.log(Level.WARNING, "FileSystemHandler.afterCreate: cannot rename {0} back to {1}, {1} exists={2}", new Object[] { temporary, file, file.exists() }); //NOI18N
                                        VCSFileProxySupport.copyFile(temporary, file);
                                    }
                                } catch (IOException ex) {
                                    Subversion.LOG.log(Level.INFO, "FileSystemHandler.afterCreate: cannot copy {0} back to {1}", new Object[] { temporary, file }); //NOI18N
                                } finally {
                                    VCSFileProxySupport.delete(temporary);
                                }
                            }
                            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
                        }
                    }
                } catch (Throwable t) {
                    Subversion.LOG.log(Level.INFO, null, t);
                }
            }
        });
    }

    @Override
    public void afterChange(final VCSFileProxy file) {
        if(!SvnClientFactory.isClientAvailable(new Context(file))) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.fine(" skipping afterChange due to missing client");
            }
            return;
        }
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "afterChange {0}", file);
        }
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
    public Object getAttribute(final VCSFileProxy file, String attrName) {
        if("ProvidedExtensions.RemoteLocation".equals(attrName)) { //NOI18N
            return getRemoteRepository(file);
        } else if("ProvidedExtensions.Refresh".equals(attrName)) { //NOI18N
            return new Runnable() {
                @Override
                public void run() {
                    if (!SvnClientFactory.isClientAvailable(new Context(file))) {
                        if (Subversion.LOG.isLoggable(Level.FINE)) {
                            Subversion.LOG.fine(" skipping ProvidedExtensions.Refresh due to missing client"); //NOI18N
                        }
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
                        SvnClientExceptionHandler.notifyException(new Context(file), ex, true, true);
                        return;
                    }
                }
            };
        } else if (SearchHistorySupport.PROVIDED_EXTENSIONS_SEARCH_HISTORY.equals(attrName)){
            return new SvnSearchHistorySupport(file);
        } else if ("ProvidedExtensions.VCSIsModified".equals(attrName)) { //NOI18N

            if (file == null) {
                return null;
            }

            if (!SvnClientFactory.isClientAvailable(new Context(file))) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.fine(" skipping ProvidedExtensions.VCSIsModified due to missing client"); //NOI18N
                }
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
                SvnClientExceptionHandler.notifyException(new Context(file), ex, false, false);
            }
            return null;
        } else {
            return super.getAttribute(file, attrName);
        }
    }

    @Override
    public void beforeEdit (final VCSFileProxy file) {
        if (cache.ready()) {
            NotificationsManager.getInstance().scheduleFor(file);
        }
        ensureLocked(file);
    }

    @Override
    public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
        long retval = -1;
        if (SvnUtils.isAdministrative(dir.getName())) {
            retval = 0;
        }
        return retval;
    }

    @Override
    public boolean isMutable(VCSFileProxy file) {
        boolean mutable = SvnUtils.isPartOfSubversionMetadata(file) || super.isMutable(file);
        if (!mutable && SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(file)).isAutoLock() && !readOnlyFiles.containsKey(file)) {
            toLockFiles.add(file);
            return true;
        }
        return mutable;
    }

    private String getRemoteRepository(VCSFileProxy file) {
        if(file == null) {
            return null;
        }
        SVNUrl url = null;
        try {
            url = SvnUtils.getRepositoryRootUrl(file);
        } catch (SVNClientException ex) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, "No repository root url found for managed file : [" + file + "]", ex); //NOI18N
            }
            try {
                url = SvnUtils.getRepositoryUrl(file); // try to falback
            } catch (SVNClientException ex1) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, "No repository url found for managed file : [" + file + "]", ex1);
                }
            }
        }
        return url != null ? SvnUtils.decodeToString(url) : null;
    }

    /**
     * Removes invalid metadata from all known folders.
     */
    void removeInvalidMetadata() {
        synchronized(invalidMetadata) {
            for (VCSFileProxy file : invalidMetadata) {
                SvnUtils.deleteRecursively(file);
            }
            invalidMetadata.clear();
        }
    }

    // private methods ---------------------------

    private boolean hasMetadata(VCSFileProxy file) {
        return VCSFileProxySupport.canRead(VCSFileProxy.createFileProxy(file, SvnUtils.SVN_ENTRIES_DIR));
    }

    private boolean isVersioned(VCSFileProxy file) {
        if (SvnUtils.isPartOfSubversionMetadata(file)) return false;
        if  (( !file.isFile() && hasMetadata(file) ) || ( file.isFile() && hasMetadata(file.getParentFile()) )) {
            return true;
        }
        try {
            SVNStatusKind statusKind = SvnUtils.getSingleStatus(Subversion.getInstance().getClient(false, new Context(file)), file).getTextStatus();
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
    private static List<VCSFileProxy> getDeletedParents(VCSFileProxy file, SvnClient client) throws SVNClientException {
        List<VCSFileProxy> ret = new ArrayList<>();
        for(VCSFileProxy parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {
            ISVNStatus status = getStatus(client, parent);
            if (status == null || !status.getTextStatus().equals(SVNStatusKind.DELETED)) {
                return ret;
            }
            ret.add(parent);
        }
        return ret;
    }

    private void revertDeleted(SvnClient client, final VCSFileProxy file, boolean checkParents) {
        try {
            ISVNStatus status = getStatus(client, file);
            revertDeleted(client, status, file, checkParents);
        } catch (SVNClientException ex) {
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                SvnClientExceptionHandler.notifyException(new Context(file), ex, false, false);
            }
        }
    }

    private void revertDeleted(SvnClient client, ISVNStatus status, final VCSFileProxy file, boolean checkParents) {
        try {
            if (FilesystemHandler.this.equals(status, SVNStatusKind.DELETED)) {
                if(checkParents) {
                    // we have a file scheduled for deletion but it's going to be created again,
                    // => it's parent folder can't stay deleted either
                    final List<VCSFileProxy> deletedParents = getDeletedParents(file, client);
                    // XXX JAVAHL client.revert(deletedParents.toArray(new File[deletedParents.size()]), false);
                    for (VCSFileProxy parent : deletedParents) {
                        client.revert(parent, false);
                    }
                    if (!deletedParents.isEmpty()) {
                        Subversion.getInstance().getStatusCache().refreshAsync(deletedParents.toArray(new VCSFileProxy[deletedParents.size()]));
                    }
                }

                // reverting the file will set the metadata uptodate
                client.revert(file, false);
                // our goal was ony to fix the metadata ->
                //  -> get rid of the reverted file
                internalyDeletedFiles.add(file); // prevents later removal in afterDelete if the file is recreated
                VCSFileProxySupport.delete(file);
            }
        } catch (SVNClientException ex) {
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                SvnClientExceptionHandler.notifyException(new Context(file), ex, false, false);
            }
        }
    }

    private void svnMoveImplementation(final VCSFileProxy from, final VCSFileProxy to) throws IOException {        
        try {
            boolean force = true; // file with local changes must be forced
            SvnClient client = Subversion.getInstance().getClient(false, new Context(from));

            // prepare destination, it must be under Subversion control
            removeInvalidMetadata();

            VCSFileProxy parent;
            if (to.isDirectory()) {
                parent = to;
            } else {
                parent = to.getParentFile();
            }

            boolean parentIgnored = false;
            if (parent != null) {
                assert SvnUtils.isManaged(parent) : "Cannot move " + from.getPath() + " to " + to.getPath() + ", " + parent.getPath() + " is not managed";  // NOI18N see implsMove above
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
                    List<VCSFileProxy> srcChildren = null;
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

                            VCSFileProxy temp = from;
                            if (VCSFileProxySupport.isMac(from) && from.getPath().equalsIgnoreCase(to.getPath())) {
                                if (Subversion.LOG.isLoggable(Level.FINE)) {
                                    Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround for filename case change {0} -> {1}", new Object[] { from, to }); //NOI18N
                                }
                                temp = VCSFileProxySupport.generateTemporaryFile(from.getParentFile(), from.getName());
                                if (Subversion.LOG.isLoggable(Level.FINE)) {
                                    Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround, step 1: {0} -> {1}", new Object[] { from, temp }); //NOI18N
                                }
                                client.move(from, temp, force);
                            }
                            
                            // check if the file wasn't just deleted in this session
                            revertDeleted(client, toStatus, to, true);

                            moved = VCSFileProxySupport.renameTo(temp, to);
                            if (moved) {
                                // indeed just ADDED, not REPLACED
                                if (status.getTextStatus().equals(SVNStatusKind.ADDED)) {
                                    client.revert(temp, true);
                                } else {
                                    client.remove(new VCSFileProxy[] { temp }, true);
                                }
                            }
                        } else if (status != null && (status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)
                                || status.getTextStatus().equals(SVNStatusKind.IGNORED))) { // ignored file CAN'T be moved via svn
                            // check if the file wasn't just deleted in this session
                            revertDeleted(client, toStatus, to, true);

                            moved = VCSFileProxySupport.renameTo(from, to);
                        } else if (parentIgnored) {
                            // parent is ignored so do not add the file
                            moved = VCSFileProxySupport.renameTo(from, to);
                            client.remove(new VCSFileProxy[] { from }, true);
                        } else {
                            SVNUrl repositorySource = SvnUtils.getRepositoryRootUrl(from);
                            SVNUrl repositoryTarget = SvnUtils.getRepositoryRootUrl(parent);
                            if (repositorySource.equals(repositoryTarget)) {
                                // use client.move only for a single repository
                                try {
                                    client.move(from, to, force);
                                } catch (SVNClientException ex) {
                                    if (VCSFileProxySupport.isMac(from) && from.getPath().equalsIgnoreCase(to.getPath())) {
                                        if (Subversion.LOG.isLoggable(Level.FINE)) {
                                            Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround for filename case change {0} -> {1}", new Object[] { from, to }); //NOI18N
                                        }
                                        VCSFileProxy temp = VCSFileProxySupport.generateTemporaryFile(to.getParentFile(), from.getName());
                                        if (Subversion.LOG.isLoggable(Level.FINE)) {
                                            Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround, step 1: {0} -> {1}", new Object[] { from, temp }); //NOI18N
                                        }
                                        client.move(from, temp, force);
                                        if (Subversion.LOG.isLoggable(Level.FINE)) {
                                            Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround, step 2: {0} -> {1}", new Object[] { temp, to }); //NOI18N
                                        }
                                        client.move(temp, to, force);
                                        if (Subversion.LOG.isLoggable(Level.FINE)) {
                                            Subversion.LOG.log(Level.FINE, "svnMoveImplementation: magic workaround completed"); //NOI18N
                                        }
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
                                } else if (VCSFileProxySupport.renameTo(from, to)) {
                                    remove = true;
                                } else {
                                    Subversion.LOG.log(Level.WARNING, FilesystemHandler.class.getName()
                                            + ": cannot rename {0} to {1}", new Object[] {from, to}); //NOI18N
                                }
                                if (remove) {
                                    client.remove(new VCSFileProxy[] {from}, force);
                                    if (Subversion.LOG.isLoggable(Level.FINE)) {
                                        Subversion.LOG.log(Level.FINE, FilesystemHandler.class.getName()
                                                + ": moving between different repositories {0} to {1}", new Object[] {from, to}); //NOI18N
                                    }
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
                        SvnClientExceptionHandler.notifyException(new Context(from), e, false, false); // log this
                    }
                    IOException ex = new IOException();
                    Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(FilesystemHandler.class, "MSG_MoveFailed", new Object[] {from, to, e.getLocalizedMessage()})); //NOI18N
                    ex.getCause().initCause(e);
                    throw ex;
                }
            }
        } catch (SVNClientException e) {
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                SvnClientExceptionHandler.notifyException(new Context(from), e, false, false); // log this
            }
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(ex, "Subversion failed to move " + from.getPath() + " to: " + to.getPath() + "\n" + e.getLocalizedMessage()); // NOI18N
            ex.getCause().initCause(e);
            throw ex;
        }
    }

    /**
     * Seeks versioned root and then adds all folders
     * under Subversion (so it contains metadata),
     */
    private boolean addDirectories(final VCSFileProxy dir) throws SVNClientException  {
        SvnClient client = Subversion.getInstance().getClient(false, new Context(dir));
        ISVNStatus s = getStatus(client, dir);
        if(s.getTextStatus().equals(SVNStatusKind.IGNORED)) {
            return false;
        }
        VCSFileProxy parent = dir.getParentFile();
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

    private static ISVNStatus getStatus(SvnClient client, VCSFileProxy file) throws SVNClientException {
        // a direct cache call could, because of the synchrone beforeCreate handling,
        // trigger an reentrant call on FS => we have to check manually
        return SvnUtils.getSingleStatus(client, file);
    }

    private boolean equals(ISVNStatus status, SVNStatusKind kind) {
        return status != null && status.getTextStatus().equals(kind);
    }

    private boolean copyFile(VCSFileProxy from, VCSFileProxy to) {
        try {
            VCSFileProxySupport.copyFile(from, to);
        } catch (IOException ex) {
            SvnClientExceptionHandler.notifyException(new Context(from), ex, false, false); // log this
            return false;
        }
        return true;
    }

    private void ensureLocked (final VCSFileProxy file) {
        if (toLockFiles.contains(file)) {
            Runnable outsideAWT = new Runnable () {
                @Override
                public void run () {
                    boolean readOnly = true;
                    try {
                        // unlock files that...
                        // ... have svn:needs-lock prop set
                        SvnClient client = Subversion.getInstance().getClient(false, new Context(file));
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
                                    client = Subversion.getInstance().getClient(new Context(file), url);
                                    if (status.getLockOwner() != null) {
                                        // the file is locked yet it's still read-only, it may be a result of:
                                        // 1. svn lock A
                                        // 2. svn move A B - B is new and read-only
                                        // 3. move B A - A is now also read-only
                                        client.unlock(new VCSFileProxy[] { file }, false); //NOI18N
                                    }
                                    client.lock(new VCSFileProxy[] { file }, "", false); //NOI18N
                                    readOnly = false;
                                }
                            }
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(new Context(file), ex, false, false);
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

    private SVNUrl getCopiedUrl (SvnClient client, VCSFileProxy f) {
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
