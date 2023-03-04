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
import org.netbeans.modules.git.ui.commit.ExcludeFromCommitAction;
import org.netbeans.modules.git.ui.commit.IncludeInCommitAction;
import org.netbeans.modules.git.ui.ignore.IgnoreAction;
import org.netbeans.modules.git.ui.ignore.UnignoreAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * Container menu for ignore/exclude actions.
 *
 * @author Ondra Vrabec
 */
@NbBundle.Messages({
    "CTL_MenuItem_IgnoreMenu=&Ignore",
    "CTL_MenuItem_IgnoreMenu.popupName=Ignore"
})
public final class IgnoreMenu extends DynamicMenu {

    private final Lookup lkp;

    public IgnoreMenu (Lookup lkp) {
        super(lkp == null ? Bundle.CTL_MenuItem_IgnoreMenu() : Bundle.CTL_MenuItem_IgnoreMenu_popupName());
        this.lkp = lkp;
    }
    
    @Override
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            Action ia = SystemAction.get(IgnoreAction.class);
            Action uia = SystemAction.get(UnignoreAction.class);
            Action efca = SystemAction.get(ExcludeFromCommitAction.class);
            Action iica = SystemAction.get(IncludeInCommitAction.class);
            
            if (ia.isEnabled()) {
                item = new JMenuItem();
                Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, ia);
                Actions.connect(item, ia, false);
                menu.add(item);
            }
            if (uia.isEnabled()) {
                item = new JMenuItem();
                Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, uia);
                Actions.connect(item, uia, false);
                menu.add(item);
            }
            if (efca.isEnabled()) {
                item = new JMenuItem();
                Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, efca);
                Actions.connect(item, efca, false);
                menu.add(item);
            } else if (iica.isEnabled()) {
                item = new JMenuItem();
                Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, iica);
                Actions.connect(item, iica, false);
                menu.add(item);
            }
        } else {
            SystemActionBridge ia = SystemActionBridge.createAction(SystemAction.get(IgnoreAction.class),
                    NbBundle.getMessage(IgnoreAction.class, "LBL_IgnoreAction_PopupName"), lkp);
            SystemActionBridge uia = SystemActionBridge.createAction(SystemAction.get(UnignoreAction.class),
                    NbBundle.getMessage(UnignoreAction.class, "LBL_UnignoreAction_PopupName"), lkp);
            SystemActionBridge efca = SystemActionBridge.createAction(SystemAction.get(ExcludeFromCommitAction.class),
                    NbBundle.getMessage(ExcludeFromCommitAction.class, "LBL_ExcludeFromCommitAction_PopupName"), lkp);
            SystemActionBridge iica = SystemActionBridge.createAction(SystemAction.get(IncludeInCommitAction.class),
                    NbBundle.getMessage(IncludeInCommitAction.class, "LBL_IncludeInCommitAction_PopupName"), lkp);
            if (ia.isEnabled() || uia.isEnabled() || efca.isEnabled() || iica.isEnabled()) {
                if (ia.isEnabled()) {
                    item = menu.add(ia);
                    org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
                }
                if (uia.isEnabled()) {
                    item = menu.add(uia);
                    org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
                }
                if (efca.isEnabled()) {
                    item = menu.add(efca);
                    org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
                } else if (iica.isEnabled()) {
                    item = menu.add(iica);
                    org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
                }
            }
        }        
        return menu;
    }
}
