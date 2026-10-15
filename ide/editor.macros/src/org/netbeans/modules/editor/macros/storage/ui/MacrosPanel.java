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
package org.netbeans.modules.editor.macros.storage.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.api.ShortcutsFinder;
import org.netbeans.modules.editor.macros.storage.ui.MacrosModel.Macro;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.AsyncGUIJob;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
@OptionsPanelController.Keywords(keywords = {"#KW_Macros"}, location = OptionsDisplayer.EDITOR, tabTitle="#CTL_Macros_DisplayName")
public class MacrosPanel extends JPanel {

    private final MacrosModel model = MacrosModel.get();
    
    /**
     * Translates view > model indexes
     */
    private TableSorter sorter;
    
    private ShortcutsFinder.Writer finder;
    
    private boolean ignoreUIChanges;
    
    /** 
     * Creates new form MacrosPanel.
     */
    public MacrosPanel(Lookup lookup) {
        initComponents();

        // 1) init components
        tMacros.getAccessibleContext().setAccessibleName(loc("AN_Macros_Table")); //NOI18N
        tMacros.getAccessibleContext().setAccessibleDescription(loc("AD_Macros_Table")); //NOI18N
        epMacroCode.getAccessibleContext().setAccessibleName(loc("AN_Macro")); //NOI18N
        epMacroCode.getAccessibleContext().setAccessibleDescription(loc("AD_Macro")); //NOI18N
        bRemove.setEnabled(false);
        bSetShortcut.setEnabled(false);
        loc(bNew, "New_Macro"); //NOI18N
        loc(bRemove, "Remove_Macro"); //NOI18N
        loc(bSetShortcut, "Shortcut"); //NOI18N
        tMacros.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tMacros.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                tMacrosValueChanged(evt);
            }
        });
        tMacros.getTableHeader().setReorderingAllowed(false);
        sorter = new TableSorter(model.getTableModel());
        tMacros.setModel(sorter);
        sorter.setTableHeader(tMacros.getTableHeader());
        sorter.getTableHeader().setReorderingAllowed(false);

        tMacros.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent evt) {
                tMacrosTableChanged(evt);
            }
        });

        // Fix for #135985
        tMacros.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if( KeyEvent.VK_ENTER == e.getKeyCode()) {
                    epMacroCode.requestFocusInWindow();
                    e.consume();
                }
            }
        });
        
        epMacroCode.setEnabled(false);
        epMacroCode.setEditorKit(JEditorPane.createEditorKitForContentType("text/plain")); //NOI18N
        epMacroCode.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                epMacroCodeDocumentChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                epMacroCodeDocumentChanged();
            }

            public void changedUpdate(DocumentEvent e) {
                // ignore
            }
        });
        
        loc(lMacros, "Macro_List"); //NOI18N
        lMacros.setLabelFor(tMacros);
        loc(lMacroCode, "Macro_Code"); //NOI18N
        lMacroCode.setLabelFor(epMacroCode);
        
        initializeFinder();
    }

    public MacrosModel getModel() {
        return model;
    }
    
    public void forceAddMacro(String code) {
        MacrosModel.Macro macro = addMacro(code);
    }
    
    private void initializeFinder() {
        if (finder != null) {
            lWait.setVisible(false);
            return;
        }
        org.openide.util.Utilities.attachInitJob(this, new AsyncGUIJob() {
            ShortcutsFinder.Writer initFinder;
            
            @Override
            public void construct() {
                initFinder = Lookup.getDefault().lookup(ShortcutsFinder.class).localCopy();
            }

            @Override
            public void finished() {
                lWait.setVisible(false);
                finder = initFinder;
                updateButtons(tMacros.getSelectedRow());
            }
            
        });
        bSetShortcut.setEnabled(false);
        bRemove.setEnabled(false);
    }
    

    // UI form .................................................................
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lMacros = new javax.swing.JLabel();
        spMacros = new javax.swing.JScrollPane();
        tMacros = new javax.swing.JTable();
        bNew = new javax.swing.JButton();
        bSetShortcut = new javax.swing.JButton();
        bRemove = new javax.swing.JButton();
        lMacroCode = new javax.swing.JLabel();
        sMacroCode = new javax.swing.JScrollPane();
        epMacroCode = new javax.swing.JEditorPane();
        lWait = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lMacros.setText("Macros:");

        tMacros.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        spMacros.setViewportView(tMacros);

        bNew.setText("New");
        bNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bNewActionPerformed(evt);
            }
        });

        bSetShortcut.setText("Set Shortcut...");
        bSetShortcut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSetShortcutActionPerformed(evt);
            }
        });

        bRemove.setText("Remove");
        bRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRemoveActionPerformed(evt);
            }
        });

        lMacroCode.setText("Macro Code:");

        sMacroCode.setViewportView(epMacroCode);

        lWait.setFont(lWait.getFont().deriveFont((lWait.getFont().getStyle() | java.awt.Font.ITALIC) & ~java.awt.Font.BOLD));
        lWait.setText(org.openide.util.NbBundle.getMessage(MacrosPanel.class, "LABEL_ShortcutsLoading")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lMacros)
                    .addComponent(lMacroCode)
                    .addComponent(spMacros, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(sMacroCode, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(bNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bSetShortcut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bRemove, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(lWait)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lMacros)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bSetShortcut)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bRemove))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(spMacros, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lMacroCode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sMacroCode, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lWait)
                .addGap(0, 0, 0))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MacrosPanel.class, "AN_MacrosPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacrosPanel.class, "AD_MacrosPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void bNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bNewActionPerformed
        // TODO add your handling code here:
        addMacro(null);
    }//GEN-LAST:event_bNewActionPerformed

    private void bSetShortcutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSetShortcutActionPerformed
	       int selectedRow = tMacros.getSelectedRow();
        finder.refreshActions();
        String shortcut = finder.showShortcutsDialog();
        // is there already an action with such SC defined?
        ShortcutAction act = finder.findActionForShortcut(shortcut);

        List<Macro> list = model.getAllMacros();
        Iterator<Macro> it = list.iterator();
        while (it.hasNext()) {
            Macro m = it.next();
            if (m.getShortcuts().size() > 0) {
                List<KeyStroke> l2 = m.getShortcuts().get(0).getKeyStrokeList();
                KeyStroke[] arr = l2.toArray(new KeyStroke[0]);
                String sc = KeyStrokeUtils.getKeyStrokesAsText(arr, " "); // NOI18N
                if (sc.equals(shortcut)) {
                    m.setShortcuts(Collections.<String>emptySet());
                    finder.setShortcuts(m, Collections.<String>emptySet());
                }
            }
        }

        if (act != null) {
            Set<String> set = Collections.<String>emptySet();
            // This colliding SC is not a macro, don't try to clean it up
            if (act instanceof MacrosModel.Macro) {
                ((MacrosModel.Macro) act).setShortcuts(set);
            }

            finder.setShortcuts(act, set);
        }
        
        if (shortcut != null) {
            int modelRow = sorter.modelIndex(selectedRow);
            MacrosModel.Macro macro = model.getMacroByIndex(modelRow);
            macro.setShortcut(shortcut);
            finder.setShortcuts(macro, Collections.singleton(shortcut));
//	    shortcutsFinder.apply();
//                StorageSupport.keyStrokesToString(Arrays.asList(StorageSupport.stringToKeyStrokes(shortcut, true)), false)));
        }
    }//GEN-LAST:event_bSetShortcutActionPerformed

    private void bRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRemoveActionPerformed
        int modelIndex = sorter.modelIndex(tMacros.getSelectedRow());
	finder.setShortcuts(model.getMacroByIndex(modelIndex), Collections.<String>emptySet());
        model.deleteMacro(modelIndex);
    }//GEN-LAST:event_bRemoveActionPerformed

    private void tMacrosValueChanged(ListSelectionEvent evt) {
        int index = tMacros.getSelectedRow();
        updateButtons(index);
    }
    
    private void updateButtons(int index) {
        ignoreUIChanges = true;
        try {
            if (index < 0 || index >= tMacros.getRowCount()) {
                epMacroCode.setText(""); //NOI18N
                epMacroCode.setEnabled(false);
                bRemove.setEnabled(false);
                bSetShortcut.setEnabled(false);
            } else {
                int modelIndex = sorter.modelIndex(index);
                epMacroCode.setText(model.getMacroByIndex(modelIndex).getCode()); //NOI18N
                epMacroCode.getCaret().setDot(0);
                epMacroCode.setEnabled(true);
                // Fix for #135985 commented to avoid focus
                //epMacroCode.requestFocusInWindow();
                bRemove.setEnabled(true && finder != null);
                bSetShortcut.setEnabled(true && finder != null);
            }
        } finally {
            ignoreUIChanges = false;
        }
    }
    
    private void tMacrosTableChanged(final TableModelEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (evt.getType() == TableModelEvent.INSERT) {
                    tMacros.getSelectionModel().setSelectionInterval(evt.getFirstRow(), evt.getFirstRow());
                } else if (evt.getType() == TableModelEvent.DELETE) {
                    // try the next row after the deleted one
                    int tableRow = evt.getLastRow();
                    if (tableRow < tMacros.getModel().getRowCount()) {
                        tMacros.getSelectionModel().setSelectionInterval(tableRow, tableRow);
                    } else {
                        // try the previous row
                        tableRow = evt.getFirstRow() - 1;
                        if (tableRow >= 0) {
                            tMacros.getSelectionModel().setSelectionInterval(tableRow, tableRow);
                        } else {
                            tMacros.getSelectionModel().clearSelection();
                        }
                    }
                }
            }
        });
    }
    
    private void epMacroCodeDocumentChanged() {
        if (ignoreUIChanges) {
            return;
        }
        int index = tMacros.getSelectedRow();
        if (index >= 0) {
            model.getMacroByIndex(sorter.modelIndex(index)).setCode(epMacroCode.getText());
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bNew;
    private javax.swing.JButton bRemove;
    private javax.swing.JButton bSetShortcut;
    private javax.swing.JEditorPane epMacroCode;
    private javax.swing.JLabel lMacroCode;
    private javax.swing.JLabel lMacros;
    private javax.swing.JLabel lWait;
    private javax.swing.JScrollPane sMacroCode;
    private javax.swing.JScrollPane spMacros;
    private javax.swing.JTable tMacros;
    // End of variables declaration//GEN-END:variables
    
    private static String loc(String key) {
        return NbBundle.getMessage(MacrosPanel.class, key);
    }

    private static void loc(Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext().setAccessibleName(loc("AN_" + key)); //NOI18N
            c.getAccessibleContext().setAccessibleDescription(loc("AD_" + key)); //NOI18N
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText((AbstractButton) c, loc("CTL_" + key)); //NOI18N
        } else {
            Mnemonics.setLocalizedText((JLabel) c, loc("CTL_" + key)); //NOI18N
        }
    }

    private MacrosModel.Macro addMacro(String initCode) {
        final MacrosNamePanel panel=new MacrosNamePanel();
        final DialogDescriptor descriptor = new DialogDescriptor(panel, loc("CTL_New_macro_dialog_title"));//NO18N
        panel.setChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                String name=panel.getNameValue().trim();
                String err = model.validateMacroName(name);
                descriptor.setValid(err==null);
                panel.setErrorMessage(err);
            }
        });

        if (DialogDisplayer.getDefault().notify(descriptor)==DialogDescriptor.OK_OPTION) {
            String macroName = panel.getNameValue().trim();
	    final Macro macro = model.createMacro(MimePath.EMPTY, macroName);
            if (initCode != null) {
                macro.setCode(initCode);
            }
	    sorter.resortAfterModelChange();
            int sel = sorter.viewIndex(model.getAllMacros().size() - 1);
            tMacros.getSelectionModel().setSelectionInterval(sel, sel);
            tMacros.scrollRectToVisible(tMacros.getCellRect(sel, 0, true));
            return macro;
        }
        return null;
    }
    
    public void save() {
        getModel().save();
        // force shortcut finder flush
        if (finder != null) {
            finder.apply();
        }
    }
}
