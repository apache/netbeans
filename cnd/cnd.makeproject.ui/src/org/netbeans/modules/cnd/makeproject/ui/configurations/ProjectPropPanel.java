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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectUtils;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeContext;
import org.netbeans.modules.cnd.makeproject.ui.utils.DirectoryChooserInnerPanel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class ProjectPropPanel extends javax.swing.JPanel implements MakeContext.Savable {

    private final SourceRootChooser sourceRootChooser;
//    private TestRootChooser testRootChooser;
    private final Project project;
    private final MakeConfigurationDescriptor makeConfigurationDescriptor;
    private String originalEncoding;

    /** Creates new form ProjectPropPanel */
    public ProjectPropPanel(Project project, ConfigurationDescriptor configurationDescriptor) {
        this.project = project;
        makeConfigurationDescriptor = (MakeConfigurationDescriptor) configurationDescriptor;
        initComponents();
        projectTextField.setText(project.getProjectDirectory().getPath());
        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        sourceRootPanel.add(sourceRootChooser = new SourceRootChooser(
                configurationDescriptor.getBaseDir(),
                makeConfigurationDescriptor.getSourceRoots(),
                MakeProjectUtils.getSourceFileSystemHost(project)));
        ignoreFoldersTextField.setText(makeConfigurationDescriptor.getFolderVisibilityQuery().getRegEx());
//        if (makeConfigurationDescriptor.getActiveConfiguration() != null && makeConfigurationDescriptor.getActiveConfiguration().isMakefileConfiguration()) {
//            testRootPanel.add(testRootChooser = new TestRootChooser(configurationDescriptor.getBaseDir(), makeConfigurationDescriptor.getTestRoots()));
//        }
//        MakeCustomizerProviderImpl makeCustomizerProvider = project.getLookup().lookup(MakeCustomizerProviderImpl.class);
//        makeCustomizerProvider.addActionListener(this);

        originalEncoding = ((MakeProject) project).getSourceEncoding();
//        if (originalEncoding != null) {
//            try {
//                FileEncodingQuery.setDefaultEncoding(Charset.forName(value));
//            } catch (UnsupportedCharsetException e) {
//                //When the encoding is not supported by JVM do not set it as default
//            }
//        }
        if (originalEncoding == null) {
            originalEncoding = Charset.defaultCharset().name();
        }

        encoding.setModel(ProjectCustomizer.encodingModel(originalEncoding));
        encoding.setRenderer(ProjectCustomizer.encodingRenderer());

        encoding.addActionListener((ActionEvent arg0) -> {
            handleEncodingChange();
        });
    }

    private void handleEncodingChange() {
    }

    @Override
    public void save() {
        Charset enc = (Charset) encoding.getSelectedItem();
        String encName;
        if (enc != null) {
            encName = enc.name();
        } else {
            encName = originalEncoding;
        }
        ((MakeProject) project).setSourceEncoding(encName);

        makeConfigurationDescriptor.setSourceRoots(sourceRootChooser.getListData());
//        if (testRootChooser != null) {
//            makeConfigurationDescriptor.setTestRoots(testRootChooser.getListData());
//        }
        try {
            Pattern.compile(ignoreFoldersTextField.getText());
            makeConfigurationDescriptor.setFolderVisibilityQuery(ignoreFoldersTextField.getText());
        } catch (PatternSyntaxException ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ProjectPropPanel.class, "ProjectPropPanel.incorrectRegEx", ex.getMessage().trim()), NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    private static class SourceRootChooser extends DirectoryChooserInnerPanel {

        public SourceRootChooser(String baseDir, List<String> feed, ExecutionEnvironment env) {
            super(baseDir, feed, env);
            getCopyButton().setVisible(false);
            getEditButton().setVisible(false);
        }

        @Override
        public String getListLabelText() {
            return getString("ProjectPropPanel.sourceRootLabel.text");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("ProjectPropPanel.sourceRootLabel.mn").charAt(0);
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("ADD_BUTTON_MN").charAt(0);
        }

        @Override
        public String getAddButtonText() {
            return getString("ADD_BUTTON_TXT");
        }
    }

//    private static class TestRootChooser extends DirectoryChooserInnerPanel {
//
//        public TestRootChooser(String baseDir, List<String> feed) {
//            super(baseDir, feed);
//            getCopyButton().setVisible(false);
//            getEditButton().setVisible(false);
//        }
//
//        @Override
//        public String getListLabelText() {
//            return getString("ProjectPropPanel.testRootLabel.text");
//        }
//
//        @Override
//        public char getListLabelMnemonic() {
//            return getString("ProjectPropPanel.testRootLabel.mn").charAt(0);
//        }
//
//        @Override
//        public char getAddButtonMnemonics() {
//            return getString("ADD_BUTTON_MN").charAt(0);
//        }
//
//        @Override
//        public String getAddButtonText() {
//            return getString("ADD_BUTTON_TXT");
//        }
//    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        sourceRootPanel = new javax.swing.JPanel();
        ignoreFolderPanel = new javax.swing.JPanel();
        ignoreFoldersLabel = new javax.swing.JLabel();
        ignoreFoldersTextField = new javax.swing.JTextField();
        ignoreFoldersDefaultButton = new javax.swing.JButton();
        seeAlsoLabel = new javax.swing.JLabel();
        testRootPanel = new javax.swing.JPanel();
        encodingPanel = new javax.swing.JPanel();
        encodingLabel = new javax.swing.JLabel();
        encoding = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(ProjectPropPanel.class, "ProjectPropPanel.projectLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(projectLabel, gridBagConstraints);
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectPropPanel.class, "ProjectPropPanel.projectLabel.ad")); // NOI18N

        projectTextField.setEditable(false);
        projectTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(projectTextField, gridBagConstraints);

        sourceRootPanel.setBackground(new java.awt.Color(255, 255, 255));
        sourceRootPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(sourceRootPanel, gridBagConstraints);

        ignoreFolderPanel.setLayout(new java.awt.GridBagLayout());

        ignoreFoldersLabel.setLabelFor(ignoreFoldersTextField);
        org.openide.awt.Mnemonics.setLocalizedText(ignoreFoldersLabel, org.openide.util.NbBundle.getMessage(ProjectPropPanel.class, "ProjectPropPanel.ignoreFoldersLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ignoreFolderPanel.add(ignoreFoldersLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        ignoreFolderPanel.add(ignoreFoldersTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ignoreFoldersDefaultButton, org.openide.util.NbBundle.getMessage(ProjectPropPanel.class, "ProjectPropPanel.ignoreFoldersDefaultButton.text")); // NOI18N
        ignoreFoldersDefaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreFoldersDefaultButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        ignoreFolderPanel.add(ignoreFoldersDefaultButton, gridBagConstraints);

        seeAlsoLabel.setText(org.openide.util.NbBundle.getMessage(ProjectPropPanel.class, "ProjectPropPanel.seeAlsoLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        ignoreFolderPanel.add(seeAlsoLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(ignoreFolderPanel, gridBagConstraints);

        testRootPanel.setBackground(new java.awt.Color(255, 255, 255));
        testRootPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(testRootPanel, gridBagConstraints);

        encodingLabel.setLabelFor(encoding);
        org.openide.awt.Mnemonics.setLocalizedText(encodingLabel, org.openide.util.NbBundle.getMessage(ProjectPropPanel.class, "ProjectPropPanel.encodingLabel.text")); // NOI18N

        javax.swing.GroupLayout encodingPanelLayout = new javax.swing.GroupLayout(encodingPanel);
        encodingPanel.setLayout(encodingPanelLayout);
        encodingPanelLayout.setHorizontalGroup(
            encodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(encodingPanelLayout.createSequentialGroup()
                .addComponent(encodingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(encoding, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        encodingPanelLayout.setVerticalGroup(
            encodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(encodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(encodingLabel)
                .addComponent(encoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(encodingPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void ignoreFoldersDefaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreFoldersDefaultButtonActionPerformed
        // TODO add your handling code here:
        ignoreFoldersTextField.setText(MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN);
    }//GEN-LAST:event_ignoreFoldersDefaultButtonActionPerformed

    private void projectTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_projectTextFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox encoding;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JPanel encodingPanel;
    private javax.swing.JPanel ignoreFolderPanel;
    private javax.swing.JButton ignoreFoldersDefaultButton;
    private javax.swing.JLabel ignoreFoldersLabel;
    private javax.swing.JTextField ignoreFoldersTextField;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JLabel seeAlsoLabel;
    private javax.swing.JPanel sourceRootPanel;
    private javax.swing.JPanel testRootPanel;
    // End of variables declaration//GEN-END:variables

    private static String getString(String key) {
        return NbBundle.getMessage(ProjectPropPanel.class, key);
    }
}
