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

package org.netbeans.modules.mercurial.remote.ui.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.ui.repository.ChangesetPickerPanel;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Reverts local changes.
 *
 * 
 */
public class RevertModificationsAction extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/remote/resources/icons/get_clean.png"; //NOI18N
    
    public RevertModificationsAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<VCSFileProxy> ctxFiles = context != null? context.getRootFiles(): null;
        if(!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.isEmpty()) {
            return false;
        }
        Set<VCSFileProxy> roots = context.getRootFiles();
        if(roots == null) {
            return false;
        }
        for (VCSFileProxy root : roots) {
            FileInformation info = Mercurial.getInstance().getFileStatusCache().getCachedStatus(root);
            if(info != null && info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_GetClean";                                 //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        revert(context);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    public static void revert(final VCSContext ctx) {
        final VCSFileProxy files[] = HgUtils.getActionRoots(ctx);
        if (files == null || files.length == 0) {
            return;
        }
        final VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(files[0]);


        final RevertModifications revertModifications = new RevertModifications(repository, Arrays.asList(files).contains(repository) ? null : files); // this is much faster when getting revisions
        if (!revertModifications.showDialog()) {
            return;
        }
        final String revStr = revertModifications.getSelectionRevision();
        final boolean doBackup = revertModifications.isBackupRequested();
        final boolean removeNewFiles = revertModifications.isRemoveNewFilesRequested();
        HgModuleConfig.getDefault(repository).setRemoveNewFilesOnRevertModifications(removeNewFiles);
        HgModuleConfig.getDefault(repository).setBackupOnRevertModifications(doBackup);

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                performRevert(repository, revStr, files, doBackup, removeNewFiles, this.getLogger());
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Revert_Progress")); // NOI18N
    }

    public static void performRevert(VCSFileProxy repository, String revStr, VCSFileProxy file, boolean doBackup, OutputLogger logger) {
        List<VCSFileProxy> revertFiles = new ArrayList<>();
        revertFiles.add(file);        

        performRevert(repository, revStr, revertFiles, doBackup, false, logger);
    }
    
    public static void performRevert(VCSFileProxy repository, String revStr, VCSFileProxy[] files, boolean doBackup, boolean removeNewFiles, OutputLogger logger) {
        List<VCSFileProxy> revertFiles = new ArrayList<>();
        revertFiles.addAll(Arrays.asList(files));
        performRevert(repository, revStr, revertFiles, doBackup, removeNewFiles, logger);
    }
    
    public static void performRevert(final VCSFileProxy repository, final String revStr, final List<VCSFileProxy> revertFiles, final boolean doBackup, final boolean removeNewFiles, final OutputLogger logger) {
        try{
            logger.outputInRed(
                    NbBundle.getMessage(RevertModificationsAction.class,
                    "MSG_REVERT_TITLE")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(RevertModificationsAction.class,
                    "MSG_REVERT_TITLE_SEP")); // NOI18N
            
            // revStr == null => no -r REV in hg revert command
            // No revisions to revert too
            if (revStr != null && NbBundle.getMessage(ChangesetPickerPanel.class,
                    "MSG_Revision_Default").startsWith(revStr)) { //NOI18N
                logger.output(
                        NbBundle.getMessage(RevertModificationsAction.class,
                        "MSG_REVERT_NOTHING")); // NOI18N
                logger.outputInRed(
                        NbBundle.getMessage(RevertModificationsAction.class,
                        "MSG_REVERT_DONE")); // NOI18N
                logger.outputInRed(""); // NOI18N
                return;
            }

            // revision with no events - e.g. automatic merge
            if (revertFiles.isEmpty()) {
                logger.outputInRed(
                    NbBundle.getMessage(RevertModificationsAction.class,
                    "MSG_REVERT_NOFILES")); // NOI18N
                logger.outputInRed(
                        NbBundle.getMessage(RevertModificationsAction.class,
                        "MSG_REVERT_DONE")); // NOI18N
                logger.outputInRed(""); // NOI18N
                return;
            }

            logger.output(revStr == null ?
                    NbBundle.getMessage(RevertModificationsAction.class, "MSG_REVERT_REVISION_PARENT") :
                    NbBundle.getMessage(RevertModificationsAction.class, "MSG_REVERT_REVISION_STR", revStr)); // NOI18N
            for (VCSFileProxy file : revertFiles) {
                logger.output(file.getPath());
            }
            logger.output(""); // NOI18N

            HgUtils.runWithoutIndexing(new Callable<Void>() {

                @Override
                public Void call () throws HgException {
                    HgCommand.doRevert(repository, revertFiles, revStr, doBackup, logger);
                    if (removeNewFiles) {
                        // must exclude nonsharable files/folders purge deletes them because they appear new to hg
                        HgCommand.doPurge(repository, revertFiles, HgUtils.getNotSharablePaths(repository, revertFiles), logger);
                    }
                    FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
                    VCSFileProxy[] conflictFiles = cache.listFiles(revertFiles.toArray(new VCSFileProxy[0]), FileInformation.STATUS_VERSIONED_CONFLICT);
                    if (conflictFiles.length != 0) {
                        ConflictResolvedAction.conflictResolved(repository, conflictFiles);
                    }
                    return null;
                }
                
            }, revertFiles);
        } catch (HgException.HgCommandCanceledException ex) {
            // canceled by user, do nothing
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        }

        Mercurial.getInstance().getFileStatusCache().refreshAllRoots(Collections.singletonMap(repository, (Set<VCSFileProxy>)new HashSet<>(revertFiles)));

        logger.outputInRed(
                NbBundle.getMessage(RevertModificationsAction.class,
                "MSG_REVERT_DONE")); // NOI18N
        logger.outputInRed(""); // NOI18N
 
    }
}
