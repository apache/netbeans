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
                        archiveJars.add(new URL(destJar.toURL().toExternalForm() + "/")); // NOI18N

                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    URL[] archiveURLs = archiveJars.toArray(new URL[0]);
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
