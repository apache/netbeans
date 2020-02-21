/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial.remote.ui.shelve;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry.ShelveChangesActionProvider;
import org.netbeans.modules.versioning.shelve.ShelveChangesSupport;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * 
 */
@ActionID(id = "org.netbeans.modules.mercurial.remote.ui.shelve.ShelveChangesAction", category = "MercurialRemote")
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
        VCSFileProxy root = HgUtils.getRootFile(ctx);
        if (root == null) {
            Mercurial.LOG.log(Level.FINE, "No versioned folder in the selected context for {0}", nodes); //NOI18N
            return;
        }
        HgShelveChangesSupport supp = new HgShelveChangesSupport(root);
        if (supp.open()) {
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
            supp.startAsync(rp, root, ctx);
        }
    }

    private static class HgShelveChangesSupport extends ShelveChangesSupport {
        private HgProgressSupport support;
        private OutputLogger logger;
        private Set<VCSFileProxy> filteredRoots;
        private final JPanel optionsPanel;
        private final JCheckBox doBackupChxBox;
        private final JCheckBox doPurgeChxBox;
        private boolean doBackup;
        private boolean doPurge;
        private final VCSFileProxy root;

        public HgShelveChangesSupport (VCSFileProxy root) {
            this.root = root;
            doBackupChxBox = new JCheckBox();
            org.openide.awt.Mnemonics.setLocalizedText(doBackupChxBox, org.openide.util.NbBundle.getMessage(ShelveChangesAction.class, "ShelvePanel.doBackupChxBox.text")); //NOI18N
            doBackupChxBox.setToolTipText(org.openide.util.NbBundle.getMessage(ShelveChangesAction.class, "ShelvePanel.doBackupChxBox.desc")); //NOI18N
            doBackupChxBox.getAccessibleContext().setAccessibleDescription(doBackupChxBox.getToolTipText());
            doBackupChxBox.setSelected(HgModuleConfig.getDefault(root).getBackupOnRevertModifications());
            doPurgeChxBox = new JCheckBox();
            org.openide.awt.Mnemonics.setLocalizedText(doPurgeChxBox, org.openide.util.NbBundle.getMessage(ShelveChangesAction.class, "ShelvePanel.doPurgeChxBox.text")); //NOI18N
            doPurgeChxBox.setToolTipText(org.openide.util.NbBundle.getMessage(ShelveChangesAction.class, "ShelvePanel.doPurgeChxBox.desc")); //NOI18N
            doPurgeChxBox.getAccessibleContext().setAccessibleDescription(doPurgeChxBox.getToolTipText());
            doPurgeChxBox.setSelected(HgModuleConfig.getDefault(root).isRemoveNewFilesOnRevertModifications());
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
            optionsPanel.add(doBackupChxBox);
            optionsPanel.add(doPurgeChxBox);
        }
        
//        @Override
//        protected void exportPatch (VCSFileProxy toFile, VCSFileProxy commonParent) throws IOException {
//            support.setDisplayName(NbBundle.getMessage(ShelveChangesAction.class, "MSG_ShelveChanges.progress.exporting")); //NOI18N
//            List<Setup> setups = new ArrayList<Setup>(filteredRoots.size());
//            for (VCSFileProxy file : filteredRoots) {
//                Setup setup = new Setup(file, null, Setup.DIFFTYPE_LOCAL);
//                setups.add(setup);
//            }
//            SystemAction.get(ExportDiffChangesAction.class).exportDiff(setups, toFile, commonParent, support);
//        }

        @Override
        protected void exportPatch(File toFile, File commonParent) throws IOException {
            //TODO: bug #249105
            throw new UnsupportedOperationException();
        }

        @Override
        protected void postExportCleanup () {
            Map<VCSFileProxy, Set<VCSFileProxy>> sorted = HgUtils.sortUnderRepository(filteredRoots);
            for (Map.Entry<VCSFileProxy, Set<VCSFileProxy>> e : sorted.entrySet()) {
                VCSFileProxy root = e.getKey();
                Set<VCSFileProxy> roots = e.getValue();
                if (!roots.isEmpty()) {
                    support.setDisplayName(NbBundle.getMessage(ShelveChangesAction.class, "MSG_ShelveChanges.progress.reverting", root.getName())); //NOI18N
                    RevertModificationsAction.performRevert(root, null, roots.toArray(new VCSFileProxy[roots.size()]), doBackup, doPurge, logger);
                }
            }
        }

        @Override
        protected boolean isCanceled () {
            return support == null ? false : support.isCanceled();
        }
        
        private void startAsync (RequestProcessor rp, VCSFileProxy root, final VCSContext context) {
            support = new HgProgressSupport() {
                @Override
                protected void perform () {
                    logger = getLogger();
                    filteredRoots = new HashSet<>(Arrays.asList(HgUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE, true)));
                    //shelveChanges(filteredRoots.toArray(new VCSFileProxy[filteredRoots.size()]));
                }
            };
            support.start(rp, root, NbBundle.getMessage(ShelveChangesAction.class, "LBL_ShelveChanges_Progress")); //NOI18N
        }

        private boolean open () {
            boolean retval = prepare(optionsPanel, "org.netbeans.modules.mercurial.remote.ui.shelve.ShelveChangesPanel"); //NOI18N
            if (retval) {
                doBackup = doBackupChxBox.isSelected();
                doPurge = doPurgeChxBox.isSelected();
                HgModuleConfig.getDefault(root).setBackupOnRevertModifications(doBackup);
                HgModuleConfig.getDefault(root).setRemoveNewFilesOnRevertModifications(doPurge);
            }
            return retval;
        }
    };
    
    public static synchronized ShelveChangesActionProvider getProvider () {
        if (ACTION_PROVIDER == null) {
            ACTION_PROVIDER = new ShelveChangesActionProvider() {
                @Override
                public Action getAction () {
                    Action a = SystemAction.get(ShelveChangesAction.class);
                    Utils.setAcceleratorBindings("Actions/MercurialRemote", a); //NOI18N
                    return a;
                }
            };
        }
        return ACTION_PROVIDER;
    }
}
