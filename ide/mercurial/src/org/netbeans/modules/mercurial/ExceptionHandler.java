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
package org.netbeans.modules.mercurial;

import javax.swing.JButton;
import java.util.logging.Level;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class ExceptionHandler {    
     
    public static final int EX_UNKNOWN = 0;
    public static final int EX_ACTION_CANCELED_BY_USER = 2;
    public static final int EX_NO_REPOSITORY = 4;
    public static final int EX_ALREADY_TRACKED = 8;
    public static final int EX_NOT_TRACKED = 16;
    public static final int EX_CANNOT_READ_COMMIT_MSG = 32;
    public static final int EX_UNABLE_EXECUTE_CMD = 64;
                 
    static final String ACTION_CANCELED_BY_USER = org.openide.util.NbBundle.getMessage(ExceptionHandler.class, "MSG_ActionCanceledByUser"); // NOI18N
    private final HgException exception;
    private final int exceptionMask;



    public ExceptionHandler(HgException exception) {
        this.exception = exception;        
        exceptionMask = getMask(exception.getMessage());
    }

    protected int getExceptionMask() {
        return exceptionMask;
    }
    
    protected HgException getException() {
        return exception;
    }

    private static int getMask(String msg) {
        if(msg == null || msg.trim().equals("")) { // NOI18N
            return EX_UNKNOWN;
        }
        msg = msg.toLowerCase();        
        if(isCancelledAction(msg)) {
            return EX_ACTION_CANCELED_BY_USER;
        } else if(isNoRepository(msg)) {
            return EX_NO_REPOSITORY;               
        } else if(isNotTracked(msg)) {
            return EX_NOT_TRACKED;               
        } else if(isCannotReadCommitMsg(msg)) {
            return EX_CANNOT_READ_COMMIT_MSG;               
        } else if(isUnableExecuteCmd(msg)) {
            return EX_UNABLE_EXECUTE_CMD;               
        } else if(isAlreadyTracked(msg)) {
            return EX_ALREADY_TRACKED;
        }                
        return EX_UNKNOWN;
    }
    
    private static boolean isCancelledAction(String msg) {
        return msg.equals(ACTION_CANCELED_BY_USER);
    }
    
    public static boolean isNoRepository(String msg) {
        msg = msg.toLowerCase();
        return msg.indexOf("abort: There is no Mercurial repository here") > -1;                                   // NOI18N
    }
        
    public static boolean isUpdateSpansBranches(String msg) {
        msg = msg.toLowerCase();
        return msg.indexOf("abort: update spans branches") > -1;                                   // NOI18N
    }

    public static boolean isAlreadyTracked(String msg) {
        return msg.indexOf(" already tracked!") > -1;                          // NOI18N
    }

    public static boolean isNotTracked(String msg) {
        return msg.indexOf(" not tracked!") > -1;                          // NOI18N
    }
    
    public static boolean isCannotReadCommitMsg(String msg) {
        return msg.indexOf("abort: can't read commit message") > -1;                          // NOI18N
    }
    
    public static boolean isUnableExecuteCmd(String msg) {
        return msg.indexOf("unable to execute hg command") > -1;                          // NOI18N
    }
    
    public static boolean isUnableClone(String msg) {
        return msg.indexOf("abort: destination ") > -1;                          // NOI18N
    }
    
    public static boolean isNotFound(String msg) {
        return msg.indexOf("not found!") > -1;    // NOI18N
    }

    public static boolean isNoChangeNeeded(String msg) {
        return msg.indexOf("no change needed") > -1;    // NOI18N
    }

    // TODO: decide how to report exceptions to the user - information dialog?? 
    /** Analyzes exception and notifies user. */
    public void notifyException() {
        if(isCancelledAction(exception.getMessage())) {
            cancelledAction();
            return;
        }
        Mercurial.LOG.log(Level.INFO, null, exception);
    }
    
    public void notifyException(boolean notCanceled) {
        if(!notCanceled) {
            cancelledAction();
            return;
        }
        Mercurial.LOG.log(Level.INFO, null, exception);
    }

    public static String parseExceptionMessage(HgException exception) {
        String msg = exception.getMessage();
        int idx = msg.lastIndexOf("hg: "); // NOI18N
        if(idx > -1) {
            msg = msg.substring(idx);
        }
        return msg;
    }

    private void cancelledAction() {
        JButton ok = new JButton(NbBundle.getMessage(ExceptionHandler.class, "CTL_Action_OK")); // NOI18N
        NotifyDescriptor descriptor = new NotifyDescriptor(
                ACTION_CANCELED_BY_USER,
                NbBundle.getMessage(ExceptionHandler.class, "CTL_ActionCanceled_Title"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object [] { ok },
                ok);
        DialogDisplayer.getDefault().notify(descriptor);        
        return;
    }


    
}
