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
package org.netbeans.modules.websvc.core.jaxws.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.ServiceAlreadyExistsExeption;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType={
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-j2ee-ejbjarproject",
    "org-netbeans-modules-j2ee-clientproject"
})
public class JavaEEWSOpenHook extends ProjectOpenedHook {

    private static final RequestProcessor METADATA_MODEL_RP =
            new RequestProcessor("JavaEEWSOpenHook.WS_REQUEST_PROCESSOR"); //NOI18N

    private static final RequestProcessor PROJECT_UPDATE_RP =
            new RequestProcessor("JavaEEWSOpenHook.WS_PROJECT_UPDATE"); //NOI18N

    private final Project prj;
    public JavaEEWSOpenHook(Project prj) {
        this.prj = prj;
    }

            PropertyChangeListener pcl;

            protected void projectOpened() {
                JAXWSSupport support = JAXWSSupport.getJAXWSSupport(prj.getProjectDirectory());
                if (support != null && (JaxWsUtils.isEjbJavaEE5orHigher(prj) || ProjectUtil.isJavaEE5orHigher(prj))) {
                    final MetadataModel<WebservicesMetadata> wsModel = support.getWebservicesMetadataModel();
                    try {
                        wsModel.runReadActionWhenReady(new MetadataModelAction<WebservicesMetadata, Void>() {

                            public Void run(final WebservicesMetadata metadata) {
                                Webservices webServices = metadata.getRoot();
                                pcl = new WebservicesChangeListener(wsModel, prj);
                                webServices.addPropertyChangeListener(pcl);
                                return null;
                            }
                        });
                    } catch (java.io.IOException ex) {

                    }
                }
                FileObject jaxWsFo = WSUtils.findJaxWsFileObject(prj);
                try {
                    if (jaxWsFo != null && WSUtils.hasClients(jaxWsFo)) {
                        final JAXWSClientSupport jaxWsClientSupport = prj.getLookup().lookup(JAXWSClientSupport.class);
                        if (jaxWsClientSupport != null) {
                            FileObject wsdlFolder = null;
                            try {
                                wsdlFolder = jaxWsClientSupport.getWsdlFolder(false);
                            } catch (IOException ex) {}
                            if (wsdlFolder == null || wsdlFolder.getParent().getFileObject("jax-ws-catalog.xml") == null) { //NOI18N
                                RequestProcessor.getDefault().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JaxWsCatalogPanel.generateJaxWsCatalog(prj, jaxWsClientSupport);
                                        } catch (IOException ex) {
                                            Logger.getLogger(JaxWsCatalogPanel.class.getName()).log(Level.WARNING, "Cannot create jax-ws-catalog.xml", ex);
                                        }
                                    }
                                });
                            }
                        }
                    }
                } catch (IOException ex) {
                     Logger.getLogger(JavaEEWSOpenHook.class.getName()).log(Level.WARNING, "Cannot read nbproject/jax-ws.xml file", ex);
                }
            }

            protected void projectClosed() {
                JAXWSSupport support = JAXWSSupport.getJAXWSSupport(prj.getProjectDirectory());
                if (support != null && (JaxWsUtils.isEjbJavaEE5orHigher(prj) || ProjectUtil.isJavaEE5orHigher(prj))) {
                    final MetadataModel<WebservicesMetadata> wsModel = support.getWebservicesMetadataModel();
                    try {
                        wsModel.runReadActionWhenReady(new MetadataModelAction<WebservicesMetadata, Void>() {

                            public Void run(final WebservicesMetadata metadata) {
                                Webservices webServices = metadata.getRoot();
                                webServices.removePropertyChangeListener(pcl);
                                return null;
                            }
                        });
                    } catch (java.io.IOException ex) {

                    }
                }
            }

    private static class WebservicesChangeListener implements PropertyChangeListener {

        MetadataModel<WebservicesMetadata> wsModel;
        Project prj;
        private RequestProcessor.Task updateJaxWsTask = METADATA_MODEL_RP.create(new Runnable() {

            public void run() {
                updateJaxWs();
            }
        });

        WebservicesChangeListener(MetadataModel<WebservicesMetadata> wsModel, Project prj) {
            this.wsModel = wsModel;
            this.prj = prj;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            updateJaxWsTask.schedule(1000);
        }

        private void updateJaxWs() {
            try {
                wsModel.runReadActionWhenReady(new MetadataModelAction<WebservicesMetadata, Void>() {

                    public Void run(WebservicesMetadata metadata) {
                        Map<String, String> result = new HashMap<String, String>();
                        Webservices webServices = metadata.getRoot();
                        for (WebserviceDescription wsDesc : webServices.getWebserviceDescription()) {
                            PortComponent[] ports = wsDesc.getPortComponent();
                            for (PortComponent port : ports) {
                                if ("javax.xml.ws.WebServiceProvider".equals(wsDesc.getDisplayName())) { //NOI18N
                                    result.put("fromWsdl:"+wsDesc.getWebserviceDescriptionName(), port.getDisplayName()); //NOI18N
                                } else if (JaxWsUtils.isInSourceGroup(prj, port.getServiceEndpointInterface())) {
                                    result.put(port.getDisplayName(), port.getPortComponentName());
                                } else if (wsDesc.getWsdlFile() != null) {
                                    result.put("fromWsdl:"+wsDesc.getWebserviceDescriptionName(), port.getDisplayName()); //NOI18N
                                }
                            }

                        }
                        updateWsModel( result );
                        return null;
                    }
                });

            } catch (java.io.IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        
        private void updateWsModel(Map<String, String> services){
            final JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel != null) {
                // create list of all existing services (from java)
                Map<String, Service> oldServices = new HashMap<String, Service>();
                Map<String, Service> oldServicesFromWsdl = new HashMap<String, Service>();
                Service[] allServices = jaxWsModel.getServices();

                for (Service s: allServices) {
                    // add only services created from java
                    if (s.getWsdlUrl() == null) {
                        // implementationClass -> Service
                        oldServices.put(s.getImplementationClass(), s);
                    } else {
                        // serviceName -> Service
                        oldServicesFromWsdl.put("fromWsdl:"+s.getServiceName(), s); //NOI18N
                    }
                }
                // compare new services with existing

                // looking for common services (implementationClass)
                Set<String> commonServices = new HashSet<String>();
                Set<String> keys1 = oldServices.keySet();
                Set<String> keys2 = services.keySet();
                for (String key : keys1) {
                    if (keys2.contains(key)) {
                        commonServices.add(key);
                    }
                }

                for (String key : commonServices) {
                    oldServices.remove(key);
                    services.remove(key);
                }
                
                // remove old services
                boolean needToSave = false;
                for (Service service : oldServices.values()) {
                    jaxWsModel.removeService(service.getName());
                    needToSave = true;
                }
                Set<String> removedFromWsdl = new HashSet<String>( 
                        oldServicesFromWsdl.keySet());
                removedFromWsdl.removeAll( services.keySet() );
                for( String key : removedFromWsdl ){
                    Service service = oldServicesFromWsdl.remove(key);
                    if ( service != null ){
                        jaxWsModel.removeService(service.getName());
                    }
                }

                
                // add new services
                for (Map.Entry<String, String> it : services.entrySet()) { // services from WSDL
                    String key = it.getKey();

                    if (key.startsWith("fromWsdl:")) { //NOI18N
                        Service oldServiceFromWsdl = oldServicesFromWsdl.get(key);
                        String newImplClass = it.getValue();
                        if (oldServiceFromWsdl != null && !oldServiceFromWsdl.getImplementationClass().equals(newImplClass)) {
                            oldServiceFromWsdl.setImplementationClass(newImplClass);
                            needToSave = true;
                        }
                    } else { // services from JAVA
                        // add only if doesn't exists
                        if (jaxWsModel.findServiceByImplementationClass(key) == null) {
                            try {
                                jaxWsModel.addService(it.getValue(), key);
                                needToSave = true;
                            } catch (ServiceAlreadyExistsExeption ex) {
                            // TODO: need to handle this
                            }
                        }
                    }
                }
                if (needToSave) {
                    PROJECT_UPDATE_RP.post(new Runnable() {
                        @Override
                        public void run() {
                            ProjectManager.mutex().writeAccess(new Runnable() {

                                public void run() {
                                    try {
                                        jaxWsModel.write();
                                    } catch (IOException ex) {
                                        ErrorManager.getDefault().notify(ex);
                                    }
                                }
                            });
                        }
                    });

                }
            }
        }
    }

}
