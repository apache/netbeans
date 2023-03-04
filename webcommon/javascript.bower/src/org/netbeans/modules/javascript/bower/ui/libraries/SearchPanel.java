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

package org.netbeans.modules.javascript.bower.ui.libraries;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for searching of Bower libraries.
 *
 * @author Jan Stola
 */
public class SearchPanel extends javax.swing.JPanel {
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(SearchPanel.class);
    /** Listener for library provider. */
    private final Listener listener = new Listener();
    /** The last search term. */
    private String lastSearchTerm;
    /** Library provider used by this panel. */
    private final LibraryProvider libraryProvider;
    /** Selected library. */
    private Library selectedLibrary;

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
        Library[] libraries = libraryProvider.findLibraries(lastSearchTerm);
        if (libraries == null) {
            messageLabel.setText(Bundle.SearchPanel_message_searching(lastSearchTerm));
            showComponent(messageLabel);
        } else {
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
    @NbBundle.Messages({"SearchPanel.message.searchFailed=<html><center>Search failed :-(</center><br><center>Check if 'bower search <i>query</i>' works on the command line.</center>"})
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
        Arrays.sort(libraries, alphabeticalSortButton.isSelected() ?
                new AlphabeticLibraryComparator() :
                new PopularityLibraryComparator());
        DefaultListModel<Library> listModel = new DefaultListModel<>();
        for (Library library : libraries) {
            listModel.addElement(library);
        }
        return listModel;
    }

    /**
     * Invoked when a library is selected.
     * 
     * @param library selected library (or {@code null} when no library is selected).
     */
    @NbBundle.Messages({
        "SearchPanel.message.loadingDetail=Loading..."
    })
    private void librarySelected(Library library) {
        assert EventQueue.isDispatchThread();
        synchronized (this) {
            if (selectedLibrary == library) {
                return;
            }
            selectedLibrary = library;
        }
        updateLibraryDetail(null, null);
        if (library == null) {
            return;
        }
        final String libraryName = library.getName();
        Library details = libraryProvider.libraryDetails(libraryName, true);
        if (details == null) {
            loadingLabel.setText(Bundle.SearchPanel_message_loadingDetail());
        } else {
            updateLibraryDetail(libraryName, details);
            return;
        }
        RP.post(new Runnable() { 
            @Override
            public void run() {
                String selectedLibraryName;
                synchronized (SearchPanel.this) {
                    selectedLibraryName = (selectedLibrary == null) ? null : selectedLibrary.getName();
                }
                if (libraryName.equals(selectedLibraryName)) {
                    final Library details = libraryProvider.libraryDetails(libraryName, false);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateLibraryDetail(libraryName, details);
                        }
                    });
                }
            }
        });
    }

    /**
     * Cache of library details. The details are cached in {@code LibraryProvider}
     * in fact, but they are held weakly there. The purpose of this collection
     * is to keep strong references to library details shown in the customizer
     * until the customizer is closed.
     */
    private final Map<String,Library> libraryDetailCache = new HashMap<>();

    @NbBundle.Messages({
        "SearchPanel.nodeDescription=<No Description>",
        "SearchPanel.message.loadingOfDetailFailed=Loading of package detail failed! :-("
    })
    private void updateLibraryDetail(String libraryName, Library libraryDetails) {
        assert EventQueue.isDispatchThread();
        libraryDetailCache.put(libraryName, libraryDetails);
        synchronized (this) {
            if (libraryName != null && selectedLibrary != null && !libraryName.equals(selectedLibrary.getName())) {
                return;
            }
        }
        loadingLabel.setText((libraryName != null) && (libraryDetails == null)
                ? Bundle.SearchPanel_message_loadingOfDetailFailed() : null);
        boolean emptySelection = (libraryDetails == null);
        String description = null;
        String keywords = null;
        String homePage = null;
        boolean descriptionEnabled = true;
        if (!emptySelection) {
            if (libraryDetails.getDescription().isEmpty()) {
                description = Bundle.SearchPanel_nodeDescription();
                descriptionEnabled = false;
            } else {
                description = "<html>" + libraryDetails.getDescription(); // NOI18N
            }
            if (libraryDetails.getKeywords().length > 0) {
                StringBuilder keywordsText = new StringBuilder("<html>"); // NOI18N
                for (String keyword : libraryDetails.getKeywords()) {
                    keywordsText.append(keyword).append(" "); // NOI18N
                }
                keywords = keywordsText.toString();
            }
            Dependency dependency = new Dependency(libraryDetails.getName());
            Library.Version latestVersion = libraryDetails.getLatestVersion();
            if (latestVersion != null) {
                String versionName = latestVersion.getName();
                dependency.setInstalledVersion(versionName);
                dependency.setRequiredVersion(versionName);
            }
            editPanel.setDependency(dependency);
            homePage = libraryDetails.getHomePage();
        }

        descriptionComponent.setText(description);
        descriptionComponent.setVisible(description != null);
        descriptionComponent.setEnabled(descriptionEnabled);

        keywordsComponent.setText(keywords);
        keywordsComponent.setVisible(keywords != null);
        keywordsLabel.setVisible(keywords != null);

        editPanel.setVisible(!emptySelection);
        addButton.setEnabled(!emptySelection);

        homePageLinkLabel.setText(homePage);
        homePageLabel.setVisible(homePage != null);
        homePageLinkLabel.setVisible(homePage != null);
        updateHomePageLink(false);
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
     * Returns the home page of the selected library.
     * 
     * @return home page of the selected library.
     */
    private String getHomePage() {
        String homePage = homePageLinkLabel.getText();
        if (homePage != null) {
            int index = homePage.lastIndexOf('>');
            homePage = homePage.substring(index+1);
        }
        return homePage;
    }

    /**
     * Updates the label that shows the home-page of the selected library.
     * 
     * @param linkVisible if {@code true} then the text of the label
     * is underlined, i.e., the label looks like a link.
     */
    void updateHomePageLink(boolean linkVisible) {
        String homePage = getHomePage();
        if (homePage != null) {
            homePage = "<html>" + (linkVisible ? "<u>" : "") + homePage; // NOI18N
            homePageLinkLabel.setText(homePage);
        }
    }

    /**
     * Updates the order of the libraries/packages in the list.
     */
    void updateLibraryOrder() {
        Library selected = librariesList.getSelectedValue();
        ListModel<Library> model = librariesList.getModel();
        Library[] libraries = new Library[model.getSize()];
        for (int i=0; i<model.getSize(); i++) {
            libraries[i] = model.getElementAt(i);
        }
        librariesList.setModel(libraryListModelFor(libraries));
        if (selected != null) {
            librariesList.setSelectedValue(selected, true);
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

        searchPanel = new javax.swing.JPanel();
        librariesLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        librariesList = new javax.swing.JList<Library>();
        descriptionLabel = new javax.swing.JLabel();
        descriptionComponent = new javax.swing.JLabel();
        keywordsLabel = new javax.swing.JLabel();
        keywordsComponent = new javax.swing.JLabel();
        editPanel = new org.netbeans.modules.javascript.bower.ui.libraries.EditPanel();
        loadingLabel = new javax.swing.JLabel();
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
        popularitySortButton = new javax.swing.JToggleButton();
        alphabeticalSortButton = new javax.swing.JToggleButton();
        addButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        sortButtonGroup = new javax.swing.ButtonGroup();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();

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

        loadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        loadingLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(homePageLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.homePageLabel.text")); // NOI18N

        homePageLinkLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        homePageLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                homePageLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                homePageLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                homePageLinkLabelMouseExited(evt);
            }
        });

        sortButtonGroup.add(popularitySortButton);
        popularitySortButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/javascript/bower/ui/resources/popularity-sort.png"))); // NOI18N
        popularitySortButton.setSelected(true);
        popularitySortButton.setToolTipText(org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.popularitySortButton.toolTipText")); // NOI18N
        popularitySortButton.setFocusPainted(false);
        popularitySortButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        popularitySortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popularitySortButtonActionPerformed(evt);
            }
        });

        sortButtonGroup.add(alphabeticalSortButton);
        alphabeticalSortButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/javascript/bower/ui/resources/alphabetic-sort.png"))); // NOI18N
        alphabeticalSortButton.setToolTipText(org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.alphabeticalSortButton.toolTipText")); // NOI18N
        alphabeticalSortButton.setFocusPainted(false);
        alphabeticalSortButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        alphabeticalSortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alphabeticalSortButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(librariesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(alphabeticalSortButton)
                        .addGap(0, 0, 0)
                        .addComponent(popularitySortButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(descriptionComponent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(keywordsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(keywordsComponent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loadingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(homePageLabel)
                            .addComponent(homePageLinkLabel))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(librariesLabel)
                    .addComponent(descriptionLabel)
                    .addComponent(popularitySortButton)
                    .addComponent(alphabeticalSortButton))
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(descriptionComponent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(keywordsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keywordsComponent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(homePageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(homePageLinkLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(editPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loadingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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

    private void homePageLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homePageLinkLabelMouseEntered
        updateHomePageLink(true);
    }//GEN-LAST:event_homePageLinkLabelMouseEntered

    private void homePageLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homePageLinkLabelMouseExited
        updateHomePageLink(false);
    }//GEN-LAST:event_homePageLinkLabelMouseExited

    private void homePageLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homePageLinkLabelMouseClicked
        try {
            URI uri = new URI(getHomePage());
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(SearchPanel.class.getName()).log(Level.INFO, null, ex);
        }
    }//GEN-LAST:event_homePageLinkLabelMouseClicked

    private void popularitySortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popularitySortButtonActionPerformed
        updateLibraryOrder();
    }//GEN-LAST:event_popularitySortButtonActionPerformed

    private void alphabeticalSortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alphabeticalSortButtonActionPerformed
        updateLibraryOrder();
    }//GEN-LAST:event_alphabeticalSortButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JToggleButton alphabeticalSortButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel descriptionComponent;
    private javax.swing.JLabel descriptionLabel;
    private org.netbeans.modules.javascript.bower.ui.libraries.EditPanel editPanel;
    private javax.swing.JLabel homePageLabel;
    private javax.swing.JLabel homePageLinkLabel;
    private javax.swing.JLabel keywordsComponent;
    private javax.swing.JLabel keywordsLabel;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JList<Library> librariesList;
    private javax.swing.JLabel loadingLabel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JToggleButton popularitySortButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.ButtonGroup sortButtonGroup;
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

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE",
            justification = "No need to be serializable")
    static class AlphabeticLibraryComparator implements Comparator<Library> {
        @Override
        public int compare(Library library1, Library library2) {
            return library1.getName().compareTo(library2.getName());
        }
    };

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE",
            justification = "No need to be serializable")
    static class PopularityLibraryComparator implements Comparator<Library> {
        @Override
        public int compare(Library library1, Library library2) {
            return library2.getPopularity() - library1.getPopularity();
        }        
    }

}
