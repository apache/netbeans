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

package org.netbeans.modules.debugger.jpda.ui.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.DeadlockDetectorImpl;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * @author Daniel Prusa
 */
public class CheckDeadlocksAction extends AbstractAction
                                  implements Runnable {

    private final EnableListener listener;
    
    public CheckDeadlocksAction () {
        listener = new EnableListener (this);
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                listener);
        putValue (NAME, getDisplayName());
        checkEnabled();
    }

    @NbBundle.Messages("CTL_CheckDeadlocks=Chec&k for Deadlock")
    public static String getDisplayName() {
        return Bundle.CTL_CheckDeadlocks();
    }
    
    @Override
    public void actionPerformed (ActionEvent evt) {
        DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (de == null) return;
        final JPDADebuggerImpl debugger = (JPDADebuggerImpl) de.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) return;
        debugger.getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                checkForDeadlock(debugger);
            }
        });
    }

    @NbBundle.Messages("CTL_No_Deadlock=No deadlock found.")
    public static void checkForDeadlock(JPDADebuggerImpl debugger) {
        if (debugger.getState() == JPDADebuggerImpl.STATE_DISCONNECTED) {
            return;
        }
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) {
            return ;
        }
        try {
            VirtualMachineWrapper.suspend(vm);
            List<JPDAThreadImpl> threadsToNotify = new ArrayList<JPDAThreadImpl>();
            for (ThreadReference threadRef : VirtualMachineWrapper.allThreads(vm)) {
                try {
                    if (ThreadReferenceWrapper.suspendCount(threadRef) == 1) {
                        JPDAThreadImpl jpdaThread = debugger.getThread(threadRef);
                        jpdaThread.notifySuspended();
                        threadsToNotify.add(jpdaThread);
                    }
                } catch (ObjectCollectedExceptionWrapper e) {
                } catch (IllegalThreadStateExceptionWrapper e) {
                }
            }
            DeadlockDetector detector = debugger.getThreadsCollector().getDeadlockDetector();
            try {
                ((DeadlockDetectorImpl) detector).waitForUnfinishedTasks(5000);
            } catch (InterruptedException ex) {}
            Set dealocks = detector.getDeadlocks();
            if (dealocks == null || dealocks.isEmpty()) {
                String msg = Bundle.CTL_No_Deadlock();
                NotifyDescriptor desc = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                for (JPDAThreadImpl thread : threadsToNotify) {
                    thread.notifyToBeResumed();
                }
                VirtualMachineWrapper.resume(vm);
            }
        } catch (InternalExceptionWrapper e) {
        } catch (VMDisconnectedExceptionWrapper e) {
        }
    }
    
    private synchronized boolean canBeEnabled() {
        DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (de == null) return false;
        JPDADebugger debugger = de.lookupFirst(null, JPDADebugger.class);
        return debugger != null;
    }
    
    private void checkEnabled() {
        SwingUtilities.invokeLater(this);
    }
    
    @Override
    public void run() {
        setEnabled(canBeEnabled());
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            DebuggerManager.getDebuggerManager().removeDebuggerListener(
                    DebuggerManager.PROP_CURRENT_ENGINE,
                    listener);
        } finally {
            super.finalize();
        }
    }

        
    private static class EnableListener extends DebuggerManagerAdapter {
        
        private final Reference<CheckDeadlocksAction> actionRef;
        
        public EnableListener(CheckDeadlocksAction action) {
            actionRef = new WeakReference<CheckDeadlocksAction>(action);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            CheckDeadlocksAction action = actionRef.get();
            if (action != null) {
                action.checkEnabled();
            }
        }
        
    }
    
}
