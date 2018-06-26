/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * Icefaces2CustomizerPanelVisual.java
 *
 * Created on Aug 22, 2011, 9:00:29 AM
 */
package org.netbeans.modules.web.jsf.icefaces.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.jsf.icefaces.Icefaces2Customizer;
import org.netbeans.modules.web.jsf.icefaces.Icefaces2Implementation;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Fousek <marfous@nebeans.org>
 */
public class Icefaces2CustomizerPanelVisual extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(Icefaces2CustomizerPanelVisual.class.getName());
    private static final long serialVersionUID = 1L;
    private static final RequestProcessor RP = new RequestProcessor("Icefaces Libraries Loader"); //NOI18N

    private volatile Set<Library> icefacesLibraries = new HashSet<Library>();
    private final Icefaces2Customizer customizer;
    private ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates new form Icefaces2CustomizerPanelVisual.
     */
    public Icefaces2CustomizerPanelVisual(Icefaces2Customizer customizer) {
        this.customizer = customizer;
        initComponents();
        noteLabel.setPreferredSize(new Dimension(1, 1));

        changeSupport.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Icefaces2CustomizerPanelVisual.this.customizer.fireChange();
            }
        });

        initLibraries(true);

        icefacesLibraryComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    /**
     * Initialize {@link #icefacesLibraryComboBox} with all ICEfaces2 libraries if needed.
     *
     * @param setStoredValue {@code true} if should be selected stored value from preferences,
     * {@code false} otherwise
     */
    public final void initLibraries(final boolean setStoredValue) {
        long time = System.currentTimeMillis();
        final List<String> registeredRichfaces = new ArrayList<String>();

        RP.post(new Runnable() {
            @Override
            public void run() {
                for (Library library : Icefaces2Customizer.getIcefacesLibraries()) {
                    registeredRichfaces.add(library.getDisplayName());
                    icefacesLibraries.add(library);
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setLibrariesComboBox(icefacesLibraryComboBox, registeredRichfaces);

                        if (setStoredValue && !icefacesLibraries.isEmpty()) {
                            setDefaultComboBoxValues();
                        } else {
                            customizer.setFixedLibrary(!icefacesLibraries.isEmpty());
                            changeSupport.fireChange();
                        }
                    }
                });
            }
        });

        LOGGER.log(Level.FINEST, "Time spent in {0} initLibraries = {1} ms", //NOI18N
                new Object[]{this.getClass().getName(), System.currentTimeMillis() - time});
    }

    /**
     * Gets in combo box chosen ICEfaces2 library.
     *
     * @return name of selected library
     */
    public String getIcefacesLibrary() {
        if (icefacesLibraryComboBox.getSelectedItem() != null) {
            return (String) icefacesLibraryComboBox.getSelectedItem();
        }
        return null;
    }

    /**
     * Gets the error message from panel.
     *
     * @return error {@code String} in cases of any error, {@code null} otherwise
     */
    public String getErrorMessage() {
        if (icefacesLibraries == null || icefacesLibraries.isEmpty()) {
            return NbBundle.getMessage(Icefaces2CustomizerPanelVisual.class, "LBL_MissingIcefacesLibraries"); //NOI18N
        }
        return null;
    }

   /**
     * Gets the warning message from panel.
     *
     * @return warning {@code String} in cases of any warning, {@code null} otherwise
     */
    public String getWarningMessage() {
        return null;
    }

    private void setDefaultComboBoxValues() {
        Preferences preferences = Icefaces2Implementation.getIcefacesPreferences();
        icefacesLibraryComboBox.setSelectedItem(
                preferences.get(Icefaces2Implementation.PREF_LIBRARY_NAME, "")); //NOI18N
    }

    private void setLibrariesComboBox(JComboBox comboBox, List<String> items) {
        comboBox.setModel(new DefaultComboBoxModel(items.toArray()));
        comboBox.setEnabled(!items.isEmpty());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerLabel = new javax.swing.JLabel();
        icefacesLibraryLabel = new javax.swing.JLabel();
        icefacesLibraryComboBox = new javax.swing.JComboBox();
        notExistingLibraryLabel = new javax.swing.JLabel();
        createIcefacesLibraryButton = new javax.swing.JButton();
        noteLabel = new javax.swing.JLabel();

        headerLabel.setText(org.openide.util.NbBundle.getMessage(Icefaces2CustomizerPanelVisual.class, "Icefaces2CustomizerPanelVisual.headerLabel.text")); // NOI18N

        icefacesLibraryLabel.setText(org.openide.util.NbBundle.getMessage(Icefaces2CustomizerPanelVisual.class, "Icefaces2CustomizerPanelVisual.icefacesLibraryLabel.text")); // NOI18N

        icefacesLibraryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Search ICEfaces Libraries..." }));

        notExistingLibraryLabel.setText(org.openide.util.NbBundle.getMessage(Icefaces2CustomizerPanelVisual.class, "Icefaces2CustomizerPanelVisual.notExistingLibraryLabel.text")); // NOI18N

        createIcefacesLibraryButton.setText(org.openide.util.NbBundle.getMessage(Icefaces2CustomizerPanelVisual.class, "Icefaces2CustomizerPanelVisual.createIcefacesLibraryButton.text")); // NOI18N
        createIcefacesLibraryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createIcefacesLibraryButtonActionPerformed(evt);
            }
        });

        noteLabel.setText(org.openide.util.NbBundle.getMessage(Icefaces2CustomizerPanelVisual.class, "Icefaces2CustomizerPanelVisual.noteLabel.text")); // NOI18N
        noteLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        noteLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(icefacesLibraryLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(icefacesLibraryComboBox, 0, 336, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(notExistingLibraryLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createIcefacesLibraryButton))
                    .addComponent(noteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 501, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(icefacesLibraryLabel)
                    .addComponent(icefacesLibraryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(notExistingLibraryLabel)
                    .addComponent(createIcefacesLibraryButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(noteLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void createIcefacesLibraryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createIcefacesLibraryButtonActionPerformed
    LibrariesCustomizer.showCreateNewLibraryCustomizer(LibraryManager.getDefault());
    initLibraries(false);
}//GEN-LAST:event_createIcefacesLibraryButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createIcefacesLibraryButton;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JComboBox icefacesLibraryComboBox;
    private javax.swing.JLabel icefacesLibraryLabel;
    private javax.swing.JLabel notExistingLibraryLabel;
    private javax.swing.JLabel noteLabel;
    // End of variables declaration//GEN-END:variables

}
