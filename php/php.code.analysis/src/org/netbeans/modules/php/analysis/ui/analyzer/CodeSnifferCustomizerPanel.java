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

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.ui.CodeSnifferStandardsComboBoxModel;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class CodeSnifferCustomizerPanel extends JPanel {

    private static final long serialVersionUID = 46872132457657L;
    private static final RequestProcessor RP = new RequestProcessor(CodeSnifferCustomizerPanel.class);

    public static final String ENABLED = "codeSniffer.enabled"; // NOI18N
    public static final String STANDARD = "codeSniffer.standard"; // NOI18N

    final CodeSnifferStandardsComboBoxModel standardsModel = new CodeSnifferStandardsComboBoxModel();
    final Analyzer.CustomizerContext<Void, CodeSnifferCustomizerPanel> context;
    final Preferences settings;


    public CodeSnifferCustomizerPanel(Analyzer.CustomizerContext<Void, CodeSnifferCustomizerPanel> context) {
        assert context != null;

        this.context = context;
        this.settings = context.getSettings();

        initComponents();
        init();
    }

    private void init() {
        boolean isEnabled = settings.getBoolean(ENABLED, false);
        enabledCheckBox.setSelected(isEnabled);
        // don't set errors in initialization becuase NPE occurs
        // so add the listener after setSelected method
        enabledCheckBox.addItemListener((e) -> {
            setStandardComponentsEnabled(enabledCheckBox.isSelected());
            setCodeSnifferEnabled();
            if (!enabledCheckBox.isSelected()) {
                // NETBEANS-1550 clear errors because user can't set other configurations
                context.setError(null);
            } else {
                validateData();
            }
        });

        standardComboBox.setModel(standardsModel);
        standardsModel.fetchStandards(standardComboBox);
        standardsModel.setSelectedItem(settings.get(STANDARD, AnalysisOptions.getInstance().getCodeSnifferStandard()));
        // listeners
        standardComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (enabledCheckBox.isSelected()) {
                    validateAndSetData();
                }
            }
        });

        RP.schedule(() -> {
            EventQueue.invokeLater(() -> {
                setStandardComponentsEnabled(isEnabled);
            });
        }, 1000, TimeUnit.MILLISECONDS);
    }

    void validateAndSetData() {
        if (validateData()) {
            setData();
        }
    }

    private boolean validateData() {
        ValidationResult result = new AnalysisOptionsValidator()
                .validateCodeSnifferStandard(standardsModel.getSelectedStandard())
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
        settings.put(STANDARD, standardsModel.getSelectedStandard());
    }

    private void setCodeSnifferEnabled() {
        settings.putBoolean(ENABLED, enabledCheckBox.isSelected());
    }

    private void setStandardComponentsEnabled(boolean isEnabled) {
        standardComboBox.setEnabled(isEnabled);
        standardLabel.setEnabled(isEnabled);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        standardLabel = new JLabel();
        standardComboBox = new JComboBox<>();
        enabledCheckBox = new JCheckBox();

        Mnemonics.setLocalizedText(standardLabel, NbBundle.getMessage(CodeSnifferCustomizerPanel.class, "CodeSnifferCustomizerPanel.standardLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(enabledCheckBox, NbBundle.getMessage(CodeSnifferCustomizerPanel.class, "CodeSnifferCustomizerPanel.enabledCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(standardLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(standardComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(enabledCheckBox)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(standardLabel)
                    .addComponent(standardComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox enabledCheckBox;
    private JComboBox<String> standardComboBox;
    private JLabel standardLabel;
    // End of variables declaration//GEN-END:variables

}
