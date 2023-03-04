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
import org.netbeans.modules.mercurial.ui.queues.QCreatePatchAction;
import org.netbeans.modules.mercurial.ui.queues.QDiffAction;
import org.netbeans.modules.mercurial.ui.queues.QFinishPatchesAction;
import org.netbeans.modules.mercurial.ui.queues.QGoToPatchAction;
import org.netbeans.modules.mercurial.ui.queues.QPopAllAction;
import org.netbeans.modules.mercurial.ui.queues.QPushAllPatchesAction;
import org.netbeans.modules.mercurial.ui.queues.QRefreshPatchAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * Container menu for mqueues actions.
 *
 * @author Ondra Vrabec
 */
@NbBundle.Messages({
    "CTL_MenuItem_QueuesMenu=&Queues",
    "CTL_MenuItem_QueuesMenu.popupName=Queues"
})
public class QueuesMenu extends DynamicMenu implements Presenter.Popup {
    private final Lookup lkp;

    public QueuesMenu (Lookup lkp) {
        super(Bundle.CTL_MenuItem_QueuesMenu());
        this.lkp = lkp;
    }

    @Override
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            org.openide.awt.Mnemonics.setLocalizedText(menu, NbBundle.getMessage(QueuesMenu.class, "CTL_MenuItem_QueuesMenu")); // NOI18N
            item = new JMenuItem();
            Action action = SystemAction.get(QDiffAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(QGoToPatchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(QPopAllAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(QPushAllPatchesAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            
            item = new JMenuItem();
            action = SystemAction.get(QCreatePatchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(QRefreshPatchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(QFinishPatchesAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QDiffAction.class), NbBundle.getMessage(QDiffAction.class, "CTL_PopupMenuItem_QDiff"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QGoToPatchAction.class), NbBundle.getMessage(QGoToPatchAction.class, "CTL_PopupMenuItem_QGoToPatch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QPopAllAction.class), NbBundle.getMessage(QPopAllAction.class, "CTL_PopupMenuItem_QPopAllPatches"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QPushAllPatchesAction.class), NbBundle.getMessage(QPushAllPatchesAction.class, "CTL_PopupMenuItem_QPushAllPatches"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QCreatePatchAction.class), NbBundle.getMessage(QCreatePatchAction.class, "CTL_PopupMenuItem_QCreatePatch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QRefreshPatchAction.class), NbBundle.getMessage(QRefreshPatchAction.class, "CTL_PopupMenuItem_QRefreshPatch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QFinishPatchesAction.class), NbBundle.getMessage(QFinishPatchesAction.class, "CTL_PopupMenuItem_QFinishPatches"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }        
        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = createMenu();
        menu.setText(Bundle.CTL_MenuItem_QueuesMenu_popupName());
        enableMenu(menu);
        return menu;
    }
}
