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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/** Event describing change in the list of a node's children.
*
* @author Jaroslav Tulach
*/
public class NodeMemberEvent extends NodeEvent {
    static final long serialVersionUID = -3973509253579305102L;

    /** is this add event? */
    private boolean add;

    /** list of changed nodes */
    private Node[] delta;

    /** list of nodes indexes, can be null if it should be computed lazily */
    private int[] indices;

    /** current snapshot */
    private final List<Node> currSnapshot;
    
    /** previous snapshot or null */
    private final List<Node> prevSnapshot;
 
    org.openide.nodes.Children.Entry sourceEntry;

    /** Package private constructor to allow construction only
    * @param n node that should fire change
    * @param add true if nodes has been added
    * @param delta array of nodes that have changed
    * @param from nodes to find indices in
    */
    NodeMemberEvent(Node n, boolean add, Node[] delta, Node[] from) {
        super(n);
        this.add = add;
        this.delta = delta;
        this.prevSnapshot = from != null ? Arrays.asList(from) : null;
        this.currSnapshot = n.getChildren().snapshot();
    }

    /** Provides static and immmutable info about the number, and instances of
     * nodes available during the time the event was emited.
     * @return immutable and unmodifiable list of nodes
     * @since 7.7
     */
    public final List<Node> getSnapshot() {
        return currSnapshot;
    }

    /** Get the type of action.
    * @return <CODE>true</CODE> if children were added,
    *    <CODE>false</CODE> if removed
    */
    public final boolean isAddEvent() {
        return add;
    }
    
    /** Fires when non-constructed nodes has been removed.
     * @param add is add or remove
     * @param indices the indicies that changed
     * @param previous snaphost of the state before this event happened or null
     */
    NodeMemberEvent(Node n, boolean add, int[] indices, List<Node> current, List<Node> previous) {
        super(n);
        this.add = add;
        this.indices = indices;
        Arrays.sort(this.indices);
        this.currSnapshot = current;
        this.prevSnapshot = previous;
    }
    
    List<Node> getPrevSnapshot() {
        return prevSnapshot == null ? currSnapshot : prevSnapshot;
    }
    
    /** Get a list of children that changed.
    * @return array of nodes that changed
    */
    public final Node[] getDelta() {
        if (delta == null) {
            assert indices != null : "Well, indices cannot be null now"; // NOI18N
            List<Node> l = getPrevSnapshot();

            Node[] arr = new Node[indices.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = l.get(indices[i]);
            }
            delta = arr;
        }
        return delta;
    }

    /** Get an array of indices of the changed nodes.
    * @return array with the same length as {@link #getDelta}
    */
    public synchronized int[] getDeltaIndices() {
        if (indices != null) {
            return indices;
        }

        List<Node> nodes = getPrevSnapshot();

        List<Node> list = Arrays.asList(delta);
        Set<Node> set = new HashSet<Node>(list);

        indices = new int[delta.length];

        int j = 0;
        int i = 0;

        while ((i < nodes.size()) && (j < indices.length)) {
            if (set.contains(nodes.get(i))) {
                indices[j++] = i;
            }

            i++;
        }

        if (j != delta.length) {
            StringBuilder m = new StringBuilder(1000);
            m.append("Some of a set of deleted nodes are not present in the original ones.\n"); // NOI18N
            m.append("See #15478; you may need to check that your Children.Keys keys are safely comparable."); // NOI18N
            m.append("\ni: ").append(i); // NOI18N
            m.append("\nj: ").append(j); // NOI18N
            m.append("\nThis: ").append(this); // NOI18N
            m.append("\nCurrent state:\n"); // NOI18N
            m.append(nodes);
            m.append("\nDelta:\n"); // NOI18N
            m.append(list);
            throw new IllegalStateException(m.toString());
        }

        return indices;
    }

    /** Human presentable information about the event */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append("[node="); // NOI18N
        sb.append(getSource());
        sb.append(", add="); // NOI18N
        sb.append(isAddEvent());

        Node[] deltaNodes = delta;
        int[] deltaIndices = getDeltaIndices();

        for (int i = 0; i < deltaIndices.length; i++) {
            sb.append("\n  "); // NOI18N
            sb.append(i);
            sb.append(" at "); // NOI18N
            sb.append(deltaIndices[i]);
            if (deltaNodes != null) {
                sb.append(" = "); // NOI18N
                sb.append(deltaNodes[i]);
            }
        }

        sb.append("\n]"); // NOI18N
        sb.append("\ncurr. snapshot: " + currSnapshot.getClass().getName()); // NOI18N
        sb.append("\n" + currSnapshot); // NOI18N
        sb.append("\nprev. snapshot: " + getPrevSnapshot().getClass().getName()); // NOI18N
        sb.append("\n" + getPrevSnapshot()); // NOI18N

        return sb.toString();
    }
}
