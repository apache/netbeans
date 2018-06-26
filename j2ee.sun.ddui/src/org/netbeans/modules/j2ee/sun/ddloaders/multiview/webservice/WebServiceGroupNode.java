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
