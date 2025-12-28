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

package org.netbeans.api.db.explorer.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.node.RootNode;
import org.netbeans.modules.db.util.DataComboBoxModel;
import org.netbeans.modules.db.util.DataComboBoxSupport;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * This class contains utility methods for working with and/or displaying
 * database connections in the UI. Currently it provides a method for
 * populating a combo box with the list of database connections from
 * {@link ConnectionManager}.
 *
 * @author Andrei Badea
 *
 * @since 1.18
 */
public final class DatabaseExplorerUIs {

    private DatabaseExplorerUIs() {
    }
    
    /**
     * Populates and manages the contents of the passed combo box. The combo box
     * contents consists of the database connections defined in
     * the passes instance of {@link ConnectionManager} and a Add Database Connection
     * item which displays the New Database Connection dialog when selected.
     *
     * <p>This method may cause the replacement of the combo box model,
     * thus the caller is recommended to register a
     * {@link java.beans.PropertyChangeListener} on the combo box when
     * it needs to check the combo box content when it changes.</p>
     *
     * @param comboBox combo box to be filled with the database connections.
     */
    public static void connect(JComboBox comboBox, ConnectionManager connectionManager) {
        DataComboBoxSupport.connect(comboBox, new ConnectionDataComboBoxModel(connectionManager));
    }

    private static final class ConnectionDataComboBoxModel implements DataComboBoxModel {

        private final ConnectionManager connectionManager;
        private final ConnectionComboBoxModel comboBoxModel;

        public ConnectionDataComboBoxModel(ConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            this.comboBoxModel = new ConnectionComboBoxModel(connectionManager);
        }

        @Override
        public String getItemTooltipText(Object item) {
            return ((DatabaseConnection)item).toString();
        }

        @Override
        public String getItemDisplayName(Object item) {
            return ((DatabaseConnection)item).getDisplayName();
        }

        @Override
        public void newItemActionPerformed() {
            Set<DatabaseConnection> oldConnections = new HashSet<>(Arrays.<DatabaseConnection>asList(connectionManager.getConnections()));
            connectionManager.showAddConnectionDialog(null);

            // try to find the new connection
            DatabaseConnection[] newConnections = connectionManager.getConnections();
            if (newConnections.length == oldConnections.size()) {
                // no new connection, so...
                return;
            }
            for (int i = 0; i < newConnections.length; i++) {
                if (!oldConnections.contains(newConnections[i])) {
                    comboBoxModel.setSelectedItem(newConnections[i]);
                    break;
                }
            }
        }

        @Override
        public String getNewItemDisplayName() {
            return NbBundle.getMessage(DatabaseExplorerUIs.class, "LBL_NewDbConnection");
        }

        @Override
        public ComboBoxModel getListModel() {
            return comboBoxModel;
        }
    }

    private static final class ConnectionComboBoxModel extends AbstractListModel implements ComboBoxModel {

        private final ConnectionManager connectionManager;
        private final List<Object> connectionList = new ArrayList<>();
        private Object selectedItem; // can be anything, not just a database connection
        private ConnectionListener cl = new ConnectionListener() {
            @Override
            public void connectionsChanged() {
                updateConnectionList();
            }
        };

        public ConnectionComboBoxModel(ConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            connectionManager.addConnectionListener(WeakListeners.create(
                    ConnectionListener.class, cl, connectionManager));
            updateConnectionList();
        }

        private void updateConnectionList() {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    int oldLength = connectionList.size();
                    connectionList.clear();
                    connectionList.addAll(
                            Arrays.asList(connectionManager.getConnections()));
                    connectionList.sort(new ConnectionComparator());
                    fireContentsChanged(this, 0,
                            Math.max(connectionList.size(), oldLength));
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            } else {
                SwingUtilities.invokeLater(r);
            }
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selectedItem = anItem;
        }

        @Override
        public Object getElementAt(int index) {
            return connectionList.get(index);
        }

        @Override
        public int getSize() {
            return connectionList.size();
        }

        @Override
        public Object getSelectedItem() {
            return selectedItem;
        }
    }

    private static final class ConnectionComparator implements Comparator {

        @Override
        public boolean equals(Object that) {
            return that instanceof ConnectionComparator;
        }

        @Override
        public int compare(Object dbconn1, Object dbconn2) {
            if (dbconn1 == null) {
                return dbconn2 == null ? 0 : -1;
            } else {
                if (dbconn2 == null) {
                    return 1;
                }
            }

            String dispName1 = ((DatabaseConnection)dbconn1).getDisplayName();
            String dispName2 = ((DatabaseConnection)dbconn2).getDisplayName();
            if (dispName1 == null) {
                return dispName2 == null ? 0 : -1;
            } else {
                return dispName2 == null ? 1 : dispName1.compareToIgnoreCase(dispName2);
            }
        }
    }
    
    /**
     * Provides access to defined connections. The returned node contains connections,
     * possibly not active, as its children. Active connections may offer database content as
     * nested Nodes, depending on the actual DB provider.
     * 
     * @return connection nodes' parent
     * @since 1.82
     */
    public static Node connectionsNode() {
        Node original = RootNode.instance();
        return new FilterNode(original, new ConnChildren(original));
    }
    
    static final class ConnChildren extends FilterNode.Children {

        public ConnChildren(Node or) {
            super(or);
        }

        @Override
        protected Node[] createNodes(Node key) {
            DatabaseConnection c = key.getLookup().lookup(DatabaseConnection.class);
            return c != null ? super.createNodes(key) : null;
        }
    }
}
