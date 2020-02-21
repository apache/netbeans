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

package org.netbeans.modules.subversion.remote.ui.repository;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.config.SvnConfigFiles;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public abstract class ConnectionType implements ActionListener, DocumentListener, FocusListener {
    
    private final static String LOCAL_URL_HELP          = "file:///repository_path[@REV]";              // NOI18N
    private final static String HTTP_URL_HELP           = "http://hostname/repository_path[@REV]";      // NOI18N
    private final static String HTTPS_URL_HELP          = "https://hostname/repository_path[@REV]";     // NOI18N
    private final static String SVN_URL_HELP            = "svn://hostname/repository_path[@REV]";       // NOI18N
    private final static String SVN_SSH_URL_HELP        = "svn+{0}://hostname/repository_path[@REV]";   // NOI18N

    protected final Repository repository;
    private List<JTextField> selectOnFocusFields = null;
    protected final FileSystem fileSystem;

    public ConnectionType(FileSystem fileSystem, Repository repository) {
        this.repository = repository;
        this.fileSystem = fileSystem;
    }

    abstract String getTip(String url);
    abstract JPanel getPanel();
    abstract void setEditable(boolean editable);
    protected void refresh(RepositoryConnection rc) { }
    protected void setEnabled(boolean enabled) { }
    protected void textChanged(Document d) { }
    protected void storeConfigValues() { }
    protected void onSelectedRepositoryChange(String urlString) { }
    protected void showHints(boolean b) { }
    protected void fillRC(RepositoryConnection editedrc) { }
    protected void updateVisibility(String selectedUrlString) { }
    boolean savePassword() { return true; }
    boolean isValid(RepositoryConnection rc) { return true; }

    protected void addSelectOnFocusFields(JTextField... txts) {
        if(selectOnFocusFields == null) {
            selectOnFocusFields = new ArrayList<>();
        }
        selectOnFocusFields.addAll(Arrays.asList(txts));
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        textChanged(e);
    }

    private void textChanged(final DocumentEvent e) {
        // repost later to AWT otherwise it can deadlock because
        // the document is locked while firing event and we try
        // synchronously access its content from selected repository
        Runnable awt = new Runnable() {
            @Override
            public void run() {
                textChanged(e.getDocument());
                repository.validateSvnUrl();
            }
        };
        SwingUtilities.invokeLater(awt);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void focusGained(FocusEvent focusEvent) {
        if(selectOnFocusFields == null) {
            return;
        }
        for (JTextField txt : selectOnFocusFields) {
            if(focusEvent.getSource()==txt) {
                txt.selectAll();
            }
        }
    }

    @Override
    public void focusLost(FocusEvent focusEvent) {
        // do nothing
    }

    protected void onBrowse(JTextField txt) {
        VCSFileProxy oldFile = VCSFileProxySupport.getResource(fileSystem, txt.getText()).normalizeFile();
        JFileChooser fileChooser = VCSFileProxySupport.createFileChooser(oldFile);
        fileChooser.setDialogTitle(NbBundle.getMessage(Repository.class, "Browse_title")); // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showDialog(this.getPanel(), NbBundle.getMessage(Repository.class, "OK_Button")); // NOI18N
        VCSFileProxy f = VCSFileProxySupport.getSelectedFile(fileChooser);
        if (f != null) {
            txt.setText(f.getPath());
        }
    }

    static class FileUrl extends ConnectionType {
        private final JPanel panel = new JPanel();
        public FileUrl(FileSystem fileSystem, Repository repository) {
            super(fileSystem, repository);
        }
        @Override
        JPanel getPanel() {
            return panel;
        }
        @Override
        String getTip(String url) {
            return LOCAL_URL_HELP;
        }
        @Override
        void setEditable(boolean editable) {
        }
    }

    static class InvalidUrl extends FileUrl {
        public InvalidUrl(FileSystem fileSystem, Repository repository) {
            super(fileSystem, repository);
        }
        @Override
        String getTip(String url) {
            return NbBundle.getMessage(Repository.class, "MSG_Repository_Url_Help", new Object [] { // NOI18N
                    LOCAL_URL_HELP, HTTP_URL_HELP, HTTPS_URL_HELP, SVN_URL_HELP, SVN_SSH_URL_HELP
                   });
        }
    }

    static class Http extends ConnectionType {

        private final HttpPanel panel = new HttpPanel();

        public Http(FileSystem fileSystem, Repository repository) {
            super(fileSystem, repository);
            panel.proxySettingsButton.addActionListener(this);
            panel.savePasswordCheckBox.addActionListener(this);

            addSelectOnFocusFields(panel.userPasswordField);
            panel.browseButton.addActionListener(this);

            panel.userPasswordField.getDocument().addDocumentListener(this);
            panel.certPasswordField.getDocument().addDocumentListener(this);
            panel.userPasswordField.addFocusListener(this);
            panel.certPasswordField.addFocusListener(this);

            panel.userTextField.getDocument().addDocumentListener(this);
            panel.certFileTextField.getDocument().addDocumentListener(this);
            panel.proxySettingsButton.setVisible(repository.isSet(Repository.FLAG_SHOW_PROXY));
        }

        @Override
        JPanel getPanel() {
            return panel;
        }

        @Override
        protected void refresh(RepositoryConnection rc) {
            panel.userTextField.setText(rc.getUsername());
            panel.userPasswordField.setText(rc.getPassword() == null ? "" : new String(rc.getPassword())); //NOI18N
            panel.savePasswordCheckBox.setSelected(rc.getSavePassword());
            panel.certFileTextField.setText(rc.getCertFile());
            panel.certPasswordField.setText(rc.getCertPassword() == null ? "" : new String(rc.getCertPassword())); //NOI18N
        }

        @Override
        public void setEnabled(boolean enabled) {
            panel.proxySettingsButton.setEnabled(enabled);
            panel.userPasswordField.setEnabled(enabled);
            panel.userTextField.setEnabled(enabled);
            panel.savePasswordCheckBox.setEnabled(enabled);
            panel.certFileTextField.setEnabled(enabled);
            panel.certPasswordField.setEnabled(enabled);
            panel.browseButton.setEnabled(enabled);
        }

        @Override
        public void setEditable(boolean editable) {
            panel.userPasswordField.setEditable(editable);
            panel.userTextField.setEditable(editable);
            panel.proxySettingsButton.setEnabled(editable);
            panel.savePasswordCheckBox.setEnabled(editable);
            panel.certFileTextField.setEnabled(editable);
            panel.certPasswordField.setEnabled(editable);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == panel.proxySettingsButton) {
                onProxyConfiguration();
            } else if(e.getSource() == panel.savePasswordCheckBox) {
                onSavePasswordChange();
            } else if(e.getSource() == panel.browseButton) {
                onBrowse(panel.certFileTextField);
            } else {
                super.actionPerformed(e);
            }
        }

        private void onProxyConfiguration() {
            OptionsDisplayer.getDefault().open("General");              // NOI18N
        }

        private void onSavePasswordChange() {
            Runnable awt = new Runnable() {
                @Override
                public void run() {
                    RepositoryConnection rc = repository.getSelectedRCIntern();
                    if (rc != null) {
                        rc.setSavePassword(panel.savePasswordCheckBox.isSelected());
                    }
                    repository.validateSvnUrl();
                }
            };
            SwingUtilities.invokeLater(awt);
        }

        @Override
        protected void storeConfigValues() {

        }

        @Override
        protected boolean savePassword() {
            return panel.savePasswordCheckBox.isSelected();
        }

        @Override
        public void onSelectedRepositoryChange(String urlString) {

        }

        @Override
        protected void textChanged(Document d) {
            if (d == panel.userTextField.getDocument()) {
                onUsernameChange(repository.getSelectedRCIntern());
            } else if (d == panel.userPasswordField.getDocument()) {
                onPasswordChange(repository.getSelectedRCIntern());
            } else if (d == panel.certFileTextField.getDocument()) {
                onCertFileChange(repository.getSelectedRCIntern());
            } else if (d == panel.certPasswordField.getDocument()) {
                onCertPasswordChange(repository.getSelectedRCIntern());
            }
        }
        
        private void onUsernameChange(RepositoryConnection rc) {
            if (rc != null) {
                rc.setUsername(panel.userTextField.getText());
            }
            repository.setValid(true, "");
        }

        private void onPasswordChange(RepositoryConnection rc) {
            if (rc != null) {
                rc.setPassword(panel.userPasswordField.getPassword());
            }
            repository.setValid(true, "");
        }

        private void onCertPasswordChange(RepositoryConnection rc) {
            if (rc != null) {
                rc.setCertPassword(panel.certPasswordField.getPassword());
            }
        }

        private void onCertFileChange(RepositoryConnection rc) {
            if (rc != null) {
                rc.setCertFile(panel.certFileTextField.getText());
            }
        }

        @Override
        protected void fillRC(RepositoryConnection editedrc) {
            editedrc.setUsername(panel.userTextField.getText());
            editedrc.setPassword(panel.userPasswordField.getPassword());
            editedrc.setSavePassword(panel.savePasswordCheckBox.isSelected());
            editedrc.setCertFile(panel.certFileTextField.getText());
            editedrc.setCertPassword(panel.certPasswordField.getPassword());
        }

        @Override
        String getTip(String url) {
            if(url.startsWith("http:")) {                             // NOI18N
                return HTTP_URL_HELP;
            } else if(url.startsWith("https:")) {                     // NOI18N
                return HTTPS_URL_HELP;
            } else if(url.startsWith("svn:")) {                       // NOI18N
                return SVN_URL_HELP;
            }
            return null;
        }

        @Override
        protected void updateVisibility(String url) {
            panel.sslPanel.setVisible(url.startsWith("https:")); //NOI18N
        }
    }

    static class SvnSSHCli extends ConnectionType {
        private final SvnSSHCliPanel panel = new SvnSSHCliPanel();
        public SvnSSHCli(FileSystem fileSystem, Repository repository) {
            super(fileSystem, repository);
            panel.tunnelCommandTextField.getDocument().addDocumentListener(this);
        }

        @Override
        JPanel getPanel() {
            return panel;
        }

        @Override
        protected void refresh(RepositoryConnection rc) {
            panel.tunnelCommandTextField.setText(rc.getExternalCommand());
        }

        @Override
        void setEditable(boolean editable) {
            panel.tunnelCommandTextField.setEditable(editable);
        }

        @Override
        protected void showHints(boolean b) {
            panel.tunnelHelpLabel.setVisible(b);
        }

        @Override
        protected void textChanged(Document d) {
            if (d == panel.tunnelCommandTextField.getDocument()) {
                onTunnelCommandChange(repository.getSelectedRCIntern());
            }
        }

        private void onTunnelCommandChange(RepositoryConnection rc) {
            if (rc != null) {
                rc.setExternalCommand(panel.tunnelCommandTextField.getText());
            }
        }

        @Override
        protected void storeConfigValues() {
            RepositoryConnection rc = repository.getSelectedRCIntern();
            if(rc == null) {
                return; // uups
            }

            try {
                SVNUrl repositoryUrl = rc.getSvnUrl();
                if(repositoryUrl.getProtocol().startsWith("svn+")) { //NOI18N
                    SvnConfigFiles.getInstance(fileSystem).setExternalCommand(SvnUtils.getTunnelName(repositoryUrl.getProtocol()), panel.tunnelCommandTextField.getText());
                }
            } catch (MalformedURLException mue) {
                // should not happen
                Subversion.LOG.log(Level.INFO, null, mue);
            }
        }

        @Override
        protected boolean savePassword() {
            return true;
        }

        @Override
        protected boolean isValid(RepositoryConnection rc) {
            return !(rc.getUrl().startsWith("svn+") && panel.tunnelCommandTextField.getText().trim().equals("")); //NOI18N
        }

        @Override
        public void onSelectedRepositoryChange(String urlString) {
            if(urlString.startsWith("svn+")) { //NOI18N
                String tunnelName = SvnUtils.getTunnelName(urlString).trim();
                if( panel.tunnelCommandTextField.getText().trim().equals("") && !tunnelName.isEmpty()) {
                    panel.tunnelCommandTextField.setText(SvnConfigFiles.getInstance(fileSystem).getExternalCommand(tunnelName));
                }
            }
        }

        @Override
        protected void fillRC(RepositoryConnection editedrc) {
            editedrc.setExternalCommand(panel.tunnelCommandTextField.getText());
        }

        @Override
        String getTip(String url) {
            String tunnelName = SvnUtils.getTunnelName(url);
            return MessageFormat.format(SVN_SSH_URL_HELP, tunnelName).trim();
        }
    }
    
}
