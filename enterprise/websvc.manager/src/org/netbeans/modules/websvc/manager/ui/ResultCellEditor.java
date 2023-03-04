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


package org.netbeans.modules.websvc.manager.ui;

/**
 *
 * @author  David Botterill
 */

import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTextField;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import java.awt.Dialog;

/**
 *
 * @author  david
 */
public class ResultCellEditor extends DefaultCellEditor implements TableCellEditor {

    private Dialog dialog;
    private DialogDescriptor dlg;
    private ResultViewerDialog viewerDialog;
    private Object saveValue;

    /** Creates a new instance of TypeCellRenderer */
    public ResultCellEditor() {
        super(new JTextField());
        this.setClickCountToStart(1);
    }
    /**
     * return the value of the last component.
     */
    @Override
    public Object getCellEditorValue() {
        return saveValue;
    }

    @Override
    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
        saveValue = value;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)table.getModel().
                getValueAt(row, 0);
        
        /**
         * Now depending on the type, create a component to edit/display the type.
         */
        viewerDialog = new ResultViewerDialog();
        if(null == node.getUserObject()) {
            viewerDialog.setText((String)value);

        } else {
            TypeNodeData data = (TypeNodeData)node.getUserObject();
            viewerDialog.setText((value == null) ? "null" : value.toString()); // NOI18N

            dlg = new DialogDescriptor(viewerDialog, data.getRealTypeName(),
            true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN, viewerDialog.getHelpCtx(), null);
            dlg.setOptions(new Object[] { viewerDialog.getOkButton() });

            dialog = DialogDisplayer.getDefault().createDialog(dlg);
            dialog.setSize(300,300);
            dialog.setVisible(true);
        }


        return null;
    }

}
