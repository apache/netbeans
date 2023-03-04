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

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * It describes children nodes of the EJB Modules node. Implements Refreshable
 * interface and due to it can be refreshed via ResreshModulesAction.
 *
 * @author Michal Mocnak
 */
public class WildflyJmsChildren  extends Children.Keys {

    WildflyJmsChildren(Lookup lookup) {
        setKeys(new Object[]{createDestinationsNode(lookup), createConnectionFactoriesNode(lookup)});
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


    final WildflyResourcesItemNode createConnectionFactoriesNode(Lookup lookup) {
        return new WildflyResourcesItemNode(new WildflyConnectionFactoriesChildren(lookup),
                NbBundle.getMessage(WildflyTargetNode.class, "LBL_Resources_ConnectionFactories"), Util.CONNECTOR_ICON);
    }

    final WildflyResourcesItemNode createDestinationsNode(Lookup lookup) {
        return new WildflyResourcesItemNode(new WildflyDestinationsChildren(lookup),
                NbBundle.getMessage(WildflyTargetNode.class, "LBL_Resources_Destinations"), Util.JMS_ICON);
    }
}
