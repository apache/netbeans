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
package org.netbeans.modules.php.analysis.ui.analyzer;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.commands.Psalm;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.options.ValidatorPsalmParameter;
import org.netbeans.modules.php.analysis.ui.AnalysisDefaultDocumentListener;
import org.netbeans.modules.php.analysis.util.AnalysisUiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class PsalmCustomizerPanel extends JPanel {

    public static final String ENABLED = "psalm.enabled"; // NOI18N
    public static final String PATH = "psalm.path"; // NOI18N
    public static final String LEVEL = "psalm.level"; // NOI18N
    public static final String CONFIGURATION = "psalm.configuration"; // NOI18N
    public static final String MEMORY_LIMIT = "psalm.memory.limit"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(PsalmCustomizerPanel.class);
    private static final long serialVersionUID = -3450253368766485405L;

    final Analyzer.CustomizerContext<Void, PsalmCustomizerPanel> context;
    final Preferences settings;

    public PsalmCustomizerPanel(Analyzer.CustomizerContext<Void, PsalmCustomizerPanel> context) {
        assert EventQueue.isDispatchThread();
        assert context != null;

        this.context = context;
        this.settings = context.getSettings();
        initComponents();
        init();
    }

    private void init() {
        initEnabledCheckBox();
        initPsalmField();
        initLevelComboBox();
        initConfigurationTextField();
        initMemoryLimitTextField();
        // avoid NPE: don't set errors during initializing
        RP.schedule(() -> {
            EventQueue.invokeLater(() -> {
                context.setError(null);
                if (psalmEnabledCheckBox.isSelected()) {
                    validateData();
                }
            });
        }, 1000, TimeUnit.MILLISECONDS);
    }

    private void initEnabledCheckBox() {
        assert EventQueue.isDispatchThread();
        psalmEnabledCheckBox.addItemListener(e -> {
            setAllComponetsEnabled(psalmEnabledCheckBox.isSelected());
            setPsalmEnabled();
        });
        boolean isEnabled = settings.getBoolean(ENABLED, false);
        psalmEnabledCheckBox.setSelected(isEnabled);
        setAllComponetsEnabled(isEnabled);
        psalmEnabledCheckBox.addItemListener(e -> {
            if (!psalmEnabledCheckBox.isSelected()) {
                context.setError(null);
            } else {
                validateData();
            }
        });
    }

    private void initPsalmField() {
        assert EventQueue.isDispatchThread();
        psalmTextField.setText(settings.get(PATH, AnalysisOptions.getInstance().getPsalmPath()));
        psalmTextField.getDocument().addDocumentListener(new AnalysisDefaultDocumentListener(() -> setPsalmPath()));
    }

    private void initLevelComboBox() {
        assert EventQueue.isDispatchThread();
        psalmLevelComboBox.removeAllItems();
        // NETBEANS-2974
        // allow empty level option to use a level of a configuration file
        psalmLevelComboBox.addItem(""); // NOI18N
        for (int i = AnalysisOptions.PSALM_MIN_LEVEL; i <= AnalysisOptions.PSALM_MAX_LEVEL; i++) {
            psalmLevelComboBox.addItem(String.valueOf(i));
        }
        psalmLevelComboBox.setSelectedItem(getValidLevel());
        psalmLevelComboBox.addItemListener(e -> setLevel());
    }

    private String getValidLevel() {
        String level = settings.get(LEVEL, AnalysisOptions.getInstance().getPsalmLevel());
        return AnalysisOptions.getValidPsalmLevel(level);
    }

    private void initConfigurationTextField() {
        assert EventQueue.isDispatchThread();
        psalmConfigurationTextField.setText(settings.get(CONFIGURATION, AnalysisOptions.getInstance().getPsalmConfigurationPath()));
        psalmConfigurationTextField.getDocument().addDocumentListener(new AnalysisDefaultDocumentListener(() -> setConfiguration()));
    }

    private void initMemoryLimitTextField() {
        assert EventQueue.isDispatchThread();
        psalmMemoryLimitTextField.setText(settings.get(MEMORY_LIMIT, AnalysisOptions.getInstance().getPsalmMemoryLimit()));
        psalmMemoryLimitTextField.getDocument().addDocumentListener(new AnalysisDefaultDocumentListener(() -> setMemoryLimit()));
    }

    public String getPsalmPath() {
        return psalmTextField.getText().trim();
    }

    public String getLevel() {
        return (String) psalmLevelComboBox.getSelectedItem();
    }

    public String getConfiguration() {
        return psalmConfigurationTextField.getText().trim();
    }

    public String getMemoryLimit() {
        return psalmMemoryLimitTextField.getText().trim();
    }

    private void setPsalmEnabled() {
        settings.putBoolean(ENABLED, psalmEnabledCheckBox.isSelected());
    }

    private void setPsalmPath() {
        if (validateData()) {
            settings.put(PATH, getPsalmPath());
        }
    }

    private void setLevel() {
        settings.put(LEVEL, getLevel());
    }

    private void setConfiguration() {
        if (validateData()) {
            settings.put(CONFIGURATION, getConfiguration());
        }
    }

    private void setMemoryLimit() {
        if (validateData()) {
            settings.put(MEMORY_LIMIT, getMemoryLimit());
        }
    }

    private boolean validateData() {
        ValidatorPsalmParameter param = ValidatorPsalmParameter.create(this);
        ValidationResult result = new AnalysisOptionsValidator()
                .validatePsalm(param)
                .getResult();
        if (result.hasErrors()) {
            context.setError(result.getErrors().get(0).getMessage());
            return false;
        }
        if (result.hasWarnings()) {
            context.setError(result.getWarnings().get(0).getMessage());
            return false;
        }
        context.setError(null);
        return true;
    }

    private void setAllComponetsEnabled(boolean isEnabled) {
        Component[] components = getComponents();
        for (Component component : components) {
            if (component != psalmEnabledCheckBox) {
                component.setEnabled(isEnabled);
            }
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

        psalmEnabledCheckBox = new javax.swing.JCheckBox();
        psalmConfigurationLabel = new javax.swing.JLabel();
        psalmConfigurationTextField = new javax.swing.JTextField();
        psalmConfigurationBrowseButton = new javax.swing.JButton();
        psalmLevelLabel = new javax.swing.JLabel();
        psalmLevelComboBox = new javax.swing.JComboBox<String>();
        psalmMemoryLimitLabel = new javax.swing.JLabel();
        psalmMemoryLimitTextField = new javax.swing.JTextField();
        psalmLabel = new javax.swing.JLabel();
        psalmTextField = new javax.swing.JTextField();
        psalmBrowseButton = new javax.swing.JButton();
        psalmSearchButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(psalmEnabledCheckBox, org.openide.util.NbBundle.getMessage(PsalmCustomizerPanel.class, "PsalmCustomizerPanel.psalmEnabledCheckBox.text")); // NOI18N

        psalmConfigurationLabel.setLabelFor(psalmConfigurationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(psalmConfigurationLabel, org.openide.util.NbBundle.getMessage(PsalmCustomizerPanel.class, "PsalmCustomizerPanel.psalmConfigurationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(psalmConfigurationBrowseButton, org.openide.util.NbBundle.getMessage(PsalmCustomizerPanel.class, "PsalmCustomizerPanel.psalmConfigurationBrowseButton.text")); // NOI18N
        psalmConfigurationBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                psalmConfigurationBrowseButtonActionPerformed(evt);
            }
        });

        psalmLevelLabel.setLabelFor(psalmLevelComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(psalmLevelLabel, org.openide.util.NbBundle.getMessage(PsalmCustomizerPanel.class, "PsalmCustomizerPanel.psalmLevelLabel.text")); // NOI18N

        psalmLevelComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7" }));

        psalmMemoryLimitLabel.setLabelFor(psalmMemoryLimitTextField);
        org.openide.awt.Mnemonics.setLocalizedText(psalmMemoryLimitLabel, org.openide.util.NbBundle.getMessage(PsalmCustomizerPanel.class, "PsalmCustomizerPanel.psalmMemoryLimitLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(psalmLabel, org.openide.util.NbBundle.getMessage(PsalmCustomizerPanel.class, "PsalmCustomizerPanel.psalmLabel.text")); // NOI18N
        psalmLabel.setRequestFocusEnabled(false);

        psalmTextField.setText(org.openide.util.NbBundle.getMessage(PsalmCustomizerPanel.class, "PsalmCustomizerPanel.psalmTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(psalmBrowseButton, org.openide.util.NbBundle.getMessage(PsalmCustomizerPanel.class, "PsalmCustomizerPanel.psalmBrowseButton.text")); // NOI18N
        psalmBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                psalmBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(psalmSearchButton, org.openide.util.NbBundle.getMessage(PsalmCustomizerPanel.class, "PsalmCustomizerPanel.psalmSearchButton.text")); // NOI18N
        psalmSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                psalmSearchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(psalmEnabledCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(psalmLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(psalmConfigurationLabel)
                        .addComponent(psalmLevelLabel)
                        .addComponent(psalmMemoryLimitLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(psalmLevelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(psalmMemoryLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(psalmConfigurationTextField)
                            .addComponent(psalmTextField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(psalmBrowseButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(psalmConfigurationBrowseButton, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(psalmSearchButton))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(psalmEnabledCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psalmLabel)
                    .addComponent(psalmTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(psalmBrowseButton)
                    .addComponent(psalmSearchButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psalmConfigurationLabel)
                    .addComponent(psalmConfigurationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(psalmConfigurationBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psalmLevelLabel)
                    .addComponent(psalmLevelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(psalmMemoryLimitLabel)
                    .addComponent(psalmMemoryLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        psalmLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PsalmCustomizerPanel.class, "PsalmCustomizerPanel.psalmLabel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void psalmConfigurationBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_psalmConfigurationBrowseButtonActionPerformed
        File file = AnalysisUiUtils.browsePsalmConfiguration();
        if (file != null) {
            psalmConfigurationTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_psalmConfigurationBrowseButtonActionPerformed

    private void psalmBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_psalmBrowseButtonActionPerformed
        File file = AnalysisUiUtils.browsePsalm();
        if (file != null) {
            psalmTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_psalmBrowseButtonActionPerformed

    private void psalmSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_psalmSearchButtonActionPerformed
        String psalm = AnalysisUiUtils.searchPsalm();
        if (psalm != null) {
            psalmTextField.setText(psalm);
        }
    }//GEN-LAST:event_psalmSearchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton psalmBrowseButton;
    private javax.swing.JButton psalmConfigurationBrowseButton;
    private javax.swing.JLabel psalmConfigurationLabel;
    private javax.swing.JTextField psalmConfigurationTextField;
    private javax.swing.JCheckBox psalmEnabledCheckBox;
    private javax.swing.JLabel psalmLabel;
    private javax.swing.JComboBox<String> psalmLevelComboBox;
    private javax.swing.JLabel psalmLevelLabel;
    private javax.swing.JLabel psalmMemoryLimitLabel;
    private javax.swing.JTextField psalmMemoryLimitTextField;
    private javax.swing.JButton psalmSearchButton;
    private javax.swing.JTextField psalmTextField;
    // End of variables declaration//GEN-END:variables
}
