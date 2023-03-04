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
package org.netbeans.modules.javaee.wildfly.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.javaee.wildfly.nodes.actions.ResourceType;
import org.netbeans.modules.javaee.wildfly.nodes.actions.StartModuleAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.StartModuleCookieImpl;
import org.netbeans.modules.javaee.wildfly.nodes.actions.StopModuleAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.StopModuleCookieImpl;
import org.netbeans.modules.javaee.wildfly.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.UndeployModuleCookieImpl;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * Node which describes enterprise application.
 *
 * @author Michal Mocnak
 */
public class WildflyEarApplicationNode extends AbstractStateNode {

    public WildflyEarApplicationNode(String fileName, Lookup lookup) {
        super(new WildflyEarModulesChildren(lookup, fileName));
        setDisplayName(fileName.substring(0, fileName.lastIndexOf('.')));
        getCookieSet().add(new UndeployModuleCookieImpl(fileName, ResourceType.EAR, lookup));
        getCookieSet().add(new StartModuleCookieImpl(fileName, lookup));
        getCookieSet().add(new StopModuleCookieImpl(fileName, lookup));
    }

    @Override
    public Action[] getActions(boolean context) {
        if (isRunning()) {
            return new SystemAction[]{
                SystemAction.get(StopModuleAction.class),
                SystemAction.get(UndeployModuleAction.class)
            };
        }
        return new SystemAction[]{
            SystemAction.get(StartModuleAction.class),
            SystemAction.get(UndeployModuleAction.class)
        };
    }

    @Override
    public Image getIcon(int type) {
        return UISupport.getIcon(ServerIcon.EAR_ARCHIVE);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    protected boolean isRunning() {
        boolean running = getChildren().getNodes().length > 0;
        for (Node node : getChildren().getNodes()) {
            if (node instanceof WildflyWebModuleNode) {
                running = running && ((WildflyWebModuleNode) node).isRunning();
            }
        }
        return running;
    }

    @Override
    protected boolean isWaiting() {
        return isRunning();
    }

    @Override
    protected Image getOriginalIcon(int type) {
        return UISupport.getIcon(ServerIcon.EAR_ARCHIVE);
    }

    @Override
    protected Image getOriginalOpenedIcon(int type) {
        return UISupport.getIcon(ServerIcon.EAR_OPENED_FOLDER);
    }
}
