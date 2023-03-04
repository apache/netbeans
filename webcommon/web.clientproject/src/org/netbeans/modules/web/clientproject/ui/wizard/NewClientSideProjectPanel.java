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
package org.netbeans.modules.web.clientproject.ui.wizard;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Panel just asking for basic info.
 */
public class NewClientSideProjectPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private final String projectNameTemplate;

    private volatile WizardDescriptor wizardDescriptor;
    // @GuardedBy("EDT") - not possible, wizard support calls store() method in EDT as well as in a background thread
    private volatile NewClientSideProject clientSideProject;


    public NewClientSideProjectPanel(String projectNameTemplate) {
        assert projectNameTemplate != null;
        this.projectNameTemplate = projectNameTemplate;
    }

    @Override
    public NewClientSideProject getComponent() {
        if (clientSideProject == null) {
            clientSideProject = new NewClientSideProject(projectNameTemplate);
        }
        return clientSideProject;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.wizard.NewClientSideProjectPanel"); // NOI18N
    }

    @Override
    public boolean isValid() {
        // error
        String error = getComponent().getErrorMessage();
        if (error != null && !error.isEmpty()) {
            setErrorMessage(error);
            return false;
        }
        // everything ok
        setErrorMessage(""); // NOI18N
        return true;
    }

    private void setErrorMessage(String message) {
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }

    @Override
    public final void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public final void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @Override
    public void readSettings(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }

    @Override
    public void storeSettings(WizardDescriptor wizardDescriptor) {
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.Wizard.PROJECT_DIRECTORY, getComponent().getProjectDirectory());
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.Wizard.NAME, getComponent().getProjectName());
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

}
