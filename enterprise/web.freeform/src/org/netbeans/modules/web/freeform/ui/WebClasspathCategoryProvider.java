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

package org.netbeans.modules.web.freeform.ui;

import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.web.freeform.LookupProviderImpl;
import org.netbeans.modules.web.freeform.WebProjectGenerator;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-ant-freeform", position=500)
public class WebClasspathCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    public Category createCategory(Lookup context) {
        AuxiliaryConfiguration aux = context.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        if (LookupProviderImpl.isMyProject(aux)) {
            Category cat = ProjectCustomizer.Category.create("WebClasspath", //NOI18N
                    NbBundle.getMessage(WebClasspathPanel.class, "LBL_ProjectCustomizer_Category_Classpath"), null);
            return cat;
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
        
        WebClasspathPanel panel = new WebClasspathPanel(false);
        List l = WebProjectGenerator.getWebmodules(acc.getHelper(), aux);
        if (l != null){
            WebProjectGenerator.WebModule wm = (WebProjectGenerator.WebModule)l.get(0);
            panel.setProjectFolders(Util.getProjectLocation(acc.getHelper(), acc.getEvaluator()),
                    FileUtil.toFile(acc.getHelper().getProjectDirectory()));
            panel.setClasspath(wm.classpath, acc.getEvaluator());
            panel.updateButtons();
        }

        category.setOkButtonListener(panel.getCustomizerOkListener(acc.getHelper()));
        return panel;
    }

}
