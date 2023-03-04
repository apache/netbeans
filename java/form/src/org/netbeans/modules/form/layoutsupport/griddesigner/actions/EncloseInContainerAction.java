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
import java.util.Collections;
import javax.swing.Action;
import org.netbeans.modules.form.layoutsupport.griddesigner.DesignerContext;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridInfoProvider;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridManager;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridUtils;
import org.openide.util.NbBundle;

/**
 * Action that encloses the selected components in a separate container.
 *
 * @author Jan Stola
 */
public class EncloseInContainerAction extends AbstractGridAction {
    private String name;

    public EncloseInContainerAction() {
        name = NbBundle.getMessage(EncloseInContainerAction.class, "EncloseInContainerAction_Name"); // NOI18N
    }

    @Override
    public Object getValue(String key) {
        return key.equals(Action.NAME) ? name : null;
    }

    @Override
    public boolean isEnabled(DesignerContext context) {
        return !context.getSelectedComponents().isEmpty();
    }

    @Override
    public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
        GridInfoProvider info = gridManager.getGridInfo();
        int columns = info.getColumnCount();
        int rows = info.getRowCount();
        GridUtils.removePaddingComponents(gridManager);

        Component container = gridManager.encloseInContainer(context.getSelectedComponents());
        context.setSelectedComponents(Collections.singleton(container));

        GridUtils.addPaddingComponents(gridManager, columns, rows);
        GridUtils.revalidateGrid(gridManager);
        return null;
    }

}
