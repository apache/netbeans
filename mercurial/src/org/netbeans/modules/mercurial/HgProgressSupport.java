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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.mercurial;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.Action;
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
            progressHandle = ProgressHandleFactory.createHandle(displayName, this,  getLogger().getOpenOutputAction());
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
