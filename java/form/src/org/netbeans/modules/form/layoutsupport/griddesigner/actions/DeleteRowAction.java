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
 * Action that deletes the focused row.
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public class DeleteRowAction extends AbstractGridAction {
    private String name;

    public DeleteRowAction() {
        name = NbBundle.getMessage(DeleteRowAction.class, "DeleteRowAction_Name"); // NOI18N
    }

    @Override
    public Object getValue(String key) {
        return key.equals(Action.NAME) ? name : null;
    }

    @Override
    public boolean isEnabled(DesignerContext context) {
        return (context.getFocusedRow() != -1) && (context.getGridInfo().getRowCount() > 1);
    }

    @Override
    public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
        GridInfoProvider gridInfo = gridManager.getGridInfo();
        boolean gapSupport = gridInfo.hasGaps();
        int[] originalColumnBounds = gridInfo.getColumnBounds();
        int[] originalRowBounds = gridInfo.getRowBounds();
        int row = context.getFocusedRow();

        GridUtils.removePaddingComponents(gridManager);
        gridManager.deleteRow(row);
        GridUtils.addPaddingComponents(gridManager, originalColumnBounds.length - 1, originalRowBounds.length - 1 - (gapSupport ? 2 : 1));
        GridUtils.revalidateGrid(gridManager);

        int[] newRowBounds = gridInfo.getRowBounds();
        int[] newColumnBounds = gridInfo.getColumnBounds();
        int[] rowBounds;
        if (row == originalRowBounds.length - 2) {
            // The last row deleted
            rowBounds = newRowBounds;
        } else {
            rowBounds = new int[newRowBounds.length + (gapSupport ? 2 : 1)];
            if(gapSupport) {
                System.arraycopy(newRowBounds, 0, rowBounds, 0, row + 1);
                rowBounds[row + 1] = rowBounds[row];
                rowBounds[row + 2] = rowBounds[row];
                System.arraycopy(newRowBounds, row + 1, rowBounds, row + 3, newRowBounds.length - row - 1);
            } else {
                System.arraycopy(newRowBounds, 0, rowBounds, 0, row + 1);
                rowBounds[row + 1] = rowBounds[row];
                System.arraycopy(newRowBounds, row + 1, rowBounds, row + 2, newRowBounds.length - row - 1);
            }
        }
        return new GridBoundsChange(originalColumnBounds, originalRowBounds, newColumnBounds, rowBounds);
    }

}
