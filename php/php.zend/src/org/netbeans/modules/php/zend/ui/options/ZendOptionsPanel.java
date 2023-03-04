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

package org.netbeans.modules.php.zend.ui.options;

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
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.zend.ZendScript;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Tomas Mysik
 */
@NbBundle.Messages("PhpOptions.zend.keywords.TabTitle=Frameworks & Tools")
@OptionsPanelController.Keywords(keywords={"php", "zend", "framework", "zf"}, location=UiUtils.OPTIONS_PATH, tabTitle= "#PhpOptions.zend.keywords.TabTitle")
public final class ZendOptionsPanel extends JPanel {
    private static final long serialVersionUID = -13564875423210L;
    private static final String ZEND_LAST_FOLDER_SUFFIX = ".zend";
    private static final RequestProcessor RP = new RequestProcessor(ZendOptionsPanel.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public ZendOptionsPanel() {
        initComponents();

        // hide default options
        defaultParametersLabel.setVisible(false);
        defaultParametersForProjectTextField.setVisible(false);

        // not set in Design because of windows (panel too wide then)
        zendScriptUsageLabel.setText(NbBundle.getMessage(ZendOptionsPanel.class, "LBL_ZendUsage", ZendScript.SCRIPT_NAME_LONG));
        errorLabel.setText(" "); // NOI18N

        zendTextField.getDocument().addDocumentListener(new DocumentListener() {
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
        });

        providerRegistrationButton.setEnabled(ZendScript.validate(getZend()) == null);
    }

    public String getZend() {
        return zendTextField.getText();
    }

    public void setZend(String zend) {
        zendTextField.setText(zend);
    }

    public String getDefaultParamsForProject() {
        return defaultParametersForProjectTextField.getText();
    }

    public void setDefaultParamsForProject(String params) {
        defaultParametersForProjectTextField.setText(params);
    }

    public void setError(String message) {
        providerRegistrationButton.setEnabled(false);
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void setWarning(String message) {
        providerRegistrationButton.setEnabled(false);
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void clearError() {
        setError(" "); // NOI18N
        providerRegistrationButton.setEnabled(true);
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

        zendLabel = new JLabel();
        zendTextField = new JTextField();
        browseButton = new JButton();
        searchButton = new JButton();
        zendScriptUsageLabel = new JLabel();
        providerRegistrationInfoLabel = new JLabel();
        providerRegistrationButton = new JButton();
        defaultParametersLabel = new JLabel();
        defaultParametersForProjectTextField = new JTextField();
        noteLabel = new JLabel();
        includePathInfoLabel = new JLabel();
        installationInfoLabel = new JLabel();
        learnMoreLabel = new JLabel();
        errorLabel = new JLabel();

        zendLabel.setLabelFor(zendTextField);
        Mnemonics.setLocalizedText(zendLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        zendScriptUsageLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(zendScriptUsageLabel, "HINT"); // NOI18N
        Mnemonics.setLocalizedText(providerRegistrationInfoLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.providerRegistrationInfoLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(providerRegistrationButton, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.providerRegistrationButton.text")); // NOI18N
        providerRegistrationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                providerRegistrationButtonActionPerformed(evt);
            }
        });

        defaultParametersLabel.setLabelFor(defaultParametersForProjectTextField);
        Mnemonics.setLocalizedText(defaultParametersLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.defaultParametersLabel.text")); // NOI18N

        noteLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.noteLabel.text")); // NOI18N

        includePathInfoLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(includePathInfoLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.includePathInfoLabel.text")); // NOI18N

        installationInfoLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(installationInfoLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.installationInfoLabel.text")); // NOI18N

        learnMoreLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(learnMoreLabel, NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.learnMoreLabel.text")); // NOI18N
        learnMoreLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                learnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                learnMoreLabelMousePressed(evt);
            }
        });
        learnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                learnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                learnMoreLabelMousePressed(evt);
            }
        });

        errorLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addComponent(zendLabel)

                .addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                        .addComponent(zendScriptUsageLabel)
                        .addContainerGap()).addComponent(providerRegistrationButton, Alignment.TRAILING).addGroup(Alignment.TRAILING, layout.createSequentialGroup()

                        .addComponent(zendTextField, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED).addComponent(browseButton).addPreferredGap(ComponentPlacement.RELATED).addComponent(searchButton)))).addGroup(layout.createSequentialGroup()

                .addGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                        .addComponent(defaultParametersLabel)

                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(defaultParametersForProjectTextField, GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)).addComponent(errorLabel).addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup()

                        .addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(providerRegistrationInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup()
                                .addContainerGap()

                                .addComponent(includePathInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(installationInfoLabel)).addGroup(layout.createSequentialGroup()
                                .addContainerGap()

                                .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))).addGap(0, 0, Short.MAX_VALUE))).addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {browseButton, searchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()

                .addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(zendLabel).addComponent(zendTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(searchButton).addComponent(browseButton)).addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.TRAILING).addGroup(layout.createSequentialGroup()
                        .addComponent(zendScriptUsageLabel)
                        .addGap(18, 18, 18)

                        .addComponent(providerRegistrationInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(providerRegistrationButton)).addGap(18, 18, 18).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(defaultParametersLabel).addComponent(defaultParametersForProjectTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(includePathInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(installationInfoLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED, 16, Short.MAX_VALUE).addComponent(errorLabel).addGap(0, 0, 0))
        );

        zendLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendLabel.AccessibleContext.accessibleName"));         zendLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendLabel.AccessibleContext.accessibleDescription"));         zendTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendTextField.AccessibleContext.accessibleName"));         zendTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendTextField.AccessibleContext.accessibleDescription"));         browseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.browseButton.AccessibleContext.accessibleName"));         browseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.browseButton.AccessibleContext.accessibleDescription"));         searchButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.searchButton.AccessibleContext.accessibleName"));         searchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.searchButton.AccessibleContext.accessibleDescription"));         zendScriptUsageLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendScriptUsageLabel.AccessibleContext.accessibleName"));         zendScriptUsageLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.zendScriptUsageLabel.AccessibleContext.accessibleDescription"));         defaultParametersLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.defaultParametersLabel.AccessibleContext.accessibleName"));         defaultParametersLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.defaultParametersLabel.AccessibleContext.accessibleDescription"));         defaultParametersForProjectTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.defaultParametersForProjectTextField.AccessibleContext.accessibleName"));         defaultParametersForProjectTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.defaultParametersForProjectTextField.AccessibleContext.accessibleDescription"));         noteLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.noteLabel.AccessibleContext.accessibleName"));         noteLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.noteLabel.AccessibleContext.accessibleDescription"));         includePathInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.includePathInfoLabel.AccessibleContext.accessibleName"));         includePathInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.includePathInfoLabel.AccessibleContext.accessibleDescription"));         installationInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.installationInfoLabel.AccessibleContext.accessibleName"));         installationInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.installationInfoLabel.AccessibleContext.accessibleDescription"));         learnMoreLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.learnMoreLabel.AccessibleContext.accessibleName"));         learnMoreLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.learnMoreLabel.AccessibleContext.accessibleDescription"));         errorLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.errorLabel.AccessibleContext.accessibleName"));         errorLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.errorLabel.AccessibleContext.accessibleDescription"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ZendOptionsPanel.class, "ZendOptionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File zendScript = new FileChooserBuilder(ZendOptionsPanel.class.getName() + ZEND_LAST_FOLDER_SUFFIX)
                .setTitle(NbBundle.getMessage(ZendOptionsPanel.class, "LBL_SelectZend"))
                .setFilesOnly(true)
                .showOpenDialog();
        if (zendScript != null) {
            zendScript = FileUtil.normalizeFile(zendScript);
            zendTextField.setText(zendScript.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
         String zendScript = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {
            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(ZendScript.SCRIPT_NAME, ZendScript.SCRIPT_NAME_LONG);
            }

            @Override
            public String getWindowTitle() {
                return NbBundle.getMessage(ZendOptionsPanel.class, "LBL_ZendScriptsTitle");
            }

            @Override
            public String getListTitle() {
                return NbBundle.getMessage(ZendOptionsPanel.class, "LBL_ZendScripts");
            }

            @Override
            public String getPleaseWaitPart() {
                return NbBundle.getMessage(ZendOptionsPanel.class, "LBL_ZendScriptsPleaseWaitPart");
            }

            @Override
            public String getNoItemsFound() {
                return NbBundle.getMessage(ZendOptionsPanel.class, "LBL_NoZendScriptsFound");
            }
        });
        if (zendScript != null) {
            zendTextField.setText(zendScript);
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void learnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_learnMoreLabelMouseEntered

    private void learnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMousePressed
        try {
            URL url = new URL("https://framework.zend.com/learn"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_learnMoreLabelMousePressed

    private void providerRegistrationButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_providerRegistrationButtonActionPerformed
        // #180347
        boolean register = true;
        final String zendScript = getZend();
        if (!zendScript.equals(ZendOptions.getInstance().getZend())) {
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(ZendOptionsPanel.class, "MSG_RegisterUsingUnsavedZendScript", zendScript),
                    NotifyDescriptor.YES_NO_OPTION);
            register = DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION;
        }
        if (register) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        ZendScript.getCustom(zendScript).registerNetBeansProvider();
                    } catch (InvalidPhpExecutableException ex) {
                        assert false : ex.getLocalizedMessage();
                    }
                }
            });
        }
    }//GEN-LAST:event_providerRegistrationButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton browseButton;
    private JTextField defaultParametersForProjectTextField;
    private JLabel defaultParametersLabel;
    private JLabel errorLabel;
    private JLabel includePathInfoLabel;
    private JLabel installationInfoLabel;
    private JLabel learnMoreLabel;
    private JLabel noteLabel;
    private JButton providerRegistrationButton;
    private JLabel providerRegistrationInfoLabel;
    private JButton searchButton;
    private JLabel zendLabel;
    private JLabel zendScriptUsageLabel;
    private JTextField zendTextField;
    // End of variables declaration//GEN-END:variables

}
