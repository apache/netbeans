/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.hudson.php.ui.options;

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
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.hudson.php.options.HudsonOptions;
import org.netbeans.modules.hudson.php.options.HudsonOptionsValidator;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
@NbBundle.Messages({
    "HudsonOptionsPanel.keywords.job=Job",
    "HudsonOptionsPanel.keywords.build=Build"
})
@OptionsPanelController.Keywords(
        location = UiUtils.OPTIONS_PATH, tabTitle= "#LBL_HudsonPHPOptionsName",
        keywords = {"php", "hudson", "jenkins", "job", "build", "phpunit", "#HudsonOptionsPanel.keywords.job", "#HudsonOptionsPanel.keywords.build"})
public class HudsonOptionsPanel extends JPanel {

    private static final long serialVersionUID = -1655785467878L;

    private static final String JOB_CONFIG_LAST_FOLDER_SUFFIX = ".jobConfig";
    private static final String HOME_DIR = System.getProperty("user.home"); // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public HudsonOptionsPanel() {
        initComponents();
        init();
    }

    @NbBundle.Messages("HudsonOptionsPanel.note.config=<html>Template for Jenkins Jobs for PHP Projects is used for new job configurations.</html>")
    private void init() {
        configNoteLabel.setText(Bundle.HudsonOptionsPanel_note_config());
        errorLabel.setText(" "); // NOI18N

        // listeners
        DocumentListener documentListener = new DefaultDocumentListener();
        buildXmlTextField.getDocument().addDocumentListener(documentListener);
        jobConfigTextField.getDocument().addDocumentListener(documentListener);
        phpUnitConfigTextField.getDocument().addDocumentListener(documentListener);
    }

    public String getBuildXml() {
        return buildXmlTextField.getText();
    }

    public void setBuildXml(String buildXml) {
        buildXmlTextField.setText(buildXml);
    }

    public String getJobConfig() {
        return jobConfigTextField.getText();
    }

    public void setJobConfig(String jobConfig) {
        jobConfigTextField.setText(jobConfig);
    }

    public String getPhpUnitConfig() {
        return phpUnitConfigTextField.getText();
    }

    public void setPhpUnitConfig(String phpUnitConfig) {
        phpUnitConfigTextField.setText(phpUnitConfig);
    }

    public void setError(String message) {
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void setWarning(String message) {
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

        buildXmlLabel = new JLabel();
        buildXmlTextField = new JTextField();
        buildXmlBrowseButton = new JButton();
        buildXmlViewLabel = new JLabel();
        jobConfigLabel = new JLabel();
        jobConfigTextField = new JTextField();
        jobConfigBrowseButton = new JButton();
        jobConfigDownloadLabel = new JLabel();
        phpUnitConfigLabel = new JLabel();
        phpUnitConfigTextField = new JTextField();
        phpUnitConfigBrowseButton = new JButton();
        phpUnitConfigViewLabel = new JLabel();
        noteLabel = new JLabel();
        configNoteLabel = new JLabel();
        installationInfoLabel = new JLabel();
        learnMoreLabel = new JLabel();
        errorLabel = new JLabel();

        buildXmlLabel.setLabelFor(buildXmlTextField);
        Mnemonics.setLocalizedText(buildXmlLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.buildXmlLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(buildXmlBrowseButton, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.buildXmlBrowseButton.text")); // NOI18N
        buildXmlBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buildXmlBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(buildXmlViewLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.buildXmlViewLabel.text")); // NOI18N
        buildXmlViewLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                buildXmlViewLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                buildXmlViewLabelMousePressed(evt);
            }
        });

        jobConfigLabel.setLabelFor(jobConfigTextField);
        Mnemonics.setLocalizedText(jobConfigLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.jobConfigLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(jobConfigBrowseButton, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.jobConfigBrowseButton.text")); // NOI18N
        jobConfigBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jobConfigBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(jobConfigDownloadLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.jobConfigDownloadLabel.text")); // NOI18N
        jobConfigDownloadLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                jobConfigDownloadLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                jobConfigDownloadLabelMousePressed(evt);
            }
        });

        phpUnitConfigLabel.setLabelFor(phpUnitConfigTextField);
        Mnemonics.setLocalizedText(phpUnitConfigLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.phpUnitConfigLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpUnitConfigBrowseButton, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.phpUnitConfigBrowseButton.text")); // NOI18N
        phpUnitConfigBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpUnitConfigBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(phpUnitConfigViewLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.phpUnitConfigViewLabel.text")); // NOI18N
        phpUnitConfigViewLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                phpUnitConfigViewLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                phpUnitConfigViewLabelMousePressed(evt);
            }
        });

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(configNoteLabel, "CONFIG NOTE"); // NOI18N

        Mnemonics.setLocalizedText(installationInfoLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.installationInfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(learnMoreLabel, NbBundle.getMessage(HudsonOptionsPanel.class, "HudsonOptionsPanel.learnMoreLabel.text")); // NOI18N
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
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(configNoteLabel)
                    .addComponent(installationInfoLabel)
                    .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(errorLabel))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(buildXmlLabel)
                    .addComponent(jobConfigLabel)
                    .addComponent(phpUnitConfigLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buildXmlTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(buildXmlBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jobConfigTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jobConfigBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(phpUnitConfigTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(phpUnitConfigBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(phpUnitConfigViewLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(jobConfigDownloadLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(buildXmlViewLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {buildXmlBrowseButton, jobConfigBrowseButton, phpUnitConfigBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(buildXmlLabel)
                    .addComponent(buildXmlTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(buildXmlBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(buildXmlViewLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jobConfigTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jobConfigBrowseButton)
                    .addComponent(jobConfigLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jobConfigDownloadLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(phpUnitConfigTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(phpUnitConfigBrowseButton)
                    .addComponent(phpUnitConfigLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(phpUnitConfigViewLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(configNoteLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(installationInfoLabel)
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
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://jenkins-php.org/")); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_learnMoreLabelMousePressed

    private void jobConfigDownloadLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_jobConfigDownloadLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jobConfigDownloadLabelMouseEntered

    private void jobConfigDownloadLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_jobConfigDownloadLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(
                    new URL("https://raw.github.com/sebastianbergmann/php-jenkins-template/master/config.xml")); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_jobConfigDownloadLabelMousePressed

    @NbBundle.Messages({
        "# {0} - file name",
        "HudsonOptionsPanel.jobConfig.browse.title=Select job config ({0})",
        "# {0} - file name",
        "HudsonOptionsPanel.jobConfig.browse.filter=Jenkins job config file ({0})",
    })
    private void jobConfigBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jobConfigBrowseButtonActionPerformed
        File jobConfig = new FileChooserBuilder(HudsonOptionsPanel.class.getName() + JOB_CONFIG_LAST_FOLDER_SUFFIX)
                .setTitle(Bundle.HudsonOptionsPanel_jobConfig_browse_title(HudsonOptionsValidator.JOB_CONFIG_NAME))
                .setFilesOnly(true)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        return HudsonOptionsValidator.JOB_CONFIG_NAME.equals(f.getName());
                    }
                    @Override
                    public String getDescription() {
                        return Bundle.HudsonOptionsPanel_jobConfig_browse_filter(HudsonOptionsValidator.JOB_CONFIG_NAME);
                    }
                }).showOpenDialog();
        if (jobConfig != null) {
            jobConfig = FileUtil.normalizeFile(jobConfig);
            jobConfigTextField.setText(jobConfig.getAbsolutePath());
        }
    }//GEN-LAST:event_jobConfigBrowseButtonActionPerformed

    @NbBundle.Messages({
        "# {0} - file name",
        "HudsonOptionsPanel.buildXml.browse.title=Select build script ({0})",
        "# {0} - file name",
        "HudsonOptionsPanel.buildXml.browse.filter=Project build script file ({0})",
    })
    private void buildXmlBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_buildXmlBrowseButtonActionPerformed
        File buildXml = new FileChooserBuilder(HudsonOptionsPanel.class)
                .setTitle(Bundle.HudsonOptionsPanel_buildXml_browse_title(HudsonOptionsValidator.BUILD_XML_NAME))
                .setDefaultWorkingDirectory(new File(HOME_DIR))
                .setFilesOnly(true)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        return HudsonOptionsValidator.BUILD_XML_NAME.equals(f.getName());
                    }
                    @Override
                    public String getDescription() {
                        return Bundle.HudsonOptionsPanel_buildXml_browse_filter(HudsonOptionsValidator.BUILD_XML_NAME);
                    }
                }).showOpenDialog();
        if (buildXml != null) {
            buildXml = FileUtil.normalizeFile(buildXml);
            buildXmlTextField.setText(buildXml.getAbsolutePath());
        }
    }//GEN-LAST:event_buildXmlBrowseButtonActionPerformed

    @NbBundle.Messages({
        "# {0} - file name 1",
        "# {1} - file name 2",
        "HudsonOptionsPanel.phpUnitConfig.browse.title=Select PHPUnit config ({0} or {1})",
        "# {0} - file name 1",
        "# {1} - file name 2",
        "HudsonOptionsPanel.phpUnitConfig.browse.filter=PHPUnit config file ({0} or {1})",
    })
    private void phpUnitConfigBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpUnitConfigBrowseButtonActionPerformed
        File phpUnitConfig = new FileChooserBuilder(HudsonOptionsPanel.class.getName())
                .setTitle(Bundle.HudsonOptionsPanel_phpUnitConfig_browse_title(
                        HudsonOptionsValidator.PHP_UNIT_CONFIG_NAME, HudsonOptionsValidator.PHP_UNIT_CONFIG_DIST_NAME))
                .setDefaultWorkingDirectory(new File(HOME_DIR))
                .setFilesOnly(true)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        String name = f.getName();
                        return name.equals(HudsonOptionsValidator.PHP_UNIT_CONFIG_NAME)
                                || name.equals(HudsonOptionsValidator.PHP_UNIT_CONFIG_DIST_NAME);
                    }
                    @Override
                    public String getDescription() {
                        return Bundle.HudsonOptionsPanel_phpUnitConfig_browse_filter(
                                HudsonOptionsValidator.PHP_UNIT_CONFIG_NAME, HudsonOptionsValidator.PHP_UNIT_CONFIG_DIST_NAME);
                    }
                }).showOpenDialog();
        if (phpUnitConfig != null) {
            phpUnitConfig = FileUtil.normalizeFile(phpUnitConfig);
            phpUnitConfigTextField.setText(phpUnitConfig.getAbsolutePath());
        }
    }//GEN-LAST:event_phpUnitConfigBrowseButtonActionPerformed

    private void buildXmlViewLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_buildXmlViewLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_buildXmlViewLabelMouseEntered

    private void buildXmlViewLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_buildXmlViewLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://jenkins-php.org/download/build.xml")); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_buildXmlViewLabelMousePressed

    private void phpUnitConfigViewLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_phpUnitConfigViewLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_phpUnitConfigViewLabelMouseEntered

    private void phpUnitConfigViewLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_phpUnitConfigViewLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://jenkins-php.org/download/phpunit.xml.dist")); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_phpUnitConfigViewLabelMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton buildXmlBrowseButton;
    private JLabel buildXmlLabel;
    private JTextField buildXmlTextField;
    private JLabel buildXmlViewLabel;
    private JLabel configNoteLabel;
    private JLabel errorLabel;
    private JLabel installationInfoLabel;
    private JButton jobConfigBrowseButton;
    private JLabel jobConfigDownloadLabel;
    private JLabel jobConfigLabel;
    private JTextField jobConfigTextField;
    private JLabel learnMoreLabel;
    private JLabel noteLabel;
    private JButton phpUnitConfigBrowseButton;
    private JLabel phpUnitConfigLabel;
    private JTextField phpUnitConfigTextField;
    private JLabel phpUnitConfigViewLabel;
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
