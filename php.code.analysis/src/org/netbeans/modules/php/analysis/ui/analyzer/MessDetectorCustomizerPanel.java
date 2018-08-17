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

import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.ui.MessDetectorRuleSetsListModel;
import org.netbeans.modules.php.analysis.util.AnalysisUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public class MessDetectorCustomizerPanel extends JPanel {

    private static final long serialVersionUID = -4687321324676897L;

    public static final String ENABLED = "messDetector.enabled"; // NOI18N
    public static final String RULE_SETS = "messDetector.ruleSets"; // NOI18N

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

        // rule sets
        List<String> ruleSets = getRuleSets(settings);
        if (ruleSets == null) {
            ruleSets = AnalysisOptions.getInstance().getMessDetectorRuleSets();
        }
        selectRuleSets(ruleSets);

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
    }

    List<String> getSelectedRuleSets() {
        return ruleSetsList.getSelectedValuesList();
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
                .validateMessDetectorRuleSets(getSelectedRuleSets())
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
    }

    private void setMessDetectorEnabled() {
        settings.putBoolean(ENABLED, enabledCheckBox.isSelected());
    }

    private void setRuleSetsComponentsEnabled(boolean isEnabled) {
        ruleSetsLabel.setEnabled(isEnabled);
        ruleSetsList.setEnabled(isEnabled);
        ruleSetsScrollPane.setEnabled(isEnabled);
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

        ruleSetsLabel.setLabelFor(ruleSetsList);
        Mnemonics.setLocalizedText(ruleSetsLabel, NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.ruleSetsLabel.text")); // NOI18N

        ruleSetsScrollPane.setViewportView(ruleSetsList);

        Mnemonics.setLocalizedText(enabledCheckBox, NbBundle.getMessage(MessDetectorCustomizerPanel.class, "MessDetectorCustomizerPanel.enabledCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ruleSetsLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ruleSetsScrollPane, GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(enabledCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(ruleSetsLabel)
                    .addComponent(ruleSetsScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox enabledCheckBox;
    private JLabel ruleSetsLabel;
    private JList<String> ruleSetsList;
    private JScrollPane ruleSetsScrollPane;
    // End of variables declaration//GEN-END:variables
}
