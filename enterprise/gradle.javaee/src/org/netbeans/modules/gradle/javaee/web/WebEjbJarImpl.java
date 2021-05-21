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

package org.netbeans.modules.gradle.javaee.web;

import org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider;
import java.io.File;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.ejb.EjbJarMetadataModelFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation2;
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
        if (webInfFo == null) {
            return null;
        }
        // ejb-jar.xml is optional
        FileObject dd = webInfFo.getFileObject(EJB_JAR_DD);
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
