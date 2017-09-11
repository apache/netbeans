/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
