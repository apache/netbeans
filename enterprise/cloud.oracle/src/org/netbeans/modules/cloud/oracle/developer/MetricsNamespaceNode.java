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
package org.netbeans.modules.cloud.oracle.developer;

import com.oracle.bmc.monitoring.MonitoringClient;
import com.oracle.bmc.monitoring.model.ListMetricsDetails;
import com.oracle.bmc.monitoring.requests.ListMetricsRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Petrovic
 */
@NbBundle.Messages({
    "MetricsNamespaceDesc=Metrics namespace"
})
public class MetricsNamespaceNode extends OCINode {
    
    private static final String METRICS_NAMESPACE_ICON = "org/netbeans/modules/cloud/oracle/resources/metrics_namespace.svg"; // NOI18N

    public MetricsNamespaceNode(MetricsNamespaceItem instance) {
        super(instance);
        setName(instance.getName());
        setDisplayName(instance.getName());
        setIconBaseWithExtension(METRICS_NAMESPACE_ICON);
        setShortDescription(Bundle.MetricsNamespaceDesc());
    }
    
    public static NodeProvider<MetricsNamespaceItem> createNode() {
        return MetricsNamespaceNode::new;
    }

    /**
     * Retrieves list of Metrics namespaces belonging to a given Compartment.
     *
     * @return Returns {@code ChildrenProvider} which fetches List of
     * {@code BucketItem} for given {@code CompartmentItem}
     */
    public static ChildrenProvider.SessionAware<CompartmentItem, MetricsNamespaceItem> getMetricNamespaces() {
        return (compartmentId, session) -> {
            MonitoringClient client = session.newClient(MonitoringClient.class);
            ListMetricsDetails listMetricsDetails = ListMetricsDetails.builder()
                    .groupBy(List.of("namespace"))
                    .build();
            
            ListMetricsRequest request = ListMetricsRequest.builder()
                    .compartmentId(compartmentId.getKey().getValue())
                    .listMetricsDetails(listMetricsDetails)
                    .build();

            return client.listMetrics(request)
                    .getItems()
                    .stream()
                    .map(d -> new MetricsNamespaceItem(
                            compartmentId.getKey().getValue(),
                            d.getNamespace()
                    ))
                    .collect(Collectors.toList());
        };
    }
}

