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

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.awt.Image;
import javax.enterprise.deploy.shared.ModuleType;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.OpenURLAction;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.OpenURLActionCookie;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.UndeployModuleCookieImpl;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * Node which describes Web Module.
 *
 * @author Michal Mocnak
 */
public class JBWebModuleNode extends AbstractNode {

    private final JBAbilitiesSupport abilitiesSupport;

    private final String url;

    public JBWebModuleNode(String fileName, Lookup lookup, String url) {
        super(new JBServletsChildren(fileName, lookup));
        setDisplayName(fileName.substring(0, fileName.lastIndexOf('.')));
        this.abilitiesSupport = new JBAbilitiesSupport(lookup);
        this.url = url;
        if (abilitiesSupport.isRemoteManagementSupported()
                && (abilitiesSupport.isJB4x() || abilitiesSupport.isJB6x())) {
            // we cannot find out the .war name w/o the management support, thus we cannot enable the Undeploy action
            getCookieSet().add(new UndeployModuleCookieImpl(fileName, ModuleType.WAR, lookup));
        }
        
        if (url != null) {
            getCookieSet().add(new OpenURLActionCookieImpl(url));
        }
    }
    
    public Action[] getActions(boolean context) {
        if (getParentNode() instanceof JBEarApplicationNode) {
            return new SystemAction[] {
                SystemAction.get(OpenURLAction.class)
            };
        } else {
            if (abilitiesSupport.isRemoteManagementSupported()
                    && (abilitiesSupport.isJB4x() || abilitiesSupport.isJB6x())) {
                if (url != null) {
                    return new SystemAction[] {
                        SystemAction.get(OpenURLAction.class),
                        SystemAction.get(UndeployModuleAction.class)
                    };
                } else {
                    return new SystemAction[] {
                        SystemAction.get(UndeployModuleAction.class)
                    };
                }
            } else if (url != null) {
                // we cannot find out the .war name w/o the management support, thus we cannot enable the Undeploy action
                return new SystemAction[] {
                    SystemAction.get(OpenURLAction.class),
                };
            }
        }
        return new SystemAction[]{};
    }
    
    public Image getIcon(int type) {
        return UISupport.getIcon(ServerIcon.WAR_ARCHIVE);
    }

    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    private static class OpenURLActionCookieImpl implements OpenURLActionCookie {
        
        private String url;
        
        public OpenURLActionCookieImpl(String url) {
            this.url = url;
        }
        
        public String getWebURL() {
            return url;
        }
    }
}
