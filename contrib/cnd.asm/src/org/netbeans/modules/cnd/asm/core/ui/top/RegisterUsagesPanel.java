/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.cnd.asm.core.ui.top;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;

import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.cnd.asm.core.assistance.RegisterUsageAccesor;
import org.netbeans.modules.cnd.asm.core.assistance.RegisterUsageAccesor.RegisterStatus;
import org.netbeans.modules.cnd.asm.core.ui.top.ext.TableSorter;
import org.netbeans.modules.cnd.asm.core.assistance.RegisterChooserListener;
import org.netbeans.modules.cnd.asm.core.assistance.RegisterChooser;
import org.netbeans.modules.cnd.asm.model.AsmModel;
import org.netbeans.modules.cnd.asm.model.lang.Register;
//import org.netbeans.modules.cnd.debugger.gdb.GdbContext;
//import org.netbeans.modules.cnd.debugger.gdb.disassembly.RegisterValue;

public class RegisterUsagesPanel extends JPanel implements NavigatorTab,
                                          RegisterUsageAccesor, PropertyChangeListener {
        
    private AsmModel model;
    private final DefaultTableModel tableModel;  
    
    private RegisterChooserListener chooserListener;
            
    private static RegisterUsagesPanel instance;       
    
    public static synchronized RegisterUsagesPanel getInstance() {
        if (instance == null) {
            instance = new RegisterUsagesPanel();
        }
        return instance;
    }
        
    private RegisterUsagesPanel() {
        initComponents();
        
        tableModel = new RegisterTableModel();
        TableSorter tmpModel = new TableSorter(tableModel);
                       
        tmpModel.setColumnComparator(String.class, TableSorter.LEXICAL_COMPARATOR);
        tmpModel.setTableHeader(jRegisterTable.getTableHeader());
        
        jRegisterTable.setDefaultRenderer(Register.class, new RegisterCellRendererForRegister());
        jRegisterTable.setDefaultRenderer(RegisterStatus.class, new RegisterCellRendererForUsage());
        //jRegisterTable.setDefaultRenderer(RegisterValue.class, new RegisterCellRendererForValue());
               
        jRegisterTable.setModel(tmpModel); 
        
        jRegisterTable.getSelectionModel().addListSelectionListener(new RegisterSelectionListener());
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(RegisterUsagesPanel.class, "CTL_REGS_NAME");
    }
    
    public void setChooserListener(RegisterChooserListener chooserListener) {
        this.chooserListener = chooserListener;
    }
    
    public JPanel getPanel() {
        return this;
    }
    
    public void opened() {
        //GdbContext.getInstance().addPropertyChangeListener(GdbContext.PROP_REGISTERS, this);
    }
    
    public void closed() {
        //GdbContext.getInstance().removePropertyChangeListener(GdbContext.PROP_REGISTERS, this);
    }
    
    public void setDocument(DataObject dob) {
        this.model = AsmObjectUtilities.getModel(dob);
        
        if (model == null)
            return;
        
        Document doc = AsmObjectUtilities.getDocument(dob);
        chooserListener = (RegisterChooserListener) 
                doc.getProperty(RegisterChooserListener.class);
        setRegisters();
        updateRegisterValues();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // register values updated
        updateRegisterValues();
    }
    
    private void updateRegisterValues() {
        /*Collection<RegisterValue> res =
                (Collection<RegisterValue>)GdbContext.getInstance().getProperty(GdbContext.PROP_REGISTERS);
        if (res == null) {
            clearValues();
        } else {
            for (RegisterValue value : res) {
                setRegisterValue(value.getName(), value);
            }
        }*/
    }
    
    private static boolean isTheSame(Register reg, String name) {
        if (reg.getName().equals(name)) {
            return true;
        }
        for (Register child : reg.getChildren()) {
            if (child.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /*private void setRegisterValue(String reg, RegisterValue value) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Register curReg = (Register)tableModel.getValueAt(i, RegisterTableModel.COLUMN_REGISTER);
            if (isTheSame(curReg, reg)) {
                tableModel.setValueAt(value, i, RegisterTableModel.COLUMN_VALUE);
                return;
            }
        }
    }*/
    
    public void setRegisterStatus(Register reg, RegisterStatus status) {        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (reg == tableModel.getValueAt(i, RegisterTableModel.COLUMN_REGISTER)) {
                tableModel.setValueAt(status, i, RegisterTableModel.COLUMN_USAGE);
                return;
            }
        }                  
    }
    
    public void setRegisterStatus(Collection<Register> regs, RegisterStatus status) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (regs.contains(tableModel.getValueAt(i, RegisterTableModel.COLUMN_REGISTER))) {
                tableModel.setValueAt(status, i, RegisterTableModel.COLUMN_USAGE);
            }
        }         
    }
    
    public void clearStatuses() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("", i, RegisterTableModel.COLUMN_USAGE);
        } 
    }
    
    public void clearValues() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("", i, RegisterTableModel.COLUMN_VALUE);
        } 
    }
    
    private void setRegisters() {
        tableModel.setNumRows(0);
        
        for (Register reg : model.getRegisterSet()) {
            if (reg.getDirectParent() == null) {
                tableModel.addRow(new Object[] { reg, "", "" } ); // NOI18N
            }
        }                
    }
    
    private class RegisterSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            int sel = jRegisterTable.getSelectedRow();
            
            if (chooserListener != null) {
                if (sel != -1) {
                    final Register reg = (Register) tableModel.getValueAt(sel, 0);                
                    chooserListener.update(new RegisterChooser() {
                        public Collection<Register> getRegisters() {
                            return Arrays.asList(reg);
                        }
                    });
                }
                else {
                    chooserListener.update(new RegisterChooser() {
                        public Collection<Register> getRegisters() {
                            return Collections.<Register>emptyList();
                        }
                    });
                }
            }
        }        
    }
    
    public static final Color ARG_COLOR = new Color(235, 222, 194);
    public static final Color READ_COLOR = new Color(220, 234, 196);
    public static final Color USED_COLOR = new Color(253, 242, 196);
    public static final Color WRITE_COLOR = new Color(235, 199, 194);
    
    private static class RegisterCellRendererForUsage extends DefaultTableCellRenderer.UIResource {
	public RegisterCellRendererForUsage() {
	    super();
	    setHorizontalAlignment(JLabel.CENTER);
	}

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setEnabled(table == null || table.isEnabled());
            
            if (RegisterUsageAccesor.PredefinedStatuses.STATUS_ARG.equals(value)) {
                setBackground(ARG_COLOR);
            } else if (RegisterUsageAccesor.PredefinedStatuses.STATUS_READ.equals(value)) {
                setBackground(READ_COLOR);
            } else if (RegisterUsageAccesor.PredefinedStatuses.STATUS_USED.equals(value)) {
                setBackground(USED_COLOR);
            } else if (RegisterUsageAccesor.PredefinedStatuses.STATUS_WRITE.equals(value)) {
                setBackground(WRITE_COLOR);
            } else {
                setBackground(null);
            }
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
        }
    }
    
    private static class RegisterCellRendererForRegister extends DefaultTableCellRenderer.UIResource {
	public RegisterCellRendererForRegister() {
	    super();
	    setHorizontalAlignment(JLabel.CENTER);
	}	                                             
    }
    
    /*private static class RegisterCellRendererForValue extends DefaultTableCellRenderer.UIResource {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setEnabled(table == null || table.isEnabled());
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof RegisterValue) {
                RegisterValue rval = (RegisterValue) value;
                if (rval.isModified()) {
                    super.setFont(getFont().deriveFont(Font.BOLD));
                }
            }
            
            return this;
        }
    }*/
    
    private static class RegisterTableModel extends DefaultTableModel {
        public static final int COLUMN_REGISTER=0;
        public static final int COLUMN_USAGE=1;
        public static final int COLUMN_VALUE=2;
        
        private final Class[] types; 
         
        public RegisterTableModel() {
             super(new Object [][]  { },
                   new String [] { 
                        NbBundle.getMessage(RegisterUsagesPanel.class, "LBL_REGUSAGE_REGISTER"),
                        NbBundle.getMessage(RegisterUsagesPanel.class, "LBL_REGUSAGE_USAGE"),
                        NbBundle.getMessage(RegisterUsagesPanel.class, "LBL_REGUSAGE_VALUE")});
             
             types = new Class [] { Register.class, RegisterStatus.class, String.class/*RegisterValue.class*/ };
         }
               
        @Override
        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }      
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTableScrollPane = new javax.swing.JScrollPane();
        jRegisterTable = new javax.swing.JTable();

        jTableScrollPane.setViewportView(jRegisterTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
  
       
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable jRegisterTable;
    private javax.swing.JScrollPane jTableScrollPane;
    // End of variables declaration//GEN-END:variables
    
}
