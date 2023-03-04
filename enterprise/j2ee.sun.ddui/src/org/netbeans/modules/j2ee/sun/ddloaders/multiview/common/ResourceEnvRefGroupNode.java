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
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ResourceEnvRefGroupNode extends NamedBeanGroupNode {

    public ResourceEnvRefGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD, ASDDVersion version) {
        super(sectionNodeView, commonDD, ResourceEnvRef.RESOURCE_ENV_REF_NAME, ResourceEnvRef.class,
                NbBundle.getMessage(ResourceEnvRefGroupNode.class, "LBL_ResourceEnvRefGroupHeader"), // NOI18N
                ICON_BASE_RESOURCE_ENV_REF_NODE, version);
        
        enableAddAction(NbBundle.getMessage(ResourceEnvRefGroupNode.class, "LBL_AddResourceEnvRef")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new ResourceEnvRefNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        ResourceEnvRef [] resourceEnvRefs = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            resourceEnvRefs = ((SunWebApp) commonDD).getResourceEnvRef();
        } else if(commonDD instanceof Ejb) {
            resourceEnvRefs = ((Ejb) commonDD).getResourceEnvRef();
        } else if(commonDD instanceof SunApplicationClient) {
            resourceEnvRefs = ((SunApplicationClient) commonDD).getResourceEnvRef();
        }
        return resourceEnvRefs;
    }

    protected CommonDDBean addNewBean() {
        ResourceEnvRef newResourceEnvRef = (ResourceEnvRef) createBean();
        newResourceEnvRef.setResourceEnvRefName(getNewBeanId(PFX_RESOURCE_ENV_REF)); // NOI18N
        return addBean(newResourceEnvRef);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        ResourceEnvRef newResourceEnvRef = (ResourceEnvRef) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addResourceEnvRef(newResourceEnvRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).addResourceEnvRef(newResourceEnvRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).addResourceEnvRef(newResourceEnvRef);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        ResourceEnvRef resourceEnvRef = (ResourceEnvRef) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeResourceEnvRef(resourceEnvRef);
        } else if(commonDD instanceof Ejb) {
            ((Ejb) commonDD).removeResourceEnvRef(resourceEnvRef);
        } else if(commonDD instanceof SunApplicationClient) {
            ((SunApplicationClient) commonDD).removeResourceEnvRef(resourceEnvRef);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new ResourceEnvRefMetadataReader(getParentNodeName());
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        ResourceEnvRef newResourceEnvRef = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            newResourceEnvRef = ((SunWebApp) commonDD).newResourceEnvRef();
        } else if(commonDD instanceof Ejb) {
            newResourceEnvRef = ((Ejb) commonDD).newResourceEnvRef();
        } else if(commonDD instanceof SunApplicationClient) {
            newResourceEnvRef = ((SunApplicationClient) commonDD).newResourceEnvRef();
        }
        
        return newResourceEnvRef;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((ResourceEnvRef) sunBean).getResourceEnvRefName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((ResourceEnvRef) sunBean).setResourceEnvRefName(newName);
    }

    public String getSunBeanNameProperty() {
        return ResourceEnvRef.RESOURCE_ENV_REF_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef) standardBean).getResourceEnvRefName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_RESOURCE_ENV_REF_NAME;
    }
}
