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


package org.netbeans.modules.websvc.wsitconf.spi;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Grebac
 */
public abstract class WsitProvider {

    protected Project project;
    protected static final String CLIENT_CFG_FOLDER = "META-INF";

    private static final Logger logger = Logger.getLogger(WsitProvider.class.getName());
    
    public ProjectSpecificSecurity getProjectSecurityUpdater() {
        return null;
    }

    public ProjectSpecificTransport getProjectTransportUpdater() {
        return null;
    }

    public boolean isJsr109Project() {
        return false;
    }

    public boolean isWsitSupported() {
        // check if the FI or TX class exists - this means we don't need to add the library
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if ((sgs != null) && (sgs.length > 0)) {
            ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
            FileObject txFO = classPath.findResource("com/sun/xml/ws/api/pipe/helper/AbstractPipeImpl.class"); // NOI18N
            if (txFO != null) {
                return true;
            } else {
                txFO = classPath.findResource("com/sun/xml/ws/tx/service/TxServerPipe.class"); // NOI18N
                if (txFO != null) {
                    return true;
                }
            }
            J2eePlatform j2eePlatform = ServerUtils.getJ2eePlatform(project);
            if (j2eePlatform != null) {
                Collection<WSStack> wsStacks = (Collection<WSStack>)
                        j2eePlatform.getLookup().lookupAll(WSStack.class);
                for (WSStack stack : wsStacks) {
                    if (stack.isFeatureSupported(JaxWs.Feature.WSIT)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isWsitRtOnClasspath() {
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if ((sgs != null) && (sgs.length > 0)) {
            ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
            FileObject rtFO = classPath.findResource("com/sun/xml/wss/impl/callback/SAMLCallback.class"); // NOI18N
            if ((rtFO != null)) {
                return true;
            }
        }
        return false;
    }
    
    public FileObject getConfigFilesFolder(boolean client) {
        return getConfigFilesFolder(client, true);
    }

    public FileObject getConfigFilesFolder(boolean client, boolean create) {
        FileObject folder = null;

        Sources sources = ProjectUtils.getSources(project);
        if (sources == null) return null;
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        if ((sourceGroups != null) && (sourceGroups.length > 0)) {
            folder = sourceGroups[0].getRootFolder();
            if (folder != null) {
                folder = folder.getFileObject(CLIENT_CFG_FOLDER);
            }
            if ( !create ){
                return folder;
            }
            if ((folder == null) || (!folder.isValid())) {
                try {
                    folder = sourceGroups[0].getRootFolder().createFolder(CLIENT_CFG_FOLDER);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
        return folder;
    }

    public void addServiceDDEntry(String serviceImplPath, String mexUrl, String targetName) {
        // NOOP - default
    }

    public boolean addMetroLibrary() {
        Library[] jaxwsLibs = new Library[] {LibraryManager.getDefault().getLibrary("jaxws21")};
        Library metroLib = LibraryManager.getDefault().getLibrary("metro"); //NOI18N
        if (metroLib != null) {
            try {
                SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if ((sourceGroups != null) && (sourceGroups.length > 0)) {
                    ProjectClassPathModifier.removeLibraries(jaxwsLibs, sourceGroups[0].getRootFolder(), ClassPath.COMPILE);
                    return ProjectClassPathModifier.addLibraries(new Library[] {metroLib}, sourceGroups[0].getRootFolder(), ClassPath.COMPILE);
                }
            } catch (IOException e) {
                //NOOP
            }
        }
        return false;
    }

    public boolean addMetroRtLibrary() {
        return false;
    }

    public void createUser() {
        try {
            project.getProjectDirectory().getFileObject("nbproject").createData("wsit.createuser");
        } catch (IOException ex) {
            logger.log(Level.FINE, null, ex);
        }
    }

}
