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

package org.netbeans.modules.websvc.saas.codegen.ui;

import java.awt.Dialog;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.event.KeyListener;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamStyle;
import org.openide.util.NbBundle;

/**
 *
 * @author  nam
 * 
 * TODO: Need to check to make sure the UriTemplate value is unique after
 * the user modifies it.
 * 
 */
public class CodeSetupPanel extends javax.swing.JPanel {

    private ParamTableModel tableModel;
    private List<ParameterInfo> inputParams;
    private boolean methodNameModified = false;
    private boolean showParamTypes;
    private Dialog dialog;

    /** Creates new form CodeSetupPanel */
    public CodeSetupPanel(List<ParameterInfo> inputParams) { 
        this(inputParams, true);
    }
    
    /** Creates new form CodeSetupPanel */
    public CodeSetupPanel(List<ParameterInfo> inputParams, boolean showParamTypes) { 
        initComponents();
  
        this.inputParams = inputParams;
        this.showParamTypes = showParamTypes;
        tableModel = new ParamTableModel();
        paramTable.setModel(tableModel);
        paramTable.addKeyListener(new TableKeyListener());
    }

    public void setDialog(Dialog d) {
        this.dialog = d;
    }
    
    private class TableKeyListener implements KeyListener {
        
        public TableKeyListener() {
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == 10) //Carriage return
                dialog.dispose();
        }

        public void keyReleased(KeyEvent e) {
        }

    }

    private class ParamTable extends JTable {

        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if(showParamTypes) {
                String paramName = (String) tableModel.getValueAt(row, 0);
                Class type = (column == 2) ? (Class) tableModel.getValueAt(row, 1) : Boolean.class;

                if (Enum.class.isAssignableFrom(type)) {
                    JComboBox combo = new JComboBox(type.getEnumConstants());
                    return new DefaultCellEditor(combo);
                } else if (type == Boolean.class || type == Boolean.TYPE) {
                    JCheckBox cb = new JCheckBox();
                    cb.setHorizontalAlignment(JLabel.CENTER);
                    cb.setBorderPainted(true);
                    return new DefaultCellEditor(cb);
                } else if (paramName.toLowerCase().contains(Constants.PASSWORD)) {
                    return new DefaultCellEditor(new JPasswordField());
                }
            }

            return super.getCellEditor(row, column);
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            if (column != 0) {
                return new ParamCellRenderer();
            }
            return super.getCellRenderer(row, column);
        }
    }

    private class ParamCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            Component ret = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String paramName = (String) tableModel.getValueAt(row, 0);

            if (value == null) {
                return new JLabel(NbBundle.getMessage(CodeSetupPanel.class, "LBL_NotSet"));
            } else if (value instanceof Class) {
                return new JLabel(((Class) value).getName());
            } else if (value instanceof Boolean) {
                JCheckBox cb = new JCheckBox();
                cb.setHorizontalAlignment(JLabel.CENTER);
                cb.setBorderPainted(true);
                cb.setSelected((Boolean) value);
                return cb;
            } else if (paramName.contains(Constants.PASSWORD)) {
                return new JPasswordField((String) value);
            } 
            return ret;
        }
    }

    private class ParamTableModel extends AbstractTableModel {

        public ParamTableModel() {
            if(showParamTypes) {
                columnNames = new String[]{NbBundle.getMessage(CodeSetupPanel.class, "LBL_Name"), NbBundle.getMessage(CodeSetupPanel.class, "LBL_Type"), NbBundle.getMessage(CodeSetupPanel.class, "LBL_DefaultValue")};
                types = new Class[]{String.class, Class.class, Object.class};
                canEdit = new boolean[]{false, false, true};
            } else {
                columnNames = new String[]{NbBundle.getMessage(CodeSetupPanel.class, "LBL_Name"), NbBundle.getMessage(CodeSetupPanel.class, "LBL_DefaultValue")};
                types = new Class[]{String.class, Object.class};
                canEdit = new boolean[]{false, true};
            }
        }
        String[] columnNames;
        Class[] types;
        boolean[] canEdit;

        public String getColumnName(int index) {
            return columnNames[index];
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return inputParams.size();
        }

        public Object getValueAt(int row, int column) {
            ParameterInfo info = inputParams.get(row);

            if(showParamTypes) {
                switch (column) {
                    case 0:
                        return info.getName();
                    case 1:
                        return info.getType();
                    case 2:
                        return info.getDefaultValue();
                    case 3:
                        return info.getStyle() == ParamStyle.QUERY;
                }
            } else {
                switch (column) {
                    case 0:
                        return info.getName();
                    case 1:
                        return info.getDefaultValue();
                    case 2:
                        return info.getStyle() == ParamStyle.QUERY;
                }
            }

            return null;
        }

        public void setValueAt(Object value, int row, int column) {
            ParameterInfo info = inputParams.get(row);

            int columnOffset = 0;
            if(showParamTypes)
                columnOffset = 1;
            if (column == columnOffset+1) {
                info.setDefaultValue(value);
            } else if (column == columnOffset+2) {
                if(((Boolean) value))
                    info.setStyle(ParamStyle.QUERY);
                else
                    info.setStyle(ParamStyle.UNKNOWN);
            }
        }

        public Class getColumnClass(int columnIndex) {
            return types[columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit[columnIndex];
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        paramLabel = new javax.swing.JLabel();
        paramScrollPane = new javax.swing.JScrollPane();
        paramTable = new ParamTable();

        paramLabel.setLabelFor(paramTable);
        paramLabel.setText(org.openide.util.NbBundle.getMessage(CodeSetupPanel.class, "LBL_Parameters")); // NOI18N

        paramTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        paramScrollPane.setViewportView(paramTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(paramLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
                    .addComponent(paramScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(paramLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paramScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addContainerGap())
        );

        paramLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeSetupPanel.class, "ACSN_Parameters")); // NOI18N
        paramLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeSetupPanel.class, "ACSD_Parameters")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel paramLabel;
    private javax.swing.JScrollPane paramScrollPane;
    private javax.swing.JTable paramTable;
    // End of variables declaration//GEN-END:variables
}
