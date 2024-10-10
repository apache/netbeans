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

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.vault.VaultsClient;
import com.oracle.bmc.vault.model.Base64SecretContentDetails;
import com.oracle.bmc.vault.model.CreateSecretDetails;
import com.oracle.bmc.vault.model.SecretContentDetails;
import com.oracle.bmc.vault.model.SecretReuseRule;
import com.oracle.bmc.vault.model.UpdateSecretDetails;
import com.oracle.bmc.vault.requests.CreateSecretRequest;
import com.oracle.bmc.vault.requests.ListSecretsRequest;
import com.oracle.bmc.vault.requests.UpdateSecretRequest;
import com.oracle.bmc.vault.responses.ListSecretsResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cloud.oracle.OCIManager;

/**
 *
 * @author Dusan Petrovic
 */
public class VaultItemClient {
    private static final Logger LOG = Logger.getLogger(VaultItemClient.class.getName());

    private final VaultsClient client;
    private final VaultItem context;

    public VaultItemClient(VaultItem context) {
        this.client = VaultsClient.builder().build(OCIManager.getDefault().getActiveProfile(context).getConfigProvider());
        this.context = context;
    }
    
    public ListSecretsResponse listSecrets(ListSecretsRequest request) {
        return client.listSecrets(request);
    }
    
    public void createNewSecret(Map.Entry<String, String> secret, KeyItem key) {
        SecretContentDetails secretContent = generateSecretContent(secret.getValue());
        CreateSecretDetails createDetails = CreateSecretDetails.builder()
                .secretName(secret.getKey())
                .secretContent(secretContent)
                .secretRules(new ArrayList<>(Arrays.asList(SecretReuseRule.builder()
                        .isEnforcedOnDeletedSecretVersions(false).build())))
                .compartmentId(context.getCompartmentId())
                .vaultId(context.getKey().getValue())
                .keyId(key.getKey().getValue())
                .build();
        CreateSecretRequest request = CreateSecretRequest.builder()
                .createSecretDetails(createDetails)
                .build();
        client.createSecret(request);
    }

    public void updateExistingSecret(Map.Entry<String, String> secret, String secretId) {
        SecretContentDetails secretContent = generateSecretContent(secret.getValue());
        UpdateSecretDetails updateSecretDetails = UpdateSecretDetails.builder()
                .secretContent(secretContent)
                .build();
        UpdateSecretRequest request = UpdateSecretRequest.builder()
                .secretId(secretId)
                .updateSecretDetails(updateSecretDetails)
                .build();
        try {
            client.updateSecret(request);
        } catch (BmcException ex) {
            // Update fails if the new value is same as the current one. It is safe to ignore
            LOG.log(Level.WARNING, "Update of secret failed", ex);
        }
    }
    
    private SecretContentDetails generateSecretContent(String secretValue) {
        String base64Content = Base64.getEncoder().encodeToString(secretValue.getBytes(StandardCharsets.UTF_8));
                    
        return Base64SecretContentDetails.builder()
                .content(base64Content)
                .stage(SecretContentDetails.Stage.Current)
                .build();
    }
}
