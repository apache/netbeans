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
package org.netbeans.modules.cloud.oracle.vault;

import com.oracle.bmc.keymanagement.KmsVaultClient;
import com.oracle.bmc.keymanagement.requests.ListVaultsRequest;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
})
public class VaultNode extends OCINode {
    private static final String VAULT_ICON = "org/netbeans/modules/cloud/oracle/resources/vault.svg"; // NOI18N

    public VaultNode(VaultItem vault) {
        super(vault);
        setName(vault.getName());
        setDisplayName(vault.getName());
        setIconBaseWithExtension(VAULT_ICON);
        setShortDescription(vault.getDescription());
    }

    public static NodeProvider<VaultItem> createNode() {
        return VaultNode::new;
    }

    /**
     * Retrieves list of Vaults belonging to a given Compartment.
     *
     * @return Returns {@code ChildrenProvider} which fetches List of {@code VaultItem} for given {@code CompartmentItem}
     */
    public static ChildrenProvider<CompartmentItem, VaultItem> getVaults() {
        return compartmentId -> {
            KmsVaultClient client = KmsVaultClient.builder().build(OCIManager.getDefault().getActiveProfile().getConfigProvider());
            
            ListVaultsRequest listVaultsRequest = ListVaultsRequest.builder()
                    .compartmentId(compartmentId.getKey().getValue())
                    .limit(88)
                    .build();
                    
            return client.listVaults(listVaultsRequest)
                    .getItems()
                    .stream()
                    .map(d -> new VaultItem(
                                OCID.of(d.getId(), "Vault"), //NOI18N
                                d.getDisplayName(),
                                d.getCompartmentId(),
                                d.getManagementEndpoint())
                    )
                    .collect(Collectors.toList());
        };
    }

}
