/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

/*
 * VMOptionEditorPanel.java
 *
 * Created on 12.10.2010, 11:39:51
 */

package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

import javax.swing.event.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.List;

/**
 * @author moonko
 */
public class VMOptionEditorPanel extends javax.swing.JPanel {
    private ValueCellEditor cellEditor;



    public static interface Callback {
        void okButtonActionPerformed(java.awt.event.ActionEvent evt);

        void cancelButtonActionPerformed(java.awt.event.ActionEvent evt);
    }

    private final Callback callback;

    /**
     * Creates new form VMOptionEditorPanel
     */
    public VMOptionEditorPanel(Callback callback, VMOptionsTableModel model) {
        this.callback = callback;
        initComponents();
        optionsTable.setModel(model);
        cellEditor = new ValueCellEditor();
        configureTable();
    }

    private void configureTable() {
        final TableColumnModel tcm = optionsTable.getColumnModel();
        setUpColumns(tcm, 0, tcm.getColumnCount() - 1);
        tcm.addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                setUpColumns(tcm, e.getFromIndex(), e.getToIndex());
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {                
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });

        optionsTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                processText();                
            }
        });

        processText();
        optionsTable.setFillsViewportHeight(true);
        optionsTable.setShowVerticalLines(false);
    }

    private void processText() {
        final List<JavaVMOption<?>> list = getOptions();
        StringBuilder sb = new StringBuilder();
        for (JavaVMOption<?> option : list) {
            option.print(sb);
            sb.append(" ");
        }
        variablePreviewTextField.setText(sb.toString());
    }                                           

    private void setUpColumns(TableColumnModel tcm, int start, int stop) {
        for (int i = start; i <= stop; i++) {
            final TableColumn column = tcm.getColumn(i);
            setUpColumn(column);
            if (i == 0) {
                column.setPreferredWidth(150);
                column.setMaxWidth(150);
            }
        }
    }

    private void setUpColumn(TableColumn column) {
        column.setCellRenderer(cellEditor);
        column.setCellEditor(cellEditor);        
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        variablePreviewTextField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        optionsTable = new javax.swing.JTable();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        variablePreviewTextField.setEditable(false);
        variablePreviewTextField.setText(org.openide.util.NbBundle.getMessage(VMOptionEditorPanel.class, "VMOptionEditorPanel.variablePreviewTextField.text")); // NOI18N

        optionsTable.setModel(new VMOptionsTableModel());
        jScrollPane1.setViewportView(optionsTable);

        cancelButton.setText(org.openide.util.NbBundle.getMessage(VMOptionEditorPanel.class, "VMOptionEditorPanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText(org.openide.util.NbBundle.getMessage(VMOptionEditorPanel.class, "VMOptionEditorPanel.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                    .addComponent(variablePreviewTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(variablePreviewTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (callback != null) {
            callback.okButtonActionPerformed(evt);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (callback != null) {
            callback.cancelButtonActionPerformed(evt);
        }
    }//GEN-LAST:event_cancelButtonActionPerformed


    void setText(String text) {
        variablePreviewTextField.setText(text);
    }

    List<JavaVMOption<?>> getOptions() {
        final VMOptionsTableModel tableModel = (VMOptionsTableModel) optionsTable.getModel();
        return tableModel.getValidOptions();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    private javax.swing.JTable optionsTable;
    private javax.swing.JTextField variablePreviewTextField;
    // End of variables declaration//GEN-END:variables

}
