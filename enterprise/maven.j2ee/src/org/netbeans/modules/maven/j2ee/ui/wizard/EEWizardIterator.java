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

package org.netbeans.modules.maven.j2ee.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import static org.netbeans.modules.maven.j2ee.ui.wizard.Bundle.*;
import org.netbeans.modules.maven.j2ee.ui.wizard.archetype.J2eeArchetypeFactory;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * This class is responsible for creating Ejb, Web and App client projects.
 *
 * @author Dafe Simonek
 * @author Martin Janicek
 */
public final class EEWizardIterator extends BaseWizardIterator {

    public static final String PROP_EE_LEVEL = "eeLevel"; // NOI18N
    private J2eeModule.Type projectType;
    private final String titleName;


    private EEWizardIterator(J2eeModule.Type projectType, String titleName) {
        this.projectType = projectType;
        this.titleName = titleName;
    }


    @Messages("template.WebApp=Web Application")
    @TemplateRegistration(
        folder = ArchetypeWizards.TEMPLATE_FOLDER,
        position = 200,
        displayName = "#template.WebApp",
        iconBase = "org/netbeans/modules/maven/j2ee/ui/resources/WebIcon.png",
        description = "../wizard/resources/WebAppDescription.html"
    )
    public static EEWizardIterator createWebAppIterator() {
        return new EEWizardIterator(J2eeModule.Type.WAR, template_WebApp());
    }

    @Messages("template.EJB=EJB Module")
    @TemplateRegistration(
        folder = ArchetypeWizards.TEMPLATE_FOLDER,
        position = 250,
        displayName = "#template.EJB",
        iconBase = "org/netbeans/modules/maven/j2ee/ui/resources/EjbIcon.png",
        description = "../wizard/resources/EjbDescription.html"
    )
    public static EEWizardIterator createEJBIterator() {
        return new EEWizardIterator(J2eeModule.Type.EJB, template_EJB());
    }

    @Messages("template.APPCLIENT=Enterprise Application Client")
    @TemplateRegistration(
        folder = ArchetypeWizards.TEMPLATE_FOLDER,
        position = 277,
        displayName = "#template.APPCLIENT",
        iconBase = "org/netbeans/modules/maven/j2ee/ui/resources/AppClientIcon.png",
        description = "../wizard/resources/AppClientDescription.html"
    )
    public static EEWizardIterator createAppClientIterator() {
        return new EEWizardIterator(J2eeModule.Type.CAR, template_APPCLIENT());
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        ProjectInfo vi = new ProjectInfo((String) wiz.getProperty("groupId"), (String) wiz.getProperty("artifactId"), (String) wiz.getProperty("version"), (String) wiz.getProperty("package")); //NOI18N

        Profile profile = (Profile) wiz.getProperty(PROP_EE_LEVEL);
        Archetype archetype = J2eeArchetypeFactory.getInstance().findArchetypeFor(projectType, profile);
        ArchetypeWizards.logUsage(archetype.getGroupId(), archetype.getArtifactId(), archetype.getVersion());

        File rootFile = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); // NOI18N
        ArchetypeWizards.createFromArchetype(rootFile, vi, archetype, null, true);

        Set<FileObject> projects = ArchetypeWizards.openProjects(rootFile, rootFile);
        for (FileObject projectFile : projects) {
            Project project = ProjectManager.getDefault().findProject(projectFile);
            if (project == null) {
                continue;
            }

            saveSettingsToNbConfiguration(project);
            MavenProjectSupport.changeServer(project, true);
        }

        return projects;
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        super.initialize(wiz);
        wiz.putProperty("NewProjectWizard_Title", titleName);
    }


    @Override
    protected WizardDescriptor.Panel[] createPanels(ValidationGroup vg) {
        return new WizardDescriptor.Panel[] {
            ArchetypeWizards.basicWizardPanel(vg, false, null),
            new EELevelPanel(projectType),
        };
    }
}
