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
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class WebServiceGroupNode extends NamedBeanGroupNode {

    private final SunWebApp sunWebApp;
    private final SunEjbJar sunEjbJar;
    
    public WebServiceGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, WebserviceDescription.WEBSERVICE_DESCRIPTION_NAME, WebserviceDescription.class,
                NbBundle.getMessage(WebServiceGroupNode.class, "LBL_WebServiceGroupHeader"), // NOI18N
                ICON_BASE_SERVICE_REF_NODE, version);
        
        sunWebApp = (commonDD instanceof SunWebApp) ? (SunWebApp) commonDD : null;
        sunEjbJar = (commonDD instanceof SunEjbJar) ? (SunEjbJar) commonDD : null;
        
        enableAddAction(NbBundle.getMessage(WebServiceGroupNode.class, "LBL_AddWebService")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new WebServiceNode(getSectionNodeView(), binding, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        WebserviceDescription [] webServiceDesc = null;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            webServiceDesc = sunWebApp.getWebserviceDescription();
        } else if(sunEjbJar != null) {
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            if(eb != null) {
                webServiceDesc = eb.getWebserviceDescription();
            }
        }
        
        return webServiceDesc;
    }

    protected CommonDDBean addNewBean() {
        WebserviceDescription newWebServiceDesc = (WebserviceDescription) createBean();
        newWebServiceDesc.setWebserviceDescriptionName(getNewBeanId(PFX_SERVICE)); // NOI18N
        return addBean(newWebServiceDesc);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        WebserviceDescription newWebServiceDesc = (WebserviceDescription) newBean;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            sunWebApp.addWebserviceDescription(newWebServiceDesc);
        } else if(sunEjbJar != null) {
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            if(eb == null) {
                eb = sunEjbJar.newEnterpriseBeans();
                sunEjbJar.setEnterpriseBeans(eb);
            }
            eb.addWebserviceDescription(newWebServiceDesc);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        WebserviceDescription webServiceDesc = (WebserviceDescription) bean;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            sunWebApp.removeWebserviceDescription(webServiceDesc);
        } else if(sunEjbJar != null) {
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            if(eb != null) {
                eb.removeWebserviceDescription(webServiceDesc);
                if(eb.isTrivial(null)) {
                    sunEjbJar.setEnterpriseBeans(null);
                }
            }
        }
    }
    
    /** WebServiceGroupNode gets events from <EnterpriseBeans> when in 
     *  sun-ejb-jar so we need custom event source matching.
     */
    @Override
    protected boolean isEventSource(Object source) {
        if(source != null && (
                sunEjbJar != null && source == sunEjbJar.getEnterpriseBeans() ||
                super.isEventSource(source))
                ) {
            return true;
        }
        return false;
        
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
        return new WebServiceMetadataReader();
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    private volatile EnterpriseBeans ejbWebserviceDescFactory = null;
    
    public CommonDDBean createBean() {
        WebserviceDescription newWebServiceDesc = null;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            newWebServiceDesc = sunWebApp.newWebserviceDescription();
        } else if(sunEjbJar != null) {
            if(ejbWebserviceDescFactory == null) {
                ejbWebserviceDescFactory = sunEjbJar.newEnterpriseBeans();
            }
            newWebServiceDesc = ejbWebserviceDescFactory.newWebserviceDescription();
        }
        
        return newWebServiceDesc;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((WebserviceDescription) sunBean).getWebserviceDescriptionName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((WebserviceDescription) sunBean).setWebserviceDescriptionName(newName);
    }

    public String getSunBeanNameProperty() {
        return WebserviceDescription.WEBSERVICE_DESCRIPTION_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription)
                standardBean).getWebserviceDescriptionName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_WEBSERVICE_DESC_NAME;
    }
}
