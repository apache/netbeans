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

package org.netbeans.modules.git.ui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.git.Annotator;
import org.netbeans.modules.git.ui.diff.ExportCommitAction;
import org.netbeans.modules.git.ui.diff.ExportUncommittedChangesAction;
import org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Container menu for export actions.
 *
 * @author Ondra
 */
public final class PatchesMenu extends DynamicMenu {
    private final ActionDestination dest;
    private final Lookup lkp;

    @NbBundle.Messages({
        "CTL_MenuItem_ExportMenu=&Patches",
        "CTL_MenuItem_ExportMenu.popup=Patches",
        "CTL_PopupName.PatchAction=Apply Diff Patch..."
    })
    public PatchesMenu (ActionDestination dest, Lookup lkp) {
        super(dest.equals(ActionDestination.MainMenu) 
                ? Bundle.CTL_MenuItem_ExportMenu()
                : Bundle.CTL_MenuItem_ExportMenu_popup());
        this.dest = dest;
        this.lkp = lkp;
    }

    @Override
    protected JMenu createMenu () {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        Action patchAction = getPatchAction();
        if (dest.equals(ActionDestination.MainMenu)) {
            item = new JMenuItem();
            Action action = (Action) SystemAction.get(ExportUncommittedChangesAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(ExportCommitAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            if (patchAction != null) {
                menu.addSeparator();
                item = new JMenuItem();
                Actions.connect(item, patchAction, false);
                menu.add(item);
            }
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ExportUncommittedChangesAction.class), NbBundle.getMessage(ExportUncommittedChangesAction.class, "LBL_ExportUncommittedChangesAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ExportCommitAction.class), NbBundle.getMessage(ExportCommitAction.class, "LBL_ExportCommitAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            if (patchAction != null) {
                menu.addSeparator();
                item = menu.add(SystemActionBridge.createAction(patchAction, Bundle.CTL_PopupName_PatchAction(), lkp));
                org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            }
        }        
        return menu;
    }

    private Action getPatchAction () {
        return Actions.forID("Tools", "org.netbeans.modules.diff.PatchAction"); //NOI18N
    }
}
