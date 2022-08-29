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

package org.netbeans.spi.viewmodel;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.openide.util.datatransfer.PasteType;

/**
 * Extension of {@link NodeModelFilter} with support for Drag and Drop of nodes.
 *
 * @author Martin Entlicher
 * @since 1.24
 */
public interface DnDNodeModelFilter extends NodeModelFilter {

    /**
     * Action constants from {@link java.awt.dnd.DnDConstants}.
     * No actions are allowed by default.
     * @param original The original DnDNodeModel to filter
     * @return int representing set of actions which are allowed when dragging from
     * asociated component.
     */
    int getAllowedDragActions(DnDNodeModel original);

    /**
     * Action constants from {@link java.awt.dnd.DnDConstants}.
     * No actions are allowed by default.
     * @param original The original DnDNodeModel to filter
     * @param t The transferable for which the allowed drop actions are requested,
     *          or <code>null</code> to get actions for the creation of DropTarget for the view.
     * @return int representing set of actions which are allowed when dropping
     * the transferable into the asociated component.
     */
    int getAllowedDropActions(DnDNodeModel original, Transferable t);

    /**
     * Initiate a drag operation.
     * @param original The original DnDNodeModel to filter
     * @param node The node to drag
     * @return transferable to represent this node during a drag
     * @throws IOException when the drag cannot be performed
     * @throws UnknownTypeException if this model implementation is not
     *          able to perform drag for given node type
     */
    Transferable drag(DnDNodeModel original, Object node) throws IOException, UnknownTypeException;

    /**
     * Determines if there is a paste operation that can be performed
     * on provided transferable when drop is done.
     *
     * @param original The original DnDNodeModel to filter
     * @param object The node where to drop
     * @param t the transferable to drop
     * @param action the Drag and Drop action from {@link java.awt.dnd.DnDConstants}
     * @param index index between children the drop occured at or -1 if not specified
     * @return the paste type or <code>null</code> when the transferable cannot be accepted
     * @throws UnknownTypeException if this model implementation is not
     *          able to perform drop for given node type
     */
    PasteType getDropType(DnDNodeModel original, Object object, Transferable t, int action, int index)
            throws UnknownTypeException;

}
