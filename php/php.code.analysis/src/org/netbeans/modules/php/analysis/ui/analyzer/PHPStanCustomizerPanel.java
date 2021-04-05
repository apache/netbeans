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
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.commands.PHPStan;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.ui.PHPStanLevelListCellRenderer;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.options.ValidatorPHPStanParameter;
import org.netbeans.modules.php.analysis.ui.options.PHPStanOptionsPanel;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;

public class PHPStanCustomizerPanel extends JPanel {

    public static final String ENABLED = "phpStan.enabled"; // NOI18N
    public static final String LEVEL = "phpStan.level"; // NOI18N
    public static final String CONFIGURATION = "phpStan.configuration"; // NOI18N
    public static final String MEMORY_LIMIT = "phpStan.memory.limit"; // NOI18N
    private static final String PHPSTAN_CONFIGURATION_LAST_FOLDER_SUFFIX = ".phpstan.config"; // NOI18N
    private static final long serialVersionUID = 2318201027384364349L;

    final Analyzer.CustomizerContext<Void, PHPStanCustomizerPanel> context;
    final Preferences settings;

    public PHPStanCustomizerPanel(Analyzer.CustomizerContext<Void, PHPStanCustomizerPanel> context) {
        assert EventQueue.isDispatchThread();
        assert context != null;

        this.context = context;
        this.settings = context.getSettings();
        initComponents();
        init();
    }

    private void init() {
        setEnabledCheckBox();
        setLevelComboBox();
        setConfigurationTextField();
        setMemoryLimitTextField();
    }

    private void setEnabledCheckBox() {
        assert EventQueue.isDispatchThread();
        phpStanEnabledCheckBox.addItemListener(e -> {
            setAllComponetsEnabled(phpStanEnabledCheckBox.isSelected());
            setPHPStanEnabled();
        });
        boolean isEnabled = settings.getBoolean(ENABLED, false);
        phpStanEnabledCheckBox.setSelected(isEnabled);
        setAllComponetsEnabled(isEnabled);
    }

    private void setLevelComboBox() {
        assert EventQueue.isDispatchThread();
        phpStanLevelComboBox.removeAllItems();
        // NETBEANS-2974
        // allow empty level option to use a level of a configuration file
        phpStanLevelComboBox.addItem(""); // NOI18N
        for (int i = AnalysisOptions.PHPSTAN_MIN_LEVEL; i <= AnalysisOptions.PHPSTAN_MAX_LEVEL; i++) {
            phpStanLevelComboBox.addItem(String.valueOf(i));
        }
        phpStanLevelComboBox.addItem(PHPStan.MAX_LEVEL);
        phpStanLevelComboBox.setRenderer(new PHPStanLevelListCellRenderer(phpStanLevelComboBox.getRenderer()));
        phpStanLevelComboBox.setSelectedItem(getValidLevel());
        phpStanLevelComboBox.addItemListener(e -> setLevel());
    }

    private String getValidLevel() {
        String level = settings.get(LEVEL, AnalysisOptions.getInstance().getPHPStanLevel());
        return AnalysisOptions.getValidPHPStanLevel(level);
    }

    private void setConfigurationTextField() {
        assert EventQueue.isDispatchThread();
        phpStanConfigurationTextField.setText(settings.get(CONFIGURATION, AnalysisOptions.getInstance().getPHPStanConfigurationPath()));
        phpStanConfigurationTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }

            private void processUpdate() {
                setConfiguration();
            }
        });
    }

    private void setMemoryLimitTextField() {
        assert EventQueue.isDispatchThread();
        phpStanMemoryLimitTextField.setText(settings.get(MEMORY_LIMIT, AnalysisOptions.getInstance().getPHPStanMemoryLimit()));
        phpStanMemoryLimitTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }

            private void processUpdate() {
                setMemoryLimit();
            }
        });
    }

    public String getLevel() {
        return (String) phpStanLevelComboBox.getSelectedItem();
    }

    public String getConfiguration() {
        return phpStanConfigurationTextField.getText().trim();
    }

    public String getMemoryLimit() {
        return phpStanMemoryLimitTextField.getText().trim();
    }

    private void setPHPStanEnabled() {
        settings.putBoolean(ENABLED, phpStanEnabledCheckBox.isSelected());
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
        ValidatorPHPStanParameter param = ValidatorPHPStanParameter.create(this);
        ValidationResult result = new AnalysisOptionsValidator()
                .validatePHPStan(param)
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
            if (component != phpStanEnabledCheckBox) {
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

        phpStanEnabledCheckBox = new javax.swing.JCheckBox();
        phpStanConfigurationLabel = new javax.swing.JLabel();
        phpStanConfigurationTextField = new javax.swing.JTextField();
        phpStanConfigurationBrowseButton = new javax.swing.JButton();
        phpStanLevelLabel = new javax.swing.JLabel();
        phpStanLevelComboBox = new javax.swing.JComboBox<>();
        phpStanMemoryLimitLabel = new javax.swing.JLabel();
        phpStanMemoryLimitTextField = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(phpStanEnabledCheckBox, org.openide.util.NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanEnabledCheckBox.text")); // NOI18N

        phpStanConfigurationLabel.setLabelFor(phpStanConfigurationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(phpStanConfigurationLabel, org.openide.util.NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanConfigurationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(phpStanConfigurationBrowseButton, org.openide.util.NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanConfigurationBrowseButton.text")); // NOI18N
        phpStanConfigurationBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phpStanConfigurationBrowseButtonActionPerformed(evt);
            }
        });

        phpStanLevelLabel.setLabelFor(phpStanLevelComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(phpStanLevelLabel, org.openide.util.NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanLevelLabel.text")); // NOI18N

        phpStanLevelComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7" }));

        phpStanMemoryLimitLabel.setLabelFor(phpStanMemoryLimitTextField);
        org.openide.awt.Mnemonics.setLocalizedText(phpStanMemoryLimitLabel, org.openide.util.NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanMemoryLimitLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(phpStanEnabledCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(phpStanConfigurationLabel)
                    .addComponent(phpStanLevelLabel)
                    .addComponent(phpStanMemoryLimitLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(phpStanConfigurationTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phpStanConfigurationBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(phpStanLevelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(phpStanMemoryLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(phpStanEnabledCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(phpStanConfigurationLabel)
                    .addComponent(phpStanConfigurationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(phpStanConfigurationBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(phpStanLevelLabel)
                    .addComponent(phpStanLevelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(phpStanMemoryLimitLabel)
                    .addComponent(phpStanMemoryLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("PHPStanCustomizerPanel.configuration.browse.title=Select PHPStan Configuration File")
    private void phpStanConfigurationBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phpStanConfigurationBrowseButtonActionPerformed
        File file = new FileChooserBuilder(PHPStanOptionsPanel.class.getName() + PHPSTAN_CONFIGURATION_LAST_FOLDER_SUFFIX)
                .setFilesOnly(true)
                .setTitle(Bundle.PHPStanCustomizerPanel_configuration_browse_title())
                .showOpenDialog();
        if (file != null) {
            phpStanConfigurationTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_phpStanConfigurationBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton phpStanConfigurationBrowseButton;
    private javax.swing.JLabel phpStanConfigurationLabel;
    private javax.swing.JTextField phpStanConfigurationTextField;
    private javax.swing.JCheckBox phpStanEnabledCheckBox;
    private javax.swing.JComboBox<String> phpStanLevelComboBox;
    private javax.swing.JLabel phpStanLevelLabel;
    private javax.swing.JLabel phpStanMemoryLimitLabel;
    private javax.swing.JTextField phpStanMemoryLimitTextField;
    // End of variables declaration//GEN-END:variables
}
