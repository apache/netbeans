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
package org.netbeans.modules.cloud.oracle.assets.k8s;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 * Node representing all active port forwards for a given PodItem.
 */
@NbBundle.Messages({
    "PortForwardDisplayName=Local {0} →  Pod {1}"
})
public class PortForwardNode extends AbstractNode {
    private static final String PORT_FORWARD_ICON = "org/netbeans/modules/cloud/oracle/resources/port_forward.svg"; // NOI18N
    private final PortForwardItem portForward;

    /**
     * Creates a new node for an active port forward.
     *
     * @param portForward The PortForward instance to represent.
     */
    public PortForwardNode(PortForwardItem portForward) {
        super(Children.LEAF, Lookups.singleton(portForward));
        this.portForward = portForward;
        setDisplayName(Bundle.PortForwardDisplayName(
                String.valueOf(portForward.portLocal),
                String.valueOf(portForward.portRemote)
                
        ));
        setIconBaseWithExtension(PORT_FORWARD_ICON);
    }

}
