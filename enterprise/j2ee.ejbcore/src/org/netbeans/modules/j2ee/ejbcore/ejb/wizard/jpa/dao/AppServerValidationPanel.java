/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.jpa.dao;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.common.ServerUtil;
import org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.javaee.specs.support.api.EjbSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * A validation panel which checks that the target project has a valid server set
 * otherwise it delegates to the real panel.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class AppServerValidationPanel extends DelegatingWizardDescriptorPanel {

    public AppServerValidationPanel(WizardDescriptor.Panel delegate) {
        super(delegate);
    }

    @Override
    public boolean isValid() {
        Project project = getProject();
        WizardDescriptor wizardDescriptor = getWizardDescriptor();

        // check that this project has a valid target server
        J2eePlatform j2eePlatform = ProjectUtil.getPlatform(project);
        if (j2eePlatform == null || !ServerUtil.isValidServerInstance(project)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AppServerValidationPanel.class, "ERR_MissingServer")); // NOI18N
            return false;
        }

        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null && (wm.getJ2eeProfile().isAtLeast(Profile.JAVA_EE_6_WEB))) {
            // check that server is EJB lite sufficient
            EjbSupport ejbSupport = EjbSupport.getInstance(j2eePlatform);
            if (!ejbSupport.isEjb31LiteSupported(j2eePlatform)) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AppServerValidationPanel.class,
                    "ERR_J2ee6AndNotSufficientJ2eeServer")); //NOI18N
                return false;
            }

            // requirement to have EJB Lite classes on the project CP for the code generator
            if (!isSessionBeanCodeGenerationAlowed(project)) { //NOI18N
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AppServerValidationPanel.class,
                    "ERR_NoEjbLiteClassesOnProjectClasspath")); //NOI18N
                return false;
            }
        }

        return super.isValid();
    }

    /**
     * Checks if the code generation of Session Beans is allowed for given project.
     * Means that all required classes are on the project classpath.
     *
     * @param project project
     * @return {@code true} if the Session Beans related code generation is safe, {@code false} otherwise
     */
    private static boolean isSessionBeanCodeGenerationAlowed(Project project) {
        ClassPath classpath = ClassPath.getClassPath(project.getProjectDirectory(), ClassPath.COMPILE);
        return !(classpath.findResource("jakarta/ejb/Stateless.class") == null //NOI18N
                || classpath.findResource("jakarta/ejb/Stateful.class") == null //NOI18N
                || classpath.findResource("jakarta/ejb/Singleton.class") == null) //NOI18N
                || //NOI18N
                !(classpath.findResource("javax/ejb/Stateless.class") == null //NOI18N
                || classpath.findResource("javax/ejb/Stateful.class") == null //NOI18N
                || classpath.findResource("javax/ejb/Singleton.class") == null); //NOI18N
    }
}
