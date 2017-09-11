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
