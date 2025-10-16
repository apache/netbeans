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

package org.netbeans.modules.mercurial;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;

/**
 *
 * @author Tomas Stupka
 */
public abstract class HgProgressSupport implements Runnable, Cancellable {

    private Cancellable delegate; 
    private volatile boolean canceled;
    
    private ProgressHandle progressHandle = null;    
    private String displayName = ""; // NOI18N
    private String originalDisplayName = ""; // NOI18N
    private OutputLogger logger;
    private HgURL repositoryRoot;
    private RequestProcessor.Task task;
    
    public HgProgressSupport() {
    }

    public HgProgressSupport(String displayName, JButton cancel) {
        this.displayName = displayName;
        if(cancel != null) {
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancel();
                }
            });
        }
    }
    
    public RequestProcessor.Task start(RequestProcessor rp, String displayName) {
        return start(rp, new File(""), displayName);                    //NOI18N
    }

    public RequestProcessor.Task start(RequestProcessor rp, File repositoryRoot, String displayName) {
        HgURL hgUrl = (repositoryRoot != null) ? new HgURL(repositoryRoot)
                                               : null;
        return start(rp, hgUrl, displayName);
    }

    public RequestProcessor.Task start(RequestProcessor rp, HgURL repositoryRoot, String displayName) {
        setDisplayName(displayName);
        this.repositoryRoot = repositoryRoot;
        startProgress();
        setProgressQueued();
        task = rp.post(this);
        task.addTaskListener(new TaskListener() {
            public void taskFinished(org.openide.util.Task task) {
                delegate = null;
            }
        });
        return task;
    }

    public RequestProcessor.Task start(RequestProcessor rp) {
        startProgress();
        task = rp.post(this);
        return task;
    }

    public JComponent getProgressComponent() {
        return ProgressHandleFactory.createProgressComponent(getProgressHandle());
    }

    public void setRepositoryRoot(HgURL repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
        logger = null;
    }

    protected HgURL getRepositoryRoot() {
        return repositoryRoot;
    }

    public void run() {        
        setProgress();
        performIntern();
    }

    protected void performIntern() {
        try {
            log("Start - " + displayName); // NOI18N
            if(!canceled) {
                perform();
            }
        } finally {
            log("End - " + displayName); // NOI18N
            finnishProgress();
            getLogger().closeLog();
        }
    }

    protected abstract void perform();

    public synchronized boolean isCanceled() {
        return canceled;
    }

    public synchronized boolean cancel() {
        if(delegate != null) {
            if(!delegate.cancel()) 
                return false;
        }
        if (canceled) {
            return false;
        }        
        if(task != null) {
            task.cancel();
        }
        Mercurial.getInstance().clearRequestProcessor(repositoryRoot);
        finnishProgress();
        canceled = true;
        return true;
    }

    public void setCancellableDelegate(Cancellable cancellable) {
        this.delegate = cancellable;
    }

    public void setDisplayName(String displayName) {
        if(originalDisplayName.equals("")) { // NOI18N
            originalDisplayName = displayName;
        }
        logChangedDisplayName(this.displayName, displayName);
        this.displayName = displayName;
        setProgress();
    }

    private void setProgressQueued() {
        if(progressHandle!=null) {
            progressHandle.progress(NbBundle.getMessage(HgProgressSupport.class, "LBL_Queued", displayName)); // NOI18N
        }
    }
 
    private void setProgress() {
        if(progressHandle!=null) {
            progressHandle.progress(displayName);
        }
    }

    protected String getDisplayName() {
        return displayName;
    }

    protected ProgressHandle getProgressHandle() {
        if(progressHandle==null) {
            progressHandle = ProgressHandle.createHandle(displayName, this, getLogger().getOpenOutputAction());
        }
        return progressHandle;
    }

    protected void startProgress() {
        getProgressHandle().start();
    }

    protected void finnishProgress() {
        getProgressHandle().finish();
    }
    
    public OutputLogger getLogger() {
        if (logger == null) {
            String loggerId = (repositoryRoot != null)
                              ? repositoryRoot.toHgCommandUrlStringWithoutUserInfo()
                              : null;
            logger = Mercurial.getInstance().getLogger(loggerId);
        }
        return logger;
    }

    public void annotate(HgException ex) {        
        ExceptionHandler eh = new ExceptionHandler(ex);
        if(isCanceled()) {
            eh.notifyException(false);
        } else {
            eh.notifyException();    
        }
    }
    
    private static void log(String msg) {
        HgUtils.logT9Y(msg);
        Mercurial.LOG.log(Level.FINE, msg); 
    }

    private void logChangedDisplayName(String thisDisplayName, String displayName) {
        if(thisDisplayName != null && !thisDisplayName.equals(displayName)) {
            if(!thisDisplayName.equals("")) {
                log("End - " + thisDisplayName); // NOI18N
                log("Start - " + displayName); // NOI18N
            }
        }
    }
}
