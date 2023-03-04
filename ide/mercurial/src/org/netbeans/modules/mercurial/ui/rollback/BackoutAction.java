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
package org.netbeans.modules.mercurial.ui.rollback;

import org.netbeans.modules.versioning.spi.VCSContext;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.RepositoryRevision;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;

/**
 * Pull action for mercurial: 
 * hg pull - pull changes from the specified source
 * 
 * @author John Rice
 */
public class BackoutAction extends ContextAction {
    
    private static final String HG_BACKOUT_REVISION_REPLACE = "\\{revision}"; //NOI18N
    static final String HG_BACKOUT_REVISION = "{revision}"; //NOI18N
    private static String HG_TIP = "tip"; // NOI18N
            
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Backout";                                  //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        backout(context);
    }
    
    public static void backout(final VCSContext ctx){
        backout(ctx, null);
    }
    public static void backout(final RepositoryRevision repoRev){
        backout(null, repoRev);
    }

    public static void backout(final VCSContext ctx, final RepositoryRevision repoRev){
        final File root;        
        if(repoRev != null){
            root = repoRev.getRepositoryRoot();
            if ((root == null) || (root.getPath().equals(""))) {        //NOI18N
                return;
            }
        }else{
            File roots[] = HgUtils.getActionRoots(ctx);
            if (roots == null || roots.length == 0) return;
            root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        }
        if (root == null) return;
         

        final Backout backout = new Backout(root, repoRev == null ? null : repoRev.getLog());
        if (!backout.showDialog()) {
            return;
        }
        final String rev = backout.getSelectionRevision();
        final String commitMsg = backout.getCommitMessage();
        final boolean doMerge = false; // Now handling this using our own merge mechanism, not backout's
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                String revStr = rev;
                if (revStr == null) {
                    try {
                        revStr = HgCommand.getParent(root, null, null).getChangesetId();
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                        return;
                    }
                }
                final String commitMsgStr = commitMsg.replaceAll(HG_BACKOUT_REVISION_REPLACE, revStr); //NOI18N
                if (revStr == null) {
                    try {
                        revStr = HgCommand.getParent(root, null, null).getChangesetId();
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                        return;
                    }
                }
                final OutputLogger logger = getLogger();
                try {
                    logger.outputInRed(
                                NbBundle.getMessage(BackoutAction.class,
                                "MSG_BACKOUT_TITLE")); // NOI18N
                    logger.outputInRed(
                                NbBundle.getMessage(BackoutAction.class,
                                "MSG_BACKOUT_TITLE_SEP")); // NOI18N
                    logger.output(
                                NbBundle.getMessage(BackoutAction.class,
                                "MSG_BACKOUT_INFO_SEP", revStr, root.getAbsolutePath())); // NOI18N
                    final String revision = revStr;
                    HgUtils.runWithoutIndexing(new Callable<Void>() {

                        @Override
                        public Void call () throws HgException {
                            List<String> list = HgCommand.doBackout(root, revision, doMerge, commitMsgStr, logger);
                            if(list != null && !list.isEmpty()){ 
                                boolean bMergeNeededDueToBackout = HgCommand.isBackoutMergeNeededMsg(list.get(list.size() - 1));
                                if(bMergeNeededDueToBackout){
                                    list.remove(list.size() - 1);
                                    list.remove(list.size() - 1);
                                }
                                logger.output(list);                            

                                if(HgCommand.isUncommittedChangesBackout(list.get(0))){
                                    logger.outputInRed(
                                            NbBundle.getMessage(BackoutAction.class,
                                            "MSG_UNCOMMITTED_CHANGES_BACKOUT"));     // NOI18N           
                                    return null;
                                } else if(HgCommand.isMergeChangesetBackout(list.get(0))){
                                    logger.outputInRed(
                                            NbBundle.getMessage(BackoutAction.class,
                                            "MSG_MERGE_CSET_BACKOUT", revision));     // NOI18N        
                                    return null;
                                } else if(HgCommand.isNoRevStrip(list.get(0))){
                                    logger.outputInRed(
                                            NbBundle.getMessage(BackoutAction.class,
                                            "MSG_NO_REV_BACKOUT", revision));     // NOI18N        
                                    return null;
                                }

                                // Handle Merge - both automatic and merge with conflicts
                                boolean bConfirmMerge = false;
                                boolean warnMoreHeads = true;
                                if (bMergeNeededDueToBackout) {
                                    bConfirmMerge = HgUtils.confirmDialog(
                                            BackoutAction.class, "MSG_BACKOUT_MERGE_CONFIRM_TITLE", "MSG_BACKOUT_MERGE_CONFIRM_QUERY"); // NOI18N
                                    warnMoreHeads = false;
                                }
                                if (bConfirmMerge) {
                                    logger.output(""); // NOI18N
                                    logger.outputInRed(NbBundle.getMessage(BackoutAction.class, "MSG_BACKOUT_MERGE_DO")); // NOI18N
                                    MergeAction.doMergeAction(root, null, logger);
                                } else {
                                    HgLogMessage[] heads = HgCommand.getHeadRevisionsInfo(root, false, OutputLogger.getLogger(null));
                                    Map<String, Collection<HgLogMessage>> branchHeads = HgUtils.sortByBranch(heads);
                                    if (!branchHeads.isEmpty()) {
                                        MergeAction.displayMergeWarning(branchHeads, logger, warnMoreHeads);
                                    }
                                }
                                if(ctx != null){
                                    HgUtils.forceStatusRefreshProject(ctx);
                                }else if(repoRev != null){
                                    HgUtils.forceStatusRefresh(root);
                                }
                            }
                            return null;
                        }

                    }, root);
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                } finally {
                    logger.outputInRed(
                                NbBundle.getMessage(BackoutAction.class,
                                "MSG_BACKOUT_DONE")); // NOI18N
                    logger.output(""); // NOI18N
                    Mercurial.getInstance().versionedFilesChanged();
                }
            }
        };
        support.start(rp, root, org.openide.util.NbBundle.getMessage(BackoutAction.class, "MSG_BACKOUT_PROGRESS")); // NOI18N
    }
}
