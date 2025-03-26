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

import com.oracle.bmc.keymanagement.KmsManagementClient;
import com.oracle.bmc.keymanagement.model.Vault;
import com.oracle.bmc.keymanagement.requests.ListKeysRequest;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({})
public class KeyNode extends OCINode {

    private static final String KEY_ICON = "org/netbeans/modules/cloud/oracle/resources/key.svg"; // NOI18N
    private static final Logger LOG = Logger.getLogger(KeyNode.class.getName());

    public KeyNode(KeyItem key) {
        super(key, Children.LEAF);
        setName(key.getName());
        setDisplayName(key.getName());
        setIconBaseWithExtension(KEY_ICON);
        setShortDescription(key.getDescription());
    }

    public static NodeProvider<KeyItem> createNode() {
        return KeyNode::new;
    }

    /**
     * Retrieves list of Keys belonging to a given Vault.
     *
     * @return Returns {@code ChildrenProvider} which fetches List of {@code KeyItem} for given {@code VaultItem}
     */
    public static ChildrenProvider.SessionAware<VaultItem, KeyItem> getKeys() {
        return (vault, session) -> {
            Vault v = Vault.builder()
                    .compartmentId(vault.getCompartmentId())
                    .id(vault.getKey().getValue())
                    .managementEndpoint(vault.getManagementEndpoint())
                    .build();
            KmsManagementClient client = KmsManagementClient.builder()
                    .vault(v)
                    .build(session.getAuthenticationProvider());
            ListKeysRequest listKeysRequest = ListKeysRequest.builder()
                    .compartmentId(vault.getCompartmentId())
                    .limit(88)
                    .build();

            String tenancyId = session.getTenancy().isPresent() ? session.getTenancy().get().getKey().getValue() : null;
            String regionCode = session.getRegion().getRegionCode();

            return client.listKeys(listKeysRequest)
                    .getItems()
                    .stream()
                    .map(d -> new KeyItem(
                            OCID.of(d.getId(), "Vault/Key"), //NOI18N
                            d.getCompartmentId(),
                            d.getDisplayName(),
                            vault.getKey().getValue(),
                            tenancyId,
                            regionCode)
                    )
                    .collect(Collectors.toList());
        };
    }

}
