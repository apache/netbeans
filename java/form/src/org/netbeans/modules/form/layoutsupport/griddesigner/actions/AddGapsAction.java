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
import org.netbeans.modules.form.FormLoaderSettings;
import org.netbeans.modules.form.layoutsupport.griddesigner.DesignerContext;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridInfoProvider;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridManager;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridUtils;
import org.openide.util.NbBundle;

/**
 * Action that adds gaps between rows and columns.
 *
 * @author Petr Somol
 * @author Jan Stola
 */
public class AddGapsAction extends AbstractGridAction {
    private String name;

    public AddGapsAction() {
        name = NbBundle.getMessage(AddGapsAction.class, "AddGapsAction_Name"); // NOI18N
    }

    @Override
    public Object getValue(String key) {
        return key.equals(Action.NAME) ? name : null;
    }

    @Override
    public boolean isEnabled(DesignerContext context) {
        return !context.getGridInfo().hasGaps();
    }

    @Override
    public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
        GridInfoProvider gridInfo = gridManager.getGridInfo();
        int[] originalColumnBounds = gridInfo.getColumnBounds();
        int[] originalRowBounds = gridInfo.getRowBounds();
        int columnCount = Math.max( 2 * originalColumnBounds.length - 3, 1);
        int rowCount = Math.max(2 * originalRowBounds.length - 3, 1);

        GridUtils.removePaddingComponents(gridManager);
        int gapWidth = FormLoaderSettings.getInstance().getGapWidth();
        int gapHeight = FormLoaderSettings.getInstance().getGapHeight();
        gridManager.addGaps(gapWidth, gapHeight);
        GridUtils.addPaddingComponents(gridManager, columnCount, rowCount);
        GridUtils.revalidateGrid(gridManager);

        int[] newColumnBounds = gridInfo.getColumnBounds();
        int[] newRowBounds = gridInfo.getRowBounds();
        int[] columnBounds;
        int[] rowBounds;

        columnBounds = new int[newColumnBounds.length];
        columnBounds[0] = originalColumnBounds[0];
        for(int i=1; i<originalColumnBounds.length - 1; i++) {
            columnBounds[2 * i] = columnBounds[2 * i - 1] = originalColumnBounds[i];
        }
        columnBounds[newColumnBounds.length - 1] = originalColumnBounds[originalColumnBounds.length - 1];
        rowBounds = new int[newRowBounds.length];
        rowBounds[0] = originalRowBounds[0];
        for(int j=1; j<originalRowBounds.length - 1; j++) {
            rowBounds[2 * j] = rowBounds[2 * j - 1] = originalRowBounds[j];
        }
        rowBounds[newRowBounds.length - 1] = originalRowBounds[originalRowBounds.length - 1];
        return new GridBoundsChange(columnBounds, rowBounds, newColumnBounds, newRowBounds);
    }

}
