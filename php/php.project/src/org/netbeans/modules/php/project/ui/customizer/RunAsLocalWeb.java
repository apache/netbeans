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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import org.netbeans.modules.php.project.connections.ConfigManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.runconfigs.RunConfigLocal;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigLocalValidator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * @author  Radek Matous, Tomas Mysik
 */
public class RunAsLocalWeb extends RunAsPanel.InsidePanel {

    private static final long serialVersionUID = 7891231321100L;

    private final PhpProjectProperties properties;
    private final PhpProject project;
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    final Category category;


    public RunAsLocalWeb(PhpProjectProperties properties, ConfigManager manager, Category category) {
        super(manager);
        this.properties = properties;
        this.category = category;
        project = properties.getProject();

        initComponents();
        this.labels = new JLabel[] {
            urlLabel,
            indexFileLabel,
            argsLabel
        };
        this.textFields = new JTextField[] {
            urlTextField,
            indexFileTextField,
            argsTextField
        };
        this.propertyNames = new String[] {
            PhpProjectProperties.URL,
            PhpProjectProperties.INDEX_FILE,
            PhpProjectProperties.ARGS
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }
    }

    @Override
    protected boolean isDefault() {
        return true;
    }

    @Override
    protected RunAsType getRunAsType() {
        return RunConfigLocal.getRunAsType();
    }

    @Override
    public String getDisplayName() {
        return RunConfigLocal.getDisplayName();
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
            textFields[i].setText(getValue(propertyNames[i]));
        }
    }

    @Override
    protected void validateFields() {
        category.setErrorMessage(RunConfigLocalValidator.validateCustomizer(createRunConfig()));
        // #148957 always allow to save customizer
        category.setValid(true);
    }

    private RunConfigLocal createRunConfig() {
        return RunConfigLocal.create()
                .setUrl(urlTextField.getText())
                .setIndexParentDir(getWebRoot())
                .setIndexRelativePath(indexFileTextField.getText())
                .setArguments(argsTextField.getText());
    }

    private File getWebRoot() {
        return ProjectPropertiesSupport.getSourceSubdirectory(project, properties.getWebRoot());
    }

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
        }

        @Override
        protected final String getDefaultValue() {
            return RunAsLocalWeb.this.getDefaultValue(getPropName());
        }

        @Override
        protected void processUpdate() {
            super.processUpdate();
            hintLabel.setText(createRunConfig().getUrlHint());
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

        runAsLabel = new JLabel();
        runAsCombo = new JComboBox<String>();
        urlLabel = new JLabel();
        urlTextField = new JTextField();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();
        argsLabel = new JLabel();
        argsTextField = new JTextField();
        hintLabel = new JTextPane();
        advancedButton = new JButton();

        runAsLabel.setLabelFor(runAsCombo);
        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_RunAs")); // NOI18N

        urlLabel.setLabelFor(urlTextField);
        Mnemonics.setLocalizedText(urlLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_ProjectUrl")); // NOI18N

        urlTextField.setColumns(20);

        indexFileLabel.setLabelFor(indexFileTextField);
        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_IndexFile")); // NOI18N

        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_Browse")); // NOI18N
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });

        argsLabel.setLabelFor(argsTextField);
        Mnemonics.setLocalizedText(argsLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_Arguments")); // NOI18N

        argsTextField.setColumns(20);

        hintLabel.setEditable(false);
        hintLabel.setBackground(UIManager.getDefaults().getColor("Label.background"));
        hintLabel.setBorder(null);
        hintLabel.setFocusable(false);

        Mnemonics.setLocalizedText(advancedButton, NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.advancedButton.text")); // NOI18N
        advancedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                advancedButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(advancedButton))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(argsLabel)
                    .addComponent(urlLabel)
                    .addComponent(indexFileLabel)
                    .addComponent(runAsLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(hintLabel)
                    .addComponent(argsTextField, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(indexFileTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(indexFileBrowseButton))
                    .addComponent(runAsCombo, Alignment.TRAILING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(urlTextField, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(runAsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(runAsLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(urlLabel)
                    .addComponent(urlTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                    .addComponent(indexFileBrowseButton)
                    .addComponent(indexFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(indexFileLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                    .addComponent(argsLabel)
                    .addComponent(argsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(hintLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(advancedButton))
        );

        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.runAsCombo.AccessibleContext.accessibleName")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.runAsCombo.AccessibleContext.accessibleDescription")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlLabel.AccessibleContext.accessibleName")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlTextField.AccessibleContext.accessibleName")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.argsLabel.AccessibleContext.accessibleName")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.argsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.argsTextField.AccessibleContext.accessibleName")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.argsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        hintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.jTextPane1.AccessibleContext.accessibleName")); // NOI18N
        hintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.jTextPane1.AccessibleContext.accessibleDescription")); // NOI18N
        advancedButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.advancedButton.AccessibleContext.accessibleName")); // NOI18N
        advancedButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.advancedButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("RunAsLocalWeb.webRoot.notFound=Web Root directory does not exist (see Sources).")
    private void indexFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        try {
            Utils.browseFolderFile(PhpVisibilityQuery.forProject(project), getWebRoot(), indexFileTextField);
        } catch (FileNotFoundException ex) {
            category.setErrorMessage(Bundle.RunAsLocalWeb_webRoot_notFound());
            category.setValid(true);
        }
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    private void advancedButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_advancedButtonActionPerformed
        RunAsWebAdvanced.Properties props = new RunAsWebAdvanced.Properties(
                getValue(PhpProjectProperties.DEBUG_URL),
                hintLabel.getText(),
                getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE),
                getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_LOCAL),
                getValue(PhpProjectProperties.DEBUG_PROXY_HOST),
                getValue(PhpProjectProperties.DEBUG_PROXY_PORT));
        RunAsWebAdvanced advanced = new RunAsWebAdvanced(project, props);
        if (advanced.open()) {
            Pair<String, String> pathMapping = advanced.getPathMapping();
            Pair<String, String> debugProxy = advanced.getDebugProxy();
            RunAsLocalWeb.this.putValue(PhpProjectProperties.DEBUG_URL, advanced.getDebugUrl().name());
            RunAsLocalWeb.this.putValue(PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE, pathMapping.first());
            RunAsLocalWeb.this.putValue(PhpProjectProperties.DEBUG_PATH_MAPPING_LOCAL, pathMapping.second());
            RunAsLocalWeb.this.putValue(PhpProjectProperties.DEBUG_PROXY_HOST, debugProxy.first());
            RunAsLocalWeb.this.putValue(PhpProjectProperties.DEBUG_PROXY_PORT, debugProxy.second());
        }
    }//GEN-LAST:event_advancedButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton advancedButton;
    private JLabel argsLabel;
    private JTextField argsTextField;
    private JTextPane hintLabel;
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JComboBox<String> runAsCombo;
    private JLabel runAsLabel;
    private JLabel urlLabel;
    private JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
