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

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.mercurial.MercurialAnnotator;
import org.netbeans.modules.mercurial.ui.clone.CloneAction;
import org.netbeans.modules.mercurial.ui.log.IncomingAction;
import org.netbeans.modules.mercurial.ui.log.OutAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.netbeans.modules.mercurial.ui.pull.FetchAction;
import org.netbeans.modules.mercurial.ui.pull.PullAction;
import org.netbeans.modules.mercurial.ui.pull.PullCurrentBranchAction;
import org.netbeans.modules.mercurial.ui.pull.PullOtherAction;
import org.netbeans.modules.mercurial.ui.push.PushAction;
import org.netbeans.modules.mercurial.ui.push.PushCurrentBranchAction;
import org.netbeans.modules.mercurial.ui.push.PushOtherAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;

/**
 * Container menu for remote repository actions.
 *
 * @author Maros Sandor
 */
@NbBundle.Messages({
    "CTL_MenuItem_RemoteMenu=R&emote",
    "CTL_MenuItem_RemoteMenu.popupName=Remote"
})
public class RemoteMenu extends DynamicMenu implements Presenter.Popup {
    private final Lookup lkp;

    public RemoteMenu (Lookup lkp) {
        super(Bundle.CTL_MenuItem_RemoteMenu());
        this.lkp = lkp;
    }

    @Override
    @NbBundle.Messages({
        "CTL_PopupMenuItem_Clone=Clone",
        "CTL_PopupMenuItem_Fetch=Fetch",
        "CTL_PopupMenuItem_PushOther=Push...",
        "CTL_PopupMenuItem_Push=Push All Branches",
        "CTL_PopupMenuItem_PushBranch=Push Current Branch",
        "CTL_PopupMenuItem_PullOther=Pull...",
        "CTL_PopupMenuItem_Pull=Pull All Branches",
        "CTL_PopupMenuItem_PullBranch=Pull Current Branch",
        "CTL_PopupMenuItem_ShowIncoming=Show Incoming",
        "CTL_PopupMenuItem_ShowOut=Show Outgoing"
    })
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            org.openide.awt.Mnemonics.setLocalizedText(menu, Bundle.CTL_MenuItem_RemoteMenu());
            
            Action action = Utils.getAcceleratedAction("Actions/Mercurial/org-netbeans-modules-mercurial-ui-clone-CloneExternalAction.instance"); //NOI18N
            if (action != null) {
                item = new JMenuItem();
                Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
                Actions.connect(item, action, false);
                menu.add(item);
            }
            item = new JMenuItem();
            action = SystemAction.get(CloneAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            item = new JMenuItem();
            action = SystemAction.get(PullCurrentBranchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            item = new JMenuItem();
            action = SystemAction.get(PullAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            item = new JMenuItem();
            action = SystemAction.get(PullOtherAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            item = new JMenuItem();
            action = SystemAction.get(PushCurrentBranchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            item = new JMenuItem();
            action = SystemAction.get(PushAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            item = new JMenuItem();
            action = SystemAction.get(PushOtherAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            item = new JMenuItem();
            action = SystemAction.get(FetchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            item = new JMenuItem();
            action = SystemAction.get(IncomingAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            item = new JMenuItem();
            action = SystemAction.get(OutAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(CloneAction.class), Bundle.CTL_PopupMenuItem_Clone(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PullCurrentBranchAction.class), Bundle.CTL_PopupMenuItem_PullBranch(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PullAction.class), Bundle.CTL_PopupMenuItem_Pull(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PullOtherAction.class), Bundle.CTL_PopupMenuItem_PullOther(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PushCurrentBranchAction.class), Bundle.CTL_PopupMenuItem_PushBranch(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PushAction.class), Bundle.CTL_PopupMenuItem_Push(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PushOtherAction.class), Bundle.CTL_PopupMenuItem_PushOther(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(FetchAction.class), Bundle.CTL_PopupMenuItem_Fetch(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(IncomingAction.class), Bundle.CTL_PopupMenuItem_ShowIncoming(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(OutAction.class), Bundle.CTL_PopupMenuItem_ShowOut(), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }        
        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = createMenu();
        menu.setText(Bundle.CTL_MenuItem_RemoteMenu_popupName());
        enableMenu(menu);
        return menu;
    }
}
