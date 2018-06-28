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

import java.awt.EventQueue;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.php.analysis.commands.CodingStandardsFixer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public class CodingStandardsFixerCustomizerPanel extends JPanel {

    private static final long serialVersionUID = -575231773758741767L;

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
        setLevelComboBox();
        setConfigComboBox();
        setOptionsTextField();
    }

    private void setLevelComboBox() {
        assert EventQueue.isDispatchThread();
        DefaultComboBoxModel<String> levelComboBoxModel = new DefaultComboBoxModel<>();
        for (String level : CodingStandardsFixer.ALL_LEVEL) {
            levelComboBoxModel.addElement(level);
        }
        levelComboBoxModel.setSelectedItem(settings.get(LEVEL, AnalysisOptions.getInstance().getCodingStandardsFixerLevel())); // NOI18N
        levelComboBox.setModel(levelComboBoxModel);
    }

    private void setConfigComboBox() {
        assert EventQueue.isDispatchThread();
        DefaultComboBoxModel<String> configComboBoxModel = new DefaultComboBoxModel<>();
        for (String config : CodingStandardsFixer.ALL_CONFIG) {
            configComboBoxModel.addElement(config);
        }
        configComboBoxModel.setSelectedItem(settings.get(CONFIG, AnalysisOptions.getInstance().getCodingStandardsFixerConfig())); // NOI18N
        configComboBox.setModel(configComboBoxModel);
    }

    private void setOptionsTextField() {
        assert EventQueue.isDispatchThread();
        optionsTextField.setText(settings.get(OPTIONS, AnalysisOptions.getInstance().getCodingStandardsFixerOptions()));
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
        levelComboBox = new JComboBox();
        configLabel = new JLabel();
        configComboBox = new JComboBox();
        optionsLabel = new JLabel();
        optionsTextField = new JTextField();

        Mnemonics.setLocalizedText(levelLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.levelLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(configLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.configLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(CodingStandardsFixerCustomizerPanel.class, "CodingStandardsFixerCustomizerPanel.optionsLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(levelLabel)
                    .addComponent(configLabel)
                    .addComponent(optionsLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(configComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(levelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(optionsTextField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox configComboBox;
    private JLabel configLabel;
    private JComboBox levelComboBox;
    private JLabel levelLabel;
    private JLabel optionsLabel;
    private JTextField optionsTextField;
    // End of variables declaration//GEN-END:variables
}
