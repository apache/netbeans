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
package org.openide.nodes;

import java.util.List;


/** Event describing change in the list of a node's children.
*
* @author Jaroslav Tulach
*/
public final class NodeReorderEvent extends NodeEvent {
    static final long serialVersionUID = 4479234495493767448L;

    /** list of new nodes indexes on the original positions */
    private int[] newIndices;

    /** current snapshot */
    private final List<Node> currSnapshot;    

    /** Package private constructor to allow construction only
    * @param n the node that has changed
    * @param newIndices new indexes of the nodes
    */
    NodeReorderEvent(Node n, int[] newIndices) {
        super(n);
        this.newIndices = newIndices;
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
    
    /** Get the new position of the child that had been at a given position.
    * @param i the original position of the child
    * @return the new position of the child
    */
    public int newIndexOf(int i) {
        return newIndices[i];
    }

    /** Get the permutation used for reordering.
    * @return array of integers used for reordering
    */
    public int[] getPermutation() {
        return newIndices;
    }

    /** Get the number of children reordered.
     * @return size of the permutation array */
    public int getPermutationSize() {
        return newIndices.length;
    }

    /** Human presentable information about the event */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[node="); // NOI18N
        sb.append(getSource());
        sb.append(", permutation = ("); // NOI18N

        int[] perm = getPermutation();

        for (int i = 0; i < perm.length;) {
            sb.append(perm[i]);

            if (++i < perm.length) {
                sb.append(", "); // NOI18N
            }
        }

        sb.append(")]"); // NOI18N

        return sb.toString();
    }
}
