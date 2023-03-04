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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;

import com.sun.jdi.VMDisconnectedException;
import java.util.Arrays;
import java.util.List;

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadGroupReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.openide.util.Exceptions;


/**
 * The implementation of JPDAThreadGroup.
 */
public class JPDAThreadGroupImpl implements JPDAThreadGroup {

    private ThreadGroupReference tgr;
    private JPDADebuggerImpl debugger;
    private String name;
    
    public JPDAThreadGroupImpl (ThreadGroupReference tgr, JPDADebuggerImpl debugger) {
        this.tgr = tgr;
        this.debugger = debugger;
        name = "";
        try {
            name = ThreadGroupReferenceWrapper.name(tgr);
        } catch (InternalExceptionWrapper de) {
        } catch (VMDisconnectedExceptionWrapper de) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        }
        debugger.interestedInThreadGroup(this);
    }

    /**
    * Returns parent thread group.
    *
    * @return parent thread group.
    */
    public JPDAThreadGroupImpl getParentThreadGroup () {
        ThreadGroupReference ptgr = null;
        try {
            ptgr = ThreadGroupReferenceWrapper.parent(tgr);
        } catch (InternalExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
        } catch (VMDisconnectedExceptionWrapper e) {
        }
        if (ptgr == null) return null;
        return (JPDAThreadGroupImpl) debugger.getThreadGroup(ptgr);
    }
    
    public JPDAThreadImpl[] getThreads () {
        ThreadsCache tc = debugger.getThreadsCache();
        if (tc == null) {
            return new JPDAThreadImpl[0];
        }
        List<ThreadReference> l = tc.getThreads(tgr);
        int i, k = l.size ();
        JPDAThreadImpl[] ts = new JPDAThreadImpl[k];
        i = 0;
        for (ThreadReference t : l) {
            JPDAThreadImpl thread = debugger.getThread(t);
            if (thread.getName().contains(ThreadsCache.THREAD_NAME_FILTER_PATTERN)) {
                ts = Arrays.copyOf(ts, k - 1);
            } else {
                ts[i++] = thread;
            }
        }
        return ts;
    }
    
    public JPDAThreadGroupImpl[] getThreadGroups () {
        ThreadsCache tc = debugger.getThreadsCache();
        if (tc == null) {
            return new JPDAThreadGroupImpl[0];
        }
        List<ThreadGroupReference> l = tc.getGroups(tgr);
        int i, k = l.size ();
        JPDAThreadGroupImpl[] ts = new JPDAThreadGroupImpl[k];
        for (i = 0; i < k; i++) {
            ts [i] = (JPDAThreadGroupImpl) debugger.getThreadGroup(l.get (i));
        }
        return ts;
    }
    
    public String getName () {
        return name;
    }
    
    // XXX Add some synchronization so that the threads can not be resumed at any time
    public void resume () {
        ThreadsCache tc = debugger.getThreadsCache();
        if (tc == null) {
            return ;
        }
        notifyToBeResumed(tc);
        try {
            ThreadGroupReferenceWrapper.resume(tgr);
        } catch (InternalExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    // XXX Add some synchronization
    public void suspend () {
        ThreadsCache tc = debugger.getThreadsCache();
        if (tc == null) {
            return ;
        }
        try {
            ThreadGroupReferenceWrapper.suspend(tgr);
        } catch (InternalExceptionWrapper ex) {
            return ;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return ;
        } catch (ObjectCollectedExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
            return ;
        }
        notifySuspended(tc);
    }
    
    void notifyToBeResumed(ThreadsCache tc) {
        List<ThreadReference> threads = tc.getThreads(tgr);
        for (ThreadReference threadRef : threads) {
            JPDAThreadImpl thread = debugger.getThread(threadRef);
            thread.notifyToBeResumed();
        }
        List<ThreadGroupReference> groups = tc.getGroups(tgr);
        for (ThreadGroupReference groupRef : groups) {
            JPDAThreadGroupImpl group = (JPDAThreadGroupImpl) debugger.getThreadGroup(groupRef);
            group.notifyToBeResumed(tc);
        }
    }
    
    void notifySuspended(ThreadsCache tc) {
        List<ThreadReference> threads = tc.getThreads(tgr);
        for (ThreadReference threadRef : threads) {
            JPDAThreadImpl thread = debugger.getThread(threadRef);
            thread.notifySuspended();
        }
        List<ThreadGroupReference> groups = tc.getGroups(tgr);
        for (ThreadGroupReference groupRef : groups) {
            JPDAThreadGroupImpl group = (JPDAThreadGroupImpl) debugger.getThreadGroup(groupRef);
            group.notifySuspended(tc);
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + "["+name+"] @" + Integer.toHexString(hashCode());
    }

}
