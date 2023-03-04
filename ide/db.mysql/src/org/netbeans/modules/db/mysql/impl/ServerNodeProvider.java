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

package org.netbeans.modules.db.mysql.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.DatabaseServerManager;
import org.netbeans.modules.db.mysql.nodes.ServerNode;
import org.netbeans.modules.db.mysql.util.DatabaseUtils;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * Provides a node for working with a local MySQL Server instance
 * 
 * @author David Van Couvering
 */
public final class ServerNodeProvider extends NodeProvider {
    private static final Logger LOGGER = Logger.getLogger(ServerNodeProvider.class.getName());
    private static ServerNodeProvider DEFAULT = null;

    private static final MySQLOptions options = MySQLOptions.getDefault();
    private final CopyOnWriteArrayList<ChangeListener> listeners =
            new CopyOnWriteArrayList<ChangeListener>();

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            public ServerNodeProvider createInstance(Lookup lookup) {
                DEFAULT = new ServerNodeProvider(lookup);
                return DEFAULT;
            }
        };
    }

    public static ServerNodeProvider getDefault() {
        return DEFAULT;
    }

    private ServerNodeProvider(Lookup lookup) {
        super(lookup);
    }

    @Override
    protected void initialize() {

        if (! options.isProviderRegistered() && ! options.isProviderRemoved()) {
            findAndRegisterMySQL();
        }

        if ( !options.isProviderRegistered() ) {
            DatabaseServerManager.getDatabaseServer().disconnect();
        }

        update();

    }

    private void update() {
        List<Node> newList = new ArrayList<Node>();
        if (isRegistered()) {
            Node node = ServerNode.create(DatabaseServerManager.getDatabaseServer());
            newList.add(node);
        }

        setNodes(newList);
    }

    /**
     * Try to find MySQL on the local machine, and if it can be found,
     * register a connection and the MySQL server node in the Database
     * Explorer.
     */
    private void findAndRegisterMySQL() {
        if ( (DatabaseUtils.getJDBCDriver()) == null ) {
            // Driver not registered, that's OK, the user may
            // have deleted it, but nothing to do here.
            return;
        }

        if (options.isProviderRegistered() || options.isProviderRemoved()) {
            // If someone explicitly removes the MySQL node, we shouldn't
            // put it back - that's annoying...
            return;
        }

        registerConnectionListener();
        findAndRegisterInstallation();
    }

    private void registerConnectionListener() {
        // Register a listener that will auto-register the MySQL
        // server provider when a user adds a MySQL connection
        ConnectionManager.getDefault().addConnectionListener(
                new DbExplorerConnectionListener());
    }

    private void findAndRegisterInstallation() {
        Installation installation = InstallationManager.detectInstallation();
        registerInstallation(installation);
    }

    public void registerInstallation(Installation installation) {
        if ( installation == null ) {
            return;
        }

        String[] command = installation.getAdminCommand();
        if ( Utils.isValidExecutable(command[0], true /*emptyOK*/) ||
             Utils.isValidURL(command[0], true /*emptyOK*/ )) {
            options.setAdminPath(command[0]);
            options.setAdminArgs(command[1]);
        }

        command = installation.getStartCommand();
        if ( Utils.isValidExecutable(command[0], true)) {
            options.setStartPath(command[0]);
            options.setStartArgs(command[1]);
        }

        command = installation.getStopCommand();
        if ( Utils.isValidExecutable(command[0], true)) {
            options.setStopPath(command[0]);
            options.setStopArgs(command[1]);
        }

        options.setPort(installation.getDefaultPort());

        setRegistered(true);
    }
    
    public void setRegistered(boolean registered) {
        boolean old = isRegistered();
        if ( registered != old ) {
            final DatabaseServer instance = DatabaseServerManager.getDatabaseServer();
            options.setProviderRegistered(registered);
            
            if ( ! registered ) {
                instance.disconnect();
            } else {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            instance.checkConfiguration();
                        } catch (DatabaseException dbe) {
                            LOGGER.log(Level.INFO, null, dbe);
                        }
                    }
                });
            }

            update();
            notifyChange();
        }
    }
    
    public synchronized boolean isRegistered() {
        return options.isProviderRegistered();
    }
    
    void notifyChange() {
        ChangeEvent evt = new ChangeEvent(this);
        for ( ChangeListener listener : listeners ) {
            listener.stateChanged(evt);
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }
}
