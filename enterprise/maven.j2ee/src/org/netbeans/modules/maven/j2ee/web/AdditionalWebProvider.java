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
package org.netbeans.modules.maven.j2ee.web;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject;
import org.netbeans.modules.j2ee.spi.ejbjar.support.EjbJarSupport;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(service = {EjbJarProvider.class, EjbJarsInProject.class}, projectType = {
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_JAR,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_OSGI
})
public class AdditionalWebProvider implements EjbJarProvider, EjbJarsInProject {

    private final Project project;
    
    public AdditionalWebProvider(Project project) {
        this.project = project;
    }
    
    private EjbJar apiEjbJar() {
        WebModuleProviderImpl moduleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
        String packaging = project.getLookup().lookup(NbMavenProject.class).getPackagingType();

        if (moduleProvider == null || moduleProvider.getModuleImpl() == null) {
            return null;
        }

        Profile profile = moduleProvider.getModuleImpl().getJ2eeProfile();
        
        boolean javaEE6profile = profile != null && profile.isAtLeast(Profile.JAVA_EE_6_WEB);
        
        if (javaEE6profile) {
            return EjbJarFactory.createEjbJar(new WebEjbJarImpl(moduleProvider.getModuleImpl(), project));
        } else {
            return null;
        }
    }

    @Override
    public EjbJar findEjbJar(FileObject file) {
        EjbJar apiEjbJar = apiEjbJar();
        if (apiEjbJar != null) {
            return EjbJarSupport.createEjbJarProvider(project, apiEjbJar).findEjbJar(file);
        } else {
            return null;
        }
    }

    @Override
    public EjbJar[] getEjbJars() {
        EjbJar apiEjbJar = apiEjbJar();
        if (apiEjbJar != null) {
            return EjbJarSupport.createEjbJarsInProject(apiEjbJar).getEjbJars();
        } else {
            return null;
        }
    }
}
