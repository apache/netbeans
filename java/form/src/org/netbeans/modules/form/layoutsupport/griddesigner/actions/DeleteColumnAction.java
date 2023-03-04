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
 * Action that deletes the focused column.
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public class DeleteColumnAction extends AbstractGridAction {
    private String name;

    public DeleteColumnAction() {
        name = NbBundle.getMessage(DeleteColumnAction.class, "DeleteColumnAction_Name"); // NOI18N
    }

    @Override
    public Object getValue(String key) {
        return key.equals(Action.NAME) ? name : null;
    }

    @Override
    public boolean isEnabled(DesignerContext context) {
        return (context.getFocusedColumn() != -1) && (context.getGridInfo().getColumnCount() > 1);
    }

    @Override
    public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
        GridInfoProvider gridInfo = gridManager.getGridInfo();
        boolean gapSupport = gridInfo.hasGaps();
        int[] originalColumnBounds = gridInfo.getColumnBounds();
        int[] originalRowBounds = gridInfo.getRowBounds();
        int column = context.getFocusedColumn();

        GridUtils.removePaddingComponents(gridManager);
        gridManager.deleteColumn(column);
        GridUtils.addPaddingComponents(gridManager, originalColumnBounds.length - 1 -(gapSupport ? 2 : 1), originalRowBounds.length - 1);
        GridUtils.revalidateGrid(gridManager);

        int[] newColumnBounds = gridInfo.getColumnBounds();
        int[] newRowBounds = gridInfo.getRowBounds();
        int[] columnBounds;
        if (column == originalColumnBounds.length - 2) {
            // The last column deleted
            columnBounds = newColumnBounds;
        } else {
            columnBounds = new int[newColumnBounds.length + (gapSupport ? 2 : 1)];
            if(gapSupport) {
                System.arraycopy(newColumnBounds, 0, columnBounds, 0, column + 1);
                columnBounds[column + 1]=columnBounds[column];
                columnBounds[column + 2]=columnBounds[column];
                System.arraycopy(newColumnBounds, column + 1, columnBounds, column + 3, newColumnBounds.length - column - 1);
            } else {
                System.arraycopy(newColumnBounds, 0, columnBounds, 0, column + 1);
                columnBounds[column + 1]=columnBounds[column];
                System.arraycopy(newColumnBounds, column + 1, columnBounds, column + 2, newColumnBounds.length - column - 1);
            }
        }
        return new GridBoundsChange(originalColumnBounds, originalRowBounds, columnBounds, newRowBounds);
    }

}
