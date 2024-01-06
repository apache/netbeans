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

package org.netbeans.modules.web.project.jaxws;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.spi.jaxws.client.ProjectJAXWSClientSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class WebProjectJAXWSClientSupport extends ProjectJAXWSClientSupport /*implements JAXWSClientSupportImpl*/ {
    WebProject project;

    /** Creates a new instance of WebProjectJAXWSClientSupport */
    public WebProjectJAXWSClientSupport(WebProject project,AntProjectHelper helper) {
        super(project,helper);
        this.project=project;
    }

    public FileObject getWsdlFolder(boolean create) throws IOException {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule!=null) {
            FileObject webInfFo = webModule.getWebInf();
            if (webInfFo!=null) {
                FileObject wsdlFo = webInfFo.getFileObject("wsdl"); //NOI18N
                if (wsdlFo!=null) return wsdlFo;
                else if (create) {
                    return webInfFo.createFolder("wsdl"); //NOI18N
                }
            }
        }
        return null;
    }

    @Override
    protected void addJaxWs20Library() throws Exception{
        SourceGroup[] sgs = SourceGroups.getJavaSourceGroups(project);
        if (sgs.length > 0) {
            FileObject srcRoot = sgs[0].getRootFolder();

            ClassPath compileClassPath = ClassPath.getClassPath(srcRoot,ClassPath.COMPILE);
            ClassPath bootClassPath = ClassPath.getClassPath(srcRoot,ClassPath.BOOT);
            ClassPath classPath = ClassPathSupport.createProxyClassPath(new ClassPath[]{compileClassPath, bootClassPath});
            FileObject jaxWsClass = classPath.findResource("javax/xml/ws/WebServiceFeature.class"); // NOI18N

            if (jaxWsClass == null) {
                //Add the jaxws21 library to the project to be packed with the archive
                Library MetroLib = LibraryManager.getDefault().getLibrary("metro"); //NOI18N
                if (MetroLib != null) {
                    try {
                        ProjectClassPathModifier.addLibraries(new Library[]{MetroLib}, srcRoot, ClassPath.COMPILE);
                    } catch(IOException e){
                        throw new Exception("Unable to add Metro library", e);
                    }
                } else {
                    throw new Exception("Unable to add Metro Library" ); //NOI18N
                }
            }
            // add JAX-WS Endorsed Classpath
            try {
                WSUtils.addJaxWsApiEndorsed(project, srcRoot);
            } catch (IOException ex) {
                Logger.getLogger(WebProjectJAXWSClientSupport.class.getName()).log(Level.FINE, "Cannot add JAX-WS-ENDORSED classpath", ex);
            }
        }
    }

    /** return root folder for xml artifacts
     */
    @Override
    protected FileObject getXmlArtifactsRoot() {
        FileObject confDir = project.getWebModule().getConfDir();
        return confDir == null ? super.getXmlArtifactsRoot():confDir;
    }

    @Override
    protected String getProjectJavaEEVersion() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            switch (webModule.getJ2eeProfile()) {
                case JAVA_EE_6_WEB:
                case JAVA_EE_6_FULL:
                    return JAVA_EE_VERSION_16;
                case JAVA_EE_7_WEB:
                case JAVA_EE_7_FULL:
                    return JAVA_EE_VERSION_17;
                case JAVA_EE_8_WEB:
                case JAVA_EE_8_FULL:
                    return JAVA_EE_VERSION_18;
                case JAKARTA_EE_8_WEB:
                case JAKARTA_EE_8_FULL:
                    return JAKARTA_EE_VERSION_8;
                case JAKARTA_EE_9_WEB:
                case JAKARTA_EE_9_FULL:
                    return JAKARTA_EE_VERSION_9;
                case JAKARTA_EE_9_1_WEB:
                case JAKARTA_EE_9_1_FULL:
                    return JAKARTA_EE_VERSION_91;
                case JAKARTA_EE_10_WEB:
                case JAKARTA_EE_10_FULL:
                    return JAKARTA_EE_VERSION_10;
                case JAKARTA_EE_11_WEB:
                case JAKARTA_EE_11_FULL:
                    return JAKARTA_EE_VERSION_11;
                case JAVA_EE_5:
                    return JAVA_EE_VERSION_15;
                default:
                    break;
            }
        }
        return JAVA_EE_VERSION_NONE;
    }
}
