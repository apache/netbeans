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
package org.netbeans.modules.php.codeception.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.codeception.preferences.CodeceptionPreferences;
import org.netbeans.modules.php.codeception.preferences.CodeceptionPreferencesValidator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class CustomizerCodeception extends JPanel implements HelpCtx.Provider {

    private final ProjectCustomizer.Category category;
    private final PhpModule phpModule;


    public CustomizerCodeception(ProjectCustomizer.Category category, PhpModule phpModule) {
        this.category = category;
        this.phpModule = phpModule;
        initComponents();
        init();
    }

    private void init() {
        initFile(CodeceptionPreferences.isCustomCodeceptEnabled(phpModule),
                CodeceptionPreferences.getCustomCodeceptPath(phpModule),
                scriptCheckBox, scriptTextField);
        initFile(CodeceptionPreferences.isCustomCodeceptionYmlEnabled(phpModule),
                CodeceptionPreferences.getCustomCodeceptionYmlPath(phpModule),
                codeceptionCheckBox, codeceptionTextField);

        enableFile(scriptCheckBox.isSelected(), scriptLabel, scriptTextField, scriptBrowseButton);
        enableFile(codeceptionCheckBox.isSelected(), codeceptionLabel, codeceptionTextField, codeceptionBrowseButton);
        askForAdditionalParametersCheckBox.setSelected(CodeceptionPreferences.askForAdditionalParameters(phpModule));

        addListeners();
        validateData();
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                storeData();
            }
        });
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.codeception.ui.customizer.CustomizerCodeception"); // NOI18N
    }

    void storeData() {
        CodeceptionPreferences.setCustomCodeceptEnabled(phpModule, scriptCheckBox.isSelected());
        CodeceptionPreferences.setCustomCodeceptPath(phpModule, scriptTextField.getText());
        CodeceptionPreferences.setCustomCodeceptionYmlEnabled(phpModule, codeceptionCheckBox.isSelected());
        CodeceptionPreferences.setCustomCodeceptionYmlPath(phpModule, codeceptionTextField.getText());
        CodeceptionPreferences.setAskForAdditionalParameters(phpModule, askForAdditionalParametersCheckBox.isSelected());
    }

    private void initFile(boolean enabled, String file, JCheckBox checkBox, JTextField textField) {
        checkBox.setSelected(enabled);
        textField.setText(file);
    }

    void enableFile(boolean enabled, JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }

    void validateData() {
        ValidationResult result = new CodeceptionPreferencesValidator()
                .validateCodecept(scriptCheckBox.isSelected(), scriptTextField.getText())
                .validateCodeceptionYml(codeceptionCheckBox.isSelected(), codeceptionTextField.getText())
                .getResult();
        for (ValidationResult.Message message : result.getErrors()) {
            category.setErrorMessage(message.getMessage());
            category.setValid(false);
            return;
        }
        for (ValidationResult.Message message : result.getWarnings()) {
            category.setErrorMessage(message.getMessage());
            category.setValid(true);
            return;
        }
        category.setErrorMessage(null);
        category.setValid(true);
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        scriptCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, scriptLabel, scriptTextField, scriptBrowseButton);
                validateData();
            }
        });
        scriptTextField.getDocument().addDocumentListener(defaultDocumentListener);

        codeceptionCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, codeceptionLabel, codeceptionTextField, codeceptionBrowseButton);
                validateData();
            }
        });
        codeceptionTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    private File getDefaultDirectory() {
        File defaultDirectory;
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory != null) {
            defaultDirectory = FileUtil.toFile(sourceDirectory);
        } else {
            FileObject projectDirectory = phpModule.getProjectDirectory();
            defaultDirectory = FileUtil.toFile(projectDirectory);
        }
        assert defaultDirectory != null;
        return defaultDirectory;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scriptCheckBox = new JCheckBox();
        scriptLabel = new JLabel();
        scriptTextField = new JTextField();
        scriptBrowseButton = new JButton();
        codeceptionCheckBox = new JCheckBox();
        codeceptionLabel = new JLabel();
        codeceptionTextField = new JTextField();
        codeceptionBrowseButton = new JButton();
        askForAdditionalParametersCheckBox = new JCheckBox();

        Mnemonics.setLocalizedText(scriptCheckBox, NbBundle.getMessage(CustomizerCodeception.class, "CustomizerCodeception.scriptCheckBox.text")); // NOI18N

        scriptLabel.setLabelFor(scriptTextField);
        Mnemonics.setLocalizedText(scriptLabel, NbBundle.getMessage(CustomizerCodeception.class, "CustomizerCodeception.scriptLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(scriptBrowseButton, NbBundle.getMessage(CustomizerCodeception.class, "CustomizerCodeception.scriptBrowseButton.text")); // NOI18N
        scriptBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                scriptBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(codeceptionCheckBox, NbBundle.getMessage(CustomizerCodeception.class, "CustomizerCodeception.codeceptionCheckBox.text")); // NOI18N

        codeceptionLabel.setLabelFor(codeceptionTextField);
        Mnemonics.setLocalizedText(codeceptionLabel, NbBundle.getMessage(CustomizerCodeception.class, "CustomizerCodeception.codeceptionLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(codeceptionBrowseButton, NbBundle.getMessage(CustomizerCodeception.class, "CustomizerCodeception.codeceptionBrowseButton.text")); // NOI18N
        codeceptionBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                codeceptionBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(askForAdditionalParametersCheckBox, NbBundle.getMessage(CustomizerCodeception.class, "CustomizerCodeception.askForAdditionalParametersCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(codeceptionLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codeceptionTextField))
                    .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(scriptLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scriptTextField)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(scriptBrowseButton)
                    .addComponent(codeceptionBrowseButton)))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(scriptCheckBox)
                    .addComponent(codeceptionCheckBox)
                    .addComponent(askForAdditionalParametersCheckBox))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scriptCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(scriptTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(scriptLabel)
                    .addComponent(scriptBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeceptionCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codeceptionLabel)
                    .addComponent(codeceptionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(codeceptionBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(askForAdditionalParametersCheckBox))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CustomizerCodeception.chooser.codecept=Select Codecept Script")
    private void scriptBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_scriptBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerCodeception.class)
                .setTitle(Bundle.CustomizerCodeception_chooser_codecept())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            scriptTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_scriptBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerCodeception.chooser.codeception.yml=Select codeception.yml")
    private void codeceptionBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_codeceptionBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerCodeception.class)
                .setTitle(Bundle.CustomizerCodeception_chooser_codeception_yml())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            codeceptionTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_codeceptionBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox askForAdditionalParametersCheckBox;
    private JButton codeceptionBrowseButton;
    private JCheckBox codeceptionCheckBox;
    private JLabel codeceptionLabel;
    private JTextField codeceptionTextField;
    private JButton scriptBrowseButton;
    private JCheckBox scriptCheckBox;
    private JLabel scriptLabel;
    private JTextField scriptTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

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
            validateData();
        }

    }

}
