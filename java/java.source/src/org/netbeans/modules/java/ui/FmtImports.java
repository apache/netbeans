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

package org.netbeans.modules.java.ui;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TryTree;
import java.util.Collections;
import java.util.EnumSet;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.util.NbBundle;
import static org.netbeans.modules.java.ui.FmtOptions.*;
import static org.netbeans.modules.java.ui.CategorySupport.OPTION_ID;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dusan Balek, Petr Hrebejk
 */
public class FmtImports extends javax.swing.JPanel implements Runnable, ListSelectionListener {
    
    private Preferences preferences;
    
    /** Creates new form FmtImports */
    public FmtImports(Preferences preferences) {
        this.preferences = preferences;
        initComponents();
        buttonGroup.add(singleClassImportsRadioButton);
        buttonGroup.add(packageImportsRadioButton);
        buttonGroup.add(fqnRadioButton);
        singleClassImportsRadioButton.putClientProperty(OPTION_ID, useSingleClassImport);
        importInnerClassesCheckBox.putClientProperty(OPTION_ID, importInnerClasses);
        preferStaticImportsCheckBox.putClientProperty(OPTION_ID, preferStaticImports);
        starImportTresholdCheckBox.putClientProperty(OPTION_ID, allowConvertToStarImport);
        starImportTresholdSpinner.putClientProperty(OPTION_ID, countForUsingStarImport);
        starStaticImportTresholdCheckBox.putClientProperty(OPTION_ID, allowConvertToStaticStarImport);
        startStaticImportTresholdSpinner.putClientProperty(OPTION_ID, countForUsingStaticStarImport);
        starImportPackagesTable.putClientProperty(OPTION_ID, packagesForStarImport);
        packageImportsRadioButton.putClientProperty(OPTION_ID, usePackageImport);
        fqnRadioButton.putClientProperty(OPTION_ID, useFQNs);
        separateStaticImportsCheckBox.putClientProperty(OPTION_ID, separateStaticImports);
        importLayoutTable.putClientProperty(OPTION_ID, importGroupsOrder);
        separateGroupsCheckBox.putClientProperty(OPTION_ID, separateImportGroups);
    }
    
    public static PreferencesCustomizer.Factory getController() {
        return new PreferencesCustomizer.Factory() {
            public PreferencesCustomizer create(Preferences preferences) {
                ImportsCategorySupport support = new ImportsCategorySupport(preferences, new FmtImports(preferences));
                ((Runnable)support.panel).run();
                return support;
            }
        };
    }
    
    @Override
    public void run() {
        starImportPackagesTable.getSelectionModel().addListSelectionListener(this);
        importLayoutTable.getSelectionModel().addListSelectionListener(this);
        enableControls(singleClassImportsRadioButton.isSelected());
        enableImportLayoutButtons();
        setStarImportPackagesTableColumnsWidth();
        setImportLayoutTableColumnsWidth();
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        enableStarImportPackageButton(singleClassImportsRadioButton.isSelected());
        enableImportLayoutButtons();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        singleClassImportsRadioButton = new javax.swing.JRadioButton();
        importInnerClassesCheckBox = new javax.swing.JCheckBox();
        starImportTresholdCheckBox = new javax.swing.JCheckBox();
        starImportTresholdSpinner = new javax.swing.JSpinner();
        starStaticImportTresholdCheckBox = new javax.swing.JCheckBox();
        startStaticImportTresholdSpinner = new javax.swing.JSpinner();
        starImportPackagesLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        starImportPackagesTable = new javax.swing.JTable();
        addStarImportPackageButton = new javax.swing.JButton();
        removeStarImportPackageButton = new javax.swing.JButton();
        packageImportsRadioButton = new javax.swing.JRadioButton();
        fqnRadioButton = new javax.swing.JRadioButton();
        preferStaticImportsCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        separateStaticImportsCheckBox = new javax.swing.JCheckBox();
        importLayoutLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        importLayoutTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        separateGroupsCheckBox = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_Imports")); // NOI18N
        setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(singleClassImportsRadioButton, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_useSingleClass")); // NOI18N
        singleClassImportsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleClassImportsRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(importInnerClassesCheckBox, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_importInnerClasses")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(starImportTresholdCheckBox, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_importTreshold")); // NOI18N
        starImportTresholdCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                starImportTresholdCheckBoxActionPerformed(evt);
            }
        });

        starImportTresholdSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        org.openide.awt.Mnemonics.setLocalizedText(starStaticImportTresholdCheckBox, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_staticImportTreshold")); // NOI18N
        starStaticImportTresholdCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                starStaticImportTresholdCheckBoxActionPerformed(evt);
            }
        });

        startStaticImportTresholdSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        starImportPackagesLabel.setLabelFor(starImportPackagesTable);
        org.openide.awt.Mnemonics.setLocalizedText(starImportPackagesLabel, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_starImportPackages")); // NOI18N

        starImportPackagesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        starImportPackagesTable.getTableHeader().setResizingAllowed(false);
        starImportPackagesTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(starImportPackagesTable);

        org.openide.awt.Mnemonics.setLocalizedText(addStarImportPackageButton, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_add")); // NOI18N
        addStarImportPackageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStarImportPackageButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeStarImportPackageButton, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_remove")); // NOI18N
        removeStarImportPackageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeStarImportPackageButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(packageImportsRadioButton, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_usePackage")); // NOI18N
        packageImportsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packageImportsRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(fqnRadioButton, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_useFQN")); // NOI18N
        fqnRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fqnRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(preferStaticImportsCheckBox, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_preferStaticImports")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(separateStaticImportsCheckBox, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_separateStaticImports")); // NOI18N
        separateStaticImportsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separateStaticImportsCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(importLayoutLabel, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_importLayout")); // NOI18N

        importLayoutTable.getTableHeader().setResizingAllowed(false);
        importLayoutTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(importLayoutTable);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_moveUp")); // NOI18N
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_moveDown")); // NOI18N
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_remove")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(separateGroupsCheckBox, org.openide.util.NbBundle.getMessage(FmtImports.class, "LBL_imp_separateGroups")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(starImportTresholdCheckBox)
                                    .addComponent(starImportPackagesLabel)
                                    .addComponent(starStaticImportTresholdCheckBox))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(addStarImportPackageButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(removeStarImportPackageButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(starImportTresholdSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                .addComponent(startStaticImportTresholdSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(separateStaticImportsCheckBox))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(addButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(moveUpButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(moveDownButton, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(singleClassImportsRadioButton)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(importInnerClassesCheckBox))
                            .addComponent(packageImportsRadioButton)
                            .addComponent(fqnRadioButton)
                            .addComponent(preferStaticImportsCheckBox)
                            .addComponent(importLayoutLabel)
                            .addComponent(separateGroupsCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {starImportTresholdSpinner, startStaticImportTresholdSpinner});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(singleClassImportsRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importInnerClassesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(starImportTresholdSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(starImportTresholdCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startStaticImportTresholdSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(starStaticImportTresholdCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(starImportPackagesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addStarImportPackageButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeStarImportPackageButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(packageImportsRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fqnRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(preferStaticImportsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(importLayoutLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separateStaticImportsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveUpButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveDownButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separateGroupsCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {starImportTresholdSpinner, startStaticImportTresholdSpinner});

    }// </editor-fold>//GEN-END:initComponents

    private void singleClassImportsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleClassImportsRadioButtonActionPerformed
        enableControls(singleClassImportsRadioButton.isSelected());
    }//GEN-LAST:event_singleClassImportsRadioButtonActionPerformed

    private void packageImportsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageImportsRadioButtonActionPerformed
        enableControls(singleClassImportsRadioButton.isSelected());
    }//GEN-LAST:event_packageImportsRadioButtonActionPerformed

    private void fqnRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fqnRadioButtonActionPerformed
        enableControls(singleClassImportsRadioButton.isSelected());
    }//GEN-LAST:event_fqnRadioButtonActionPerformed

    private void addStarImportPackageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStarImportPackageButtonActionPerformed
        ((DefaultTableModel)starImportPackagesTable.getModel()).addRow(new Object[] {"", true}); //NOI18N
        final int rowIndex = starImportPackagesTable.getRowCount() - 1;
        starImportPackagesTable.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
        starImportPackagesTable.requestFocusInWindow();
        starImportPackagesTable.editCellAt(rowIndex, 0);
    }//GEN-LAST:event_addStarImportPackageButtonActionPerformed

    private void removeStarImportPackageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeStarImportPackageButtonActionPerformed
        int row = starImportPackagesTable.getSelectedRow();
        if (row >= 0) {
            TableCellEditor cellEditor = starImportPackagesTable.getCellEditor();
            if (cellEditor != null)
                cellEditor.cancelCellEditing();
            ((DefaultTableModel)starImportPackagesTable.getModel()).removeRow(row);
        }
    }//GEN-LAST:event_removeStarImportPackageButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        if (importLayoutTable.getColumnCount() == 1) {
            ((DefaultTableModel)importLayoutTable.getModel()).addRow(new Object[] {""}); //NOI18N
        } else {
            ((DefaultTableModel)importLayoutTable.getModel()).addRow(new Object[] {false, ""}); //NOI18N
        }
        final int rowIndex = importLayoutTable.getRowCount() - 1;
        importLayoutTable.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
        importLayoutTable.requestFocusInWindow();
        importLayoutTable.editCellAt(rowIndex, importLayoutTable.getColumnCount() - 1);
    }//GEN-LAST:event_addButtonActionPerformed

    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        int row = importLayoutTable.getSelectedRow();
        if (row > 0) {
            ((DefaultTableModel)importLayoutTable.getModel()).moveRow(row, row, row - 1);
            importLayoutTable.getSelectionModel().setSelectionInterval(row - 1, row - 1);
        }
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        int row = importLayoutTable.getSelectedRow();
        if (row >= 0 && row < importLayoutTable.getRowCount() - 1) {
            ((DefaultTableModel)importLayoutTable.getModel()).moveRow(row, row, row + 1);
            importLayoutTable.getSelectionModel().setSelectionInterval(row + 1, row + 1);
        }
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int row = importLayoutTable.getSelectedRow();
        if (row >= 0) {
            TableCellEditor cellEditor = importLayoutTable.getCellEditor();
            if (cellEditor != null)
                cellEditor.cancelCellEditing();
            ((DefaultTableModel)importLayoutTable.getModel()).removeRow(row);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void separateStaticImportsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separateStaticImportsCheckBoxActionPerformed
        TableModel oldModel = importLayoutTable.getModel();
        TableModel newModel = (DefaultTableModel)createTableModel(importGroupsOrder, preferences);
        importLayoutTable.setModel(newModel);
        setImportLayoutTableColumnsWidth();
        for (TableModelListener l : ((DefaultTableModel)oldModel).getTableModelListeners()) {
            oldModel.removeTableModelListener(l);
            newModel.addTableModelListener(l);
            l.tableChanged(null);
        }
    }//GEN-LAST:event_separateStaticImportsCheckBoxActionPerformed

    private void starImportTresholdCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_starImportTresholdCheckBoxActionPerformed
        starImportTresholdSpinner.setEnabled(starImportTresholdCheckBox.isSelected());
    }//GEN-LAST:event_starImportTresholdCheckBoxActionPerformed

    private void starStaticImportTresholdCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_starStaticImportTresholdCheckBoxActionPerformed
        startStaticImportTresholdSpinner.setEnabled(starStaticImportTresholdCheckBox.isSelected());
    }//GEN-LAST:event_starStaticImportTresholdCheckBoxActionPerformed
        
    private void enableControls(boolean b) {
        importInnerClassesCheckBox.setEnabled(b);
        starImportTresholdCheckBox.setEnabled(b);
        starImportTresholdSpinner.setEnabled(b && starImportTresholdCheckBox.isSelected());
        starStaticImportTresholdCheckBox.setEnabled(b);
        startStaticImportTresholdSpinner.setEnabled(b && starStaticImportTresholdCheckBox.isSelected());
        starImportPackagesLabel.setEnabled(b);
        starImportPackagesTable.setEnabled(b);
        addStarImportPackageButton.setEnabled(b);
        enableStarImportPackageButton(b);
    }
    
    private void enableStarImportPackageButton(boolean b) {
        removeStarImportPackageButton.setEnabled(b && starImportPackagesTable.getSelectedRow() >= 0);
    }
    
    private void enableImportLayoutButtons() {
        int row = importLayoutTable.getSelectedRow();                
        moveUpButton.setEnabled(row > 0);
        moveDownButton.setEnabled(row >= 0 && row < importLayoutTable.getRowCount() - 1);
        removeButton.setEnabled(row >= 0 && allOtherImports != importLayoutTable.getValueAt(row, importLayoutTable.getColumnCount() - 1));
    }
    
    private void setStarImportPackagesTableColumnsWidth() {
        int tableWidth = starImportPackagesTable.getPreferredSize().width;
        TableColumn column = starImportPackagesTable.getColumnModel().getColumn(1);
        int colWidth = starImportPackagesTable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(starImportPackagesTable, column.getHeaderValue(), false, false, 0, 0).getPreferredSize().width;
        column.setPreferredWidth(colWidth);
        starImportPackagesTable.getColumnModel().getColumn(0).setPreferredWidth(tableWidth - colWidth);        
    }

    private void setImportLayoutTableColumnsWidth() {
        if (importLayoutTable.getColumnCount() > 1) {
            int tableWidth = importLayoutTable.getPreferredSize().width;
            TableColumn column = importLayoutTable.getColumnModel().getColumn(0);
            int colWidth = importLayoutTable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(importLayoutTable, column.getHeaderValue(), false, false, 0, 0).getPreferredSize().width;
            column.setPreferredWidth(colWidth);
            importLayoutTable.getColumnModel().getColumn(1).setPreferredWidth(tableWidth - colWidth);            
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addStarImportPackageButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton fqnRadioButton;
    private javax.swing.JCheckBox importInnerClassesCheckBox;
    private javax.swing.JLabel importLayoutLabel;
    private javax.swing.JTable importLayoutTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JRadioButton packageImportsRadioButton;
    private javax.swing.JCheckBox preferStaticImportsCheckBox;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeStarImportPackageButton;
    private javax.swing.JCheckBox separateGroupsCheckBox;
    private javax.swing.JCheckBox separateStaticImportsCheckBox;
    private javax.swing.JRadioButton singleClassImportsRadioButton;
    private javax.swing.JLabel starImportPackagesLabel;
    private javax.swing.JTable starImportPackagesTable;
    private javax.swing.JCheckBox starImportTresholdCheckBox;
    private javax.swing.JSpinner starImportTresholdSpinner;
    private javax.swing.JCheckBox starStaticImportTresholdCheckBox;
    private javax.swing.JSpinner startStaticImportTresholdSpinner;
    // End of variables declaration//GEN-END:variables

    static final String allOtherImports = NbBundle.getMessage(FmtImports.class, "LBL_imp_allOtherImports"); //NOI18N

    private static TableModel createTableModel(final String optionID, final Preferences node) {
        DefaultTableModel model = null;
        if (packagesForStarImport.equals(optionID)) {
            model = new DefaultTableModel(new Object[][]{}, new String[]{
                        NbBundle.getMessage(FmtImports.class, "LBL_imp_Package"), //NOI18N
                        NbBundle.getMessage(FmtImports.class, "LBL_imp_WithSub") //NOI18N
                    }) {
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? String.class : Boolean.class;
                }
            };
        } else {
            boolean separate = node.getBoolean(separateStaticImports, getDefaultAsBoolean(separateStaticImports));
            String[] colNames = separate
                    ? new String[] {NbBundle.getMessage(FmtImports.class, "LBL_imp_Static"), NbBundle.getMessage(FmtImports.class, "LBL_imp_Package")} //NOI18N
                    : new String[] {NbBundle.getMessage(FmtImports.class, "LBL_imp_Package")}; //NOI18N
            model = new DefaultTableModel(new Object[][] {}, colNames) {
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == getColumnCount() - 1 ? String.class : Boolean.class;
                }
                public boolean isCellEditable(int row, int column) {
                    return allOtherImports != getValueAt(row, getColumnCount() - 1);
                }                
            };
        }
        if (model != null) {
            boolean containsOtherImports = false;
            boolean containsStaticOtherImports = false;
            String value = node.get(optionID, getDefaultAsString(optionID));
            for (String s : value.trim().split("\\s*[,;]\\s*")) { //NOI18N
                boolean isStatic = false;
                boolean isStar = false;
                if (s.startsWith("static ")) { //NOI18N
                    isStatic = true;
                    s = s.substring(7);
                }
                if (s.endsWith(".*")) { //NOI18N
                    isStar = true;
                    s = s.substring(0, s.length() - 2);
                }
                if (s.length() > 0 && (!importGroupsOrder.equals(optionID) || model.getColumnCount() > 1 || !isStatic)) {
                    if ("*".equals(s)) { //NOI18N
                        s = allOtherImports;
                        if (isStatic) {
                            containsStaticOtherImports = true;
                        } else {
                            containsOtherImports = true;
                        }
                    }
                    Object[] val = new Object[model.getColumnCount()];
                    for (int i = 0; i < val.length; i++) {
                        if (Boolean.class.equals(model.getColumnClass(i))) {
                            val[i] = i == 0 ? isStatic : isStar;
                        } else {
                            val[i] = s;
                        }
                    }
                    model.addRow(val);
                }
            }
            if (importGroupsOrder.equals(optionID)) {
                if (model.getColumnCount() == 1) {
                    if (!containsOtherImports)
                        model.addRow(new Object[] {allOtherImports});                    
                } else {
                    if (!containsOtherImports)
                        model.addRow(new Object[] {false, allOtherImports});
                    if (!containsStaticOtherImports)
                        model.addRow(new Object[] {true, allOtherImports});
                }
            }
        }
        return model;
    }

    private static final class ImportsCategorySupport extends CategorySupport.DocumentCategorySupport {
        private ImportsCategorySupport(Preferences preferences, JPanel panel) {
            super(preferences, "imports", panel, NbBundle.getMessage(FmtImports.class, "SAMPLE_Imports")); //NOI18N
        }
    
        @Override
        protected void loadTableData(JTable table, String optionID, Preferences p) {
            TableModel model = createTableModel(optionID, p);
            table.setModel(model);
        }
        
        @Override
        protected void storeTableData(final JTable table, final String optionID, final Preferences node) {
            StringBuilder sb = null;
            for (int i = 0; i < table.getRowCount(); i++) {
                if (sb == null) {
                    sb = new StringBuilder();
                } else {
                    sb.append(';');
                }
                for (int j = 0; j < table.getColumnCount(); j++) {
                    if (Boolean.class.equals(table.getColumnClass(j))) {
                        if (((Boolean)table.getValueAt(i, j)).booleanValue())
                            sb.append(j == 0 ? "static " : ".*"); //NOI18N
                    } else {
                        Object val = table.getValueAt(i, j);
                        sb.append(allOtherImports == val ? "*" : val); //NOI18N
                    }
                }
            }
            String value = sb != null ? sb.toString() : ""; //NOI18N
            if (getDefaultAsString(optionID).equals(value))
                node.remove(optionID);
            else
                node.put(optionID, value);            
        }
        
        protected void doModification(ResultIterator resultIterator) throws Exception {
            WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
            copy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = copy.getCompilationUnit();
            ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
            MethodTree mt = (MethodTree) ct.getMembers().get(1);
            TreeMaker treeMaker = copy.getTreeMaker();
            BlockTree bt = mt.getBody();
            StatementTree stmt = treeMaker.Variable(treeMaker.Modifiers(EnumSet.noneOf(Modifier.class)), "is", treeMaker.QualIdent("java.io.InputStream"), treeMaker.Literal(null)); //NOI18N
            bt = treeMaker.addBlockStatement(bt, stmt);
            BlockTree tryBlock = treeMaker.Block(Collections.<StatementTree>emptyList(), false);
            ExpressionTree et = treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(), treeMaker.QualIdent("java.io.File"), Collections.singletonList(treeMaker.Literal("test.txt")), null); //NOI18N
            stmt = treeMaker.Variable(treeMaker.Modifiers(EnumSet.noneOf(Modifier.class)), "f", treeMaker.QualIdent("java.io.File"), et); //NOI18N
            tryBlock = treeMaker.addBlockStatement(tryBlock, stmt);
            et = treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(), treeMaker.QualIdent("java.io.FileInputStream"), Collections.singletonList(treeMaker.Identifier("f")), null); //NOI18N
            et = treeMaker.Assignment(treeMaker.Identifier("is"), et); //NOI18N
            tryBlock = treeMaker.addBlockStatement(tryBlock, treeMaker.ExpressionStatement(et));
            et = treeMaker.MemberSelect(treeMaker.Identifier("is"), "read"); //NOI18N
            et = treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(), et, Collections.<ExpressionTree>emptyList());
            stmt = treeMaker.Try(treeMaker.Block(Collections.singletonList(treeMaker.ExpressionStatement(et)), false), Collections.<CatchTree>emptyList(), null);
            et = treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(), treeMaker.MemberSelect(treeMaker.QualIdent("java.util.logging.Logger"), "getLogger"), Collections.<ExpressionTree>emptyList()); //NOI18N
            et = treeMaker.addMethodInvocationArgument((MethodInvocationTree) et, treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(), treeMaker.MemberSelect(treeMaker.MemberSelect(treeMaker.QualIdent("org.netbeans.samples.ClassA"), "class"), "getName"), Collections.<ExpressionTree>emptyList())); //NOI18N
            et = treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(), treeMaker.MemberSelect(et, "log"), Collections.<ExpressionTree>emptyList()); //NOI18N
            et = treeMaker.addMethodInvocationArgument((MethodInvocationTree) et, treeMaker.MemberSelect(treeMaker.QualIdent("java.util.logging.Logger"), "SEVERE")); //NOI18N
            et = treeMaker.addMethodInvocationArgument((MethodInvocationTree) et, treeMaker.Literal(null));
            et = treeMaker.addMethodInvocationArgument((MethodInvocationTree) et, treeMaker.Identifier("ex")); //NOI18N
            BlockTree catchBlock = treeMaker.Block(Collections.singletonList(treeMaker.ExpressionStatement(et)), false);
            stmt = treeMaker.addTryCatch((TryTree) stmt, treeMaker.Catch(treeMaker.Variable(treeMaker.Modifiers(EnumSet.noneOf(Modifier.class)), "ex", treeMaker.QualIdent("java.io.IOException"), null), catchBlock)); //NOI18N
            tryBlock = treeMaker.addBlockStatement(tryBlock, stmt);
            et = treeMaker.MemberSelect(treeMaker.Identifier("is"), "close"); //NOI18N
            et = treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(), et, Collections.<ExpressionTree>emptyList());
            stmt = treeMaker.Try(treeMaker.Block(Collections.singletonList(treeMaker.ExpressionStatement(et)), false), Collections.<CatchTree>emptyList(), null);
            stmt = treeMaker.addTryCatch((TryTree) stmt, treeMaker.Catch(treeMaker.Variable(treeMaker.Modifiers(EnumSet.noneOf(Modifier.class)), "ex", treeMaker.QualIdent("java.io.IOException"), null), catchBlock)); //NOI18N
            stmt = treeMaker.Try(tryBlock, Collections.<CatchTree>emptyList(), treeMaker.Block(Collections.singletonList(stmt), false));
            stmt = treeMaker.addTryCatch((TryTree) stmt, treeMaker.Catch(treeMaker.Variable(treeMaker.Modifiers(EnumSet.noneOf(Modifier.class)), "ex", treeMaker.QualIdent("java.io.FileNotFoundException"), null), catchBlock)); //NOI18N
            bt = treeMaker.addBlockStatement(bt, stmt);
            copy.rewrite(mt.getBody(), bt);
        }
    }
}
