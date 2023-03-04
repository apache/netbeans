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
package org.netbeans.modules.javascript.nodejs.ui.wizard;

import java.io.File;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

/**
 * Controller for existing project.
 */
public class ExistingProjectPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private volatile WizardDescriptor wizardDescriptor;
    // @GuardedBy("EDT") - not possible, wizard support calls store() method in EDT as well as in a background thread
    private volatile ExistingProjectVisual existingProjectVisual;


    @Override
    public ExistingProjectVisual getComponent() {
        // assert EventQueue.isDispatchThread(); - not possible, see comment above (@GuardedBy())
        if (existingProjectVisual == null) {
            existingProjectVisual = new ExistingProjectVisual();
        }
        return existingProjectVisual;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.javascript.nodejs.ui.wizard.ExistingProjectPanel"); // NOI18N
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_DIRECTORY, getNormalizedFile(getComponent().getProjectDirectory()));
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_NAME, getComponent().getProjectName());
        wizardDescriptor.putProperty(ExistingProjectWizardIterator.SOURCE_ROOT, getNormalizedFile(getComponent().getSources()));
        wizardDescriptor.putProperty(ExistingProjectWizardIterator.SITE_ROOT, getNormalizedFile(getComponent().getSiteRoot()));
        wizardDescriptor.putProperty(ExistingProjectWizardIterator.TEST_ROOT, getNormalizedFile(getComponent().getTestDir()));
    }

    private File getNormalizedFile(String path) {
        if (!StringUtilities.hasText(path)) {
            return null;
        }
        return FileUtil.normalizeFile(new File(path));
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
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

}
