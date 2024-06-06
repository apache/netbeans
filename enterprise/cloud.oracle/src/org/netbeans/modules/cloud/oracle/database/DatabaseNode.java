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
package org.netbeans.modules.cloud.oracle.database;

import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.DatabaseConnectionStringProfile;
import com.oracle.bmc.database.requests.ListAutonomousDatabasesRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import static org.netbeans.modules.cloud.oracle.OCIManager.getDefault;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "LBL_WorkloadType=Workload Type: {0}\n",
    "LBL_DatabaseVersion=Database version: {0}\n",
    "LBL_Storage=Storage: {0}TB",
    "LBL_ADB=Oracle Autonomous Database: {0}"
})
public class DatabaseNode extends OCINode {

    private static final String DB_ICON = "org/netbeans/modules/cloud/oracle/resources/database.svg"; // NOI18N
    private static final String SERVICE_CONSOLE_SUFFIX = "admin/_sdw/"; // NOI18N

    public DatabaseNode(DatabaseItem dbSummary) {
        super(dbSummary, Children.LEAF);
        setName(dbSummary.getName());
        setDisplayName(dbSummary.getName());
        setIconBaseWithExtension(DB_ICON);
        setShortDescription(Bundle.LBL_ADB(dbSummary.getDescription()));
    }

    public static NodeProvider<DatabaseItem> createNode() {
        return DatabaseNode::new;
    }

    /**
     * Retrieves list of Databases belonging to a given Compartment.
     *
     * @param compartmentId OCID of the Compartment
     * @return List of {@code OCIItem} describing databases in a given
     * Compartment
     */
    public static ChildrenProvider<CompartmentItem, DatabaseItem> getDatabases() {
        return compartmentId -> {
            DatabaseClient client = new DatabaseClient(getDefault().getConfigProvider());
            ListAutonomousDatabasesRequest listAutonomousDatabasesRequest = ListAutonomousDatabasesRequest.builder()
                    .compartmentId(compartmentId.getKey().getValue())
                    .limit(88)
                    .build();
            
            
            return client.listAutonomousDatabases(listAutonomousDatabasesRequest)
                    .getItems()
                    .stream()
                    .map(d -> {
                        List<DatabaseConnectionStringProfile> profiles = d.getConnectionStrings().getProfiles();
                        DatabaseItem item = new DatabaseItem(
                                OCID.of(d.getId(), "Databases"), //NOI18N
                                compartmentId.getKey().getValue(),
                                d.getDbName(),
                                d.getConnectionUrls().getOrdsUrl()+SERVICE_CONSOLE_SUFFIX,
                                getConnectionName(profiles));
                        StringBuilder sb = new StringBuilder();
                        sb.append(Bundle.LBL_WorkloadType(d.getDbWorkload().getValue()));
                        sb.append(Bundle.LBL_DatabaseVersion(d.getDbVersion()));
                        sb.append(Bundle.LBL_Storage(d.getDataStorageSizeInTBs()));
                        item.setDescription(sb.toString());
                        return item;
                    })
                    .collect(Collectors.toList());
        };
    }

    private static String getConnectionName(List<DatabaseConnectionStringProfile> profiles) {
        
        if (profiles != null && !profiles.isEmpty()) {
            for (DatabaseConnectionStringProfile profile : profiles) {
                if (profile.getDisplayName().contains("high")) { //NOI18N
                    return profile.getDisplayName();
                }
            }
            return profiles.get(0).getDisplayName();
        }
        return null;
    }

}
