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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class EjbGroupNode extends NamedBeanGroupNode {

    private SunEjbJar sunEjbJar;
    
    public EjbGroupNode(SectionNodeView sectionNodeView, SunEjbJar sunEjbJar, ASDDVersion version) {
        super(sectionNodeView, sunEjbJar, Ejb.EJB_NAME, Ejb.class,
                NbBundle.getMessage(EjbGroupNode.class, "LBL_EjbGroupHeader"), // NOI18N
                ICON_EJB_GROUP_NODE, version);
        
        this.sunEjbJar = sunEjbJar;
        enableAddAction(NbBundle.getMessage(EjbGroupNode.class, "LBL_AddEjb")); // NOI18N
    }

    protected SectionNode createNode(DDBinding binding) {
        return new EjbNode(getSectionNodeView(), binding, version);
    }
    
    protected CommonDDBean [] getBeansFromModel() {
        EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
        if(eb != null) {
            return eb.getEjb();
        }
        return null;
    }

    protected CommonDDBean addNewBean() {
        Ejb newEjb = (Ejb) createBean();
        newEjb.setEjbName(getNewBeanId(PFX_EJB)); // NOI18N
        return addBean(newEjb);
    }
    
    protected CommonDDBean addBean(CommonDDBean newBean) {
        EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
        if(eb == null) {
            eb = sunEjbJar.newEnterpriseBeans();
            sunEjbJar.setEnterpriseBeans(eb);
        }
        eb.addEjb((Ejb) newBean);
        return newBean;
    }
    
    protected void removeBean(CommonDDBean bean) {
        EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
        if(eb != null) {
            Ejb ejb = (Ejb) bean;
            eb.removeEjb(ejb);
            if(eb.isTrivial(null)) {
                sunEjbJar.setEnterpriseBeans(null);
            }
        }
    }
    
    /** EjbGroupNode usually gets events from <EnterpriseBeans> so we need
     *  custom event source matching.
     */
    @Override
    protected boolean isEventSource(Object source) {
        if(source != null && (
                source == sunEjbJar.getEnterpriseBeans() ||
                super.isEventSource(source))
                ) {
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // DescriptorReader implementation
    // ------------------------------------------------------------------------
    @Override
    public Map<String, Object> readDescriptor() {
        Map<String, Object> resultMap = null;
        
        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = getStandardRootDD();
        if(stdRootDD instanceof org.netbeans.modules.j2ee.dd.api.ejb.EjbJar) {
            org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = (org.netbeans.modules.j2ee.dd.api.ejb.EjbJar) stdRootDD;
            resultMap = EjbMetadataReader.readDescriptor(ejbJar);
        }
        
        return resultMap;
    }

    @Override
    public Map<String, Object> readAnnotations() {
        Map<String, Object> resultMap = null;
        
        try {
            MetadataModel<EjbJarMetadata> ejbJarModel = getMetadataModel(EjbJarMetadata.class);
            if(ejbJarModel != null) {
                resultMap = ejbJarModel.runReadAction(new EjbMetadataReader());
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
    private volatile EnterpriseBeans ejbFactory = null;
    
    public CommonDDBean createBean() {
        if(ejbFactory == null) {
            ejbFactory = sunEjbJar.newEnterpriseBeans();
        }
        return ejbFactory.newEjb();
    }
    
    public String getBeanName(CommonDDBean sunBean) {
        return ((Ejb) sunBean).getEjbName();
    }

    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((Ejb) sunBean).setEjbName(newName);
    }

    public String getSunBeanNameProperty() {
        return Ejb.EJB_NAME;
    }

    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.ejb.Ejb) standardBean).getEjbName();
    }

    public String getStandardBeanNameProperty() {
        return STANDARD_EJB_NAME;
    }
}
