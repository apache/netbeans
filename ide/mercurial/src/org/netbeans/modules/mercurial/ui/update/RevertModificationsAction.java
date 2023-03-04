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

package org.netbeans.modules.mercurial.ui.update;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.nodes.Node;

/**
 * Reverts local changes.
 *
 * @author Padraig O'Briain
 */
public class RevertModificationsAction extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/get_clean.png"; //NOI18N
    
    public RevertModificationsAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if(!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.isEmpty()) {
            return false;
        }
        Set<File> roots = context.getRootFiles();
        if(roots == null) return false;
        for (File root : roots) {
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
        final File files[] = HgUtils.getActionRoots(ctx);
        if (files == null || files.length == 0) return;
        final File repository = Mercurial.getInstance().getRepositoryRoot(files[0]);


        final RevertModifications revertModifications = new RevertModifications(repository, Arrays.asList(files).contains(repository) ? null : files); // this is much faster when getting revisions
        if (!revertModifications.showDialog()) {
            return;
        }
        final String revStr = revertModifications.getSelectionRevision();
        final boolean doBackup = revertModifications.isBackupRequested();
        final boolean removeNewFiles = revertModifications.isRemoveNewFilesRequested();
        HgModuleConfig.getDefault().setRemoveNewFilesOnRevertModifications(removeNewFiles);
        HgModuleConfig.getDefault().setBackupOnRevertModifications(doBackup);

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                performRevert(repository, revStr, files, doBackup, removeNewFiles, this.getLogger());
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Revert_Progress")); // NOI18N
    }

    public static void performRevert(File repository, String revStr, File file, boolean doBackup, OutputLogger logger) {
        List<File> revertFiles = new ArrayList<File>();
        revertFiles.add(file);        

        performRevert(repository, revStr, revertFiles, doBackup, false, logger);
    }
    
    public static void performRevert(File repository, String revStr, File[] files, boolean doBackup, boolean removeNewFiles, OutputLogger logger) {
        List<File> revertFiles = new ArrayList<File>();
        revertFiles.addAll(Arrays.asList(files));
        performRevert(repository, revStr, revertFiles, doBackup, removeNewFiles, logger);
    }
    
    public static void performRevert(final File repository, final String revStr, final List<File> revertFiles, final boolean doBackup, final boolean removeNewFiles, final OutputLogger logger) {
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
                    "MSG_Revision_Default").startsWith(revStr)) {
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
            for (File file : revertFiles) {
                logger.output(file.getAbsolutePath());
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
                    File[] conflictFiles = cache.listFiles(revertFiles.toArray(new File[0]), FileInformation.STATUS_VERSIONED_CONFLICT);
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

        Mercurial.getInstance().getFileStatusCache().refreshAllRoots(Collections.singletonMap(repository, (Set<File>)new HashSet<File>(revertFiles)));

        logger.outputInRed(
                NbBundle.getMessage(RevertModificationsAction.class,
                "MSG_REVERT_DONE")); // NOI18N
        logger.outputInRed(""); // NOI18N
 
    }
}
