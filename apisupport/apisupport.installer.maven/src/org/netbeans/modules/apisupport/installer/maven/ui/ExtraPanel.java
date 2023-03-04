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

package org.netbeans.modules.apisupport.installer.maven.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JComponent;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.apisupport.installer.maven.ui.Bundle.*;
import org.netbeans.modules.apisupport.installer.ui.InstallerPanel;
import org.netbeans.modules.apisupport.installer.ui.SuiteInstallerProjectProperties;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Dmitry Lipin <dlipin@netbeans.org>
 */

@ProjectCustomizer.CompositeCategoryProvider.Registration(
    projectType="org-netbeans-modules-maven",
    position=1100
)
public class ExtraPanel implements ProjectCustomizer.CompositeCategoryProvider {
    
    @Messages("LBL_InstallerPanel=Installer")
    @Override
        public Category createCategory(Lookup context) {
        Project project = context.lookup(Project.class);
        NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
        if (watcher!=null &&
                NbMavenProject.TYPE_NBM_APPLICATION.equalsIgnoreCase(watcher.getPackagingType())) {
            String version = PluginPropertyUtils.getPluginVersion(watcher.getMavenProject(), "org.codehaus.mojo", "nbm-maven-plugin");
            if (version == null || new ComparableVersion(version).compareTo(new ComparableVersion("3.7-SNAPSHOT")) >= 0) {
                return null; // now handled by maven.apisupport
            }
            return ProjectCustomizer.Category.create(
                    "Installer",
                    LBL_InstallerPanel(),
                    null,
                    (ProjectCustomizer.Category[])null);
        }
        return null;
    }

    @Messages("LBL_deprecated=Using deprecated Ant-based installer creator. Upgrade to 3.7+ plugin to fix.")
    public @Override JComponent createComponent(Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        SuiteInstallerProjectProperties installerProjectProperties =
                new SuiteInstallerProjectProperties(project);
        // use OkListener to create new configuration first
        category.setStoreListener(new SavePropsListener(installerProjectProperties));
        category.setErrorMessage(LBL_deprecated());
        return new InstallerPanel(installerProjectProperties);
    }

    private static class SavePropsListener implements ActionListener {

        private SuiteInstallerProjectProperties installerProps;

        public SavePropsListener(SuiteInstallerProjectProperties props) {
            installerProps = props;
        }

        public @Override void actionPerformed(ActionEvent e) {
            try {
                installerProps.store();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
}
}

