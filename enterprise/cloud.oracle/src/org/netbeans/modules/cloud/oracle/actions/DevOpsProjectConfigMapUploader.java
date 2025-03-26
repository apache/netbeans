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

import com.oracle.bmc.devops.DevopsClient;
import com.oracle.bmc.devops.model.DeployArtifactSource;
import com.oracle.bmc.devops.model.DeployArtifactSummary;
import com.oracle.bmc.devops.model.InlineDeployArtifactSource;
import com.oracle.bmc.devops.model.UpdateDeployArtifactDetails;
import com.oracle.bmc.devops.requests.GetDeployArtifactRequest;
import com.oracle.bmc.devops.requests.ListDeployArtifactsRequest;
import com.oracle.bmc.devops.requests.UpdateDeployArtifactRequest;
import com.oracle.bmc.devops.responses.GetDeployArtifactResponse;
import com.oracle.bmc.devops.responses.ListDeployArtifactsResponse;
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
import com.oracle.bmc.vault.responses.UpdateSecretResponse;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.netbeans.modules.cloud.oracle.steps.DevopsStep;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.showMessage;
import org.netbeans.modules.cloud.oracle.OCIManager;
import static org.netbeans.modules.cloud.oracle.OCIManager.getDefault;
import org.netbeans.modules.cloud.oracle.assets.CloudAssets;
import org.netbeans.modules.cloud.oracle.assets.DependencyUtils;
import org.netbeans.modules.cloud.oracle.assets.PropertiesGenerator;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.devops.DevopsProjectItem;
import org.netbeans.modules.cloud.oracle.steps.KeyStep;
import org.netbeans.modules.cloud.oracle.steps.PasswordStep;
import org.netbeans.modules.cloud.oracle.steps.ProjectStep;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.steps.CompartmentStep;
import org.netbeans.modules.cloud.oracle.steps.TenancyStep;
import org.netbeans.modules.cloud.oracle.vault.KeyItem;
import org.netbeans.modules.cloud.oracle.vault.VaultItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Command that updates a ConfigMap with the current properties generated from the contents of {@link CloudAssets}.
 * 
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "SuggestVault=For better security when using Autonomous Database, be sure to also add OCI Vault.",
    "UpdatingConfigMap=Updating Config Map",
    "CMUpdated=ConfigMap in \"{0}\" project was updated. The changes will take place only after redeploying the application",
    "NoConfigMap=No ConfigMap found in the Devops project {0}",
})
public class DevOpsProjectConfigMapUploader {

    private static final Logger LOG = Logger.getLogger(DevOpsProjectConfigMapUploader.class.getName());

    public static void uploadConfigMap(CompletableFuture<Object> future) {
        Steps.NextStepProvider.Builder nsProviderBuilder = Steps.NextStepProvider.builder();
        
        nsProviderBuilder.stepForClass(TenancyStep.class, (s) -> new CompartmentStep())
            .stepForClass(CompartmentStep.class, (s) -> new DevopsStep())
            .stepForClass(DevopsStep.class, (s) -> new ProjectStep());
        
        Collection<OCIItem> items = CloudAssets.getDefault().getAssignedItems();
        AtomicReference<VaultItem> vaultRef = new AtomicReference<>();
        AtomicReference<DatabaseItem> dbRef = new AtomicReference<>();
        for (OCIItem item : items) {
            if (item instanceof VaultItem) {
                vaultRef.set((VaultItem) item);
                nsProviderBuilder.stepForClass(PasswordStep.class, (s) -> new KeyStep(vaultRef.get()));
            } else if (item instanceof DatabaseItem) {
                dbRef.set((DatabaseItem) item);
                DatabaseConnection conn = ((DatabaseItem) item).getCorrespondingConnection();
                String user, password;
                if (conn != null) {
                    user = conn.getUser();
                    password = conn.getPassword();
                } else {
                    user = null;
                    password = null;
                }
                nsProviderBuilder.stepForClass(ProjectStep.class, (s) -> new PasswordStep(user, password));
            }
        }
        
        if (vaultRef.get() == null && dbRef.get() != null) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(Bundle.SuggestVault(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(msg);
        }
        
        Lookup lookup = Lookups.fixed(nsProviderBuilder.build());
        Steps.getDefault()
            .executeMultistep(new TenancyStep(), lookup)
            .thenAccept(values -> {
                Project project = values.getValueForStep(ProjectStep.class);
                KeyItem key = values.getValueForStep(KeyStep.class);
                DevopsProjectItem devopsProject = values.getValueForStep(DevopsStep.class);

                ProgressHandle h = ProgressHandle.createHandle(Bundle.UpdatingConfigMap());
                try {
                    PropertiesGenerator propGen = new PropertiesGenerator(false);
                    if (vaultRef.get() != null) {
                        updateVault(h, key, vaultRef.get(), propGen, project);
                    }
                    if (updateConfigMap(h, devopsProject, propGen)) {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(Bundle.CMUpdated(devopsProject.getName()), NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(msg);
                    } else {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(Bundle.NoConfigMap(devopsProject.getName()), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(msg);
                    }
                    future.complete(null);
                } catch(ThreadDeath e) {
                    future.completeExceptionally(e);
                    throw e;
                } catch (Throwable e) {
                    future.completeExceptionally(e);
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                } finally {
                    h.finish();
                }
            });
    }

    private static boolean updateConfigMap(ProgressHandle h, DevopsProjectItem devopsProject, PropertiesGenerator propGen) {
        // Add Vault to the ConfigMap artifact
        DevopsClient devopsClient = DevopsClient.builder().build(OCIManager.getDefault().getActiveProfile(devopsProject).getConfigProvider());
        ListDeployArtifactsRequest request = ListDeployArtifactsRequest.builder()
                .projectId(devopsProject.getKey().getValue()).build();
        ListDeployArtifactsResponse response = devopsClient.listDeployArtifacts(request);
        List<DeployArtifactSummary> artifacts = response.getDeployArtifactCollection().getItems();
        boolean found = false;
        for (DeployArtifactSummary artifact : artifacts) {
            if ((devopsProject.getName() + "_oke_configmap").equals(artifact.getDisplayName())) { //NOI18N
                h.progress("updating  " + devopsProject.getName() + "_oke_configmap"); //NOI18N
                found = true;
                GetDeployArtifactRequest artRequest = GetDeployArtifactRequest.builder().deployArtifactId(artifact.getId()).build();
                GetDeployArtifactResponse artResponse = devopsClient.getDeployArtifact(artRequest);
                DeployArtifactSource source = artResponse.getDeployArtifact().getDeployArtifactSource();
                if (source instanceof InlineDeployArtifactSource) {
                    byte[] content = ((InlineDeployArtifactSource) source).getBase64EncodedContent();
                    String srcString = updateProperties(new String(content, StandardCharsets.UTF_8), propGen);
                    byte[] base64Content = Base64.getEncoder().encode(srcString.getBytes(StandardCharsets.UTF_8));
                    DeployArtifactSource updatedSource = InlineDeployArtifactSource.builder()
                            .base64EncodedContent(base64Content).build();
                    UpdateDeployArtifactDetails updateArtifactDetails = UpdateDeployArtifactDetails.builder()
                            .deployArtifactSource(updatedSource)
                            .build();
                    UpdateDeployArtifactRequest updateArtifactRequest = UpdateDeployArtifactRequest.builder()
                            .updateDeployArtifactDetails(updateArtifactDetails)
                            .deployArtifactId(artifact.getId())
                            .build();
                    devopsClient.updateDeployArtifact(updateArtifactRequest);
                }
            }
        }
        return found;
    }

    private static void updateVault(ProgressHandle h, KeyItem key, VaultItem vault, PropertiesGenerator propGen, Project project) {
        h.progress(Bundle.UpdatingVault(vault.getName()));
        VaultsClient client = VaultsClient.builder().build(getDefault().getActiveProfile(vault).getConfigProvider());
        ListSecretsRequest listSecretsRequest = ListSecretsRequest.builder()
                .compartmentId(vault.getCompartmentId())
                .vaultId(vault.getKey().getValue())
                .limit(88)
                .build();
        ListSecretsResponse secrets = client.listSecrets(listSecretsRequest);
        Map<String, String> existingSecrets = secrets.getItems().stream()
                .collect(Collectors.toMap(s -> s.getSecretName(), s -> s.getId()));

        for (Map.Entry<String, String> entry : propGen.getVaultSecrets().entrySet()) {
            String secretName = entry.getKey();
            String base64Content = Base64.getEncoder().encodeToString(entry.getValue().getBytes(StandardCharsets.UTF_8));

            SecretContentDetails contentDetails = Base64SecretContentDetails.builder()
                    .content(base64Content)
                    .stage(SecretContentDetails.Stage.Current).build();
            if (existingSecrets.containsKey(secretName)) {
                h.progress(Bundle.UpdatingSecret(secretName));
                UpdateSecretDetails updateSecretDetails = UpdateSecretDetails.builder()
                        .secretContent(contentDetails)
                        .build();
                UpdateSecretRequest request = UpdateSecretRequest.builder()
                        .secretId(existingSecrets.get(secretName))
                        .updateSecretDetails(updateSecretDetails)
                        .build();
                try {
                    UpdateSecretResponse response = client.updateSecret(request);
                } catch (BmcException ex) {
                    // Update fails if the new value is same as the current one. It is safe to ignore
                    LOG.log(Level.WARNING, "Update of secret failed", ex);
                }
            } else {
                h.progress(Bundle.CreatingSecret(secretName));
                CreateSecretDetails createDetails = CreateSecretDetails.builder()
                        .secretName(secretName)
                        .secretContent(contentDetails)
                        .secretRules(new ArrayList<>(Arrays.asList(SecretReuseRule.builder()
                                .isEnforcedOnDeletedSecretVersions(false).build())))
                        .compartmentId(vault.getCompartmentId())
                        .vaultId(vault.getKey().getValue())
                        .keyId(key.getKey().getValue())
                        .build();
                CreateSecretRequest request = CreateSecretRequest
                        .builder()
                        .createSecretDetails(createDetails)
                        .build();
                client.createSecret(request);
            }
        }

        // Add Vault dependency to the project
        try {
            DependencyUtils.addDependency(project, new String[] {"io.micronaut.oraclecloud", "micronaut-oraclecloud-vault"});
        } catch (IllegalStateException e) {
            LOG.log(Level.INFO, "Unable to add Vault dependency", e);
        }
    }

    protected static String updateProperties(String configmap, PropertiesGenerator propGen) {
        StringWriter output = new StringWriter();
        String[] lines = configmap.split("\n");
        int previousIndent = 0;
        Map<Integer, String> path = new LinkedHashMap<>();
        String propertiesName = null;
        Map<String, String> properties = new LinkedHashMap<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().startsWith("#") || line.isEmpty()) {
                output.append(line);
                output.append("\n");
                continue;
            }
            int indent = 0;
            while (line.charAt(indent) == ' ') {
                indent++;
            }
            if (previousIndent > indent || (propertiesName != null && !line.contains("="))) {
                final int f = indent;
                path.entrySet().removeIf(entry -> entry.getKey() >= f);
                if (propertiesName != null) {
                    int propIndent = previousIndent;
                    if (properties.isEmpty()) {
                        propIndent = indent + 2;
                    }
                    output.append(
                            formatProperties(propertiesName, properties, propIndent, propGen));

                    properties.clear();
                }
                propertiesName = null;
                if (line.trim().equals("---")) { //NOI18N
                    output.append(line);
                    output.append("\n");
                    continue;
                }
            }
            if (propertiesName == null) {
                if (line.indexOf(':') < 0) {
                    throw new IllegalStateException("Invalid ConfigMap format"); //NOI18N
                }
                String k = line.substring(0, line.indexOf(':')).trim();
                String v = line.substring(line.indexOf(':') + 1).trim();
                if (k == null) {
                    throw new IllegalStateException();
                }

                path.put(indent, k);
                output.append(line);
                output.append("\n");
                if (v.trim().equals("|")) {
                    propertiesName = k;
                    continue;
                }
            }
            if (propertiesName != null && line.contains("=")) {
                properties.put(line.substring(0, line.indexOf('=')).trim(),
                        line.substring(line.indexOf('=') + 1).trim());
            }
            previousIndent = indent;
        }
        output.append(formatProperties(propertiesName, properties, previousIndent, propGen));

        return output.toString();
    }

    
    private static String formatProperties(String proprtiesName, Map<String, String> prop, int indent, PropertiesGenerator propGen) {
        StringBuilder output = new StringBuilder();
        prop.entrySet().removeIf(entry -> propGen.getAllPropertiesNames().contains(entry.getKey()));
        if (proprtiesName.startsWith("bootstrap")) { // NOI18N
            prop.putAll(propGen.getBootstrap());
        } else if (proprtiesName.startsWith("application")) { // NOI18N
            prop.putAll(propGen.getApplication());
        }
        for (Map.Entry<String, String> entry : prop.entrySet()) {
            output.append(new String(new char[indent]).replace('\0', ' '));
            output.append(entry.getKey());
            output.append("=");
            output.append(entry.getValue());
            output.append("\n");
        }
        return output.toString();
    }
    
}
