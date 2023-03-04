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
package org.netbeans.modules.javafx2.project.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Somol
 * @author Tomas Zezula
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-java-j2seproject", category="BuildCategory", position=250)
public final class JFXDeploymentCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CAT_DEPLOYMENT = "Deployment"; // NOI18N
    
    private static final Map<String, JFXProjectProperties> projectProperties = new HashMap<String, JFXProjectProperties>();
       
    @Override
    public Category createCategory(Lookup context) {
        boolean deploymentEnabled = true;
        final Project project = context.lookup(Project.class);
        if (project != null) {
            final J2SEPropertyEvaluator j2sepe = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            String fxEnabled = j2sepe.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED);
            String fxPreloader = j2sepe.evaluator().getProperty(JFXProjectProperties.JAVAFX_PRELOADER);
            deploymentEnabled = JFXProjectProperties.isTrue(fxEnabled) && !JFXProjectProperties.isTrue(fxPreloader); 
        }
        if(deploymentEnabled) {
            ProjectCustomizer.Category c = ProjectCustomizer.Category.create(CAT_DEPLOYMENT,
                    NbBundle.getMessage(JFXDeploymentCategoryProvider.class, "LBL_Category_Deployment"), null); //NOI18N
            c.setOkButtonListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        JFXProjectProperties prop = JFXProjectProperties.getInstanceIfExists(project.getLookup());
                        if(prop != null) {
                            projectProperties.put(project.getProjectDirectory().getPath(), prop);
                        }
                    }
                }
            });
            c.setStoreListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        JFXProjectProperties prop = projectProperties.get(project.getProjectDirectory().getPath());
                        if(prop != null) {
                            try {
                                prop.store();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        projectProperties.remove(project.getProjectDirectory().getPath());
                    }
                }
            });
            c.setCloseListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        JFXProjectProperties.cleanup(project.getLookup());
                    }
                }
            });
            return c;
        }
        return null;
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        return new JFXDeploymentPanel(JFXProjectProperties.getInstance(context));
    }

}
