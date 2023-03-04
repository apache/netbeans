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

/*
 * Richfaces4CustomizerPanelVisual.java
 *
 * Created on Jun 22, 2011, 1:25:30 PM
 */
package org.netbeans.modules.web.jsf.richfaces.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.web.jsf.richfaces.Richfaces4Customizer;
import org.netbeans.modules.web.jsf.richfaces.Richfaces4Implementation;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class Richfaces4CustomizerPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider {

    public static final RequestProcessor RP = new RequestProcessor("JSF Component Libraries Updater", 1);
    public static final Logger LOGGER = Logger.getLogger(Richfaces4CustomizerPanelVisual.class.getName());

    private volatile Set<Library> richfacesLibraries = new HashSet<Library>();
    private final Richfaces4Customizer customizer;
    private ChangeSupport changeSupport = new ChangeSupport(this);

    /** Creates new form Richfaces4CustomizerPanelVisual */
    public Richfaces4CustomizerPanelVisual(Richfaces4Customizer customizer) {
        this.customizer = customizer;
        initComponents();
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Richfaces4CustomizerPanelVisual.this.customizer.fireChange();
            }
        });

        initLibraries(true);

        richfacesComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void initLibraries(final boolean firstInit) {
        long time = System.currentTimeMillis();
        final List<String> registeredRichfaces = new ArrayList<String>();

        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                for (Library library : Richfaces4Customizer.getRichfacesLibraries()) {
                    registeredRichfaces.add(library.getDisplayName());
                    richfacesLibraries.add(library);
                }

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        setLibrariesComboBox(richfacesComboBox, registeredRichfaces);

                        if (firstInit && !richfacesLibraries.isEmpty()) {
                            setDefaultComboBoxValues();
                        } else {
                            customizer.setFixedLibrary(!registeredRichfaces.isEmpty());
                            changeSupport.fireChange();
                        }
                    }
                });
            }
        });

        LOGGER.log(Level.FINEST, "Time spent in {0} initLibraries = {1} ms", //NOI18N
                new Object[]{this.getClass().getName(), System.currentTimeMillis() - time});   //NOI18N
    }

    private void setDefaultComboBoxValues() {
        Preferences preferences = Richfaces4Implementation.getRichfacesPreferences();
        richfacesComboBox.setSelectedItem(preferences.get(Richfaces4Implementation.PREF_RICHFACES_LIBRARY, ""));
    }

    private void setLibrariesComboBox(JComboBox comboBox, List<String> items) {
        comboBox.setModel(new DefaultComboBoxModel(items.toArray()));
        comboBox.setEnabled(!items.isEmpty());
    }

    public String getErrorMessage() {
        if (richfacesLibraries == null || richfacesLibraries.isEmpty()) {
            return NbBundle.getMessage(Richfaces4CustomizerPanelVisual.class, "LBL_MissingRichFaces"); //NOI18N
        }
        return null;
    }

    public String getWarningMessage() {
        if (richfacesLibraries == null || !richfacesLibraries.isEmpty()) {
            Library library = LibraryManager.getDefault().getLibrary(getRichFacesLibrary());
            if (library == null) {
                return null;
            }

            List<URL> content = library.getContent("classpath"); //NOI18N
            StringBuilder recommendedJars = new StringBuilder();
            if (library != null) {
                Set<Entry<String, String>> entrySet = Richfaces4Implementation.RF_DEPENDENCIES.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    try {
                        if (!ClasspathUtil.containsClass(content, entry.getKey())) {
                            recommendedJars.append(entry.getValue()).append(", ");
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    }
                }
                if (!"".equals(recommendedJars.toString())) {
                    return NbBundle.getMessage(Richfaces4CustomizerPanelVisual.class,
                            "LBL_MissingDependency", recommendedJars .toString().substring(0,
                            recommendedJars .toString().length() - 2)); //NOI18N
                }
            }
        }
        return null;
    }

    public String getRichFacesLibrary() {
        return (String)richfacesComboBox.getSelectedItem();
    }

   @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(Richfaces4CustomizerPanelVisual.class);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        richfacesLibraryLabel = new javax.swing.JLabel();
        richfacesComboBox = new javax.swing.JComboBox();
        newLibraryButton = new javax.swing.JButton();
        richfacesInfoLabel = new javax.swing.JLabel();

        richfacesLibraryLabel.setText(org.openide.util.NbBundle.getMessage(Richfaces4CustomizerPanelVisual.class, "Richfaces4CustomizerPanelVisual.richfacesLibraryLabel.text")); // NOI18N

        richfacesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Search RichFaces Libraries..." }));

        newLibraryButton.setText(org.openide.util.NbBundle.getMessage(Richfaces4CustomizerPanelVisual.class, "Richfaces4CustomizerPanelVisual.newLibraryButton.text")); // NOI18N
        newLibraryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newLibraryButtonActionPerformed(evt);
            }
        });

        richfacesInfoLabel.setText(org.openide.util.NbBundle.getMessage(Richfaces4CustomizerPanelVisual.class, "Richfaces4CustomizerPanelVisual.richfacesInfoLabel.text")); // NOI18N
        richfacesInfoLabel.setPreferredSize(new java.awt.Dimension(100, 15));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(richfacesLibraryLabel)
                        .addGap(11, 11, 11)
                        .addComponent(richfacesComboBox, 0, 395, Short.MAX_VALUE))
                    .addComponent(newLibraryButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(richfacesInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(richfacesLibraryLabel)
                    .addComponent(richfacesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newLibraryButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(richfacesInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newLibraryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newLibraryButtonActionPerformed
        LibrariesCustomizer.showCreateNewLibraryCustomizer(LibraryManager.getDefault());
        initLibraries(false);
    }//GEN-LAST:event_newLibraryButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton newLibraryButton;
    private javax.swing.JComboBox richfacesComboBox;
    private javax.swing.JLabel richfacesInfoLabel;
    private javax.swing.JLabel richfacesLibraryLabel;
    // End of variables declaration//GEN-END:variables

}
