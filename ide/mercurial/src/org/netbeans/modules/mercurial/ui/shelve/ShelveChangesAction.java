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

package org.netbeans.modules.mercurial.ui.shelve;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.diff.ExportDiffChangesAction;
import org.netbeans.modules.mercurial.ui.diff.Setup;
import org.netbeans.modules.mercurial.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry.ShelveChangesActionProvider;
import org.netbeans.modules.versioning.shelve.ShelveChangesSupport;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Ondra Vrabec
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.shelve.ShelveChangesAction", category = "Mercurial")
@ActionRegistration(displayName = "#CTL_ShelveChanges_Title")
public class ShelveChangesAction extends ContextAction {
    private static ShelveChangesActionProvider ACTION_PROVIDER;

    @Override
    public boolean enable(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        if(!HgUtils.isFromHgRepository(ctx) || !Mercurial.getInstance().getFileStatusCache().containsFileOfStatus(ctx, FileInformation.STATUS_LOCAL_CHANGE, true)) {
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
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        File root = HgUtils.getRootFile(ctx);
        if (root == null) {
            Mercurial.LOG.log(Level.FINE, "No versioned folder in the selected context for {0}", nodes); //NOI18N
            return;
        }
        HgShelveChangesSupport supp = new HgShelveChangesSupport();
        if (supp.open()) {
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
            supp.startAsync(rp, root, ctx);
        }
    }

    private static class HgShelveChangesSupport extends ShelveChangesSupport {
        private HgProgressSupport support;
        private OutputLogger logger;
        private Set<File> filteredRoots;
        private final JPanel optionsPanel;
        private final JCheckBox doBackupChxBox;
        private final JCheckBox doPurgeChxBox;
        private boolean doBackup;
        private boolean doPurge;

        public HgShelveChangesSupport () {
            doBackupChxBox = new JCheckBox();
            org.openide.awt.Mnemonics.setLocalizedText(doBackupChxBox, org.openide.util.NbBundle.getMessage(ShelveChangesAction.class, "ShelvePanel.doBackupChxBox.text")); //NOI18N
            doBackupChxBox.setToolTipText(org.openide.util.NbBundle.getMessage(ShelveChangesAction.class, "ShelvePanel.doBackupChxBox.desc")); //NOI18N
            doBackupChxBox.getAccessibleContext().setAccessibleDescription(doBackupChxBox.getToolTipText());
            doBackupChxBox.setSelected(HgModuleConfig.getDefault().getBackupOnRevertModifications());
            doPurgeChxBox = new JCheckBox();
            org.openide.awt.Mnemonics.setLocalizedText(doPurgeChxBox, org.openide.util.NbBundle.getMessage(ShelveChangesAction.class, "ShelvePanel.doPurgeChxBox.text")); //NOI18N
            doPurgeChxBox.setToolTipText(org.openide.util.NbBundle.getMessage(ShelveChangesAction.class, "ShelvePanel.doPurgeChxBox.desc")); //NOI18N
            doPurgeChxBox.getAccessibleContext().setAccessibleDescription(doPurgeChxBox.getToolTipText());
            doPurgeChxBox.setSelected(HgModuleConfig.getDefault().isRemoveNewFilesOnRevertModifications());
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
            optionsPanel.add(doBackupChxBox);
            optionsPanel.add(doPurgeChxBox);
        }
        
        @Override
        protected void exportPatch (File toFile, File commonParent) throws IOException {
            support.setDisplayName(NbBundle.getMessage(ShelveChangesAction.class, "MSG_ShelveChanges.progress.exporting")); //NOI18N
            List<Setup> setups = new ArrayList<Setup>(filteredRoots.size());
            for (File file : filteredRoots) {
                Setup setup = new Setup(file, null, Setup.DIFFTYPE_LOCAL);
                setups.add(setup);
            }
            SystemAction.get(ExportDiffChangesAction.class).exportDiff(setups, toFile, commonParent, support);
        }

        @Override
        protected void postExportCleanup () {
            Map<File, Set<File>> sorted = HgUtils.sortUnderRepository(filteredRoots);
            for (Map.Entry<File, Set<File>> e : sorted.entrySet()) {
                File root = e.getKey();
                Set<File> roots = e.getValue();
                if (!roots.isEmpty()) {
                    support.setDisplayName(NbBundle.getMessage(ShelveChangesAction.class, "MSG_ShelveChanges.progress.reverting", root.getName())); //NOI18N
                    RevertModificationsAction.performRevert(root, null, roots.toArray(new File[0]), doBackup, doPurge, logger);
                }
            }
        }

        @Override
        protected boolean isCanceled () {
            return support == null ? false : support.isCanceled();
        }
        
        private void startAsync (RequestProcessor rp, File root, final VCSContext context) {
            support = new HgProgressSupport() {
                @Override
                protected void perform () {
                    logger = getLogger();
                    filteredRoots = new HashSet<File>(Arrays.asList(HgUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE, true)));
                    shelveChanges(filteredRoots.toArray(new File[0]));
                }
            };
            support.start(rp, root, NbBundle.getMessage(ShelveChangesAction.class, "LBL_ShelveChanges_Progress")); //NOI18N
        }

        private boolean open () {
            boolean retval = prepare(optionsPanel, "org.netbeans.modules.mercurial.ui.shelve.ShelveChangesPanel"); //NOI18N
            if (retval) {
                doBackup = doBackupChxBox.isSelected();
                doPurge = doPurgeChxBox.isSelected();
                HgModuleConfig.getDefault().setBackupOnRevertModifications(doBackup);
                HgModuleConfig.getDefault().setRemoveNewFilesOnRevertModifications(doPurge);
            }
            return retval;
        }
    };
    
    public static ShelveChangesActionProvider getProvider () {
        if (ACTION_PROVIDER == null) {
            ACTION_PROVIDER = new ShelveChangesActionProvider() {
                @Override
                public Action getAction () {
                    Action a = SystemAction.get(ShelveChangesAction.class);
                    Utils.setAcceleratorBindings("Actions/Mercurial", a); //NOI18N
                    return a;
                }
            };
        };
        return ACTION_PROVIDER;
    }
}
