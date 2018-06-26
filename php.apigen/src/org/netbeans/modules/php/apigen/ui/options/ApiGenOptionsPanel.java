/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.apigen.ui.options;

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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.apigen.commands.ApiGenScript;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@NbBundle.Messages({"ApiGenOptionsPanel.keywords.documentation=documentation", "ApiGenOptionsPanel.keywords.tabtitle=Frameworks & Tools"})
@OptionsPanelController.Keywords(keywords={"php", "apigen", "phpdoc", "phpdocumentor", "#ApiGenOptionsPanel.keywords.documentation"},
        location=UiUtils.OPTIONS_PATH, tabTitle= "#ApiGenOptionsPanel.keywords.tabtitle")
public class ApiGenOptionsPanel extends JPanel {

    private static final long serialVersionUID = 458797646546L;

    private static final String APIGEN_LAST_FOLDER_SUFFIX = ".apigen";

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public ApiGenOptionsPanel() {
        initComponents();
        init();
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "# {1} - long script name",
        "ApiGenOptionsPanel.apiGen.hint=Full path of ApiGen script (typically {0} or {1})."
    })
    private void init() {
        hintLabel.setText(Bundle.ApiGenOptionsPanel_apiGen_hint(ApiGenScript.SCRIPT_NAME, ApiGenScript.SCRIPT_NAME_LONG));
        errorLabel.setText(" "); // NOI18N

        // listeners
        DocumentListener documentListener = new DefaultDocumentListener();
        apiGenTextField.getDocument().addDocumentListener(documentListener);
    }

    public String getApiGen() {
        return apiGenTextField.getText();
    }

    public void setApiGen(String apiGen) {
        apiGenTextField.setText(apiGen);
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
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        apiGenLabel = new JLabel();
        apiGenTextField = new JTextField();
        browseButton = new JButton();
        searchButton = new JButton();
        hintLabel = new JLabel();
        noteLabel = new JLabel();
        versionLabel = new JLabel();
        installationInstructionsLabel = new JLabel();
        learnMoreLabel = new JLabel();
        errorLabel = new JLabel();

        apiGenLabel.setLabelFor(apiGenTextField);
        Mnemonics.setLocalizedText(apiGenLabel, NbBundle.getMessage(ApiGenOptionsPanel.class, "ApiGenOptionsPanel.apiGenLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(ApiGenOptionsPanel.class, "ApiGenOptionsPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(ApiGenOptionsPanel.class, "ApiGenOptionsPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(hintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(ApiGenOptionsPanel.class, "ApiGenOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(ApiGenOptionsPanel.class, "ApiGenOptionsPanel.versionLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(installationInstructionsLabel, NbBundle.getMessage(ApiGenOptionsPanel.class, "ApiGenOptionsPanel.installationInstructionsLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(learnMoreLabel, NbBundle.getMessage(ApiGenOptionsPanel.class, "ApiGenOptionsPanel.learnMoreLabel.text")); // NOI18N
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
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(apiGenLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hintLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(apiGenTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(browseButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(searchButton))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(errorLabel))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(installationInstructionsLabel)
                    .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(versionLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {browseButton, searchButton});

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(apiGenTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(apiGenLabel)
                    .addComponent(searchButton)
                    .addComponent(browseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(hintLabel)
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(versionLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(installationInstructionsLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void learnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_learnMoreLabelMouseEntered

    private void learnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://apigen.org/")); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_learnMoreLabelMousePressed

    @NbBundle.Messages("ApiGenOptionsPanel.browse.title=Select ApiGen script")
    private void browseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File apiGenScript = new FileChooserBuilder(ApiGenOptionsPanel.class.getName() + APIGEN_LAST_FOLDER_SUFFIX)
                .setTitle(Bundle.ApiGenOptionsPanel_browse_title())
                .setFilesOnly(true)
                .showOpenDialog();
        if (apiGenScript != null) {
            apiGenScript = FileUtil.normalizeFile(apiGenScript);
            apiGenTextField.setText(apiGenScript.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    @NbBundle.Messages({
        "ApiGenOptionsPanel.search.scripts.title=ApiGen scripts",
        "ApiGenOptionsPanel.search.scripts=&ApiGen scripts:",
        "ApiGenOptionsPanel.search.scripts.pleaseWaitPart=ApiGen scripts",
        "ApiGenOptionsPanel.search.scripts.notFound=No ApiGen scripts found."
    })
    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        String script = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {
            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(ApiGenScript.SCRIPT_NAME, ApiGenScript.SCRIPT_NAME_LONG);
            }
            @Override
            public String getWindowTitle() {
                return Bundle.ApiGenOptionsPanel_search_scripts_title();
            }
            @Override
            public String getListTitle() {
                return Bundle.ApiGenOptionsPanel_search_scripts();
            }
            @Override
            public String getPleaseWaitPart() {
                return Bundle.ApiGenOptionsPanel_search_scripts_pleaseWaitPart();
            }
            @Override
            public String getNoItemsFound() {
                return Bundle.ApiGenOptionsPanel_search_scripts_notFound();
            }
        });
        if (script != null) {
            apiGenTextField.setText(script);
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel apiGenLabel;
    private JTextField apiGenTextField;
    private JButton browseButton;
    private JLabel errorLabel;
    private JLabel hintLabel;
    private JLabel installationInstructionsLabel;
    private JLabel learnMoreLabel;
    private JLabel noteLabel;
    private JButton searchButton;
    private JLabel versionLabel;
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
