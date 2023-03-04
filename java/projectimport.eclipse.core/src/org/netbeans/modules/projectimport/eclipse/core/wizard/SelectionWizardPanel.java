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

package org.netbeans.modules.projectimport.eclipse.core.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.projectimport.eclipse.core.EclipseUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

/**
 * Selection wizard panel for Eclipse Wizard importer.
 *
 * @author mkrauskopf
 */
final class SelectionWizardPanel extends ImporterWizardPanel implements
        PropertyChangeListener, WizardDescriptor.ValidatingPanel<WizardDescriptor> {
    
    private SelectionPanel panel;
    
    /** Creates a new instance of WorkspaceWizardPanel */
    SelectionWizardPanel() {
        panel = new SelectionPanel();
        panel.addPropertyChangeListener(this);
        initPanel(panel, 0);
    }
    
    public Component getComponent() {
        return panel;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if ("errorMessage".equals(propName)) { //NOI18N
            setErrorMessage((String) evt.getNewValue());
        } else if ("workspaceChoosen".equals(propName)) { // NOI18N
            String[] steps;
            if (((Boolean) evt.getNewValue()).booleanValue()) {
                steps = new String[] { WORKSPACE_LOCATION_STEP, PROJECTS_SELECTION_STEP };
            } else {
                steps = new String[] { PROJECT_SELECTION_STEP };
            }
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            // force Next and Finish buttons state refresh:
            setValid(isValid(), true);
        }
    }
    
    // ==== delegate methods ==== //

    boolean isWorkspaceChosen() {
        return panel.isWorkspaceChosen();
    }
    
    /** Returns project directory of single-selected project. */
    String getProjectDir() {
        return panel.getProjectDir();
    }
    
    /** Returns destination directory for single-selected project. */
    public String getProjectDestinationDir() {
        return panel.getProjectDestinationDir();
    }
    
    /** Returns workspace directory choosed by user. */
    public File getWorkspaceDir() {
        return panel.getWorkspaceDir();
    }
    
    public void validate() throws WizardValidationException {
        if (!panel.isWorkspaceChosen()) {
            String dest = getProjectDestinationDir();

            String message = null;
            if (dest != null && ((!new File(dest).isAbsolute()) || !EclipseUtils.isWritable(dest))) {
                message = ProjectImporterWizard.getMessage(
                        "MSG_CannotCreateProjectInFolder", dest); // NOI18N
            } else if (!EclipseUtils.isRegularProject(getProjectDir())) {
                message = ProjectImporterWizard.getMessage(
                        "MSG_CannotImportNonJavaProject"); // NOI18N
            }
            if (message != null) {
                setErrorMessage(message);
                throw new WizardValidationException(panel, message, null);
            }
        }
    }
}
