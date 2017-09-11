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
