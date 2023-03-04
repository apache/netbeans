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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.javaee.wildfly.nodes.actions.ResourceType;
import org.netbeans.modules.javaee.wildfly.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.UndeployModuleCookieImpl;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * Node which describes an EJB Module.
 *
 * @author Michal Mocnak
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyEjbModuleNode extends AbstractNode {

    public WildflyEjbModuleNode(String fileName, Lookup lookup) {
        this(fileName, lookup, new ArrayList<WildflyEjbComponentNode>(), false);
    }

    public WildflyEjbModuleNode(String fileName, Lookup lookup, boolean isEJB3) {
        this(fileName, lookup, new ArrayList<WildflyEjbComponentNode>(), isEJB3);
    }

    public WildflyEjbModuleNode(String fileName, Lookup lookup, List<WildflyEjbComponentNode> ejbs, boolean isEJB3) {
        super(new WildflyEjbComponentsChildren(lookup, fileName, ejbs));
        setDisplayName(fileName.substring(0, fileName.lastIndexOf('.')));
        if (isEJB3) {
            getCookieSet().add(new UndeployModuleCookieImpl(fileName, lookup));
        }
        else {
            getCookieSet().add(new UndeployModuleCookieImpl(fileName, ResourceType.EJB, lookup));
        }
    }

    @Override
    public Action[] getActions(boolean context){
        if(getParentNode() instanceof WildflyEarApplicationNode)
            return new SystemAction[] {};
        else
            return new SystemAction[] {
                SystemAction.get(UndeployModuleAction.class)
            };
    }

    @Override
    public Image getIcon(int type) {
        return UISupport.getIcon(ServerIcon.EJB_ARCHIVE);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
}
