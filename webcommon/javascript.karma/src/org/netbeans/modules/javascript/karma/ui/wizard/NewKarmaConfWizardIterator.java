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

package org.netbeans.modules.javascript.karma.ui.wizard;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.javascript.karma.util.KarmaUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

@TemplateRegistration(folder = "Html5Tests",
        content = "../resources/karma.conf.js",
        scriptEngine = "freemarker",
        position = 100,
        category = "html5-test",
        displayName = "#NewKarmaConfWizardIterator.karma.template.displayName",
        description = "../resources/KarmaConfDescription.html")
@NbBundle.Messages("NewKarmaConfWizardIterator.karma.template.displayName=Karma Configuration File")
public class NewKarmaConfWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private WizardDescriptor wizard;
    private WizardDescriptor.Panel<WizardDescriptor> wizardPanel;


    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        wizardPanel = createWizardPanel();
    }

    @Override
    public Set instantiate() throws IOException {
        FileObject dir = Templates.getTargetFolder(wizard);
        FileObject template = Templates.getTemplate(wizard);

        DataFolder dataFolder = DataFolder.findFolder(dir);
        DataObject dataTemplate = DataObject.find(template);
        DataObject createdFile = dataTemplate.createFromTemplate(dataFolder, Templates.getTargetName(wizard));
        return Collections.singleton(createdFile.getPrimaryFile());
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
        return ""; // NOI18N
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
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void previousPanel() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        // noop
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        // noop
    }

    private WizardDescriptor.Panel<WizardDescriptor> createWizardPanel() {
        Project project = getProject();
        assert project != null;
        Templates.setTargetName(wizard, "karma.conf"); // NOI18N
        ensureProperTargetFolder(project);
        return Templates
                .buildSimpleTargetChooser(project, getSourceGroups(project))
                .create();
    }

    private Project getProject() {
        return Templates.getProject(wizard);
    }

    private void ensureProperTargetFolder(Project project) {
        Templates.setTargetFolder(wizard, FileUtil.toFileObject(KarmaUtils.getKarmaConfigDir(project)));
    }

    private SourceGroup[] getSourceGroups(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        return sources.getSourceGroups(Sources.TYPE_GENERIC);
    }

}
