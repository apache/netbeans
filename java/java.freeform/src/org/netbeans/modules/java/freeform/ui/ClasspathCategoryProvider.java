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
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.java.freeform.LookupProviderImpl;
import org.netbeans.modules.java.freeform.jdkselection.JdkConfiguration;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-ant-freeform", position=400)
public class ClasspathCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    public Category createCategory(Lookup context) {
        AuxiliaryConfiguration aux = context.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        if (LookupProviderImpl.isMyProject(aux)) {
            Category cat = ProjectCustomizer.Category.create("classpath", //NOI18N
                    NbBundle.getMessage(ClasspathPanel.class, "LBL_ProjectCustomizer_Category_Classpath"), null);
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
        
        final JdkConfiguration jdkConf = new JdkConfiguration(project, acc.getHelper(), acc.getEvaluator());
        
        final ClasspathPanel panel = new ClasspathPanel(jdkConf);
        final JavaPlatform initialPlatform = (JavaPlatform) panel.javaPlatform.getSelectedItem();
        ProjectModel pm = context.lookup(ProjectModel.class);
        assert pm != null;
        panel.setModel(pm);
        pm.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                panel.updateControls();
            }
        });
        category.setOkButtonListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JavaPlatform p = (JavaPlatform) panel.javaPlatform.getSelectedItem();
                if (p != initialPlatform) {
                    try {
                        jdkConf.setSelectedPlatform(p);
                    } catch (IOException x) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, x);
                    }
                }
            }
        });
        return panel;
        
    }

}
