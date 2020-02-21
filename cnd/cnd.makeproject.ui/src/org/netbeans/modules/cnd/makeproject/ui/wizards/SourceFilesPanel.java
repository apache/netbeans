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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FileObjectFilter;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class SourceFilesPanel extends javax.swing.JPanel {

    private final Color defaultTextFieldFg;
    private final List<FolderEntry> sourceData = new ArrayList<>();
    private final List<FolderEntry> testData = new ArrayList<>();
    private SourceFileTable sourceFileTable = null;
    private final SourceFileTable testFileTable = null;
    private String baseDir;
    private String wd;
    private final SourceFoldersDescriptorPanel controller;
    private final Project project;

    public SourceFilesPanel(Project project) {
        this.controller = null;
        this.project = project;
        initComponents();
        defaultTextFieldFg = excludePatternTextField.getForeground();
        init();
    }

    /** Creates new form SourceFilesPanel */
    /*package*/ SourceFilesPanel(SourceFoldersDescriptorPanel controller) {
        this.controller = controller;
        this.project = null;
        initComponents();
        defaultTextFieldFg = excludePatternTextField.getForeground();
        init();
    }

    private void init() {

        scrollPane.getViewport().setBackground(getBackground());

        getAccessibleContext().setAccessibleDescription(getString("SourceFilesPanelAD"));
        addButton.getAccessibleContext().setAccessibleDescription(getString("AddButtonAD"));
        deleteButton.getAccessibleContext().setAccessibleDescription(getString("DeleteButtonAD"));
        refresh();
        initFocus();
        excludePatternTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }
        });
    }

    private void update() {
        String excludeStr = excludePatternTextField.getText();
        try {
            Pattern.compile(excludeStr);
            excludePatternTextField.setForeground(defaultTextFieldFg);
        } catch (PatternSyntaxException ex) {
            excludePatternTextField.setForeground(Color.RED);
        }
        if (controller != null) {
            controller.stateChanged(null);
        }
    }

    public void setSeed(String baseDir, String wd) {
        this.baseDir = baseDir;
        this.wd = wd;
    }

    public void setFoldersFilter(String regex) {
        excludePatternTextField.setText(regex);
    }

    public String getFoldersFilter() {
        return excludePatternTextField.getText();
    }

    public void setResolveSymLinks(boolean resolve) {
        resolveSymLink.setSelected(resolve);
    }
    
    public boolean getResolveSymLinks() {
        return resolveSymLink.isSelected();
    }

    public FileObjectFilter getFileFilter() {
        Pattern excludePattern = null;

        String excludeStr = excludePatternTextField.getText().trim();
        if (!excludeStr.isEmpty()) {
            try {
                excludePattern = Pattern.compile(excludeStr.trim());
            } catch (PatternSyntaxException ex) {
                // ignore
            }
        }

        if (excludePattern == null) {
            // by default exclude nothing
            excludePattern = Pattern.compile("^$"); // NOI18N
        }

        return new RegexpExcludeFileFilter(excludePattern);
    }

    public final void initFocus() {
        CndUIUtilities.requestFocus(addButton);
    }

    public List<FolderEntry> getSourceListData() {
        return sourceData;
    }

    public List<FolderEntry> getTestListData() {
        return testData;
    }

    private class TargetSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            validateSelection();
        }
    }

    private void validateSelection() {
        addButton.setEnabled(true);
        if (sourceData.isEmpty() || sourceFileTable.getSelectedRow() < 0) {
            deleteButton.setEnabled(false);
        } else {
            deleteButton.setEnabled(true);
        }

//        addButton1.setEnabled(true);
//        if (testData.isEmpty() || testFileTable.getSelectedRow() < 0) {
//            deleteButton1.setEnabled(false);
//        } else {
//            deleteButton1.setEnabled(true);
//        }
    }

    private void refresh() {
        scrollPane.setViewportView(sourceFileTable = new SourceFileTable(sourceData, getString("TABLE_COLUMN_SOURCE_TXT")));
        sourceFilesLabel.setLabelFor(sourceFileTable);
//        scrollPane1.setViewportView(testFileTable = new SourceFileTable(testData, getString("TABLE_COLUMN_TEST_TXT")));
//        sourceFilesLabel1.setLabelFor(testFileTable);
        validateSelection();
    }

    private static final class RegexpExcludeFileFilter implements FileObjectFilter {

        private final Pattern excludePattern;

        public RegexpExcludeFileFilter(Pattern excludeFilter) {
            this.excludePattern = excludeFilter;
        }

        @Override
        public boolean accept(FileObject pathname) {
            return !excludePattern.matcher(pathname.getNameExt()).find();
        }
    }

    private final class SourceFileTable extends JTable {

        public SourceFileTable(List<FolderEntry> data, String columnTitle) {
            //setTableHeader(null); // Hides table headers
            setModel(new MyTableModel(data, columnTitle));
            // Left align table header
            ((DefaultTableCellRenderer) getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

            getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            getSelectionModel().addListSelectionListener(new TargetSelectionListener());
            getAccessibleContext().setAccessibleDescription(getString("SourceFileTableAD"));
        }

        @Override
        public boolean getShowHorizontalLines() {
            return false;
        }

        @Override
        public boolean getShowVerticalLines() {
            return false;
        }
    }

    private static final class MyTableModel extends DefaultTableModel {
        private final List<FolderEntry> data;
        private final String columnTitle;

        public MyTableModel(List<FolderEntry> data, String columnTitle) {
            this.data = data;
            this.columnTitle = columnTitle;
        }

        @Override
        public String getColumnName(int col) {
            return " " + columnTitle; // NOI18N
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            if (data == null) {
                return 0;
            }
            return data.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (data == null) {
                return null;
            }
            return data.get(row).getFolderName();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
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

        sourceFilesLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        excludePatternLabel = new javax.swing.JLabel();
        excludePatternTextField = new javax.swing.JTextField();
        seeAlsoLabel = new javax.swing.JLabel();
        resolveSymLink = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(450, 350));

        sourceFilesLabel.setLabelFor(list);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(sourceFilesLabel, bundle.getString("SourceFileFoldersLbl")); // NOI18N

        scrollPane.setViewportView(list);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, bundle.getString("AddButtonTxt")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, bundle.getString("DeleteButtonTxt")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        excludePatternLabel.setLabelFor(excludePatternTextField);
        org.openide.awt.Mnemonics.setLocalizedText(excludePatternLabel, org.openide.util.NbBundle.getMessage(SourceFilesPanel.class, "SourceFilesPanel.excludePatternLabel.text")); // NOI18N

        seeAlsoLabel.setText(org.openide.util.NbBundle.getMessage(SourceFilesPanel.class, "SourceFilesPanel.seeAlsoLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resolveSymLink, org.openide.util.NbBundle.getMessage(SourceFilesPanel.class, "RESOLVE_SYM_LINK")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                    .addComponent(excludePatternTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(deleteButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(excludePatternLabel)
                    .addComponent(seeAlsoLabel)
                    .addComponent(sourceFilesLabel)
                    .addComponent(resolveSymLink))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sourceFilesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton))
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resolveSymLink)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excludePatternLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excludePatternTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seeAlsoLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void deleteFile(List<FolderEntry> data, SourceFileTable table) {
        int index = table.getSelectedRow();
        if (index < 0 || index >= data.size()) {
            return;
        }
        data.remove(index);
        refresh();
        if (data.size() > 0) {
            if (data.size() > index) {
                table.getSelectionModel().setSelectionInterval(index, index);
            } else {
                table.getSelectionModel().setSelectionInterval(index - 1, index - 1);
            }
        }
    }

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
       deleteFile(sourceData, sourceFileTable);
    }//GEN-LAST:event_deleteButtonActionPerformed

    private String lastSelectedPath;

    private void addFile(List<FolderEntry> data) {
        String seed = null;
        if (lastSelectedPath  != null) {
            seed = lastSelectedPath;
        }
        if (seed == null) {
            if (wd != null && wd.length() > 0 && !CndPathUtilities.isPathAbsolute(wd)) {
                seed = baseDir + File.separator + wd;
            } else if (wd != null) {
                seed = wd;
            } else if (baseDir != null) {
                seed = baseDir;
            } else if (controller != null) {
                seed = WizardConstants.PROPERTY_NATIVE_PROJ_DIR.get(controller.getWizardDescriptor());
            }
        }
        //FileChooser fileChooser = new FileChooser(title, buttonText, FileChooser.DIRECTORIES_ONLY, null, seed, true);
        String title = getString("FOLDER_CHOOSER_TITLE_TXT");
        String buttonText = getString("FOLDER_CHOOSER_BUTTON_TXT");
        JFileChooser fileChooser;
        if (project != null) {
            fileChooser = NewProjectWizardUtils.createFileChooser(project, title, buttonText, JFileChooser.DIRECTORIES_ONLY, null, seed, true);
        } else {
            fileChooser = NewProjectWizardUtils.createFileChooser(controller.getWizardDescriptor(), title, buttonText, JFileChooser.DIRECTORIES_ONLY, null, seed, true);
        }
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        if (!fileChooser.getSelectedFile().exists() || !fileChooser.getSelectedFile().isDirectory()) {
            // FIXUP: error message
            return;
        }
        File file = fileChooser.getSelectedFile();
        lastSelectedPath = file.getAbsolutePath();
        FileObject fo = CndFileUtils.toFileObject(file);
        data.add(new FolderEntry(fo, CndPathUtilities.toAbsoluteOrRelativePath(baseDir, fileChooser.getSelectedFile().getPath()))); //TODO:fullRemote use base dir file object
        refresh();
    }

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
       addFile(sourceData);
    }//GEN-LAST:event_addButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel excludePatternLabel;
    private javax.swing.JTextField excludePatternTextField;
    private javax.swing.JList list;
    private javax.swing.JCheckBox resolveSymLink;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel seeAlsoLabel;
    private javax.swing.JLabel sourceFilesLabel;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getMessage(SourceFilesPanel.class, s);
    }
}
