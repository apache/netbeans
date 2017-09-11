/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
