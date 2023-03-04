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


package org.netbeans.tests.j2eeserver.plugin.registry;

import org.netbeans.tests.j2eeserver.plugin.jsr88.*;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
/**
 *
 * @author  nn136682
 */
public class NodeFactory implements org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory {

    /** Creates a new instance of NodeFactory */
    public NodeFactory() {
    }

    public org.openide.nodes.Node getFactoryNode(org.openide.util.Lookup lookup) {
        DeploymentFactory depFactory = (DeploymentFactory) lookup.lookup(DeploymentFactory.class);
        if (depFactory == null || ! (depFactory instanceof TestDeploymentFactory)) {
            System.out.println("WARNING: getFactoryNode lookup returned "+depFactory);
            return null;
        }
        System.out.println("INFO: getFactoryNode returning new plugin node");
        return new PluginNode((TestDeploymentFactory)depFactory);
    }
    
    public org.openide.nodes.Node getManagerNode(org.openide.util.Lookup lookup) {
        DeploymentManager depManager = (DeploymentManager) lookup.lookup(DeploymentManager.class);
        if (depManager == null || ! (depManager instanceof TestDeploymentManager)) {
            System.out.println("WARNING: getManagerNode lookup returned "+depManager);
            return null;
        }
        System.out.println("INFO: getManagerNode returning new Manager node");
        return new ManagerNode((TestDeploymentManager)depManager);
    }
    
    public org.openide.nodes.Node getTargetNode(org.openide.util.Lookup lookup) {
        Target target = (Target) lookup.lookup(Target.class);
        if (target == null || ! (target instanceof TestTarget) ) {
            System.out.println("WARNING: getTargetNode lookup returned "+target);
            return null;
        }
        System.out.println("INFO: getManagerNode returning new Target node");
        return new TargNode((TestTarget)target);
    }
}
