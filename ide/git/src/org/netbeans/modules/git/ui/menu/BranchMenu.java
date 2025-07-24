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

package org.netbeans.modules.git.ui.menu;

import java.io.File;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.Annotator;
import org.netbeans.modules.git.ui.branch.CherryPickAction;
import org.netbeans.modules.git.ui.branch.CreateBranchAction;
import org.netbeans.modules.git.ui.branch.RenameBranchAction;
import org.netbeans.modules.git.ui.branch.DeleteBranchAction;
import org.netbeans.modules.git.ui.branch.SetTrackingAction;
import org.netbeans.modules.git.ui.checkout.AbstractCheckoutAction;
import org.netbeans.modules.git.ui.checkout.SwitchBranchAction;
import org.netbeans.modules.git.ui.merge.MergeRevisionAction;
import org.netbeans.modules.git.ui.rebase.RebaseAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.tag.CreateTagAction;
import org.netbeans.modules.git.ui.tag.ManageTagsAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.SystemAction;

/**
 * Container menu for export actions.
 *
 * @author Ondra
 */
public final class BranchMenu extends DynamicMenu {
    private final ActionDestination dest;
    private final Lookup lkp;
    private final VCSContext ctx;

    @NbBundle.Messages({
        "CTL_MenuItem_BranchMenu=&Branch/Tag",
        "CTL_MenuItem_BranchMenu.popup=Branch/Tag"
    })
    public BranchMenu (ActionDestination dest, Lookup lkp, VCSContext ctx) {
        super(dest.equals(ActionDestination.MainMenu) ? Bundle.CTL_MenuItem_BranchMenu() : Bundle.CTL_MenuItem_BranchMenu_popup());
        this.dest = dest;
        this.lkp = lkp;
        this.ctx = ctx;
    }

    @Override
    protected JMenu createMenu () {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (dest.equals(ActionDestination.MainMenu)) {
            item = new JMenuItem();
            Action action = (Action) SystemAction.get(CreateBranchAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(SwitchBranchAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(SetTrackingAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            item = new JMenuItem();
            action = (Action) SystemAction.get(CreateTagAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);

            item = new JMenuItem();
            action = (Action) SystemAction.get(ManageTagsAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            item = new JMenuItem();
            action = (Action) SystemAction.get(MergeRevisionAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(RebaseAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);

            item = new JMenuItem();
            action = (Action) SystemAction.get(RenameBranchAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);

            item = new JMenuItem();
            action = (Action) SystemAction.get(CherryPickAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(DeleteBranchAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(CreateBranchAction.class), NbBundle.getMessage(CreateBranchAction.class, "LBL_CreateBranchAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(SwitchBranchAction.class), NbBundle.getMessage(SwitchBranchAction.class, "LBL_SwitchBranchAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            if (ctx != null) {
                File repositoryRoot = null;
                Set<File> repositoryRoots = GitUtils.getRepositoryRoots(ctx);
                if (repositoryRoots.size() == 1) {
                    repositoryRoot = repositoryRoots.iterator().next();
                }
                if (repositoryRoot != null) {
                    RepositoryInfo info = RepositoryInfo.getInstance(repositoryRoot);
                    GitBranch branch = info.getActiveBranch();
                    List<String> recentlySwitched = Utils.getStringList(NbPreferences.forModule(BranchMenu.class), AbstractCheckoutAction.PREF_KEY_RECENT_BRANCHES + repositoryRoot.getAbsolutePath());
                    int index = 0;
                    for (String recentBranch : recentlySwitched) {
                        if (recentBranch.equals(branch.getName())) {
                            continue;
                        }
                        menu.add(new SwitchBranchAction.KnownBranchAction(recentBranch, ctx));
                        if (++index > 2) {
                            break;
                        }
                    }
                }
            }
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(SetTrackingAction.class), NbBundle.getMessage(SetTrackingAction.class, "LBL_SetTrackingAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(CreateTagAction.class), NbBundle.getMessage(CreateTagAction.class, "LBL_CreateTagAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ManageTagsAction.class), NbBundle.getMessage(ManageTagsAction.class, "LBL_ManageTagsAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(MergeRevisionAction.class), NbBundle.getMessage(MergeRevisionAction.class, "LBL_MergeRevisionAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(RebaseAction.class), NbBundle.getMessage(RebaseAction.class, "LBL_RebaseAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(RenameBranchAction.class), NbBundle.getMessage(RenameBranchAction.class, "LBL_RenameBranchAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(CherryPickAction.class), NbBundle.getMessage(CherryPickAction.class, "LBL_CherryPickAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(DeleteBranchAction.class), NbBundle.getMessage(DeleteBranchAction.class, "LBL_DeleteBranchAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }        
        return menu;
    }
}
