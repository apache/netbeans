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
