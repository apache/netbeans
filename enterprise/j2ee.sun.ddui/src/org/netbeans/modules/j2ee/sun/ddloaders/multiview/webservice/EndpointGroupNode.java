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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice;

import java.util.Map;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class EndpointGroupNode extends NamedBeanGroupNode {

    public EndpointGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, WebserviceEndpoint.PORT_COMPONENT_NAME, WebserviceEndpoint.class,
                NbBundle.getMessage(EndpointGroupNode.class, "LBL_EndpointGroupHeader"), // NOI18N
                ICON_BASE_ENDPOINT_NODE, version);
        
        setExpanded(false);
        enableAddAction(NbBundle.getMessage(EndpointGroupNode.class, "LBL_AddEndpoint")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new EndpointNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        WebserviceEndpoint [] endpoints = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof Servlet) {
            endpoints = ((Servlet) commonDD).getWebserviceEndpoint();
        } else if(commonDD instanceof Ejb) {
            endpoints = ((Ejb) commonDD).getWebserviceEndpoint();
        }
        return endpoints;
    }
    
    protected CommonDDBean addNewBean() {
        WebserviceEndpoint newWebserviceEndpoint = (WebserviceEndpoint) createBean();
        newWebserviceEndpoint.setPortComponentName(getNewBeanId(PFX_ENDPOINT)); // NOI18N
        return addBean(newWebserviceEndpoint);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        WebserviceEndpoint newWebserviceEndpoint = (WebserviceEndpoint) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof Servlet) {
            ((Servlet) commonDD).addWebserviceEndpoint(newWebserviceEndpoint);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).addWebserviceEndpoint(newWebserviceEndpoint);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        WebserviceEndpoint ejbRef = (WebserviceEndpoint) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof Servlet) {
            ((Servlet) commonDD).removeWebserviceEndpoint(ejbRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).removeWebserviceEndpoint(ejbRef);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override
    public Map<String, Object> readDescriptor() {
        CommonBeanReader reader = getModelReader();
        return reader != null ? reader.readDescriptor(getWebServicesRootDD()) : null;
    }
    
    @Override
    protected CommonBeanReader getModelReader() {
        return new PortComponentMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        WebserviceEndpoint newWebserviceEndpoint = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof Servlet) {
            newWebserviceEndpoint = ((Servlet) commonDD).newWebserviceEndpoint();
        } else if(commonDD instanceof Ejb) {
            newWebserviceEndpoint = ((Ejb) commonDD).newWebserviceEndpoint();
        }
        
        return newWebserviceEndpoint;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((WebserviceEndpoint) sunBean).getPortComponentName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((WebserviceEndpoint) sunBean).setPortComponentName(newName);
    }

    public String getSunBeanNameProperty() {
        return WebserviceEndpoint.PORT_COMPONENT_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.webservices.PortComponent) standardBean).getPortComponentName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_PORTCOMPONENT_NAME;
    }
}
