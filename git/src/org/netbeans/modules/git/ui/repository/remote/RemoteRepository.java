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

package org.netbeans.modules.git.ui.repository.remote;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.libs.git.GitURI;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel.Message;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Stupka
 */
public class RemoteRepository implements DocumentListener, ActionListener, ItemListener {
    private boolean valid;
    private Message msg;

    private ChangeSupport support = new ChangeSupport(this);
    private final boolean urlFixed;
    private final ConnectionSettingsType[] settingTypes;
    private ConnectionSettingsType activeSettingsType;
    private boolean enabled = true;
    private String[] sortedModelUrls;
    private String[] schemeUris;
    private boolean urlComboEnabled = true;

    private enum Scheme {
        FILE("file", NbBundle.getMessage(RemoteRepository.class, "Scheme.FILE")), //NOI18N
        HTTP("http", NbBundle.getMessage(RemoteRepository.class, "Scheme.HTTP")), //NOI18N
        HTTPS("https", NbBundle.getMessage(RemoteRepository.class, "Scheme.HTTPS")), //NOI18N
//        FTP("ftp", NbBundle.getMessage(RemoteRepository.class, "Scheme.FTP")), //NOI18N
//        FTPS("ftps", NbBundle.getMessage(RemoteRepository.class, "Scheme.FTPS")), //NOI18N
        SSH("ssh", NbBundle.getMessage(RemoteRepository.class, "Scheme.SSH")), //NOI18N
        SFTP("sftp", NbBundle.getMessage(RemoteRepository.class, "Scheme.SFTP")), //NOI18N
//        RSYNC("rsync", NbBundle.getMessage(RemoteRepository.class, "Scheme.RSYNC")), //NOI18N
        GIT("git", NbBundle.getMessage(RemoteRepository.class, "Scheme.GIT")); //NOI18N
        
        private final String name;
        private final String tip;

        private Scheme(String name, String tip) {
            this.name = name;
            this.tip = tip;
        };        
         
        private String getTip() {
            return tip;
        }
        
        @Override
        public String toString() {
            return name;
        }
    };
    
    private final RemoteRepositoryPanel panel;
    
    public RemoteRepository(String forPath) {
        this(null, forPath);
    }

    public RemoteRepository(PasswordAuthentication pa, String forPath) {
        this(pa, forPath, false);
    }
    
    private RemoteRepository(PasswordAuthentication pa, String forPath, boolean fixedUrl) {
        assert !fixedUrl || forPath != null && !forPath.trim().isEmpty();
        this.panel = new RemoteRepositoryPanel();
        
        this.urlFixed = fixedUrl;
        settingTypes = new ConnectionSettingsType[] {
            new GitConnectionSettingsType(),
            new SSHConnectionSettingsType(),
            new FileConnectionSettingsType(),
            new DefaultConnectionSettingsType()
        };
        this.activeSettingsType = settingTypes[0];
        initHeight();
        attachListeners();
        initUrlComboValues(forPath, pa);
        updateCurrentSettingsType();
        validateFields();
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public Message getMessage() {
        return msg;
    }
    
    public GitURI getURI() {
        return getURI(true);
    }
    
    private GitURI getURI (boolean trimSpaces) {
        String uriString = getURIString(trimSpaces);        
        if(uriString != null && !uriString.isEmpty()) {
            try {
                return new GitURI(uriString);
            } catch (URISyntaxException ex) {
                Git.LOG.log(Level.INFO, uriString, ex);
            }
        }
        return null;
    }
    
    public void store () {
        activeSettingsType.store();
    }
    
    public void setEnabled (boolean enabled) {
        this.enabled = enabled;
        panel.urlComboBox.setEnabled(enabled && urlComboEnabled);
        for (ConnectionSettingsType type : settingTypes) {
            type.setEnabled(enabled);
        }
    }
    
    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
    }
    
    private String getURIString (boolean trimSpaces) {
        String uriString = (String) panel.urlComboBox.getEditor().getItem();
        return uriString == null ? null : (trimSpaces ? uriString.trim() : uriString);
    }
    
    private void attachListeners () {
        panel.proxySettingsButton.addActionListener(this);
        panel.directoryBrowseButton.addActionListener(this);
        ((JTextComponent) panel.urlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);        
        panel.urlComboBox.addItemListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        uriTextChanged(true);
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        uriTextChanged(false);
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        uriTextChanged(true);
    }

    private void uriTextChanged (final boolean findExisting) {
        if(ignoreComboEvents) return;
        validateFields();
        updateCurrentSettingsType();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                findComboItem(false, findExisting);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if(ae.getSource() == panel.directoryBrowseButton) {
            onBrowse();
        } else if(ae.getSource() == panel.proxySettingsButton) {
            onProxyConfiguration();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        GitURI guri = getURI();
        if(guri != null) {
            ConnectionSettings setts = recentConnectionSettings.get(guri.toString());
            if (setts != null) {
                activeSettingsType.populateFields(setts);
            }
        }
    }

    public static boolean updateFor (String url) {
        boolean retval = false;
        final RemoteRepository repository = new RemoteRepository(null, url, true);
        JPanel panel = repository.getPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        final DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(RemoteRepositoryPanel.class, "ACSD_RepositoryPanel_Title"), //NOI18N
                true, new Object[] { DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION }, DialogDescriptor.OK_OPTION, 
                DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(RemoteRepository.class), null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        repository.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent e) {
                dd.setValid(repository.isValid());
            }
        });
        if (repository.isValid()) {
            dd.setValid(true);
        }
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            repository.activeSettingsType.store();
            retval = true;
        }
        return retval;
    }

    private void validateFields () {
        try {
            valid = true;
            msg = null;
            
            GitURI uri = getURI();
            if(uri == null) {
                valid = false;
                msg = new Message(NbBundle.getMessage(RemoteRepository.class, "MSG_EMPTY_URI_ERROR"), true); // NOI18N
            } else {
                // XXX check suported protocols
            }
        } finally {
            support.fireChange();
        }
    }    
    
    private void updateCurrentSettingsType () {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                GitURI uri = getURI();
                if (uri == null) {
                    return;
                }
                for (ConnectionSettingsType type : settingTypes) {
                    if (type.acceptUri(uri)) {
                        activeSettingsType = type;
                        break;
                    }
                }
                if (urlFixed) {
                    panel.tipLabel.setText(null);
                    activeSettingsType.requestFocusInWindow();
                }
            }
        });
    }

    private void enableUrlCombo (boolean comboEnabled) {
        urlComboEnabled = comboEnabled;
        panel.urlComboBox.setEnabled(comboEnabled && enabled);
    }

    private boolean ignoreComboEvents = false;
    private void findComboItem(boolean selectAll, boolean resetFields) {
        final GitURI uri = getURI(false);
        String uriString = uri == null ? getURIString(false) : uri.setUser(null).setPass(null).toString();
        if(uriString == null || uriString.isEmpty()) {
            return;
        }
        boolean preferSchemeUris = false;
        if (uri != null && uri.getScheme() != null && !Scheme.FILE.name.equals(uri.getScheme()) && uri.getHost() == null) {
            uriString = getURIString(false);
            preferSchemeUris = !resetFields;
            resetFields = true;
        } else if (uriString.endsWith("/") && !getURIString(false).endsWith("/")) { //NOI18N
            // GitURI adds a '/' at the end of its uri string
            uriString = uriString.substring(0, uriString.length() - 1);
        }
        if (!resetFields) {
            return;
        }
        for (String[] uris : preferSchemeUris ? new String[][] { schemeUris, sortedModelUrls } : new String[][] { sortedModelUrls }) {
            for (final String item : uris) {
                if(item.startsWith(uriString)) {
                    final int start = selectAll ? 0 : uriString.length();
                    final int end = item.length();
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ignoreComboEvents = true;
                            try {
                                setComboText(item, start, end);
                                updateCurrentSettingsType();
                                ConnectionSettings setts = recentConnectionSettings.get(item);
                                if (setts != null && uri != null) {
                                    String username = uri.getUser();
                                    username = username == null ? "" : username.trim(); //NOI18N
                                    if (!username.isEmpty() && !username.equals(setts.getUser())) {
                                        setts = setts.copy();
                                        setts.setUser(username);
                                    }
                                }
                                activeSettingsType.populateFields(setts);
                            } finally {
                                ignoreComboEvents = false;
                            }
                        }
                    });
                    return;
                }
            }
        }
    }

    private void setComboText (String item, int start, int end) {
        JTextComponent txt = (JTextComponent)panel.urlComboBox.getEditor().getEditorComponent();
        txt.setText(item);
        txt.setCaretPosition(end);
        txt.moveCaretPosition(start);
    }
    
    private boolean initialized;
    public void waitPopulated() {
        synchronized (this) {
            while (!initialized) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    
                }
            }
        }
    }
    
    private Map<String, ConnectionSettings> recentConnectionSettings = new HashMap<String, ConnectionSettings>();
    private void initUrlComboValues(final String forPath, final PasswordAuthentication pa) {
        enableUrlCombo(false);
        Git.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    final DefaultComboBoxModel model = new DefaultComboBoxModel();
                    
                    try {
                        List<ConnectionSettings> settings = GitModuleConfig.getDefault().getRecentConnectionSettings();
                        for (ConnectionSettings sett : settings) {
                            // strip user/psswd
                            GitURI g = sett.getUri().setPass(null).setUser(null);
                            model.addElement(g.toString());
                            recentConnectionSettings.put(g.toString(), sett);
                        }
                    } catch (Throwable t) {
                        Git.LOG.log(Level.WARNING, null, t);
                    }
                    
                    final List<String> schemeUris = new ArrayList<String>(Scheme.values().length);
                    for (Scheme s : Scheme.values()) {
                        String uri = s.toString() + (s == Scheme.FILE ? ":///" : "://");
                        model.addElement(uri);
                        schemeUris.add(uri);
                    }
                    final String[] uris = new String[model.getSize()];
                    for (int i = 0; i < model.getSize(); i++) {
                        uris[i] = (String) model.getElementAt(i);
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ignoreComboEvents = true;
                            sortedModelUrls = uris;
                            RemoteRepository.this.schemeUris = schemeUris.toArray(new String[schemeUris.size()]);
                            panel.urlComboBox.setModel(model);
                            if (forPath != null) {
                                setComboText(forPath, 0, forPath.length());
                            }
                            ignoreComboEvents = false;
                            if(pa == null) {
                                findComboItem(true, true);
                                updateCurrentSettingsType();
                            } else {
                                updateCurrentSettingsType();
                                activeSettingsType.populateCredentials(pa);
                            }
                            validateFields();
                        }
                    });
                } finally {
                    if (!urlFixed) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                enableUrlCombo(true);
                                synchronized (RemoteRepository.this) {
                                    initialized = true;
                                    RemoteRepository.this.notifyAll();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void initHeight () {
        int maxHeight = 0;
        for (ConnectionSettingsType t : settingTypes) {
            maxHeight = Math.max(maxHeight, t.getPreferedPanelHeight());
        }
        panel.connectionSettings.setPreferredSize(new Dimension(0, maxHeight));
        panel.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded (AncestorEvent event) {
                panel.connectionSettings.setPreferredSize(null);
                panel.invalidate();
                panel.repaint();
                panel.removeAncestorListener(this);
            }

            @Override
            public void ancestorRemoved (AncestorEvent event) {
            }

            @Override
            public void ancestorMoved (AncestorEvent event) {
            }
        });
    }

    private void onBrowse() {
        JTextComponent comboEditor = ((JTextComponent) panel.urlComboBox.getEditor().getEditorComponent());
        String txt = comboEditor.getText();
        if(txt == null || txt.trim().isEmpty()) {
            return;
        }
        File file = null;
        try {
            URI uri = new URI(comboEditor.getText());
            if (uri.isAbsolute() && "file".equalsIgnoreCase(uri.getScheme())) { //NOI18N
                file = new File(uri);
            } else {
                file = new File(comboEditor.getText());
            }
        } catch (URISyntaxException ex) {
            //
        }
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(RemoteRepositoryPanel.class, "RepositoryPanel.FileChooser.Descritpion"), //NOI18N
                file);
        fileChooser.setDialogTitle(NbBundle.getMessage(RemoteRepositoryPanel.class, "RepositoryPanel.FileChooser.Title")); //NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(panel, null);
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            comboEditor.setText(f.toURI().toString());
        }
    }    
    
    private void onProxyConfiguration() {
        OptionsDisplayer.getDefault().open("General");              // NOI18N
    }       
    
    private abstract class ConnectionSettingsType {
        protected abstract void setEnabled (boolean enabled);
        protected abstract void store ();
        protected abstract boolean acceptUri (GitURI uri);
        protected int getPreferedPanelHeight () { return 0; }
        protected void populateFields (ConnectionSettings connSettings) { }
        protected void populateCredentials(PasswordAuthentication pa) { }
        protected void requestFocusInWindow () { }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Connection Setting Types">
    private class DefaultConnectionSettingsType extends ConnectionSettingsType {
        private final JComponent[] inputFields;
        private final UserPasswordPanel settingsPanel;
        private final EnumSet<Scheme> acceptableSchemes;
        
        public DefaultConnectionSettingsType () {
            settingsPanel = new UserPasswordPanel();
            this.inputFields = new JComponent[] {
                settingsPanel.userTextField,
                settingsPanel.userPasswordField,
                settingsPanel.savePasswordCheckBox,
                panel.directoryBrowseButton,
                panel.proxySettingsButton,
                panel.repositoryLabel,
                settingsPanel.userLabel,
                settingsPanel.passwordLabel,
                settingsPanel.leaveBlankLabel,
                panel.tipLabel
            };
            acceptableSchemes = EnumSet.of(Scheme.GIT, Scheme.HTTP, Scheme.HTTPS);
        }
        
        @Override
        protected void setEnabled (boolean enabled) {
            for (JComponent inputField : inputFields) {
                inputField.setEnabled(enabled);
            }
        }
        
        @Override
        protected void populateFields (ConnectionSettings settings) {
            if (settings == null) {
                // reset to defaults
                settingsPanel.userTextField.setText(""); //NOI18N
                settingsPanel.userPasswordField.setText(""); //NOI18N
                settingsPanel.savePasswordCheckBox.setSelected(false);
                return;
            }
            settingsPanel.userTextField.setText(settings.getUser());
            char[] pass = settings.getPassword();
            if (pass != null) {
                settingsPanel.userPasswordField.setText(new String(pass));
            } else {
                settingsPanel.userPasswordField.setText(""); //NOI18N
            }
            settingsPanel.savePasswordCheckBox.setSelected(settings.isSaveCredentials());
        }

        @Override
        protected void populateCredentials(PasswordAuthentication pa) {
            settingsPanel.userTextField.setText(pa.getUserName());
            settingsPanel.userPasswordField.setText(new String(pa.getPassword()));
            settingsPanel.savePasswordCheckBox.setSelected(true);
            settingsPanel.savePasswordCheckBox.setEnabled(false);
        }
        
        @Override
        protected void store () {
            GitURI guri = getURI();
            assert guri != null;
            if(guri == null) {
                return;
            }
            
            final ConnectionSettings settings = new ConnectionSettings(guri);
            settings.setUser(settingsPanel.userTextField.getText());
            settings.setPrivateKeyAuth(false);
            settings.setSaveCredentials(settingsPanel.savePasswordCheckBox.isSelected());
            settings.setPassword(settingsPanel.userPasswordField.getPassword());
            
            Runnable outOfAWT = new Runnable() {
                @Override
                public void run() {
                    GitModuleConfig.getDefault().insertRecentConnectionSettings(settings);
                    recentConnectionSettings.put(settings.getUri().setUser(null).toString(), settings);
                }
            };
            if (EventQueue.isDispatchThread()) {
                Git.getInstance().getRequestProcessor().post(outOfAWT);
            } else {
                outOfAWT.run();
            }
        }
        
        @Override
        protected boolean acceptUri (GitURI uri) {
            boolean accepts = false;
            panel.tipLabel.setText(null);
            if (uri.getScheme() != null) {
                accepts = true; // accept all possible schemes, acts as a default
                for (Scheme s : acceptableSchemes) {
                    if(uri.getScheme().equals(s.toString())) {
                        panel.tipLabel.setText(s.getTip());
                        break;
                    }
                }
            }
            if (accepts) {
                panel.directoryBrowseButton.setVisible(false);
                panel.proxySettingsButton.setVisible(true);
                panel.connectionSettings.removeAll();
                panel.connectionSettings.add(settingsPanel, BorderLayout.NORTH);
            }
            return accepts;
        }

        @Override
        protected int getPreferedPanelHeight () {
            return settingsPanel.getPreferredSize().height;
        }

        @Override
        protected void requestFocusInWindow () {
            settingsPanel.userTextField.requestFocusInWindow();
        }
    }
    
    private final class SSHConnectionSettingsType extends ConnectionSettingsType implements ActionListener {
        private final JComponent[] inputFields;
        private final SSHPanel settingsPanel;
        private final JComponent[] authKeyFields;
        private final JComponent[] authPasswordFields;
        private final EnumSet<Scheme> acceptableSchemes;
        
        public SSHConnectionSettingsType () {
            settingsPanel = new SSHPanel();
            this.inputFields = new JComponent[] {
                settingsPanel.lblUser,
                settingsPanel.lblPassword,
                settingsPanel.lblLeaveBlank,
                settingsPanel.lblIdentityFile,
                settingsPanel.lblPassphrase,
                settingsPanel.userTextField,
                settingsPanel.userPasswordField,
                settingsPanel.savePasswordCheckBox,
                settingsPanel.txtIdentityFile,
                settingsPanel.txtPassphrase,
                settingsPanel.btnBrowse,
                settingsPanel.savePassphrase,
                settingsPanel.rbPrivateKey,
                settingsPanel.rbUsernamePassword,
                panel.directoryBrowseButton,
                panel.proxySettingsButton,
                panel.repositoryLabel,
                panel.tipLabel
            };
            this.authKeyFields = new JComponent[] {
                settingsPanel.lblIdentityFile,
                settingsPanel.lblPassphrase,
                settingsPanel.txtIdentityFile,
                settingsPanel.txtPassphrase,
                settingsPanel.btnBrowse,
                settingsPanel.savePassphrase
            };
            this.authPasswordFields = new JComponent[] {
                settingsPanel.lblPassword,
                settingsPanel.userPasswordField,
                settingsPanel.savePasswordCheckBox
            };
            acceptableSchemes = EnumSet.of(Scheme.SSH, Scheme.SFTP);
            attachListeners();
        }
        
        private void attachListeners () {
            settingsPanel.btnBrowse.addActionListener(this);
            settingsPanel.rbPrivateKey.addActionListener(this);
            settingsPanel.rbUsernamePassword.addActionListener(this);
        }
        
        @Override
        protected void setEnabled (boolean enabled) {
            for (JComponent inputField : inputFields) {
                inputField.setEnabled(enabled);
            }
            if (enabled) {
                updateAuthSelection();
            }
        }
        
        @Override
        protected void populateFields (ConnectionSettings settings) {
            if(settings == null) {
                // reset to defaults
                settingsPanel.userTextField.setText(""); //NOI18N
                settingsPanel.userPasswordField.setText(""); //NOI18N
                settingsPanel.txtPassphrase.setText(""); //NOI18N
                settingsPanel.savePasswordCheckBox.setSelected(false);
                settingsPanel.savePassphrase.setSelected(false);
                settingsPanel.rbPrivateKey.setSelected(false);
                settingsPanel.rbUsernamePassword.setSelected(true);
                String identityFile = getDefaultIdentityFilePath();
                settingsPanel.txtIdentityFile.setText(identityFile);
                return;
            }
            settingsPanel.userTextField.setText(settings.getUser());
            char[] pass = settings.getPassword();
            if (pass != null) {
                settingsPanel.userPasswordField.setText(new String(pass));
            } else {
                settingsPanel.userPasswordField.setText(""); //NOI18N
            }
            pass = settings.getPassphrase();
            if (pass != null) {
                settingsPanel.txtPassphrase.setText(new String(pass));
            } else {
                settingsPanel.txtPassphrase.setText(""); //NOI18N
            }
            settingsPanel.savePasswordCheckBox.setSelected(settings.isSaveCredentials());
            settingsPanel.savePassphrase.setSelected(settings.isSaveCredentials());
            settingsPanel.rbPrivateKey.setSelected(settings.isPrivateKeyAuth());
            settingsPanel.rbUsernamePassword.setSelected(!settings.isPrivateKeyAuth());
            settingsPanel.txtIdentityFile.setText(settings.getIdentityFile());
            updateAuthSelection();
        }
        
        @Override
        protected void store () {
            GitURI guri = getURI();
            assert guri != null;
            if(guri == null) {
                return;
            }
            
            final ConnectionSettings settings = new ConnectionSettings(guri);
            settings.setUser(settingsPanel.userTextField.getText());
            settings.setPrivateKeyAuth(settingsPanel.rbPrivateKey.isSelected());
            settings.setSaveCredentials(settings.isPrivateKeyAuth() ? settingsPanel.savePassphrase.isSelected() : settingsPanel.savePasswordCheckBox.isSelected());
            settings.setPassword(settingsPanel.userPasswordField.getPassword());
            settings.setPassphrase(settingsPanel.txtPassphrase.getPassword());
            settings.setIdentityFile(settingsPanel.txtIdentityFile.getText());
            Runnable outOfAWT = new Runnable() {
                @Override
                public void run() {
                    GitModuleConfig.getDefault().insertRecentConnectionSettings(settings);
                    recentConnectionSettings.put(settings.getUri().setUser(null).toString(), settings);
                }
            };
            if (EventQueue.isDispatchThread()) {
                Git.getInstance().getRequestProcessor().post(outOfAWT);
            } else {
                outOfAWT.run();
            }
        }
        
        @Override
        protected boolean acceptUri (GitURI uri) {
            boolean accepts = false;
            panel.tipLabel.setText(null);
            if (uri.getScheme() == null) {
                if (uri.getHost() != null && uri.getHost().length() != 0) {
                    accepts = true;
                    panel.tipLabel.setText("[user@]host.xz:path/to/repo.git/"); //NOI18N
                }
            } else {
                for (Scheme s : acceptableSchemes) {
                    if(uri.getScheme().equals(s.toString())) {
                        accepts = true;
                        panel.tipLabel.setText(s.getTip());
                        break;
                    }
                }
            }
            if (accepts) {
                panel.directoryBrowseButton.setVisible(false);
                panel.proxySettingsButton.setVisible(true);
                panel.connectionSettings.removeAll();
                panel.connectionSettings.add(settingsPanel, BorderLayout.NORTH);
                updateAuthSelection();
            }
            return accepts;
        }

        @Override
        public final void actionPerformed (ActionEvent e) {
            if (e.getSource() == settingsPanel.btnBrowse) {
                onBrowse();
            } else if (e.getSource() == settingsPanel.rbPrivateKey || e.getSource() == settingsPanel.rbUsernamePassword) {
                updateAuthSelection();
            }
        }
        
        private void onBrowse() {
            String path = settingsPanel.txtIdentityFile.getText();
            if (path.isEmpty()) {
                path = getDefaultIdentityFilePath();
            }
            File file = new File(path);
            JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(RemoteRepositoryPanel.class, "RepositoryPanel.IdentityFile.FileChooser.Descritpion"), //NOI18N
                    path.isEmpty() ? null : file.getParentFile());
            if (!path.isEmpty()) {
                fileChooser.setSelectedFile(file);
            }
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setDialogTitle(NbBundle.getMessage(RemoteRepositoryPanel.class, "RepositoryPanel.IdentityFile.FileChooser.Title")); //NOI18N
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileHidingEnabled(false);
            if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(panel, null)) {
                File f = fileChooser.getSelectedFile();
                settingsPanel.txtIdentityFile.setText(f.getAbsolutePath());
            }
        }

        private void updateAuthSelection () {
            boolean authViaPrivateKey = settingsPanel.rbPrivateKey.isSelected();
            boolean authViaPassword = settingsPanel.rbUsernamePassword.isSelected();
            if (!authViaPassword && !authViaPrivateKey) {
                authViaPassword = true;
                settingsPanel.rbUsernamePassword.setSelected(true);
            }
            authViaPassword &= settingsPanel.rbUsernamePassword.isEnabled();
            authViaPrivateKey &= settingsPanel.rbPrivateKey.isEnabled();
            for (JComponent c : authKeyFields) {
                c.setEnabled(authViaPrivateKey);
            }
            for (JComponent c : authPasswordFields) {
                c.setEnabled(authViaPassword);
            }
        }

        @Override
        protected int getPreferedPanelHeight () {
            return settingsPanel.getPreferredSize().height;
        }

        private String getDefaultIdentityFilePath () {
            String identityFile = ""; //NOI18N
            if (!Utilities.isWindows()) {
                identityFile = System.getProperty("user.home") + File.separator //NOI18N
                        + ".ssh" + File.separator + "id_dsa"; //NOI18N
            }
            return identityFile;
        }

        @Override
        protected void requestFocusInWindow () {
            settingsPanel.userTextField.requestFocusInWindow();
        }
    }
    
    private final class FileConnectionSettingsType extends ConnectionSettingsType {
        private final JComponent[] inputFields;
        private final EnumSet<Scheme> acceptableSchemes;
        
        public FileConnectionSettingsType () {
            this.inputFields = new JComponent[] {
                panel.directoryBrowseButton,
                panel.proxySettingsButton,
                panel.repositoryLabel,
                panel.tipLabel
            };
            acceptableSchemes = EnumSet.of(Scheme.FILE);
        }
        
        @Override
        protected void setEnabled (boolean enabled) {
            for (JComponent inputField : inputFields) {
                inputField.setEnabled(enabled);
            }
        }
        
        @Override
        protected void store () {
            GitURI guri = getURI();
            assert guri != null;
            if (guri == null) {
                return;
            }
            
            final ConnectionSettings settings = new ConnectionSettings(guri);
            Runnable outOfAWT = new Runnable() {
                @Override
                public void run() {
                    GitModuleConfig.getDefault().insertRecentConnectionSettings(settings);
                    recentConnectionSettings.put(settings.getUri().setUser(null).toString(), settings);
                }
            };
            if (EventQueue.isDispatchThread()) {
                Git.getInstance().getRequestProcessor().post(outOfAWT);
            } else {
                outOfAWT.run();
            }
        }
        
        @Override
        protected boolean acceptUri (GitURI uri) {
            boolean accepts = false;
            if (uri.getScheme() == null) {
                accepts = true;
            } else {
                for (Scheme s : acceptableSchemes) {
                    if(uri.getScheme().equals(s.toString())) {
                        accepts = true;
                        break;
                    }
                }
            }
            if (accepts) {
                panel.directoryBrowseButton.setVisible(true);
                panel.proxySettingsButton.setVisible(false);
                panel.connectionSettings.removeAll();

                panel.tipLabel.setText(Scheme.FILE.getTip());
            }
            return accepts;
        }
    }
    
    private class GitConnectionSettingsType extends ConnectionSettingsType {
        private final JComponent[] inputFields;
        
        public GitConnectionSettingsType () {
            this.inputFields = new JComponent[] {
                panel.directoryBrowseButton,
                panel.proxySettingsButton,
                panel.repositoryLabel,
                panel.tipLabel
            };
        }
        
        @Override
        protected void setEnabled (boolean enabled) {
            for (JComponent inputField : inputFields) {
                inputField.setEnabled(enabled);
            }
        }
        
        @Override
        protected void store () {
            GitURI guri = getURI();
            assert guri != null;
            if(guri == null) {
                return;
            }
            
            final ConnectionSettings settings = new ConnectionSettings(guri);
            
            Runnable outOfAWT = new Runnable() {
                @Override
                public void run() {
                    GitModuleConfig.getDefault().insertRecentConnectionSettings(settings);
                    recentConnectionSettings.put(settings.getUri().setUser(null).toString(), settings);
                }
            };
            if (EventQueue.isDispatchThread()) {
                Git.getInstance().getRequestProcessor().post(outOfAWT);
            } else {
                outOfAWT.run();
            }
        }
        
        @Override
        protected boolean acceptUri (GitURI uri) {
            boolean accepts = false;
            panel.tipLabel.setText(null);
            if (uri.getScheme() != null && uri.getScheme().equals(Scheme.GIT.toString())) {
                accepts = true;
                panel.tipLabel.setText(Scheme.GIT.getTip());
            }
            if (accepts) {
                panel.directoryBrowseButton.setVisible(false);
                panel.proxySettingsButton.setVisible(true);
                panel.connectionSettings.removeAll();
            }
            return accepts;
        }
    }
    //</editor-fold>
}
