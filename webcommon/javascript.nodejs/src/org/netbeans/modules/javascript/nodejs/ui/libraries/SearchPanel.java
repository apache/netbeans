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

package org.netbeans.modules.javascript.nodejs.ui.libraries;

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import org.openide.util.NbBundle;

/**
 * Panel for searching of npm libraries.
 *
 * @author Jan Stola
 */
public class SearchPanel extends javax.swing.JPanel {
    /** Listener for library provider. */
    private final Listener listener = new Listener();
    /** The last search term. */
    private String lastSearchTerm;
    /** Determines whether we should have searched in package names only in the last search. */
    private boolean lastSearchInNames;
    /** Library provider used by this panel. */
    private final LibraryProvider libraryProvider;

    /**
     * Creates a new {@code SearchPanel}.
     */
    public SearchPanel(LibraryProvider libraryProvider) {
        this.libraryProvider = libraryProvider;
        initComponents();
        editPanel.setLibraryProvider(libraryProvider);
        librariesList.setCellRenderer(new LibraryRenderer());
        updateLibraries(new Library[0]);
        librarySelected(null);
    }

    /**
     * Activates this panel (i.e. registers all necessary listeners).
     * This method should be called before the panel is shown to the user.
     */
    final void activate() {
        libraryProvider.addPropertyChangeListener(listener);
    }

    /**
     * Deactivates this panel (i.e. unregisters the listeners). This method
     * should be called when the panel is no longer shown to the user.
     */
    final void deactivate() {
        libraryProvider.removePropertyChangeListener(listener);
    }

    /**
     * Returns the name of the selected library.
     * 
     * @return name of the selected library (or {@code null} when no library
     * is selected).
     */
    String getSelectedLibrary() {
        Library library = librariesList.getSelectedValue();
        return (library == null) ? null : library.getName();
    }

    /**
     * Returns the required version of the library.
     * 
     * @return required version of the library.
     */
    String getRequiredVersion() {
        return editPanel.getRequiredVersion();
    }

    /**
     * Returns the installed version of the library.
     * 
     * @return installed version of the library.
     */
    String getInstalledVersion() {
        return editPanel.getInstalledVersion();
    }

    /**
     * Starts the search for the libraries matching the current search term.
     */
    @NbBundle.Messages({
        "# {0} - search term",
        "SearchPanel.message.searching=Looking for \"{0}\" packages"
    })
    private void startSearch() {
        librarySelected(null);
        lastSearchTerm = searchField.getText().trim();
        lastSearchInNames = searchInNamesCheckBox.isSelected();
        Library[] libraries = libraryProvider.findLibraries(lastSearchTerm);
        if (libraries == null) {
            messageLabel.setText(Bundle.SearchPanel_message_searching(lastSearchTerm));
            showComponent(messageLabel);
        } else {
            if (lastSearchInNames) {
                libraries = filterByName(libraries, lastSearchTerm);
            }
            updateLibraries(libraries);
        }
    }

    /**
     * Shows the given component in the main area of the layout.
     * 
     * @param component component to show.
     */
    private void showComponent(JComponent component) {
        if (component.getParent() == null) {
            JComponent shownComponent = (component == messageLabel) ? searchPanel : messageLabel;
            ((GroupLayout)getLayout()).replace(shownComponent, component);
        }
    }

    /**
     * Updates the list of displayed libraries. This method is invoked when
     * a search is finished.
     * 
     * @param libraries libraries matching the last search term (or {@code null}
     * when the search failed). When there are no matching libraries than
     * an empty array is given.
     */
    @NbBundle.Messages({"SearchPanel.message.searchFailed=<html><center>Search failed :-(</center><br><center>Check if 'npm search <i>query</i>' works on the command line.</center>"})
    final void updateLibraries(Library[] libraries) {
        if (libraries == null) {
            messageLabel.setText(Bundle.SearchPanel_message_searchFailed());
            showComponent(messageLabel);
        } else if (libraries.length == 0) {
            messageLabel.setText(NbBundle.getMessage(SearchPanel.class, "SearchPanel.messageLabel.text")); // NOI18N
            showComponent(messageLabel);
        } else {
            librariesList.setModel(libraryListModelFor(libraries));
            librariesList.setSelectedIndex(0);
            showComponent(searchPanel);
            librariesList.requestFocusInWindow();
        }
    }

    /**
     * Returns the {@code ListModel} for the given libraries.
     * 
     * @param libraries libraries for which to return the model.
     * @return {@code ListModel} for the given libraries.
     */
    private ListModel<Library> libraryListModelFor(Library[] libraries) {
        DefaultListModel<Library> listModel = new DefaultListModel<>();
        if (libraries != null) {
            for (Library library : libraries) {
                listModel.addElement(library);
            }
        }
        return listModel;
    }

    /**
     * Invoked when a library is selected.
     * 
     * @param library selected library (or {@code null} when no library is selected).
     */
    private void librarySelected(Library library) {
        boolean emptySelection = (library == null);
        String description = null;
        String keywords = null;
        if (!emptySelection) {
            if (!library.getDescription().isEmpty()) {
                description = "<html>" + library.getDescription(); // NOI18N
            }
            if (library.getKeywords().length > 0) {
                StringBuilder keywordsText = new StringBuilder("<html>"); // NOI18N
                for (String keyword : library.getKeywords()) {
                    keywordsText.append(keyword).append(" "); // NOI18N
                }
                keywords = keywordsText.toString();
            }
            Dependency dependency = new Dependency(library.getName());
            Library.Version latestVersion = library.getLatestVersion();
            if (latestVersion != null) {
                String versionName = latestVersion.getName();
                dependency.setInstalledVersion(versionName);
                dependency.setRequiredVersion(versionName);
            }
            editPanel.setDependency(dependency);
        }
        descriptionComponent.setText(description);
        keywordsComponent.setText(keywords);
        descriptionComponent.setVisible(description != null);
        keywordsLabel.setVisible(keywords != null);
        editPanel.setVisible(!emptySelection);
        addButton.setEnabled(!emptySelection);
    }

    /**
     * Returns the Add button (to be used in a dialog showing this panel).
     * 
     * @return Add button.
     */
    JButton getAddButton() {
        return addButton;
    }

    /**
     * Returns the Cancel button (to be used in a dialog showing this panel).
     * 
     * @return Cancel button.
     */
    JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Returns only the libraries that contain the specified sub-string
     * in the library name.
     * 
     * @param libraries libraries to filter by name.
     * @param searchTerm sub-string to look for in library names.
     * @return libraries that contain the specified sub-string in their name.
     */
    Library[] filterByName(Library[] libraries, String searchTerm) {
        List<Library> list = new ArrayList<>();
        for (Library library : libraries) {
            if (library.getName().contains(searchTerm)) {
                list.add(library);
            }
        }
        return list.toArray(new Library[0]);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchPanel = new javax.swing.JPanel();
        librariesLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        librariesList = new javax.swing.JList<Library>();
        descriptionLabel = new javax.swing.JLabel();
        descriptionComponent = new javax.swing.JLabel();
        keywordsLabel = new javax.swing.JLabel();
        keywordsComponent = new javax.swing.JLabel();
        editPanel = new org.netbeans.modules.javascript.nodejs.ui.libraries.EditPanel();
        addButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();
        searchInNamesCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(librariesLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.librariesLabel.text")); // NOI18N

        librariesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        librariesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                librariesListValueChanged(evt);
            }
        });
        scrollPane.setViewportView(librariesList);

        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.descriptionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keywordsLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.keywordsLabel.text")); // NOI18N

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(librariesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(descriptionComponent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(keywordsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(keywordsComponent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(librariesLabel)
                    .addComponent(descriptionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(descriptionComponent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(keywordsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keywordsComponent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(editPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.addButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.cancelButton.text")); // NOI18N

        searchLabel.setLabelFor(searchField);
        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchLabel.text")); // NOI18N

        searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.messageLabel.text")); // NOI18N
        messageLabel.setEnabled(false);

        searchInNamesCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(searchInNamesCheckBox, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchInNamesCheckBox.text")); // NOI18N
        searchInNamesCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchInNamesCheckBox.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(searchLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(searchInNamesCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(searchField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchLabel)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchInNamesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        startSearch();
    }//GEN-LAST:event_searchFieldActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        startSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void librariesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_librariesListValueChanged
        Library library = librariesList.getSelectedValue();
        librarySelected(library);
    }//GEN-LAST:event_librariesListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel descriptionComponent;
    private javax.swing.JLabel descriptionLabel;
    private org.netbeans.modules.javascript.nodejs.ui.libraries.EditPanel editPanel;
    private javax.swing.JLabel keywordsComponent;
    private javax.swing.JLabel keywordsLabel;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JList<Library> librariesList;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JCheckBox searchInNamesCheckBox;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Listener for the library provider.
     */
    class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String searchTerm = evt.getPropertyName();
                    if (searchTerm.equals(lastSearchTerm)) {
                        Library[] libraries = (Library[])evt.getNewValue();
                        if (lastSearchInNames && (libraries != null)) {
                            libraries = filterByName(libraries, lastSearchTerm);
                        }
                        updateLibraries(libraries);
                    }
                }
            });
        }
        
    }

    /**
     * Renderer of {@code Library} objects.
     */
    static class LibraryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Library) {
                Library library = (Library)value;
                value = library.getName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

}
