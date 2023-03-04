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
package org.netbeans.modules.php.symfony2.ui.options;

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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.symfony2.commands.InstallerExecutable;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Panel for Symfony 2/3 options.
 */
@NbBundle.Messages({
    "LBL_ZipFilesFilter=Zip File (*.zip)",
    "PhpOptions.Symfony.keywordsTabTitle=Frameworks & Tools",
})
@OptionsPanelController.Keywords(keywords={"php", "symfony", "symfony2", "symfony3", "framework", "sf", "sf2", "sf3"},
        location=UiUtils.OPTIONS_PATH, tabTitle= "#PhpOptions.Symfony.keywordsTabTitle")
public class SymfonyOptionsPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(SymfonyOptionsPanel.class.getName());

    private static final String INSTALLER_LAST_FOLDER_SUFFIX = ".installer"; // NOI18N
    private static final String SANDBOX_LAST_FOLDER_SUFFIX = ".sandbox"; // NOI18N
    private static final FileFilter ZIP_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            return f.isFile()
                    && f.getName().toLowerCase().endsWith(".zip"); // NOI18N
        }
        @Override
        public String getDescription() {
            return Bundle.LBL_ZipFilesFilter();
        }
    };

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public SymfonyOptionsPanel() {
        assert EventQueue.isDispatchThread();
        initComponents();
        init();
    }

    @NbBundle.Messages({
        "# {0} - installer file name",
        "SymfonyOptionsPanel.installer.hint=Full path of Symfony Installer (typically {0}).",
    })
    private void init() {
        installerInfoLabel.setText(Bundle.SymfonyOptionsPanel_installer_hint(InstallerExecutable.NAME));
        errorLabel.setText(" "); // NOI18N
        enableComponents();
        initListeners();
    }

    private void initListeners() {
        DefaultDocumentListener defaultDocumentListener = new DefaultDocumentListener();
        installerTextField.getDocument().addDocumentListener(defaultDocumentListener);
        sandboxTextField.getDocument().addDocumentListener(defaultDocumentListener);
        DefaultItemListener defaultItemListener = new DefaultItemListener();
        installerRadioButton.addItemListener(defaultItemListener);
        sandboxRadioButton.addItemListener(defaultItemListener);
        ignoreCacheCheckBox.addItemListener(defaultItemListener);
    }

    public boolean isUseInstaller() {
        return installerRadioButton.isSelected();
    }

    public void setUseInstaller(boolean useInstaller) {
        if (useInstaller) {
            installerRadioButton.setSelected(true);
        } else {
            sandboxRadioButton.setSelected(true);
        }
    }

    public String getInstaller() {
        return installerTextField.getText();
    }

    public void setInstaller(String installer) {
        installerTextField.setText(installer);
    }

    public String getSandbox() {
        return sandboxTextField.getText();
    }

    public void setSandbox(String sandbox) {
        sandboxTextField.setText(sandbox);
    }

    public boolean getIgnoreCache() {
        return ignoreCacheCheckBox.isSelected();
    }

    public void setIgnoreCache(boolean ignoreCache) {
        ignoreCacheCheckBox.setSelected(ignoreCache);
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

    void enableComponents() {
        boolean useInstaller = installerRadioButton.isSelected();
        installerTextField.setEnabled(useInstaller);
        installerBrowseButton.setEnabled(useInstaller);
        installerSearchButton.setEnabled(useInstaller);
        sandboxTextField.setEnabled(!useInstaller);
        sandboxBrowseButton.setEnabled(!useInstaller);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        useButtonGroup = new ButtonGroup();
        installerRadioButton = new JRadioButton();
        installerTextField = new JTextField();
        installerBrowseButton = new JButton();
        installerSearchButton = new JButton();
        installerInfoLabel = new JLabel();
        sandboxRadioButton = new JRadioButton();
        sandboxTextField = new JTextField();
        sandboxBrowseButton = new JButton();
        sandboxInfoLabel = new JLabel();
        ignoreCacheCheckBox = new JCheckBox();
        errorLabel = new JLabel();
        noteLabel = new JLabel();
        downloadLabel = new JLabel();

        useButtonGroup.add(installerRadioButton);
        Mnemonics.setLocalizedText(installerRadioButton, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.installerRadioButton.text")); // NOI18N

        Mnemonics.setLocalizedText(installerBrowseButton, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.installerBrowseButton.text")); // NOI18N
        installerBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                installerBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(installerSearchButton, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.installerSearchButton.text")); // NOI18N
        installerSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                installerSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(installerInfoLabel, "HINT"); // NOI18N

        useButtonGroup.add(sandboxRadioButton);
        Mnemonics.setLocalizedText(sandboxRadioButton, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.sandboxRadioButton.text")); // NOI18N

        Mnemonics.setLocalizedText(sandboxBrowseButton, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.sandboxBrowseButton.text")); // NOI18N
        sandboxBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sandboxBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(sandboxInfoLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.sandboxInfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(ignoreCacheCheckBox, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.ignoreCacheCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(downloadLabel, NbBundle.getMessage(SymfonyOptionsPanel.class, "SymfonyOptionsPanel.downloadLabel.text")); // NOI18N
        downloadLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                downloadLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                downloadLabelMousePressed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(downloadLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(ignoreCacheCheckBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(errorLabel)
                    .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sandboxRadioButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(sandboxInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(sandboxTextField))))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(sandboxBrowseButton))
            .addGroup(layout.createSequentialGroup()
                .addComponent(installerRadioButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(installerInfoLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(installerTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(installerBrowseButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(installerSearchButton))))
        );
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(installerRadioButton)
                    .addComponent(installerTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(installerSearchButton)
                    .addComponent(installerBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(installerInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(sandboxRadioButton)
                    .addComponent(sandboxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(sandboxBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(sandboxInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(ignoreCacheCheckBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(downloadLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("LBL_SelectSandbox=Select Symfony Standard Edition (.zip)")
    private void sandboxBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sandboxBrowseButtonActionPerformed
        File sandbox = new FileChooserBuilder(SymfonyOptionsPanel.class.getName() + SANDBOX_LAST_FOLDER_SUFFIX)
                .setTitle(Bundle.LBL_SelectSandbox())
                .setFilesOnly(true)
                .setFileFilter(ZIP_FILE_FILTER)
                .showOpenDialog();
        if (sandbox != null) {
            sandbox = FileUtil.normalizeFile(sandbox);
            sandboxTextField.setText(sandbox.getAbsolutePath());
        }
    }//GEN-LAST:event_sandboxBrowseButtonActionPerformed

    private void downloadLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_downloadLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_downloadLabelMouseEntered

    private void downloadLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_downloadLabelMousePressed
        try {
            URL url = new URL("http://symfony.com/download"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }//GEN-LAST:event_downloadLabelMousePressed

    @NbBundle.Messages("SymfonyOptionsPanel.installer.browse.title=Select Symfony Installer")
    private void installerBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_installerBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(SymfonyOptionsPanel.class.getName() + INSTALLER_LAST_FOLDER_SUFFIX)
                .setFilesOnly(true)
                .setTitle(Bundle.SymfonyOptionsPanel_installer_browse_title())
                .showOpenDialog();
        if (file != null) {
            installerTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_installerBrowseButtonActionPerformed

    @NbBundle.Messages({
        "SymfonyOptionsPanel.search.installer.title=Symfony Installers",
        "SymfonyOptionsPanel.search.installer=&Symfony Installers:",
        "SymfonyOptionsPanel.search.installer.pleaseWaitPart=Symfony Installers",
        "SymfonyOptionsPanel.search.installer.notFound=No Symfony Installers found.",
    })
    private void installerSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_installerSearchButtonActionPerformed
        assert EventQueue.isDispatchThread();
        String file = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {
            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(InstallerExecutable.NAME);
            }
            @Override
            public String getWindowTitle() {
                return Bundle.SymfonyOptionsPanel_search_installer_title();
            }
            @Override
            public String getListTitle() {
                return Bundle.SymfonyOptionsPanel_search_installer();
            }
            @Override
            public String getPleaseWaitPart() {
                return Bundle.SymfonyOptionsPanel_search_installer_pleaseWaitPart();
            }
            @Override
            public String getNoItemsFound() {
                return Bundle.SymfonyOptionsPanel_search_installer_notFound();
            }
        });
        if (file != null) {
            installerTextField.setText(file);
        }
    }//GEN-LAST:event_installerSearchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel downloadLabel;
    private JLabel errorLabel;
    private JCheckBox ignoreCacheCheckBox;
    private JButton installerBrowseButton;
    private JLabel installerInfoLabel;
    private JRadioButton installerRadioButton;
    private JButton installerSearchButton;
    private JTextField installerTextField;
    private JLabel noteLabel;
    private JButton sandboxBrowseButton;
    private JLabel sandboxInfoLabel;
    private JRadioButton sandboxRadioButton;
    private JTextField sandboxTextField;
    private ButtonGroup useButtonGroup;
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
            enableComponents();
            fireChange();
        }

    }

}
