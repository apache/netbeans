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
import java.util.Arrays;
import java.util.List;
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
import org.netbeans.modules.php.analysis.commands.CodingStandardsFixer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.options.ValidatorCodingStandardsFixerParameter;
import org.netbeans.modules.php.analysis.ui.AnalysisDefaultDocumentListener;
import org.netbeans.modules.php.analysis.util.AnalysisUiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class CodingStandardsFixerCustomizerPanel extends JPanel {

    private static final long serialVersionUID = -2799627803431340323L;

    public static final String ENABLED = "codingStandardsFixer.enabled"; // NOI18N
    public static final String PATH = "codingStandardsFixer.path"; // NOI18N
    public static final String VERSION = "codingStandardsFixer.version"; // NOI18N
    public static final String LEVEL = "codingStandardsFixer.level"; // NOI18N
    public static final String CONFIG = "codingStandardsFixer.config"; // NOI18N
    public static final String OPTIONS = "codingStandardsFixer.options"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(PHPStanCustomizerPanel.class);

    final Analyzer.CustomizerContext<Void, CodingStandardsFixerCustomizerPanel> context;
    final Preferences settings;


    public CodingStandardsFixerCustomizerPanel(Analyzer.CustomizerContext<Void, CodingStandardsFixerCustomizerPanel> context) {
        assert EventQueue.isDispatchThread();
        assert context != null;

        this.context = context;
        this.settings = context.getSettings();

        initComponents();
        init();
    }

    private void init() {
        initEnabledCheckBox();
        initCodingStandardsFixerField();
        initVersionComboBox();
        initLevelComboBox();
        initConfigComboBox();
        initOptionsTextField();
        // avoid NPE: don't set errors during initializing
        RP.schedule(() -> {
            EventQueue.invokeLater(() -> {
                context.setError(null);
                if (enabledCheckBox.isSelected()) {
                    validateData();
                }
            });
        }, 1000, TimeUnit.MILLISECONDS);
    }

    private void initEnabledCheckBox() {
        assert EventQueue.isDispatchThread();
        enabledCheckBox.addItemListener((e) -> {
            setAllComponetsEnabled(enabledCheckBox.isSelected());
            setCodingStandardsFixerEnabled();
        });
        boolean isEnabled = settings.getBoolean(ENABLED, false);
        enabledCheckBox.setSelected(isEnabled);
        setAllComponetsEnabled(isEnabled);
        enabledCheckBox.addItemListener(e -> {
            if (!enabledCheckBox.isSelected()) {
                context.setError(null);
            } else {
                validateData();
            }
        });
    }

    private void initCodingStandardsFixerField() {
        assert EventQueue.isDispatchThread();
        codingStandardsFixerTextField.setText(settings.get(PATH, AnalysisOptions.getInstance().getCodingStandardsFixerPath()));
        codingStandardsFixerTextField.getDocument().addDocumentListener(new AnalysisDefaultDocumentListener(() -> setCodingStandardsFixerPath()));
    }

    private void initVersionComboBox() {
        assert EventQueue.isDispatchThread();
        DefaultComboBoxModel<String> versionComboBoxModel = new DefaultComboBoxModel<>();
        CodingStandardsFixer.VERSIONS.forEach((version) -> {
            versionComboBoxModel.addElement(version);
        });
        versionComboBox.setModel(versionComboBoxModel);
        versionComboBox.addItemListener((e) -> {
            setVersion();
        });
        versionComboBoxModel.setSelectedItem(settings.get(VERSION, AnalysisOptions.getInstance().getCodingStandardsFixerVersion()));
    }

    private void initLevelComboBox() {
        assert EventQueue.isDispatchThread();
        DefaultComboBoxModel<String> levelComboBoxModel = new DefaultComboBoxModel<>();
        CodingStandardsFixer.ALL_LEVEL.forEach((level) -> {
            levelComboBoxModel.addElement(level);
        });
        levelComboBoxModel.setSelectedItem(settings.get(LEVEL, AnalysisOptions.getInstance().getCodingStandardsFixerLevel()));
        levelComboBox.setModel(levelComboBoxModel);
        levelComboBox.addItemListener((e) -> {
            setLevel();
        });
    }

    private void initConfigComboBox() {
        assert EventQueue.isDispatchThread();
        DefaultComboBoxModel<String> configComboBoxModel = new DefaultComboBoxModel<>();
        CodingStandardsFixer.ALL_CONFIG.forEach((config) -> {
            configComboBoxModel.addElement(config);
        });
        configComboBoxModel.setSelectedItem(settings.get(CONFIG, AnalysisOptions.getInstance().getCodingStandardsFixerConfig()));
        configComboBox.setModel(configComboBoxModel);
        configComboBox.addItemListener((e) -> {
            setConfig();
        });
    }

    private void initOptionsTextField() {
        assert EventQueue.isDispatchThread();
        optionsTextField.setText(settings.get(OPTIONS, AnalysisOptions.getInstance().getCodingStandardsFixerOptions()));
        optionsTextField.getDocument().addDocumentListener(new AnalysisDefaultDocumentListener(() -> setOptions()));
    }

    public String getCodingStandardsFixerPath() {
        return codingStandardsFixerTextField.getText().trim();
    }

    private void setCodingStandardsFixerEnabled() {
        settings.putBoolean(ENABLED, enabledCheckBox.isSelected());
    }

    private void setCodingStandardsFixerPath() {
        if (validateData()) {
            settings.put(PATH, getCodingStandardsFixerPath());
        }
    }

    private void setVersion() {
        settings.put(VERSION, (String) versionComboBox.getSelectedItem());
    }

    private void setLevel() {
        settings.put(LEVEL, (String) levelComboBox.getSelectedItem());
    }

    private void setConfig() {
        settings.put(CONFIG, (String) configComboBox.getSelectedItem());
    }

    private void setOptions() {
        settings.put(OPTIONS, optionsTextField.getText());
    }

    private void setVersion1ComponentsVisible(boolean isVisible) {
        for (Component component : getVersion1Components()) {
            component.setVisible(isVisible);
        }
    }

    private List<Component> getVersion1Components() {
        return Arrays.asList(
                levelLabel,
                levelComboBox,
                configLabel,
                configComboBox
        );
    }

    private boolean validateData() {
        ValidatorCodingStandardsFixerParameter param = ValidatorCodingStandardsFixerParameter.create(this);
        ValidationResult result = new AnalysisOptionsValidator()
                .validateCodingStandardsFixer(param)
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
            if (component != enabledCheckBox) {
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

        levelLabel = new JLabel();
        levelComboBox = new JComboBox<>();
        configLabel = new JLabel();
        configComboBox = new JComboBox<>();
        optionsLabel = new JLabel();
        optionsTextField = new JTextField();
        versionLabel = new JLabel();
        versionComboBox = new JComboBox<>();
        enabledCheckBox = new JCheckBox();
        codingStandarsFixerLabel = new JLabel();
        codingStandardsFixerTextField = new JTextField();
        browseButton = new JButton();
        searchButton = new JButton();

        Mnemonics.setLocalizedText(levelLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.levelLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(configLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.configLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.optionsLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.versionLabel.text")); // NOI18N

        versionComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                versionComboBoxActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(enabledCheckBox, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.enabledCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(codingStandarsFixerLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.codingStandarsFixerLabel.text")); // NOI18N

        codingStandardsFixerTextField.setText(NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.codingStandardsFixerTextField.text")); // NOI18N

        Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(codingStandarsFixerLabel)
                    .addComponent(levelLabel)
                    .addComponent(configLabel)
                    .addComponent(optionsLabel)
                    .addComponent(versionLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(levelComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(configComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addComponent(versionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(codingStandardsFixerTextField)
                            .addComponent(optionsTextField))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton))))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandarsFixerLabel)
                    .addComponent(codingStandardsFixerTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton)
                    .addComponent(searchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(versionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(versionLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(levelLabel)
                    .addComponent(levelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(configComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(optionsLabel)
                    .addComponent(optionsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void versionComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_versionComboBoxActionPerformed
        switch ((String) versionComboBox.getSelectedItem()) {
            case "1": // NOI18N
                setVersion1ComponentsVisible(true);
                break;
            case "2": // NOI18N
                setVersion1ComponentsVisible(false);
                break;
            default:
                throw new AssertionError();
        }
    }//GEN-LAST:event_versionComboBoxActionPerformed

    private void browseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File file = AnalysisUiUtils.browseCodingStandardsFixer();
        if (file != null) {
            codingStandardsFixerTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        String codingStandardsFixer = AnalysisUiUtils.searchCodingStandardsFixer();
        if (codingStandardsFixer != null) {
            codingStandardsFixerTextField.setText(codingStandardsFixer);
        }
    }//GEN-LAST:event_searchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton browseButton;
    private JTextField codingStandardsFixerTextField;
    private JLabel codingStandarsFixerLabel;
    private JComboBox<String> configComboBox;
    private JLabel configLabel;
    private JCheckBox enabledCheckBox;
    private JComboBox<String> levelComboBox;
    private JLabel levelLabel;
    private JLabel optionsLabel;
    private JTextField optionsTextField;
    private JButton searchButton;
    private JComboBox<String> versionComboBox;
    private JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}
