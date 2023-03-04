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
import java.io.File;
import java.util.List;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.core.EclipseUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

/**
 * Workspace panel for Eclipse Wizard importer.
 *
 * @author mkrauskopf
 */
final class ProjectWizardPanel extends ImporterWizardPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {
    
    private ProjectSelectionPanel panel;
    
    /** Creates a new instance of WorkspaceWizardPanel */
    ProjectWizardPanel() {
        panel = new ProjectSelectionPanel(this);
        initPanel(panel, 1);
    }
    
    public Component getComponent() {
        return panel;
    }
    
    // ==== delegate methods ==== //
    List<EclipseProject> getProjects() {
        return panel.getProjects();
    }
    
    int getNumberOfImportedProject() {
        return panel.getNumberOfImportedProject();
    }
    
    String getDestination() {
        return panel.getDestination();
    }
    
    void loadProjects(File workspaceDir) {
        panel.loadProjects(workspaceDir);
    }
    
    public void validate() throws WizardValidationException {
        String dest = panel.getDestination();
        if (panel.isSeparateFolder() && dest != null && (!new File(dest).isAbsolute() || !EclipseUtils.isWritable(dest))) {
            String message = ProjectImporterWizard.getMessage(
                    "MSG_CannotCreateProjectInFolder", dest); // NOI18N
            setErrorMessage(message);
            throw new WizardValidationException(panel, message, null);
        }
    }
    
    void fireProjectListChanged() {
        cs.fireChange();
    }
    
}
