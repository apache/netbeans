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

package org.apache.tools.ant.module.wizards.shortcut;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;


/**
 * Wizard panel that lets you select a menu or toolbar folder and a display name
 * for the menu or toolbar item.
 */
final class SelectFolderPanel extends JPanel implements DocumentListener {

    private final String prop;
    private final boolean stripAmps;
    private final SelectFolderWizardPanel wiz;
    private final DataFolder top;
    
    /** Create the wizard panel component and set up some basic properties. */
    public SelectFolderPanel(SelectFolderWizardPanel wiz, String name, 
            String hint, String displayNameLabelText, DataFolder top, 
            boolean stripAmps, String prop) {
        this.wiz = wiz;
        initComponents ();
        // Provide a name in the title bar.
        setName (name);
        hintsArea.setText (hint);
        initAccessibility (hint);                
        //displayNameLabel.setText(displayNameLabelText);
        displayNameLabel.getAccessibleContext().setAccessibleDescription( 
                displayNameLabelText );
        Mnemonics.setLocalizedText(displayNameLabel,displayNameLabelText);
        displayNameField.getAccessibleContext().setAccessibleName(displayNameLabelText);
        displayNameField.getAccessibleContext().setAccessibleDescription(displayNameLabelText);
        
        this.prop = prop;
        this.top = top;
        this.stripAmps = stripAmps;
        DefaultListModel<DataObject> model = new DefaultListModel<>();
        DataObject[] folders = findFolders(top);
        for (int i = 0; i < folders.length; i++) {
            model.addElement(folders[i]);
        }
        folderList.setModel(model);
        folderList.setCellRenderer(new CellRenderer());
        displayNameField.getDocument().addDocumentListener(this);
    }
    
    ListModel getModel() {
        return folderList.getModel();
    }
    
    private String getDisplayName(DataFolder folder) {
        String name = folder.getNodeDelegate().getDisplayName();
        if (stripAmps) {
            // XXX use o.o.a.Mnemonics instead
            int idx = name.indexOf('&');
            if (idx != -1) {
                name = name.substring(0, idx) + name.substring(idx + 1);
            }
        }
        return name;
    }
    
    String getNestedDisplayName(DataFolder folder) {
        DataFolder f = folder;
        StringBuffer b = new StringBuffer();
        while (f != top) {
            if (b.length() > 0) {
                b.insert(0, " \u2192 "); // XXX I18N? just a right-arrow
            }
            b.insert(0, getDisplayName(f));
            f = f.getFolder();
        }
        return b.toString();
    }
    
    private DataFolder getFolder() {
        return (DataFolder)folderList.getSelectedValue();
    }
    
    private void setFolder(DataFolder f) {
        folderList.setSelectedValue(f, true);
    }
    
    private static DataFolder[] findFolders(DataFolder top) {
        List<DataFolder> folders = new ArrayList<DataFolder>();
        // Needs to be DFS, so children(true) is no good
        visit(folders, top);
        folders.remove(0);
        return folders.toArray(new DataFolder[0]);
    }
    
    private static void visit(List<DataFolder> folders, DataFolder f) {
        folders.add(f);
        DataObject[] kids = f.getChildren();
        for (int i = 0; i < kids.length; i++) {
            if (kids[i] instanceof DataFolder) {
                visit(folders, (DataFolder)kids[i]);
            }
        }
    }
    
    private final class CellRenderer extends DefaultListCellRenderer {
        
        public CellRenderer() {}
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            DataFolder f = (DataFolder)value;
            String display = getNestedDisplayName(f);
            return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
        }
        
    }
    
    // --- VISUAL DESIGN OF PANEL ---
    
    @Override
    public void requestFocus() {
        super.requestFocus();
        folderList.requestFocus();
    }
    
    private void initAccessibility(String hint) {
        this.getAccessibleContext().setAccessibleDescription(hint);    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hintsArea = new javax.swing.JTextArea();
        folderScrollPane = new javax.swing.JScrollPane();
        folderList = new javax.swing.JList();
        displayNamePanel = new javax.swing.JPanel();
        displayNameLabel = new javax.swing.JLabel();
        displayNameField = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout(0, 11));

        hintsArea.setBackground(new java.awt.Color(204, 204, 204));
        hintsArea.setEditable(false);
        hintsArea.setFont(javax.swing.UIManager.getFont ("Label.font"));
        hintsArea.setForeground(new java.awt.Color(102, 102, 153));
        hintsArea.setLineWrap(true);
        hintsArea.setText("<hints>");
        hintsArea.setWrapStyleWord(true);
        hintsArea.setDisabledTextColor(javax.swing.UIManager.getColor ("Label.foreground"));
        hintsArea.setEnabled(false);
        hintsArea.setOpaque(false);
        add(hintsArea, java.awt.BorderLayout.NORTH);

        folderList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        folderList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                folderListValueChanged(evt);
            }
        });
        folderScrollPane.setViewportView(folderList);
        folderList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SelectFolderPanel.class, "ACSN_folderList")); // NOI18N
        folderList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectFolderPanel.class, "ACSD_folderList")); // NOI18N

        add(folderScrollPane, java.awt.BorderLayout.CENTER);

        displayNamePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        displayNameLabel.setLabelFor(displayNameField);
        displayNameLabel.setText("<set display name>");
        displayNamePanel.add(displayNameLabel);

        displayNameField.setColumns(30);
        displayNamePanel.add(displayNameField);

        add(displayNamePanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void folderListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_folderListValueChanged
        wiz.fireChangeEvent();
    }//GEN-LAST:event_folderListValueChanged

    public void insertUpdate(DocumentEvent e) {
        // From displayNameField.
        wiz.fireChangeEvent();
    }
    
    public void removeUpdate(DocumentEvent e) {
        // From displayNameField.
        wiz.fireChangeEvent();
    }

    public void changedUpdate(DocumentEvent e) {
        // ignore
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameField;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JPanel displayNamePanel;
    private javax.swing.JList folderList;
    private javax.swing.JScrollPane folderScrollPane;
    private javax.swing.JTextArea hintsArea;
    // End of variables declaration//GEN-END:variables

    public static class SelectFolderWizardPanel implements WizardDescriptor.Panel<ShortcutWizard> {

        private SelectFolderPanel panel;

        private String namePanel;
        private String hintPanel;
        private String displayNameLabelText;
        private DataFolder topPanel;
        private boolean stripAmpsPanel;
        private String propPanel;
        
        public SelectFolderWizardPanel(String name, String hint, String displayNameLabelText, DataFolder top, boolean stripAmps, String prop) {
            this.namePanel = name;
            this.hintPanel = hint;
            this.displayNameLabelText = displayNameLabelText;
            this.topPanel = top;
            this.stripAmpsPanel = stripAmps;
            this.propPanel = prop;
        }
        
        public Component getComponent () {
            return getPanel();
        }
        
        SelectFolderPanel getPanel() {
            if (panel == null) {
                panel = new SelectFolderPanel(this, namePanel, hintPanel, displayNameLabelText, topPanel, stripAmpsPanel, propPanel);
            }
            return panel;
        }

        public HelpCtx getHelp () {
            return HelpCtx.DEFAULT_HELP;
        }

        public boolean isValid () {
            return getPanel().getFolder() != null &&
                getPanel().displayNameField.getText().length() > 0;
        }

        private final ChangeSupport cs = new ChangeSupport(this);
        public final void addChangeListener (ChangeListener l) {
            cs.addChangeListener(l);
        }
        public final void removeChangeListener (ChangeListener l) {
            cs.removeChangeListener(l);
        }
        protected final void fireChangeEvent () {
            cs.fireChange();
        }

        public void readSettings(ShortcutWizard wiz) {
            getPanel().setFolder((DataFolder)wiz.getProperty(getPanel().prop));
            String dn = (String)wiz.getProperty(ShortcutWizard.PROP_DISPLAY_NAME);
            getPanel().displayNameField.setText(dn != null ? dn : ""); // NOI18N
        }
        
        public void storeSettings(ShortcutWizard wiz) {
            DataFolder folder = getPanel().getFolder();
            wiz.putProperty(getPanel().prop, folder);
            wiz.putProperty(ShortcutWizard.PROP_DISPLAY_NAME, getPanel().displayNameField.getText());
        }

    }
}
