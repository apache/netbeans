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
