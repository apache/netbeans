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

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.ProjectNameProvider;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;

/**
 * @author Tomas Mysik
 */
public abstract class ConfigurableProjectPanel extends JPanel implements ProjectNameProvider, DocumentListener, ChangeListener, ActionListener {

    private static final int STEP_INDEX = 0;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    protected final ProjectFolder projectFolderComponent;

    public ConfigurableProjectPanel(ConfigureProjectPanel wizardPanel) {
        projectFolderComponent = new ProjectFolder(this, wizardPanel);
        projectFolderComponent.addProjectFolderListener(this);

        setName(wizardPanel.getSteps()[STEP_INDEX]);
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, STEP_INDEX);
        // Step name (actually the whole list for reference).
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, wizardPanel.getSteps());
    }

    // abstract methods
    public abstract void setProjectName(String projectName);
    public abstract String getSourcesFolder();
    public abstract LocalServer getSourcesLocation();
    public abstract void selectSourcesLocation(LocalServer localServer);
    public abstract MutableComboBoxModel<LocalServer> getLocalServerModel();
    public abstract void setLocalServerModel(MutableComboBoxModel<LocalServer> localServers);
    public abstract PhpVersion getPhpVersion();
    public abstract void setPhpVersion(PhpVersion phpVersion);
    public abstract Charset getEncoding();
    public abstract void setEncoding(Charset encoding);
    public abstract void setState(boolean enabled);
    public abstract boolean getState();

    public String getProjectFolder() {
        return projectFolderComponent.getProjectFolder();
    }

    public void setProjectFolder(String projectFolder) {
        projectFolderComponent.setProjectFolder(projectFolder);
    }

    public boolean isProjectFolderUsed() {
        return projectFolderComponent.isProjectFolderUsed();
    }

    public void setProjectFolderUsed(boolean used) {
        projectFolderComponent.setProjectFolderUsed(used);
    }

    public void addConfigureProjectListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeConfigureProjectListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    // listeners
    @Override
    public void insertUpdate(DocumentEvent e) {
        processUpdate();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        processUpdate();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        processUpdate();
    }

    private void processUpdate() {
        changeSupport.fireChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        changeSupport.fireChange();
    }
}
