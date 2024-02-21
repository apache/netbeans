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
package org.netbeans.modules.maven.j2ee.appclient;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.j2ee.BaseEEModuleProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

@ProjectServiceProvider(service = {AppClientModuleProviderImpl.class, J2eeModuleProvider.class}, projectType = {"org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT})
public class AppClientModuleProviderImpl extends BaseEEModuleProvider {
    
    private AppClientImpl appClientImpl;

    
    public AppClientModuleProviderImpl(Project project) {
        super(project);
        appClientImpl = new AppClientImpl(project, this);
    }

    @Override
    public AppClientImpl getModuleImpl() {
        return appClientImpl;
    }
    
    @Override
    public FileObject[] getSourceRoots() {
        ProjectSourcesClassPathProvider cppImpl = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        ClassPath cp = cppImpl.getProjectSourcesClassPath(ClassPath.SOURCE);
        List<URL> resUris = new ArrayList<URL>();
        for (URI uri : project.getLookup().lookup(NbMavenProject.class).getResources(false)) {
            try {
                resUris.add(uri.toURL());
            } catch (MalformedURLException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }
        Iterator<ClassPath.Entry> en = cp.entries().listIterator();
        List<FileObject> toRet = new ArrayList<FileObject>();
        int index = 0;
        while (en.hasNext()) {
            ClassPath.Entry ent = en.next();
            if (ent.getRoot() == null) continue;
            if (resUris.contains(ent.getURL())) {
                //put resources up front..
                toRet.add(index, ent.getRoot());
                index = index + 1;
            } else {
                toRet.add(ent.getRoot());
            }
        }
        return toRet.toArray(new FileObject[0]);
    }

    @Override
    public File[] getRequiredLibraries() {
        ProjectSourcesClassPathProvider cppImpl = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        // do not use COMPILE classpath here because it contains dependencies
        // with *provided* scope which should not be deployed
        ClassPath cp = cppImpl.getProjectSourcesClassPath(ClassPath.EXECUTE);
        List<File> files = new ArrayList<File>();
        for (FileObject fo : cp.getRoots()) {
            fo = FileUtil.getArchiveFile(fo);
            if (fo == null) {
                continue;
            }
            files.add(FileUtil.toFile(fo));
        }
        return files.toArray(new File[0]);
    }
}
