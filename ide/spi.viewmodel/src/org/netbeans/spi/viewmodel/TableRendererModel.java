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

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Model that provides custom cell renderer and cell editor for table cells.
 * 
 * @author Martin Entlicher
 * @since 1.28
 */
public interface TableRendererModel extends Model {

    /**
     * Test whether this renderer can render the given cell.
     * @param node Tree node representing the row
     * @param columnID The column name
     * @return <code>true</code> if the implementation can render the given cell, <code>false</code> otherwise
     * @throws UnknownTypeException If the implementation can not decide whether to render the given cell.
     */
    public boolean canRenderCell(Object node, String columnID) throws UnknownTypeException;

    /**
     * Get the renderer of the given cell
     * @param node Tree node representing the row
     * @param columnID The column name
     * @return The cell renderer
     * @throws UnknownTypeException If the implementation can not render the given cell.
     */
    public TableCellRenderer getCellRenderer(Object node, String columnID) throws UnknownTypeException;

    /**
     * Test whether this renderer can edit the given cell.
     * @param node Tree node representing the row
     * @param columnID The column name
     * @return <code>true</code> if the implementation can edit the given cell, <code>false</code> otherwise
     * @throws UnknownTypeException If the implementation can not decide whether to edit the given cell.
     */
    public boolean canEditCell(Object node, String columnID) throws UnknownTypeException;

    /**
     * Get the editor of the given cell
     * @param node Tree node representing the row
     * @param columnID The column name
     * @return The cell editor
     * @throws UnknownTypeException If the implementation can not edit the given cell.
     */
    public TableCellEditor getCellEditor(Object node, String columnID) throws UnknownTypeException;

    /**
     * Registers given listener.
     *
     * @param l the listener to add
     */
    public abstract void addModelListener (ModelListener l);

    /**
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public abstract void removeModelListener (ModelListener l);
}
