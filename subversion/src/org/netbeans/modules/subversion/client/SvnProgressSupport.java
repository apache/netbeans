/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.subversion.client;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.subversion.OutputLogger;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public abstract class SvnProgressSupport implements Runnable, Cancellable, ISVNNotifyListener {

    private Cancellable delegate; 
    private volatile boolean canceled;
    
    private ProgressHandle progressHandle = null;    
    private String displayName = "";            // NOI18N
    private String originalDisplayName = "";    // NOI18N
    private OutputLogger logger;
    private SVNUrl repositoryRoot;
    private RequestProcessor.Task task;
    
    public RequestProcessor.Task start(RequestProcessor rp, SVNUrl repositoryRoot, String displayName) {
        setDisplayName(displayName);        
        this.repositoryRoot = repositoryRoot;
        startProgress();         
        setProgressQueued();                
        task = rp.post(this);
        task.addTaskListener(new TaskListener() {
            @Override
            public void taskFinished(org.openide.util.Task task) {
                delegate = null;
            }
        });
        return task;
    }

    public void setRepositoryRoot(SVNUrl repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
        logger = null;
    }

    @Override
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

    @Override
    public synchronized boolean cancel() {
        if (canceled) {
            return false;
        }                
        getLogger().flushLog();
        if(delegate != null) {
            delegate.cancel();
        } 
        if(task != null) {
            task.cancel();
        }
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
        if(progressHandle != null) {            
            progressHandle.progress(NbBundle.getMessage(SvnProgressSupport.class,  "LBL_Queued", displayName));
        }
    }

    private void setProgress() {
        if(progressHandle != null) {            
            progressHandle.progress(displayName);
        }
    }
    
    protected String getDisplayName() {
        return displayName;
    }

    protected ProgressHandle getProgressHandle() {
        if(progressHandle == null) {
            if(repositoryRoot != null) {
                progressHandle = ProgressHandleFactory.createHandle(displayName, this, getLogger().getOpenOutputAction());
            } else {
                progressHandle = ProgressHandleFactory.createHandle(displayName, this);
            }
        }
        return progressHandle;
    }

    protected void startProgress() {
        getProgressHandle().start();
        getLogger().logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName); // NOI18N
    }

    protected void finnishProgress() {
        getProgressHandle().finish();
        if (isCanceled() == false) {
            getLogger().logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName + " " + org.openide.util.NbBundle.getMessage(SvnProgressSupport.class, "MSG_Progress_Finished")); // NOI18N
        } else {
            getLogger().logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName + " " + org.openide.util.NbBundle.getMessage(SvnProgressSupport.class, "MSG_Progress_Canceled")); // NOI18N
        }
    }
    
    protected OutputLogger getLogger() {
        if (logger == null) {
            logger = Subversion.getInstance().getLogger(repositoryRoot);
        }
        return logger;
    }

    private static void log(String msg) {
        SvnUtils.logT9Y(msg);
        Subversion.LOG.log(Level.FINE, msg); 
    }

    private void logChangedDisplayName(String thisDisplayName, String displayName) {
        if(thisDisplayName != null && !thisDisplayName.equals(displayName)) {
            if(!thisDisplayName.equals("")) {
                log("End - " + thisDisplayName); // NOI18N
                log("Start - " + displayName); // NOI18N
            }
        }
    }

    public void annotate(SVNClientException ex) {                        
        SvnClientExceptionHandler.notifyException(ex, !isCanceled(), true);        
    }

    @Override
    public void setCommand(int i) { }

    @Override
    public void logCommandLine(String string) { }

    @Override
    public void logMessage(String string) { }

    @Override
    public void logError(String string) { }

    @Override
    public void logRevision(long l, String string) { }

    @Override
    public void logCompleted(String string) { }

    @Override
    public void onNotify(File file, SVNNodeKind svnnk) {
        if(progressHandle != null) {
            progressHandle.progress(file.getName());
        }
    }
}
