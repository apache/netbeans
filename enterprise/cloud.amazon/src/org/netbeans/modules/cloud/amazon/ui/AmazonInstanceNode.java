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
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class AmazonInstanceNode extends AbstractNode {
    
    private static final String AMAZON_ICON = "org/netbeans/modules/cloud/amazon/ui/resources/amazon.png"; // NOI18N
    
    public AmazonInstanceNode(AmazonInstance ai) {
        super(Children.LEAF, Lookups.fixed(ai));
        setName(""); // NOI18N
        setDisplayName(ai.getName());
        setIconBaseWithExtension(AMAZON_ICON);
    }
    
    private static final String WAITING_ICON
            = "org/netbeans/modules/cloud/amazon/ui/resources/waiting.png"; // NOI18N
    
    @Override
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return badgeIcon(super.getOpenedIcon(type));
    }
    
    private Image badgeIcon(Image origImg) {
        return origImg;
    }
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(RefreshAmazonInstanceNodeAction.class),
            SystemAction.get(ViewAdminConsoleAction.class),
            null,
            SystemAction.get(RemoveAmazonInstanceAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }

}
