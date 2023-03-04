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
package org.netbeans.modules.java.j2sedeploy.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2sedeploy.J2SEDeployProperties;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
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
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-java-j2seproject", category="BuildCategory", position=225)
public final class JSEDeploymentCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CAT_DEPLOYMENT = "Deployment"; // NOI18N
    
    private static final Map<String, J2SEDeployProperties> projectProperties = new HashMap<String, J2SEDeployProperties>();
    private WeakReference<JSEDeploymentPanel> panelRef = null;

    @Override
    public Category createCategory(Lookup context) {
        boolean deploymentEnabled = true;
        final Project project = context.lookup(Project.class);
        if (project != null) {
            final J2SEPropertyEvaluator j2sepe = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            String fxEnabled = j2sepe.evaluator().getProperty(J2SEDeployProperties.JAVAFX_ENABLED);
            deploymentEnabled = !J2SEDeployProperties.isTrue(fxEnabled);
        }
        if(deploymentEnabled) {
            ProjectCustomizer.Category c = ProjectCustomizer.Category.create(CAT_DEPLOYMENT,
                    NbBundle.getMessage(JSEDeploymentCategoryProvider.class, "LBL_Category_Deployment"), null); //NOI18N
            c.setOkButtonListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        J2SEDeployProperties prop = J2SEDeployProperties.getInstanceIfExists(project.getLookup());
                        if(prop != null) {
                            projectProperties.put(project.getProjectDirectory().getPath(), prop);
                        }
                        if(panelRef != null) {
                            JSEDeploymentPanel panel = panelRef.get();
                            if(panel != null) {
                                List<ActionListener> listeners = panel.getOKListeners();
                                for(ActionListener listener : listeners) {
                                    listener.actionPerformed(e);
                                }
                            }
                        }
                    }
                }
            });
            c.setStoreListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        J2SEDeployProperties prop = projectProperties.get(project.getProjectDirectory().getPath());
                        if(prop != null) {
                            try {
                                prop.store();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        projectProperties.remove(project.getProjectDirectory().getPath());
                        if(panelRef != null) {
                            JSEDeploymentPanel panel = panelRef.get();
                            if(panel != null) {
                                List<ActionListener> listeners = panel.getStoreListeners();
                                for(ActionListener listener : listeners) {
                                    listener.actionPerformed(e);
                                }
                            }
                        }
                    }
                }
            });
            c.setCloseListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(project != null) {
                        J2SEDeployProperties.cleanup(project.getLookup());
                        if(panelRef != null) {
                            JSEDeploymentPanel panel = panelRef.get();
                            if(panel != null) {
                                List<ActionListener> listeners = panel.getCloseListeners();
                                for(ActionListener listener : listeners) {
                                    listener.actionPerformed(e);
                                }
                            }
                        }
                    }
                }
            });
            return c;
        }
        return null;
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        JSEDeploymentPanel panel = new JSEDeploymentPanel(J2SEDeployProperties.getInstance(context));
        panelRef = new WeakReference<JSEDeploymentPanel>(panel);
        return panel;
    }

}
