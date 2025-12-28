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

package org.netbeans.modules.j2ee.clientproject.wsclient;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.spi.jaxws.client.ProjectJAXWSClientSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class AppClientProjectJAXWSClientSupport extends ProjectJAXWSClientSupport /*implements JAXWSClientSupportImpl*/ {
    AppClientProject project;

    /**
     * Creates a new instance of AppClientProjectJAXWSClientSupport
     */
    public AppClientProjectJAXWSClientSupport(AppClientProject project, AntProjectHelper antProjectHelper) {
        super(project, antProjectHelper);
        this.project=project;
    }

    public FileObject getWsdlFolder(boolean create) throws IOException {
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        Car carModule = Car.getCar(project.getProjectDirectory());
        if (carModule!=null) {
            FileObject webInfFo = carModule.getMetaInf();
            if (webInfFo!=null) {
                FileObject wsdlFo = webInfFo.getFileObject("wsdl"); //NOI18N
                if (wsdlFo!=null) {
                    return wsdlFo;
                } else if (create) {
                    return webInfFo.createFolder("wsdl"); //NOI18N
                }
            }
        }
        return null;
    }

    protected void addJaxWs20Library() throws Exception {
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length > 0) {
            try {
                FileObject srcRoot = sgs[0].getRootFolder();
                WSUtils.addJaxWsApiEndorsed(project, srcRoot);
            } catch (java.io.IOException ex) {
                Logger.getLogger(AppClientProjectJAXWSClientSupport.class.getName()).log(Level.FINE, "Cannot add JAX-WS-ENDORSED classpath", ex);
            }
        }
    }

    /** return root folder for xml artifacts
     */
    @Override
    protected FileObject getXmlArtifactsRoot() {
        return project.getCarModule().getMetaInf();
    }

    @Override
    protected String getProjectJavaEEVersion() {
        Car j2eeClientModule = Car.getCar(project.getProjectDirectory());
        if (j2eeClientModule != null) {
            switch (j2eeClientModule.getJ2eeProfile()) {
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
