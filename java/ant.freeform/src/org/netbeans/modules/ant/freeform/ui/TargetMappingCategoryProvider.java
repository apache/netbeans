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

package org.netbeans.modules.ant.freeform.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author mkleint
 */
@CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-ant-freeform", position=700)
public class TargetMappingCategoryProvider implements CompositeCategoryProvider {
    
    public Category createCategory(Lookup context) {
        FreeformProject project = context.lookup(FreeformProject.class);
        assert project != null;
        if (project.usesAntScripting()) {
            return ProjectCustomizer.Category.create("targetMapping",   //NOI18N
                    NbBundle.getMessage(TargetMappingPanel.class, "LBL_ProjectCustomizer_Category_Targets"), null);  //NOI18N
        }
        return null;
    }

    public JComponent createComponent(Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        ProjectAccessor acc = context.lookup(ProjectAccessor.class);
        AuxiliaryConfiguration aux = context.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        assert acc != null;
        assert project != null;
        
        List<TargetDescriptor> extraTargets = new ArrayList<TargetDescriptor>();
        for (ProjectNature pn : FreeformProject.PROJECT_NATURES.allInstances()) {
            extraTargets.addAll(pn.getExtraTargets(project, acc.getHelper(), acc.getEvaluator(), aux));
        }
        
        TargetMappingPanel panel = new TargetMappingPanel(extraTargets, acc.getEvaluator(), acc.getHelper());
        category.setOkButtonListener(panel.getCustomizerOkListener());
        return panel;
    }

}
