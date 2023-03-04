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
import org.netbeans.modules.java.j2sedeploy.api.J2SEDeployConstants;
import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Somol
 * @author Milan Kubec
 */
@ProjectServiceProvider(service=J2SECategoryExtensionProvider.class, projectType="org-netbeans-modules-java-j2seproject")
public class JSEDeploymentCategoryExtender implements J2SECategoryExtensionProvider {

    private static final Map<String, JFXProjectProperties> projectProperties = new HashMap<String, JFXProjectProperties>();
    
    public JSEDeploymentCategoryExtender() {}
    
    @Override
    public ExtensibleCategory getCategory() {
        return ExtensibleCategory.DEPLOYMENT;
    }

    @Override
    public JComponent createComponent(final Project project, ConfigChangeListener listener) {
        boolean seProject = false;
        if (project != null) {
            final J2SEPropertyEvaluator j2sepe = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            seProject = !JFXProjectProperties.isTrue(j2sepe.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED));

            if(seProject) {
                final JFXProjectProperties props = JFXProjectProperties.getInstance(project.getLookup());
                JComponent comp = props.getSEDeploymentPanel();
                //create listeners, put them to component properties
                comp.putClientProperty(J2SEDeployConstants.PASS_OK_LISTENER, new ActionListener() {
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
                comp.putClientProperty(J2SEDeployConstants.PASS_STORE_LISTENER, new ActionListener() {
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
                comp.putClientProperty(J2SEDeployConstants.PASS_CLOSE_LISTENER, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(project != null) {
                            JFXProjectProperties.cleanup(project.getLookup());
                        }
                    }
                });
                return comp;
            }
        }
        return null;
    }
    
    @Override
    public void configUpdated(Map<String,String> m) {
    }
    
}
