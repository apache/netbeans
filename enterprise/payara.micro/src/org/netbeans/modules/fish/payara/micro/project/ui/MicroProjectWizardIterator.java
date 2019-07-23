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
package org.netbeans.modules.fish.payara.micro.project.ui;

import static org.netbeans.modules.fish.payara.micro.plugin.Constants.ARCHETYPE_ARTIFACT_ID;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.ARCHETYPE_GROUP_ID;
import org.netbeans.modules.fish.payara.micro.project.VersionRepository;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.TemplateRegistration;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROJECT_ICON;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROJECT_TYPE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_ARTIFACT_ID;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_AUTO_BIND_HTTP;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_GROUP_ID;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_JAVA_EE_VERSION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_PACKAGE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_PAYARA_MICRO_VERSION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_VERSION;
import static org.netbeans.modules.fish.payara.micro.plugin.MicroPluginWizardDescriptor.updateMicroMavenPlugin;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.openide.util.NbBundle.getMessage;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public final class MicroProjectWizardIterator extends BaseWizardIterator {

    public MicroProjectWizardIterator() {
    }

    @TemplateRegistration(
            folder = ArchetypeWizards.TEMPLATE_FOLDER,
            position = 550,
            displayName = "#TTL_NewProjectWizard",
            iconBase = PROJECT_ICON,
            description = "../resources/PayaraMicroProjectDescription.html"
    )
    public static MicroProjectWizardIterator createWebAppIterator() {
        return new MicroProjectWizardIterator();
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        ProjectInfo projectInfo = new ProjectInfo(
                (String) descriptor.getProperty(PROP_GROUP_ID),
                (String) descriptor.getProperty(PROP_ARTIFACT_ID),
                (String) descriptor.getProperty(PROP_VERSION),
                (String) descriptor.getProperty(PROP_PACKAGE)
        );
        String payaraMicroVersion = (String) descriptor.getProperty(PROP_PAYARA_MICRO_VERSION);
        String autoBindHttp = (String) descriptor.getProperty(PROP_AUTO_BIND_HTTP);
        Archetype archetype = createMojoArchetype();

        Map<String, String> properties = new HashMap<>();
        properties.put(PROP_PAYARA_MICRO_VERSION, payaraMicroVersion);
        properties.put(PROP_JAVA_EE_VERSION, VersionRepository.getInstance().getJavaEEVersion(payaraMicroVersion));
        properties.put(PROP_AUTO_BIND_HTTP, autoBindHttp);

        ArchetypeWizards.logUsage(archetype.getGroupId(), archetype.getArtifactId(), archetype.getVersion());

        File rootFile = FileUtil.normalizeFile((File) descriptor.getProperty("projdir")); // NOI18N
        ArchetypeWizards.createFromArchetype(rootFile, projectInfo, archetype, properties, true);

        Set<FileObject> projects = ArchetypeWizards.openProjects(rootFile, rootFile);
        for (FileObject projectFile : projects) {
            Project project = ProjectManager.getDefault().findProject(projectFile);
            if (project == null) {
                continue;
            }
            MavenProjectSupport.changeServer(project, true);
            updateMicroMavenPlugin(project, payaraMicroVersion, autoBindHttp);
        }

        return projects;
    }

    private Archetype createMojoArchetype() {
        Archetype archetype = new Archetype();
        archetype.setGroupId(ARCHETYPE_GROUP_ID);
        archetype.setArtifactId(ARCHETYPE_ARTIFACT_ID);
        // latest version of archetype automatically fetched from remote catalog
        return archetype;
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        super.initialize(wiz);
        wiz.putProperty("NewProjectWizard_Title", getMessage(MicroProjectWizardIterator.class, "TTL_NewProjectWizard"));
    }

    @Override
    protected WizardDescriptor.Panel[] createPanels(ValidationGroup vg) {
        return new WizardDescriptor.Panel[]{
            ArchetypeWizards.basicWizardPanel(vg, false, null),
            new PayaraMicroDescriptor(PROJECT_TYPE)
        };
    }
}
