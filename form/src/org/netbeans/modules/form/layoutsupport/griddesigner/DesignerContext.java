/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
