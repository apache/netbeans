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
import java.awt.EventQueue;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for specification of installed and required version.
 *
 * @author Jan Stola
 */
public class EditPanel extends javax.swing.JPanel {
    /** Request processor used by this panel. */
    private static final RequestProcessor RP = new RequestProcessor(EditPanel.class);
    /** Provider of library details. */
    private LibraryProvider libraryProvider;
    /** Library details. */
    private Library libraryDetails;
    /** Determines whether we are loading the library details. */
    private boolean loadingLibraryDetails;

    /**
     * Creates a new {@code EditPanel}.
     */
    public EditPanel() {
        initComponents();
        installVersionCombo.setRenderer(new VersionRenderer());
    }

    /**
     * Sets the library provider.
     * 
     * @param libraryProvider library provider.
     */
    void setLibraryProvider(LibraryProvider libraryProvider) {
        this.libraryProvider = libraryProvider;
    }

    /**
     * Sets the dependency to edit.
     * 
     * @param dependency dependency to edit.
     */
    void setDependency(Dependency dependency) {
        Library.Version versionToSelect = null;
        String installedVersionName = dependency.getInstalledVersion();
        libraryDetails = libraryProvider.libraryDetails(dependency.getName(), true);
        if (libraryDetails == null) {
            // Temporary library details
            libraryDetails = new Library(dependency.getName());
            if (installedVersionName != null) {
                versionToSelect = new Library.Version(libraryDetails, installedVersionName);
            }
        } else {
            if (installedVersionName == null) {
                versionToSelect = libraryDetails.getLatestVersion();
            } else {
                versionToSelect = findVersion(installedVersionName);
            }
        }

        requiredVersionField.setText(dependency.getRequiredVersion());
        updateInstalledCombo(versionToSelect);
        loadingLibraryDetails = false;
    }

    /**
     * Returns the required version of the dependency.
     * 
     * @return required version of the dependency.
     */
    String getRequiredVersion() {
        return requiredVersionField.getText();
    }

    /**
     * Returns the installed version of the dependency.
     * 
     * @return installed version of the dependency.
     */
    String getInstalledVersion() {
        Library.Version selectedVersion = (Library.Version)installVersionCombo.getSelectedItem();
        return (selectedVersion == null) ? null : selectedVersion.getName();
    }

    /**
     * Finds the version with the specified name among the versions
     * in library details.
     * 
     * @param versionName name of the version to find.
     * @return version with the specified name or {@code null} when there
     * is no such version.
     */
    private Library.Version findVersion(String versionName) {
        for (Library.Version version : libraryDetails.getVersions()) {
            if (versionName.equals(version.getName())) {
                return version;
            }
        }
        return null;
    }

    /**
     * Updates the library details (invoked when library details are provided).
     * 
     * @param libraryDetails library details.
     */
    private void updateLibraryDetails(Library libraryDetails) {
        this.libraryDetails = libraryDetails;
        loadingLibraryDetails = false;
        if (libraryDetails != null) {
            Library.Version installedVersion = (Library.Version)installVersionCombo.getSelectedItem();
            updateInstalledCombo(installedVersion);
        }
    }

    /** The last selected version. */
    private Library.Version lastSelectedVersion;

    /**
     * Updates the version combo-box. It sets its model (to match the versions
     * in library details) and selected item (to match the specified installed
     * version). 
     * 
     * @param installedVersion version to select in version combo-box.
     */
    private void updateInstalledCombo(Library.Version installedVersion) {
        DefaultComboBoxModel<Library.Version> model = new DefaultComboBoxModel<>();
        Library.Version[] versions = libraryDetails.getVersions();
        if (versions == null) {
            if (installedVersion == null) {
                // PENDING insert dummy item that will show 'Loading versions'
            } else {
                model.addElement(installedVersion);
            }
        } else {
            for (Library.Version version : versions) {
                model.addElement(version);
            }
        }
        installVersionCombo.setModel(model);
        lastSelectedVersion = installedVersion;
        installVersionCombo.setSelectedItem(installedVersion);
    }

    /**
     * Loads details of the given library.
     * 
     * @param library library whose details should be loaded.
     */
    private void loadLibraryDetails(final Library library) {
        final Library details = libraryProvider.libraryDetails(library.getName(), false);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (library.getName().equals(libraryDetails.getName())) {
                    // PENDING notify the user when library details are not available
                    updateLibraryDetails(details);
                } // else we've received details for a library that is no longer selected
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        installVersionLabel = new javax.swing.JLabel();
        installVersionCombo = new javax.swing.JComboBox<Library.Version>();
        requiredVersionLabel = new javax.swing.JLabel();
        requiredVersionField = new javax.swing.JTextField();

        installVersionLabel.setLabelFor(installVersionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(installVersionLabel, org.openide.util.NbBundle.getMessage(EditPanel.class, "EditPanel.installVersionLabel.text")); // NOI18N

        installVersionCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                installVersionComboPopupMenuWillBecomeVisible(evt);
            }
        });
        installVersionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installVersionComboActionPerformed(evt);
            }
        });

        requiredVersionLabel.setLabelFor(requiredVersionField);
        org.openide.awt.Mnemonics.setLocalizedText(requiredVersionLabel, org.openide.util.NbBundle.getMessage(EditPanel.class, "EditPanel.requiredVersionLabel.text")); // NOI18N

        requiredVersionField.setColumns(10);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(installVersionLabel)
                    .addComponent(requiredVersionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(installVersionCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(requiredVersionField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(installVersionLabel)
                    .addComponent(installVersionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(requiredVersionLabel)
                    .addComponent(requiredVersionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void installVersionComboPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_installVersionComboPopupMenuWillBecomeVisible
        if (libraryDetails != null && libraryDetails.getVersions() == null && !loadingLibraryDetails) {
            loadingLibraryDetails = true;
            final Library library = libraryDetails;
            RP.post(new Runnable() {
                @Override
                public void run() {
                    loadLibraryDetails(library);
                }
            });
        }
    }//GEN-LAST:event_installVersionComboPopupMenuWillBecomeVisible

    private void installVersionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installVersionComboActionPerformed
        String lastSelectedVersionName = lastSelectedVersion == null ? null : lastSelectedVersion.getName();
        String requiredVersion = requiredVersionField.getText();
        lastSelectedVersion = (Library.Version)installVersionCombo.getSelectedItem();
        if ((lastSelectedVersionName == null || lastSelectedVersionName.equals(requiredVersion))
                && lastSelectedVersion != null) {
            String newRequiredVersion = lastSelectedVersion.getName();
            if (Library.Version.LATEST_VERSION_PLACEHOLDER.equals(newRequiredVersion)) {
                newRequiredVersion = "*"; // NOI18N
            }
            requiredVersionField.setText(newRequiredVersion);
        }
    }//GEN-LAST:event_installVersionComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<Library.Version> installVersionCombo;
    private javax.swing.JLabel installVersionLabel;
    private javax.swing.JTextField requiredVersionField;
    private javax.swing.JLabel requiredVersionLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Renderer of the versions in version combo-box.
     */
    private class VersionRenderer extends DefaultListCellRenderer {

        @NbBundle.Messages({
            "EditPanel.loadingVersions=Loading...",
            "EditPanel.latestVersionPlaceholder=latest"
        })
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object renderedValue;
            if (value instanceof Library.Version) {
                String versionName = ((Library.Version)value).getName();
                if (Library.Version.LATEST_VERSION_PLACEHOLDER.equals(versionName)) {
                    versionName = Bundle.EditPanel_latestVersionPlaceholder();
                }
                renderedValue = versionName;
            } else {
                renderedValue = value;
            }
            if (loadingLibraryDetails) {
                renderedValue = Bundle.EditPanel_loadingVersions();
            }
            return super.getListCellRendererComponent(list, renderedValue, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
        }
        
    }

}
