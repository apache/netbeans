/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.protractor.wizard;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
@TemplateRegistration(folder = "SeleniumTests",
        content = "protractor.conf.js",
        scriptEngine = "freemarker",
        position = 1,
        category = "selenium-types",
        displayName = "#NewProtractorConfWizardIterator.protractor.template.displayName",
        description = "ProtractorConfDescription.html")
@NbBundle.Messages("NewProtractorConfWizardIterator.protractor.template.displayName=Protractor Configuration File")
public class NewProtractorConfWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

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
        Templates.setTargetName(wizard, "protractor.conf"); // NOI18N
        ensureProperTargetFolder(project);
        return Templates
                .buildSimpleTargetChooser(project, getSourceGroups(project))
                .create();
    }

    private Project getProject() {
        return Templates.getProject(wizard);
    }

    private void ensureProperTargetFolder(Project project) {
        Templates.setTargetFolder(wizard, project.getProjectDirectory());
    }

    private SourceGroup[] getSourceGroups(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        return sources.getSourceGroups(Sources.TYPE_GENERIC);
    }

}
