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
import org.netbeans.modules.javaee.wildfly.nodes.actions.OpenURLAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.OpenURLActionCookie;
import org.netbeans.modules.javaee.wildfly.nodes.actions.ResourceType;
import org.netbeans.modules.javaee.wildfly.nodes.actions.StartModuleAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.StartModuleCookieImpl;
import org.netbeans.modules.javaee.wildfly.nodes.actions.StopModuleAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.StopModuleCookieImpl;
import org.netbeans.modules.javaee.wildfly.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.UndeployModuleCookieImpl;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * Node which describes Web Module.
 *
 * @author Michal Mocnak
 */
public class WildflyWebModuleNode extends AbstractStateNode {

    private final String url;

    public WildflyWebModuleNode(String fileName, Lookup lookup, String url) {
        super(new WildflyDeploymentChildren(lookup, fileName));
        setDisplayName(fileName.substring(0, fileName.lastIndexOf('.')));
        this.url = url;
        // we cannot find out the .war name w/o the management support, thus we cannot enable the Undeploy action
        getCookieSet().add(new UndeployModuleCookieImpl(fileName, ResourceType.WAR, lookup));
        getCookieSet().add(new StartModuleCookieImpl(fileName, lookup));
        getCookieSet().add(new StopModuleCookieImpl(fileName, lookup));
        if (url != null) {
            getCookieSet().add(new OpenURLActionCookieImpl(url));
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        if (getParentNode() instanceof WildflyEarApplicationNode) {
            if (url != null) {
                return new SystemAction[]{
                    SystemAction.get(OpenURLAction.class)
                };
            } else {
                return new SystemAction[0];
            }
        } else {
            if (url != null) {
                return new SystemAction[]{
                    SystemAction.get(OpenURLAction.class),
                    SystemAction.get(StopModuleAction.class),
                    SystemAction.get(UndeployModuleAction.class)
                };
            } else {
                return new SystemAction[]{
                    SystemAction.get(StartModuleAction.class),
                    SystemAction.get(UndeployModuleAction.class)
                };
            }
        }
    }

    @Override
    public Image getOriginalIcon(int type) {
        return UISupport.getIcon(ServerIcon.WAR_ARCHIVE);
    }

    @Override
    public Image getOriginalOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    protected boolean isRunning() {
        return this.url != null;
    }

    @Override
    protected boolean isWaiting() {
        return this.url == null;
    }

    private static class OpenURLActionCookieImpl implements OpenURLActionCookie {

        private final String url;

        public OpenURLActionCookieImpl(String url) {
            this.url = url;
        }

        @Override
        public String getWebURL() {
            return url;
        }
    }
}
