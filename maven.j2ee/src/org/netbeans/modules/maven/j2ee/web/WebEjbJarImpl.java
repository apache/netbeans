/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.web;

import java.io.File;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.ejb.EjbJarMetadataModelFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation2;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * An EjbJar implementation
 * 
 * @author Milos Kleint
 */
public class WebEjbJarImpl implements EjbJarImplementation2 {

    public static final String EJB_JAR_DD = "ejb-jar.xml";//NOI18N
    private final WebModuleImplementation2 webModule;
    private MetadataModel<EjbJarMetadata> ejbJarMetadataModel;
    private final Project project;

    public WebEjbJarImpl(WebModuleImplementation2 webModule, Project prj) {
        this.webModule = webModule;
        this.project = prj;
    }

    @Override
    public Profile getJ2eeProfile() {
        return this.webModule.getJ2eeProfile();
    }

    @Override
    public FileObject getMetaInf() {
        FileObject webInf = webModule.getWebInf();
        
        if (webInf != null) {
            return webInf;
        }
        return null;
    }

    @Override
    public FileObject getDeploymentDescriptor() {
        FileObject webInfFo = this.webModule.getWebInf();
        if (webInfFo==null) {
            return null;
        }
        // ejb-jar.xml is optional
        FileObject dd = webInfFo.getFileObject (EJB_JAR_DD);
        return dd;
    }

    @Override
    public FileObject[] getJavaSources() {
        return this.webModule.getJavaSources();
    }

    @Override
    public synchronized MetadataModel<EjbJarMetadata> getMetadataModel() {
        if (ejbJarMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            ejbJarMetadataModel = EjbJarMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return ejbJarMetadataModel;
    }
}
