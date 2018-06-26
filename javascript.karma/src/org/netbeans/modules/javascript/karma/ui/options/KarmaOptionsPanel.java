/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.karma.ui.options;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.javascript.karma.exec.KarmaExecutable;
import org.netbeans.modules.javascript.karma.util.FileUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

@OptionsPanelController.Keywords(keywords={"#KW.KarmaOptionsPanel"}, location="Html5", tabTitle= "Karma")
public class KarmaOptionsPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(KarmaOptionsPanel.class.getName());

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    KarmaOptionsPanel() {
        assert EventQueue.isDispatchThread();
        initComponents();

        init();
    }

    @NbBundle.Messages({
        "# {0} - karma file name",
        "KarmaOptionsPanel.karma.hint=Full path of Karma file (typically {0}).",
    })
    private void init() {
        errorLabel.setText(" "); // NOI18N
        hintLabel.setText(Bundle.KarmaOptionsPanel_karma_hint(KarmaExecutable.KARMA_NAME));
        karmaTextField.getDocument().addDocumentListener(new DefaultDocumentListener());
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

    public String getKarma() {
        return karmaTextField.getText();
    }

    public void setKarma(String karma) {
        karmaTextField.setText(karma);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        karmaLabel = new JLabel();
        karmaTextField = new JTextField();
        karmaBrowseButton = new JButton();
        karmaSearchButton = new JButton();
        hintLabel = new JLabel();
        installLabel = new JLabel();
        errorLabel = new JLabel();

        karmaLabel.setLabelFor(karmaTextField);
        Mnemonics.setLocalizedText(karmaLabel, NbBundle.getMessage(KarmaOptionsPanel.class, "KarmaOptionsPanel.karmaLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(karmaBrowseButton, NbBundle.getMessage(KarmaOptionsPanel.class, "KarmaOptionsPanel.karmaBrowseButton.text")); // NOI18N
        karmaBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                karmaBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(karmaSearchButton, NbBundle.getMessage(KarmaOptionsPanel.class, "KarmaOptionsPanel.karmaSearchButton.text")); // NOI18N
        karmaSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                karmaSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(hintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(installLabel, NbBundle.getMessage(KarmaOptionsPanel.class, "KarmaOptionsPanel.installLabel.text")); // NOI18N
        installLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                installLabelMousePressed(evt);
            }
            public void mouseEntered(MouseEvent evt) {
                installLabelMouseEntered(evt);
            }
        });

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(karmaLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hintLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(installLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(karmaTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(karmaBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(karmaSearchButton))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(errorLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(karmaLabel)
                    .addComponent(karmaTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(karmaBrowseButton)
                    .addComponent(karmaSearchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(hintLabel)
                    .addComponent(installLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("KarmaOptionsPanel.karma.browse.title=Select Karma")
    private void karmaBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_karmaBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(KarmaOptionsPanel.class)
                .setFilesOnly(true)
                .setTitle(Bundle.KarmaOptionsPanel_karma_browse_title())
                .showOpenDialog();
        if (file != null) {
            karmaTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_karmaBrowseButtonActionPerformed

    @NbBundle.Messages("KarmaOptionsPanel.karma.none=No Karma executable was found.")
    private void karmaSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_karmaSearchButtonActionPerformed
        assert EventQueue.isDispatchThread();
        for (String karma : FileUtils.findFileOnUsersPath(KarmaExecutable.KARMA_NAME)) {
            karmaTextField.setText(new File(karma).getAbsolutePath());
            return;
        }
        // no karma found
        StatusDisplayer.getDefault().setStatusText(Bundle.KarmaOptionsPanel_karma_none());
    }//GEN-LAST:event_karmaSearchButtonActionPerformed

    private void installLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_installLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_installLabelMouseEntered

    private void installLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_installLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://karma-runner.github.io/latest/intro/installation.html")); // NOI18N
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }//GEN-LAST:event_installLabelMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel errorLabel;
    private JLabel hintLabel;
    private JLabel installLabel;
    private JButton karmaBrowseButton;
    private JLabel karmaLabel;
    private JButton karmaSearchButton;
    private JTextField karmaTextField;
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
