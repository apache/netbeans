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
package org.netbeans.modules.php.atoum.ui.customizer;

import java.awt.Component;
import java.awt.EventQueue;
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
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.atoum.commands.Atoum;
import org.netbeans.modules.php.atoum.preferences.AtoumPreferences;
import org.netbeans.modules.php.atoum.preferences.AtoumPreferencesValidator;
import org.netbeans.modules.php.atoum.ui.options.AtoumOptionsPanelController;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

public class CustomizerAtoum extends JPanel implements HelpCtx.Provider {

    private static final RequestProcessor RP = new RequestProcessor(CustomizerAtoum.class);

    private final ProjectCustomizer.Category category;
    private final PhpModule phpModule;


    CustomizerAtoum(ProjectCustomizer.Category category, PhpModule phpModule) {
        assert category != null;
        assert phpModule != null;

        this.category = category;
        this.phpModule = phpModule;

        initComponents();
        init();
    }

    private void init() {
        initFile(AtoumPreferences.isBootstrapEnabled(phpModule),
                AtoumPreferences.getBootstrapPath(phpModule),
                bootstrapCheckBox, bootstrapTextField);
        initFile(AtoumPreferences.isConfigurationEnabled(phpModule),
                AtoumPreferences.getConfigurationPath(phpModule),
                configurationCheckBox, configurationTextField);
        initFile(AtoumPreferences.isAtoumEnabled(phpModule),
                AtoumPreferences.getAtoumPath(phpModule),
                scriptCheckBox, scriptTextField);

        enableFile(bootstrapCheckBox.isSelected(), bootstrapLabel, bootstrapTextField, bootstrapBrowseButton);
        enableFile(configurationCheckBox.isSelected(), configurationLabel, configurationTextField, configurationBrowseButton, configurationWarningLabel);
        enableFile(scriptCheckBox.isSelected(), scriptLabel, scriptTextField, scriptBrowseButton);

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
        return new HelpCtx("org.netbeans.modules.php.atoum.ui.customizer.CustomizerAtoum"); // NOI18N
    }

    void enableFile(boolean enabled, JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }

    void validateData() {
        ValidationResult result = new AtoumPreferencesValidator()
                .validateBootstrap(bootstrapCheckBox.isSelected(), bootstrapTextField.getText())
                .validateConfiguration(configurationCheckBox.isSelected(), configurationTextField.getText())
                .validateAtoum(scriptCheckBox.isSelected(), scriptTextField.getText())
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

    void storeData() {
        AtoumPreferences.setBootstrapEnabled(phpModule, bootstrapCheckBox.isSelected());
        AtoumPreferences.setBootstrapPath(phpModule, bootstrapTextField.getText());
        AtoumPreferences.setConfigurationEnabled(phpModule, configurationCheckBox.isSelected());
        AtoumPreferences.setConfigurationPath(phpModule, configurationTextField.getText());
        AtoumPreferences.setAtoumEnabled(phpModule, scriptCheckBox.isSelected());
        AtoumPreferences.setAtoumPath(phpModule, scriptTextField.getText());
    }

    private void initFile(boolean enabled, String file, JCheckBox checkBox, JTextField textField) {
        checkBox.setSelected(enabled);
        textField.setText(file);
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        bootstrapCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED,
                        bootstrapLabel, bootstrapTextField, bootstrapBrowseButton);
                validateData();
            }
        });
        bootstrapTextField.getDocument().addDocumentListener(defaultDocumentListener);

        configurationCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED,
                        configurationLabel, configurationTextField, configurationBrowseButton, configurationWarningLabel);
                validateData();
            }
        });
        configurationTextField.getDocument().addDocumentListener(defaultDocumentListener);

        scriptCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, scriptLabel, scriptTextField, scriptBrowseButton);
                validateData();
            }
        });
        scriptTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    private File getDefaultDirectory() {
        File defaultDirectory;
        FileObject testDirectory = phpModule.getTestDirectory(null);
        if (testDirectory != null) {
            defaultDirectory = FileUtil.toFile(testDirectory);
        } else {
            FileObject sourcesDirectory = phpModule.getSourceDirectory();
            assert sourcesDirectory != null;
            defaultDirectory = FileUtil.toFile(sourcesDirectory);
        }
        assert defaultDirectory != null;
        return defaultDirectory;
    }

    @NbBundle.Messages("CustomizerAtoum.error.noTestDir=Test directory is not set yet. Set it in Sources category and save this dialog.")
    private boolean checkTestDirectory() {
        if (phpModule.getTestDirectory(null) == null) {
            informUser(Bundle.CustomizerAtoum_error_noTestDir());
            return false;
        }
        return true;
    }

    boolean checkFile(File file, String errorMessage) {
        assert file != null;
        if (file.exists()) {
            informUser(errorMessage);
            return false;
        }
        return true;
    }

    private void informUser(String message) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE));
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bootstrapCheckBox = new JCheckBox();
        bootstrapLabel = new JLabel();
        bootstrapTextField = new JTextField();
        bootstrapBrowseButton = new JButton();
        configurationCheckBox = new JCheckBox();
        configurationLabel = new JLabel();
        configurationTextField = new JTextField();
        configurationBrowseButton = new JButton();
        configurationWarningLabel = new JLabel();
        scriptCheckBox = new JCheckBox();
        scriptLabel = new JLabel();
        scriptTextField = new JTextField();
        scriptBrowseButton = new JButton();
        createLabel = new JLabel();
        createButton = new JButton();

        Mnemonics.setLocalizedText(bootstrapCheckBox, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.bootstrapCheckBox.text")); // NOI18N

        bootstrapLabel.setLabelFor(bootstrapTextField);
        Mnemonics.setLocalizedText(bootstrapLabel, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.bootstrapLabel.text")); // NOI18N

        bootstrapTextField.setColumns(20);

        Mnemonics.setLocalizedText(bootstrapBrowseButton, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.bootstrapBrowseButton.text")); // NOI18N
        bootstrapBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bootstrapBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(configurationCheckBox, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.configurationCheckBox.text")); // NOI18N

        configurationLabel.setLabelFor(configurationTextField);
        Mnemonics.setLocalizedText(configurationLabel, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.configurationLabel.text")); // NOI18N

        configurationTextField.setColumns(20);

        Mnemonics.setLocalizedText(configurationBrowseButton, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.configurationBrowseButton.text")); // NOI18N
        configurationBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configurationBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(configurationWarningLabel, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.configurationWarningLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(scriptCheckBox, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.scriptCheckBox.text")); // NOI18N

        scriptLabel.setLabelFor(scriptTextField);
        Mnemonics.setLocalizedText(scriptLabel, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.scriptLabel.text")); // NOI18N

        scriptTextField.setColumns(20);

        Mnemonics.setLocalizedText(scriptBrowseButton, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.scriptBrowseButton.text")); // NOI18N
        scriptBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                scriptBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(createLabel, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.createLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(createButton, NbBundle.getMessage(CustomizerAtoum.class, "CustomizerAtoum.createButton.text")); // NOI18N
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(createLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(createButton))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(bootstrapCheckBox)
                    .addComponent(configurationCheckBox)
                    .addComponent(scriptCheckBox))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(configurationWarningLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bootstrapLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bootstrapTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bootstrapBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(configurationLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configurationTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configurationBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scriptLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scriptTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scriptBrowseButton))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {bootstrapBrowseButton, configurationBrowseButton, createButton, scriptBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(bootstrapCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bootstrapLabel)
                    .addComponent(bootstrapTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bootstrapBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configurationCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(configurationLabel)
                    .addComponent(configurationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configurationBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configurationWarningLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scriptCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(scriptLabel)
                    .addComponent(scriptTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(scriptBrowseButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(createLabel)
                    .addComponent(createButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CustomizerAtoum.chooser.bootstrap=Select atoum bootstrap file")
    private void bootstrapBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_bootstrapBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerAtoum.class)
                .setTitle(Bundle.CustomizerAtoum_chooser_bootstrap())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            bootstrapTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_bootstrapBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerAtoum.chooser.configuration=Select atoum configuration file")
    private void configurationBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configurationBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerAtoum.class)
                .setTitle(Bundle.CustomizerAtoum_chooser_configuration())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            configurationTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_configurationBrowseButtonActionPerformed

    @NbBundle.Messages({
        "# {0} - file path",
        "CustomizerAtoum.error.bootstrap.exists=Bootstrap {0} already exists.",
        "# {0} - file path",
        "CustomizerAtoum.error.configuration.exists=Configuration {0} already exists.",
    })
    private void createButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        if (!checkTestDirectory()) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                // check
                final File bootstrap = Atoum.getDefaultBootstrap(phpModule);
                assert bootstrap != null;
                if (!checkFile(bootstrap, Bundle.CustomizerAtoum_error_bootstrap_exists(bootstrap.getAbsolutePath()))) {
                    return;
                }
                final File configuration = Atoum.getDefaultConfiguration(phpModule);
                assert configuration != null;
                if (!checkFile(configuration, Bundle.CustomizerAtoum_error_configuration_exists(configuration.getAbsolutePath()))) {
                    return;
                }
                // run
                Atoum atoum;
                try {
                    atoum = Atoum.getDefault();
                } catch (InvalidPhpExecutableException ex) {
                    UiUtils.invalidScriptProvided(ex.getLocalizedMessage(), AtoumOptionsPanelController.OPTIONS_SUB_PATH);
                    return;
                }
                assert atoum != null;
                final Pair<File, File> files = atoum.init(phpModule);
                if (files == null) {
                    return;
                }
                // set
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        assert bootstrap.equals(files.first()) : bootstrap + " should equal " + files.first();
                        assert bootstrap.isFile() : bootstrap;
                        bootstrapCheckBox.setSelected(true);
                        bootstrapTextField.setText(bootstrap.getAbsolutePath());
                        assert configuration.equals(files.second()) : configuration + " should equal " + files.second();
                        assert configuration.isFile() : configuration;
                        configurationCheckBox.setSelected(true);
                        configurationTextField.setText(configuration.getAbsolutePath());
                    }
                });
            }
        });
    }//GEN-LAST:event_createButtonActionPerformed

    @NbBundle.Messages("CustomizerAtoum.chooser.atoum=Select atoum file")
    private void scriptBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_scriptBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerAtoum.class)
                .setTitle(Bundle.CustomizerAtoum_chooser_atoum())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            scriptTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_scriptBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton bootstrapBrowseButton;
    private JCheckBox bootstrapCheckBox;
    private JLabel bootstrapLabel;
    private JTextField bootstrapTextField;
    private JButton configurationBrowseButton;
    private JCheckBox configurationCheckBox;
    private JLabel configurationLabel;
    private JTextField configurationTextField;
    private JLabel configurationWarningLabel;
    private JButton createButton;
    private JLabel createLabel;
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
