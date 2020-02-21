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
package org.netbeans.modules.mercurial.remote.ui.queues;

import java.awt.EventQueue;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.ui.queues.CreateRefreshAction.Cmd.CreateRefreshPatchCmd;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.remotefs.versioning.hooks.HgQueueHook;
import org.netbeans.modules.remotefs.versioning.hooks.HgQueueHookContext;
import org.netbeans.modules.remotefs.versioning.util.common.VCSCommitTable;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
abstract class CreateRefreshAction extends ContextAction {

    static final String RECENT_COMMIT_MESSAGES = "recentCommitMessage"; // NOI18N
    private final String bundleKeyPostfix;

    public CreateRefreshAction (String bundleKeyPostfix) {
        super();
        this.bundleKeyPostfix = bundleKeyPostfix;
    }
    
    @Override
    protected boolean enable (Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }
    
    @Override
    protected void performContextAction (final Node[] nodes) {
        final VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final VCSFileProxy roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) {
            return;
        }
        final VCSFileProxy root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        new HgProgressSupport() {

            @Override
            protected void perform () {
                if (!QUtils.isMQEnabledExtension(root)) {
                    return;
                }
                // show commit dialog
                final QCommitPanel panel = createPanel(root, roots);
                if (panel != null) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            performAction(root, roots, panel, ctx);
                        }
                    });
                }
            }
        }.start(Mercurial.getInstance().getRequestProcessor(root), root, NbBundle.getMessage(CreateRefreshAction.class, "LBL_CreateRefreshAction.preparing.progress")); //NOI18N
    }

    abstract QCommitPanel createPanel (VCSFileProxy root, VCSFileProxy[] roots);
    
    private void performAction (final VCSFileProxy root, final VCSFileProxy[] roots, final QCommitPanel panel, final VCSContext ctx) {
        VCSCommitTable<QFileNode> table = panel.getCommitTable();
        String contentTitle = VCSFileProxySupport.getContextDisplayName(ctx);
        boolean ok = panel.open(ctx, panel.getHelpContext(), NbBundle.getMessage(CreateRefreshAction.class, "CTL_RefreshPatchDialog_Title." + bundleKeyPostfix, contentTitle)); //NOI18N

        if (ok) {
            final List<QFileNode> commitFiles = table.getCommitFiles();
            persistCanceledCommitMessage(root, panel.getParameters(), "");
            panel.getParameters().storeCommitMessage();
            new HgProgressSupport() {
                @Override
                protected void perform () {
                    String message = panel.getParameters().getCommitMessage();
                    String patchName = panel.getParameters().getPatchName();
                    Set<VCSFileProxy> excludedFiles = new HashSet<>();
                    List<VCSFileProxy> addCandidates = new LinkedList<>();
                    List<VCSFileProxy> deleteCandidates = new LinkedList<>();
                    List<VCSFileProxy> commitCandidates = new LinkedList<>();
                    Collection<HgQueueHook> hooks = panel.getHooks();
                    String user = panel.getParameters().getUser();
                    if (user != null) {
                        HgModuleConfig.getDefault(root).putRecentCommitAuthors(user);
                    }
                    FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
                    for (QFileNode node : commitFiles) {
                        if (isCanceled()) {
                            return;
                        }
                        VCSCommitOptions option = node.getCommitOptions();
                        if (option != QFileNode.EXCLUDE) {
                            int status = cache.getStatus(node.getFile()).getStatus();
                            if ((status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0) {
                                addCandidates.add(node.getFile());
                            } else  if ((status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) != 0) {
                                addCandidates.add(node.getFile());
                            }
                            commitCandidates.add(node.getFile());
                        } else {
                            excludedFiles.add(node.getFile());
                        }
                    }
                    if (isCanceled()) {
                        return;
                    }
                    
                    OutputLogger logger = getLogger();
                    Set<VCSFileProxy> filesToRefresh = new HashSet<>();
                    try {
                        logger.outputInRed(NbBundle.getMessage(CreateRefreshAction.class, "MSG_CREATE_REFRESH_TITLE." + bundleKeyPostfix)); //NOI18N
                        logger.outputInRed(NbBundle.getMessage(CreateRefreshAction.class, "MSG_CREATE_REFRESH_TITLE_SEP." + bundleKeyPostfix)); //NOI18N
                        logger.output(NbBundle.getMessage(CreateRefreshAction.class, "MSG_CREATE_REFRESH_INFO_SEP." + bundleKeyPostfix, patchName, root.getPath())); //NOI18N

                        new Cmd.AddCmd(root, addCandidates, logger, null, "hg add {0} into {1}").handle(); //NOI18N
                        new Cmd.RemoveCmd(root, deleteCandidates, logger, null, "hg delete {0} from {1}").handle(); //NOI18N

                        VCSFileProxy[] hookFiles = null;
                        if (hooks.size() > 0) {
                            hookFiles = commitCandidates.toArray(new VCSFileProxy[commitCandidates.size()]);
                        }
                        HgModuleConfig.getDefault(root).setLastUsedQPatchMessage(patchName, message);
                        HgQueueHookContext context = new HgQueueHookContext(hookFiles, message, patchName);
                        for (HgQueueHook hook : hooks) {
                            try {
                                // XXX handle returned context
                                context = hook.beforePatchRefresh(context);
                                if (context != null) {
                                    message = context.getMessage();
                                }
                            } catch (IOException ex) {
                                // XXX handle veto
                            }
                        }
                        Cmd.CreateRefreshPatchCmd commitCmd = createHgCommand(root, commitCandidates, logger,
                                message, patchName, user,
                                bundleKeyPostfix, Arrays.asList(roots), excludedFiles, filesToRefresh);
                        commitCmd.setCommitHooks(context, hooks, hookFiles);
                        commitCmd.handle();

                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                    } finally {
                        Mercurial.getInstance().getFileStatusCache().refreshAllRoots(filesToRefresh);
                        Mercurial.getInstance().getMercurialHistoryProvider().fireHistoryChange(filesToRefresh.toArray(new VCSFileProxy[filesToRefresh.size()]));
                        logger.outputInRed(NbBundle.getMessage(CreateRefreshAction.class, "MSG_CREATE_REFRESH_DONE." + bundleKeyPostfix)); // NOI18N
                        logger.output(""); // NOI18N
                    }
                }

            }.start(Mercurial.getInstance().getRequestProcessor(root), root, NbBundle.getMessage(CreateRefreshAction.class, "LBL_CreateRefreshAction.progress." + bundleKeyPostfix)); //NOI18N
        } else if (!panel.getParameters().getCommitMessage().isEmpty()) {
            persistCanceledCommitMessage(root, panel.getParameters(), panel.getParameters().getCommitMessage());
        }
    }

    abstract CreateRefreshPatchCmd createHgCommand (VCSFileProxy root, List<VCSFileProxy> commitCandidates, OutputLogger logger,
            String message, String patchName, String user, String bundleKeyPostfix,
            List<VCSFileProxy> roots, Set<VCSFileProxy> excludedFiles, Set<VCSFileProxy> filesToRefresh);

    abstract void persistCanceledCommitMessage (VCSFileProxy root, QCreatePatchParameters parameters, String canceledCommitMessage);

    static abstract class Cmd {
        protected final List<VCSFileProxy> candidates;
        protected final OutputLogger logger;
        protected final String logMsgFormat;
        protected final String msg;
        protected final VCSFileProxy repository;
        public Cmd(VCSFileProxy repository, List<VCSFileProxy> candidates, OutputLogger logger, String msg, String logMsgFormat) {
            this.repository = repository;
            this.candidates = candidates;
            this.logger = logger;
            this.logMsgFormat = logMsgFormat;
            this.msg = msg;
        }
        void handle() throws HgException {
            if(candidates.isEmpty()) {
                return;
            }
            doCmd();
            for (VCSFileProxy f : candidates) {
                logger.output(MessageFormat.format(logMsgFormat, f.getName(), repository));
            }
        }
        abstract void doCmd () throws HgException;
        static class AddCmd extends Cmd {
            public AddCmd(VCSFileProxy repository, List<VCSFileProxy> m, OutputLogger logger, String msgFormat, String msg) {
                super(repository, m, logger, msgFormat, msg);
            }
            @Override
            void doCmd () throws HgException {
                HgCommand.doAdd(repository, candidates, logger);
            }
        }
        static class RemoveCmd extends Cmd {
            public RemoveCmd(VCSFileProxy repository, List<VCSFileProxy> m, OutputLogger logger, String msgFormat, String msg) {
                super(repository, m, logger, msgFormat, msg);
            }
            @Override
            void doCmd () throws HgException {
                HgCommand.doRemove(repository, candidates, logger);
            }
        }
        static abstract class CreateRefreshPatchCmd extends Cmd {
            private HgQueueHookContext context;
            private Collection<HgQueueHook> hooks;
            private VCSFileProxy[] hookFiles;
            private final List<VCSFileProxy> rootFiles;
            private final Set<VCSFileProxy> refreshFiles;
            private final Set<VCSFileProxy> excludedFiles;
            private final String patchId;
            private final String bundleKeyPostfix;
            private final String user;

            public CreateRefreshPatchCmd(VCSFileProxy repository, List<VCSFileProxy> m, OutputLogger logger, String commitMessage,
                    String patchId, String user, String bundleKeyPostfix,
                    List<VCSFileProxy> rootFiles, Set<VCSFileProxy> excludedFiles, Set<VCSFileProxy> filesToRefresh) {
                super(repository, m, logger, commitMessage, null);
                this.patchId = patchId;
                this.user = user;
                this.bundleKeyPostfix = bundleKeyPostfix;
                this.rootFiles = rootFiles;
                this.excludedFiles = excludedFiles;
                this.refreshFiles = filesToRefresh;
            }

            public void setCommitHooks (HgQueueHookContext context, Collection<HgQueueHook> hooks, VCSFileProxy[] hookFiles) {
                this.context = context;
                this.hooks = hooks;
                this.hookFiles = hookFiles;
            }

            @Override
            void handle() throws HgException {
                doCmd();
            }

            @Override
            void doCmd () throws HgException {
                Set<VCSFileProxy> files = new HashSet<>(candidates);
                files.addAll(excludedFiles); // should be also refreshed because previously included files will now change to modified
                try {                    
                    runHgCommand(repository, candidates, excludedFiles, patchId, msg, user, logger);
                } catch (HgException.HgTooLongArgListException e) {
                    Mercurial.LOG.log(Level.INFO, null, e);
                    List<VCSFileProxy> reducedCommitCandidates;
                    StringBuilder offeredFileNames = new StringBuilder();
                    if (rootFiles != null && rootFiles.size() < 5) {
                        reducedCommitCandidates = new ArrayList<>(rootFiles);
                        files = new HashSet<>(rootFiles);
                        for (VCSFileProxy f : reducedCommitCandidates) {
                            offeredFileNames.append('\n').append(f.getName());     //NOI18N
                        }
                    } else {
                        reducedCommitCandidates = Collections.<VCSFileProxy>emptyList();
                        files = Collections.singleton(repository);
                        offeredFileNames.append('\n').append(repository.getName()); //NOI18N
                    }
                    NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CreateRefreshAction.class, "MSG_LONG_COMMAND_QUERY." + bundleKeyPostfix, offeredFileNames.toString())); //NOI18N
                    descriptor.setTitle(NbBundle.getMessage(CreateRefreshAction.class, "MSG_LONG_COMMAND_TITLE")); //NOI18N
                    descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
                    descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

                    Object res = DialogDisplayer.getDefault().notify(descriptor);
                    if (res == NotifyDescriptor.NO_OPTION) {
                        return;
                    }
                    Mercurial.LOG.log(Level.INFO, "QRefresh: refreshing patch with a reduced set of files: {0}", reducedCommitCandidates.toString()); //NOI18N
                    runHgCommand(repository, reducedCommitCandidates, Collections.<VCSFileProxy>emptySet(), patchId, msg, user, logger);
                } finally {
                    refreshFiles.addAll(files);
                }

                HgLogMessage tip = HgCommand.doTip(repository, logger);

                context = new HgQueueHookContext(hookFiles, msg, patchId);
                for (HgQueueHook hook : hooks) {
                    hook.afterPatchRefresh(context);
                }

                if (candidates.size() == 1) {
                    logger.output(
                            NbBundle.getMessage(CreateRefreshAction.class,
                            "MSG_PATCH_REFRESH_SEP_ONE." + bundleKeyPostfix, patchId)); //NOI18N
                } else if (!candidates.isEmpty()) {
                    logger.output(
                            NbBundle.getMessage(CreateRefreshAction.class,
                            "MSG_PATCH_REFRESH_SEP." + bundleKeyPostfix, patchId, candidates.size())); //NOI18N
                }
                for (VCSFileProxy f : candidates) {
                    logger.output("\t" + f.getPath()); //NOI18N
                }
                HgUtils.logHgLog(tip, logger);
            }

            protected abstract void runHgCommand (VCSFileProxy repository, List<VCSFileProxy> candidates, Set<VCSFileProxy> excludedFiles,
                    String patchId, String msg, String user, OutputLogger logger) throws HgException;
        }
    }
}
