/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
