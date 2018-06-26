/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
