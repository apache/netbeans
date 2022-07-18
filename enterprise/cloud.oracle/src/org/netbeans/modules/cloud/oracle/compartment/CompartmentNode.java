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
package org.netbeans.modules.cloud.oracle.compartment;

import com.oracle.bmc.identity.Identity;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static org.netbeans.modules.cloud.oracle.OCIManager.getDefault;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCINode;

/**
 *
 * @author Jan Horvath
 */
public class CompartmentNode extends OCINode {

    private static final String COMPARTMENT_ICON = "org/netbeans/modules/cloud/oracle/resources/compartment.svg"; // NOI18N

    public CompartmentNode(CompartmentItem compartment) {
        super(compartment);
        setDisplayName(compartment.getName());
        setIconBaseWithExtension(COMPARTMENT_ICON);
    }

    public static NodeProvider<CompartmentItem> createNode() {
        return CompartmentNode::new;
    }

    /**
     * Retrieves list of Compartments in the Tenancy.
     *
     * @param tenancyId OCID of the Tenancy
     * @return List of {@code OCIItem} describing tenancy Compartments
     */
    public static ChildrenProvider<OCIItem, CompartmentItem> getCompartments() {
        return parent -> {
            Identity identityClient = new IdentityClient(getDefault().getConfigProvider());
            identityClient.setRegion(getDefault().getConfigProvider().getRegion());

            List<CompartmentItem> compartments = new ArrayList<>();

            String nextPageToken = null;
            do {
                ListCompartmentsResponse response
                        = identityClient.listCompartments(
                                ListCompartmentsRequest.builder()
                                        .limit(30)
                                        .compartmentId(parent.getKey().getValue())
                                        .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible)
                                        .page(nextPageToken)
                                        .build());
                response.getItems().stream()
                        .map(c -> new CompartmentItem(OCID.of(c.getId(), "Compartment"), c.getName())) // NOI18N
                        .collect(Collectors.toCollection(() -> compartments));
                nextPageToken = response.getOpcNextPage();
            } while (nextPageToken != null);
            return compartments;
        };
    }
}
