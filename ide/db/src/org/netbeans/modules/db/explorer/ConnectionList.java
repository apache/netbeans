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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;
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
    private static final String PREF_PREFERRED_CONNECTION_NAME = "preferredConnection"; // NOI18N

    private static ConnectionList DEFAULT;

    private final List<ConnectionListener> listeners = new CopyOnWriteArrayList<ConnectionListener>();

    private final Preferences dbPreferences;

    /**
     * Name of the LAST connection returned as a default.
     */
    private String lastPrefName;

    private Lookup.Result<DatabaseConnection> result = getLookupResult();
    
    private Reference<DatabaseConnection> preferred = new WeakReference<>(null);
    
    /**
     * Set of connections that the listeners were notified about. Stored not to
     * fire change events if the list has not been actually changed.
     */
    private WeakHashMap<DatabaseConnection, Boolean> lastKnownConnections =
            new WeakHashMap<DatabaseConnection, Boolean>();
    
    private boolean prefChanged;

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
        dbPreferences = NbPreferences.forModule(ConnectionList.class);
        dbPreferences.addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (evt.getNode() == null || !evt.getNode().absolutePath().equals(dbPreferences.absolutePath())) {
                    return;
                }
                if (!PREF_PREFERRED_CONNECTION_NAME.equals(evt.getKey())) {
                    return;
                }
                DatabaseConnection c = getPreferredConnection(true);
                synchronized (ConnectionList.this) {
                    Reference<DatabaseConnection> ref = preferred;
                    DatabaseConnection pref = ref.get();
                    if (c == pref) {
                        return;
                    }
                    prefChanged = true;
                }
                fireListeners();
            }
        });
    }

    public DatabaseConnection[] getConnections() {
        Collection<? extends DatabaseConnection> dbconns = result.allInstances();
        return dbconns.toArray(new DatabaseConnection[0]);
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
        synchronized (this) {
            DatabaseConnection pref = preferred.get();
            if (pref == dbconn) {
                preferred.clear();
            }
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
        DatabaseConnection publicPref = getPreferredConnection(true);
        synchronized (this) {
            // verify that the connection list actually contains the preferred one, and clear the ref if not
            Reference<DatabaseConnection> ref = preferred;
            DatabaseConnection pref = ref.get();
            if (pref != null && pref != publicPref) {
                ref.clear();
                prefChanged = true;
            }
            if (connections.length == lastKnownConnections.size()) {
                theSame = prefChanged;
                for (int i = 0; i < connections.length; i++) {
                    if (!lastKnownConnections.containsKey(connections[i])) {
                        theSame = false;
                        break;
                    }
                }
            } else {
                theSame = false;
            }
            if (!prefChanged && theSame) {
                return;
            } else {
                lastKnownConnections.clear();
                for (DatabaseConnection dc : connections) {
                    lastKnownConnections.put(dc, Boolean.TRUE);
                }
            }
            prefChanged = false;
        }
        for (ConnectionListener listener : listeners) {
            listener.connectionsChanged();
        }
    }

    private synchronized Lookup.Result<DatabaseConnection> getLookupResult() {
        return Lookups.forPath(DatabaseConnectionConvertor.CONNECTIONS_PATH).lookupResult(DatabaseConnection.class);
    }
    
    private class PreferredRef extends WeakReference<DatabaseConnection> implements PropertyChangeListener {
        private final PropertyChangeListener l;
        
        public PreferredRef(DatabaseConnection referent) {
            super(referent);
            referent.addPropertyChangeListener(l = WeakListeners.propertyChange(this, referent));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            DatabaseConnection c = get();
            if (c != null && DatabaseConnection.PROP_NAME.equals(evt)) {
                dbPreferences.put(PREF_PREFERRED_CONNECTION_NAME, c.getName());
            }
        }

        @Override
        public void clear() {
            DatabaseConnection c = get();
            super.clear(); 
            if (c != null) {
                c.removePropertyChangeListener(l);
            }
        }
    }
    
    public void setPreferredConnection(DatabaseConnection c) {
        if (!contains(c)) {
            throw new IllegalArgumentException();
        }
        synchronized (this) {
            Reference<DatabaseConnection> ref = preferred;
            if (ref.get() == c) {
                return;
            }
            ref.clear();
            preferred = new PreferredRef(c);
            lastPrefName = null;
        }
        dbPreferences.put(PREF_PREFERRED_CONNECTION_NAME, c.getName());
        fireListeners();
    }
    
    public DatabaseConnection getPreferredConnection(boolean selectFirst) {
        String fallback;
        DatabaseConnection p;
        synchronized (this) {
            p = preferred.get();
            if (p != null) {
                return p;
            }
            fallback = this.lastPrefName;
        }
        String prefName = dbPreferences.get(PREF_PREFERRED_CONNECTION_NAME, null);
        DatabaseConnection[] conns = getConnections();
        DatabaseConnection selected = findConnection(prefName, conns);
        boolean prefChanged = false;
        if (selected == null) {
            if (conns.length > 0) {
                // find the last one, or just anything.
                selected = findConnection(fallback, conns);
            }
            if (selected != null) {
                prefChanged = !Objects.equals(lastPrefName, selected.getName());
                lastPrefName = selected.getName();
            } else {
                return null;
            }
        }
        synchronized (this) {
            p = preferred.get();
            if (p == null) {
                preferred = new PreferredRef(selected);
                p = selected;
            }
        }
        if (prefChanged) {
            fireListeners();
        }
        return p;
    }
    
    private static DatabaseConnection findConnection(String name, DatabaseConnection[] conns) {
        for (int i = 0; i < conns.length; i++) {
            DatabaseConnection c = conns[i];
            // accept anything when no preference is set.
            if (name == null || name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

}
