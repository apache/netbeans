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

package org.netbeans.modules.db.explorer.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Rob Englander
 */
public class TableNodeProvider extends NodeProvider {

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            @Override
            public TableNodeProvider createInstance(Lookup lookup) {
                TableNodeProvider provider = new TableNodeProvider(lookup, false);
                return provider;
            }
        };
    }

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getSystemFactory() {
        return SystemFactoryHolder.FACTORY;
    }

    private static class SystemFactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            @Override
            public TableNodeProvider createInstance(Lookup lookup) {
                TableNodeProvider provider = new TableNodeProvider(lookup, true);
                return provider;
            }
        };
    }

    private final DatabaseConnection connection;
    private final boolean system;
    private MetadataElementHandle<Schema> schemaHandle;

    @SuppressWarnings("unchecked")
    private TableNodeProvider(Lookup lookup, boolean system) {
        super(lookup, tableComparator);
        this.system = system;
        connection = getLookup().lookup(DatabaseConnection.class);
        schemaHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    @Override
    protected void initialize() {
        
        final List<Node> newList = new ArrayList<Node>();

        boolean connected = connection.isConnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                        @Override
                        public void run(Metadata metaData) {
                            Schema schema = schemaHandle.resolve(metaData);
                            if (schema != null) {
                                Collection<Table> tables = schema.getTables();
                                for (Table table : tables) {
                                    if (connection.isSeparateSystemTables() && ((!system) && table.isSystem())
                                            || (system && (!table.isSystem()))) {
                                        continue;
                                    }
                                    MetadataElementHandle<Table> handle = MetadataElementHandle.create(table);
                                    Collection<Node> matches = getNodes(handle);
                                    if (matches.size() > 0) {
                                        newList.addAll(matches);
                                    } else {
                                        NodeDataLookup lookup = new NodeDataLookup();
                                        lookup.add(connection);
                                        lookup.add(handle);
                                        newList.add(TableNode.create(lookup, TableNodeProvider.this));
                                    }
                                }
                            }
                        }
                    }
                );
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }
        }

        setNodes(newList);
    }

    private static final Comparator<Node> tableComparator = new Comparator<Node>() {

        @Override
        public int compare(Node node1, Node node2) {
            return node1.getDisplayName().compareTo(node2.getDisplayName());
        }

    };
}
