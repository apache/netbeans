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

package org.netbeans.modules.selenium2.webclient.mocha;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaJSPreferences;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaSeleniumPreferences;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaPreferencesValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public final class CustomizerMocha extends javax.swing.JPanel {

    private final Project project;
    private final boolean isSelenium;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final SpinnerNumberModel timeoutModel;
    private boolean autoDiscovered = false;

    private volatile String mochaInstallFolder;
    private volatile int timeout;

    // @GuardedBy("EDT")
    private ValidationResult validationResult;


    public CustomizerMocha(Project project, boolean isSelenium) {
        assert EventQueue.isDispatchThread();
        assert project != null;

        this.project = project;
        this.isSelenium = isSelenium;
        timeoutModel = new SpinnerNumberModel(65534, 1, 65534, 1);

        initComponents();
        init();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getMochaInstallFolder() {
        return mochaInstallFolder;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean getAutoWatch() {
        return autowatchCheckBox.isSelected();
    }

    public String getWarningMessage() {
        assert EventQueue.isDispatchThread();
        for (ValidationResult.Message message : validationResult.getWarnings()) {
            return message.getMessage();
        }
        return null;
    }

    public String getErrorMessage() {
        assert EventQueue.isDispatchThread();
        for (ValidationResult.Message message : validationResult.getErrors()) {
            return message.getMessage();
        }
        return null;
    }

    @NbBundle.Messages({"CustomizerMocha.mocha.dir.info=Full path of mocha installation dir (typically node_modules/mocha).",
    "CustomizerMocha.timeout.info=Test-case timeout in milliseconds."})
    private void init() {
        assert EventQueue.isDispatchThread();
        String mochaDir;
        // get saved mocha install dir if previously set from selenium/unit mocha preferences
        mochaDir = isSelenium ? MochaSeleniumPreferences.getMochaDir(project) : MochaJSPreferences.getMochaDir(project);
        if(mochaDir == null) {
            // that did not work so try to get saved mocha install dir from unit/selenium mocha preferences
            mochaDir = isSelenium ? MochaJSPreferences.getMochaDir(project) : MochaSeleniumPreferences.getMochaDir(project);
        }
        if(mochaDir == null) { // mocha dir not set yet, try searching for it in project's local node_modules dir
            String dir = new File(FileUtil.toFile(project.getProjectDirectory()), "node_modules/mocha").getAbsolutePath();
            ValidationResult result = new MochaPreferencesValidator()
                .validateMochaInstallFolder(dir)
                .getResult();
            if(result.isFaultless()) { // mocha is installed in project's local node_modules dir
                mochaDir = dir;
                autoDiscovered = true;
            }
        }
        mochaDirTextField.setText(mochaDir);
        mochaDirInfoLabel.setText(Bundle.CustomizerMocha_mocha_dir_info());
        timeoutSpinner.setModel(timeoutModel);
        timeout = isSelenium ? MochaSeleniumPreferences.getTimeout(project) : MochaJSPreferences.getTimeout(project);
        timeoutModel.setValue(timeout);
        timeoutInfoLabel.setText(Bundle.CustomizerMocha_timeout_info());
        if(isSelenium) {
            autowatchCheckBox.setVisible(false);
        } else {
            autowatchCheckBox.setSelected(MochaJSPreferences.isAutoWatch(project));
        }
        // listeners
        addListeners();
        // initial validation
        validateData();
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        mochaDirTextField.getDocument().addDocumentListener(defaultDocumentListener);
        timeoutModel.addChangeListener(new DefaultChangeListener());
    }

    @NbBundle.Messages({"CustomizerMocha.confirm.autodiscovered.info=Mocha installation dir was auto-discovered. Please confirm by clicking OK."})
    void validateData() {
        assert EventQueue.isDispatchThread();
        mochaInstallFolder = mochaDirTextField.getText();
        validationResult = new MochaPreferencesValidator()
                .validateMochaInstallFolder(mochaInstallFolder)
                .getResult();
        if (autoDiscovered) { // auto-discovered, show confirmation message to the user
            validationResult.addWarning(new ValidationResult.Message("path", Bundle.CustomizerMocha_confirm_autodiscovered_info())); // NOI18N
        }
        changeSupport.fireChange();
    }


    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mochaDirLabel = new JLabel();
        mochaDirTextField = new JTextField();
        mochaDirBrowseButton = new JButton();
        mochaDirInfoLabel = new JLabel();
        timeoutLabel = new JLabel();
        timeoutInfoLabel = new JLabel();
        timeoutSpinner = new JSpinner();
        autowatchCheckBox = new JCheckBox();

        Mnemonics.setLocalizedText(mochaDirLabel, NbBundle.getMessage(CustomizerMocha.class, "CustomizerMocha.mochaDirLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(mochaDirBrowseButton, NbBundle.getMessage(CustomizerMocha.class, "CustomizerMocha.mochaDirBrowseButton.text")); // NOI18N
        mochaDirBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mochaDirBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(mochaDirInfoLabel, NbBundle.getMessage(CustomizerMocha.class, "CustomizerMocha.mochaDirInfoLabel.text")); // NOI18N

        timeoutLabel.setLabelFor(timeoutSpinner);
        Mnemonics.setLocalizedText(timeoutLabel, NbBundle.getMessage(CustomizerMocha.class, "CustomizerMocha.timeoutLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(timeoutInfoLabel, NbBundle.getMessage(CustomizerMocha.class, "CustomizerMocha.timeoutInfoLabel.text")); // NOI18N

        timeoutSpinner.setEditor(new JSpinner.NumberEditor(timeoutSpinner, "#"));

        Mnemonics.setLocalizedText(autowatchCheckBox, NbBundle.getMessage(CustomizerMocha.class, "CustomizerMocha.autowatchCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(mochaDirLabel)
                    .addComponent(timeoutLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mochaDirTextField, GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mochaDirBrowseButton)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(mochaDirInfoLabel)
                            .addComponent(timeoutInfoLabel)
                            .addComponent(timeoutSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(autowatchCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(mochaDirLabel)
                    .addComponent(mochaDirTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(mochaDirBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mochaDirInfoLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(timeoutLabel)
                    .addComponent(timeoutSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeoutInfoLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autowatchCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CustomizerMocha.chooser.config=Select mocha install location")
    private void mochaDirBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_mochaDirBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(CustomizerMocha.class)
        .setTitle(Bundle.CustomizerMocha_chooser_config())
//        .setFilesOnly(true)
        .setDirectoriesOnly(true)
        .setDefaultWorkingDirectory(FileUtil.toFile(project.getProjectDirectory()))
        .forceUseOfDefaultWorkingDirectory(true)
        .showOpenDialog();
        if (file != null) {
            mochaDirTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_mochaDirBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox autowatchCheckBox;
    private JButton mochaDirBrowseButton;
    private JLabel mochaDirInfoLabel;
    private JLabel mochaDirLabel;
    private JTextField mochaDirTextField;
    private JLabel timeoutInfoLabel;
    private JLabel timeoutLabel;
    private JSpinner timeoutSpinner;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

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
            validateData();
        }

    }

    private final class DefaultChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            timeout = timeoutModel.getNumber().intValue();
            validateData();
        }

    }

}
