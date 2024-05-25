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
package org.netbeans.modules.cloud.oracle.assets;

import com.oracle.bmc.identity.Identity;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.Tenancy;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import com.oracle.bmc.model.BmcException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.OCISessionInitiator;
import org.netbeans.modules.cloud.oracle.bucket.BucketNode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.compute.ClusterNode;
import org.netbeans.modules.cloud.oracle.compute.ComputeInstanceNode;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.database.DatabaseNode;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.items.TenancyItem;
import org.netbeans.modules.cloud.oracle.vault.VaultNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "Databases=Oracle Autonomous Database",
    "Vault=OCI Vault",
    "Bucket=Object Storage Bucket",
    "Cluster=Oracle Container Engine",
    "Compute=Compute Instance"
})
public final class Steps {
    private static final Logger LOG = Logger.getLogger(Steps.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("Steps"); //NOI18N
    private static Steps instance = null;

    public static synchronized Steps getDefault() {
        if (instance == null) {
            instance = new Steps();
        }
        return instance;
    }

    public CompletableFuture<Object> executeMultistep(Step firstStep, Lookup lookup) {
        DialogDisplayer dd = DialogDisplayer.getDefault();
        CompletableFuture future = new CompletableFuture();
        RP.post(() -> {
            Multistep multistep = new Multistep(firstStep, lookup);
            NotifyDescriptor.ComposedInput ci = new NotifyDescriptor.ComposedInput(Bundle.AddSuggestedItem(), 3, multistep.createInput());
            if (DialogDescriptor.OK_OPTION == dd.notify(ci)) {
                future.complete(multistep.getResult());
            } else {
                future.complete(null);
            }
        });
        return future;
    }

    private static class Multistep {

        private final LinkedList<Step> steps = new LinkedList<>();
        private final Lookup lookup;

        Multistep(Step firstStep, Lookup lookup) {
            steps.add(firstStep);
            this.lookup = lookup;
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
                        steps.getLast().prepare(null, lookup);
                    } else if (lastNumber > number) {
                        steps.removeLast();
                        while (steps.getLast().onlyOneChoice() && steps.size() > 1) {
                            steps.removeLast();
                        }
                        lastNumber = number;
                        return steps.getLast().createInput();
                    } else {
                        readValue(steps.getLast(), input.getInputs()[number - 2]);
                        steps.add(steps.getLast().getNext());
                    }
                    lastNumber = number;

                    while (steps.getLast() != null && steps.getLast().onlyOneChoice()) {
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

        public Object getResult() {
            return steps.getLast().getValue();
        }
    }

    static final class TenancyStep implements Step<Object, TenancyItem> {

        List<OCIProfile> profiles = new LinkedList<>();
        private AtomicReference<TenancyItem> selected = new AtomicReference<>();
        private Lookup lookup;

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
            return new CompartmentStep().prepare(getValue(), lookup);
        }

        @Override
        public Step<Object, TenancyItem> prepare(Object i, Lookup lookup) {
            this.lookup = lookup;
            ProgressHandle h = ProgressHandle.createHandle(Bundle.CollectingProfiles());
            h.start();
            h.progress(Bundle.CollectingProfiles_Text());
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

    static final class CompartmentStep implements Step<TenancyItem, CompartmentItem> {

        private Map<String, OCIItem> compartments = null;
        private CompartmentItem selected;
        private Lookup lookup;

        public Step<TenancyItem, CompartmentItem> prepare(TenancyItem tenancy, Lookup lookup) {
            this.lookup = lookup;
            ProgressHandle h = ProgressHandle.createHandle(Bundle.CollectingItems());
            h.start();
            h.progress(Bundle.CollectingItems_Text());
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
                return createQuickPick(compartments, Bundle.NoCompartment());
            }
            return createQuickPick(compartments, Bundle.SelectCompartment());
        }

        @Override
        public Step getNext() {
            return new SuggestedStep().prepare(getValue(), lookup);
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

    /**
     * Context of SuggestedStep. Determines next step.
     */
    public interface SuggestedContext {

        String getItemType();

        Step getNextStep();

        public void setItemType(String selected);
    }

    /**
     * Show list of items for a suggested type.
     * 
     */
    static class SuggestedStep implements Step<CompartmentItem, OCIItem> {

        private Map<String, OCIItem> items = new HashMap<>();
        private OCIItem selected;
        private Lookup lookup;
        private SuggestedContext context = null;

        public SuggestedStep prepare(CompartmentItem compartment, Lookup lookup) {
            this.lookup = lookup;
            context = lookup.lookup(SuggestedContext.class);
            ProgressHandle h = ProgressHandle.createHandle(Bundle.CollectingItems());
            h.start();
            h.progress(Bundle.CollectingItems_Text());
            try {
                getItemsByPath(compartment, context.getItemType()).forEach((db) -> items.put(db.getName(), db));
            } finally {
                h.finish();
            }
            return this;
        }

        private String getSuggestedItemName() {
            switch (context.getItemType()) {
                case "Databases":
                    return Bundle.Databases();
                case "Vault":
                    return Bundle.Vault();
                case "Bucket":
                    return Bundle.Bucket();
                case "Cluster":
                    return Bundle.Cluster();
                case "ComputeInstance":
                    return Bundle.Compute();
            }
            throw new MissingResourceException("Missing OCI type", null, context.getItemType());
        }

        @Override
        public NotifyDescriptor createInput() {
            return createQuickPick(items, Bundle.SelectItem(getSuggestedItemName()));
        }

        @Override
        public Step getNext() {
            Step next = context.getNextStep();
            if (next != null) {
                next.prepare(getValue(), lookup);
            }
            return next;
        }

        @Override
        public void setValue(String selected) {
            this.selected = items.get(selected);
        }

        @Override
        public OCIItem getValue() {
            if (onlyOneChoice()) {
                selected = items.values().iterator().next();
            }
            return selected;
        }

        @Override
        public boolean onlyOneChoice() {
            return false;
        }
    }

    private static <T extends OCIItem> NotifyDescriptor.QuickPick createQuickPick(Map<String, T> ociItems, String title) {
        List<NotifyDescriptor.QuickPick.Item> items = new ArrayList<> ();
        for (Map.Entry<String, T> entry : ociItems.entrySet()) {
            String description = entry.getValue().getDescription();
            if (description == null || description.isBlank()) {
                description = entry.getValue().getName();
            }
            items.add(new NotifyDescriptor.QuickPick.Item(entry.getKey(), description));
        }
        return new NotifyDescriptor.QuickPick(title, title, items, false);
    }
    
    

    /**
     * Retrieve all compartments from a tenancy.
     * 
     * @param tenancy
     * @return 
     */
    static private Map<String, OCIItem> getFlatCompartment(TenancyItem tenancy) {
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
        pickItems.put(tenancy.getName() + " (root)", tenancy);        // NOI18N
        return pickItems;
    }
    
    static private Map<String, OCIItem> computeFlatNames(Map<OCID, FlatCompartmentItem> compartments) {
        Map<String, OCIItem> pickItems = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (FlatCompartmentItem comp : compartments.values()) {
            pickItems.put(comp.getName(), comp);
        }
        return pickItems;
    }

    /**
     * This class represents compartments in a flat structure. Individual levels are separated by slashes.
     * 
     */
    static private abstract class FlatCompartmentItem extends CompartmentItem {

        private final OCID parentId;
        private String flatName;

        private FlatCompartmentItem(Compartment ociComp) {
            super(OCID.of(ociComp.getId(), "Compartment"), ociComp.getCompartmentId(), ociComp.getName());      // NOI18N
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
                    flatName = parentFlatName + "/" + flatName;  // NOI18N
                }
            }
            return flatName;
        }

        abstract FlatCompartmentItem getItem(OCID compId);
    }
    
    /**
     *  This step allows the user to select which type of resource will be added.
     */
    static class ItemTypeStep implements Step<Object, String> {
        String[] types = {"Databases", "Vault", "Bucket"}; //NOI18N

        private Lookup lookup;
        private SuggestedContext context;

        @Override
        public Step<Object, String> prepare(Object item, Lookup lookup) {
            context = lookup.lookup(SuggestedContext.class);
            this.lookup = lookup;
            return this;
        }

        @Override
        public NotifyDescriptor createInput() {
            List<NotifyDescriptor.QuickPick.Item> items = new ArrayList<>(types.length);
            for (String itemType : types) {
                items.add(new NotifyDescriptor.QuickPick.Item(itemType, itemType));
            }
            return new NotifyDescriptor.QuickPick(Bundle.SelectResourceType(), Bundle.SelectResourceType(), items, false);
        }

        @Override
        public boolean onlyOneChoice() {
            return false;
        }

        @Override
        public Step getNext() {
            return new TenancyStep().prepare(null, lookup);
        }

        @Override
        public void setValue(String selected) {
            context.setItemType(selected);
        }

        @Override
        public String getValue() {
            return context.getItemType();
        }

    }

    /**
     * The purpose of this step is to select a project to update dependencies.
     */
    static class ProjectStep implements Step<OCIItem, Object> {

        private final CompletableFuture<Project[]> projectsFuture;
        Map<String, Project> projects;
        private Project selectedProject;
        private OCIItem item;

        ProjectStep(CompletableFuture<Project[]> projectsFuture) {
            this.projectsFuture = projectsFuture;
            this.projects = new HashMap<> ();
        }

        @Override
        public Step<OCIItem, Object> prepare(OCIItem item, Lookup lookup) {
            this.item = item;
            try {
                Project[] p = projectsFuture.get();
                for (int i = 0; i < p.length; i++) {
                    ProjectInformation pi = ProjectUtils.getInformation(p[i]);
                    projects.put(pi.getDisplayName(), p[i]);
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
            return this;
        }

        @Override
        public NotifyDescriptor createInput() {
            List<NotifyDescriptor.QuickPick.Item> items = new ArrayList<>(projects.size());
            for (Map.Entry<String, Project> entry : projects.entrySet()) {
                
                items.add(new NotifyDescriptor.QuickPick.Item(
                        entry.getKey(),
                        entry.getValue().getProjectDirectory().getName()));
            }
            String title = Bundle.SelectProject();
            if (projects.size() == 0) {
                title = Bundle.NoProjects();
            }
            return new NotifyDescriptor.QuickPick(title, title, items, false);
        }

        @Override
        public boolean onlyOneChoice() {
            return projects.size() == 1;
        }

        @Override
        public Step getNext() {
            return null;
        }

        @Override
        public void setValue(String selected) {
            selectedProject = projects.get(selected);
        }

        @Override
        public Object getValue() {
            if (projects.size() == 1) {
                selectedProject = (Project) projects.values().toArray()[0];
            }
            return Pair.of(selectedProject, item);
        }

    }
    
    /**
     * Retrieve items of a given type from a specified compartment.
     * @param parent Compartment to search for items
     * @param path Type of the items
     * @return  List of items found
     */
    protected static List<? extends OCIItem> getItemsByPath(CompartmentItem parent, String path) {
        Map<String, OCIItem> items = new HashMap<>();
        try {
            switch (path) {
                case "Databases": //NOI18N
                    return DatabaseNode.getDatabases().apply(parent);
                case "Vault": //NOI18N
                    return VaultNode.getVaults().apply(parent);
                case "Bucket": //NOI18N
                    return BucketNode.getBuckets().apply(parent);
                case "Cluster": //NOI18N
                    return ClusterNode.getClusters().apply(parent);
                case "ComputeInstance": //NOI18N
                    return ComputeInstanceNode.getComputeInstances().apply(parent);
                default:
                    return Collections.emptyList();
            }
        } catch (BmcException e) {
            LOG.log(Level.SEVERE, "Unable to load vault list", e); //NOI18N
        }
        return Collections.emptyList();
    }
 
    /**
     * Step to select an existing database connection from the Database Explorer.
     * 
     */
    static final class DatabaseConnectionStep implements Step<Object, DatabaseItem> {

        private final Map<String, DatabaseItem> adbConnections;
        private DatabaseItem selected;

        public DatabaseConnectionStep(Map<String, DatabaseItem> adbConnections) {
            this.adbConnections = adbConnections;
        }

        @Override
        public Step<Object, DatabaseItem> prepare(Object empty, Lookup lookup) {
            return this;
        }

        @Override
        public NotifyDescriptor createInput() {
            return createQuickPick(adbConnections, Bundle.SelectDBConnection());
        }

        @Override
        public Step getNext() {
            return null;
        }

        @Override
        public void setValue(String selected) {
            this.selected = (DatabaseItem) adbConnections.get(selected);
        }

        @Override
        public DatabaseItem getValue() {
            return selected;
        }

        @Override
        public boolean onlyOneChoice() {
            return false;
        }
    }

}
