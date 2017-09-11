/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
