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
package org.netbeans.modules.javascript.cdnjs.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.cdnjs.Library;
import org.netbeans.modules.javascript.cdnjs.LibraryProvider;
import org.netbeans.modules.javascript.cdnjs.LibraryUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for customization of CDNJS libraries.
 *
 * @author Jan Stola
 */
public class SelectionPanel extends JPanel implements HelpCtx.Provider {
    /** Panel for searching CDNJS libraries. */
    private SearchPanel searchPanel;
    /** Selected libraries. */
    private final List<Library.Version> libraries;
    /** Model for a table of selected libraries. */
    private final LibraryTableModel tableModel;
    /** Web-root, i.e., the folder the library folder is relative to. */
    private final File webRoot;
    /** Maps the name of the library to its CDNJS meta-data. */
    private final Map<String,Library> libraryInfo = new HashMap<>();
    private static final RequestProcessor RP = new RequestProcessor(SearchPanel.class.getName(), 3);

    /**
     * Creates a new {@code SelectionPanel}.
     * 
     * @param project owning project.
     * @param existingLibraries libraries present in the project already.
     * @param webRoot web-root.
     * @param libraryFolder library folder (relative path from web-root).
     */
    public SelectionPanel(Project project, Library.Version[] existingLibraries, File webRoot, String libraryFolder) {
        assert project != null;
        libraries = new ArrayList<>(Arrays.asList(existingLibraries));
        libraries.sort(new LibraryVersionComparator());
        this.webRoot = webRoot;
        tableModel = new LibraryTableModel();
        initComponents();
        folderField.setText(libraryFolder);
        librariesTable.getSelectionModel().addListSelectionListener(new Listener());
        TableCellRenderer versionColumnRenderer = new VersionColumnRenderer();
        TableColumnModel tableColumnModel = librariesTable.getColumnModel();
        tableColumnModel.getColumn(0).setCellRenderer(new LibraryNameColumnRenderer(project));
        tableColumnModel.getColumn(1).setCellRenderer(versionColumnRenderer);
        tableColumnModel.getColumn(2).setCellRenderer(versionColumnRenderer);
        loadLibraryInfo(existingLibraries);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.javascript.cdnjs.ui.SelectionPanel"); // NOI18N
    }

    /**
     * Returns the selected libraries.
     * 
     * @return selected libraries.
     */
    public Library.Version[] getSelectedLibraries() {
        return libraries.toArray(new Library.Version[0]);
    }

    /**
     * Returns the selected library folder.
     * 
     * @return library folder.
     */
    public String getLibraryFolder() {
        return folderField.getText();
    }

    /**
     * Shows the search panel.
     */
    @NbBundle.Messages({"SelectionPanel.searchDialog.title=Add CDNJS Library"})
    private void showSearchPanel() {
        SearchPanel panel = getSearchPanel();
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                Bundle.SelectionPanel_searchDialog_title(),
                true,
                new Object[] {
                    panel.getAddButton(),
                    panel.getCancelButton()
                },
                panel.getAddButton(),
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null
        );
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() == panel.getAddButton()) {
            Library.Version selectedVersion = panel.getSelectedVersion();
            if (selectedVersion != null) {
                addLibrary(selectedVersion);
            }
        }
    }

    /**
     * Shows the edit panel.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "ES_COMPARING_STRINGS_WITH_EQ",
            justification = "Comparing instances is OK here")
    @NbBundle.Messages({
        "SelectionPanel.editDialog.title=Edit Library",
        "SelectionPanel.editDialog.update=Update",
        "SelectionPanel.editDialog.cancel=Cancel"
    })
    private void showEditPanel() {
        int selectedRow = librariesTable.getSelectedRow();
        Library.Version selectedVersion = libraries.get(selectedRow);
        Library library = libraryInfo.get(selectedVersion.getLibrary().getName());
        EditPanel editPanel = new EditPanel(library, selectedVersion);
        String update = Bundle.SelectionPanel_editDialog_update();
        String cancel = Bundle.SelectionPanel_editDialog_cancel();
        DialogDescriptor descriptor = new DialogDescriptor(
                editPanel,
                Bundle.SelectionPanel_editDialog_title(),
                true,
                new Object[] { update, cancel },
                update,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null
        );
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() == update) {
            Library.Version version = editPanel.getSelection();
            if (version == null) {
                removeSelectedLibraries();
            } else {
                addLibrary(version);
            }
        }
    }

    /**
     * Returns the panel for searching CNDJS libraries.
     * 
     * @return panel for searching CDNJS libraries.
     */
    private SearchPanel getSearchPanel() {
        if (searchPanel == null) {
            searchPanel = new SearchPanel();
        }
        return searchPanel;
    }

    /**
     * Adds the specified library among the selected ones.
     * 
     * @param libraryVersion version of a library to add. 
     */
    private void addLibrary(Library.Version libraryVersion) {
        String newLibraryName = libraryVersion.getLibrary().getName();
        Library.Version versionToReplace = findLibrary(newLibraryName);
        if (versionToReplace != null) {
            libraries.remove(versionToReplace);
        }
        libraries.add(libraryVersion);
        libraries.sort(new LibraryVersionComparator());
        libraryInfo.put(newLibraryName, libraryVersion.getLibrary());
        tableModel.fireTableDataChanged();
    }

    /**
     * Finds a selected version of a library with the specified name.
     * 
     * @param libraryName name of the library to find.
     * @return selected version of a library or {@code null} if there
     * is no version selected for a given library.
     */
    private Library.Version findLibrary(String libraryName) {
        for (Library.Version existingVersion : libraries) {
            String existingLibraryName = existingVersion.getLibrary().getName();
            if (libraryName.equals(existingLibraryName)) {
                return existingVersion;
            }
        }
        return null;
    }

    /**
     * Removes the libraries selected in the table from the set of selected libraries.
     */
    private void removeSelectedLibraries() {
        int[] selectedRows = librariesTable.getSelectedRows();
        int length = selectedRows.length;
        for (int i=1; i<=length; i++) {
            libraries.remove(selectedRows[length-i]);
        }
        tableModel.fireTableDataChanged();
    }

    /**
     * Shows the dialog for the selection of the library folder.
     */
    @NbBundle.Messages({"SelectionPanel.browseDialog.title=Select directory for JS libraries"})
    private void showBrowseDialog() {
        File libraryFolder = PropertyUtils.resolveFile(webRoot, getLibraryFolder());
        File selectedDir = new FileChooserBuilder(SelectionPanel.class)
                .setDirectoriesOnly(true)
                .setTitle(Bundle.SelectionPanel_browseDialog_title())
                .setDefaultWorkingDirectory(libraryFolder)
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (selectedDir != null) {
            String relativePath = PropertyUtils.relativizeFile(webRoot, selectedDir);
            String path;
            if (relativePath == null) {
                path = selectedDir.getAbsolutePath();
            } else {
                path = relativePath;
            }
            folderField.setText(path);
        }
    }

    /**
     * Loads the CDNJS meta-data about the existing libraries.
     * 
     * @param existingLibraries libraries already present in the project.
     */
    private void loadLibraryInfo(final Library.Version[] existingLibraries) {
        RP.execute(() -> {
            LibraryProvider provider = LibraryProvider.getInstance();
            for (Library.Version libraryVersion : existingLibraries) {
                String libraryName = libraryVersion.getLibrary().getName();
                Library library = provider.loadLibrary(libraryName);
                if (library != null) {
                    SwingUtilities.invokeLater(() -> updateLibraryInfo(library));
                }
            }
        });
    }

    /**
     * Updates the CDNJS meta-data about the given library.
     * 
     * @param libraryName name of the library.
     * @param foundLibraries search result for a search term equal to the name of the library.
     */
    void updateLibraryInfo(Library foundLibrary) {
        libraryInfo.put(foundLibrary.getName(), foundLibrary);
        tableModel.fireTableDataChanged();
    }

    /**
     * Updates the state of the Update button.
     */
    void updateUpdateButton() {
        boolean updatePossible = false;
        for (int row : librariesTable.getSelectedRows()) {
            Object value = tableModel.getValueAt(row, 2);
            if (value != VersionColumnRenderer.CHECKING
                    && value != VersionColumnRenderer.UNKNOWN
                    && value != VersionColumnRenderer.UP_TO_DATE) {
                updatePossible = true;
                break;
            }
        }
        updateButton.setEnabled(updatePossible);
    }

    /**
     * Updates the selected libraries to the latest version.
     */
    void updateSelectedLibraries() {
        for (int row : librariesTable.getSelectedRows()) {
            Library.Version currentVersion = libraries.get(row);
            String libraryName = currentVersion.getLibrary().getName();
            Library library = libraryInfo.get(libraryName);
            Library.Version latestVersion = library.getVersions()[0];
            if (!currentVersion.getName().equals(latestVersion.getName())) {
                addLibrary(latestVersion);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        librariesScrollPane = new javax.swing.JScrollPane();
        librariesTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        folderLabel = new javax.swing.JLabel();
        folderField = new javax.swing.JTextField();
        folderInfoLabel = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        librariesTable.setModel(tableModel);
        librariesScrollPane.setViewportView(librariesTable);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.addButton.text")); // NOI18N
        addButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.removeButton.text")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(folderLabel, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.folderLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(folderInfoLabel, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.folderInfoLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.editButton.text")); // NOI18N
        editButton.setEnabled(false);
        editButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(updateButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "SelectionPanel.updateButton.text")); // NOI18N
        updateButton.setEnabled(false);
        updateButton.addActionListener(formListener);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(librariesScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(folderLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(folderInfoLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(folderField))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(addButton)
                        .addComponent(removeButton, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(browseButton)
                    .addComponent(editButton)
                    .addComponent(updateButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(0, 0, 0))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, browseButton, editButton, removeButton, updateButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updateButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(librariesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(folderLabel)
                    .addComponent(folderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(folderInfoLabel))
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == addButton) {
                SelectionPanel.this.addButtonActionPerformed(evt);
            }
            else if (evt.getSource() == removeButton) {
                SelectionPanel.this.removeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == browseButton) {
                SelectionPanel.this.browseButtonActionPerformed(evt);
            }
            else if (evt.getSource() == editButton) {
                SelectionPanel.this.editButtonActionPerformed(evt);
            }
            else if (evt.getSource() == updateButton) {
                SelectionPanel.this.updateButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        showSearchPanel();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        removeSelectedLibraries();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        showBrowseDialog();
    }//GEN-LAST:event_browseButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        showEditPanel();
    }//GEN-LAST:event_editButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        updateSelectedLibraries();
    }//GEN-LAST:event_updateButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton editButton;
    private javax.swing.JTextField folderField;
    private javax.swing.JLabel folderInfoLabel;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JScrollPane librariesScrollPane;
    private javax.swing.JTable librariesTable;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Selection listener for libraries table.
     */
    class Listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int selectedRows = librariesTable.getSelectedRowCount();
            editButton.setEnabled(selectedRows == 1);
            removeButton.setEnabled(selectedRows != 0);
            updateUpdateButton();
        }
    }

    /**
     * Comparator of {@code Library.Version}s.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE",
            justification = "No need to be serializable")
    static class LibraryVersionComparator implements Comparator<Library.Version> {
        @Override
        public int compare(Library.Version o1, Library.Version o2) {
            String name1 = o1.getLibrary().getName();
            String name2 = o2.getLibrary().getName();
            return name1.compareTo(name2);
        }        
    }

    /**
     * Renderer of the library names columns.
     */
    static class LibraryNameColumnRenderer extends DefaultTableCellRenderer {

        @StaticResource
        private static final String BROKEN_ICON = "org/netbeans/modules/javascript/cdnjs/ui/resources/broken.png"; // NOI18N

        private final Project project;


        LibraryNameColumnRenderer(Project project) {
            assert project != null;
            this.project = project;
        }

        @NbBundle.Messages("SelectionPanel.version.broken=Broken")
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String icon = null;
            String toolTip = null;
            if (value instanceof Library.Version) {
                Library.Version libraryVersion = (Library.Version) value;
                value = libraryVersion.getLibrary().getName();
                if (LibraryUtils.isBroken(project, libraryVersion)) {
                    icon = BROKEN_ICON;
                    toolTip = Bundle.SelectionPanel_version_broken();
                }
            }
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setToolTipText(toolTip);
            if (icon == null) {
                setIcon(null);
            } else {
                setIcon(ImageUtilities.loadImageIcon(icon, false));
            }
            return this;
        }

    }

    /**
     * Renderer of the version columns.
     */
    static class VersionColumnRenderer extends DefaultTableCellRenderer {

        static final Object UP_TO_DATE = new Object();
        static final Object CHECKING = new Object();
        static final Object UNKNOWN = new Object();

        @StaticResource
        private static final String UP_TO_DATE_ICON = "org/netbeans/modules/javascript/cdnjs/ui/resources/uptodate.gif"; // NOI18N
        @StaticResource
        private static final String CHECKING_ICON = "org/netbeans/modules/javascript/cdnjs/ui/resources/checking.png"; // NOI18N
        @StaticResource
        private static final String UNKNOWN_ICON = "org/netbeans/modules/javascript/cdnjs/ui/resources/unknown.png"; // NOI18N


        @Override
        @NbBundle.Messages({
            "SelectionPanel.version.unknown=Version information not available",
            "SelectionPanel.version.checking=Checking for updates ...",
            "SelectionPanel.version.uptodate=Up to date"
        })
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String icon = null;
            String toolTip = null;
            if (value == UNKNOWN) {
                icon = UNKNOWN_ICON;
                toolTip = Bundle.SelectionPanel_version_unknown();
            } else if (value == CHECKING) {
                icon = CHECKING_ICON;
                toolTip = Bundle.SelectionPanel_version_checking();
            } else if (value == UP_TO_DATE) {
                icon = UP_TO_DATE_ICON;
                toolTip = Bundle.SelectionPanel_version_uptodate();
            }
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            setToolTipText(toolTip);
            if (icon == null) {
                setIcon(null);
            } else {
                setText(null);
                setIcon(ImageUtilities.loadImageIcon(icon, false));
            }
            return this;
        }
        
    }

    /**
     * Model for the libraries table.
     */
    class LibraryTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return libraries.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        @NbBundle.Messages({
            "SelectionPanel.table.libraryColumn=Library",
            "SelectionPanel.table.versionColumn=Version",
            "SelectionPanel.table.latestVersionColumn=Latest Version"
        })
        public String getColumnName(int column) {
            String columnName;
            switch (column) {
                case 0: columnName = Bundle.SelectionPanel_table_libraryColumn(); break;
                case 1: columnName = Bundle.SelectionPanel_table_versionColumn(); break;
                case 2: columnName = Bundle.SelectionPanel_table_latestVersionColumn(); break;
                default: throw new IllegalArgumentException();
            }
            return columnName;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Library.Version libraryVersion = libraries.get(rowIndex);
            Object value;
            switch (columnIndex) {
                case 0: value = libraryVersion; break;
                case 1: value = libraryVersion.getName(); break;
                case 2:
                    String libraryName = libraryVersion.getLibrary().getName();
                    Library library = libraryInfo.get(libraryName);
                    if (library == null || library.getVersions() == null || library.getVersions().length == 0) {
                        value = libraryInfo.containsKey(libraryName)
                                ? VersionColumnRenderer.UNKNOWN
                                : VersionColumnRenderer.CHECKING;
                    } else {
                        String latestVersion = library.getVersions()[0].getName();
                        String currentVersion = libraryVersion.getName();
                        value = currentVersion.equals(latestVersion)
                                ? VersionColumnRenderer.UP_TO_DATE : latestVersion;
                    }
                    break;
                default: throw new IllegalArgumentException();
            }
            return value;
        }

    }

}
