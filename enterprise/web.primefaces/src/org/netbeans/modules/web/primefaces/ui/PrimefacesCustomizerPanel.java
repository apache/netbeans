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
package org.netbeans.modules.web.primefaces.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.primefaces.PrimefacesCustomizer;
import org.netbeans.modules.web.primefaces.PrimefacesImplementation;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for choosing Primefaces libraries for project.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@SuppressWarnings("serial")
public class PrimefacesCustomizerPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private static final Logger LOGGER = Logger.getLogger(PrimefacesCustomizerPanel.class.getName());
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final PrimefacesCustomizer customizer;

    /**
     * Creates new form PrimefacesCustomizerPanel.
     * @param customizer PrimeFaces customizer
     */
    public PrimefacesCustomizerPanel(PrimefacesCustomizer customizer) {
        this.customizer = customizer;
        initComponents();
        changeSupport.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                PrimefacesCustomizerPanel.this.customizer.fireChange();
            }
        });
        initLibraries(true);

        primefacesLibrariesComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    /**
     * Initialize {@link #primefacesLibrariesComboBox} with all PrimeFaces libraries.
     * @param setStoredValue {@code true} if should be selected stored value from preferences, {@code false} otherwise
     */
    @NbBundle.Messages({
        "PrimefacesCustomizerPanel.lbl.searching=Searching Primefaces Libraries..."
    })
    public final void initLibraries(final boolean setStoredValue) {
        setPrimefacesLibrariesComboBox(Arrays.asList(Bundle.PrimefacesCustomizerPanel_lbl_searching()));
        long time = System.currentTimeMillis();
        final List<Library> primefacesLibraries = new ArrayList<>();

        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                for (Library library : PrimefacesImplementation.getAllRegisteredPrimefaces()) {
                    primefacesLibraries.add(library);
                }

                // update the combo box with libraries
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setPrimefacesLibrariesComboBox(primefacesLibraries);
                        if (setStoredValue && !primefacesLibraries.isEmpty()) {
                            setDefaultPrimefacesComboBoxValue(primefacesLibraries);
                        } else {
                            customizer.setFixedLibrary(!primefacesLibraries.isEmpty());
                            changeSupport.fireChange();
                        }
                    }
                });
            }
        });

        LOGGER.log(Level.FINEST, "Time spent in {0} initLibraries = {1} ms",
                new Object[]{this.getClass().getName(), System.currentTimeMillis() - time});
    }

    /**
     * Gets in combo box chosen PrimeFaces library.
     * @return name of selected library
     */
    public Library getPrimefacesLibrary() {
        Object selectedItem = primefacesLibrariesComboBox.getSelectedItem();
        if (selectedItem instanceof Library) {
            return (Library) selectedItem;
        }
        return null;
    }

    private void setPrimefacesLibrariesComboBox(List items) {
        primefacesLibrariesComboBox.setModel(new DefaultComboBoxModel(items.toArray()));
        primefacesLibrariesComboBox.setRenderer(new LibraryComboBoxRenderer());
        primefacesLibrariesComboBox.setEnabled(!items.isEmpty());
    }

    private void setDefaultPrimefacesComboBoxValue(List<Library> foundLibraries) {
        Preferences preferences = PrimefacesImplementation.getPrimefacesPreferences();
        String preferred = preferences.get(PrimefacesImplementation.PROP_PREFERRED_LIBRARY, ""); //NOI18N
        for (Library library : foundLibraries) {
            if (library.getName().equals(preferred)) {
                primefacesLibrariesComboBox.setSelectedItem(library);
            }
        }
    }

    /**
     * Gets error messages from the panel.
     * @return error message in cases of any error, {@code null} otherwise
     */
    @NbBundle.Messages("PrimefacesCustomizerPanel.MissingLibraries.label=No valid PrimeFaces libraries found.")
    public String getErrorMessage() {
        if (getPrimefacesLibrary() == null) {
            return Bundle.PrimefacesCustomizerPanel_MissingLibraries_label();
        }
        return null;
    }

   /**
     * Gets warning messages from the panel.
     * @return warning message in cases of any warning, {@code null} otherwise
     */
    public String getWarningMessage() {
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        primefacesLibraryLabel = new javax.swing.JLabel();
        primefacesLibrariesComboBox = new javax.swing.JComboBox();
        createLibraryButton = new javax.swing.JButton();
        noteLabel = new javax.swing.JLabel();

        primefacesLibraryLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/primefaces/ui/Bundle").getString("PrimefacesCustomizerPanel.primefacesLibraryLabel.mnemonics").charAt(0));
        primefacesLibraryLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        primefacesLibraryLabel.setLabelFor(primefacesLibrariesComboBox);
        primefacesLibraryLabel.setText(org.openide.util.NbBundle.getMessage(PrimefacesCustomizerPanel.class, "PrimefacesCustomizerPanel.primefacesLibraryLabel.text")); // NOI18N

        primefacesLibrariesComboBox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        primefacesLibrariesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Searching Primefaces Libraries..." }));

        createLibraryButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/primefaces/ui/Bundle").getString("PrimefacesCustomizerPanel.createLibraryButton.mnemonic").charAt(0));
        createLibraryButton.setText(org.openide.util.NbBundle.getMessage(PrimefacesCustomizerPanel.class, "PrimefacesCustomizerPanel.createLibraryButton.text")); // NOI18N
        createLibraryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLibraryButtonActionPerformed(evt);
            }
        });

        noteLabel.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        noteLabel.setText(org.openide.util.NbBundle.getMessage(PrimefacesCustomizerPanel.class, "PrimefacesCustomizerPanel.noteLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(primefacesLibraryLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(primefacesLibrariesComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(createLibraryButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(noteLabel, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(primefacesLibraryLabel)
                    .addComponent(primefacesLibrariesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createLibraryButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(noteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createLibraryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLibraryButtonActionPerformed
        LibrariesCustomizer.showCreateNewLibraryCustomizer(LibraryManager.getDefault());
        initLibraries(false);
    }//GEN-LAST:event_createLibraryButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createLibraryButton;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JComboBox primefacesLibrariesComboBox;
    private javax.swing.JLabel primefacesLibraryLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private class LibraryComboBoxRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Library) {
                ((JLabel) component).setText(((Library) value).getDisplayName());
            } else {
                ((JLabel) component).setText((String) value);
            }
            return component;
        }

    }
}
