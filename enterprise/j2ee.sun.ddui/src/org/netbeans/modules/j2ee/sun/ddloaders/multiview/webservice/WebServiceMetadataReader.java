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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;


/**
 *
 * @author Peter Williams
 */
public class WebServiceMetadataReader extends CommonBeanReader {

    public WebServiceMetadataReader() {
        super(DDBinding.PROP_WEBSERVICE_DESC);
    }
    
    @Override
    public Map<String, Object> readAnnotations(DataObject dObj) {
        Map<String, Object> result = null;
        try {
            File key = FileUtil.toFile(dObj.getPrimaryFile());
            GlassfishConfiguration dc = GlassfishConfiguration.getConfiguration(key);
            if(dc != null) {
                J2eeModule module = dc.getJ2eeModule();
                if(module != null) {
                    Object moduleType = module.getType();
                    if(J2eeModule.Type.WAR.equals(moduleType) || J2eeModule.Type.EJB.equals(moduleType)) {
                        result = readWebservicesMetadata(module.getMetadataModel(WebservicesMetadata.class));
                        if(result != null && result.size() > 0) {
                            filterInvalidServices(result, moduleType);
                        }
                    }
                }
            }
        } catch(MetadataModelException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return result;
    }
    
    /**
     * Examine map of annotated web services we have detailed.  If any of the
     * services contains ports of the wrong type (e.g ejb hosted service located
     * in web app metadata, see IZ 118314), remove those services from the
     * annotation map so they are not displayed and configurable.
     * 
     * @param annotationMap Organized details of the annotated web services 
     *   described by the metadata for this module.
     * @param moduleType Type of module being examined (WAR or EJB JAR only)
     */
    private void filterInvalidServices(Map<String, Object> annotationMap, Object moduleType) {
        String invalidBindingType = J2eeModule.Type.WAR.equals(moduleType) ? DDBinding.PROP_EJB_LINK : DDBinding.PROP_SERVLET_LINK;
        
        List<String> servicesToRemove = new ArrayList<String>();
        for(Map.Entry<String, Object> serviceEntry: annotationMap.entrySet()) {
            Object serviceValue = serviceEntry.getValue();
            if(serviceValue instanceof Map<?, ?>) {
                Map<String, Object> serviceDescMap = (Map<String, Object>) serviceValue;
                Object pm = serviceDescMap.get(DDBinding.PROP_PORTCOMPONENT);
                if(pm instanceof Map<?, ?>) {
                    Map<String, Object> portMap = (Map<String, Object>) pm;
                    for(Map.Entry<String, Object> portEntry: portMap.entrySet()) {
                        Object portValue = portEntry.getValue();
                        if(portValue instanceof Map<?, ?>) {
                            Map<String, Object> portDescMap = (Map<String, Object>) portValue;
                            if(portDescMap.get(invalidBindingType) != null) {
                                // This port references an invalid binding so mark
                                // the owning service for removal.
                                servicesToRemove.add(serviceEntry.getKey());
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        annotationMap.keySet().removeAll(servicesToRemove);
    }
    
    /** Maps interesting fields from service-ref descriptor to a multi-level property map.
     * 
     * @param beans Array of WebserviceDescription DDBeans to extract data from.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and so forth, ad infinitum)
     */
    public Map<String, Object> genProperties(CommonDDBean [] beans) {
        Map<String, Object> result = null;
        if(beans instanceof WebserviceDescription []) {
            WebserviceDescription [] webServices = (WebserviceDescription []) beans;
            for(WebserviceDescription webServiceDesc: webServices) {
                String webServiceDescName = webServiceDesc.getWebserviceDescriptionName();
                if(Utils.notEmpty(webServiceDescName)) {
                    if(result == null) {
                        result = new HashMap<String, Object>();
                    }
                    Map<String, Object> webServiceDescMap = new HashMap<String, Object>();
                    result.put(webServiceDescName, webServiceDescMap);
                    webServiceDescMap.put(DDBinding.PROP_NAME, webServiceDescName);
                    
                    PortComponent [] ports = webServiceDesc.getPortComponent();
                    if(ports != null && ports.length > 0) {
                        Map<String, Object> portGroupMap = new HashMap<String, Object>();
                        webServiceDescMap.put(DDBinding.PROP_PORTCOMPONENT, portGroupMap);
                        for(PortComponent port: ports) {
                            String portName = port.getPortComponentName();
                            if(Utils.notEmpty(portName)) {
                                Map<String, Object> portMap = new HashMap<String, Object>(7);
                                portMap.put(DDBinding.PROP_NAME, portName);
                                portGroupMap.put(portName, portMap);
                                
                                addMapString(portMap, DDBinding.PROP_SEI, port.getServiceEndpointInterface());

                                // Wsdl port is actually 3 fields wrapped in a QName.  Do we really need it?
//                                port.getWsdlPort();
                                
                                ServiceImplBean serviceBean = port.getServiceImplBean();
                                if(serviceBean != null) {
                                    addMapString(portMap, DDBinding.PROP_SERVLET_LINK, serviceBean.getServletLink());
                                    addMapString(portMap, DDBinding.PROP_EJB_LINK, serviceBean.getEjbLink());
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
