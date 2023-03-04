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
package org.netbeans.modules.gradle.configurations;

import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.execute.ProjectConfigurationUpdater;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "TITLE_ConfigurationsPanel=Configurations"
})
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-gradle", position=212)
public class ConfigurationsPanelProvider implements CompositeCategoryProvider {
    
    public static final String PANEL_CONFIGURATIONS = "CONFIGURATIONS"; // NOI18N
    
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(PANEL_CONFIGURATIONS, Bundle.TITLE_ConfigurationsPanel(), null);
                
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        ConfigurationSnapshot snap = ConfigurationSnapshot.forProject(context, project, (r) -> category.setCloseListener((e) -> r.run()));
        ProjectConfigurationUpdater upd = project.getLookup().lookup(ProjectConfigurationUpdater.class);
        ConfigurationsPanel panel = new ConfigurationsPanel(upd, snap, project);
        
        category.setStoreListener((e) -> updateConfigurations(project, snap));
        return panel;
    }
    
    @NbBundle.Messages({
        "ERROR_UnableSaveConfigurations=Unable to save configurations."
    })
    private void updateConfigurations(Project p, ConfigurationSnapshot snap) {
        try {
            snap.save();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            ProjectConfigurationProvider<GradleExecConfiguration> confProvider = p.getLookup().lookup(ProjectConfigurationProvider.class);
            confProvider.setActiveConfiguration(snap.getActiveConfiguration());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
