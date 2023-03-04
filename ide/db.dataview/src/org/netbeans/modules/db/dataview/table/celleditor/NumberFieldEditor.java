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
package org.netbeans.modules.db.dataview.table.celleditor;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.table.ResultSetTableCellEditor;
import org.netbeans.modules.db.dataview.table.ResultSetTableModel;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.openide.util.Exceptions;

public class NumberFieldEditor extends ResultSetTableCellEditor {
    private final JTextField textField;
    private DBColumn dbColumn;
    private Object oldValue;
    private final InputVerifier verifier = new InputVerifier() {
        @Override
        public boolean verify(JComponent input) {
            if (dbColumn != null && input instanceof JTextComponent) {
                String inputText = ((JTextComponent) input).getText();
                try {
                    DBReadWriteHelper.validate(inputText, dbColumn);
                } catch (DBException ex) {
                    return false;
                }
                return true;
            } else {
                return true;
            }
        }
    };

    public NumberFieldEditor(final JTextField textField) {
        super(textField);
        this.textField = textField;
        ((JTextField) getComponent()).setHorizontalAlignment(JTextField.RIGHT);
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        oldValue = value;
        int modelColumn = table.convertColumnIndexToModel(column);
        TableModel tm = table.getModel();
        dbColumn = null;
        if (tm instanceof ResultSetTableModel) {
            textField.setInputVerifier(verifier);
            dbColumn = ((ResultSetTableModel) tm).getColumn(modelColumn);
        } else {
            textField.setInputVerifier(null);
        }
        Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        if (suppressEditorBorder && c instanceof JComponent) {
            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
        }
        return c;
    }

    /**
     * Override getCellEditorValue to build a number.
     */
    @Override
    public Object getCellEditorValue() {
        try {
            Object superVal = super.getCellEditorValue();
            if (dbColumn != null) {
                try {
                    return DBReadWriteHelper.validate(superVal, dbColumn);
                } catch (DBException ex) {
                    Exceptions.printStackTrace(ex);
                    return oldValue;
                }
            } else {
                return superVal;
            }
        } finally {
            oldValue = null;
        }
    }

    @Override
    public boolean stopCellEditing() {
        try {
            Object value = super.getCellEditorValue();
            DBReadWriteHelper.validate(value, dbColumn);
            return super.stopCellEditing();
        } catch (DBException ex) {
            return false;
        }
    }

    @Override
    public void cancelCellEditing() {
        oldValue = null;
        super.cancelCellEditing();
    }
}
