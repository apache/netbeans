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
package org.netbeans.spi.editor.hints.projects;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.netbeans.spi.editor.hints.projects.PerProjectHintsPanel.MimeType2Preferences;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences.GlobalHintPreferencesProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**XXX: should not be visible in the API as a panel!
 *
 * @author lahvac
 */
class PerProjectHintsPanelUI extends javax.swing.JPanel {

    private static final Logger LOG = Logger.getLogger(PerProjectHintsPanelUI.class.getName());
    private MimeType2Preferences globalPreferencesProvider;
    private MimeType2Preferences preferencesProvider;
    private final Map<LanguageDescription, OptionsPanelController> mimeType2OptionsController = new HashMap<>();
    private final Map<LanguageDescription, JComponent> mimeType2OptionsPanel = new HashMap<>();
    private final Set<String> supportsFiltering = new HashSet<>();
    
    public PerProjectHintsPanelUI(FileObject customizersFolder) {
        this.globalPreferencesProvider = new MimeType2Preferences() {
            @Override public Preferences getPreferences(String mimeType) {
                for (GlobalHintPreferencesProvider p : MimeLookup.getLookup(mimeType).lookupAll(GlobalHintPreferencesProvider.class)) {
                    Preferences prefs = p.getGlobalPreferences();

                    if (prefs != null) {
                        return prefs;
                    }
                }

                throw new IllegalStateException("Must have some working GlobalHintPreferencesProvider!");
            }
        };

        initComponents();
        for (FileObject customizer : customizersFolder.getChildren()) {
            try {
                InstanceCookie ic = customizer.getLookup().lookup(InstanceCookie.class);
                
                if (ic == null) continue;
                
                Object value = ic.instanceCreate();
                
                if (value instanceof OptionsPanelController) {
                    Object mimeType = customizer.getAttribute("mimeType");
                    
                    if (!(mimeType instanceof String)) {
                        LOG.log(Level.WARNING, "{0} does not provide a string-based mimeType!", FileUtil.getFileDisplayName(customizer));
                        continue;
                    }
                    
                    FileObject editorFolder = FileUtil.getConfigFile("Editors/" + (String) mimeType);
                    String mimeDN = editorFolder != null ? getFileObjectLocalizedName(editorFolder, (String) mimeType) : (String) mimeType;
                    
                    mimeType2OptionsController.put(new LanguageDescription((String) mimeType, mimeDN), (OptionsPanelController) value);
                }
            } catch (IOException | ClassNotFoundException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
        
        LanguageDescription[] mimeTypes = mimeType2OptionsController.keySet().toArray(new LanguageDescription[0]);
        
        Arrays.sort(mimeTypes, new Comparator<LanguageDescription>() {
            @Override public int compare(LanguageDescription o1, LanguageDescription o2) {
                return o1.displayName.compareTo(o2.displayName);
            }
        });
        
        customPanel.setLayout(new BorderLayout());
        languageCombo.setModel(new DefaultComboBoxModel(mimeTypes));
        if (mimeTypes.length > 0)
            languageCombo.setSelectedIndex(0);
        languageCombo.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                change();
            }
        });
        
        searchEnableDisable();
    }

    public void setPerProjectSettings(final MimeType2Preferences preferences) {
        this.preferencesProvider = preferences;
        mimeType2OptionsPanel.clear();
        change();
    }
    
    public void setGlobalSettings() {
        this.preferencesProvider = globalPreferencesProvider;
        mimeType2OptionsPanel.clear();
        change();
    }
    
    private void change() {
        final LanguageDescription mimeType = (LanguageDescription) languageCombo.getSelectedItem();
        customPanel.removeAll();
        
        if (mimeType != null) {
            JComponent panel = mimeType2OptionsPanel.get(mimeType);
            
            if (panel == null) {
                OptionsPanelController c = mimeType2OptionsController.get(mimeType);
                panel = c.getComponent(Lookups.fixed(PerProjectHintsPanelUI.this.preferencesProvider.getPreferences(mimeType.mimeType),
                                                     OptionsFilter.create(searchText.getDocument(), new Runnable() {
                    @Override public void run() {
                        supportsFiltering.add(mimeType.mimeType);
                        searchEnableDisable();
                    }
                })));
                mimeType2OptionsPanel.put(mimeType, panel);
                c.update();
            }
            customPanel.add(panel, BorderLayout.CENTER);
        }
        
        customPanel.invalidate();
        customPanel.revalidate();
    }
    
    private static String getFileObjectLocalizedName(FileObject fo, String mimeType) {
        Object o = fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
        if ( o instanceof String ) {
            String bundleName = (String)o;
            try {
                ResourceBundle rb = NbBundle.getBundle(bundleName);
                String localizedName = rb.getString(mimeType);
                return localizedName;
            }
            catch(MissingResourceException ex ) {
                // Do nothing return file path;
            }
        }
        return mimeType;
    }
    
    private void searchEnableDisable() {
        searchText.setEnabled(supportsFiltering.contains(((LanguageDescription) languageCombo.getSelectedItem()).mimeType));
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        languageCombo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        searchText = new javax.swing.JTextField();
        customPanel = new javax.swing.JPanel();

        jLabel1.setLabelFor(languageCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PerProjectHintsPanelUI.class, "PerProjectHintsPanelUI.jLabel1.text")); // NOI18N

        languageCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        languageCombo.setPrototypeDisplayValue("9999999999");

        jLabel2.setLabelFor(searchText);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PerProjectHintsPanelUI.class, "PerProjectHintsPanelUI.jLabel2.text")); // NOI18N

        searchText.setColumns(10);
        searchText.setText(org.openide.util.NbBundle.getMessage(PerProjectHintsPanelUI.class, "PerProjectHintsPanelUI.searchText.text")); // NOI18N

        javax.swing.GroupLayout customPanelLayout = new javax.swing.GroupLayout(customPanel);
        customPanel.setLayout(customPanelLayout);
        customPanelLayout.setHorizontalGroup(
            customPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        customPanelLayout.setVerticalGroup(
            customPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(languageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(customPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(languageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(searchText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(customPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox languageCombo;
    private javax.swing.JTextField searchText;
    // End of variables declaration//GEN-END:variables

    public void applyChanges() {
        for (Entry<LanguageDescription, OptionsPanelController> e : mimeType2OptionsController.entrySet()) {
            e.getValue().applyChanges();
        }
    }
    
    private static final class LanguageDescription {
        public final String mimeType;
        public final String displayName;

        public LanguageDescription(String mimeType, String displayName) {
            this.mimeType = mimeType;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;//TODO: should rather use renderer
        }
        
    }

}
