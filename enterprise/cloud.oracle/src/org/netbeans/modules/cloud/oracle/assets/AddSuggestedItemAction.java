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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.cloud.oracle.actions.AddADBAction;
import org.netbeans.modules.cloud.oracle.assets.Steps.DatabaseConnectionStep;
import org.netbeans.modules.cloud.oracle.assets.Steps.TenancyStep;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
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
    "SelectProfile=Select OCI Profile",
    "# {0} - tenancy name",
    "# {1} - region id",
    "SelectProfile_Description={0} (region: {1})",
    "SelectCompartment=Select Compartment",
    "NoProfile=There is not any OCI profile in the config",
    "NoCompartment=There are no compartments in the Tenancy",
    "CollectingProfiles=Searching for OCI Profiles",
    "CollectingProfiles_Text=Loading OCI Profiles",
    "CollectingItems=Loading OCI contents",
    "CollectingItems_Text=Listing compartments and databases",
    "SelectItem=Select {0}",
    "SelectDBConnection=Select Database Connection"
})
public class AddSuggestedItemAction implements ActionListener {

    private static final Logger LOG = Logger.getLogger(AddADBAction.class.getName());

    private final SuggestedItem context;

    public AddSuggestedItemAction(SuggestedItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Databases".equals(context.getPath())) { //NOI18N
            Map<String, DatabaseItem> adbConnections = new HashMap<>();
            DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
            for (int i = 0; i < connections.length; i++) {
                String name = connections[i].getDisplayName();
                String ocid = connections[i].getConnectionProperties().getProperty("OCID"); //NOI18N
                String compartmentId = connections[i].getConnectionProperties().getProperty("CompartmentOCID"); //NOI18N
                if (ocid != null && compartmentId != null) {
                    DatabaseItem dbItem = 
                            new DatabaseItem(OCID.of(ocid, "Databases"), compartmentId, name, null, name); //NOI18N
                    adbConnections.put(name, dbItem);
                }
            }
            if (adbConnections.isEmpty()) {
                DatabaseItem db = new AddADBAction().addADB();
                if (db != null) {
                    CloudAssets.getDefault().addItem(db);
                }
            } else {
                Steps.getDefault().executeMultistep(new DatabaseConnectionStep(adbConnections), Lookup.EMPTY)
                        .thenAccept(result -> CloudAssets.getDefault().addItem((OCIItem) result));
            }
            return;
        }
        Lookup lookup = Lookups.fixed(new SingleSuggestedContext(context.getPath()));
        Steps.getDefault().executeMultistep(new TenancyStep(), lookup)
                .thenAccept(result -> CloudAssets.getDefault().addItem((OCIItem) result));
    }
    
    private final class SingleSuggestedContext implements Steps.SuggestedContext {

        private String itemType = null;

        public SingleSuggestedContext(String itemType) {
            this.itemType = itemType;
        }

        @Override
        public String getItemType() {
            return itemType;
        }

        @Override
        public Step getNextStep() {
            return null;
        }

        @Override
        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

    }

}
