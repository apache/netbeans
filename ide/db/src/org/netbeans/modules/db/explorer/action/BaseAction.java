/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.db.explorer.action;

import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.node.NodeRegistry;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Rob Englander
 */
public abstract class BaseAction extends NodeAction {

    @Override
    public boolean asynchronous() {
        return false;
    }
    
    protected static String findSchemaWorkingName(Lookup lookup) {
        DatabaseConnection conn = lookup.lookup(DatabaseConnection.class);
        MetadataModel model = conn.getMetadataModel();
        final MetadataElementHandle handle = lookup.lookup(MetadataElementHandle.class);

        final String[] array = { null };

        try {
            model.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Schema schema = (Schema)handle.resolve(metaData);
                        Catalog catalog = schema.getParent();
                        String schemaName = schema.getName();
                        if (schemaName == null) {
                            schemaName = catalog.getName();
                        }
                        array[0] = schemaName;
                    }
                }
            );
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(BaseAction.class, null, e, true);
        }

        return array[0];
    }
}
