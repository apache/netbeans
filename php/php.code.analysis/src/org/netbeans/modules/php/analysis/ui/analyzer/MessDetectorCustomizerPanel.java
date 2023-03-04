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
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.options.ValidatorMessDetectorParameter;
import org.netbeans.modules.php.analysis.ui.AnalysisDefaultDocumentListener;
import org.netbeans.modules.php.analysis.ui.MessDetectorRuleSetsListCellRenderer;
import org.netbeans.modules.php.analysis.ui.MessDetectorRuleSetsListModel;
import org.netbeans.modules.php.analysis.util.AnalysisUiUtils;
import org.netbeans.modules.php.analysis.util.AnalysisUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class MessDetectorCustomizerPanel extends JPanel {

    private static final long serialVersionUID = -1536063097951347093L;

    public static final String ENABLED = "messDetector.enabled"; // NOI18N
    public static final String PATH = "messDetector.path"; // NOI18N
    public static final String RULE_SETS = "messDetector.ruleSets"; // NOI18N
    public static final String RULE_SET_FILE = "messDetector.ruleSetFile"; // NOI18N
    public static final String OPTIONS = "messDetector.options"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(MessDetectorCustomizerPanel.class);

    private final MessDetectorRuleSetsListModel ruleSetsListModel = new MessDetectorRuleSetsListModel();
    final Analyzer.CustomizerContext<Void, MessDetectorCustomizerPanel> context;
    final Preferences settings;


    public MessDetectorCustomizerPanel(Analyzer.CustomizerContext<Void, MessDetectorCustomizerPanel> context) {
        assert context != null;

        this.context = context;
        this.settings = context.getSettings();

        initComponents();
        init();
    }

    @CheckForNull
    public static List<String> getRuleSets(Preferences settings) {
        if (settings == null) {
            return null;
        }
        String ruleSets = settings.get(MessDetectorCustomizerPanel.RULE_SETS, null);
        if (ruleSets == null) {
            return null;
        }
        return AnalysisUtils.deserialize(ruleSets);
    }

    private void init() {
        DocumentListener defaultDocumentListener = new AnalysisDefaultDocumentListener(() -> validateAndSetData());
        initEnabledCheckBox();
        initMessDetectorTextField(defaultDocumentListener);
        initRuleSetsList();
        initRuleSetFileTextField(defaultDocumentListener);
        initOptionsTextField(defaultDocumentListener);
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
        enabledCheckBox.addItemListener((e) -> {
            setMessDetectorEnabled();
            setAllComponentsEnabled(enabledCheckBox.isSelected());
        });
        boolean isEnabled = settings.getBoolean(ENABLED, false);
        enabledCheckBox.setSelected(isEnabled);
        setAllComponentsEnabled(isEnabled);
        enabledCheckBox.addItemListener(e -> {
            if (!enabledCheckBox.isSelected()) {
                context.setError(null);
            } else {
                validateData();
            }
        });
    }

    private void initMessDetectorTextField(DocumentListener documentListener) {
        messDetectorTextField.setText(settings.get(PATH, AnalysisOptions.getInstance().getMessDetectorPath()));
        messDetectorTextField.getDocument().addDocumentListener(documentListener);
    }

    private void initRuleSetsList() {
        ruleSetsList.setModel(ruleSetsListModel);
        ruleSetsList.setCellRenderer(new MessDetectorRuleSetsListCellRenderer(ruleSetsList.getCellRenderer()));
        List<String> ruleSets = getRuleSets(settings);
        if (ruleSets == null) {
            ruleSets = AnalysisOptions.getInstance().getMessDetectorRuleSets();
        }
        selectRuleSets(ruleSets);
        ruleSetsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                validateAndSetData();
            }
        });
    }

    private void initRuleSetFileTextField(DocumentListener documentListener) {
        String ruleSetFile = settings.get(RULE_SET_FILE, AnalysisOptions.getInstance().getMessDetectorRuleSetFilePath());
        ruleSetFileTextField.setText(ruleSetFile);
        ruleSetFileTextField.getDocument().addDocumentListener(documentListener);
    }

    private void initOptionsTextField(DocumentListener documentListener) {
        String options = settings.get(OPTIONS, AnalysisOptions.getInstance().getMessDetectorOptions());
        optionsTextField.setText(options);
        optionsTextField.getDocument().addDocumentListener(documentListener);
    }

    public String getMessDetectorPath() {
        return messDetectorTextField.getText().trim();
    }

    /**
     * Get the valid Mess Detector path.
     *
     * @return the path for Options if the path for this panel is empty.
     * Otherwise, the path for this panel. Can be {@code null}.
     */
    @CheckForNull
    public String getValidMessDetectorPath() {
        if (StringUtils.hasText(getMessDetectorPath())) {
            return getMessDetectorPath();
        }
        return AnalysisOptions.getInstance().getMessDetectorPath();
    }

    public List<String> getSelectedRuleSets() {
        return ruleSetsList.getSelectedValuesList();
    }

    public String getRuleSetFile() {
        return ruleSetFileTextField.getText().trim();
    }

    public String getOptions() {
        return optionsTextField.getText().trim();
    }

    void selectRuleSets(List<String> ruleSets) {
        ruleSetsList.clearSelection();
        for (String ruleSet : ruleSets) {
            int indexOf = MessDetectorRuleSetsListModel.getAllRuleSets().indexOf(ruleSet);
            assert indexOf != -1 : "Rule set not found: " + ruleSet; // NOI18N
            ruleSetsList.addSelectionInterval(indexOf, indexOf);
        }
    }

    void validateAndSetData() {
        if (validateData()) {
            setData();
        }
    }

    private boolean validateData() {
        ValidationResult result = new AnalysisOptionsValidator()
                .validateMessDetector(ValidatorMessDetectorParameter.create(this))
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

    private void setData() {
        settings.put(PATH, getMessDetectorPath());
        settings.put(RULE_SETS, AnalysisUtils.serialize(getSelectedRuleSets()));
        settings.put(RULE_SET_FILE, getRuleSetFile());
        settings.put(OPTIONS, getOptions());
    }

    private void setMessDetectorEnabled() {
        settings.putBoolean(ENABLED, enabledCheckBox.isSelected());
    }

    private void setAllComponentsEnabled(boolean isEnabled) {
        for (Component component : getAllComponentsForEnabling()) {
            component.setEnabled(isEnabled);
        }
    }

    private List<Component> getAllComponentsForEnabling() {
        return Arrays.asList(
                messDetectorLabel,
                messDetectorTextField,
                browseButton,
                searchButton,
                ruleSetsLabel,
                ruleSetsList,
                ruleSetsScrollPane,
                ruleSetFileLabel,
                ruleSetFileTextField,
                ruleSetFileBrowseButton,
                optionsLabel,
                optionsTextField
        );
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ruleSetsLabel = new JLabel();
        ruleSetsScrollPane = new JScrollPane();
        ruleSetsList = new JList<>();
        enabledCheckBox = new JCheckBox();
        ruleSetFileTextField = new JTextField();
        ruleSetFileLabel = new JLabel();
        ruleSetFileBrowseButton = new JButton();
        optionsTextField = new JTextField();
        optionsLabel = new JLabel();
        messDetectorLabel = new JLabel();
        messDetectorTextField = new JTextField();
        browseButton = new JButton();
        searchButton = new JButton();

        ruleSetsLabel.setLabelFor(ruleSetsList);
        Mnemonics.setLocalizedText(ruleSetsLabel, NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.ruleSetsLabel.text")); // NOI18N

        ruleSetsScrollPane.setViewportView(ruleSetsList);

        Mnemonics.setLocalizedText(enabledCheckBox, NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.enabledCheckBox.text")); // NOI18N

        ruleSetFileLabel.setLabelFor(ruleSetFileTextField);
        Mnemonics.setLocalizedText(ruleSetFileLabel, NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.ruleSetFileLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(ruleSetFileBrowseButton, NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.ruleSetFileBrowseButton.text")); // NOI18N
        ruleSetFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ruleSetFileBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.optionsLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(messDetectorLabel, NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.messDetectorLabel.text")); // NOI18N

        messDetectorTextField.setText(NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.messDetectorTextField.text")); // NOI18N

        Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.searchButton.text")); // NOI18N
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
                    .addComponent(messDetectorLabel)
                    .addComponent(ruleSetsLabel)
                    .addComponent(ruleSetFileLabel)
                    .addComponent(optionsLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(ruleSetFileTextField)
                            .addComponent(optionsTextField))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ruleSetFileBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(ruleSetsScrollPane)
                            .addComponent(messDetectorTextField))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchButton))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(messDetectorLabel)
                    .addComponent(messDetectorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton)
                    .addComponent(searchButton))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(ruleSetsLabel)
                    .addComponent(ruleSetsScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(ruleSetFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(ruleSetFileLabel)
                    .addComponent(ruleSetFileBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(optionsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(optionsLabel)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ruleSetFileBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_ruleSetFileBrowseButtonActionPerformed
         File file = AnalysisUiUtils.browseMessDetectorRuleSet();
        if (file != null) {
            ruleSetFileTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_ruleSetFileBrowseButtonActionPerformed

    private void browseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File file = AnalysisUiUtils.browseMessDetector();
        if (file != null) {
            messDetectorTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        String messDetector = AnalysisUiUtils.searchMessDetector();
        if (messDetector != null) {
            messDetectorTextField.setText(messDetector);
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton browseButton;
    private JCheckBox enabledCheckBox;
    private JLabel messDetectorLabel;
    private JTextField messDetectorTextField;
    private JLabel optionsLabel;
    private JTextField optionsTextField;
    private JButton ruleSetFileBrowseButton;
    private JLabel ruleSetFileLabel;
    private JTextField ruleSetFileTextField;
    private JLabel ruleSetsLabel;
    private JList<String> ruleSetsList;
    private JScrollPane ruleSetsScrollPane;
    private JButton searchButton;
    // End of variables declaration//GEN-END:variables
}
