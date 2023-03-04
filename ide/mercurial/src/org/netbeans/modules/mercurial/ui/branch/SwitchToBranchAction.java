/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.mercurial.ui.branch;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.RequestProcessor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.SystemAction;
import static org.netbeans.modules.mercurial.ui.branch.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * 
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.branch.SwitchToBranchAction", category = "Mercurial")
@ActionRegistration(displayName = "#CTL_MenuItem_SwitchBranch")
@Messages({
    "CTL_MenuItem_SwitchBranch=&Switch Branch...",
    "CTL_PopupMenuItem_SwitchToBranch=Switch Branch..."
})
public class SwitchToBranchAction extends ContextAction {
    public static final String PREF_KEY_RECENT_BRANCHES = "recentlySwitchedBranches"; //NOI18N
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_SwitchBranch"; // NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        final VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);

        final BranchSelector switchBranch = new BranchSelector(root);
        JCheckBox forcedUpdateChxBox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(forcedUpdateChxBox, org.openide.util.NbBundle.getMessage(SwitchToBranchAction.class, "SwitchTo.forcedUpdateChxBox.text")); // NOI18N
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.add(forcedUpdateChxBox, BorderLayout.NORTH);
        optionsPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        switchBranch.setOptionsPanel(optionsPanel, null);
        
        JButton okButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(okButton, org.openide.util.NbBundle.getMessage(BranchSelector.class, "CTL_SwitchToForm_Action_SwitchTo")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSD_SwitchToForm_Action_SwitchTo")); // NOI18N
        okButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSN_SwitchToForm_Action_SwitchTo")); // NOI18N
        okButton.setEnabled(false);
        
        if (!switchBranch.showDialog(okButton, org.openide.util.NbBundle.getMessage(BranchSelector.class, "CTL_SwitchToDialog", root.getName()), //NOI18N
                org.openide.util.NbBundle.getMessage(SwitchToBranchAction.class, "SwitchToPanel.infoLabel.text"))) { //NOI18N
            return;
        }
        final String revStr = switchBranch.getBranchName();
        if (revStr == null) {
            return;
        }
        final boolean doForcedUpdate = forcedUpdateChxBox.isSelected();
        doSwitch(root, revStr, doForcedUpdate, ctx); //NOI18N
    }

    public void doSwitch (final File root, final String revStr, final boolean doForcedUpdate, final VCSContext ctx) throws MissingResourceException {
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                final OutputLogger logger = getLogger();
                try {
                    logger.outputInRed(NbBundle.getMessage(SwitchToBranchAction.class, "MSG_SWITCH_TITLE")); //NOI18N
                    logger.outputInRed(NbBundle.getMessage(SwitchToBranchAction.class, "MSG_SWITCH_TITLE_SEP")); //NOI18N
                    logger.output(NbBundle.getMessage(SwitchToBranchAction.class, "MSG_SWITCH_INFO_SEP", revStr, root.getAbsolutePath())); //NOI18N
                    HgUtils.runWithoutIndexing(new Callable<Void>() {
                        @Override
                        public Void call () throws Exception {
                            List<String> list = HgCommand.doUpdateAll(root, doForcedUpdate, revStr);
                            Utils.insert(NbPreferences.forModule(SwitchToBranchAction.class), PREF_KEY_RECENT_BRANCHES + root.getAbsolutePath(), revStr, 5);

                            if (list != null && !list.isEmpty()){
                                // Force Status Refresh from this dir and below
                                if(!HgCommand.isNoUpdates(list.get(0))) {
                                    HgUtils.notifyUpdatedFiles(root, list);
                                    HgUtils.forceStatusRefreshProject(ctx);
                                }
                                //logger.clearOutput();
                                logger.output(list);
                                logger.output(""); // NOI18N
                            }
                            return null;
                        }
                    }, root);
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }

                logger.outputInRed(NbBundle.getMessage(SwitchToBranchAction.class, "MSG_SWITCH_DONE")); // NOI18N
                logger.output(""); // NOI18N
            }
        };
        support.start(rp, root, org.openide.util.NbBundle.getMessage(SwitchToBranchAction.class, "MSG_SwitchBranch_Progress", revStr)); //NOI18N
    }

    public static class KnownBranchAction extends AbstractAction {
        private final VCSContext ctx;
        private final String branchName;

        @Messages({"# {0} - branch name", "SwitchToBranchAction.KnownBranchAction.name=Switch to {0}"})
        public KnownBranchAction (String recentBranch, VCSContext ctx) {
            super(SwitchToBranchAction_KnownBranchAction_name(recentBranch));
            this.branchName = recentBranch;
            this.ctx = ctx;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            final File roots[] = HgUtils.getActionRoots(ctx);
            if (roots != null && roots.length > 0) {
                final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
                SystemAction.get(SwitchToBranchAction.class).doSwitch(root, branchName, false, ctx);
            }
        }
    }
}
