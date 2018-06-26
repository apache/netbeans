/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
