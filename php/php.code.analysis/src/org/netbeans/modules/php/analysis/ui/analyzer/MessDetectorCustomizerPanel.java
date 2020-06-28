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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.options.ValidatorMessDetectorParameter;
import org.netbeans.modules.php.analysis.ui.MessDetectorRuleSetsListCellRenderer;
import org.netbeans.modules.php.analysis.ui.MessDetectorRuleSetsListModel;
import org.netbeans.modules.php.analysis.util.AnalysisUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;

public class MessDetectorCustomizerPanel extends JPanel {

    private static final long serialVersionUID = -4687321324676897L;

    public static final String ENABLED = "messDetector.enabled"; // NOI18N
    public static final String RULE_SETS = "messDetector.ruleSets"; // NOI18N
    public static final String RULE_SET_FILE = "messDetector.ruleSetFile"; // NOI18N
    private static final String RULE_SET_FILE_LAST_FOLDER_SUFFIX = ".messDetector.ruleSetFile"; // NOI18N

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
        enabledCheckBox.addItemListener((e) -> {
            setMessDetectorEnabled();
            setRuleSetsComponentsEnabled(enabledCheckBox.isSelected());
        });
        boolean isEnabled = settings.getBoolean(ENABLED, false);
        enabledCheckBox.setSelected(isEnabled);
        setRuleSetsComponentsEnabled(isEnabled);

        ruleSetsList.setModel(ruleSetsListModel);
        ruleSetsList.setCellRenderer(new MessDetectorRuleSetsListCellRenderer(ruleSetsList.getCellRenderer()));

        // rule sets
        List<String> ruleSets = getRuleSets(settings);
        if (ruleSets == null) {
            ruleSets = AnalysisOptions.getInstance().getMessDetectorRuleSets();
        }
        selectRuleSets(ruleSets);

        String ruleSetFile = settings.get(RULE_SET_FILE, AnalysisOptions.getInstance().getMessDetectorRuleSetFilePath());
        ruleSetFileTextField.setText(ruleSetFile);

        // listeners
        ruleSetsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                validateAndSetData();
            }
        });

        ruleSetFileTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateAndSetData();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateAndSetData();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateAndSetData();
            }
        });
    }

    public List<String> getSelectedRuleSets() {
        return ruleSetsList.getSelectedValuesList();
    }

    public String getRuleSetFile() {
        return ruleSetFileTextField.getText().trim();
    }

    void selectRuleSets(List<String> ruleSets) {
        ruleSetsList.clearSelection();
        for (String ruleSet : ruleSets) {
            int indexOf = MessDetectorRuleSetsListModel.getAllRuleSets().indexOf(ruleSet);
            assert indexOf != -1 : "Rule set not found: " + ruleSet;
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
        settings.put(RULE_SETS, AnalysisUtils.serialize(getSelectedRuleSets()));
        settings.put(RULE_SET_FILE, getRuleSetFile());
    }

    private void setMessDetectorEnabled() {
        settings.putBoolean(ENABLED, enabledCheckBox.isSelected());
    }

    private void setRuleSetsComponentsEnabled(boolean isEnabled) {
        ruleSetsLabel.setEnabled(isEnabled);
        ruleSetsList.setEnabled(isEnabled);
        ruleSetsScrollPane.setEnabled(isEnabled);
        ruleSetFileLabel.setEnabled(isEnabled);
        ruleSetFileTextField.setEnabled(isEnabled);
        ruleSetFileBrowseButton.setEnabled(isEnabled);
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

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(ruleSetsLabel)
                    .addComponent(ruleSetFileLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(ruleSetFileTextField)
                    .addComponent(ruleSetsScrollPane, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ruleSetFileBrowseButton))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(ruleSetsLabel)
                    .addComponent(ruleSetsScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(ruleSetFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(ruleSetFileLabel)
                    .addComponent(ruleSetFileBrowseButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("MessDetectorCustomizerPanel.ruleSetFile.browse.title=Select Mess Detector Rule Set File")
    private void ruleSetFileBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_ruleSetFileBrowseButtonActionPerformed
         File file = new FileChooserBuilder(MessDetectorCustomizerPanel.class.getName() + RULE_SET_FILE_LAST_FOLDER_SUFFIX)
                .setFilesOnly(true)
                .setTitle(Bundle.MessDetectorCustomizerPanel_ruleSetFile_browse_title())
                .showOpenDialog();
        if (file != null) {
            ruleSetFileTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_ruleSetFileBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox enabledCheckBox;
    private JButton ruleSetFileBrowseButton;
    private JLabel ruleSetFileLabel;
    private JTextField ruleSetFileTextField;
    private JLabel ruleSetsLabel;
    private JList<String> ruleSetsList;
    private JScrollPane ruleSetsScrollPane;
    // End of variables declaration//GEN-END:variables
}
