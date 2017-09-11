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
