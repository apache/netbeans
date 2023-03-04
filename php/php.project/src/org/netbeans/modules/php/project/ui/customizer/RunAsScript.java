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
package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.netbeans.modules.php.project.connections.ConfigManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.modules.php.project.runconfigs.RunConfigScript;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigScriptValidator;
import org.netbeans.modules.php.project.ui.LastUsedFolders;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * @author  Radek Matous, Tomas Mysik
 */
@org.netbeans.api.annotations.common.SuppressWarnings("SE_BAD_FIELD_STORE")
public final class RunAsScript extends RunAsPanel.InsidePanel {

    private static final long serialVersionUID = 5468731321321L;

    private final PhpProject project;
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    private final PropertyChangeListener phpInterpreterListener;
    final Category category;


    public RunAsScript(PhpProject project, ConfigManager manager, Category category) {
        super(manager);
        this.project = project;
        this.category = category;

        initComponents();
        this.labels = new JLabel[] {
            indexFileLabel,
            interpreterLabel,
            argsLabel,
            workDirLabel,
            phpOptionsLabel,
        };
        this.textFields = new JTextField[] {
            indexFileTextField,
            interpreterTextField,
            argsTextField,
            workDirTextField,
            phpOptionsTextField,
        };
        this.propertyNames = new String[] {
            PhpProjectProperties.INDEX_FILE,
            PhpProjectProperties.INTERPRETER,
            PhpProjectProperties.ARGS,
            PhpProjectProperties.WORK_DIR,
            PhpProjectProperties.PHP_ARGS,
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }

        // php cli
        defaultInterpreterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = defaultInterpreterCheckBox.isSelected();
                interpreterBrowseButton.setEnabled(!selected);
                interpreterTextField.setEditable(!selected);
                String newValue = null;
                if (selected) {
                    newValue = getDefaultPhpInterpreter();
                } else {
                    newValue = interpreterTextField.getText();
                }
                // hack - fire event in _every_ case (need to update run configuration)
                interpreterTextField.setText(newValue + " "); // NOI18N
            }
        });
        phpInterpreterListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (PhpOptions.PROP_PHP_INTERPRETER.equals(evt.getPropertyName())) {
                    if (defaultInterpreterCheckBox.isSelected()) {
                        // #143315
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                interpreterTextField.setText(getDefaultPhpInterpreter());
                                composeHint();
                            }
                        });
                    }
                }
            }
        };
        PhpOptions phpOptions = PhpOptions.getInstance();
        phpOptions.addPropertyChangeListener(WeakListeners.propertyChange(phpInterpreterListener, phpOptions));
        composeHint();
    }

    private String getDefaultPhpInterpreter() {
        String phpInterpreter = PhpOptions.getInstance().getPhpInterpreter();
        return phpInterpreter != null ? phpInterpreter : ""; //NOI18N
    }

    private String initPhpInterpreterFields() {
        String phpInterpreter = getValue(PhpProjectProperties.INTERPRETER);
        boolean def = phpInterpreter == null || phpInterpreter.length() == 0;
        defaultInterpreterCheckBox.setSelected(def);
        interpreterBrowseButton.setEnabled(!def);
        interpreterTextField.setEditable(!def);
        if (def) {
            return getDefaultPhpInterpreter();
        }
        return phpInterpreter;
    }

    @Override
    protected RunAsType getRunAsType() {
        return RunConfigScript.getRunAsType();
    }

    @Override
    public String getDisplayName() {
        return RunConfigScript.getDisplayName();
    }

    @Override
    protected JLabel getRunAsLabel() {
        return runAsLabel;
    }

    @Override
    public JComboBox<String> getRunAsCombo() {
        return runAsCombo;
    }

    @Override
    protected void loadFields() {
        for (int i = 0; i < textFields.length; i++) {
            String val = getValue(propertyNames[i]);
            if (PhpProjectProperties.INTERPRETER.equals(propertyNames[i])) {
                val = initPhpInterpreterFields();
            }
            textFields[i].setText(val);
        }
    }

    @Override
    protected void validateFields() {
        category.setErrorMessage(RunConfigScriptValidator.validateCustomizer(createRunConfig()));
        // #148957 always allow to save customizer
        category.setValid(true);
    }

    private RunConfigScript createRunConfig() {
        return RunConfigScript.create()
                .setUseDefaultInterpreter(defaultInterpreterCheckBox.isSelected())
                .setInterpreter(interpreterTextField.getText().trim())
                .setOptions(phpOptionsTextField.getText().trim())
                .setIndexParentDir(FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project)))
                .setIndexRelativePath(indexFileTextField.getText().trim())
                .setArguments(argsTextField.getText().trim())
                .setWorkDir(workDirTextField.getText().trim());
    }

    void composeHint() {
        hintLabel.setText("<html><body>" + createRunConfig().getHint()); // NOI18N
    }

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
        }

        @Override
        protected final String getDefaultValue() {
            return RunAsScript.this.getDefaultValue(getPropName());
        }

        @Override
        protected void processUpdate() {
            super.processUpdate();
            composeHint();
        }

        @Override
        protected String getPropValue() {
            if (PhpProjectProperties.INTERPRETER.equals(getPropName())
                    && defaultInterpreterCheckBox.isSelected()) {
                return ""; // NOI18N
            }
            return super.getPropValue().trim();
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

        interpreterLabel = new JLabel();
        interpreterTextField = new JTextField();
        interpreterBrowseButton = new JButton();
        defaultInterpreterCheckBox = new JCheckBox();
        configureButton = new JButton();
        argsLabel = new JLabel();
        argsTextField = new JTextField();
        runAsLabel = new JLabel();
        runAsCombo = new JComboBox<String>();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();
        workDirLabel = new JLabel();
        workDirTextField = new JTextField();
        workDirBrowseButton = new JButton();
        phpOptionsLabel = new JLabel();
        phpOptionsTextField = new JTextField();
        hintLabel = new JLabel();

        interpreterLabel.setLabelFor(interpreterTextField);
        Mnemonics.setLocalizedText(interpreterLabel, NbBundle.getMessage(RunAsScript.class, "LBL_PhpInterpreter")); // NOI18N

        interpreterTextField.setEditable(false);
        interpreterTextField.setColumns(20);

        Mnemonics.setLocalizedText(interpreterBrowseButton, NbBundle.getMessage(RunAsScript.class, "LBL_BrowseInterpreter")); // NOI18N
        interpreterBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                interpreterBrowseButtonActionPerformed(evt);
            }
        });

        defaultInterpreterCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(defaultInterpreterCheckBox, NbBundle.getMessage(RunAsScript.class, "LBL_UseDefaultInterpreter")); // NOI18N

        Mnemonics.setLocalizedText(configureButton, NbBundle.getMessage(RunAsScript.class, "LBL_Configure")); // NOI18N
        configureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        argsLabel.setLabelFor(argsTextField);
        Mnemonics.setLocalizedText(argsLabel, NbBundle.getMessage(RunAsScript.class, "LBL_Arguments")); // NOI18N

        argsTextField.setColumns(20);

        runAsLabel.setLabelFor(runAsCombo);
        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsScript.class, "LBL_RunAs")); // NOI18N

        indexFileLabel.setLabelFor(indexFileTextField);
        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsScript.class, "LBL_IndexFile")); // NOI18N

        indexFileTextField.setColumns(20);

        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsScript.class, "LBL_Browse")); // NOI18N
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(workDirLabel, NbBundle.getMessage(RunAsScript.class, "RunAsScript.workDirLabel.text")); // NOI18N

        workDirTextField.setColumns(20);

        Mnemonics.setLocalizedText(workDirBrowseButton, NbBundle.getMessage(RunAsScript.class, "RunAsScript.workDirBrowseButton.text")); // NOI18N
        workDirBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                workDirBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(phpOptionsLabel, NbBundle.getMessage(RunAsScript.class, "RunAsScript.phpOptionsLabel.text")); // NOI18N

        phpOptionsTextField.setColumns(20);

        Mnemonics.setLocalizedText(hintLabel, "dummy"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(runAsLabel)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(interpreterLabel)
                    .addComponent(indexFileLabel)
                    .addComponent(argsLabel)
                    .addComponent(workDirLabel)
                    .addComponent(phpOptionsLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hintLabel)
                        .addContainerGap())
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(argsTextField, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(indexFileTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(indexFileBrowseButton))
                        .addComponent(runAsCombo, Alignment.TRAILING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(interpreterTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(interpreterBrowseButton))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(defaultInterpreterCheckBox)
                            .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(configureButton))
                        .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(workDirTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(workDirBrowseButton))
                        .addComponent(phpOptionsTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {configureButton, indexFileBrowseButton, interpreterBrowseButton, workDirBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(runAsLabel)
                    .addComponent(runAsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(interpreterLabel)
                    .addComponent(interpreterBrowseButton)
                    .addComponent(interpreterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(defaultInterpreterCheckBox)
                    .addComponent(configureButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(indexFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(indexFileLabel)
                    .addComponent(indexFileBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(argsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(argsLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(workDirLabel)
                    .addComponent(workDirTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(workDirBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(phpOptionsLabel)
                    .addComponent(phpOptionsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(hintLabel))
        );

        interpreterLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterLabel.AccessibleContext.accessibleName")); // NOI18N
        interpreterLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterLabel.AccessibleContext.accessibleDescription")); // NOI18N
        interpreterTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterTextField.AccessibleContext.accessibleName")); // NOI18N
        interpreterTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterTextField.AccessibleContext.accessibleDescription")); // NOI18N
        interpreterBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        interpreterBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        defaultInterpreterCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.defaultInterpreterCheckBox.AccessibleContext.accessibleName")); // NOI18N
        defaultInterpreterCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.defaultInterpreterCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        configureButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.configureButton.AccessibleContext.accessibleName")); // NOI18N
        configureButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.configureButton.AccessibleContext.accessibleDescription")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.argsLabel.AccessibleContext.accessibleName")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.argsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.argsTextField.AccessibleContext.accessibleName")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.argsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsCombo.AccessibleContext.accessibleName")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsCombo.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        hintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.hintLabel.AccessibleContext.accessibleName")); // NOI18N
        hintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.hintLabel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        Utils.showGeneralOptionsPanel();
    }//GEN-LAST:event_configureButtonActionPerformed

    private void indexFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        Utils.browseSourceFile(project, indexFileTextField);
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    @NbBundle.Messages("RunAsScript.interpreter.browse.title=Select PHP Interpreter")
    private void interpreterBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_interpreterBrowseButtonActionPerformed
        File file = Utils.browseFileAction(LastUsedFolders.PHP_INTERPRETER, Bundle.RunAsScript_interpreter_browse_title());
        if (file != null) {
            interpreterTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_interpreterBrowseButtonActionPerformed

    private void workDirBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_workDirBrowseButtonActionPerformed
        File curDir;
        String workDir = createRunConfig().getWorkDir();
        if (StringUtils.hasText(workDir)) {
            curDir = new File(workDir);
        } else {
            curDir = FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project));
        }
        File selectedFile = new FileChooserBuilder(RunAsScript.class)
                .forceUseOfDefaultWorkingDirectory(true)
                .setTitle(NbBundle.getMessage(RunAsScript.class, "LBL_SelectWorkingDirectory"))
                .setDirectoriesOnly(true)
                .setDefaultWorkingDirectory(curDir)
                .showOpenDialog();
        if (selectedFile != null) {
            workDirTextField.setText(FileUtil.normalizeFile(selectedFile).getAbsolutePath());
        }
    }//GEN-LAST:event_workDirBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel argsLabel;
    private JTextField argsTextField;
    private JButton configureButton;
    private JCheckBox defaultInterpreterCheckBox;
    private JLabel hintLabel;
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JButton interpreterBrowseButton;
    private JLabel interpreterLabel;
    private JTextField interpreterTextField;
    private JLabel phpOptionsLabel;
    private JTextField phpOptionsTextField;
    private JComboBox<String> runAsCombo;
    private JLabel runAsLabel;
    private JButton workDirBrowseButton;
    private JLabel workDirLabel;
    private JTextField workDirTextField;
    // End of variables declaration//GEN-END:variables
}
