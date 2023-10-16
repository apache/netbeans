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
import com.oracle.bmc.vault.responses.CreateSecretResponse;
import com.oracle.bmc.vault.responses.ListSecretsResponse;
import com.oracle.bmc.vault.responses.UpdateSecretResponse;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
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
    "NoCompartment=There are no compartments in the Tenancy"
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

        public void setValue(String selected) {
            for (OCIProfile profile : profiles) {
                if (profile.getId().equals(selected)) {
                    profile.getTenancy().ifPresent(t -> this.selected.set(t));
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

    static class CompartmentStep implements Step<TenancyItem, CompartmentItem> {

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

    static class VaultStep implements Step<CompartmentItem, VaultItem> {

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
            return new KeyStep().prepare(selected);
        }

        @Override
        public void setValue(String selected) {
            this.selected = vaults.get(selected);
        }

        @Override
        public VaultItem getValue() {
            if (onlyOneChoice()) {
                vaults.values().iterator().next();
            }
            return selected;
        }

        @Override
        public boolean onlyOneChoice() {
            return vaults.size() == 1;
        }
    }

    static class KeyStep implements Step<VaultItem, Pair<VaultItem, KeyItem>> {

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

    static class DatasourceNameStep implements Step<Pair<VaultItem, KeyItem>, Result> {

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

    static class OverwriteStep implements Step<Result, Result> {

        private Result result;
        private Set<String> dsNames;
        private String choice;

        @Override
        public Step<Result, Result> prepare(Result result) {
            this.result = result;
            List<SecretItem> secrets = SecretNode.getSecrets().apply(result.vault);
            this.dsNames = secrets.stream()
                    .map(s -> extractDatasourceName(s.getName()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return this;
        }

        @Override
        public NotifyDescriptor createInput() {
            List<Item> yesNo = new ArrayList();
            yesNo.add(new Item(Bundle.AddVersion(), ""));
            yesNo.add(new Item(Bundle.Cancel(), ""));
            return new NotifyDescriptor.QuickPick("", Bundle.SecretExists(result.datasourceName), yesNo, false);
        }

        @Override
        public Step getNext() {
            return null;
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
            return !dsNames.contains(result.datasourceName);
        }

    }

    static class Result {
        VaultItem vault;
        KeyItem key;
        String datasourceName;
        private boolean update;
    }

    static class Multistep {

        private final LinkedList<Step> steps = new LinkedList<>();

        Multistep(Step firstStep) {
            steps.add(firstStep);
        }

        NotifyDescriptor.ComposedInput.Callback createInput() {
            return new NotifyDescriptor.ComposedInput.Callback() {

                private void showInput(Step step, NotifyDescriptor desc) {
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

                NotifyDescriptor prepareInput(NotifyDescriptor.ComposedInput input, int number) {
                    if (number == 1) {
                        steps.get(0).prepare(null);
                        return steps.get(0).createInput();
                    }
                    if (steps.size() > number) {
                        steps.removeLast();
                        return steps.getLast().createInput();
                    }
                    showInput(steps.getLast(), input.getInputs()[number - 2]);
                    Step currentStep = steps.getLast().getNext();
                    if (currentStep == null) {
                        return null;
                    }

                    steps.add(currentStep);
                    if (currentStep.onlyOneChoice()) {
                        return prepareInput(input, number);
                    }
                    return currentStep.createInput();
                }

                @Override
                public NotifyDescriptor createInput(NotifyDescriptor.ComposedInput input, int number) {
                    return prepareInput(input, number);
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

        NotifyDescriptor.ComposedInput ci = new NotifyDescriptor.ComposedInput(Bundle.AddADB(), 3, multistep.createInput());
        if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(ci)) {
            if (multistep.getResult() != null) {
                Result v = (Result) multistep.getResult();
                addDbConnectionToVault(v.vault, v.key, context, v.datasourceName);
            }
        }

    }

    private static void addDbConnectionToVault(VaultItem vault, KeyItem key, DatabaseConnection connection, String datasourceName) {
        VaultsClient client = VaultsClient.builder().build(getDefault().getActiveProfile().getConfigProvider());

        ListSecretsRequest listSecretsRequest = ListSecretsRequest.builder()
                .compartmentId(vault.getCompartmentId())
                .vaultId(vault.getKey().getValue())
                .limit(88)
                .build();

        ListSecretsResponse secrets = client.listSecrets(listSecretsRequest);

        Map<String, String> existingSecrets = secrets.getItems().stream()
                .collect(Collectors.toMap(s -> s.getSecretName(), s -> s.getId()));

        Map<String, String> values = new HashMap<String, String>() {
            {
                put("Username", connection.getUser()); //NOI18N
                put("Password", connection.getPassword()); //NOI18N
                put("OCID", (String) connection.getConnectionProperties().get("OCID")); //NOI18N
                put("wallet_Password", UUID.randomUUID().toString()); //NOI18N
            }
        };

        try {
            for (Entry<String, String> entry : values.entrySet()) {
                String secretName = "DATASOURCES_" + datasourceName + "_" + entry.getKey().toUpperCase(); //NOI18N
                String base64Content = Base64.getEncoder().encodeToString(entry.getValue().getBytes(StandardCharsets.UTF_8));

                SecretContentDetails contentDetails = Base64SecretContentDetails.builder()
                        .content(base64Content)
                        .stage(SecretContentDetails.Stage.Current).build();
                if (existingSecrets.containsKey(secretName)) {
                    UpdateSecretDetails updateSecretDetails = UpdateSecretDetails.builder()
                            .secretContent(contentDetails)
                            .build();
                    UpdateSecretRequest request = UpdateSecretRequest.builder()
                            .secretId(existingSecrets.get(secretName))
                            .updateSecretDetails(updateSecretDetails)
                            .build();
                    UpdateSecretResponse response = client.updateSecret(request);
                } else {
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
                    CreateSecretResponse response = client.createSecret(request);
                }
            }

        } catch (BmcException e) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(e.getMessage());
            DialogDisplayer.getDefault().notify(msg);
            throw new RuntimeException(e);
        }
        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(Bundle.SecretsCreated());
        DialogDisplayer.getDefault().notify(msg);
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
                KeyNode.getKeys().apply((VaultItem) parent).forEach((db) -> items.put(db.getName(), db));
            }
        } catch (BmcException e) {
            LOG.log(Level.SEVERE, "Unable to load vault list", e); //NOI18N
        }
        return items;
    }

    static Pattern p = Pattern.compile("[A-Z]*_([A-Z]*)_[A-Z]*"); //NOI18N

    protected static String extractDatasourceName(String value) {
        Matcher m = p.matcher(value);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }
}
