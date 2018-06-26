/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.analysis.ui.options;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.ui.CodeSnifferStandardsComboBoxModel;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class CodeSnifferOptionsPanel extends AnalysisCategoryPanel {

    private static final long serialVersionUID = 1342405149329523117L;
    private static final String CODE_SNIFFER_LAST_FOLDER_SUFFIX = ".codeSniffer"; // NOI18N

    final CodeSnifferStandardsComboBoxModel codeSnifferStandardsModel = new CodeSnifferStandardsComboBoxModel();

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // #232367
    volatile boolean ignoreChanges = false;


    public CodeSnifferOptionsPanel() {
        super();
        initComponents();

        init();
    }

    private void init() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        initCodeSniffer(defaultDocumentListener);
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "# {1} - long script name",
        "CodeSnifferOptionsPanel.hint=Full path of Code Sniffer script (typically {0} or {1}).",
    })
    private void initCodeSniffer(DocumentListener defaultDocumentListener) {
        codeSnifferHintLabel.setText(Bundle.CodeSnifferOptionsPanel_hint(CodeSniffer.NAME, CodeSniffer.LONG_NAME));
        codeSnifferStandardComboBox.setModel(codeSnifferStandardsModel);

        // listeners
        codeSnifferTextField.getDocument().addDocumentListener(defaultDocumentListener);
        codeSnifferTextField.getDocument().addDocumentListener(new CodeSnifferPathDocumentListener());
        final ItemListener defaultItemListener = new DefaultItemListener();
        codeSnifferStandardsModel.fetchStandards(codeSnifferStandardComboBox, new Runnable() {
            @Override
            public void run() {
                // #232279
                codeSnifferStandardComboBox.addItemListener(defaultItemListener);
            }
        });
    }

    void setStandards(final String selectedCodeSnifferStandard, String customCodeSnifferPath) {
        codeSnifferStandardsModel.fetchStandards(codeSnifferStandardComboBox, customCodeSnifferPath, null);
        codeSnifferStandardsModel.setSelectedItem(selectedCodeSnifferStandard);
    }

    public String getCodeSnifferPath() {
        return codeSnifferTextField.getText();
    }

    public void setCodeSnifferPath(String path) {
        ignoreChanges = true;
        codeSnifferTextField.setText(path);
        ignoreChanges = false;
    }

    @CheckForNull
    public String getCodeSnifferStandard() {
        if (!codeSnifferStandardComboBox.isEnabled()) {
            // fetching standards
            return null;
        }
        return codeSnifferStandardsModel.getSelectedStandard();
    }

    public void setCodeSnifferStandard(String standard) {
        codeSnifferStandardsModel.setSelectedItem(standard);
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

    @NbBundle.Messages("CodeSnifferOptionsPanel.category.name=Code Sniffer")
    @Override
    public String getCategoryName() {
        return Bundle.CodeSnifferOptionsPanel_category_name();
    }

    @Override
    public void update() {
        AnalysisOptions analysisOptions = AnalysisOptions.getInstance();
        setCodeSnifferPath(analysisOptions.getCodeSnifferPath());
        setCodeSnifferStandard(analysisOptions.getCodeSnifferStandard());
    }

    @Override
    public void applyChanges() {
        AnalysisOptions analysisOptions = AnalysisOptions.getInstance();
        analysisOptions.setCodeSnifferPath(getCodeSnifferPath());
        analysisOptions.setCodeSnifferStandard(getCodeSnifferStandard());
    }
    
    @Override
    public boolean isChanged() {
        String saved = AnalysisOptions.getInstance().getCodeSnifferPath();
        String current = getCodeSnifferPath().trim();
        if(saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        saved = AnalysisOptions.getInstance().getCodeSnifferStandard();
        current = getCodeSnifferStandard();
        if(saved == null) {
            if(current == null) {
                return false;
            }
            return !current.trim().isEmpty();
        }
        return current == null ? !saved.trim().isEmpty() : !saved.equals(current);
    }

    @Override
    public ValidationResult getValidationResult() {
        return new AnalysisOptionsValidator()
                .validateCodeSniffer(getCodeSnifferPath(), getCodeSnifferStandard())
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

        codeSnifferLabel = new JLabel();
        codeSnifferTextField = new JTextField();
        codeSnifferBrowseButton = new JButton();
        codeSnifferSearchButton = new JButton();
        codeSnifferHintLabel = new JLabel();
        codeSnifferStandardLabel = new JLabel();
        codeSnifferStandardComboBox = new JComboBox<String>();
        noteLabel = new JLabel();
        minVersionInfoLabel = new JLabel();
        codeSnifferLearnMoreLabel = new JLabel();

        codeSnifferLabel.setLabelFor(codeSnifferTextField);
        Mnemonics.setLocalizedText(codeSnifferLabel, NbBundle.getMessage(CodeSnifferOptionsPanel.class, "CodeSnifferOptionsPanel.codeSnifferLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(codeSnifferBrowseButton, NbBundle.getMessage(CodeSnifferOptionsPanel.class, "CodeSnifferOptionsPanel.codeSnifferBrowseButton.text")); // NOI18N
        codeSnifferBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                codeSnifferBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(codeSnifferSearchButton, NbBundle.getMessage(CodeSnifferOptionsPanel.class, "CodeSnifferOptionsPanel.codeSnifferSearchButton.text")); // NOI18N
        codeSnifferSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                codeSnifferSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(codeSnifferHintLabel, "HINT"); // NOI18N

        codeSnifferStandardLabel.setLabelFor(codeSnifferStandardComboBox);
        Mnemonics.setLocalizedText(codeSnifferStandardLabel, NbBundle.getMessage(CodeSnifferOptionsPanel.class, "CodeSnifferOptionsPanel.codeSnifferStandardLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(CodeSnifferOptionsPanel.class, "CodeSnifferOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(minVersionInfoLabel, NbBundle.getMessage(CodeSnifferOptionsPanel.class, "CodeSnifferOptionsPanel.minVersionInfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(codeSnifferLearnMoreLabel, NbBundle.getMessage(CodeSnifferOptionsPanel.class, "CodeSnifferOptionsPanel.codeSnifferLearnMoreLabel.text")); // NOI18N
        codeSnifferLearnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                codeSnifferLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                codeSnifferLearnMoreLabelMousePressed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(codeSnifferLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(codeSnifferLabel)
                    .addComponent(codeSnifferStandardLabel))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(codeSnifferTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codeSnifferBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codeSnifferSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(codeSnifferHintLabel)
                            .addComponent(codeSnifferStandardComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(minVersionInfoLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {codeSnifferBrowseButton, codeSnifferSearchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codeSnifferLabel)
                    .addComponent(codeSnifferTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(codeSnifferSearchButton)
                    .addComponent(codeSnifferBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeSnifferHintLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(codeSnifferStandardComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(codeSnifferStandardLabel))
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minVersionInfoLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeSnifferLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CodeSnifferOptionsPanel.browse.title=Select Code Sniffer")
    private void codeSnifferBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_codeSnifferBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CodeSnifferOptionsPanel.class.getName() + CODE_SNIFFER_LAST_FOLDER_SUFFIX)
                .setFilesOnly(true)
                .setTitle(Bundle.CodeSnifferOptionsPanel_browse_title())
                .showOpenDialog();
        if (file != null) {
            codeSnifferTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_codeSnifferBrowseButtonActionPerformed

    @NbBundle.Messages({
        "CodeSnifferOptionsPanel.search.title=Code Sniffer scripts",
        "CodeSnifferOptionsPanel.search.scripts=Co&de Sniffer scripts:",
        "CodeSnifferOptionsPanel.search.pleaseWaitPart=Code Sniffer scripts",
        "CodeSnifferOptionsPanel.search.notFound=No Code Sniffer scripts found."
    })
    private void codeSnifferSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_codeSnifferSearchButtonActionPerformed
        String codeSniffer = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {

            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(CodeSniffer.NAME, CodeSniffer.LONG_NAME);
            }

            @Override
            public String getWindowTitle() {
                return Bundle.CodeSnifferOptionsPanel_search_title();
            }

            @Override
            public String getListTitle() {
                return Bundle.CodeSnifferOptionsPanel_search_scripts();
            }

            @Override
            public String getPleaseWaitPart() {
                return Bundle.CodeSnifferOptionsPanel_search_pleaseWaitPart();
            }

            @Override
            public String getNoItemsFound() {
                return Bundle.CodeSnifferOptionsPanel_search_notFound();
            }
        });
        if (codeSniffer != null) {
            codeSnifferTextField.setText(codeSniffer);
        }
    }//GEN-LAST:event_codeSnifferSearchButtonActionPerformed

    private void codeSnifferLearnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_codeSnifferLearnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_codeSnifferLearnMoreLabelMouseEntered

    private void codeSnifferLearnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_codeSnifferLearnMoreLabelMousePressed
        try {
            URL url = new URL("http://pear.php.net/package/PHP_CodeSniffer"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_codeSnifferLearnMoreLabelMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton codeSnifferBrowseButton;
    private JLabel codeSnifferHintLabel;
    private JLabel codeSnifferLabel;
    private JLabel codeSnifferLearnMoreLabel;
    private JButton codeSnifferSearchButton;
    private JComboBox<String> codeSnifferStandardComboBox;
    private JLabel codeSnifferStandardLabel;
    private JTextField codeSnifferTextField;
    private JLabel minVersionInfoLabel;
    private JLabel noteLabel;
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

    private final class CodeSnifferPathDocumentListener implements DocumentListener {

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
            if (ignoreChanges) {
                return;
            }
            String codeSnifferPath = getCodeSnifferPath();
            // reset cached standards only if the new path is valid
            ValidationResult result = new AnalysisOptionsValidator()
                    .validateCodeSnifferPath(codeSnifferPath)
                    .getResult();
            if (!result.hasErrors()
                    && !result.hasWarnings()) {
                CodeSniffer.clearCachedStandards();
                setStandards(getCodeSnifferStandard(), codeSnifferPath);
            }
        }

    }

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            fireChange();
        }

    }

}
