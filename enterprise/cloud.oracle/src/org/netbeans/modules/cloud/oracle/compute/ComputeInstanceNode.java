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

import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.requests.ListInstancesRequest;
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
    "CoputeInstanceDesc=Compute Instance: {0}"
})
public class ComputeInstanceNode extends OCINode {
    private static final String COMPUTE_INSTANCE_ICON = "org/netbeans/modules/cloud/oracle/resources/computeinstance.svg"; // NOI18N

    public ComputeInstanceNode(ComputeInstanceItem instance) {
        super(instance, Children.LEAF);
        setName(instance.getName());
        setDisplayName(instance.getName());
        setIconBaseWithExtension(COMPUTE_INSTANCE_ICON);
        setShortDescription(Bundle.CoputeInstanceDesc(instance.getName()));
    }

    public static NodeProvider<ComputeInstanceItem> createNode() {
        return ComputeInstanceNode::new;
    }

    /**
     * Retrieves list of Vaults belonging to a given Compartment.
     *
     * @return Returns {@code ChildrenProvider} which fetches List of
     * {@code BucketItem} for given {@code CompartmentItem}
     */
    public static ChildrenProvider.SessionAware<CompartmentItem, ComputeInstanceItem> getComputeInstances() {
        return (compartmentId, session) -> {
            ComputeClient client = session.newClient(ComputeClient.class);

            ListInstancesRequest listInstancesRequest = ListInstancesRequest.builder()
                    .compartmentId(compartmentId.getKey().getValue())
                    .limit(88)
                    .build();

            return client.listInstances(listInstancesRequest)
                    .getItems()
                    .stream()
                    .filter(c -> !c.getLifecycleState().equals(Instance.LifecycleState.Terminated))
                    .filter(c -> !c.getFreeformTags().containsKey("OKEnodePoolName"))
                    .map(d -> new ComputeInstanceItem(
                        OCID.of(d.getId(), "ComputeInstance"), //NOI18N
                            compartmentId.getKey().getValue(),
                        d.getDisplayName()
                    ))
                    .collect(Collectors.toList());
        };
    }
    
}
