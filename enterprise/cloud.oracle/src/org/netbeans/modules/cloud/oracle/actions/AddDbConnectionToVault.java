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
import com.oracle.bmc.devops.model.ProjectSummary;
import com.oracle.bmc.devops.model.UpdateDeployArtifactDetails;
import com.oracle.bmc.devops.requests.GetDeployArtifactRequest;
import com.oracle.bmc.devops.requests.ListDeployArtifactsRequest;
import com.oracle.bmc.devops.requests.ListProjectsRequest;
import com.oracle.bmc.devops.requests.UpdateDeployArtifactRequest;
import com.oracle.bmc.devops.responses.GetDeployArtifactResponse;
import com.oracle.bmc.devops.responses.ListDeployArtifactsResponse;
import com.oracle.bmc.devops.responses.ListProjectsResponse;
import com.oracle.bmc.identity.Identity;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import com.oracle.bmc.identity.model.Tenancy;
import org.netbeans.api.db.explorer.DatabaseConnection;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.OCIManager;
import static org.netbeans.modules.cloud.oracle.OCIManager.getDefault;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.OCISessionInitiator;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.devops.DevopsProjectItem;
import org.netbeans.modules.cloud.oracle.devops.DevopsProjectService;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.items.TenancyItem;
import org.netbeans.modules.cloud.oracle.vault.KeyItem;
import org.netbeans.modules.cloud.oracle.vault.KeyNode;
import org.netbeans.modules.cloud.oracle.vault.SecretItem;
import org.netbeans.modules.cloud.oracle.vault.SecretNode;
import org.netbeans.modules.cloud.oracle.vault.VaultItem;
import org.netbeans.modules.cloud.oracle.vault.VaultNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.QuickPick.Item;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.AddDbConnectionToVault"
)
@ActionRegistration(
        displayName = "#AddADBToVault",
        asynchronous = true
)
@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Databases/Actions", position = 250)
})
@NbBundle.Messages({
    "AddADBToVault=Add Oracle Autonomous DB details to OCI Vault",
    "SelectKey=Select Key",
    "SelectVault=Select Vault",
    "SecretsCreated=Secrets were created or updated",
    "NoKeys=No keys in this Vault. Select another one.",
    "DatasourceName=Datasource Name",
    "AddVersion=Add new versions",
    "Cancel=Cancel",
    "SecretExists=Secrets with name {0} already exists",
    "NoProfile=There is not any OCI profile in the config",
    "NoCompartment=There are no compartments in the Tenancy",
    "Password=Enter password for Database user {0}",
    "NoConfigMap=No ConfigMap found in the Devops project {0}",
    "SelectDevopsProject=Select Devops Project",
    "NoDevopsProjects=There are no Devops Projects in selected Compartment",
    "ConfigmapUpdateFailed=Failed to update ConfigMap",
    "CreatingSecret=Creating secret {0}",
    "UpdatingSecret=Updating secret {0}",
    "UpdatingVault=Updating {0} Vault",
    "ReadingSecrets=Reading existing Secrets",
    "DatasourceEmpty=Datasource name cannot be empty"
})
public class AddDbConnectionToVault implements ActionListener {

    private static final Logger LOG = Logger.getLogger(AddDbConnectionToVault.class.getName());

    private final DatabaseConnection context;

    public AddDbConnectionToVault(DatabaseConnection context) {
        this.context = context;
    }

    static interface Step<T, U> {

        Step<T, U> prepare(T item);

        NotifyDescriptor createInput();

        boolean onlyOneChoice();

        Step getNext();

        void setValue(String selected);

        U getValue();
    }

    class TenancyStep implements Step<Object, TenancyItem> {

        List<OCIProfile> profiles = new LinkedList<>();
        private AtomicReference<TenancyItem> selected = new AtomicReference<>();

        @Override
        public NotifyDescriptor createInput() {
            if (onlyOneChoice()) {
                throw new IllegalStateException("No data to create input"); // NOI18N
            }
            String title = Bundle.SelectProfile();
            List<NotifyDescriptor.QuickPick.Item> items = new ArrayList<>(profiles.size());
            for (OCIProfile p : profiles) {
                Tenancy t = p.getTenancyData();
                if (t != null) {
                    items.add(new NotifyDescriptor.QuickPick.Item(p.getId(), Bundle.SelectProfile_Description(t.getName(), t.getHomeRegionKey())));
                }
            }
            if (profiles.stream().filter(p -> p.getTenancy().isPresent()).count() == 0) {
                title = Bundle.NoProfile();
            }
            return new NotifyDescriptor.QuickPick(title, title, items, false);
        }

        @Override
        public Step getNext() {
            return new CompartmentStep().prepare(getValue());
        }

        public Step<Object, TenancyItem> prepare(Object i) {
            ProgressHandle h = ProgressHandle.createHandle(Bundle.MSG_CollectingProfiles());
            h.start();
            h.progress(Bundle.MSG_CollectingProfiles_Text());
            try {
                profiles = OCIManager.getDefault().getConnectedProfiles();
            } finally {
                h.finish();
            }
            return this;
        }

        public void setValue(String value) {
            for (OCIProfile profile : profiles) {
                if (profile.getId().equals(value)) {
                    profile.getTenancy().ifPresent(t -> this.selected.set(t));
                    OCIManager.getDefault().setActiveProfile(profile);
                    break;
                }
            }
        }

        @Override
        public TenancyItem getValue() {
            if (onlyOneChoice()) {
                return profiles.stream()
                        .map(p -> p.getTenancy())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .get();
            }
            return selected.get();
        }

        @Override
        public boolean onlyOneChoice() {
            return profiles.stream().filter(p -> p.getTenancy().isPresent()).count() == 1;
        }
    }

    class CompartmentStep implements Step<TenancyItem, CompartmentItem> {

        private Map<String, OCIItem> compartments = null;
        private CompartmentItem selected;

        public Step<TenancyItem, CompartmentItem> prepare(TenancyItem tenancy) {
            ProgressHandle h = ProgressHandle.createHandle(Bundle.MSG_CollectingItems());
            h.start();
            h.progress(Bundle.MSG_CollectingItems_Text());
            try {
                compartments = getFlatCompartment(tenancy);
            } finally {
                h.finish();
            }
            return this;
        }

        @Override
        public NotifyDescriptor createInput() {
            if (onlyOneChoice()) {
                throw new IllegalStateException("Input shouldn't be displayed for one choice"); // NOI18N
            }
            if (compartments.isEmpty()) {
                createQuickPick(compartments, Bundle.NoCompartment());
            }
            return createQuickPick(compartments, Bundle.SelectCompartment());
        }

        @Override
        public Step getNext() {
            return new VaultStep().prepare(getValue());
        }

        @Override
        public void setValue(String selected) {
            this.selected = (CompartmentItem) compartments.get(selected);
        }

        @Override
        public CompartmentItem getValue() {
            if (onlyOneChoice()) {
                return (CompartmentItem) compartments.values().iterator().next();
            }
            return selected;
        }

        @Override
        public boolean onlyOneChoice() {
            return compartments.size() == 1;
        }
    }

    class VaultStep implements Step<CompartmentItem, VaultItem> {

        private Map<String, VaultItem> vaults = null;
        private VaultItem selected;

        public Step<CompartmentItem, VaultItem> prepare(CompartmentItem compartment) {
            ProgressHandle h = ProgressHandle.createHandle(Bundle.MSG_CollectingItems());
            h.start();
            h.progress(Bundle.MSG_CollectingItems_Text());
            try {
                vaults = getVaults(compartment);
            } finally {
                h.finish();
            }
            return this;
        }

        @Override
        public NotifyDescriptor createInput() {
            return createQuickPick(vaults, Bundle.SelectVault());
        }

        @Override
        public Step getNext() {
            return new KeyStep().prepare(getValue());
        }

        @Override
        public void setValue(String selected) {
            this.selected = vaults.get(selected);
        }

        @Override
        public VaultItem getValue() {
            if (onlyOneChoice()) {
                selected = vaults.values().iterator().next();
            }
            return selected;
        }

        @Override
        public boolean onlyOneChoice() {
            return vaults.size() == 1;
        }
    }

    class KeyStep implements Step<VaultItem, Pair<VaultItem, KeyItem>> {

        private Map<String, KeyItem> keys = null;
        private KeyItem selected;
        private VaultItem vault;

        public Step<VaultItem, Pair<VaultItem, KeyItem>> prepare(VaultItem vault) {
            this.vault = vault;
            ProgressHandle h = ProgressHandle.createHandle(Bundle.MSG_CollectingItems());
            h.start();
            h.progress(Bundle.MSG_CollectingItems_Text());
            try {
                keys = getKeys(vault);
            } finally {
                h.finish();
            }
            return this;
        }

        @Override
        public boolean onlyOneChoice() {
            return keys.size() == 1;
        }

        @Override
        public NotifyDescriptor createInput() {
            if (keys.size() > 1) {
                return createQuickPick(keys, Bundle.SelectKey());
            }
            if (keys.size() == 0) {
                return new NotifyDescriptor.QuickPick("", Bundle.NoKeys(), Collections.emptyList(), false);
            }

            throw new IllegalStateException("No data to create input"); // NOI18N
        }

        @Override
        public Step getNext() {
            return new DatasourceNameStep().prepare(getValue());
        }

        @Override
        public void setValue(String selected) {
            this.selected = keys.get(selected);
        }

        @Override
        public Pair<VaultItem, KeyItem> getValue() {
            if (keys.size() == 1) {
                return Pair.of(vault, keys.values().iterator().next());
            }
            return Pair.of(vault, selected);
        }

    }

    class DatasourceNameStep implements Step<Pair<VaultItem, KeyItem>, Result> {

        private Result result = new Result();

        @Override
        public Step<Pair<VaultItem, KeyItem>, Result> prepare(Pair<VaultItem, KeyItem> item) {
            result.vault = item.first();
            result.key = item.second();
            return this;
        }

        @Override
        public NotifyDescriptor createInput() {
            return new NotifyDescriptor.InputLine("DEFAULT", Bundle.DatasourceName()); //NOI18N
        }

        @Override
        public Step getNext() {
            return new OverwriteStep().prepare(result);
        }

        @Override
        public void setValue(String selected) {
            result.datasourceName = selected;
        }

        @Override
        public Result getValue() {
            return result;
        }

        @Override
        public boolean onlyOneChoice() {
            return false;
        }

    }

    class OverwriteStep implements Step<Result, Result> {

        private Result result;
        private Set<String> dsNames;
        private String choice;

        @Override
        public Step<Result, Result> prepare(Result result) {
            this.result = result;
            if (result.datasourceName == null || result.datasourceName.isEmpty()) {
                return this;
            }
            List<SecretItem> secrets = SecretNode.getSecrets().apply(result.vault);
            this.dsNames = secrets.stream()
                    .map(s -> extractDatasourceName(s.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return this;
        }

        @Override
        public NotifyDescriptor createInput() {
            if (result.datasourceName == null || result.datasourceName.isEmpty()) {
                return new NotifyDescriptor.QuickPick("", Bundle.DatasourceEmpty(), Collections.emptyList(), false);
            }
            List<Item> yesNo = new ArrayList();
            yesNo.add(new Item(Bundle.AddVersion(), ""));
            yesNo.add(new Item(Bundle.Cancel(), ""));
            return new NotifyDescriptor.QuickPick("", Bundle.SecretExists(result.datasourceName), yesNo, false);
        }

        @Override
        public Step getNext() {
            return new PasswordStep().prepare(result);
        }

        @Override
        public void setValue(String choice) {
            this.choice = choice;
        }

        @Override
        public Result getValue() {
            if (Bundle.AddVersion().equals(choice) || onlyOneChoice()) {
                result.update = true;
                return result;
            }
            return null;
        }

        @Override
        public boolean onlyOneChoice() {
            return dsNames != null && !dsNames.contains(result.datasourceName);
        }

    }

    class PasswordStep implements Step<Result, Result> {

        private Result item;
        private boolean ask;

        @Override
        public Step<Result, Result> prepare(Result item) {
            item.password = context.getPassword();
            ask = item.password == null || item.password.isEmpty();
            this.item = item;
            return this;
        }

        @Override
        public NotifyDescriptor createInput() {
            return new NotifyDescriptor.PasswordLine("DEFAULT", Bundle.Password(context.getUser())); //NOI18N
        }

        @Override
        public boolean onlyOneChoice() {
            return !ask;
        }

        @Override
        public Step getNext() {
            return new DevopsStep().prepare(item);
        }

        @Override
        public void setValue(String password) {
            item.password = password;
        }

        @Override
        public Result getValue() {
            return item;
        }
    }

    class DevopsStep implements Step<Result, Result> {

        private Result item;
        private Map<String, DevopsProjectItem> devopsProjects;

        @Override
        public Step<Result, Result> prepare(Result item) {
            this.item = item;
            ProgressHandle h = ProgressHandle.createHandle(Bundle.MSG_CollectingItems());
            h.start();
            h.progress(Bundle.MSG_CollectingItems_Text());
            try {
                List<String> devops = DevopsProjectService.getDevopsProjectOcid();
                
                Map<String, DevopsProjectItem> allProjectsInCompartment = getDevopsProjects(item.vault.getCompartmentId());
                Map<String, DevopsProjectItem> filtered = allProjectsInCompartment.entrySet()
                        .stream()
                        .filter(e -> devops.contains(e.getValue().getKey().getValue()))
                        .collect(Collectors
                                .toMap(Entry::getKey, Entry::getValue));
                if (filtered.size() > 0) {
                    devopsProjects = filtered;
                } else {
                    devopsProjects = allProjectsInCompartment;
                }
                if (devopsProjects.size() == 1) {
                    item.project = devopsProjects.values().iterator().next();
                }
                
            } finally {
                h.finish();
            }
            return this;
        }

        @Override
        public NotifyDescriptor createInput() {
            if (devopsProjects.size() > 1) {
                return createQuickPick(devopsProjects, Bundle.SelectDevopsProject());
            }
            if (devopsProjects.isEmpty()) {
                return new NotifyDescriptor.QuickPick("", Bundle.NoDevopsProjects(), Collections.emptyList(), false);
            }
            throw new IllegalStateException("No data to create input"); // NOI18N
        }

        @Override
        public boolean onlyOneChoice() {
            return devopsProjects.size() == 1;
        }

        @Override
        public Step getNext() {
            return null;
        }

        @Override
        public void setValue(String projectName) {
            item.project = devopsProjects.get(projectName);
        }

        @Override
        public Result getValue() {
            return item;
        }
    }

    static class Result {
        VaultItem vault;
        KeyItem key;
        String datasourceName;
        String password;
        DevopsProjectItem project;
        private boolean update;
    }

    static class Multistep {

        private final LinkedList<Step> steps = new LinkedList<>();

        Multistep(Step firstStep) {
            steps.add(firstStep);
        }

        NotifyDescriptor.ComposedInput.Callback createInput() {
            return new NotifyDescriptor.ComposedInput.Callback() {
                private int lastNumber = 0;

                private void readValue(Step step, NotifyDescriptor desc) {
                    String selected = null;
                    if (!step.onlyOneChoice()) {
                        if (desc instanceof NotifyDescriptor.QuickPick) {
                            for (NotifyDescriptor.QuickPick.Item item : ((NotifyDescriptor.QuickPick) desc).getItems()) {
                                if (item.isSelected()) {
                                    selected = item.getLabel();
                                    break;
                                }
                            }
                        } else if (desc instanceof NotifyDescriptor.InputLine) {
                            selected = ((NotifyDescriptor.InputLine) desc).getInputText();
                        }
                        step.setValue(selected);
                    }
                }

                @Override
                public NotifyDescriptor createInput(NotifyDescriptor.ComposedInput input, int number) {
                    if (number == 1) {
                        while (steps.size() > 1) {
                            steps.removeLast();
                        }
                        steps.getLast().prepare(null);
                    } else if (lastNumber > number) {
                        steps.removeLast();
                        while(steps.getLast().onlyOneChoice() && steps.size() > 1) {
                            steps.removeLast();
                        }
                        lastNumber = number;
                        return steps.getLast().createInput();
                    } else {
                        readValue(steps.getLast(), input.getInputs()[number - 2]);
                        steps.add(steps.getLast().getNext());
                    }
                    lastNumber = number;
                    
                    while(steps.getLast() != null && steps.getLast().onlyOneChoice()) {
                        steps.add(steps.getLast().getNext());
                    }
                    if (steps.getLast() == null) {
                        steps.removeLast();
                        return null;
                    }
                    return steps.getLast().createInput();
                }
            };
        }

        Object getResult() {
            return steps.getLast().getValue();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Multistep multistep = new Multistep(new TenancyStep());

        NotifyDescriptor.ComposedInput ci = new NotifyDescriptor.ComposedInput(Bundle.AddADBToVault(), 3, multistep.createInput());
        if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(ci)) {
            if (multistep.getResult() != null) {
                Result result = (Result) multistep.getResult();
                if (result.datasourceName == null || result.datasourceName.isEmpty()) {
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(Bundle.DatasourceEmpty());
                    DialogDisplayer.getDefault().notify(msg);
                    return;
                }
                addDbConnectionToVault(result);
            }
        }

    }

    private void addDbConnectionToVault(Result item) {
        ProgressHandle h = ProgressHandle.createHandle(Bundle.UpdatingVault(item.vault.getName()));
        h.start();
        h.progress(Bundle.ReadingSecrets());
           
        try {
            VaultsClient client = VaultsClient.builder().build(getDefault().getActiveProfile().getConfigProvider());

            ListSecretsRequest listSecretsRequest = ListSecretsRequest.builder()
                    .compartmentId(item.vault.getCompartmentId())
                    .vaultId(item.vault.getKey().getValue())
                    .limit(88)
                    .build();

            ListSecretsResponse secrets = client.listSecrets(listSecretsRequest);

        Map<String, String> existingSecrets = secrets.getItems().stream()
                .collect(Collectors.toMap(s -> s.getSecretName(), s -> s.getId()));

            Map<String, String> values = new HashMap<String, String>() {
                {
                    put("Username", context.getUser()); //NOI18N
                    put("Password", item.password); //NOI18N
                    put("OCID", (String) context.getConnectionProperties().get("OCID")); //NOI18N
                    put("wallet_Password", UUID.randomUUID().toString()); //NOI18N
                }
            };

            for (Entry<String, String> entry : values.entrySet()) {
                String secretName = "DATASOURCES_" + item.datasourceName + "_" + entry.getKey().toUpperCase(); //NOI18N
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
                            .compartmentId(item.vault.getCompartmentId())
                            .vaultId(item.vault.getKey().getValue())
                            .keyId(item.key.getKey().getValue())
                            .build();
                    CreateSecretRequest request = CreateSecretRequest
                            .builder()
                            .createSecretDetails(createDetails)
                            .build();
                    client.createSecret(request);
                }
            }

            // Add Vault to the ConfigMap artifact
            DevopsClient devopsClient = DevopsClient.builder().build(OCIManager.getDefault().getActiveProfile().getConfigProvider());
            ListDeployArtifactsRequest request = ListDeployArtifactsRequest.builder()
                    .projectId(item.project.getKey().getValue()).build();
            ListDeployArtifactsResponse response = devopsClient.listDeployArtifacts(request);
            List<DeployArtifactSummary> artifacts = response.getDeployArtifactCollection().getItems();
            boolean found = false;
            for (DeployArtifactSummary artifact : artifacts) {
                if ((item.project.getName() + "_oke_configmap").equals(artifact.getDisplayName())) { //NOI18N
                    h.progress("updating  " + item.project.getName() + "_oke_configmap"); //NOI18N
                    found = true;
                    GetDeployArtifactRequest artRequest = GetDeployArtifactRequest.builder().deployArtifactId(artifact.getId()).build();
                    GetDeployArtifactResponse artResponse = devopsClient.getDeployArtifact(artRequest);
                    DeployArtifactSource source = artResponse.getDeployArtifact().getDeployArtifactSource();
                    if (source instanceof InlineDeployArtifactSource) {
                        byte[] content = ((InlineDeployArtifactSource) source).getBase64EncodedContent();
                        String srcString = updateProperties(new String(content, StandardCharsets.UTF_8),
                                item.vault.getCompartmentId(), item.vault.getKey().getValue(), item.datasourceName);
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
            if (!found) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(Bundle.NoConfigMap(item.project.getName()), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(Bundle.SecretsCreated());
            DialogDisplayer.getDefault().notify(msg);
        } catch(ThreadDeath e) {
            throw e;
        } catch (Throwable e) {
            h.finish();
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
        } finally {
            h.finish();
        }
    }

    protected static String updateProperties(String configmap, String compartmentOcid, String vaultOcid, String datasourceName) {
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
                    if (properties.size() == 0) {
                        propIndent = indent + 2;
                    }
                    output.append(
                            formatProperties(propertiesName, properties, propIndent, compartmentOcid, vaultOcid, datasourceName));

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
        output.append(
                formatProperties(propertiesName, properties, previousIndent, compartmentOcid, vaultOcid, datasourceName));

        return output.toString();
    }

    private static String formatProperties(String proprtiesName, Map<String, String> prop, int indent, String compartmentId, String vaultId, String datasourceName) {
        StringBuilder output = new StringBuilder();
        if (proprtiesName.startsWith("bootstrap")) { // NOI18N
            prop.entrySet().removeIf(entry -> ((String) entry.getKey()).startsWith("oci.vault.vaults")); // NOI18N
            prop.put("oci.config.instance-principal.enabled", "true"); // NOI18N
            prop.put("micronaut.config-client.enabled", "true"); // NOI18N
            prop.put("oci.vault.config.enabled", "true"); // NOI18N
            prop.put("oci.vault.vaults[0].ocid", vaultId); // NOI18N
            prop.put("oci.vault.vaults[0].compartment-ocid", compartmentId); // NOI18N
        } else if (proprtiesName.startsWith("application")) { // NOI18N
            prop.put("datasources.default.dialect", "ORACLE"); // NOI18N
            prop.put("datasources.default.ocid", "${DATASOURCES_" + datasourceName + "_OCID}"); // NOI18N
            prop.put("datasources.default.walletPassword", "${DATASOURCES_" + datasourceName + "_WALLET_PASSWORD}"); // NOI18N
            prop.put("datasources.default.username", "${DATASOURCES_" + datasourceName + "_USERNAME}"); // NOI18N
            prop.put("datasources.default.password", "${DATASOURCES_" + datasourceName + "_PASSWORD}"); // NOI18N
        }
        for (Entry<String, String> entry : prop.entrySet()) {
            output.append(new String(new char[indent]).replace('\0', ' '));
            output.append(entry.getKey());
            output.append("=");
            output.append(entry.getValue());
            output.append("\n");
        }
        return output.toString();
    }

    private static <T extends OCIItem> NotifyDescriptor.QuickPick createQuickPick(Map<String, T> ociItems, String title) {

        List<NotifyDescriptor.QuickPick.Item> items = ociItems.entrySet().stream()
                .map(entry -> new NotifyDescriptor.QuickPick.Item(entry.getKey(), entry.getValue().getDescription()))
                .collect(Collectors.toList());
        return new NotifyDescriptor.QuickPick(title, title, items, false);
    }

    private static Map<String, OCIItem> getFlatCompartment(TenancyItem tenancy) {
        Map<OCID, FlatCompartmentItem> compartments = new HashMap<>();
        OCISessionInitiator session = OCIManager.getDefault().getActiveSession();
        Identity identityClient = session.newClient(IdentityClient.class);
        String nextPageToken = null;

        do {
            ListCompartmentsResponse response
                    = identityClient.listCompartments(
                            ListCompartmentsRequest.builder()
                                    .compartmentId(tenancy.getKey().getValue())
                                    .compartmentIdInSubtree(true)
                                    .lifecycleState(Compartment.LifecycleState.Active)
                                    .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible)
                                    .limit(1000)
                                    .page(nextPageToken)
                                    .build());
            for (Compartment comp : response.getItems()) {
                FlatCompartmentItem ci = new FlatCompartmentItem(comp) {
                    FlatCompartmentItem getItem(OCID compId) {
                        return compartments.get(compId);
                    }
                };
                compartments.put(ci.getKey(), ci);
            }
            nextPageToken = response.getOpcNextPage();
        } while (nextPageToken != null);
        Map<String, OCIItem> pickItems = computeFlatNames(compartments);
        pickItems.put(tenancy.getName() + " (root)", tenancy); // NOI18N
        return pickItems;
    }

    private static Map<String, OCIItem> computeFlatNames(Map<OCID, FlatCompartmentItem> compartments) {
        Map<String, OCIItem> pickItems = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (FlatCompartmentItem comp : compartments.values()) {
            pickItems.put(comp.getName(), comp);
        }
        return pickItems;
    }

    private static abstract class FlatCompartmentItem extends CompartmentItem {

        private final OCID parentId;
        private String flatName;

        private FlatCompartmentItem(Compartment ociComp) {
            super(OCID.of(ociComp.getId(), "Compartment"), ociComp.getName()); // NOI18N
            setDescription(ociComp.getDescription());
            parentId = OCID.of(ociComp.getCompartmentId(), "Compartment"); // NOI18N
        }

        public String getName() {
            if (parentId.getValue() == null) {
                return "";
            }
            if (flatName == null) {
                String parentFlatName = "";
                FlatCompartmentItem parentComp = getItem(parentId);
                if (parentComp != null) {
                    parentFlatName = parentComp.getName();
                }
                flatName = super.getName();
                if (!parentFlatName.isEmpty()) {
                    flatName = parentFlatName + "/" + flatName; // NOI18N
                }
            }
            return flatName;
        }

        abstract FlatCompartmentItem getItem(OCID compId);
    }

    protected static Map<String, DevopsProjectItem> getDevopsProjects(String compartmentId) {
        try (DevopsClient client = new DevopsClient(OCIManager.getDefault().getConfigProvider());) {
            ListProjectsRequest request = ListProjectsRequest.builder().compartmentId(compartmentId).build();
            ListProjectsResponse response = client.listProjects(request);

            List<ProjectSummary> projects = response.getProjectCollection().getItems();
            for (ProjectSummary project : projects) {
                project.getNotificationConfig().getTopicId();

            }
            return projects.stream()
                    .map(p -> new DevopsProjectItem(OCID.of(p.getId(), "DevopsProject"), // NOI18N
                    p.getName()))
                    .collect(Collectors.toMap(DevopsProjectItem::getName, Function.identity()));
        }
    }

    protected static Map<String, VaultItem> getVaults(OCIItem parent) {
        Map<String, VaultItem> items = new HashMap<>();
        try {
            if (parent instanceof CompartmentItem) {
                VaultNode.getVaults().apply((CompartmentItem) parent).forEach((db) -> items.put(db.getName(), db));
            }
        } catch (BmcException e) {
            LOG.log(Level.SEVERE, "Unable to load vault list", e); //NOI18N
        }
        return items;
    }

    protected static Map<String, KeyItem> getKeys(OCIItem parent) {
        Map<String, KeyItem> items = new HashMap<>();
        try {
            if (parent instanceof VaultItem) {
                KeyNode.getKeys().apply((VaultItem) parent).forEach(key -> items.put(key.getName(), key));
            }
        } catch (BmcException e) {
            LOG.log(Level.SEVERE, "Unable to load key list", e); //NOI18N
        }
        return items;
    }

    static Pattern p = Pattern.compile("[A-Z]*_([a-zA-Z0-9]*)_[A-Z]*"); //NOI18N

    protected static String extractDatasourceName(String value) {
        Matcher m = p.matcher(value);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }
}
