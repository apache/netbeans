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
