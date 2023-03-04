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
 * It describes children nodes of the Applications node
 *
 * @author Michal Mocnak
 */
public class WildflyResourcesChildren extends Children.Keys {

    WildflyResourcesChildren(Lookup lookup) {
        setKeys(new Object[]{createDatasourcesNode(lookup), createJMSNode(lookup), createMailSessionsNode(lookup), createResourceAdaptersNode(lookup)});
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

    final WildflyResourcesItemNode createDatasourcesNode(Lookup lookup) {
        return new WildflyResourcesItemNode(new WildflyDatasourcesChildren(lookup),
                NbBundle.getMessage(WildflyTargetNode.class, "LBL_Resources_Datasources"), Util.JDBC_RESOURCE_ICON);
    }

    final WildflyResourcesItemNode createResourceAdaptersNode(Lookup lookup) {
        return new WildflyResourcesItemNode(new WildflyResourceAdaptersChildren(lookup),
                NbBundle.getMessage(WildflyTargetNode.class, "LBL_Resources_ResourceAdapters"), Util.CONNECTOR_ICON);
    }


    final WildflyResourcesItemNode createMailSessionsNode(Lookup lookup) {
        return new WildflyResourcesItemNode(new WildflyMailSessionsChildren(lookup),
                NbBundle.getMessage(WildflyTargetNode.class, "LBL_Resources_MailSessions"), Util.JAVAMAIL_ICON);
    }

    private Object createJMSNode(Lookup lookup) {
        return new WildflyResourcesItemNode(new WildflyJmsChildren(lookup),
                NbBundle.getMessage(WildflyTargetNode.class, "LBL_Resources_JMS"), Util.JMS_ICON);
    }



}
