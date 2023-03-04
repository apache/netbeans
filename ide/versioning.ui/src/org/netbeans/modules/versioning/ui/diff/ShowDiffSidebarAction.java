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
package org.netbeans.modules.versioning.ui.diff;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.awt.Mnemonics;
import org.openide.awt.DynamicMenuContent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;

/**
 * View/Show Diff Sidebar toggle item in main menu.
 * 
 * @author Maros Sandor
 */
public class ShowDiffSidebarAction extends SystemAction implements DynamicMenuContent {

    private JCheckBoxMenuItem [] menuItems;

    public JComponent[] getMenuPresenters() {
        createItems();
        updateState();
        return menuItems;
    }

    public JComponent[] synchMenuPresenters(JComponent[] items) {
        updateState();
        return items;
    }

    private void updateState() {
        menuItems[0].setSelected(DiffSidebarManager.getInstance().getPreferences().getBoolean(DiffSidebarManager.SIDEBAR_ENABLED, true));
    }

    private void createItems() {
        if (menuItems == null) {
            menuItems = new JCheckBoxMenuItem[1];
            menuItems[0] = new JCheckBoxMenuItem(this);
            menuItems[0].setIcon(null);
            Mnemonics.setLocalizedText(menuItems[0], NbBundle.getMessage(ShowDiffSidebarAction.class, "CTL_ShowDiffSidebar"));
        }
    }

    public String getName() {
        return NbBundle.getMessage(ShowDiffSidebarAction.class, "CTL_ShowDiffSidebar");
    }

    public boolean isEnabled() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShowDiffSidebarAction.class);
    }

    public void actionPerformed(ActionEvent e) {
        Preferences prefs = DiffSidebarManager.getInstance().getPreferences();
        prefs.putBoolean(DiffSidebarManager.SIDEBAR_ENABLED, !prefs.getBoolean(DiffSidebarManager.SIDEBAR_ENABLED, true));
    }
}
