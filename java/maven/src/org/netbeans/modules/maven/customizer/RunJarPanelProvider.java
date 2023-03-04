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

package org.netbeans.modules.maven.customizer;
import javax.swing.JComponent;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import static org.netbeans.modules.maven.customizer.Bundle.*;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Milos Kleint 
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType="org-netbeans-modules-maven", 
        position=300
)
public class RunJarPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    @Override
    @Messages("TIT_Run=Run")
    public Category createCategory(Lookup context) {
        NbMavenProjectImpl project = context.lookup(NbMavenProjectImpl.class);
        NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
        
        if (NbMavenProject.TYPE_JAR.equalsIgnoreCase(watcher.getPackagingType())) {
            return ProjectCustomizer.Category.create(
                    ModelHandle2.PANEL_RUN,
                    TIT_Run(),
                    null);
        }
        return null;
    }
    
    @Override
    public JComponent createComponent(Category category, Lookup context) {
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        NbMavenProjectImpl project = context.lookup(NbMavenProjectImpl.class);
        final RunJarPanel panel = new RunJarPanel(handle, project, category);
        return panel;
    }
    
}
