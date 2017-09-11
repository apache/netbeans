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
package org.netbeans.modules.mercurial.ui.pull;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDisplayer;
import org.openide.util.RequestProcessor;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.queues.QGoToPatchAction;
import org.netbeans.modules.mercurial.ui.queues.QPatch;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Fetch action for mercurial: 
 * hg fetch - launch hg view to view the dependency tree for the repository
 * Pull changes from a remote repository, merge new changes if needed.
 * This finds all changes from the repository at the specified path
 * or URL and adds them to the local repository.
 * 
 * If the pulled changes add a new head, the head is automatically
 * merged, and the result of the merge is committed.  Otherwise, the
 * working directory is updated.
 * 
 * @author John Rice
 */
@NbBundle.Messages({
    "CTL_MenuItem_FetchLocal=&Fetch",
    "# {0} - repository folder name",
    "CTL_MenuItem_FetchRoot=&Fetch - {0}"
})
@ActionID(id = "org.netbeans.modules.mercurial.ui.pull.FetchAction", category = "Mercurial")
@ActionRegistration(lazy = false, displayName = "#CTL_MenuItem_FetchLocal")
public class FetchAction extends ContextAction {
    
    public static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/fetch.png"; //NOI18N
    
    public FetchAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_FetchLocal";                               //NOI18N
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    public String getName(String role, Node[] activatedNodes) {
        VCSContext ctx = HgUtils.getCurrentContext(activatedNodes);
        Set<File> roots = HgUtils.getRepositoryRoots(ctx);
        String name = roots.size() == 1 ? "CTL_MenuItem_FetchRoot" : "CTL_MenuItem_FetchLocal"; //NOI18N
        return roots.size() == 1 ? NbBundle.getMessage(FetchAction.class, name, roots.iterator().next().getName()) : NbBundle.getMessage(FetchAction.class, name);
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
                    final File root = repositoryRoot;
                    RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                    // run every repository fetch in its own support with its own output window
                    HgProgressSupport support = new HgProgressSupport() {
                        @Override
                        public void perform() {
                            performFetch(root, null, this);
                        }
                    };
                    support.start(rp, root, org.openide.util.NbBundle.getMessage(FetchAction.class, "MSG_FETCH_PROGRESS")).waitFinished(); //NOI18N
                    if (support.isCanceled()) {
                        break;
                    }
                }
            }
        });
    }

    @NbBundle.Messages({
        "MSG_FetchAction.popingPatches=Popping applied patches",
        "MSG_FetchAction.fetching=Fetching changesets",
        "MSG_FetchAction.pushingPatches=Pushing patches"
    })
    public static void performFetch(final File root, final String revision, final HgProgressSupport supp) {
        final OutputLogger logger = supp.getLogger();
        HgURL pullSource = null;
        try {
            final QPatch topPatch = selectPatch(root);
            logger.outputInRed(NbBundle.getMessage(FetchAction.class, "MSG_FETCH_TITLE")); // NOI18N
            logger.outputInRed(NbBundle.getMessage(FetchAction.class, "MSG_FETCH_TITLE_SEP")); // NOI18N
            
            HgConfigFiles config = new HgConfigFiles(root);
            final String pullSourceString = config.getDefaultPull(true);
            // If the repository has no default pull path then inform user
            if (HgUtils.isNullOrEmpty(pullSourceString)) {
                notifyDefaultPullUrlNotSpecified(logger);
                return;
            }
            
            boolean enableFetch = !config.containsProperty(HgConfigFiles.HG_EXTENSIONS, HgConfigFiles.HG_EXTENSIONS_FETCH);
            if (enableFetch) {
                HgConfigFiles sysConfig = HgConfigFiles.getSysInstance();
                sysConfig.doReload();
                enableFetch = !sysConfig.containsProperty(HgConfigFiles.HG_EXTENSIONS, HgConfigFiles.HG_EXTENSIONS_FETCH);
            }

            logger.outputInRed(NbBundle.getMessage(FetchAction.class, 
                    "MSG_FETCH_LAUNCH_INFO", root.getAbsolutePath())); // NOI18N
            try {
                pullSource = new HgURL(pullSourceString);
            } catch (URISyntaxException ex) {
                File sourceRoot = new File(root, pullSourceString);
                if (sourceRoot.isDirectory()) {
                    pullSource = new HgURL(FileUtil.normalizeFile(sourceRoot));
                } else {
                    String msg = NbBundle.getMessage(FetchAction.class, "MSG_DEFAULT_PULL_INVALID", pullSourceString); //NOI18N
                    DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(msg));
                    Mercurial.LOG.log(Level.INFO, null, ex);
                    return;
                }
            }

            final HgURL from = pullSource;
            final boolean enableExtension = enableFetch;
            HgUtils.runWithoutIndexing(new Callable<Void>() {
                @Override
                public Void call () throws Exception {
                    if (topPatch != null) {
                        supp.setDisplayName(Bundle.MSG_FetchAction_popingPatches());
                        SystemAction.get(QGoToPatchAction.class).popAllPatches(root, logger);
                        supp.setDisplayName(Bundle.MSG_FetchAction_fetching());
                        logger.output(""); // NOI18N
                    }
                    if (supp.isCanceled()) {
                        return null;
                    }
                    List<String> list = HgCommand.doFetch(root, from, revision, enableExtension, logger);
                    if (!supp.isCanceled() && list != null && !list.isEmpty()) {
                        logger.output(HgUtils.replaceHttpPassword(list));
                        if (MergeAction.handleMergeOutput(root, list, logger, true) && topPatch != null) {
                            logger.output(""); // NOI18N
                            supp.setDisplayName(Bundle.MSG_FetchAction_pushingPatches());
                            SystemAction.get(QGoToPatchAction.class).applyPatch(root, topPatch.getId(), logger);
                            HgLogMessage parent = HgCommand.getParents(root, null, null).get(0);
                            logger.output(""); // NOI18N
                            HgUtils.logHgLog(parent, logger);
                        }
                        Mercurial.getInstance().historyChanged(root);
                        HgUtils.notifyUpdatedFiles(root, list);
                        HgUtils.forceStatusRefresh(root);
                    }
                    return null;
                }
            }, root);

        } catch (HgException.HgCommandCanceledException ex) {
            // canceled by user, do nothing
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        } finally {
            logger.outputInRed(NbBundle.getMessage(FetchAction.class, "MSG_FETCH_DONE")); // NOI18N
            logger.output(""); // NOI18N
            if (pullSource != null) {
                pullSource.clearPassword();
            }
        }
    }

    @NbBundle.Messages({
        "LBL_FetchAction.appliedPatches.title=Mercurial Patches applied",
        "MSG_FetchAction.appliedPatches.text=You have applied patches from the active Mercurial queue.\n\n"
            + "Do you want to pop them first before the operation\nstarts and apply them right after it finishes?"
    })
    static QPatch selectPatch (File root) throws HgException {
        QPatch[] patches = HgCommand.qListSeries(root);
        QPatch topPatch = null;
        for (QPatch patch : patches) {
            if (patch.isApplied()) {
                topPatch = patch;
            }
        }
        if (topPatch != null) {
            Object conf = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                    Bundle.MSG_FetchAction_appliedPatches_text(), 
                    Bundle.LBL_FetchAction_appliedPatches_title(),
                    NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE));
            if (conf == NotifyDescriptor.CANCEL_OPTION) {
                throw new HgException.HgCommandCanceledException("Canceled"); //NOI18N
            } else if (conf == NotifyDescriptor.NO_OPTION) {
                topPatch = null;
            }
        }
        return topPatch;
    }
    
    private static void notifyDefaultPullUrlNotSpecified(OutputLogger logger) {
        logger.output(NbBundle.getMessage(FetchAction.class, "MSG_NO_DEFAULT_PULL_SET_MSG")); //NOI18N
        logger.output(""); //NOI18N
        DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(NbBundle.getMessage(FetchAction.class, "MSG_NO_DEFAULT_PULL_SET"))); //NOI18N
    }
}
