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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 */
public class CloudChildFactory extends ChildFactory<OCIItem> {
    private static final Logger LOG = Logger.getLogger(CloudChildFactory.class.getName());
    
    private final OCIItem parent;
    private final OCISessionInitiator session;
    
    public CloudChildFactory(OCISessionInitiator session, OCIItem parent) {
        this.parent = parent;
        this.session = session;
    }

    public CloudChildFactory(OCIItem parent) {
        this(OCIManager.getDefault().getActiveSession(), parent);
    }

    @Override
    protected boolean createKeys(List<OCIItem> toPopulate) {
        return OCIManager.usingSession(session, () -> {
            Lookup.Result<ChildrenProvider> lkpResult = Lookups.forPath(
                    String.format("Cloud/Oracle/%s/Nodes", parent.getKey().getPath()))
                    .lookupResult(ChildrenProvider.class);
            lkpResult.allItems()
                    .parallelStream()
                    .forEach(it -> {
                        try {
                            ChildrenProvider kp = it.getInstance();
                            if (kp instanceof ChildrenProvider.SessionAware) {
                                toPopulate.addAll(((ChildrenProvider.SessionAware)kp).apply(parent, session));
                            } else {
                                OCIManager.usingSession(session, () -> 
                                    toPopulate.addAll(kp.apply(parent))
                                );
                            }
                        } catch (RuntimeException ex) {
                            // log
                            LOG.log(Level.WARNING, "Error fetching children for {0}/{1}", new Object[] { parent.getKey(), it.getId() });
                        }
            });
            return true;
        });
    }
    
    @Override
    protected Node[] createNodesForKey(OCIItem key) {
        NodeProvider nodeProvider = Lookups.forPath(
                String.format("Cloud/Oracle/%s/Nodes", key.getKey().getPath()))
                .lookup(NodeProvider.class);
        return new Node[]{nodeProvider.apply(key, session)};
    }
    
    public void refreshKeys() {
        refresh(false);
    }

}
