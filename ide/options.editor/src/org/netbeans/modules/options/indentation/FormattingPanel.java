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
package org.netbeans.modules.options.indentation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.netbeans.modules.options.util.LanguagesComparator;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Dusan Balek
 */
@OptionsPanelController.Keywords(keywords={"org.netbeans.modules.options.editor.Bundle#KW_FormattingPanel"}, location=OptionsDisplayer.EDITOR, tabTitle= "org.netbeans.modules.options.editor.Bundle#CTL_Formating_DisplayName")
public final class FormattingPanel extends JPanel implements PropertyChangeListener {
    
    /** Creates new form FormattingPanel */
    public FormattingPanel() {
        initComponents();
        
//        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
//            setOpaque(false);
//        }

        // Languages combobox renderer
        languageCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof String) {
                    value = ((String)value).length() > 0
                            ? EditorSettings.getDefault().getLanguageName((String)value)
                            : org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_AllLanguages"); //NOI18N                                
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }            
        });
        
        // Category combobox renderer
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof PreferencesCustomizer) {
                    value = ((PreferencesCustomizer) value).getDisplayName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }            
        });

    }

    private String storedMimeType = null;
    private String storedCategory = null;
    
    public void setSelector(CustomizerSelector selector) {
        if (selector == null) {
            storedMimeType = (String)languageCombo.getSelectedItem();
            Object o = categoryCombo.getSelectedItem();
            if (o instanceof PreferencesCustomizer) {
                storedCategory = ((PreferencesCustomizer)o).getId();
            }
        }
        
        if (this.selector != null) {
            this.selector.removePropertyChangeListener(weakListener);
        }

        this.selector = selector;

        if (this.selector != null) {
            // Languages combobox model
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            ArrayList<String> mimeTypes = new ArrayList<>(selector.getMimeTypes());
            mimeTypes.sort(LanguagesComparator.INSTANCE);

            String preSelectMimeType = null;
            for (String mimeType : mimeTypes) {
                model.addElement(mimeType);
                if (mimeType.equals(storedMimeType)) {
                    preSelectMimeType = mimeType;
                }
            }
            languageCombo.setModel(model);

            // Pre-select a language
            if (preSelectMimeType == null) {
                JTextComponent pane = EditorRegistry.lastFocusedComponent();
                preSelectMimeType = pane != null ? (String)pane.getDocument().getProperty("mimeType") : ""; // NOI18N
            }
            languageCombo.setSelectedItem(preSelectMimeType);
            if (!preSelectMimeType.equals(languageCombo.getSelectedItem())) {
                languageCombo.setSelectedIndex(0);
            }

            this.weakListener = WeakListeners.propertyChange(this, this.selector);
            this.selector.addPropertyChangeListener(weakListener);
            this.propertyChange(new PropertyChangeEvent(this.selector, CustomizerSelector.PROP_MIMETYPE, null, null));
        } else {
            languageCombo.setModel(new DefaultComboBoxModel());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || CustomizerSelector.PROP_MIMETYPE.equals(evt.getPropertyName())) {
            DefaultComboBoxModel<PreferencesCustomizer> model = new DefaultComboBoxModel<>();
            List<? extends PreferencesCustomizer> nue = selector.getCustomizers(selector.getSelectedMimeType());
            int preSelectIndex = 0;
            int idx = 0;
            for(PreferencesCustomizer c : nue) {
                model.addElement(c);
                if (c.getId().equals(storedCategory)) {
                    preSelectIndex = idx;
                }
                idx++;
            }
            categoryCombo.setModel(model);
            categoryCombo.setSelectedIndex(preSelectIndex);
        }

        if (evt.getPropertyName() == null || CustomizerSelector.PROP_CUSTOMIZER.equals(evt.getPropertyName())) {
            // remove the category customizer and its preview
            categoryPanel.setVisible(false);
            categoryPanel.removeAll();
            previewScrollPane.setVisible(false);

            // get the new category customizer
            PreferencesCustomizer c = selector.getSelectedCustomizer();
            if (c != null) {
                // there can be no category selected
                categoryPanel.add(c.getComponent(), BorderLayout.CENTER);
            }
            categoryPanel.setVisible(true);  

            // get the category customizer's preview component
            JComponent previewComponent;
            if (c instanceof PreviewProvider) {
                previewComponent = ((PreviewProvider) c).getPreviewComponent();
                previewComponent.setDoubleBuffered(true);
                if (previewComponent instanceof JTextComponent) {
                    Document doc = ((JTextComponent) previewComponent).getDocument();
                    // This is here solely for the purpose of previewing changes in formatting settings
                    // in Tools-Options. This is NOT, repeat NOT, to be used by anybody else!
                    // The name of this property is also hardcoded in editor.indent/.../CodeStylePreferences.java
                    doc.putProperty("Tools-Options->Editor->Formatting->Preview - Preferences", selector.getCustomizerPreferences(c)); //NOI18N
                }
            } else {
                JLabel noPreviewLabel = new JLabel(NbBundle.getMessage(FormattingPanel.class, "MSG_no_preview_available")); //NOI18N
                noPreviewLabel.setOpaque(true);
                noPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
                noPreviewLabel.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
                noPreviewLabel.setVisible(true);
                previewComponent = new JPanel(new BorderLayout());
                previewComponent.add(noPreviewLabel, BorderLayout.CENTER);
            }

            // add the preview component to the preview area
            previewScrollPane.setViewportView(previewComponent);
            previewScrollPane.setVisible(true);
            previewLabel.setLabelFor(previewComponent);

            if (c instanceof PreviewProvider) {
                final PreviewProvider pp = (PreviewProvider) c;
                SwingUtilities.invokeLater(pp::refreshPreview);
            }
            jSplitPane1.resetToPreferredSizes();
            
            // parent might need a scrollbar now due to category panel change
            if (getParent() != null) {
                getParent().validate();
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        previewPanel = new javax.swing.JPanel();
        previewLabel = new javax.swing.JLabel();
        previewScrollPane = new javax.swing.JScrollPane();
        optionsPanel = new javax.swing.JPanel();
        languageLabel = new javax.swing.JLabel();
        languageCombo = new javax.swing.JComboBox();
        categoryLabel = new javax.swing.JLabel();
        categoryCombo = new javax.swing.JComboBox();
        categoryPanel = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.GridBagLayout());

        previewPanel.setMinimumSize(new java.awt.Dimension(150, 100));
        previewPanel.setOpaque(false);
        previewPanel.setPreferredSize(new java.awt.Dimension(150, 100));
        previewPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_Preview")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        previewPanel.add(previewLabel, gridBagConstraints);
        previewLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormattingPanel.class, "AN_Preview")); // NOI18N
        previewLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingPanel.class, "AD_Preview")); // NOI18N

        previewScrollPane.setDoubleBuffered(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        previewPanel.add(previewScrollPane, gridBagConstraints);
        previewScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormattingPanel.class, "AN_Preview")); // NOI18N
        previewScrollPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingPanel.class, "AD_Preview")); // NOI18N

        jSplitPane1.setRightComponent(previewPanel);

        optionsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optionsPanel.setOpaque(false);

        languageLabel.setLabelFor(languageCombo);
        org.openide.awt.Mnemonics.setLocalizedText(languageLabel, org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_Language")); // NOI18N

        languageCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        languageCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageChanged(evt);
            }
        });

        categoryLabel.setLabelFor(categoryCombo);
        org.openide.awt.Mnemonics.setLocalizedText(categoryLabel, org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_Category")); // NOI18N

        categoryCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        categoryCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryChanged(evt);
            }
        });

        categoryPanel.setOpaque(false);
        categoryPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(languageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(languageCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(categoryLabel)
                        .addGap(10, 10, 10)
                        .addComponent(categoryCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(categoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(languageLabel)
                    .addComponent(languageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(categoryLabel)
                    .addComponent(categoryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(categoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE))
        );

        languageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingPanel.class, "AD_Language")); // NOI18N
        languageCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormattingPanel.class, "FormattingPanel.languageCombo.AccessibleContext.accessibleName")); // NOI18N
        languageCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingPanel.class, "FormattingPanel.languageCombo.AccessibleContext.accessibleDescription")); // NOI18N
        categoryLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingPanel.class, "AD_Category")); // NOI18N
        categoryCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormattingPanel.class, "FormattingPanel.categoryCombo.AccessibleContext.accessibleName")); // NOI18N
        categoryCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingPanel.class, "FormattingPanel.categoryCombo.AccessibleContext.accessibleDescription")); // NOI18N

        jSplitPane1.setLeftComponent(optionsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void languageChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageChanged
        selector.setSelectedMimeType((String)languageCombo.getSelectedItem());
    }//GEN-LAST:event_languageChanged

    private void categoryChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryChanged
        PreferencesCustomizer selectedCustomizer = ((PreferencesCustomizer)categoryCombo.getSelectedItem());
        if (selectedCustomizer != null) {
            selector.setSelectedCustomizer(selectedCustomizer.getId());
        } // else #168066 - no idea how this can happen
    }//GEN-LAST:event_categoryChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox categoryCombo;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JComboBox languageCombo;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JScrollPane previewScrollPane;
    // End of variables declaration//GEN-END:variables
 
    private CustomizerSelector selector;
    private PropertyChangeListener weakListener;

}
