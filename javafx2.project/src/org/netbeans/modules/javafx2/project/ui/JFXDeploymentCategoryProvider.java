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
