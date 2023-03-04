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
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.options.ValidatorCodeSnifferParameter;
import org.netbeans.modules.php.analysis.ui.AnalysisDefaultDocumentListener;
import org.netbeans.modules.php.analysis.ui.CodeSnifferStandardsComboBoxModel;
import org.netbeans.modules.php.analysis.util.AnalysisUiUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class CodeSnifferCustomizerPanel extends JPanel {

    private static final long serialVersionUID = 46872132457657L;
    private static final RequestProcessor RP = new RequestProcessor(CodeSnifferCustomizerPanel.class);

    public static final String ENABLED = "codeSniffer.enabled"; // NOI18N
    public static final String PATH = "codeSniffer.path"; // NOI18N
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
        initEnabledCheckBox();
        initCodeSnifferTextField();
        initStandardComboBox();
        // avoid NPE
        RP.schedule(() -> {
            EventQueue.invokeLater(() -> {
                setAllComponetsEnabled(enabledCheckBox.isSelected());
                // clear error because the error may be left when the configuration is changed
                context.setError(null);
                if (enabledCheckBox.isSelected()) {
                    validateData();
                }
            });
        }, 1000, TimeUnit.MILLISECONDS);
    }

    private void initEnabledCheckBox() {
        assert EventQueue.isDispatchThread();
        enabledCheckBox.setSelected(settings.getBoolean(ENABLED, false));
        // don't set errors in initialization becuase NPE occurs
        // so add the listener after setSelected method
        enabledCheckBox.addItemListener((e) -> {
            setAllComponetsEnabled(enabledCheckBox.isSelected());
            setCodeSnifferEnabled();
            if (!enabledCheckBox.isSelected()) {
                // NETBEANS-1550 clear errors because user can't set other configurations
                context.setError(null);
            } else {
                validateData();
            }
        });
    }

    private void initCodeSnifferTextField() {
        assert EventQueue.isDispatchThread();
        codeSnifferTextField.setText(settings.get(PATH, AnalysisOptions.getInstance().getCodeSnifferPath()));
        codeSnifferTextField.getDocument().addDocumentListener(new AnalysisDefaultDocumentListener(() -> {
            String codeSnifferPath = getValidCodeSnifferPath();
            // reset cached standards only if the new path is valid
            ValidationResult result = new AnalysisOptionsValidator()
                    .validateCodeSnifferPath(codeSnifferPath)
                    .getResult();
            if (validateData(result)) {
                setCodeSnifferPath();
                CodeSniffer.clearCachedStandards();
                setStandards(getCodeSnifferStandard(), codeSnifferPath);
            }
        }));
    }

    private void initStandardComboBox() {
        assert EventQueue.isDispatchThread();
        standardComboBox.setModel(standardsModel);
        standardsModel.fetchStandards(standardComboBox, settings.get(PATH, null), ()-> {
            standardComboBox.addItemListener((ItemEvent e) -> {
                if (enabledCheckBox.isSelected()) {
                    validateAndSetStandard();
                }
            });
        });
        standardsModel.setSelectedItem(settings.get(STANDARD, AnalysisOptions.getInstance().getCodeSnifferStandard()));
    }

    void validateAndSetStandard() {
        if (validateData()) {
            setStandard();
        }
    }

    private boolean validateData(ValidationResult result) {
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

    private boolean validateData() {
        ValidationResult result = new AnalysisOptionsValidator()
                .validateCodeSniffer(ValidatorCodeSnifferParameter.create(this))
                .getResult();
        return validateData(result);
    }

    public String getCodeSnifferPath() {
        return codeSnifferTextField.getText().trim();
    }

    /**
     * Get the valid code sniffer path.
     *
     * @return the path for Options if the path for this panel is empty.
     * Otherwise, the path for this panel. Can be {@code null}.
     */
    @CheckForNull
    public String getValidCodeSnifferPath() {
        if (StringUtils.hasText(getCodeSnifferPath())) {
            return getCodeSnifferPath();
        }
        return AnalysisOptions.getInstance().getCodeSnifferPath();
    }

    @CheckForNull
    public String getCodeSnifferStandard() {
         if (!standardComboBox.isEnabled()) {
            // fetching standards
            return null;
        }
       return standardsModel.getSelectedStandard();
    }

    private void setStandard() {
        settings.put(STANDARD, standardsModel.getSelectedStandard());
    }

    private void setCodeSnifferEnabled() {
        settings.putBoolean(ENABLED, enabledCheckBox.isSelected());
    }

    private void setCodeSnifferPath() {
        settings.put(PATH, getCodeSnifferPath());
    }

    private void setAllComponetsEnabled(boolean isEnabled) {
        Component[] components = getComponents();
        for (Component component : components) {
            if (component != enabledCheckBox) {
                component.setEnabled(isEnabled);
            }
        }
    }

    void setStandards(final String selectedCodeSnifferStandard, String customCodeSnifferPath) {
        standardsModel.fetchStandards(standardComboBox, customCodeSnifferPath, null);
        standardsModel.setSelectedItem(selectedCodeSnifferStandard);
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
        codeSnifferLabel = new JLabel();
        codeSnifferTextField = new JTextField();
        codeSnifferBrowseButton = new JButton();
        codeSnifferSearchButton = new JButton();

        Mnemonics.setLocalizedText(standardLabel, NbBundle.getMessage(CodeSnifferCustomizerPanel.class, "CodeSnifferCustomizerPanel.standardLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(enabledCheckBox, NbBundle.getMessage(CodeSnifferCustomizerPanel.class, "CodeSnifferCustomizerPanel.enabledCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(codeSnifferLabel, NbBundle.getMessage(CodeSnifferCustomizerPanel.class, "CodeSnifferCustomizerPanel.codeSnifferLabel.text")); // NOI18N

        codeSnifferTextField.setText(NbBundle.getMessage(CodeSnifferCustomizerPanel.class, "CodeSnifferCustomizerPanel.codeSnifferTextField.text")); // NOI18N

        Mnemonics.setLocalizedText(codeSnifferBrowseButton, NbBundle.getMessage(CodeSnifferCustomizerPanel.class, "CodeSnifferCustomizerPanel.codeSnifferBrowseButton.text")); // NOI18N
        codeSnifferBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                codeSnifferBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(codeSnifferSearchButton, NbBundle.getMessage(CodeSnifferCustomizerPanel.class, "CodeSnifferCustomizerPanel.codeSnifferSearchButton.text")); // NOI18N
        codeSnifferSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                codeSnifferSearchButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(codeSnifferLabel)
                    .addComponent(standardLabel)
                    .addComponent(enabledCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(standardComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(codeSnifferTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codeSnifferBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codeSnifferSearchButton))))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codeSnifferLabel)
                    .addComponent(codeSnifferTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(codeSnifferBrowseButton)
                    .addComponent(codeSnifferSearchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(standardLabel)
                    .addComponent(standardComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void codeSnifferBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_codeSnifferBrowseButtonActionPerformed
        File file = AnalysisUiUtils.browseCodeSniffer();
        if (file != null) {
            codeSnifferTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_codeSnifferBrowseButtonActionPerformed

    private void codeSnifferSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_codeSnifferSearchButtonActionPerformed
        String codeSniffer = AnalysisUiUtils.searchCodeSniffer();
        if (codeSniffer != null) {
            codeSnifferTextField.setText(codeSniffer);
        }
    }//GEN-LAST:event_codeSnifferSearchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton codeSnifferBrowseButton;
    private JLabel codeSnifferLabel;
    private JButton codeSnifferSearchButton;
    private JTextField codeSnifferTextField;
    private JCheckBox enabledCheckBox;
    private JComboBox<String> standardComboBox;
    private JLabel standardLabel;
    // End of variables declaration//GEN-END:variables
}
