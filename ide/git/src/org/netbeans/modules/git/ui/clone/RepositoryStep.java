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

package org.netbeans.modules.git.ui.clone;

import java.awt.EventQueue;
import org.netbeans.modules.git.ui.repository.remote.RemoteRepository;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import java.io.File;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitURI;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.utils.WizardStepProgressSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.AsynchronousValidatingPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryStep extends AbstractWizardPanel implements ChangeListener, AsynchronousValidatingPanel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, DocumentListener {

    private RepositoryStepProgressSupport support;
    private Map<String, GitBranch> branches;
    
    private Map<String, GitBranch> remoteBranches;
    private final RepositoryStepPanel panel;
    private final RemoteRepository repository;
    private boolean destinationValid = true;
    private boolean validatingFinish;
    private final CloneWizard wiz;

    public RepositoryStep (CloneWizard wiz, PasswordAuthentication pa, String forPath) {
        this.wiz = wiz;
        repository = new RemoteRepository(pa, forPath);
        repository.addChangeListener(this);
        panel = new RepositoryStepPanel(repository.getPanel());
        panel.txtDestination.getDocument().addDocumentListener(this);
        validateRepository();
    }

    @Override
    protected final boolean validateBeforeNext () {
        waitPopulated();
        boolean valid = false;
        try {
            branches = null;
            if(!validateRepository()) return false;
            if (validatingFinish) {
                Message msg = null;
                try {
                    if ((msg = validateNoEmptyDestination()) != null) {
                        // cannot finish
                        destinationValid = false;
                        return false;
                    }
                    File dest = getDestination();
                    if (dest.isFile()) {
                        setValid(false, msg = new Message(NbBundle.getMessage(CloneDestinationStep.class, "MSG_DEST_IS_FILE_ERROR"), false));
                        destinationValid = false;
                        return false;
                    }
                    File[] files = dest.listFiles();
                    if (files != null && files.length > 0) {
                        setValid(false, msg = new Message(NbBundle.getMessage(CloneDestinationStep.class, "MSG_DEST_IS_NOT_EMPTY_ERROR"), false));
                        destinationValid = false;
                        return false;
                    }
                } finally {
                    if (msg != null) {
                        final Message message = msg;
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                setValid(true, message);
                            }
                        });
                    }
                }
                destinationValid = true;
            }

            final File tempRepository = Utils.getTempFolder();
            GitURI uri = repository.getURI();
            if (uri != null) {
                repository.store();
                support = new RepositoryStepProgressSupport(panel.progressPanel, uri);        
                RequestProcessor.Task task = support.start(Git.getInstance().getRequestProcessor(tempRepository), tempRepository, NbBundle.getMessage(RepositoryStep.class, "BK2012"));
                task.waitFinished();
                final Message message = support.message;
                valid = isValid();
                if (message != null) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            setValid(true, message);
                        }
                    });
                }
            }    
        } finally {
            support = null;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    enable(true);
                }
            });
        }
        return valid;
    }

    void waitPopulated () {
        repository.waitPopulated();
    }

    private boolean validateRepository() {
        boolean valid = repository.isValid();
        setValid(valid, repository.getMessage());
        return valid;
    }

    private Message validateNoEmptyDestination () throws MissingResourceException {
        String parent = panel.txtDestination.getText();
        if (parent.trim().isEmpty()) {
            destinationValid = false;
            Message msg = new Message(NbBundle.getMessage(CloneDestinationStep.class, "MSG_EMPTY_PARENT_ERROR"), true);
            setValid(true, msg);
            return msg;
        }
        String name = panel.lblCloneName.getText();
        if (name == null || name.trim().isEmpty()) {
            destinationValid = false;
            Message msg = new Message(NbBundle.getMessage(CloneDestinationStep.class, "MSG_EMPTY_NAME_ERROR"), true);
            setValid(true, msg);
            return msg;
        }
        destinationValid = true;
        setValid(true, null);
        return null;
    }

    public Map<String, GitBranch> getBranches() {
        return branches;
    }
        
    public GitURI getURI() {
        return repository.getURI();
    }

    @Override
    protected JComponent getJComponent () {
        return panel;
    }

    @Override
    public HelpCtx getHelp () {
        return new HelpCtx(RepositoryStep.class);
    }

    @Override
    public void prepareValidation () {
        validatingFinish = wiz.isFinishing();
        enable(false);
    }    
    
    public void cancelBackgroundTasks () {
        if (support != null) {
            support.cancel();
        }
    }

    public Map<String, GitBranch> getRemoteBranches () {
        return remoteBranches;
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        panel.lblCloneName.setText(File.separator + getCloneName(repository.getURI()));
        setValid(repository.isValid(), repository.getMessage());
    }

    @Override
    public boolean isFinishPanel () {
        return destinationValid;
    }

    void store() {
        repository.store();
    }

    String getDestinationFolder () {
        return panel.txtDestination.getText().trim();
    }

    private void enable (boolean enabled) {
        repository.setEnabled(enabled);
        panel.txtDestination.setEnabled(enabled);
        panel.btnBrowseDestination.setEnabled(enabled);
    }

    private String getCloneName (GitURI uri) {
        String lastElem = ""; //NOI18N
        if (uri != null) {
            String path = uri.getPath();
            // get the last path element
            String[] pathElements = path.split("[/\\\\]"); //NOI18N
            for (int i = pathElements.length - 1; i >= 0; --i) {
                lastElem = pathElements[i];
                if (!lastElem.isEmpty()) {
                    break;
                }
            }
            if (!lastElem.isEmpty()) {
                // is it of the usual form abcdrepository.git ?
                if (lastElem.endsWith(".git")) { //NOI18N
                    lastElem = lastElem.substring(0, lastElem.length() - 4);
                }
                if (!lastElem.isEmpty()) {
                    return lastElem;
                }
            }
        }
        return lastElem.trim();
    }

    File getDestination () {
        return new File(panel.txtDestination.getText().trim() + File.separator + panel.lblCloneName.getText());
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        validateNoEmptyDestination();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        validateNoEmptyDestination();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }

    @NbBundle.Messages({
        "# {0} - repository URL",
        "MSG_RepositoryStep.errorCredentials=Incorrect credentials for repository at {0}",
        "# {0} - repository URL",
        "MSG_RepositoryStep.errorCannotConnect=Cannot connect to repository at {0}"
    })
    private class RepositoryStepProgressSupport extends WizardStepProgressSupport {
        private final GitURI uri;
        private Message message;

        public RepositoryStepProgressSupport(JPanel panel, GitURI uri) {
            super(panel, true);
            this.uri = uri;
        }

        @Override
        public void perform() {
            GitClient client = null;
            try {
                client = Git.getInstance().getClient(getRepositoryRoot(), this, false);
                client.init(getProgressMonitor());
                branches = new HashMap<String, GitBranch>();
                branches.putAll(client.listRemoteBranches(uri.toPrivateString(), getProgressMonitor()));
            } catch (GitException.AuthorizationException ex) {
                GitClientExceptionHandler.notifyException(ex, false);
                message = new Message(Bundle.MSG_RepositoryStep_errorCredentials(uri.toString()), false);
                setValid(false, message);
            } catch (final GitException ex) {
                GitClientExceptionHandler.notifyException(ex, false);
                message = new Message(Bundle.MSG_RepositoryStep_errorCannotConnect(uri.toString()), false);
                GitModuleConfig.getDefault().removeConnectionSettings(repository.getURI());
                setValid(false, message);
            } finally {
                if (client != null) {
                    client.release();
                }
                Utils.deleteRecursively(getRepositoryRoot());
                if (message == null && isCanceled()) {
                    message = new Message(NbBundle.getMessage(RepositoryStep.class, "MSG_RepositoryStep.validationCanceled"), true); //NOI18N
                    setValid(false, message);
                }
            }
        }

        @Override
        public void setEnabled(boolean editable) {
            enable(editable);
        }        
    };

}
