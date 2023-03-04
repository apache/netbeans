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
package org.netbeans.modules.javascript.cdnjs.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.View;
import org.netbeans.modules.javascript.cdnjs.Library;
import org.netbeans.modules.javascript.cdnjs.LibraryProvider;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for searching CDNJS libraries.
 *
 * @author Jan Stola
 */
class SearchPanel extends javax.swing.JPanel {
    /** Minimum length of the text to be searched. */
    private static final int MIN_SEARCH_TEXT_LENGTH = 2;
    private static final RequestProcessor RP = new RequestProcessor(SearchPanel.class.getName(), 3);
    /** The last search term. */
    private String lastSearchTerm;


    /**
     * Creates a new {@code SearchPanel}.
     */
    SearchPanel() {
        initComponents();
        initDocumentListener();
        librariesList.setCellRenderer(new LibraryRenderer());
        libraryInfoPanel.setPreferredSize(librariesScrollPane.getPreferredSize());
        versionComboBox.setRenderer(new LibraryVersionRenderer());
        updateLibraries(new Library[0]);
        librarySelected(null);
    }

    private void initDocumentListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processEvent(e);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processEvent(e);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processEvent(e);
            }
            private void processEvent(DocumentEvent e) {
                String text = searchField.getText();
                searchButton.setEnabled(text != null && text.trim().length() >= MIN_SEARCH_TEXT_LENGTH);
            }
        });
    }

    /**
     * Invoked when a library is selected.
     * 
     * @param library selected library (or {@code null} when no library is selected).
     */
    private void librarySelected(Library library) {
        boolean emptySelection = (library == null);
        String description = null;
        if (!emptySelection) {
            description = library.getDescription();
            if (description == null) { // Issue 248134
                description = ""; // NOI18N
            }
            description = "<html>" + description; // NOI18N
        }
        updateHomePageLink(library, false);
        descriptionTextLabel.setText(description);
        versionComboBox.setModel(versionComboBoxModelFor(library));
        versionComboBox.setEnabled(!emptySelection);
        addButton.setEnabled(!emptySelection);
        updateFileSelectionPanel();
    }

    /**
     * Returns {@code ComboBoxModel} for the given library.
     * 
     * @param library library for which to return the model.
     * @return {@code ComboBoxModel} for the given library.
     */
    private ComboBoxModel<Library.Version> versionComboBoxModelFor(Library library) {
        return new DefaultComboBoxModel<>(library == null ? new Library.Version[0] : library.getVersions());
    }

    /**
     * Starts the search for the libraries matching the current search term.
     */
    @NbBundle.Messages({
        "# {0} - search term",
        "SearchPanel.message.searching=Looking for \"{0}\" libraries"
    })
    private void startSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.length() < MIN_SEARCH_TEXT_LENGTH) {
            return;
        }
        librarySelected(null);
        lastSearchTerm = searchTerm;
        messageLabel.setText(Bundle.SearchPanel_message_searching(lastSearchTerm));
        showComponent(messageLabel);
        RP.execute(() ->  {
            Library[] libraries = LibraryProvider.getInstance().findLibraries(searchTerm);
            SwingUtilities.invokeLater(() -> updateLibraries(libraries));
        });
    }

    /**
     * Updates the list of displayed libraries. This method is invoked when
     * a search is finished.
     * 
     * @param libraries libraries matching the last search term (or {@code null}
     * when the search failed). When there are no matching libraries than
     * an empty array is given.
     */
    @NbBundle.Messages({"SearchPanel.message.searchFailed=Search failed :-("})
    final void updateLibraries(Library[] libraries) {
        if (libraries == null) {
            messageLabel.setText(Bundle.SearchPanel_message_searchFailed());
            showComponent(messageLabel);
        } else if (libraries.length == 0) {
            messageLabel.setText(NbBundle.getMessage(SearchPanel.class, "SearchPanel.messageLabel.text")); // NOI18N
            showComponent(messageLabel);
        } else {
            Arrays.sort(libraries, new LibraryComparator());
            librariesList.setModel(libraryListModelFor(libraries));
            preSelectSearchedLibrary(libraries);
            showComponent(searchPanel);
        }
    }

    /**
     * Attempts to pre-select the library whose name matches the last search term.
     * Selects the first library otherwise.
     * 
     * @param libraries latest search result.
     */
    private void preSelectSearchedLibrary(Library[] libraries) {
        int index = 0;
        String term = lastSearchTerm == null ? "" : lastSearchTerm; // NOI18N
        for (int i=0; i<libraries.length; i++) {
            if (libraries[i].getName().equalsIgnoreCase(term)) {
                index = i;
                break;
            }
        }
        librariesList.setSelectedIndex(index);
        librariesList.ensureIndexIsVisible(index);
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
     * Returns the selected library version.
     * 
     * @return selected library version.
     */
    Library.Version getSelectedVersion() {
        return fileSelectionPanel.getSelection();
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
     * Shows the home-page of the selected library.
     */
    void showHomePage() {
        String homePage = getSelectedVersion().getLibrary().getHomePage();
        try {
            URI uri = new URI(homePage);
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(SearchPanel.class.getName()).log(Level.INFO, null, ex);
        }
    }

    /**
     * Updates the label that shows the home-page of the selected library.
     * 
     * @param library selected library.
     * @param linkVisible if {@code true} then the text of the label
     * is underlined, i.e., the label looks like a link.
     */
    @NbBundle.Messages({
        "SearchPanel.nohomepage=<No Homepage>"
    })
    void updateHomePageLink(Library library, boolean linkVisible) {
        String homePage = null;
        if (library != null) {
            homePage = library.getHomePage();
            if (homePage != null) {
                homePage = "<html>" + (linkVisible ? "<u>" : "") + homePage; // NOI18N
            }
        }
        if (homePage == null) {
            homePageLinkLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            homePageLinkLabel.setEnabled(false);
            homePageLinkLabel.setText((library == null) ? null : Bundle.SearchPanel_nohomepage());
        } else {
            homePageLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            homePageLinkLabel.setText(homePage);
            homePageLinkLabel.setEnabled(true);
        }
    }

    /**
     * Updates the file selection panel according to the selected library version.
     */
    void updateFileSelectionPanel() {
        Library.Version version = (Library.Version)versionComboBox.getSelectedItem();
        fileSelectionPanel.setLibrary(version, null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        searchPanel = new javax.swing.JPanel();
        librariesScrollPane = new javax.swing.JScrollPane();
        librariesList = new javax.swing.JList<Library>();
        filesLabel = new javax.swing.JLabel();
        fileSelectionPanel = new org.netbeans.modules.javascript.cdnjs.ui.FileSelectionPanel();
        librariesLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        libraryInfoPanel = new javax.swing.JPanel();
        versionLabel = new javax.swing.JLabel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextLabel = new javax.swing.JLabel() {
            @Override
            public Dimension getPreferredSize() {
                Object view = getClientProperty("html"); // NOI18N
                Container container = getParent();
                if ((view instanceof View) && (container != null)) {
                    Dimension containerDim = container.getSize();
                    ((View)view).setSize(containerDim.width, containerDim.height);
                }
                return super.getPreferredSize();
            }
        };
        versionComboBox = new javax.swing.JComboBox<Library.Version>();
        homePageLabel = new javax.swing.JLabel();
        homePageLinkLabel = new javax.swing.JLabel() {
            public Dimension getMinimumSize() {
                Dimension dim = super.getMinimumSize();
                return new Dimension(0, dim.height);
            }
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();

        FormListener formListener = new FormListener();

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.addButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.cancelButton.text")); // NOI18N

        librariesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        librariesList.addListSelectionListener(formListener);
        librariesScrollPane.setViewportView(librariesList);

        org.openide.awt.Mnemonics.setLocalizedText(filesLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.filesLabel.text")); // NOI18N

        librariesLabel.setLabelFor(librariesList);
        org.openide.awt.Mnemonics.setLocalizedText(librariesLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.librariesLabel.text")); // NOI18N

        descriptionLabel.setLabelFor(descriptionTextLabel);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.descriptionLabel.text")); // NOI18N

        versionLabel.setLabelFor(versionComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.versionLabel.text")); // NOI18N

        descriptionScrollPane.setBorder(null);
        descriptionScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        descriptionTextLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        descriptionScrollPane.setViewportView(descriptionTextLabel);

        versionComboBox.addActionListener(formListener);

        javax.swing.GroupLayout libraryInfoPanelLayout = new javax.swing.GroupLayout(libraryInfoPanel);
        libraryInfoPanel.setLayout(libraryInfoPanelLayout);
        libraryInfoPanelLayout.setHorizontalGroup(
            libraryInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(libraryInfoPanelLayout.createSequentialGroup()
                .addComponent(versionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(descriptionScrollPane)
        );
        libraryInfoPanelLayout.setVerticalGroup(
            libraryInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(libraryInfoPanelLayout.createSequentialGroup()
                .addComponent(descriptionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 5, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(libraryInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        org.openide.awt.Mnemonics.setLocalizedText(homePageLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.homePageLabel.text")); // NOI18N

        homePageLinkLabel.addMouseListener(formListener);

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fileSelectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addComponent(filesLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(librariesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(librariesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(descriptionLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(libraryInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addComponent(homePageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(homePageLinkLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(librariesLabel)
                    .addComponent(descriptionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(libraryInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(librariesScrollPane)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(homePageLabel)
                            .addComponent(homePageLinkLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileSelectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
        );

        searchLabel.setLabelFor(searchField);
        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchLabel.text")); // NOI18N

        searchField.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchButton.text")); // NOI18N
        searchButton.setEnabled(false);
        searchButton.addActionListener(formListener);

        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.messageLabel.text")); // NOI18N
        messageLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(searchLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton)))
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
                .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.MouseListener, javax.swing.event.ListSelectionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == searchField) {
                SearchPanel.this.searchFieldActionPerformed(evt);
            }
            else if (evt.getSource() == searchButton) {
                SearchPanel.this.searchButtonActionPerformed(evt);
            }
            else if (evt.getSource() == versionComboBox) {
                SearchPanel.this.versionComboBoxActionPerformed(evt);
            }
        }

        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == homePageLinkLabel) {
                SearchPanel.this.homePageLinkLabelMouseClicked(evt);
            }
        }

        public void mouseEntered(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == homePageLinkLabel) {
                SearchPanel.this.homePageLinkLabelMouseEntered(evt);
            }
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == homePageLinkLabel) {
                SearchPanel.this.homePageLinkLabelMouseExited(evt);
            }
        }

        public void mousePressed(java.awt.event.MouseEvent evt) {
        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
        }

        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
            if (evt.getSource() == librariesList) {
                SearchPanel.this.librariesListValueChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void librariesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_librariesListValueChanged
        final Library library = librariesList.getSelectedValue();
        if(library != null && (library.getVersions() == null || library.getVersions().length == 0)) {
            RP.execute(() -> {
                LibraryProvider.getInstance().updateLibraryVersions(library);
                SwingUtilities.invokeLater(() -> librarySelected(library));
            });
        } else {
            librarySelected(library);
        }
    }//GEN-LAST:event_librariesListValueChanged

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        startSearch();
    }//GEN-LAST:event_searchFieldActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        startSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void homePageLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homePageLinkLabelMouseClicked
        if (homePageLinkLabel.isEnabled()) {
            showHomePage();
        }
    }//GEN-LAST:event_homePageLinkLabelMouseClicked

    private void homePageLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homePageLinkLabelMouseEntered
        Library library = librariesList.getSelectedValue();
        updateHomePageLink(library, true);
    }//GEN-LAST:event_homePageLinkLabelMouseEntered

    private void homePageLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homePageLinkLabelMouseExited
        Library library = librariesList.getSelectedValue();
        updateHomePageLink(library, false);
    }//GEN-LAST:event_homePageLinkLabelMouseExited

    private void versionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionComboBoxActionPerformed
        updateFileSelectionPanel();
    }//GEN-LAST:event_versionComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JLabel descriptionTextLabel;
    private org.netbeans.modules.javascript.cdnjs.ui.FileSelectionPanel fileSelectionPanel;
    private javax.swing.JLabel filesLabel;
    private javax.swing.JLabel homePageLabel;
    private javax.swing.JLabel homePageLinkLabel;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JList<Library> librariesList;
    private javax.swing.JScrollPane librariesScrollPane;
    private javax.swing.JPanel libraryInfoPanel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JComboBox<Library.Version> versionComboBox;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables


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

    /**
     * Renderer of {@code Library.Version} objects.
     */
    static class LibraryVersionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Library.Version) {
                Library.Version version = (Library.Version)value;
                value = version.getName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    /**
     * Comparator for sorting of libraries in libraries list.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE",
            justification = "No need to be serializable")
    static class LibraryComparator implements Comparator<Library> {

        @Override
        public int compare(Library library1, Library library2) {
            return library1.getName().toLowerCase().compareTo(library2.getName().toLowerCase());
        }

    }

}
