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

package org.netbeans.modules.maven.apisupport;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import static org.netbeans.modules.maven.apisupport.Bundle.*;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * Registrator for public packages customizer panel
 *
 * @author Dafe Simonek
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-maven", position=360)
public class PackagesPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    @Override
    @Messages("TIT_Packages=Public Packages")
    public Category createCategory(Lookup context) {
        Project project = context.lookup(Project.class);
        NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
        if (NbMavenProject.TYPE_NBM.equalsIgnoreCase(watcher.getPackagingType())) {
            return ProjectCustomizer.Category.create(
                    ModelHandle2.PANEL_COMPILE,
                    TIT_Packages(),
                    null);
        }
        return null;
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        Project prj = context.lookup(Project.class);
        return new PublicPackagesPanel(handle, prj);
    }

}
