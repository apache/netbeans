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

package org.netbeans.modules.versioning.util.status;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.openide.explorer.view.NodeTableModel;
import org.openide.nodes.Node;

/**
 *
 * @author ondra
 */
public class VCSStatusTableModel<T extends VCSStatusNode> extends NodeTableModel {

    private T[] nodes;

    public VCSStatusTableModel (T[] nodes) {
        this.nodes = nodes;
    }

    @SuppressWarnings("unchecked")
    public final Class<T> getItemClass () {
        return (Class<T>) nodes.getClass().getComponentType();
    }

    @Override
    public final void setNodes (Node[] nodes) {
        throw new IllegalStateException("Do not call this method");
    }

    public final void setNodes (T[] nodes) {
        this.nodes = nodes;
        super.setNodes(nodes);
    }

    public T getNode (int idx) {
        return nodes[idx];
    }

    public T[] getNodes () {
        return Arrays.copyOf(nodes, nodes.length);
    }

    @SuppressWarnings("unchecked")
    public void remove (List<T> toRemove) {
        Set<T> newNodes = new LinkedHashSet<T>(Arrays.asList(nodes));
        newNodes.removeAll(toRemove);
        nodes = newNodes.toArray((T[]) java.lang.reflect.Array.newInstance(getItemClass(), newNodes.size()));
        super.setNodes(nodes);
    }

    @SuppressWarnings("unchecked")
    public void add (List<T> toAdd) {
        Set<T> newNodes = new LinkedHashSet<T>(Arrays.asList(nodes));
        newNodes.addAll(toAdd);
        nodes = newNodes.toArray((T[]) java.lang.reflect.Array.newInstance(getItemClass(), newNodes.size()));
        super.setNodes(nodes);
    }
}
