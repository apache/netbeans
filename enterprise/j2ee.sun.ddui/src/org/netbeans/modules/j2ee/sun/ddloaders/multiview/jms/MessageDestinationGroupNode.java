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
