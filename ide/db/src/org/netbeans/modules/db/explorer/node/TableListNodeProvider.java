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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.node.TableListNode.Type;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 * @author Rob Englander
 */
public class TableListNodeProvider extends NodeProvider {
    
    private final DatabaseConnection connection;
    private PropertyChangeListener propertyChangeListener;
    private boolean setup = false;

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            @Override
            public TableListNodeProvider createInstance(Lookup lookup) {
                TableListNodeProvider provider = new TableListNodeProvider(lookup);
                return provider;
            }
        };
    }

    @Override
    protected void initialize() {
        if (! connection.isConnected()) {
            removeAllNodes();
            setup = false;
        } else {
            if (!setup) {
                setNodesForCurrentSettings();
                setup = true;
            }
        }
        if (propertyChangeListener == null) {
            propertyChangeListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("separateSystemTables")) { //NOI18N
                        setNodesForCurrentSettings();
                    }
                }
            };
            connection.addPropertyChangeListener(WeakListeners.propertyChange(
                    propertyChangeListener, connection));
        }
    }

    private void setNodesForCurrentSettings() {
        List<Node> newList = new ArrayList<>();
        if (connection.isSeparateSystemTables()) {
            newList.add(
                    TableListNode.create(createLookup(), this, Type.STANDARD));
            newList.add(TableListNode.create(createLookup(), this, Type.SYSTEM));
        } else {
            newList.add(TableListNode.create(createLookup(), this, Type.ALL));
        }
        setNodes(newList);
    }

    /**
     * Create a lookup for TableListNode. Each TableListNode needs a unique
     * lookup, because it will be used as key for the node.
     */
    private NodeDataLookup createLookup() {
        NodeDataLookup lookup = new NodeDataLookup();
        lookup.add(connection);

        MetadataElementHandle<Schema> schemaHandle = getLookup().lookup(
                MetadataElementHandle.class);
        if (schemaHandle != null) {
            lookup.add(schemaHandle);
        }
        return lookup;
    }

    private TableListNodeProvider(Lookup lookup) {
        super(lookup, tableNodeComparator);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    private static final Comparator<Node> tableNodeComparator = new Comparator<Node>() {

        @Override
        public int compare(Node o1, Node o2) {
            if (o1 instanceof TableListNode && o2 instanceof TableListNode) {
                return (((TableListNode) o1).getType().equals(Type.SYSTEM))
                        ? 1 : -1;
            } else {
                return o1.getDisplayName().compareToIgnoreCase(
                        o2.getDisplayName());
            }
        }
    };
}
