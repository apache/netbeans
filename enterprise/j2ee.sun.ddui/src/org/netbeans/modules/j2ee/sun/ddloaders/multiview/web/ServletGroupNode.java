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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.web;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ServletGroupNode extends NamedBeanGroupNode {

    private SunWebApp sunWebApp;
    
    public ServletGroupNode(SectionNodeView sectionNodeView, SunWebApp sunWebApp, ASDDVersion version) {
        super(sectionNodeView, sunWebApp, Servlet.SERVLET_NAME, Servlet.class,
                NbBundle.getMessage(ServletGroupNode.class, "LBL_ServletGroupHeader"), // NOI18N
                ICON_BASE_SERVLET_NODE, version);
        
        this.sunWebApp = sunWebApp;
        enableAddAction(NbBundle.getMessage(ServletGroupNode.class, "LBL_AddServlet")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new ServletNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        return sunWebApp.getServlet();
    }
    
    protected CommonDDBean addNewBean() {
        Servlet newServlet = sunWebApp.newServlet();
        newServlet.setServletName(getNewBeanId(PFX_SERVLET)); // NOI18N
        return addBean(newServlet);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        sunWebApp.addServlet((Servlet) newBean);
        return newBean;
    }

    protected void removeBean(CommonDDBean bean) {
        Servlet servlet = (Servlet) bean;
        sunWebApp.removeServlet(servlet);
    }
    
    // ------------------------------------------------------------------------
    // DescriptorReader implementation
    // ------------------------------------------------------------------------
    @Override
    public Map<String, Object> readDescriptor() {
        Map<String, Object> resultMap = null;
        
        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = getStandardRootDD();
        if(stdRootDD instanceof org.netbeans.modules.j2ee.dd.api.web.WebApp) {
            org.netbeans.modules.j2ee.dd.api.web.WebApp webApp = (org.netbeans.modules.j2ee.dd.api.web.WebApp) stdRootDD;
            resultMap = ServletMetadataReader.readDescriptor(webApp);
        }
        
        return resultMap;
    }

    @Override
    public Map<String, Object> readAnnotations() {
        Map<String, Object> resultMap = null;
        
        try {
            MetadataModel<WebAppMetadata> webAppModel = getMetadataModel(WebAppMetadata.class);
            if(webAppModel != null) {
                resultMap = webAppModel.runReadAction(new ServletMetadataReader());
            }
        } catch (MetadataModelException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return resultMap;
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        return sunWebApp.newServlet();
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((Servlet) sunBean).getServletName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((Servlet) sunBean).setServletName(newName);
    }

    public String getSunBeanNameProperty() {
        return Servlet.SERVLET_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.web.Servlet) standardBean).getServletName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_SERVLET_NAME;
    }
    
}
