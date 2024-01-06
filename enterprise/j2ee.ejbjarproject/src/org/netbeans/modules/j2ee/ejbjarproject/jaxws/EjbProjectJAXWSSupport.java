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

package org.netbeans.modules.j2ee.ejbjarproject.jaxws;

import java.io.IOException;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.api.jaxws.project.LogUtils;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.spi.ProjectJAXWSSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 *
 * @author mkuchtiak
 */
public class EjbProjectJAXWSSupport extends ProjectJAXWSSupport /*implements JAXWSSupportImpl*/ {
    private EjbJarProject project;
    
    /** Creates a new instance of JAXWSSupport */
    public EjbProjectJAXWSSupport(EjbJarProject project, AntProjectHelper antProjectHelper) {
        super(project,antProjectHelper);
        this.project = project;
    }

    public FileObject getWsdlFolder(boolean create) throws java.io.IOException {
        EjbJar ejbModule = EjbJar.getEjbJar(project.getProjectDirectory());
        if (ejbModule!=null) {
            FileObject metaInfFo = ejbModule.getMetaInf();
            if (metaInfFo!=null) {
                FileObject wsdlFo = metaInfFo.getFileObject("wsdl"); //NOI18N
                if (wsdlFo!=null) {
                    return wsdlFo;
                }
                else if (create) {
                    return metaInfFo.createFolder("wsdl"); //NOI18N
                }
            }
        }
        return null;
    }
    
    /** Get wsdlLocation information 
     * Useful for web service from wsdl
     * @param name service "display" name
     */
    public String getWsdlLocation(String serviceName) {
        String localWsdl = serviceName+".wsdl"; //NOI18N
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Service service = jaxWsModel.findServiceByName(serviceName);
            if (service!=null) {
                String localWsdlFile = service.getLocalWsdlFile();
                if (localWsdlFile!=null) {
                    localWsdl = localWsdlFile;
                }
            }
        }
        String prefix = "META-INF/wsdl/"; //NOI18N
        return prefix+serviceName+"/"+localWsdl; //NOI18N
    }

    public FileObject getDeploymentDescriptorFolder() {
        EjbJar ejbModule = EjbJar.getEjbJar(project.getProjectDirectory());
        if (ejbModule!=null) {
            return ejbModule.getMetaInf();
        }   
        return null;
    }

    protected void addJaxwsArtifacts(Project project, String wsName, String serviceImpl) throws Exception {
    }
    
    /** return root folder for xml artifacts
     */
    @Override
    protected FileObject getXmlArtifactsRoot() {
        return project.getAPIEjbJar().getMetaInf();
    }

    public void removeNonJsr109Entries(String serviceName) throws IOException {
        //noop since nonJSR 109 web services are not supported in the EJB module
    }

    @Override
public String addService(String name, String serviceImpl, String wsdlUrl, String serviceName, 
            String portName, String packageName, boolean isJsr109, boolean useProvider) {
        // create jax-ws.xml if necessary
        FileObject fo = WSUtils.findJaxWsFileObject(project);
        if (fo==null) {
            try {
                WSUtils.createJaxWsFileObject(project);
                // logging first service creation
                logWsDetected();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return super.addService(name, serviceImpl, wsdlUrl, serviceName, portName, packageName, isJsr109, useProvider);
    }

    @Override
    public void addService(String serviceName, String serviceImpl, boolean isJsr109) {
        // create jax-ws.xml if necessary
        FileObject fo = WSUtils.findJaxWsFileObject(project);
        if (fo==null) {
            try {
                WSUtils.createJaxWsFileObject(project);
                // logging first service creation
                logWsDetected();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        super.addService(serviceName, serviceImpl, isJsr109);
    }

    public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        return project.getEjbModule().getWebservicesMetadataModel();
    }

    private void logWsDetected() {
        // logging jax-ws.xml creation (web service is detected first time in project)
        Object[] params = new Object[3];
        params[0] = LogUtils.WS_STACK_JAXWS;
        params[1] = project.getClass().getName();
        params[2] = "SERVICE"; // NOI18N
        LogUtils.logWsDetect(params);
    }

    @Override
    protected String getProjectJavaEEVersion() {
        EjbJar ejbModule = EjbJar.getEjbJar(project.getProjectDirectory());
        if (ejbModule != null) {
            switch (ejbModule.getJ2eeProfile()) {
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
