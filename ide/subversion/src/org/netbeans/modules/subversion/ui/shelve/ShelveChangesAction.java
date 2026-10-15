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

package org.netbeans.modules.subversion.ui.shelve;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.Action;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.OutputLogger;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.ui.diff.ExportDiffAction;
import org.netbeans.modules.subversion.ui.diff.Setup;
import org.netbeans.modules.subversion.ui.update.RevertModificationsAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry;
import org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry.ShelveChangesActionProvider;
import org.netbeans.modules.versioning.shelve.ShelveChangesSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Ondra Vrabec
 */
@ActionID(id = "org.netbeans.modules.subversion.ui.shelve.ShelveChangesAction", category = "Subversion")
@ActionRegistration(displayName = "#CTL_ShelveChanges_Title")
public class ShelveChangesAction extends ContextAction {

    private static final int enabledForStatus =
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;
    private static ShelveChangesActionProvider ACTION_PROVIDER;
    
    @Override
    public boolean enable(Node[] nodes) {
        if (!Subversion.getInstance().getStatusCache().ready()) {
            return false;
        }
        Context ctx = getCachedContext(nodes);
        if(!Subversion.getInstance().getStatusCache().containsFiles(ctx, enabledForStatus, true)) {
            return false;
        }
        return super.enable(nodes);
    }

    @Override
    protected String getBaseName (Node[] activatedNodes) {
        return "CTL_ShelveChanges_Title"; //NOI18N
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        Context ctx = getContext(nodes);
        final File[] roots = ctx.getRootFiles();
        if (roots == null || roots.length == 0) {
            Subversion.LOG.log(Level.FINE, "No versioned folder in the selected context for {0}", nodes); //NOI18N
            return;
        }

        File root = roots[0];

        SVNUrl repositoryUrl = null;
        try {
            repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        if(repositoryUrl == null) {
            Subversion.LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{ root }); //NOI18N
            return;
        }
        SvnShelveChangesSupport supp = new SvnShelveChangesSupport(roots);
        if (supp.prepare("org.netbeans.modules.subversion.ui.shelve.ShelveChangesPanel")) { //NOI18N
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
            supp.startAsync(rp, repositoryUrl);
        }
    }

    private static class SvnShelveChangesSupport extends ShelveChangesSupport {
        private SvnProgressSupport support;
        private final File[] roots;
        private File[] filteredRoots;
        private Context context;
        private OutputLogger logger;

        public SvnShelveChangesSupport (File[] roots) {
            this.roots = roots;
        }

        @Override
        protected void exportPatch (File toFile, File commonParent) throws IOException {
            support.setDisplayName(NbBundle.getMessage(ShelveChangesAction.class, "MSG_ShelveChanges.progress.exporting")); //NOI18N
            File [] files = SvnUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
            List<Setup> setups = new ArrayList<Setup>(files.length);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                Setup setup = new Setup(file, null, Setup.DIFFTYPE_LOCAL);
                setups.add(setup);
            }
            SystemAction.get(ExportDiffAction.class).exportDiff(setups, toFile, commonParent, support);
            logger.logMessage(NbBundle.getMessage(ShelveChangesAction.class, "MSG_ShelveChangesAction.output.exporting", toFile.getAbsolutePath())); //NOI18N
        }

        @Override
        protected void postExportCleanup () {
            support.setDisplayName(NbBundle.getMessage(ShelveChangesAction.class, "MSG_ShelveChanges.progress.reverting")); //NOI18N
            RevertModificationsAction.performRevert(null, true, false, context, support);
        }

        @Override
        protected boolean isCanceled () {
            return support == null ? false : support.isCanceled();
        }
        
        private void startAsync (RequestProcessor rp, SVNUrl repositoryUrl) {
            support = new SvnProgressSupport() {
                @Override
                protected void perform () {
                    // filter managed roots
                    List<File> l = new LinkedList<File>();
                    for (File file : roots) {
                        if(SvnUtils.isManaged(file)) {
                            l.add(file);
                        }
                    }
                    if (!l.isEmpty()) {
                        filteredRoots = l.toArray(new File[0]);
                        context = new Context(filteredRoots);
                        logger = getLogger();
                        shelveChanges(filteredRoots);
                    }
                }
            };
            support.start(rp, repositoryUrl, NbBundle.getMessage(ShelveChangesAction.class, "LBL_ShelveChanges_Progress")); //NOI18N
        }
    };

    public static ShelveChangesActionsRegistry.ShelveChangesActionProvider getProvider () {
        if (ACTION_PROVIDER == null) {
            ACTION_PROVIDER = new ShelveChangesActionsRegistry.ShelveChangesActionProvider() {
                @Override
                public Action getAction () {
                    Action a = SystemAction.get(ShelveChangesAction.class);
                    Utils.setAcceleratorBindings("Actions/Subversion", a); //NOI18N
                    return a;
                }
            };
        };
        return ACTION_PROVIDER;
    }
}
