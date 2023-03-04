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

package org.netbeans.modules.javascript.jstestdriver.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 */
public class NewConfigFileWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    private WizardDescriptor descriptor;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private InstallJasmineWizardDescriptorPanel jasmineWizardPanel;
    private int index;


    @Override
    public void initialize(WizardDescriptor wizard) {
        this.descriptor = wizard;
        Templates.setTargetName(wizard, "jsTestDriver"); //NOI18N
        panels = getPanels();

        // make sure list of steps is accurate.
        String[] beforeSteps = (String[]) wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        int beforeStepLength = beforeSteps.length - 1;
        String[] steps = createSteps(beforeSteps);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i + beforeStepLength - 1));
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        FileObject dir = Templates.getTargetFolder(descriptor);
        FileObject template = Templates.getTemplate(descriptor);

        DataFolder dataFolder = DataFolder.findFolder(dir);
        DataObject dataTemplate = DataObject.find(template);
        Map<String, String> map = new HashMap<String, String>();
        if (jasmineWizardPanel != null
                && jasmineWizardPanel.installJasmine()) {
            map.put("jasmine", "true"); // NOI18N
        }
        DataObject createdFile = dataTemplate.createFromTemplate(dataFolder, Templates.getTargetName(descriptor), map);

        // create folder for unit tests and libraries if they do not exist yet:
        Project project = Templates.getProject(descriptor);
        SourceGroup[] groups = getTestGroups(project);
        FileObject libs = null;
        if (groups != null && groups.length > 0) {
            FileUtil.createFolder(groups[0].getRootFolder(), "unit"); // NOI18N
            libs = FileUtil.createFolder(groups[0].getRootFolder(), "lib"); // NOI18N
        }

        if (jasmineWizardPanel != null
                && jasmineWizardPanel.installJasmine()
                && libs != null) {
            jasmineWizardPanel.downloadJasmine(libs);
        }
        return Collections.singleton(createdFile.getPrimaryFile());
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public String name() {
        return ""; // NOI18N
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        // noop
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        // noop
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        Project project = Templates.getProject(descriptor);
        Templates.SimpleTargetChooserBuilder targetChooser = Templates
                .buildSimpleTargetChooser(project, ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC));
        if (getTestGroups(project) != null) {
            jasmineWizardPanel = new InstallJasmineWizardDescriptorPanel();
            targetChooser.bottomPanel(jasmineWizardPanel);
        }
        WizardDescriptor.Panel<WizardDescriptor> simpleTargetChooserPanel = targetChooser.create();

        @SuppressWarnings("unchecked")
        WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {simpleTargetChooserPanel};
        return panels;
    }

    private String[] createSteps(String[] beforeSteps) {
        int beforeStepLength = beforeSteps.length - 1;
        String[] res = new String[beforeStepLength + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeStepLength)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeStepLength].getComponent().getName();
            }
        }
        return res;
    }

    @CheckForNull
    private SourceGroup[] getTestGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST);
        if (sourceGroups.length == 0) {
            return null;
        }
        return sourceGroups;
    }

}
