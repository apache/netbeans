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

/*
 * SmartyOptionsPanel.java
 *
 * Created on 11.6.2009, 13:02:10
 */

package org.netbeans.modules.php.smarty.ui.options;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.netbeans.modules.php.smarty.SmartyFramework.ToggleCommentOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Martin Fousek
 */
@NbBundle.Messages({
    "SmartyOptionsPanel.keywords.template=template",
    "SmartyOptionsPanel.keywords.framework=framework",
    "SmartyOptionsPanel.keywords.TabTitle=Frameworks & Tools"
})
@OptionsPanelController.Keywords(
        keywords = {"php", "smarty", "framework", "template", "tpl",
            "#SmartyOptionsPanel.keywords.template", "#SmartyOptionsPanel.keywords.framework"},
        location = UiUtils.OPTIONS_PATH,
        tabTitle = "#SmartyOptionsPanel.keywords.TabTitle")
public class SmartyOptionsPanel extends JPanel {
    private static final long serialVersionUID = -1384644114740L;

    private final transient ChangeSupport changeSupport = new ChangeSupport(this);

    /** Maximum scanning depth which appears in options combo box. */
    private static final int MAX_SCANNING_DEPTH = 3;

    private static final String LINK_QUICK_START = "http://www.smarty.net/quick_install"; //NOI18N
    private boolean firstOpening = true;

    public SmartyOptionsPanel() {
        initComponents();
        initSmartyVersionsComboBox();

        errorLabel.setText(" "); // NOI18N

        // initialize
        setSmartyVersion(SmartyOptions.getInstance().getSmartyVersion());
        setOpenDelimiter(SmartyOptions.getInstance().getDefaultOpenDelimiter());
        setCloseDelimiter(SmartyOptions.getInstance().getDefaultCloseDelimiter());
        setToggleCommentOption(SmartyOptions.getInstance().getToggleCommentOption());

        smartyVersionComboBox.addActionListener(new SmartyActionListener());
        openDelimiterTextField.getDocument().addDocumentListener(new SmartyDocumentListener());
        closeDelimiterTextField.getDocument().addDocumentListener(new SmartyDocumentListener());
    }

    public String getCloseDelimiter() {
        return closeDelimiterTextField.getText();
    }

    public final void setCloseDelimiter(String closeDelimiter) {
        this.closeDelimiterTextField.setText(closeDelimiter);
    }

    public String getOpenDelimiter() {
        return openDelimiterTextField.getText();
    }

    public final void setOpenDelimiter(String openDelimiter) {
        this.openDelimiterTextField.setText(openDelimiter);
    }

    public final void setSmartyVersion(SmartyFramework.Version version) {
        smartyVersionComboBox.setSelectedItem(version);
    }

    public SmartyFramework.Version getSmartyVersion() {
        return (SmartyFramework.Version) smartyVersionComboBox.getSelectedItem();
    }

    private void setToggleCommentOption(ToggleCommentOption toggleCommentOption) {
        if (toggleCommentOption == SmartyFramework.ToggleCommentOption.SMARTY) {
            asSmartyRadioButton.setSelected(true);
        } else {
            perContextRadioButton.setSelected(true);
        }
    }

    private SmartyFramework.ToggleCommentOption getToggleCommentOption() {
        if (asSmartyRadioButton.isSelected()) {
            return SmartyFramework.ToggleCommentOption.SMARTY;
        } else {
            return SmartyFramework.ToggleCommentOption.CONTEXT;
        }
    }

    public void setError(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void setWarning(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    protected void update() {
        if(firstOpening || !changed()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            setSmartyVersion(getOptions().getSmartyVersion());
            setOpenDelimiter(getOptions().getDefaultOpenDelimiter());
            setCloseDelimiter(getOptions().getDefaultCloseDelimiter());
            setToggleCommentOption(getOptions().getToggleCommentOption());
        }
    }

    protected void applyChanges() {
        getOptions().setSmartyVersion(getSmartyVersion());
        getOptions().setDefaultOpenDelimiter(getOpenDelimiter());
        getOptions().setDefaultCloseDelimiter(getCloseDelimiter());
        getOptions().setToggleCommentOption(getToggleCommentOption());
    }

    protected void cancel() {
        if(changed()) { // if panel is modified by the user and options window closes, discard any changes
            setSmartyVersion(getOptions().getSmartyVersion());
            setOpenDelimiter(getOptions().getDefaultOpenDelimiter());
            setCloseDelimiter(getOptions().getDefaultCloseDelimiter());
            setToggleCommentOption(getOptions().getToggleCommentOption());
        }
    }

    protected boolean valid() {
        // warnings
        if (getOpenDelimiter().equals("") || getCloseDelimiter().equals("")) {  //NOI18N
            setError(NbBundle.getMessage(SmartyOptionsPanel.class, "WRN_EmptyDelimiterFields")); //NOI18N
            return false;
        }

        // everything ok
        setWarning(" "); // NOI18N
        return true;
    }

    protected boolean changed() {
        return !getOptions().getSmartyVersion().equals(getSmartyVersion())
                || !getOptions().getDefaultOpenDelimiter().equals(getOpenDelimiter().trim())
                || !getOptions().getDefaultCloseDelimiter().equals(getCloseDelimiter().trim())
                || !getOptions().getToggleCommentOption().equals(getToggleCommentOption());
    }

    private SmartyOptions getOptions() {
        return SmartyOptions.getInstance();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toggleCommentButtonGroup = new ButtonGroup();
        errorLabel = new JLabel();
        learnMoreLabel = new JLabel();
        installationInfoLabel = new JLabel();
        editorSettingsLabel = new JLabel();
        jSeparator1 = new JSeparator();
        smartyVersionLabel = new JLabel();
        smartyVersionComboBox = new JComboBox();
        openDelimiterLabel = new JLabel();
        openDelimiterTextField = new JTextField();
        closeDelimiterLabel = new JLabel();
        closeDelimiterTextField = new JTextField();
        toggleCommentLable = new JLabel();
        asSmartyRadioButton = new JRadioButton();
        perContextRadioButton = new JRadioButton();

        Mnemonics.setLocalizedText(errorLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.errorLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(learnMoreLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.learnMoreLabel.text")); // NOI18N
        learnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                learnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                learnMoreLabelMousePressed(evt);
            }
        });

        Mnemonics.setLocalizedText(installationInfoLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.installationInfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(editorSettingsLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "LBL_OptionsEditorPanel")); // NOI18N

        smartyVersionLabel.setDisplayedMnemonic('v');
        smartyVersionLabel.setLabelFor(smartyVersionComboBox);
        Mnemonics.setLocalizedText(smartyVersionLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.smartyVersionLabel.text")); // NOI18N

        openDelimiterLabel.setDisplayedMnemonic('O');
        openDelimiterLabel.setLabelFor(openDelimiterTextField);
        Mnemonics.setLocalizedText(openDelimiterLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.openDelimiterLabel.text")); // NOI18N

        closeDelimiterLabel.setDisplayedMnemonic('C');
        closeDelimiterLabel.setLabelFor(closeDelimiterTextField);
        Mnemonics.setLocalizedText(closeDelimiterLabel, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.closeDelimiterLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(toggleCommentLable, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.toggleCommentLable.text")); // NOI18N

        toggleCommentButtonGroup.add(asSmartyRadioButton);
        asSmartyRadioButton.setMnemonic('a');
        asSmartyRadioButton.setSelected(true);
        Mnemonics.setLocalizedText(asSmartyRadioButton, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.asSmartyRadioButton.text")); // NOI18N

        toggleCommentButtonGroup.add(perContextRadioButton);
        perContextRadioButton.setMnemonic('l');
        Mnemonics.setLocalizedText(perContextRadioButton, NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.perContextRadioButton.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(editorSettingsLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jSeparator1))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(errorLabel)
                    .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(installationInfoLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(openDelimiterLabel)
                            .addComponent(smartyVersionLabel)
                            .addComponent(toggleCommentLable))
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(asSmartyRadioButton)
                                .addGap(18, 18, 18)
                                .addComponent(perContextRadioButton, GroupLayout.PREFERRED_SIZE, 178, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(smartyVersionComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(openDelimiterTextField, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(closeDelimiterLabel)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(closeDelimiterTextField, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(editorSettingsLabel)
                    .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(smartyVersionLabel)
                    .addComponent(smartyVersionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(openDelimiterLabel)
                    .addComponent(openDelimiterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(closeDelimiterLabel)
                    .addComponent(closeDelimiterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(toggleCommentLable)
                    .addComponent(asSmartyRadioButton)
                    .addComponent(perContextRadioButton))
                .addPreferredGap(ComponentPlacement.RELATED, 248, Short.MAX_VALUE)
                .addComponent(installationInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(errorLabel))
        );

        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SmartyOptionsPanel.class, "SmartyOptionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void learnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_learnMoreLabelMouseEntered

    private void learnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMousePressed
        try {
            URL url = new URL(LINK_QUICK_START); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_learnMoreLabelMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JRadioButton asSmartyRadioButton;
    private JLabel closeDelimiterLabel;
    private JTextField closeDelimiterTextField;
    private JLabel editorSettingsLabel;
    private JLabel errorLabel;
    private JLabel installationInfoLabel;
    private JSeparator jSeparator1;
    private JLabel learnMoreLabel;
    private JLabel openDelimiterLabel;
    private JTextField openDelimiterTextField;
    private JRadioButton perContextRadioButton;
    private JComboBox smartyVersionComboBox;
    private JLabel smartyVersionLabel;
    private ButtonGroup toggleCommentButtonGroup;
    private JLabel toggleCommentLable;
    // End of variables declaration//GEN-END:variables

    private void initSmartyVersionsComboBox() {
        for (SmartyFramework.Version version : SmartyFramework.Version.values()) {
            smartyVersionComboBox.addItem(version);
        }
    }

    private final class SmartyDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }
        private void processUpdate() {
            fireChange();
        }
    }

    private final class SmartyActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            fireChange();
        }
    }

}
