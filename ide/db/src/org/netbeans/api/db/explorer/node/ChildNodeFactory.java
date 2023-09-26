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

package org.netbeans.api.db.explorer.node;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.db.explorer.node.BaseFilterNode;
import org.netbeans.modules.db.explorer.node.NodeRegistry;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * The ChildNodeFactory is used for getting node instances based on
 * a Lookup instance as the key.  Subclasses of BaseNode that can have
 * children are constructed with an instance of ChildNodeFactory.
 * 
 * @author Rob Englander
 */
public class ChildNodeFactory extends ChildFactory<Lookup> {
    
    private final Lookup dataLookup;

    /**
     * Constructor. 
     * 
     * @param lookup the associated data lookup
     */
    public ChildNodeFactory(Lookup lookup) {
        dataLookup = lookup;
    }

    /**
     * Refreshes this factory which causes it to get its
     * child keys and subsequently its child nodes 
     */
    public void refresh() {
        super.refresh(false);
    }

    /**
     * Refreshes this factory which causes it to get its
     * child keys and subsequently its child nodes immeditately.
     */
    public void refreshSync() {
        super.refresh(true);
    }

    @Override
    public Node[] createNodesForKey(Lookup key) {
        
        // the node should be in the lookup
        Node childNode = key.lookup(Node.class);
        
        if (childNode == null) {
            return new Node[] {  };
        }
        else {
            return new Node[]{new BaseFilterNode(childNode)}; // clone - #221817
        }
    }

    @Override
    protected boolean createKeys(List<Lookup> toPopulate) {
        
        // the node registry is in the data lookup
        NodeRegistry registry = dataLookup.lookup(NodeRegistry.class);
        Collection<? extends Node> nodes = registry.getNodes();
        for (Node node : nodes) {
            // the key for each node is its lookup
            Lookup lookup = node.getLookup();
            toPopulate.add(lookup);
        }

        return true;
    }
}
