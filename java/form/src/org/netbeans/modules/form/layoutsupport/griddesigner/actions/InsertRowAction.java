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

package org.netbeans.modules.form.layoutsupport.griddesigner.actions;

import javax.swing.Action;
import org.netbeans.modules.form.layoutsupport.griddesigner.DesignerContext;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridInfoProvider;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridManager;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridUtils;
import org.openide.util.NbBundle;

/**
 * Action that inserts a new (default) row before or after the focused row.
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public class InsertRowAction extends AbstractGridAction {
    private String name;
    /** Determines whether the new row should be inserted before of after the focused row. */
    private boolean insertAfter;

    public InsertRowAction(boolean insertAfter) {
        String key = "InsertRow" + (insertAfter ? "After" : "Before") + "Action_Name";  // NOI18N
        name = NbBundle.getMessage(InsertRowAction.class, key);
        this.insertAfter = insertAfter;
    }

    @Override
    public Object getValue(String key) {
        return key.equals(Action.NAME) ? name : null;
    }

    @Override
    public boolean isEnabled(DesignerContext context) {
        return (context.getFocusedRow() != -1);
    }

    @Override
    public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
        GridInfoProvider gridInfo = gridManager.getGridInfo();
        boolean gapSupport = gridInfo.hasGaps();
        int[] originalColumnBounds = gridInfo.getColumnBounds();
        int[] originalRowBounds = gridInfo.getRowBounds();

        GridUtils.removePaddingComponents(gridManager);

        int row = context.getFocusedRow();
        if (insertAfter) {
            row += (gapSupport ? 2 : 1);
        }
        gridManager.insertRow(row);

        GridUtils.addPaddingComponents(gridManager, originalColumnBounds.length - 1, originalRowBounds.length - 1 + (gapSupport ? 2 : 1));
        GridUtils.revalidateGrid(gridManager);

        int[] newColumnBounds = gridInfo.getColumnBounds();
        int[] newRowBounds = gridInfo.getRowBounds();
        int[] oldRowBounds = new int[originalRowBounds.length + (gapSupport ? 2 : 1)];
        if(gapSupport) {
            if(originalRowBounds.length == row) {
                // inserting after bottommost row
                System.arraycopy(originalRowBounds, 0, oldRowBounds, 0, row);
                oldRowBounds[row] = oldRowBounds[row - 1];
                oldRowBounds[row + 1] = oldRowBounds[row - 1];
            } else {
                System.arraycopy(originalRowBounds, 0, oldRowBounds, 0, row + 1);
                oldRowBounds[row + 1] = oldRowBounds[row];
                oldRowBounds[row + 2] = oldRowBounds[row];
                System.arraycopy(originalRowBounds, row + 1, oldRowBounds, row + 3, originalRowBounds.length - row - 1);
            }
        } else {
            System.arraycopy(originalRowBounds, 0, oldRowBounds, 0, row + 1);
            oldRowBounds[row + 1] = oldRowBounds[row];
            System.arraycopy(originalRowBounds, row + 1, oldRowBounds, row + 2, originalRowBounds.length - row - 1);
        }
        return new GridBoundsChange(originalColumnBounds, oldRowBounds, newColumnBounds, newRowBounds);
    }

}
