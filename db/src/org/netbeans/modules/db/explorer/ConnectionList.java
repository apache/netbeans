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

package org.netbeans.modules.db.explorer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * Class which encapsulates the Vector used to keep a list of connections
 * in the Database Explorer. It also can fire events when connections are added
 * and removed through ConnectionListener.
 *
 * This class only maintains a list of DBConnection objects. It has no links
 * to the UI (nodes representing these objects), therefore adding a DBConnection
 * doesn't create a node for it.
 *
 * @author Andrei Badea
 */
public class ConnectionList {

    private static ConnectionList DEFAULT;

    private final List<ConnectionListener> listeners = new CopyOnWriteArrayList<ConnectionListener>();

    private Lookup.Result<DatabaseConnection> result = getLookupResult();

    /**
     * Set of connections that the listeners were notified about. Stored not to
     * fire change events if the list has not been actually changed.
     */
    private WeakHashMap<DatabaseConnection, Boolean> lastKnownConnections =
            new WeakHashMap<DatabaseConnection, Boolean>();

    public static ConnectionList getDefault(boolean initialize) {
        if (initialize) {
            return getDefault();
        } else {
            return DEFAULT;
        }
    }

    public static synchronized ConnectionList getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new ConnectionList();
        }
        return DEFAULT;
    }

    private ConnectionList() {
        // issue 75204: forces the DataObject's corresponding to the DatabaseConnection's
        // to be initialized and held strongly so the same DatabaseConnection is
        // returns as long as it is held strongly
        result.allInstances();

        result.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent e) {
                fireListeners();
            }
        });
    }

    public DatabaseConnection[] getConnections() {
        Collection<? extends DatabaseConnection> dbconns = result.allInstances();
        return dbconns.toArray(new DatabaseConnection[dbconns.size()]);
    }

    public DatabaseConnection getConnection(DatabaseConnection impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        DatabaseConnection[] dbconns = getConnections();
        for (int i = 0; i < dbconns.length; i++) {
            if (impl.equals(dbconns[i])) {
                return dbconns[i];
            }
        }
        return null;
    }

    public void add(DatabaseConnection dbconn) throws DatabaseException {
        if (dbconn == null) {
            throw new NullPointerException();
        }

        if (contains(dbconn)) {
            throw new DatabaseException(NbBundle.getMessage (ConnectionList.class, "EXC_ConnectionAlreadyExists")); // NOI18N
        }

        try {
            DatabaseConnectionConvertor.create(dbconn);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    public boolean contains(DatabaseConnection dbconn) {
        return getConnection(dbconn) != null;
    }

    public void remove(DatabaseConnection dbconn) throws DatabaseException {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        try {
            DatabaseConnectionConvertor.remove(dbconn);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Fire change event. Check whether the list of connections has changed
     * since the last invocation of this method. If it has not changed,
     * listeners are not notified.
     */
    private void fireListeners() {
        boolean theSame;
        DatabaseConnection[] connections = getConnections();
        synchronized (this) {
            if (connections.length == lastKnownConnections.size()) {
                theSame = true;
                for (int i = 0; i < connections.length; i++) {
                    if (!lastKnownConnections.containsKey(connections[i])) {
                        theSame = false;
                        break;
                    }
                }
            } else {
                theSame = false;
            }
            if (theSame) {
                return;
            } else {
                lastKnownConnections.clear();
                for (DatabaseConnection dc : connections) {
                    lastKnownConnections.put(dc, Boolean.TRUE);
                }
            }
        }
        for (ConnectionListener listener : listeners) {
            listener.connectionsChanged();
        }
    }

    private synchronized Lookup.Result<DatabaseConnection> getLookupResult() {
        return Lookups.forPath(DatabaseConnectionConvertor.CONNECTIONS_PATH).lookupResult(DatabaseConnection.class);
    }
}
