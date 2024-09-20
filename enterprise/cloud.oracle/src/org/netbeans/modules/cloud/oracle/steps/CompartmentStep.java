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
package org.netbeans.modules.cloud.oracle.steps;

import com.oracle.bmc.identity.Identity;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCISessionInitiator;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps.Values;
import static org.netbeans.modules.cloud.oracle.assets.Steps.createQuickPick;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.items.IncompatibleTenancyItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.items.TenancyItem;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "NoTenancy=Tenancy is not selected",
    "IncompatibleTenancy=Tenancy not compatible with other selected Cloud Assets. Please return and select other one.",
    "NoCompartment=There are no compartments in the Tenancy",
    "CollectingItems_Text=Listing compartments and databases",
    "SelectCompartment=Select Compartment",
    "# {0} - Name of the compartment",
    "Root={0} (root)"
})
public final class CompartmentStep extends AbstractStep<CompartmentItem> {

    private Map<String, OCIItem> compartments = null;
    private TenancyItem tenancy;
    private CompartmentItem selected;

    @Override
    public void prepare(ProgressHandle h, Values values) {
        h.progress(Bundle.CollectingItems_Text());
        tenancy = values.getValueForStep(TenancyStep.class);
        if (tenancy != null && !(tenancy instanceof IncompatibleTenancyItem)) {
            compartments = getFlatCompartment(tenancy);

        } else {
            compartments = Collections.emptyMap();
        }
    }

    @Override
    public NotifyDescriptor createInput() {
        if (tenancy == null) {
            return new NotifyDescriptor.QuickPick("", Bundle.NoTenancy(), Collections.emptyList(), false);
        } 
        if (tenancy instanceof IncompatibleTenancyItem) {
            return new NotifyDescriptor.QuickPick("", Bundle.IncompatibleTenancy(), Collections.emptyList(), false);
        }
        if (onlyOneChoice()) {
            throw new IllegalStateException("Input shouldn't be displayed for one choice"); // NOI18N
        }
        if (compartments.isEmpty()) {
            return createQuickPick(compartments, Bundle.NoCompartment());
        }
        return createQuickPick(compartments, Bundle.SelectCompartment());
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
        String tenancyId = session.getTenancy().isPresent() ? session.getTenancy().get().getKey().getValue() : null;
        String regionCode = session.getRegion().getRegionCode();

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
                FlatCompartmentItem ci = new FlatCompartmentItem(comp, tenancyId, regionCode) {
                    @Override
                    FlatCompartmentItem getItem(OCID compId) {
                        return compartments.get(compId);
                    }
                };
                compartments.put(ci.getKey(), ci);
            }
            nextPageToken = response.getOpcNextPage();
        } while (nextPageToken != null);
        Map<String, OCIItem> pickItems = computeFlatNames(compartments);
        pickItems.put(Bundle.Root(tenancy.getName()), tenancy);
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
     * This class represents compartments in a flat structure. Individual levels
     * are separated by slashes.
     *
     */
    static private abstract class FlatCompartmentItem extends CompartmentItem {

        private final OCID parentId;
        private String flatName;

        private FlatCompartmentItem(Compartment ociComp, String tenancyId, String regionCode) {
            super(OCID.of(ociComp.getId(), "Compartment"), ociComp.getCompartmentId(), ociComp.getName(), tenancyId, regionCode);      // NOI18N
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

}
