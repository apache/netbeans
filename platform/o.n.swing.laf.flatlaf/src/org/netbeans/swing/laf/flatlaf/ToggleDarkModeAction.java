/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.swing.laf.flatlaf;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.EventQueue;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

@ActionID(
    category = "View",
    id = "org.netbeans.swing.laf.flatlaf.ToggleDarkModeAction"
)
@ActionRegistration(
    displayName = "#CTL_ToggleDarkModeAction",
    lazy = false
)
@ActionReferences({
    @ActionReference(path = "Menu/View", position = 1298),
})
public final class ToggleDarkModeAction extends SystemAction implements DynamicMenuContent {

    private JCheckBoxMenuItem [] menuItems;

    @Override
    public String getName() {
        return NbBundle.getMessage(ToggleDarkModeAction.class, "CTL_ToggleDarkModeAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isEnabled() {
        return UIManager.getLookAndFeel() instanceof FlatLaf;
    }

    @Override
    public JComponent[] getMenuPresenters() {
        createItems();
        updateState();
        return menuItems;
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        updateState();
        return menuItems;
    }

    private void updateState() {
        Runnable r = () -> {
            synchronized (ToggleDarkModeAction.this) {
                createItems();
                menuItems[0].setSelected(FlatLaf.isLafDark());
            }
        };

        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }

    private void createItems() {
        synchronized (this) {
            if (menuItems == null) {
                menuItems = new JCheckBoxMenuItem[1];
                menuItems[0] = new JCheckBoxMenuItem(this);
                menuItems[0].setIcon(null);
                Mnemonics.setLocalizedText(menuItems[0], getName());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // toggle light/dark laf
        boolean isDark = FlatLaf.isLafDark();
        if (isDark)
            FlatLightLaf.setup();
        else
            FlatDarkLaf.setup();

        // remember laf
        NbPreferences.root().node( "laf" ).put( "laf", UIManager.getLookAndFeel().getClass().getName() );

        // update UI
        WindowManager wmgr = Lookup.getDefault().lookup(WindowManager.class);
        wmgr.updateUI();
    }
}
