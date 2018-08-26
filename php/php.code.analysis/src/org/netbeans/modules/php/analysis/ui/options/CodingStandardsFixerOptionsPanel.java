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

package org.netbeans.modules.php.analysis.ui.options;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.analysis.commands.CodingStandardsFixer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class CodingStandardsFixerOptionsPanel extends AnalysisCategoryPanel {

    private static final long serialVersionUID = 4988988248465791859L;
    private static final String CODING_STANDARDS_FIXER_LAST_FOLDER_SUFFIX = ".codingStandarsFixer"; // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public CodingStandardsFixerOptionsPanel() {
        initComponents();
        init();
    }

    private void init() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        initCodingStandardsFixer(defaultDocumentListener);
        codingStandardsFixerVersionComboBox.setModel(new DefaultComboBoxModel<>(CodingStandardsFixer.VERSIONS.toArray(new String[0])));
        codingStandardsFixerLevelComboBox.setModel(new DefaultComboBoxModel<>(CodingStandardsFixer.ALL_LEVEL.toArray(new String[0])));
        codingStandardsFixerConfigComboBox.setModel(new DefaultComboBoxModel<>(CodingStandardsFixer.ALL_CONFIG.toArray(new String[0])));
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "# {1} - long script name",
        "CodingStandardsFixerOptionsPanel.hint=Full path of Coding Standards Fixer script (typically {0} or {1}).",
    })
    private void initCodingStandardsFixer(DocumentListener defaultDocumentListener) {
        codingStandardsFixerHintLabel.setText(Bundle.CodingStandardsFixerOptionsPanel_hint(CodingStandardsFixer.NAME, CodingStandardsFixer.LONG_NAME));
        codingStandardsFixerLevelComboBox.setModel(new DefaultComboBoxModel<>());

        // listeners
        codingStandardsFixerTextField.getDocument().addDocumentListener(defaultDocumentListener);
        codingStandardsFixerOptionsTextField.getDocument().addDocumentListener(defaultDocumentListener);
        ActionListener defaultAL = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireChange();
            }
        };
        codingStandardsFixerLevelComboBox.addActionListener(defaultAL);
        codingStandardsFixerConfigComboBox.addActionListener(defaultAL);
    }

    public String getCodingStandardsFixerPath() {
        return codingStandardsFixerTextField.getText();
    }

    public void setCodingStandardsFixerPath(String path) {
        codingStandardsFixerTextField.setText(path);
    }

    public String getCodingStandardsFixerVersion() {
        return (String) codingStandardsFixerVersionComboBox.getSelectedItem();
    }

    public void setCodingStandardsFixerVersion(String version) {
        codingStandardsFixerVersionComboBox.setSelectedItem(version);
    }

    @CheckForNull
    public String getCodingStandardsFixerLevel() {
        return (String) codingStandardsFixerLevelComboBox.getSelectedItem();
    }

    public void setCodingStandardsFixerLevel(String level) {
        codingStandardsFixerLevelComboBox.setSelectedItem(level);
    }

    @CheckForNull
    public String getCodingStandardsFixerConfig() {
        return (String) codingStandardsFixerConfigComboBox.getSelectedItem();
    }

    public void setCodingStandardsFixerConfig(String config) {
        codingStandardsFixerConfigComboBox.setSelectedItem(config);
    }

    public String getCodingStandardsFixerOptions() {
        return codingStandardsFixerOptionsTextField.getText();
    }

    public void setCodingStandardsFixerOptoins(String options) {
        codingStandardsFixerOptionsTextField.setText(options);
    }

    @NbBundle.Messages("CodingStandardsFixerOptionsPanel.category.name=Coding Standards Fixer")
    @Override
    public String getCategoryName() {
        return Bundle.CodingStandardsFixerOptionsPanel_category_name();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void update() {
        AnalysisOptions analysisOptions = AnalysisOptions.getInstance();
        setCodingStandardsFixerVersion(analysisOptions.getCodingStandardsFixerVersion());
        setCodingStandardsFixerPath(analysisOptions.getCodingStandardsFixerPath());
        setCodingStandardsFixerLevel(analysisOptions.getCodingStandardsFixerLevel());
        setCodingStandardsFixerConfig(analysisOptions.getCodingStandardsFixerConfig());
        setCodingStandardsFixerOptoins(analysisOptions.getCodingStandardsFixerOptions());
    }

    @Override
    public void applyChanges() {
        AnalysisOptions analysisOptions = AnalysisOptions.getInstance();
        analysisOptions.setCodingStandardsFixerVersion(getCodingStandardsFixerVersion());
        analysisOptions.setCodingStandardsFixerPath(getCodingStandardsFixerPath());
        analysisOptions.setCodingStandardsFixerLevel(getCodingStandardsFixerLevel());
        analysisOptions.setCodingStandardsFixerConfig(getCodingStandardsFixerConfig());
        analysisOptions.setCodingStandardsFixerOptions(getCodingStandardsFixerOptions());
    }

    @Override
    public boolean isChanged() {
        String saved = AnalysisOptions.getInstance().getCodingStandardsFixerPath();
        String current = getCodingStandardsFixerPath().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = AnalysisOptions.getInstance().getCodingStandardsFixerVersion();
        current = getCodingStandardsFixerVersion();
        if (saved == null ? StringUtils.hasText(current) : !saved.equals(current)) {
            return true;
        }
        saved = AnalysisOptions.getInstance().getCodingStandardsFixerLevel();
        current = getCodingStandardsFixerLevel();
        if (saved == null ? StringUtils.hasText(current) : !saved.equals(current)) {
            return true;
        }
        saved = AnalysisOptions.getInstance().getCodingStandardsFixerConfig();
        current = getCodingStandardsFixerConfig();
        if (saved == null ? StringUtils.hasText(current) : !saved.equals(current)) {
            return true;
        }
        saved = AnalysisOptions.getInstance().getCodingStandardsFixerOptions();
        current = getCodingStandardsFixerOptions();
        return !saved.equals(current);
    }

    @Override
    public ValidationResult getValidationResult() {
        // TODO
        return new AnalysisOptionsValidator()
                .validateCodingStandardsFixer(getCodingStandardsFixerPath())
                .getResult();
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    private void setVersion1ComponentsVisible(boolean visible) {
        codingStandardsFixerLevelLabel.setVisible(visible);
        codingStandardsFixerLevelComboBox.setVisible(visible);
        codingStandardsFixerConfigLabel.setVisible(visible);
        codingStandardsFixerConfigComboBox.setVisible(visible);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        codingStandardsFixerLabel = new javax.swing.JLabel();
        codingStandardsFixerTextField = new javax.swing.JTextField();
        codingStandardsFixerBrowseButton = new javax.swing.JButton();
        codingStandardsFixerSearchButton = new javax.swing.JButton();
        codingStandardsFixerHintLabel = new javax.swing.JLabel();
        codingStandardsFixerVersionLabel = new javax.swing.JLabel();
        codingStandardsFixerVersionComboBox = new javax.swing.JComboBox<>();
        codingStandardsFixerLevelLabel = new javax.swing.JLabel();
        codingStandardsFixerLevelComboBox = new javax.swing.JComboBox<>();
        codingStandardsFixerConfigLabel = new javax.swing.JLabel();
        codingStandardsFixerConfigComboBox = new javax.swing.JComboBox<>();
        codingStandardsFixerOptionsLabel = new javax.swing.JLabel();
        codingStandardsFixerOptionsTextField = new javax.swing.JTextField();
        noteLabel = new javax.swing.JLabel();
        codingStandardsFixerLearnMoreLabel = new javax.swing.JLabel();

        codingStandardsFixerLabel.setLabelFor(codingStandardsFixerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(codingStandardsFixerLabel, org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerLabel.text")); // NOI18N

        codingStandardsFixerTextField.setText(org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(codingStandardsFixerBrowseButton, org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerBrowseButton.text")); // NOI18N
        codingStandardsFixerBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codingStandardsFixerBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(codingStandardsFixerSearchButton, org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerSearchButton.text")); // NOI18N
        codingStandardsFixerSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codingStandardsFixerSearchButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(codingStandardsFixerHintLabel, "HINT"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(codingStandardsFixerVersionLabel, org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerVersionLabel.text")); // NOI18N

        codingStandardsFixerVersionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codingStandardsFixerVersionComboBoxActionPerformed(evt);
            }
        });

        codingStandardsFixerLevelLabel.setLabelFor(codingStandardsFixerLevelComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(codingStandardsFixerLevelLabel, org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerLevelLabel.text")); // NOI18N

        codingStandardsFixerConfigLabel.setLabelFor(codingStandardsFixerConfigComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(codingStandardsFixerConfigLabel, org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerConfigLabel.text")); // NOI18N

        codingStandardsFixerOptionsLabel.setLabelFor(codingStandardsFixerOptionsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(codingStandardsFixerOptionsLabel, org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerOptionsLabel.text")); // NOI18N

        codingStandardsFixerOptionsTextField.setText(org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerOptionsTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(noteLabel, org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.noteLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(codingStandardsFixerLearnMoreLabel, org.openide.util.NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerLearnMoreLabel.text")); // NOI18N
        codingStandardsFixerLearnMoreLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                codingStandardsFixerLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                codingStandardsFixerLearnMoreLabelMousePressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(codingStandardsFixerLearnMoreLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(codingStandardsFixerLabel)
                    .addComponent(codingStandardsFixerLevelLabel)
                    .addComponent(codingStandardsFixerConfigLabel)
                    .addComponent(codingStandardsFixerOptionsLabel)
                    .addComponent(noteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(codingStandardsFixerVersionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(codingStandardsFixerOptionsTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(codingStandardsFixerTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codingStandardsFixerBrowseButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codingStandardsFixerSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(codingStandardsFixerVersionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(codingStandardsFixerConfigComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(codingStandardsFixerLevelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(codingStandardsFixerHintLabel))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {codingStandardsFixerBrowseButton, codingStandardsFixerSearchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandardsFixerLabel)
                    .addComponent(codingStandardsFixerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(codingStandardsFixerSearchButton)
                    .addComponent(codingStandardsFixerBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codingStandardsFixerHintLabel)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandardsFixerVersionLabel)
                    .addComponent(codingStandardsFixerVersionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandardsFixerLevelLabel)
                    .addComponent(codingStandardsFixerLevelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandardsFixerConfigComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(codingStandardsFixerConfigLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandardsFixerOptionsLabel)
                    .addComponent(codingStandardsFixerOptionsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(noteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codingStandardsFixerLearnMoreLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CodingStandardsFixerOptionsPanel.browse.title=Select Coding Standards Fixer")
    private void codingStandardsFixerBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codingStandardsFixerBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CodingStandardsFixerOptionsPanel.class.getName() + CODING_STANDARDS_FIXER_LAST_FOLDER_SUFFIX)
                .setFilesOnly(true)
                .setTitle(Bundle.CodingStandardsFixerOptionsPanel_browse_title())
                .showOpenDialog();
        if (file != null) {
            codingStandardsFixerTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_codingStandardsFixerBrowseButtonActionPerformed

    @NbBundle.Messages({
        "CodingStandardsFixerOptionsPanel.search.title=Coding Standards Fixer scripts",
        "CodingStandardsFixerOptionsPanel.search.scripts=Coding Standards Fixer scripts:",
        "CodingStandardsFixerOptionsPanel.search.pleaseWaitPart=Coding Standards Fixer scripts",
        "CodingStandardsFixerOptionsPanel.search.notFound=No Coding Standards Fixer scripts found."
    })
    private void codingStandardsFixerSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codingStandardsFixerSearchButtonActionPerformed
        String codingStandardsFixer = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {

            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(CodingStandardsFixer.NAME, CodingStandardsFixer.LONG_NAME);
            }

            @Override
            public String getWindowTitle() {
                return Bundle.CodingStandardsFixerOptionsPanel_search_title();
            }

            @Override
            public String getListTitle() {
                return Bundle.CodingStandardsFixerOptionsPanel_search_scripts();
            }

            @Override
            public String getPleaseWaitPart() {
                return Bundle.CodingStandardsFixerOptionsPanel_search_pleaseWaitPart();
            }

            @Override
            public String getNoItemsFound() {
                return Bundle.CodingStandardsFixerOptionsPanel_search_notFound();
            }
        });
        if (codingStandardsFixer != null) {
            codingStandardsFixerTextField.setText(codingStandardsFixer);
        }
    }//GEN-LAST:event_codingStandardsFixerSearchButtonActionPerformed

    private void codingStandardsFixerLearnMoreLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_codingStandardsFixerLearnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_codingStandardsFixerLearnMoreLabelMouseEntered

    private void codingStandardsFixerLearnMoreLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_codingStandardsFixerLearnMoreLabelMousePressed
        try {
            URL url = new URL("https://github.com/FriendsOfPHP/PHP-CS-Fixer"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_codingStandardsFixerLearnMoreLabelMousePressed

    private void codingStandardsFixerVersionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codingStandardsFixerVersionComboBoxActionPerformed
        switch (getCodingStandardsFixerVersion()) {
            case "1": // NOI18N
                setVersion1ComponentsVisible(true);
                break;
            case "2": // NOI18N
                setVersion1ComponentsVisible(false);
                break;
            default:
                throw new AssertionError();
        }
    }//GEN-LAST:event_codingStandardsFixerVersionComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton codingStandardsFixerBrowseButton;
    private javax.swing.JComboBox<String> codingStandardsFixerConfigComboBox;
    private javax.swing.JLabel codingStandardsFixerConfigLabel;
    private javax.swing.JLabel codingStandardsFixerHintLabel;
    private javax.swing.JLabel codingStandardsFixerLabel;
    private javax.swing.JLabel codingStandardsFixerLearnMoreLabel;
    private javax.swing.JComboBox<String> codingStandardsFixerLevelComboBox;
    private javax.swing.JLabel codingStandardsFixerLevelLabel;
    private javax.swing.JLabel codingStandardsFixerOptionsLabel;
    private javax.swing.JTextField codingStandardsFixerOptionsTextField;
    private javax.swing.JButton codingStandardsFixerSearchButton;
    private javax.swing.JTextField codingStandardsFixerTextField;
    private javax.swing.JComboBox<String> codingStandardsFixerVersionComboBox;
    private javax.swing.JLabel codingStandardsFixerVersionLabel;
    private javax.swing.JLabel noteLabel;
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
            fireChange();
        }

    }

}
