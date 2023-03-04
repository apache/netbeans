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

package org.netbeans.modules.php.nette.tester.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.nette.tester.preferences.TesterPreferences;
import org.netbeans.modules.php.nette.tester.preferences.TesterPreferencesValidator;
import org.netbeans.modules.php.nette.tester.util.TesterUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class CustomizerTester extends JPanel implements HelpCtx.Provider {

    private final ProjectCustomizer.Category category;
    private final PhpModule phpModule;


    public CustomizerTester(ProjectCustomizer.Category category, PhpModule phpModule) {
        assert category != null;
        assert phpModule != null;

        this.category = category;
        this.phpModule = phpModule;

        initComponents();
        init();
    }

    private void init() {
        initFile(TesterPreferences.isPhpIniEnabled(phpModule),
                TesterPreferences.getPhpIniPath(phpModule),
                phpIniCheckBox, phpIniTextField);
        initFile(TesterPreferences.isTesterEnabled(phpModule),
                TesterPreferences.getTesterPath(phpModule),
                testerCheckBox, testerTextField);
        initBinaryExecutable();
        initFile(TesterPreferences.isCoverageSourcePathEnabled(phpModule),
                TesterPreferences.getCoverageSourcePath(phpModule),
                coverageSrcCheckBox, coverageSrcTextField);

        enableComponents(phpIniCheckBox.isSelected(), phpIniLabel, phpIniTextField, phpIniBrowseButton);
        enableComponents(testerCheckBox.isSelected(), testerLabel, testerTextField, testerBrowseButton);
        enableComponents(binaryExecutableCheckBox.isSelected(), binaryExecutableLabel, binaryExecutableComboBox);
        enableComponents(coverageSrcCheckBox.isSelected(), coverageSrcLabel, coverageSrcTextField, coverageSrcBrowseButton);

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
        return new HelpCtx("org.netbeans.modules.php.nette.tester.ui.customizer.CustomizerTester"); // NOI18N
    }

    void enableComponents(boolean enabled, JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }

    void validateData() {
        ValidationResult result = new TesterPreferencesValidator()
                .validatePhpIni(phpIniCheckBox.isSelected(), phpIniTextField.getText())
                .validateTester(testerCheckBox.isSelected(), testerTextField.getText())
                .validateCoverageSourcePath(coverageSrcCheckBox.isSelected(), coverageSrcTextField.getText())
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
        TesterPreferences.setPhpIniEnabled(phpModule, phpIniCheckBox.isSelected());
        TesterPreferences.setPhpIniPath(phpModule, phpIniTextField.getText());
        TesterPreferences.setTesterEnabled(phpModule, testerCheckBox.isSelected());
        TesterPreferences.setTesterPath(phpModule, testerTextField.getText());
        TesterPreferences.setBinaryEnabled(phpModule, binaryExecutableCheckBox.isSelected());
        TesterPreferences.setBinaryExecutable(phpModule, (String) binaryExecutableComboBox.getSelectedItem());
        TesterPreferences.setCoverageSourcePathEnabled(phpModule, coverageSrcCheckBox.isSelected());
        TesterPreferences.setCoverageSourcePath(phpModule, coverageSrcTextField.getText());
    }

    private void initFile(boolean enabled, String file, JCheckBox checkBox, JTextField textField) {
        checkBox.setSelected(enabled);
        textField.setText(file);
    }

    private void initBinaryExecutable() {
        binaryExecutableCheckBox.setSelected(TesterPreferences.isBinaryEnabled(phpModule));
        for (String binaryExecutable : TesterUtils.BINARY_EXECUTABLES) {
            binaryExecutableComboBox.addItem(binaryExecutable);
        }
        binaryExecutableComboBox.setSelectedItem(TesterPreferences.getBinaryExecutable(phpModule));
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        ActionListener defaultActionListener = new DefaultActionListener();

        phpIniCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableComponents(e.getStateChange() == ItemEvent.SELECTED, phpIniLabel, phpIniTextField, phpIniBrowseButton);
                validateData();
            }
        });
        phpIniTextField.getDocument().addDocumentListener(defaultDocumentListener);

        testerCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableComponents(e.getStateChange() == ItemEvent.SELECTED, testerLabel, testerTextField, testerBrowseButton);
                validateData();
            }
        });
        testerTextField.getDocument().addDocumentListener(defaultDocumentListener);

        binaryExecutableCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableComponents(e.getStateChange() == ItemEvent.SELECTED, binaryExecutableLabel, binaryExecutableComboBox);
                validateData();
            }
        });
        binaryExecutableComboBox.addActionListener(defaultActionListener);

        coverageSrcCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableComponents(e.getStateChange() == ItemEvent.SELECTED, coverageSrcLabel, coverageSrcTextField, coverageSrcBrowseButton);
                validateData();
            }
        });
        coverageSrcTextField.getDocument().addDocumentListener(defaultDocumentListener);
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

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        phpIniCheckBox = new JCheckBox();
        phpIniLabel = new JLabel();
        phpIniTextField = new JTextField();
        phpIniBrowseButton = new JButton();
        testerCheckBox = new JCheckBox();
        testerLabel = new JLabel();
        testerTextField = new JTextField();
        testerBrowseButton = new JButton();
        binaryExecutableCheckBox = new JCheckBox();
        binaryExecutableLabel = new JLabel();
        binaryExecutableComboBox = new JComboBox<String>();
        noteLabel = new JLabel();
        infoLabel = new JLabel();
        coverageSrcCheckBox = new JCheckBox();
        coverageSrcLabel = new JLabel();
        coverageSrcTextField = new JTextField();
        coverageSrcBrowseButton = new JButton();

        Mnemonics.setLocalizedText(phpIniCheckBox, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.phpIniCheckBox.text")); // NOI18N

        phpIniLabel.setLabelFor(phpIniTextField);
        Mnemonics.setLocalizedText(phpIniLabel, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.phpIniLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpIniBrowseButton, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.phpIniBrowseButton.text")); // NOI18N
        phpIniBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpIniBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(testerCheckBox, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.testerCheckBox.text")); // NOI18N

        testerLabel.setLabelFor(testerTextField);
        Mnemonics.setLocalizedText(testerLabel, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.testerLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(testerBrowseButton, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.testerBrowseButton.text")); // NOI18N
        testerBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                testerBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(binaryExecutableCheckBox, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.binaryExecutableCheckBox.text")); // NOI18N

        binaryExecutableLabel.setLabelFor(binaryExecutableComboBox);
        Mnemonics.setLocalizedText(binaryExecutableLabel, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.binaryExecutableLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(infoLabel, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.infoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(coverageSrcCheckBox, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.coverageSrcCheckBox.text")); // NOI18N

        coverageSrcLabel.setLabelFor(coverageSrcTextField);
        Mnemonics.setLocalizedText(coverageSrcLabel, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.coverageSrcLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(coverageSrcBrowseButton, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.coverageSrcBrowseButton.text")); // NOI18N
        coverageSrcBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                coverageSrcBrowseButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(phpIniCheckBox)
            .addComponent(testerCheckBox)
            .addComponent(binaryExecutableCheckBox)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(binaryExecutableLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(binaryExecutableComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel))
            .addComponent(coverageSrcCheckBox)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(phpIniLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phpIniTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phpIniBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(testerLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testerTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testerBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(coverageSrcLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(coverageSrcTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(coverageSrcBrowseButton))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {coverageSrcBrowseButton, phpIniBrowseButton, testerBrowseButton});

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(phpIniCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(phpIniLabel)
                    .addComponent(phpIniTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(phpIniBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testerCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(testerLabel)
                    .addComponent(testerTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(testerBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(binaryExecutableCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(binaryExecutableLabel)
                    .addComponent(binaryExecutableComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(coverageSrcCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(coverageSrcLabel)
                    .addComponent(coverageSrcTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(coverageSrcBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages({
        "CustomizerTester.chooser.php.ini=Select file or folder for php.ini",
        "CustomizerTester.chooser.php.ini.ok=Select",
    })
    private void phpIniBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpIniBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerTester.class)
                .setTitle(Bundle.CustomizerTester_chooser_php_ini())
                .setApproveText(Bundle.CustomizerTester_chooser_php_ini_ok())
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            phpIniTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_phpIniBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerTester.chooser.tester=Select Tester file")
    private void testerBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_testerBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerTester.class)
                .setTitle(Bundle.CustomizerTester_chooser_tester())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            testerTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_testerBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerTester.chooser.coverage.src.path=Select Source Path for Coverage")
    private void coverageSrcBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_coverageSrcBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerTester.class)
                .setTitle(Bundle.CustomizerTester_chooser_coverage_src_path())
                .setDirectoriesOnly(true)
                .setDefaultWorkingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            coverageSrcTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_coverageSrcBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox binaryExecutableCheckBox;
    private JComboBox<String> binaryExecutableComboBox;
    private JLabel binaryExecutableLabel;
    private JButton coverageSrcBrowseButton;
    private JCheckBox coverageSrcCheckBox;
    private JLabel coverageSrcLabel;
    private JTextField coverageSrcTextField;
    private JLabel infoLabel;
    private JLabel noteLabel;
    private JButton phpIniBrowseButton;
    private JCheckBox phpIniCheckBox;
    private JLabel phpIniLabel;
    private JTextField phpIniTextField;
    private JButton testerBrowseButton;
    private JCheckBox testerCheckBox;
    private JLabel testerLabel;
    private JTextField testerTextField;
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

    private final class DefaultActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            validateData();
        }

    }

}
