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

package org.netbeans.modules.java.freeform.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.LookupProviderImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author mkleint
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-ant-freeform", position=200)
public class SourceFoldersCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    public Category createCategory(Lookup context) {
        AuxiliaryConfiguration aux = context.lookup(AuxiliaryConfiguration.class);
        final ProjectAccessor acc = context.lookup(ProjectAccessor.class);
        Project project = context.lookup(Project.class);
        assert aux != null;
        assert acc != null;
        assert project != null;
        if (LookupProviderImpl.isMyProject(aux)) {
            Category cat = ProjectCustomizer.Category.create("SourceFolders", //NOI18N
                    NbBundle.getMessage(ClasspathPanel.class, "LBL_ProjectCustomizer_Category_Sources"), null);
            final ProjectModel pm = ProjectModel.createModel(Util.getProjectLocation(acc.getHelper(), acc.getEvaluator()), 
                    FileUtil.toFile(project.getProjectDirectory()), acc.getEvaluator(), acc.getHelper());
            InstanceContent ic = context.lookup(InstanceContent.class);
            assert ic != null;
            ic.add(pm);
            cat.setOkButtonListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                   // store changes always because model could be modified in ClasspathPanel or OutputPanel
                    ProjectModel.saveProject(acc.getHelper(), pm);                
                }
            });
            return cat;
        }
        return null;
    }

    public JComponent createComponent(Category category, Lookup context) {
        ProjectModel pm = context.lookup(ProjectModel.class);
        ProjectAccessor acc = context.lookup(ProjectAccessor.class);
        assert pm != null;
        SourceFoldersPanel panel = new SourceFoldersPanel(false);
        panel.setModel(pm, acc.getHelper());    
        return panel;
        
    }

}
