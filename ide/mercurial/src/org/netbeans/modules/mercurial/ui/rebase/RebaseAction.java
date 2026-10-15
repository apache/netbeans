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
package org.netbeans.modules.mercurial.ui.rebase;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.commands.RebaseCommand;
import org.netbeans.modules.mercurial.commands.RebaseCommand.Result.State;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * 
 * @author Ondrej Vrabec
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.rebase.RebaseAction", category = "Mercurial")
@ActionRegistration(displayName = "#CTL_MenuItem_RebaseAction")
@NbBundle.Messages({
    "MSG_Rebase_Progress=Rebasing...",
    "MSG_Rebase_Started=Starting rebase",
    "CTL_MenuItem_RebaseAction=&Rebase...",
    "MSG_Rebase_Title_Sep=----------------",
    "MSG_Rebase_Title=Mercurial Rebase",
    "# Capitalized letters used intentionally to emphasize the words in an output window, should be translated",
    "MSG_Rebase_Finished=INFO: End of Rebase"
})
public class RebaseAction extends ContextAction {
    
    private static final Logger LOG = Logger.getLogger(RebaseAction.class.getName());
    private static final String NB_REBASE_INFO_FILE = "netbeans-rebase.info"; //NOI18N
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_RebaseAction"; // NOI18N
    }

    @Override
    @NbBundle.Messages({
        "MSG_Rebase.unfinishedMerge=Cannot rebase because of an unfinished merge.",
        "MSG_Rebase.noBranchHeads=No heads in the current branch \"{0}\".\n"
            + "Did you forget to commit to permanently create the branch?\n\n"
            + "Please switch to a fully operational branch before starting rebase.",
        "MSG_Rebase_Preparing_Progress=Preparing Rebase..."
    })
    protected void performContextAction(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                HgLogMessage[] workingCopyParents = WorkingCopyInfo.getInstance(root).getWorkingCopyParents();
                if (HgUtils.isRebasing(root)) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            finishRebase(root);
                        }
                    });
                } else if (workingCopyParents.length > 1) {
                    // inside a merge
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                Bundle.MSG_Rebase_unfinishedMerge(),
                                NotifyDescriptor.ERROR_MESSAGE));
                } else {
                    try {
                        final HgLogMessage workingCopyParent = workingCopyParents[0];
                        final String currentBranch = HgCommand.getBranch(root);
                        HgLogMessage[] heads = HgCommand.getHeadRevisionsInfo(root, false, OutputLogger.getLogger(null));
                        final Collection<HgLogMessage> branchHeads = HgUtils.sortByBranch(heads).get(currentBranch);
                        if (isCanceled()) {
                            return;
                        }
                        if (branchHeads == null) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                    Bundle.MSG_Rebase_noBranchHeads(currentBranch),
                                    NotifyDescriptor.ERROR_MESSAGE));
                            return;
                        }
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                doRebase(root, workingCopyParent, branchHeads);
                            }
                        });
                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                    }
                }
            }
        };
        support.start(rp, root, Bundle.MSG_Rebase_Preparing_Progress());
    }

    @NbBundle.Messages({
        "MSG_RebaseAction.progress.preparingChangesets=Preparing changesets to rebase",
        "MSG_RebaseAction.progress.rebasingChangesets=Rebasing changesets"
    })
    public static boolean doRebase (File root, String base, String source, String dest,
            HgProgressSupport supp) throws HgException {
        OutputLogger logger = supp.getLogger();
        Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);
        String destRev = dest;
        String sourceRev = source;
        if (!hooks.isEmpty()) {
            try {
                if (destRev == null) {
                    HgLogMessage[] heads = HgCommand.getHeadRevisionsInfo(root, false, OutputLogger.getLogger(null));
                    final Collection<HgLogMessage> branchHeads = HgUtils.sortByBranch(heads).get(HgCommand.getBranch(root));
                    if (branchHeads != null && !branchHeads.isEmpty()) {
                        HgLogMessage tipmostHead = branchHeads.iterator().next();
                        for (HgLogMessage head : branchHeads) {
                            if (head.getRevisionAsLong() > tipmostHead.getRevisionAsLong()) {
                                tipmostHead = head;
                            }
                        }
                        destRev = tipmostHead.getCSetShortID();
                    }
                }
                if (supp.isCanceled()) {
                    return false;
                }
                if (sourceRev == null) {
                    String baseRev = base;
                    if (baseRev == null) {
                        baseRev = HgCommand.getParent(root, null, null).getChangesetId();
                    }
                    supp.setDisplayName(Bundle.MSG_RebaseAction_progress_preparingChangesets());
                    String revPattern = MessageFormat.format("last(limit(ancestor({0},{1})::{1}, 2), 1)", destRev, baseRev); //NOI18N
                    HgLogMessage[] revs = HgCommand.getRevisionInfo(root, Collections.<String>singletonList(revPattern), null);
                    if (revs.length == 0) {
                        LOG.log(Level.FINE, "doRebase: no revision returned for {0}", revPattern); //NOI18N
                    } else {
                        sourceRev = revs[0].getCSetShortID();
                    }
                }
            } catch (HgException.HgCommandCanceledException ex) {
            } catch (HgException ex) {
                // do nothing, just log, probably an unsupported hg revision language
                LOG.log(Level.INFO, null, ex);
            }
        }
        if (supp.isCanceled()) {
            return false;
        }
        
        supp.setDisplayName(Bundle.MSG_RebaseAction_progress_rebasingChangesets());
        RebaseCommand.Result rebaseResult = new RebaseCommand(root, RebaseCommand.Operation.START, logger)
                .setRevisionBase(base)
                .setRevisionSource(source)
                .setRevisionDest(dest)
                .call();
        handleRebaseResult(new RebaseHookContext(root, sourceRev, destRev, hooks), rebaseResult, supp);
        return rebaseResult.getState() == State.OK;
    }

    @NbBundle.Messages({
        "MSG_RebaseAction.progress.refreshingFiles=Refreshing files"
    })
    private void doRebase (final File root, HgLogMessage workingCopyParent,
            Collection<HgLogMessage> branchHeads) {
        final Rebase rebase = new Rebase(root, workingCopyParent, branchHeads);
        if (rebase.showDialog()) {
            new HgProgressSupport() {
                @Override
                protected void perform () {
                    doRebase(rebase);
                }

                private void doRebase (final Rebase rebase) {
                    final HgProgressSupport supp = this;
                    OutputLogger logger = getLogger();
                    try {
                        logger.outputInRed(Bundle.MSG_Rebase_Title());
                        logger.outputInRed(Bundle.MSG_Rebase_Title_Sep());
                        logger.output(Bundle.MSG_Rebase_Started());
                        
                        HgUtils.runWithoutIndexing(new Callable<Void>() {
                            @Override
                            public Void call () throws Exception {
                                RebaseAction.doRebase(root, rebase.getRevisionBase(),
                                        rebase.getRevisionSource(),
                                        rebase.getRevisionDest(), supp);
                                supp.setDisplayName(Bundle.MSG_RebaseAction_progress_refreshingFiles());
                                HgUtils.forceStatusRefresh(root);
                                return null;
                            }
                        }, root);
                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                    }
                    logger.outputInRed(Bundle.MSG_Rebase_Finished());
                    logger.output(""); // NOI18N
                }
            }.start(Mercurial.getInstance().getRequestProcessor(root), root, Bundle.MSG_Rebase_Progress());
        }
    }

    @NbBundle.Messages({
        "MSG_Rebase_Abort=Aborting an interrupted rebase",
        "MSG_Rebase_Aborted=Rebase Aborted",
        "MSG_Rebase_Merging_Failed=Rebase interrupted because of a failed merge.\nResolve the conflicts and run the rebase again.",
        "MSG_Rebase_Continue=Continuing an interrupted rebase",
        "CTL_RebaseAction.continueButton.text=C&ontinue",
        "CTL_RebaseAction.continueButton.TTtext=Continue the interrupted rebase",
        "CTL_RebaseAction.abortButton.text=Abo&rt",
        "CTL_RebaseAction.abortButton.TTtext=Abort the interrupted rebase",
        "LBL_Rebase.rebasingState.title=Unfinished Rebase",
        "# {0} - repository name", "MSG_Rebase.rebasingState.text=Repository {0} is in the middle of an unfinished rebase.\n"
            + "Do you want to continue or abort the unfinished rebase?"
    })
    private void finishRebase (final File root) {
        // abort or continue?
        JButton btnContinue = new JButton();
        Mnemonics.setLocalizedText(btnContinue, Bundle.CTL_RebaseAction_continueButton_text());
        btnContinue.setToolTipText(Bundle.CTL_RebaseAction_continueButton_TTtext());
        JButton btnAbort = new JButton();
        Mnemonics.setLocalizedText(btnAbort, Bundle.CTL_RebaseAction_abortButton_text());
        btnAbort.setToolTipText(Bundle.CTL_RebaseAction_abortButton_TTtext());
        Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                Bundle.MSG_Rebase_rebasingState_text(root.getName()),
                Bundle.LBL_Rebase_rebasingState_title(),
                NotifyDescriptor.YES_NO_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                new Object[] { btnContinue, btnAbort, NotifyDescriptor.CANCEL_OPTION }, 
                btnContinue));
        if (value == btnAbort || value == btnContinue) {
            final boolean cont = btnContinue == value;
            new HgProgressSupport() {
                @Override
                protected void perform () {
                    finishRebase(cont);
                }
                
                private void finishRebase (final boolean cont) {
                    final OutputLogger logger = getLogger();
                    final HgProgressSupport supp = this;
                    try {
                        logger.outputInRed(Bundle.MSG_Rebase_Title());
                        logger.outputInRed(Bundle.MSG_Rebase_Title_Sep());
                        logger.output(cont
                                ? Bundle.MSG_Rebase_Continue()
                                : Bundle.MSG_Rebase_Abort());
                        HgUtils.runWithoutIndexing(new Callable<Void>() {
                            @Override
                            public Void call () throws Exception {
                                RebaseHookContext rebaseCtx = buildRebaseContext(root);
                                RebaseCommand.Result rebaseResult = new RebaseCommand(root, cont
                                        ? RebaseCommand.Operation.CONTINUE
                                        : RebaseCommand.Operation.ABORT, logger).call();
                                HgUtils.forceStatusRefresh(root);
                                handleRebaseResult(rebaseCtx, rebaseResult, supp);
                                return null;
                            }
                        }, root);
                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                    }
                    logger.outputInRed(Bundle.MSG_Rebase_Finished());
                    logger.output(""); // NOI18N
                }
            }.start(Mercurial.getInstance().getRequestProcessor(root), root, Bundle.MSG_Rebase_Progress());
        }
    }

    @NbBundle.Messages({
        "MSG_RebaseAction.progress.repairingPushHooks=Updating push hooks"
    })
    private static void handleRebaseResult (RebaseHookContext rebaseCtx, RebaseCommand.Result rebaseResult, HgProgressSupport supp) {
        OutputLogger logger = supp.getLogger();
        for (File f : rebaseResult.getTouchedFiles()) {
            Mercurial.getInstance().notifyFileChanged(f);
        }
        logger.output(rebaseResult.getOutput());
        File repository = rebaseCtx.repository;
        Mercurial.getInstance().historyChanged(repository);
        getNetBeansRebaseInfoFile(repository).delete();
        
        if (rebaseResult.getState() == State.ABORTED) {
            logger.outputInRed(Bundle.MSG_Rebase_Aborted());
        } else if (rebaseResult.getState() == State.MERGING) {
            storeRebaseContext(rebaseCtx);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        Bundle.MSG_Rebase_Merging_Failed(),
                        NotifyDescriptor.ERROR_MESSAGE));
            logger.outputInRed(Bundle.MSG_Rebase_Merging_Failed());
        } else if (rebaseResult.getState() == State.OK) {
            if (!rebaseCtx.hooks.isEmpty() && rebaseCtx.source != null && rebaseCtx.dest != null) {
                File bundleFile = rebaseResult.getBundleFile();
                if (bundleFile != null && bundleFile.exists()) {
                    supp.setDisplayName(Bundle.MSG_RebaseAction_progress_repairingPushHooks());
                    try {
                        HgHookContext.LogEntry[] originalEntries = findOriginalEntries(repository, bundleFile);
                        HgHookContext.LogEntry[] newEntries = findNewEntries(repository, rebaseCtx.dest);
                        Map<String, String> mapping = findChangesetMapping(originalEntries, newEntries);
                        for (HgHook hgHook : rebaseCtx.hooks) {
                            hgHook.afterCommitReplace(
                                    new HgHookContext(new File[] { repository }, null, originalEntries),
                                    new HgHookContext(new File[] { repository }, null, newEntries),
                                    mapping);
                        }
                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        // do nothing, just log
                        // probably an unsupported hg revision language
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        }
        logger.output("");
    }

    private static HgHookContext.LogEntry[] findOriginalEntries (File repository, File bundleFile) throws HgException {
        List<HgLogMessage> originalMessages = HgCommand.getBundleChangesets(repository, bundleFile, null);
        return convertToEntries(originalMessages.toArray(new HgLogMessage[0]));
    }

    private static HgHookContext.LogEntry[] findNewEntries (File repository, String destRevision) {
        HgLogMessage[] newMessages = HgCommand.getRevisionInfo(repository,
                Collections.<String>singletonList(MessageFormat.format(
                "descendants(last(children({0}), 1))", //NOI18N
                destRevision)), null);
        return convertToEntries(newMessages);
    }

    private static HgHookContext.LogEntry[] convertToEntries (HgLogMessage[] messages) {
        List<HgHookContext.LogEntry> entries = new ArrayList<HgHookContext.LogEntry>(messages.length);
        for (HgLogMessage msg : messages) {
            entries.add(new HgHookContext.LogEntry(
                    msg.getMessage(),
                    msg.getAuthor(),
                    msg.getCSetShortID(),
                    msg.getDate()));
        }
        return entries.toArray(new HgHookContext.LogEntry[0]);
    }
    
    private static Map<String, String> findChangesetMapping (HgHookContext.LogEntry[] originalEntries, HgHookContext.LogEntry[] newEntries) {
        Map<String, String> mapping = new HashMap<String, String>(originalEntries.length);
        for (HgHookContext.LogEntry original : originalEntries) {
            boolean found = false;
            for (HgHookContext.LogEntry newEntry : newEntries) {
                if (original.getDate().equals(newEntry.getDate())
                        && original.getAuthor().equals(newEntry.getAuthor())
                        && original.getMessage().equals(newEntry.getMessage())) {
                    // is it really the same commit???
                    mapping.put(original.getChangeset(), newEntry.getChangeset());
                    found = true;
                    break;
                }
            }
            if (!found) {
                // delete ????
                mapping.put(original.getChangeset(), null);
            }
        }
        return mapping;
    }

    private static File getNetBeansRebaseInfoFile (File root) {
        return new File(HgUtils.getHgFolderForRoot(root), NB_REBASE_INFO_FILE);
    }

    private static RebaseHookContext buildRebaseContext (File root) {
        Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);
        File info = getNetBeansRebaseInfoFile(root);
        String source = null;
        String dest = null;
        if (info.canRead()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(info));
                String line = br.readLine();
                if (line != null) {
                    source = line;
                }
                line = br.readLine();
                if (line != null) {
                    dest = line;
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, null, ex);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) {}
                }
            }
        }
        return new RebaseHookContext(root, source, dest, hooks);
    }

    private static void storeRebaseContext (RebaseHookContext context) {
        if (context.source == null || context.dest == null) {
            return;
        }
        File info = getNetBeansRebaseInfoFile(context.repository);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(info));
            bw.write(context.source);
            bw.newLine();
            bw.write(context.dest);
            bw.newLine();
            bw.flush();
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {}
            }
        }
    }
    
    private static class RebaseHookContext {
        private final File repository;
        private final String source;
        private final String dest;
        private final Collection<HgHook> hooks;

        public RebaseHookContext (File repository, String sourceRev, String destRev, Collection<HgHook> hooks) {
            this.repository = repository;
            this.source = sourceRev;
            this.dest = destRev;
            this.hooks = hooks;
        }
    }
}
