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

package org.netbeans.modules.maven.j2ee.ear;

import java.io.IOException;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation2;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.javaee.project.spi.ear.EarDDGeneratorImplementation;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.web.WebModuleImpl;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(
    service = {
        EarDDGeneratorImplementation.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR
    }
)
public class EarDDGeneratorImpl implements EarDDGeneratorImplementation {

    private static final String APPLICATION_XML = "application.xml"; //NOI18N
    private final Project project;

    public EarDDGeneratorImpl(Project project) {
        this.project = project;
    }

    @Override
    public FileObject setupDD(boolean force) {
        J2eeApplicationProvider applicationProvider = project.getLookup().lookup(J2eeApplicationProvider.class);
        if (applicationProvider != null) {
            Profile profile = JavaEEProjectSettings.getProfile(project);

            return createApplicationXML(project, profile, applicationProvider.getChildModuleProviders());
        }
        return null;
    }

    /**
     * Creates application.xml deployment descriptor for given project (see #228191)
     *
     * @param project project for which should DD be generated
     * @param serverID server ID of given project
     */
    public FileObject createApplicationXML(Project project, Profile profile, J2eeModuleProvider[] childProviders) {
        EarModuleProviderImpl earProvider = project.getLookup().lookup(EarModuleProviderImpl.class);

        if (earProvider != null) {
            EarImplementation2 earImpl = earProvider.getEarImplementation();

            FileObject applicationXML = earImpl.getDeploymentDescriptor();
            FileObject metaInf = earImpl.getMetaInf();

            if (applicationXML == null) {
                return setupApplicationXML(profile, metaInf, project, childProviders, true);
            }
        }
        return null;
    }

    private FileObject setupApplicationXML(
            final Profile profile,
            final FileObject docBase,
            final Project project,
            final J2eeModuleProvider[] childProviders,
            boolean force) {

        try {
            FileObject dd = docBase.getFileObject(APPLICATION_XML);
            if (dd == null && (force || DDHelper.isApplicationXMLCompulsory(project))) {
                dd = DDHelper.createApplicationXml(profile, docBase, true);
            }

            if (dd != null) {
                Application app = DDProvider.getDefault().getDDRoot(dd);
                app.setDisplayName(ProjectUtils.getInformation(project).getDisplayName());

                if (app.getModule().length == 0 && childProviders.length > 0) {
                    for (J2eeModuleProvider moduleProvider : childProviders) {
                        addModuleToDD(app, moduleProvider);
                    }
                }

                app.write(dd);
            }
            return dd;
        } catch (IOException ex) {
            return null;
        }
    }

    private void addModuleToDD(Application app, J2eeModuleProvider moduleProvider) {
        final J2eeModule j2eeModule = moduleProvider.getJ2eeModule();
        final J2eeModule.Type type = j2eeModule.getType();

        try {
            Module module = (Module) app.createBean(Application.MODULE);
            String path = j2eeModule.getUrl();

            if (J2eeModule.Type.EJB.equals(type)) {
                module.setEjb(path + ".jar"); //NOI18N

            } else if (J2eeModule.Type.WAR.equals(type)) {
                Web w = module.newWeb();
                w.setWebUri(path + ".war"); //NOI18N

                if (moduleProvider instanceof WebModuleProviderImpl) {
                    WebModuleImpl webModuleImpl = ((WebModuleProviderImpl) moduleProvider).getModuleImpl();
                    String contextPath = webModuleImpl.getContextPath();
                    
                    if (!contextPath.isEmpty()) {
                        w.setContextRoot(contextPath);
                    } else {
                        w.setContextRoot("/"); //NOI18N
                    }
                }
                module.setWeb(w);

            } else if (J2eeModule.Type.RAR.equals(type)) {
                module.setConnector(path);

            } else if (J2eeModule.Type.CAR.equals(type)) {
                module.setJava(path);
            }

            app.addModule(module);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
