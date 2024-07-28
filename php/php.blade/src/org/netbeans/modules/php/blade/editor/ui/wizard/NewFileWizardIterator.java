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
package org.netbeans.modules.php.blade.editor.ui.wizard;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.loaders.DataObject;

/**
 *
 * @author bhaidu
 */
public class NewFileWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    WizardDescriptor wizard;
    private WizardDescriptor.Panel<WizardDescriptor> wizardPanel;

    private NewFileWizardIterator() {
    }

    @TemplateRegistration(folder = "Blade", category = "PHP",
            content = "../../../resources/emptyBladeFile.blade.php",
            description = "../../../resources/NewBladeFileDescription.html",
            position = 120,
            displayName = "Blade file",
            scriptEngine = "freemarker")
    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> createBladeWizardIterator() {
        return new NewFileWizardIterator();
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
  //      getBottomPanel().save();

        FileObject dir = Templates.getTargetFolder(wizard);
        FileObject template = Templates.getTemplate(wizard);

        DataFolder dataFolder = DataFolder.findFolder(dir);
        DataObject dataTemplate = DataObject.find(template);
        DataObject createdFile = dataTemplate.createFromTemplate(dataFolder, Templates.getTargetName(wizard) + ".blade");
        return Collections.singleton(createdFile.getPrimaryFile());
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        Templates.setTargetName(wizard, "myfile");
        wizardPanel = createWizardPanel();
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        this.wizard = null;
        wizardPanel = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return wizardPanel;
    }

    @Override
    public String name() {
        return "new php file wizaed"; // NOI18N
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void nextPanel() {
    }

    @Override
    public void previousPanel() {
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }


    private WizardDescriptor.Panel<WizardDescriptor> createWizardPanel() {
        Project project = getProject();
        assert project != null;
        return Templates.buildSimpleTargetChooser(project, getSourceGroups(project))
                .create();
    }

    private SourceGroup[] getSourceGroups(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        return sources.getSourceGroups(Sources.TYPE_GENERIC);
    }

    private Project getProject() {
        return Templates.getProject(wizard);
    }

}
