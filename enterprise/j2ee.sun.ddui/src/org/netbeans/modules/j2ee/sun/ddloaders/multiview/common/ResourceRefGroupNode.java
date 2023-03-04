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
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ResourceRefGroupNode extends NamedBeanGroupNode {

    public ResourceRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, ResourceRef.RES_REF_NAME, ResourceRef.class,
                NbBundle.getMessage(ResourceRefGroupNode.class, "LBL_ResourceRefGroupHeader"), // NOI18N
                ICON_BASE_RESOURCE_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(ResourceRefGroupNode.class, "LBL_AddResourceRef")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new ResourceRefNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        ResourceRef [] resourceRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            resourceRefs = ((SunWebApp) commonDD).getResourceRef();
        } else if(commonDD instanceof Ejb) {
            resourceRefs = ((Ejb) commonDD).getResourceRef();
        } else if(commonDD instanceof SunApplicationClient) {
            resourceRefs = ((SunApplicationClient) commonDD).getResourceRef();
        }
        return resourceRefs;
    }

    protected CommonDDBean addNewBean() {
        ResourceRef newResourceRef = (ResourceRef) createBean();
        newResourceRef.setResRefName(getNewBeanId(PFX_RESOURCE_REF)); // NOI18N
        return addBean(newResourceRef);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        ResourceRef newResourceRef = (ResourceRef) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addResourceRef(newResourceRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).addResourceRef(newResourceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).addResourceRef(newResourceRef);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        ResourceRef resourceRef = (ResourceRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeResourceRef(resourceRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).removeResourceRef(resourceRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).removeResourceRef(resourceRef);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new ResourceRefMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        ResourceRef newResourceRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            newResourceRef = ((SunWebApp) commonDD).newResourceRef();
        } else if(commonDD instanceof Ejb) {
            newResourceRef = ((Ejb) commonDD).newResourceRef();
        } else if(commonDD instanceof SunApplicationClient) {
            newResourceRef = ((SunApplicationClient) commonDD).newResourceRef();
        }
        
        return newResourceRef;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((ResourceRef) sunBean).getResRefName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((ResourceRef) sunBean).setResRefName(newName);
    }

    public String getSunBeanNameProperty() {
        return ResourceRef.RES_REF_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.ResourceRef) standardBean).getResRefName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_RES_REF_NAME;
    }
}
