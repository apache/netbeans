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

import java.awt.Component;
import javax.swing.Action;
import org.netbeans.modules.form.layoutsupport.griddesigner.DesignerContext;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridInfoProvider;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridManager;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridUtils;
import org.openide.util.NbBundle;

/**
 * Action that deletes the content of the focused row
 * (i.e., all components that intersect the focused row).
 *
 * @author Jan Stola
 */
public class DeleteRowContentAction extends AbstractGridAction {
    private String name;

    public DeleteRowContentAction() {
        name = NbBundle.getMessage(DeleteRowContentAction.class, "DeleteRowContentAction_Name"); // NOI18N
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
        GridInfoProvider info = gridManager.getGridInfo();
        int columns = info.getColumnCount();
        int rows = info.getRowCount();
        GridUtils.removePaddingComponents(gridManager);

        GridInfoProvider gridInfo = gridManager.getGridInfo();
        int row = context.getFocusedRow();
        for (Component component : gridManager.getContainer().getComponents()) {
            int y = gridInfo.getGridY(component);
            int height = gridInfo.getGridHeight(component);
            if (y<=row && row<y+height) {
                gridManager.removeComponent(component);
            }
        }

        GridUtils.addPaddingComponents(gridManager, columns, rows);
        GridUtils.revalidateGrid(gridManager);
        return null;
    }

}
