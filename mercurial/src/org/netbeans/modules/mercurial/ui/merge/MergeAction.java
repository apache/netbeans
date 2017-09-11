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
package org.netbeans.modules.mercurial.ui.merge;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.openide.DialogDescriptor;
import org.openide.nodes.Node;

/**
 * Merge action for mercurial:
 * hg merge - attempts to merge changes when the repository has 2 heads
 *
 * @author John Rice
 */
@NbBundle.Messages({
    "CTL_MenuItem_Merge=&Merge Changes"
})
public class MergeAction extends ContextAction {

    private static final Logger LOG = Logger.getLogger(MergeAction.class.getName());

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if(!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.isEmpty())
            return false;
        return true; // #121293: Speed up menu display, warn user if nothing to merge when Merge selected
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Merge";                                    //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        File roots[] = HgUtils.getActionRoots(context);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        if (root == null) {
            OutputLogger logger = Mercurial.getInstance().getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            logger.outputInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE")); // NOI18N
            logger.outputInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE_SEP")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(MergeAction.class, "MSG_MERGE_NOT_SUPPORTED_INVIEW_INFO")); // NOI18N
            logger.output(""); // NOI18N
            logger.closeLog();
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(MergeAction.class, "MSG_MERGE_NOT_SUPPORTED_INVIEW"),// NOI18N
                    NbBundle.getMessage(MergeAction.class, "MSG_MERGE_NOT_SUPPORTED_INVIEW_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                final OutputLogger logger = getLogger();
                try {
                    List<String> headList = HgCommand.getHeadRevisions(root);
                    if (headList.size() <= 1) {
                        logger.outputInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE")); // NOI18N
                        logger.outputInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE_SEP")); // NOI18N
                        logger.output( NbBundle.getMessage(MergeAction.class,"MSG_NOTHING_TO_MERGE")); // NOI18N
                        logger.outputInRed( NbBundle.getMessage(MergeAction.class, "MSG_MERGE_DONE")); // NOI18N
                        logger.output(""); // NOI18N
                        JOptionPane.showMessageDialog(null,
                            NbBundle.getMessage(MergeAction.class,"MSG_NOTHING_TO_MERGE"),// NOI18N
                            NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE"),// NOI18N
                            JOptionPane.INFORMATION_MESSAGE);
                         return;
                    }
                    String revStr = null;
                    MergeRevisions mergeDlg = new MergeRevisions(root, null);
                    if (!mergeDlg.showDialog()) {
                        return;
                    }
                    revStr = mergeDlg.getSelectionRevision();
                    logger.outputInRed(
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE")); // NOI18N
                    logger.outputInRed(
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE_SEP")); // NOI18N
                    doMergeAction(root, revStr, logger);
                    HgUtils.forceStatusRefreshProject(context);
                    logger.output(""); // NOI18N
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                } finally {
                    logger.closeLog();
                }
            }
        };
        support.start(rp, root, NbBundle.getMessage(MergeAction.class, "MSG_MERGE_PROGRESS")); // NOI18N
    }

    public static List<String> doMergeAction(final File root, final String revStr, OutputLogger logger) throws HgException {
        List<String> listMerge = HgUtils.runWithoutIndexing(new Callable<List<String>>() {
            @Override
            public List<String> call () throws Exception {
                return HgCommand.doMerge(root, revStr);
            }
        }, root);
        
        if (listMerge != null && !listMerge.isEmpty()) {
            logger.output(listMerge);
            if (handleMergeOutput(root, listMerge, logger, true)) {
                logger.outputInRed(NbBundle.getMessage(MergeAction.class, "MSG_MERGE_DONE")); //NOI18N
            }
        }
        return listMerge;
    }

    public static boolean handleMergeOutput(File root, List<String> listMerge, OutputLogger logger, boolean interactive) {
        if (listMerge == null || listMerge.isEmpty()) return true;

        Boolean bConflicts = false;
        Boolean bMergeFailed = false;
        
        for (String line : listMerge) {
            if (HgCommand.isMergeAbortUncommittedMsg(line)) {
                bMergeFailed = true;
                if (interactive) {
                    logger.outputInRed(NbBundle.getMessage(MergeAction.class,
                            "MSG_MERGE_FAILED")); // NOI18N
                    JOptionPane.showMessageDialog(null,
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_UNCOMMITTED"), // NOI18N
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                }
                break;
            }

            if (HgCommand.isMergeAbortMultipleHeadsMsg(line)) {
                bMergeFailed = true;
                if (interactive) {
                    logger.outputInRed(NbBundle.getMessage(MergeAction.class, "MSG_MERGE_FAILED")); // NOI18N
                }
                break;
            }
            String filepath = null;
            if (HgCommand.isMergeFailedMsg(line)) {
                bConflicts = true;
                if (line.contains(HgCommand.HG_MERGE_FAILED2_ERR)) {
                    filepath = line.substring(HgCommand.HG_MERGE_FAILED1_ERR.length(), line.length() - HgCommand.HG_MERGE_FAILED2_ERR.length()).trim().replace("/", File.separator); // NOI18N
                } else if (line.contains(HgCommand.HG_MERGE_FAILED3_ERR)) {
                    filepath = line.substring(HgCommand.HG_MERGE_FAILED1_ERR.length(), line.lastIndexOf(HgCommand.HG_MERGE_FAILED3_ERR)).trim().replace("/", File.separator); // NOI18N
                }
                filepath = root.getAbsolutePath() + File.separator + filepath;
            } else if (HgCommand.isConflictDetectedInMsg(line)) {
                bConflicts = true;
                filepath = line.substring(HgCommand.HG_MERGE_CONFLICT_ERR.length());
            }
            if (filepath != null) {
                LOG.log(Level.FINER, "File {0} in conflict", filepath);
                if (interactive) {
                    logger.outputInRed(NbBundle.getMessage(MergeAction.class, "MSG_MERGE_CONFLICT", filepath)); // NOI18N
                }
            }

            
            if (HgCommand.isMergeUnavailableMsg(line)) {
                bMergeFailed = true;
                if (interactive) {
                    JOptionPane.showMessageDialog(null,
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_UNAVAILABLE"), // NOI18N
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                    logger.outputInRed(
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_INFO"));// NOI18N            
                    logger.outputLink(
                            NbBundle.getMessage(MergeAction.class, "MSG_MERGE_INFO_URL")); // NOI18N 
                }
            }
        }

        if (bConflicts) {
            LOG.log(Level.FINER, "Conflicts detected: {0}", root);
            if (interactive) {
                logger.outputInRed(NbBundle.getMessage(MergeAction.class, "MSG_MERGE_DONE_CONFLICTS")); // NOI18N
                DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(NbBundle.getMessage(MergeAction.class, "MSG_Merge.ConflictsCreated"))); //NOI18N
            }
        }
        return !bMergeFailed && !bConflicts;
    }

    public static void printMergeWarning(List<String> list, OutputLogger logger){
        if(list == null || list.isEmpty() || list.size() <= 1) return;
        
        if (list.size() == 2) {
            logger.outputInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_NEEDED", list)); // NOI18N
            logger.outputInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DO_NEEDED")); // NOI18N
        } else {
            logger.outputInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_MULTIPLE_HEADS", list.size(), list)); // NOI18N
            logger.outputInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DONE_MULTIPLE_HEADS")); // NOI18N
        }
    }

    public static void displayMergeWarning (Map<String, Collection<HgLogMessage>> branchHeads, OutputLogger logger, boolean warnInDialog) {
        boolean mulitpleHeads = false;
        for (Map.Entry<String, Collection<HgLogMessage>> e : branchHeads.entrySet()) {
            if (e.getValue().size() > 1) {
                mulitpleHeads = true;
                break;
            }
        }
        if (!mulitpleHeads) {
            return;
        }
        Action a = logger.getOpenOutputAction();
        if (warnInDialog && a != null && JOptionPane.showConfirmDialog(null, NbBundle.getMessage(MergeAction.class, "MSG_MERGE_NEEDED_BRANCHES"), //NOI18N
                NbBundle.getMessage(MergeAction.class, "TITLE_MERGE_NEEDED_BRANCHES"), //NOI18N
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            a.actionPerformed(new ActionEvent(MergeAction.class, ActionEvent.ACTION_PERFORMED, null));
        }
        logger.outputInRed(NbBundle.getMessage(MergeAction.class, "MSG_MERGE_WARN_NEEDED_BRANCHES")); //NOI18N
        logger.outputInRed(NbBundle.getMessage(MergeAction.class, "MSG_MERGE_DO_NEEDED_BRANCHES")); //NOI18N
        for (Map.Entry<String, Collection<HgLogMessage>> e : branchHeads.entrySet()) {
            Collection<HgLogMessage> heads = e.getValue();
            if (heads.size() > 1) {
                logger.outputInRed(NbBundle.getMessage(MergeAction.class, "MSG_MERGE_WARN_NEEDED_IN_BRANCH", e.getKey())); //NOI18N
                for (HgLogMessage head : heads) {
                    HgUtils.logHgLog(head, logger);
                }
            }
        }
    }
}
