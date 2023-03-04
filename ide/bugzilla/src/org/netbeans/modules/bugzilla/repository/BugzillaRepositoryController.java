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

package org.netbeans.modules.bugzilla.repository;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Collection;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConnector;
import org.netbeans.modules.bugzilla.commands.ValidateCommand;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.util.*;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaRepositoryController implements RepositoryController, DocumentListener, ActionListener {
    private final BugzillaRepository repository;
    private final RepositoryPanel panel;
    private String errorMessage;
    private boolean validateError;
    private boolean populated = false;
    private TaskRunner taskRunner;
    private RequestProcessor rp;
    private final ChangeSupport support = new ChangeSupport(this);
    
    BugzillaRepositoryController(BugzillaRepository repository) {
        this.repository = repository;
        panel = new RepositoryPanel(this);
        panel.nameField.getDocument().addDocumentListener(this);
        panel.userField.getDocument().addDocumentListener(this);
        panel.urlField.getDocument().addDocumentListener(this);
        panel.psswdField.getDocument().addDocumentListener(this);

        panel.validateButton.addActionListener(this);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    public HelpCtx getHelpContext() {
        return new HelpCtx("org.netbeans.modules.bugzilla.repository.BugzillaRepository"); // NOI18N
    }

    @Override
    public boolean isValid() {
        return validate();
    }

    private String getUrl() {
        String url = panel.urlField.getText().trim();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url; // NOI18N
    }
    
    private String getName() {
        return panel.nameField.getText().trim();
    }

    private String getUser() {
        return panel.userField.getText();
    }

    private char[] getPassword() {
        return panel.psswdField.getPassword();
    }

    private String getHttpUser() {
        return panel.httpCheckBox.isSelected() ? panel.httpUserField.getText() : null;
    }

    private char[] getHttpPassword() {
        return panel.httpCheckBox.isSelected() ? panel.httpPsswdField.getPassword() : new char[0];
    }

    private boolean isLocalUserEnabled () {
        return panel.cbEnableLocalUsers.isSelected();
    }

    private boolean validate() {
        if(validateError) {
            UIUtils.runInAWT(new Runnable() {
                @Override
                public void run() {            
                    panel.setValidateEnabled(true);
                }
            });
            return false;
        }
        panel.setValidateEnabled(false);

        if(!populated) {
            return false;
        }
        errorMessage = null;

        // check name
        String name = panel.nameField.getText().trim();
        if(name.equals("")) { // NOI18N
            errorMessage = NbBundle.getMessage(BugzillaRepositoryController.class, "MSG_MISSING_NAME");  // NOI18N
            return false;
        }

        // is name unique?
        Collection<Repository> repositories = null;
        if(repository.getTaskRepository() == null) {
            repositories = RepositoryManager.getInstance().getRepositories(BugzillaConnector.ID);
            for (Repository repo : repositories) {
                if(name.equals(repo.getDisplayName())) {
                    errorMessage = NbBundle.getMessage(BugzillaRepositoryController.class, "MSG_NAME_ALREADY_EXISTS");  // NOI18N
                    return false;
                }
            }
        }

        // check url
        String url = getUrl();
        if(url.equals("")) { // NOI18N
            errorMessage = NbBundle.getMessage(BugzillaRepositoryController.class, "MSG_MISSING_URL");  // NOI18N
            return false;
        }

        if(!isValid(url) || "http://".equals(url) || "https://".equals(url)) {
            errorMessage = NbBundle.getMessage(BugzillaRepositoryController.class, "MSG_WRONG_URL_FORMAT");  // NOI18N
            return false;
        }
        
        if(url.startsWith("http://netbeans.org/bugzilla")) {
            errorMessage = NbBundle.getMessage(BugzillaRepositoryController.class, "MSG_WRONG_NETBEANS_URL_FORMAT");  // NOI18N
            return false;
        }

        // the url format is ok - lets enable the validate button
        panel.setValidateEnabled(true);

        // is url unique?
        if(repository.getTaskRepository() == null) {
            for (Repository repo : repositories) {
                if(url.trim().equals(repo.getUrl())) {
                    errorMessage = NbBundle.getMessage(BugzillaRepositoryController.class, "MSG_URL_ALREADY_EXISTS");  // NOI18N
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void applyChanges() {
        repository.setInfoValues(
            getName(),
            getUrl(),
            getUser(),
            getPassword(),
            getHttpUser(),
            getHttpPassword(),
            isLocalUserEnabled());
        if(BugzillaUtil.isNbRepository(repository)) {
            NBRepositorySupport.getInstance().setNBBugzillaRepository(repository);
        }
    }

    @Override
    public void cancelChanges() {
        
    }
    
    @Override
    public void populate() {
        taskRunner = new TaskRunner(NbBundle.getMessage(RepositoryPanel.class, "LBL_ReadingRepoData")) {  // NOI18N
            @Override
            protected void preRun() {
                panel.validateButton.setVisible(false);
                super.preRun();
            }
            @Override
            protected void postRun() {
                panel.validateButton.setVisible(true);
                super.postRun();
            }
            @Override
            void execute() {
                UIUtils.runInAWT(new Runnable() {
                    @Override
                    public void run() {
                        RepositoryInfo info = repository.getInfo();
                        if(info != null) {
                            panel.userField.setText(info.getUsername());
                            char[] psswd = info.getPassword();
                            panel.psswdField.setText(psswd != null ? new String(psswd) : "");
                            String httpUser = info.getHttpUsername();
                            char[] httpPsswd = info.getHttpPassword();
                            if(httpUser != null && !httpUser.equals("")) {
                                panel.httpCheckBox.setSelected(true);
                                panel.httpUserField.setText(httpUser);
                            }
                            if(httpPsswd != null && httpPsswd.length > 0) {
                                panel.httpCheckBox.setSelected(true);
                                panel.httpPsswdField.setText(new String(httpPsswd));
                            }
                            panel.urlField.setText(info.getUrl());
                            panel.nameField.setText(repository.getDisplayName());
                            panel.cbEnableLocalUsers.setSelected(repository.isShortUsernamesEnabled());
                        }
                        populated = true;
                        fireChange();
                    }
                });
            }
        };
        taskRunner.startTask();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if(!populated) return;
        validateErrorOff(e);
        fireChange();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.validateButton) {
            onValidate();
        }
    }

    private void onValidate() {
        taskRunner = new TaskRunner(NbBundle.getMessage(RepositoryPanel.class, "LBL_Validating")) {  // NOI18N
            @Override
            void execute() {
                validateError = false;
                repository.resetRepository(true); // reset mylyns caching

                String name = getName();
                String url = getUrl();
                String user = getUser();
                String httpUser = getHttpUser();
                TaskRepository taskRepo = BugzillaRepository.createTemporaryTaskRepository(
                        name,
                        url,
                        user,
                        getPassword(),
                        httpUser,
                        getHttpPassword(),
                        isLocalUserEnabled());

                ValidateCommand cmd = new ValidateCommand(taskRepo);
                repository.getExecutor().execute(cmd, false, false, false);
                if(cmd.hasFailed()) {
                    if(cmd.getErrorMessage() == null) {
                        logValidateMessage("validate for [{0},{1},{2},{3},{4},{5}] has failed, yet the returned error message is null.", // NOI18N
                                           Level.WARNING, name, url, user, getPassword(), httpUser, getHttpPassword());
                        errorMessage = NbBundle.getMessage(BugzillaRepositoryController.class, "MSG_VALIDATION_FAILED"); // NOI18N
                    } else {
                        errorMessage = cmd.getErrorMessage();
                        logValidateMessage("validate for [{0},{1},{2},{3},{4},{5}] has failed: " + errorMessage, // NOI18N
                                           Level.WARNING, name, url, user, getPassword(), httpUser, getHttpPassword());
                    }
                    validateError = true;
                } else {
                    UIUtils.runInAWT(new Runnable() {
                        @Override
                        public void run() {
                            panel.connectionLabel.setVisible(true);
                        }
                    });
                    logValidateMessage("validate for [{0},{1},{2},{3},{4},{5}] ok.", // NOI18N
                                       Level.INFO, name, url, user, getPassword(), httpUser, getHttpPassword());
                }
                fireChange();
            }

            private void logValidateMessage(String msg, Level level, String name, String url, String user, char[] psswd, String httpUser, char[] httpPsswd) {
                Bugzilla.LOG.log(level, msg, new Object[] {name, url, user, LogUtils.getPasswordLog(psswd), httpUser, LogUtils.getPasswordLog(httpPsswd)});
            }
        };
        taskRunner.startTask();
    }

    private void validateErrorOff(DocumentEvent e) {
        if (e.getDocument() == panel.userField.getDocument() || e.getDocument() == panel.urlField.getDocument() || e.getDocument() == panel.psswdField.getDocument()) {
            validateError = false;
        }
    }

    void cancel() {
        if(taskRunner != null) {
            taskRunner.cancel();
        }
    }

    private boolean isValid(String url) {
        if (!url.startsWith("https://") && !url.startsWith("http://")) { 
            return false;
        }
        try {
            new URI(url); 
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private abstract class TaskRunner implements Runnable, Cancellable, ActionListener {
        private Task task;
        private ProgressHandle handle;
        private final String labelText;

        public TaskRunner(String labelText) {
            this.labelText = labelText;            
        }

        final void startTask() {
            cancel();
            task = getRequestProcessor().create(this);
            task.schedule(0);
        }

        @Override
        public final void run() {
            UIUtils.runInAWT(new Runnable() {
                @Override
                public void run() {
                    preRun();
                }
            });
            try {
                execute();
            } finally {
                UIUtils.runInAWT(new Runnable() {
                    @Override
                    public void run() {                
                        postRun();
                    }
                });
            }
        }

        abstract void execute();

        protected void preRun() {
            handle = ProgressHandleFactory.createHandle(labelText, this);
            JComponent comp = ProgressHandleFactory.createProgressComponent(handle);
            panel.progressPanel.removeAll();
            panel.progressPanel.add(comp, BorderLayout.CENTER);
            panel.cancelButton.addActionListener(this);
            panel.connectionLabel.setVisible(false);
            handle.start();
            panel.progressPanel.setVisible(true);
            panel.cancelButton.setVisible(true);
            panel.validateButton.setVisible(false);
            panel.validateLabel.setVisible(true);
            panel.enableFields(false);
            panel.validateLabel.setText(labelText); // NOI18N
        }

        protected void postRun() {
            if(handle != null) {
                handle.finish();
            }
            panel.cancelButton.removeActionListener(this);
            panel.progressPanel.setVisible(false);
            panel.validateLabel.setVisible(false);
            panel.validateButton.setVisible(true);
            panel.cancelButton.setVisible(false);
            panel.enableFields(true);
        }

        @Override
        public boolean cancel() {
            boolean ret = true;
            postRun();
            if(task != null) {
                ret = task.cancel();
            }
            errorMessage = null;
            return ret;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == panel.cancelButton) {
                cancel();
            }
        }

    }

    private RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Bugzilla Repository tasks", 1, true); // NOI18N
        }
        return rp;
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
        support.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
    }
    
    protected void fireChange() {
        support.fireChange();
    }    

}
