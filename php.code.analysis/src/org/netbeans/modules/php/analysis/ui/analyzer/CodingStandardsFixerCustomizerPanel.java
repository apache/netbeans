/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.analysis.ui.analyzer;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.commands.CodingStandardsFixer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public class CodingStandardsFixerCustomizerPanel extends JPanel {

    private static final long serialVersionUID = -575231773758741767L;

    public static final String ENABLED = "codingStandardsFixer.enabled"; // NOI18N
    public static final String VERSION = "codingStandardsFixer.version"; // NOI18N
    public static final String LEVEL = "codingStandardsFixer.level"; // NOI18N
    public static final String CONFIG = "codingStandardsFixer.config"; // NOI18N
    public static final String OPTIONS = "codingStandardsFixer.options"; // NOI18N

    final Analyzer.CustomizerContext<Void, CodingStandardsFixerCustomizerPanel> context;
    final Preferences settings;


    public CodingStandardsFixerCustomizerPanel(Analyzer.CustomizerContext<Void, CodingStandardsFixerCustomizerPanel> context) {
        assert EventQueue.isDispatchThread();
        assert context != null;

        this.context = context;
        this.settings = context.getSettings();

        initComponents();
        init();
    }

    private void init() {
        setEnabledCheckBox();
        setVersionComboBox();
        setLevelComboBox();
        setConfigComboBox();
        setOptionsTextField();
    }

    private void setEnabledCheckBox() {
        assert EventQueue.isDispatchThread();
        enabledCheckBox.addItemListener((e) -> {
            setAllComponetsEnabled(enabledCheckBox.isSelected());
            setCodingStandardsFixerEnabled();
        });
        boolean isEnabled = settings.getBoolean(ENABLED, false);
        enabledCheckBox.setSelected(isEnabled);
        setAllComponetsEnabled(isEnabled);
    }

    private void setVersionComboBox() {
        assert EventQueue.isDispatchThread();
        DefaultComboBoxModel<String> versionComboBoxModel = new DefaultComboBoxModel<>();
        CodingStandardsFixer.VERSIONS.forEach((version) -> {
            versionComboBoxModel.addElement(version);
        });
        versionComboBox.setModel(versionComboBoxModel);
        versionComboBox.addItemListener((e) -> {
            setVersion();
        });
        versionComboBoxModel.setSelectedItem(settings.get(VERSION, AnalysisOptions.getInstance().getCodingStandardsFixerVersion()));
    }

    private void setLevelComboBox() {
        assert EventQueue.isDispatchThread();
        DefaultComboBoxModel<String> levelComboBoxModel = new DefaultComboBoxModel<>();
        CodingStandardsFixer.ALL_LEVEL.forEach((level) -> {
            levelComboBoxModel.addElement(level);
        });
        levelComboBoxModel.setSelectedItem(settings.get(LEVEL, AnalysisOptions.getInstance().getCodingStandardsFixerLevel()));
        levelComboBox.setModel(levelComboBoxModel);
        levelComboBox.addItemListener((e) -> {
            setLevel();
        });
    }

    private void setConfigComboBox() {
        assert EventQueue.isDispatchThread();
        DefaultComboBoxModel<String> configComboBoxModel = new DefaultComboBoxModel<>();
        CodingStandardsFixer.ALL_CONFIG.forEach((config) -> {
            configComboBoxModel.addElement(config);
        });
        configComboBoxModel.setSelectedItem(settings.get(CONFIG, AnalysisOptions.getInstance().getCodingStandardsFixerConfig()));
        configComboBox.setModel(configComboBoxModel);
        configComboBox.addItemListener((e) -> {
            setConfig();
        });
    }

    private void setOptionsTextField() {
        assert EventQueue.isDispatchThread();
        optionsTextField.setText(settings.get(OPTIONS, AnalysisOptions.getInstance().getCodingStandardsFixerOptions()));
        optionsTextField.getDocument().addDocumentListener(new DocumentListener() {
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
                setOptions();
            }
        });
    }

    private void setCodingStandardsFixerEnabled() {
        settings.putBoolean(ENABLED, enabledCheckBox.isSelected());
    }

    private void setVersion() {
        settings.put(VERSION, (String) versionComboBox.getSelectedItem());
    }

    private void setLevel() {
        settings.put(LEVEL, (String) levelComboBox.getSelectedItem());
    }

    private void setConfig() {
        settings.put(CONFIG, (String) configComboBox.getSelectedItem());
    }

    private void setOptions() {
        settings.put(OPTIONS, optionsTextField.getText());
    }

    private void setVersion1ComponentsVisible(boolean isVisible) {
        levelLabel.setVisible(isVisible);
        levelComboBox.setVisible(isVisible);
        configLabel.setVisible(isVisible);
        configComboBox.setVisible(isVisible);
    }

    private void setAllComponetsEnabled(boolean isEnabled) {
        Component[] components = getComponents();
        for (Component component : components) {
            if (component != enabledCheckBox) {
                component.setEnabled(isEnabled);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        levelLabel = new JLabel();
        levelComboBox = new JComboBox<>();
        configLabel = new JLabel();
        configComboBox = new JComboBox<>();
        optionsLabel = new JLabel();
        optionsTextField = new JTextField();
        versionLabel = new JLabel();
        versionComboBox = new JComboBox<>();
        enabledCheckBox = new JCheckBox();

        Mnemonics.setLocalizedText(levelLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.levelLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(configLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.configLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.optionsLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.versionLabel.text")); // NOI18N

        versionComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                versionComboBoxActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(enabledCheckBox, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.enabledCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(levelLabel)
                    .addComponent(configLabel)
                    .addComponent(optionsLabel)
                    .addComponent(versionLabel))
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(optionsTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(levelComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(configComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(versionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(versionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(versionLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(levelLabel)
                    .addComponent(levelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(configComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(optionsLabel)
                    .addComponent(optionsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void versionComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_versionComboBoxActionPerformed
        switch ((String) versionComboBox.getSelectedItem()) {
            case "1": // NOI18N
                setVersion1ComponentsVisible(true);
                break;
            case "2": // NOI18N
                setVersion1ComponentsVisible(false);
                break;
            default:
                throw new AssertionError();
        }
    }//GEN-LAST:event_versionComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<String> configComboBox;
    private JLabel configLabel;
    private JCheckBox enabledCheckBox;
    private JComboBox<String> levelComboBox;
    private JLabel levelLabel;
    private JLabel optionsLabel;
    private JTextField optionsTextField;
    private JComboBox<String> versionComboBox;
    private JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}
