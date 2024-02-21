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
package org.netbeans.modules.editor.fold.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import static org.netbeans.modules.editor.fold.ui.Bundle.*;
import org.netbeans.spi.options.OptionsPanelController;

/**
 * UI for the folding enable + language switch.
 * The panel contains a placeholder for language-specific contents. When language is selected, it replaces the
 * interior with a language-specific panel.
 *
 * @author sdedic
 */
@OptionsPanelController.Keywords(keywords = {"#KW_Options"}, location = OptionsDisplayer.EDITOR, tabTitle="#CTL_OptionsDisplayName")
final class FoldOptionsPanel extends javax.swing.JPanel implements ActionListener, PreferenceChangeListener {
    /**
     * All mime types presented in the selector
     */
    private List<String[]>  languageMimeTypes;
    
    /**
     * Our controller
     */
    private FoldOptionsController ctrl;
    
    private Preferences parentPrefs;
    
    /**
     * Creates new form FoldOptionsPanel
     */
    public FoldOptionsPanel(FoldOptionsController ctrl) {
        this.ctrl = ctrl;
        initComponents();
        
        langSelect.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof String[]) {
                    value = ((String[])value)[1];
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
            }
        });
        langSelect.addActionListener(this);
        contentPreview.addActionListener(this);
        foldedSummary.addActionListener(this);
        
        // preferences should be set as a reaction to index selection
    }
    
    /**
     * The preferences object for the currently selected language
     */
    private Preferences currentPreferences;
    
    /**
     * Panels created for individual Mime types
     */
    private Map<String, JComponent> panels = new HashMap<String, JComponent>();
    
    private PreferenceChangeListener wPrefL = WeakListeners.create(PreferenceChangeListener.class, this, null);
    
    private void languageSelected() {
        String[] sel = (String[])langSelect.getSelectedItem();
        if (sel == null) {
            return;
        }
        String mime = sel[0];
        if (currentPreferences != null) {
            currentPreferences.removePreferenceChangeListener(wPrefL);
        }
        currentPreferences = ctrl.prefs(mime);
        JComponent panel = panels.get(mime);
        String parentMime = MimePath.parse(mime).getInheritedType();
        if (parentMime != null) {
            parentPrefs = ctrl.prefs(parentMime);
        } else {
            parentPrefs = null;
        }
        if (panel == null) {
            panel = new DefaultFoldingOptions(mime, currentPreferences);
            if (panel instanceof CustomizerWithDefaults) {
                    ((CustomizerWithDefaults)panel).setDefaultPreferences(parentPrefs);
            }
            panels.put(mime, panel);
            content.add(panel, mime);
        }
        ((CardLayout)content.getLayout()).show(content, mime);
        currentPreferences.addPreferenceChangeListener(wPrefL);
        useDefaults.setVisible(!"".equals(mime)); // NOI18N
        preferenceChange(null);
    }
    
    private void previewChanged() {
        currentPreferences.putBoolean(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW, contentPreview.isSelected());
    }
    
    private void summaryChanged() {
        currentPreferences.putBoolean(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY, foldedSummary.isSelected());
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (ignoreEnableTrigger) {
            return;
        }
        if (o == langSelect) {
            languageSelected();
        } else if (o == contentPreview) {
            previewChanged();
        } else if (o == foldedSummary) {
            summaryChanged();
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        String k = evt == null ? null : evt.getKey();
        ignoreEnableTrigger = true;
        try {
            if (k == null || k.equals(FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS)) {
                useDefaults.setSelected(currentPreferences.getBoolean(FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS, true));
            }
            if (k == null || k.equals(SimpleValueNames.CODE_FOLDING_ENABLE)) {
                boolean enabled = currentPreferences.getBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, true);
                enableFolds.setSelected(enabled);
                contentPreview.setEnabled(enabled);
                foldedSummary.setEnabled(enabled);
                useDefaults.setEnabled(enabled);
            } 
            if (k == null || FoldUtilitiesImpl.PREF_CONTENT_PREVIEW.equals(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW)) {
                contentPreview.setSelected(currentPreferences.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW, true));
            }
            if (k == null || FoldUtilitiesImpl.PREF_CONTENT_SUMMARY.equals(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY)) {
                foldedSummary.setSelected(currentPreferences.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY, true));
            } 
            // must not replicate defaults over current settings if unspecified key arrives.
            if (k != null && FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS.equals(k)) {
                boolean b = parentPrefs == null || !currentPreferences.getBoolean(FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS, true);
                if (parentPrefs != null) {
                    if (b) {
                        currentPreferences.putBoolean(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW, 
                                parentPrefs.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW, true));
                        currentPreferences.putBoolean(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY, 
                                parentPrefs.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY, true));
                    } else {
                        currentPreferences.remove(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW);
                        currentPreferences.remove(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY);
                    }
                }
                contentPreview.setEnabled(b);
                foldedSummary.setEnabled(b);
                contentPreview.setSelected(currentPreferences.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW, true));
                foldedSummary.setSelected(currentPreferences.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY, true));
            }
        } finally {
            ignoreEnableTrigger = false;
        }
    }
    
    boolean isChanged() {
        boolean isChanged= false;
        for(JComponent panel : panels.values()) {
            if(panel instanceof DefaultFoldingOptions) {
                isChanged |= ((DefaultFoldingOptions)panel).isChanged();
            }
        }
        return isChanged;
    }
    
    void update() {
        initialize();
    }
    
    @NbBundle.Messages({
        "ITEM_AllLanguages=All Languages"
    })
    private void initialize() {
        Collection<String> mimeTypes = ctrl.getUpdatedLanguages();
        List<String[]> langMimes = new ArrayList<String[]>(mimeTypes.size());
        langMimes.add(new String[] { "", ITEM_AllLanguages() }); // NOI18N
        for (String s : mimeTypes) {
            Language l = Language.find(s);
            if (l == null) {
                continue;
            }
            // filter out languages, whose author didn't name it - probably
            // unimportant && should not be displayed.
            String name = EditorSettings.getDefault().getLanguageName(s);
            if (name.equals(s)) {
                continue;
            }
            // last, discard everything that does not have any FoldTypes:
            if (FoldUtilities.getFoldTypes(s).values().isEmpty()) {
                continue;
            }
            langMimes.add(new String[] {
                s, EditorSettings.getDefault().getLanguageName(s)
            });
        }
        langMimes.sort(LANG_COMPARATOR);
        languageMimeTypes = langMimes;
        int idx = langSelect.getSelectedIndex();
        langSelect.setModel(new DefaultComboBoxModel(languageMimeTypes.toArray(new Object[0])));
        langSelect.setSelectedIndex(idx >= 0 && idx < langSelect.getItemCount() ? idx : 0);
    }
    
    void clear() {
        panels.clear();
    }

    /**
     * Special comparator, which sorts "" mime type first, other Mimetypes are then sorted based on the language names,
     * alphabetically. It is expected  that the 1st member of the String[]is a Mimetype string, the 2nd member is a 
     * human-readable language name.
     */
    private static final Comparator<String[]> LANG_COMPARATOR = new Comparator<String[]>() {
        @Override
        public int compare(String[] o1, String[] o2) {
            if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            if (o1[0].equals(o2[0])) {
                return 0;
            }
            // sort 'all languages' first
            if (o1[0].length() == 0) {
                return -1;
            } else if (o2[0].length() == 0) {
                return 1;
            }
            return o1[1].compareToIgnoreCase(o2[1]);
        }
    };
    
    private boolean ignoreEnableTrigger;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        langSelect = new javax.swing.JComboBox();
        langLabel = new javax.swing.JLabel();
        content = new javax.swing.JPanel();
        enableFolds = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        contentPreview = new javax.swing.JCheckBox();
        foldedSummary = new javax.swing.JCheckBox();
        useDefaults = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(langLabel, org.openide.util.NbBundle.getMessage(FoldOptionsPanel.class, "FoldOptionsPanel.langLabel.text")); // NOI18N

        content.setLayout(new java.awt.CardLayout());

        org.openide.awt.Mnemonics.setLocalizedText(enableFolds, org.openide.util.NbBundle.getMessage(FoldOptionsPanel.class, "FoldOptionsPanel.enableFolds.text")); // NOI18N
        enableFolds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableFoldsActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FoldOptionsPanel.class, "Title_FoldDisplayOptions"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(contentPreview, org.openide.util.NbBundle.getMessage(FoldOptionsPanel.class, "FoldOptionsPanel.contentPreview.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(foldedSummary, org.openide.util.NbBundle.getMessage(FoldOptionsPanel.class, "FoldOptionsPanel.foldedSummary.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentPreview)
                    .addComponent(foldedSummary))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contentPreview)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(foldedSummary)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(useDefaults, org.openide.util.NbBundle.getMessage(FoldOptionsPanel.class, "FoldOptionsPanel.useDefaults.text")); // NOI18N
        useDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useDefaultsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(useDefaults)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(langLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(langSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(enableFolds)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(langSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(langLabel)
                    .addComponent(enableFolds))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(useDefaults)
                .addGap(12, 12, 12)
                .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void enableFoldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableFoldsActionPerformed
        if (ignoreEnableTrigger) {
            return;
        }
        boolean enable = enableFolds.isSelected();
        currentPreferences.putBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, enable);
        // visual feedback handled by listener.
    }//GEN-LAST:event_enableFoldsActionPerformed

    private void useDefaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useDefaultsActionPerformed
        if (ignoreEnableTrigger) {
            return;
        }
        currentPreferences.putBoolean(FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS, 
                useDefaults.isSelected());
    }//GEN-LAST:event_useDefaultsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel content;
    private javax.swing.JCheckBox contentPreview;
    private javax.swing.JCheckBox enableFolds;
    private javax.swing.JCheckBox foldedSummary;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel langLabel;
    private javax.swing.JComboBox langSelect;
    private javax.swing.JCheckBox useDefaults;
    // End of variables declaration//GEN-END:variables
}
