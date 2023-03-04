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
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class EjbRefGroupNode extends NamedBeanGroupNode {

    public EjbRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, EjbRef.EJB_REF_NAME, EjbRef.class,
                NbBundle.getMessage(EjbRefGroupNode.class, "LBL_EjbRefGroupHeader"), // NOI18N
                ICON_BASE_EJB_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(EjbRefGroupNode.class, "LBL_AddEjbRef")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new EjbRefNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        EjbRef [] ejbRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ejbRefs = ((SunWebApp) commonDD).getEjbRef();
        } else if(commonDD instanceof Ejb) {
            ejbRefs = ((Ejb) commonDD).getEjbRef();
        } else if(commonDD instanceof SunApplicationClient) {
            ejbRefs = ((SunApplicationClient) commonDD).getEjbRef();
        }
        return ejbRefs;
    }
    
    protected CommonDDBean addNewBean() {
        EjbRef newEjbRef = (EjbRef) createBean();
        newEjbRef.setEjbRefName(getNewBeanId(PFX_EJB_REF)); // NOI18N
        return addBean(newEjbRef);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        EjbRef newEjbRef = (EjbRef) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addEjbRef(newEjbRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).addEjbRef(newEjbRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).addEjbRef(newEjbRef);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        EjbRef ejbRef = (EjbRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeEjbRef(ejbRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).removeEjbRef(ejbRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).removeEjbRef(ejbRef);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new EjbRefMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        EjbRef newEjbRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            newEjbRef = ((SunWebApp) commonDD).newEjbRef();
        } else if(commonDD instanceof Ejb) {
            newEjbRef = ((Ejb) commonDD).newEjbRef();
        } else if(commonDD instanceof SunApplicationClient) {
            newEjbRef = ((SunApplicationClient) commonDD).newEjbRef();
        }
        
        return newEjbRef;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((EjbRef) sunBean).getEjbRefName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((EjbRef) sunBean).setEjbRefName(newName);
    }

    public String getSunBeanNameProperty() {
        return EjbRef.EJB_REF_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.EjbRef) standardBean).getEjbRefName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_EJB_REF_NAME;
    }
}
