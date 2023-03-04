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

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.earproject.ModuleType;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Martin Krauskopf
 */
public class PanelModuleDetectionVisual extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private final Vector<Vector<String>> modules = new Vector<Vector<String>>();
    private static final int REL_PATH_INDEX = 0;
    private static final int TYPE_INDEX = 1;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    // Location of Enterprise Application to be imported, chosen on the previous panel.
    private File eaLocation;
    
    public PanelModuleDetectionVisual() {
        initComponents();
        initModuleTable();
        // Provide a name in the title bar.
        setName(getMessage("LBL_IW_ApplicationModulesStep"));
        putClientProperty("NewProjectWizard_Title", getMessage("TXT_ImportProject"));
        getAccessibleContext().setAccessibleDescription(getMessage("ACS_NWP1_NamePanel_A11YDesc"));
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    private void initModuleTable() {
        Vector<String> colNames = new Vector<String>();
        colNames.add(getMessage("LBL_IW_Module"));
        colNames.add(getMessage("LBL_IW_Type"));
        DefaultTableModel moduleTableModel = new DefaultTableModel(modules, colNames);
        moduleTable.setModel(moduleTableModel);
        TableColumnModel tcm = moduleTable.getColumnModel();
        TableColumn tc = tcm.getColumn(1);
        ModuleTypeRenderer renderer = new ModuleTypeRenderer();
        tc.setCellRenderer(renderer);
        tc.setCellEditor(new ModuleTypeEditor());
        moduleTable.setRowHeight((int) renderer.getPreferredSize().getHeight());
        moduleSP.getViewport().setBackground(moduleTable.getBackground());
    }
    
    void read(WizardDescriptor settings) {
        File newEALocation = (File) settings.getProperty(WizardProperties.SOURCE_ROOT);
        assert newEALocation != null : "Location is not available!";
        if (!newEALocation.equals(eaLocation)) {
            // reset all set of modules
            this.modules.removeAllElements();
        }
        eaLocation = newEALocation;
        FileObject eaLocationFO = FileUtil.toFileObject(eaLocation);
        Map<FileObject, ModuleType> modules = ModuleType.detectModules(eaLocationFO);
        for (FileObject moduleDir : modules.keySet()) {
            addModuleToTable(FileUtil.toFile(moduleDir));
        }
        getModuleTableModel().fireTableDataChanged();
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        // #143772 - we need to check whether the directory is not already NB project, but NOT j2ee module
        for (Vector<String> module : modules) {
            String moduleDirectory = module.get(REL_PATH_INDEX);
            if (isForbiddenProject(moduleDirectory)) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PanelModuleDetectionVisual.class, "MSG_ModuleNotJavaEEModule", moduleDirectory));
                return false;
            }
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
        return true;
    }

    // return true for nb project which is not java ee module
    private boolean isForbiddenProject(String moduleDirectory) {
        File module = FileUtil.normalizeFile(new File(eaLocation, moduleDirectory));
        Project project = null;
        try {
            project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(module));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (project == null) {
            // not nb project at all
            return false;
        }
        return !EarProjectUtil.isJavaEEModule(project);
    }

    void store(WizardDescriptor wd) {
        Map<FileObject, ModuleType> userModules =
                new HashMap<FileObject, ModuleType>();
        for (Vector<String> module : modules) {
            String description = module.get(TYPE_INDEX);
            for (ModuleType type : ModuleType.values()) {
                if (type.getDescription().equals(description)) {
                    File moduleDir = new File(eaLocation, module.get(REL_PATH_INDEX));
                    FileObject moduleDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(moduleDir));
                    assert moduleDirFO != null;
                    userModules.put(moduleDirFO, type);
                    break;
                }
            }
        }
        wd.putProperty(WizardProperties.USER_MODULES, userModules);
    }
    
    private DefaultTableModel getModuleTableModel() {
        return (DefaultTableModel) moduleTable.getModel();
    }
    
    private void addModuleToTable(final File moduleF) {
        String relPath = PropertyUtils.relativizeFile(eaLocation, moduleF);
        if (relPath == null) {
            return;
        }
        for (Vector<String> module : modules) {
            if (relPath.equals(module.get(REL_PATH_INDEX))) {
                // already added
                return;
            }
        }
        Vector<String> row = new Vector<String>();
        row.add(relPath);
        row.add(getModuleType(relPath).getDescription());
        modules.add(row);
        changeSupport.fireChange();
    }
    
    private static final String getMessage(String bundleKey) {
        return NbBundle.getMessage(PanelModuleDetectionVisual.class, bundleKey);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        appModulesLabel = new JLabel();
        moduleSP = new JScrollPane();
        moduleTable = new JTable();
        addModuleButton = new JButton();
        removeModuleButton = new JButton();

        appModulesLabel.setLabelFor(moduleTable);
        Mnemonics.setLocalizedText(appModulesLabel, NbBundle.getMessage(PanelModuleDetectionVisual.class, "LBL_IW_ApplicationModules")); // NOI18N

        moduleSP.setViewportView(moduleTable);
        Mnemonics.setLocalizedText(addModuleButton, NbBundle.getMessage(PanelModuleDetectionVisual.class, "LBL_IW_Add"));
        addModuleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addModuleButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(removeModuleButton, NbBundle.getMessage(PanelModuleDetectionVisual.class, "LBL_IW_Remove"));
        removeModuleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeModuleButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(appModulesLabel)
                    .addComponent(moduleSP, GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(addModuleButton, GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                    .addComponent(removeModuleButton)))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {addModuleButton, removeModuleButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(appModulesLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addModuleButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(removeModuleButton)
                        .addContainerGap())
                    .addComponent(moduleSP, GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)))
        );

        appModulesLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelModuleDetectionVisual.class, "ACSD_LBL_IW_ApplicationModules")); // NOI18N
        moduleSP.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PanelModuleDetectionVisual.class, "ACSN_CTL_AppModules")); // NOI18N
        moduleSP.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelModuleDetectionVisual.class, "ACSD_CTL_AppModules")); // NOI18N
        addModuleButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelModuleDetectionVisual.class, "ACSD_LBL_IW_Add")); // NOI18N
        removeModuleButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelModuleDetectionVisual.class, "ACSD_LBL_IW_Remove")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void removeModuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeModuleButtonActionPerformed
        int row = moduleTable.getSelectedRow();
        if (row != -1) {
            modules.remove(row);
            getModuleTableModel().fireTableRowsDeleted(row, row);
            changeSupport.fireChange();
        }
    }//GEN-LAST:event_removeModuleButtonActionPerformed
    
    private void addModuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModuleButtonActionPerformed
        JFileChooser chooser = new JFileChooser(eaLocation);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (eaLocation.equals(chooser.getSelectedFile())) {
                // XXX show some dialog to the user that Enterprise Application
                // itself cannot be added
                return;
            }
            addModuleToTable(chooser.getSelectedFile());
            getModuleTableModel().fireTableDataChanged();
        }
    }//GEN-LAST:event_addModuleButtonActionPerformed
    
    private ModuleType getModuleType(final String relPath) {
        ModuleType type = null;
        File dir = FileUtil.normalizeFile(new File(eaLocation, relPath));
        FileObject dirFO = FileUtil.toFileObject(dir);
        if (dirFO != null) {
            type = ModuleType.detectModuleType(dirFO);
        }
        return type == null ? ModuleType.WEB : type; // WEB is default if detection fails;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addModuleButton;
    private JLabel appModulesLabel;
    private JScrollPane moduleSP;
    private JTable moduleTable;
    private JButton removeModuleButton;
    // End of variables declaration//GEN-END:variables
    
    private static final class ModuleTypeRenderer extends JComboBox implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        
        ModuleTypeRenderer() {
            for (ModuleType type : ModuleType.values()) {
                addItem(type.getDescription());
            }
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            
            String moduleType = (String) value;
            setSelectedItem(moduleType);
            return this;
        }
        
    }
    
    private class ModuleTypeEditor extends JComboBox implements TableCellEditor {
        private static final long serialVersionUID = 1L;
        
        private EventListenerList myListenerList = new EventListenerList();
        private ChangeEvent changeEvent = new ChangeEvent(this);
        
        ModuleTypeEditor() {
            for (ModuleType type : ModuleType.values()) {
                addItem(type.getDescription());
            }
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    fireEditingStopped();
                }
            });
        }
        
        @Override
        public void addCellEditorListener(CellEditorListener listener) {
            myListenerList.add(CellEditorListener.class, listener);
        }
        
        @Override
        public void removeCellEditorListener(CellEditorListener listener) {
            myListenerList.remove(CellEditorListener.class, listener);
        }
        
        protected void fireEditingStopped() {
            CellEditorListener listener;
            Object[] listeners = myListenerList.getListenerList();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == CellEditorListener.class) {
                    listener = (CellEditorListener) listeners[i + 1];
                    listener.editingStopped(changeEvent);
                }
            }
        }
        
        protected void fireEditingCanceled() {
            CellEditorListener listener;
            Object[] listeners = myListenerList.getListenerList();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == CellEditorListener.class) {
                    listener = (CellEditorListener) listeners[i + 1];
                    listener.editingCanceled(changeEvent);
                }
            }
        }
        
        @Override
        public void cancelCellEditing() {
            fireEditingCanceled();
        }
        
        @Override
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }
        
        @Override
        public boolean isCellEditable(EventObject event) {
            return true;
        }
        
        @Override
        public boolean shouldSelectCell(EventObject event) {
            return true;
        }
        
        @Override
        public Object getCellEditorValue() {
            return getSelectedItem();
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            String moduleType = (String) value;
            setSelectedItem(moduleType);
            return this;
        }
        
    }
    
}
