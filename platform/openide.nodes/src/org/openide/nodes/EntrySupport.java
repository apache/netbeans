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
package org.openide.nodes;

import java.util.Collection;
import java.util.List;
import org.openide.nodes.Children.Entry;

/**
 *
 * @author t_h
 */
abstract class EntrySupport {

    /** children we are attached to */
    public final Children children;

    /** Creates a new instance of EntrySupport */
    protected EntrySupport(Children children) {
        this.children = children;
    }

    //
    // API methods to be called from Children
    //
    public abstract int getNodesCount(boolean optimalResult);

    public abstract Node[] getNodes(boolean optimalResult);

    /** Getter for a node at given position. If node with such index
     * does not exists it should return null.*/
    public abstract Node getNodeAt(int index);

    /**
     * @return currently created nodes or null if no node is created
     */
    public abstract Node[] testNodes();

    public abstract boolean isInitialized();

    abstract void notifySetEntries();

    final void setEntries(Collection<? extends Entry> entries) {
        setEntries(entries, false);
    }
    abstract void setEntries(Collection<? extends Entry> entries, boolean noCheck);

    /** Access to copy of current entries.
     * @return copy of entries in the objects
     */
    protected abstract List<Entry> getEntries();

    /** Abililty to create a snaphshot
     * @return immutable and unmodifiable list of Nodes that represent the children at current moment
     */
    abstract List<Node> snapshot();

    /** Refreshes content of one entry. Updates the state of children appropriately. */
    abstract void refreshEntry(Entry entry);
}
