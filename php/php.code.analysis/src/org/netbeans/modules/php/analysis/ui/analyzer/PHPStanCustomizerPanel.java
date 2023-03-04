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
import org.netbeans.modules.php.analysis.commands.PHPStan;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.ui.PHPStanLevelListCellRenderer;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.options.ValidatorPHPStanParameter;
import org.netbeans.modules.php.analysis.ui.AnalysisDefaultDocumentListener;
import org.netbeans.modules.php.analysis.util.AnalysisUiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class PHPStanCustomizerPanel extends JPanel {

    public static final String ENABLED = "phpStan.enabled"; // NOI18N
    public static final String PATH = "phpStan.path"; // NOI18N
    public static final String LEVEL = "phpStan.level"; // NOI18N
    public static final String CONFIGURATION = "phpStan.configuration"; // NOI18N
    public static final String MEMORY_LIMIT = "phpStan.memory.limit"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(PHPStanCustomizerPanel.class);
    private static final long serialVersionUID = -3450253368766485405L;

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
        initEnabledCheckBox();
        initPHPStanField();
        initLevelComboBox();
        initConfigurationTextField();
        initMemoryLimitTextField();
        // avoid NPE: don't set errors during initializing
        RP.schedule(() -> {
            EventQueue.invokeLater(() -> {
                context.setError(null);
                if (phpStanEnabledCheckBox.isSelected()) {
                    validateData();
                }
            });
        }, 1000, TimeUnit.MILLISECONDS);
    }

    private void initEnabledCheckBox() {
        assert EventQueue.isDispatchThread();
        phpStanEnabledCheckBox.addItemListener(e -> {
            setAllComponetsEnabled(phpStanEnabledCheckBox.isSelected());
            setPHPStanEnabled();
        });
        boolean isEnabled = settings.getBoolean(ENABLED, false);
        phpStanEnabledCheckBox.setSelected(isEnabled);
        setAllComponetsEnabled(isEnabled);
        phpStanEnabledCheckBox.addItemListener(e -> {
            if (!phpStanEnabledCheckBox.isSelected()) {
                context.setError(null);
            } else {
                validateData();
            }
        });
    }

    private void initPHPStanField() {
        assert EventQueue.isDispatchThread();
        phpStanTextField.setText(settings.get(PATH, AnalysisOptions.getInstance().getPHPStanPath()));
        phpStanTextField.getDocument().addDocumentListener(new AnalysisDefaultDocumentListener(() -> setPHPStanPath()));
    }

    private void initLevelComboBox() {
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

    private void initConfigurationTextField() {
        assert EventQueue.isDispatchThread();
        phpStanConfigurationTextField.setText(settings.get(CONFIGURATION, AnalysisOptions.getInstance().getPHPStanConfigurationPath()));
        phpStanConfigurationTextField.getDocument().addDocumentListener(new AnalysisDefaultDocumentListener(() -> setConfiguration()));
    }

    private void initMemoryLimitTextField() {
        assert EventQueue.isDispatchThread();
        phpStanMemoryLimitTextField.setText(settings.get(MEMORY_LIMIT, AnalysisOptions.getInstance().getPHPStanMemoryLimit()));
        phpStanMemoryLimitTextField.getDocument().addDocumentListener(new AnalysisDefaultDocumentListener(() -> setMemoryLimit()));
    }

    public String getPHPStanPath() {
        return phpStanTextField.getText().trim();
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

    private void setPHPStanPath() {
        if (validateData()) {
            settings.put(PATH, getPHPStanPath());
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

        phpStanEnabledCheckBox = new JCheckBox();
        phpStanConfigurationLabel = new JLabel();
        phpStanConfigurationTextField = new JTextField();
        phpStanConfigurationBrowseButton = new JButton();
        phpStanLevelLabel = new JLabel();
        phpStanLevelComboBox = new JComboBox<>();
        phpStanMemoryLimitLabel = new JLabel();
        phpStanMemoryLimitTextField = new JTextField();
        phpStanLabel = new JLabel();
        phpStanTextField = new JTextField();
        phpStanBrowseButton = new JButton();
        phpStanSearchButton = new JButton();

        Mnemonics.setLocalizedText(phpStanEnabledCheckBox, NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanEnabledCheckBox.text")); // NOI18N

        phpStanConfigurationLabel.setLabelFor(phpStanConfigurationTextField);
        Mnemonics.setLocalizedText(phpStanConfigurationLabel, NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanConfigurationLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpStanConfigurationBrowseButton, NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanConfigurationBrowseButton.text")); // NOI18N
        phpStanConfigurationBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpStanConfigurationBrowseButtonActionPerformed(evt);
            }
        });

        phpStanLevelLabel.setLabelFor(phpStanLevelComboBox);
        Mnemonics.setLocalizedText(phpStanLevelLabel, NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanLevelLabel.text")); // NOI18N

        phpStanLevelComboBox.setModel(new DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7" }));

        phpStanMemoryLimitLabel.setLabelFor(phpStanMemoryLimitTextField);
        Mnemonics.setLocalizedText(phpStanMemoryLimitLabel, NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanMemoryLimitLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpStanLabel, NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanLabel.text")); // NOI18N

        phpStanTextField.setText(NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanTextField.text")); // NOI18N

        Mnemonics.setLocalizedText(phpStanBrowseButton, NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanBrowseButton.text")); // NOI18N
        phpStanBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpStanBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(phpStanSearchButton, NbBundle.getMessage(PHPStanCustomizerPanel.class, "PHPStanCustomizerPanel.phpStanSearchButton.text")); // NOI18N
        phpStanSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpStanSearchButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(phpStanEnabledCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(phpStanLabel, GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(phpStanConfigurationLabel)
                        .addComponent(phpStanLevelLabel)
                        .addComponent(phpStanMemoryLimitLabel)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(phpStanLevelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(phpStanMemoryLimitTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(phpStanConfigurationTextField)
                            .addComponent(phpStanTextField))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(phpStanBrowseButton, GroupLayout.Alignment.TRAILING)
                            .addComponent(phpStanConfigurationBrowseButton, GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phpStanSearchButton))))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(phpStanEnabledCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(phpStanLabel)
                    .addComponent(phpStanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(phpStanBrowseButton)
                    .addComponent(phpStanSearchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(phpStanConfigurationLabel)
                    .addComponent(phpStanConfigurationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(phpStanConfigurationBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(phpStanLevelLabel)
                    .addComponent(phpStanLevelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(phpStanMemoryLimitLabel)
                    .addComponent(phpStanMemoryLimitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void phpStanConfigurationBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpStanConfigurationBrowseButtonActionPerformed
        File file = AnalysisUiUtils.browsePHPStanConfiguration();
        if (file != null) {
            phpStanConfigurationTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_phpStanConfigurationBrowseButtonActionPerformed

    private void phpStanBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpStanBrowseButtonActionPerformed
        File file = AnalysisUiUtils.browsePHPStan();
        if (file != null) {
            phpStanTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_phpStanBrowseButtonActionPerformed

    private void phpStanSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpStanSearchButtonActionPerformed
        String phpStan = AnalysisUiUtils.searchPHPStan();
        if (phpStan != null) {
            phpStanTextField.setText(phpStan);
        }
    }//GEN-LAST:event_phpStanSearchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton phpStanBrowseButton;
    private JButton phpStanConfigurationBrowseButton;
    private JLabel phpStanConfigurationLabel;
    private JTextField phpStanConfigurationTextField;
    private JCheckBox phpStanEnabledCheckBox;
    private JLabel phpStanLabel;
    private JComboBox<String> phpStanLevelComboBox;
    private JLabel phpStanLevelLabel;
    private JLabel phpStanMemoryLimitLabel;
    private JTextField phpStanMemoryLimitTextField;
    private JButton phpStanSearchButton;
    private JTextField phpStanTextField;
    // End of variables declaration//GEN-END:variables
}
