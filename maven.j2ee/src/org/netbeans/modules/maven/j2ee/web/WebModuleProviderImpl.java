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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.web;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
        WebModuleProviderImpl.class,
        WebModuleProvider.class,
        J2eeModuleProvider.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
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
                FileObject clazz = FileUtil.toFileObject(original.getFile());
                if (targetClasses != null && clazz != null) {
                    String path = FileUtil.getRelativePath(targetClasses, clazz);
                    if (path != null) {
                        try {
                            FileObject webBuildBase = implementation.getContentDirectory();
                            if (webBuildBase != null) {
                                File base = FileUtil.toFile(webBuildBase);
                                File dist = new File(base, "WEB-INF" + File.separator + "classes" + File.separator + path.replace("/", File.separator));
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
