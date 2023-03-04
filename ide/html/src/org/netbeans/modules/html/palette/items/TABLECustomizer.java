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


package org.netbeans.modules.html.palette.items;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;



/**
 *
 * @author  Libor Kotouc
 */
public class TABLECustomizer extends javax.swing.JPanel {

    private static final int CELLS_LIMIT = 100 * 100;
    
    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;

    private TABLE table;
            
    /** Creates new form TABLE_1Panel */
    public TABLECustomizer(TABLE table) {
        this.table = table;

        initComponents();
        try {
            ((JSpinner.NumberEditor)jSpinner1.getEditor()).getTextField().getAccessibleContext().setAccessibleName(jSpinner1.getAccessibleContext().getAccessibleName());
            ((JSpinner.NumberEditor)jSpinner1.getEditor()).getTextField().getAccessibleContext().setAccessibleDescription(jSpinner1.getAccessibleContext().getAccessibleDescription());
            ((JSpinner.NumberEditor)jSpinner2.getEditor()).getTextField().getAccessibleContext().setAccessibleName(jSpinner2.getAccessibleContext().getAccessibleName());
            ((JSpinner.NumberEditor)jSpinner2.getEditor()).getTextField().getAccessibleContext().setAccessibleDescription(jSpinner2.getAccessibleContext().getAccessibleDescription());
            ((JSpinner.NumberEditor)jSpinner3.getEditor()).getTextField().getAccessibleContext().setAccessibleName(jSpinner3.getAccessibleContext().getAccessibleName());
            ((JSpinner.NumberEditor)jSpinner3.getEditor()).getTextField().getAccessibleContext().setAccessibleDescription(jSpinner3.getAccessibleContext().getAccessibleDescription());
            ((JSpinner.NumberEditor)jSpinner4.getEditor()).getTextField().getAccessibleContext().setAccessibleName(jSpinner4.getAccessibleContext().getAccessibleName());
            ((JSpinner.NumberEditor)jSpinner4.getEditor()).getTextField().getAccessibleContext().setAccessibleDescription(jSpinner4.getAccessibleContext().getAccessibleDescription());
            ((JSpinner.NumberEditor)jSpinner5.getEditor()).getTextField().getAccessibleContext().setAccessibleName(jSpinner5.getAccessibleContext().getAccessibleName());
            ((JSpinner.NumberEditor)jSpinner5.getEditor()).getTextField().getAccessibleContext().setAccessibleDescription(jSpinner5.getAccessibleContext().getAccessibleDescription());
            ((JSpinner.NumberEditor)widthSpinner.getEditor()).getTextField().getAccessibleContext().setAccessibleName(widthSpinner.getAccessibleContext().getAccessibleName());
            ((JSpinner.NumberEditor)widthSpinner.getEditor()).getTextField().getAccessibleContext().setAccessibleDescription(widthSpinner.getAccessibleContext().getAccessibleDescription());
        } catch (Exception e) {
        }
    }
    
    public boolean showDialog() {
        checkTableSize();
        
        dialogOK = false;
        
        String displayName = "";
        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.html.palette.items.resources.Bundle").getString("NAME_html-TABLE"); // NOI18N
        }
        catch (Exception e) {}
        
        descriptor = new DialogDescriptor
                (this, NbBundle.getMessage(TABLECustomizer.class, "LBL_Customizer_InsertPrefix") + " " + displayName, true,
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            evaluateInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
		     }
		 } 
                );
        
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Dialog"));
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Dialog"));
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private void evaluateInput() {
        
        int rows = ((Integer)jSpinner1.getValue()).intValue();
        table.setRows(rows);

        int cols = ((Integer)jSpinner2.getValue()).intValue();
        table.setCols(cols);

        int border = ((Integer)jSpinner3.getValue()).intValue();
        table.setBorder(border);

        int width = ((Integer)widthSpinner.getValue()).intValue();
        table.setWidth(width);

        int cspac = ((Integer)jSpinner4.getValue()).intValue();
        table.setCspac(cspac);

        int cpadd = ((Integer)jSpinner5.getValue()).intValue();
        table.setCpadd(cpadd);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jSpinner3 = new javax.swing.JSpinner();
        jSpinner4 = new javax.swing.JSpinner();
        jSpinner5 = new javax.swing.JSpinner();
        widthSpinner = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();

        jLabel1.setLabelFor(jSpinner2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "LBL_TABLE_Columns")); // NOI18N

        jLabel2.setLabelFor(jSpinner1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "LBL_TABLE_Rows")); // NOI18N

        jSpinner1.setModel(new SpinnerNumberModel(table.getRows(), 0, Integer.MAX_VALUE, 1));
        jSpinner1.setEditor(new JSpinner.NumberEditor(jSpinner1, "#"));
        jSpinner1.setValue(Integer.valueOf(table.getRows()));
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });

        jSpinner2.setModel(new SpinnerNumberModel(table.getCols(), 0, Integer.MAX_VALUE, 1));
        jSpinner2.setEditor(new JSpinner.NumberEditor(jSpinner2, "#"));
        jSpinner2.setValue(Integer.valueOf(table.getCols()));
        jSpinner2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner2StateChanged(evt);
            }
        });

        jLabel3.setLabelFor(jSpinner3);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "LBL_TABLE_Border")); // NOI18N

        jLabel4.setLabelFor(widthSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "LBL_TABLE_Width")); // NOI18N

        jLabel5.setLabelFor(jSpinner4);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "LBL_TABLE_Spacing")); // NOI18N

        jLabel6.setLabelFor(jSpinner5);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "LBL_TABLE_Padding")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "LBL_TABLE_SpacingHelp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "LBL_TABLE_PaddingHelp")); // NOI18N

        jSpinner3.setModel(new SpinnerNumberModel(table.getBorder(), 0, Integer.MAX_VALUE, 1));
        jSpinner3.setEditor(new JSpinner.NumberEditor(jSpinner3, "#"));
        jSpinner3.setValue(Integer.valueOf(table.getBorder()));

        jSpinner4.setModel(new SpinnerNumberModel(table.getCspac(), 0, Integer.MAX_VALUE, 1));
        jSpinner4.setEditor(new JSpinner.NumberEditor(jSpinner4, "#"));
        jSpinner4.setValue(Integer.valueOf(table.getCspac()));

        jSpinner5.setModel(new SpinnerNumberModel(table.getCpadd(), 0, Integer.MAX_VALUE, 1));
        jSpinner5.setEditor(new JSpinner.NumberEditor(jSpinner5, "#"));
        jSpinner5.setValue(Integer.valueOf(table.getCpadd()));

        widthSpinner.setModel(new SpinnerNumberModel(table.getWidth(), 0, Integer.MAX_VALUE, 1));
        widthSpinner.setEditor(new JSpinner.NumberEditor(widthSpinner, "#"));
        widthSpinner.setValue(Integer.valueOf(table.getWidth()));

        jLabel9.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "TABLECustomizer.jLabel9.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(widthSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSpinner2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSpinner3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSpinner4, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSpinner5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(widthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jSpinner5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Columns")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Columns")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Rows")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Rows")); // NOI18N
        jSpinner1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Rows_Spinner")); // NOI18N
        jSpinner1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Rows_Spinner")); // NOI18N
        jSpinner2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Columns_Spinner")); // NOI18N
        jSpinner2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Columns_Spinner")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Border")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Border")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Width")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Width")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Spacing")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Spacing")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Padding")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Padding")); // NOI18N
        jLabel7.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_SpacingHelp")); // NOI18N
        jLabel7.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_SpacingHelp")); // NOI18N
        jLabel8.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_PaddingHelp")); // NOI18N
        jLabel8.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_PaddingHelp")); // NOI18N
        jSpinner3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Border_Spinner")); // NOI18N
        jSpinner3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Border_Spinner")); // NOI18N
        jSpinner4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Spacing_Spinner")); // NOI18N
        jSpinner4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Spacing_Spinner")); // NOI18N
        jSpinner5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Padding_Spinner")); // NOI18N
        jSpinner5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Padding_Spinner")); // NOI18N
        widthSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSN_TABLE_Width_Spinner")); // NOI18N
        widthSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "ACSD_TABLE_Width_Spinner")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        checkTableSize();
    }//GEN-LAST:event_jSpinner1StateChanged

    private void jSpinner2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner2StateChanged
        checkTableSize();
    }//GEN-LAST:event_jSpinner2StateChanged
    
    private void checkTableSize() {
        int rows = ((Integer)jSpinner1.getValue()).intValue();
        int cols = ((Integer)jSpinner2.getValue()).intValue();
        
        boolean tooMuch = Math.max(rows * cols, cols) > CELLS_LIMIT;
        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, tooMuch ? org.openide.util.NbBundle.getMessage(TABLECustomizer.class, "TABLECustomizer.jLabel9.text") : ""); // NOI18N
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner4;
    private javax.swing.JSpinner jSpinner5;
    private javax.swing.JSpinner widthSpinner;
    // End of variables declaration//GEN-END:variables
    
}
