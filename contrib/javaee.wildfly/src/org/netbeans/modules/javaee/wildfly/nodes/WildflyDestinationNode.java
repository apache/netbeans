/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javaee.wildfly.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.javaee.wildfly.nodes.actions.ResourceType;
import org.netbeans.modules.javaee.wildfly.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.UndeployModuleCookieImpl;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * 
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyDestinationNode extends AbstractNode {

    public WildflyDestinationNode(String name, MessageDestination destination, Lookup lookup) {
        super(Children.LEAF);
        getCookieSet().add(new UndeployModuleCookieImpl(destination.getName(), destination.getType() == MessageDestination.Type.QUEUE ? ResourceType.QUEUE : ResourceType.TOPIC,lookup));
        setDisplayName(destination.getName());
        setName(name);
        setShortDescription(destination.getName());
    }

    @Override
    public Action[] getActions(boolean context) {
        return new SystemAction[]{
            SystemAction.get(UndeployModuleAction.class)
        };
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(Util.JMS_ICON);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(Util.JMS_ICON);
    }

}
