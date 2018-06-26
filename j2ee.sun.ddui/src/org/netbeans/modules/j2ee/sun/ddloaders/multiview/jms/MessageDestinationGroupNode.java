/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
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
public class MessageDestinationGroupNode extends NamedBeanGroupNode {

    private final SunWebApp sunWebApp;
    private final SunEjbJar sunEjbJar;
    private final SunApplicationClient sunAppClient;
    
    public MessageDestinationGroupNode(SectionNodeView sectionNodeView, RootInterface rootDD, ASDDVersion version) {
        super(sectionNodeView, rootDD, MessageDestination.MESSAGE_DESTINATION_NAME, MessageDestination.class,
                NbBundle.getMessage(MessageDestinationGroupNode.class, "LBL_MessageDestinationGroupHeader"), // NOI18N
                ICON_BASE_MESSAGE_DESTINATION_NODE, version);
        
        sunWebApp = (commonDD instanceof SunWebApp) ? (SunWebApp) commonDD : null;
        sunEjbJar = (commonDD instanceof SunEjbJar) ? (SunEjbJar) commonDD : null;
        sunAppClient = (commonDD instanceof SunApplicationClient) ? (SunApplicationClient) commonDD : null;
        
        enableAddAction(NbBundle.getMessage(MessageDestinationGroupNode.class, "LBL_AddMessageDestination")); // NOI18N
    }

    @Override
    protected SectionNode createNode(DDBinding binding) {
        return new MessageDestinationNode(getSectionNodeView(), binding, version);
    }

    @Override
    protected CommonDDBean [] getBeansFromModel() {
        MessageDestination [] destinations = null;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            destinations = sunWebApp.getMessageDestination();
        } else if(sunEjbJar != null) {
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            destinations = (eb != null) ? eb.getMessageDestination() : null;
        } else if(sunAppClient != null) {
            destinations = sunAppClient.getMessageDestination();
        }
        return destinations;
    }

    @Override
    protected CommonDDBean addNewBean() {
        MessageDestination newMsgDest = (MessageDestination) createBean();
        newMsgDest.setMessageDestinationName(getNewBeanId(PFX_DESTINATION)); // NOI18N
        return addBean(newMsgDest);
    }
    
    @Override
    protected CommonDDBean addBean(CommonDDBean newBean) {
        MessageDestination newMsgDest = (MessageDestination) newBean;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            sunWebApp.addMessageDestination(newMsgDest);
        } else if(sunEjbJar != null) {
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            if(eb == null) {
                eb = sunEjbJar.newEnterpriseBeans();
                sunEjbJar.setEnterpriseBeans(eb);
            }
            eb.addMessageDestination(newMsgDest);
        } else if(sunAppClient != null) {
            sunAppClient.addMessageDestination(newMsgDest);
        }
        
        return newMsgDest;
    }
    
    @Override
    protected void removeBean(CommonDDBean bean) {
        MessageDestination msgDest = (MessageDestination) bean;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            sunWebApp.removeMessageDestination(msgDest);
        } else if(sunEjbJar != null) {
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
            if(eb != null) {
                eb.removeMessageDestination(msgDest);
                if(eb.isTrivial(null)) {
                    sunEjbJar.setEnterpriseBeans(null);
                }
            }
        } else if(sunAppClient != null) {
            sunAppClient.removeMessageDestination(msgDest);
        }
    }
    
    /** MessageDestinationGroupNode gets events from <EnterpriseBeans> when in 
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
    protected CommonBeanReader getModelReader() {
        return new MessageDestinationMetadataReader();
    }
    
    // ------------------------------------------------------------------------
    // BeanResolver interface implementation
    // ------------------------------------------------------------------------
    private volatile EnterpriseBeans ejbJarMesgDestFactory = null;
    
    @Override
    public CommonDDBean createBean() {
        MessageDestination newMsgDest = null;
        
        // TODO find a better way to do this for common beans.
        if(sunWebApp != null) {
            newMsgDest = sunWebApp.newMessageDestination();
        } else if(sunEjbJar != null) {
            if(ejbJarMesgDestFactory == null) {
                ejbJarMesgDestFactory = sunEjbJar.newEnterpriseBeans();
            }
            newMsgDest = ejbJarMesgDestFactory.newMessageDestination();
        } else if(sunAppClient != null) {
            newMsgDest = sunAppClient.newMessageDestination();
        }
        
        return newMsgDest;
    }
    
    @Override
    public String getBeanName(CommonDDBean sunBean) {
        return ((MessageDestination) sunBean).getMessageDestinationName();
    }

    @Override
    public void setBeanName(CommonDDBean sunBean, String newName) {
        ((MessageDestination) sunBean).setMessageDestinationName(newName);
    }

    @Override
    public String getSunBeanNameProperty() {
        return MessageDestination.MESSAGE_DESTINATION_NAME;
    }

    @Override
    public String getBeanName(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean standardBean) {
        return ((org.netbeans.modules.j2ee.dd.api.common.MessageDestination) standardBean).getMessageDestinationName();
    }

    @Override
    public String getStandardBeanNameProperty() {
        return STANDARD_MSGDEST_NAME;
    }
}
