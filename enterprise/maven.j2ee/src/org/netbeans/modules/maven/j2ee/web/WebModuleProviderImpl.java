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

package org.netbeans.modules.maven.j2ee.web;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.j2ee.BaseEEModuleProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


/**
 * web module provider implementation for maven2 project type.
 * @author  Milos Kleint 
 */
@ProjectServiceProvider(
    service = {
        WebModuleProvider.class,
        J2eeModuleProvider.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR
    }
)
public class WebModuleProviderImpl extends BaseEEModuleProvider implements WebModuleProvider {
    
    private WebModuleImpl implementation;
    private WebModule module;
    
    
    public WebModuleProviderImpl(Project project) {
        super(project);
    }
    
    @Override
    public WebModuleImpl getModuleImpl() {
        if (implementation == null) {
            implementation = new WebModuleImpl(project, this);
        }
        return implementation;
    }

    @Override
    public WebModule findWebModule(FileObject fileObject) {
        WebModuleImpl impl = getModuleImpl();
        if (impl != null && impl.isValid()) {
            if (module == null) {
                module = WebModuleFactory.createWebModule(impl);
            }
            return module;
        }
        return null;
    }
    
    @Override
    public DeployOnSaveClassInterceptor getDeployOnSaveClassInterceptor() {
        return new DeployOnSaveClassInterceptor() {
            @Override
            public ArtifactListener.Artifact convert(ArtifactListener.Artifact original) {
                NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
                FileObject targetClasses = FileUtil.toFileObject(prj.getOutputDirectory(false));
                Path targetClassesPath = Paths.get(prj.getOutputDirectory(false).toURI());
                Path classPath = Paths.get(original.getFile().toURI());
                if (targetClasses != null) {
                    Path path = targetClassesPath.relativize(classPath);
                    if (path != null) {
                        try {
                            FileObject webBuildBase = implementation.getContentDirectory();
                            if (webBuildBase != null) {
                                File base = FileUtil.toFile(webBuildBase);
                                File dist = new File(base,
                                        "WEB-INF" + File.separator
                                        + "classes" + File.separator
                                        + path.toString().replace("/", File.separator)
                                );
                                return original.distributionPath(dist);
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                return original;
            }
        };
    }
    
    /**
     *  Returns list of root directories for source files including configuration files.
     *  Examples: file objects for src/java, src/conf.  
     *  Note: 
     *  If there is a standard configuration root, it should be the first one in
     *  the returned list.
     */
    
    @Override
    public FileObject[] getSourceRoots() {
        ProjectSourcesClassPathProvider cppImpl = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        if (cppImpl == null) {
            return new FileObject[0];
        }

        ClassPath cp = cppImpl.getProjectSourcesClassPath(ClassPath.SOURCE);
        NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        List<URL> resUris = new ArrayList<URL>();
        URI webapp = prj.getWebAppDirectory();
        if (webapp != null) {
            try {
                resUris.add(webapp.toURL());
            } catch (MalformedURLException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }
        for (URI uri : prj.getResources(false)) {
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
}
