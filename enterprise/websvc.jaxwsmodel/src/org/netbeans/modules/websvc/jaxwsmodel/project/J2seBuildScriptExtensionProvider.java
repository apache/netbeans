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

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.websvc.api.jaxws.project.JaxWsBuildScriptExtensionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=JaxWsBuildScriptExtensionProvider.class, projectType="org-netbeans-modules-java-j2seproject")
public class J2seBuildScriptExtensionProvider implements JaxWsBuildScriptExtensionProvider {
    private static String COMPILE_ON_SAVE_UNSUPPORTED = "compile.on.save.unsupported.jaxws"; //NOI18N
    static String JAX_WS_STYLESHEET_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jaxws-j2se.xsl"; //NOI18N
    private Project project;

    /** Creates a new instance of J2seBuildScriptExtensionProvider */
    public J2seBuildScriptExtensionProvider(Project project) {
        this.project = project;
    }

    @Override
    public void addJaxWsExtension(AntBuildExtender ext) throws IOException {
        TransformerUtils.transformClients(project.getProjectDirectory(), JAX_WS_STYLESHEET_RESOURCE);
        FileObject jaxws_build = project.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
        assert jaxws_build!=null;
        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
        if (extension==null) {
            extension = ext.addExtension(JAXWS_EXTENSION, jaxws_build);
            //adding dependencies
            extension.addDependency("-pre-pre-compile", "wsimport-client-generate"); //NOI18N

            // disable Compile On Save feature
            disableCompileOnSave(project);
            ProjectManager.getDefault().saveProject(project);
        }
    }

    @Override
    public void removeJaxWsExtension(final AntBuildExtender ext) throws IOException {
        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
        if (extension!=null) {
            ProjectManager.mutex().writeAccess(new Runnable() {
                @Override
                public void run() {
                    ext.removeExtension(JAXWS_EXTENSION);
                }
            });
            // enable Compile on Save feature
            enableCompileOnSave();
            ProjectManager.getDefault().saveProject(project);
        }
        FileObject jaxws_build = project.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
        if (jaxws_build != null) {
            FileLock fileLock = jaxws_build.lock();
            if (fileLock!=null) {
                try {
                    jaxws_build.delete(fileLock);
                } finally {
                    fileLock.releaseLock();
                }
            }
        }
    }

    private static void disableCompileOnSave(Project prj) throws IOException {
        EditableProperties props = WSUtils.getEditableProperties(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.put(COMPILE_ON_SAVE_UNSUPPORTED, "true"); //NOI18N
        WSUtils.storeEditableProperties(prj,  AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    }

    private void enableCompileOnSave() throws IOException {
        EditableProperties props = WSUtils.getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.remove(COMPILE_ON_SAVE_UNSUPPORTED);
        WSUtils.storeEditableProperties(project,  AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    }

    @Override
    public void handleJaxWsModelChanges(JaxWsModel model) throws IOException {
        AntBuildExtender ext = project.getLookup().lookup(AntBuildExtender.class);
        if (ext != null) {
            //FileObject jaxws_build = project.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
            if (model.getClients().length == 0) {
                // remove nbproject/jaxws-build.xml
                // remove the jaxws extension
                removeJaxWsExtension(ext);
            } else {
                // re-generate nbproject/jaxws-build.xml
                // add jaxws extension
                addJaxWsExtension(ext);
            }
        }
    }

}
