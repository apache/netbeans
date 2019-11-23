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
package org.netbeans.modules.fish.payara.micro.project;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public class MicroPropertiesPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-maven",
            position = 305
    )
    public static MicroPropertiesPanelProvider createRun() {
        return new MicroPropertiesPanelProvider();
    }
    
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        Project project = context.lookup(Project.class);
        if (MicroApplication.getInstance(project) == null) {
            return null;
        }
        return ProjectCustomizer.Category.create("PayaraMicro", "Payara Micro", null); // NOI18N
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        final Project project = context.lookup(Project.class);
        if (MicroApplication.getInstance(project) == null) {
            return null;
        }
        
        MicroPropertiesPanel microPanel = new MicroPropertiesPanel(handle, project);
        category.setOkButtonListener(event -> microPanel.applyChanges());
        category.setStoreListener(event -> microPanel.applyChanges());
        return microPanel;
    }

}
