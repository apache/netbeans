/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.debugger.jpda;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ThreadsCollector;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.WeakListeners;

/**
 *
 * @author martin
 */
public class ThreadsCollectorImpl extends ThreadsCollector {
    
    private JPDADebuggerImpl debugger;
    
    private PropertyChangeListener changesInThreadsListener;
    private final Map<JPDAThread, ThreadStateListener> threadStateListeners = new WeakHashMap<JPDAThread, ThreadStateListener>();
    private final List<JPDAThread> threads = new ArrayList<JPDAThread>();

    public ThreadsCollectorImpl(JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        List<JPDAThread> allThreads = debugger.getAllThreads();
        synchronized (threads) {
            threads.addAll(allThreads);
        }
        changesInThreadsListener = new ChangesInThreadsListener();
        debugger.addPropertyChangeListener(WeakListeners.propertyChange(changesInThreadsListener, debugger));
        for (JPDAThread thread : allThreads) {
            watchThread(thread);
        }
    }

    @Override
    public List<JPDAThread> getAllThreads() {
        synchronized (threads) {
            return Collections.unmodifiableList(new ArrayList(threads));
        }
    }

    @Override
    public DeadlockDetector getDeadlockDetector() {
        return debugger.getDeadlockDetector();
    }
    
    private void watchThread(JPDAThread thread) {
        synchronized (threadStateListeners) {
            if (!threadStateListeners.containsKey(thread)) {
                threadStateListeners.put(thread, new ThreadStateListener(thread));
            }
        }
    }

    public boolean isSomeThreadRunning() {
        for (JPDAThread thread : getAllThreads()) {
            if (!thread.isSuspended() && !((JPDAThreadImpl) thread).isMethodInvoking()) {
                return true;
            }
        }
        return false;
    }

    public boolean isSomeThreadSuspended() {
        for (JPDAThread thread : getAllThreads()) {
            if (thread.isSuspended() || ((JPDAThreadImpl) thread).isMethodInvoking()) {
                return true;
            }
        }
        return false;
    }

    private class ThreadStateListener implements PropertyChangeListener {
        
        //private JPDAThread thread;
        
        public ThreadStateListener(JPDAThread thread) {
            //this.thread = thread;
            ((JPDAThreadImpl) thread).addPropertyChangeListener(WeakListeners.propertyChange(this, thread));
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (JPDAThread.PROP_SUSPENDED.equals(evt.getPropertyName())) {
                if ("methodInvoke".equals(evt.getPropagationId())) {
                    return ; // Ignore events associated with method invocations
                }
                JPDAThread thread = (JPDAThread) evt.getSource();
                if (thread.isSuspended()) {
                    firePropertyChange(PROP_THREAD_SUSPENDED, null, thread);
                } else {
                    firePropertyChange(PROP_THREAD_RESUMED, null, thread);
                }
            }
        }
        
    }
    
    private class ChangesInThreadsListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (JPDADebugger.PROP_THREAD_STARTED.equals(propertyName)) {
                JPDAThread thread = (JPDAThread) evt.getNewValue();
                watchThread(thread);
                synchronized (threads) {
                    if (!threads.contains(thread)) {
                        threads.add(thread); // Could be already added in constructor...
                    }
                }
                firePropertyChange(PROP_THREAD_STARTED, evt.getOldValue(), evt.getNewValue());
            } else if (JPDADebugger.PROP_THREAD_DIED.equals(propertyName)) {
                JPDAThread thread = (JPDAThread) evt.getOldValue();
                synchronized (threads) {
                    threads.remove(thread);
                }
                firePropertyChange(PROP_THREAD_DIED, evt.getOldValue(), evt.getNewValue());
            } else if (JPDADebugger.PROP_THREAD_GROUP_ADDED.equals(propertyName)) {
                firePropertyChange(PROP_THREAD_GROUP_ADDED, evt.getOldValue(), evt.getNewValue());
            }
        }
        
    }
}
