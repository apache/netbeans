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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Lookup;

/**
 * A node provider that provides ConnectionNode instances.
 * 
 * @author Rob Englander
 */
public class ConnectionNodeProvider extends NodeProvider {
    private final PropertyChangeListener PCL = new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent pce) {
            if (pce.getPropertyName().equals(BaseNode.PROP_DISPLAY_NAME)) {
                if (pce.getSource() instanceof DatabaseConnection) {
                    initialize((DatabaseConnection) pce.getSource());
                } else {
                    initialize();
                }
            }
        }
    };
    
    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private void scheduleNodeSelectionAfterUpdate(
            final DatabaseConnection connToSelect) {

        final NodeListener nl = new NodeAdapter() {
            @Override
            public void childrenAdded(NodeMemberEvent ev) {
                select();
            }

            @Override
            public void childrenReordered(NodeReorderEvent ev) {
                select();
            }

            private void select() {
                if (SwingUtilities.isEventDispatchThread()) {
                    connToSelect.selectInExplorer(false);
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            connToSelect.selectInExplorer(false);
                        }
                    });
                }
                RootNode.instance().removeNodeListener(this);
            }
        };
        RootNode.instance().addNodeListener(nl);
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            @Override
            public ConnectionNodeProvider createInstance(Lookup lookup) {
                ConnectionNodeProvider provider = new ConnectionNodeProvider(lookup);
                provider.setup();
                return provider;
            }
        };
    }
    
    private final ConnectionList connectionList;
    
    private ConnectionNodeProvider(Lookup lookup) {
        super(lookup, connectionNodeComparator);
        connectionList = getLookup().lookup(ConnectionList.class);
    }
    
    private void setup() {
        connectionList.addConnectionListener(
            new ConnectionListener() {
                @Override
                public void connectionsChanged() {
                    initialize();
                }
            }
        );
    }

    @Override
    protected synchronized void initialize() {
        initialize(null);
    }

    protected synchronized void initialize(DatabaseConnection selectedConn) {
        List<Node> newList = new ArrayList<>();
        DatabaseConnection newConnection = null;
        DatabaseConnection[] connections = connectionList.getConnections();
        for (DatabaseConnection connection : connections) {
            // Make sure the PCL is only added once
            connection.removePropertyChangeListener(PCL);
            connection.addPropertyChangeListener(PCL);
            Collection<Node> matches = getNodes(connection);
            if (matches.size() > 0) {
                newList.addAll(matches);
            } else {
                NodeDataLookup lookup = new NodeDataLookup();
                lookup.add(connection);
                newConnection = connection;
                newList.add(ConnectionNode.create(lookup, this));
            }
        }

        // select added connection in explorer
        final DatabaseConnection connToSelect = newConnection != null
                ? newConnection : selectedConn; // new or last selected one
        if (connToSelect != null) {
            scheduleNodeSelectionAfterUpdate(connToSelect);
        }
        setNodes(newList);
    }

    private static final Comparator<Node> connectionNodeComparator = new Comparator<Node>() {
        @Override
        public int compare(Node model1, Node model2) {
            return model1.getDisplayName().compareToIgnoreCase(model2.getDisplayName());
        }
    };
}
