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

package org.netbeans.modules.php.analysis.ui.options;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.analysis.commands.CodingStandardsFixer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.options.ValidatorCodingStandardsFixerParameter;
import org.netbeans.modules.php.analysis.ui.AnalysisDefaultDocumentListener;
import org.netbeans.modules.php.analysis.util.AnalysisUiUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class CodingStandardsFixerOptionsPanel extends AnalysisCategoryPanel {

    private static final long serialVersionUID = -1267197272368337313L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public CodingStandardsFixerOptionsPanel() {
        initComponents();
        init();
    }

    private void init() {
        DocumentListener defaultDocumentListener = new AnalysisDefaultDocumentListener(() -> fireChange());
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

    @Override
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
        return new AnalysisOptionsValidator()
                .validateCodingStandardsFixer(ValidatorCodingStandardsFixerParameter.create(this))
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

        codingStandardsFixerLabel = new JLabel();
        codingStandardsFixerTextField = new JTextField();
        codingStandardsFixerBrowseButton = new JButton();
        codingStandardsFixerSearchButton = new JButton();
        codingStandardsFixerHintLabel = new JLabel();
        codingStandardsFixerVersionLabel = new JLabel();
        codingStandardsFixerVersionComboBox = new JComboBox<>();
        codingStandardsFixerLevelLabel = new JLabel();
        codingStandardsFixerLevelComboBox = new JComboBox<>();
        codingStandardsFixerConfigLabel = new JLabel();
        codingStandardsFixerConfigComboBox = new JComboBox<>();
        codingStandardsFixerOptionsLabel = new JLabel();
        codingStandardsFixerOptionsTextField = new JTextField();
        noteLabel = new JLabel();
        codingStandardsFixerLearnMoreLabel = new JLabel();

        codingStandardsFixerLabel.setLabelFor(codingStandardsFixerTextField);
        Mnemonics.setLocalizedText(codingStandardsFixerLabel, NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerLabel.text")); // NOI18N

        codingStandardsFixerTextField.setText(NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerTextField.text")); // NOI18N

        Mnemonics.setLocalizedText(codingStandardsFixerBrowseButton, NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerBrowseButton.text")); // NOI18N
        codingStandardsFixerBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                codingStandardsFixerBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(codingStandardsFixerSearchButton, NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerSearchButton.text")); // NOI18N
        codingStandardsFixerSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                codingStandardsFixerSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(codingStandardsFixerHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(codingStandardsFixerVersionLabel, NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerVersionLabel.text")); // NOI18N

        codingStandardsFixerVersionComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                codingStandardsFixerVersionComboBoxActionPerformed(evt);
            }
        });

        codingStandardsFixerLevelLabel.setLabelFor(codingStandardsFixerLevelComboBox);
        Mnemonics.setLocalizedText(codingStandardsFixerLevelLabel, NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerLevelLabel.text")); // NOI18N

        codingStandardsFixerConfigLabel.setLabelFor(codingStandardsFixerConfigComboBox);
        Mnemonics.setLocalizedText(codingStandardsFixerConfigLabel, NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerConfigLabel.text")); // NOI18N

        codingStandardsFixerOptionsLabel.setLabelFor(codingStandardsFixerOptionsTextField);
        Mnemonics.setLocalizedText(codingStandardsFixerOptionsLabel, NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerOptionsLabel.text")); // NOI18N

        codingStandardsFixerOptionsTextField.setText(NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerOptionsTextField.text")); // NOI18N

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(codingStandardsFixerLearnMoreLabel, NbBundle.getMessage(CodingStandardsFixerOptionsPanel.class, "CodingStandardsFixerOptionsPanel.codingStandardsFixerLearnMoreLabel.text")); // NOI18N
        codingStandardsFixerLearnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                codingStandardsFixerLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                codingStandardsFixerLearnMoreLabelMousePressed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(codingStandardsFixerLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(codingStandardsFixerLabel)
                    .addComponent(codingStandardsFixerLevelLabel)
                    .addComponent(codingStandardsFixerConfigLabel)
                    .addComponent(codingStandardsFixerOptionsLabel)
                    .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(codingStandardsFixerVersionLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(codingStandardsFixerVersionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(codingStandardsFixerConfigComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(codingStandardsFixerLevelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(codingStandardsFixerHintLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(codingStandardsFixerOptionsTextField, GroupLayout.Alignment.LEADING)
                            .addComponent(codingStandardsFixerTextField))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codingStandardsFixerBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codingStandardsFixerSearchButton))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {codingStandardsFixerBrowseButton, codingStandardsFixerSearchButton});

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandardsFixerLabel)
                    .addComponent(codingStandardsFixerTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(codingStandardsFixerSearchButton)
                    .addComponent(codingStandardsFixerBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codingStandardsFixerHintLabel)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandardsFixerVersionLabel)
                    .addComponent(codingStandardsFixerVersionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandardsFixerLevelLabel)
                    .addComponent(codingStandardsFixerLevelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandardsFixerConfigComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(codingStandardsFixerConfigLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codingStandardsFixerOptionsLabel)
                    .addComponent(codingStandardsFixerOptionsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codingStandardsFixerLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void codingStandardsFixerBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_codingStandardsFixerBrowseButtonActionPerformed
        File file = AnalysisUiUtils.browseCodingStandardsFixer();
        if (file != null) {
            codingStandardsFixerTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_codingStandardsFixerBrowseButtonActionPerformed

    private void codingStandardsFixerSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_codingStandardsFixerSearchButtonActionPerformed
        String codingStandardsFixer = AnalysisUiUtils.searchCodingStandardsFixer();
        if (codingStandardsFixer != null) {
            codingStandardsFixerTextField.setText(codingStandardsFixer);
        }
    }//GEN-LAST:event_codingStandardsFixerSearchButtonActionPerformed

    private void codingStandardsFixerLearnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_codingStandardsFixerLearnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_codingStandardsFixerLearnMoreLabelMouseEntered

    private void codingStandardsFixerLearnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_codingStandardsFixerLearnMoreLabelMousePressed
        try {
            URL url = new URL("https://github.com/FriendsOfPHP/PHP-CS-Fixer"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_codingStandardsFixerLearnMoreLabelMousePressed

    private void codingStandardsFixerVersionComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_codingStandardsFixerVersionComboBoxActionPerformed
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
    private JButton codingStandardsFixerBrowseButton;
    private JComboBox<String> codingStandardsFixerConfigComboBox;
    private JLabel codingStandardsFixerConfigLabel;
    private JLabel codingStandardsFixerHintLabel;
    private JLabel codingStandardsFixerLabel;
    private JLabel codingStandardsFixerLearnMoreLabel;
    private JComboBox<String> codingStandardsFixerLevelComboBox;
    private JLabel codingStandardsFixerLevelLabel;
    private JLabel codingStandardsFixerOptionsLabel;
    private JTextField codingStandardsFixerOptionsTextField;
    private JButton codingStandardsFixerSearchButton;
    private JTextField codingStandardsFixerTextField;
    private JComboBox<String> codingStandardsFixerVersionComboBox;
    private JLabel codingStandardsFixerVersionLabel;
    private JLabel noteLabel;
    // End of variables declaration//GEN-END:variables
}
