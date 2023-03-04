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

package org.netbeans.spi.viewmodel;

/**
 * Filters an original tree data model that supports reordering
 * of child nodes. The created {@link org.openide.nodes.Node} will contain
 * an implementation of {@link org.openide.nodes.Index} in it's lookup if
 * {@link #canReorder(org.netbeans.spi.viewmodel.ReorderableTreeModel, java.lang.Object)}
 * returns <code>true</code>.
 *
 * @author Martin Entlicher
 * @since 1.25
 */
public interface ReorderableTreeModelFilter extends TreeModelFilter {

    /**
     * Provide if this filter implementation can reorder children nodes.
     * @param original The original ReorderableTreeModel
     * @param parent The parent node of children that are test for reorder
     * @return <code>true</code> if this model can handle reordering of children,
     *         <code>false</code> otherwise
     * @throws UnknownTypeException if this model implementation is not
     *         able to decide the reorder capability for given node type
     */
    public boolean canReorder(ReorderableTreeModel original, Object parent) throws UnknownTypeException;

    /**
     * Reorder children nodes with a given permutation.
     * @param parent The parent node of children that are being reordered
     * @param perm permutation with the length of current child nodes. The permutation
     * lists the new positions of the original nodes, that is, for nodes
     * <code>[A,B,C,D]</code> and permutation <code>[0,3,1,2]</code>, the final
     * order would be <code>[A,C,D,B]</code>.
     * @throws IllegalArgumentException if the permutation is not valid
     * @throws UnknownTypeException if this model implementation is not
     *         able to perform the reorder for given node type
     */
    public void reorder(ReorderableTreeModel original, Object parent, int[] perm) throws UnknownTypeException;

}
