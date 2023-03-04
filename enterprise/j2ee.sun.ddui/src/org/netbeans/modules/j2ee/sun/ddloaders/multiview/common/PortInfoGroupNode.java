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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class PortInfoGroupNode extends NamedBeanGroupNode {

    private ServiceRef serviceRef;
    
    public PortInfoGroupNode(SectionNodeView sectionNodeView, ServiceRef serviceRef, ASDDVersion version) {
        super(sectionNodeView, serviceRef, ServiceRef.SERVICE_REF_NAME, PortInfo.class,
                NbBundle.getMessage(ServiceRefGroupNode.class, "LBL_PortInfoGroupHeader"), // NOI18N
                ICON_BASE_PORT_INFO_NODE, version);
        
        this.serviceRef = serviceRef;
        enableAddAction(NbBundle.getMessage(ServiceRefGroupNode.class, "LBL_AddPortInfo")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new PortInfoNode(getSectionNodeView(), binding, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        return serviceRef.getPortInfo();
    }

    protected CommonDDBean addNewBean() {
        PortInfo portInfo = serviceRef.newPortInfo();
        serviceRef.addPortInfo(portInfo);
        return portInfo;
    }    
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        serviceRef.addPortInfo((PortInfo) newBean);
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        PortInfo portInfo = (PortInfo) bean;
        serviceRef.removePortInfo(portInfo);
    }
    
    protected org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] getStandardBeansFromModel() {
        org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [] stdBeans = null;
//        !PW FIXME are we going to bind port-info to anything?  wsdl-port from standard descriptor?
//        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = getStandardRootDD();
//        if(stdRootDD instanceof org.netbeans.modules.j2ee.dd.api.web.WebApp) {
//            org.netbeans.modules.j2ee.dd.api.web.WebApp webApp = (org.netbeans.modules.j2ee.dd.api.web.WebApp) stdRootDD;
//            stdBeans = webApp.getServlet();
//        }
        return stdBeans != null ? stdBeans : new org.netbeans.modules.j2ee.dd.api.common.CommonDDBean [0];
    }

//    protected CommonDDBean addNewBean() {
//        PortInfo newPortInfo = (PortInfo) createBean();
//        newPortInfo.setPortInfoName("service" + getNewBeanId()); // NOI18N
//        return addBean(newPortInfo);
//    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        String serviceRefName = getParentNodeName();
        NamedBeanGroupNode serviceRefGroupNode = getParentGroupNode();
        String ejbName = serviceRefGroupNode != null ? serviceRefGroupNode.getParentNodeName() : null;
        return new PortComponentRefMetadataReader(serviceRefName, ejbName);
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        return serviceRef.newPortInfo();
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return PortInfoNode.generateTitle((PortInfo) sunBean);
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((PortInfo) sunBean).setServiceEndpointInterface(newName);
    }

    public String getSunBeanNameProperty() {
        return PortInfo.SERVICE_ENDPOINT_INTERFACE;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.PortComponentRef) standardBean).getServiceEndpointInterface();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_PORTCOMPONENT_REF_NAME;
    }
}
