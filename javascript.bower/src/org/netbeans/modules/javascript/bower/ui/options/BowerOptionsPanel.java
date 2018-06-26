/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.bower.ui.options;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.javascript.bower.exec.BowerExecutable;
import org.netbeans.modules.javascript.bower.util.FileUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

@OptionsPanelController.Keywords(keywords={"#KW.BowerOptionsPanel"}, location="Html5", tabTitle= "Bower")
public class BowerOptionsPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(BowerOptionsPanel.class.getName());

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public BowerOptionsPanel() {
        assert EventQueue.isDispatchThread();
        initComponents();

        init();
    }

    @NbBundle.Messages({
        "# {0} - bower file name",
        "BowerOptionsPanel.bower.hint=Full path of Bower file (typically {0}).",
    })
    private void init() {
        errorLabel.setText(" "); // NOI18N
        bowerHintLabel.setText(Bundle.BowerOptionsPanel_bower_hint(BowerExecutable.BOWER_NAME));
        bowerTextField.getDocument().addDocumentListener(new DefaultDocumentListener());
        ignoreBowerComponentsCheckBox.addItemListener(new DefaultItemListener());
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
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

    public String getBower() {
        return bowerTextField.getText();
    }

    public void setBower(String bower) {
        bowerTextField.setText(bower);
    }

    public boolean isIgnoreBowerComponents() {
        return ignoreBowerComponentsCheckBox.isSelected();
    }

    public void setIgnoreBowerComponents(boolean npmIgnoreNodeModules) {
        ignoreBowerComponentsCheckBox.setSelected(npmIgnoreNodeModules);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bowerjLabel = new JLabel();
        bowerTextField = new JTextField();
        bowerBrowseButton = new JButton();
        bowerSearchButton = new JButton();
        bowerHintLabel = new JLabel();
        installLabel = new JLabel();
        ignoreBowerComponentsCheckBox = new JCheckBox();
        errorLabel = new JLabel();

        bowerjLabel.setLabelFor(bowerTextField);
        Mnemonics.setLocalizedText(bowerjLabel, NbBundle.getMessage(BowerOptionsPanel.class, "BowerOptionsPanel.bowerjLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(bowerBrowseButton, NbBundle.getMessage(BowerOptionsPanel.class, "BowerOptionsPanel.bowerBrowseButton.text")); // NOI18N
        bowerBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bowerBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(bowerSearchButton, NbBundle.getMessage(BowerOptionsPanel.class, "BowerOptionsPanel.bowerSearchButton.text")); // NOI18N
        bowerSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bowerSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(bowerHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(installLabel, NbBundle.getMessage(BowerOptionsPanel.class, "BowerOptionsPanel.installLabel.text")); // NOI18N
        installLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                installLabelMousePressed(evt);
            }
            public void mouseEntered(MouseEvent evt) {
                installLabelMouseEntered(evt);
            }
        });

        Mnemonics.setLocalizedText(ignoreBowerComponentsCheckBox, NbBundle.getMessage(BowerOptionsPanel.class, "BowerOptionsPanel.ignoreBowerComponentsCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(bowerjLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bowerHintLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(installLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bowerTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bowerBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bowerSearchButton))))
            .addComponent(errorLabel)
            .addComponent(ignoreBowerComponentsCheckBox)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bowerjLabel)
                    .addComponent(bowerTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bowerSearchButton)
                    .addComponent(bowerBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bowerHintLabel)
                    .addComponent(installLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ignoreBowerComponentsCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("BowerOptionsPanel.bower.browse.title=Select Bower")
    private void bowerBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_bowerBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(BowerOptionsPanel.class)
                .setFilesOnly(true)
                .setTitle(Bundle.BowerOptionsPanel_bower_browse_title())
                .showOpenDialog();
        if (file != null) {
            bowerTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_bowerBrowseButtonActionPerformed

    @NbBundle.Messages("BowerOptionsPanel.bower.none=No Bower executable was found.")
    private void bowerSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_bowerSearchButtonActionPerformed
        assert EventQueue.isDispatchThread();
        for (String bower : FileUtils.findFileOnUsersPath(BowerExecutable.BOWER_NAME)) {
            bowerTextField.setText(new File(bower).getAbsolutePath());
            return;
        }
        // no bower found
        StatusDisplayer.getDefault().setStatusText(Bundle.BowerOptionsPanel_bower_none());
    }//GEN-LAST:event_bowerSearchButtonActionPerformed

    private void installLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_installLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_installLabelMouseEntered

    private void installLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_installLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://bower.io/")); // NOI18N
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }//GEN-LAST:event_installLabelMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton bowerBrowseButton;
    private JLabel bowerHintLabel;
    private JButton bowerSearchButton;
    private JTextField bowerTextField;
    private JLabel bowerjLabel;
    private JLabel errorLabel;
    private JCheckBox ignoreBowerComponentsCheckBox;
    private JLabel installLabel;
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

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            fireChange();
        }

    }

}
