/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
