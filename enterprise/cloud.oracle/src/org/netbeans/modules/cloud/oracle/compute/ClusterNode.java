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
package org.netbeans.modules.cloud.oracle.compute;

import com.oracle.bmc.containerengine.ContainerEngineClient;
import com.oracle.bmc.containerengine.requests.ListClustersRequest;
import com.oracle.bmc.core.model.Instance;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "ClusterDesc=Cluster: {0}"
})
public class ClusterNode extends OCINode {
    private static final String CLUSTER_ICON = "org/netbeans/modules/cloud/oracle/resources/cluster.svg"; // NOI18N

    public ClusterNode(ClusterItem cluster) {
        super(cluster, Children.LEAF);
        setName(cluster.getName());
        setDisplayName(cluster.getName());
        setIconBaseWithExtension(CLUSTER_ICON);
        setShortDescription(Bundle.ClusterDesc(cluster.getName()));
    }

    public static NodeProvider<ClusterItem> createNode() {
        return ClusterNode::new;
    }

    /**
     * Retrieves list of Vaults belonging to a given Compartment.
     *
     * @return Returns {@code ChildrenProvider} which fetches List of
     * {@code BucketItem} for given {@code CompartmentItem}
     */
    public static ChildrenProvider.SessionAware<CompartmentItem, ClusterItem> getClusters() {
        return (compartmentId, session) -> {
            ContainerEngineClient client = session.newClient(ContainerEngineClient.class);

            ListClustersRequest listClustersRequest = ListClustersRequest.builder()
                    .compartmentId(compartmentId.getKey().getValue())
                    .limit(88)
                    .build();

            return client.listClusters(listClustersRequest)
                    .getItems()
                    .stream()
                    .filter(c -> !c.getLifecycleState().equals(Instance.LifecycleState.Terminated))
                    .map(d -> new ClusterItem(
                        OCID.of(d.getId(), "Cluster"), //NOI18N
                            compartmentId.getKey().getValue(),
                        d.getName()
                    ))
                    .collect(Collectors.toList());
        };
    }
}
