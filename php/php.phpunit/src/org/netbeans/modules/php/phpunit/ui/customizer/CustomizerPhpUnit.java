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

package org.netbeans.modules.php.phpunit.ui.customizer;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.phpunit.PhpUnitVersion;
import org.netbeans.modules.php.phpunit.commands.PhpUnit;
import org.netbeans.modules.php.phpunit.options.PhpUnitOptions;
import org.netbeans.modules.php.phpunit.preferences.PhpUnitPreferences;
import org.netbeans.modules.php.phpunit.preferences.PhpUnitPreferencesValidator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Tomas Mysik
 */
public final class CustomizerPhpUnit extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = 2171421712032630826L;
    private static final RequestProcessor RP = new RequestProcessor(CustomizerPhpUnit.class);

    private final ProjectCustomizer.Category category;
    private final PhpModule phpModule;


    public CustomizerPhpUnit(ProjectCustomizer.Category category, PhpModule phpModule) {
        this.category = category;
        this.phpModule = phpModule;

        initComponents();
        init();
    }

    private void init() {
        initFile(PhpUnitPreferences.isBootstrapEnabled(phpModule),
                PhpUnitPreferences.getBootstrapPath(phpModule),
                bootstrapCheckBox, bootstrapTextField);
        bootstrapForCreateTestsCheckBox.setSelected(PhpUnitPreferences.isBootstrapForCreateTests(phpModule));
        initFile(PhpUnitPreferences.isConfigurationEnabled(phpModule),
                PhpUnitPreferences.getConfigurationPath(phpModule),
                configurationCheckBox, configurationTextField);
        initFile(PhpUnitPreferences.isCustomSuiteEnabled(phpModule),
                PhpUnitPreferences.getCustomSuitePath(phpModule),
                suiteCheckBox, suiteTextField);
        initFile(PhpUnitPreferences.isPhpUnitEnabled(phpModule),
                PhpUnitPreferences.getPhpUnitPath(phpModule),
                scriptCheckBox, scriptTextField);
        runPhpUnitOnlyCheckBox.setSelected(PhpUnitPreferences.getRunPhpUnitOnly(phpModule));
        runTestUsingUnitCheckBox.setSelected(PhpUnitPreferences.getRunAllTestFiles(phpModule));
        askForTestGroupsCheckBox.setSelected(PhpUnitPreferences.getAskForTestGroups(phpModule));
        isRelativePathEnabled.setSelected(PhpUnitPreferences.isRelativePathEnabled(phpModule));
        initPhpUnitVersion();

        enableFile(bootstrapCheckBox.isSelected(), bootstrapLabel, bootstrapTextField, bootstrapGenerateButton, bootstrapBrowseButton, bootstrapForCreateTestsCheckBox);
        enableFile(configurationCheckBox.isSelected(), configurationLabel, configurationTextField, configurationGenerateButton, configurationBrowseButton);
        enableFile(suiteCheckBox.isSelected(), suiteLabel, suiteTextField, suiteBrowseButton, suiteInfoLabel);
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
        // do not change, backward compatibility
        return new HelpCtx("org.netbeans.modules.php.project.ui.customizer.CustomizerPhpUnit"); // NOI18N
    }

    void enableFile(boolean enabled, JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }

    void validateData() {
        ValidationResult result = new PhpUnitPreferencesValidator()
                .validateBootstrap(bootstrapCheckBox.isSelected(), bootstrapTextField.getText())
                .validateConfiguration(configurationCheckBox.isSelected(), configurationTextField.getText())
                .validateCustomSuite(suiteCheckBox.isSelected(), suiteTextField.getText())
                .validatePhpUnit(scriptCheckBox.isSelected(), scriptTextField.getText())
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

    void updatePhpUnitVersion() {
        assert EventQueue.isDispatchThread();
        ValidationResult result = new PhpUnitPreferencesValidator()
                .validatePhpUnit(scriptCheckBox.isSelected(), scriptTextField.getText())
                .getResult();
        // errors and warnings are shown with validateData()
        if (result.hasErrors() || result.hasWarnings()) {
            versionLineLabel.setText(Bundle.CustomizerPhpUnit_notFound_phpUnit_version());
            return;
        }
        setPhpUnitVersion();
    }

    private boolean resetPhpUnitVersions() {
        return scriptCheckBox.isSelected() != PhpUnitPreferences.isPhpUnitEnabled(phpModule)
                || !scriptTextField.getText().equals(PhpUnitPreferences.getPhpUnitPath(phpModule));
    }

    void storeData() {
        if (resetPhpUnitVersions()) {
            PhpUnit.resetVersions();
        }
        PhpUnitPreferences.setBootstrapEnabled(phpModule, bootstrapCheckBox.isSelected());
        PhpUnitPreferences.setBootstrapPath(phpModule, bootstrapTextField.getText());
        PhpUnitPreferences.setBootstrapForCreateTests(phpModule, bootstrapForCreateTestsCheckBox.isSelected());
        PhpUnitPreferences.setConfigurationEnabled(phpModule, configurationCheckBox.isSelected());
        PhpUnitPreferences.setConfigurationPath(phpModule, configurationTextField.getText());
        PhpUnitPreferences.setCustomSuiteEnabled(phpModule, suiteCheckBox.isSelected());
        PhpUnitPreferences.setCustomSuitePath(phpModule, suiteTextField.getText());
        PhpUnitPreferences.setPhpUnitEnabled(phpModule, scriptCheckBox.isSelected());
        PhpUnitPreferences.setPhpUnitPath(phpModule, scriptTextField.getText());
        PhpUnitPreferences.setRunPhpUnitOnly(phpModule, runPhpUnitOnlyCheckBox.isSelected());
        PhpUnitPreferences.setRunAllTestFiles(phpModule, runTestUsingUnitCheckBox.isSelected());
        PhpUnitPreferences.setAskForTestGroups(phpModule, askForTestGroupsCheckBox.isSelected());
        PhpUnitPreferences.setRelativePathEnabled(phpModule, isRelativePathEnabled.isSelected());
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
                        bootstrapLabel, bootstrapTextField, bootstrapGenerateButton, bootstrapBrowseButton, bootstrapForCreateTestsCheckBox);
                validateData();
            }
        });
        bootstrapTextField.getDocument().addDocumentListener(defaultDocumentListener);
        bootstrapForCreateTestsCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                validateData();
            }
        });

        configurationCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, configurationLabel, configurationTextField, configurationGenerateButton, configurationBrowseButton);
                validateData();
            }
        });
        configurationTextField.getDocument().addDocumentListener(defaultDocumentListener);

        suiteCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, suiteLabel, suiteTextField, suiteBrowseButton, suiteInfoLabel);
                validateData();
            }
        });
        suiteTextField.getDocument().addDocumentListener(defaultDocumentListener);

        scriptCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, scriptLabel, scriptTextField, scriptBrowseButton);
                validateData();
                updatePhpUnitVersion();
            }
        });
        scriptTextField.getDocument().addDocumentListener(defaultDocumentListener);
        scriptTextField.getDocument().addDocumentListener(new PhpUnitScriptDocumentListener());

        final ItemListener validateItemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                validateData();
            }
        };
        runPhpUnitOnlyCheckBox.addItemListener(validateItemListener);
        runTestUsingUnitCheckBox.addItemListener(validateItemListener);
        askForTestGroupsCheckBox.addItemListener(validateItemListener);
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

    @NbBundle.Messages("CustomizerPhpUnit.error.noTestDir=Test directory is not set yet. Set it in Sources category and save this dialog.")
    private boolean checkTestDirectory() {
        if (phpModule.getTestDirectory(null) == null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.CustomizerPhpUnit_error_noTestDir(), NotifyDescriptor.INFORMATION_MESSAGE));
            return false;
        }
        return true;
    }

    private void initPhpUnitVersion() {
        setPhpUnitVersion();
    }

    @NbBundle.Messages({
        "CustomizerPhpUnit.getting.phpUnit.version=Getting the version...",
        "CustomizerPhpUnit.notFound.phpUnit.version=PHPUnit version not found"
    })
    private void setPhpUnitVersion() {
        assert EventQueue.isDispatchThread();
        versionLineLabel.setText(Bundle.CustomizerPhpUnit_getting_phpUnit_version());
        final String phpUnitPath;
        if (scriptCheckBox.isSelected()) {
            phpUnitPath = scriptTextField.getText().trim();
        } else {
            phpUnitPath = PhpUnitOptions.getInstance().getPhpUnitPath();
        }
        if (!StringUtils.hasText(phpUnitPath)) {
            SwingUtilities.invokeLater(() -> versionLineLabel.setText(Bundle.CustomizerPhpUnit_notFound_phpUnit_version()));
        } else {
            RP.post(() -> {
                String versionLine = PhpUnit.getVersionLine(phpUnitPath);
                SwingUtilities.invokeLater(() -> versionLineLabel.setText(versionLine));
            });
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bootstrapLabel = new JLabel();
        bootstrapTextField = new JTextField();
        bootstrapBrowseButton = new JButton();
        bootstrapGenerateButton = new JButton();
        bootstrapForCreateTestsCheckBox = new JCheckBox();
        configurationCheckBox = new JCheckBox();
        configurationLabel = new JLabel();
        configurationTextField = new JTextField();
        configurationBrowseButton = new JButton();
        bootstrapCheckBox = new JCheckBox();
        configurationGenerateButton = new JButton();
        suiteCheckBox = new JCheckBox();
        suiteLabel = new JLabel();
        suiteTextField = new JTextField();
        suiteBrowseButton = new JButton();
        suiteInfoLabel = new JLabel();
        scriptCheckBox = new JCheckBox();
        scriptLabel = new JLabel();
        scriptTextField = new JTextField();
        scriptBrowseButton = new JButton();
        runPhpUnitOnlyCheckBox = new JCheckBox();
        runTestUsingUnitCheckBox = new JCheckBox();
        askForTestGroupsCheckBox = new JCheckBox();
        versionLabel = new JLabel();
        versionLineLabel = new JLabel();
        isRelativePathEnabled = new JCheckBox();

        bootstrapLabel.setLabelFor(bootstrapTextField);
        Mnemonics.setLocalizedText(bootstrapLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapLabel.text")); // NOI18N

        bootstrapTextField.setColumns(20);

        Mnemonics.setLocalizedText(bootstrapBrowseButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapBrowseButton.text")); // NOI18N
        bootstrapBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bootstrapBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(bootstrapGenerateButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapGenerateButton.text")); // NOI18N
        bootstrapGenerateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bootstrapGenerateButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(bootstrapForCreateTestsCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapForCreateTestsCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(configurationCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationCheckBox.text")); // NOI18N

        configurationLabel.setLabelFor(configurationTextField);
        Mnemonics.setLocalizedText(configurationLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationLabel.text")); // NOI18N

        configurationTextField.setColumns(20);

        Mnemonics.setLocalizedText(configurationBrowseButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationBrowseButton.text")); // NOI18N
        configurationBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configurationBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(bootstrapCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(configurationGenerateButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationGenerateButton.text")); // NOI18N
        configurationGenerateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configurationGenerateButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(suiteCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteCheckBox.text")); // NOI18N

        suiteLabel.setLabelFor(suiteTextField);
        Mnemonics.setLocalizedText(suiteLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteLabel.text")); // NOI18N

        suiteTextField.setColumns(20);

        Mnemonics.setLocalizedText(suiteBrowseButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteBrowseButton.text")); // NOI18N
        suiteBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                suiteBrowseButtonActionPerformed(evt);
            }
        });

        suiteInfoLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(suiteInfoLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteInfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(scriptCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.scriptCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(scriptLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.scriptLabel.text")); // NOI18N

        scriptTextField.setColumns(20);

        Mnemonics.setLocalizedText(scriptBrowseButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.scriptBrowseButton.text")); // NOI18N
        scriptBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                scriptBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(runPhpUnitOnlyCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.runPhpUnitOnlyCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(runTestUsingUnitCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.runTestUsingUnitCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(askForTestGroupsCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.askForTestGroupsCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.versionLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(versionLineLabel, "VERSION"); // NOI18N

        Mnemonics.setLocalizedText(isRelativePathEnabled, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.isRelativePathEnabled.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(configurationLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(configurationTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(configurationBrowseButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(configurationGenerateButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(suiteLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(suiteInfoLabel)
                                .addContainerGap())
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(suiteTextField)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(suiteBrowseButton))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bootstrapForCreateTestsCheckBox)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bootstrapLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(bootstrapTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(bootstrapBrowseButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(bootstrapGenerateButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scriptLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(scriptTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(scriptBrowseButton))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(configurationCheckBox)
                    .addComponent(suiteCheckBox)
                    .addComponent(bootstrapCheckBox)
                    .addComponent(runTestUsingUnitCheckBox)
                    .addComponent(askForTestGroupsCheckBox)
                    .addComponent(scriptCheckBox)
                    .addComponent(runPhpUnitOnlyCheckBox)
                    .addComponent(isRelativePathEnabled))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(versionLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(versionLineLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {bootstrapBrowseButton, bootstrapGenerateButton, configurationBrowseButton, configurationGenerateButton, scriptBrowseButton, suiteBrowseButton});

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(bootstrapCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(bootstrapLabel)
                    .addComponent(bootstrapTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bootstrapGenerateButton)
                    .addComponent(bootstrapBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(bootstrapForCreateTestsCheckBox)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(configurationCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(configurationLabel)
                    .addComponent(configurationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configurationGenerateButton)
                    .addComponent(configurationBrowseButton))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(suiteCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(suiteLabel)
                    .addComponent(suiteTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(suiteBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(suiteInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(scriptCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(scriptLabel)
                    .addComponent(scriptTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(scriptBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(isRelativePathEnabled)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(runPhpUnitOnlyCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(runTestUsingUnitCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(askForTestGroupsCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionLineLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bootstrapLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapLabel.AccessibleContext.accessibleName")); // NOI18N
        bootstrapLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapTextField.AccessibleContext.accessibleName")); // NOI18N
        bootstrapTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapTextField.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        bootstrapBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapGenerateButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapGenerateButton.AccessibleContext.accessibleName")); // NOI18N
        bootstrapGenerateButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapGenerateButton.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapForCreateTestsCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapForCreateTestsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        bootstrapForCreateTestsCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapForCreateTestsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        configurationCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationCheckBox.AccessibleContext.accessibleName")); // NOI18N
        configurationCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        configurationLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationLabel.AccessibleContext.accessibleName")); // NOI18N
        configurationLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationLabel.AccessibleContext.accessibleDescription")); // NOI18N
        configurationTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationTextField.AccessibleContext.accessibleName")); // NOI18N
        configurationTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationTextField.AccessibleContext.accessibleDescription")); // NOI18N
        configurationBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        configurationBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapCheckBox.AccessibleContext.accessibleName")); // NOI18N
        bootstrapCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        configurationGenerateButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationGenerateButton.AccessibleContext.accessibleName")); // NOI18N
        configurationGenerateButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationGenerateButton.AccessibleContext.accessibleDescription")); // NOI18N
        suiteCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteCheckBox.AccessibleContext.accessibleName")); // NOI18N
        suiteCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        suiteLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteLabel.AccessibleContext.accessibleName")); // NOI18N
        suiteLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteLabel.AccessibleContext.accessibleDescription")); // NOI18N
        suiteTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteTextField.AccessibleContext.accessibleName")); // NOI18N
        suiteTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteTextField.AccessibleContext.accessibleDescription")); // NOI18N
        suiteBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        suiteBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        suiteInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        suiteInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        versionLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.versionLabel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CustomizerPhpUnit.chooser.bootstrap=Select PHPUnit bootstrap file")
    private void bootstrapBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_bootstrapBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerPhpUnit.class)
                .setTitle(Bundle.CustomizerPhpUnit_chooser_bootstrap())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            bootstrapTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_bootstrapBrowseButtonActionPerformed

    private void bootstrapGenerateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_bootstrapGenerateButtonActionPerformed
        if (checkTestDirectory()) {
            File bootstrap = PhpUnit.createBootstrapFile(phpModule);
            if (bootstrap != null) {
                bootstrapTextField.setText(bootstrap.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_bootstrapGenerateButtonActionPerformed

    @NbBundle.Messages("CustomizerPhpUnit.chooser.configuration=Select PHPUnit XML configuration file")
    private void configurationBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configurationBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerPhpUnit.class)
                .setTitle(Bundle.CustomizerPhpUnit_chooser_configuration())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            configurationTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_configurationBrowseButtonActionPerformed

    private void configurationGenerateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configurationGenerateButtonActionPerformed
        if (checkTestDirectory()) {
            File configuration = PhpUnit.createConfigurationFile(phpModule);
            if (configuration != null) {
                configurationTextField.setText(configuration.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_configurationGenerateButtonActionPerformed

    @NbBundle.Messages("CustomizerPhpUnit.chooser.suite=Select PHPUnit test suite file")
    private void suiteBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_suiteBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerPhpUnit.class)
                .setTitle(Bundle.CustomizerPhpUnit_chooser_suite())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            suiteTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_suiteBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerPhpUnit.chooser.phpUnit=Select PHPUnit script")
    private void scriptBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_scriptBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerPhpUnit.class)
                .setTitle(Bundle.CustomizerPhpUnit_chooser_phpUnit())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            scriptTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_scriptBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox askForTestGroupsCheckBox;
    private JButton bootstrapBrowseButton;
    private JCheckBox bootstrapCheckBox;
    private JCheckBox bootstrapForCreateTestsCheckBox;
    private JButton bootstrapGenerateButton;
    private JLabel bootstrapLabel;
    private JTextField bootstrapTextField;
    private JButton configurationBrowseButton;
    private JCheckBox configurationCheckBox;
    private JButton configurationGenerateButton;
    private JLabel configurationLabel;
    private JTextField configurationTextField;
    private JCheckBox isRelativePathEnabled;
    private JCheckBox runPhpUnitOnlyCheckBox;
    private JCheckBox runTestUsingUnitCheckBox;
    private JButton scriptBrowseButton;
    private JCheckBox scriptCheckBox;
    private JLabel scriptLabel;
    private JTextField scriptTextField;
    private JButton suiteBrowseButton;
    private JCheckBox suiteCheckBox;
    private JLabel suiteInfoLabel;
    private JLabel suiteLabel;
    private JTextField suiteTextField;
    private JLabel versionLabel;
    private JLabel versionLineLabel;
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

    private final class PhpUnitScriptDocumentListener implements DocumentListener {

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
            updatePhpUnitVersion();
        }
    }

    // if we have to add the phpunit version combobox in the future, we can use this
    private static class PhpUnitVersionComboBoxModel extends DefaultComboBoxModel<PhpUnitVersion> {

        private static final long serialVersionUID = 5850175934190021687L;

        public PhpUnitVersionComboBoxModel() {
            this(null);
        }

        public PhpUnitVersionComboBoxModel(PhpUnitVersion preselected) {
            super(PhpUnitVersion.values());

            if (preselected != null) {
                setSelectedItem(preselected);
            } else {
                setSelectedItem(PhpUnitVersion.getDefault());
            }
        }
    }

}
