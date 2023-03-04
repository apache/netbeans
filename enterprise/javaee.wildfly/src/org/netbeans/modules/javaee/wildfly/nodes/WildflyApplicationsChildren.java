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

import javax.enterprise.deploy.shared.ModuleType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * It describes children nodes of the Applications node
 *
 * @author Michal Mocnak
 */
public class WildflyApplicationsChildren extends Children.Keys {

    WildflyApplicationsChildren(Lookup lookup) {
        setKeys(new Object[]{createEarApplicationsNode(lookup),
            createEjbModulesNode(lookup),
            createWebApplicationsNode(lookup)});
    }

    @Override
    protected void addNotify() {
    }

    @Override
    protected void removeNotify() {
    }

    @Override
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof AbstractNode) {
            return new Node[]{(AbstractNode) key};
        }
        return null;
    }

    /*
     * Creates an EAR Applications parent node
     */
    public static WildflyItemNode createEarApplicationsNode(Lookup lookup) {
        return new WildflyItemNode(new WildflyEarApplicationsChildren(lookup), NbBundle.getMessage(WildflyTargetNode.class, "LBL_EarApps"), ModuleType.EAR);
    }

    /*
     * Creates an Web Applications parent node
     */
    public static WildflyItemNode createWebApplicationsNode(Lookup lookup) {
        return new WildflyItemNode(new WildflyWebApplicationsChildren(lookup), NbBundle.getMessage(WildflyTargetNode.class, "LBL_WebApps"), ModuleType.WAR);
    }

    /*
     * Creates an EJB Modules parent node
     */
    public static WildflyItemNode createEjbModulesNode(Lookup lookup) {
        return new WildflyItemNode(new WildflyEjbModulesChildren(lookup), NbBundle.getMessage(WildflyTargetNode.class, "LBL_EjbModules"), ModuleType.EJB);
    }
}
