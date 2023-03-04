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
import java.util.BitSet;
import java.util.Set;

/**
 * Various designer-related aspects that can affect whether some
 * action is enabled or not.
 *
 * @author Jan Stola
 */
public class DesignerContext {
    /** Selected columns. */
    private BitSet selectedColumns;
    /** Selected rows. */
    private BitSet selectedRows;
    /** Selected components. */
    private Set<Component> selectedComponents;
    /** Focused component (if the action is invoked on a component). */
    private Component focusedComponent;
    /** Focused column (if the action is invoked on a column). */
    private int focusedColumn = -1;
    /** Focused row (if the action is invoked on a row). */
    private int focusedRow = -1;
    /** Grid information. */
    private GridInfoProvider gridInfo;

    /**
     * Returns grid information.
     *
     * @return grid information.
     */
    public GridInfoProvider getGridInfo() {
        return gridInfo;
    }

    /**
     * Sets grid information.
     *
     * @param gridInfo grid information.
     */
    public void setGridInfo(GridInfoProvider gridInfo) {
        this.gridInfo = gridInfo;
    }

    /**
     * Returns selected columns.
     *
     * @return selected columns.
     */
    public BitSet getSelectedColumns() {
        return selectedColumns;
    }

    /**
     * Sets selected columns.
     *
     * @param selectedColumns selected columns.
     */
    public void setSelectedColumns(BitSet selectedColumns) {
        this.selectedColumns = selectedColumns;
    }

    /**
     * Returns selected rows.
     *
     * @return selected rows.
     */
    public BitSet getSelectedRows() {
        return selectedRows;
    }

    /**
     * Sets selected rows.
     *
     * @param selectedRows selected rows.
     */
    public void setSelectedRows(BitSet selectedRows) {
        this.selectedRows = selectedRows;
    }

    /**
     * Returns selected components.
     *
     * @return sected components.
     */
    public Set<Component> getSelectedComponents() {
        return selectedComponents;
    }

    /**
     * Sets selected components.
     *
     * @param selectedComponents selected components.
     */
    public void setSelectedComponents(Set<Component> selectedComponents) {
        this.selectedComponents = selectedComponents;
    }

    /**
     * Returns focused component (when the action is invoked on a component).
     *
     * @return focused component if the action is invoked on a component,
     * returns {@code null} otherwise.
     */
    public Component getFocusedComponent() {
        return focusedComponent;
    }

    /**
     * Sets focused component.
     *
     * @param focusedComponent focused component.
     */
    public void setFocusedComponent(Component focusedComponent) {
        this.focusedComponent = focusedComponent;
    }

    /**
     * Returns focused column (when the action is invoked on a column).
     *
     * @return focused component if the action is invoked on a column,
     * returns {@code null} otherwise.
     */
    public int getFocusedColumn() {
        return focusedColumn;
    }

    /**
     * Sets focused column.
     *
     * @param focusedColumn focused column.
     */
    public void setFocusedColumn(int focusedColumn) {
        this.focusedColumn = focusedColumn;
    }

    /**
     * Returns focused row (when the action is invoked on a row).
     *
     * @return focused row if the action is invoked on a row,
     * returns {@code null} otherwise.
     */
    public int getFocusedRow() {
        return focusedRow;
    }

    /**
     * Sets focused row.
     *
     * @param focusedRow focused row.
     */
    public void setFocusedRow(int focusedRow) {
        this.focusedRow = focusedRow;
    }

}
