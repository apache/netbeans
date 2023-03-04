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

package org.netbeans.modules.maven.osgi.customizer;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.osgi.OSGiConstants;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;

/**
 *
 * @author dafe
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-maven", position=350)
public class PackagesPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    @Override
    public Category createCategory(Lookup context) {
        Project project = context.lookup(Project.class);
        NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
        String effPackaging = watcher.getPackagingType();
        String[] types = PluginPropertyUtils.getPluginPropertyList(project, OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN, "supportedProjectTypes", "supportedProjectType", /*"bundle" would not work for GlassFish parent POM*/null);
        if (types != null) {
            for (String type : types) {
                if (effPackaging.equals(type)) {
                    effPackaging = NbMavenProject.TYPE_OSGI;
                }
            }
        }
        if (NbMavenProject.TYPE_OSGI.equalsIgnoreCase(effPackaging)) {
            return ProjectCustomizer.Category.create(
                    ModelHandle2.PANEL_COMPILE,
                    org.openide.util.NbBundle.getMessage(PackagesPanelProvider.class, "TIT_Packages"),
                    null);
        }
        return null;
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        Project prj = context.lookup(Project.class);
        final PackagesPanel panel = new PackagesPanel(handle, prj);
        return panel;
    }

}
