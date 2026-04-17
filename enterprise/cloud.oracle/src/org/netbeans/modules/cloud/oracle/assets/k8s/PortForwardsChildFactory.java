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

import java.util.List;
import org.netbeans.modules.cloud.oracle.RefreshableKeys;
import org.openide.nodes.Node;

/**
 * Factory to create child nodes for active port forwards of a PodItem.
 */
class PortForwardsChildFactory extends org.openide.nodes.ChildFactory<PortForwardItem> implements RefreshableKeys {

    private final PodItem podItem;

    /**
     * Creates a new factory for child nodes of port forwards.
     *
     * @param podItem The PodItem to fetch port forwards for.
     */
    public PortForwardsChildFactory(PodItem podItem) {
        this.podItem = podItem;
        PortForwards.getDefault().addPropertyChangeListener(podItem, evt -> {
            if (podItem.getName().equals(evt.getPropertyName())) {
                refreshKeys();
            }
        });
    }

    @Override
    protected boolean createKeys(List<PortForwardItem> toPopulate) {
        List<PortForwardItem> portForwards = PortForwards.getDefault().getActivePortForwards(podItem);
        if (portForwards != null) {
            toPopulate.addAll(portForwards);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(PortForwardItem portForward) {
        return new PortForwardNode(portForward);
    }

    @Override
    public void refreshKeys() {
        refresh(false);
    }
    
}
