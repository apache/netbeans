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

package org.netbeans.modules.subversion.ui.repository;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
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
import org.netbeans.modules.diff.options.AccessibleJFileChooser;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public abstract class ConnectionType implements ActionListener, DocumentListener, FocusListener {
    
    private static final String LOCAL_URL_HELP          = "file:///repository_path[@REV]";              // NOI18N
    private static final String HTTP_URL_HELP           = "http://hostname/repository_path[@REV]";      // NOI18N
    private static final String HTTPS_URL_HELP          = "https://hostname/repository_path[@REV]";     // NOI18N
    private static final String SVN_URL_HELP            = "svn://hostname/repository_path[@REV]";       // NOI18N
    private static final String SVN_SSH_URL_HELP        = "svn+{0}://hostname/repository_path[@REV]";   // NOI18N

    protected final Repository repository;
    private List<JTextField> selectOnFocusFields = null;

    public ConnectionType(Repository repository) {
        this.repository = repository;
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
            selectOnFocusFields = new ArrayList<JTextField>();
        }
        selectOnFocusFields.addAll(Arrays.asList(txts));
    }

    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void changedUpdate(DocumentEvent e) {
        textChanged(e);
    }

    private void textChanged(final DocumentEvent e) {
        // repost later to AWT otherwise it can deadlock because
        // the document is locked while firing event and we try
        // synchronously access its content from selected repository
        Runnable awt = new Runnable() {
            public void run() {
                textChanged(e.getDocument());
                repository.validateSvnUrl();
            }
        };
        SwingUtilities.invokeLater(awt);
    }

    public void actionPerformed(ActionEvent e) {

    }

    public void focusGained(FocusEvent focusEvent) {
        if(selectOnFocusFields == null) return;
        for (JTextField txt : selectOnFocusFields) {
            if(focusEvent.getSource()==txt) {
                txt.selectAll();
            }
        }
    }

    public void focusLost(FocusEvent focusEvent) {
        // do nothing
    }

    protected void onBrowse(JTextField txt) {
        File oldFile = FileUtil.normalizeFile(new File(txt.getText()));
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(Repository.class, "ACSD_BrowseCertFile"), oldFile); // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(Repository.class, "Browse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showDialog(this.getPanel(), NbBundle.getMessage(Repository.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            txt.setText(f.getAbsolutePath());
        }
    }

    static class FileUrl extends ConnectionType {
        private JPanel panel = new JPanel();
        public FileUrl(Repository repository) {
            super(repository);
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
        public InvalidUrl(Repository repository) {
            super(repository);
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

        public Http(Repository repository) {
            super(repository);
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
            panel.sslPanel.setVisible(url.startsWith("https:"));
        }
    }

    static class SvnSSHCli extends ConnectionType {
        private org.netbeans.modules.subversion.ui.repository.SvnSSHCliPanel panel = new org.netbeans.modules.subversion.ui.repository.SvnSSHCliPanel();
        public SvnSSHCli(Repository repository) {
            super(repository);
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
                if(repositoryUrl.getProtocol().startsWith("svn+")) {
                    SvnConfigFiles.getInstance().setExternalCommand(SvnUtils.getTunnelName(repositoryUrl.getProtocol()), panel.tunnelCommandTextField.getText());
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
            return !(rc.getUrl().startsWith("svn+") && panel.tunnelCommandTextField.getText().trim().equals(""));
        }

        @Override
        public void onSelectedRepositoryChange(String urlString) {
            if(urlString.startsWith("svn+")) {
                String tunnelName = SvnUtils.getTunnelName(urlString).trim();
                if( panel.tunnelCommandTextField.getText().trim().equals("") &&
                    tunnelName != null &&
                    !tunnelName.equals("") )
                {
                    panel.tunnelCommandTextField.setText(SvnConfigFiles.getInstance().getExternalCommand(tunnelName));
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

    /*
     * The dialog and settings look the same as for https
     */
    static class SvnSSHSvnKit extends ConnectionType {
        
        private final SvnSSHSvnKitPanel panel = new SvnSSHSvnKitPanel();
        private boolean portNumberValid;
        
        public SvnSSHSvnKit(Repository repository) {
            super(repository);
            panel.proxySettingsButton.addActionListener(this);
            panel.cbSavePassword.addActionListener(this);

            addSelectOnFocusFields(panel.txtPassword);
            panel.browseButton.addActionListener(this);

            panel.txtPassword.getDocument().addDocumentListener(this);
            panel.certPasswordField.getDocument().addDocumentListener(this);
            panel.txtPassword.addFocusListener(this);
            panel.certPasswordField.addFocusListener(this);

            panel.txtUserName.getDocument().addDocumentListener(this);
            panel.txtPort.getDocument().addDocumentListener(this);
            panel.certFileTextField.getDocument().addDocumentListener(this);
            panel.proxySettingsButton.setVisible(repository.isSet(Repository.FLAG_SHOW_PROXY));
        }

        @Override
        String getTip(String url) {
            String tunnelName = getTunnelName(url);
            return MessageFormat.format(SVN_SSH_URL_HELP, tunnelName).trim();
        }

        private String getTunnelName(String urlString) {
            int idx = urlString.indexOf(":", 4);
            if(idx < 0) {
                idx = urlString.length();
            }
            return urlString.substring(4, idx);
        }

        @Override
        JPanel getPanel() {
            return panel;
        }

        @Override
        protected void refresh(RepositoryConnection rc) {
            panel.txtUserName.setText(rc.getUsername());
            int portNumber = rc.getSshPortNumber();
            panel.txtPort.setText(portNumber > 0 ? Integer.toString(portNumber) : ""); //NOI18N
            panel.txtPassword.setText(rc.getPassword() == null ? "" : new String(rc.getPassword())); //NOI18N
            panel.cbSavePassword.setSelected(rc.getSavePassword());
            panel.certFileTextField.setText(rc.getCertFile());
            panel.certPasswordField.setText(rc.getCertPassword() == null ? "" : new String(rc.getCertPassword())); //NOI18N
        }

        @Override
        public void setEnabled(boolean enabled) {
            panel.proxySettingsButton.setEnabled(enabled);
            panel.txtPassword.setEnabled(enabled);
            panel.txtUserName.setEnabled(enabled);
            panel.txtPort.setEnabled(enabled);
            panel.cbSavePassword.setEnabled(enabled);
            panel.certFileTextField.setEnabled(enabled);
            panel.certPasswordField.setEnabled(enabled);
            panel.browseButton.setEnabled(enabled);
        }

        @Override
        public void setEditable(boolean editable) {
            panel.txtPassword.setEditable(editable);
            panel.txtUserName.setEditable(editable);
            panel.txtPort.setEditable(editable);
            panel.proxySettingsButton.setEnabled(editable);
            panel.cbSavePassword.setEnabled(editable);
            panel.certFileTextField.setEnabled(editable);
            panel.certPasswordField.setEnabled(editable);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == panel.proxySettingsButton) {
                onProxyConfiguration();
            } else if(e.getSource() == panel.cbSavePassword) {
                onSavePasswordChange();
            } else if(e.getSource() == panel.browseButton) {
                onBrowse(panel.certFileTextField);
            } else {
                super.actionPerformed(e);
            }
        }

        private void onProxyConfiguration () {
            OptionsDisplayer.getDefault().open("General"); //NOI18N
        }

        private void onSavePasswordChange () {
            Runnable awt = new Runnable() {
                @Override
                public void run() {
                    RepositoryConnection rc = repository.getSelectedRCIntern();
                    if (rc != null) {
                        rc.setSavePassword(panel.cbSavePassword.isSelected());
                    }
                    repository.validateSvnUrl();
                }
            };
            EventQueue.invokeLater(awt);
        }

        @Override
        protected void storeConfigValues () {

        }

        @Override
        protected boolean savePassword () {
            return panel.cbSavePassword.isSelected();
        }

        @Override
        public void onSelectedRepositoryChange (String urlString) {

        }

        @Override
        protected void textChanged (Document d) {
            if (d == panel.txtUserName.getDocument()) {
                onUsernameChange(repository.getSelectedRCIntern());
            } else if (d == panel.txtPort.getDocument()) {
                onPortNumberChange(repository.getSelectedRCIntern());
            } else if (d == panel.txtPassword.getDocument()) {
                onPasswordChange(repository.getSelectedRCIntern());
            } else if (d == panel.certFileTextField.getDocument()) {
                onCertFileChange(repository.getSelectedRCIntern());
            } else if (d == panel.certPasswordField.getDocument()) {
                onCertPasswordChange(repository.getSelectedRCIntern());
            }
        }
        
        private void onUsernameChange (RepositoryConnection rc) {
            if (rc != null) {
                rc.setUsername(panel.txtUserName.getText());
            }
            validateConnection();
        }

        private void onPasswordChange (RepositoryConnection rc) {
            if (rc != null) {
                rc.setPassword(panel.txtPassword.getPassword());
            }
            validateConnection();
        }

        private void onPortNumberChange (RepositoryConnection rc) {
            boolean valid;
            int portNumber = 22;
            if (panel.txtPort.getText().trim().isEmpty()) {
                valid = true;
            } else {
                try {
                    portNumber = Integer.parseInt(panel.txtPort.getText());
                    valid = portNumber > 0 && portNumber <= 65535;
                } catch (NumberFormatException ex) {
                    valid = false;
                }
            }
            if (rc != null && valid) {
                rc.setSshPortNumber(portNumber);
            }
            portNumberValid = valid;
            validateConnection();
        }

        private void onCertPasswordChange (RepositoryConnection rc) {
            if (rc != null) {
                rc.setCertPassword(panel.certPasswordField.getPassword());
            }
        }

        private void onCertFileChange (RepositoryConnection rc) {
            if (rc != null) {
                rc.setCertFile(panel.certFileTextField.getText());
            }
        }

        @Override
        protected void fillRC(RepositoryConnection editedrc) {
            editedrc.setUsername(panel.txtUserName.getText());
            editedrc.setPassword(panel.txtPassword.getPassword());
            editedrc.setSavePassword(panel.cbSavePassword.isSelected());
            editedrc.setCertFile(panel.certFileTextField.getText());
            editedrc.setCertPassword(panel.certPasswordField.getPassword());
            int portNumber = -1;
            try {
                portNumber = Integer.parseInt(panel.txtPort.getText());
            } catch (NumberFormatException ex) { }
            editedrc.setSshPortNumber(portNumber);
        }

        private void validateConnection () {
            if (portNumberValid) {
                repository.setValid(true, "");
            } else {
                repository.setValid(false, NbBundle.getMessage(ConnectionType.class, "MSG_ConnectionType.invalidPort")); //NOI18N
            }
        }

        @Override
        boolean isValid (RepositoryConnection rc) {
            return super.isValid(rc) && portNumberValid;
        }
    }
}
