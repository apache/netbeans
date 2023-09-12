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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.spi.jaxws.client.ProjectJAXWSClientSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;


/**
 *
 * @author mkuchtiak
 */
public class EjbProjectJAXWSClientSupport extends ProjectJAXWSClientSupport/* implements JAXWSClientSupportImpl*/ {
    private EjbJarProject project;
    
    /** Creates a new instance of WebProjectJAXWSClientSupport */
    public EjbProjectJAXWSClientSupport(EjbJarProject project,AntProjectHelper helper) {
        super(project,helper);
        this.project=project;    
    }
    
    public FileObject getWsdlFolder(boolean create) throws IOException {
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

    protected void addJaxWs20Library() throws Exception {
        // add JAX-WS Endorsed Classpath
        SourceGroup[] sgs = SourceGroups.getJavaSourceGroups(project);
        if (sgs.length > 0) {
            try {
                FileObject srcRoot = sgs[0].getRootFolder();
                WSUtils.addJaxWsApiEndorsed(project, srcRoot);
            } catch (java.io.IOException ex) {
                Logger.getLogger(EjbProjectJAXWSClientSupport.class.getName()).log(Level.FINE, "Cannot add JAX-WS-ENDORSED classpath", ex);
            }
        }
    }
    
    /** return root folder for xml artifacts
     */
    @Override
    protected FileObject getXmlArtifactsRoot() {
        return project.getAPIEjbJar().getMetaInf();
    }

    @Override
    protected String getProjectJavaEEVersion() {
        EjbJar ejbModule = EjbJar.getEjbJar(project.getProjectDirectory());
        if (ejbModule != null) {
            if (Profile.JAVA_EE_6_WEB.equals(ejbModule.getJ2eeProfile())) {
                return JAVA_EE_VERSION_16;
            } else if (Profile.JAVA_EE_6_FULL.equals(ejbModule.getJ2eeProfile())) {
                return JAVA_EE_VERSION_16;
            } else if (Profile.JAVA_EE_7_WEB.equals(ejbModule.getJ2eeProfile())) {
                return JAVA_EE_VERSION_17;
            } else if (Profile.JAVA_EE_7_FULL.equals(ejbModule.getJ2eeProfile())) {
                return JAVA_EE_VERSION_17;
            } else if (Profile.JAVA_EE_8_WEB.equals(ejbModule.getJ2eeProfile())) {
                return JAVA_EE_VERSION_18;
            } else if (Profile.JAVA_EE_8_FULL.equals(ejbModule.getJ2eeProfile())) {
                return JAVA_EE_VERSION_18;
            } else if (Profile.JAKARTA_EE_8_WEB.equals(ejbModule.getJ2eeProfile())) {
                return JAKARTA_EE_VERSION_8;
            } else if (Profile.JAKARTA_EE_8_FULL.equals(ejbModule.getJ2eeProfile())) {
                return JAKARTA_EE_VERSION_8;
            } else if (Profile.JAKARTA_EE_9_WEB.equals(ejbModule.getJ2eeProfile())) {
                return JAKARTA_EE_VERSION_9;
            } else if (Profile.JAKARTA_EE_9_FULL.equals(ejbModule.getJ2eeProfile())) {
                return JAKARTA_EE_VERSION_9;
            } else if (Profile.JAKARTA_EE_9_1_WEB.equals(ejbModule.getJ2eeProfile())) {
                return JAKARTA_EE_VERSION_91;
            } else if (Profile.JAKARTA_EE_9_1_FULL.equals(ejbModule.getJ2eeProfile())) {
                return JAKARTA_EE_VERSION_91;
            } else if (Profile.JAKARTA_EE_10_WEB.equals(ejbModule.getJ2eeProfile())) {
                return JAKARTA_EE_VERSION_10;
            } else if (Profile.JAKARTA_EE_10_FULL.equals(ejbModule.getJ2eeProfile())) {
                return JAKARTA_EE_VERSION_10;
            } else if (Profile.JAVA_EE_5.equals(ejbModule.getJ2eeProfile())) {
                return JAVA_EE_VERSION_15;
            }
        }
        return JAVA_EE_VERSION_NONE;
    }
}
