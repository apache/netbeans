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

package org.netbeans.modules.subversion.remote.ui.wizards.importstep;

import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.remote.RepositoryFile;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.WizardStepProgressSupport;
import org.netbeans.modules.subversion.remote.ui.browser.Browser;
import org.netbeans.modules.subversion.remote.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.remote.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.remote.ui.checkout.CheckoutAction;
import org.netbeans.modules.subversion.remote.ui.commit.CommitAction;
import org.netbeans.modules.subversion.remote.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.StringSelector;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * 
 */
public class ImportStep extends AbstractStep implements DocumentListener, WizardDescriptor.AsynchronousValidatingPanel, WizardDescriptor.FinishablePanel {
    
    private ImportPanel importPanel;

    private RepositoryPaths repositoryPaths;
    private final BrowserAction[] actions;
    private final VCSFileProxy importDirectory;       
    private WizardStepProgressSupport support;
    
    public ImportStep(BrowserAction[] actions, VCSFileProxy importDirectory) {
        this.actions = actions;
        this.importDirectory = importDirectory;
    }
    
    @Override
    public HelpCtx getHelp() {    
        return new HelpCtx(ImportStep.class);
    }    

    @Override
    protected JComponent createComponent() {
        if (importPanel == null) {
            importPanel = new ImportPanel();            
            importPanel.messageTextArea.getDocument().addDocumentListener(this);            
            importPanel.repositoryPathTextField.getDocument().addDocumentListener(this);                       
            importPanel.btnRecentMessages.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onBrowseRecentMessages();
                }
            });
        }            
        return importPanel;              
    }

    @Override
    protected void validateBeforeNext() {
        try {
            support =  new ImportProgressSupport(importPanel.progressPanel, importPanel.progressLabel);  
            SVNUrl url = getUrl();
            if (url == null) {
                return;
            }
            support.setRepositoryRoot(url);            
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
            RequestProcessor.Task task = support.start(rp, url, org.openide.util.NbBundle.getMessage(ImportStep.class, "CTL_Import_Progress"));
            task.waitFinished();
        } finally {
            support = null;
        }
    }   
        
    @Override
    public void prepareValidation() {        
    }

    private SVNUrl getUrl() {        
        RepositoryFile repositoryFile = getRepositoryFile();
        return repositoryFile == null ? null : repositoryFile.getRepositoryUrl();
    }
    
    public boolean validateUserInput() {
        invalid(null);
        
        String text = importPanel.repositoryPathTextField.getText().trim();
        if (text.length() == 0) {
            invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(ImportStep.class, "BK2014"), true)); // NOI18N
            return false;
        }        
        
        text = importPanel.messageTextArea.getText().trim();
        boolean valid = text.length() > 0;
        if(valid) {
            valid();
        } else {
            invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(ImportStep.class, "CTL_Import_MessageRequired"), true)); // NOI18N
        }

        return valid;
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        validateUserInput();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validateUserInput();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    public void focusGained(FocusEvent e) {
        
    }

    public void focusLost(FocusEvent e) {
        validateUserInput();
    }

    public String getImportMessage() {
        return SvnUtils.fixLineEndings(importPanel.messageTextArea.getText());
    }

    public void setup(RepositoryFile repositoryFile) {
        if(importPanel.repositoryPathTextField.getText().trim().equals("") //NOI18N
                || repositoryPaths != null && !repositoryPaths.getRepositoryUrl().equals(repositoryFile.getRepositoryUrl())) {
            // no value set yet ...
            if(repositoryPaths == null) {
                repositoryPaths =
                    new RepositoryPaths (
                        VCSFileProxySupport.getFileSystem(importDirectory), repositoryFile,
                        importPanel.repositoryPathTextField,
                        importPanel.browseRepositoryButton,
                        null,
                        null
                    );
                String browserPurposeMessage = org.openide.util.NbBundle.getMessage(ImportStep.class, "LBL_BrowserMessage");
                int browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;
                repositoryPaths.setupBehavior(browserPurposeMessage, browserMode, actions, Browser.BROWSER_HELP_ID_IMPORT, null);
            } else {
                repositoryPaths.setRepositoryFile(repositoryFile);
            }
        }
        importPanel.repositoryPathTextField.setText(repositoryFile.getPath());
        validateUserInput();
    }

    public RepositoryFile getRepositoryFile() {
        try {
            return repositoryPaths.getRepositoryFiles()[0]; // more files doesn't make sence
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);
            invalid(new AbstractStep.WizardMessage(ex.getLocalizedMessage(), false)); // NOI18N
        } 
        return null;
    }

    public SVNUrl getRepositoryFolderUrl() {
        return getRepositoryFile().getFileUrl();
    }

    public void stop() {
        if(support != null) {
            support.cancel();
        }
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
    
    private void onBrowseRecentMessages() {
        StringSelector.RecentMessageSelector selector = new StringSelector.RecentMessageSelector(SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(importDirectory)).getPreferences());
        String message = selector.getRecentMessage(NbBundle.getMessage(ImportStep.class, "CTL_ImportPanel_RecentTitle"), //NOI18N
                                               NbBundle.getMessage(ImportStep.class, "CTL_ImportPanel_RecentPrompt"), //NOI18N
            org.netbeans.modules.subversion.remote.versioning.util.Utils.getStringList(SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(importDirectory)).getPreferences(), CommitAction.RECENT_COMMIT_MESSAGES));
        if (message != null) {
            importPanel.messageTextArea.replaceSelection(message);
        }
    }

    private class ImportProgressSupport extends WizardStepProgressSupport {
        public ImportProgressSupport(JPanel panel, JLabel label) {
            super(new Context(importDirectory).getFileSystem(), panel);
        }
        @Override
        public void perform() {
            AbstractStep.WizardMessage invalidMsg = null;
            try {
                if(!validateUserInput()) {
                    return;
                }        

                invalid(null);

                SvnClient client;
                final Context context = new Context(importDirectory);
                try {
                    client = Subversion.getInstance().getClient(context, repositoryPaths.getRepositoryUrl(), this);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(context, ex, true, true);
                    invalidMsg = new AbstractStep.WizardMessage(SvnClientExceptionHandler.parseExceptionMessage(ex), false);
                    return;
                }

                try {
                    RepositoryFile repositoryFile = getRepositoryFile();
                    SVNUrl repositoryUrl = repositoryFile.getRepositoryUrl();
                    if (!importIntoExisting(context, client, repositoryFile.getFileUrl())) {
                        invalidMsg = new AbstractStep.WizardMessage(NbBundle.getMessage(ImportStep.class, "MSG_TargetFolderExists"), true); //NOI18N
                        return;
                    }
                    try {
                        // if the user came back from the last step and changed the repository folder name,
                        // then this could be already a working copy ...    
                        SvnUtils.deleteRecursively(VCSFileProxy.createFileProxy(importDirectory, SvnUtils.SVN_ADMIN_DIR)); // NOI18N
                        try {
                            VCSFileProxy importDummyFolder = VCSFileProxySupport.getTempFolder(importDirectory, true);
                            client.doImport(importDummyFolder, repositoryFile.getFileUrl(), getImportMessage(), false);
                        } catch (IOException ex) {
                            throw new SVNClientException(ex);
                        }
                    } catch (SVNClientException ex) {
                        if (isCanceled() || SvnClientExceptionHandler.isFileAlreadyExists(ex.getMessage())) {
                            // ignore
                        } else {
                            throw ex;
                        }         
                    }
                    if(isCanceled()) {
                        return;
                    }

                    RepositoryFile[] repositoryFiles = new RepositoryFile[] { repositoryFile };
                    CheckoutAction.checkout(client, repositoryUrl, repositoryFiles, importDirectory, true, false, this);
                    Subversion.getInstance().versionedFilesChanged();
                    SvnUtils.refreshParents(importDirectory);
                    // XXX this is ugly and expensive! the client should notify (onNotify()) the cache. find out why it doesn't work...
                    Subversion.getInstance().getStatusCache().refreshRecursively(importDirectory);
                    if(isCanceled()) {                        
                        SvnUtils.deleteRecursively(VCSFileProxy.createFileProxy(importDirectory, SvnUtils.SVN_ADMIN_DIR)); // NOI18N
                        return;
                    }
                } catch (SVNClientException ex) {
                    annotate(ex);
                    invalidMsg = new AbstractStep.WizardMessage(SvnClientExceptionHandler.parseExceptionMessage(ex), false);
                }

            } finally {
                Subversion.getInstance().versionedFilesChanged();
                if(isCanceled()) {
                    valid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(ImportStep.class, "MSG_Import_ActionCanceled"), false)); // NOI18N
                } else if(invalidMsg != null) {
                    valid(invalidMsg);
                } else {
                    valid();
                }
            }
        }            

        @Override
        public void setEditable(boolean editable) {
            importPanel.browseRepositoryButton.setEnabled(editable);
            importPanel.messageTextArea.setEditable(editable);
            importPanel.repositoryPathTextField.setEditable(editable);
        }

        /**
         * Checks if the target folder already exists in the repository.
         * If it does exist, user will be asked to confirm the import into the existing folder.
         * @param client
         * @param repositoryFileUrl
         * @return true if the target does not exist or user wishes to import anyway.
         */
        private boolean importIntoExisting(Context context, SvnClient client, SVNUrl repositoryFileUrl) {
            try {
                ISVNInfo info = client.getInfo(context, repositoryFileUrl);
                if (info != null) {
                    // target folder exists, ask user for confirmation
                    final boolean flags[] = {true};
                    NotifyDescriptor nd = new NotifyDescriptor(NbBundle.getMessage(ImportStep.class, "MSG_ImportIntoExisting", SvnUtils.decodeToString(repositoryFileUrl)), //NOI18N
                            NbBundle.getMessage(ImportStep.class, "CTL_TargetFolderExists"), NotifyDescriptor.YES_NO_CANCEL_OPTION, //NOI18N
                            NotifyDescriptor.QUESTION_MESSAGE, null, NotifyDescriptor.YES_OPTION);
                    if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.YES_OPTION) {
                        flags[0] = false;
                    }
                    return flags[0];
                }
            } catch (SVNClientException ex) {
                // ignore
            }
            return true;
        }
    };

}

