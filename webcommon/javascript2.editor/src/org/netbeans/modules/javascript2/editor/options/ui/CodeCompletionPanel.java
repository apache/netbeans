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

package org.netbeans.modules.javascript2.editor.options.ui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javascript2.editor.options.OptionsUtils;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 * XXX copied from PHP
 */
public class CodeCompletionPanel extends JPanel {

    private final Preferences preferences;
    private final ItemListener defaultCheckBoxListener = new DefaultCheckBoxListener();
    private final ItemListener defaultRadioButtonListener = new DefaultRadioButtonListener();
    private final ChangeListener defaultChangeListener = new DefaultChangeListener();
    private final Map<String, Object> id2Saved = new HashMap<>();

    public CodeCompletionPanel(Preferences preferences) {
        assert preferences != null;

        this.preferences = preferences;

        initComponents();

        initAutoCompletion();
    }

    public static PreferencesCustomizer.Factory getCustomizerFactory() {
        return CodeCompletionPreferencesCustomizer::new;
    }

    private void initAutoCompletion() {
        boolean codeCompletionTypeResolution = preferences.getBoolean(
                OptionsUtils.AUTO_COMPLETION_TYPE_RESOLUTION,
                OptionsUtils.AUTO_COMPLETION_TYPE_RESOLUTION_DEFAULT);
        autoCompletionTypeResolutionCheckBox.setSelected(codeCompletionTypeResolution);
        autoCompletionTypeResolutionCheckBox.addItemListener(defaultCheckBoxListener);

        boolean codeCompletionSmartQuotes = preferences.getBoolean(
                OptionsUtils.AUTO_COMPLETION_SMART_QUOTES,
                OptionsUtils.AUTO_COMPLETION_SMART_QUOTES_DEFAULT);
        autoCompletionSmartQuotesCheckBox.setSelected(codeCompletionSmartQuotes);
        autoCompletionSmartQuotesCheckBox.addItemListener(defaultCheckBoxListener);

        boolean codeCompletionStringAutoConcatination = preferences.getBoolean(
                OptionsUtils.AUTO_STRING_CONCATINATION,
                OptionsUtils.AUTO_STRING_CONCATINATION_DEFAULT);
        autoStringConcatenationCheckBox.setSelected(codeCompletionStringAutoConcatination);
        autoStringConcatenationCheckBox.addItemListener(defaultCheckBoxListener);

        autoCompletionFullRadioButton.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setAutoCompletionState(false);
            }
        });
        autoCompletionCustomizeRadioButton.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setAutoCompletionState(true);
            }
        });

        boolean autoCompletionFull = preferences.getBoolean(
                OptionsUtils.AUTO_COMPLETION_FULL,
                OptionsUtils.AUTO_COMPLETION_FULL_DEFAULT);

        autoCompletionFullRadioButton.setSelected(autoCompletionFull);
        autoCompletionCustomizeRadioButton.setSelected(!autoCompletionFull);

        autoCompletionFullRadioButton.addItemListener(defaultRadioButtonListener);
        autoCompletionCustomizeRadioButton.addItemListener(defaultRadioButtonListener);

        boolean autoCompletionVariables = preferences.getBoolean(
                OptionsUtils.AUTO_COMPLETION_AFTER_DOT,
                OptionsUtils.AUTO_COMPLETION_AFTER_DOT_DEFAULT);
        autoCompletionAfterDotCheckBox.setSelected(autoCompletionVariables);
        autoCompletionAfterDotCheckBox.addItemListener(defaultCheckBoxListener);

        int codeCompletionItemSignatureWidth = preferences.getInt(
                OptionsUtils.COMPETION_ITEM_SIGNATURE_WIDTH,
                OptionsUtils.COMPETION_ITEM_SIGNATURE_WIDTH_DEFAULT);
        codeCompletionSignatureWidthSpinner.setValue(codeCompletionItemSignatureWidth);
        codeCompletionSignatureWidthSpinner.addChangeListener(defaultChangeListener);

        id2Saved.put(OptionsUtils.AUTO_COMPLETION_TYPE_RESOLUTION, autoCompletionTypeResolutionCheckBox.isSelected());
        id2Saved.put(OptionsUtils.AUTO_COMPLETION_SMART_QUOTES, autoCompletionSmartQuotesCheckBox.isSelected());
        id2Saved.put(OptionsUtils.AUTO_STRING_CONCATINATION, autoStringConcatenationCheckBox.isSelected());
        id2Saved.put(OptionsUtils.AUTO_COMPLETION_FULL, autoCompletionFullRadioButton.isSelected());
        id2Saved.put(OptionsUtils.AUTO_COMPLETION_AFTER_DOT, autoCompletionAfterDotCheckBox.isSelected());
        id2Saved.put(OptionsUtils.COMPETION_ITEM_SIGNATURE_WIDTH, codeCompletionSignatureWidthSpinner.getValue());
    }

    void setAutoCompletionState(boolean enabled) {
        autoCompletionAfterDotCheckBox.setEnabled(enabled);
    }

    void validateData() {
        preferences.putBoolean(OptionsUtils.AUTO_COMPLETION_TYPE_RESOLUTION, autoCompletionTypeResolutionCheckBox.isSelected());
        preferences.putBoolean(OptionsUtils.AUTO_COMPLETION_SMART_QUOTES, autoCompletionSmartQuotesCheckBox.isSelected());
        preferences.putBoolean(OptionsUtils.AUTO_STRING_CONCATINATION, autoStringConcatenationCheckBox.isSelected());
        preferences.putBoolean(OptionsUtils.AUTO_COMPLETION_FULL, autoCompletionFullRadioButton.isSelected());
        preferences.putBoolean(OptionsUtils.AUTO_COMPLETION_AFTER_DOT, autoCompletionAfterDotCheckBox.isSelected());
        preferences.putInt(OptionsUtils.COMPETION_ITEM_SIGNATURE_WIDTH, (Integer)codeCompletionSignatureWidthSpinner.getValue());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        autoStringConcatenationCheckBox = new JCheckBox();
        autoCompletionButtonGroup = new ButtonGroup();
        autoCompletionSmartQuotesLabel = new JLabel();
        autoCompletionSmartQuotesCheckBox = new JCheckBox();
        autoCompletionTypeResolutionLabel = new JLabel();
        autoCompletionTypeResolutionCheckBox = new JCheckBox();
        enableAutocompletionLabel = new JLabel();
        autoCompletionFullRadioButton = new JRadioButton();
        autoCompletionCustomizeRadioButton = new JRadioButton();
        autoCompletionAfterDotCheckBox = new JCheckBox();
        codeCompletionSignatureWidthLabel = new JLabel();
        codeCompletionSignatureWidthSpinner = new JSpinner();

        Mnemonics.setLocalizedText(autoStringConcatenationCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoStringConcatenationCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionSmartQuotesLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionSmartQuotesLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionSmartQuotesCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionSmartQuotesCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionTypeResolutionLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionTypeResolutionLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionTypeResolutionCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionTypeResolutionCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(enableAutocompletionLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.enableAutocompletionLabel.text")); // NOI18N

        autoCompletionButtonGroup.add(autoCompletionFullRadioButton);
        Mnemonics.setLocalizedText(autoCompletionFullRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionFullRadioButton.text")); // NOI18N

        autoCompletionButtonGroup.add(autoCompletionCustomizeRadioButton);
        Mnemonics.setLocalizedText(autoCompletionCustomizeRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionCustomizeRadioButton.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionAfterDotCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionAfterDotCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(codeCompletionSignatureWidthLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionSignatureWidthLabel.text")); // NOI18N

        codeCompletionSignatureWidthSpinner.setMinimumSize(new Dimension(100, 28));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(autoCompletionSmartQuotesCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(autoCompletionSmartQuotesLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(autoCompletionTypeResolutionCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(enableAutocompletionLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(autoCompletionCustomizeRadioButton, Alignment.LEADING)
                            .addComponent(autoCompletionFullRadioButton, Alignment.LEADING)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(autoCompletionTypeResolutionLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(autoCompletionAfterDotCheckBox, GroupLayout.PREFERRED_SIZE, 144, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(codeCompletionSignatureWidthLabel)
                        .addGap(4, 4, 4)
                        .addComponent(codeCompletionSignatureWidthSpinner, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enableAutocompletionLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoCompletionFullRadioButton)
                .addGap(1, 1, 1)
                .addComponent(autoCompletionCustomizeRadioButton)
                .addGap(1, 1, 1)
                .addComponent(autoCompletionAfterDotCheckBox)
                .addGap(18, 18, 18)
                .addComponent(autoCompletionTypeResolutionLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoCompletionTypeResolutionCheckBox)
                .addGap(18, 18, 18)
                .addComponent(autoCompletionSmartQuotesLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoCompletionSmartQuotesCheckBox)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(codeCompletionSignatureWidthLabel)
                    .addComponent(codeCompletionSignatureWidthSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(88, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox autoCompletionAfterDotCheckBox;
    private ButtonGroup autoCompletionButtonGroup;
    private JRadioButton autoCompletionCustomizeRadioButton;
    private JRadioButton autoCompletionFullRadioButton;
    private JCheckBox autoCompletionSmartQuotesCheckBox;
    private JLabel autoCompletionSmartQuotesLabel;
    private JCheckBox autoCompletionTypeResolutionCheckBox;
    private JLabel autoCompletionTypeResolutionLabel;
    private JCheckBox autoStringConcatenationCheckBox;
    private JLabel codeCompletionSignatureWidthLabel;
    private JSpinner codeCompletionSignatureWidthSpinner;
    private JLabel enableAutocompletionLabel;
    // End of variables declaration//GEN-END:variables

    private final class DefaultCheckBoxListener implements ItemListener, Serializable {
        @Override
        public void itemStateChanged(ItemEvent e) {
            validateData();
        }
    }

    private final class DefaultRadioButtonListener implements ItemListener, Serializable {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                validateData();
            }
        }
    }

    private final class DefaultChangeListener implements ChangeListener, Serializable {
        @Override
        public void stateChanged(ChangeEvent e) {
            validateData();
        }
    }

    static final class CodeCompletionPreferencesCustomizer implements PreferencesCustomizer {

        private final Preferences preferences;
        private CodeCompletionPanel component;

        private CodeCompletionPreferencesCustomizer(Preferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public String getId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.javascript2.editor.options.CodeCompletionPanel");
        }

        @Override
        public JComponent getComponent() {
            if (component == null) {
                component = new CodeCompletionPanel(preferences);
            }
            return component;
        }
    }

    String getSavedValue(String key) {
        return id2Saved.get(key).toString();
    }

    public static final class CustomCustomizerImpl extends PreferencesCustomizer.CustomCustomizer {

        @Override
        public String getSavedValue(PreferencesCustomizer customCustomizer, String key) {
            if (customCustomizer instanceof CodeCompletionPreferencesCustomizer) {
                return ((CodeCompletionPanel) customCustomizer.getComponent()).getSavedValue(key);
            }
            return null;
        }
    }
}
