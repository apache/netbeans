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
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.php.analysis.commands.MessDetector;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.options.ValidatorMessDetectorParameter;
import org.netbeans.modules.php.analysis.ui.AnalysisDefaultDocumentListener;
import org.netbeans.modules.php.analysis.ui.MessDetectorRuleSetsListCellRenderer;
import org.netbeans.modules.php.analysis.ui.MessDetectorRuleSetsListModel;
import org.netbeans.modules.php.analysis.util.AnalysisUiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class MessDetectorOptionsPanel extends AnalysisCategoryPanel {

    private static final long serialVersionUID = -8206936666925671148L;

    private final MessDetectorRuleSetsListModel ruleSetsListModel = new MessDetectorRuleSetsListModel();
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public MessDetectorOptionsPanel() {
        initComponents();

        init();
    }

    private void init() {
        DocumentListener defaultDocumentListener = new AnalysisDefaultDocumentListener(() -> fireChange());
        initMessDetector(defaultDocumentListener);
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "# {1} - long script name",
        "MessDetectorOptionsPanel.hint=Full path of Mess Detector script (typically {0} or {1}).",
    })
    private void initMessDetector(DocumentListener defaultDocumentListener) {
        messDetectorHintLabel.setText(Bundle.MessDetectorOptionsPanel_hint(MessDetector.NAME, MessDetector.LONG_NAME));

        // listeners
        messDetectorTextField.getDocument().addDocumentListener(defaultDocumentListener);
        messDetectorRuleSetsList.addListSelectionListener(new DefaultListSelectionListener());
        messDetectorRuleSetFileTextField.getDocument().addDocumentListener(defaultDocumentListener);
        messDetectorOptionsTextField.getDocument().addDocumentListener(defaultDocumentListener);

        // rulesets
        messDetectorRuleSetsList.setModel(ruleSetsListModel);
        messDetectorRuleSetsList.setCellRenderer(new MessDetectorRuleSetsListCellRenderer(messDetectorRuleSetsList.getCellRenderer()));
    }

    public String getMessDetectorPath() {
        return messDetectorTextField.getText();
    }

    public void setMessDetectorPath(String path) {
        messDetectorTextField.setText(path);
    }

    public List<String> getMessDetectorRuleSets() {
        return getSelectedRuleSets();
    }

    public void setMessDetectorRuleSets(List<String> ruleSets) {
        selectRuleSets(ruleSets);
    }

    public String getMessDetectorRuleSetFilePath() {
        return messDetectorRuleSetFileTextField.getText().trim();
    }

    private void setMessDetectorRuleSetFilePath(String path) {
        messDetectorRuleSetFileTextField.setText(path);
    }

    public String getMessDetectorOptions() {
        return messDetectorOptionsTextField.getText().trim();
    }

    private void setMessDetectorOptions(String options) {
        messDetectorOptionsTextField.setText(options);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    List<String> getSelectedRuleSets() {
        return messDetectorRuleSetsList.getSelectedValuesList();
    }

    void selectRuleSets(List<String> ruleSets) {
        messDetectorRuleSetsList.clearSelection();
        for (String ruleSet : ruleSets) {
            int indexOf = MessDetectorRuleSetsListModel.getAllRuleSets().indexOf(ruleSet);
            assert indexOf != -1 : "Rule set not found: " + ruleSet; // NOI18N
            messDetectorRuleSetsList.addSelectionInterval(indexOf, indexOf);
        }
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    @NbBundle.Messages("MessDetectorOptionsPanel.category.name=Mess Detector")
    @Override
    public String getCategoryName() {
        return Bundle.MessDetectorOptionsPanel_category_name();
    }

    @Override
    public void update() {
        AnalysisOptions analysisOptions = AnalysisOptions.getInstance();
        setMessDetectorPath(analysisOptions.getMessDetectorPath());
        setMessDetectorRuleSets(analysisOptions.getMessDetectorRuleSets());
        setMessDetectorRuleSetFilePath(analysisOptions.getMessDetectorRuleSetFilePath());
        setMessDetectorOptions(analysisOptions.getMessDetectorOptions());
    }

    @Override
    public void applyChanges() {
        AnalysisOptions analysisOptions = AnalysisOptions.getInstance();
        analysisOptions.setMessDetectorPath(getMessDetectorPath());
        analysisOptions.setMessDetectorRuleSets(getMessDetectorRuleSets());
        analysisOptions.setMessDetectorRuleSetFilePath(getMessDetectorRuleSetFilePath());
        analysisOptions.setMessDetectorOptions(getMessDetectorOptions());
    }

    @Override
    public boolean isChanged() {
        String saved = AnalysisOptions.getInstance().getMessDetectorPath();
        String current = getMessDetectorRuleSetFilePath();
        if(saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = AnalysisOptions.getInstance().getMessDetectorRuleSetFilePath();
        current = getMessDetectorRuleSetFilePath().trim();
        if(saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = AnalysisOptions.getInstance().getMessDetectorOptions();
        current = getMessDetectorOptions().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        return !AnalysisOptions.getInstance().getMessDetectorRuleSets().equals(getMessDetectorRuleSets());
    }

    @Override
    public ValidationResult getValidationResult() {
        return new AnalysisOptionsValidator()
                .validateMessDetector(ValidatorMessDetectorParameter.create(this))
                .getResult();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messDetectorLabel = new JLabel();
        messDetectorTextField = new JTextField();
        messDetectorBrowseButton = new JButton();
        messDetectorSearchButton = new JButton();
        messDetectorHintLabel = new JLabel();
        messDetectorRuleSetsLabel = new JLabel();
        messDetectorRuleSetsScrollPane = new JScrollPane();
        messDetectorRuleSetsList = new JList<>();
        noteLabel = new JLabel();
        minVersionInfoLabel = new JLabel();
        messDetectorLearnMoreLabel = new JLabel();
        messDetectorRuleSetFileTextField = new JTextField();
        messDetectorRuleSetFileLabel = new JLabel();
        messDetectorRuleSetFileBrowseButton = new JButton();
        messDetectorOptionsTextField = new JTextField();
        messDetectorOptionsLabel = new JLabel();

        messDetectorLabel.setLabelFor(messDetectorTextField);
        Mnemonics.setLocalizedText(messDetectorLabel, NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.messDetectorLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(messDetectorBrowseButton, NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.messDetectorBrowseButton.text")); // NOI18N
        messDetectorBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                messDetectorBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(messDetectorSearchButton, NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.messDetectorSearchButton.text")); // NOI18N
        messDetectorSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                messDetectorSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(messDetectorHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(messDetectorRuleSetsLabel, NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.messDetectorRuleSetsLabel.text")); // NOI18N

        messDetectorRuleSetsScrollPane.setViewportView(messDetectorRuleSetsList);

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(minVersionInfoLabel, NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.minVersionInfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(messDetectorLearnMoreLabel, NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.messDetectorLearnMoreLabel.text")); // NOI18N
        messDetectorLearnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                messDetectorLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                messDetectorLearnMoreLabelMousePressed(evt);
            }
        });

        messDetectorRuleSetFileLabel.setLabelFor(messDetectorRuleSetFileTextField);
        Mnemonics.setLocalizedText(messDetectorRuleSetFileLabel, NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.messDetectorRuleSetFileLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(messDetectorRuleSetFileBrowseButton, NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.messDetectorRuleSetFileBrowseButton.text")); // NOI18N
        messDetectorRuleSetFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                messDetectorRuleSetFileBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(messDetectorOptionsLabel, NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.messDetectorOptionsLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(minVersionInfoLabel)
                    .addComponent(messDetectorLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(messDetectorLabel)
                    .addComponent(messDetectorRuleSetsLabel)
                    .addComponent(messDetectorRuleSetFileLabel)
                    .addComponent(messDetectorOptionsLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(messDetectorRuleSetsScrollPane, Alignment.LEADING)
                            .addComponent(messDetectorTextField))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(messDetectorBrowseButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(messDetectorSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(messDetectorHintLabel)
                        .addContainerGap())
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(messDetectorOptionsTextField, Alignment.LEADING)
                            .addComponent(messDetectorRuleSetFileTextField))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(messDetectorRuleSetFileBrowseButton)
                        .addGap(83, 83, 83))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {messDetectorBrowseButton, messDetectorSearchButton});

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(messDetectorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(messDetectorSearchButton)
                    .addComponent(messDetectorBrowseButton)
                    .addComponent(messDetectorLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(messDetectorHintLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(messDetectorRuleSetsLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(messDetectorRuleSetsScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                            .addComponent(messDetectorRuleSetFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(messDetectorRuleSetFileBrowseButton)
                            .addComponent(messDetectorRuleSetFileLabel))))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(messDetectorOptionsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(messDetectorOptionsLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(minVersionInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(messDetectorLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        messDetectorRuleSetFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MessDetectorOptionsPanel.class, "MessDetectorOptionsPanel.messDetectorRuleSetFileLabel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void messDetectorLearnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_messDetectorLearnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_messDetectorLearnMoreLabelMouseEntered

    private void messDetectorLearnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_messDetectorLearnMoreLabelMousePressed
        try {
            URL url = new URL("http://phpmd.org/"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_messDetectorLearnMoreLabelMousePressed

    private void messDetectorBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_messDetectorBrowseButtonActionPerformed
        File file = AnalysisUiUtils.browseMessDetector();
        if (file != null) {
            messDetectorTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_messDetectorBrowseButtonActionPerformed

    private void messDetectorSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_messDetectorSearchButtonActionPerformed
        String messDetector = AnalysisUiUtils.searchMessDetector();
        if (messDetector != null) {
            messDetectorTextField.setText(messDetector);
        }
    }//GEN-LAST:event_messDetectorSearchButtonActionPerformed

    private void messDetectorRuleSetFileBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_messDetectorRuleSetFileBrowseButtonActionPerformed
         File file = AnalysisUiUtils.browseMessDetectorRuleSet();
        if (file != null) {
            messDetectorRuleSetFileTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_messDetectorRuleSetFileBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton messDetectorBrowseButton;
    private JLabel messDetectorHintLabel;
    private JLabel messDetectorLabel;
    private JLabel messDetectorLearnMoreLabel;
    private JLabel messDetectorOptionsLabel;
    private JTextField messDetectorOptionsTextField;
    private JButton messDetectorRuleSetFileBrowseButton;
    private JLabel messDetectorRuleSetFileLabel;
    private JTextField messDetectorRuleSetFileTextField;
    private JLabel messDetectorRuleSetsLabel;
    private JList<String> messDetectorRuleSetsList;
    private JScrollPane messDetectorRuleSetsScrollPane;
    private JButton messDetectorSearchButton;
    private JTextField messDetectorTextField;
    private JLabel minVersionInfoLabel;
    private JLabel noteLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            fireChange();
        }

    }

}
