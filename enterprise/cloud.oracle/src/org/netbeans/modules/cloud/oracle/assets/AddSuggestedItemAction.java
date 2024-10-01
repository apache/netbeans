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

import org.netbeans.modules.cloud.oracle.steps.SuggestedStep;
import org.netbeans.modules.cloud.oracle.steps.CompartmentStep;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.netbeans.modules.cloud.oracle.actions.AddADBAction;
import org.netbeans.modules.cloud.oracle.actions.OCIItemCreator;
import org.netbeans.modules.cloud.oracle.steps.DatabaseConnectionStep;
import org.netbeans.modules.cloud.oracle.steps.TenancyStep;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.AddSuggestedItemAction"
)
@ActionRegistration(
        displayName = "#AddSuggestedItem",
        asynchronous = true
)

@NbBundle.Messages({
    "AddSuggestedItem=Add Suggested Oracle Cloud Resource",
    "# {0} - tenancy name",
    "# {1} - region id",
    "NoCompartment=There are no compartments in the Tenancy",
    "CollectingProfiles=Searching for OCI Profiles",
    "CollectingItems=Loading OCI contents",})
public class AddSuggestedItemAction implements ActionListener {
    private static final Logger LOG = Logger.getLogger(AddADBAction.class.getName());

    private final SuggestedItem context;

    public AddSuggestedItemAction(SuggestedItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Databases".equals(context.getPath())) { //NOI18N
            Steps.getDefault().executeMultistep(new DatabaseConnectionStep(), Lookup.EMPTY)
                    .thenAccept(values -> {
                        DatabaseItem db = values.getValueForStep(DatabaseConnectionStep.class);
                        if (db != null) {
                            CloudAssets.getDefault().addItem(db);
                        } else {
                            new AddADBAction().addADB().thenAccept(i -> {
                                CloudAssets.getDefault().addItem(i);
                            });
                        }
                    });
            return;
        }
        Steps.NextStepProvider nsProvider = Steps.NextStepProvider.builder()
                .stepForClass(TenancyStep.class, (s) -> new CompartmentStep())
                .stepForClass(CompartmentStep.class, (s) -> new SuggestedStep(context.getPath()))
                .build();
        Lookup lookup = Lookups.fixed(nsProvider);
        Steps.getDefault().executeMultistep(new TenancyStep(), lookup)
                .thenAccept(values -> {
                    if (values.getValueForStep(SuggestedStep.class) instanceof CreateNewResourceItem) {
                        OCIItemCreator creator = OCIItemCreator.getCreator(context.getPath());
                        if (creator != null) {
                            CompletableFuture<Map<String, Object>> vals = creator.steps();
                            vals.thenCompose(params -> {
                                return creator.create(values, params);
                            }).thenAccept(i -> {
                                CloudAssets.getDefault().addItem(i);
                            });
                        }
                    } else {
                        OCIItem item = values.getValueForStep(SuggestedStep.class);
                        CloudAssets.getDefault().addItem(item);
                    }
                });
    }
}
