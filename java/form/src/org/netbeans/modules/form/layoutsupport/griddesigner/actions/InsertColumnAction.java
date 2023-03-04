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
 * Action that inserts a new (default) column before or after the focused column.
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public class InsertColumnAction extends AbstractGridAction {
    private String name;
    /** Determines whether the new column should be inserted before of after the focused column. */
    private boolean insertAfter;

    public InsertColumnAction(boolean insertAfter) {
        String key = "InsertColumn" + (insertAfter ? "After" : "Before") + "Action_Name";  // NOI18N
        name = NbBundle.getMessage(InsertColumnAction.class, key);
        this.insertAfter = insertAfter;
    }

    @Override
    public Object getValue(String key) {
        return key.equals(Action.NAME) ? name : null;
    }

    @Override
    public boolean isEnabled(DesignerContext context) {
        return (context.getFocusedColumn() != -1);
    }

    @Override
    public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
        GridInfoProvider gridInfo = gridManager.getGridInfo();
        boolean gapSupport = gridInfo.hasGaps();
        int[] originalColumnBounds = gridInfo.getColumnBounds();
        int[] originalRowBounds = gridInfo.getRowBounds();

        GridUtils.removePaddingComponents(gridManager);

        int column = context.getFocusedColumn();
        if (insertAfter) {
            column += (gapSupport ? 2 : 1);
        }
        gridManager.insertColumn(column);

        GridUtils.addPaddingComponents(gridManager, originalColumnBounds.length - 1 + (gapSupport ? 2 : 1), originalRowBounds.length - 1);
        GridUtils.revalidateGrid(gridManager);

        int[] newColumnBounds = gridInfo.getColumnBounds();
        int[] newRowBounds = gridInfo.getRowBounds();
        int[] oldColumnBounds = new int[originalColumnBounds.length + (gapSupport ? 2 : 1)];
        if(gapSupport) {
            if(originalColumnBounds.length == column) {
                // inserting after rightmost column
                System.arraycopy(originalColumnBounds, 0, oldColumnBounds, 0, column);
                oldColumnBounds[column] = oldColumnBounds[column - 1];
                oldColumnBounds[column + 1] = oldColumnBounds[column - 1];
            } else {
                System.arraycopy(originalColumnBounds, 0, oldColumnBounds, 0, column + 1);
                oldColumnBounds[column + 1] = oldColumnBounds[column];
                oldColumnBounds[column + 2] = oldColumnBounds[column];
                System.arraycopy(originalColumnBounds, column + 1, oldColumnBounds, column + 3, originalColumnBounds.length - column - 1);
            }
        } else {
            System.arraycopy(originalColumnBounds, 0, oldColumnBounds, 0, column + 1);
            oldColumnBounds[column + 1] = oldColumnBounds[column];
            System.arraycopy(originalColumnBounds, column + 1, oldColumnBounds, column + 2, originalColumnBounds.length - column - 1);
        }
        return new GridBoundsChange(oldColumnBounds, originalRowBounds, newColumnBounds, newRowBounds);
    }

}
