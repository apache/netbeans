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
package org.netbeans.modules.cloud.oracle;

import java.util.List;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 */
public class OCIChildFactory extends ChildFactory<OCIItem> {

    private final OCIItem parent;

    public OCIChildFactory(OCIItem parent) {
        this.parent = parent;
    }

    @Override
    protected boolean createKeys(List<OCIItem> toPopulate) {
        Lookup.Result<ChildrenProvider> lkpResult = Lookups.forPath(
                String.format("Cloud/Oracle/%s/Nodes", parent.getKey().getPath()))
                .lookupResult(ChildrenProvider.class);
        lkpResult.allInstances()
                .parallelStream()
                .forEach(kp -> toPopulate.addAll(kp.apply(parent)));
        return true;
    }
    
    @Override
    protected Node[] createNodesForKey(OCIItem key) {
        NodeProvider nodeProvider = Lookups.forPath(
                String.format("Cloud/%s/Nodes", key.getKey().getPath()))
                .lookup(NodeProvider.class);
        return new Node[]{nodeProvider.apply(key)};
    }
    
    public void refreshKeys() {
        refresh(false);
    }

}
