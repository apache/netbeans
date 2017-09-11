/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
