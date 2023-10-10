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
package org.netbeans.modules.mercurial.ui.pull;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ListIterator;
import java.net.URISyntaxException;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.commit.CommitAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.queues.QGoToPatchAction;
import org.netbeans.modules.mercurial.ui.queues.QPatch;
import org.netbeans.modules.mercurial.ui.rebase.RebaseAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.DialogDescriptor;
import org.openide.nodes.Node;
import static org.netbeans.modules.mercurial.util.HgUtils.isNullOrEmpty;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Pull action for mercurial:
 * hg pull - pull changes from the specified source
 *
 * @author John Rice
 */
@NbBundle.Messages({
    "CTL_MenuItem_PullLocal=P&ull All Branches",
    "# {0} - repository folder name",
    "CTL_MenuItem_PullRoot=P&ull All Branches - {0}"
})
@ActionID(id = "org.netbeans.modules.mercurial.ui.pull.PullAction", category = "Mercurial")
@ActionRegistration(lazy = false, displayName = "#CTL_MenuItem_PullLocal")
public class PullAction extends ContextAction {
    private static final String CHANGESET_FILES_PREFIX = "files:"; //NOI18N

    private static void logCommand (String fromPrjName, OutputLogger logger, HgURL pullSource, String toPrjName, File root) throws MissingResourceException {
        if (fromPrjName != null) {
            logger.outputInRed(NbBundle.getMessage(
                    PullAction.class, "MSG_PULL_FROM", fromPrjName, HgUtils.stripDoubleSlash(pullSource.toString()))); // NOI18N
        } else {
            logger.outputInRed(NbBundle.getMessage(
                    PullAction.class, "MSG_PULL_FROM_NONAME", HgUtils.stripDoubleSlash(pullSource.toString()))); // NOI18N
        }
        if (toPrjName != null) {
            logger.outputInRed(NbBundle.getMessage(
                    PullAction.class, "MSG_PULL_TO", toPrjName, root)); // NOI18N
        } else {
            logger.outputInRed(NbBundle.getMessage(
                    PullAction.class, "MSG_PULL_TO_NONAME", root)); // NOI18N
        }
    }
    
    public enum PullType {

        LOCAL, OTHER
    }
    {
    }

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_PullLocal";                                //NOI18N
    }

    @Override
    public String getName(String role, Node[] activatedNodes) {
        VCSContext ctx = HgUtils.getCurrentContext(activatedNodes);
        Set<File> roots = HgUtils.getRepositoryRoots(ctx);
        String name = roots.size() == 1 ? "CTL_MenuItem_PullRoot" : "CTL_MenuItem_PullLocal"; //NOI18N
        return roots.size() == 1 ? NbBundle.getMessage(PullAction.class, name, roots.iterator().next().getName()) : NbBundle.getMessage(PullAction.class, name);
    }
    
    @Override
    protected void performContextAction(Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        final Set<File> repositoryRoots = HgUtils.getRepositoryRoots(context);
        // run the whole bulk operation in background
        Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                for (File repositoryRoot : repositoryRoots) {
                    final File repository = repositoryRoot;
                    // run every repository fetch in its own support with its own output window
                    RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
                    HgProgressSupport support = new HgProgressSupport() {
                        @Override
                        public void perform() {
                            getDefaultAndPerformPull(repository, null, null, this);
                        }
                    };
                    support.start(rp, repository, org.openide.util.NbBundle.getMessage(PullAction.class, "MSG_PULL_PROGRESS")).waitFinished(); //NOI18N
                    if (support.isCanceled()) {
                        break;
                    }
                }
            }
        });
    }

    public static boolean confirmWithLocalChanges(File rootFile, Class bundleLocation, String title, String query, 
        List<String> listIncoming, OutputLogger logger) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] roots = HgUtils.splitIntoSeenRoots(rootFile);
        File[] localModNewFiles = cache.listFiles(roots, 
                FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | 
                FileInformation.STATUS_VERSIONED_CONFLICT | 
                FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        List<String> listIncomingAndLocalMod = new ArrayList<String>();
        Set<String> setFiles = new HashSet<String>();
        String filesStr;
        String[] aFileStr;
        String root = rootFile.getAbsolutePath();
        
        for(String s: listIncoming){
            if(s.indexOf(CHANGESET_FILES_PREFIX) == 0){
                filesStr = (s.substring(CHANGESET_FILES_PREFIX.length())).trim();
                aFileStr = filesStr.split(" ");
                for(String fileStr: aFileStr){
                    setFiles.add(root + File.separator + fileStr);
                    break;
                }
            }
        }
        for(File f : localModNewFiles){
            for(String s : setFiles){
                if( s.equals(f.getAbsolutePath())){
                    listIncomingAndLocalMod.add(s);
                }
            }
        }

        if (listIncomingAndLocalMod.size() > 0) {
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_OVERWRITE_LOCAL")); // NOI18N
            logger.output(listIncomingAndLocalMod);
            int response = JOptionPane.showOptionDialog(Utilities.findDialogParent(), 
                    NbBundle.getMessage(bundleLocation, query), NbBundle.getMessage(bundleLocation, title), 
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

            if (response == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        return true;
    }


    static void annotateChangeSets(List<String> list, Class bundleLocation, String title, OutputLogger logger) {
        logger.outputInRed(NbBundle.getMessage(bundleLocation, title));
        for (String s : list) {
            if (s.indexOf(Mercurial.CHANGESET_STR) == 0) {
                logger.outputInRed(s);
            } else if (!s.equals("")) {
                logger.output(s);
            }
        }
        logger.output("");
    }

    public static void getDefaultAndPerformPull(File root, String revision, 
            String branch, HgProgressSupport supp) {
        OutputLogger logger = supp.getLogger();
        final String pullSourceString = HgRepositoryContextCache.getInstance().getPullDefault(root);
        // If the repository has no default pull path then inform user
        if (isNullOrEmpty(pullSourceString)) {
            notifyDefaultPullUrlNotSpecified(logger);
            return;
        }

        HgURL pullSource;
        try {
            pullSource = new HgURL(pullSourceString);
        } catch (URISyntaxException ex) {
            File sourceRoot = new File(root, pullSourceString);
            if (sourceRoot.isDirectory()) {
                pullSource = new HgURL(FileUtil.normalizeFile(sourceRoot));
            } else {
                notifyDefaultPullUrlInvalid(pullSourceString, ex.getReason(), logger);
                return;
            }
        }

        // We assume that if fromPrjName is null that it is a remote pull.
        // This is not true as a project which is in a subdirectory of a
        // repository will report a project name of null. This does no harm.
        String fromPrjName;
        PullType pullType;

        if (pullSource.isFile()) {
            fromPrjName = HgProjectUtils.getProjectName(new File(pullSource.getPath()));
            pullType = (fromPrjName != null) ? PullType.LOCAL
                                             : PullType.OTHER;
        } else {
            fromPrjName = null;
            pullType = PullType.OTHER;
        }
        final String toPrjName = HgProjectUtils.getProjectName(root);
        performPull(pullType, root, pullSource, fromPrjName, toPrjName, 
                revision, branch, supp);
    }

    private static void notifyDefaultPullUrlNotSpecified(OutputLogger logger) {
        String title = getMessage("MSG_PULL_TITLE");                    //NOI18N

        logger.outputInRed(title);
        logger.outputInRed(getMessage("MSG_PULL_TITLE_SEP"));           //NOI18N
        logger.output     (getMessage("MSG_NO_DEFAULT_PULL_SET_MSG"));  //NOI18N
        logger.outputInRed(getMessage("MSG_PULL_DONE"));                //NOI18N
        logger.output     ("");                                         //NOI18N
        DialogDisplayer.getDefault().notify(
                new DialogDescriptor.Message(
                        getMessage("MSG_NO_DEFAULT_PULL_SET")));        //NOI18N
    }

    private static void notifyDefaultPullUrlInvalid(String pullUrl,
                                                    String reason,
                                                    OutputLogger logger) {
        String title = getMessage("MSG_PULL_TITLE");                    //NOI18N
        String msg = getMessage("MSG_DEFAULT_PULL_INVALID", pullUrl);   //NOI18N

        logger.outputInRed(title);
        logger.outputInRed(getMessage("MSG_PULL_TITLE_SEP"));           //NOI18N
        logger.output     (msg);
        logger.outputInRed(getMessage("MSG_PULL_DONE"));                //NOI18N
        logger.output     ("");                                         //NOI18N
        DialogDisplayer.getDefault().notify(
                new DialogDescriptor.Message(msg));
    }

    /**
     *
     * @param type
     * @param root
     * @param pullSource password is nulled
     * @param fromPrjName
     * @param toPrjName
     * @param logger
     */
    @NbBundle.Messages({
        "MSG_PullAction.progress.incoming=Checking incoming changesets",
        "# Capitalized letters used intentionally to emphasize the words in an output window, should be translated",
        "MSG_PULL_LOCALMODS_CANCEL=INFO Pull cancelled by user as there are conflict/locally modified/new file(s)."
    })
    static void performPull(final PullType type, final File root, 
    final HgURL pullSource, final String fromPrjName, final String toPrjName, 
    final String revision, final String branch, final HgProgressSupport supp) {
        if(root == null || pullSource == null) return;
        File bundleFile = null; 
        final OutputLogger logger = supp.getLogger();
        
        try {
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_TITLE")); // NOI18N
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_TITLE_SEP")); // NOI18N

            logCommand(fromPrjName, logger, pullSource, toPrjName, root);
            final List<String> listIncoming;
            supp.setDisplayName(Bundle.MSG_PullAction_progress_incoming());
            if(type == PullType.LOCAL){
                listIncoming = HgCommand.doIncoming(root, revision, branch, logger);
            }else{
                for (int i = 0; i < 10000; i++) {
                    if (!new File(root.getParentFile(), root.getName() + "_bundle" + i).exists()) { // NOI18N
                        bundleFile = new File(root.getParentFile(), root.getName() + "_bundle" + i); // NOI18N
                        break;
                    }
                }
                listIncoming = HgCommand.doIncoming(root, pullSource, revision, branch, bundleFile, logger, false);
            }
            if (listIncoming == null || listIncoming.isEmpty()) return;
            
            boolean bNoChanges = false;
            for (ListIterator<String> it = listIncoming.listIterator(listIncoming.size()); it.hasPrevious(); ) {
                bNoChanges = HgCommand.isNoChanges(it.previous());
                if (bNoChanges) {
                    break;
                }
            }

            Boolean localChangesConfirmed = false;
            // Certain high-ranking users such as tstupka require to be asked
            // before pulling while there are local modifications
            if (!bNoChanges && Boolean.getBoolean("versioning.mercurial.checkForModificationsBeforePull")
                    && Boolean.FALSE.equals(localChangesConfirmed = confirmOverallModifications(root, supp))) {
                logger.outputInRed(Bundle.MSG_PULL_LOCALMODS_CANCEL());
                logger.output(""); // NOI18N
                return;
            }
            
            // Warn User when there are Local Changes present that Pull will overwrite
            if (!bNoChanges && !Boolean.TRUE.equals(localChangesConfirmed)
                    && !confirmWithLocalChanges(root, PullAction.class, "MSG_PULL_LOCALMODS_CONFIRM_TITLE", "MSG_PULL_LOCALMODS_CONFIRM_QUERY", listIncoming, logger)) { // NOI18N
                logger.outputInRed(Bundle.MSG_PULL_LOCALMODS_CANCEL());
                logger.output(""); // NOI18N
                return;
            }

            // Do Pull if there are changes to be pulled
            if (bNoChanges || supp.isCanceled()) {
                logger.output(HgUtils.replaceHttpPassword(listIncoming));
            } else {
                HgUtils.runWithoutIndexing(new PullImpl(supp, root, type, 
                        revision, branch, bundleFile, listIncoming),
                        root);
            }
        } catch (HgException.HgCommandCanceledException ex) {
            // canceled by user, do nothing
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        } finally {
            if (bundleFile != null) {
                bundleFile.delete();
            }
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_DONE")); // NOI18N
            logger.output(""); // NOI18N
            pullSource.clearPassword();
        }
    }

    @NbBundle.Messages({
        "MSG_PullAction.progress.checkForModifications=Checking for local modifications",
        "LBL_PullAction.localModifications.title=Confirm Pull with Local Modifications",
        "MSG_PullAction.localModifications.text=There are local modifications that may prevent from subsequent merge or rebase.\n"
            + "Do you still want to perform the Pull?",
    })
    private static Boolean confirmOverallModifications (File root, HgProgressSupport supp) {
        Boolean confirmed = null;
        supp.setDisplayName(Bundle.MSG_PullAction_progress_checkForModifications());
        int interestingStatus = FileInformation.STATUS_LOCAL_CHANGE & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
        try {
            Map<File, FileInformation> statuses = HgCommand.getStatus(root,
                    Collections.<File>singletonList(root), null, null, false);
            for (Map.Entry<File, FileInformation> e : statuses.entrySet()) {
                FileInformation info = e.getValue();
                if ((info.getStatus() & interestingStatus) != 0) {
                    confirmed = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(Utilities.findDialogParent(), 
                            Bundle.MSG_PullAction_localModifications_text(),
                            Bundle.LBL_PullAction_localModifications_title(), 
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    break;
                }
            }
        } catch (HgException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
        }
        return confirmed;
    }

    private static String getMessage(String msgKey, String... args) {
        return NbBundle.getMessage(PullAction.class, msgKey, args);
    }

    private static boolean isRebaseAllowed (File reposiory, List<HgLogMessage> parents) {
        try {
            if (parents.size() == 1 && parents.get(0).getCSetShortID().equals(
                    HgCommand.getParents(reposiory, null, null).get(0).getCSetShortID())) {
                return true;
            }
        } catch (HgException.HgCommandCanceledException ex) {
            
        } catch (HgException ex) {
            Logger.getLogger(PullAction.class.getName()).log(Level.INFO, null, ex);
        }
        return false;
    }

    @NbBundle.Messages({
        "MSG_PullAction.popingPatches=Popping applied patches",
        "MSG_PullAction.pulling=Pulling changesets",
        "MSG_PullAction.pushingPatches=Pushing patches",
        "# Capitalized letters used intentionally to emphasize the words in "
            + "an output window, should be translated",
        "MSG_PULL_MERGE_DO=INFO: Performing Merge with pulled changes",
        "# Capitalized letters used intentionally to emphasize the words in "
            + "the output window, should be translated",
        "MSG_PULL_REBASE_DO=INFO: Performing Rebase of local commits "
            + "onto pulled changes",
        "MSG_PullAction.progress.pullingFromLocal=Getting incoming changesets",
        "MSG_PullAction.progress.unbundling=Unbundling incoming changesets"
    })
    private static class PullImpl implements Callable<Void> {

        private QPatch topPatch;
        private final HgProgressSupport supp;
        private final File root;
        private final PullType type;
        private final String revision;
        private final String branch;
        private final File fileToUnbundle;
        private final List<String> listIncoming;
        private boolean rebaseAccepted;
        private boolean mergeAccepted;

        public PullImpl (HgProgressSupport supp, File root, 
                PullType type, String revision, String branch,
                File fileToUnbundle, List<String> listIncoming) {
            this.supp = supp;
            this.root = root;
            this.type = type;
            this.revision = revision;
            this.branch = branch;
            this.fileToUnbundle = fileToUnbundle;
            this.listIncoming = listIncoming;
        }

        @Override
        public Void call () throws HgException {
            OutputLogger logger = supp.getLogger();
            topPatch = FetchAction.selectPatch(root);
            if (topPatch != null) {
                supp.setDisplayName(Bundle.MSG_PullAction_popingPatches());
                SystemAction.get(QGoToPatchAction.class).popAllPatches(root, logger);
                supp.setDisplayName(Bundle.MSG_PullAction_pulling());
                logger.output(""); // NOI18N
            }
            if (supp.isCanceled()) {
                return null;
            }
            List<String> list;
            if(type == PullType.LOCAL){
                supp.setDisplayName(Bundle.MSG_PullAction_progress_pullingFromLocal());
                list = HgCommand.doPull(root, revision, branch, logger);
            }else{
                supp.setDisplayName(Bundle.MSG_PullAction_progress_unbundling());
                list = HgCommand.doUnbundle(root, fileToUnbundle, false, logger);
            }
            if (list != null && !list.isEmpty()) {
                annotateChangeSets(HgUtils.replaceHttpPassword(listIncoming), PullAction.class, "MSG_CHANGESETS_TO_PULL", logger); // NOI18N
                handlePulledChangesets(list);
            }

            return null;
        }

        @NbBundle.Messages({
            "MSG_PullAction_mergeNeeded_text=Pull has completed and created "
                + "multiple heads.\n"
                + "Do you want to Merge or Rebase the two heads or Keep them "
                + "unmerged in the repository?",
            "LBL_PullAction_mergeNeeded_title=Pull Created Multiple Heads",
            "CTL_PullAction_mergeButton_text=&Merge",
            "CTL_PullAction_mergeButton_TTtext=Merge the two created heads",
            "CTL_PullAction_rebaseButton_text=&Rebase",
            "CTL_PullAction_rebaseButton_TTtext=Rebase local changesets onto "
                + "the tipmost branch head",
            "CTL_PullAction_keepButton_text=&Keep Heads",
            "CTL_PullAction_keepButton_TTtext=Leaves the heads alone and does "
                + "nothing",
            "MSG_PullAction.progress.merging=Merging heads",
            "MSG_PullAction.progress.finishing=Refreshing file statuses",
            "MSG_PullAction.progress.updating=Updating to branch's tip",
            "# {0} - branch name", "MSG_PullAction.progress.updatingBranch=Updating to tip of branch \"{0}\""
        })
        private void handlePulledChangesets (List<String> list) throws HgException {
            OutputLogger logger = supp.getLogger();
            boolean bMergeNeeded = false;
            boolean updateNeeded = false;
            boolean update = true;
            for (String s : list) {
                logger.output(HgUtils.replaceHttpPassword(s));
                if (HgCommand.isMergeNeededMsg(s)) {
                    bMergeNeeded = true;
                } else if (HgCommand.isUpdateNeededMsg(s) || HgCommand.isHeadsNeededMsg(s)) {
                    updateNeeded = true;
                }
            }
            
            // Handle Merge - both automatic and merge with conflicts
            mergeAccepted = false;
            rebaseAccepted = false;
            boolean warnMoreHeads = true;
            if (!supp.isCanceled()) {
                if (bMergeNeeded) {
                    List<HgLogMessage> parents = HgCommand.getParents(root, null, null);
                    askForMerge(parents);
                    warnMoreHeads = false;
                } else if (updateNeeded) {
                    String currentBranch = null;
                    try {
                        currentBranch = HgCommand.getBranch(root);
                    } catch (HgException ex) {
                        Mercurial.LOG.log(Level.FINE, null, ex);
                    }
                    update = HgModuleConfig.getDefault().isPullWithUpdate();
                    if (currentBranch == null) {
                        supp.setDisplayName(Bundle.MSG_PullAction_progress_updating());
                    } else {
                        if (update) {
                            supp.setDisplayName(Bundle.MSG_PullAction_progress_updatingBranch(currentBranch));
                        }
                    }
                    if (update) {
                        list = HgCommand.doUpdateAll(root, false, currentBranch);
                        logger.output(list);
                    }
                }
            }
            boolean finished = !bMergeNeeded;
            try {
                if (rebaseAccepted) {
                    logger.output(""); //NOI18N
                    logger.outputInRed(Bundle.MSG_PULL_REBASE_DO());
                    finished = RebaseAction.doRebase(root, null, null, null, supp);
                } else if (mergeAccepted) {
                    logger.output(""); //NOI18N
                    logger.outputInRed(Bundle.MSG_PULL_MERGE_DO());
                    supp.setDisplayName(Bundle.MSG_PullAction_progress_merging());
                    list = MergeAction.doMergeAction(root, null, logger);
                    if (MergeAction.handleMergeOutput(root, list, logger, false)) {
                        final Action commitAction = SystemActionBridge.createAction(
                                SystemAction.get(CommitAction.class), "commit", Lookups.fixed(
                                new AbstractNode(Children.LEAF, Lookups.fixed(root))));
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                commitAction.actionPerformed(new ActionEvent(root, ActionEvent.ACTION_PERFORMED, null));
                            }
                        });
                    }
                }
                if (!supp.isCanceled() && finished && topPatch != null) {
                    logger.output(""); //NOI18N
                    supp.setDisplayName(Bundle.MSG_PullAction_pushingPatches());
                    SystemAction.get(QGoToPatchAction.class).applyPatch(root, topPatch.getId(), logger);
                    HgLogMessage parent = HgCommand.getParents(root, null, null).get(0);
                    logger.output(""); //NOI18N
                    HgUtils.logHgLog(parent, logger);
                }
            } finally {
                supp.setDisplayName(Bundle.MSG_PullAction_progress_finishing());
                HgLogMessage[] heads = HgCommand.getHeadRevisionsInfo(root, false, OutputLogger.getLogger(null));
                Map<String, Collection<HgLogMessage>> branchHeads = HgUtils.sortByBranch(heads);
                if (!branchHeads.isEmpty()) {
                    MergeAction.displayMergeWarning(branchHeads, logger, warnMoreHeads && !supp.isCanceled());
                }
                Mercurial.getInstance().historyChanged(root);
                if (update) {
                    HgUtils.notifyUpdatedFiles(root, list);
                    HgUtils.forceStatusRefresh(root);
                }
            }
        }

        private void askForMerge (List<HgLogMessage> parents) {
            if (isRebaseAllowed(root, parents)) {
                if (Boolean.getBoolean("versioning.mercurial.pullwithrebase")) { //NOI18N
                    mergeAccepted = false;
                    rebaseAccepted = true;
                    return;
                }
                JButton btnMerge = new JButton();
                Mnemonics.setLocalizedText(btnMerge, Bundle.CTL_PullAction_mergeButton_text());
                btnMerge.setToolTipText(Bundle.CTL_PullAction_mergeButton_TTtext());
                JButton btnRebase = new JButton();
                Mnemonics.setLocalizedText(btnRebase, Bundle.CTL_PullAction_rebaseButton_text());
                btnRebase.setToolTipText(Bundle.CTL_PullAction_rebaseButton_TTtext());
                JButton btnKeep = new JButton();
                Mnemonics.setLocalizedText(btnKeep, Bundle.CTL_PullAction_keepButton_text());
                btnKeep.setToolTipText(Bundle.CTL_PullAction_keepButton_TTtext());
                Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                        Bundle.MSG_PullAction_mergeNeeded_text(),
                        Bundle.LBL_PullAction_mergeNeeded_title(),
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE,
                        new Object[] { btnMerge, btnRebase, btnKeep },
                        btnMerge));
                mergeAccepted = value == btnMerge;
                rebaseAccepted = value == btnRebase;
            } else {
                mergeAccepted = HgUtils.confirmDialog(
                        PullAction.class, "MSG_PULL_MERGE_CONFIRM_TITLE", "MSG_PULL_MERGE_CONFIRM_QUERY"); //NOI18N
            }
        }
    }

}
