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

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Represents <em>Libraries</em> panel in J2SE Library Descriptor Wizard.
 */
final class SelectLibraryPanel extends BasicWizardIterator.Panel {
    
    private NewLibraryDescriptor.DataModel data;
    private final LibraryChooser.Panel panel;
    
    public SelectLibraryPanel(WizardDescriptor setting, NewLibraryDescriptor.DataModel data) {
        super(setting);
        this.data = data;
        getAccessibleContext().setAccessibleDescription(getMessage("ACS_SelectLibraryPanel"));
        putClientProperty("NewFileWizard_Title", getMessage("LBL_LibraryWizardTitle"));
        panel = LibraryChooser.createPanel(null, null);
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (LibraryChooser.Panel.PROP_SELECTED_LIBRARIES.equals(evt.getPropertyName())) {
                    checkValidity();
                }
            }
        });
        setLayout(new BorderLayout());
        add(panel.getVisualComponent(), BorderLayout.CENTER);
    }
    
    private void checkValidity() {
        if (getSelectedLibrary() != null) {
            markValid();
        } else {
            markInvalid();
        }
    }
    
    private Library getSelectedLibrary() {
        Set<Library> selection = panel.getSelectedLibraries();
        return selection.size() == 1 ? selection.iterator().next() : null;
    }
    
    protected void storeToDataModel() {
        data.setLibrary(getSelectedLibrary());
    }
    
    protected void readFromDataModel() {
        checkValidity();
    }
    
    protected String getPanelName() {
        return getMessage("LBL_SelectLibraryPanel_Title");
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(SelectLibraryPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(SelectLibraryPanel.class, key);
    }
    
}
