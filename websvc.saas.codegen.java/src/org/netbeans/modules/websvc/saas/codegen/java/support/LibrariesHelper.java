/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.websvc.saas.codegen.java.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
//import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author nam
 */
public class LibrariesHelper {

    public static final String PATH_LIBRARIES = "lib";  // NOI18N

    public static final String WEBSERVICE_CLIENTS_SUB_DIR = "webservice_clients"; // NOI18N


    public static void addDefaultJaxWsClientJars(Project project, FileObject targetSource, WsdlSaas saas) {
        List<String> jarPaths = getDefaultJaxWsClientJars(saas, WsdlServiceProxyDescriptor.JarEntry.PROXY_JAR_TYPE);
        addArchiveRefsToProject(project, targetSource, jarPaths, ClassPath.COMPILE);
    // NOT SUPPORTED by current ProjectClassPathModifier
        /*List<String> sourceJarPaths = getDefaultJaxWsClientJars(saas, WsdlServiceProxyDescriptor.JarEntry.SRC_JAR_TYPE);
    addArchiveRefsToProject(project, targetSource, sourceJarPaths, ClassPath.SOURCE);*/
    }

    public static void addDefaultJaxRpcClientJars(Project project, FileObject targetSource, WsdlSaas saas) {
        List<String> jarPaths = getDefaultJaxRpcClientJars(saas, WsdlServiceProxyDescriptor.JarEntry.PROXY_JAR_TYPE);
        addArchiveRefsToProject(project, targetSource, jarPaths, ClassPath.COMPILE);

    }

    public static void addClientJars(Project project, FileObject targetSource, WadlSaas saas) {
        addArchivesToProject(project, targetSource, saas.getLibraryJars(), ClassPath.COMPILE);
    /* NOT SUPPORTED by current ProjectClassPathModifier
    addArchivesToProject(project, targetSource, saas.getJaxbSourceJars(), ClassPath.SOURCE);*/
    }

    /**
     * Adds the given jars to the project as archive references
     *
     * @param project the project to be added to
     * @param jars jar files to be added to the project (filename Strings)
     */
    public static void addArchiveRefsToProject(Project project, FileObject targetSource, List<String> jars) {
        addArchiveRefsToProject(project, targetSource, jars, ClassPath.COMPILE);
    }

    public static void addArchiveRefsToProject(Project project, FileObject targetSource,
            List<String> jars, String classPathType) {
        List<FileObject> jarFiles = new ArrayList<FileObject>();
        for (String jarPath : jars) {
            FileObject jarFO = FileUtil.toFileObject(new File(jarPath));
            if (jarFO != null) {
                jarFiles.add(jarFO);
            }
        }

        addArchivesToProject(project, targetSource, jarFiles, classPathType);
    }

    public static void addArchivesToProject(Project project, FileObject targetSource, List<FileObject> jars) {
        addArchivesToProject(project, targetSource, jars, ClassPath.COMPILE);
    }

    public static void addArchivesToProject(Project project, FileObject targetSource,
            List<FileObject> jars, String classPathType) {
        if (targetSource == null) {
            targetSource = getSourceRoot(project);
        }
        ClassPath classPath = ClassPath.getClassPath(targetSource, classPathType);
        if (classPath != null) {  //hack for PHP
            try {
                FileObject wsClientsSubDir = getWebServiceClientLibraryDir(project);
                ArrayList<URL> archiveJars = new ArrayList<URL>();
                for (FileObject jarFO : jars) {
                    try {
                        FileObject destJar = wsClientsSubDir.getFileObject(jarFO.getNameExt());
                        if (destJar == null) {
                            destJar = FileUtil.copyFile(jarFO, wsClientsSubDir, jarFO.getName());
                        }
                        if (classPath.contains(destJar)) {
                            continue;
                        }
                        archiveJars.add(new URL(destJar.getURL().toExternalForm() + "/")); // NOI18N

                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    URL[] archiveURLs = archiveJars.toArray(new URL[archiveJars.size()]);
                    ProjectClassPathModifier.addRoots(archiveURLs, targetSource, classPathType);
                }

            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    public static FileObject getProjectLibraryDirectory(Project project) throws IOException {
        FileObject projRoot = project.getProjectDirectory();
        FileObject libRoot = projRoot.getFileObject("lib");
        if (libRoot == null) {
            libRoot = FileUtil.createFolder(projRoot, PATH_LIBRARIES);
        }
        return libRoot;
    }

    public static FileObject getWebServiceClientLibraryDir(Project project) throws IOException {
        FileObject projectLibDir = getProjectLibraryDirectory(project);
        FileObject wsClientsSubDir = projectLibDir.getFileObject(WEBSERVICE_CLIENTS_SUB_DIR);
        if (wsClientsSubDir == null) {
            wsClientsSubDir = projectLibDir.createFolder(WEBSERVICE_CLIENTS_SUB_DIR);
        }
        return wsClientsSubDir;
    }

    public static List<String> getDefaultJaxWsClientJars(WsdlSaas saas, String jarType) {
        WsdlData data = saas.getWsdlData();
        List<String> jarPaths = new ArrayList<String>();
        File basePath = data.getJaxWsDescriptor().getXmlDescriptorFile().getParentFile();
        for (WsdlServiceProxyDescriptor.JarEntry jar : data.getJaxWsDescriptor().getJars()) {
            if (jar.getType().equals(jarType)) {
                File jarPath = new File(basePath, jar.getName());
                jarPaths.add(jarPath.getAbsolutePath());
            }
        }
        return jarPaths;
    }

    public static List<String> getDefaultJaxRpcClientJars(WsdlSaas saas, String jarType) {
        WsdlData data = saas.getWsdlData();
        List<String> jarPaths = new ArrayList<String>();
        File basePath = data.getJaxRpcDescriptor().getXmlDescriptorFile().getParentFile();
        for (WsdlServiceProxyDescriptor.JarEntry jar : data.getJaxRpcDescriptor().getJars()) {
            if (jar.getType().equals(jarType)) {
                File jarPath = new File(basePath, jar.getName());
                jarPaths.add(jarPath.getAbsolutePath());
            }
        }
        return jarPaths;
    }

    /**
     * Convenience method to obtain the source root folder.
     * @param project the Project object
     * @return the FileObject of the source root folder
     */
    public static FileObject getSourceRoot(Project project) {
        if (project == null) {
            return null;
        }

        // Search the ${src.dir} Source Package Folder first, use the first source group if failed.
        Sources src = ProjectUtils.getSources(project);
        SourceGroup[] grp = src.getSourceGroups("java");
        for (int i = 0; i < grp.length; i++) {
            if ("${src.dir}".equals(grp[i].getName())) { // NOI18N

                return grp[i].getRootFolder();
            }
        }
        if (grp.length != 0) {
            return grp[0].getRootFolder();
        }

        return null;
    }
}
