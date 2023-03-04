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
package org.netbeans.modules.cloud.amazon.ui;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.cloud.amazon.serverplugin.AmazonJ2EEInstance;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class AmazonJ2EEInstanceNode extends AbstractNode {
    
    public static final String TOMCAT_ICON = "org/netbeans/modules/cloud/amazon/ui/resources/tomcat.png"; // NOI18N
    
    private AmazonJ2EEInstance aij;
    
    public AmazonJ2EEInstanceNode(AmazonJ2EEInstance aij) {
        super(Children.LEAF, Lookups.fixed(aij));
        this.aij = aij;
        setName(""); // NOI18N
        setDisplayName(aij.getDisplayName());
        //setShortDescription(NbBundle.getMessage(RootNode.class, "Amazon_Node_Short_Description"));
        setIconBaseWithExtension(TOMCAT_ICON);
    }
    
    void showServerType() {
        setDisplayName(aij.getEnvironmentName()+" ["+aij.getApplicationName()+"]"+" on "+aij.getContainerType());
    }

    @Override
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return badgeIcon(super.getOpenedIcon(type));
    }   
    
    private static final String RUNNING_ICON 
            = "org/netbeans/modules/cloud/amazon/ui/resources/running.png"; // NOI18N
    private static final String WAITING_ICON
            = "org/netbeans/modules/cloud/amazon/ui/resources/waiting.png"; // NOI18N
    private static final String TERMINATED_ICON
            = "org/netbeans/modules/cloud/amazon/ui/resources/terminated.png"; // NOI18N
    
    private Image badgeIcon(Image origImg) {
        Image badge = null;        
        switch (aij.getState()) {
            case UPDATING:
            case LAUNCHING:
            case TERMINATING:
                badge = ImageUtilities.loadImage(WAITING_ICON);
                break;
            case READY:
                badge = ImageUtilities.loadImage(RUNNING_ICON);
                break;
            case TERMINATED:
                badge = ImageUtilities.loadImage(TERMINATED_ICON);
                break;
        }
        return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(RemoteServerPropertiesAction.class)
        };
    }
    
}
