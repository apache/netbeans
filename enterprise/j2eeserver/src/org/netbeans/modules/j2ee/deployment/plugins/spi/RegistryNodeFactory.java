/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi;


import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * <i>Do not use this class anymore, use Common Server SPI to display nodes.</i>
 *
 * This interface allows plugin to create all the registry nodes
 * (other than the root node) as {@link org.openide.nodes.Node} subclasses,
 * and use {@link org.openide.nodes.FilterNode} to generate the display,
 * adding infrastructure actions in, and exposing certain infrastructure to
 * the plugins for use in constructing nodes.
 * Use a look-like infrastructure so migration to looks can happen easier.
 * Plugins need to register an instance of this class in module layer in folder
 * <code>J2EE/DeploymentPlugins/{plugin_name}</code>.
 *
 * @see org.openide.nodes.Node
 * @see org.openide.nodes.FilterNode
 *
 * @author  George Finklang
 * @deprecated use the Common Server SPI for registering nodes
 */
@Deprecated
public interface RegistryNodeFactory {

     /**
      * Return node representing the admin server.  Children of this node are filtered.
      * Start/Stop/Remove/SetAsDefault actions will be added by FilterNode if appropriate.
      * @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
      * @return admin server node.
      */
     public Node getManagerNode(Lookup lookup);

     /**
      * Provide node representing Deployment API Target object.  
      * Start/Stop/SetAsDefault actions will be added by FilterNode if appropriate.
      * @param lookup will contain DeploymentFactory, DeploymentManager, Target, Management objects.
      * @return target server node
      */
     public Node getTargetNode(Lookup lookup);
}
