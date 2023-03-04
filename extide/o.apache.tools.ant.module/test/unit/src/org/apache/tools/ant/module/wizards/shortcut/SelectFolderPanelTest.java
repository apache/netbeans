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

package org.apache.tools.ant.module.wizards.shortcut;

import java.util.Arrays;
import javax.swing.ListModel;
import org.openide.loaders.DataFolder;

/**
 * Test functionality of SelectFolderPanel.
 * @author Jesse Glick
 */
public final class SelectFolderPanelTest extends ShortcutWizardTestBase {

    public SelectFolderPanelTest(String name) {
        super(name);
    }

    private SelectFolderPanel.SelectFolderWizardPanel menuPanel;
    private SelectFolderPanel.SelectFolderWizardPanel toolbarsPanel;
    private ListModel menuListModel;
    private ListModel toolbarsListModel;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        iter.nextPanel();
        wiz.putProperty(ShortcutWizard.PROP_SHOW_MENU, true);
        wiz.putProperty(ShortcutWizard.PROP_SHOW_TOOL, true);
        menuPanel = (SelectFolderPanel.SelectFolderWizardPanel)iter.current();
        menuListModel = menuPanel.getPanel().getModel();
        iter.nextPanel();
        toolbarsPanel = (SelectFolderPanel.SelectFolderWizardPanel)iter.current();
        toolbarsListModel = toolbarsPanel.getPanel().getModel();
    }
    
    public void testFolderListDisplay() throws Exception {
        String[] names = new String[menuListModel.getSize()];
        for (int i = 0; i < names.length; i++) {
            names[i] = menuPanel.getPanel().getNestedDisplayName((DataFolder)menuListModel.getElementAt(i));
        }
        String[] expected = {
            "File",
            "Edit",
            "Build",
            "Build \u2192 Other",
            "Help",
        };
        assertEquals("right names in list", Arrays.asList(expected), Arrays.asList(names));
        names = new String[toolbarsListModel.getSize()];
        for (int i = 0; i < names.length; i++) {
            names[i] = toolbarsPanel.getPanel().getNestedDisplayName((DataFolder)toolbarsListModel.getElementAt(i));
        }
        expected = new String[] {
            "Build",
            "Help",
        };
        assertEquals("right names in list", Arrays.asList(expected), Arrays.asList(names));
    }
    
    // XXX test setting correct folder & display name in wizard data
    
}
