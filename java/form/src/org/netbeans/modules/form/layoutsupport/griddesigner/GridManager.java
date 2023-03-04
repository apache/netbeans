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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;

/**
 * Manager of a specific grid. It provides information about the grid
 * and supports some basic modifications of the grid.
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public interface GridManager {

    /**
     * Returns the managed container (i.e., the container with the grid).
     *
     * @return the managed container.
     */
    Container getContainer();

    /**
     * Returns provider of information about the grid.
     *
     * @return provider of information about the grid.
     */
    GridInfoProvider getGridInfo();

    /**
     * Sets grid X coordinate of the specified component.
     *
     * @param component component whose grid X coordinate should be changed.
     * @param gridX new grid X coordinate of the specified component.
     */
    void setGridX(Component component, int gridX);

    /**
     * Sets grid Y coordinate of the specified component.
     *
     * @param component component whose grid Y cooridnate should be changed.
     * @param gridY new grid Y coordinate of the specified component.
     */
    void setGridY(Component component, int gridY);

    /**
     * Sets grid width of the specified component.
     *
     * @param component component whose grid width should be changed.
     * @param gridWidth new grid width of the specified component.
     */
    void setGridWidth(Component component, int gridWidth);

    /**
     * Sets grid height of the specified component.
     *
     * @param component component whose grid height should be changed.
     * @param gridHeight new grid height of the specified component.
     */
    void setGridHeight(Component component, int gridHeight);

    /**
     * Sets the position of the component in the grid. This method is called
     * when several position properties (i.e., gridX, gridY, etc.) may be
     * modified at once. The difference between invocation of this method
     * and a sequence of the specific methods (like {@code setGridX})
     * is that some {@code GridManager} may benefit from the knowledge
     * that several changes belong together. For example, this method
     * helps {@code GridBagManager} to keep special values like {@code REMAINDER}
     * width/height where it is appropriate.
     * 
     * @param component component whose grid position should be changed.
     * @param gridX new grid X coordinate of the specified component.
     * @param gridY new grid Y coordinate of the specified component.
     * @param gridWidth new grid width of the specified component.
     * @param gridHeight  new grid height of the specified component.
     */
    void setGridPosition(Component component, int gridX, int gridY, int gridWidth, int gridHeight);

    /**
     * Adds a component with the specified grid location.
     *
     * @param component component to add.
     * @param gridX grid X coordinate of the added component.
     * @param gridY grid Y coordinate of the added component.
     * @param gridWidth grid width of the added component.
     * @param gridHeight grid height of the added component.
     */
    void addComponent(Component component, int gridX, int gridY, int gridWidth, int gridHeight);

    /**
     * Removes the specified component from the grid (and the container).
     *
     * @param component component to remove.
     */
    void removeComponent(Component component);

    /**
     * Inserts a new column (with the default properties) on the specified index.
     *
     * @param newColumnIndex index of the added column.
     */
    void insertColumn(int newColumnIndex);

    /**
     * Deletes a specified column.
     *
     * @param columnIndex index of the column to delete.
     */
    void deleteColumn(int columnIndex);

    /**
     * Inserts a new row (with the default properties) on the specified index.
     *
     * @param newRowIndex index of the added row.
     */
    void insertRow(int newRowIndex);

    /**
     * Deletes a specified row.
     *
     * @param rowIndex index of the row to delete.
     */
    void deleteRow(int rowIndex);

    /**
     * Adds gaps between rows and columns.
     *
     * @param gapWidth gap column width (typically in pixels).
     * @param gapHeight gap row height (typically in pixels).
     */
    void addGaps(int gapWidth, int gapHeight);

    /**
     * Updates gaps either to accomodate layout changes or to change gap sizes.
     * Optionally modifies positions and sizes of components to accomodate
     * edits outside grid designer (e.g., in free form) that may have broken gap consistency
     *
     * @param updateComponents if true, updates also component position/sizes.
     */
    void updateGaps(boolean updateComponents);

    /**
     * Removes gaps from between rows/columns.
     *
     */
    void removeGaps();

    /**
     * Encloses given set of components in a new container.
     * 
     * @param components set of subcomponents of the managed container.
     * @return the enclosing container.
     */
    Container encloseInContainer(Set<Component> components);

    /**
     * Updates the layout from the model. The modification methods
     * modify the underlying model only. If you want to propagate
     * these changes into the managed container then you have
     * to call this method.
     *
     * Several modification methods are followed by a single invocation
     * of this method in a typical use-case.
     * 
     * @param includingSubcontainers determines whether it is sufficient
     * to update the layout of the managed container or whether
     * also subcontainers should be updated.
     */
    void updateLayout(boolean includingSubcontainers);

    /**
     * Returns designer actions for the specified context
     * (component, row, column or grid).
     *
     * @param context context of the action (component, row, column or grid).
     * @return designer actions for the specified context.
     */
    List<GridAction> designerActions(GridAction.Context context);
    
    GridCustomizer getCustomizer(GridActionPerformer performer);

}