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

package org.netbeans.modules.maven.jaxws;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportFactory;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportImpl;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider;

import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.spi.LookupMergerSupport;
import org.netbeans.modules.websvc.project.spi.WebServiceFactory;
import org.netbeans.modules.websvc.project.spi.WebServiceDataProvider;

import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(projectType="org-netbeans-modules-maven", 
        service={JAXWSLightSupportProvider.class, WebServiceDataProvider.class, ProjectOpenedHook.class})
public class MavenJaxWsSupportProvider extends ProjectOpenedHook 
    implements JAXWSLightSupportProvider, WebServiceDataProvider, PropertyChangeListener
{

    private static final RequestProcessor MAVEN_WS_RP =
            new RequestProcessor("MavenJaxWsSupportProvider.WS_REQUEST_PROCESSOR"); //NOI18N
    
    private static final RequestProcessor rp = 
            new RequestProcessor("MavenJaxWsSupportProvider-request-processor");    // NOI18N

    private RequestProcessor.Task pomChangesTask = MAVEN_WS_RP.create(new Runnable() {

        @Override
        public void run() {
            reactOnPomChanges();
        }
    });
    
    private static final Logger LOG = Logger.getLogger(MavenJaxWsSupportProvider.class.getName());

    private JAXWSLightSupport jaxWsSupport;
    private PropertyChangeListener pcl;
    private NbMavenProject mp;
    private Project prj;
    private volatile String serverInstance; 
    //private MetadataModel<WebservicesMetadata> wsModel;
    
    private List<WebService> providers = new LinkedList<WebService>();
    private List<WebService> consumers = new LinkedList<WebService>();

    public MavenJaxWsSupportProvider(final Project prj) {
        JAXWSLightSupportImpl spiJAXWSSupport = new MavenJAXWSSupportImpl(prj);
        this.prj = prj;
        this.jaxWsSupport = JAXWSLightSupportFactory.createJAXWSSupport(spiJAXWSSupport);
        
        jaxWsSupport.addPropertyChangeListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.spi.project.ui.ProjectOpenedHook#projectClosed()
     */
    @Override
    protected void projectClosed() {
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.project.ui.ProjectOpenedHook#projectOpened()
     */
    @Override
    protected void projectOpened() {
        
        MAVEN_WS_RP.post(new Runnable() {

            public void run() {
                registerPCL();
                J2eeModuleProvider provider = prj.getLookup().lookup(
                        J2eeModuleProvider.class);

                MetadataModel<WebservicesMetadata> model = jaxWsSupport
                        .getWebservicesMetadataModel();
                if (model != null) {
                    registerAnnotationListener(model);
                }
                serverInstance = provider == null ? null : provider
                        .getServerInstanceID();
                // wsModel = model;
            }
        });
        rp.post(new Runnable() {

            @Override
            public void run() {
                WSUtils.detectWsdlClients(prj, jaxWsSupport);
            }

        });
    }

    @Override
    public JAXWSLightSupport findJAXWSSupport() {
        return jaxWsSupport;
    }
    
    @Override
    public List<WebService> getServiceProviders() {
        return providers;
    }

    @Override
    public List<WebService> getServiceConsumers() {
        return consumers;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        jaxWsSupport.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        jaxWsSupport.removePropertyChangeListener(pcl);
    }
    
    @LookupMerger.Registration(projectType="org-netbeans-modules-maven")
    public static LookupMerger<WebServiceDataProvider> getLookupMerger(){
        return LookupMergerSupport.createWebServiceDataProviderMerger();
    }

    void registerPCL() {
        unregisterPCL();
        mp = prj.getLookup().lookup(NbMavenProject.class);
        mp.addPropertyChangeListener(this);
    }

    void registerAnnotationListener(final MetadataModel<WebservicesMetadata> wsModel) {
        try {
            wsModel.runReadActionWhenReady(new MetadataModelAction<WebservicesMetadata, Void>() {

                @Override
                public Void run(final WebservicesMetadata metadata) {
                    Webservices webServices = metadata.getRoot();
                    if (pcl != null) {
                        webServices.removePropertyChangeListener(pcl);
                    }
                    pcl = new WebservicesChangeListener(jaxWsSupport, wsModel);
                    webServices.addPropertyChangeListener(pcl);
                    return null;
                }
            });
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    void unregisterPCL() {
        if (mp != null) {
            mp.removePropertyChangeListener(this);
        }
    }

    /*void unregisterAnnotationListener() {
        if (pcl != null) {
            if (wsModel != null) {
                try {
                    wsModel.runReadActionWhenReady(new MetadataModelAction<WebservicesMetadata, Void>() {

                        public Void run(final WebservicesMetadata metadata) {
                            Webservices webServices = metadata.getRoot();
                            webServices.removePropertyChangeListener(pcl);
                            return null;
                        }
                    });
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }*/

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
            pomChangesTask.schedule(1000);
        }
        else if (JAXWSLightSupport.PROPERTY_SERVICE_ADDED.equals(evt.getPropertyName())) {
            MavenWebService mavenService = new MavenWebService((JaxWsService) evt.getNewValue(), prj);
            WebService webService = WebServiceFactory.createWebService(mavenService);
            if (webService.isServiceProvider()) {
                providers.add(webService);
            } else {
                consumers.add(webService);
            }
        } 
        else if (JAXWSLightSupport.PROPERTY_SERVICE_REMOVED.equals(evt.getPropertyName())) {
            JaxWsService jaxWsService = (JaxWsService) evt.getOldValue();
            if (jaxWsService.isServiceProvider()) {
                String implClass = jaxWsService.getImplementationClass();
                for (WebService service : providers) {
                    if (implClass.equals(service.getIdentifier())) {
                        providers.remove(service);
                        break;
                    }
                }
            } else {
                String clientId = jaxWsService.getId();
                for (WebService client : consumers) {
                    if (clientId != null && clientId.equals(client.getIdentifier())) {
                        consumers.remove(client);
                        break;
                    }
                }
            }
        }
    }

    private void reactOnPomChanges() {
        WSUtils.updateClients(prj, jaxWsSupport);
        List<JaxWsService> services = jaxWsSupport.getServices();
        if (services.size() > 0) {
            J2eeModuleProvider provider = prj.getLookup().lookup( 
                    J2eeModuleProvider.class);
            String serverInstanceID = provider== null ? null : 
                provider.getServerInstanceID();
            boolean instanceChanged = false;
            if ( serverInstanceID == null ){
                if ( serverInstance != null ){
                    instanceChanged = true;
                }
            }
            else if (!serverInstanceID.equals( serverInstance)){
                instanceChanged = true;
            }
            if ( instanceChanged ){
                serverInstance = serverInstanceID;
                MavenModelUtils.reactOnServerChanges(prj);
            }
            if (WSUtils.isWeb(prj)) {
                for (JaxWsService s : services) {
                    if (s.isServiceProvider()) {
                        // add|remove sun-jaxws.xml and WS entries to web.xml file
                        // depending on selected target server
                        WSUtils.checkNonJSR109Entries(prj);
                        break;
                    }
                }
            }
        }
    }

    private class WebservicesChangeListener implements PropertyChangeListener {

        private JAXWSLightSupport jaxWsSupport;

        private MetadataModel<WebservicesMetadata> wsModel;

        private RequestProcessor.Task updateJaxWsTask = MAVEN_WS_RP.create(new Runnable() {

            @Override
            public void run() {
                updateJaxWs();
            }
        });

        WebservicesChangeListener(JAXWSLightSupport jaxWsSupport, MetadataModel<WebservicesMetadata> wsModel) {
            this.jaxWsSupport = jaxWsSupport;
            this.wsModel = wsModel;
            updateJaxWsTask.schedule(1000);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //requestModelUpdate();
            updateJaxWsTask.schedule(1000);
        }
        
        private void updateJaxWs() {
            try {
                final Map<String, ServiceInfo> newServices = wsModel.runReadAction(
                        new MetadataModelAction<WebservicesMetadata, Map<String, ServiceInfo>>() {

                    @Override
                    public Map<String, ServiceInfo> run(WebservicesMetadata metadata) {
                        Map<String, ServiceInfo> result = new HashMap<String, ServiceInfo>();
                        Webservices webServices = metadata.getRoot();
                        for (WebserviceDescription wsDesc : webServices.getWebserviceDescription()) {
                            PortComponent[] ports = wsDesc.getPortComponent();
                            for (PortComponent port : ports) {
                                // key = imlpementation class package name
                                // value = service name
                                String implClass = port.getDisplayName();
                                if (WSUtils.isInSourceGroup(prj, implClass)) {
                                    QName portName = port.getWsdlPort();
                                    result.put(implClass,
                                    new ServiceInfo(
                                            wsDesc.getWebserviceDescriptionName(),
                                            (portName == null ? null : portName.getLocalPart()),
                                            implClass,
                                            wsDesc.getWsdlFile()));
                                }
                            }

                        }
                        return result;
                    }
                });
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        doUpdateJaxWs(newServices);        
                    }
                };
                jaxWsSupport.runAtomic(runnable);
                
            } catch (java.io.IOException ioe) {
                ioe.printStackTrace();
            }

        }

        private void doUpdateJaxWs( Map<String, ServiceInfo> newServices ) {
            List<JaxWsService> oldJaxWsServices = jaxWsSupport.getServices();
            Map<String, JaxWsService> oldServices = new HashMap<String, JaxWsService>();

            for (JaxWsService s : oldJaxWsServices) {
                // implementationClass -> Service
                if (s.isServiceProvider()) {
                    oldServices.put(s.getImplementationClass(), s);
                }
            }
            // compare new services with existing
            // looking for common services (implementationClass)
            Set<String> commonServices = new HashSet<String>();
            Set<String> keys1 = oldServices.keySet();
            Set<String> keys2 = newServices.keySet();
            for (String key : keys1) {
                if (keys2.contains(key)) {
                    commonServices.add(key);
                }
            }

            for (String key : commonServices) {
                oldServices.remove(key);
                newServices.remove(key);
            }

            // remove old services
            boolean needToSave = false;
            for (JaxWsService jaxWsService : oldServices.values()) {
                jaxWsSupport.removeService(jaxWsService);
            }
            // add new services
            for (Map.Entry<String, ServiceInfo> entry : newServices.entrySet()) {
                String key = entry.getKey();
                ServiceInfo serviceInfo = entry.getValue();
                String wsdlLocation = serviceInfo.getWsdlLocation();
                JaxWsService service = new JaxWsService(serviceInfo.getServiceName(), key);
                if (wsdlLocation != null && wsdlLocation.length() > 0) {
                    service.setWsdlLocation(wsdlLocation);
                    if (wsdlLocation.startsWith("WEB-INF/wsdl/")) {
                        service.setLocalWsdl(wsdlLocation.substring(13));
                    } else if (wsdlLocation.startsWith("META-INF/wsdl/")) {
                        service.setLocalWsdl(wsdlLocation.substring(14));
                    } else {
                        service.setLocalWsdl(wsdlLocation);
                    }
                    FileObject wsdlFo = WSUtils.getLocalWsdl(jaxWsSupport, service.getLocalWsdl());
                    if (wsdlFo != null) {
                        service.setId(WSUtils.getUniqueId(wsdlFo.getName(), oldJaxWsServices));
                    }
                    service.setWsdlUrl(WSUtils.getOriginalWsdlUrl(prj, service.getId(), true));
                }
                service.setPortName(serviceInfo.getPortName());
                jaxWsSupport.addService(service);
            }
        }
        
        
    }

    private static class ServiceInfo {
        private String serviceName;
        private String portName;
        private String implClass;
        private String wsdlLocation;

        public ServiceInfo(String serviceName, String portName, String implClass, String wsdlLocation) {
            this.serviceName = serviceName;
            this.portName = portName;
            this.implClass = implClass;
            this.wsdlLocation = wsdlLocation;
        }

        public String getImplClass() {
            return implClass;
        }

        public void setImplClass(String implClass) {
            this.implClass = implClass;
        }

        public String getPortName() {
            return portName;
        }

        public void setPortName(String portName) {
            this.portName = portName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getWsdlLocation() {
            return wsdlLocation;
        }

        public void setWsdlLocation(String wsdlLocation) {
            this.wsdlLocation = wsdlLocation;
        }
    }

}
