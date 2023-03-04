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

package org.netbeans.modules.project.ui;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.SystemAction;

/**
 * Action for enabling/disabling synchronized update of Projects and Files views
 * according to file selected in editor
 *
 * @author Milan Kubec
 */
@ActionID(id = "org.netbeans.modules.project.ui.SyncEditorWithViewsAction", category = "Project")
@ActionRegistration(displayName = "#CTL_SYNC_EDITOR_WITH_VIEWS", lazy=false)
@ActionReference(path = "Menu/View", position = 1050)
public class SyncEditorWithViewsAction extends SystemAction implements DynamicMenuContent {

    public static final String SYNC_ENABLED_PROP_NAME = "synchronizeEditorWithViews";
    
    private JCheckBoxMenuItem menuItems[];

    @Override
    public String getName() {
        return NbBundle.getMessage(SyncEditorWithViewsAction.class, "CTL_SYNC_EDITOR_WITH_VIEWS");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SyncEditorWithViewsAction.class);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Preferences prefs = NbPreferences.forModule(SyncEditorWithViewsAction.class);
        prefs.putBoolean(SYNC_ENABLED_PROP_NAME, !prefs.getBoolean(SYNC_ENABLED_PROP_NAME, false));
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
        return items;
    }

    private void createItems() {
        if (menuItems == null) {
            menuItems = new JCheckBoxMenuItem[1];
            menuItems[0] = new JCheckBoxMenuItem(this);
            menuItems[0].setIcon(null);
            Mnemonics.setLocalizedText(menuItems[0],
                    NbBundle.getMessage(SyncEditorWithViewsAction.class,
                    "CTL_SYNC_EDITOR_WITH_VIEWS"));
        }
    }

    private void updateState() {
        boolean sel = NbPreferences.forModule(SyncEditorWithViewsAction.class).getBoolean(SYNC_ENABLED_PROP_NAME, false);
        menuItems[0].setSelected(sel);
    }

}
