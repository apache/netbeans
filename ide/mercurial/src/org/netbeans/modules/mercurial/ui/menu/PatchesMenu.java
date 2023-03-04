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
import org.netbeans.modules.mercurial.ui.diff.ApplyDiffPatchAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.ui.diff.ExportBundleAction;
import org.netbeans.modules.mercurial.ui.diff.ExportDiffAction;
import org.netbeans.modules.mercurial.ui.diff.ExportDiffChangesAction;
import org.netbeans.modules.mercurial.ui.diff.ImportDiffAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Container menu for export/import actions.
 *
 * @author Ondra
 */
@NbBundle.Messages({
    "CTL_MenuItem_PatchesMenu=&Patches",
    "CTL_MenuItem_PatchesMenu.popupName=Patches"
})
public final class PatchesMenu extends DynamicMenu implements Presenter.Popup {

    private final Lookup lkp;

    public PatchesMenu (Lookup lkp) {
        super(Bundle.CTL_MenuItem_PatchesMenu());
        this.lkp = lkp;
    }
    
    @Override
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            org.openide.awt.Mnemonics.setLocalizedText(menu, Bundle.CTL_MenuItem_PatchesMenu());
            item = new JMenuItem();
            Action action = (Action) SystemAction.get(ExportDiffChangesAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(ExportDiffAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(ExportBundleAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);

            menu.addSeparator();
            
            item = new JMenuItem();
            action = SystemAction.get(ApplyDiffPatchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(ImportDiffAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ExportDiffAction.class), NbBundle.getMessage(ExportDiffAction.class, "CTL_PopupMenuItem_ExportDiff"), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());

            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ExportDiffChangesAction.class), NbBundle.getMessage(ExportDiffChangesAction.class, "CTL_PopupMenuItem_ExportDiffChanges"), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());

            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ExportBundleAction.class), NbBundle.getMessage(ExportBundleAction.class, "CTL_PopupMenuItem_ExportBundle"), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ApplyDiffPatchAction.class), NbBundle.getMessage(ApplyDiffPatchAction.class, "CTL_PopupMenuItem_ApplyDiffPatch"), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ImportDiffAction.class), NbBundle.getMessage(ImportDiffAction.class, "CTL_PopupMenuItem_ImportDiff"), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
        }        
        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = createMenu();
        menu.setText(Bundle.CTL_MenuItem_PatchesMenu_popupName());
        enableMenu(menu);
        return menu;
    }
}
