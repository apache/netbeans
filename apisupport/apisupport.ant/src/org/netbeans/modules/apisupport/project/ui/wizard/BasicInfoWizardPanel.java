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
import java.io.File;
import java.util.regex.Pattern;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * First panel of <code>NewNbModuleWizardIterator</code>. Allows user to enter
 * basic module information:
 *
 * <ul>
 *  <li>Project name</li>
 *  <li>Project Location</li>
 *  <li>Project Folder</li>
 *  <li>If should be set as a Main Project</li>
 *  <li>NetBeans Platform (for standalone modules)</li>
 *  <li>Module Suite (for suite modules)</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
final class BasicInfoWizardPanel extends NewTemplatePanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {
    
    /** Representing visual component for this step. */
    private BasicInfoVisualPanel visualPanel;
    
    /** Creates a new instance of BasicInfoWizardPanel */
    public BasicInfoWizardPanel(final NewModuleProjectData data) {
        super(data);
    }
    
    public void reloadData() {
        getVisualPanel().refreshData();
    }
    
    public void storeData() {
        getVisualPanel().storeData();
    }
    
    private BasicInfoVisualPanel getVisualPanel() {
        return (BasicInfoVisualPanel) getComponent();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new BasicInfoVisualPanel(getData());
            visualPanel.addPropertyChangeListener(WeakListeners.propertyChange(this, visualPanel));
            visualPanel.setName(NbBundle.getMessage(BasicInfoWizardPanel.class, "LBL_BasicInfoPanel_Title"));
            visualPanel.updateAndCheck();
        }
        return visualPanel;
    }
    
    public @Override HelpCtx getHelp() {
        return new HelpCtx(BasicInfoWizardPanel.class.getName() + "_" + getWizardTypeString());
    }
    
    public void validate() throws WizardValidationException {
        // XXX this is little strange. Since this method is called first time the panel appears.
        // So we have to do this null check (data are uninitialized)
        String prjFolder = getData().getProjectFolder();
        if (prjFolder != null) {
            File prjFolderF = new File(prjFolder);
            String name = getData().getProjectName();

            String pattern;
            String forbiddenChars;
            if (Utilities.isWindows()) {
                pattern = ".*[\\/:*?\"<>|].*";    // NOI18N
                forbiddenChars = "\\ / : * ? \" < > |";    // NOI18N
            } else {
                pattern = ".*[\\/].*";    // NOI18N
                forbiddenChars = "\\ /";    // NOI18N
            }
            // #145574: check for forbidden characters in FolderObject
            if (Pattern.matches(pattern, name)) {
                String message = NbBundle.getMessage(BasicInfoWizardPanel.class, "MSG_ProjectFolderInvalidCharacters");
                message = String.format(message, forbiddenChars);
                throw new WizardValidationException(getVisualPanel().nameValue, message, message);
            }
                    if (prjFolderF.mkdir()) {
                prjFolderF.delete();
            } else {
                String message = NbBundle.getMessage(BasicInfoWizardPanel.class, "MSG_UnableToCreateProjectFolder");
                throw new WizardValidationException(getVisualPanel().nameValue, message, message);
            }
        }
    }
    
}
