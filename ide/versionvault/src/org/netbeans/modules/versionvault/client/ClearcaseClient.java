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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.versionvault.client;

import org.netbeans.modules.versionvault.ClearcaseException;
import org.netbeans.modules.versionvault.ClearcaseUnavailableException;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.CommandReport;
import org.openide.util.RequestProcessor;
import org.openide.util.Cancellable;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.util.ProgressSupport;
import org.openide.util.NbBundle;

/**
 * Interface to Clearcase functionality. 
 * ClearcaseClient commands execution is synchronized and serialized.
 * 
 * @author Maros Sandor
 */
public class ClearcaseClient {

    private static boolean notifiedUnavailable;
            
    private Cleartool ct;

    private List<Pattern> suppressedMessages;
    
    /**
     * Request processor that executes clearcase commands.
     */
    private final RequestProcessor  rp = new RequestProcessor("Clearcase commands", 1);

    /**
     * Indicates that the cleartool shell is meant only for one use as reusing 
     * might cause errors with some commands - e.g. co, ci, ...
     */
    private boolean singleUse;

    public ClearcaseClient() {
        singleUse = false;
    }
    
    public ClearcaseClient(boolean singleUse) {
        this.singleUse = singleUse;
    }

    public static boolean isAvailable() {
        return !notifiedUnavailable;
    }    
    
    public RequestProcessor getRequestProcessor() {
        return rp;
    }

    /**
     * Execute a command in a separate thread, command execution and notification happens asynchronously and the method returns
     * immediately. Clearcase errors are notified. 
     * 
     * @param command command to execute
     * @param displayName creates a progress handle with the given display name
     * @return RequestProcessor.Task
     */    
    public RequestProcessor.Task post(String displayName, final ClearcaseCommand command) {
        ProgressSupport ps = new ProgressSupport(rp, displayName) {            
            @Override
            protected void perform() {
                execImpl(new ExecutionUnit(command), true, this);
            }   
        };
        return ps.start();
    }
    
    /**
     * Execute a command synchronously, command execution and notification happens synchronously. 
     * 
     * @param command command to execute
     * @param notifyErrors notifies errors is true     
     */
    public void exec(ClearcaseCommand command, boolean notifyErrors) {        
        exec(new ExecutionUnit(command), notifyErrors);
    }
    
    /**
     * Execute commands synchronously, command execution and notification happens synchronously
     * 
     * @param eu commands to execute     
     * @param notifyErrors notifies errors is true          
     */
    public void exec(ExecutionUnit eu, boolean notifyErrors) {           
        execImpl(eu, notifyErrors, null);
    }        

    /**
     * Execute command synchronously, command execution and notification happens synchronously
     * immediately.  
     * 
     * @param command command to execute
     * @param notifyErrors notifies errors is true
     * @return CommandRunnable
     */       
    public void exec(ClearcaseCommand command, boolean notifyErrors, ProgressSupport ps) {
        execImpl(new ExecutionUnit(command), notifyErrors, ps);        
    }

    private synchronized void execImpl(ExecutionUnit eu, boolean notifyErrors, ProgressSupport ps) {
        if(notifiedUnavailable) return;
        CommandRunnable commandRunnable = new CommandRunnable(eu, notifyErrors);
        if(ps != null) {
            ps.setCancellableDelegate(commandRunnable);
        }
        commandRunnable.run();
        if(ps != null) {
            ps.setCancellableDelegate(null);
        }
    }
    
    /**
     * Execute a clearcase command, but do not block other commands from execution. Use this call for launching
     * graphical clearcase processes such as History Browser. 
     * 
     * @param cmd command to execute
     * @throws org.netbeans.modules.clearcase.ClearcaseException if the command is invalid, its execution fails, etc.
     */
    public void execAsync(final ClearcaseCommand cmd) throws IOException {
        final Cleartool ctshell = new Cleartool();
        RequestProcessor.Task rptask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                try {
                    // it happens (describe -graphical) that the
                    // cleartools magic prompt closes the external 
                    // commands window
                    ctshell.setFireAndForget(true); 
                    ctshell.exec(cmd);
                } catch (Exception e) {
                    Utils.logError(this, e);
                } finally {
                    try {
                        ctshell.quit();
                    } catch (Exception e) {
                        Utils.logWarn(this, e);
                    }
                }
            }
        });
        rptask.schedule(0);
    }    
    
    private class CommandRunnable implements Runnable, Cancellable, Action {
        
        private final ExecutionUnit eu;
        private boolean             canceled;
        private final boolean       notifyErrors;

        public CommandRunnable(ExecutionUnit eu, boolean notifyErrors) {
            this.eu = eu;
            this.notifyErrors = notifyErrors;
        }

        public void run() {            
            if (canceled) return;
            try {
                ensureCleartool();
                for (ClearcaseCommand command : eu) {
                    if (canceled) break;
                    try {
                        ct.exec(command);
                    } catch (Exception e) {                        
                        command.setException(e);
                        eu.setFailedCommand(command);
                        break;
                    }                                        
                    if (command.hasFailed()) {
                        eu.setFailedCommand(command);
                        break;
                    }
                }
            } catch (Exception e) {
                // let the first command fail
                ClearcaseCommand firstCommand = eu.iterator().next();
                firstCommand.setException(e);
                eu.setFailedCommand(firstCommand);
            } finally {
                if(singleUse) {
                    ct.interrupt();
                }
            }
            if (eu.getFailedCommand() != null) {
                handleCommandError(notifyErrors);
            }
        }

        /**
         * Logs and notifies CC errrors.
         * 
         * @param notifyErrors Pops up a dialog that notifies the user that clearcase command failed.        
         */
        private void handleCommandError(boolean notifyErrors) {
            final List<String> errors = new ArrayList<String>(100);
            
            Exception exception = eu.getFailedCommand().getThrownException();
            if (exception != null) {
                if (exception instanceof ClearcaseUnavailableException) {
                    if (!notifiedUnavailable) {
                        Logger.getLogger(ClearcaseClient.class.getName()).log(Level.FINE, "Clearcase module cannot be initialized: 'cleartool' executable not found or couldn't be initialized.", exception);
                        notifiedUnavailable = true;
                    }
                    return;
                }
                errors.add(exception.getMessage());
                Utils.logWarn(this, exception);
            }
            
            errors.addAll(eu.getFailedCommand().getCmdError());
            
            StringBuffer sb = new StringBuffer();
            sb.append(NbBundle.getMessage(ClearcaseClient.class, "MSG_Clearcase_Command_Failure")); //NOI18N
            sb.append(eu.getFailedCommand());
            for (String err : errors) {
                sb.append('\n');                
                sb.append(err);
            }   
            Clearcase.LOG.log(Level.INFO, null, new ClearcaseException(sb.toString()));
            
            if(notifyErrors) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        report(
                            NbBundle.getMessage(ClearcaseClient.class, "Report_ClearcaseCommandFailure_Title"), 
                            NbBundle.getMessage(ClearcaseClient.class, "Report_ClearcaseCommandFailure_Prompt"), 
                            filterSuppressedMessages(errors), 
                            NotifyDescriptor.ERROR_MESSAGE); //NOI18N
                    }
                });
            }
        }
        
        private List<String> filterSuppressedMessages(List<String> messages) {
            String suppressedFile = System.getProperty("org.netbeans.modules.clearcase.suppressMessages");
            if(suppressedFile == null || suppressedFile.trim().equals("")) {
                return messages;
            }
            if(suppressedMessages == null) {
                suppressedMessages = new ArrayList<Pattern>(1);
                BufferedReader fr = null;
                try {
                    File f = new File(suppressedFile);
                    fr = new BufferedReader(new FileReader(f));
                    String line;
                    while( (line = fr.readLine()) != null) {
                        suppressedMessages.add(Pattern.compile(line));
                    }
                } catch (Exception ex) {
                    Clearcase.LOG.log(Level.INFO, "Could not open suppressedMessages file " + suppressedFile, ex);
                    System.setProperty("org.netbeans.modules.clearcase.suppressMessages", ""); // do not try again
                    return messages;
                } finally {
                    try { if(fr != null) fr.close(); } catch (IOException ex) { /*ignore*/ }
                }                
            }
            List<String> ret = new ArrayList<String>();
            for (String msg : messages) {
                if(msg == null) {
                    continue;
                }
                boolean matches = false;
                for (Pattern p : suppressedMessages) {
                    if(p.matcher(msg).matches()) {
                        matches = true;
                        break; 
                    }
                }
                if(!matches) {
                    ret.add(msg);
                }
            }
            return ret;
        }
                
        private void report(String title, String prompt, List<String> messages, int type) {
            boolean emptyReport = true;
            for (String message : messages) {
                if (message != null && message.length() > 0) {
                    emptyReport = false;
                    break;
                }
            }
            if (emptyReport) return;
            
            CommandReport report = new CommandReport(prompt, messages);
            JButton ok = new JButton(NbBundle.getMessage(ClearcaseClient.class, "CommandReport_OK")); //NOI18N
            NotifyDescriptor descriptor = new NotifyDescriptor(
                    report, 
                    title, 
                    NotifyDescriptor.DEFAULT_OPTION,
                    type,
                    new Object [] { ok },
                    ok);
            DialogDisplayer.getDefault().notify(descriptor);
        }
        
        public boolean isCanceled() {
            return canceled;
        }

        public boolean cancel() {
            canceled = true;
            ct.interrupt();
            return true;
        }

        public Object getValue(String key) {
            return null;
        }

        public void putValue(String key, Object value) {
        }

        public void setEnabled(boolean b) {
        }

        public boolean isEnabled() {
            return false;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public void actionPerformed(ActionEvent e) {
        }
    }
    
    public void shutdown() {
        shutdownCleartool();
    }
    
    private void shutdownCleartool() {
        if (ct != null && ct.isValid()) {
            try {
                ct.quit();
            } catch (Exception e) {
                Utils.logFine(this, e);
            }
        }
    }

    private void ensureCleartool() throws ClearcaseException, IOException {
        if (ct == null || !ct.isValid() || singleUse) {
            try {
                ct = new Cleartool();
            } catch (IOException e) {
                throw new ClearcaseUnavailableException(e);
            }
        }
    }
}
