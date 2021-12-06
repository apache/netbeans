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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.versionvault.Clearcase;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public abstract class ProgressSupport implements Runnable, Cancellable {

    private RequestProcessor.Task task;
    private String displayName;
    private ProgressHandle progressHandle;
    private boolean canceled = false;
    private RequestProcessor rp;
    private Cancellable cancellableDelegate;
    private AbstractAction focusLogAction;

    public ProgressSupport(RequestProcessor rp, String displayName) {
        this(rp, displayName, null);
    }
    
    public ProgressSupport(RequestProcessor rp, String displayName, JButton cancel) {
        this.displayName = displayName;
        this.rp = rp;
        if(cancel != null) {
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancel();
                }
            });
        }
    }

    public void setDisplayMessage(String displayName) {
        this.displayName = displayName;
        ProgressHandle ph = getProgressHandle();
        if(ph != null) {
            ph.setDisplayName(displayName);
        }
    }

    public void setCancellableDelegate(Cancellable c) {
        cancellableDelegate = c;
        if(canceled) {
            c.cancel();
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void schedule(int delay) {
        if(task == null) {
            task = rp.create(this);
        }      
        task.schedule(delay);
    }
    
    public RequestProcessor.Task start() {                        
        task = rp.post(this);    
        return task;
    }

    public JComponent getProgressComponent() {
        return ProgressHandleFactory.createProgressComponent(getProgressHandle());                                            
    }
        
    private void startProgress() {  
        getProgressHandle().start();
        String msg = "==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + displayName + "\n\n";       // NOI18N        
        logOutput(msg);   
        Clearcase.LOG.fine(msg);   
    }         
            
    private ProgressHandle getProgressHandle() {
        if(progressHandle == null) {
            progressHandle = ProgressHandleFactory.createHandle(displayName, this, getFocusLogAction());
        }
        return progressHandle;
    }
    
    public void run() {                
        try {            
            canceled = false;
            startProgress();
            perform();               
        } finally {            
            finnishProgress();
        }
    }

    protected abstract void perform();

    protected void finnishProgress() {
        getProgressHandle().finish();
        progressHandle = null;
        if(isCanceled()) {            
            String msg = "==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + displayName + " " + org.openide.util.NbBundle.getMessage(ProgressSupport.class, "MSG_Progress_Canceled") + "\n\n";
            logOutput(msg); 
            Clearcase.LOG.fine(msg);
        } else {
            String msg = "==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + displayName + " " + org.openide.util.NbBundle.getMessage(ProgressSupport.class, "MSG_Progress_Finished") + "\n\n";
            logOutput(msg); // NOI18N
            Clearcase.LOG.fine(msg);   
        }            
    }        
    
    public boolean cancel() {
        canceled = true;
        if(cancellableDelegate != null) {
            cancellableDelegate.cancel();
        }
        return task != null ?  task.cancel() : true;
    }
    
    private void logOutput(String output) {
        Clearcase.getInstance().printlnOut(output);
        Clearcase.getInstance().flushLog();    
    }

    private Action getFocusLogAction() {
        if(focusLogAction == null) {
            focusLogAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    Clearcase.getInstance().focusLog();
                }
            };
        }
        return focusLogAction;
    }
}
