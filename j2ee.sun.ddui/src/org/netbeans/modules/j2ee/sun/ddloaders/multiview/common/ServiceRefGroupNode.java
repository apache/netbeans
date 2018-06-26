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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ServiceRefGroupNode extends NamedBeanGroupNode {

    public ServiceRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, ServiceRef.SERVICE_REF_NAME, ServiceRef.class,
                NbBundle.getMessage(ServiceRefGroupNode.class, "LBL_ServiceRefGroupHeader"), // NOI18N
                ICON_BASE_SERVICE_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(ServiceRefGroupNode.class, "LBL_AddServiceRef")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new ServiceRefNode(getSectionNodeView(), binding, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        ServiceRef [] serviceRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            serviceRefs = ((SunWebApp) commonDD).getServiceRef();
        } else if(commonDD instanceof Ejb) {
            serviceRefs = ((Ejb) commonDD).getServiceRef();
        } else if(commonDD instanceof SunApplicationClient) {
            serviceRefs = ((SunApplicationClient) commonDD).getServiceRef();
        }
        return serviceRefs;
    }

    protected CommonDDBean addNewBean() {
        ServiceRef newServiceRef = (ServiceRef) createBean();
        newServiceRef.setServiceRefName(getNewBeanId(PFX_SERVICE_REF)); // NOI18N
        return addBean(newServiceRef);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        ServiceRef newServiceRef = (ServiceRef) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addServiceRef(newServiceRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).addServiceRef(newServiceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).addServiceRef(newServiceRef);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        ServiceRef serviceRef = (ServiceRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeServiceRef(serviceRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).removeServiceRef(serviceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).removeServiceRef(serviceRef);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new ServiceRefMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        ServiceRef newServiceRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            newServiceRef = ((SunWebApp) commonDD).newServiceRef();
        } else if(commonDD instanceof Ejb) {
            newServiceRef = ((Ejb) commonDD).newServiceRef();
        } else if(commonDD instanceof SunApplicationClient) {
            newServiceRef = ((SunApplicationClient) commonDD).newServiceRef();
        }
        
        return newServiceRef;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((ServiceRef) sunBean).getServiceRefName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((ServiceRef) sunBean).setServiceRefName(newName);
    }

    public String getSunBeanNameProperty() {
        return ServiceRef.SERVICE_REF_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.ServiceRef) standardBean).getServiceRefName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_SERVICE_REF_NAME;
    }
}
