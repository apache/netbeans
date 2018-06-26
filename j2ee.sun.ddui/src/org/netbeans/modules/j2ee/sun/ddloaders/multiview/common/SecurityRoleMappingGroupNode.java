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
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class SecurityRoleMappingGroupNode extends NamedBeanGroupNode {

    public SecurityRoleMappingGroupNode(SectionNodeView sectionNodeView, RootInterface rootDD, ASDDVersion version) {
        super(sectionNodeView, rootDD, SecurityRoleMapping.ROLE_NAME, SecurityRoleMapping.class,
                NbBundle.getMessage(SecurityRoleMappingGroupNode.class, "LBL_SecurityRoleMappingGroupHeader"), // NOI18N
                ICON_BASE_SECURITY_ROLE_MAPPING_NODE, version);
        
        enableAddAction(NbBundle.getMessage(SecurityRoleMappingGroupNode.class, "LBL_AddSecurityRoleMapping")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new SecurityRoleMappingNode(getSectionNodeView(), binding, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        SecurityRoleMapping [] mappings = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            mappings = ((SunWebApp) commonDD).getSecurityRoleMapping();
        } else if(commonDD instanceof SunEjbJar) {
            mappings = ((SunEjbJar) commonDD).getSecurityRoleMapping();
        } else if(commonDD instanceof SunApplication) {
            mappings = ((SunApplication) commonDD).getSecurityRoleMapping();
        }
        return mappings;
    }

    protected CommonDDBean addNewBean() {
        SecurityRoleMapping newSecurityRoleMapping = (SecurityRoleMapping) createBean();
        newSecurityRoleMapping.setRoleName(getNewBeanId(PFX_ROLE)); // NOI18N
        return addBean(newSecurityRoleMapping);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        SecurityRoleMapping newSecurityRoleMapping = (SecurityRoleMapping) newBean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).addSecurityRoleMapping(newSecurityRoleMapping);
        } else if(commonDD instanceof SunEjbJar) {
            ((SunEjbJar) commonDD).addSecurityRoleMapping(newSecurityRoleMapping);
        } else if(commonDD instanceof SunApplication) {
            ((SunApplication) commonDD).addSecurityRoleMapping(newSecurityRoleMapping);
        }
        
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        SecurityRoleMapping mapping = (SecurityRoleMapping) bean;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            ((SunWebApp) commonDD).removeSecurityRoleMapping(mapping);
        } else if(commonDD instanceof SunEjbJar) {
            ((SunEjbJar) commonDD).removeSecurityRoleMapping(mapping);
        } else if(commonDD instanceof SunApplication) {
            ((SunApplication) commonDD).removeSecurityRoleMapping(mapping);
        }
    }
    
    // ------------------------------------------------------------------------
    // Support for DescriptorReader interface implementation
    // ------------------------------------------------------------------------
    @Override 
    protected CommonBeanReader getModelReader() {
        return new SecurityRoleMetadataReader();
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    public CommonDDBean createBean() {
        SecurityRoleMapping newSecurityRoleMapping = null;
        
        // TODO find a better way to do this for common beans.
        if(commonDD instanceof SunWebApp) {
            newSecurityRoleMapping = ((SunWebApp) commonDD).newSecurityRoleMapping();
        } else if(commonDD instanceof SunEjbJar) {
            newSecurityRoleMapping = ((SunEjbJar) commonDD).newSecurityRoleMapping();
        } else if(commonDD instanceof SunApplication) {
            newSecurityRoleMapping = ((SunApplication) commonDD).newSecurityRoleMapping();
        }
        
        return newSecurityRoleMapping;
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((SecurityRoleMapping) sunBean).getRoleName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((SecurityRoleMapping) sunBean).setRoleName(newName);
    }

    public String getSunBeanNameProperty() {
        return SecurityRoleMapping.ROLE_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.SecurityRole) standardBean).getRoleName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_ROLE_NAME;
    }
}
