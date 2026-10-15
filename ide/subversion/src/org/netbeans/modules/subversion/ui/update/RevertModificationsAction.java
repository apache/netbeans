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

package org.netbeans.modules.subversion.ui.update;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Reverts local changes.
 *
 * @author Petr Kuzel
 */
public class RevertModificationsAction extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/resources/icons/get_clean.png"; //NOI18N
    
    public RevertModificationsAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Revert"; // NOI18N
    }
    
    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_VERSIONED & ~FileInformation.STATUS_VERSIONED_NEWINREPOSITORY;
    }
    
    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_VERSIONED & ~FileInformation.STATUS_VERSIONED_NEWINREPOSITORY;
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected void performContextAction(final Node[] nodes) {
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        final Context ctx = getContext(nodes);
        File[] roots = ctx.getRootFiles();
        // filter managed roots
        List<File> l = new ArrayList<>();
        for (File file : roots) {
            if(SvnUtils.isManaged(file)) {
                l.add(file);
            }
        }
        roots = l.toArray(new File[0]);

        if(roots == null || roots.length == 0) return;

        File interestingFile;
        if(roots.length == 1) {
            interestingFile = roots[0];
        } else {
            interestingFile = SvnUtils.getPrimaryFile(roots[0]);
        }

        final SVNUrl rootUrl;
        final SVNUrl url;
        
        try {
            rootUrl = SvnUtils.getRepositoryRootUrl(interestingFile);
            url = SvnUtils.getRepositoryUrl(interestingFile);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        final RepositoryFile repositoryFile = new RepositoryFile(rootUrl, url, SVNRevision.HEAD);
        
        final RevertModifications revertModifications = new RevertModifications(repositoryFile);
        if(!revertModifications.showDialog()) {
            return;
        }
        
        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes, ctx) {
            @Override
            public void perform() {
                performRevert(revertModifications.getRevisionInterval(), revertModifications.revertNewFiles(), !revertModifications.revertRecursively(), ctx, this);
            }
        };
        support.start(createRequestProcessor(ctx));
    }
    
    /**
     * Reverts given files
     * @param revisions
     * @param revertNewFiles
     * @param onlySelectedFiles if set to false then the revert will act recursively, otherwise only selected roots will be reverted (without any of their children)
     * @param ctx
     * @param support 
     */
    public static void performRevert(final RevertModifications.RevisionInterval revisions, boolean revertNewFiles, final boolean onlySelectedFiles, final Context ctx, final SvnProgressSupport support) {
        final SvnClient client;
        try {
            client = Subversion.getInstance().getClient(ctx, support);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        
        File files[] = ctx.getFiles();
        final File[][] split;
        if (onlySelectedFiles) {
            split = new File[2][0];
        } else {
            split = Utils.splitFlatOthers(files);
        }
        try {
            SvnUtils.runWithoutIndexing(new Callable<Void>() {

                @Override
                public Void call () throws Exception {
                    for (int c = 0; c<split.length; c++) {
                        if(support.isCanceled()) {
                            return null;
                        }
                        File[] files = split[c];
                        boolean recursive = c == 1;
                        if (!recursive && revisions == null) {
                            // not recursively
                            if (onlySelectedFiles) {
                                // ONLY the selected files, no children
                                files = ctx.getFiles();
                            } else {
                                // get selected files and it's direct descendants for flat folders
                                files = SvnUtils.flatten(files, FileInformation.STATUS_REVERTIBLE_CHANGE);
                            }
                        }

                        try {
                            if(revisions != null) {
                                for (int i= 0; i < files.length; i++) {
                                    if(support.isCanceled()) {
                                        return null;
                                    }
                                    SVNUrl url = SvnUtils.getRepositoryUrl(files[i]);
                                    RevertModifications.RevisionInterval targetInterval = recountStartRevision(client, url, revisions);
                                    if(files[i].exists()) {
                                        client.merge(url, targetInterval.endRevision,
                                                     url, targetInterval.startRevision,
                                                     files[i], false, recursive);
                                    } else {
                                        assert targetInterval.startRevision instanceof SVNRevision.Number
                                               : "The revision has to be a Number when trying to undelete file!";
                                        client.copy(url, files[i], targetInterval.startRevision);
                                    }
                                }
                            } else {
                                if(support.isCanceled()) {
                                    return null;
                                }
                                if(files.length > 0 ) {                        
                                    // check for deleted files, we also want to undelete their parents
                                    Set<File> deletedFiles = new HashSet<>();
                                    for(File file : files) {
                                        deletedFiles.addAll(getDeletedParents(file));
                                    }
                                    
                                    handleCopiedFiles(client, files, recursive);

                                    // XXX JAVAHL client.revert(files, recursive);
                                    for (File file : files) {
                                        client.revert(file, recursive);
                                    }

                                    // revert also deleted parent folders
                                    // for all undeleted files
                                    if(deletedFiles.size() > 0) {
                                        // XXX JAVAHL client.revert(deletedFiles.toArray(new File[deletedFiles.size()]), false);
                                        for (File file : deletedFiles) {
                                            client.revert(file, false);
                                        }    
                                    }
                                }
                            }
                        } catch (SVNClientException ex) {
                            support.annotate(ex);
                        }
                    }
                    return null;
                }

                private void handleCopiedFiles (SvnClient client, File[] files, boolean recursively) {
                    FileStatusCache cache = Subversion.getInstance().getStatusCache();
                    if (recursively) {
                        files = cache.listFiles(files, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
                    }
                    for (File f : files) {
                        FileInformation fi = cache.getStatus(f);
                        if (fi.getStatus() == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) {
                            ISVNStatus entry = fi.getEntry(f);
                            if (entry != null && entry.isCopied()) {
                                // file exists but it's status is set to deleted
                                File temporary = FileUtils.generateTemporaryFile(f.getParentFile(), f.getName());
                                try {
                                    if (f.renameTo(temporary)) {
                                        client.remove(new File[] { f }, true);
                                    } else {
                                        Subversion.LOG.log(Level.WARNING, "RevertModifications.handleCopiedFiles: cannot rename {0} to {1}", new Object[] { f, temporary }); //NOI18N
                                    }
                                } catch (SVNClientException ex) {
                                    Subversion.LOG.log(Level.INFO, null, ex);
                                } finally {
                                    if (temporary.exists()) {
                                        try {
                                            if (!temporary.renameTo(f)) {
                                                FileUtils.copyFile(temporary, f);
                                            }
                                        } catch (IOException ex) {
                                            Subversion.LOG.log(Level.INFO, "RevertModifications.handleCopiedFiles: cannot copy {0} back to {1}", new Object[] { temporary, f }); //NOI18N
                                        } finally {
                                            temporary.delete();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
            }, files);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, false);
        }
        
        if(support.isCanceled()) {
            return;
        }
        
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (File file : cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            FileInformation fi;
            if (file.isDirectory() 
                    || (fi = cache.getCachedStatus(file)) != null && (fi.getStatus() & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) != 0) { // added files turned to not versioned
                cache.refresh(file, null);
            }
        }
        
        if(support.isCanceled()) {
            return;
        }

        if(revertNewFiles) {
            File[] newfiles = Subversion.getInstance().getStatusCache().listFiles(ctx.getRootFiles(), FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
            for (File file : newfiles) {
                // do not act recursively if not allowed
                if (!onlySelectedFiles || ctx.getRoots().contains(file)) {
                    FileObject fo = FileUtil.toFileObject(file);
                    try {
                        if(fo != null) {
                            fo.delete();
                        }
                    } catch (IOException ex) {
                        Subversion.LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }     

    private static List<File> getDeletedParents(File file) {
        List<File> ret = new ArrayList<>();
        for(File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {        
            FileInformation info = Subversion.getInstance().getStatusCache().getStatus(parent);
            if( !((info.getStatus() & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) != 0 ||
                  (info.getStatus() & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) != 0) )  
            {
                return ret;
            }
            ret.add(parent);                                
        }        
        return ret;
    }
    
    private static RevertModifications.RevisionInterval recountStartRevision(SvnClient client, SVNUrl repository, RevertModifications.RevisionInterval ret) throws SVNClientException {
        SVNRevision currStartRevision = ret.startRevision;
        SVNRevision currEndRevision = ret.endRevision;

        if(currStartRevision.equals(SVNRevision.HEAD)) {
            ISVNInfo info = client.getInfo(repository);
            currStartRevision = info.getRevision();
        }

        long currStartRevNum = Long.parseLong(currStartRevision.toString());
        long newStartRevNum = (currStartRevNum > 0) ? currStartRevNum - 1
                                                    : currStartRevNum;

        return new RevertModifications.RevisionInterval(
                                         new SVNRevision.Number(newStartRevNum),
                                         currEndRevision);
    }

}
