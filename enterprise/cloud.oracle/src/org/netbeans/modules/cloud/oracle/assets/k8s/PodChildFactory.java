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
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Jan Horvath
 */
public class PodChildFactory extends ChildFactory<PodItem> implements RefreshableKeys {

    Pods pods;

    public PodChildFactory(ClusterItem cluster) {
        pods = Pods.from(cluster);
        pods.addPropertyChangeListener(evt -> refreshKeys());
    }

    @Override
    protected boolean createKeys(List<PodItem> toPopulate) {
        toPopulate.addAll(pods.getItems());
        return true;
    }

    @Override
    protected Node[] createNodesForKey(PodItem key) {
        return new Node[]{
            new PodNode(key)
        };
    }

    @Override
    public void refreshKeys() {
        refresh(false);
    }
    
    
}
