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

package org.netbeans.modules.php.phpdoc.ui.customizer;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.phpdoc.PhpDocumentorProvider;
import org.netbeans.modules.php.phpdoc.ui.PhpDocPreferences;
import org.netbeans.modules.php.phpdoc.ui.PhpDocPreferencesValidator;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

final class PhpDocPanel extends JPanel {

    private static final long serialVersionUID = -4686321547613435L;

    private final PhpModule phpModule;
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    PhpDocPanel(PhpModule phpModule) {
        assert EventQueue.isDispatchThread();
        assert phpModule != null;

        this.phpModule = phpModule;

        initComponents();

        init();
    }

    private void init() {
        targetTextField.setText(PhpDocPreferences.getPhpDocTarget(phpModule, false));
        titleTextField.setText(PhpDocPreferences.getPhpDocTitle(phpModule));
        configurationTextField.setText(PhpDocPreferences.getPhpDocConfigurationPath(phpModule));
        configurationCheckBox.setSelected(PhpDocPreferences.isConfigurationEnabled(phpModule));
        enableComponents(configurationCheckBox.isSelected(), getConfigurationFileComponents());
        addListeners();
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        targetTextField.getDocument().addDocumentListener(defaultDocumentListener);
        titleTextField.getDocument().addDocumentListener(defaultDocumentListener);
        configurationTextField.getDocument().addDocumentListener(defaultDocumentListener);
        configurationCheckBox.addItemListener((ItemEvent e) -> {
            enableComponents(e.getStateChange() == ItemEvent.SELECTED, getConfigurationFileComponents());
            fireChange();
        });
    }

    private JComponent[] getConfigurationFileComponents() {
        return new JComponent[] {
            configurationLabel,
            configurationTextField,
            configurationBrowseButton
        };
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private String getPhpDocTarget() {
        return targetTextField.getText().trim();
    }

    private String getPhpDocTitle() {
        return titleTextField.getText().trim();
    }

    private String getPhpDocConfigurationPath() {
        return configurationTextField.getText().trim();
    }

    private boolean isPhpDocConfigurationEnabled() {
        return configurationCheckBox.isSelected();
    }

    boolean isValidData() {
        ValidationResult result = getValidationResult();
        return !result.hasErrors();
    }

    public String getErrorMessage() {
        ValidationResult result = getValidationResult();
        ValidationResult.Message error = result.getFirstError();
        if (error != null) {
            return error.getMessage();
        }
        return null;
    }

    public String getWarningMessage() {
        ValidationResult result = getValidationResult();
        ValidationResult.Message warning = result.getFirstWarning();
        if (warning != null) {
            return warning.getMessage();
        }
        return null;
    }

    public void storeData() {
        PhpDocPreferences.setPhpDocTarget(phpModule, getPhpDocTarget());
        PhpDocPreferences.setPhpDocTitle(phpModule, getPhpDocTitle());
        PhpDocPreferences.setPhpDocConfigurationPath(phpModule, getPhpDocConfigurationPath());
        PhpDocPreferences.setConfigurationEnabled(phpModule, isPhpDocConfigurationEnabled());
    }

    private ValidationResult getValidationResult() {
        return new PhpDocPreferencesValidator()
                .validateTarget(getPhpDocTarget())
                .validateTitle(getPhpDocTitle())
                .validateConfiguration(isPhpDocConfigurationEnabled(), getPhpDocConfigurationPath())
                .getResult();
    }

    void enableComponents(boolean enabled, JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        targetLabel = new JLabel();
        targetTextField = new JTextField();
        targetButton = new JButton();
        titleLabel = new JLabel();
        titleTextField = new JTextField();
        configurationCheckBox = new JCheckBox();
        configurationLabel = new JLabel();
        configurationTextField = new JTextField();
        configurationBrowseButton = new JButton();

        targetLabel.setLabelFor(targetTextField);
        Mnemonics.setLocalizedText(targetLabel, NbBundle.getMessage(PhpDocPanel.class, "PhpDocPanel.targetLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(targetButton, NbBundle.getMessage(PhpDocPanel.class, "PhpDocPanel.targetButton.text")); // NOI18N
        targetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                targetButtonActionPerformed(evt);
            }
        });

        titleLabel.setLabelFor(titleTextField);
        Mnemonics.setLocalizedText(titleLabel, NbBundle.getMessage(PhpDocPanel.class, "PhpDocPanel.titleLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(configurationCheckBox, NbBundle.getMessage(PhpDocPanel.class, "PhpDocPanel.configurationCheckBox.text")); // NOI18N

        configurationLabel.setLabelFor(configurationTextField);
        Mnemonics.setLocalizedText(configurationLabel, NbBundle.getMessage(PhpDocPanel.class, "PhpDocPanel.configurationLabel.text")); // NOI18N

        configurationTextField.setText(NbBundle.getMessage(PhpDocPanel.class, "PhpDocPanel.configurationTextField.text")); // NOI18N

        Mnemonics.setLocalizedText(configurationBrowseButton, NbBundle.getMessage(PhpDocPanel.class, "PhpDocPanel.configurationBrowseButton.text")); // NOI18N
        configurationBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configurationBrowseButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(targetLabel)
                    .addComponent(titleLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(targetTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(targetButton))
                    .addComponent(titleTextField)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(configurationCheckBox)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configurationLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(configurationTextField)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(configurationBrowseButton))
        );
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(targetLabel)
                    .addComponent(targetTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(configurationCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(configurationLabel)
                    .addComponent(configurationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configurationBrowseButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void targetButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_targetButtonActionPerformed
        File phpDocTarget = new FileChooserBuilder(PhpDocumentorProvider.class.getName() + PhpDocumentorProvider.PHPDOC_LAST_FOLDER_SUFFIX + phpModule.getName())
                .setTitle(NbBundle.getMessage(PhpDocPanel.class, "LBL_SelectDocFolder"))
                .setDirectoriesOnly(true)
                .setFileHiding(true)
                .setDefaultWorkingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
                .showOpenDialog();
        if (phpDocTarget != null) {
            phpDocTarget = FileUtil.normalizeFile(phpDocTarget);
            targetTextField.setText(phpDocTarget.getAbsolutePath());
        }
    }//GEN-LAST:event_targetButtonActionPerformed

    @NbBundle.Messages("PhpDocPanel.chooser.configuration=Select phpDocumentor XML configuration file")
    private void configurationBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configurationBrowseButtonActionPerformed
        File configurationFile = new FileChooserBuilder(PhpDocumentorProvider.class.getName() + PhpDocumentorProvider.PHPDOC_LAST_FOLDER_SUFFIX + phpModule.getName())
                .setTitle(Bundle.PhpDocPanel_chooser_configuration())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
                .showOpenDialog();
        if (configurationFile != null) {
            configurationFile = FileUtil.normalizeFile(configurationFile);
            configurationTextField.setText(configurationFile.getAbsolutePath());
        }
    }//GEN-LAST:event_configurationBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton configurationBrowseButton;
    private JCheckBox configurationCheckBox;
    private JLabel configurationLabel;
    private JTextField configurationTextField;
    private JButton targetButton;
    private JLabel targetLabel;
    private JTextField targetTextField;
    private JLabel titleLabel;
    private JTextField titleTextField;
    // End of variables declaration//GEN-END:variables

    private final class DefaultDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }
        private void processChange() {
            fireChange();
        }
    }
}
