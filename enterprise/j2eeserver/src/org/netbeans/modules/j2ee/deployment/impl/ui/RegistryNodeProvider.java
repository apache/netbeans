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

package org.netbeans.modules.j2ee.deployment.impl.ui;


import org.openide.nodes.Node;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstanceLookup;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory;


/*
 * RegistryNodeProvider.java
 *
 * Created on December 19, 2003, 11:21 AM
 * @author  nn136682
 */

public class RegistryNodeProvider {
    RegistryNodeFactory factory;

    /** Creates a new instance of RegistryNodeProvider */
    public RegistryNodeProvider(RegistryNodeFactory factory) {
        this.factory = factory;
    }

    public Node createInstanceNode(ServerInstance instance) {
        return InstanceNodeDecorator.getInstance(createInstanceNodeImpl(instance, true), instance);
    }

    public Node createTargetNode(ServerTarget target) {
        if (factory != null) {
            Node original = factory.getTargetNode(new ServerInstanceLookup(
                    target.getInstance(),
                    target.getInstance().getServer().getDeploymentFactory(),
                    target.getTarget()));
            if (original != null) {
                TargetBaseNode xnode = new TargetBaseNode(org.openide.nodes.Children.LEAF, target);
                return new FilterXNode(original, xnode, true);
            }
        }
        return new TargetBaseNode(org.openide.nodes.Children.LEAF, target);
    }

    public Node createInstanceTargetNode(ServerInstance instance) {
        Node original = createInstanceNodeImpl(instance, false);
        return InstanceNodeDecorator.getInstance(new InstanceTargetXNode(original, instance), instance);
    }

    private Node createInstanceNodeImpl(ServerInstance instance, boolean addStateListener) {
        InstanceNode xnode = new InstanceNode(instance, addStateListener);

        if (factory != null) {
            Node original = factory.getManagerNode(new ServerInstanceLookup(instance,
                    instance.getServer().getDeploymentFactory(), null));
            if (original != null) {
                return new FilterXNode(original, xnode, true, new FilterXNode.XChildren(xnode));
            }
        }
        return xnode;
    }
}
