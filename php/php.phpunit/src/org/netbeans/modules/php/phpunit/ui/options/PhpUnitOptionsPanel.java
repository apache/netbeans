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

package org.netbeans.modules.php.phpunit.ui.options;

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
import org.netbeans.modules.php.phpunit.commands.PhpUnit;
import org.netbeans.modules.php.phpunit.commands.SkeletonGenerator;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@NbBundle.Messages({"PhpUnitOptionsPanel.keywords.coverage=coverage","PhpUnitOptionsPanel.keywords.TabTitle=Frameworks & Tools"})
@OptionsPanelController.Keywords(keywords={"php", "phpunit", "unit testing", "framework", "coverage", "#PhpUnitOptionsPanel.keywords.coverage"},
        location=UiUtils.OPTIONS_PATH, tabTitle= "#PhpUnitOptionsPanel.keywords.TabTitle")
public class PhpUnitOptionsPanel extends JPanel {

    private static final long serialVersionUID = -846796509345860L;

    private static final String PHP_UNIT_LAST_FOLDER_SUFFIX = ".phpUnit"; // NOI18N
    private static final String SKELETON_GENERATOR_LAST_FOLDER_SUFFIX = ".skeletonGenerator"; // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public PhpUnitOptionsPanel() {
        initComponents();

        init();
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "# {1} - long script name",
        "# {2} - PHAR script name",
        "PhpUnitOptionsPanel.phpUnit.hint=<html>Full path of PHPUnit script (typically {0}, {1} or {2}).",
        "# {0} - short script name",
        "# {1} - long script name",
        "# {2} - PHAR script name",
        "PhpUnitOptionsPanel.skelGen.hint=<html>Full path of Skeleton Generator script (typically {0}, {1} or {2})."
    })
    private void init() {
        errorLabel.setText(" "); // NOI18N
        phpUnitHintLabel.setText(Bundle.PhpUnitOptionsPanel_phpUnit_hint(
                PhpUnit.SCRIPT_NAME, PhpUnit.SCRIPT_NAME_LONG, PhpUnit.SCRIPT_NAME_PHAR));
        skelGenHintLabel.setText(Bundle.PhpUnitOptionsPanel_skelGen_hint(
                SkeletonGenerator.SCRIPT_NAME, SkeletonGenerator.SCRIPT_NAME_LONG, SkeletonGenerator.SCRIPT_NAME_PHAR));

        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        phpUnitTextField.getDocument().addDocumentListener(defaultDocumentListener);
        skelGenTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    public String getPhpUnit() {
        return phpUnitTextField.getText();
    }

    public void setPhpUnit(String phpUnit) {
        phpUnitTextField.setText(phpUnit);
    }

    public String getPhpUnitSkelGen() {
        return skelGenTextField.getText();
    }

    public void setPhpUnitSkelGen(String phpUnitSkelGen) {
        skelGenTextField.setText(phpUnitSkelGen);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        phpUnitLabel = new JLabel();
        phpUnitTextField = new JTextField();
        phpUnitBrowseButton = new JButton();
        phpUnitSearchButton = new JButton();
        phpUnitHintLabel = new JLabel();
        skelGenLabel = new JLabel();
        skelGenTextField = new JTextField();
        skelGenBrowseButton = new JButton();
        skelGenSearchButton = new JButton();
        skelGenHintLabel = new JLabel();
        noteLabel = new JLabel();
        phpUnitInfoLabel = new JLabel();
        phpUnitPhp53InfoLabel = new JLabel();
        phpUnit370InfoLabel = new JLabel();
        skelGenAbandonedLabel = new JLabel();
        installationInfoLabel = new JLabel();
        phpUnitLearnMoreLabel = new JLabel();
        skelGenLearnMoreLabel = new JLabel();
        errorLabel = new JLabel();

        phpUnitLabel.setLabelFor(phpUnitBrowseButton);
        Mnemonics.setLocalizedText(phpUnitLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpUnitBrowseButton, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitBrowseButton.text")); // NOI18N
        phpUnitBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpUnitBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(phpUnitSearchButton, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitSearchButton.text")); // NOI18N
        phpUnitSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpUnitSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(phpUnitHintLabel, "HINT"); // NOI18N

        skelGenLabel.setLabelFor(skelGenTextField);
        Mnemonics.setLocalizedText(skelGenLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.skelGenLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(skelGenBrowseButton, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.skelGenBrowseButton.text")); // NOI18N
        skelGenBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                skelGenBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(skelGenSearchButton, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.skelGenSearchButton.text")); // NOI18N
        skelGenSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                skelGenSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(skelGenHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpUnitInfoLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitInfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpUnitPhp53InfoLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitPhp53InfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpUnit370InfoLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnit370InfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(skelGenAbandonedLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.skelGenAbandonedLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(installationInfoLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.installationInfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpUnitLearnMoreLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitLearnMoreLabel.text")); // NOI18N
        phpUnitLearnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                phpUnitLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                phpUnitLearnMoreLabelMousePressed(evt);
            }
        });

        Mnemonics.setLocalizedText(skelGenLearnMoreLabel, NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.skelGenLearnMoreLabel.text")); // NOI18N
        skelGenLearnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                skelGenLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                skelGenLearnMoreLabelMousePressed(evt);
            }
        });

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(phpUnitLabel)
                    .addComponent(skelGenLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(skelGenTextField, Alignment.TRAILING)
                            .addComponent(phpUnitTextField, Alignment.TRAILING))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(phpUnitBrowseButton)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(phpUnitSearchButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(skelGenBrowseButton)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(skelGenSearchButton))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(skelGenHintLabel)
                            .addComponent(phpUnitHintLabel))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(errorLabel)
                    .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(phpUnitInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(phpUnitPhp53InfoLabel)
                            .addComponent(installationInfoLabel)
                            .addComponent(phpUnitLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(skelGenLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(phpUnit370InfoLabel))))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(skelGenAbandonedLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {phpUnitBrowseButton, phpUnitSearchButton, skelGenBrowseButton, skelGenSearchButton});

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(phpUnitLabel)
                    .addComponent(phpUnitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(phpUnitSearchButton)
                    .addComponent(phpUnitBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(phpUnitHintLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(skelGenTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(skelGenLabel)
                    .addComponent(skelGenSearchButton)
                    .addComponent(skelGenBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(skelGenHintLabel)
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(phpUnitInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(phpUnitPhp53InfoLabel)
                .addGap(18, 18, 18)
                .addComponent(phpUnit370InfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(skelGenAbandonedLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(installationInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(phpUnitLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(skelGenLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addGap(0, 0, 0))
        );

        phpUnitLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitLabel.AccessibleContext.accessibleName_1")); // NOI18N
        phpUnitLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitLabel.AccessibleContext.accessibleDescription_1")); // NOI18N
        phpUnitTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitTextField.AccessibleContext.accessibleName_1")); // NOI18N
        phpUnitTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitTextField.AccessibleContext.accessibleDescription_1")); // NOI18N
        phpUnitBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitBrowseButton.AccessibleContext.accessibleName_1")); // NOI18N
        phpUnitBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitBrowseButton.AccessibleContext.accessibleDescription_1")); // NOI18N
        phpUnitSearchButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitSearchButton.AccessibleContext.accessibleName_1")); // NOI18N
        phpUnitSearchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitSearchButton.AccessibleContext.accessibleDescription_1")); // NOI18N
        phpUnitHintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.scriptInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        phpUnitHintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.scriptInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        noteLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.noteLabel.AccessibleContext.accessibleName")); // NOI18N
        noteLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.noteLabel.AccessibleContext.accessibleDescription")); // NOI18N
        phpUnitInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        phpUnitInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        phpUnitPhp53InfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitPhp53InfoLabel.AccessibleContext.accessibleName")); // NOI18N
        phpUnitPhp53InfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.phpUnitPhp53InfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        installationInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.installationInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        installationInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.installationInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        phpUnitLearnMoreLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.learnMoreLabel.AccessibleContext.accessibleName")); // NOI18N
        phpUnitLearnMoreLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.learnMoreLabel.AccessibleContext.accessibleDescription")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.errorLabel.AccessibleContext.accessibleName")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.errorLabel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpUnitOptionsPanel.class, "PhpUnitOptionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("PhpUnitOptionsPanel.phpunit.browse.title=Select PHPUnit")
    private void phpUnitBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpUnitBrowseButtonActionPerformed
        File file = new FileChooserBuilder(PhpUnitOptionsPanel.class.getName() + PHP_UNIT_LAST_FOLDER_SUFFIX)
                .setFilesOnly(true)
                .setTitle(Bundle.PhpUnitOptionsPanel_phpunit_browse_title())
                .showOpenDialog();
        if (file != null) {
            phpUnitTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_phpUnitBrowseButtonActionPerformed

    @NbBundle.Messages({
        "PhpUnitOptionsPanel.phpUnit.search.title=PHPUnit scripts",
        "PhpUnitOptionsPanel.phpUnit.search.scripts=&PHPUnit scripts:",
        "PhpUnitOptionsPanel.phpUnit.search.pleaseWaitPart=PHPUnit scripts",
        "PhpUnitOptionsPanel.phpUnit.search.notFound=No PHPUnit scripts found."
    })
    private void phpUnitSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpUnitSearchButtonActionPerformed
        String phpUnit = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {

            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(PhpUnit.SCRIPT_NAME, PhpUnit.SCRIPT_NAME_LONG, PhpUnit.SCRIPT_NAME_PHAR);
            }

            @Override
            public String getWindowTitle() {
                return Bundle.PhpUnitOptionsPanel_phpUnit_search_title();
            }

            @Override
            public String getListTitle() {
                return Bundle.PhpUnitOptionsPanel_phpUnit_search_scripts();
            }

            @Override
            public String getPleaseWaitPart() {
                return Bundle.PhpUnitOptionsPanel_phpUnit_search_pleaseWaitPart();
            }

            @Override
            public String getNoItemsFound() {
                return Bundle.PhpUnitOptionsPanel_phpUnit_search_notFound();
            }
        });
        if (phpUnit != null) {
            phpUnitTextField.setText(phpUnit);
        }
    }//GEN-LAST:event_phpUnitSearchButtonActionPerformed

    private void phpUnitLearnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_phpUnitLearnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_phpUnitLearnMoreLabelMouseEntered

    private void phpUnitLearnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_phpUnitLearnMoreLabelMousePressed
        try {
            URL url = new URL("http://phpunit.de/"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_phpUnitLearnMoreLabelMousePressed

    private void skelGenLearnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_skelGenLearnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_skelGenLearnMoreLabelMouseEntered

    private void skelGenLearnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_skelGenLearnMoreLabelMousePressed
        try {
            // the original skeleton generator(https://github.com/sebastianbergmann/phpunit-skeleton-generator) has been abandoned
            URL url = new URL("https://github.com/VitexSoftware/phpunit-skeleton-generator"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_skelGenLearnMoreLabelMousePressed

    @NbBundle.Messages("PhpUnitOptionsPanel.skelGen.browse=Select Skeleton Generator script")
    private void skelGenBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_skelGenBrowseButtonActionPerformed
        File skelGen = new FileChooserBuilder(PhpUnitOptionsPanel.class.getName() + SKELETON_GENERATOR_LAST_FOLDER_SUFFIX)
                .setTitle(Bundle.PhpUnitOptionsPanel_skelGen_browse())
                .setFilesOnly(true)
                .showOpenDialog();
        if (skelGen != null) {
            skelGen = FileUtil.normalizeFile(skelGen);
            skelGenTextField.setText(skelGen.getAbsolutePath());
        }
    }//GEN-LAST:event_skelGenBrowseButtonActionPerformed

    @NbBundle.Messages({
        "PhpUnitOptionsPanel.skelGen.search.title=Skeleton Generator scripts",
        "PhpUnitOptionsPanel.skelGen.search.scripts=&Skeleton Generator scripts:",
        "PhpUnitOptionsPanel.skelGen.search.pleaseWaitPart=Skeleton Generator scripts",
        "PhpUnitOptionsPanel.skelGen.search.notFound=No Skeleton Generator scripts found."
    })
    private void skelGenSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_skelGenSearchButtonActionPerformed
        String skelGen = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {
            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(SkeletonGenerator.SCRIPT_NAME, SkeletonGenerator.SCRIPT_NAME_LONG, SkeletonGenerator.SCRIPT_NAME_PHAR);
            }
            @Override
            public String getWindowTitle() {
                return Bundle.PhpUnitOptionsPanel_skelGen_search_title();
            }
            @Override
            public String getListTitle() {
                return Bundle.PhpUnitOptionsPanel_skelGen_search_scripts();
            }
            @Override
            public String getPleaseWaitPart() {
                return Bundle.PhpUnitOptionsPanel_skelGen_search_pleaseWaitPart();
            }
            @Override
            public String getNoItemsFound() {
                return Bundle.PhpUnitOptionsPanel_skelGen_search_notFound();
            }
        });
        if (skelGen != null) {
            skelGenTextField.setText(skelGen);
        }
    }//GEN-LAST:event_skelGenSearchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel errorLabel;
    private JLabel installationInfoLabel;
    private JLabel noteLabel;
    private JLabel phpUnit370InfoLabel;
    private JButton phpUnitBrowseButton;
    private JLabel phpUnitHintLabel;
    private JLabel phpUnitInfoLabel;
    private JLabel phpUnitLabel;
    private JLabel phpUnitLearnMoreLabel;
    private JLabel phpUnitPhp53InfoLabel;
    private JButton phpUnitSearchButton;
    private JTextField phpUnitTextField;
    private JLabel skelGenAbandonedLabel;
    private JButton skelGenBrowseButton;
    private JLabel skelGenHintLabel;
    private JLabel skelGenLabel;
    private JLabel skelGenLearnMoreLabel;
    private JButton skelGenSearchButton;
    private JTextField skelGenTextField;
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
