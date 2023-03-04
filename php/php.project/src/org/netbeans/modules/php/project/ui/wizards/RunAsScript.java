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
package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.runconfigs.RunConfigScript;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.RunAsPanel;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author  Radek Matous, Tomas Mysik
 */
public class RunAsScript extends RunAsPanel.InsidePanel {

    private static final long serialVersionUID = 8423354564321210L;

    final ChangeSupport changeSupport = new ChangeSupport(this);
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    private final SourcesFolderProvider sourcesFolderProvider;


    public RunAsScript(ConfigManager manager, SourcesFolderProvider sourcesFolderProvider) {
        super(manager);
        this.sourcesFolderProvider = sourcesFolderProvider;

        initComponents();

        addListeners();
        labels = new JLabel[] {
            indexFileLabel,
        };
        textFields = new JTextField[] {
            indexFileTextField,
        };
        propertyNames = new String[] {
            RunConfigurationPanel.INDEX_FILE,
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }
    }

    private void addListeners() {
        interpreterTextField.getDocument().addDocumentListener(new DocumentListener() {
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
                changeSupport.fireChange();
            }
        });
        runAsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    public void loadPhpInterpreter() {
        String phpInterpreter = PhpOptions.getInstance().getPhpInterpreter();
        interpreterTextField.setText(phpInterpreter != null ? phpInterpreter : ""); // NOI18N
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
        loadPhpInterpreter();
    }

    @Override
    protected void validateFields() {
        // validation is done in RunConfigurationPanel
        changeSupport.fireChange();
    }

    public void addRunAsScriptListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeRunAsScriptListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
        }

        @Override
        protected final String getDefaultValue() {
            return RunAsScript.this.getDefaultValue(getPropName());
        }
    }

    public RunConfigScript createRunConfig() {
        return RunConfigScript.create()
                .setUseDefaultInterpreter(false)
                .setInterpreter(interpreterTextField.getText().trim())
                .setIndexParentDir(sourcesFolderProvider.getSourcesFolder())
                .setIndexRelativePath(indexFileTextField.getText().trim());
    }

    public void setIndexFile(String indexFile) {
        indexFileTextField.setText(indexFile);
    }

    public void hideIndexFile() {
        indexFileLabel.setVisible(false);
        indexFileTextField.setVisible(false);
        indexFileBrowseButton.setVisible(false);
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
        runAsLabel = new JLabel();
        runAsCombo = new JComboBox<String>();
        configureButton = new JButton();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();

        interpreterLabel.setLabelFor(interpreterTextField);
        Mnemonics.setLocalizedText(interpreterLabel, NbBundle.getMessage(RunAsScript.class, "LBL_PhpInterpreter")); // NOI18N

        interpreterTextField.setEditable(false);
        interpreterTextField.setColumns(20);

        runAsLabel.setLabelFor(runAsCombo);
        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsScript.class, "LBL_RunAs")); // NOI18N

        Mnemonics.setLocalizedText(configureButton, NbBundle.getMessage(RunAsScript.class, "LBL_Configure")); // NOI18N
        configureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        indexFileLabel.setLabelFor(indexFileTextField);
        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsScript.class, "LBL_IndexFile")); // NOI18N

        indexFileTextField.setColumns(20);

        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsScript.class, "LBL_BrowseIndex")); // NOI18N
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });

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
                    .addComponent(indexFileLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(runAsCombo, Alignment.TRAILING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(indexFileTextField)
                            .addComponent(interpreterTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(configureButton, Alignment.TRAILING)
                            .addComponent(indexFileBrowseButton, Alignment.TRAILING)))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {configureButton, indexFileBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(runAsLabel)
                    .addComponent(runAsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(interpreterLabel)
                    .addComponent(interpreterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configureButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(indexFileLabel)
                    .addComponent(indexFileBrowseButton)
                    .addComponent(indexFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );

        interpreterLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterLabel.AccessibleContext.accessibleName")); // NOI18N
        interpreterLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterLabel.AccessibleContext.accessibleDescription")); // NOI18N
        interpreterTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterTextField.AccessibleContext.accessibleName")); // NOI18N
        interpreterTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterTextField.AccessibleContext.accessibleDescription")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsCombo.AccessibleContext.accessibleName")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsCombo.AccessibleContext.accessibleDescription")); // NOI18N
        configureButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.configureButton.AccessibleContext.accessibleName")); // NOI18N
        configureButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.configureButton.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        Utils.showGeneralOptionsPanel();
    }//GEN-LAST:event_configureButtonActionPerformed

    private void indexFileBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        try {
            Utils.browseFolderFile(PhpVisibilityQuery.getDefault(), sourcesFolderProvider.getSourcesFolder(), indexFileTextField);
        } catch (FileNotFoundException ex) {
            // cannot happen for sources
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton configureButton;
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JLabel interpreterLabel;
    private JTextField interpreterTextField;
    private JComboBox<String> runAsCombo;
    private JLabel runAsLabel;
    // End of variables declaration//GEN-END:variables

}
