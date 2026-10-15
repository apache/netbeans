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

package org.netbeans.modules.git.ui.repository.remote;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import javax.swing.event.ChangeEvent;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitURI;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.git.utils.WizardStepProgressSupport;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.AsynchronousValidatingPanel;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import static org.netbeans.modules.git.ui.repository.remote.Bundle.*;

/**
 *
 * @author ondra
 */
public class SelectUriStep extends AbstractWizardPanel implements ActionListener, ItemListener, ChangeListener, DocumentListener, AsynchronousValidatingPanel<WizardDescriptor> {

    private final Map<String, GitRemoteConfig> remotes;
    private final SelectUriPanel panel;
    private final JComponent[] inputFields;
    private GitProgressSupport supp;
    private final File repositoryFile;
    private Map<String, GitBranch> remoteBranches;
    private Map<String, String> remoteTags;
    private final RemoteRepository repository;
    private final Mode mode;
    private final Timer remoteNameEditTimer;

    public static enum Mode {
        PULL("pull"), //NOI18N
        PUSH("push"), //NOI18N
        FETCH("fetch"); //NOI18N

        private String helpId;
        
        Mode (String helpId) {
            this.helpId = helpId;
        }
    }

    public SelectUriStep (File repositoryFile, Map<String, GitRemoteConfig> remotes, Mode mode) {
        this.repositoryFile = repositoryFile;
        this.repository = new RemoteRepository(null);
        this.panel = new SelectUriPanel(repository.getPanel());
        this.remotes = remotes;
        this.inputFields = new JComponent[] {
            panel.cmbConfiguredRepositories,
            panel.rbConfiguredUri,
            panel.rbCreateNew,
            panel.lblRemoteNames,
            panel.cbPersistRemote,
            panel.cmbRemoteNames
        };
        this.mode = mode;
        remoteNameEditTimer = new Timer(300, this);
        remoteNameEditTimer.setRepeats(false);
        remoteNameEditTimer.stop();
        fillPanel();
        attachListeners();
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                enableFields();
                validateBeforeNext();
            }
        });
    }
    
    private void fillPanel () {
        LinkedList<RemoteUri> list = new LinkedList<RemoteUri>();
        for (Map.Entry<String, GitRemoteConfig> e : remotes.entrySet()) {
            boolean empty = true;
            if (mode == Mode.PUSH) {
                for (String uri : e.getValue().getPushUris()) {
                    list.add(new RemoteUri(e.getKey(), uri));
                    empty = false;
                }
            }
            if (empty) {
                for (String uri : e.getValue().getUris()) {
                    list.add(new RemoteUri(e.getKey(), uri));
                }
            }
        }
        RemoteUri[] uris = list.toArray(new RemoteUri[0]);
        Arrays.sort(uris);
        panel.cmbConfiguredRepositories.removeAllItems();
        panel.cmbConfiguredRepositories.setModel(new DefaultComboBoxModel(uris));
        panel.rbCreateNew.setSelected(list.isEmpty());
        if (!list.isEmpty()) {
            panel.cmbConfiguredRepositories.setSelectedIndex(0);
        }
        panel.rbConfiguredUri.setSelected(!panel.rbCreateNew.isSelected());
        if (remotes.isEmpty()) {
            panel.cmbRemoteNames.setModel(new DefaultComboBoxModel(new String[] { GitUtils.REMOTE_ORIGIN }));
        } else {
            panel.cmbRemoteNames.setModel(new DefaultComboBoxModel(remotes.keySet().toArray()));
        }
    }

    private void attachListeners () {
        panel.rbCreateNew.addActionListener(this);
        panel.rbConfiguredUri.addActionListener(this);
        panel.cbPersistRemote.addActionListener(this);
        panel.cmbConfiguredRepositories.addActionListener(this);
        panel.cmbRemoteNames.addItemListener(this);
        ((JTextComponent) panel.cmbRemoteNames.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        repository.addChangeListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.rbCreateNew || e.getSource() == panel.rbConfiguredUri || e.getSource() == panel.cmbConfiguredRepositories) {
            enableFields();
            validateBeforeNext();
        } else if (e.getSource() == remoteNameEditTimer) {
            validateBeforeNext();
        } else if (e.getSource() == panel.cbPersistRemote) {
            validateBeforeNext();
        }
    }

    @Override
    public void itemStateChanged (ItemEvent e) {
        validateBeforeNext();
    }
    
    @Override
    public void insertUpdate (DocumentEvent e) {
        restartTimer();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        restartTimer();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
        restartTimer();
    }

    private void restartTimer () {
        remoteNameEditTimer.restart();
    }

    private void enableFields () {
        repository.setEnabled(panel.rbCreateNew.isSelected());
        panel.cmbConfiguredRepositories.setEnabled(panel.rbConfiguredUri.isSelected());
        panel.cmbRemoteNames.setEnabled(panel.rbCreateNew.isSelected());
        panel.lblRemoteNames.setEnabled(panel.rbCreateNew.isSelected());
        panel.cbPersistRemote.setEnabled(panel.rbCreateNew.isSelected());
    }

    @Override
    @NbBundle.Messages({
        "# {0} - repository URL", "MSG_SelectUriStep.errorCredentials=Incorrect credentials for repository at {0}",
        "# {0} - repository URL", "MSG_SelectUriStep.errorCannotConnect=Cannot connect to the remote repository at {0}"
    })
    protected final boolean validateBeforeNext () {
        boolean valid = true;
        Message msg = null;
        final boolean newRepositorySpecification = panel.rbCreateNew.isSelected();
        if (panel.rbConfiguredUri.isSelected()) {
            if (panel.cmbConfiguredRepositories.getSelectedIndex() == -1) {
                msg = new Message(NbBundle.getMessage(SelectUriStep.class, "MSG_SelectUriStep.errorEmptySelection"), false); //NOI18N
                valid = false;
            }
        } else if (panel.rbCreateNew.isSelected()) {
            valid = repository.isValid();
            msg = repository.getMessage();
            if (valid && (mode != Mode.PUSH || panel.cbPersistRemote.isSelected())
                    && ((JTextComponent) panel.cmbRemoteNames.getEditor().getEditorComponent()).getText().trim().isEmpty()) {
                valid = false;
                msg = new Message(NbBundle.getMessage(SelectUriStep.class, "MSG_SelectUriStep.errorEmptyRemoteName"), false); //NOI18N
            }
        }
        setValid(valid, msg);
        if (valid && !EventQueue.isDispatchThread()) {
            final Message[] message = new Message[1];
            supp = new WizardStepProgressSupport(panel.progressPanel, true) {
                @Override
                protected void perform () {
                    String uri = getSelectedUri();
                    GitClient client = null;
                    try {
                        if (newRepositorySpecification) {
                            repository.store();
                            client = Git.getInstance().getClient(getRepositoryRoot(), this, false);
                        } else {
                            client = getClient();
                        }
                        remoteBranches = client.listRemoteBranches(uri, getProgressMonitor());
                        if (!isCanceled() && mode == Mode.PUSH) {
                            remoteTags = client.listRemoteTags(uri, getProgressMonitor());
                        }
                    } catch (GitException.AuthorizationException ex) {
                        Logger.getLogger(SelectUriStep.class.getName()).log(Level.INFO, "Auth failed when accessing " + uri, ex); //NOI18N
                        message[0] = new Message(Bundle.MSG_SelectUriStep_errorCredentials(uri), false);
                    } catch (GitException ex) {
                        if (panel.rbCreateNew.isSelected()) {
                            GitModuleConfig.getDefault().removeConnectionSettings(repository.getURI());
                        }
                        Logger.getLogger(SelectUriStep.class.getName()).log(Level.INFO, "Cannot connect to " + uri, ex); //NOI18N
                        message[0] = new Message(Bundle.MSG_SelectUriStep_errorCannotConnect(uri), false);
                    } finally {
                        if (client != null) {
                            client.release();
                        }
                    }
                }

                @Override
                public void setEnabled (boolean flag) {
                    SelectUriStep.this.setEnabled(flag);
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repositoryFile), repositoryFile, NbBundle.getMessage(SelectUriStep.class, "LBL_SelectUriStep.progressName")).waitFinished(); //NOI18N
            if (message[0] == null && supp.isCanceled()) {
                message[0] = new Message(NbBundle.getMessage(SelectUriStep.class, "MSG_SelectUriStep.validationCanceled"), true); //NOI18N
            }
            if (message[0] != null) {
                setValid(false, message[0]);
            }
            valid = isValid();
            //enable input
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    if (message[0] != null) {
                        setValid(true, message[0]);
                    }
                    setEnabled(true);
                    enableFields();
                }
            });
        }
        return valid;
    }

    @Override
    protected JComponent getJComponent () {
        return panel;
    }

    @Override
    public HelpCtx getHelp () {
        return new HelpCtx(SelectUriStep.class.getName() + "." + mode.helpId);
    }

    public String getSelectedUri () {
        String selectedUri;
        if (panel.rbConfiguredUri.isSelected()) {
            selectedUri = ((RemoteUri) panel.cmbConfiguredRepositories.getSelectedItem()).uri;
        } else {
            GitURI uri = repository.getURI();
            selectedUri = uri != null ? uri.toPrivateString().trim() : "";             // NOI18N
        }
        return selectedUri;
    }

    public GitRemoteConfig getSelectedRemote () {
        GitRemoteConfig selectedRemote;
        if (panel.rbConfiguredUri.isSelected()) {
            selectedRemote = remotes.get(((RemoteUri) panel.cmbConfiguredRepositories.getSelectedItem()).remoteName);
        } else {
            selectedRemote = RemoteConfig.createUpdatableRemote(repositoryFile, (String) panel.cmbRemoteNames.getSelectedItem()).toGitRemote();
        }
        return selectedRemote;
    }

    public boolean isConfiguredRemoteSelected () {
        return panel.rbConfiguredUri.isSelected();
    }

    @Override
    public void prepareValidation () {
        setEnabled(false);
    }

    private void setEnabled (boolean enabled) {
        for (JComponent inputField : inputFields) {
            inputField.setEnabled(enabled);
        }
        repository.setEnabled(enabled);
    }

    public void cancelBackgroundTasks () {
        if (supp != null) {
            supp.cancel();
        }
    }

    public Map<String, GitBranch> getRemoteBranches () {
        return remoteBranches;
    }

    public Map<String, String> getRemoteTags () {
        return remoteTags;
    }

    public String getRemoteName () {
        if (isConfiguredRemoteSelected()) {
            GitRemoteConfig remote = getSelectedRemote();
            return remote == null ? null : remote.getRemoteName();
        } else if (panel.cbPersistRemote.isSelected()
                && panel.cbPersistRemote.isVisible()
                && panel.cbPersistRemote.isEnabled()) {
            return (String) panel.cmbRemoteNames.getSelectedItem();
        } else {
            return null;
        }
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        validateBeforeNext();
    }

    public void storeURI() {
        if (panel.rbCreateNew.isSelected()) {
            repository.store();
        }
    }
    
    private static class RemoteUri implements Comparable<RemoteUri> {
        private final String label;
        private final String uri;
        private final String remoteName;

        public RemoteUri (String remoteName, String uri) {
            this.uri = uri;
            this.remoteName = remoteName;
            this.label = NbBundle.getMessage(SelectUriPanel.class, "SelectUriPanel.configuredRepository.uri", new Object[] { remoteName, uri }); //NOI18N
        }

        @Override
        public String toString () {
            return label;
        }

        @Override
        public int compareTo (RemoteUri other) {
            int comp = remoteName.compareToIgnoreCase(other.remoteName);
            if (comp == 0) {
                comp = uri.compareToIgnoreCase(other.uri);
            }
            return comp;
        }
    }
}
