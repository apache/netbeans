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
package org.netbeans.modules.db.dataview.table;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;
import org.netbeans.modules.db.dataview.util.DataViewUtils;

public class ResultSetTableCellEditor extends DefaultCellEditor {

    protected Object val;
    protected JTable table;
    protected static final boolean suppressEditorBorder;

    static {
        boolean suppressBorder = false;
        suppressBorder |= "GTK".equals(UIManager.getLookAndFeel().getID());  //NOI18N
        suppressBorder |= "Nimbus".equals(UIManager.getLookAndFeel().getName());  //NOI18N
        suppressEditorBorder = suppressBorder;
    }

    public ResultSetTableCellEditor(final JTextField textField) {
        super(textField);
        delegate = new EditorDelegate() {

            @Override
            public void setValue(Object value) {
                val = value;
                textField.setText((value != null) ? value.toString() : "");
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                if (evt instanceof MouseEvent) {
                    return ((MouseEvent) evt).getClickCount() >= 2;
                }
                return true;
            }

            @Override
            public Object getCellEditorValue() {
                String txtVal = textField.getText();
                if (val == null && txtVal.equals("")) {
                    return null;
                } else {
                        return txtVal;
                    }
                }
        };

        textField.addActionListener(delegate);
        // #204176 - workarround for MacOS L&F
        textField.setCaret(new DefaultCaret());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (DataViewUtils.isSQLConstantString(value, null)) {
            value = "";
        }
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public ResultSetTableCellEditor(final JCheckBox checkBox) {
        super(checkBox);
        delegate = new EditorDelegate() {

            @Override
            public void setValue(Object value) {
                val = value;
                checkBox.setSelected((value instanceof Boolean) ? ((Boolean) value) : false);
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                if (evt instanceof MouseEvent) {
                    return ((MouseEvent) evt).getClickCount() >= 2;
                }
                return true;
            }

            @Override
            public Object getCellEditorValue() {
                Boolean bolVal = checkBox.isSelected();
                if (val == null && !checkBox.isSelected()) {
                    return null;
                } else {
                    return bolVal;
                }
            }
        };

        checkBox.addActionListener(delegate);
    }
}
