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

import com.oracle.bmc.vault.VaultsClient;
import com.oracle.bmc.vault.requests.ListSecretsRequest;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import static org.netbeans.modules.cloud.oracle.OCIManager.getDefault;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.nodes.Children;

/**
 *
 * @author Jan Horvath
 */
public class SecretNode extends OCINode {
    private static final String SECRET_ICON = "org/netbeans/modules/cloud/oracle/resources/secret.svg"; // NOI18N

    public SecretNode(SecretItem vault) {
        super(vault, Children.LEAF);
        setName(vault.getName());
        setDisplayName(vault.getName());
        setIconBaseWithExtension(SECRET_ICON);
        setShortDescription(vault.getDescription());
    }

    public static NodeProvider<SecretItem> createNode() {
        return SecretNode::new;
    }

    /**
     * Retrieves list of Secrets belonging to a given Vault.
     *
     * @@return Returns {@code ChildrenProvider} which fetches List of {@code SecretItem} for given {@code VaultItem}
     */
    public static ChildrenProvider<VaultItem, SecretItem> getSecrets() {
        return vault -> {
            VaultsClient client = VaultsClient.builder().build(getDefault().getActiveProfile().getConfigProvider());
            
            ListSecretsRequest listSecretsRequest = ListSecretsRequest.builder()
                    .compartmentId(vault.getCompartmentId())
                    .vaultId(vault.getKey().getValue())
                    .limit(88)
                    .build();
                    
            return client.listSecrets(listSecretsRequest)
                    .getItems()
                    .stream()
                    .map(d -> new SecretItem(
                                OCID.of(d.getId(), "Vault/Secret"), //NOI18N
                                d.getSecretName(),
                                d.getCompartmentId())
                    )
                    .collect(Collectors.toList());
        };
    }

}
