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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            action = (Action) SystemAction.get(CherryPickAction.class);
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
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(CherryPickAction.class), NbBundle.getMessage(CherryPickAction.class, "LBL_CherryPickAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }        
        return menu;
    }
}
