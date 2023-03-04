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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * ConnectedNodeprovider serves as a base class for all node providers
 * that work with a database connection.
 *
 * @author Rob Englander
 */
public abstract class ConnectedNodeProvider  extends NodeProvider {

    private final DatabaseConnection connection;
    private boolean setup = false;

    protected ConnectedNodeProvider(Lookup lookup) {
        super(lookup);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    /**
     * Create a BaseNode instance.
     *
     * @param lookup the lookup to use to create the node
     * @return the created baseNode
     */
    protected abstract BaseNode createNode(NodeDataLookup lookup);

    protected synchronized void initialize() {
        if (! connection.isConnected()) {
            removeAllNodes();
            setup = false;
        } else {
            if (!setup) {
                NodeDataLookup lookup = new NodeDataLookup();
                lookup.add(connection);

                MetadataElementHandle<Schema> schemaHandle = getLookup().lookup(MetadataElementHandle.class);
                if (schemaHandle != null) {
                    lookup.add(schemaHandle);
                }

                List<Node> newList = new ArrayList<Node>();

                newList.add(createNode(lookup));

                setNodes(newList);
                setup = true;
            }
        }
    }
}
