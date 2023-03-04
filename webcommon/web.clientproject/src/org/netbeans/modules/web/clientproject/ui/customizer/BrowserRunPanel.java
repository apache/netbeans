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
package org.netbeans.modules.web.clientproject.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.EnumSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.api.BrowserUISupport.BrowserComboBoxModel;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.ui.BrowseFolders;
import org.netbeans.modules.web.clientproject.ui.customizer.ClientSideProjectProperties.ProjectServer;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.clientproject.validation.ProjectFoldersValidator;
import org.netbeans.modules.web.clientproject.validation.RunProjectValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.api.WebServer;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author david
 */
public class BrowserRunPanel extends JPanel implements DocumentListener, ItemListener {

    private static final long serialVersionUID = 98712411454L;

    private final ClientSideProject project;
    private final ComboBoxModel webServerModel;
    private final ClientSideProjectProperties uiProperties;
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    BrowserRunPanel(ClientSideProjectProperties uiProperties) {
        assert uiProperties != null;

        this.uiProperties = uiProperties;
        project = uiProperties.getProject();
        webServerModel = new DefaultComboBoxModel(ClientSideProjectProperties.ProjectServer.values());

        initComponents();
        init();
        initListeners();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        File siteRoot = getSiteRoot();
        ValidationResult result = new ProjectFoldersValidator()
                .validateSiteRootFolder(siteRoot)
                .getResult();
        boolean siteRootValid = siteRoot != null
                && !result.hasErrors();
        String info;
        if (siteRootValid) {
            assert siteRoot != null;
            info = NbBundle.getMessage(BrowserRunPanel.class, "URL_DESCRIPTION", siteRoot.getAbsolutePath());
        } else {
            info = " "; // NOI18N
        }
        projectUrlDescriptionLabel.setText(info);
        startFileTextField.setEnabled(siteRootValid);
        startFileBrowseButton.setEnabled(siteRootValid);
        storeAndFireChange();
    }

    private void init() {
        // start file
        startFileTextField.setText(uiProperties.getStartFile());
        // server
        webServerComboBox.setModel(webServerModel);
        webServerComboBox.setRenderer(new ServerRenderer(webServerComboBox.getRenderer()));
        webServerComboBox.setSelectedItem(uiProperties.getProjectServer());
        //jServerComboBox.setSelectedIndex(cfg.isUseServer() ? 1 : 0); // XXX: indexes are obsolete, use enums directly
        // url
        projectUrlTextField.setText(uiProperties.getProjectUrl());
        // web root
        webRootTextField.setText(uiProperties.getWebRoot());
        // configuration customizer
        updateConfigurationCustomizer();
    }

    private void initListeners() {
        // config
        browserComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged( ItemEvent e ) {
                if( e.getStateChange() == ItemEvent.DESELECTED )
                    return;
                storeAndFireChange();
            }
        });
        // start file
        startFileTextField.getDocument().addDocumentListener(this);
        // server
        webServerComboBox.addItemListener(this);
        // url
        projectUrlTextField.getDocument().addDocumentListener(this);
        // web root
        webRootTextField.getDocument().addDocumentListener(this);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getErrorMessage() {
        ValidationResult result = validateData();
        if (result.hasErrors()) {
            return result.getErrors().get(0).getMessage();
        }
        return null;
    }

    public String getWarningMessage() {
        ValidationResult result = validateData();
        if (result.hasWarnings()) {
            return result.getWarnings().get(0).getMessage();
        }
        return null;
    }

    public void onlyExternalUrl(boolean enable) {
        if (enable) {
            webServerComboBox.setSelectedItem(ClientSideProjectProperties.ProjectServer.EXTERNAL);
            projectUrlLabel.setVisible(true);
            projectUrlTextField.setVisible(true);
            projectUrlDescriptionLabel.setVisible(true);
            webServerLabel.setVisible(false);
            webServerComboBox.setVisible(false);
            webRootLabel.setVisible(false);
            webRootTextField.setVisible(false);
            webRootExampleLabel.setVisible(false);
        } else {
            updateConfigurationCustomizer();
        }
    }

    void storeAndFireChange() {
        storeData();
        changeSupport.fireChange();
    }

    private ValidationResult validateData() {
        RunProjectValidator validator = new RunProjectValidator()
                .validateStartFile(getSiteRoot(), getResolvedStartFile());
        if (projectUrlTextField.isVisible()) {
            validator.validateProjectUrl(getProjectUrl());
        }
        return validator.getResult();
    }

    private void storeData() {
        uiProperties.setStartFile(getStartFile());
        uiProperties.setProjectServer(getProjectServer());
        uiProperties.setProjectUrl(getProjectUrl());
        uiProperties.setWebRoot(getWebRoot());
        uiProperties.setSelectedBrowser(getSelectedBrowserId());
    }

    private void updateConfigurationCustomizer() {
        configurationPlaceholder.removeAll();
        WebBrowser wb = getSelectedBrowser();
        ClientProjectEnhancedBrowserImplementation enhancedBrowser =
                uiProperties.createEnhancedBrowserSettings(wb);
        if (enhancedBrowser != null) {
            ProjectConfigurationCustomizer customizerPanel = enhancedBrowser.getProjectConfigurationCustomizer();
            if (customizerPanel != null) {
                configurationPlaceholder.add(customizerPanel.createPanel(), BorderLayout.CENTER);
                EnumSet<ProjectConfigurationCustomizer.HiddenProperties> hiddenProperties = customizerPanel.getHiddenProperties();
                showWebServer(!hiddenProperties.contains(ProjectConfigurationCustomizer.HiddenProperties.WEB_SERVER));
            } else {
                showWebServer(true);
            }
        }
        validate();
        repaint();
    }

    private void showWebServer(boolean visible) {
        webServerLabel.setVisible(visible);
        webServerComboBox.setVisible(visible);
        projectUrlDescriptionLabel.setVisible(visible);
        projectUrlLabel.setVisible(visible);
        projectUrlTextField.setVisible(visible);
        webRootExampleLabel.setVisible(visible);
        webRootLabel.setVisible(visible);
        webRootTextField.setVisible(visible);
        if (visible) {
            updateWebRootEnablement();
        } else {
            storeAndFireChange();
        }
    }

    @CheckForNull
    private File getSiteRoot() {
        return uiProperties.getResolvedSiteRootFolder();
    }

    private String getStartFile() {
        return startFileTextField.getText();
    }

    private String getSelectedBrowserId() {
        return ((BrowserComboBoxModel)browserComboBox.getModel()).getSelectedBrowserId();
    }

    private WebBrowser getSelectedBrowser() {
        return ((BrowserComboBoxModel)browserComboBox.getModel()).getSelectedBrowser();
    }

    @CheckForNull
    private File getResolvedStartFile() {
        String startFile = getStartFile();
        if (startFile == null) {
            return null;
        }
        // drop fragment from the path:
        startFile = ClientSideProjectUtilities.splitPathAndFragment(startFile)[0];
        File directFile = new File(startFile);
        if (directFile.isAbsolute()) {
            return directFile;
        }
        File siteRoot = getSiteRoot();
        if (siteRoot == null) {
            return null;
        }
        FileObject siteRootFo = FileUtil.toFileObject(siteRoot);
        if (siteRootFo == null) {
            return null;
        }
        FileObject fo = siteRootFo.getFileObject(startFile);
        if (fo == null) {
            return null;
        }
        return FileUtil.toFile(fo);
    }

    private ClientSideProjectProperties.ProjectServer getProjectServer() {
        return (ProjectServer) webServerComboBox.getSelectedItem();
    }

    private String getProjectUrl() {
        return projectUrlTextField.getText();
    }

    private String getWebRoot() {
        return webRootTextField.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        browserLabel = new javax.swing.JLabel();
        browserComboBox = createBrowserComboBox();
        configurationPlaceholder = new javax.swing.JPanel();
        startFileLabel = new javax.swing.JLabel();
        startFileTextField = new javax.swing.JTextField();
        startFileBrowseButton = new javax.swing.JButton();
        webServerLabel = new javax.swing.JLabel();
        webServerComboBox = new javax.swing.JComboBox();
        projectUrlLabel = new javax.swing.JLabel();
        projectUrlTextField = new javax.swing.JTextField();
        projectUrlDescriptionLabel = new javax.swing.JLabel();
        webRootLabel = new javax.swing.JLabel();
        webRootTextField = new javax.swing.JTextField();
        webRootExampleLabel = new javax.swing.JLabel();

        browserLabel.setLabelFor(browserComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(browserLabel, org.openide.util.NbBundle.getMessage(BrowserRunPanel.class, "BrowserRunPanel.browserLabel.text")); // NOI18N

        browserComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browserComboBoxActionPerformed(evt);
            }
        });

        configurationPlaceholder.setLayout(new java.awt.BorderLayout());

        startFileLabel.setLabelFor(startFileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(startFileLabel, org.openide.util.NbBundle.getMessage(BrowserRunPanel.class, "BrowserRunPanel.startFileLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(startFileBrowseButton, org.openide.util.NbBundle.getMessage(BrowserRunPanel.class, "BrowserRunPanel.startFileBrowseButton.text")); // NOI18N
        startFileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startFileBrowseButtonActionPerformed(evt);
            }
        });

        webServerLabel.setLabelFor(webServerComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(webServerLabel, org.openide.util.NbBundle.getMessage(BrowserRunPanel.class, "BrowserRunPanel.webServerLabel.text")); // NOI18N

        projectUrlLabel.setLabelFor(projectUrlTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectUrlLabel, org.openide.util.NbBundle.getMessage(BrowserRunPanel.class, "BrowserRunPanel.projectUrlLabel.text")); // NOI18N

        projectUrlDescriptionLabel.setFont(projectUrlDescriptionLabel.getFont().deriveFont(projectUrlDescriptionLabel.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(projectUrlDescriptionLabel, org.openide.util.NbBundle.getMessage(BrowserRunPanel.class, "BrowserRunPanel.projectUrlDescriptionLabel.text")); // NOI18N
        projectUrlDescriptionLabel.setEnabled(false);

        webRootLabel.setLabelFor(webRootTextField);
        org.openide.awt.Mnemonics.setLocalizedText(webRootLabel, org.openide.util.NbBundle.getMessage(BrowserRunPanel.class, "BrowserRunPanel.webRootLabel.text")); // NOI18N

        webRootExampleLabel.setFont(webRootExampleLabel.getFont().deriveFont(webRootExampleLabel.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(webRootExampleLabel, "EXAMPLE"); // NOI18N
        webRootExampleLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(browserLabel)
                    .addComponent(webServerLabel)
                    .addComponent(startFileLabel)
                    .addComponent(webRootLabel)
                    .addComponent(projectUrlLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(webRootExampleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startFileTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startFileBrowseButton))
                    .addComponent(browserComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(webServerComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(projectUrlTextField)
                    .addComponent(webRootTextField)
                    .addComponent(configurationPlaceholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(projectUrlDescriptionLabel)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(browserLabel)
                    .addComponent(browserComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configurationPlaceholder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startFileLabel)
                    .addComponent(startFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startFileBrowseButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(webServerLabel)
                    .addComponent(webServerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectUrlLabel)
                    .addComponent(projectUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectUrlDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(webRootLabel)
                    .addComponent(webRootTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webRootExampleLabel)
                .addGap(0, 2, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void startFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startFileBrowseButtonActionPerformed
        FileObject siteRootFolder = FileUtil.toFileObject(getSiteRoot());
        assert siteRootFolder != null;
        FileObject selectedFile = BrowseFolders.showDialog(new FileObject[] {siteRootFolder}, DataObject.class, getStartFile());
        if (selectedFile != null) {
            startFileTextField.setText(FileUtil.getRelativePath(siteRootFolder, selectedFile));
        }
    }//GEN-LAST:event_startFileBrowseButtonActionPerformed

    private void browserComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browserComboBoxActionPerformed
        updateConfigurationCustomizer();
    }//GEN-LAST:event_browserComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox browserComboBox;
    private javax.swing.JLabel browserLabel;
    private javax.swing.JPanel configurationPlaceholder;
    private javax.swing.JLabel projectUrlDescriptionLabel;
    private javax.swing.JLabel projectUrlLabel;
    private javax.swing.JTextField projectUrlTextField;
    private javax.swing.JButton startFileBrowseButton;
    private javax.swing.JLabel startFileLabel;
    private javax.swing.JTextField startFileTextField;
    private javax.swing.JLabel webRootExampleLabel;
    private javax.swing.JLabel webRootLabel;
    private javax.swing.JTextField webRootTextField;
    private javax.swing.JComboBox webServerComboBox;
    private javax.swing.JLabel webServerLabel;
    // End of variables declaration//GEN-END:variables

    @NbBundle.Messages({
        "# {0} - part of URL",
        "BrowserRunPanel.webRoot.example=<html>The project''s URL will be http://localhost:{0}",
    })
    private void updateWebRooExample() {
        if (!webRootTextField.isVisible()) {
            return;
        }
        if (!webRootTextField.isEnabled()) {
            webRootExampleLabel.setText(" "); //NOI18N
            return;
        }
        StringBuilder s = new StringBuilder();
        s.append(WebServer.getWebserver().getPort());
        String ctx = webRootTextField.getText();
        if (ctx.trim().length() == 0) {
            s.append("/"); //NOI18N
        } else {
            if (!ctx.startsWith("/")) { //NOI18N
                s.append("/"); //NOI18N
            }
            s.append(ctx);
        }
        webRootExampleLabel.setText(Bundle.BrowserRunPanel_webRoot_example(s.toString()));
    }

    private boolean isEmbeddedServer() {
        return webServerComboBox.getSelectedItem() == ClientSideProjectProperties.ProjectServer.INTERNAL;
    }

    private void updateWebRootEnablement() {
        webRootTextField.setVisible(isEmbeddedServer());
        webRootLabel.setVisible(isEmbeddedServer());
        webRootExampleLabel.setVisible(isEmbeddedServer());
        projectUrlLabel.setVisible(!isEmbeddedServer());
        projectUrlTextField.setVisible(!isEmbeddedServer());
        projectUrlDescriptionLabel.setVisible(!isEmbeddedServer());
        updateWebRooExample();
    }


    @Override
    public void insertUpdate(DocumentEvent e) {
        updateWebRooExample();
        storeAndFireChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateWebRooExample();
        storeAndFireChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateWebRooExample();
        storeAndFireChange();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        updateWebRootEnablement();
        storeAndFireChange();
    }

    private JComboBox createBrowserComboBox() {
        String selectedBrowser = uiProperties.getSelectedBrowser();
        if (selectedBrowser == null || BrowserUISupport.getBrowser(selectedBrowser) == null) {
            WebBrowser wb = project.getProjectWebBrowser();
            if (wb != null) {
                selectedBrowser = wb.getId();
            }
        }
        return BrowserUISupport.createBrowserPickerComboBox( selectedBrowser, false, true );
    }

    //~ Inner classes

    private static final class ConfigRenderer implements ListCellRenderer {

        private final ListCellRenderer original;

        public ConfigRenderer(ListCellRenderer original) {
            this.original = original;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof ProjectConfiguration) {
                value = ((ProjectConfiguration) value).getDisplayName();
            }
            return original.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }

    private static final class ServerRenderer implements ListCellRenderer {

        private final ListCellRenderer original;


        public ServerRenderer(ListCellRenderer original) {
            this.original = original;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            value = ((ClientSideProjectProperties.ProjectServer) value).getTitle();
            return original.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }

}
