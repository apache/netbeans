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
package org.netbeans.modules.web.client.samples.wizard.ui;

import java.io.File;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class SamplePanel implements Panel<WizardDescriptor> {

    private SampleVisualPanel myPanel;
    private WizardDescriptor myDescriptor;


    public SamplePanel(WizardDescriptor descriptor) {
        myDescriptor = descriptor;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public SampleVisualPanel getComponent() {
        if (myPanel == null) {
            myPanel = new SampleVisualPanel(myDescriptor);
        }
        return myPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
        //return new HelpCtx("html5.samples"); // NOI18N
    }

    @Override
    public boolean isValid() {
        String error = getComponent().getErrorMessage();
        if (error != null && !error.isEmpty()) {
            setErrorMessage(error);
            return false;
        }
        // everything ok
        setErrorMessage(null);
        return true;
    }

    @Override
    public void readSettings(WizardDescriptor descriptor) {
        descriptor.putProperty("NewProjectWizard_Title", NbBundle.getMessage(
                SamplePanel.class, "TTL_SamplePanel"));         // NOI18N
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @Override
    public void storeSettings(WizardDescriptor descriptor) {
        File projectLocation = new File(getComponent().getProjectLocation());
        FileObject directory = FileUtil.toFileObject(FileUtil.normalizeFile(projectLocation));
        Templates.setTargetFolder(myDescriptor, directory);
        Templates.setTargetName(myDescriptor, getComponent().getProjectName());
    }

    private void setErrorMessage(String message) {
        myDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }
}
