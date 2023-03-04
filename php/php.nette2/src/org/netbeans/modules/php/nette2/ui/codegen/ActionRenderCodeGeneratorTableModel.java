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
package org.netbeans.modules.php.nette2.ui.codegen;

import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.php.nette2.codegen.ActionRenderMethodChecker;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ActionRenderCodeGeneratorTableModel  extends DefaultTableModel {
    private static final String ACTION_COLUMN_TITLE = "action<action>()"; //NOI18N
    private static final String RENDER_COLUMN_TITLE = "render<action>()"; //NOI18N
    private final ActionRenderMethodChecker methodChecker;
    private static final Class[] TYPES = new Class[] {
        String.class, Boolean.class, Boolean.class
    };

    @NbBundle.Messages("LBL_ActionName=Action name:")
    public ActionRenderCodeGeneratorTableModel(ActionRenderMethodChecker methodChecker) {
        super(
                null,
                new String[]{
                    Bundle.LBL_ActionName(),
                    ACTION_COLUMN_TITLE,
                    RENDER_COLUMN_TITLE
                });
        this.methodChecker = methodChecker;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return TYPES[columnIndex];
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        String action = (String) getValueAt(row, 0);
        switch (column) {
            case 1:
                if (methodChecker.existsActionMethod(action)) {
                    super.setValueAt(false, row, column);
                } else {
                    super.setValueAt(aValue, row, column);
                }
                break;
            case 2:
                if (methodChecker.existsRenderMethod(action)) {
                    super.setValueAt(false, row, column);
                } else {
                    super.setValueAt(aValue, row, column);
                }
                break;
            default:
                super.setValueAt(aValue, row, column);
        }
    }
}
