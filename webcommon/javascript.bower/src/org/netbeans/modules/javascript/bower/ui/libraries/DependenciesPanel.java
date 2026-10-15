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

package org.netbeans.modules.javascript.bower.ui.libraries;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.bower.file.BowerJson;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for customization of one set (regular/development) of Bower dependencies
 *
 * @author Jan Stola
 */
public class DependenciesPanel extends javax.swing.JPanel {
    /** Request processor for this class. */
    private static final RequestProcessor RP = new RequestProcessor(DependenciesPanel.class.getName(), 3);
    /** All selected dependencies (not just of the type customized by this panel). */
    private Dependencies allDependencies;
    /** Selected dependencies. */
    private List<Dependency> dependencies;
    /** Model for a table of selected dependencies. */
    private final DependencyTableModel tableModel;
    /** Determines whether the installed libraries were set. */
    private boolean installedLibrariesSet;
    /** Owning project. */
    private Project project;
    /** Panel for searching Bower libraries. */
    private SearchPanel searchPanel;
    /** Type of dependencies customizer by this panel. */
    private Dependency.Type dependencyType;

    /**
     * Creates a new {@code DependenciesPanel}.
     */
    public DependenciesPanel() {
        tableModel = new DependencyTableModel();
        initComponents();
        table.getSelectionModel().addListSelectionListener(new Listener());
        TableCellRenderer versionColumnRenderer = new VersionColumnRenderer();
        TableColumnModel tableColumnModel = table.getColumnModel();
        tableColumnModel.getColumn(1).setCellRenderer(versionColumnRenderer);
        tableColumnModel.getColumn(2).setCellRenderer(versionColumnRenderer);
        tableColumnModel.getColumn(3).setCellRenderer(versionColumnRenderer);
        GroupLayout layout = (GroupLayout)getLayout();
        layout.setHonorsVisibility(dummyButton, false);
        updateButtons();
    }

    /**
     * Sets the owning project.
     * 
     * @param project owning project.
     */
    void setProject(Project project) {
        this.project = project;
    }

    /**
     * Sets the type of dependencies customized by this panel.
     * 
     * @param dependencyType type of dependencies customized by this panel.
     */
    void setDependencyType(Dependency.Type dependencyType) {
        this.dependencyType = dependencyType;

        moveRegular.setVisible(dependencyType != Dependency.Type.REGULAR);
        moveDevelopment.setVisible(dependencyType != Dependency.Type.DEVELOPMENT);
        dummyButton.setVisible(false);

        // Make sure that the button column has the same width even
        // when the widest move button is hidden
        String hiddenButtonText = null;
        switch (dependencyType) {
            case REGULAR: hiddenButtonText = moveRegular.getText(); break;
            case DEVELOPMENT: hiddenButtonText = moveDevelopment.getText(); break;
        }
        dummyButton.setText(hiddenButtonText);
    }

    /**
     * Returns the type of dependencies customized by this panel.
     * 
     * @return type of dependencies customized by this panel.
     */
    Dependency.Type getDependencyType() {
        return dependencyType;
    }

    /**
     * Sets the existing dependencies.
     * 
     * @param allDependencies existing dependencies.
     */
    void setDependencies(Dependencies allDependencies) {
        this.allDependencies = allDependencies;
        this.dependencies = allDependencies.forType(dependencyType);
        sortDependencies();
        Set<String> dependencyNames = new HashSet<>();
        for (Dependency dependency : dependencies) {
            dependencyNames.add(dependency.getName());
        }
        loadDependencyInfo(dependencyNames);
        allDependencies.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                sortDependencies();
                tableModel.fireTableDataChanged();
            }
        });
    }

    /**
     * Returns the selected dependencies.
     * 
     * @return selected dependencies.
     */
    List<Dependency> getSelectedDependencies() {
        return dependencies;
    }

    /**
     * Sorts the list of dependencies.
     */
    private void sortDependencies() {
        dependencies.sort(new DependencyComparator());
    }

    /**
     * Loads information about given libraries/dependencies/packages.
     * 
     * @param dependencyNames names of the dependencies to load information about.
     */
    private void loadDependencyInfo(final Set<String> dependencyNames) {
        if (RP.isRequestProcessorThread()) {
            LibraryProvider provider = LibraryProvider.forProject(project);
            for (String dependencyName : dependencyNames) {
                Library library = provider.libraryDetails(dependencyName, false);
                updateDependencyInfo(dependencyName, library);
            }
        } else {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    loadDependencyInfo(dependencyNames);
                }
            });
        }
    }

    /**
     * Updates the view according to the newly loaded library/dependency information.
     * 
     * @param libraryName name of the library.
     * @param library information about the library.
     */
    private void updateDependencyInfo(final String libraryName, final Library library) {
        if (EventQueue.isDispatchThread()) {
            allDependencies.dependencyInfo.put(libraryName, library);
            tableModel.fireTableRowsUpdated(0, dependencies.size()-1);
            updateButtons();
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateDependencyInfo(libraryName, library);
                }
            });
        }
    }

    /**
     * Sets the map of the installed libraries.
     * 
     * @param installedLibraries installed libraries (maps name
     * of the library/package to the name of the installed version).
     */
    void setInstalledLibraries(Map<String,String> installedLibraries) {
        this.installedLibrariesSet = true;
        if (installedLibraries != null) {
            for (Dependency dependency : dependencies) {
                String name = dependency.getName();
                String installedVersion = installedLibraries.get(name);
                dependency.setInstalledVersion(installedVersion);
            }
        }
        tableModel.fireTableRowsUpdated(0, dependencies.size()-1);
    }

    /**
     * Updates the state of the buttons.
     */
    final void updateButtons() {
        int selectedRows = table.getSelectedRowCount();
        editButton.setEnabled(selectedRows == 1);
        removeButton.setEnabled(selectedRows > 0);
        moveRegular.setEnabled(selectedRows > 0);
        moveDevelopment.setEnabled(selectedRows > 0);

        boolean updateAvailable = false;
        for (int selectedRow : table.getSelectedRows()) {
            Dependency dependency = dependencies.get(selectedRow);
            String name = dependency.getName();
            Library library = allDependencies.dependencyInfo.get(name);
            if (library != null) {
                Library.Version latestVersion = library.getLatestVersion();
                if (latestVersion != null && !Objects.equals(latestVersion.getName(), dependency.getInstalledVersion())) {
                    updateAvailable = true;
                    break;
                }
            }
        }
        updateButton.setEnabled(updateAvailable);
    }

    /**
     * Returns the panel for searching Bower libraries.
     * 
     * @return panel for searching Bower libraries.
     */
    private SearchPanel getSearchPanel() {
        if (searchPanel == null) {
            searchPanel = new SearchPanel(LibraryProvider.forProject(project));
        }
        return searchPanel;
    }

    /**
     * Shows the search panel.
     */
    @NbBundle.Messages({"DependenciesPanel.searchDialog.title=Add Bower package"})
    private void showSearchDialog() {
        SearchPanel panel = getSearchPanel();
        panel.activate();
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                Bundle.DependenciesPanel_searchDialog_title(),
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
        panel.deactivate();
        if (descriptor.getValue() == panel.getAddButton()) {
            String libraryName = panel.getSelectedLibrary();
            if (libraryName != null) {
                String requiredVersion = panel.getRequiredVersion();
                String installedVersion = panel.getInstalledVersion();
                addLibrary(libraryName, requiredVersion, installedVersion);
            }
        }
    }

    /**
     * Shows the search panel.
     */
    @NbBundle.Messages({
        "DependenciesPanel.editDialog.title=Edit Bower package",
        "DependenciesPanel.editDialog.update=Update",
        "DependenciesPanel.editDialog.cancel=Cancel"
    })
    private void showEditDialog() {
        EditPanel panel = new EditPanel();
        int border = LayoutStyle.getInstance().getContainerGap(panel, SwingConstants.NORTH, null);
        panel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        panel.setLibraryProvider(LibraryProvider.forProject(project));

        int selectedRow = table.getSelectedRow();
        Dependency dependency = dependencies.get(selectedRow);
        panel.setDependency(dependency);

        String update = Bundle.DependenciesPanel_editDialog_update();
        String cancel = Bundle.DependenciesPanel_editDialog_cancel();
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                Bundle.DependenciesPanel_editDialog_title(),
                true,
                new Object[] { update, cancel},
                update,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null
        );
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (update.equals(descriptor.getValue())) {
            addLibrary(dependency.getName(), panel.getRequiredVersion(), panel.getInstalledVersion());
        }
    }

    /**
     * Adds the given selected dependency.
     * 
     * @param libraryName library/package name.
     * @param requiredVersion required version of the library/package.
     * @param installedVersion installed version of the library/package.
     */
    private void addLibrary(String libraryName, String requiredVersion, String installedVersion) {
        int index = findDependency(libraryName);
        Dependency dependency;
        if (index == -1) { // Add
            if (!checkOtherDependencyTypes(libraryName)) {
                return;
            }
            dependency = new Dependency(libraryName);
            dependencies.add(dependency);
            loadDependencyInfo(Collections.singleton(libraryName));
            sortDependencies();
        } else { // Edit
            dependency = dependencies.get(index);
        }
        dependency.setRequiredVersion(requiredVersion);
        dependency.setInstalledVersion(installedVersion);
        if (index == -1) { // Add
            tableModel.fireTableDataChanged();
            index = findDependency(libraryName);
            table.getSelectionModel().setSelectionInterval(index, index);
        } else { // Edit
            tableModel.fireTableRowsUpdated(0, dependencies.size()-1);
        }
    }

    /**
     * Checks whether there is already dependency of the same name but
     * of a different type. If so then a dialog is displayed to determine
     * what should happen.
     * 
     * @param libraryName name of the dependency to check.
     * @return {@code true} when the new dependency should be added,
     * returns {@code false} when the user decided to cancel addition
     * of the dependency.
     */
    @NbBundle.Messages({
        "DependenciesPanel.otherDependencyTitle=Another Dependency Type",
        "# {0} - library name",
        "# {1} - other dependencies message",
        "DependenciesPanel.otherDependencyWarning=There is another type "
                + "of dependency for \"{0}\" package. "
                + "{1}Do you want to remove these existing dependencies?",
        "DependenciesPanel.otherRegularDependency=a regular",
        "DependenciesPanel.otherDevelopmentDependency=a development",
        "# {0} - type of dependency",
        "DependenciesPanel.alsoDependency=It is also {0} dependency. ",
        "DependenciesPanel.addAndKeep=Add and Keep Existing",
        "DependenciesPanel.addAndRemove=Add and Remove Existing",
        "DependenciesPanel.cancel=Cancel"
    })
    private boolean checkOtherDependencyTypes(String libraryName) {
        List<Dependency.Type> types = allDependencies.otherDependencyTypes(libraryName, dependencyType);
        if (!types.isEmpty()) {
            StringBuilder dependencyTypesMessage = new StringBuilder();
            for (Dependency.Type type : types) {
                String dependencyTypeMessage;
                switch(type) {
                    case REGULAR: dependencyTypeMessage = Bundle.DependenciesPanel_otherRegularDependency(); break;
                    case DEVELOPMENT: dependencyTypeMessage = Bundle.DependenciesPanel_otherDevelopmentDependency(); break;
                    default: throw new InternalError();
                }
                dependencyTypesMessage.append(Bundle.DependenciesPanel_alsoDependency(dependencyTypeMessage));
            }
            String message = Bundle.DependenciesPanel_otherDependencyWarning(libraryName, dependencyTypesMessage);
            NotifyDescriptor descriptor = new NotifyDescriptor(
                    message,
                    Bundle.DependenciesPanel_otherDependencyTitle(),
                    -1,
                    NotifyDescriptor.INFORMATION_MESSAGE,
                    new Object[] {
                        Bundle.DependenciesPanel_addAndRemove(),
                        Bundle.DependenciesPanel_addAndKeep(),
                        Bundle.DependenciesPanel_cancel()
                    },
                    Bundle.DependenciesPanel_addAndRemove()
            );
            DialogDisplayer.getDefault().notify(descriptor);
            Object retVal = descriptor.getValue();
            if (Bundle.DependenciesPanel_addAndKeep().equals(retVal)){
                return true;
            } else if (Bundle.DependenciesPanel_addAndRemove().equals(retVal)) {
                allDependencies.removeOtherDependencies(libraryName, dependencyType);
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Finds a dependency with the specified name.
     * 
     * @param name name of the dependency to find.
     * @return index of the dependency with the specified name or -1 if there
     * is no such dependency.
     */
    private int findDependency(String name) {
        int index = -1;
        for (int i=0; i<dependencies.size(); i++) {
            Dependency dependency = dependencies.get(i);
            if (dependency.getName().equals(name)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * Removes the selected dependencies.
     */
    private void removeSelectedDependencies() {
        int[] selectedRows = table.getSelectedRows();
        for (int i=selectedRows.length-1; i>=0; i--) {
            dependencies.remove(selectedRows[i]);
        }
        tableModel.fireTableDataChanged();
    }

    /**
     * Updates the selected dependencies to the latest version.
     */
    private void updateSelectedDependencies() {
        int[] selectedRows = table.getSelectedRows();
        for (int selectedRow : selectedRows) {
            Dependency dependency = dependencies.get(selectedRow);
            String name = dependency.getName();
            Library library = allDependencies.dependencyInfo.get(name);
            if (library != null) {
                Library.Version version = library.getLatestVersion();
                if (version != null) {
                    String oldVersion = dependency.getInstalledVersion();
                    String newVersion = version.getName();
                    if (!Objects.equals(oldVersion, newVersion)) {
                        dependency.setInstalledVersion(newVersion);
                        if (Objects.equals(dependency.getRequiredVersion(), oldVersion)) {
                            dependency.setRequiredVersion(newVersion);
                        }
                    }
                }
            }
        }
        tableModel.fireTableRowsUpdated(0, dependencies.size()-1);
        updateButtons();
    }

    /**
     * Changes the type of the selected dependencies.
     * 
     * @param targetType target type of the dependencies.
     */
    private void moveSelectedDependencies(Dependency.Type targetType) {
        List<Dependency> toMove = new ArrayList<>();
        int[] selectedRows = table.getSelectedRows();
        for (int i=selectedRows.length-1; i>=0; i--) {
            Dependency dependency = dependencies.remove(selectedRows[i]);
            toMove.add(dependency);
        }
        allDependencies.addDependencies(toMove, targetType);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        moveRegular = new javax.swing.JButton();
        moveDevelopment = new javax.swing.JButton();
        moveLabel = new javax.swing.JLabel();
        dummyButton = new javax.swing.JButton();

        table.setModel(tableModel);
        scrollPane.setViewportView(table);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.editButton.text")); // NOI18N
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(updateButton, org.openide.util.NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.updateButton.text")); // NOI18N
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(moveRegular, org.openide.util.NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.moveRegular.text")); // NOI18N
        moveRegular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveRegularActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(moveDevelopment, org.openide.util.NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.moveDevelopment.text")); // NOI18N
        moveDevelopment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDevelopmentActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(moveLabel, org.openide.util.NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.moveLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton)
                    .addComponent(editButton)
                    .addComponent(removeButton)
                    .addComponent(updateButton)
                    .addComponent(moveRegular)
                    .addComponent(moveDevelopment)
                    .addComponent(moveLabel)
                    .addComponent(dummyButton))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, dummyButton, editButton, moveDevelopment, moveRegular, removeButton, updateButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updateButton)
                        .addGap(18, 18, 18)
                        .addComponent(moveLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveRegular)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveDevelopment)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dummyButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(scrollPane))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        showSearchDialog();
    }//GEN-LAST:event_addButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        showEditDialog();
    }//GEN-LAST:event_editButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        removeSelectedDependencies();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        updateSelectedDependencies();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void moveRegularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveRegularActionPerformed
        moveSelectedDependencies(Dependency.Type.REGULAR);
    }//GEN-LAST:event_moveRegularActionPerformed

    private void moveDevelopmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDevelopmentActionPerformed
        moveSelectedDependencies(Dependency.Type.DEVELOPMENT);
    }//GEN-LAST:event_moveDevelopmentActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton dummyButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton moveDevelopment;
    private javax.swing.JLabel moveLabel;
    private javax.swing.JButton moveRegular;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Comparator of {@code Dependency} objects.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE",
            justification = "No need to be serializable")
    static class DependencyComparator implements Comparator<Dependency> {
        @Override
        public int compare(Dependency o1, Dependency o2) {
            String name1 = o1.getName();
            String name2 = o2.getName();
            return name1.compareTo(name2);
        }        
    }

    /**
     * Renderer of the version columns.
     */
    static class VersionColumnRenderer extends DefaultTableCellRenderer {
        static final Object UP_TO_DATE = new Object();
        static final Object CHECKING = new Object();
        static final Object UNKNOWN = new Object();
        static final Object NO_VERSION = new Object();

        @Override
        @NbBundle.Messages({
            "DependenciesPanel.version.unknown=Version information not available",
            "DependenciesPanel.version.checking=Checking ...",
            "DependenciesPanel.version.uptodate=Up to date",
            "DependenciesPanel.version.noversion=No version installed"
        })
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String icon = null;
            String toolTip = null;
            if (value == UNKNOWN) {
                icon = "org/netbeans/modules/javascript/bower/ui/resources/unknown.png"; // NOI18N
                toolTip = Bundle.DependenciesPanel_version_unknown();
            } else if (value == CHECKING) {
                icon = "org/netbeans/modules/javascript/bower/ui/resources/checking.png"; // NOI18N
                toolTip = Bundle.DependenciesPanel_version_checking();
            } else if (value == UP_TO_DATE) {
                icon = "org/netbeans/modules/javascript/bower/ui/resources/uptodate.gif"; // NOI18N
                toolTip = Bundle.DependenciesPanel_version_uptodate();
            } else if (value == NO_VERSION) {
                icon = "org/netbeans/modules/javascript/bower/ui/resources/no-version.png"; // NOI18N
                toolTip = Bundle.DependenciesPanel_version_noversion();
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
     * Model for the dependencies table.
     */
    class DependencyTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return (dependencies == null) ? 0 : dependencies.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        @NbBundle.Messages({
            "DependenciesPanel.table.libraryColumn=Package",
            "DependenciesPanel.table.requiredVersionColumn=Required Version",
            "DependenciesPanel.table.installedVersionColumn=Installed Version",
            "DependenciesPanel.table.latestVersionColumn=Latest Version"
        })
        public String getColumnName(int column) {
            String columnName;
            switch (column) {
                case 0: columnName = Bundle.DependenciesPanel_table_libraryColumn(); break;
                case 1: columnName = Bundle.DependenciesPanel_table_requiredVersionColumn(); break;
                case 2: columnName = Bundle.DependenciesPanel_table_installedVersionColumn(); break;
                case 3: columnName = Bundle.DependenciesPanel_table_latestVersionColumn(); break;
                default: throw new IllegalArgumentException();
            }
            return columnName;
        }

        @Override
        @NbBundle.Messages({
            "DependenciesPanel.table.latestVersionPlaceholder=latest"
        })
        public Object getValueAt(int rowIndex, int columnIndex) {
            Dependency dependency = dependencies.get(rowIndex);
            Object value;
            switch (columnIndex) {
                case 0: value = dependency.getName(); break;
                case 1: value = dependency.getRequiredVersion(); break;
                case 2:
                    if (installedLibrariesSet) {
                        value = dependency.getInstalledVersion();
                        if (value == null) {
                            value = VersionColumnRenderer.NO_VERSION;
                        } else if (Library.Version.LATEST_VERSION_PLACEHOLDER.equals(value)) {
                            value = Bundle.DependenciesPanel_table_latestVersionPlaceholder();
                        }
                    } else {
                        value = VersionColumnRenderer.CHECKING;
                    }
                    break;
                case 3:
                    String libraryName = dependency.getName();
                    Library library = allDependencies.dependencyInfo.get(libraryName);
                    if (library == null) {
                        value = allDependencies.dependencyInfo.containsKey(libraryName)
                                ? VersionColumnRenderer.UNKNOWN
                                : VersionColumnRenderer.CHECKING;
                    } else {
                        String latestVersion = library.getLatestVersion().getName();
                        if (latestVersion.equals(dependency.getInstalledVersion())) {
                            value = VersionColumnRenderer.UP_TO_DATE;
                        } else {
                            if (Library.Version.LATEST_VERSION_PLACEHOLDER.equals(latestVersion)) {
                                value = Bundle.DependenciesPanel_table_latestVersionPlaceholder();
                            } else {
                                value = latestVersion;
                            }
                        }
                    }
                    break;
                default: throw new IllegalArgumentException();
            }
            return value;
        }

    }

    /**
     * Selection listener for the table.
     */
    class Listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            updateButtons();
        }
    }

    /**
     * Bower dependencies.
     */
    static class Dependencies {
        /** Bower dependencies - maps the type of dependency to the list of dependencies of this type. */
        private final Map<Dependency.Type,List<Dependency>> dependencies = new EnumMap<>(Dependency.Type.class);
        /** Change listeners. */
        private final List<ChangeListener> listeners = new CopyOnWriteArrayList<>();
        /** Maps the name of the library to its Bower meta-data. */
        final Map<String,Library> dependencyInfo = new HashMap<>();

        /**
         * Creates a new {@code Dependencies} object.
         * 
         * @param bowerDependencies Bower dependencies.
         */
        Dependencies(BowerJson.BowerDependencies bowerDependencies) {
            dependencies.put(Dependency.Type.REGULAR, toList(bowerDependencies.dependencies));
            dependencies.put(Dependency.Type.DEVELOPMENT, toList(bowerDependencies.devDependencies));
        }

        /**
         * Converts name-to-version map of dependencies to list of {@code Dependency} objects.
         * 
         * @param map name-to-version map of dependencies.
         * @return list of {@code Dependency} objects that correspond
         * to the given map.
         */
        private List<Dependency> toList(Map<String,String> map) {
            List<Dependency> list = new ArrayList<>();
            for (Map.Entry<String,String> entry : map.entrySet()) {
                String name = entry.getKey();
                String requiredVersion = entry.getValue();
                Dependency dependency = new Dependency(name);
                dependency.setRequiredVersion(requiredVersion);
                list.add(dependency);
            }
            return list;
        }

        /**
         * Returns dependencies of the given type.
         * 
         * @param type type of dependencies.
         * @return dependencies of the given type.
         */
        List<Dependency> forType(Dependency.Type type) {
            return dependencies.get(type);
        }

        /**
         * Collects dependency types (different from the given type)
         * for which a dependency with the specified name exists.
         * 
         * @param dependencyName dependency name to check.
         * @param dependencyType dependency type to skip.
         * @return dependency types for which a dependency with the specified
         * name exists.
         */
        List<Dependency.Type> otherDependencyTypes(String dependencyName, Dependency.Type dependencyType) {
            List<Dependency.Type> types = new ArrayList<>();
            for (Map.Entry<Dependency.Type,List<Dependency>> entry : dependencies.entrySet()) {
                Dependency.Type type = entry.getKey();
                if (dependencyType != type) {
                    for (Dependency dependency : entry.getValue()) {
                        if (dependencyName.equals(dependency.getName())) {
                            types.add(type);
                            break;
                        }
                    }
                }
            }
            return types;
        }

        /**
         * Removes the dependencies with the specified name that are
         * of a different type than the given type.
         * 
         * @param dependencyName name of the dependencies to remove.
         * @param dependencyType type of the dependencies to keep.
         */
        void removeOtherDependencies(String dependencyName, Dependency.Type dependencyType) {
            for (Map.Entry<Dependency.Type,List<Dependency>> entry : dependencies.entrySet()) {
                Dependency.Type type = entry.getKey();
                if (dependencyType != type) {
                    for (Dependency dependency : entry.getValue()) {
                        if (dependencyName.equals(dependency.getName())) {
                            entry.getValue().remove(dependency);
                            break;
                        }
                    }
                }
            }
            fireChange();
        }

        /**
         * Adds the specified dependencies with the specified type.
         * 
         * @param toAdd dependencies to add.
         * @param targetType target dependency type.
         */
        void addDependencies(List<Dependency> toAdd, Dependency.Type targetType) {
            List<Dependency> targetList = dependencies.get(targetType);
            targetList.addAll(toAdd);
            fireChange();
        }

        /**
         * Adds a listener for the changes done through methods of this class.
         * 
         * @param changeListener listener to register.
         */
        void addChangeListener(ChangeListener changeListener) {
            listeners.add(changeListener);
        }

        /**
         * Notifies all registered listeners about a change.
         */
        private void fireChange() {
            for (ChangeListener listener : listeners) {
                listener.stateChanged(null);
            }
        }
        
    }

}
