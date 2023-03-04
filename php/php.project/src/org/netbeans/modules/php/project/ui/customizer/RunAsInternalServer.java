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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.runconfigs.RunConfigInternal;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigInternalValidator;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Configuration panel for internal web server.
 */
public class RunAsInternalServer extends RunAsPanel.InsidePanel {

    private static final long serialVersionUID = -83545468177742547L;

    private final PhpProjectProperties properties;
    private final PhpProject project;
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    final ProjectCustomizer.Category category;


    public RunAsInternalServer(PhpProjectProperties properties, ConfigManager manager, ProjectCustomizer.Category category) {
        super(manager);
        this.properties = properties;
        this.category = category;
        project = properties.getProject();

        initComponents();

        this.labels = new JLabel[] {
            hostnameLabel,
            portLabel,
            routerLabel
        };
        this.textFields = new JTextField[] {
            hostnameTextField,
            portTextField,
            routerTextField
        };
        this.propertyNames = new String[] {
            PhpProjectProperties.HOSTNAME,
            PhpProjectProperties.PORT,
            PhpProjectProperties.ROUTER
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }
    }

    @Override
    protected RunAsType getRunAsType() {
        return RunConfigInternal.getRunAsType();
    }

    @Override
    protected String getDisplayName() {
        return RunConfigInternal.getDisplayName();
    }

    @Override
    protected JLabel getRunAsLabel() {
        return runAsLabel;
    }

    @Override
    protected JComboBox<String> getRunAsCombo() {
        return runAsComboBox;
    }

    @Override
    protected void loadFields() {
        // XXX remove index.file from properties, so run/debug project can then work
        putValue(PhpProjectProperties.INDEX_FILE, null);
        for (int i = 0; i < textFields.length; i++) {
            String value = getValue(propertyNames[i]);
            if (value == null) {
                if (PhpProjectProperties.HOSTNAME.equals(propertyNames[i])) {
                    value = RunConfigInternal.DEFAULT_HOSTNAME;
                } else if (PhpProjectProperties.PORT.equals(propertyNames[i])) {
                    value = String.valueOf(RunConfigInternal.DEFAULT_PORT);
                }
            }
            textFields[i].setText(value);
        }
    }

    @Override
    protected void validateFields() {
        category.setErrorMessage(RunConfigInternalValidator.validateCustomizer(createRunConfig()));
        // #148957 always allow to save customizer
        category.setValid(true);
    }

    private RunConfigInternal createRunConfig() {
        return RunConfigInternal.create()
                .setHostname(hostnameTextField.getText())
                .setPort(portTextField.getText())
                .setWorkDir(getSources())
                .setDocumentRoot(getWebRoot())
                .setRouterRelativePath(routerTextField.getText());
    }

    private File getSources() {
        return FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project));
    }

    private File getWebRoot() {
        return ProjectPropertiesSupport.getSourceSubdirectory(project, properties.getWebRoot());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        runAsLabel = new JLabel();
        runAsComboBox = new JComboBox<String>();
        hostnameLabel = new JLabel();
        hostnameTextField = new JTextField();
        portLabel = new JLabel();
        portTextField = new JTextField();
        urlHintLabel = new JLabel();
        routerLabel = new JLabel();
        routerTextField = new JTextField();
        routerBrowseButton = new JButton();
        noteLabel = new JLabel();
        phpVersionInfoLabel = new JLabel();

        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsInternalServer.class, "RunAsInternalServer.runAsLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(hostnameLabel, NbBundle.getMessage(RunAsInternalServer.class, "RunAsInternalServer.hostnameLabel.text")); // NOI18N

        hostnameTextField.setColumns(20);

        Mnemonics.setLocalizedText(portLabel, NbBundle.getMessage(RunAsInternalServer.class, "RunAsInternalServer.portLabel.text")); // NOI18N

        portTextField.setColumns(20);

        Mnemonics.setLocalizedText(urlHintLabel, " "); // NOI18N

        Mnemonics.setLocalizedText(routerLabel, NbBundle.getMessage(RunAsInternalServer.class, "RunAsInternalServer.routerLabel.text")); // NOI18N

        routerTextField.setColumns(20);

        Mnemonics.setLocalizedText(routerBrowseButton, NbBundle.getMessage(RunAsInternalServer.class, "RunAsInternalServer.routerBrowseButton.text")); // NOI18N
        routerBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                routerBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(RunAsInternalServer.class, "RunAsInternalServer.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpVersionInfoLabel, NbBundle.getMessage(RunAsInternalServer.class, "RunAsInternalServer.phpVersionInfoLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(routerLabel)
                    .addComponent(portLabel)
                    .addComponent(runAsLabel)
                    .addComponent(hostnameLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(routerTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(routerBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(urlHintLabel)
                        .addContainerGap())
                    .addComponent(runAsComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(hostnameTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(portTextField, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(phpVersionInfoLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(runAsLabel)
                    .addComponent(runAsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(hostnameLabel)
                    .addComponent(hostnameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(urlHintLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(routerLabel)
                    .addComponent(routerTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(routerBrowseButton))
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(phpVersionInfoLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void routerBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_routerBrowseButtonActionPerformed
        try {
            Utils.browseFolderFile(PhpVisibilityQuery.forProject(project), getSources(), routerTextField);
        } catch (FileNotFoundException ex) {
            // cannot happen for sources
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_routerBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel hostnameLabel;
    private JTextField hostnameTextField;
    private JLabel noteLabel;
    private JLabel phpVersionInfoLabel;
    private JLabel portLabel;
    private JTextField portTextField;
    private JButton routerBrowseButton;
    private JLabel routerLabel;
    private JTextField routerTextField;
    private JComboBox<String> runAsComboBox;
    private JLabel runAsLabel;
    private JLabel urlHintLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
        }

        @Override
        protected final String getDefaultValue() {
            return RunAsInternalServer.this.getDefaultValue(getPropName());
        }

        @Override
        protected void processUpdate() {
            super.processUpdate();
            updateAndSetUrl();
        }

        // update hint & store project url as well (for running/debugging the project)
        private void updateAndSetUrl() {
            String url = createRunConfig().getUrlHint();
            urlHintLabel.setText(url != null ? "<html><body>" + url : " "); // NOI18N
            putValue(PhpProjectProperties.URL, url);
        }
    }

}
