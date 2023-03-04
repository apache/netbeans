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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kubec
 * @author Tomas Zezula
 * @author Petr Somol
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-java-j2seproject", category="Application", position=200)
public class JWSCompositeCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CAT_WEBSTART = "WebStart"; // NOI18N
    
    private static final Map<String, JWSProjectProperties> projectProperties = new HashMap<String, JWSProjectProperties>();
    
    public JWSCompositeCategoryProvider() {}
    
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        boolean fxOverride = false;
        final Project project = context.lookup(Project.class);
        if (project != null) {
            final J2SEPropertyEvaluator j2sepe = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            fxOverride = JWSProjectProperties.isTrue(j2sepe.evaluator().getProperty("javafx.enabled")); //NOI18N
        }
        if(!fxOverride) {
            ProjectCustomizer.Category c = ProjectCustomizer.Category.create(CAT_WEBSTART,
                    NbBundle.getMessage(JWSCompositeCategoryProvider.class, "LBL_Category_WebStart"), null); //NOI18N
            c.setOkButtonListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        JWSProjectProperties prop = JWSProjectProperties.getInstanceIfExists(project.getLookup());
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
                        JWSProjectProperties prop = projectProperties.get(project.getProjectDirectory().getPath());
                        if(prop != null) {
                            JWSProjectPropertiesUtils.updateJnlpExtensionAndSave(prop, project);
                        }
                        projectProperties.remove(project.getProjectDirectory().getPath());
                    }
                }
            });
            c.setCloseListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        JWSProjectProperties.cleanup(project.getLookup());
                    }
                }
            });
            return c;
        }
        return null;
    }
    
    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        return new JWSCustomizerPanel(JWSProjectProperties.getInstance(context));
    }

}
