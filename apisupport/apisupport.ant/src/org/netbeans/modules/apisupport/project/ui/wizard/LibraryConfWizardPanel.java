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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.Component;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Third panel of Library Wrapper wizard.
 *
 * <ul>
 *  <li>Code Name Base</li>
 *  <li>Module Display Name</li>
 *  <li>Localizing Bundle</li>
 *  <li>XML Layer</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
final class LibraryConfWizardPanel extends NewTemplatePanel {

    /** Representing visual component for this step. */
    private BasicConfVisualPanel visualPanel;
    
    LibraryConfWizardPanel(final NewModuleProjectData data) {
        super(data);
    }
    
    void reloadData() {
        if (getData().getCodeNameBase() == null) {
            // #138003: default code name base is empty
            getData().setCodeNameBase("");    // NOI18N
        }
        if (getData().getProjectDisplayName() == null) {
            getData().setProjectDisplayName(getData().getProjectName());
        }
        visualPanel.refreshData();
    }
    
    void storeData() {
        visualPanel.storeData();
    }
    
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new BasicConfVisualPanel(getData());
            visualPanel.addPropertyChangeListener(this);
            visualPanel.setName(NbBundle.getMessage(LibraryConfWizardPanel.class, "LBL_BasicConfigPanel_Title")); // NOI18N
        }
        return visualPanel;
    }
    
    public @Override HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.apisupport.project.ui.wizard.LibraryConfWizardPanel");
    }
    
    
    
}
