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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
