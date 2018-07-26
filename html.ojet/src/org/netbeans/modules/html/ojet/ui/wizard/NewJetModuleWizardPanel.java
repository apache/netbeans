/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.ojet.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class NewJetModuleWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    static final String FILE_NAME = "FILE_NAME"; // NOI18N
    static final String PROJECT = "PROJECT"; // NOI18N
    static final String JS_FOLDER = "JS_FOLDER"; // NOI18N
    static final String HTML_FOLDER = "HTML_FOLDER"; // NOI18N

    private volatile NewJetModuleWizardPanelUi panel = null;
    // @GuardedBy("EDT")
    private WizardDescriptor wizard;


    @Override
    public Component getComponent() {
        return getPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        assert EventQueue.isDispatchThread();
        this.wizard = settings;
        getPanel().setFileName((String) settings.getProperty(FILE_NAME));
        getPanel().setProject((Project) settings.getProperty(PROJECT));
        getPanel().setJsFolder((String) settings.getProperty(JS_FOLDER));
        getPanel().setHtmlFolder((String) settings.getProperty(HTML_FOLDER));
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(FILE_NAME, getPanel().getFileName());
        settings.putProperty(JS_FOLDER, getPanel().getJsFolder());
        settings.putProperty(HTML_FOLDER, getPanel().getHtmlFolder());
    }

    @NbBundle.Messages({
        "NewJetModuleWizardPanel.error.project=New module can be created only for project.",
        "NewJetModuleWizardPanel.error.name=File name must be set.",
        "NewJetModuleWizardPanel.error.jsFolder=JS folder must be set.",
        "NewJetModuleWizardPanel.error.jsFile=JS file already exists.",
        "NewJetModuleWizardPanel.error.htmlFolder=HTML folder must be set.",
        "NewJetModuleWizardPanel.error.htmlFile=HTML file already exists.",
    })
    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        if (!getPanel().hasProject()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_project());
            return false;
        }
        String name = getPanel().getFileName();
        if (name == null
                || name.trim().isEmpty()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_name());
            return false;
        }
        String jsFolder = getPanel().getJsFolder();
        if (jsFolder == null
                || jsFolder.trim().isEmpty()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_jsFolder());
            return false;
        }
        String createdJsFile = getPanel().getCreatedJsFile();
        if (new File(createdJsFile).exists()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_jsFile());
            return false;
        }
        String htmlFolder = getPanel().getHtmlFolder();
        if (htmlFolder == null
                || htmlFolder.trim().isEmpty()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_htmlFolder());
            return false;
        }
        String createdHtmlFile = getPanel().getCreatedHtmlFile();
        if (new File(createdHtmlFile).exists()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_htmlFile());
            return false;
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getPanel().removeChangeListener(listener);
    }

    private NewJetModuleWizardPanelUi getPanel() {
        if (panel == null) {
            panel = new NewJetModuleWizardPanelUi();
        }
        return panel;
    }

}
