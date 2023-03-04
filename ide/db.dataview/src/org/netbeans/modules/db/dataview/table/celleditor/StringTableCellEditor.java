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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.db.dataview.table.ResultSetTableCellEditor;
import org.openide.windows.WindowManager;

public class StringTableCellEditor extends ResultSetTableCellEditor implements TableCellEditor, ActionListener {

    private final JButton customEditorButton = new JButton("...");
    private JTextComponent tc;
    private TableModel tableModel;
    private String columnName;
    private int modelRow, modelColumn;
    
    public StringTableCellEditor(final JTextField textField) {
        super(textField);
        customEditorButton.addActionListener(this);

        // ui-tweaking
        customEditorButton.setFocusable(false);
        customEditorButton.setFocusPainted(false);
        customEditorButton.setMargin(new Insets(0, 0, 0, 0));
        customEditorButton.setPreferredSize(new Dimension(20, 10));
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, final int column) {
        final JComponent c = (JComponent) super.getTableCellEditorComponent(table, value, isSelected, row, column);      
        
        this.tableModel = table.getModel();
        this.columnName = table.getColumnName(column);
        this.modelRow = table.convertRowIndexToModel(row);
        this.modelColumn = table.convertColumnIndexToModel(column);  
        this.tc = c instanceof JTextComponent ? (JTextComponent) c : null;

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            public void addNotify() {
                super.addNotify();
                c.requestFocus();
            }
        };
        panel.add(c);
        if (suppressEditorBorder) {
            c.setBorder(BorderFactory.createEmptyBorder());
        }
        panel.add(customEditorButton, BorderLayout.EAST);
        panel.revalidate();
        panel.repaint();

        return panel;
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        super.cancelCellEditing();
        editCell();
    }

    private void editCell() {
        assert tc != null : "String TableCellEditor called without a TextComponent";
        
        JTextArea textArea = new JTextArea(20, 80);
        // Work aroung JDK bugs 7027598 (this bug suggests this work-around) #233347
        textArea.setDropTarget(null);
        boolean editable = tableModel.isCellEditable(modelRow, modelColumn);

        textArea.setText(tc.getText());
        textArea.setCaretPosition(0);
        textArea.setEditable(editable);
        
        JScrollPane pane = new JScrollPane(textArea);
        pane.addHierarchyListener(new MakeResizableListener(pane));
        Component parent = WindowManager.getDefault().getMainWindow();

        if (editable) {
            int result = JOptionPane.showOptionDialog(parent, pane, columnName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                tableModel.setValueAt(textArea.getText(), modelRow, modelColumn);
            }
        } else {
            JOptionPane.showMessageDialog(parent, pane, columnName, JOptionPane.PLAIN_MESSAGE, null);
        }
    }

    /**
     * Hack to make JOptionPane resizable.
     * https://blogs.oracle.com/scblog/entry/tip_making_joptionpane_dialog_resizable
     */
    static class MakeResizableListener implements HierarchyListener {

        private Component pane;

        public MakeResizableListener(Component pane) {
            this.pane = pane;
        }

        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            Window window = SwingUtilities.getWindowAncestor(pane);
            if (window instanceof Dialog) {
                Dialog dialog = (Dialog) window;
                if (!dialog.isResizable()) {
                    dialog.setResizable(true);
                }
            }
        }
    }
}
