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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Factory that creates nodes that will appear in the server registry.
 *
 * @author Kirill Sorokin
 */
public class WLRegistryNodeFactory implements RegistryNodeFactory {

    /**
     * Creates a node that represents a concrete target in a particular server
     * instance. By default it is filtered and does not get visible
     *
     * @param lookup a lookup with useful objects such as the deployment 
     *      manager for the instance
     * 
     * @return the node for the target
     */
    public Node getTargetNode(Lookup lookup) {
        return new WLTargetNode(lookup);
    }
    
    /**
     * Creates a node that represents a particular server instance.
     * 
     * @param lookup a lookup with useful objects such as the deployment 
     *      manager for the instance
     * 
     * @return the node for the instance
     */
    public Node getManagerNode(Lookup lookup) {
        return new WLManagerNode(new Children.Map(), lookup);
    }
    
}
