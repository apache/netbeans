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

package org.netbeans.modules.subversion.ui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.subversion.Annotator;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.commit.ExcludeFromCommitAction;
import org.netbeans.modules.subversion.ui.ignore.IgnoreAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
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
    private final Node[] nodes;

    public IgnoreMenu (Lookup lkp, Node[] nodes) {
        super(lkp == null ? Bundle.CTL_MenuItem_IgnoreMenu() : Bundle.CTL_MenuItem_IgnoreMenu_popupName());
        this.lkp = lkp;
        this.nodes = nodes;
    }
    
    @Override
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            item = new JMenuItem();
            Action action = SystemAction.get(IgnoreAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(ExcludeFromCommitAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            if (Subversion.getInstance().getStatusCache().ready()) {
                item = menu.add(SystemActionBridge.createAction(SystemAction.get(IgnoreAction.class),
                        SystemAction.get(IgnoreAction.class).getActionStatus(nodes) == IgnoreAction.UNIGNORING
                        ? NbBundle.getMessage(Annotator.class, "CTL_PopupMenuItem_Unignore") //NOI18N
                        : NbBundle.getMessage(Annotator.class, "CTL_PopupMenuItem_Ignore"), lkp)); //NOI18N
                org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
                item = menu.add(SystemActionBridge.createAction(SystemAction.get(ExcludeFromCommitAction.class),
                        SystemAction.get(ExcludeFromCommitAction.class).getActionStatus(nodes) == ExcludeFromCommitAction.INCLUDING
                        ? NbBundle.getMessage(Annotator.class, "CTL_PopupMenuItem_IncludeInCommit") //NOI18N
                        : NbBundle.getMessage(Annotator.class, "CTL_PopupMenuItem_ExcludeFromCommit"), lkp)); //NOI18N
                org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            }
        }        
        return menu;
    }
}
