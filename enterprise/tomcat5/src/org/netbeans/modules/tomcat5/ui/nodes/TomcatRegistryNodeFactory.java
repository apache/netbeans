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

package org.netbeans.modules.tomcat5.ui.nodes;

import org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * @author Petr Pisl
 */

public class TomcatRegistryNodeFactory implements RegistryNodeFactory {


    /** Creates a new instance of TomcatRegistryNodeFactory */
    public TomcatRegistryNodeFactory() {
    }

    /**
      * Return node representing the admin server.  Children of this node are filtered.
      * @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
      * @return admin server node.
      */
    @Override
    public Node getManagerNode(Lookup lookup) {
        TomcatInstanceNode tn = new TomcatInstanceNode (new Children.Map(), lookup);
        return tn;
    }
    
    /**
      * Provide node representing JSR88 Target object.  
      * @param lookup will contain DeploymentFactory, DeploymentManager, Target, Management objects.
      * @return target server node
      */
    @Override
    public Node getTargetNode(Lookup lookup) {
        return new TomcatTargetNode(lookup);

    }
}
