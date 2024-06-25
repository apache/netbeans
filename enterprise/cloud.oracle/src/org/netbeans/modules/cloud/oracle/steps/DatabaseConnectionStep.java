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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Step to select an existing database connection from the Database Explorer.
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "SelectDBConnection=Select Database Connection"
})
public final class DatabaseConnectionStep extends AbstractStep<DatabaseItem> {

    private final Map<String, DatabaseItem> adbConnections;
    private DatabaseItem selected = null;

    public DatabaseConnectionStep() {
        adbConnections = new HashMap<>();
        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
        for (int i = 0; i < connections.length; i++) {
            String name = connections[i].getDisplayName();
            String ocid = connections[i].getConnectionProperties().getProperty("OCID"); //NOI18N
            String compartmentId = connections[i].getConnectionProperties().getProperty("CompartmentOCID"); //NOI18N
            String description = connections[i].getConnectionProperties().getProperty("Description"); //NOI18N
            if (ocid != null && compartmentId != null) {
                DatabaseItem dbItem
                        = new DatabaseItem(OCID.of(ocid, "Databases"), compartmentId, name, null, name); //NOI18N
                dbItem.setDescription(description);
                adbConnections.put(name, dbItem);
            }
        }
    }

    @Override
    public NotifyDescriptor createInput() {
        return Steps.createQuickPick(adbConnections, Bundle.SelectDBConnection());
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
        return adbConnections.isEmpty();
    }
    
}
