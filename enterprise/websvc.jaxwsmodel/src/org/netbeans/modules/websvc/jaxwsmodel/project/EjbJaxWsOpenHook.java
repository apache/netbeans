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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.websvc.api.jaxws.project.JaxWsBuildScriptExtensionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.WebServiceNotifier;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-j2ee-ejbjarproject")
public class EjbJaxWsOpenHook extends ProjectOpenedHook {
    private Project prj;
    private JaxWsModel.ServiceListener serviceListener;

    /** Creates a new instance of EjbJaxWsOpenHook */
    public EjbJaxWsOpenHook(Project prj) {
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
        JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null) {
            serviceListener = new JaxWsModel.ServiceListener() {

                @Override
                public void serviceAdded(String name, String implementationClass) {
                    WebServiceNotifier servicesNotifier = prj.getLookup().lookup(WebServiceNotifier.class);
                    if (servicesNotifier != null) {
                        servicesNotifier.serviceAdded(name, implementationClass);
                    }
                }

                @Override
                public void serviceRemoved(String name) {
                    WebServiceNotifier servicesNotifier = prj.getLookup().lookup(WebServiceNotifier.class);
                    if (servicesNotifier != null) {
                        servicesNotifier.serviceRemoved(name);
                    }
                }
            };
            jaxWsModel.addServiceListener(serviceListener);


            JaxWsBuildScriptExtensionProvider extProvider = prj.getLookup().lookup(JaxWsBuildScriptExtensionProvider.class);
            AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
            
            if (extProvider != null && ext != null) {
                boolean buildScriptGenerated = false;
                FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
                FileObject jaxWsFo = jaxWsModel.getJaxWsFile();
                try {
                    boolean hasServiceOrClient = jaxWsFo != null && WSUtils.hasServiceOrClient(jaxWsFo);
                    AntBuildExtender.Extension extension = ext.getExtension(JaxWsBuildScriptExtensionProvider.JAXWS_EXTENSION);
                    if (jaxws_build == null || extension == null) {
                        // generate nbproject/jaxws-build.xml
                        // add jaxws extension
                        if (hasServiceOrClient) {
                            extProvider.addJaxWsExtension(ext);
                            ProjectManager.getDefault().saveProject(prj);
                            buildScriptGenerated = true;
                        }
                    } else if (!hasServiceOrClient) {
                        // remove nbproject/jaxws-build.xml
                        // remove the jaxws extension
                        extProvider.removeJaxWsExtension(ext);
                        ProjectManager.getDefault().saveProject(prj);
                        buildScriptGenerated = true;
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

                if (jaxWsFo != null && !buildScriptGenerated) {
                    URL stylesheet = EjbJaxWsOpenHook.class.getResource(EjbBuildScriptExtensionProvider.JAX_WS_STYLESHEET_RESOURCE);
                    assert stylesheet != null;
                    try {
                        boolean needToCallTransformer = false;
                        InputStream is = stylesheet.openStream();
                        String crc32 = null;
                        try {
                            crc32 = TransformerUtils.getCrc32(is);
                        } finally {
                            is.close();
                        }

                        if (crc32 != null) {
                            EditableProperties ep = WSUtils.getEditableProperties(prj, TransformerUtils.GENFILES_PROPERTIES_PATH);
                            if (ep != null) {
                                String oldCrc32 = ep.getProperty(TransformerUtils.JAXWS_BUILD_XML_PATH + TransformerUtils.KEY_SUFFIX_JAXWS_BUILD_CRC);
                                if (!crc32.equals(oldCrc32)) {
                                    ep.setProperty(TransformerUtils.JAXWS_BUILD_XML_PATH + TransformerUtils.KEY_SUFFIX_JAXWS_BUILD_CRC, crc32);
                                    WSUtils.storeEditableProperties(prj, TransformerUtils.GENFILES_PROPERTIES_PATH, ep);
                                    needToCallTransformer = true;
                                }
                            }
                        }
                        if (needToCallTransformer) {
                            TransformerUtils.transformClients(prj.getProjectDirectory(), EjbBuildScriptExtensionProvider.JAX_WS_STYLESHEET_RESOURCE, true);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "failed to generate jaxws-build.xml from stylesheet", ex); //NOI18N
                    }
                }
            }
        }
    }

    @Override
    protected void projectClosed() {
        JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null) {
            jaxWsModel.removeServiceListener(serviceListener);
        }
    }

    /** make old project backward compatible with new projects
     *
     */
    private void removeCompileDependencies (
                        Project prj,
                        FileObject project_xml,
                        final AntBuildExtender ext) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader( 
                new FileInputStream(FileUtil.toFile(project_xml)), StandardCharsets.UTF_8));
        String line = null;
        boolean isOldVersion = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("wsimport-client-compile") || line.contains("wsimport-service-compile")) { //NOI18N
                isOldVersion = true;
                break;
            }
        }
        br.close();
        if (isOldVersion) {
            TransformerUtils.transformClients(prj.getProjectDirectory(), EjbBuildScriptExtensionProvider.JAX_WS_STYLESHEET_RESOURCE);
            AntBuildExtender.Extension extension = ext.getExtension(JaxWsBuildScriptExtensionProvider.JAXWS_EXTENSION);
            if (extension!=null) {
                extension.removeDependency("-do-compile", "wsimport-client-compile"); //NOI18N
                extension.removeDependency("-do-compile-single", "wsimport-client-compile"); //NOI18N
                extension.removeDependency("-do-compile", "wsimport-service-compile"); //NOI18N
                extension.removeDependency("-do-compile-single", "wsimport-service-compile"); //NOI18N
                ProjectManager.getDefault().saveProject(prj);
            }
        }

    }
}
