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

package org.netbeans.api.db.explorer.node;

import java.util.*;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

/**
 * This is the base class for all node providers, which are used to provide
 * lists of Node instances.  This is the mechanism used to dynamically
 * add child nodes to other database explorer nodes.  Instances of NodeProvider
 * are attached to nodes through the xml layer.
 * 
 * @author Rob Englander
 */
public abstract class NodeProvider implements Lookup.Provider {
    // @GuardedBy("nodeSet")
    private final TreeSet<Node> nodeSet;
    private final ChangeSupport changeSupport;
    private final Lookup lookup;
    protected boolean initialized = false;
    private boolean isProxied = false;

    /**
     * Constructor
     * 
     * @param lookup the associated lookup
     */
    public NodeProvider(Lookup lookup) {
        this.lookup = lookup;
        changeSupport = new ChangeSupport(this);
        nodeSet = new TreeSet<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node n1, Node n2) {
                return n1.getDisplayName().compareTo(n2.getDisplayName());
            }
        });
    }
    
    /**
     * Constructor
     * 
     * @param lookup the associated lookup
     * @param comparator the comparator to use for sorting the nodes
     */
    public NodeProvider(Lookup lookup, Comparator<Node> comparator) {
        this.lookup = lookup;
        changeSupport = new ChangeSupport(this);
        nodeSet = new TreeSet<Node>(comparator);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    /**
     * Get the list of nodes.
     * 
     * @return the list of nodes.
     */
    public synchronized Collection<Node> getNodes() {
        if (!initialized) {
            initialize();
            initialized = true;
        }

        if (isProxied) {
            List<Node> nodes = new ArrayList<Node>();

            for (Node child : nodeSet) {
                if (child instanceof BaseNode) {
                    BaseNode node = (BaseNode) child;
                    Collection<? extends Node> list = node.getNodeRegistry().getNodes();
                    for (Node n : list) {
                        nodes.add(n);
                    }
                } else {
                    for (Node n : child.getChildren().getNodes()) {
                        nodes.add(n);
                    }
                }
            }

            return Collections.unmodifiableCollection(nodes);

        } else {
            return Collections.unmodifiableCollection(nodeSet);
        }
    }

    public synchronized void refresh() {
        initialized = false;
        @SuppressWarnings("unchecked")
        TreeSet<Node> nodes = (TreeSet<Node>)nodeSet.clone();

        for (Node child : nodes) {
            if (child instanceof BaseNode) {
                ((BaseNode)child).refresh();
            }
        }
    }

    protected abstract void initialize();

    /**
     * Get the list of nodes that contain a lookup that in turn contains 
     * an object with a matching hash code.
     * 
     * @param dataObject the data object.
     * 
     * @return the list of nodes that contain a lookup containing the data object
     */
    protected Collection<Node> getNodes(Object dataObject) {
        
        List<Node> results = new ArrayList<Node>();

        synchronized (nodeSet) {
            for (Node child : nodeSet) {
                Object obj = child.getLookup().lookup(dataObject.getClass());
                if (obj != null && obj.hashCode() == dataObject.hashCode() && obj.equals(dataObject)) {
                    results.add(child);
                }
            }
        }
        
        return Collections.unmodifiableCollection(results);
    }

    public void setProxyNodes(Collection<Node> newList) {
        synchronized (nodeSet) {
            isProxied = true;
            nodeSet.clear();
            nodeSet.addAll(newList);
        }

        changeSupport.fireChange();
    }

    /**
     * Sets the list of nodes.
     * 
     * @param newList the new list of nodes
     */
    public void setNodes(Collection<Node> newList) {
        synchronized (nodeSet) {
            isProxied = false;
            nodeSet.clear();
            nodeSet.addAll(newList);
        }

        changeSupport.fireChange();
    }
    
    /**
     * Add a Node.
     * 
     * @param node the node to add
     */
    public void addNode(Node node) {

        synchronized (nodeSet) {
            nodeSet.add(node);
        }
        
        changeSupport.fireChange();
    }

    public void removeNode(Node node) {
        synchronized (nodeSet) {
            nodeSet.remove(node);
        }
        
        changeSupport.fireChange();
    }

    /**
     * Remove all nodes.
     */
    public void removeAllNodes() {
        synchronized (nodeSet) {
            nodeSet.clear();
        }

        changeSupport.fireChange();
    }
    
    /**
     * Add a change listener.
     * 
     * @param listener the listener to add.
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    /**
     * Remove a change listener.
     * 
     * @param listener the listener to remove.
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
}
