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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.websvc.api.jaxws.project.JaxWsBuildScriptExtensionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-j2ee-clientproject")
public class AppClientJaxWsOpenHook extends ProjectOpenedHook {
    Project prj;

    /** Creates a new instance of AppClientJaxWsOpenHook */
    public AppClientJaxWsOpenHook(Project prj) {
        this.prj = prj;
        try {
            Class.forName(WSUtils.class.getName());
        }
        catch (ClassNotFoundException e) {
            assert false;
        }
    }
    
    @Override
    protected void projectOpened() {
        AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
        final JaxWsBuildScriptExtensionProvider extProvider = prj.getLookup().lookup(JaxWsBuildScriptExtensionProvider.class);
        if (ext != null && extProvider != null) {
            FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
            try {
                AntBuildExtender.Extension extension = ext.getExtension(JaxWsBuildScriptExtensionProvider.JAXWS_EXTENSION);
                FileObject jaxWsFo = WSUtils.findJaxWsFileObject(prj);
                if (jaxws_build==null || extension == null) {
                    if (jaxWsFo != null && WSUtils.hasClients(jaxWsFo)) {
                        // generate nbproject/jaxws-build.xml
                        // add jaxws extension
                        extProvider.addJaxWsExtension(ext);
                    }
                } else if (jaxWsFo == null || !WSUtils.hasClients(jaxWsFo)) {
                    // remove nbproject/jaxws-build.xml
                    // remove the jaxws extension
                    extProvider.removeJaxWsExtension(ext);
                } else {
                    // remove compile dependencies, and re-generate build-script if needed
                    FileObject project_xml = prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                    if (project_xml != null) {
                        removeCompileDependencies(prj, project_xml, ext);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void projectClosed() {
    }
    
    /** make old project backward compatible with new projects
     *
     */
    private void removeCompileDependencies (
                        Project prj,
                        FileObject project_xml,
                        final AntBuildExtender ext) throws IOException {

        BufferedReader br = new BufferedReader( new InputStreamReader( 
                new FileInputStream(FileUtil.toFile(project_xml)), StandardCharsets.UTF_8));
        String line = null;
        boolean isOldVersion = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("wsimport-client-compile")) { //NOI18N
                isOldVersion = true;
                break;
            }
        }
        br.close();
        if (isOldVersion) {
            TransformerUtils.transformClients(prj.getProjectDirectory(), AppClientBuildScriptExtensionProvider.JAX_WS_STYLESHEET_RESOURCE);
            AntBuildExtender.Extension extension = ext.getExtension(JaxWsBuildScriptExtensionProvider.JAXWS_EXTENSION);
            if (extension!=null) {
                extension.removeDependency("-do-compile", "wsimport-client-compile"); //NOI18N
                extension.removeDependency("-do-compile-single", "wsimport-client-compile"); //NOI18N
                ProjectManager.getDefault().saveProject(prj);
            }
        }

    }

}
