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
package org.netbeans.modules.mercurial.ui.push;

import java.io.IOException;
import java.net.URISyntaxException;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.versioning.spi.VCSContext;


import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.JOptionPane;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.pull.PullAction;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import static org.netbeans.modules.mercurial.util.HgUtils.isNullOrEmpty;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;

/**
 * Push action for mercurial:
 * hg push - push changes to the specified destination
 *
 * @author John Rice
 */
@NbBundle.Messages({
    "CTL_MenuItem_PushLocal=Push &All Branches",
    "# {0} - repository folder name",
    "CTL_MenuItem_PushRoot=Push &All Branches - {0}"
})
@ActionID(id = "org.netbeans.modules.mercurial.ui.push.PushAction", category = "Mercurial")
@ActionRegistration(lazy = false, displayName = "#CTL_MenuItem_PushLocal")
public class PushAction extends ContextAction {

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if(!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.isEmpty())
            return false;
        return true; // #121293: Speed up menu display, warn user if not set when Push selected
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_PushLocal";                                //NOI18N
    }

    @Override
    public String getName(String role, Node[] activatedNodes) {
        VCSContext ctx = HgUtils.getCurrentContext(activatedNodes);
        Set<File> roots = HgUtils.getRepositoryRoots(ctx);
        String name = roots.size() == 1 ? "CTL_MenuItem_PushRoot" : "CTL_MenuItem_PushLocal"; //NOI18N
        return roots.size() == 1 ? NbBundle.getMessage(PushAction.class, name, roots.iterator().next().getName()) : NbBundle.getMessage(PushAction.class, name);
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
                    final boolean[] canceled = new boolean[1];
                    // run every repository fetch in its own support with its own output window
                    RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
                    HgProgressSupport support = new HgProgressSupport() {
                        @Override
                        public void perform() {
                            getDefaultAndPerformPush(repository, null, null,
                                    this.getLogger());
                            canceled[0] = isCanceled();
                        }
                    };
                    support.start(rp, repository, org.openide.util.NbBundle.getMessage(PushAction.class, "MSG_PUSH_PROGRESS")).waitFinished(); //NOI18N
                    if (canceled[0]) {
                        break;
                    }
                }
            }
        });
    }

    public static void getDefaultAndPerformPush(File root, String revisionToPush,
            String branchToPush, OutputLogger logger) {
        // If the repository has no default push path then inform user
        String tmpPushPath = HgRepositoryContextCache.getInstance().getPushDefault(root);
        if (isNullOrEmpty(tmpPushPath)) {
            tmpPushPath = HgRepositoryContextCache.getInstance().getPullDefault(root);
        }
        if (isNullOrEmpty(tmpPushPath)) {
            notifyDefaultPushUrlNotSpecified(logger);
            return;
        }

        HgURL pushTarget;
        try {
            pushTarget = new HgURL(tmpPushPath);
        } catch (URISyntaxException ex) {
            File sourceRoot = new File(root, tmpPushPath);
            if (sourceRoot.isDirectory()) {
                pushTarget = new HgURL(FileUtil.normalizeFile(sourceRoot));
            } else {
                notifyDefaultPushUrlInvalid(tmpPushPath, ex.getReason(), logger);
                return;
            }
        }

        final String fromPrjName = HgProjectUtils.getProjectName(root);
        final String toPrjName = pushTarget.isFile()
                                 ? HgProjectUtils.getProjectName(new File(pushTarget.getPath()))
                                 : null;
        performPush(root, pushTarget, fromPrjName, toPrjName, revisionToPush,
                branchToPush, logger, true);

    }

    private static void notifyDefaultPushUrlNotSpecified(OutputLogger logger) {
        String title = getMessage("MSG_PUSH_TITLE");                    //NOI18N

        logger.outputInRed(title);
        logger.outputInRed(getMessage("MSG_PUSH_TITLE_SEP"));           //NOI18N
        logger.output     (getMessage("MSG_NO_DEFAULT_PUSH_SET_MSG"));  //NOI18N
        logger.outputInRed(getMessage("MSG_PUSH_DONE"));                //NOI18N
        logger.output     ("");                                         //NOI18N
        DialogDisplayer.getDefault().notify(
                new DialogDescriptor.Message(
                        getMessage("MSG_NO_DEFAULT_PUSH_SET")));        //NOI18N
    }

    private static void notifyDefaultPushUrlInvalid(String pushUrl,
                                                    String reason,
                                                    OutputLogger logger) {
        String title = getMessage("MSG_PUSH_TITLE");                    //NOI18N
        String msg = getMessage("MSG_DEFAULT_PUSH_INVALID", pushUrl);   //NOI18N

        logger.outputInRed(title);
        logger.outputInRed(getMessage("MSG_PUSH_TITLE_SEP"));           //NOI18N
        logger.output     (msg);
        logger.outputInRed(getMessage("MSG_PUSH_DONE"));                //NOI18N
        logger.output     ("");                                         //NOI18N
        DialogDisplayer.getDefault().notify(
                new DialogDescriptor.Message(msg));
    }

    /**
     *
     * @param root
     * @param pushUrl password is nulled
     * @param fromPrjName
     * @param toPrjName
     * @param logger
     * @param showSaveCredsOption
     */
    @NbBundle.Messages({
        "# {0} - names of new branches", "MSG_PushAction.questionPushNewBranches=Push would create new remote branches: {0}.\n"
            + "Do you still want to continue and push also the local branches?"
    })
    static void performPush(File root, HgURL pushUrl, String fromPrjName,
            String toPrjName, String revision, String branch,
            OutputLogger logger, boolean showSaveCredsOption) {
        try {
            boolean bLocalPush = pushUrl.isFile();
            String pushPath = bLocalPush ? pushUrl.getPath() : null;
            File pushFile = bLocalPush ? new File(pushPath) : null;

            logger.outputInRed(NbBundle.getMessage(PushAction.class, "MSG_PUSH_TITLE")); // NOI18N
            logger.outputInRed(NbBundle.getMessage(PushAction.class, "MSG_PUSH_TITLE_SEP")); // NOI18N
            if (toPrjName == null) {
                logger.outputInRed(
                        NbBundle.getMessage(
                                PushAction.class,
                                "MSG_PUSHING_TO_NONAME",                //NOI18N
                                bLocalPush ? HgUtils.stripDoubleSlash(pushPath)
                                           : pushUrl));
            } else {
                logger.outputInRed(
                        NbBundle.getMessage(
                                PushAction.class,
                                "MSG_PUSHING_TO",
                                toPrjName,
                                bLocalPush ? HgUtils.stripDoubleSlash(pushPath)
                                           : pushUrl));
            }

            List<String> listOutgoing = HgCommand.doOutgoing(root, pushUrl, 
                    revision, branch, logger, showSaveCredsOption);
            if ((listOutgoing == null) || listOutgoing.isEmpty()) {
                return;
            }
            List<HgLogMessage> messages = HgCommand.processLogMessages(root, null, listOutgoing);

            boolean bNoChanges = false;
            for (ListIterator<String> it = listOutgoing.listIterator(listOutgoing.size()); it.hasPrevious(); ) {
                bNoChanges = HgCommand.isNoChanges(it.previous());
                if (bNoChanges) {
                    break;
                }
            }

            if (bLocalPush) {
                // Warn user if there are local changes which Push will overwrite
                if (!bNoChanges && !PullAction.confirmWithLocalChanges(pushFile, PushAction.class,
                        "MSG_PUSH_LOCALMODS_CONFIRM_TITLE", "MSG_PUSH_LOCALMODS_CONFIRM_QUERY", listOutgoing, logger)) { // NOI18N
                    logger.outputInRed(NbBundle.getMessage(PushAction.class, "MSG_PUSH_LOCALMODS_CANCEL")); // NOI18N
                    logger.output(""); // NOI18N
                    return;
                }
            }

            List<String> list;
            HgHookContext context = null;
            Collection<HgHook> hooks = null;
            boolean newHeadsExpected = false;
            if (bNoChanges) {
                list = listOutgoing;
            } else {
                hooks = VCSHooks.getInstance().getHooks(HgHook.class);
                if(hooks.size() > 0) {
                    HgHookContext.LogEntry[] entries = new HgHookContext.LogEntry[messages.size()];
                    for (int i = 0; i < messages.size(); i++) {
                        entries[i] = new HgHookContext.LogEntry(
                                messages.get(i).getMessage(),
                                messages.get(i).getAuthor(),
                                messages.get(i).getCSetShortID(),
                                messages.get(i).getDate());
                    }
                    context = new HgHookContext(new File[] {root}, null, entries);
                }

                for (HgHook hgHook : hooks) {
                    try {
                        // XXX handle returned context
                        hgHook.beforePush(context);
                    } catch (IOException ex) {
                        // XXX handle veto
                    }
                }
                list = HgCommand.doPush(root, pushUrl, revision, branch, branch != null,
                        logger, showSaveCredsOption);
                String newBranches = failedNewBranch(list);
                if (newBranches != null) {
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, 
                            Bundle.MSG_PushAction_questionPushNewBranches(newBranches),
                            NbBundle.getMessage(PushAction.class, "MSG_PUSH_ERROR_TITLE"), //NOI18N
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        newHeadsExpected = true;
                        list = HgCommand.doPush(root, pushUrl, revision, branch,
                                true, logger, showSaveCredsOption);
                    }
                }
                if (!list.isEmpty() && (HgCommand.isErrorAbortPush(list.get(list.size() - 1))
                        || list.size() > 1 && HgCommand.isErrorAbortPush(list.get(list.size() - 2)))) {
                    logger.output(list);
                    logger.output("");
                    HgUtils.warningDialog(PushAction.class,
                            "MSG_PUSH_ERROR_TITLE", "MSG_PUSH_ERROR_QUERY"); // NOI18N
                    logger.outputInRed(NbBundle.getMessage(PushAction.class, "MSG_PUSH_ERROR_CANCELED")); // NOI18N
                    return;
                }
            }

            if(hooks != null && context != null) {
                for (HgHook hgHook : hooks) {
                    hgHook.afterPush(context);
                }
            }

            if (list != null && !list.isEmpty()) {

                if (!bNoChanges) {
                    logger.outputInRed(NbBundle.getMessage(PushAction.class, "MSG_CHANGESETS_TO_PUSH")); // NOI18N
                    if(messages.size() > 0) {
                        for (HgLogMessage m : messages) {
                            HgUtils.logHgLog(m, logger);
                        }
                    } else {
                        for (String s : listOutgoing) {
                            if (s.indexOf(Mercurial.CHANGESET_STR) == 0) {
                                logger.outputInRed(s);
                            } else if (!s.equals("")) { // NOI18N
                                logger.output(HgUtils.replaceHttpPassword(s));
                            }
                        }
                    }
                    logger.output(""); // NOI18N
                }

                logger.output(HgUtils.replaceHttpPassword(list));

                if (toPrjName == null) {
                    logger.outputInRed(
                            NbBundle.getMessage(PushAction.class,
                                    "MSG_PUSH_TO_NONAME",               //NOI18N
                                    bLocalPush ? HgUtils.stripDoubleSlash(pushPath)
                                               : pushUrl));
                } else {
                    logger.outputInRed(
                            NbBundle.getMessage(PushAction.class,
                                    "MSG_PUSH_TO",                      //NOI18N
                                    toPrjName,
                                    bLocalPush ? HgUtils.stripDoubleSlash(pushPath)
                                               : pushUrl));
                }

                if (fromPrjName == null ){
                    logger.outputInRed(
                            NbBundle.getMessage(PushAction.class,
                            "MSG_PUSH_FROM_NONAME", root)); // NOI18N
                } else {
                    logger.outputInRed(
                            NbBundle.getMessage(PushAction.class,
                            "MSG_PUSH_FROM", fromPrjName, root)); // NOI18N
                }

                boolean bMergeNeeded = false;
                if (bLocalPush) {
                    bMergeNeeded = !newHeadsExpected && HgCommand.isHeadsCreated(list.get(list.size() - 1));
                }
                boolean bConfirmMerge = false;
                // Push does not do an Update of the target Working Dir
                if (!bMergeNeeded) {
                    if (bNoChanges) {
                        return;
                    }
                    if (bLocalPush) {
                        list = HgCommand.doUpdateAll(pushFile, false, null, false);
                        logger.output(list);
                        if (toPrjName != null) {
                            logger.outputInRed(
                                    NbBundle.getMessage(PushAction.class,
                                    "MSG_PUSH_UPDATE_DONE", toPrjName, HgUtils.stripDoubleSlash(pushPath))); // NOI18N
                        } else {
                            logger.outputInRed(
                                    NbBundle.getMessage(PushAction.class,
                                    "MSG_PUSH_UPDATE_DONE_NONAME", HgUtils.stripDoubleSlash(pushPath))); // NOI18N
                        }
                        boolean bOutStandingUncommittedMerges = HgCommand.isMergeAbortUncommittedMsg(list.get(list.size() - 1));
                        if (bOutStandingUncommittedMerges) {
                            bConfirmMerge = HgUtils.confirmDialog(PushAction.class, "MSG_PUSH_MERGE_CONFIRM_TITLE", "MSG_PUSH_MERGE_UNCOMMITTED_CONFIRM_QUERY"); // NOI18N
                        } else {
                            HgUtils.notifyUpdatedFiles(pushFile, list);
                        }
                    }

                } else {
                    bConfirmMerge = HgUtils.confirmDialog(PushAction.class, "MSG_PUSH_MERGE_CONFIRM_TITLE", "MSG_PUSH_MERGE_CONFIRM_QUERY"); // NOI18N
                }

                if (bConfirmMerge) {
                    logger.output(""); // NOI18N
                    logger.outputInRed(
                            NbBundle.getMessage(PushAction.class,
                            "MSG_PUSH_MERGE_DO")); // NOI18N
                    MergeAction.doMergeAction(pushFile, null, logger);
                } else {
                    List<String> headRevList = HgCommand.getHeadRevisions(pushPath);
                    if (headRevList != null && headRevList.size() > 1) {
                        MergeAction.printMergeWarning(headRevList, logger);
                    }
                }
            }
            if (bLocalPush && !bNoChanges) {
                HgUtils.forceStatusRefresh(pushFile);
            }
        } catch (HgException.HgCommandCanceledException ex) {
            // canceled by user, do nothing
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        } finally {
            logger.outputInRed(NbBundle.getMessage(PushAction.class, "MSG_PUSH_DONE")); // NOI18N
            logger.output(""); // NOI18N
            pushUrl.clearPassword();
        }
    }

    private static String getMessage(String msgKey, String... args) {
        return NbBundle.getMessage(PushAction.class, msgKey, args);
    }

    private static final String FAILURE_NEW_BRANCHES = "abort: push creates new remote branches:"; //NOI18N
    private static String failedNewBranch (List<String> output) {
        for (String line : output) {
            if (line.startsWith(FAILURE_NEW_BRANCHES)) {
                line = line.substring(FAILURE_NEW_BRANCHES.length());
                if (line.endsWith("!")) {
                    line = line.substring(0, line.length() - 1);
                }
                line = line.trim();
                return line;
            }
        }
        return null;
    }

}
