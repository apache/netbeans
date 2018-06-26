/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.phing.ui.options;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
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
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.phing.exec.PhingExecutable;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public class PhingOptionsPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PhingOptionsPanel.class.getName());

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public PhingOptionsPanel() {
        assert EventQueue.isDispatchThread();
        initComponents();

        init();
    }

    @NbBundle.Messages({
        "# {0} - phing file name 1",
        "# {1} - phing file name 2",
        "# {2} - phing file name 3",
        "PhingOptionsPanel.phing.hint=Full path of Phing (typically {0}, {1} or {2}).",
    })
    private void init() {
        errorLabel.setText(" "); // NOI18N
        String[] names = PhingExecutable.PHING_NAMES;
        assert names.length == 3 : Arrays.toString(names);
        hintLabel.setText(Bundle.PhingOptionsPanel_phing_hint(names[0], names[1], names[2]));
        phingTextField.getDocument().addDocumentListener(new DefaultDocumentListener());
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

    public String getPhing() {
        return phingTextField.getText();
    }

    public void setPhing(String phing) {
        phingTextField.setText(phing);
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

        phingLabel = new JLabel();
        phingTextField = new JTextField();
        phingBrowseButton = new JButton();
        phingSearchButton = new JButton();
        hintLabel = new JLabel();
        noteLabel = new JLabel();
        installationLabel = new JLabel();
        learnMoreLabel = new JLabel();
        errorLabel = new JLabel();

        phingLabel.setLabelFor(phingTextField);
        Mnemonics.setLocalizedText(phingLabel, NbBundle.getMessage(PhingOptionsPanel.class, "PhingOptionsPanel.phingLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phingBrowseButton, NbBundle.getMessage(PhingOptionsPanel.class, "PhingOptionsPanel.phingBrowseButton.text")); // NOI18N
        phingBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phingBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(phingSearchButton, NbBundle.getMessage(PhingOptionsPanel.class, "PhingOptionsPanel.phingSearchButton.text")); // NOI18N
        phingSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phingSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(hintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(PhingOptionsPanel.class, "PhingOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(installationLabel, NbBundle.getMessage(PhingOptionsPanel.class, "PhingOptionsPanel.installationLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(learnMoreLabel, NbBundle.getMessage(PhingOptionsPanel.class, "PhingOptionsPanel.learnMoreLabel.text")); // NOI18N
        learnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                learnMoreLabelMousePressed(evt);
            }
            public void mouseEntered(MouseEvent evt) {
                learnMoreLabelMouseEntered(evt);
            }
        });

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(phingLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hintLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(phingTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phingBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phingSearchButton))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(errorLabel)
                    .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(installationLabel)
                    .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(phingLabel)
                    .addComponent(phingTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(phingBrowseButton)
                    .addComponent(phingSearchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hintLabel)
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(installationLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void learnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_learnMoreLabelMouseEntered

    private void learnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://www.phing.info/")); // NOI18N
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }//GEN-LAST:event_learnMoreLabelMousePressed

    @NbBundle.Messages("PhingOptionsPanel.phing.browse.title=Select Phing")
    private void phingBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phingBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(PhingOptionsPanel.class)
                .setFilesOnly(true)
                .setTitle(Bundle.PhingOptionsPanel_phing_browse_title())
                .showOpenDialog();
        if (file != null) {
            phingTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_phingBrowseButtonActionPerformed

    @NbBundle.Messages({
        "PhingOptionsPanel.search.scripts.title=Phing files",
        "PhingOptionsPanel.search.scripts=&Phing files:",
        "PhingOptionsPanel.search.scripts.pleaseWaitPart=Phing files",
        "PhingOptionsPanel.search.scripts.notFound=No Phing files found."
    })
    private void phingSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phingSearchButtonActionPerformed
        assert EventQueue.isDispatchThread();
        String phing = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {
            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(PhingExecutable.PHING_NAMES);
            }
            @Override
            public String getWindowTitle() {
                return Bundle.PhingOptionsPanel_search_scripts_title();
            }
            @Override
            public String getListTitle() {
                return Bundle.PhingOptionsPanel_search_scripts();
            }
            @Override
            public String getPleaseWaitPart() {
                return Bundle.PhingOptionsPanel_search_scripts_pleaseWaitPart();
            }
            @Override
            public String getNoItemsFound() {
                return Bundle.PhingOptionsPanel_search_scripts_notFound();
            }
        });
        if (phing != null) {
            phingTextField.setText(phing);
        }
    }//GEN-LAST:event_phingSearchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel errorLabel;
    private JLabel hintLabel;
    private JLabel installationLabel;
    private JLabel learnMoreLabel;
    private JLabel noteLabel;
    private JButton phingBrowseButton;
    private JLabel phingLabel;
    private JButton phingSearchButton;
    private JTextField phingTextField;
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
