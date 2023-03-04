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

package org.netbeans.modules.mercurial.ui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.mercurial.MercurialAnnotator;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.ui.rollback.BackoutAction;
import org.netbeans.modules.mercurial.ui.rollback.RollbackAction;
import org.netbeans.modules.mercurial.ui.rollback.StripAction;
import org.netbeans.modules.mercurial.ui.rollback.VerifyAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * Container menu for repository maintenance actions.
 *
 * @author Ondra Vrabec
 */
@NbBundle.Messages({
    "CTL_MenuItem_RecoverMenu=Reco&ver",
    "CTL_MenuItem_RecoverMenu.popupName=Recover"
})
public class RecoverMenu extends DynamicMenu implements Presenter.Popup {

    private final Lookup lkp;
    
    public RecoverMenu (Lookup lkp) {
        super(Bundle.CTL_MenuItem_RecoverMenu());
        this.lkp = lkp;
    }

    @Override
    @NbBundle.Messages({
        "CTL_PopupMenuItem_Strip=Strip...",
        "CTL_PopupMenuItem_Backout=Backout...",
        "CTL_PopupMenuItem_Rollback=Rollback...",
        "CTL_PopupMenuItem_Verify=Verify..."
    })
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            org.openide.awt.Mnemonics.setLocalizedText(menu, Bundle.CTL_MenuItem_RecoverMenu());

            item = new JMenuItem();
            Action action = (Action) SystemAction.get(StripAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            item = new JMenuItem();
            action = (Action) SystemAction.get(BackoutAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            item = new JMenuItem();
            action = (Action) SystemAction.get(RollbackAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            item = new JMenuItem();
            action = (Action) SystemAction.get(VerifyAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(StripAction.class), Bundle.CTL_PopupMenuItem_Strip(), lkp));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(BackoutAction.class), Bundle.CTL_PopupMenuItem_Backout(), lkp));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(RollbackAction.class), Bundle.CTL_PopupMenuItem_Rollback(), lkp));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(VerifyAction.class), Bundle.CTL_PopupMenuItem_Verify(), lkp));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }

        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = createMenu();
        menu.setText(Bundle.CTL_MenuItem_RecoverMenu_popupName());
        enableMenu(menu);
        return menu;
    }
}
