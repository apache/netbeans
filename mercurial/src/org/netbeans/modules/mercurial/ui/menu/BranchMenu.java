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

package org.netbeans.modules.mercurial.ui.menu;

import java.io.File;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.MercurialAnnotator;
import org.netbeans.modules.mercurial.ui.branch.CloseBranchAction;
import org.netbeans.modules.mercurial.ui.branch.CreateBranchAction;
import org.netbeans.modules.mercurial.ui.branch.SwitchToBranchAction;
import org.netbeans.modules.mercurial.ui.rebase.RebaseAction;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;
import static org.netbeans.modules.mercurial.ui.menu.Bundle.*;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.tag.CreateTagAction;
import org.netbeans.modules.mercurial.ui.tag.ManageTagsAction;

/**
 * Container menu for branch actions.
 *
 * @author Maros Sandor
 */
@NbBundle.Messages({
    "CTL_MenuItem_BranchMenu=&Branch/Tag",
    "CTL_MenuItem_BranchMenu.popupName=Branch/Tag"
})
public class BranchMenu extends DynamicMenu implements Presenter.Popup {
    private final Lookup lkp;
    private final VCSContext ctx;

    public BranchMenu (Lookup lkp) {
        this(lkp, null);
    }

    public BranchMenu (Lookup lkp, VCSContext ctx) {
        super(Bundle.CTL_MenuItem_BranchMenu());
        this.lkp = lkp;
        this.ctx = ctx;
    }

    @Override
    @NbBundle.Messages({
        "CTL_PopupMenuItem_Rebase=Rebase..."
    })
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            org.openide.awt.Mnemonics.setLocalizedText(menu, NbBundle.getMessage(BranchMenu.class, "CTL_MenuItem_BranchMenu")); // NOI18N
            item = new JMenuItem();
            Action action = (Action) SystemAction.get(CreateBranchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(SwitchToBranchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);

            item = new JMenuItem();
            action = (Action) SystemAction.get(CloseBranchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);

            menu.addSeparator();
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(CreateTagAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(ManageTagsAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            
            item = new JMenuItem();
            action = SystemAction.get(MergeAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(RebaseAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(CreateBranchAction.class), NbBundle.getMessage(CreateBranchAction.class, "CTL_PopupMenuItem_CreateBranch"), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(SwitchToBranchAction.class), NbBundle.getMessage(SwitchToBranchAction.class, "CTL_PopupMenuItem_SwitchToBranch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            if (ctx != null) {
                File repositoryRoot = null;
                for (File f : ctx.getRootFiles()) {
                    repositoryRoot = Mercurial.getInstance().getRepositoryRoot(f);
                    if (repositoryRoot != null) {
                        break;
                    }
                }
                if (repositoryRoot != null) {
                    List<String> recentlySwitched = Utils.getStringList(NbPreferences.forModule(BranchMenu.class), SwitchToBranchAction.PREF_KEY_RECENT_BRANCHES + repositoryRoot.getAbsolutePath());
                    if (!recentlySwitched.isEmpty()) {
                        int index = 0;
                        for (String recentBranch : recentlySwitched) {
                            menu.add(new SwitchToBranchAction.KnownBranchAction(recentBranch, ctx));
                            if (++index > 2) {
                                break;
                            }
                        }
                        menu.addSeparator();
                    }
                }
            }
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(CloseBranchAction.class), NbBundle.getMessage(CloseBranchAction.class, "CTL_PopupMenuItem_CloseBranch"), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(CreateTagAction.class), NbBundle.getMessage(CreateTagAction.class, "CTL_PopupMenuItem_CreateTag"), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ManageTagsAction.class), NbBundle.getMessage(ManageTagsAction.class, "CTL_PopupMenuItem_ManageTags"), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(MergeAction.class), NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_Merge"),lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(RebaseAction.class), CTL_PopupMenuItem_Rebase(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }        
        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = createMenu();
        menu.setText(Bundle.CTL_MenuItem_BranchMenu_popupName());
        enableMenu(menu);
        return menu;
    }
}
