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

package org.netbeans.modules.git.ui.repository;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
public class RevisionInfoPanelController {
    private final RevisionInfoPanel panel;
    private static final String MSG_LOADING = NbBundle.getMessage(RevisionDialogController.class, "MSG_RevisionInfoPanel.loading"); //NOI18N
    private static final String MSG_UNKNOWN = NbBundle.getMessage(RevisionDialogController.class, "MSG_RevisionInfoPanel.unknown"); //NOI18N
    private final LoadInfoWorker loadInfoWorker = new LoadInfoWorker();
    private final Task loadInfoTask;
    private String currentCommit;
    private final File repository;
    private boolean valid;
    private final PropertyChangeSupport support;
    public static final String PROP_VALID = "RevisionInfoPanelController.valid"; //NOI18N
    private String mergingInto;
    private Revision info;

    public RevisionInfoPanelController (File repository) {
        this.repository = repository;
        this.loadInfoTask = Git.getInstance().getRequestProcessor(null).create(loadInfoWorker);
        this.panel = new RevisionInfoPanel();
        this.support = new PropertyChangeSupport(this);
        resetInfoFields();
    }

    public RevisionInfoPanel getPanel () {
        return panel;
    }

    public void loadInfo (String revision) {
        loadInfoTask.cancel();
        loadInfoWorker.monitor.cancel();
        currentCommit = revision;
        setValid(false);
        if (revision == null || revision.isEmpty()) {
            setUnknownRevision();
        } else {
            resetInfoFields();
            loadInfoTask.schedule(100);
        }
    }

    void displayMergedStatus (String revision) {
        this.mergingInto = revision;
    }

    Revision getInfo () {
        return info;
    }

    private void resetInfoFields () {
        panel.taMessage.setText(MSG_LOADING);
        panel.tbAuthor.setText(MSG_LOADING);
        panel.tbDate.setText(MSG_LOADING);
        panel.tbRevisionId.setText(MSG_LOADING);
    }

    private void updateInfoFields (String revision, GitRevisionInfo info, Boolean revisionMerged) {
        assert EventQueue.isDispatchThread();
        panel.tbAuthor.setText(info.getAuthor().toString());
        if (!panel.tbAuthor.getText().isEmpty()) {
            panel.tbAuthor.setCaretPosition(0);
        }
        panel.tbDate.setText(DateFormat.getDateTimeInstance().format(new Date(info.getCommitTime())));
        if (!panel.tbDate.getText().isEmpty()) {
            panel.tbDate.setCaretPosition(0);
        }
        String id = info.getRevision();
        if (id.length() > 10) {
            id = id.substring(0, 10);
        }
        if (revision.equals(info.getRevision())) {
            panel.tbRevisionId.setText(new StringBuilder(id).append(getMergedStatus(revisionMerged)).toString());
            this.info = new Revision(revision, revision, info.getShortMessage(), info.getFullMessage());
        } else {
            this.info = new Revision(info.getRevision(), revision, info.getShortMessage(), info.getFullMessage());
            if (revision.startsWith(GitUtils.PREFIX_R_HEADS)) { //NOI18N
                revision = revision.substring(GitUtils.PREFIX_R_HEADS.length());
            } else if (revision.startsWith(GitUtils.PREFIX_R_REMOTES)) { //NOI18N
                revision = revision.substring(GitUtils.PREFIX_R_REMOTES.length());
            }
            panel.tbRevisionId.setText(new StringBuilder(revision).append(getMergedStatus(revisionMerged)).append(" (").append(id).append(')').toString()); //NOI18N
        }
        if (!panel.tbRevisionId.getText().isEmpty()) {
            panel.tbRevisionId.setCaretPosition(0);
        }
        panel.taMessage.setText(info.getFullMessage());
        if (!panel.taMessage.getText().isEmpty()) {
            panel.taMessage.setCaretPosition(0);
        }
    }

    private void setUnknownRevision () {
        panel.tbAuthor.setText(MSG_UNKNOWN);
        panel.tbRevisionId.setText(MSG_UNKNOWN);
        panel.taMessage.setText(MSG_UNKNOWN);
    }

    private void setValid (boolean flag) {
        boolean oldValue = valid;
        valid = flag;
        if (oldValue != valid) {
            support.firePropertyChange(PROP_VALID, oldValue, valid);
        }
    }
    
    public void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener list) {
        support.removePropertyChangeListener(list);
    }

    @NbBundle.Messages("MSG_RevisionMerged.status= [merged]")
    private String getMergedStatus (Boolean revisionMerged) {
        if (Boolean.TRUE.equals(revisionMerged)) {
            return Bundle.MSG_RevisionMerged_status();
        } else {
            return "";
        }
    }

    private class LoadInfoWorker implements Runnable {

        ProgressMonitor.DefaultProgressMonitor monitor = new ProgressMonitor.DefaultProgressMonitor();

        @Override
        public void run () {
            final String revision = currentCommit;
            GitRevisionInfo revisionInfo;
            GitClient client = null;
            Boolean mergedStatus = null;
            try {
                monitor = new ProgressMonitor.DefaultProgressMonitor();
                if (Thread.interrupted()) {
                    return;
                }
                client = Git.getInstance().getClient(repository);
                revisionInfo = client.log(revision, monitor);
                if (!monitor.isCanceled() && mergingInto != null) {
                    GitRevisionInfo commonAncestor = client.getCommonAncestor(new String[] { mergingInto, revisionInfo.getRevision() }, monitor);
                    mergedStatus = commonAncestor != null && commonAncestor.getRevision().equals(revisionInfo.getRevision());
                }
            } catch (GitException ex) {
                if (!(ex instanceof GitException.MissingObjectException)) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
                revisionInfo = null;
            } finally {
                if (client != null) {
                    client.release();
                }
            }
            final GitRevisionInfo info = revisionInfo;
            final Boolean fMergedStatus = mergedStatus;
            final ProgressMonitor.DefaultProgressMonitor m = monitor;
            if (!monitor.isCanceled()) {
                Mutex.EVENT.readAccess(new Runnable () {
                    @Override
                    public void run () {
                        if (!m.isCanceled()) {
                            if (info == null) {
                                setUnknownRevision();
                            } else {
                                updateInfoFields(revision, info, fMergedStatus);
                                setValid(true);
                            }
                        }
                    }
                });
            }
        }
    }
}
