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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.CarFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.CarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.CarsInProject;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.j2ee.BaseEEModuleProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Application Client module provider implementation for maven2 project type.
 * @author  Milos Kleint 
 */
@ProjectServiceProvider(
        service = {
            AppClientModuleProviderImpl.class,
            CarProvider.class,
            CarsInProject.class,
            J2eeModuleProvider.class
        },
        projectType = {
            "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT
        }
)
public class AppClientModuleProviderImpl extends BaseEEModuleProvider implements CarProvider, CarsInProject {
    
    private AppClientImpl appClientImpl;
    private Car apiCarJar;
    
    
    public AppClientModuleProviderImpl(Project project) {
        super(project);
    }

    @Override
    public AppClientImpl getModuleImpl() {
        if (appClientImpl == null) {
            appClientImpl = new AppClientImpl(project, this);
        }
        return appClientImpl;
    }
    
    @Override
    public Car findCar(FileObject file) {
        getModuleImpl();
        Project proj = FileOwnerQuery.getOwner(file);
        if (proj != null) {
            proj = proj.getLookup().lookup(Project.class);
        }
        if (proj != null && project == proj) {
            if (appClientImpl.isValid()) {
                if (apiCarJar == null) {
                    apiCarJar = CarFactory.createCar(appClientImpl);
                }
                return apiCarJar;
            }
        }
        return null;
    }
    
    @Override
    public FileObject[] getSourceRoots() {
        ProjectSourcesClassPathProvider cppImpl = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        ClassPath cp = cppImpl.getProjectSourcesClassPath(ClassPath.SOURCE);
        List<URL> resUris = new ArrayList<>();
        for (URI uri : project.getLookup().lookup(NbMavenProject.class).getResources(false)) {
            try {
                resUris.add(uri.toURL());
            } catch (MalformedURLException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }
        Iterator<ClassPath.Entry> en = cp.entries().listIterator();
        List<FileObject> toRet = new ArrayList<>();
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
        return toRet.toArray(FileObject[]::new);
    }

    @Override
    public File[] getRequiredLibraries() {
        ProjectSourcesClassPathProvider cppImpl = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        // do not use COMPILE classpath here because it contains dependencies
        // with *provided* scope which should not be deployed
        ClassPath cp = cppImpl.getProjectSourcesClassPath(ClassPath.EXECUTE);
        List<File> files = new ArrayList<>();
        for (FileObject fo : cp.getRoots()) {
            fo = FileUtil.getArchiveFile(fo);
            if (fo == null) {
                continue;
            }
            files.add(FileUtil.toFile(fo));
        }
        return files.toArray(File[]::new);
    }

    @Override
    public Car[] getCars() {
        getModuleImpl();
        if (appClientImpl.isValid()) {
            if (apiCarJar == null) {
                apiCarJar =  CarFactory.createCar(appClientImpl);
            }
            return new Car[] {apiCarJar};
        }
        return new Car[0];
    }

}
