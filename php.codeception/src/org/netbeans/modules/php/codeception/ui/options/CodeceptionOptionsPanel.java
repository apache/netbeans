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
package org.netbeans.modules.php.codeception.ui.options;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "CodeceptionOptionsPanel.keywords.coverage=coverage",
    "CodeceptionOptionsPanel.keywords.TabTitle=Frameworks & Tools"
})
@OptionsPanelController.Keywords(
        keywords = {"php", "codeception", "unit testing", "framework", "coverage", "#CodeceptionOptionsPanel.keywords.coverage"},
        location = UiUtils.OPTIONS_PATH, tabTitle = "#CodeceptionOptionsPanel.keywords.TabTitle")
final class CodeceptionOptionsPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(CodeceptionOptionsPanel.class.getName());

    private static final String CODECEPTION_LAST_FOLDER_SUFFIX = ".codeception"; // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    CodeceptionOptionsPanel() {
        initComponents();

        init();
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "# {1} - long script name",
        "# {2} - PHAR script name",
        "CodeceptionOptionsPanel.codeception.hint=<html>Full path of Codecept file (typically {0}, {1} or {2}).",
    })
    private void init() {
        errorLabel.setText(" "); // NOI18N
        codeceptionHintLabel.setText(Bundle.CodeceptionOptionsPanel_codeception_hint(Codecept.SCRIPT_NAME, Codecept.SCRIPT_NAME_LONG, Codecept.SCRIPT_NAME_PHAR));

        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        codeceptionTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    public String getCodeception() {
        return codeceptionTextField.getText();
    }

    public void setCodeception(String codeception) {
        codeceptionTextField.setText(codeception);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        codeceptionLabel = new JLabel();
        codeceptionTextField = new JTextField();
        browseButton = new JButton();
        searchButton = new JButton();
        codeceptionHintLabel = new JLabel();
        noteLabel = new JLabel();
        minVersionLabel = new JLabel();
        installLabel = new JLabel();
        learnMoreLabel = new JLabel();
        errorLabel = new JLabel();

        codeceptionLabel.setLabelFor(codeceptionTextField);
        Mnemonics.setLocalizedText(codeceptionLabel, NbBundle.getMessage(CodeceptionOptionsPanel.class, "CodeceptionOptionsPanel.codeceptionLabel.text")); // NOI18N
        codeceptionLabel.setToolTipText(NbBundle.getMessage(CodeceptionOptionsPanel.class, "CodeceptionOptionsPanel.codeceptionLabel.toolTipText")); // NOI18N

        Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(CodeceptionOptionsPanel.class, "CodeceptionOptionsPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(CodeceptionOptionsPanel.class, "CodeceptionOptionsPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(codeceptionHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(CodeceptionOptionsPanel.class, "CodeceptionOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(minVersionLabel, NbBundle.getMessage(CodeceptionOptionsPanel.class, "CodeceptionOptionsPanel.minVersionLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(installLabel, NbBundle.getMessage(CodeceptionOptionsPanel.class, "CodeceptionOptionsPanel.installLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(learnMoreLabel, NbBundle.getMessage(CodeceptionOptionsPanel.class, "CodeceptionOptionsPanel.learnMoreLabel.text")); // NOI18N
        learnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                learnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                learnMoreLabelMousePressed(evt);
            }
        });

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(codeceptionLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(codeceptionHintLabel)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(codeceptionTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(minVersionLabel)
                    .addComponent(installLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(errorLabel)
                    .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codeceptionLabel)
                    .addComponent(codeceptionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton)
                    .addComponent(searchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeceptionHintLabel)
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minVersionLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(installLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CodeceptionOptionsPanel.codeception.browse.title=Select codecept")
    private void browseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File file = new FileChooserBuilder(CodeceptionOptionsPanel.class.getName() + CODECEPTION_LAST_FOLDER_SUFFIX)
                .setFilesOnly(true)
                .setTitle(Bundle.CodeceptionOptionsPanel_codeception_browse_title())
                .showOpenDialog();
        if (file != null) {
            codeceptionTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    @NbBundle.Messages({
        "CodeceptionOptionsPanel.codeception.search.title=Codecept files",
        "CodeceptionOptionsPanel.codeception.search.files=&Codecept files:",
        "CodeceptionOptionsPanel.codeception.search.pleaseWaitPart=Codecept files",
        "CodeceptionOptionsPanel.codeception.search.notFound=No Codecept files found."
    })
    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        String codeception = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {

            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(Codecept.SCRIPT_NAME, Codecept.SCRIPT_NAME_LONG, Codecept.SCRIPT_NAME_PHAR);
            }

            @Override
            public String getWindowTitle() {
                return Bundle.CodeceptionOptionsPanel_codeception_search_title();
            }

            @Override
            public String getListTitle() {
                return Bundle.CodeceptionOptionsPanel_codeception_search_files();
            }

            @Override
            public String getPleaseWaitPart() {
                return Bundle.CodeceptionOptionsPanel_codeception_search_pleaseWaitPart();
            }

            @Override
            public String getNoItemsFound() {
                return Bundle.CodeceptionOptionsPanel_codeception_search_notFound();
            }
        });
        if (codeception != null) {
            codeceptionTextField.setText(codeception);
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void learnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMousePressed
        try {
            URL url = new URL("http://codeception.com/"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }//GEN-LAST:event_learnMoreLabelMousePressed

    private void learnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_learnMoreLabelMouseEntered

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton browseButton;
    private JLabel codeceptionHintLabel;
    private JLabel codeceptionLabel;
    private JTextField codeceptionTextField;
    private JLabel errorLabel;
    private JLabel installLabel;
    private JLabel learnMoreLabel;
    private JLabel minVersionLabel;
    private JLabel noteLabel;
    private JButton searchButton;
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
