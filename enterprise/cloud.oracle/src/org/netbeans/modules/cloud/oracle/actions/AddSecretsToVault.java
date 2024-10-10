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
package org.netbeans.modules.cloud.oracle.actions;

import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.bmc.vault.requests.ListSecretsRequest;
import com.oracle.bmc.vault.responses.ListSecretsResponse;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.progress.ProgressHandle;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.showMessage;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.showWarningMessage;
import org.netbeans.modules.cloud.oracle.assets.CloudAssets;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.steps.KeyStep;
import org.netbeans.modules.cloud.oracle.vault.KeyItem;
import org.netbeans.modules.cloud.oracle.vault.SensitiveData;
import org.netbeans.modules.cloud.oracle.vault.VaultItem;
import org.netbeans.modules.cloud.oracle.vault.VaultItemClient;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Petrovic
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.AddSecretsToVault"
)
@ActionRegistration(
        displayName = "#AddSecretsToVault",
        asynchronous = true
)
@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Vault/Actions", position = 250)
})
@NbBundle.Messages({
    "AddSecretsToVault=Add Cloud Assets Secrets to OCI Vault",
    "ReadingSecrets=Reading existing Secrets",
    "CreatingSecret=Creating secret {0}",
    "UpdatingSecret=Updating secret {0}",
    "SecretsCreated=Secrets were created or updated",
    "UpdatingVault=Updating {0} Vault"
})
public class AddSecretsToVault implements ActionListener {
    private static final Logger LOG = Logger.getLogger(AddSecretsToVault.class.getName());

    private final VaultItem context;

    public AddSecretsToVault(VaultItem context) {
        this.context = context;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Steps.getDefault().executeMultistep(new KeyStep(context), Lookup.EMPTY).thenAccept(vals -> {  
            KeyItem key = vals.getValueForStep(KeyStep.class);

            ProgressHandle h = ProgressHandle.createHandle(Bundle.UpdatingVault(context.getName()));
            h.start();
            h.progress(Bundle.ReadingSecrets());

            try {
                VaultItemClient vclient = new VaultItemClient(context);
                Map<String, String> existingSecrets = getExistingSecrets(vclient).stream()
                        .collect(Collectors.toMap(s -> s.getSecretName(), s -> s.getId()));
                
                Map<String, String> cloudAssetsSecrets = getCloudAssetsSecrets();
                for (Map.Entry<String, String> entry : cloudAssetsSecrets.entrySet()) { 
                    String secretName = entry.getKey();
                    if (existingSecrets.containsKey(secretName)) {
                        h.progress(Bundle.UpdatingSecret(secretName));
                        vclient.updateExistingSecret(entry, existingSecrets.get(secretName));
                    } else {
                        h.progress(Bundle.CreatingSecret(secretName));
                        vclient.createNewSecret(entry, key);
                    }
                }
                showMessage(Bundle.SecretsCreated());
            } catch (Throwable ex) {
                h.finish();
                showWarningMessage(ex.getMessage());
            } finally {
                h.finish();
            }
        });
    }
    
    private List<SecretSummary> getExistingSecrets(VaultItemClient client) {
        ListSecretsRequest listSecretsRequest = ListSecretsRequest.builder()
            .compartmentId(context.getCompartmentId())
            .vaultId(context.getKey().getValue())
            .limit(88)
            .build();
        
        ListSecretsResponse secrets = client.listSecrets(listSecretsRequest);
        return secrets.getItems();
    }
    
    private Map<String, String> getCloudAssetsSecrets() {
        Map<String, String> secrets = new HashMap<>();
        Collection<OCIItem> items = CloudAssets.getDefault().getItems();
        for (OCIItem item : items) {
            if (item instanceof SensitiveData) {
                secrets.putAll(((SensitiveData) item).getSecrets());
            }
        }
        return secrets;
    }
    
}
